#!/bin/bash
bn=${0##*/}
host=$(hostname)
dig ${host}
if [ $? -ne 0 ]; then
    host=127.0.0.1
fi

serverName=$1
poaBaseName=

if [[ -z $serverName ]] ; then
    echo "Usage: $bn <implName of EchoServer1 or EchoServer2>"
    exit 1
fi
if [[ $serverName != "EchoServer1" && $serverName != "EchoServer2" ]] ; then
    echo "Usage: $bn <implName of EchoServer1 or EchoServer2>"
    exit 1
fi

[[ -z $poaBaseName ]] && poaBaseName="EchoServer"

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

server_name="test.orbreinvoke.tao_imr.SimpleServer"
iorfile="/tmp/${server_name}.${serverName}.${poaBaseName}-POA.ior"
if [[ ! -e $iorfile || ! -r $iorfile ]] ; then
    echo "ERROR::$bn: IOR file $iorfile does not exist or in inaccessible"
    exit 1
fi

echo "$bn: IOR file is ${iorfile}"
echo "$bn: starting ${client} (${server_name}) ..."
$JACORB_HOME/bin/jaco org.jacorb.test.listenendpoints.echo_corbaloc.Client \
    -iorfile  ${iorfile} \
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
echo "WARNING`::$bn: ${client} (${server_name}) may fail to start!  Please check the log file"
exit 1
