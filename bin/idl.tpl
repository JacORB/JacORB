#!/bin/sh
# @DONT_EDIT@

@RESOLVE_JACORB_HOME@
JACORB_HOME=${RESOLVED_JACORB_HOME}
@JAVA_CMD@  -classpath ${JACORB_HOME}/lib/idl.jar:${JACORB_HOME}/lib/logkit-1.2.jar:${CLASSPATH} org.jacorb.idl.parser  "$@"
