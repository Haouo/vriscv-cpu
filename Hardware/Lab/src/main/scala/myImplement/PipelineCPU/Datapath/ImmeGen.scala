package myImplement.PipelineCPU.Datapath

import chisel3._
import chisel3.util._

class ImmeGenIO extends Bundle {
  val inst = Input(UInt(32.W))
  val imme = Output(UInt(32.W))
}

class ImmeGen extends Module {
  val io     = IO(new ImmeGenIO)
  // main part
  val opcode = WireDefault(io.inst(6, 2))
  io.imme := MuxLookup(
    opcode,
    0.U,
    Seq(
      OP     -> 0.U,
      OP_IMM -> (Fill(20, io.inst(31)) ## io.inst(31, 20)),
      LOAD   -> (Fill(20, io.inst(31)) ## io.inst(31, 20)),
      JALR   -> (Fill(20, io.inst(31)) ## io.inst(31, 20)),
      STORE  -> (Fill(20, io.inst(31)) ## io.inst(31, 25) ## io.inst(11, 7)),
      BRANCH -> (Fill(20, io.inst(31)) ## io.inst(7) ## io.inst(30, 25) ## io.inst(11, 8) ## 0.U(1.W)),
      LUI    -> (io.inst(31, 12) ## 0.U(12.W)),
      AUIPC  -> (io.inst(31, 12) ## 0.U(12.W)),
      JAL    -> (Fill(12, io.inst(31)) ## io.inst(19, 12) ## io.inst(20) ## io.inst(30, 21) ## 0.U(1.W))
    )
  )
}
