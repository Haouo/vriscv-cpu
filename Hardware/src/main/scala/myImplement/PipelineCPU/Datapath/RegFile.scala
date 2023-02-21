package myImplement.PipelineCPU.Datapath

import chisel3._
import chisel3.util._

class RegFileIO extends Bundle {
  val wEnable   = Input(Bool())
  val rs1_index = Input(UInt(5.W))
  val rs2_index = Input(UInt(5.W))
  val rd_index  = Input(UInt(5.W))
  val writeData = Input(UInt(32.W))
  val rs1_data  = Output(UInt(32.W))
  val rs2_data  = Output(UInt(32.W))
  // for testing
  val regs      = Output(Vec(32, UInt(32.W)))
}

class RegFile extends Module {
  val io      = IO(new RegFileIO)
  val regFile = Mem(32, UInt(32.W))

  // $x0 is always zero
  regFile(0)           := 0.U
  // output value
  io.rs1_data          := regFile(io.rs1_index)
  io.rs2_data          := regFile(io.rs2_index)
  // write (cannot write $x0)
  regFile(io.rd_index) := Mux(io.wEnable & (io.rd_index =/= 0.U), io.writeData, regFile(io.rd_index))

  // for testing
  for (i <- 0 until 32) {
    io.regs(i) := regFile(i.U)
  }
}
