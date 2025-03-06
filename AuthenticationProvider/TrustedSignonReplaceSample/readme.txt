/**
 * Licensed Materials - Property of IBM
 * 
 * IBM Cognos Products: CAMAAA
 * 
 * (C) Copyright IBM Corp. 2005, 2012
 * 
 * US Government Users Restricted Rights - Use, duplication or disclosure restricted by GSA ADP Schedule Contract with
 * IBM Corp.
 */

Running the IBM Cognos SDK custom Trusted Signon Sample
-------------------------------------------------------

To configure and run the sample provided, please follow these steps:
    1) Add the Java SDK to your path.
    2) Build the sample using the command build.bat on Windows or build.sh on
       Unix.
    3) Add the jar file to your IBM Cognos 10 classpath or copy the jar to
       <install>/webapps/p2pd/WEB-INF/lib.
    4) In Cognos configuration, configure the Custom Java namespace with the 
       following settings (the class name is case sensitive):
            Type:               Custom Java provider
            Namespace ID:       Sample2
            Java class name:    TrustedSignonReplaceSample
    5) In Cognos configuration, configure a namespace with Namespace ID "TS",
       which uses the REMOTE_USER without the domain prefix for external identity 
       mapping.
	6) Apply changes and restart IBM Cognos 10 
    7) Copy the SetCookie.htm to your WebServer
    8) You must "hit" the SetCookie.htm prior to the logon to the trusted
       signon namespace.

The new authentication namespace should now appear in IBM Cognos 10 for logon.

