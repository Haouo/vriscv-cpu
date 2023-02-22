package myImplement.PipelineCPU.Datapath

import chisel3._
import chisel3.util._

import myImplement.PipelineCPU.Control.nop

class IF_PipeReg extends Module {
  val io = IO(new Bundle {
    // control
    val stall     = Input(Bool())
    // data
    val nextPC    = Output(UInt(32.W))
    val currentPC = Output(UInt(32.W))
  })

  val PC = RegInit(UInt(32.W))
  PC           := Mux(io.stall, PC, io.nextPC)
  io.currentPC := PC
}

class ID_PipeReg extends Module {
  val io = IO(new Bundle {
    // control
    val stall    = Input(Bool())
    val flush    = Input(Bool())
    // data
    val pc_in    = Input(UInt(32.W))
    val pc_out   = Output(UInt(32.W))
    val inst_in  = Input(UInt(32.W))
    val inst_out = Output(UInt(32.W))
  })

  // regs
  val pc   = Reg(UInt(32.W))
  val inst = Reg(UInt(32.W))

  // stall & flush control
  pc   := Mux(io.flush, 0.U, Mux(io.stall, pc, io.pc_in))
  inst := Mux(io.flush, nop, Mux(io.stall, inst, io.inst_in))

  // output
  io.pc_out   := pc
  io.inst_out := inst
}

class EXE_PipeReg extends Module {
  val io = IO(new Bundle {
    // control
    val stall        = Input(Bool())
    val flush        = Input(Bool())
    // data
    val pc_in        = Input(UInt(32.W))
    val pc_out       = Output(UInt(32.W))
    val inst_in      = Input(UInt(32.W))
    val inst_out     = Output(UInt(32.W))
    val rs1_data_in  = Input(UInt(32.W))
    val rs1_data_out = Output(UInt(32.W))
    val rs2_data_in  = Output(UInt(32.W))
    val rs2_data_out = Output(UInt(32.W))
    val imme_in      = Input(UInt(32.W))
    val imme_out     = Output(UInt(32.W))
  })

  // regs
  val pc       = Reg(UInt(32.W))
  val inst     = Reg(UInt(32.W))
  val rs1_data = Reg(UInt(32.W))
  val rs2_data = Reg(UInt(32.W))
  val imme     = Reg(UInt(32.W))

  // stall & flush control
  pc       := Mux(io.flush, 0.U, Mux(io.stall, pc, io.pc_in))
  inst     := Mux(io.flush, nop, Mux(io.stall, inst, io.inst_in))
  rs1_data := Mux(io.flush, 0.U, Mux(io.stall, rs1_data, io.rs1_data_in))
  rs2_data := Mux(io.flush, 0.U, Mux(io.stall, rs2_data, io.rs2_data_in))
  imme     := Mux(io.flush, 0.U, Mux(io.stall, imme, io.imme_in))

  // output
  io.pc_out       := pc
  io.inst_out     := inst
  io.rs1_data_out := rs1_data
  io.rs2_data_out := rs2_data
  io.imme_out     := imme
}

class MEM_PipeReg extends Module {
  val io = IO(new Bundle {
    // control
    val stall        = Input(Bool())
    // data
    val pc_in        = Input(UInt(32.W))
    val pc_out       = Output(UInt(32.W))
    val inst_in      = Input(UInt(32.W))
    val inst_out     = Output(UInt(32.W))
    val aluOut_in    = Input(UInt(32.W))
    val aluOut_out   = Output(UInt(32.W))
    val rs2_data_in  = Input(UInt(32.W))
    val rs2_data_out = Output(UInt(32.W))
  })

  // regs
  val pc       = Reg(UInt(32.W))
  val inst     = Reg(UInt(32.W))
  val aluOut   = Reg(UInt(32.W))
  val rs2_data = Reg(UInt(32.W))

  // stall control
  pc       := Mux(io.stall, pc, io.pc_in)
  inst     := Mux(io.stall, inst, io.inst_in)
  aluOut   := Mux(io.stall, aluOut, io.aluOut_in)
  rs2_data := Mux(io.stall, rs2_data, io.rs2_data_in)

  // output
  io.pc_out       := pc
  io.inst_out     := inst
  io.aluOut_out   := aluOut
  io.rs2_data_out := rs2_data
}

class WB_PipeReg extends Module {
  val io = IO(new Bundle {
    // control
    val stall       = Input(Bool())
    // data
    val pc_in       = Input(UInt(32.W))
    val pc_out      = Output(UInt(32.W))
    val inst_in     = Input(UInt(32.W))
    val inst_out    = Output(UInt(32.W))
    val ld_data_in  = Input(UInt(32.W))
    val ld_data_out = Output(UInt(32.W))
  })

  // regs
  val pc      = Reg(UInt(32.W))
  val inst    = Reg(UInt(32.W))
  val ld_data = Reg(UInt(32.W))

  // stall control
  pc      := Mux(io.stall, pc, io.pc_in)
  inst    := Mux(io.stall, inst, io.inst_in)
  ld_data := Mux(io.stall, ld_data, io.ld_data_in)

  // output
  io.pc_out      := pc
  io.inst_out    := inst
  io.ld_data_out := ld_data
}
