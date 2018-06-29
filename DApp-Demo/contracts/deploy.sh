#!/bin/bash

export PATH=/Users/leeyr/Documents/cryptape/code/cita-cli/target/debug:$PATH

# compile contacts
#rm *.bin
#solcjs voting.sol --bin &> compile.log

contract_bin=0x`cat voting_sol_Voting.bin`
echo "$contract_bin";

# deploy contract
cita-cli rpc sendRawTransaction --code $contract_bin --private-key 0xb0c33c376f1a99652421f17741cfddbcfcd970ffe4ed1e9d8e99893c26e01036 --url http://47.97.108.229:1337
