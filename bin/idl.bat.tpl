@echo off
@JAVA_CMD@ -classpath "@JACORB_HOME@\lib\idl.jar;%CLASSPATH%" org.jacorb.idl.parser %*
