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

# CHANGE the following environment variables to point to IBM Cognos on your system.

if [ "$CRN_HOME" = "" ] ; then
	CRN_HOME=/usr/cognos/c10
fi

if [ "$JAVA_HOME" = "" ] ; then
	JAVA_HOME=/c/j2sdk6
fi

JAR_HOME=$CRN_HOME/sdk/java/lib

for jar in activation axis axisCognosClient commons-discovery-0.2 commons-logging-1.1 commons-logging-adapters-1.1 \
	commons-logging-api-1.1 dom4j-1.6.1 jaxen-1.1.1 jaxrpc mail saaj wsdl4j-1.5.1 ; do
  CLASSPATH="$CLASSPATH:$JAR_HOME/$jar.jar"
done

${JAVA_HOME}/bin/java -classpath ${CLASSPATH} Trigger ${1} ${2} ${3} ${4} ${5}
