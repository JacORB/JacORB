@echo off
rem decodes and prints the components of a stringified IOR

IF "%1" == "" GOTO USAGE

jaco jacorb.orb.util.PrintIOR %1 %2
GOTO EXIT

:USAGE
   echo.
   echo Usage: %0 ^<filename^> 

:EXIT
