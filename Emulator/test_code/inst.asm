lui x05, 0x00000008
addi x05, x05, 0x00000000
nop zero, zero, 0
nop zero, zero, 0
nop zero, zero, 0
nop zero, zero, 0
nop zero, zero, 0
vle8_v v1, 0(t0)
nop zero, zero, 0
nop zero, zero, 0
nop zero, zero, 0
nop zero, zero, 0
nop zero, zero, 0
vadd_vv v2, v1, v1
nop zero, zero, 0
nop zero, zero, 0
nop zero, zero, 0
nop zero, zero, 0
nop zero, zero, 0
addi t0, t0, 128
nop zero, zero, 0
nop zero, zero, 0
nop zero, zero, 0
nop zero, zero, 0
nop zero, zero, 0
vse8_v v2, 0(t0)
nop zero, zero, 0
nop zero, zero, 0
nop zero, zero, 0
nop zero, zero, 0
nop zero, zero, 0
vle8_v v3, 0(t0)
nop zero, zero, 0
nop zero, zero, 0
nop zero, zero, 0
nop zero, zero, 0
nop zero, zero, 0
lui x06, 0x00000000
addi x06, x06, 0x00000002
nop zero, zero, 0
nop zero, zero, 0
nop zero, zero, 0
nop zero, zero, 0
nop zero, zero, 0
vmul_vx v4, v3, t1
nop zero, zero, 0
nop zero, zero, 0
nop zero, zero, 0
nop zero, zero, 0
nop zero, zero, 0
hcf
nop
nop
nop
nop
nop
hcf
