@echo off
rem Starts the JacORB name server

IF "%1" == "" GOTO USAGE
echo "TS"
jaco jacorb.trading.TradingService %1 %2 
GOTO EXIT

:USAGE
   echo.
   echo Usage: %0 ^<filename^>

:EXIT
