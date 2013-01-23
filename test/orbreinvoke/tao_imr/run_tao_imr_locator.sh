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

out_dir="${JACORB_HOME}/test/orbreinvoke/tao_imr/output"
if [[ ! -d $out_dir ]] ; then
    mkdir -p $out_dir
fi

APP="tao_imr_locator"
log="${out_dir}/${APP}.log"
pid=$(ps -ef | grep -v grep | grep "${APP}.*${APP}.ior" | awk '{print $2}')
[[ ! -z $pid ]] && kill -s 15 $pid && sleep 5

echo "$bn: starting up ${APP} ..."
rm -f ${log} 2>&1
export ImplRepoServiceIOR=corbaloc::${host}:44555/ImR
$TAO_ROOT/orbsvcs/ImplRepo_Service/${APP} \
 -ORBListenEndpoints iiop://:44555 \
 -ORBDebug \
 -ORBDebugLevel 15 \
 -d 2 \
 -m 1 \
 -x /tmp/${APP}.persistent.xml \
 -t 120 \
 -o /tmp/${APP}.ior > $log 2>&1 &

pid=$!
echo "$bn: $pid: $log"
(( cnt = 10 ))
while (( cnt > 0 )) ; do
    echo "."
    sleep 5
    if ps $pid ; then
        tail -5 ${log}
        echo "SUCCESS::$bn: $pid: ${APP} server is running"
        exit 0
    fi
    (( cnt = cnt - 1 ))
done
cat ${log}
echo "WARNING::$bn: ${APP} server may not be running!  Please check the log file"
exit 1
