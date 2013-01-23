#!/bin/bash
bn=${0##*/}
cur_dir=$(pwd)
host=$(hostname)
home_dir="${JACORB_HOME}/test/orbreinvoke/jacorb_imr"
fi
trap exittrap 1 2 3 15

exittrap()
{
    cd $cur_dir
    echo "$bn: exit_trap: aborted!"
    exit 1
}

exitok()
{
    cd $cur_dir
    exit 0
}

(cd $home_dir; ./run_server.sh EchoServer1)
exitok
