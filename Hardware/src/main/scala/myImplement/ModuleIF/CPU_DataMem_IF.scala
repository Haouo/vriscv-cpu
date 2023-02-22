package myImplement.ModuleIF

import chisel3._
import chisel3.util._
import myImplement.Memory.DataMemoryIO

class CPU_DataMem_IF(memAddrWidth: Int, memDataWidth: Int) extends Module {
  val io = IO(new Bundle {
    val to_cpu = new DataMemoryIO(memAddrWidth, memDataWidth)
    val to_mem = Flipped(new DataMemoryIO(memAddrWidth, memDataWidth))
  })

  // connect directly (without supporting AXI Burst Mode)
  io.to_cpu <> io.to_mem
}
