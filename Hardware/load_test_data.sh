#!/bin/bash

src_dir=.

if [ -n "$1" ]
then
    if [ "$1" = "Emulator" ]
    then
	set -x
        cp ../Emulator/test_code/inst.asm $src_dir/src/main/resource/inst.asm
        cp ../Emulator/test_code/inst.hex $src_dir/src/main/resource/inst.hex
	cp ../Emulator/test_code/data.hex $src_dir/src/main/resource/data.hex
    elif [ "$1" = "-s" ]
    then
	set -x
        cp $src_dir/riscv-test/out/asm/rv32ui_SingleTest-$2.asm $src_dir/src/main/resource/inst.asm
        cp $src_dir/riscv-test/out/hex/text/rv32ui_SingleTest-$2.hex $src_dir/src/main/resource/inst.hex
	cp $src_dir/riscv-test/out/hex/data/rv32ui_SingleTest-$2.hex $src_dir/src/main/resource/data.hex
    else
	set -x
        cp $src_dir/riscv-test/out/asm/rv32ui_FullTest-$1.asm $src_dir/src/main/resource/inst.asm
        cp $src_dir/riscv-test/out/hex/text/rv32ui_FullTest-$1.hex $src_dir/src/main/resource/inst.hex
        cp $src_dir/riscv-test/out/hex/data/rv32ui_FullTest-$1.hex $src_dir/src/main/resource/data.hex
    fi
else
    echo "[Error] usage should be: ./test_data.sh <which Test program> (Emulator/(-s) <inst code>)"
fi
