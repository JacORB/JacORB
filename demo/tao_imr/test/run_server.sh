#!/bin/bash
bn=${0##*/}
host=$(hostname)
if [[ $host == "phil" ]] ; then
    host="phil.ociweb.com"
fi

PATH=${PATH}
CLASSPATH=${CLASSPATH}
export JACORB_HOME
export PATH=${PATH}:${JACORB_HOME}/bin
export CLASSPATH=${CLASSPATH}:${JACORB_HOME}/classes:${JACORB_HOME}/demo/tao_imr/build/classes

out_dir=`pwd`/output
if [[ ! -d $out_dir ]] ; then
    if ! mkdir -p $out_dir ; then
        echo "ERROR::$bn: can't create directory $out_dir"
        exit 1
    fi
fi

server_name="demo.tao_imr.Server"
log="${out_dir}/Server.log"

pid=$(ps -ef | grep -v grep | grep "^.* ${server_name}" | awk '{print $2}')
if [[ ! -z $pid ]] ; then
    echo "${bn} nothing to do, ${server_name} is running!"
    exit 0
fi

echo "$bn: starting Server ..."
export ImplRepoServiceIOR=file:///tmp/tao_imr_locator.ior
rm -f ${log} server.ior 2>&1
${JACORB_HOME}/bin/jaco ${server_name} server.ior 60 > ${log} 2>&1 &
sleep 10
cat ${log}
exit 0
