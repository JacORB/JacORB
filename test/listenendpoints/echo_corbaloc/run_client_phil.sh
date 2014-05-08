#!/bin/bash
bn=${0##*/}
host=$(hostname)

if [[ ! -n $JACORB_HOME ]] ; then
   $JACORB_HOME=$(cd ../../../; pwd)
   echo "$bn: setting JACORB_HOME to $JACORB_HOME"
fi
export JACORB_HOME

echo "Compile the test/regression classes prior to runnning this!"

PATH=${PATH}
CLASSPATH=${CLASSPATH}
export PATH=${PATH}:${JACORB_HOME}/bin
export CLASSPATH=${CLASSPATH}:$PWD/../../regression/target/test-classes
echo "$bn: JACORB_HOME=<${JACORB_HOME}>"
echo "$bn: CLASSPATH=<${CLASSPATH}>"
echo "$bn: PATH=<${PATH}>"

out_dir="${JACORB_HOME}/test/listenendpoints/echo_corbaloc/build/output"
if [[ ! -d $out_dir ]] ; then
    mkdir -p $out_dir
fi

log_file="$out_dir/Client"
rm -f ${log_file}.log 2>&1

# The corbaloc strings 0-4 will connect to server on port 32000
corbaloc_str[0]="corbaloc:iiop:10.201.200.52:32000/EchoServer/EchoPOAP/EchoID"
corbaloc_str[1]="corbaloc:iiop:10.201.200.173:32000/EchoServer/EchoPOAP/EchoID"
corbaloc_str[2]="corbaloc:iiop:dhcp22.ociweb.com:32000/EchoServer/EchoPOAP/EchoID"
corbaloc_str[3]="corbaloc:iiop:phil.ociweb.com:32000/EchoServer/EchoPOAP/EchoID"
corbaloc_str[4]="corbaloc:iiop:localhost:32000/EchoServer/EchoPOAP/EchoID"

# The corbaloc strings 5-9 will connect to server on port 32999
corbaloc_str[5]="corbaloc:iiop:10.201.200.52:32999/EchoServer/EchoPOAP/EchoID"
corbaloc_str[6]="corbaloc:iiop:10.201.200.173:32999/EchoServer/EchoPOAP/EchoID"
corbaloc_str[7]="corbaloc:iiop:dhcp22.ociweb.com:32999/EchoServer/EchoPOAP/EchoID"
corbaloc_str[8]="corbaloc:iiop:phil.ociweb.com:32999/EchoServer/EchoPOAP/EchoID"
corbaloc_str[9]="corbaloc:iiop:localhost:32999/EchoServer/EchoPOAP/EchoID"

(( i = 0 ))
while (( i <= 9 )) ; do
    corbaloc=${corbaloc_str[i]}
    echo "$bn: starting client #${i} with ${corbaloc}"
    $JACORB_HOME/bin/jaco org.jacorb.test.listenendpoints.echo_corbaloc.Client \
	-ntimes 10 -nthreads 2 -delay 5000 \
	-corbaloc ${corbaloc} \
	-msg "Client #${i} on ${host} is hailing server using ${corbaloc}" > ${log_file}_${i}.log 2>&1 &
    sleep 5
    cat ${log_file}_${i}.log
    (( i = i + 1 ))
done

exit 0
