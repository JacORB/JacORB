#!/bin/sh
#
# JACO - JacORB cmdline
# @author Christoph Becker (PrismTech)
# @author Alexander Fetke (PrismTech)
# @author Alphonse Bendt (PrismTech)
# @version $Id$

# use hardcoded settings if available (true/false)
USE_HARDCODED=false

@RESOLVE_JACORB_HOME@

# If hardcoded vars should be used, set them, otherwise do nothing
if "$USE_HARDCODED"
then
    JRE_HOME="@@@JRE_HOME@@@"
    JACORB_HOME="@@@JACORB_HOME@@@"
fi

# Test if hardcoded JRE provides java-binary
# if not, test $JAVA_HOME-environment var
# if also not available, look for JAVA inside path and resolve symlinks
# if that fails, die
if [ ! -x "$JRE_HOME/bin/java" ]
then
    if [ -x "$JAVA_HOME/bin/java" ]
    then
        JRE_HOME=$JAVA_HOME
    else
        #echo "resolve java"
        JAVA_CMD=$(resolve "$(type java | sed 's/.* is //')")
        JRE_HOME="$(dirname "$(dirname "$JAVA_CMD")")"
        if [ ! -x "$JRE_HOME/bin/java" ];
        then
           echo "Compiletime JRE nor JAVA_HOME nor PATH point to an existing JAVA-dir!">2
           exit 1;
        fi
    fi
fi

# Test if JACORB_HOME contains jaco
# if not, look at $PATH and resolve all symlinks
# obviously is jaco in path...
if [ ! -x "$JACORB_HOME/bin/jaco" ]
then
    JACORB_HOME=${RESOLVED_JACORB_HOME}
fi

# verbosity output
echo "Using Java from  : $JRE_HOME"
echo "Using JacORB from: $JACORB_HOME"

# call java interpreter

classpath=$JACORB_HOME/lib/jacorb.jar
classpath=$classpath:$JACORB_HOME/lib/logkit-1.2.jar
classpath=$classpath:$JACORB_HOME/lib/avalon-framework-4.1.5.jar
classpath=$classpath:$JRE_HOME/lib/rt.jar
classpath=$classpath:$CLASSPATH

exec "$JRE_HOME/bin/java"                                         \
    -Xbootclasspath/p:"$classpath"                                \
    -Djacorb.home="$JACORB_HOME"                                  \
    -Dorg.omg.CORBA.ORBClass=org.jacorb.orb.ORB                   \
    -Dorg.omg.CORBA.ORBSingletonClass=org.jacorb.orb.ORBSingleton \
        "$@"
