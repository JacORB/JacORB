#!/bin/bash
bn=${0##*/}
host=$(hostname)
imr_host=$2
serverName=$1
if [[ -z $serverName ]] ; then
    echo "$bn: Usage: $bn <EchoServer1 or EchoServer2> <ImR hostname (default=${host})>"
    exit 1
fi
if [[ $serverName != "EchoServer1" && $serverName != "EchoServer2" ]] ; then
    echo "$bn: Usage: $bn <EchoServer1 or EchoServer2> <ImR hostname (default=${host})>"
    exit 1
fi
[[ -z $imr_host ]] && imr_host=${host}

client="Client1"
[[ $serverName == "EchoServer2" ]] && client="Client2"

if [[ ! -n $JACORB_HOME ]] ; then
   $JACORB_HOME=$(cd ../../../; pwd)
   echo "$bn: setting JACORB_HOME to $JACORB_HOME"
fi
export JACORB_HOME

osname=$(uname -s | awk '{print tolower($0)}')
PATH=${PATH}
CLASSPATH=${CLASSPATH}
export PATH=${PATH}:${JACORB_HOME}/bin
export CLASSPATH=${CLASSPATH}:`pwd`/target/test-classes
echo "$bn: JACORB_HOME=<${JACORB_HOME}>"
echo "$bn: CLASSPATH=<${CLASSPATH}>"
echo "$bn: PATH=<${PATH}>"

out_dir="${JACORB_HOME}/test/orbreinvoke/jacorb_imr/output"
if [[ ! -d $out_dir ]] ; then
    if ! mkdir -p $out_dir ; then
        echo "ERROR::$bn: can't create directory $out_dir"
        exit 1
    fi
fi

log="${out_dir}/${client}_$$.log"
rm -f ${log} 2>&1

client_name="org.jacorb.test.listenendpoints.echo_corbaloc.Client"
pid=$(ps -ax | grep -v grep | grep "^.* ${client_name}.*corbaloc.*44444.*${serverName}" | awk '{print $1}')
[[ ! -z $pid ]] && kill -s 15 $pid && wait 5

# This will connect to EchoServer[1,2] via MyImR port 44444
corbaloc="corbaloc::${imr_host}:44444/${serverName}/EchoServer-POA/${serverName}-ID"

echo "$bn: starting ${client} with ${corbaloc} ..."
$JACORB_HOME/bin/jaco ${client_name} \
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
echo "WARNING::$bn: ${client_name} (${serverName}) may not be running!  Please check the log file"
exit 1
