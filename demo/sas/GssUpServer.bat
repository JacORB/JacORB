@echo off
set JACORBDEF=-Dorg.omg.CORBA.ORBClass=org.jacorb.orb.ORB -Dorg.omg.CORBA.ORBSingletonClass=org.jacorb.orb.ORBSingleton
set CP=build/classes
set XCP=../../lib/jacorb.jar;../../lib/slf4j-api-1.5.10.jar;../../lib/slf4j-jdk14-1.5.10.jar;../../lib/antlr-2.7.2.jar;
java -Xbootclasspath/p:%XCP% -cp %CP% %JACORBDEF% -Dcustom.props=GssUpServer.properties org.jacorb.demo.sas.GssUpServer sas.ior
