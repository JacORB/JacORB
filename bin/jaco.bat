@echo off
set JACORB_HOME=%~dp0..
java -Djava.endorsed.dirs="%JACORB_HOME%\lib" -Djacorb.home="%JACORB_HOME%" -Dorg.omg.CORBA.ORBClass=org.jacorb.orb.ORB -Dorg.omg.CORBA.ORBSingletonClass=org.jacorb.orb.ORBSingleton -classpath "%CLASSPATH%" %*
