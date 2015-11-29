#!/bin/bash
bn=${0##*/}
client_name="org.jacorb.test.listenendpoints.echo_corbaloc.Client"
host=$(hostname)
serverName=$1
imr_host=$2
remote_imr_host=$3
poaBaseName=

if [[ -z $serverName ]] ; then
    echo "Usage: $bn <implName of EchoServer1 or EchoServer2> [first ImR host (default=localhost)] [second ImR host]"
    exit 1
fi
if [[ $serverName != "EchoServer1" && $serverName != "EchoServer2" ]] ; then
    echo "Usage: $bn <implName of EchoServer1 or EchoServer2> [first ImR host (default=localhost)] [second ImR host]"
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

out_dir="`pwd`/output"
if [[ ! -d $out_dir ]] ; then
    if ! mkdir -p $out_dir ; then
        echo "ERROR::$bn: can't create directory $out_dir"
        exit 1
    fi
fi

log="${out_dir}/${client}_$$.log"
rm -f ${log} 2>&1

pid=$(ps -ax | grep -v grep | grep "^.* ${client_name}.*${serverName}.*${client}" | awk '{print $1}')
[[ ! -z $pid ]] && kill -s 15 $pid

# This will connect to EchoServer[1,2] via ImR
if [[ ! -z $imr_host && ! -z $remote_imr_host ]] ; then
    corbaloc="corbaloc::${imr_host}:44555,:${remote_host}:44555/${serverName}/${poaBaseName}-Parent-POA/${poaBaseName}-POA1/${poaBaseName}-ID1"
else
    corbaloc="corbaloc::${imr_host}:44555/${serverName}/${poaBaseName}-Parent-POA/${poaBaseName}-POA1/${poaBaseName}-ID1"
fi
echo "$bn: using IOR is ${corbaloc}"
echo "$bn: starting ${client} (${server_name}) ..."
$JACORB_HOME/bin/jaco ${client_name} \
    -corbaloc ${corbaloc} \
    -delay 5000 \
    -loop \
    -msg "${client} $$ on ${host} is hailing ${serverName}" > ${log} 2>&1 &
pid=$!
echo "$bn: $pid: $log"
if [[ ! -z $pid ]] ; then
    (( cnt = 10 ))
    while (( cnt > 0 )) ; do
    echo "."
        sleep 6
        if ps -p $pid ; then
            tail -5 ${log}
            echo "SUCCESS::$bn: ${client} (${server_name}) is running."
            exit 0
        fi
        (( cnt = cnt - 1 ))
    done
fi
cat ${log}
echo "WARNING::$bn: ${client} (${server_name}) may not be running!  Please check log file."
exit 1
