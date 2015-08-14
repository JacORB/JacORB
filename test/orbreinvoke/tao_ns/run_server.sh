#!/bin/bash
bn=${0##*/}
host=$(hostname)
server_name="org.jacorb.test.orbreinvoke.tao_ns.Server"
implName=$1
if [[ -z $implName ]] ; then
    echo "$bn: Usage: $bn <EchoServer1 or EchoServer2>"
    exit 1
fi
if [[ $implName != "EchoServer1" && $implName != "EchoServer2" ]] ; then
    echo "$bn: Usage: $bn <EchoServer1 or EchoServer2>"
    exit 1
fi

endpoint="iiop://${host}:33000"
if [[ $implName == "EchoServer2" ]] ; then
    endpoint="iiop://${host}:33999"
fi

export JACORB_HOME
PATH=${PATH}
CLASSPATH=${CLASSPATH}
export PATH=${PATH}:${JACORB_HOME}/bin
export CLASSPATH=${CLASSPATH}:`pwd`/target/test-classes
echo "$bn: JACORB_HOME=<${JACORB_HOME}>"
echo "$bn: CLASSPATH=<${CLASSPATH}>"
echo "$bn: PATH=<${PATH}>"

out_dir="${JACORB_HOME}/test/orbreinvoke/tao_ns/output"
if [[ ! -d $out_dir ]] ; then
    if ! mkdir -p $out_dir ; then
        echo "ERROR::$bn: can't create directory $out_dir"
        exit 1
    fi
fi

log="${out_dir}/${implName}_$$.log"

echo "$bn: starting $implName at endpoints $endpoint ..."
rm -f ${log} 2>&1
${JACORB_HOME}/bin/jaco ${server_name} \
    -iorfile /tmp/${server_name}.${implName}.ior \
    -DOAAddress=${endpoint} \
    -Djacorb.implname=${implName} \
    > ${log} 2>&1 &
pid=$!
if [[ ! -z $pid ]] ; then
(( cnt = 2 ))
while (( cnt > 0 )) ; do
    sleep 10
    if ps -p $pid ; then
        tail -5 ${log}
        echo "SUCCESS::$bn: ${implName} is running"
        exit 0
    fi
    (( cnt = cnt - 1 ))
done
fi
cat ${log}
echo "WARNING::$bn: ${implName} may not be running! Please check log file"
exit 1
