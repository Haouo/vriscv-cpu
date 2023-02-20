package myImplement.PipelineCPU.Datapath

import chisel3._
import chisel3.util._

class DecoderIO extends Bundle {
  val inst      = Input(UInt(32.W))
  val opcode    = Output(UInt(5.W))
  val func3     = Output(UInt(3.W))
  val func7     = Output(UInt(1.W))
  val rs1_index = Output(UInt(5.W))
  val rs2_index = Output(UInt(5.W))
  val rd_index  = Output(UInt(5.W))
}

class Decoder extends Module {
  val io = IO(new DecoderIO)

  io.opcode    := io.inst(6, 2)
  io.func3     := io.inst(14, 12)
  io.func7     := io.inst(30)
  io.rs1_index := io.inst(19, 15)
  io.rs2_index := io.inst(24, 20)
  io.rd_index  := io.inst(11, 7)
}
