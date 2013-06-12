#!/bin/bash
bn=${0##*/}
host=$(hostname)
export ACE_ROOT
export TAO_ROOT
export PATH=$PATH:$ACE_ROOT/bin
export LD_LIBRARY_PATH=$ACE_ROOT/lib
export DYLD_LIBRARY_PATH=$ACE_ROOT/lib

export JACORB_HOME

out_dir="${JACORB_HOME}/test/orbreinvoke/tao_ns/output"
if [[ ! -d $out_dir ]] ; then
    mkdir -p $out_dir
fi

myNS="NameServer"
log="${out_dir}/MyNs.log"
pid=$(ps -ax | grep -v grep | grep -i "${myNS}.*MyNs.ior" | awk '{print $1}')
if [[ ! -z $pid ]] ; then
    echo "$bn: nothing to do!  ${myNS} is already running ..."
    exit 0
fi

echo "$bn: starting up ${myNS} server ..."
rm -f ${log} 2>&1
exec ${JACORB_HOME}/bin/ns -printIOR \
    -Dcustom.props=MyNs.properties \
    -DOAAddress=iiop://${host}:66666 > ${log} 2>&1 &

pid=$!
echo "$bn: $pid: $log"
if [[ ! -z $pid ]] ; then
    (( cnt = 2 ))
    while (( cnt > 0 )) ; do
        sleep 15
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
