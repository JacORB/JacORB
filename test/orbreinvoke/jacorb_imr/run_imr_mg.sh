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

log="${out_dir}/ImRMG.log"
echo "$bn: starting up ImRManager server ..."
rm -f $log 2>&1
${JACORB_HOME}/bin/imr_mg "$@" > ${log} 2>&1 &
pid=$!
if [[ ! -z $pid ]] ; then
echo $pid
(( cnt = 2 ))
while (( cnt > 0 )) ; do
    sleep 10
    if ps -p $pid ; then
        echo "SUCCESS::$bn: $pid: ImRManager server is running"
        exit 0
    fi
    (( cnt = cnt - 1 ))
done
fi
cat ${log}
echo "ERROR::$bn: failed to start ImRManager server"
exit 1
