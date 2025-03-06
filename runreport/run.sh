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
# Run reportrunner.class

# REMOVE this when you edit this file.
echo You MUST edit this file before you can compile this application.


# CHANGE the following environment variables to point to IBM Cognos on your system.
#
# You need to change the user, password, namespace, and search path at the bottom,
# too.

if [ "$CRN_HOME" = "" ] ; then
	CRN_HOME=/usr/cognos/c10
fi
if [ "$JAVA_HOME" = "" ] ; then
	JAVA_HOME=/c/j2sdk6
fi

JAVA=$JAVA_HOME/bin/java

# Build the CLASSPATH required

CLASSPATH=
for jar in activation axis axisCognosClient commons-discovery-0.2 commons-logging-1.1 commons-logging-adapters-1.1 \
	commons-logging-api-1.1 dom4j-1.6.1 jaxen-1.1.1 jaxrpc mail saaj wsdl4j-1.5.1 ; do
  CLASSPATH="$CLASSPATH:$CRN_HOME/sdk/java/lib/$jar.jar"
done
CLASSPATH="$CLASSPATH:../Common"


# Go!
#
# By default, runs as anonymous. To run with security on, run as follows, 
# substituting your username, password, and namespace:
# $JAVA -classpath "$CLASSPATH" reportrunner -user myusername -password mypassword -namespace mynamespace
$JAVA -classpath "$CLASSPATH" reportrunner
