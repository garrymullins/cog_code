-- Licensed Materials - Property of IBM
-- IBM Cognos Products: camaaa
-- (C) Copyright IBM Corp. 2011, 2012
-- US Government Users Restricted Rights - Use, duplication or disclosure restricted by GSA ADP Schedule Contract with IBM Corp. 
USE [SampleUserData]

IF  EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'USERS') AND OBJECTPROPERTY(object_id, N'IsUserTable') = 1)
DROP TABLE USERS

IF  EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'GROUPS') AND OBJECTPROPERTY(object_id, N'IsUserTable') = 1)
DROP TABLE GROUPS

IF  EXISTS (SELECT * FROM sys.views WHERE object_id = OBJECT_ID(N'OBJECTVIEW'))
DROP VIEW OBJECTVIEW

GO

CREATE TABLE USERS(
	USERID		[int] NOT NULL,
	USERNAME	[nvarchar](255) NOT NULL,
	PASSWORD	[nvarchar](255) NOT NULL,
	FULLNAME	[nvarchar](255) NULL,
	EMAIL		[nvarchar](255) NULL,
	LOCALE		[char](5) NULL,
	TENANT		[nvarchar](128) NULL,
 CONSTRAINT [PK_USERS] PRIMARY KEY CLUSTERED 
(
	[USERID] ASC
)
) ON [PRIMARY]

CREATE TABLE GROUPS(
	GROUPID		[int] NOT NULL,
	GROUPNAME	[nvarchar](255) NOT NULL,
	USERID		[int] NULL,
	TENANT		[nvarchar](128) NOT NULL
) ON [PRIMARY]

GO

CREATE VIEW OBJECTVIEW
AS
SELECT	
	USERID		AS ID, 
	USERNAME	AS USERNAME, 
	FULLNAME	AS [NAME],
	TENANT      AS TENANT,
	1			AS ISUSER, 
	0			AS ISGROUP
FROM USERS
UNION
SELECT	
	GROUPID		AS ID, 
	NULL		AS USERNAME, 
	GROUPNAME	AS [NAME], 
	TENANT      AS TENANT,
	0			AS ISUSER, 
	1			AS ISGROUP
FROM GROUPS
