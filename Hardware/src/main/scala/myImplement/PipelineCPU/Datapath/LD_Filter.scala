package myImplement.PipelineCPU.Datapath

import chisel3._
import chisel3.util._

import myImplement.PipelineCPU.func3_set.LOAD_func3._

class LD_FilterIO extends Bundle {
  val inData   = Input(UInt(32.W))
  val WB_func3 = Input(UInt(3.W))
  val outData  = Output(UInt(32.W))
}

class LD_Filter extends Module {
  val io = IO(new LD_FilterIO)

  val sextData = Wire(SInt(32.W))
  io.outData := sextData.asUInt
  sextData   := MuxLookup(
    io.WB_func3,
    io.inData.asSInt,
    Seq(
      lw  -> io.inData.asSInt,
      lb  -> io.inData(7, 0).asSInt,                // sign extend
      lh  -> io.inData(15, 0).asSInt,
      lbu -> (0.U(24.U) ## io.inData(7, 0)).asSInt, // zero extend
      lhu -> (0.U(16.W) ## io.inData(15, 0)).asSInt
    )
  )
}
