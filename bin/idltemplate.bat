@echo off
java -classpath "@@@\lib\idl.jar;@@@\lib\logkit-1.2.jar;%CLASSPATH%" org.jacorb.idl.parser %*
