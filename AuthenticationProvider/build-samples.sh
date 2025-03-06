#!/bin/sh
#
# Licensed Materials - Property of IBM
# 
# IBM Cognos Products: CAMAAA
# 
# (C) Copyright IBM Corp. 2005, 2012
# 
# US Government Users Restricted Rights - Use, duplication or disclosure restricted by GSA ADP Schedule Contract with
# IBM Corp.
# 
#  Copyright © 2008 Cognos ULC, an IBM Company. All Rights Reserved.
#  Cognos and the Cognos logo are trademarks of Cognos ULC (formerly Cognos Incorporated).
#
# Build the Authentication Provider samples.
#
for d in JDBCSample MultiTenancyTenantProviderSample MultiTenancyBoundingSetProviderSample TrustedSignonSample TrustedSignonMappingSample TrustedSignonReplaceSample;do cd $d;./build.sh;cd ..; done
