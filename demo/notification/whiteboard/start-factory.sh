CLASSPATH=build/classes:$CLASSPATH
export CLASSPATH

echo "start WhiteboardFactory"

../../../bin/jaco demo.notification.whiteboard.WhiteBoardFactory $*
