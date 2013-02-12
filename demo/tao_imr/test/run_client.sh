#!/bin/bash
bn=${0##*/}
host=$(hostname)
imr_host=$2
[[ -z $imr_host ]] && imr_host=$host

if [[ ! -n $JACORB_HOME ]] ; then
   $JACORB_HOME=$(cd ../../../; pwd)
   echo "$bn: setting JACORB_HOME to $JACORB_HOME"
fi
export JACORB_HOME

PATH=${PATH}
CLASSPATH=${CLASSPATH}
export PATH=${PATH}:${JACORB_HOME}/bin
export CLASSPATH=${CLASSPATH}:${JACORB_HOME}/classes:${JACORB_HOME}/demo/tao_imr/build/classes
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

log="${out_dir}/Client.log"
rm -f ${log} 2>&1

echo "$bn: starting ${client} ..."
$JACORB_HOME/bin/jaco demo.tao_imr.Client server.ior > $log 2>&1 &
sleep 15
cat ${log}
exit 0
