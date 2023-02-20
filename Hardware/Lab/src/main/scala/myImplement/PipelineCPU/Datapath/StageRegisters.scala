package myImplement.PipelineCPU.Datapath

import chisel3._
import chisel3.util._

class IF_reg extends Nodule {
  val io = IO(new Bundle {
    val stall     = Input(Bool())
    val nextPC    = Output(UInt(32.W))
    val currentPC = Output(UInt(32.W))
  })

  val PC = RegInit(UInt(32.W))
  PC           := Mux(stall, PC, io.nextPC)
  io.currentPC := PC
}

class ID_reg extends Module {
  val io = IO(new Bundle {
    val stall  = Input(Bool())
    val flush  = Input(Bool())
    val instIn = Input(UInt(32.W))
  })
}

class EXE_reg extends Module {
  val io = IO(new Bundle {
    val stall = Input(Bool())
    val flush = Input(Bool())
  })
}

class MEM_reg extends Module {
  val io = IO(new Bundle {
    val stall = Input(Bool())
  })
}

class WB_reg extends Module {
  val io = IO(new Bundle {
    val stall = Input(Bool())
  })
}
