#!/bin/bash

"$@" &
_PID=$!
echo "$@" > $JACUNIT_PID_DIR/$_PID

trap "kill $_PID; wait $_PID" TERM QUIT INT
wait $_PID

