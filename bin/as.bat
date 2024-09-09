@echo off

REM ----------------------------------------------------------------------------
REM  program : UTKiller
REM ----------------------------------------------------------------------------


set ERROR_CODE=0

set BASEDIR=%~dp0

if ["%~1"]==[""] (
  echo Example:
  echo   %~nx0 com.imagedance.zpai.ZpaiApplication
  echo(
  echo Need the main class argument, you can run jps to list all java process ids.
  goto exit_bat
)

set AGENT_JAR=%BASEDIR%utkiller-agent.jar
set CORE_JAR=%BASEDIR%utkiller-core.jar

set MAIN_CLASS=%~1

REM from https://stackoverflow.com/a/35445653
:read_params
if not %1/==/ (
    if not "%__var%"=="" (
        if not "%__var:~0,1%"=="-" (
            endlocal
            goto read_params
        )
        endlocal & set %__var:~1%=%~1
    ) else (
        setlocal & set __var=%~1
    )
    shift
    goto read_params
)

echo JAVA_HOME: %JAVA_HOME%

REM Setup JAVA_HOME
if "%JAVA_HOME%" == "" goto noJavaHome
if not exist "%JAVA_HOME%\bin\java.exe" goto noJavaHome

if not exist "%JAVA_HOME%\lib\tools.jar" (
  echo Can not find lib\tools.jar under %JAVA_HOME%!
  echo If java version ^<^= 1.8, please make sure JAVA_HOME point to a JDK not a JRE.
  echo If java version ^>^= 9, try to run as.bat ^<pid^> --ignore-tools
  goto exit_bat
)
set BOOT_CLASSPATH="-Xbootclasspath/a:%JAVA_HOME%\lib\tools.jar"


set JAVACMD="%JAVA_HOME%\bin\java.exe"
echo JAVACMD: %JAVACMD%
goto okJava

:noJavaHome
echo The JAVA_HOME environment variable is not defined correctly.
echo It is needed to run this program.
echo NB: JAVA_HOME should point to a JDK not a JRE.
goto exit_bat

:okJava
echo "CORE_JAR": %CORE_JAR%
%JAVACMD% -Dfile.encoding=UTF-8 %BOOT_CLASSPATH% -jar %CORE_JAR% %MAIN_CLASS% configPath=E:\\code\\utkiller\bin\utkiller.yaml
if %ERRORLEVEL% NEQ 0 goto exit_bat
if %exitProcess%==1 goto exit_bat
goto attachSuccess

:attachSuccess

:exit_bat
if "%exitProcess%"=="1" exit %ERROR_CODE%
exit /B %ERROR_CODE%
