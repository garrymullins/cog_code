@echo off

rem Licensed Materials - Property of IBM
rem IBM Cognos Products: DOCS
rem (C) Copyright IBM Corp. 2005, 2010
rem US Government Users Restricted Rights - Use, duplication or disclosure restricted by GSA ADP Schedule Contract with
rem IBM Corp.

rem  Copyright (C) 2008 Cognos ULC, an IBM Company. All rights reserved.
rem  Cognos (R) is a trademark of Cognos ULC, (formerly Cognos Incorporated).

rem Build the Java samples.

rem REMOVE this when you edit this file.
echo You MUST edit this file before you can compile this application.

rem *  *  *  *  *  *  *  *  *  *  *
rem CHANGE the following environment variables to point to the
rem Java Development Kit and the IBM Cognos installation location on your system.
rem *  *  *  *  *  *  *  *  *  *  *

set JAVA_HOME=c:/jdk6
set CRN_HOME=../../

rem Build the CLASSPATH

set JAR_HOME=%CRN_HOME%/sdk/java/lib
set JAVAC=%JAVA_HOME%/bin/javac

set CLASSPATH=%JAVA_HOME%/lib/tools.jar
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

rem Compile the new samples, and identify subdirectories that 
rem contain the .java files for the CLASSPATH.

"%JAVAC%" -classpath "%CLASSPATH%" Agents/*.java Alerts/*.java CancelExec/*.java Common/*.java CapabilitiesGUI/*.java ContentStoreExplorer/*.java CreateDrillThrough/*.java DeployPackage/*.java DispatcherControl/*.java ReportCopyMove/*.java EventTrigger/*.java ExecReports/*.java ExecReportsAt/*.java GroupsAndRolesGUI/*.java HandlersCS/*.java PermissionsGUI/*.java PrintReport/*.java QueryCM/*.java RenderReport/*.java ReportAdd/*.java ReportCreate/*.java ReportDelete/*.java ReportParams/*.java ReportSpec/*.java ReportUpgrade/*.java runreport/*.java SaveAs/*.java Scheduler/*.java Security/*.java SendEmail/*.java Submit/*.java TesterCM/*.java TesterQueryService/*.java TestDIMS/*.java ViewAll/*.java ViewCMReports/*.java ViewCMPackages/*.java
