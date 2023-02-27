package myImplement.PipelineCPU

import chisel3._
import chisel3.util._

import myImplement.PipelineCPU.Control._
import myImplement.PipelineCPU.Controller.ControllerState

class Controller_DatapathIO(memDataWidth: Int) extends Bundle {
  // IF Stage
  val IF_next_pc_sel = Output(PC_sel())
  // ID Stage
  val ID_inst          = Input(UInt(32.W))
  val ID_vs2_index_sel = Output(vs2_index_sel_control())
  // EXE Stage
  val EXE_inst          = Input(UInt(32.W))
  val EXE_alu_op        = Output(ALU_op())
  val EXE_alu_src1_sel  = Output(ALU_src1_sel())
  val EXE_alu_src2_sel  = Output(ALU_src2_sel())
  val EXE_BrUn          = Output(Bool())
  val EXE_BrEq          = Input(Bool())
  val EXE_BrLT          = Input(Bool())
  val EXE_VALU_op       = Output(VALU_op())       // for vector
  val EXE_VALU_src1_sel = Output(VALU_src1_sel()) // for vector
  // MEM Stage
  val MEM_inst           = Input(UInt(32.W))
  val MEM_write_data_sel = Output(MEM_write_data_sel_control())
  // WB Stage
  val WB_inst         = Input(UInt(32.W))
  val WB_wEnable      = Output(Bool())
  val WB_wb_sel       = Output(WB_sel_control())
  val WB_vreg_wEnable = Output(Bool())             // for vector
  val WB_v_wb_sel     = Output(WB_v_sel_control()) // for vector
  // PipelineReg Control
  val stall_memory_access = Output(Bool()) // highest priority
  val IF_stall            = Output(Bool())
  val ID_stall            = Output(Bool())
  val ID_flush            = Output(Bool())
  val EXE_stall           = Output(Bool())
  val EXE_flush           = Output(Bool())
  val MEM_stall           = Output(Bool())
  val WB_stall            = Output(Bool())
  // state transfer
  val controller_state = Output(ControllerState())
}
