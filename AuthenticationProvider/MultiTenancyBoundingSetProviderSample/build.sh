#!/bin/sh
#
# Licensed Materials - Property of IBM
# 
# IBM Cognos Products: CAMAAA
# 
# (C) Copyright IBM Corp. 2011, 2016
# 
# US Government Users Restricted Rights - Use, duplication or disclosure restricted by GSA ADP Schedule Contract with
# IBM Corp.

#
# Build Java files in directory Multitenancy SampleBoundingSetProvider

echo Building SampleBoundingSetProvider

# Build the CLASSPATH required to build the Java samples
_CLASSPATH=../lib/CAM_AAA_CustomIF.jar:../adpaters

# Compile
javac -classpath "$_CLASSPATH" -d . -source 7 -target 7 *.java

# Create jar file
jar cfm0 CAM_AAA_SampleBoundingSetProvider.jar MANIFEST *.class

echo done
