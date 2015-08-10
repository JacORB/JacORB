z#!/bin/bash
bn=${0##*/}
host=$(hostname)

if [[ ! -n $JACORB_HOME ]] ; then
   $JACORB_HOME=$(cd ../../../; pwd)
   echo "$bn: setting JACORB_HOME to $JACORB_HOME"
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
    mkdir -p $out_dir
fi

echo "$bn: starting EchoServer1-Client ..."
./run_client.sh "EchoServer1"

echo "$bn: starting EchoServer2-Client ..."
./run_client.sh "EchoServer2"

exit 0
