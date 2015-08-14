#!/bin/bash
bn=${0##*/}
export JACORB_HOME

PATH=${PATH}
CLASSPATH=${CLASSPATH}
export PATH=${PATH}:${JACORB_HOME}/bin
export CLASSPATH=${CLASSPATH}:`pwd`/target/test-classes
echo "$bn: JACORB_HOME=<${JACORB_HOME}>"
echo "$bn: CLASSPATH=<${CLASSPATH}>"
echo "$bn: PATH=<${PATH}>"

out_dir="${JACORB_HOME}/test/orbreinvoke/jacorb_imr/output"
if [[ ! -d $out_dir ]] ; then
    mkdir -p $out_dir
fi

logx="${out_dir}/ImRSSD.log"
echo "$bn: starting up ServerStartupDaemon server ..."
rm -f $logx 2>&1
${JACORB_HOME}/bin/imr_ssd "$@" > $logx 2>&1 &
pid=$!
if [[ ! -z $pid ]] ; then
(( cnt = 2 ))
while (( cnt > 0 )) ; do
    sleep 10
    if ps -p $pid ; then
        echo "SUCCESS::$bn: $pid: ServerStartupDaemon server is running"
        exit 0
    fi
    (( cnt = cnt - 1 ))
done
fi
cat ${log}
echo "ERROR::$bn: failed to start ServerStartupDaemon server"
exit 1
