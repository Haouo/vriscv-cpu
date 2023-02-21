package myImplement.PipelineCPU

import chisel3._
import chisel3.util._

class TopCPU_IO(memAddrWidth: Int) extends Bundle {
  // System
  val regs = Output(Vec(32, UInt(32.W)))
  val Hcf  = Output(Bool())

  // Test
  val E_Branch_taken = Output(Bool())
  val Flush          = Output(Bool())
  val Stall_MA       = Output(Bool())
  val Stall_DH       = Output(Bool())
  val IF_PC          = Output(UInt(memAddrWidth.W))
  val ID_PC          = Output(UInt(memAddrWidth.W))
  val EXE_PC         = Output(UInt(memAddrWidth.W))
  val MEM_PC         = Output(UInt(memAddrWidth.W))
  val WB_PC          = Output(UInt(memAddrWidth.W))
  val EXE_src1       = Output(UInt(32.W))
  val EXE_src2       = Output(UInt(32.W))
  val ALU_src1       = Output(UInt(32.W))
  val ALU_src2       = Output(UInt(32.W))
  val EXE_alu_out    = Output(UInt(32.W))
  val WB_rd          = Output(UInt(5.W))
  val WB_wdata       = Output(UInt(32.W))
  val EXE_Jump       = Output(Bool())
  val EXE_Branch     = Output(Bool())
}

class TopCPU(memAddrWidth: Int) extends Bundle {
  val io = IO(new TopCPU_IO(memAddrWidth))

  // TODO
}

object TopCPU extends App {
  emitVerilog(
    new top,
    Array("--target-dir", "./generated/top")
  )
}
