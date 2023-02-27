package myImplement.PipelineCPU.Controller

import chisel3._
import chisel3.util._
import chisel3.experimental.ChiselEnum

import myImplement.PipelineCPU.opcode._
import myImplement.PipelineCPU.vector_op._
import myImplement.PipelineCPU.vector_func6
import myImplement.PipelineCPU.vector_func3
import myImplement.PipelineCPU.Control.VALU_op._
import myImplement.PipelineCPU.Control.VALU_src1_sel
import myImplement.PipelineCPU.func3_set._
import myImplement.PipelineCPU.func3_set.Arithmetic_func3._
import myImplement.PipelineCPU.utilFunctions._
import myImplement.PipelineCPU.Control._
import myImplement.PipelineCPU.Controller_DatapathIO
import myImplement.ModuleIF.Wrapper_to_CPU_IO_from_Controller
import myImplement.PipelineCPU.func3_set

object ControllerState extends ChiselEnum {
  // sNormal -> CPU operate as usual
  // sReadWait -> send read request and wait for data from bus
  // sWriteWait -> send write request and wait for write resp
  // in both sReadWait and sWriteWait -> whole CPU have to stall to wait until memory operation is finished
  val sNormal, sReadSend, sReadWait, sWriteSend, sWriteWait, sDone = Value
}

class ControllerIO(memDataWidth: Int) extends Bundle {
  val controller_datapath_io = new Controller_DatapathIO(memDataWidth)
  // for wrapper support
  val controller_to_wrapper = Flipped(new Wrapper_to_CPU_IO_from_Controller)

  // test ports
  val isHcf          = Output(Bool())
  val flush          = Output(Bool())
  val stall_MA       = Output(Bool())
  val stall_DH       = Output(Bool())
  val EXE_branch     = Output(Bool())
  val EXE_jump       = Output(Bool())
  val E_branch_taken = Output(Bool())
}

class Controller(memDataWidth: Int) extends Module {
  import ControllerState._

  val io = IO(new ControllerIO(memDataWidth))

  // for reducing the code
  val ID_inst  = io.controller_datapath_io.ID_inst
  val EXE_inst = io.controller_datapath_io.EXE_inst
  val MEM_inst = io.controller_datapath_io.MEM_inst
  val WB_inst  = io.controller_datapath_io.WB_inst

  // * stage register * //
  val state = RegInit(sNormal)
  // * Wires * //
  val actual_branch_result = Wire(Bool())

  // * IF Stage * //
  io.controller_datapath_io.IF_next_pc_sel := Mux(
    actual_branch_result,
    PC_sel.sel_EXE_target_pc,
    PC_sel.sel_IF_pc_plue_4
  )

  // * ID Stage * //
  io.controller_datapath_io.ID_vs2_index_sel := Mux(
    get_op(io.controller_datapath_io.ID_inst) === VSTORE,
    vs2_index_sel_control.sel_vs3,
    vs2_index_sel_control.sel_vs2
  )

  // * EXE Stage * //
  io.controller_datapath_io.EXE_BrUn := get_func3(EXE_inst) === BRANCH_func3.bltu || get_func3(
    EXE_inst
  ) === BRANCH_func3.bgeu
  io.controller_datapath_io.EXE_alu_src1_sel := Mux(
    get_op(EXE_inst) === BRANCH || get_op(EXE_inst) === JAL || get_op(EXE_inst) === AUIPC,
    ALU_src1_sel.sel_PC,
    ALU_src1_sel.sel_rs1
  )
  io.controller_datapath_io.EXE_alu_src2_sel := Mux(
    get_op(EXE_inst) === OP,
    ALU_src2_sel.sel_rs2,
    ALU_src2_sel.sel_Imme
  )
  io.controller_datapath_io.EXE_alu_op := MuxLookup(
    get_op(EXE_inst),
    ALU_op.ADD,
    Seq(
      OP -> MuxLookup(
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
      LUI -> ALU_op.COPY_OP2
    )
  )
  // for vector
  io.controller_datapath_io.EXE_VALU_op := Mux(
    get_op(io.controller_datapath_io.EXE_inst) =/= OPV,
    DontCare,
    MuxLookup(
      get_func3(io.controller_datapath_io.EXE_inst),
      DontCare,
      Seq(
        vector_func3.arithmetic.OPIVV -> VALU_op.ADD_VV,
        vector_func3.arithmetic.OPIVX -> VALU_op.MUL_VX
      )
    )
  )
  io.controller_datapath_io.EXE_VALU_src1_sel := Mux(
    get_func3(io.controller_datapath_io.EXE_inst) === vector_func3.arithmetic.OPIVX,
    VALU_src1_sel.sel_rs1,
    VALU_src1_sel.sel_vs1
  )

  // * MEM Stage * //
  val MEM_op = get_op(io.controller_datapath_io.MEM_inst)

  // stall signal due to memory access
  io.controller_datapath_io.stall_memory_access := Mux(
    state === sDone,
    false.B,
    Mux(
      MEM_op === LOAD || MEM_op === STORE || MEM_op === VLOAD || MEM_op === VSTORE,
      true.B,
      false.B
    )
  )
  io.controller_datapath_io.controller_state := state

  // next state logic
  switch(state) {
    is(sNormal) {
      state := Mux(
        MEM_op === LOAD || MEM_op === VLOAD,
        sReadSend,
        Mux(
          MEM_op === STORE || MEM_op === VSTORE,
          sWriteSend,
          sNormal
        )
      )
    }
    is(sReadSend) {
      state := Mux(io.controller_to_wrapper.start, sReadWait, sReadSend)
    }
    is(sReadWait) {
      state := Mux(io.controller_to_wrapper.done, sDone, sReadWait)
    }
    is(sWriteSend) {
      state := Mux(io.controller_to_wrapper.start, sWriteWait, sWriteSend)
    }
    is(sWriteWait) {
      state := Mux(io.controller_to_wrapper.done, sDone, sWriteWait)
    }
    is(sDone) {
      state := sNormal
    }
  }

  // output decoder
  io.controller_to_wrapper.toRead  := false.B
  io.controller_to_wrapper.toWrite := false.B
  io.controller_to_wrapper.length  := 0.U
  switch(state) {
    is(sNormal) {
      io.controller_to_wrapper.toRead  := (MEM_op === LOAD) | (MEM_op === VLOAD)
      io.controller_to_wrapper.toWrite := (MEM_op === STORE) | (MEM_op === VSTORE)
      io.controller_to_wrapper.length := Mux(
        MEM_op === VLOAD || MEM_op === VSTORE,
        15.U,
        0.U
      )
    }
    is(sReadSend) {
      // blank
    }
    is(sReadWait) {
      // blank
    }
    is(sWriteSend) {
      // blank
    }
    is(sWriteWait) {
      // blank
    }
    is(sDone) {
      // blank
    }
  }

  io.controller_to_wrapper.write_strb := MuxLookup(
    get_op(io.controller_datapath_io.MEM_inst),
    0.U,
    Seq(
      STORE -> MuxLookup(
        get_func3(io.controller_datapath_io.MEM_inst),
        0.U,
        Seq(
          func3_set.STORE_func3.sb -> "b0001".U,
          func3_set.STORE_func3.sh -> "b0011".U,
          func3_set.STORE_func3.sw -> "b1111".U
        )
      ),
      VSTORE -> "b1111".U
    )
  )

  // * WB Stage * //
  io.controller_datapath_io.WB_wEnable := Mux(
    get_op(WB_inst) === STORE || get_op(WB_inst) === BRANCH,
    false.B,
    true.B
  )
  io.controller_datapath_io.WB_wb_sel := Mux(
    get_op(WB_inst) === JAL || get_op(WB_inst) === JALR,
    WB_sel_control.sel_pc_plue_4,
    Mux(get_op(WB_inst) === LOAD, WB_sel_control.sel_ld_filter_data, WB_sel_control.sel_alu_out)
  )
  // for vector
  io.controller_datapath_io.WB_v_wb_sel := Mux(
    get_op(io.controller_datapath_io.WB_inst) === VLOAD,
    WB_v_sel_control.sel_v_ld_data,
    WB_v_sel_control.sel_valu_out
  )
  io.controller_datapath_io.WB_vreg_wEnable := Mux(
    get_op(io.controller_datapath_io.WB_inst) === VLOAD || get_op(io.controller_datapath_io.WB_inst) === OPV,
    true.B,
    false.B
  )

  // * Control Hazard Detection * //
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

  // * Data Hazard Detection, add vector support * //
  // TODO: add data hazard detection for vector inst
  // wires
  val is_ID_use_rs1, is_ID_use_rs2, is_EXE_use_rd, is_MEM_use_rd, is_WB_use_rd = Wire(Bool())
  val is_ID_rs1_EXE_rd_overlap, is_ID_rs2_EXE_rd_overlap                       = Wire(Bool())
  val is_ID_rs1_MEM_rd_overlap, is_ID_rs2_MEM_rd_overlap                       = Wire(Bool())
  val is_ID_rs1_WB_rd_overlap, is_ID_rs2_WB_rd_overlap                         = Wire(Bool())
  val is_ID_EXE_overlap, is_ID_MEM_overlap, is_ID_WB_overlap                   = Wire(Bool())

  val ID_is_OPIVV = WireDefault((get_op(ID_inst) === OPV) && (get_func3(ID_inst) === vector_func3.arithmetic.OPIVV))

  val is_ID_use_vs1, is_ID_use_vs2, is_ID_use_vs3, is_EXE_use_vd, is_MEM_use_vd, is_WB_use_vd = Wire(Bool())
  val is_ID_vs1_EXE_vd_overlap, is_ID_vs2_EXE_vd_overlap, is_ID_vs3_EXE_vd_overlap            = Wire(Bool())
  val is_ID_vs1_MEM_vd_overlap, is_ID_vs2_MEM_vd_overlap, is_ID_vs3_MEM_vd_overlap            = Wire(Bool())
  val is_ID_vs1_WB_vd_overlap, is_ID_vs2_WB_vd_overlap, is_ID_vs3_WB_vd_overlap               = Wire(Bool())

  // use_rs1 & use_rs2 & use_rd
  is_ID_use_rs1 := Mux(
    get_op(ID_inst) === JAL || get_op(ID_inst) === LUI || get_op(ID_inst) === AUIPC || ID_is_OPIVV,
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
  is_WB_use_rd := Mux(
    get_op(WB_inst) === STORE || get_op(WB_inst) === BRANCH,
    false.B,
    Mux(get_rd_index(WB_inst) === 0.U, false.B, true.B)
  )

  is_ID_use_vs1 := Mux(ID_is_OPIVV, true.B, false.B)
  is_ID_use_vs2 := Mux(get_op(ID_inst) === OPV, true.B, false.B)
  is_ID_use_vs3 := Mux(get_op(ID_inst) === VSTORE, true.B, false.B)
  is_EXE_use_vd := Mux(get_op(EXE_inst) === VSTORE, false.B, true.B)
  is_MEM_use_vd := Mux(get_op(MEM_inst) === VSTORE, false.B, true.B)
  is_WB_use_vd  := Mux(get_op(WB_inst) === VSTORE, false.B, true.B)

  // overlap
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
  is_ID_rs1_WB_rd_overlap := is_ID_use_rs1 & is_WB_use_rd & (get_rs1_index(ID_inst) === get_rd_index(
    WB_inst
  )) & (get_rd_index(WB_inst) =/= 0.U)
  is_ID_rs2_WB_rd_overlap := is_ID_use_rs2 & is_WB_use_rd & (get_rs2_index(ID_inst) === get_rd_index(
    WB_inst
  )) & (get_rd_index(WB_inst) =/= 0.U)

  is_ID_vs1_EXE_vd_overlap := is_ID_use_vs1 & is_EXE_use_vd & (get_vs1_index(ID_inst) === get_vd_index(
    EXE_inst
  )) & (get_vd_index(EXE_inst) =/= 0.U)
  is_ID_vs2_EXE_vd_overlap := is_ID_use_vs2 & is_EXE_use_vd & (get_vs2_index(ID_inst) === get_vd_index(
    EXE_inst
  )) & (get_vd_index(EXE_inst) =/= 0.U)
  is_ID_vs3_EXE_vd_overlap := is_ID_use_vs3 & is_EXE_use_vd & (get_vs3_index(ID_inst) === get_vd_index(
    EXE_inst
  )) & (get_vd_index(EXE_inst) =/= 0.U)
  is_ID_vs1_MEM_vd_overlap := is_ID_use_vs1 & is_MEM_use_vd & (get_vs1_index(ID_inst) === get_vd_index(
    MEM_inst
  )) & (get_vd_index(MEM_inst) =/= 0.U)
  is_ID_vs2_MEM_vd_overlap := is_ID_use_vs2 & is_MEM_use_vd & (get_vs2_index(ID_inst) === get_vd_index(
    MEM_inst
  )) & (get_vd_index(MEM_inst) =/= 0.U)
  is_ID_vs3_MEM_vd_overlap := is_ID_use_vs3 & is_MEM_use_vd & (get_vs3_index(ID_inst) === get_vd_index(
    MEM_inst
  )) & (get_vd_index(MEM_inst) =/= 0.U)
  is_ID_vs1_WB_vd_overlap := is_ID_use_vs1 & is_WB_use_vd & (get_vs1_index(ID_inst) === get_vd_index(
    WB_inst
  )) & (get_vd_index(WB_inst) =/= 0.U)
  is_ID_vs2_WB_vd_overlap := is_ID_use_vs2 & is_WB_use_vd & (get_vs2_index(ID_inst) === get_vd_index(
    WB_inst
  )) & (get_vd_index(WB_inst) =/= 0.U)
  is_ID_vs3_WB_vd_overlap := is_ID_use_vs3 & is_WB_use_vd & (get_vs3_index(ID_inst) === get_vd_index(
    WB_inst
  )) & (get_vd_index(WB_inst) =/= 0.U)

  // total overlap
  is_ID_EXE_overlap := is_ID_rs1_EXE_rd_overlap | is_ID_rs2_EXE_rd_overlap | is_ID_vs1_EXE_vd_overlap | is_ID_vs2_EXE_vd_overlap | is_ID_vs3_EXE_vd_overlap
  is_ID_MEM_overlap := is_ID_rs1_MEM_rd_overlap | is_ID_rs2_MEM_rd_overlap | is_ID_vs1_MEM_vd_overlap | is_ID_vs2_MEM_vd_overlap | is_ID_vs3_MEM_vd_overlap
  is_ID_WB_overlap := is_ID_rs1_WB_rd_overlap | is_ID_rs2_WB_rd_overlap | is_ID_vs1_WB_vd_overlap | is_ID_vs2_WB_vd_overlap | is_ID_vs3_WB_vd_overlap

  // * Pipeline Regs Stall & Flush Control * //
  io.controller_datapath_io.IF_stall  := is_ID_EXE_overlap | is_ID_MEM_overlap | is_ID_WB_overlap
  io.controller_datapath_io.ID_stall  := is_ID_EXE_overlap | is_ID_MEM_overlap | is_ID_WB_overlap
  io.controller_datapath_io.ID_flush  := actual_branch_result
  io.controller_datapath_io.EXE_stall := false.B
  io.controller_datapath_io.EXE_flush := actual_branch_result | is_ID_EXE_overlap | is_ID_MEM_overlap | is_ID_WB_overlap
  io.controller_datapath_io.MEM_stall := false.B
  io.controller_datapath_io.WB_stall  := false.B

  // * test ports * //
  io.isHcf      := get_op(io.controller_datapath_io.ID_inst) === HCF
  io.flush      := actual_branch_result
  io.stall_DH   := io.controller_datapath_io.IF_stall
  io.stall_MA   := io.controller_datapath_io.stall_memory_access
  io.EXE_branch := get_op(io.controller_datapath_io.EXE_inst) === BRANCH
  io.EXE_jump := get_op(io.controller_datapath_io.EXE_inst) === JAL || get_op(
    io.controller_datapath_io.EXE_inst
  ) === JALR
  io.E_branch_taken := actual_branch_result
}
