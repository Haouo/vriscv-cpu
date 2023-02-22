package myImplement.PipelineCPU.Controller

import chisel3._
import chisel3.util._

import myImplement.PipelineCPU.opcode._
import myImplement.PipelineCPU.func3_set._
import myImplement.PipelineCPU.func3_set.Arithmetic_func3._
import myImplement.PipelineCPU.utilFunctions._
import myImplement.PipelineCPU.Control._
import myImplement.PipelineCPU.Controller_DatapathIO

class ControllerIO(memDataWidth: Int) extends Bundle {
  val controller_datapath_io = new Controller_DatapathIO(memDataWidth)
  // test ports
}

class Controller(memDataWidth: Int) extends Module {
  val io = IO(new ControllerIO(memDataWidth))

  // for reducing the code
  val ID_inst  = io.controller_datapath_io.ID_inst
  val EXE_inst = io.controller_datapath_io.EXE_inst
  val MEM_inst = io.controller_datapath_io.MEM_inst
  val WB_inst  = io.controller_datapath_io.WB_inst

  // * terminate signal * //
  io.controller_datapath_io.isHcf := get_op(WB_inst) === HCF

  // * IF Stage * //
  io.controller_datapath_io.IF_next_pc_sel := Mux(
    actual_branch_result,
    PC_sel.sel_EXE_target_pc,
    PC_sel.sel_IF_pc_plue_4
  )

  // * ID Stage * //
  // blank

  // * EXE Stage * //
  io.controller_datapath_io.EXE_BrUn         := get_func3(EXE_inst) === BRANCH_func3.bltu || get_func3(
    EXE_inst
  ) === BRANCH_func3.bgeu
  io.controller_datapath_io.EXE_alu_src1_sel := Mux(
    get_op(EXE_inst) === BRANCH || get_op(EXE_inst) === JAL || get_op(EXE_inst) === JALR,
    ALU_src1_sel.sel_PC,
    ALU_src1_sel.sel_rs1
  )
  io.controller_datapath_io.EXE_alu_src2_sel := Mux(
    get_op(EXE_inst) === OP,
    ALU_src2_sel.sel_rs2,
    ALU_src2_sel.sel_Imme
  )
  io.controller_datapath_io.EXE_alu_op       := MuxLookup(
    get_op(EXE_inst),
    ALU_op.ADD,
    Seq(
      OP     -> MuxLookup(
        get_func3(EXE_inst),
        ALU_op.ADD,
        Seq(
          add_sub -> Mux(get_func7(EXE_inst).asBool, ALU_op.SUB, ALU_op.ADD),
          sll     -> ALU_op.SLL,
          slt     -> ALU_op.SLT,
          sltu    -> ALU_op.SLTU,
          xor     -> ALU_op.XOR,
          srl_sra -> Mux(get_func7(EXE_inst).asBool, ALU_op.SRA, ALU_op.SRL),
          or      -> ALU_op.OR,
          and     -> ALU_op.AND
        )
      ),
      OP_IMM -> MuxLookup(
        get_func3(EXE_inst),
        ALU_op.ADD,
        Seq(
          add_sub -> ALU_op.ADD,
          sll     -> ALU_op.SLL,
          slt     -> ALU_op.SLT,
          sltu    -> ALU_op.SLTU,
          xor     -> ALU_op.XOR,
          srl_sra -> Mux(get_func7(EXE_inst).asBool, ALU_op.SRA, ALU_op.SRL),
          or      -> ALU_op.OR,
          and     -> ALU_op.AND
        )
      ),
      LUI    -> ALU_op.COPY_OP2
    )
  )

  // * MEM Stage * //
  io.controller_datapath_io.MEM_dataMem_wEnable := Mux(
    get_op(MEM_inst) === STORE,
    MuxLookup(
      get_func3(MEM_inst),
      0.U,
      Seq(
        STORE_func3.sb -> "b0001".U,
        STORE_func3.sh -> "b0011".U,
        STORE_func3.sw -> "b1111".U
      )
    ),
    0.U
  )

  // * WB Stage * //
  io.controller_datapath_io.WB_wEnable := Mux(
    get_op(WB_inst) === STORE || get_op(WB_inst) === BRANCH,
    false.B,
    true.B
  )
  io.controller_datapath_io.WB_wb_sel  := Mux(
    get_op(WB_inst) === JAL || get_op(WB_inst) === JALR,
    WB_sel_control.sel_pc_plue_4,
    Mux(get_op(WB_inst) === LOAD, WB_sel_control.sel_ld_filter_data, WB_sel_control.sel_alu_out)
  )

  // * Control Hazard * //
  val actual_branch_result = Wire(Bool())

  actual_branch_result := Mux(
    get_op(EXE_inst) === JAL || get_op(EXE_inst) === JALR,
    true.B,
    Mux(
      get_op(EXE_inst) === BRANCH,
      MuxLookup(
        get_func3(EXE_inst),
        false.B,
        Seq(
          BRANCH_func3.beq  -> io.controller_datapath_io.EXE_BrEq,
          BRANCH_func3.bne  -> ~io.controller_datapath_io.EXE_BrEq,
          BRANCH_func3.blt  -> io.controller_datapath_io.EXE_BrLT,
          BRANCH_func3.bge  -> ~io.controller_datapath_io.EXE_BrLT,
          BRANCH_func3.bltu -> io.controller_datapath_io.EXE_BrLT,
          BRANCH_func3.bgeu -> ~io.controller_datapath_io.EXE_BrLT
        )
      ),
      false.B
    )
  )

  // * Data Hazard * //
  // wires
  val is_ID_use_rs1, is_ID_use_rs2, is_EXE_use_rd, is_MEM_use_rd, is_WB_use_rd = Wire(Bool())
  val is_ID_rs1_EXE_rd_overlap, is_ID_rs2_EXE_rd_overlap                       = Wire(Bool())
  val is_ID_rs1_MEM_rd_overlap, is_ID_rs2_MEM_rd_overlap                       = Wire(Bool())
  val is_ID_rs1_WB_rd_overlap, is_ID_rs2_WB_rd_overlap                         = Wire(Bool())
  val is_ID_EXE_overlap, is_ID_MEM_overlap, is_ID_WB_overlap                   = Wire(Bool())

  is_ID_use_rs1 := Mux(
    get_op(ID_inst) === JAL || get_op(ID_inst) === LUI || get_op(ID_inst) === AUIPC,
    false.B,
    Mux(get_rs1_index(ID_inst) === 0.U, false.B, true.B)
  )
  is_ID_use_rs2 := Mux(
    get_op(ID_inst) === OP || get_op(ID_inst) === STORE || get_op(ID_inst) === BRANCH,
    true.B,
    false.B
  )
  is_EXE_use_rd := Mux(
    get_op(EXE_inst) === STORE || get_op(EXE_inst) === BRANCH,
    false.B,
    Mux(get_rd_index(EXE_inst) === 0.U, false.B, true.B)
  )
  is_MEM_use_rd := Mux(
    get_op(MEM_inst) === STORE || get_op(MEM_inst) === BRANCH,
    false.B,
    Mux(get_rd_index(MEM_inst) === 0.U, false.B, true.B)
  )
  is_WB_use_rd  := Mux(
    get_op(WB_inst) === STORE || get_op(WB_inst) === BRANCH,
    false.B,
    Mux(get_rd_index(WB_inst) === 0.U, false.B, true.B)
  )

  // TODO
  is_ID_rs1_EXE_rd_overlap := is_ID_use_rs1 & is_EXE_use_rd & (get_rs1_index(ID_inst) === get_rd_index(
    (EXE_inst)
  )) & (get_rd_index(EXE_inst) =/= 0.U)
  is_ID_rs2_EXE_rd_overlap := is_ID_use_rs2 & is_EXE_use_rd & (get_rs2_index(ID_inst) === get_rd_index(
    (EXE_inst)
  )) & (get_rd_index(EXE_inst) =/= 0.U)
  is_ID_rs1_MEM_rd_overlap := is_ID_use_rs1 & is_MEM_use_rd & (get_rs1_index(ID_inst) === get_rd_index(
    MEM_inst
  )) & (get_rd_index(MEM_inst) =/= 0.U)
  is_ID_rs2_MEM_rd_overlap := is_ID_use_rs2 & is_MEM_use_rd & (get_rs2_index(ID_inst) === get_rd_index(
    MEM_inst
  )) & (get_rd_index(MEM_inst) =/= 0.U)
  is_ID_rs1_WB_rd_overlap  := is_ID_use_rs1 & is_WB_use_rd & (get_rs1_index(ID_inst) === get_rd_index(
    WB_inst
  )) & (get_rd_index(WB_inst) =/= 0.U)
  is_ID_rs2_WB_rd_overlap  := is_ID_use_rs2 & is_WB_use_rd & (get_rs2_index(ID_inst) === get_rd_index(
    WB_inst
  )) & (get_rd_index(WB_inst) =/= 0.U)

  is_ID_EXE_overlap := is_ID_rs1_EXE_rd_overlap | is_ID_rs2_EXE_rd_overlap
  is_ID_MEM_overlap := is_ID_rs1_MEM_rd_overlap | is_ID_rs2_MEM_rd_overlap
  is_ID_WB_overlap  := is_ID_rs1_WB_rd_overlap | is_ID_rs2_WB_rd_overlap

  // * Pipeline Regs Stall & Flush Control * //
  io.controller_datapath_io.IF_stall  := is_ID_EXE_overlap | is_ID_MEM_overlap | is_ID_WB_overlap
  io.controller_datapath_io.ID_stall  := is_ID_EXE_overlap | is_ID_MEM_overlap | is_ID_WB_overlap
  io.controller_datapath_io.ID_flush  := actual_branch_result
  io.controller_datapath_io.EXE_stall := false.B
  io.controller_datapath_io.EXE_flush := actual_branch_result | is_ID_EXE_overlap | is_ID_MEM_overlap | is_ID_WB_overlap
  io.controller_datapath_io.MEM_stall := false.B
  io.controller_datapath_io.WB_stall  := false.B
}
