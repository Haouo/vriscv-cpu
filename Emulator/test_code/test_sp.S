.text
li sp, 0x10000
li t0, 1
li t1, 2
addi sp, sp, -8
sw t0, 0(sp)
sw t1, 4(sp)
nop
nop
nop
nop
nop
lw t2, 0(sp)
lw t3, 4(sp)
addi sp, sp, 8
nop
nop
nop
nop
nop
hcf
