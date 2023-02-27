.data
tdata1: .word 0x11111111
tdata2: .word 0x22222222
tdata3: .word 0x33333333
tdata4: .word 0x44444444
tdata5: .word 0x55555555
tdata6: .word 0x66666666
tdata7: .word 0x77777777
tdata8: .word 0x88888888
tdata9: .word 0x99999999
tdata10: .word 0xaaaaaaaa
tdata11: .word 0xbbbbbbbb
tdata12: .word 0xcccccccc
tdata13: .word 0xdddddddd
tdata14: .word 0xeeeeeeee
tdata15: .word 0xffffffff
tdata16: .word 0x12345678
.text
main:
la t0, tdata1
li t1, 1
li t2, 2
nop
nop
nop
nop
nop
vle8_v	v1, 0(t0)
bne t1, t2, end
nop
nop
nop
nop
nop
addi t0, t0, 128
nop
nop
nop
nop
nop
vse8_v	v1, 0(t0)
nop
nop
nop
nop
nop
vle8_v	v3, 0(t0)
nop
nop
nop
nop
nop
end:
hcf
