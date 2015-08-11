#!/bin/bash
bn=${0##*/}
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
echo "$bn: starting EchoServer1 ..."
./run_server.sh "EchoServer1"

echo "$bn: starting EchoServer2 ..."
./run_server.sh "EchoServer2"

exit 0
