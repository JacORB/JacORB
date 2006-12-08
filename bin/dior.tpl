#!/bin/sh

# decodes and prints the components of a stringified IOR
# @version $Id$

@RESOLVE_JACORB_HOME@

JACO=${RESOLVED_JACORB_HOME}/bin/jaco

$JACO org.jacorb.orb.util.PrintIOR "$@"
