@echo off
set JACORB_HOME=%~dp0..
java -classpath "%JACORB_HOME%\lib\idl.jar;%CLASSPATH%" org.jacorb.idl.parser %*
