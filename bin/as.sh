#!/bin/bash

# ----------------------------------------------------------------------------
#  program : UTKiller
# ----------------------------------------------------------------------------

ERROR_CODE=0

BASEDIR=$(dirname "$0")

if [ -z "$1" ]; then
  echo "Example:"
  echo "  $(basename "$0") 452"
  echo
  echo "Need the main path argument, you can run jps to list all java process ids."
  exit 1
fi

AGENT_JAR="${BASEDIR}/utkiller-agent.jar"
CORE_JAR="${BASEDIR}/utkiller-core.jar"

MAIN_CLASS=$1

# Parse named parameters
while [[ $# -gt 0 ]]; do
  case $1 in
    --*=*)
      key=${1#--}
      key=${key%=*}
      value=${1#*=}
      declare "$key=$value"
      shift
      ;;
    -*)
      key=${1#-}
      value=$2
      declare "$key=$value"
      shift 2
      ;;
    *)
      shift
      ;;
  esac
done

echo "JAVA_HOME: $JAVA_HOME"

# Setup JAVA_HOME
if [ -z "$JAVA_HOME" ] || [ ! -e "$JAVA_HOME/bin/java" ]; then
  echo "The JAVA_HOME environment variable is not defined correctly."
  echo "It is needed to run this program."
  echo "NB: JAVA_HOME should point to a JDK not a JRE."
  exit 1
fi

if [ ! -e "$JAVA_HOME/lib/tools.jar" ]; then
  echo "Can not find lib/tools.jar under $JAVA_HOME!"
  exit 1
fi
BOOT_CLASSPATH="-Xbootclasspath/a:$JAVA_HOME/lib/tools.jar"

JAVACMD="$JAVA_HOME/bin/java"
echo "JAVACMD: $JAVACMD"

echo "CORE_JAR: $CORE_JAR"
"$JAVACMD" -Dfile.encoding=UTF-8 $BOOT_CLASSPATH -jar "$CORE_JAR" $MAIN_CLASS port=9999;utkiller_home="$BASEDIR"

if [ $? -ne 0 ]; then
  exit $ERROR_CODE
fi

if [ "$exitProcess" -eq 1 ]; then
  exit $ERROR_CODE
fi

# attachSuccess

exit $ERROR_CODE