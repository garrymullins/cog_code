/**
 * Licensed Materials - Property of IBM
 * 
 * IBM Cognos Products: CAMAAA
 * 
 * (C) Copyright IBM Corp. 2011, 2012
 * 
 * US Government Users Restricted Rights - Use, duplication or disclosure restricted by GSA ADP Schedule Contract with
 * IBM Corp.
 */

Running the IBM Cognos SDK custom tenant BoundingSet provider sample
--------------------------------------------------------------------

To configure and run the sample provided, please follow these steps:

    1) Add the Java SDK to your path.
    2) Build the sample using the command build.bat on Windows or 
       build.sh on Unix.
    3) Add the path to the jar file to your IBM Cognos 10 classpath 
       or copy the jar to <install>/webapps/p2pd/WEB-INF/lib.
    4) Add the path to the database driver jar file to your classpath 
       or copy the files to <install>/webapps/p2pd/WEB-INF/lib.
    5) Add the 	usersToBoundingSets.properties file to your classpath or 
       copy the files to <install>/webapps/p2pd/WEB-INF/lib.
    6) Configure the tenantBoudingSetMapping's type as ProviderClass, 
       class name is: SampleBoundingSetProvider.
    7) Add the following entries to the Advanced properties of your 
       authentication namespace.
		usersToBoundingSetsFile 		usersToBoundingSets.properties
    8) Currently the usersToBoundingSets.properties file map users in the 
	   starter namespace sample. If desired, modify the entries to 
	   users in your namespace.
    9) Apply changes and restart IBM Cognos 10.

When users in the usersToBoundingSets.properties logon to the 
authentication namespace, the tenant ID field in the audit database
should now be populated.

NOTE: If IBM Cognos 10 fails to start up once the new namespace 
is configured, please verify your configuration. IBM Cognos 10 will 
not start up if it cannot successfully communicate to the 
authentication source.

