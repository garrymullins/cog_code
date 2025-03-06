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

# Build Java files in directory ExecReports

# *  *  *  *  *  *  *  *
# CHANGE the following environment variables to point to the
# Java Development Kit and IBM Cognos on your system.
# *  *  *  *  *  *  *  *

if [ "$CRN_HOME" = "" ] ; then
	CRN_HOME=/usr/cognos/c10
fi

if [ "$JAVA_HOME" = "" ]; then
	JAVA_HOME=/c/j2sdk6
fi
# Build the CLASSPATH required to build Java files

JAR_HOME="${CRN_HOME}/sdk/java/lib"
JAVAC="${JAVA_HOME}/bin/javac"

CLASSPATH="${JAR_HOME}/tools.jar"
for jar in activation axis axisCognosClient commons-discovery-0.2 commons-logging-1.1 commons-logging-adapters-1.1 \
        commons-logging-api-1.1 dom4j-1.6.1 jaxen-1.1.1 jaxrpc mail saaj wsdl4j-1.5.1 ; do
  CLASSPATH="${CLASSPATH}:${JAR_HOME}/${jar}.jar"
done

# Compile Java files
${JAVAC} -classpath ${CLASSPATH} *.java
