Licensed Materials - Property of IBM

IBM Cognos Products: DOCS

(C) Copyright IBM Corp. 2005, 2008

US Government Users Restricted Rights - Use, duplication or disclosure restricted by GSA ADP Schedule Contract with
IBM Corp.

Copyright (C) 2008 Cognos ULC, an IBM Company. All rights reserved.
Cognos (R) is a trademark of Cognos ULC, (formerly Cognos Incorporated).

reportrunner - A Command-line Report Running Tool

reportrunner is a simple application that runs a report and saves its output
as HTML.

This version is written in Java

Requirements:

- IBM Cognos
- a running IBM Cognos BI server
- the IBM Cognos amples databases and samples deployment archive must be
  installed and configured (if you don't specify a report name on the
  command-line)

Usage:

reportrunner [options]

-host hostName            Host name of server.
                          Default: localhost
-port portNumber          Port number of the server 'host'.
                          Default: 9300
-report searchPath        Search path in the content store to a report.
                          Default: /content/folder[@name='Samples']/folder[@name='Models']/package[@name='GO Data Warehouse (query)']/folder[@name='SDK Report Samples']/report[@name='Product Introduction List']
-output outputPath        File name for the output HTML document.
                          Default: reportrunner.html
-user userName            User to log on as. Must exist in 'userNamespace'.
                          Default: none (requires Anonymous access or it will fail)
-password userPassword    Password for 'userName' in 'userNamespace'.
                          Default: none
-namespace userNamespace  Security namespace to log on to.
                          Default: none

Compiling:

To compile, you need to reference these JAR files:

* axis.jar
* axisCognosClient.jar
* jaxrpc.jar
* saaj.jar
* xalan.jar
* xml-apis.jar

Running:

To run, you need to reference these JAR files:

* axis.jar
* axisCognosClient.jar
* commons-discovery.jar
* commons-logging.jar
* jaxrpc.jar
* saaj.jar
* xalan.jar
* xercesImpl.jar
* xml-apis.jar

Please note that the samples are not intended to be treated as end-user
applications. Refer to the sample code for examples of how to use the
API when developing your own applications.

