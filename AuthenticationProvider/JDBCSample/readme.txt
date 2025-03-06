/**
 * Licensed Materials - Property of IBM
 * 
 * IBM Cognos Products: CAMAAA
 * 
 * (C) Copyright IBM Corp. 2005, 2013
 * 
 * US Government Users Restricted Rights - Use, duplication or disclosure restricted by GSA ADP Schedule Contract with
 * IBM Corp.
 */

There are two versions of the JDBC provider sample. The classic version does not support
the new session failover capabilities of IBM Cognos that were introudced in 10.2.1.

The restorable version can handle restoring a session after failover, and demonstrates the changes
necessary to implement failover in your own custom provider.


Before running these samples
----------------------------
You will need an empty database to populate with users and groups. A database initilization script 
is provided for both IBM DB2 and Microsoft SQL server

	1) Initialize your database with the appropriate script. See your database vendors
		documentation for the appropriate tool to import and run the script.
		- dbInit_db2.sql for IBM DB2
		- dbInit_sqlserver.swl for Microsoft SQL Server
	2) Add at least one user to the USERS table. All fields are required and cannot be null.
	3) Optionally, add at least one group to the GROUPS table. All fields are required and cannot be null.


Running the IBM Cognos SDK custom authentication JDBC provider sample
--------------------------------------------------------------------

To configure and run the sample provided, please follow these steps:

    1) Add the Java SDK to your path.
    2) Build the sample using the command build.bat on Windows or 
       build.sh on Unix.
    3) Add the jar file to your IBM Cognos 10 classpath or copy the jar to 
       <install>/webapps/p2pd/WEB-INF/lib.
    4) Add the database driver jar file to your classpath or copy the files to     
       <install>/webapps/p2pd/WEB-INF/lib.
    5) Copy the JDBC configuration file to the <install>/configuration  
       directory. Ensure that the name contains the namespaceID to be 
       recognized (namespaceID is case sensitive).
       Ex:
          namespaceID = Sample1 
          JDBC configurations file = JDBC_Config_Sample1.properties
    6) Modify the content of the configuration file to point to your 
       database server and specify the database username and password to use.
    7) In IBM Cognos Configuration, configure the Custom Java namespace 
       using the configuration tool, (the class name and NamespaceID
       are case sensitive):
          Type:               Custom Java provider
          Namespace ID:       Sample1
          Java class name:    JDBCSample
    8) Apply changes and restart IBM Cognos 10.

The new authentication namespace should now appear in IBM Cognos 10 for 
logon and security administration tasks within IBM Cognos 10.

NOTE: Should IBM Cognos 10 fail to start up once the new namespace 
is configured, please verify your configuration. IBM Cognos 10 will 
not start up if it cannot successfully communicate to the 
authentication source.

Running the IBM Cognos SDK custom authentication RestorableJDBC provider sample
--------------------------------------------------------------------

To configure and run the sample provided, please follow these steps:

    1) Add the Java SDK to your path.
    2) Build the sample using the command build.bat on Windows or 
       build.sh on Unix.
    3) Add the jar file to your IBM Cognos 10 classpath or copy the jar to 
       <install>/webapps/p2pd/WEB-INF/lib.
    4) Add the database driver jar file to your classpath or copy the files to     
       <install>/webapps/p2pd/WEB-INF/lib.
    5) Copy the JDBC configuration file to the <install>/configuration  
       directory. Ensure that the name contains the namespaceID to be 
       recognized (namespaceID is case sensitive).
       Ex:
          namespaceID = Restorable 
          JDBC configurations file = JDBC_Config_Restorable.properties
    6) Modify the content of the configuration file to point to your 
       database server and specify the database username and password to use.
    7) In IBM Cognos Configuration, configure the Custom Java namespace 
       using the configuration tool, (the class name and NamespaceID
       are case sensitive):
          Type:               Custom Java provider
          Namespace ID:       Restorable
          Java class name:    RestorableJDBCSample
    8) Apply changes and restart IBM Cognos 10.

The new authentication namespace should now appear in IBM Cognos 10 for 
logon and security administration tasks within IBM Cognos 10.

NOTE: Should IBM Cognos 10 fail to start up once the new namespace 
is configured, please verify your configuration. IBM Cognos 10 will 
not start up if it cannot successfully communicate to the 
authentication source.

Configuring the IBM Cognos SDK custom authentication JDBC provider sample for Multi-Tenancy
-------------------------------------------------------------------------------------------

To configure and run the sample with Multi-Tenancy enabled, please follow these steps:

    1) Configure either the JDBC or RestorableJDBC provider sample as described in steps 1 - 7
       above.
    2) In IBM Cognos Configuration, select the Custom Java namespace in the left pane.  In the
       right pane, click to edit the "Advanced properties". Add the following properties:
       
          multitenancy.TenantPattern	~/parameters/tenant
          
       NOTE: it will be necessary to set the TENANT in the users table to an appropriate value
             for each user.
    3) Apply changes and restart IBM Cognos 10.

Please refer to Tenant Patterns within the Multi-Tenancy product documentation for details.


