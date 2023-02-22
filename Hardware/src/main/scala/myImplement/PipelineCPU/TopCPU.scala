package myImplement.PipelineCPU

import chisel3._
import chisel3.util._

import myImplement.PipelineCPU.Datapath.Datapath
import myImplement.PipelineCPU.Controller.Controller
import myImplement.Memory.{DataMemoryIO, InstMemoryIO}

class TopCPU_IO(memAddrWidth: Int) extends Bundle {
  // System
  val InstMemIF = Flipped(new InstMemoryIO(memAddrWidth))
  val DataMemIF = Flipped(new DataMemoryIO(memAddrWidth, 32))

  // Test
  val regs           = Output(Vec(32, UInt(32.W)))
  val Hcf            = Output(Bool())
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

class TopCPU(memAddrWidth: Int, memDataWidth: Int) extends Module {
  val io = IO(new TopCPU_IO(memAddrWidth))

  // * Modules * //
  val datapath   = Module(new Datapath(memAddrWidth, memDataWidth))
  val controller = Module(new Controller(memDataWidth))

  // * Module Connection * //
  // Datapath <-----> Controller
  datapath.io.datapath_controller_io <> controller.io.controller_datapath_io

  // test ports
  // TODO
}

object TopCPU extends App {
  emitVerilog(
    new TopCPU(15, 32),
    Array("--target-dir", "./generated/top")
  )
}
