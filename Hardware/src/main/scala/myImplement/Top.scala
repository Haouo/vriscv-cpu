package myImplement

import chisel3._
import chisel3.util._

import myImplement.PipelineCPU.TopCPU
import myImplement.Memory.{DataMemory, InstMemory}
import ModuleIF.CPU_AXI_Wrapper
import AXI.AXIXBar

class TopIO extends Bundle {
  // test ports
  val regs        = Output(Vec(32, UInt(32.W)))
  val vector_regs = Output(Vec(32, UInt(512.W)))
  val isHcf       = Output(Bool())
  val flush       = Output(Bool())
  val stall_MA    = Output(Bool())
  val stall_DH    = Output(Bool())
  val IF_pc       = Output(UInt(32.W))
  val ID_pc       = Output(UInt(32.W))
  val EXE_pc      = Output(UInt(32.W))
  val MEM_pc      = Output(UInt(32.W))
  val WB_pc       = Output(UInt(32.W))
  val EXE_src1    = Output(UInt(32.W))
  val EXE_src2    = Output(UInt(32.W))
  val ALU_src1    = Output(UInt(32.W))
  val ALU_src2    = Output(UInt(32.W))
  val EXE_alu_out = Output(UInt(32.W))
  // val MEM_raddr      = Output(UInt(15.W))
  // val MEM_rdata      = Output(UInt(32.W))
  val WB_rd          = Output(UInt(5.W))
  val WB_wdata       = Output(UInt(32.W))
  val EXE_jump       = Output(Bool())
  val EXE_branch     = Output(Bool())
  val E_branch_taken = Output(Bool())
}

class Top extends Module {
  val io = IO(new TopIO)

  // * Modules * //
  val cpu                = Module(new TopCPU(15, 32))
  val instMem            = Module(new InstMemory(15))
  val dataMem            = Module(new DataMemory(15, 32))
  val cpu_to_axi_wrapper = Module(new CPU_AXI_Wrapper(15, 32))
  val bus                = Module(new AXIXBar(1, 1, 15, 32, List(("h8000".U, "h10000".U))))

  // * connection between modules * //
  // CPU <-----> InstMem
  cpu.io.InstMemIF <> instMem.io
  // CPU <-----> Wrapper
  cpu.io.to_wrapper <> cpu_to_axi_wrapper.io.to_cpu
  // Wrapper <-----> AXI Bus
  cpu_to_axi_wrapper.io.to_AXI_bus <> bus.io.masters(0)
  // AXI Bus <-----> DataMem
  bus.io.slaves(0) <> dataMem.io.to_bus

  // * test ports * //
  io.regs        := cpu.io.regs
  io.vector_regs := cpu.io.vector_regs
  io.isHcf       := cpu.io.isHcf
  io.flush       := cpu.io.flush
  io.stall_DH    := cpu.io.stall_DH
  io.stall_MA    := cpu.io.stall_MA
  io.IF_pc       := cpu.io.IF_pc
  io.ID_pc       := cpu.io.ID_pc
  io.EXE_pc      := cpu.io.EXE_pc
  io.MEM_pc      := cpu.io.MEM_pc
  io.WB_pc       := cpu.io.WB_pc
  io.EXE_src1    := cpu.io.EXE_src1
  io.EXE_src2    := cpu.io.EXE_src2
  io.ALU_src1    := cpu.io.ALU_src1
  io.ALU_src2    := cpu.io.ALU_src2
  io.EXE_alu_out := cpu.io.EXE_alu_out
  // io.MEM_raddr      := dataMem.io.addr
  // io.MEM_rdata      := dataMem.io.readData
  io.WB_rd          := cpu.io.WB_rd
  io.WB_wdata       := cpu.io.WB_wdata
  io.EXE_jump       := cpu.io.EXE_jump
  io.EXE_branch     := cpu.io.EXE_branch
  io.E_branch_taken := cpu.io.E_branch_taken
}

object Top extends App {
  emitVerilog(
    new Top,
    Array("--target-dir", "./generated/Top")
  )
}
