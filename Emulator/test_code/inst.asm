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
vle8_v v2, 0(t0)
nop zero, zero, 0
nop zero, zero, 0
nop zero, zero, 0
nop zero, zero, 0
nop zero, zero, 0
vadd_vv v3, v1, v2
nop zero, zero, 0
nop zero, zero, 0
vadd_vv v3, v3, v1
vadd_vv v4, v1, v2
nop zero, zero, 0
nop zero, zero, 0
vadd_vv v4, v1, v4
addi t1, x0, 2
nop zero, zero, 0
nop zero, zero, 0
vmul_vx v5, v1, t1
vadd_vv v6, v1, v2
nop zero, zero, 0
vadd_vv v6, v6, v1
vadd_vv v7, v1, v2
nop zero, zero, 0
vadd_vv v7, v1, v7
addi t2, x0, 2
nop zero, zero, 0
vmul_vx v8, v1, t2
vadd_vv v9, v1, v2
vadd_vv v9, v9, v1
vadd_vv v10, v1, v2
vadd_vv v10, v1, v10
addi t3, x0, 2
vmul_vx v11, v1, t3
lui x29, 0x00000008
addi x29, x29, 0x00000000
vle8_v v12, 0(t4)
vadd_vv v12, v12, v1
lui x30, 0x00000008
addi x30, x30, 0x00000000
vle8_v v13, 0(t5)
vadd_vv v13, v1, v13
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
