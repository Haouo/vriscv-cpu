package myImplement.PipelineCPU.Datapath

import chisel3._
import chisel3.util._

import myImplement.PipelineCPU.Control.ALU_op
import myImplement.PipelineCPU.Control.ALU_op._

class ALU_IO extends Bundle {
  val op1    = Input(UInt(32.W))
  val op2    = Input(UInt(32.W))
  val aluOP  = Input(ALU_op())
  val aluOut = Output(UInt(32.W))
}

class ALU extends Module {
  val io = IO(new ALU_IO)

  io.aluOut := MuxLookup(
    io.aluOP.asUInt,
    0.U,
    Seq(
      ADD.asUInt      -> (io.op1 + io.op2),
      SUB.asUInt      -> (io.op1 - io.op2),
      SLL.asUInt      -> (io.op1 << io.op2(4, 0)),
      SLT.asUInt      -> (io.op1.asSInt < io.op2.asSInt),
      SLTU.asUInt     -> (io.op1 < io.op2),
      XOR.asUInt      -> (io.op1 ^ io.op2),
      SRL.asUInt      -> (io.op1 >> io.op2(4, 0)),
      SRA.asUInt      -> (io.op1.asSInt >> io.op2(4, 0)).asUInt,
      OR.asUInt       -> (io.op1 | io.op2),
      AND.asUInt      -> (io.op1 & io.op2),
      COPY_OP2.asUInt -> (io.op2)
    )
  )
}
