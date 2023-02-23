package myImplement.PipelineCPU

import chisel3._
import chisel3.util._

import myImplement.PipelineCPU.Datapath.Datapath
import myImplement.PipelineCPU.Controller.Controller
import myImplement.Memory.{DataMemoryIO, InstMemoryIO}

class TopCPU_IO(memAddrWidth: Int, memDataWidth: Int) extends Bundle {
  // System
  val InstMemIF = Flipped(new InstMemoryIO(memAddrWidth))
  val DataMemIF = Flipped(new DataMemoryIO(memAddrWidth, memDataWidth))

  // Test
  val regs  = Output(Vec(32, UInt(32.W)))
  val isHcf = Output(Bool())
  // flush & stall
  val flush    = Output(Bool())
  val stall_MA = Output(Bool())
  val stall_DH = Output(Bool())
  // PCs
  val IF_pc  = Output(UInt(32.W))
  val ID_pc  = Output(UInt(32.W))
  val EXE_pc = Output(UInt(32.W))
  val MEM_pc = Output(UInt(32.W))
  val WB_pc  = Output(UInt(32.W))
  // EXE Stage
  val EXE_src1    = Output(UInt(32.W))
  val EXE_src2    = Output(UInt(32.W))
  val ALU_src1    = Output(UInt(32.W))
  val ALU_src2    = Output(UInt(32.W))
  val EXE_alu_out = Output(UInt(32.W))
  // WB Stage
  val WB_rd    = Output(UInt(5.W))
  val WB_wdata = Output(UInt(32.W))
  // about branch
  val EXE_jump       = Output(Bool())
  val EXE_branch     = Output(Bool())
  val E_branch_taken = Output(Bool())
}

class TopCPU(memAddrWidth: Int, memDataWidth: Int) extends Module {
  val io = IO(new TopCPU_IO(memAddrWidth, memDataWidth))

  // * Modules * //
  val datapath   = Module(new Datapath(memAddrWidth, memDataWidth))
  val controller = Module(new Controller(memDataWidth))

  // * Module Connection * //
  // Datapath <-----> Controller
  datapath.io.datapath_controller_io <> controller.io.controller_datapath_io
  datapath.io.datapath_instMem_io <> io.InstMemIF
  datapath.io.datapath_dataMem_io <> io.DataMemIF

  // * test ports * //
  // from datapath
  io.regs        := datapath.io.regs
  io.IF_pc       := datapath.io.IF_pc
  io.ID_pc       := datapath.io.ID_pc
  io.EXE_pc      := datapath.io.EXE_pc
  io.MEM_pc      := datapath.io.MEM_pc
  io.WB_pc       := datapath.io.WB_pc
  io.EXE_src1    := datapath.io.EXE_src1
  io.EXE_src2    := datapath.io.EXE_src2
  io.ALU_src1    := datapath.io.ALU_src1
  io.ALU_src2    := datapath.io.ALU_src2
  io.EXE_alu_out := datapath.io.EXE_alu_out
  io.WB_rd       := datapath.io.WB_rd
  io.WB_wdata    := datapath.io.WB_wdata
  // from controller
  io.isHcf          := controller.io.isHcf
  io.flush          := controller.io.flush
  io.stall_DH       := controller.io.stall_DH
  io.stall_MA       := controller.io.stall_MA
  io.EXE_jump       := controller.io.EXE_jump
  io.EXE_branch     := controller.io.EXE_branch
  io.E_branch_taken := controller.io.E_branch_taken
}

object TopCPU extends App {
  emitVerilog(
    new TopCPU(15, 32),
    Array("--target-dir", "./generated/top")
  )
}
