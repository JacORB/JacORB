@echo off
rem Starts the JacORB Notification Service
rem ex: NotifyServer notify.ior -p 9222 -c 1 
jaco org.jacorb.notification.NotifyServer %*
