li gp,1
li t1,1
nop 
nop 
nop 
nop 
nop 
li t2,1
nop 
nop 
beq t2,t1,<test2>
nop 
nop 
nop 
nop 
nop 
beqz zero,<fail>
li gp,2
li t1,2
nop 
nop 
nop 
nop 
nop 
li t2,2
nop 
nop 
beq t1,t2,<test3>
nop 
nop 
nop 
nop 
nop 
beqz zero,<fail>
li gp,3
li t1,3
nop 
nop 
nop 
nop 
nop 
li t2,3
nop 
beq t2,t1,<test4>
nop 
nop 
nop 
nop 
nop 
beqz zero,<fail>
li gp,4
li t1,4
nop 
nop 
nop 
nop 
nop 
li t2,4
nop 
beq t1,t2,<test5>
nop 
nop 
nop 
nop 
nop 
beqz zero,<fail>
li gp,5
li t1,5
nop 
nop 
nop 
nop 
nop 
li t2,5
beq t2,t1,<test6>
nop 
nop 
nop 
nop 
nop 
beqz zero,<fail>
li gp,6
li t1,6
nop 
nop 
nop 
nop 
nop 
li t2,6
beq t1,t2,<test7>
nop 
nop 
nop 
nop 
nop 
beqz zero,<fail>
li gp,7
auipc t1,0x8
addi t1,t1,-388
li t2,255
nop 
nop 
nop 
nop 
nop 
lbu t3,0(t1)
beq t3,t2,<test8>
nop 
nop 
nop 
nop 
nop 
beqz zero,<fail>
li gp,8
auipc t1,0x8
addi t1,t1,-454
li t2,15
nop 
nop 
nop 
nop 
nop 
lbu t3,0(t1)
beq t3,t2,<test9>
nop 
nop 
nop 
nop 
nop 
beqz zero,<fail>
li gp,9
li t3,1
auipc t1,0x8
addi t1,t1,-528
lb t2,0(t1)
add t3,t2,t3
add t3,t3,t2
sub t3,t2,t3
beqz t3,<test10>
beqz zero,<fail>
li gp,10
auipc t3,0x8
addi t3,t3,-560
auipc t1,0x8
addi t1,t1,-572
sw t1,0(t3)
add t2,zero,t3
lw t2,0(t2)
lb t2,0(t2)
lb t1,0(t1)
beq t1,t2,<pass>
beqz zero,<fail>
li sp,0
beqz zero,<exit>
nop 
nop 
nop 
nop 
nop 
mv sp,gp
nop 
nop 
nop 
nop 
nop 
hcf
