package myImplement

class TopIO extends Bundle {
  //
}

class Top extends Module {
  val io = IO(new TopIO)

  //
}

object Top extends App {
  emitVerilog(
    new Top,
    Array("--target-dir", "./generated/Top")
  )
}
