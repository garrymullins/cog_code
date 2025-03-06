@echo off

rem Licensed Materials - Property of IBM
rem 
rem IBM Cognos Products: CAMAAA
rem 
rem (C) Copyright IBM Corp. 2011, 2016
rem 
rem US Government Users Restricted Rights - Use, duplication or disclosure restricted by GSA ADP Schedule Contract with
rem IBM Corp.

rem Build Java files in directory SampleBoundingSetProvider

echo Building SampleBoundingSetProvider

rem Build the CLASSPATH required to build Java files in the directory SampleBoundingSetProvider

set _CLASSPATH=..\lib\CAM_AAA_CustomIF.jar;..\adapters

rem Compile Java files
javac -classpath %_CLASSPATH% -d . -source 7 -target 7 *.java

rem Create jar file
jar cfm0 CAM_AAA_SampleBoundingSetProvider.jar MANIFEST *.class

echo done
