package myImplement.Memory

import chisel3._
import chisel3.util._
import chisel3.util.experimental.loadMemoryFromFileInline
import chisel3.experimental.ChiselEnum

import myImplement.AXI.AXISlaveIF
import os.read

class DataMemoryIO(memAddrWidth: Int, memDataWidth: Int) extends Bundle {
  val to_bus = new AXISlaveIF(memAddrWidth, memDataWidth)
  // val addr      = Input(UInt(memAddrWidth.W))
  // val writeData = Input(UInt(memDataWidth.W))
  // val wEnable   = Input(UInt((memDataWidth / 8).W))
  // val readData  = Output(UInt(memDataWidth.W))
}

class DataMemory(memAddrWidth: Int, memDataWidth: Int) extends Module {
  object DataMemoryState extends ChiselEnum {
    val sIdle, sReadData, sWriteData, sWriteResp = Value
  }
  import DataMemoryState._

  val io = IO(new DataMemoryIO(memAddrWidth, memDataWidth))

  val byte = 8
  val mem  = Mem(1 << memAddrWidth, UInt(byte.W))

  // for AXI and burst mode supporting
  val state         = RegInit(sIdle)
  val length        = RegInit(0.U(4.W))
  val burst_counter = RegInit(0.U(4.W))
  val currentAddr   = RegInit(0.U(memAddrWidth.W))
  val readLast      = WireDefault(burst_counter === length)

  // pre-load memory
  loadMemoryFromFileInline(mem, "./src/main/resource/data.hex")

  // * next state logic * //
  switch(state) {
    is(sIdle) {
      state := Mux(
        io.to_bus.readAddr.valid,
        sReadData,
        Mux(
          io.to_bus.writeAddr.valid,
          sWriteData,
          sIdle
        )
      )
    }
    is(sReadData) {
      state := Mux(readLast & io.to_bus.readData.ready, sIdle, sReadData)
    }
    is(sWriteData) {
      state := Mux(io.to_bus.writeData.bits.last & io.to_bus.writeData.valid, sWriteResp, sWriteData)
    }
    is(sWriteResp) {
      state := Mux(io.to_bus.writeResp.ready, sIdle, sWriteResp)
    }
  }

  // * internal memory & register update logic * //
  switch(state) {
    is(sIdle) {
      length := Mux(
        io.to_bus.readAddr.valid,
        io.to_bus.readAddr.bits.len,
        Mux(
          io.to_bus.writeAddr.valid,
          io.to_bus.writeAddr.bits.len,
          length
        )
      )
      currentAddr := Mux(
        io.to_bus.readAddr.valid,
        io.to_bus.readAddr.bits.addr,
        Mux(
          io.to_bus.writeAddr.valid,
          io.to_bus.writeAddr.bits.addr,
          currentAddr
        )
      )
    }
    is(sReadData) {
      burst_counter := Mux(
        io.to_bus.readData.ready & (~readLast),
        burst_counter + 1.U,
        Mux(
          io.to_bus.readData.ready & readLast,
          0.U, // reset burst counter
          burst_counter
        )
      )
      currentAddr := Mux(
        io.to_bus.readData.ready,
        currentAddr + 4.U,
        currentAddr
      )
    }
    is(sWriteData) {
      burst_counter := Mux(
        io.to_bus.writeData.valid,
        burst_counter + 1.U,
        burst_counter
      )
      currentAddr := Mux(
        io.to_bus.writeData.valid,
        currentAddr + 4.U,
        currentAddr
      )
      // ! write memory only when state is sWriteData
      for (i <- 0 until 4) {
        mem(currentAddr + i.U) := Mux(
          io.to_bus.writeData.bits.strb(i).asBool,
          io.to_bus.writeData.bits.data(8 * i + 7, 8 * i),
          mem(currentAddr + i.U)
        )
      }
    }
    is(sWriteResp) {
      burst_counter := 0.U // reset burst counter
    }
  }

  // * output decoder * //
  // default value
  io.to_bus.readAddr.ready     := false.B
  io.to_bus.readData.valid     := false.B
  io.to_bus.readData.bits.last := readLast
  io.to_bus.readData.bits.resp := 0.U
  io.to_bus.writeAddr.ready    := false.B
  io.to_bus.writeData.ready    := false.B
  io.to_bus.writeResp.valid    := false.B
  io.to_bus.writeResp.bits     := 0.U
  io.to_bus.readData.bits.data := mem(currentAddr + 3.U) ## mem(currentAddr + 2.U) ## mem(currentAddr + 1.U) ## mem(
    currentAddr
  )

  switch(state) {
    is(sIdle) {
      io.to_bus.readAddr.ready  := true.B
      io.to_bus.writeAddr.ready := true.B
    }
    is(sReadData) {
      io.to_bus.readData.valid := true.B
    }
    is(sWriteData) {
      io.to_bus.writeData.ready := true.B
    }
    is(sWriteResp) {
      io.to_bus.writeResp.valid := true.B
    }
  }
}
