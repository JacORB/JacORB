@echo off
java -classpath "@@@\lib\idl.jar;@@@\lib\logkit.jar" org.jacorb.idl.parser %*
