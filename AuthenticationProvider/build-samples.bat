@echo off

rem Licensed Materials - Property of IBM
rem 
rem IBM Cognos Products: CAMAAA
rem 
rem (C) Copyright IBM Corp. 2005, 2012
rem 
rem US Government Users Restricted Rights - Use, duplication or disclosure restricted by GSA ADP Schedule Contract with
rem IBM Corp.

rem  Copyright © 2008 Cognos ULC, an IBM Company. All Rights Reserved.
rem  Cognos and the Cognos logo are trademarks of Cognos ULC (formerly Cognos Incorporated).

rem Build the Authentication Provider samples.

rem Compile all samples.
for %%i IN (JDBCSample MultiTenancyTenantProviderSample MultiTenancyBoundingSetProviderSample TrustedSignonSample TrustedSignonMappingSample  TrustedSignonReplaceSample) DO (
    cd %%i
    call build.bat
    cd ..
)
