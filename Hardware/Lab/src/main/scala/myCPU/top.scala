package myCPU

import chisel3._
import chisel3.util._

class topIO extends Bundle {
  //
}

class top extends Bundle {
  //
}

object top extends App {
  emitVerilog(
    new top,
    Array("--target-dir", "./generated/top")
  )
}
