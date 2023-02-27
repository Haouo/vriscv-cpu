package myImplement.PipelineCPU

import chisel3._
import chisel3.util._
import chisel3.experimental.ChiselEnum

// * RV32I * //
object opcode {
  val OP     = "b0110011".U(7.W)
  val OP_IMM = "b0010011".U(7.W)
  val LOAD   = "b0000011".U(7.W)
  val STORE  = "b0100011".U(7.W)
  val BRANCH = "b1100011".U(7.W)
  val JAL    = "b1101111".U(7.W)
  val JALR   = "b1100111".U(7.W)
  val LUI    = "b0110111".U(7.W)
  val AUIPC  = "b0010111".U(7.W)
  val HCF    = "b0001011".U(7.W)
}

object func3_set {
  object Arithmetic_func3 {
    val add_sub = "b000".U(3.W)
    val sll     = "b001".U(3.W)
    val slt     = "b010".U(3.W)
    val sltu    = "b011".U(3.W)
    val xor     = "b100".U(3.W)
    val srl_sra = "b101".U(3.W)
    val or      = "b110".U(3.W)
    val and     = "b111".U(3.W)
  }

  object BRANCH_func3 {
    val beq  = "b000".U(3.W)
    val bne  = "b001".U(3.W)
    val blt  = "b100".U(3.W)
    val bge  = "b101".U(3.W)
    val bltu = "b110".U(3.W)
    val bgeu = "b111".U(3.W)
  }

  object LOAD_func3 {
    val lb  = "b000".U(3.W)
    val lh  = "b001".U(3.W)
    val lw  = "b010".U(3.W)
    val lbu = "b100".U(3.W)
    val lhu = "b101".U(3.W)
  }

  object STORE_func3 {
    val sb = "b000".U(3.W)
    val sh = "b001".U(3.W)
    val sw = "b010".U(3.W)
  }
}

// * V-Extension
object vector_op {
  val OPV    = "b1010111".U(7.W)
  val VLOAD  = "b0000111".U(7.W)
  val VSTORE = "b0100111".U(7.W)
}

object vector_func3 {
  object arithmetic {
    val OPIVV = "b000".U
    val OPIVX = "b100".U
  }
}

object vector_func6 {
  object arithmetic {
    val vadd = "b000000".U(6.W)
    val vmul = "b100101".U(6.W)
  }
}

object utilFunctions {
  // for both RN32I and V-Extension
  def get_op(inst: UInt):        UInt = inst(6, 0)
  def get_rs1_index(inst: UInt): UInt = inst(19, 15)
  // for RV32I
  def get_func3(inst: UInt):     UInt = inst(14, 12)
  def get_func7(inst: UInt):     UInt = inst(30)
  def get_rs2_index(inst: UInt): UInt = inst(24, 20)
  def get_rd_index(inst: UInt):  UInt = inst(11, 7)
  // for V-Extension
  def get_func6(inst: UInt):     UInt = inst(31, 26)
  def get_vs1_index(inst: UInt): UInt = inst(19, 15)
  def get_vs2_index(inst: UInt): UInt = inst(24, 20) // for OPV
  def get_vs3_index(inst: UInt): UInt = inst(11, 7)  // for VSE
  def get_vd_index(inst: UInt):  UInt = inst(11, 7)  // for OPV or VLE
}

object Control {
  val nop = "h00000013".U(32.W) // addi $x0, $x0, 0

  // IF Stage
  object PC_sel extends ChiselEnum {
    val sel_IF_pc_plue_4, sel_EXE_target_pc = Value
  }

  // ID Stage
  // for vector
  object vs2_index_sel_control extends ChiselEnum {
    val sel_vs2, sel_vs3 = Value
  }

  // EXE Stage
  object ALU_op extends ChiselEnum {
    val ADD, SUB, SLL, SLT, SLTU, XOR, SRL, SRA, OR, AND, COPY_OP2 = Value
    // COPY_OP2 is for Lui
  }
  object ALU_src1_sel extends ChiselEnum {
    val sel_PC, sel_rs1 = Value
  }
  object ALU_src2_sel extends ChiselEnum {
    val sel_Imme, sel_rs2 = Value
  }
  // for V-Extension
  object VALU_op extends ChiselEnum {
    val ADD_VV, MUL_VX = Value
  }
  object VALU_src1_sel extends ChiselEnum {
    val sel_vs1, sel_rs1 = Value
  }

  // WB Stage
  object WB_sel_control extends ChiselEnum {
    val sel_pc_plue_4, sel_alu_out, sel_ld_filter_data = Value
  }
  // for V-Extension
  object WB_v_sel_control extends ChiselEnum {
    val sel_valu_out, sel_v_ld_data = Value
  }
}
