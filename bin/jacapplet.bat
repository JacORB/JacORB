@echo off
echo --
echo Please edit this script to match your installation
echo --

appletviewer -J-Dorg.omg.CORBA.ORBClass=org.jacorb.orb.ORB -J-Dorg.omg.CORBA.ORBSingletonClass=org.jacorb.orb.ORBSingleton -J"-Xbootclasspath:%JAVA_HOME%\jre\lib\rt.jar;%CLASSPATH%"   %*
