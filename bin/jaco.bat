@echo off
rem call java interpreter
java -classpath C:\Work\JacORB\lib\jacorb.jar;c:\jdk1.1.8\lib\classes.zip;%CLASSPATH% -Dorg.omg.CORBA.ORBClass=org.jacorb.orb.ORB -Dorg.omg.CORBA.ORBSingletonClass=org.jacorb.orb.ORBSingleton %*

