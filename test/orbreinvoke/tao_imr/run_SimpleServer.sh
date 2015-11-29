#!/bin/bash
bn=${0##*/}
server_name="org.jacorb.test.orbreinvoke.tao_imr.SimpleServer"
host=$(hostname)
dig ${host}
if [ $? -ne 0 ]; then
    host=127.0.0.1
fi

implName=$1
poaBaseName=
if [[ -z $implName ]] ; then
    echo "Usage: $bn <implName of EchoServer1 or EchoServer2>"
    exit 1
fi
if [[ $implName != "EchoServer1" && $implName != "EchoServer2" ]] ; then
    echo "Usage: $bn <implName of EchoServer1 or EchoServer2>"
    exit 1
fi

if [[ -z $poaBaseName ]] ; then
    poaBaseName="EchoServer"
fi

endpoint="iiop://${host}:42000"
if [[ $implName == "EchoServer2" ]] ; then
    endpoint="iiop://${host}:42999"
fi


PATH=${PATH}
CLASSPATH=${CLASSPATH}
export JACORB_HOME
export PATH=${PATH}:${JACORB_HOME}/bin
export CLASSPATH=${CLASSPATH}:${JACORB_HOME}/test/regression/target/test-classes
#echo "$bn: JACORB_HOME=<${JACORB_HOME}>"
#echo "$bn: CLASSPATH=<${CLASSPATH}>"
#echo "$bn: PATH=<${PATH}>"

out_dir="`pwd`/output"
if [[ ! -d $out_dir ]] ; then
    if ! mkdir -p $out_dir ; then
        echo "ERROR::$bn: can't create directory $out_dir"
        exit 1
    fi
fi

log="${out_dir}/${implName}_$$.log"

pid=$(ps -ax | grep -v grep | grep -i "^.* ${server_name}.*${implName}" | awk '{print $1}')
if [[ ! -z $pid ]] ; then
    echo "${bn} nothing to do, ${implName} is running ..."
    exit 0
fi

echo "$bn: starting $implName at endpoints $endpoint ..."
rm -f ${log} 2>&1
${JACORB_HOME}/bin/jaco ${server_name} \
    -poabasename ${poaBaseName} \
    -iorfile /tmp/${server_name}.${implName}.${poaBaseName}.ior \
    -DOAAddress=${endpoint} \
    -Djacorb.use_tao_imr=on \
    -Djacorb.implname=${implName} \
    -ORBInitRef ImplRepoService=file:///tmp/tao_imr_locator.ior \
    > ${log} 2>&1 &
pid=$!
if [[ ! -z $pid ]] ; then
(( cnt = 5 ))
while (( cnt > 0 )) ; do
    echo "."
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
echo "WARNING::$bn: ${implName} may not be running! Please check the log file"
exit 1
