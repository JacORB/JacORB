#!/bin/bash
bn=${0##*/}

if [[ ! -n $JACORB_HOME ]] ; then
   $JACORB_HOME=$(cd ../../../; pwd)
   echo "$bn: setting JACORB_HOME to $JACORB_HOME"
fi

echo "Compile the test/regression classes prior to runnning this!"

PATH=${PATH}
CLASSPATH=${CLASSPATH}
export PATH=${PATH}:${JACORB_HOME}/bin
export CLASSPATH=${CLASSPATH}:$PWD/../../regression/target/test-classes
echo "$bn: JACORB_HOME=<${JACORB_HOME}>"
echo "$bn: CLASSPATH=<${CLASSPATH}>"
echo "$bn: PATH=<${PATH}>"

out_dir="${JACORB_HOME}/test/listenendpoints/echo_corbaloc/output"
if [[ ! -d $out_dir ]] ; then
    mkdir -p $out_dir
fi

log="$out_dir/Server_$$.log"
rm -f $log 2>&1
server_name="org.jacorb.test.listenendpoints.echo_corbaloc.Server"
echo "$bn: starting server on wildcard endpoints iiop://:32000, iiop://:31000, iiop://32888, and iiop://32999 ..."
${JACORB_HOME}/bin/jaco ${server_name} \
 -iorfile /tmp/echo_corba.ior \
 -ORBListenEndpoints 'iiop://:32000,iiop://:32001;iiop://:32999' > $log 2>&1 &
pid=$!
echo "$bn: $pid $log"
cat $log
if [[ ! -z $pid ]] ; then
(( cnt = 2 ))
while (( cnt > 0 )) ; do
    echo "."
    sleep 10
    if ps -p $pid ; then
        tail -5 $log
        echo "SUCCESS::$bn: ${server_name} is running"
        exit 0
    fi
    (( cnt = cnt - 1 ))
done
fi
cat $log
echo "WARNING::$bn: ${server_name} may not be running! Please check the log file"
exit 0
