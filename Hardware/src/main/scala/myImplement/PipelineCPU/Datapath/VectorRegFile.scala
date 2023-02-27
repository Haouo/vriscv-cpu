package myImplement.PipelineCPU.Datapath

import chisel3._
import chisel3.util._
class VectorRegFileIO extends Bundle {
  val wEnable   = Input(Bool())
  val writeData = Input(UInt(512.W))
  val vd_index  = Input(UInt(5.W))
  val vs1_index = Input(UInt(5.W))
  val vs2_index = Input(UInt(5.W))
  val vs1_data  = Output(UInt(512.W))
  val vs2_data  = Output(UInt(512.W))
  // test port
  val v_regs = Output(Vec(32, UInt(512.W)))
}

class VectorRegFile extends Module {
  val io    = IO(new VectorRegFileIO)
  val v_reg = Mem(32, UInt(512.W))

  // $v0 is always 0
  v_reg(0) := 0.U

  // for test
  // val init_value = Seq(0.U(512.W)) ++
  // Seq("h000102030405060708090a0b0c0d0e0f101112131415161718191a1b1f1d1e1f".U(512.W)) ++
  // Seq("h000102030405060708090a0b0c0d0e0f101112131415161718191a1b1f1d1e1f".U(512.W)) ++
  // Seq.fill(29)(0.U(512.W))
  // val v_reg = RegInit(VecInit(init_value))

  // read reg
  io.vs1_data := v_reg(io.vs1_index)
  io.vs2_data := v_reg(io.vs2_index)
  // write reg
  v_reg(io.vd_index) := Mux(io.wEnable, io.writeData, v_reg(io.vd_index))

  // for test
  for (i <- 0 until 32) { io.v_regs(i) := v_reg(i) }
}
