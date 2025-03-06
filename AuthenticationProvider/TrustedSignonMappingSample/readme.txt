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

Running the IBM Cognos SDK custom authentication TrustedSignonMapping provider sample
--------------------------------------------------------------------

To configure and run the sample provided, follow these steps:

    1) Add the Java SDK to your path.

    2) Build the sample using the command build.bat on Windows or 
       build.sh on Unix.

    3) Add the jar file to your IBM Cognos 10 classpath or copy the jar file to 
       <install>/webapps/p2pd/WEB-INF/lib.

    4) Copy the domainMapping.xml configuration file to the <install>/configuration  
       directory. 

    5) Edit the domainMapping.xml configuration file as follows:

        Line 1: abc.cde.com=namespaceID1
	<domainName>=<mapped to the namespaceID>
	Defines the mapping between the domain name and the namespaceID. namespaceID is case-sensitive.
	For example,
	a) for a remote username in this format: userA@abc.cde.com, use abc.cde.com=namespaceID1
	b) for a remote username is this format: testABC\userA, use testABC=namespaceID1
	
        Line 2:domainSplitDelimiter=@
	<domainSplitDelimiter>=<delimiter>
	Defines the delimiter to split out domain information based on the remote username.
	For example,
	a) for a remote username in this format: userA@abc.cde.com, use domainSplitDelimiter=@
	b) for a remote username is this format: testABC\userA, use domainSplitDelimiter=\

	Line 3: domainSplitPosition=after
	<domainSplitPosition>=<before|after|none>			
	Defines which part to extract as the domain name based on the remote_user value.
	For example,
	a) for a remote username in this format: userA@abc.cde.com (where abc.cde.com is the domain), use domainSplitPostion=after
	b) for a remote username is this format: testABC\userA, use domainSplitPosition=before

	Line 4: remoteUserSplitPosition=before
	<remoteUserSplitPosition>=<before|after|none>
	Defines which part to extract as remote_user
	For example, if the remote username is userA@abc.cde.com,
	a) to pass the whole value to the namespaceID1, set remoteUserSplitPosition=none
	b) to only pass userA to namespaceID1, set remoteUserSpllitPosition=before
	

    6) In IBM Cognos Configuration, configure the Custom Java namespace 
       using the configuration tool. Note that the class name and NamespaceID
       are case sensitive.
          Type:               Custom Java provider
          Namespace ID:       Sample1
          Java class name:    TrustedSignonMapping

    7) Apply changes and restart IBM Cognos 10.

The new authentication namespace should now appear in IBM Cognos 10 for 
logon and security administration tasks within IBM Cognos 10.

NOTE: Should IBM Cognos 10 fail to start up once the new namespace 
is configured, please verify your configuration. IBM Cognos 10 will 
not start up if it cannot successfully communicate to the 
authentication source.

