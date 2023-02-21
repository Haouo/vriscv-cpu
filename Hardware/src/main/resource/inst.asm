li ra,16
nop 
nop 
nop 
nop 
nop 
slli sp,ra,0x3
nop 
nop 
nop 
nop 
nop 
xori gp,sp,-2048
nop 
nop 
nop 
nop 
nop 
slti tp,sp,-3
nop 
nop 
nop 
nop 
nop 
sltiu t0,sp,-3
nop 
nop 
nop 
nop 
nop 
srli t1,gp,0x2
nop 
nop 
nop 
nop 
nop 
srai t2,gp,0x2
nop 
nop 
nop 
nop 
nop 
ori s0,zero,123
nop 
nop 
nop 
nop 
nop 
andi s1,t0,-1
nop 
nop 
nop 
nop 
nop 
add a0,t0,t1
nop 
nop 
nop 
nop 
nop 
sub a1,gp,t2
nop 
nop 
nop 
nop 
nop 
sll a2,t0,s0
nop 
nop 
nop 
nop 
nop 
slt a3,t2,s1
nop 
nop 
nop 
nop 
nop 
sltu a4,s1,sp
nop 
nop 
nop 
nop 
nop 
xor a5,a1,gp
nop 
nop 
nop 
nop 
nop 
srl a6,a3,tp
nop 
nop 
nop 
nop 
nop 
sra a7,a5,t0
nop 
nop 
nop 
nop 
nop 
or s2,a7,ra
nop 
nop 
nop 
nop 
nop 
and s3,a3,a0
nop 
nop 
nop 
nop 
nop 
sb gp,3(zero)
nop 
nop 
nop 
nop 
nop 
sw ra,4(zero)
nop 
nop 
nop 
nop 
nop 
lb s4,3(zero)
nop 
nop 
nop 
nop 
nop 
lw s5,4(zero)
nop 
nop 
nop 
nop 
nop 
lbu s6,3(zero)
nop 
nop 
nop 
nop 
nop 
lui s8,0x1238
nop 
nop 
nop 
nop 
nop 
beq ra,sp,<hello>
nop 
nop 
nop 
nop 
nop 
bne ra,ra,<hello>
nop 
nop 
nop 
nop 
nop 
bltz ra,<hello>
nop 
nop 
nop 
nop 
nop 
bgez gp,<hello>
nop 
nop 
nop 
nop 
nop 
bltu gp,a1,<hello>
nop 
nop 
nop 
nop 
nop 
bgeu a1,gp,<hello>
nop 
nop 
nop 
nop 
nop 
jalr s9,752(zero)
nop 
nop 
nop 
nop 
nop 
add a0,t0,t1
nop 
nop 
nop 
nop 
nop 
j <exit>
nop 
nop 
nop 
nop 
nop 
sll a2,t0,s0
nop 
nop 
nop 
nop 
nop 
hcf
