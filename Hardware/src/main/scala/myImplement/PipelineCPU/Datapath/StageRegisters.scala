package myImplement.PipelineCPU.Datapath

import chisel3._
import chisel3.util._

import myImplement.PipelineCPU.Control.nop

class IF_PipeReg extends Module {
  val io = IO(new Bundle {
    // control
    val stall    = Input(Bool())
    val stall_MA = Input(Bool())
    // data
    val nextPC    = Input(UInt(32.W))
    val currentPC = Output(UInt(32.W))
  })

  val PC = RegInit(0.U(32.W))

  PC           := Mux(io.stall | io.stall_MA, PC, io.nextPC)
  io.currentPC := PC
}

class ID_PipeReg extends Module {
  val io = IO(new Bundle {
    // control
    val stall    = Input(Bool())
    val stall_MA = Input(Bool())
    val flush    = Input(Bool())
    // data
    val pc_in    = Input(UInt(32.W))
    val pc_out   = Output(UInt(32.W))
    val inst_in  = Input(UInt(32.W))
    val inst_out = Output(UInt(32.W))
  })

  // regs
  val pc   = Reg(UInt(32.W))
  val inst = Reg(UInt(32.W))

  // stall & flush control
  pc := Mux(
    io.stall_MA, // first priority
    pc,
    Mux(io.flush, 0.U, Mux(io.stall, pc, io.pc_in))
  )
  inst := Mux(
    io.stall_MA, // first priority
    inst,
    Mux(io.flush, nop, Mux(io.stall, inst, io.inst_in))
  )

  // output
  io.pc_out   := pc
  io.inst_out := inst
}

class EXE_PipeReg extends Module {
  val io = IO(new Bundle {
    // control
    val stall    = Input(Bool())
    val stall_MA = Input(Bool())
    val flush    = Input(Bool())
    // data
    val pc_in        = Input(UInt(32.W))
    val pc_out       = Output(UInt(32.W))
    val inst_in      = Input(UInt(32.W))
    val inst_out     = Output(UInt(32.W))
    val rs1_data_in  = Input(UInt(32.W))
    val rs1_data_out = Output(UInt(32.W))
    val rs2_data_in  = Input(UInt(32.W))
    val rs2_data_out = Output(UInt(32.W))
    val imme_in      = Input(UInt(32.W))
    val imme_out     = Output(UInt(32.W))
    // for vector
    val vs1_data_in  = Input(UInt(512.W))
    val vs1_data_out = Output(UInt(512.W))
    val vs2_data_in  = Input(UInt(512.W))
    val vs2_data_out = Output(UInt(512.W))
  })

  // regs
  val pc       = Reg(UInt(32.W))
  val inst     = Reg(UInt(32.W))
  val rs1_data = Reg(UInt(32.W))
  val rs2_data = Reg(UInt(32.W))
  val imme     = Reg(UInt(32.W))
  val vs1_data = Reg(UInt(512.W))
  val vs2_data = Reg(UInt(512.W))

  // stall & flush control
  pc       := Mux(io.stall_MA, pc, Mux(io.flush, 0.U, Mux(io.stall, pc, io.pc_in)))
  inst     := Mux(io.stall_MA, inst, Mux(io.flush, nop, Mux(io.stall, inst, io.inst_in)))
  rs1_data := Mux(io.stall_MA, rs1_data, Mux(io.flush, 0.U, Mux(io.stall, rs1_data, io.rs1_data_in)))
  rs2_data := Mux(io.stall_MA, rs2_data, Mux(io.flush, 0.U, Mux(io.stall, rs2_data, io.rs2_data_in)))
  imme     := Mux(io.stall_MA, imme, Mux(io.flush, 0.U, Mux(io.stall, imme, io.imme_in)))
  // for vector
  vs1_data := Mux(io.stall_MA, vs1_data, Mux(io.flush, 0.U, Mux(io.stall, vs1_data, io.vs1_data_in)))
  vs2_data := Mux(io.stall_MA, vs2_data, Mux(io.flush, 0.U, Mux(io.stall, vs2_data, io.vs2_data_in)))

  // output
  io.pc_out       := pc
  io.inst_out     := inst
  io.rs1_data_out := rs1_data
  io.rs2_data_out := rs2_data
  io.imme_out     := imme
  // for vector
  io.vs1_data_out := vs1_data
  io.vs2_data_out := vs2_data
}

class MEM_PipeReg extends Module {
  val io = IO(new Bundle {
    // control
    val stall    = Input(Bool())
    val stall_MA = Input(Bool())
    // data
    val pc_in        = Input(UInt(32.W))
    val pc_out       = Output(UInt(32.W))
    val inst_in      = Input(UInt(32.W))
    val inst_out     = Output(UInt(32.W))
    val aluOut_in    = Input(UInt(32.W))
    val aluOut_out   = Output(UInt(32.W))
    val rs2_data_in  = Input(UInt(32.W))
    val rs2_data_out = Output(UInt(32.W))
    // for vector
    val v_aluOut_in  = Input(UInt(512.W))
    val v_aluOut_out = Output(UInt(512.W))
    val vs2_data_in  = Input(UInt(512.W))
    val vs2_data_out = Output(UInt(512.W))
  })

  // regs
  val pc       = Reg(UInt(32.W))
  val inst     = Reg(UInt(32.W))
  val aluOut   = Reg(UInt(32.W))
  val rs2_data = Reg(UInt(32.W))
  val v_aluOut = Reg(UInt(512.W))
  val vs2_data = Reg(UInt(512.W))

  // stall control
  pc       := Mux(io.stall | io.stall_MA, pc, io.pc_in)
  inst     := Mux(io.stall | io.stall_MA, inst, io.inst_in)
  aluOut   := Mux(io.stall | io.stall_MA, aluOut, io.aluOut_in)
  rs2_data := Mux(io.stall | io.stall_MA, rs2_data, io.rs2_data_in)
  // for vector
  v_aluOut := Mux(io.stall | io.stall_MA, v_aluOut, io.v_aluOut_in)
  vs2_data := Mux(io.stall | io.stall_MA, vs2_data, io.vs2_data_in)

  // output
  io.pc_out       := pc
  io.inst_out     := inst
  io.aluOut_out   := aluOut
  io.rs2_data_out := rs2_data
  // for vector
  io.v_aluOut_out := v_aluOut
  io.vs2_data_out := vs2_data
}

class WB_PipeReg extends Module {
  val io = IO(new Bundle {
    // control
    val stall    = Input(Bool())
    val stall_MA = Input(Bool())
    // data
    val pc_in       = Input(UInt(32.W))
    val pc_out      = Output(UInt(32.W))
    val inst_in     = Input(UInt(32.W))
    val inst_out    = Output(UInt(32.W))
    val aluOut_in   = Input(UInt(32.W))
    val aluOut_out  = Output(UInt(32.W))
    val ld_data_in  = Input(UInt(32.W))
    val ld_data_out = Output(UInt(32.W))
    // for vector
    val v_aluOut_in   = Input(UInt(512.W))
    val v_aluOut_out  = Output(UInt(512.W))
    val v_ld_data_in  = Input(UInt(512.W))
    val v_ld_data_out = Output(UInt(512.W))
  })

  // regs
  val pc      = Reg(UInt(32.W))
  val inst    = Reg(UInt(32.W))
  val aluOut  = Reg(UInt(32.W))
  val ld_data = Reg(UInt(32.W))
  // for vector
  val v_aluOut  = Reg(UInt(512.W))
  val v_ld_data = Reg(UInt(512.W))

  // stall control
  pc      := Mux(io.stall | io.stall_MA, pc, io.pc_in)
  inst    := Mux(io.stall | io.stall_MA, inst, io.inst_in)
  aluOut  := Mux(io.stall | io.stall_MA, aluOut, io.aluOut_in)
  ld_data := Mux(io.stall | io.stall_MA, ld_data, io.ld_data_in)
  // for vector
  v_aluOut  := Mux(io.stall | io.stall_MA, v_aluOut, io.v_aluOut_in)
  v_ld_data := Mux(io.stall | io.stall_MA, v_ld_data, io.v_ld_data_in)

  // output
  io.pc_out      := pc
  io.inst_out    := inst
  io.aluOut_out  := aluOut
  io.ld_data_out := ld_data
  // for vector
  io.v_aluOut_out  := v_aluOut
  io.v_ld_data_out := v_ld_data
}
