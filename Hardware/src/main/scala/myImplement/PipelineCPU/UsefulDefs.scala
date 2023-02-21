package myImplement.PipelineCPU

import chisel3._
import chisel3.util._
import chisel3.util.experimental.ChiselEnum

object opcode {
  val OP     = "b01100".U(5.W)
  val OP_IMM = "b00100".U(5.W)
  val LOAD   = "b00000".U(5.W)
  val STORE  = "b01000".U(5.W)
  val BRANCH = "b11000".U(5.W)
  val JAL    = "b11011".U(5.W)
  val JALR   = "b11001".U(5.W)
  val LUI    = "b01101".U(5.W)
  val AUIPC  = "b00101".U(5.W)
  val HCF    = "b00010".U(5.W)
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
    val lb  = 0.U(3.W)
    val lh  = 1.U(3.W)
    val lw  = 2.U(3.W)
    val lbu = 4.U(3.W)
    val lhu = 5.U(3.W)
  }

  object STORE_func3 {
    val sb = 0.U(3.W)
    val sh = 1.U(3.W)
    val sw = 2.U(3.W)
  }
}

object Control {
  val nop = "h00000013".U(32.W)

  // IF Stage
  object PC_sel extends ChiselEnum {
    val sel_IF_pc_plue_4, sel_EXE_target_pc = Value
  }

  // EXE Stage
  object ALU_op      extends ChiselEnum {
    val ADD, SUB, SLL, SLT, SLTU, XOR, SRL, SRA, OR, AND, COPY_OP2 = Value
    // COPY_OP2 is for Lui
  }
  object ALU_op1_sel extends ChiselEnum {
    val sel_PC, sel_rs1 = Value
  }
  object ALU_op2_sel extends ChiselEnum {
    val sel_Imme, sel_rs2 = Value
  }

  // WB Stage
  object WB_sel_control extends ChiselEnum {
    val sel_pc_plue_4, sel_alu_out, sel_ld_filter_data = Value
  }
}
