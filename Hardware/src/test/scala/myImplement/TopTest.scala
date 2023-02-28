package myImplement

import chisel3._
import chisel3.util._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec
import scala.io.Source
import scala.language.implicitConversions

class TopTest extends AnyFlatSpec with ChiselScalatestTester {
  // * testing config * //
  val opts            = Seq(VerilatorBackendAnnotation, WriteVcdAnnotation)
  val max_clock_limit = 100_0000
  val file_name       = "./src/main/resource/inst.asm"
  val lines           = Source.fromFile(file_name).getLines().toList

  // * Implicit Conversion * //
  implicit def Boolean2Int(a: Boolean): Int = if (a) 1 else 0

  // * testing body * //
  "Pipeline CPU" should "pass test program" in {
    test(new Top).withAnnotations(opts) { dut =>
      // * set max clock cycle limit * //
      dut.clock.setTimeout(max_clock_limit)

      // * Performance counters * //
      var cycle_count                    = 0
      var inst_count                     = 0
      var conditional_branch_count       = 0
      var conditional_branch_hit_count   = 0
      var unconditional_branch_count     = 0
      var unconditional_branch_hit_count = 0
      var flush_count                    = 0 // only count flush due to incorrect branch
      var mem_read_stall_count           = 0
      var mem_write_stall_count          = 0
      var mem_read_request_count         = 0
      var mem_write_request_count        = 0
      var mem_read_bytes_count           = 0
      var mem_write_bytes_count          = 0

      // run program
      while (!dut.io.isHcf.peekBoolean()) {
        // branch
        val EXE_branch:     Int = dut.io.EXE_branch.peekBoolean()
        val EXE_jump:       Int = dut.io.EXE_jump.peekBoolean()
        val E_branch_taken: Int = dut.io.E_branch_taken.peekBoolean()
        // stall & flush
        val stall_MA: Int = dut.io.stall_MA.peekBoolean()
        val flush:    Int = dut.io.flush.peekBoolean()
        val stall_DH: Int = dut.io.stall_DH.peekBoolean()
        // PCs
        val IF_pc  = dut.io.IF_pc.peekInt().toInt
        val ID_pc  = dut.io.ID_pc.peekInt().toInt
        val EXE_pc = dut.io.EXE_pc.peekInt().toInt
        val MEM_pc = dut.io.MEM_pc.peekInt().toInt
        val WB_pc  = dut.io.WB_pc.peekInt().toInt
        // EXE Stage
        val EXE_src1    = dut.io.EXE_src1.peekInt().toInt.toHexString.replace(' ', '0')
        val EXE_src2    = dut.io.EXE_src2.peekInt().toInt.toHexString.replace(' ', '0')
        val ALU_src1    = dut.io.ALU_src1.peekInt().toInt.toHexString.replace(' ', '0')
        val ALU_src2    = dut.io.ALU_src2.peekInt().toInt.toHexString.replace(' ', '0')
        val EXE_alu_out = dut.io.EXE_alu_out.peekInt().toInt.toHexString.replace(' ', '0')
        // MEM Stage
        val MEM_raddr = dut.io.MEM_raddr.peekInt().toInt.toHexString.replace(' ', '0')
        val MEM_rdata = dut.io.MEM_rdata.peekInt().toInt.toHexString.replace(' ', '0')
        // WB Stage
        val WB_rd    = dut.io.WB_rd.peekInt().toInt.toHexString.replace(' ', '0')
        val WB_wdata = dut.io.WB_wdata.peekInt().toInt.toHexString.replace(' ', '0')

        // * print information * //
        println(
          s"[PC_IF ]${"%8s".format(IF_pc.toHexString.replace(' ', '0'))} [Inst] ${"%-25s".format(lines(IF_pc >> 2))} "
        )
        println(
          s"[PC_ID ]${"%8s".format(ID_pc.toHexString.replace(' ', '0'))} [Inst] ${"%-25s".format(lines(ID_pc >> 2))} "
        )
        println(
          s"[EXE_pc]${"%8s".format(EXE_pc.toHexString.replace(' ', '0'))} [Inst] ${"%-25s".format(lines(EXE_pc >> 2))} " +
            s"[EXE src1]${"%8s".format(EXE_src1)} [EXE src2]${"%8s".format(EXE_src2)} " +
            s"[Br taken] ${"%1d".format(E_branch_taken)} "
        )
        println(
          s"                                                  " +
            s"[ALU src1]${"%8s".format(ALU_src1)} [ALU src2]${"%8s".format(ALU_src2)} " +
            s"[ALU Out]${"%8s".format(EXE_alu_out)}"
        )
        println(
          s"[MEM_pc]${"%8s".format(MEM_pc.toHexString.replace(' ', '0'))} [Inst] ${"%-25s".format(lines(MEM_pc >> 2))} " +
            s"[DM Raddr]${"%8s".format(MEM_raddr)} [DM Rdata]${"%8s".format(MEM_rdata)}"
        )
        println(
          s"[WB_pc ]${"%8s".format(WB_pc.toHexString.replace(' ', '0'))} [Inst] ${"%-25s".format(lines(WB_pc >> 2))} " +
            s"[ WB rd index ]${"%8s".format(WB_rd)} [WB  data]${"%8s".format(WB_wdata)}"
        )
        println(
          s"[Flush ] ${"%1d".format(flush)} [Stall_MA ] ${"%1d".format(stall_MA)} [Stall_DH ] ${"%1d".format(stall_DH)} "
        )
        println("==============================================")

        dut.clock.step()
        cycle_count += 1

        // * performance counters * //
        if (dut.io.stall_DH.peekBoolean() == false && dut.io.stall_MA.peekBoolean() == false) inst_count += 1
        if (dut.io.EXE_branch.peekBoolean()) conditional_branch_count += 1
        if (dut.io.EXE_branch.peekBoolean() && (!dut.io.E_branch_taken.peekBoolean())) conditional_branch_hit_count += 1
        if (dut.io.EXE_jump.peekBoolean()) unconditional_branch_count += 1
        if (dut.io.EXE_jump.peekBoolean() && (!dut.io.E_branch_taken.peekBoolean())) unconditional_branch_hit_count += 1
        if (dut.io.flush.peekBoolean()) flush_count += 1
        if (dut.io.dataMemIsIdle.peekBoolean() && dut.io.readAddrValid.peekBoolean()) mem_read_request_count += 1
        if (dut.io.dataMemIsIdle.peekBoolean() && dut.io.writeAddrValid.peekBoolean()) mem_write_request_count += 1
        if (dut.io.isRead.peekBoolean() && dut.io.stall_MA.peekBoolean()) mem_read_stall_count += 1
        if (dut.io.isWrite.peekBoolean() && dut.io.stall_MA.peekBoolean()) mem_write_stall_count += 1
        if (dut.io.dataMemIsIdle.peekBoolean() && dut.io.readAddrValid.peekBoolean())
          mem_read_bytes_count += (dut.io.readLength.peekInt().toInt + 1) * 4
        if (dut.io.dataMemIsIdle.peekBoolean() && dut.io.writeAddrValid.peekBoolean())
          mem_write_bytes_count += (dut.io.writeLength.peekInt().toInt + 1) * 4
      }

      // * end simulation * //
      dut.clock.step()
      cycle_count += 1

      println("Inst:Hcf")
      println("This is the end of the program!!")
      println("==============================================")

      // * print results in RegFile * //
      println("Value in the RegFile")
      for (i <- 0 until 4) {
        var value_0 = String.format("%" + 8 + "s", dut.io.regs(8 * i + 0).peekInt().toInt.toHexString).replace(' ', '0')
        var value_1 = String.format("%" + 8 + "s", dut.io.regs(8 * i + 1).peekInt().toInt.toHexString).replace(' ', '0')
        var value_2 = String.format("%" + 8 + "s", dut.io.regs(8 * i + 2).peekInt().toInt.toHexString).replace(' ', '0')
        var value_3 = String.format("%" + 8 + "s", dut.io.regs(8 * i + 3).peekInt().toInt.toHexString).replace(' ', '0')
        var value_4 = String.format("%" + 8 + "s", dut.io.regs(8 * i + 4).peekInt().toInt.toHexString).replace(' ', '0')
        var value_5 = String.format("%" + 8 + "s", dut.io.regs(8 * i + 5).peekInt().toInt.toHexString).replace(' ', '0')
        var value_6 = String.format("%" + 8 + "s", dut.io.regs(8 * i + 6).peekInt().toInt.toHexString).replace(' ', '0')
        var value_7 = String.format("%" + 8 + "s", dut.io.regs(8 * i + 7).peekInt().toInt.toHexString).replace(' ', '0')

        println(
          s"reg[${"%02d".format(8 * i + 0)}]：${value_0} " +
            s"reg[${"%02d".format(8 * i + 1)}]：${value_1} " +
            s"reg[${"%02d".format(8 * i + 2)}]：${value_2} " +
            s"reg[${"%02d".format(8 * i + 3)}]：${value_3} " +
            s"reg[${"%02d".format(8 * i + 4)}]：${value_4} " +
            s"reg[${"%02d".format(8 * i + 5)}]：${value_5} " +
            s"reg[${"%02d".format(8 * i + 6)}]：${value_6} " +
            s"reg[${"%02d".format(8 * i + 7)}]：${value_7} "
        )
      }

      /* Lab 10_4 Modification of Vector Extension */
      println("")
      println("Value in the Vector RegFile")
      for (i <- 0 until 32) {
        val value_0 = dut.io.vector_regs(i).peekInt()
        println(s"vector_reg[${"%02d".format(i)}]s:" + f"$value_0%0128X")
      }

      // * print performance counters * //s
      println("")
      println("=============== Performance Counter ===============")
      println(s"cycle count: $cycle_count")
      println(s"instruction count: $inst_count")
      println(s"Conditional Branch Count: $conditional_branch_count")
      println(s"Conditional Branch Hit Count: $conditional_branch_hit_count")
      println(s"Unconditional Branch Count: $unconditional_branch_count")
      println(s"Unconditional Branch Hit Count: $unconditional_branch_hit_count")
      println(s"Flush Count: $flush_count")
      println(s"Memory Read Request Count: $mem_read_request_count")
      println(s"Memory Write Request Count: $mem_write_request_count")
      println(s"Memory Read Stall Cycle Count: $mem_read_stall_count")
      println(s"Memory Write Stall Cycle Count: $mem_write_stall_count")
      println(s"Memory Read Bytes Count: $mem_read_bytes_count")
      println(s"Memory Write Bytes Count: $mem_write_bytes_count")
      println("")

      println("=============== Performance Analysis ===============")
      println(f"CPI: ${cycle_count.asInstanceOf[Double] / inst_count.asInstanceOf[Double]}%.3f")
      println(
        f"Average Memory Read Request Stall Cycle: ${mem_read_stall_count.asInstanceOf[Double] / mem_read_request_count.asInstanceOf[Double]}%.3f"
      )
      println(f"Average Memory Write Request Stall Cycle: ${mem_write_stall_count
          .asInstanceOf[Double] / mem_write_request_count.asInstanceOf[Double]}%.3f")
      println(s"Total Bus Bandwidth request: ${mem_read_bytes_count + mem_write_bytes_count} bytes")
      println("")
    }
  }
}
