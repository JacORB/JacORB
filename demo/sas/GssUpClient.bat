@echo off
set JACORBDEF=-Dorg.omg.CORBA.ORBClass=org.jacorb.orb.ORB -Dorg.omg.CORBA.ORBSingletonClass=org.jacorb.orb.ORBSingleton
set CP=../../classes
set XCP=../../lib/jacorb.jar;../../lib/logkit-1.2.jar;../../lib/avalon-framework-4.1.5.jar;../../lib/concurrent-1.3.2.jar;../../lib/antlr-2.7.2.jar;
java -Xbootclasspath/p:%XCP% -cp %CP% %JACORBDEF% -Dcustom.props=GssUpClient.properties demo.sas.GssUpClient sas.ior testUser testPass
