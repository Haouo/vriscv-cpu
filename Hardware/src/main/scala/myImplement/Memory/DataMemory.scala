package muImplement.Memory

import chisel3._
import chisel3.util._
import chisel3.util.experimental.loadMemoryFromFileInline

class DataMemIO(addrWidth: Int, dataWidth: Int) extends Bundle {
  val addr      = Input(UInt(addrWidth.W))
  val writeData = Input(UInt(dataWidth.W))
  val wEnable   = Input(UInt((dataWidth / 8).W))
  val readData  = Output(UInt(dataWidth.W))
}

class DataMem(addrWidth: Int = 16, dataWidth: Int = 32) extends Module {
  val io = IO(new DataMemIO(addrWidth, dataWidth))

  val byte = 8
  val mem  = Mem(1 << addrWidth, UInt(byte.W))
  loadMemoryFromFileInline(mem, "./src/main/resource/data.hex")

  // address alignment
  val addrAlign = WideDefault(io.addr & (~"b11".U))

  // write memory
  for (i <- 0 until 4) {
    mem(addrAlign + i.U) := Mux(io.wEnable(i).asBool, writeData(8 * i + 7, 8 * i), mem(addrAlign + i.U))
  }
  // read memory
  io.readData := mem(addrAlign + 3.U) ## mem(addrAlign + 2.U) ## mem(addrAlign + 1.U) ## mem(addrAlign)
}
