#!/bin/bash
bn=${0##*/}
host=$(hostname)
[[ $host == "phil" ]] && host="phil.ociweb.com"


#export ImplRepoServiceIOR=corbaloc::${host}:44555/ImR
#export ImplRepoServiceIOR=file:///tmp/tao_imr_locator.ior

$TAO_ROOT/utils/catior/tao_catior "$@"
