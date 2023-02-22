package myImplement.PipelineCPU

import chisel3._
import chisel3.util._

import myImplement.PipelineCPU.Control._

class Controller_DatapathIO(memDataWidth: Int) extends Bundle {
  // IF Stage
  val IF_next_pc_sel      = Output(PC_sel())
  // ID Stage
  val ID_inst             = Input(UInt(32.W))
  // EXE Stage
  val EXE_inst            = Input(UInt(32.W))
  val EXE_alu_op          = Output(ALU_op())
  val EXE_alu_src1_sel    = Output(ALU_src1_sel())
  val EXE_alu_src2_sel    = Output(ALU_src2_sel())
  val EXE_BrUn            = Output(Bool())
  val EXE_BrEq            = Input(Bool())
  val EXE_BrLT            = Input(Bool())
  // MEM Stage
  val MEM_inst            = Input(UInt(32.W))
  val MEM_dataMem_wEnable = Output(UInt((memDataWidth / 8).W))
  // WB Stage
  val WB_inst             = Input(UInt(32.W))
  val WB_wEnable          = Output(Bool())
  val WB_wb_sel           = Output(WB_sel_control())
  // PipelineReg Control
  val IF_stall            = Output(Bool())
  val ID_stall            = Output(Bool())
  val ID_flush            = Output(Bool())
  val EXE_stall           = Output(Bool())
  val EXE_flush           = Output(Bool())
  val MEM_stall           = Output(Bool())
  val WB_stall            = Output(Bool())
  // terminate signal
  val isHcf               = Output(Bool())
}
