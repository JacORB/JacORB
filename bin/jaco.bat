@echo off

setlocal enableextensions
setlocal enabledelayedexpansion
set _count=1
REM collect java version info
for /f "tokens=* usebackq " %%S in (`java -version ^2^>^&^1`) do (
	set _line!_count!=%%S
	set /a _count=!_count!+1
)
REM replace " by | in version text
set _quote=^"
set _line1=!_line1:%_quote%=^|!
REM parse version spec (XX.XX.XX) from text
for /f "delims=| tokens=2 usebackq" %%S in ('%_line1%') do (
	set java_version=%%S
)
REM parse version major and minor numbers from version spec
for /f "delims=. tokens=1,2 usebackq" %%S in ('%java_version%') do (
	set _vermajor=%%S
	set _verminor=%%T
)
REM determin JDK release number 
if "%_vermajor%" == "1" (set JDK_RELEASE=%_verminor%) else (set JDK_RELEASE=%_vermajor%)

set JACORB_HOME=%~dp0..
if %JDK_RELEASE% gtr 8 goto :NEWJDK_START
java -Djava.endorsed.dirs="%JACORB_HOME%\lib" -Djacorb.home="%JACORB_HOME%" -Dorg.omg.CORBA.ORBClass=org.jacorb.orb.ORB -Dorg.omg.CORBA.ORBSingletonClass=org.jacorb.orb.ORBSingleton -classpath "%CLASSPATH%" %*
goto :EOF
:NEWJDK_START
for %%F in (%JACORB_HOME%/lib/jacorb-*.jar %JACORB_HOME%/lib/jboss-rmi-api_1.0_spec*.jar %JACORB_HOME%/lib/slf4j-*.jar) do set CLASSPATH=%CLASSPATH%:%%F
java -Djacorb.home="%JACORB_HOME%" -Dorg.omg.CORBA.ORBClass=org.jacorb.orb.ORB -Dorg.omg.CORBA.ORBSingletonClass=org.jacorb.orb.ORBSingleton -classpath "%CLASSPATH%" %*
:EOF
