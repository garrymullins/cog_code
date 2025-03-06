@echo off

rem Licensed Materials - Property of IBM
rem IBM Cognos Products: DOCS
rem (C) Copyright IBM Corp. 2005, 2010
rem US Government Users Restricted Rights - Use, duplication or disclosure restricted by GSA ADP Schedule Contract with
rem IBM Corp.

rem  Copyright (C) 2007 Cognos ULC, an IBM Company. All rights reserved.
rem  Cognos (R) is a trademark of Cognos ULC, (formerly Cognos Incorporated).

rem Build Java files in directory ContentStoreExplorer

rem CHANGE the following environment variable to point to the Java Development Kit
rem on your system.

set JAVA_HOME=c:/jdk6
set CRN_HOME=../../../

rem Build the CLASSPATH required to build Java files in
rem the directory ContentStoreExplorer

set JAR_HOME=%CRN_HOME%sdk/java/lib
set JAVAC=%JAVA_HOME%/bin/javac

rem Create the Classpath

set CLASSPATH=
set CLASSPATH=%CLASSPATH%;%JAVA_HOME%/lib/tools.jar
set CLASSPATH=%CLASSPATH%;%JAR_HOME%/activation.jar
set CLASSPATH=%CLASSPATH%;%JAR_HOME%/axis.jar
set CLASSPATH=%CLASSPATH%;%JAR_HOME%/axisCognosClient.jar
set CLASSPATH=%CLASSPATH%;%JAR_HOME%/commons-discovery-0.2.jar
set CLASSPATH=%CLASSPATH%;%JAR_HOME%/commons-logging-1.1.jar
set CLASSPATH=%CLASSPATH%;%JAR_HOME%/commons-logging-adapters-1.1.jar
set CLASSPATH=%CLASSPATH%;%JAR_HOME%/commons-logging-api-1.1.jar
set CLASSPATH=%CLASSPATH%;%JAR_HOME%/jaxen-1.1.1.jar
set CLASSPATH=%CLASSPATH%;%JAR_HOME%/jaxrpc.jar
set CLASSPATH=%CLASSPATH%;%JAR_HOME%/mail.jar
set CLASSPATH=%CLASSPATH%;%JAR_HOME%/saaj.jar
set CLASSPATH=%CLASSPATH%;%JAR_HOME%/dom4j-1.6.1.jar
set CLASSPATH=%CLASSPATH%;%JAR_HOME%/wsdl4j-1.5.1.jar
set CLASSPATH=%CLASSPATH%;../Common
set CLASSPATH=%CLASSPATH%;../ContentStoreExplorer
set CLASSPATH=%CLASSPATH%;../ExecReports
set CLASSPATH=%CLASSPATH%;../HandlersCS
set CLASSPATH=%CLASSPATH%;../ReportParams
set CLASSPATH=%CLASSPATH%;../ReportSpec
set CLASSPATH=%CLASSPATH%;../runreport
set CLASSPATH=%CLASSPATH%;../Scheduler
set CLASSPATH=%CLASSPATH%;../Security
set CLASSPATH=%CLASSPATH%;../ViewCMReports
set CLASSPATH=%CLASSPATH%;../ViewCMPackages

rem Compile Java files
"%JAVAC%" -classpath %CLASSPATH% *.java
