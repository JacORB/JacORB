#!/bin/sh

# decodes and prints the components of a stringified IOR
# @version $Id$
# @DONT_EDIT@

@RESOLVE_JACO_CMD@

JACO_CMD=${RESOLVED_JACO_CMD}

$JACO_CMD org.jacorb.orb.util.PrintIOR "$@"
