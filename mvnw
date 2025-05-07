#!/bin/sh
# ----------------------------------------------------------------------------
# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.  The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
#
#    https://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.
# ----------------------------------------------------------------------------

# ----------------------------------------------------------------------------
# Maven Start Up Batch script
#
# Required ENV vars:
# ------------------
#   JAVA_HOME - location of a JDK home dir
#
# Optional ENV vars
# -----------------
#   M2_HOME - location of maven2's installed home dir
#   MAVEN_OPTS - parameters passed to the Java VM when running Maven
#     e.g. to debug Maven itself, use
#       set MAVEN_OPTS=-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=8000
#   MAVEN_SKIP_RC - flag to disable loading of mavenrc files
# ----------------------------------------------------------------------------

if [ -z "$MAVEN_SKIP_RC" ] ; then

  if [ -f /usr/local/etc/mavenrc ] ; then
    . /usr/local/etc/mavenrc
  fi

  if [ -f "$HOME/.mavenrc" ] ; then
    . "$HOME/.mavenrc"
  fi

fi

# OS specific support.  $var _must_ be set to either true or false.
cygwin=false;
darwin=false;
mingw=false
case "`uname`" in
  CYGWIN*) cygwin=true ;;
  MINGW*) mingw=true;;
  Darwin*) darwin=true
    # Use /usr/libexec/java_home if available, otherwise fall back to /Library/Java/Home
    # See https://developer.apple.com/library/mac/qa/qa1170/_index.html
    if [ -z "$JAVA_HOME" ]; then
      if [ -x "/usr/libexec/java_home" ]; then
        export JAVA_HOME="`/usr/libexec/java_home`"
      else
        export JAVA_HOME="/Library/Java/Home"
      fi
    fi
    ;;
esac

if [ -z "$JAVA_HOME" ] ; then
  if [ -r /etc/gentoo-release ] ; then
    JAVA_HOME=`java-config --jre-home`
  fi
fi

# For Cygwin or MSYS, switch paths to Windows format before running java
if [ "$cygwin" = "true" -o "$mingw" = "true" ] ; then
  [ -n "$M2_HOME" ] &&
    M2_HOME=`cygpath --path --windows "$M2_HOME"`
  [ -n "$JAVA_HOME" ] &&
    JAVA_HOME=`cygpath --path --windows "$JAVA_HOME"`
  [ -n "$CLASSPATH" ] &&
    CLASSPATH=`cygpath --path --windows "$CLASSPATH"`
fi

# For Cygwin, ensure the java executable is available
if $cygwin ; then
  if [ "$JAVA_HOME" = "" ] ; then
    echo "Error: JAVA_HOME not found in your environment." >&2
    echo "Please set the JAVA_HOME variable in your environment to match the" >&2
    echo "location of your Java installation." >&2
    exit 1
  fi
fi

# For Cygwin, ensure the java executable is available
if $cygwin ; then
  if [ "$JAVA_HOME" = "" ] ; then
    echo "Error: JAVA_HOME not found in your environment." >&2
    echo "Please set the JAVA_HOME variable in your environment to match the" >&2
    echo "location of your Java installation." >&2
    exit 1
  fi
fi

# traverses directory structure from process work directory to filesystem root
# first directory with .mvn subdirectory is considered project base directory
find_maven_basedir() {
  if [ -z "$1" ]
  then
    echo "Path not specified to find_maven_basedir"
    return 1
  fi

  basedir="$1"
  wdir="$1"
  while [ "$wdir" != '/' ] ; do
    if [ -d "$wdir"/.mvn ] ; then
      basedir=$wdir
      break
    fi
    # workaround for JBEAP-8937 (on Solaris 10/Sparc)
    if [ -d "${wdir}" ]; then
      wdir=`cd "$wdir/.."; pwd`
    fi
    # end of workaround
  done
  echo "${basedir}"
}

# concatenates all lines of a file
concat_lines() {
  if [ -f "$1" ]; then
    echo "$(tr -s '\n' ' ' < "$1")"
  fi
}

BASE_DIR=`find_maven_basedir "$(pwd)"`
if [ -z "$BASE_DIR" ]; then
  exit 1;
fi

MAVEN_PROJECTBASEDIR=${MAVEN_BASEDIR:-"$BASE_DIR"}
export MAVEN_PROJECTBASEDIR

##########################################################################################
# Extension to allow automatically downloading the maven-wrapper.jar from Maven-central
# This allows using the maven wrapper in projects that prohibit checking in binary data.
##########################################################################################
if [ -r "$BASE_DIR/.mvn/wrapper/maven-wrapper.jar" ]; then
    if [ "$MVNW_VERBOSE" = true ]; then
      echo "Found .mvn/wrapper/maven-wrapper.jar"
    fi
else
    if [ "$MVNW_VERBOSE" = true ]; then
      echo "Couldn't find .mvn/wrapper/maven-wrapper.jar, downloading it ..."
    fi
    if [ -n "$MVNW_REPOURL" ]; then
      jarUrl="$MVNW_REPOURL/org/apache/maven/wrapper/maven-wrapper/3.2.0/maven-wrapper-3.2.0.jar"
    else
      jarUrl="https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.2.0/maven-wrapper-3.2.0.jar"
    fi
    while [ "$wdir" != '/' ] ; do
      if [ -d "$wdir"/.mvn ] ; then
        if [ -d "$wdir"/.mvn/wrapper ] ; then
          if [ -e "$wdir"/.mvn/wrapper/maven-wrapper.jar ] ; then
            if [ "$MVNW_VERBOSE" = true ]; then
              echo "Found .mvn/wrapper/maven-wrapper.jar"
            fi
            break
          fi
        fi
      fi
      # workaround for JBEAP-8937 (on Solaris 10/Sparc)
      if [ -d "${wdir}" ]; then
        wdir=`cd "$wdir/.."; pwd`
      fi
      # end of workaround
    done
    if [ -e "$wdir"/.mvn/wrapper/maven-wrapper.jar ] ; then
      if [ "$MVNW_VERBOSE" = true ]; then
        echo "Found .mvn/wrapper/maven-wrapper.jar"
      fi
    else
      if [ "$MVNW_VERBOSE" = true ]; then
        echo "Installing .mvn/wrapper/maven-wrapper.jar"
      fi
      if [ -d "$wdir"/.mvn/wrapper ] ; then
        if [ "$MVNW_VERBOSE" = true ]; then
          echo "Installing .mvn/wrapper/maven-wrapper.jar"
        fi
        if [ -n "$MVNW_REPOURL" ]; then
          jarUrl="$MVNW_REPOURL/org/apache/maven/wrapper/maven-wrapper/3.2.0/maven-wrapper-3.2.0.jar"
        else
          jarUrl="https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.2.0/maven-wrapper-3.2.0.jar"
        fi
        if [ "$MVNW_VERBOSE" = true ]; then
          echo "Downloading from: $jarUrl"
        fi

        wrapperJarPath="$wdir/.mvn/wrapper/maven-wrapper.jar"
        if $cygwin; then
          wrapperJarPath=`cygpath --path --windows "$wrapperJarPath"`
        fi

        if command -v wget > /dev/null; then
            if [ "$MVNW_VERBOSE" = true ]; then
              echo "Found wget ... using wget"
            fi
            if [ -z "$MVNW_USERNAME" ] || [ -z "$MVNW_PASSWORD" ]; then
                wget "$jarUrl" -O "$wrapperJarPath" || rm -f "$wrapperJarPath"
            else
                wget --http-user="$MVNW_USERNAME" --http-password="$MVNW_PASSWORD" "$jarUrl" -O "$wrapperJarPath" || rm -f "$wrapperJarPath"
            fi
        elif command -v curl > /dev/null; then
            if [ "$MVNW_VERBOSE" = true ]; then
              echo "Found curl ... using curl"
            fi
            if [ -z "$MVNW_USERNAME" ] || [ -z "$MVNW_PASSWORD" ]; then
                curl -o "$wrapperJarPath" "$jarUrl" -f -L || rm -f "$wrapperJarPath"
            else
                curl --user "$MVNW_USERNAME:$MVNW_PASSWORD" -o "$wrapperJarPath" "$jarUrl" -f -L || rm -f "$wrapperJarPath"
            fi
        else
            if [ "$MVNW_VERBOSE" = true ]; then
              echo "Falling back to using Java to download"
            fi
            javaClass="$BASE_DIR/.mvn/wrapper/MavenWrapperDownloader.java"
            if [ -e "$javaClass" ]; then
                if [ ! -e "$BASE_DIR/.mvn/wrapper/MavenWrapperDownloader.class" ]; then
                    if [ "$MVNW_VERBOSE" = true ]; then
                      echo " - Compiling MavenWrapperDownloader.java ..."
                    fi
                    # Compiling the Java class
                    ("$JAVA_HOME/bin/javac" "$javaClass")
                fi
                if [ -e "$BASE_DIR/.mvn/wrapper/MavenWrapperDownloader.class" ]; then
                    # Running the downloader
                    if [ "$MVNW_VERBOSE" = true ]; then
                      echo " - Running MavenWrapperDownloader.java ..."
                    fi
                    ("$JAVA_HOME/bin/java" -cp .mvn/wrapper MavenWrapperDownloader "$MAVEN_PROJECTBASEDIR")
                fi
            fi
        fi
    fi
fi
##########################################################################################
# End of extension
##########################################################################################

# If specified, validate the SHA-256 sum of the Maven wrapper jar file
wrapperSha256Sum=""
while [ "$wdir" != '/' ] ; do
  if [ -d "$wdir"/.mvn ] ; then
    if [ -d "$wdir"/.mvn/wrapper ] ; then
      if [ -e "$wdir"/.mvn/wrapper/maven-wrapper.jar ] ; then
        if [ -e "$wdir"/.mvn/wrapper/maven-wrapper.properties ] ; then
          wrapperSha256SumProp="$(grep -E '^wrapperSha256Sum=' "$wdir"/.mvn/wrapper/maven-wrapper.properties | cut -d '=' -f 2)"
          if [ -n "$wrapperSha256SumProp" ] ; then
            wrapperSha256Sum="$wrapperSha256SumProp"
          fi
        fi
      fi
    fi
  fi
  # workaround for JBEAP-8937 (on Solaris 10/Sparc)
  if [ -d "${wdir}" ]; then
    wdir=`cd "$wdir/.."; pwd`
  fi
  # end of workaround
done
if [ -n "$wrapperSha256Sum" ]; then
  wrapperSha256Result=false
  if command -v sha256sum > /dev/null; then
    if echo "$wrapperSha256Sum  $wrapperJarPath" | sha256sum -c > /dev/null 2>&1; then
      wrapperSha256Result=true
    fi
  elif command -v shasum > /dev/null; then
    if echo "$wrapperSha256Sum  $wrapperJarPath" | shasum -a 256 -c > /dev/null 2>&1; then
      wrapperSha256Result=true
    fi
  else
    echo "Checksum validation was requested but neither 'sha256sum' or 'shasum' are available."
    echo "Please install either command, or disable validation by removing 'wrapperSha256Sum' from your maven-wrapper.properties."
    exit 1
  fi
  if [ $wrapperSha256Result = false ]; then
    echo "Error: Failed to validate Maven wrapper SHA-256, your Maven wrapper might be compromised." >&2
    echo "Investigate or delete $wrapperJarPath to attempt a clean download." >&2
    echo "If you updated your Maven version, you need to update the specified wrapperSha256Sum property." >&2
    exit 1
  fi
fi

MAVEN_OPTS="$(concat_lines "$MAVEN_PROJECTBASEDIR/.mvn/jvm.config") $MAVEN_OPTS"

# For Cygwin, switch paths to Windows format before running java
if $cygwin; then
  [ -n "$M2_HOME" ] &&
    M2_HOME=`cygpath --path --windows "$M2_HOME"`
  [ -n "$JAVA_HOME" ] &&
    JAVA_HOME=`cygpath --path --windows "$JAVA_HOME"`
  [ -n "$CLASSPATH" ] &&
    CLASSPATH=`cygpath --path --windows "$CLASSPATH"`
  [ -n "$MAVEN_PROJECTBASEDIR" ] &&
    MAVEN_PROJECTBASEDIR=`cygpath --path --windows "$MAVEN_PROJECTBASEDIR"`
fi

# Provide a "standardized" way to retrieve the CLI args that will
# work with both Windows and non-Windows executions.
MAVEN_CMD_LINE_ARGS="$MAVEN_CONFIG $@"
export MAVEN_CMD_LINE_ARGS

WRAPPER_LAUNCHER=org.apache.maven.wrapper.MavenWrapperMain

exec "$JAVA_HOME/bin/java" \
  $MAVEN_OPTS \
  $MAVEN_DEBUG_OPTS \
  -classpath "$MAVEN_PROJECTBASEDIR/.mvn/wrapper/maven-wrapper.jar" \
  "-Dmaven.multiModuleProjectDirectory=${MAVEN_PROJECTBASEDIR}" \
  ${WRAPPER_LAUNCHER} $MAVEN_CONFIG "$@"
