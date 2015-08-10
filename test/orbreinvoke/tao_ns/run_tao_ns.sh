#!/bin/bash
bn=${0##*/}
host=$(hostname)
[[ $host == "phil" ]] && host="phil.ociweb.com"

export ACE_ROOT
export TAO_ROOT
export PATH=$PATH:$ACE_ROOT/bin
export LD_LIBRARY_PATH=$ACE_ROOT/lib
export DYLD_LIBRARY_PATH=$ACE_ROOT/lib

export JACORB_HOME

out_dir="${JACORB_HOME?'JACORB_HOME environment variable must be set'}/test/orbreinvoke/tao_ns/output"
if [[ ! -d $out_dir ]] ; then
    mkdir -p $out_dir
fi

myNS="tao_cosnaming"
log="${out_dir}/MyTaoNS.log"
pid=$(ps -ax | grep -v grep | grep "${myNS}.*myTaoNs.ior" | awk '{print $1}')
if [[ ! -z $pid ]] ; then
    echo "$bn: nothing to do!  ${myNS} is already running ..."
    exit 0
fi
echo "$bn: starting up ${myNS} server ..."
rm -f ${log} 2>&1
${TAO_ROOT?'TAO_ROOT environment variable must be set'}/orbsvcs/Naming_Service/${myNS} \
 -m 1 \
 -d \
 -m 0 \
 -ORBListenEndPoints iiop://${host}:55555 \
 -o /tmp/myTaoNs.ior > $log 2>&1 &

pid=$!
echo "$bn: $pid: $log"
if [[ ! -z $pid ]] ; then
    (( cnt = 2 ))
    while (( cnt > 0 )) ; do
        sleep 10
        if ps -p $pid ; then
            tail -5 ${log}
            echo "SUCCESS::$bn: $pid: ${myNS} server is running"
            exit 0
        fi
        (( cnt = cnt - 1 ))
    done
fi
cat ${log}
echo "WARNING::$bn: ${myNS} may not be running!  Please check the log file"
exit 1




