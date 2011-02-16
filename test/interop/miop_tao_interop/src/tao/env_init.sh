#!/bin/bash

export TAO_ROOT=$1
export ACE_ROOT=$TAO_ROOT
export MPC_ROOT=$TAO_ROOT/MPC
export PATH=$TAO_ROOT/bin:$PATH
export LD_LIBRARY_PATH=$TAO_ROOT/lib:$LD_LIBRARY_PATH
