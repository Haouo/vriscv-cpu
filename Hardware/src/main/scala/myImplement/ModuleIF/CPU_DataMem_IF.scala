package myImplement.ModuleIF

import chisel3._
import chisel3.util._
import myImplement.Memory.DataMemoryIO

class CPU_Wrapper_IF(memAddrWidth: Int, memDataWidth: Int) extends Bundle {
  // TODO
}

class Bus_Wrapper(memAddrWidth: Int, memDataWidth: Int) extends Module {
  val io = IO(new Bundle {
    val to_cpu = new CPU_Wrapper_IF(memAddrWidth, memDataWidth)
    val to_mem = Flipped(new DataMemoryIO(memAddrWidth, memDataWidth))
  })

  // connect directly (without supporting AXI Burst Mode)
  // TODO
}
