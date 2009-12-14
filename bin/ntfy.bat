@echo off
rem Starts the JacORB Notification Service
set CLASSPATH="..\lib\picocontainer-1.2.jar;..\lib\antlr-2.7.2.jar"
jaco org.jacorb.notification.ConsoleMain %*
