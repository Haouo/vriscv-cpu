package myImplement.AXI

import chisel3._
import chisel3.util._

// * the required signals on an AXI4 interface
// * 0. Global
// ACLK
// ARESETn
// * 1. Write address channel
// AWVALID
// AWREADY
// AWADDR
// AWLEN (New)
// AWSIZE (New)
// AWBURST (New)
// * 2. Write data channel
// WVALID
// WREADY
// WDATA
// WLAST (New)
// WSTRB
// * 3. Write response channel
// BVALID
// BREADY
// BRESP
// * 4. Read address channel
// ARVALID
// ARREADY
// ARADDR
// ARLEN (New)
// ARSIZE (New)
// ARBURST (New)
// * 5. Read data channel
// RVALID
// RREADY
// RDATA
// RLAST (New)
// RRESP

class AXIAddress(val addrWidth: Int) extends Bundle {
  val addr  = UInt(addrWidth.W)
  val len   = UInt(8.W) // INCR mode:1~256 transfers, other modes: 1~16 transfers (Burst Length)
  val size  = UInt(3.W) // 1,2,4,8,16,32,64,128 Bytes
  val burst = UInt(2.W) // FIXED, INCR, WRAP

  // override def clone = { new AXIAddress(addrWidth).asInstanceOf[this.type] }
}

class AXIWriteData(val dataWidth: Int) extends Bundle {
  val data = UInt(dataWidth.W)
  val strb = UInt((dataWidth / 8).W) // byte masked
  val last = Bool()

  override def clone = { new AXIWriteData(dataWidth).asInstanceOf[this.type] }
}

class AXIReadData(val dataWidth: Int) extends Bundle {
  val data = UInt(dataWidth.W)
  val resp = UInt(2.W)
  val last = Bool()

  override def clone = { new AXIReadData(dataWidth).asInstanceOf[this.type] }
}

class AXISlaveIF(val addrWidth: Int, val dataWidth: Int) extends Bundle {
  // write address channel
  val writeAddr = Flipped(Decoupled(new AXIAddress(addrWidth)))
  // write data channel
  val writeData = Flipped(Decoupled(new AXIWriteData(dataWidth)))
  // write response channel
  val writeResp = Decoupled(UInt(2.W))
  // read address channel
  val readAddr = Flipped(Decoupled(new AXIAddress(addrWidth)))
  // read data channel
  val readData = Decoupled(new AXIReadData(dataWidth))

  override def clone = { new AXISlaveIF(addrWidth, dataWidth).asInstanceOf[this.type] }
}

class AXIMasterIF(val addrWidth: Int, val dataWidth: Int) extends Bundle {
  // write address channel
  val writeAddr = Decoupled(new AXIAddress(addrWidth))
  // write data channel
  val writeData = Decoupled(new AXIWriteData(dataWidth))
  // write response channel
  val writeResp = Flipped(Decoupled(UInt(2.W)))
  // read address channel
  val readAddr = Decoupled(new AXIAddress(addrWidth))
  // read data channel
  val readData = Flipped(Decoupled(new AXIReadData(dataWidth)))

  override def clone: AXIMasterIF = { new AXIMasterIF(addrWidth, dataWidth).asInstanceOf[this.type] }
}
