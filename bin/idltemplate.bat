@echo off
java -classpath "@@@\lib\idl.jar;@@@\lib\logkit.jar;%CLASSPATH%" org.jacorb.idl.parser %*
