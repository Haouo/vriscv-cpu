package myImplement.PipelineCPU.Datapath

import chisel3._
import chisel3.util._

import myImplement.Memory.{DataMemoryIO, InstMemoryIO}
import myImplement.PipelineCPU.utilFunctions._
import myImplement.PipelineCPU.Controller_DatapathIO
import myImplement.PipelineCPU.Controller.ControllerState._
import myImplement.PipelineCPU.Control._
import myImplement.ModuleIF.Wrapper_to_CPU_IO_from_Datapath

class DatapathIO(memAddrWidth: Int, memDataWidth: Int) extends Bundle {
  val datapath_controller_io = Flipped(new Controller_DatapathIO(memDataWidth))
  val datapath_instMem_io    = Flipped(new InstMemoryIO(memAddrWidth))
  // for wrapper support
  val datapath_to_wrapper = Flipped(new Wrapper_to_CPU_IO_from_Datapath(memAddrWidth, memDataWidth))

  // test ports
  val regs        = Output(Vec(32, UInt(32.W)))
  val vector_regs = Output(Vec(32, UInt(512.W)))
  val IF_pc       = Output(UInt(32.W))
  val ID_pc       = Output(UInt(32.W))
  val EXE_pc      = Output(UInt(32.W))
  val MEM_pc      = Output(UInt(32.W))
  val WB_pc       = Output(UInt(32.W))
  val EXE_src1    = Output(UInt(32.W))
  val EXE_src2    = Output(UInt(32.W))
  val ALU_src1    = Output(UInt(32.W))
  val ALU_src2    = Output(UInt(32.W))
  val EXE_alu_out = Output(UInt(32.W))
  val WB_rd       = Output(UInt(5.W))
  val WB_wdata    = Output(UInt(32.W))
}

class Datapath(memAddrWidth: Int, memDataWidth: Int) extends Module {
  val io = IO(new DatapathIO(memAddrWidth, memDataWidth))

  // * Modules * //
  val imme_gen    = Module(new ImmeGen)
  val reg_file    = Module(new RegFile)
  val alu         = Module(new ALU)
  val branch_comp = Module(new BranchComp)
  val ld_filter   = Module(new LD_Filter)
  val vrf         = Module(new VectorRegFile) // for vector
  val valu        = Module(new VectorALU)     // for vector
  // * Pipeline Regs * //
  val IF_reg  = Module(new IF_PipeReg)
  val ID_reg  = Module(new ID_PipeReg)
  val EXE_reg = Module(new EXE_PipeReg)
  val MEM_reg = Module(new MEM_PipeReg)
  val WB_reg  = Module(new WB_PipeReg)
  // * Wires * //
  val current_pc        = WireDefault(IF_reg.io.currentPC)
  val current_pc_plus_4 = WireDefault(current_pc + 4.U)
  val writeBackData     = Wire(UInt(32.W))
  val v_writeVackData   = Wire(UInt(512.W)) // for vector

  // * IF Stage * //
  IF_reg.io.stall    := io.datapath_controller_io.IF_stall
  IF_reg.io.stall_MA := io.datapath_controller_io.stall_memory_access
  IF_reg.io.nextPC := Mux(
    io.datapath_controller_io.IF_next_pc_sel === PC_sel.sel_IF_pc_plue_4,
    current_pc_plus_4,
    alu.io.aluOut
  )
  io.datapath_instMem_io.addr := current_pc(memAddrWidth - 1, 0)

  // * ID Stage * //
  ID_reg.io.stall    := io.datapath_controller_io.ID_stall
  ID_reg.io.stall_MA := io.datapath_controller_io.stall_memory_access
  ID_reg.io.flush    := io.datapath_controller_io.ID_flush
  ID_reg.io.inst_in  := io.datapath_instMem_io.inst
  ID_reg.io.pc_in    := current_pc

  reg_file.io.rs1_index := get_rs1_index(ID_reg.io.inst_out)
  reg_file.io.rs2_index := get_rs2_index(ID_reg.io.inst_out)
  reg_file.io.rd_index  := get_rd_index(WB_reg.io.inst_out)
  reg_file.io.wEnable   := io.datapath_controller_io.WB_wEnable
  reg_file.io.writeData := writeBackData

  // for vector
  vrf.io.wEnable   := io.datapath_controller_io.WB_vreg_wEnable
  vrf.io.writeData := v_writeVackData
  vrf.io.vs1_index := get_vs1_index(ID_reg.io.inst_out)
  vrf.io.vs2_index := get_vs2_index(ID_reg.io.inst_out)
  vrf.io.vd_index  := get_vd_index(WB_reg.io.inst_out)

  io.datapath_controller_io.ID_inst := ID_reg.io.inst_out

  imme_gen.io.inst := ID_reg.io.inst_out

  // * EXE Stage * //
  EXE_reg.io.stall       := io.datapath_controller_io.EXE_stall
  EXE_reg.io.stall_MA    := io.datapath_controller_io.stall_memory_access
  EXE_reg.io.flush       := io.datapath_controller_io.EXE_flush
  EXE_reg.io.inst_in     := ID_reg.io.inst_out
  EXE_reg.io.pc_in       := ID_reg.io.pc_out
  EXE_reg.io.rs1_data_in := reg_file.io.rs1_data
  EXE_reg.io.rs2_data_in := reg_file.io.rs2_data
  EXE_reg.io.imme_in     := imme_gen.io.imme
  // for vector
  EXE_reg.io.vs1_data_in := vrf.io.vs1_data
  EXE_reg.io.vs2_data_in := vrf.io.vs2_data

  io.datapath_controller_io.EXE_inst := EXE_reg.io.inst_out

  alu.io.aluOP := io.datapath_controller_io.EXE_alu_op
  alu.io.op1 := Mux(
    io.datapath_controller_io.EXE_alu_src1_sel === ALU_src1_sel.sel_PC,
    EXE_reg.io.pc_out,
    EXE_reg.io.rs1_data_out
  )
  alu.io.op2 := Mux(
    io.datapath_controller_io.EXE_alu_src2_sel === ALU_src2_sel.sel_Imme,
    EXE_reg.io.imme_out,
    EXE_reg.io.rs2_data_out
  )
  // for vector
  valu.io.aluOP := io.datapath_controller_io.EXE_VALU_op
  valu.io.src1 := Mux(
    io.datapath_controller_io.EXE_VALU_src1_sel === VALU_src1_sel.sel_rs1,
    VecInit(Seq.fill(512 / 8)(EXE_reg.io.rs1_data_out(7, 0))).asTypeOf(UInt(512.W)),
    EXE_reg.io.vs1_data_out
  )
  valu.io.src2 := EXE_reg.io.vs2_data_out

  branch_comp.io.BrUn                := io.datapath_controller_io.EXE_BrUn
  branch_comp.io.op1                 := EXE_reg.io.rs1_data_out
  branch_comp.io.op2                 := EXE_reg.io.rs2_data_out
  io.datapath_controller_io.EXE_BrEq := branch_comp.io.BrEq
  io.datapath_controller_io.EXE_BrLT := branch_comp.io.BrLT

  // * MEM Stage * //
  val temp_data_reg            = RegInit(VecInit(Seq.fill(16)(0.U(32.W)))) // for burst mode load data
  val MEM_vs2_data_vector_type = MEM_reg.io.vs2_data_out.asTypeOf(Vec(16, UInt(32.W)))

  MEM_reg.io.stall       := io.datapath_controller_io.MEM_stall
  MEM_reg.io.stall_MA    := io.datapath_controller_io.stall_memory_access
  MEM_reg.io.inst_in     := EXE_reg.io.inst_out
  MEM_reg.io.pc_in       := EXE_reg.io.pc_out
  MEM_reg.io.rs2_data_in := EXE_reg.io.rs2_data_out
  MEM_reg.io.aluOut_in   := alu.io.aluOut
  // for vector
  MEM_reg.io.vs2_data_in := EXE_reg.io.vs2_data_out
  MEM_reg.io.v_aluOut_in := valu.io.aluOut

  io.datapath_controller_io.MEM_inst := MEM_reg.io.inst_out

  // about read/write addr
  io.datapath_to_wrapper.baseAddr := MEM_reg.io.aluOut_out(memAddrWidth - 1, 0)
  // about read data
  temp_data_reg(io.datapath_to_wrapper.countValue) := Mux(
    io.datapath_to_wrapper.readData.valid,
    io.datapath_to_wrapper.readData.bits,
    temp_data_reg(io.datapath_to_wrapper.countValue)
  )
  // about write data
  io.datapath_to_wrapper.writeData.valid := Mux(
    io.datapath_controller_io.controller_state === sWriteSend,
    true.B,
    false.B
  )
  io.datapath_to_wrapper.writeData.bits := MEM_vs2_data_vector_type(io.datapath_to_wrapper.countValue)

  // * WB Stage * //
  WB_reg.io.stall      := io.datapath_controller_io.WB_stall
  WB_reg.io.stall_MA   := io.datapath_controller_io.stall_memory_access
  WB_reg.io.pc_in      := MEM_reg.io.pc_out
  WB_reg.io.inst_in    := MEM_reg.io.inst_out
  WB_reg.io.aluOut_in  := MEM_reg.io.aluOut_out
  WB_reg.io.ld_data_in := temp_data_reg(0)
  // for vector
  WB_reg.io.v_aluOut_in  := MEM_reg.io.v_aluOut_out
  WB_reg.io.v_ld_data_in := temp_data_reg.asTypeOf(UInt(512.W))

  io.datapath_controller_io.WB_inst := WB_reg.io.inst_out

  ld_filter.io.WB_func3 := get_func3(WB_reg.io.inst_out)
  ld_filter.io.inData   := WB_reg.io.ld_data_out

  writeBackData := Mux(
    io.datapath_controller_io.WB_wb_sel === WB_sel_control.sel_alu_out,
    WB_reg.io.aluOut_out,
    Mux(
      io.datapath_controller_io.WB_wb_sel === WB_sel_control.sel_pc_plue_4,
      WB_reg.io.pc_out + 4.U,
      ld_filter.io.outData
    )
  )
  v_writeVackData := Mux(
    io.datapath_controller_io.WB_v_wb_sel === WB_v_sel_control.sel_v_ld_data,
    WB_reg.io.ld_data_out, // for vector load
    WB_reg.io.v_aluOut_out // for vadd.vv or vmul.vx
  )

  // * test ports * //
  io.regs        := reg_file.io.regs
  io.vector_regs := vrf.io.v_regs
  io.IF_pc       := IF_reg.io.currentPC
  io.ID_pc       := ID_reg.io.pc_out
  io.EXE_pc      := EXE_reg.io.pc_out
  io.MEM_pc      := MEM_reg.io.pc_out
  io.WB_pc       := WB_reg.io.pc_out
  io.EXE_src1    := EXE_reg.io.rs1_data_out
  io.EXE_src2    := EXE_reg.io.rs2_data_out
  io.ALU_src1    := alu.io.op1
  io.ALU_src2    := alu.io.op2
  io.EXE_alu_out := alu.io.aluOut
  io.WB_rd       := get_rd_index(WB_reg.io.inst_out)
  io.WB_wdata    := writeBackData
}
