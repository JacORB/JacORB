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

osname=$(uname -s | awk '{print tolower($0)}')
out_dir="`pwd`/output"
if [[ ! -d $out_dir ]] ; then
    mkdir -p $out_dir
fi

APP="tao_imr_locator"
log="${out_dir}/${APP}.log"
pid=$(ps -ax | grep -v grep | grep "${APP}.*${APP}.ior" | awk '{print $1}')
if [[ ! -z $pid ]] ; then
    echo "$bn: nothing to do! $APP is running ..."
    exit 0
fi

echo "$bn: starting up ${APP} ..."
rm -f ${log} 2>&1
export ImplRepoServiceIOR=corbaloc::${host}:44555/ImR
$TAO_ROOT/orbsvcs/ImplRepo_Service/${APP} \
 -ORBListenEndpoints iiop://:44555 \
 -ORBDebugLevel 10 \
 -d 2 \
 -m 1 \
 -x /tmp/${APP}.persistent.xml \
 -t 120 \
 -o /tmp/${APP}.ior > $log 2>&1 &

pid=$!
echo "$bn: $pid: $log"
if [[ ! -z $pid ]] ; then
(( cnt = 10 ))
while (( cnt > 0 )) ; do
    echo "."
    sleep 5
    if ps -p $pid ; then
        tail -5 ${log}
        echo "SUCCESS::$bn: $pid: ${APP} server is running"
        exit 0
    fi
    (( cnt = cnt - 1 ))
done
fi
cat ${log}
echo "WARNING::$bn: ${APP} server may not be running!"
exit 1
