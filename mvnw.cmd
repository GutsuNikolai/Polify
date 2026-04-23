@REM ----------------------------------------------------------------------------
@REM Licensed to the Apache Software Foundation (ASF) under one
@REM or more contributor license agreements.  See the NOTICE file
@REM distributed with this work for additional information
@REM regarding copyright ownership.  The ASF licenses this file
@REM to you under the Apache License, Version 2.0 (the
@REM "License"); you may not use this file except in compliance
@REM with the License.  You may obtain a copy of the License at
@REM
@REM    https://www.apache.org/licenses/LICENSE-2.0
@REM
@REM Unless required by applicable law or agreed to in writing,
@REM software distributed under the License is distributed on an
@REM "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
@REM KIND, either express or implied.  See the License for the
@REM specific language governing permissions and limitations
@REM under the License.
@REM ----------------------------------------------------------------------------

@IF "%DEBUG%" == "" @ECHO OFF
@REM set title of command window
@TITLE Maven Wrapper

@REM Enable extensions and delayed expansion (needed for variables set inside parenthesized blocks)
@SETLOCAL EnableDelayedExpansion
set ERROR_CODE=0

@REM Resolve project base dir
set MAVEN_PROJECTBASEDIR=%~dp0
IF "%MAVEN_PROJECTBASEDIR%"=="" set MAVEN_PROJECTBASEDIR=.
set MAVEN_PROJECTBASEDIR=%MAVEN_PROJECTBASEDIR:~0,-1%

@REM Check for .mvn/wrapper/maven-wrapper.jar, download if missing
set WRAPPER_DIR=%MAVEN_PROJECTBASEDIR%\.mvn\wrapper
set WRAPPER_JAR=%WRAPPER_DIR%\maven-wrapper.jar
set WRAPPER_PROPERTIES=%WRAPPER_DIR%\maven-wrapper.properties

IF NOT EXIST "%WRAPPER_JAR%" (
  IF NOT EXIST "%WRAPPER_DIR%" (
    mkdir "%WRAPPER_DIR%"
  )

  set WRAPPER_URL=
  IF EXIST "%WRAPPER_PROPERTIES%" (
    for /f "usebackq tokens=1,* delims==" %%A in ("%WRAPPER_PROPERTIES%") do (
      if "%%A"=="wrapperUrl" set WRAPPER_URL=%%B
    )
  )

  IF "%WRAPPER_URL%"=="" (
    set WRAPPER_URL=https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.3.2/maven-wrapper-3.3.2.jar
  )

  @ECHO Downloading Maven Wrapper from !WRAPPER_URL!
  powershell -NoProfile -ExecutionPolicy Bypass -Command ^
    "$ErrorActionPreference='Stop';" ^
    "Invoke-WebRequest -UseBasicParsing -Uri '!WRAPPER_URL!' -OutFile '%WRAPPER_JAR%';"
  IF ERRORLEVEL 1 (
    @ECHO Failed to download Maven Wrapper.
    @EXIT /B 1
  )
)

@REM Find Java
IF NOT "%JAVA_HOME%"=="" (
  set JAVA_EXE="%JAVA_HOME%\bin\java.exe"
) ELSE (
  set JAVA_EXE=java
)

@REM Execute MavenWrapperMain
set MAVEN_WRAPPER_MAIN=org.apache.maven.wrapper.MavenWrapperMain
set MAVEN_WRAPPER_LAUNCHER=%WRAPPER_JAR%

%JAVA_EXE% ^
  -classpath "%MAVEN_WRAPPER_LAUNCHER%" ^
  "-Dmaven.multiModuleProjectDirectory=%MAVEN_PROJECTBASEDIR%" ^
  %MAVEN_WRAPPER_MAIN% %*

set ERROR_CODE=%ERRORLEVEL%
@ENDLOCAL & set ERROR_CODE=%ERROR_CODE%
@EXIT /B %ERROR_CODE%
