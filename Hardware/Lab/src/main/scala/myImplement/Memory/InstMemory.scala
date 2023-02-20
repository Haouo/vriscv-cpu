package muImplement.Memory

import chisel3._
import chisel3.util._
import chisel3.util.experimental.loadMemoryFromFileInline

class InstMemIO(addrWidth: Int) extends Bundle {
  val addr = Input(UInt(addrWidth.W))
  val inst = Output(UInt(32.W))
}

class InstMem(addrWidth: Int = 16) extends Module {
  val io = IO(new InstMemIO)

  val byte = 8
  val mem  = Mem(1 << addrWidth, UInt(byte.W))
  loadMemoryFromFileInline(mem, "./src/main/resource/inst.hex")

  // address alignment
  val addrAlign = WireDefault(io.addr & (~"b11".U))
  // read inst
  io.inst := mem(addrAlign + 3.U) ## mem(addrAlign + 2.U) ## mem(addrAlign + 1.U) ## mem(addrAlign)
}
