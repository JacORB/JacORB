@echo off
rem call java interpreter
java -classpath @@@;%CLASSPATH% -Dorg.omg.CORBA.ORBClass=jacorb.orb.ORB -Dorg.omg.CORBA.ORBSingletonClass=jacorb.orb.ORBSingleton %*

