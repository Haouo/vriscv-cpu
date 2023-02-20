package myImplement.PipelineCPU.Datapath

import chisel3._
import chisel3.util._

class BranchCompIO extends Bundle {
  val op1  = Input(UInt(32.W))
  val op2  = Input(UInt(32.W))
  val BrUn = Input(Bool())
  val BrEq = Output(Bool())
  val BrLT = Output(Bool())
}

class BranchComp extends Module {
  val io = IO(new BranchCompIO)

  io.BrEq := io.op1 === io.op2
  io.BrLT := Mux(io.BrUn, io.op1 < io.op2, io.op1.asSInt < io.op2.asSInt)
}
