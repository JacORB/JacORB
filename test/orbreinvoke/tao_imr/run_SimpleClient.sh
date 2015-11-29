#!/bin/bash
bn=${0##*/}
host=$(hostname)
dig ${host}
if [ $? -ne 0]; then
    host=127.0.0.1
fi

serverName=$1
imr_host=$2
remote_imr_host=$3

if [[ -z $serverName ]] ; then
    echo "Usage: $bn <implName of EchoServer1 or EchoServer2> [first ImR host (default=localhost)] [second ImR host]"
    exit 1
fi
if [[ $serverName != "EchoServer1" && $serverName != "EchoServer2" ]] ; then
    echo "Usage: $bn <implName of EchoServer1 or EchoServer2>  [first ImR host (default=localhost)] [second ImR host]"
    exit 1
fi

[[ -z $poaBaseName ]] && poaBaseName="EchoServer"
[[ -z $imr_host ]] && imr_host=$host

client="Client1"
[[ $serverName == "EchoServer2" ]] && client="Client2"

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

#pid=$(ps -ef | grep -v grep | grep "Client.*${serverName}" | awk '{print $2}')
#[[ ! -z $pid ]] && kill -s 15 $pid

if [[ ! -z $imr_host && ! -z $remote_imr_host ]] ; then
    corbaloc="corbaloc::${imr_host}:44555,:${remote_imr_host}:44555/${serverName}/${poaBaseName}-POA/${poaBaseName}-ID"
else
    corbaloc="corbaloc::${imr_host}:44555/${serverName}/${poaBaseName}-POA/${poaBaseName}-ID"
fi
echo "$bn: corbaloc IOR is ${corbaloc}"
echo "$bn: starting ${client} (${server_name}) ..."
$JACORB_HOME/bin/jaco org.jacorb.test.listenendpoints.echo_corbaloc.Client \
    -corbaloc ${corbaloc} \
    -delay 5000 \
    -loop \
    -msg "${client} $$ on ${host} is hailing ${serverName}" > ${log} 2>&1 &
pid=$!
echo "$bn: $pid: $log"
if [[ ! -z $pid ]] ; then
    (( cnt = 10 ))
    while (( cnt > 0 )) ; do
        sleep 6
        if ps -p $pid ; then
            tail -5 ${log}
            echo "SUCCESS::$bn: ${client} (${server_name}) is running"
            exit 0
        fi
        (( cnt = cnt - 1 ))
    done
fi
cat ${log}
echo "WARNING::$bn: ${client} (${server_name}) may fail to start! Check the log file"
exit 1
