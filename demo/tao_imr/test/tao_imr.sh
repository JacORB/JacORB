#!/bin/bash
bn=${0##*/}
host=$(hostname)
[[ $host == "phil" ]] && host="phil.ociweb.com"

APP=tao_imr

#export ImplRepoServiceIOR=corbaloc::${host}:44555/ImR
export ImplRepoServiceIOR=file://tao_imr_locator.ior

$TAO_ROOT/orbsvcs/ImplRepo_Service/${APP} "$@" \
 -ORBInitRef ImplRepoService=file:///tmp/tao_imr_locator.ior
