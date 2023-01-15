package lab10.PiplinedCPU.DatapathModule

import chisel3._
import chisel3.util._
import lab10.PiplinedCPU.pc_sel_map._

class Path_IF(addrWidth:Int) extends Module {
    val io = IO(new Bundle{

        val PCSel = Input(UInt(2.W))
        val IF_pc_in = Input(UInt(addrWidth.W))
        val EXE_pc_in = Input(UInt(addrWidth.W))
        val EXE_target_pc_in = Input(UInt(addrWidth.W))
        val Mem_data = Input(UInt(32.W))
        

        val next_pc = Output(UInt(addrWidth.W))
        val Mem_Addr = Output(UInt(addrWidth.W))
        val inst = Output(UInt(32.W))
        
        // Lab12. Branch Prediction
        val E_En = Input(Bool())
        val E_Branch_taken = Input(Bool())
        val BP_taken = Output(Bool())
    })

    /*** Lab12. Branch Prediction ***/
    val Predict_Target_pc = Wire(UInt(addrWidth.W))
    io.BP_taken := false.B
    Predict_Target_pc := 0.U
    // (E_En)           io.E_En
    // (E_Branch_taken) io.E_Branch_taken
    // (EXE_target_pc)  io.EXE_target_pc_in 
    // (EXE_pc)         io.EXE_pc_in
    // (IF_pc)          io.IF_pc_in

    // Next PC combinational circuit
    io.next_pc := MuxLookup(io.PCSel, (io.IF_pc_in + 4.U(addrWidth.W)), Seq(
        IF_PC_PLUS_4 -> (io.IF_pc_in + 4.U(addrWidth.W)),
        IF_P_T_PC -> Predict_Target_pc,
        EXE_PC_PLUS_4 -> (io.EXE_pc_in + 4.U(addrWidth.W)),
        EXE_T_PC -> io.EXE_target_pc_in
    ))
    // Instruction Memory
    io.Mem_Addr := io.IF_pc_in
    io.inst := io.Mem_data
}
