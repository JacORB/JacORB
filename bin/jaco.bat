@echo off
rem call java interpreter
java -Xbootclasspath:/home/troll/noffke/JacORB\lib\jacorb.jar;/usr/local/jdk1.3-sun/jre\lib\rt.jar;%CLASSPATH% -Dorg.omg.CORBA.ORBClass=org.jacorb.orb.ORB -Dorg.omg.CORBA.ORBSingletonClass=org.jacorb.orb.ORBSingleton %*

