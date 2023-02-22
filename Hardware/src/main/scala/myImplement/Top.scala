package myImplement

import chisel3._
import chisel3.util._

import myImplement.PipelineCPU.TopCPU
import myImplement.Memory.{DataMemory, InstMemory}
import myImplement.ModuleIF.CPU_DataMem_IF

class TopIO extends Bundle {
  //
}

class Top extends Module {
  val io = IO(new TopIO)

  // * Modules * //
  val cpu            = Module(new TopCPU(15, 32))
  val instMem        = Module(new InstMemory(15))
  val dataMem        = Module(new DataMemory(15, 32))
  val cpu_dataMem_IF = Module(new CPU_DataMem_IF(15, 32))

  // * connection between modules * //
  cpu.io.InstMemIF <> instMem.io
  cpu.io.DataMemIF <> cpu_dataMem_IF.io.to_cpu
  cpu_dataMem_IF.io.to_mem <> dataMem.io
}

object Top extends App {
  emitVerilog(
    new Top,
    Array("--target-dir", "./generated/Top")
  )
}
