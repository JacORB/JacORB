#!/bin/bash
bn=${0##*/}
APP="tao_imr_locator"
host=$(hostname)
[[ $host == "phil" ]] && host="phil.ociweb.com"
ft_opt=$1
dirname=$2
local=$3

if [[ ! -z $ft_opt && $ft_opt != primary && $ft_opt != backup ]] ; then
	echo "ERROR::$bn: Invalid fault-tolerant argument, must be \"primary\" or \"backup\""
	echo "\nUsage: $bn [<fault-tolerant mode: primary or backup, default=none> <share directory name> ] "
	exit 1
fi

out_dir="`pwd`/output"
if [[ ! -d $out_dir ]] ; then
    mkdir -p $out_dir
fi

[[ -z $dirname ]] && dirname="/tmp"
if [[ ! -d $dirname || ! -w $dirname ]] ; then
	echo "ERROR::$bn: $dirname is not a directory or is not writeable"
	exit 1
fi

ep_host=
if [[ $ft_opt == "primary" ]] ; then
	ep_port="44555"
	log="${out_dir}/${APP}-ft-primary.log"
	ft_arg="--${ft_opt} --directory ${dirname}"
    o_arg=
	x_arg=
elif [[ $ft_opt == "backup" ]] ; then
	ep_port="44555"
	[[ ! -z $local ]] && ep_port="44666"
	log="${out_dir}/${APP}-ft-backup.log"
	ft_arg="--${ft_opt} --directory ${dirname}"
	o_arg="-o ${dirname}/${APP}.ior"
	x_arg=
else
	ep_port="44555"
	log="${out_dir}/${APP}.log"
	ft_arg=
	o_arg="-o ${dirname}/${APP}.ior"
	x_arg="-x ${dirname}/${APP}.persistent.xml"
fi
endpoint="iiop://${ep_host}:${ep_port}"

export ACE_ROOT
export TAO_ROOT
export PATH=$PATH:$ACE_ROOT/bin
export LD_LIBRARY_PATH=$ACE_ROOT/lib
export DYLD_LIBRARY_PATH=$ACE_ROOT/lib

export JACORB_HOME

pid=$(ps -ax | grep -v grep | grep "${APP}.*[-]${ftopt}.*${APP}.ior" | awk '{print $1}')
if [[ ! -z $pid ]] ; then
    echo "$bn: nothing to do! $APP is running ..."
    exit 0
fi

echo "$bn: starting up ${APP} ..."
rm -f ${log} 2>&1
export ImplRepoServiceIOR=corbaloc::${ep_host}:${ep_port}/ImR
$TAO_ROOT/orbsvcs/ImplRepo_Service/${APP} \
 -ORBListenEndpoints ${endpoint}  \
 -ORBDebugLevel 10 \
 -d 10 \
 -m 1 \
 -t 120 \
 ${x_arg} \
 ${ft_arg} \
 ${o_arg} > $log 2>&1 &

pid=$!
echo "$bn: $pid: $log"
if [[ ! -z $pid ]] ; then
(( cnt = 10 ))
while (( cnt > 0 )) ; do
    echo "."
    sleep 5
    if ps -p $pid ; then
        tail -5 ${log}
        echo "SUCCESS::$bn: $pid: ${APP} server is running"
        exit 0
    fi
    (( cnt = cnt - 1 ))
done
fi
cat ${log}
echo "WARNING::$bn: ${APP} server may not be running!"
exit 1
