#!/bin/bash
bn=${0##*/}
host=$(hostname)
if [[ $host == "phil" ]] ; then
    host="phil.ociweb.com"
fi
implName=$1
if [[ -z $implName ]] ; then
    echo "$bn: Usage: $bn <EchoServer1 or EchoServer2>"
    exit 1
fi
if [[ $implName != "EchoServer1" && $implName != "EchoServer2" ]] ; then
    echo "$bn: Usage: $bn <EchoServer1 or EchoServer2>"
    exit 1
fi

endpoint="iiop://${host}:32000"
if [[ $implName == "EchoServer2" ]] ; then
    endpoint="iiop://${host}:32999"
fi

PATH=${PATH}
CLASSPATH=${CLASSPATH}
export JACORB_HOME
export PATH=${PATH}:${JACORB_HOME}/bin
export CLASSPATH=${CLASSPATH}:${JACORB_HOME}/classes
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

log="${out_dir}/${implName}_$$.log"
server_name="test.orbreinvoke.jacorb_imr.Server"

pid=$(ps -ef | grep -v grep | grep "^.*${server_name}.*${implName}.*{endpoint}" | awk '{print $2}')
if [[ ! -z $pid ]] ; then
    echo "$bn: nothing to do! ${implName} is running"
    exit 0
fi

echo "$bn: starting $implName at endpoints $endpoint ..."
rm -f ${log} 2>&1
${JACORB_HOME}/bin/jaco ${server_name} \
    -testmode P \
	-iorfile /tmp/${server_name}.${implName}.ior \
    -DOAAddress=${endpoint} \
    -Djacorb.use_imr=on \
    -Djacorb.use_tao_imr=off \
    -Djacorb.implname=${implName} \
    -DORBInitRef.ImplementationRepository=file:///tmp/MyImR.ior \
    > ${log} 2>&1 &
pid=$!
echo "$bn: $pid: $log"

(( cnt = 10 ))
while (( cnt > 0 )) ; do
    sleep 5
    if ps $pid ; then
        tail -5 ${log}
        echo "SUCCESS::$bn: ${implName} is running"
        exit 0
    fi
    (( cnt = cnt - 1 ))
done
cat ${log}
echo "ERROR::$bn: ${implName} may not be running!  Please chck the log file"
exit 1
