@echo off
rem call java interpreter
java -Xbootclasspath:C:\Work\JacORB\lib\jacorb.jar;C:\Programme\JavaSoft\JRE\1.3.0_02\lib\rt.jar;%CLASSPATH% -Dorg.omg.CORBA.ORBClass=org.jacorb.orb.ORB -Dorg.omg.CORBA.ORBSingletonClass=org.jacorb.orb.ORBSingleton %*

