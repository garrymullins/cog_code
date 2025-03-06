#!/bin/sh
#
# Licensed Materials - Property of IBM
# 
# IBM Cognos Products: CAMAAA
# 
# (C) Copyright IBM Corp. 2005, 2016
# 
# US Government Users Restricted Rights - Use, duplication or disclosure restricted by GSA ADP Schedule Contract with
# IBM Corp.
# 
#  Copyright © 2008 Cognos ULC, an IBM Company. All Rights Reserved.
#  Cognos and the Cognos logo are trademarks of Cognos ULC (formerly Cognos Incorporated).
#
# Build Java files in directory TrustedSignonSample

echo Building TrustedSignonSample

# Build the CLASSPATH required to build the Java samples
_CLASSPATH=../lib/CAM_AAA_CustomIF.jar:../adapters

# Compile
javac -classpath "$_CLASSPATH" -d . -source 7 -target 7 *.java

# Create jar file
jar cfm0 CAM_AAA_TrustedSignonSample.jar MANIFEST *.class

echo done
