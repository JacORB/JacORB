#!/bin/bash
bn=${0##*/}
host=$(hostname)
imr_host=$2
serverName=XXXXXX
imr_host=$host
client="ClientX"

if [[ ! -n $JACORB_HOME ]] ; then
   $JACORB_HOME=$(cd ../../../; pwd)
   echo "$bn: setting JACORB_HOME to $JACORB_HOME"
fi
export JACORB_HOME

PATH=${PATH}
CLASSPATH=${CLASSPATH}
export PATH=${PATH}:${JACORB_HOME}/bin
export CLASSPATH=${CLASSPATH}:${JACORB_HOME}/test/regression/target/test-classes
echo "$bn: JACORB_HOME=<${JACORB_HOME}>"
echo "$bn: CLASSPATH=<${CLASSPATH}>"
echo "$bn: PATH=<${PATH}>"

out_dir="${JACORB_HOME}/test/orbreinvoke/tao_imr/output"
if [[ ! -d $out_dir ]] ; then
    if ! mkdir -p $out_dir ; then
        echo "ERROR::$bn: can't create directory $out_dir"
        exit 1
    fi
fi

log="${out_dir}/${client}_$$.log"
rm -f ${log} 2>&1

corbaloc="corbaloc::${imr_host}:44555/${serverName}/${serverName}-Parent-POA/${serverName}-POA2/${serverName}-ID2"
echo "
echo "$bn: starting ${client} ..."
exec $JACORB_HOME/bin/jaco org.jacorb.test.listenendpoints.echo_corbaloc.Client \
    -corbaloc ${corbaloc} \
    -delay 5000 \
    -loop \
    -msg "${client} $$ on ${host} is hailing ${serverName}" > ${log} 2>&1 &
pid=$!
echo "$bn: $pid: $log"
if [[ ! -z $pid ]] ; then
    (( cnt = 2 ))
    while (( cnt > 0 )) ; do
        sleep 10
        if ps -p $pid ; then
            tail -5 ${log}
            echo "SUCCESS::$bn: ${client} is running"
            exit 0
        fi
        (( cnt = cnt - 1 ))
    done
fi
cat ${log}
echo "ERROR::$bn: failed to start ${client}"
exit 1
