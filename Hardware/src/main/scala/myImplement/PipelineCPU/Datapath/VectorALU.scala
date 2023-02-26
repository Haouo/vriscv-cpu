package myImplement.PipelineCPU.Datapath

import chisel3._
import chisel3.util._

import myImplement.PipelineCPU.Control.VALU_op
import myImplement.PipelineCPU.Control.VALU_op._

class VectorALU_IO extends Bundle {
  val aluOP  = Input(VALU_op())
  val src1   = Input(UInt(512.W))
  val src2   = Input(UInt(512.W))
  val aluOut = Output(UInt(512.W))
}

class VectorALU extends Module {
  val io = IO(new VectorALU_IO)

  val src1_unit = io.src1.asTypeOf(Vec(64, UInt(8.W)))
  val src2_unit = io.src2.asTypeOf(Vec(64, UInt(8.W)))

  val VADD_VV_wire = Wire(Vec(64, UInt(8.W)))
  val VMUL_VX_wire = Wire(Vec(64, UInt(8.W)))

  VADD_VV_wire.zip(src1_unit.zip(src2_unit)).map { case (unit, (op1, op2)) => unit := op1 + op2 }
  VMUL_VX_wire.zip(src1_unit.zip(src2_unit)).map { case (unit, (op1, op2)) => unit := (op1 * op2) (7, 0) }

  io.aluOut := Mux(io.aluOP === ADD_VV, VADD_VV_wire.asTypeOf(UInt(512.W)), VMUL_VX_wire.asTypeOf(UInt(512.W)))
}
