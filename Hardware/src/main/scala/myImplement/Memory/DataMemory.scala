package myImplement.Memory

import chisel3._
import chisel3.util._
import chisel3.util.experimental.loadMemoryFromFileInline

class DataMemoryIO(memAddrWidth: Int, memDataWidth: Int) extends Bundle {
  val addr      = Input(UInt(memAddrWidth.W))
  val writeData = Input(UInt(memDataWidth.W))
  val wEnable   = Input(UInt((memDataWidth / 8).W))
  val readData  = Output(UInt(memDataWidth.W))
}

class DataMemory(memAddrWidth: Int, memDataWidth: Int) extends Module {
  val io = IO(new DataMemoryIO(memAddrWidth, memDataWidth))

  val byte = 8
  val mem  = Mem(1 << memAddrWidth, UInt(byte.W))

  // pre-load memory
  loadMemoryFromFileInline(mem, "./src/main/resource/data.hex")

  // write memory
  for (i <- 0 until 4) {
    mem(io.addr + i.U) := Mux(io.wEnable(i).asBool, io.writeData(8 * i + 7, 8 * i), mem(io.addr + i.U))
  }
  // read memory
  io.readData := mem(io.addr + 3.U) ## mem(io.addr + 2.U) ## mem(io.addr + 1.U) ## mem(io.addr)
}
