@echo off
rem call java interpreter
java -Xbootclasspath:@@@;%CLASSPATH% -Dorg.omg.CORBA.ORBClass=jacorb.orb.ORB -Dorg.omg.CORBA.ORBSingletonClass=jacorb.orb.ORBSingleton %*

