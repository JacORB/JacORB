#!/bin/bash
bn=${0##*/}
export JACORB_HOME

PATH=${PATH}
CLASSPATH=${CLASSPATH}
export PATH=${PATH}:${JACORB_HOME}/bin
export CLASSPATH=${CLASSPATH}:${JACORB_HOME}/classes
echo "$bn: JACORB_HOME=<${JACORB_HOME}>"
echo "$bn: CLASSPATH=<${CLASSPATH}>"
echo "$bn: PATH=<${PATH}>"

out_dir="${JACORB_HOME}/test/listenendpoints/echo_corbaloc/output"
if [[ ! -d $out_dir ]] ; then
    mkdir -p $out_dir
fi

log_file="$out_dir/Server"
rm -f ${log_file}.log 2>&1

echo "$bn: starting server on wildcard endpoints iiop://:32000 and iiop://32999 ..."
${JACORB_HOME}/bin/jaco test.listenendpoints.echo_corbaloc.Server \
	-iorfile /tmp/echo_corba.ior \
	-ORBListenEndpoints 'iiop://:32000;iiop://:32999' > ${log_file}.log 2>&1 &
echo $!
mv ${log_file}.log ${log_file}_${pid}.log
exit 0
