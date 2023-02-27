package myImplement.ModuleIF

import chisel3._
import chisel3.util._
import chisel3.experimental.ChiselEnum

import myImplement.AXI.AXIMasterIF
import myImplement.Memory.DataMemoryIO

class Wrapper_to_CPU_IO_from_Controller extends Bundle {
  val toRead     = Input(Bool())
  val toWrite    = Input(Bool())
  val write_strb = Input(UInt(4.W))
  val length     = Input(UInt(8.W))
  val start      = Output(Bool()) // indicate CPU that it can start to send or receive data
  val done       = Output(Bool()) // indicate CPU that transaction is done
}

class Wrapper_to_CPU_IO_from_Datapath(memAddrWidth: Int, memDataWidth: Int) extends Bundle {
  val baseAddr   = Input(UInt(memAddrWidth.W))
  val readData   = Output(Valid(UInt(memDataWidth.W)))
  val writeData  = Input(Valid(UInt(memDataWidth.W)))
  val countValue = Output(UInt(4.W)) // current value in burst_counter
}

class Wrapper_to_CPU_IO(memAddrWidth: Int, memDataWidth: Int) extends Bundle {
  val from_controller = new Wrapper_to_CPU_IO_from_Controller
  val from_datapath   = new Wrapper_to_CPU_IO_from_Datapath(memAddrWidth, memDataWidth)
}

class CPU_AXI_WrapperIO(memAddrWidth: Int, memDataWidth: Int) extends Bundle {
  val to_cpu     = new Wrapper_to_CPU_IO(memAddrWidth, memDataWidth)
  val to_AXI_bus = new AXIMasterIF(memAddrWidth, memDataWidth)
}

object CPU_AXI_Wrapper_State extends ChiselEnum {
  val sIdle, sReadAddrSend, sReadDataWait, sWriteAddrSend, sWriteSendData, sWriteRespWait = Value
}

/** this module is designed to help cpu connect to the AXI bus
  *
  * with burst mode support
  */
class CPU_AXI_Wrapper(memAddrWidth: Int, memDataWidth: Int) extends Module {
  import CPU_AXI_Wrapper_State._

  val io = IO(new CPU_AXI_WrapperIO(memAddrWidth, memDataWidth))

  // * registers * //
  val state         = RegInit(sIdle) // state register
  val length        = RegInit(0.U(8.W))
  val burst_counter = RegInit(0.U(4.W))
  // * wires * //
  val writeLast = WireDefault(burst_counter === length)

  // * Fixed configuration * //
  // burst mode => INCR (0b01)
  io.to_AXI_bus.readAddr.bits.burst  := "b01".U
  io.to_AXI_bus.writeAddr.bits.burst := "b01".U
  // burst size -> 4 bytes (0b010)
  io.to_AXI_bus.readAddr.bits.size  := "b010".U
  io.to_AXI_bus.writeAddr.bits.size := "b010".U

  // * next state logic * //
  switch(state) {
    is(sIdle) {
      state := Mux(
        io.to_cpu.from_controller.toRead,
        sReadAddrSend,
        Mux(
          io.to_cpu.from_controller.toWrite,
          sWriteAddrSend,
          sIdle
        )
      )
    }
    is(sReadAddrSend) {
      state := Mux(io.to_AXI_bus.readAddr.ready, sReadDataWait, sReadAddrSend)
    }
    is(sReadDataWait) {
      state := Mux(io.to_AXI_bus.readData.bits.last & io.to_AXI_bus.readData.valid, sIdle, sReadDataWait)
    }
    is(sWriteAddrSend) {
      state := Mux(io.to_AXI_bus.writeAddr.ready, sWriteSendData, sWriteAddrSend)
    }
    is(sWriteSendData) {
      state := Mux(
        io.to_AXI_bus.writeData.ready & writeLast & io.to_cpu.from_datapath.writeData.valid,
        sWriteRespWait,
        sWriteSendData
      )
    }
    is(sWriteRespWait) {
      state := Mux(io.to_AXI_bus.writeResp.valid, sIdle, sWriteRespWait)
    }
  }

  // * internal register update logic * //
  switch(state) {
    is(sIdle) {
      // initialize length and burst_counter
      length := Mux(
        io.to_cpu.from_controller.toRead | io.to_cpu.from_controller.toWrite,
        io.to_cpu.from_controller.length,
        length
      )
    }
    is(sReadAddrSend) {
      // blank
    }
    is(sReadDataWait) {
      burst_counter := Mux(
        io.to_AXI_bus.readData.valid & (~io.to_AXI_bus.readData.bits.last),
        burst_counter + 1.U,
        Mux(
          io.to_AXI_bus.readData.valid & io.to_AXI_bus.readData.bits.last,
          0.U, // reset burst counter
          burst_counter
        )
      )
    }
    is(sWriteAddrSend) {
      // blank
    }
    is(sWriteSendData) {
      burst_counter := Mux(
        io.to_AXI_bus.writeData.ready & io.to_cpu.from_datapath.writeData.valid,
        burst_counter + 1.U,
        burst_counter
      )
    }
    is(sWriteRespWait) {
      burst_counter := 0.U // reset burst counter
    }
  }

  // * output decoder * //
  // default value
  io.to_AXI_bus.readAddr.valid           := false.B
  io.to_AXI_bus.writeAddr.valid          := false.B
  io.to_AXI_bus.readData.ready           := false.B
  io.to_AXI_bus.writeData.valid          := false.B
  io.to_AXI_bus.writeResp.ready          := false.B
  io.to_cpu.from_datapath.readData.valid := false.B
  io.to_cpu.from_controller.start        := false.B
  io.to_cpu.from_controller.done         := false.B

  switch(state) {
    is(sIdle) {
      // blank
    }
    is(sReadAddrSend) {
      // to cpu
      io.to_cpu.from_controller.start := io.to_AXI_bus.readAddr.ready
      // to AXI
      io.to_AXI_bus.readAddr.valid := true.B
    }
    is(sReadDataWait) {
      // to cpu
      io.to_cpu.from_datapath.readData.valid := io.to_AXI_bus.readData.valid
      io.to_cpu.from_controller.done         := io.to_AXI_bus.readData.valid & io.to_AXI_bus.readData.bits.last
      // to AXI
      io.to_AXI_bus.readAddr.valid := true.B
      io.to_AXI_bus.readData.ready := true.B
    }
    is(sWriteAddrSend) {
      // to cpu
      io.to_cpu.from_controller.start := io.to_AXI_bus.writeAddr.ready
      // to AXI
      io.to_AXI_bus.writeAddr.valid := true.B
    }
    is(sWriteSendData) {
      // to AXI
      io.to_AXI_bus.writeAddr.valid := true.B
      io.to_AXI_bus.writeData.valid := io.to_cpu.from_datapath.writeData.valid
    }
    is(sWriteRespWait) {
      // to cpu
      io.to_cpu.from_controller.done := io.to_AXI_bus.writeResp.valid
      // to AXI
      io.to_AXI_bus.writeResp.ready := true.B
    }
  }

  // * connect directly * //
  // to cpu
  io.to_cpu.from_datapath.readData.bits := io.to_AXI_bus.readData.bits.data
  io.to_cpu.from_datapath.countValue    := burst_counter
  // to AXI
  io.to_AXI_bus.readAddr.bits.len   := length
  io.to_AXI_bus.writeAddr.bits.len  := length
  io.to_AXI_bus.readAddr.bits.addr  := io.to_cpu.from_datapath.baseAddr
  io.to_AXI_bus.writeAddr.bits.addr := io.to_cpu.from_datapath.baseAddr
  io.to_AXI_bus.writeData.bits.strb := io.to_cpu.from_controller.write_strb
  io.to_AXI_bus.writeData.bits.data := io.to_cpu.from_datapath.writeData.bits
  io.to_AXI_bus.writeData.bits.last := writeLast
}
