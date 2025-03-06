#!/bin/sh
#
#  Licensed Materials - Property of IBM
#  
#  IBM Cognos Products: DOCS
#  
#  (C) Copyright IBM Corp. 2005, 2010
#  
#  US Government Users Restricted Rights - Use, duplication or disclosure restricted by GSA ADP Schedule Contract with
#  IBM Corp.
#
#  Copyright (C) 2008 Cognos ULC, an IBM Company. All rights reserved.
#  Cognos (R) is a trademark of Cognos ULC, (formerly Cognos Incorporated).
#
# Build the Java samples.

# REMOVE this when you edit this file.
echo You MUST edit this file before you can compile this application.


# *  *  *  *  *  *  *  *
# CHANGE the following environment variables to point to the
# Java Development Kit and IBM Cognos on your system.
# *  *  *  *  *  *  *  *

if [ "$CRN_HOME" = "" ] ; then
	CRN_HOME=/usr/cognos/c10
fi
if [ "$JAVA_HOME" = "" ] ; then
	JAVA_HOME=/c/j2sdk6
fi

JAR_HOME=$CRN_HOME/sdk/java/lib

JAVAC=$JAVA_HOME/bin/javac

# Build the CLASSPATH required to build the Java samples

CLASSPATH=$JAVA_HOME/lib/tools.jar
for jar in activation axis axisCognosClient commons-discovery-0.2 commons-logging-1.1 commons-logging-adapters-1.1 \
	commons-logging-api-1.1 dom4j-1.6.1 jaxen-1.1.1 jaxrpc mail saaj wsdl4j-1.5.1; do
  CLASSPATH="$CLASSPATH:$JAR_HOME/$jar.jar"
done
CLASSPATH="$CLASSPATH:../Common:../Security:../ReportParams:../HandlersCS:../ReportSpec:../ViewCMReports:../ViewCMPackages:../ExecReports:../runreport:../Scheduler:../ContentStoreExplorer" 

# Compile

$JAVAC -classpath "$CLASSPATH" *.java

