package myImplement.Memory

import chisel3._
import chisel3.util._
import chisel3.util.experimental.loadMemoryFromFileInline

class InstMemoryIO(memAddrWidth: Int) extends Bundle {
  val addr = Input(UInt(memAddrWidth.W))
  val inst = Output(UInt(32.W))
}

class InstMemory(memAddrWidth: Int) extends Module {
  val io = IO(new InstMemoryIO(memAddrWidth))

  val byte = 8
  val mem  = Mem(1 << memAddrWidth, UInt(byte.W))

  // pre-load memory
  loadMemoryFromFileInline(mem, "./src/main/resource/inst.hex")

  // read inst
  io.inst := mem(io.addr + 3.U) ## mem(io.addr + 2.U) ## mem(io.addr + 1.U) ## mem(io.addr)
}
