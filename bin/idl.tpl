#!/bin/sh
# @DONT_EDIT@

@RESOLVE_JACORB_HOME@
JACORB_HOME=${RESOLVED_JACORB_HOME}

@RESOLVE_JAVA_CMD@
JAVA_CMD=${RESOLVED_JAVA_CMD}

"$JAVA_CMD"  -classpath ${JACORB_HOME}/lib/idl.jar:${JACORB_HOME}/lib/logkit-1.2.jar:${CLASSPATH} org.jacorb.idl.parser  "$@"
