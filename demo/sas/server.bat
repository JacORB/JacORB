@echo off
set JAVA=%JAVA_HOME%\bin\java
set CP=.;classes
set CP=\Projects\JacORB\lib\jacorb.jar;%CP%
echo %CP%
set JORB1=-Dorg.omg.CORBA.ORBClass=org.jacorb.orb.ORB -Dorg.omg.CORBA.ORBSingletonClass=org.jacorb.orb.ORBSingleton
%JAVA% -classpath "%CP%" -Dcustom.props=server_props %JORB1% demo.sas.Server ior

