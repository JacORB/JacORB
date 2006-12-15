@echo off
rem call java interpreter
@JAVA_CMD@ -Djava.endorsed.dirs=@JACORB_HOME@/lib -Djacorb.home=@JACORB_HOME@ -Dorg.omg.CORBA.ORBClass=org.jacorb.orb.ORB -Dorg.omg.CORBA.ORBSingletonClass=org.jacorb.orb.ORBSingleton -classpath %CLASSPATH% %*
