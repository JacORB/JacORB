#!/bin/bash
bn=${0##*/}
host=$(hostname)
if [[ $host == "phil" ]] ; then
    host="phil.ociweb.com"
fi
debug_flag=$1

osname=$(uname -s | awk '{print tolower($0)}')
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

server_name="demo.tao_imr.Server_1"
log="${out_dir}/Server.log"

pid=$(ps -ax | grep -v grep | grep "^.* ${server_name}" | awk '{print $1}')
if [[ ! -z $pid ]] ; then
    echo "${bn} nothing to do, ${server_name} is running!"
    exit 0
fi

echo "$bn: starting Server ..."
export ImplRepoServiceIOR=file:///tmp/tao_imr_locator.ior
rm -f ${log} server.ior 2>&1

if [[ -z $debug_flag ]] ; then
    ${JACORB_HOME}/bin/jaco ${server_name} server.ior > ${log} 2>&1 &
else
    # The following command alternative can be used for debugging with a NetBean
    # IDE's Debugger Console.  Before running this, you need to start the NetBean
    # IDE and the Debugger Console at port 60574.  Quynh N.
    debug_port=60574
    /usr/bin/java \
      -Xdebug \
      -Xrunjdwp:transport=dt_socket,address=localhost:${debug_port} \
      -Djava.endorsed.dirs=${JACORB_HOME}/lib -Djacorb.home=${JACORB_HOME} \
      -Dorg.omg.CORBA.ORBClass=org.jacorb.orb.ORB \
      -Dorg.omg.CORBA.ORBSingletonClass=org.jacorb.orb.ORBSingleton \
      -classpath :${JACORB_HOME}/classes:${JACORB_HOME}/demo/tao_imr/build/classes \
         ${server_name} server.ior > ${log} 2>&1 &
fi
sleep 10
cat ${log}
exit 0
