#!/bin/sh

# @version $Id$
# @DONT_EDIT@

@RESOLVE_JACO_CMD@

JACO_CMD=${RESOLVED_JACO_CMD}

$JACO_CMD org.jacorb.util.BuildVersion
