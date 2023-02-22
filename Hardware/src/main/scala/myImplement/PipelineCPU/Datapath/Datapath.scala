package myImplement.PipelineCPU.Datapath

import chisel3._
import chisel3.util._

import myImplement.PipelineCPU.Controller_DatapathIO
import myImplement.PipelineCPU.Control._

class DatapathIO(memAddrWidth: Int, memDataWidth: Int) extends Bundle {
  val datapath_controller_io = Flipped(new Controller_DatapathIO(memDataWidth))
  // test ports
  // TODO
}

class Datapath(memAddrWidth: Int, memDataWidth: Int) extends Module {
  val io = IO(new DatapathIO(memAddrWidth, memDataWidth))

  // * Modules * //
  val imme_gen    = Module(new ImmeGen)
  val reg_file    = Module(new RegFile)
  val alu         = Module(new ALU)
  val branch_comp = Module(new BranchComp)
  val ld_filter   = Module(new LD_Filter)
  // * Pipeline Regs * //
  val IF_reg      = Module(new IF_PipeReg)
  val ID_reg      = Module(new ID_PipeReg)
  val EXE_reg     = Module(new EXE_PipeReg)
  val MEM_reg     = Module(new MEM_PipeReg)
  val WB_reg      = Module(new WB_PipeReg)

  // TODO
}
