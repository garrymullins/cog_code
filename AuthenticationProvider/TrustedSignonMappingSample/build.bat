@echo off

rem Licensed Materials - Property of IBM
rem 
rem IBM Cognos Products: CAMAAA
rem 
rem (C) Copyright IBM Corp. 2005, 2016
rem 
rem US Government Users Restricted Rights - Use, duplication or disclosure restricted by GSA ADP Schedule Contract with
rem IBM Corp.

rem  Copyright © 2008 Cognos ULC, an IBM Company. All Rights Reserved.
rem  Cognos and the Cognos logo are trademarks of Cognos ULC (formerly Cognos Incorporated).

rem Build Java files in directory TrustedSignonMapping

echo Building TrustedSignonMapping

rem Build the CLASSPATH required to build Java files in the directory TrustedSignonMapping

set _CLASSPATH=..\lib\CAM_AAA_CustomIF.jar;..\adapters

rem Compile Java files
javac -classpath %_CLASSPATH% -d . -source 7 -target 7 *.java

rem Create jar file
jar cfm0 CAM_AAA_TrustedSignonMapping.jar MANIFEST *.class

echo done
