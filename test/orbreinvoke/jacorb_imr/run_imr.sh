#!/bin/bash
bn=${0##*/}
host=$(hostname)
if [[ $host == "phil" ]] ; then
    host="10.201.200.173"
fi

osname=$(uname -s | awk '{print tolower($0)}')
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

imr_name="org.jacorb.imr.ImplementationRepositoryImpl"
log="${out_dir}/MyImR.log"

pid=$(ps -ax | grep -v grep | \
    grep "${imr_name}.*.endpoint_port_number[=]44444" | awk '{print $1}')
[[ ! -z $pid ]] && kill -s 15 $pid && sleep 5

echo "$bn: starting up MyImR server ..."
rm -f ${log} 2>&1
${JACORB_HOME}/bin/imr -printIOR \
    -Dcustom.props=./MyImR.properties > ${log} 2>&1 &
pid=$!
(( cnt = 10 ))
while (( cnt > 0 )) ; do
    echo "."
    sleep 5
    if ps -p $pid ; then
        tail -5 ${log}
        echo "SUCCESS::$bn: $pid: MyImR server is running"
        exit 0
    fi
    (( cnt = cnt - 1 ))
done
cat ${log}
echo "WARNING::$bn: MyImR server may not be running! Check the log file"
exit 1
