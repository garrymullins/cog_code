@echo off

rem Licensed Materials - Property of IBM
rem IBM Cognos Products: DOCS
rem (C) Copyright IBM Corp. 2005, 2010
rem US Government Users Restricted Rights - Use, duplication or disclosure restricted by GSA ADP Schedule Contract with
rem IBM Corp.

rem  Copyright (C) 2007 Cognos ULC, an IBM Company. All rights reserved.
rem  Cognos (R) is a trademark of Cognos ULC, (formerly Cognos Incorporated).

rem Run reportrunner.class

rem You may need to change the following environment variable to point 
rem to the Java runtime on your system.

set JAVA_HOME=../../../jre/bin/java.exe
set CRN_HOME=../../../

set JAR_HOME=%CRN_HOME%sdk/java/lib

rem Build the CLASSPATH required to run reportrunner.class

set JAVA=%JAVA_HOME%/bin/java

set CLASSPATH=
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
set CLASSPATH=%CLASSPATH%;%JAR_HOME%/wsdl4j-1.5.1.jar
set CLASSPATH=%CLASSPATH%;../Common

rem Run reportrunner.class
rem
rem By default, runs as anonymous. To run with security on, run as follows, 
rem substituting your username, password, and namespace:
rem "%JAVA_HOME%" -classpath %CLASSPATH% reportrunner -user myusername -password mypassword -namespace mynamespace
"%JAVA_HOME%" -classpath %CLASSPATH% reportrunner
