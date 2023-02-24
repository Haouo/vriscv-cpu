

import chisel3._
import chisel3.util._

import lab10.AXI._

/* Direct Memory InterFace */
class AXI_IF(memAddrWidth: Int, memDataWidth: Int, busDataWidth: Int) extends Module {
  val io = IO(new Bundle {
    val memIF = new MemIF_MEM(memAddrWidth, memDataWidth)
    // AXI
    val bus_master = new AXIMasterIF(memAddrWidth, busDataWidth)
  })

  // AXI
  val Mem_raddr = RegInit(0.U(memAddrWidth.W))
  val Mem_waddr = RegInit(0.U(memAddrWidth.W))
  val Wlast = Wire(Bool())
  Wlast := true.B // 1 transfer

	io.bus_master.readAddr.bits.addr := io.memIF.raddr

	io.bus_master.readAddr.bits.len := 0.U // 1 transfer (2 transfers if Vector Load/Store)
	io.bus_master.readAddr.bits.size := "b010".U // 4bytes
	io.bus_master.readAddr.bits.burst := "b01".U // INCR

	io.memIF.rdata := Cat(0.U((memDataWidth-busDataWidth).W),io.bus_master.readData.bits.data) // lower 32 bits are values

  io.bus_master.writeAddr.bits.addr := io.memIF.waddr

	io.bus_master.writeAddr.bits.len := 0.U // 1 transfer (2 transfers if Vector Load/Store)
	io.bus_master.writeAddr.bits.size := "b010".U // 4bytes
	io.bus_master.writeAddr.bits.burst := "b01".U // INCR

	io.bus_master.writeData.bits.data := io.memIF.wdata(busDataWidth-1,0) // lower 32 bits are values
	io.bus_master.writeData.bits.strb := MuxLookup(io.memIF.Length, 0.U, Seq(
        "b0000".U(4.W) -> "b1".U,
        "b0001".U(4.W) -> "b11".U,
        "b0010".U(4.W) -> "b1111".U
      ))
      
	io.bus_master.writeData.bits.last := Wlast // transfer are always 1 (2 transfers if Vector Load/Store)
  
  val sNormal :: sAXIReadSend :: sAXIReadWait :: sAXIWriteSendAddr :: sAXIWriteSendData :: sAXIWriteWait :: Nil = Enum(6)

  val MemAccessState = RegInit(sNormal)

  // MemAccessState - next state decoder
  switch(MemAccessState) {
    is(sNormal) {
      when(io.memIF.Mem_R) {
        MemAccessState := Mux(io.bus_master.readAddr.ready, sAXIReadWait, sAXIReadSend)
      }.elsewhen(io.memIF.Mem_W) {
        MemAccessState := Mux(io.bus_master.writeAddr.ready, sAXIWriteWait, sAXIWriteSendAddr)
      }.otherwise {
        MemAccessState := sNormal
      }
    }
    is(sAXIReadSend) {
      MemAccessState := Mux(io.bus_master.readAddr.ready, sAXIReadWait, sAXIReadSend)
    }
    is(sAXIReadWait) {
      MemAccessState := Mux(io.bus_master.readData.valid & io.bus_master.readData.bits.last, sNormal, sAXIReadWait)
    }
    is(sAXIWriteSendAddr) {
        MemAccessState := Mux(io.bus_master.writeAddr.ready, sAXIWriteSendData, sAXIWriteSendAddr)
    }
    is(sAXIWriteSendData) {
        MemAccessState := Mux(io.bus_master.writeData.ready & Wlast, sAXIWriteWait, sAXIWriteSendData)
    }
    is(sAXIWriteWait) {
      MemAccessState := Mux(io.bus_master.writeResp.valid, sNormal, sAXIWriteWait)
    }
  }

  // AXI output gnenrator
  io.bus_master.readAddr.valid := false.B
  io.bus_master.readData.ready := false.B
  io.bus_master.writeAddr.valid := false.B
  io.bus_master.writeData.valid := false.B
  io.bus_master.writeResp.ready := false.B

  io.memIF.Valid := io.bus_master.writeResp.valid || (io.bus_master.readData.valid & io.bus_master.readData.bits.last)

  switch(MemAccessState) {
    is(sNormal) {
      io.bus_master.readAddr.valid := io.memIF.Mem_R
      io.bus_master.writeAddr.valid := (io.memIF.Mem_W & io.bus_master.writeAddr.ready)
      io.bus_master.writeData.valid := (io.memIF.Mem_W & io.bus_master.writeData.ready)
    }
    is(sAXIReadSend) {
      io.bus_master.readAddr.valid := Mux(io.bus_master.readAddr.ready, true.B, false.B)
    }
    is(sAXIReadWait) {
      io.bus_master.readData.ready := true.B
    }
    is(sAXIWriteSendAddr) {
      io.bus_master.writeAddr.valid := (io.memIF.Mem_W & io.bus_master.writeAddr.ready)
    }
    is(sAXIWriteSendData) {
      io.bus_master.writeData.valid := (io.memIF.Mem_W & io.bus_master.writeData.ready)
    }
    is(sAXIWriteWait) {
      io.bus_master.writeResp.ready := true.B
    }
  }

  // Address
  switch(MemAccessState) {
    is(sNormal) {
      Mem_raddr := io.memIF.raddr
      Mem_waddr := io.memIF.waddr
    }
    is(sAXIReadSend) {
      Mem_raddr := Mem_raddr
      Mem_waddr := Mem_waddr
    }
    is(sAXIReadWait) {
      Mem_raddr := Mux(io.bus_master.readData.valid, Mem_raddr+4.U(memAddrWidth.W) ,Mem_raddr)
      Mem_waddr := Mem_waddr
    }
    is(sAXIWriteSendAddr) {
      Mem_raddr := Mem_raddr
      Mem_waddr := Mem_waddr
    }
    is(sAXIWriteSendData) {
      Mem_raddr := Mem_raddr
      Mem_waddr := Mux(io.bus_master.writeData.ready, Mem_waddr+4.U(memAddrWidth.W) ,Mem_waddr)
    }
    is(sAXIWriteWait) {
      Mem_raddr := Mem_raddr
      Mem_waddr := Mem_waddr
    }
  }
}