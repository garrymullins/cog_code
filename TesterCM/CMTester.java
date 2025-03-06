/** 
Licensed Materials - Property of IBM

IBM Cognos Products: DOCS

(C) Copyright IBM Corp. 2005, 2008

US Government Users Restricted Rights - Use, duplication or disclosure restricted by GSA ADP Schedule Contract with
IBM Corp.
*/
/**
 * CMTester.java
 *
 * Copyright (C) 2008 Cognos ULC, an IBM Company. All rights reserved.
 * Cognos (R) is a trademark of Cognos ULC, (formerly Cognos Incorporated).
 *
 * Description: This code sample demonstrates how to get information about 
 *           an object using the query method. 
 *           Use this method to request objects from Content Manager.
 */

import com.cognos.developer.schemas.bibus._3.Account;
import com.cognos.developer.schemas.bibus._3.BaseClass;
import com.cognos.developer.schemas.bibus._3.PropEnum;
import com.cognos.developer.schemas.bibus._3.QueryOptions;
import com.cognos.developer.schemas.bibus._3.SearchPathMultipleObject;
import com.cognos.developer.schemas.bibus._3.Sort;

public class CMTester
{
	/**
	* This Java method returns a string that contains either the information 
	* about the specified objects if the request succeeded or an error message 
	* if the request failed.
	*
	* @param connection
	* 		 Connection to Server
	* @return output
	*      Returns the searchPath, defaultName, creationTime, and version 
	*      of the specified objects. 
	*/

	public String contentMgrTester(CRNConnect connection)
	{
		String output = new String();
		PropEnum props[] =
			new PropEnum[] {
				PropEnum.searchPath,
				PropEnum.defaultName,
				PropEnum.creationTime,
				PropEnum.version };

		if (connection.getCMService() == null)
		{
			System.out.println(
				"\n\nInvalid parameter passed to function contentMgrTester.");
			output =
				output.concat(
					"Invalid parameter passed to function contentMgrTester.");
			return output;
		}

		try
		{
			BaseClass bc[] =
				connection.getCMService().query(
					new SearchPathMultipleObject("~"),
					props,
					new Sort[] {},
					new QueryOptions());

			if (bc != null)
			{
				if (bc.length > 0)
				{
					for (int i = 0; i < bc.length; i++)
					{
						Account myAccount = (Account)bc[i];
						String sCD = new String();

						// Build up the formatted date and time 
						String crDatTim = new String();
						sCD = myAccount.getCreationTime().getValue().toString();

						// Define the appropriate strings to look for to 
						// return the date and time values
						String yrSrchStr = new String();
						yrSrchStr = ",YEAR=";
						String monSrchStr = new String();
						monSrchStr = ",MONTH=";
						String daySrchStr = new String();
						daySrchStr = ",DAY_OF_MONTH=";
						String hrSrchStr = new String();
						hrSrchStr = ",HOUR_OF_DAY=";
						String minSrchStr = new String();
						minSrchStr = ",MINUTE=";
						String secSrchStr = new String();
						secSrchStr = ",SECOND=";

						// Find the correct position in the returned 
						// string for date and time values
						int yearPos = sCD.indexOf(yrSrchStr);
						int monthPos = sCD.indexOf(monSrchStr);
						int dayPos = sCD.indexOf(daySrchStr);
						int hourPos = sCD.indexOf(hrSrchStr);
						int minPos = sCD.indexOf(minSrchStr);
						int secPos = sCD.indexOf(secSrchStr);

						String chrsMonth = new String();

						// Build the creation date string starting 
						// with the month
						String char1 =
							sCD.substring(monthPos + 8, monthPos + 9);
						if (char1.equals(","))
						{
							crDatTim =
								"The CreationTime is: 0"
									+ sCD.substring(monthPos + 7, monthPos + 8)
									+ "/";
							chrsMonth =
								sCD.substring(monthPos + 7, monthPos + 8);
						}
						else
						{
							crDatTim =
								"The CreationTime is: "
									+ sCD.substring(monthPos + 7, monthPos + 9)
									+ "/";
							chrsMonth =
								sCD.substring(monthPos + 7, monthPos + 9);
						}

						// adjust the month from the index 
						// starting at 0 for January
						int intMonth;
						intMonth = Integer.parseInt(chrsMonth);
						intMonth = intMonth + 1;
						crDatTim =
							"The creation time is: "
								+ Integer.toString(intMonth)
								+ "/";

						// add the day
						String char2 = sCD.substring(dayPos + 15, dayPos + 16);
						if (char2.equals(","))
						{
							crDatTim =
								crDatTim
									+ sCD.substring(dayPos + 14, dayPos + 15)
									+ "/";
						}
						else
						{
							crDatTim =
								crDatTim
									+ sCD.substring(dayPos + 14, dayPos + 16)
									+ "/";
						}

						// add the year
						crDatTim =
							crDatTim
								+ sCD.substring(yearPos + 6, yearPos + 10)
								+ " ";

						// add the hour
						String char3 =
							sCD.substring(hourPos + 14, hourPos + 15);
						if (char3.equals(","))
						{
							crDatTim =
								crDatTim
									+ "0"
									+ sCD.substring(hourPos + 13, hourPos + 14)
									+ ":";
						}
						else
						{
							crDatTim =
								crDatTim
									+ sCD.substring(hourPos + 13, hourPos + 15)
									+ ":";
						}

						// add the minute
						String char4 = sCD.substring(minPos + 9, minPos + 10);
						if (char4.equals(","))
						{
							crDatTim =
								crDatTim
									+ "0"
									+ sCD.substring(minPos + 8, minPos + 9)
									+ ":";
						}
						else
						{
							crDatTim =
								crDatTim
									+ sCD.substring(minPos + 8, minPos + 10)
									+ ":";
						}

						// add the second
						String char5 = sCD.substring(secPos + 9, secPos + 10);
						if (char5.equals(","))
						{
							crDatTim =
								crDatTim
									+ "0"
									+ sCD.substring(secPos + 8, secPos + 9)
									+ "  \n";
						}
						else
						{
							crDatTim =
								crDatTim
									+ sCD.substring(secPos + 8, secPos + 10)
									+ "  \n";
						}

						System.out.println(
							"The searchPath is: "
								+ myAccount.getSearchPath().getValue());
						System.out.println(
							"\n\nThe DefaultName is: "
								+ myAccount.getDefaultName().getValue());
						System.out.println(crDatTim);
						System.out.println(
							"The Version is: "
								+ myAccount.getVersion().getValue());
						System.out.println(
							"\nContent Manager is responding and operational.");

						output =
							output.concat(
								"The searchPath is: "
									+ myAccount.getSearchPath().getValue()
									+ "\n");
						output =
							output.concat(
								"The DefaultName is: "
									+ myAccount.getDefaultName().getValue()
									+ "\n");
						output = output.concat(crDatTim);
						output =
							output.concat(
								"The Version is: "
									+ myAccount.getVersion().getValue()
									+ "\n\n");
						output =
							output.concat(
								"Content Manager is responding and operational.");
					}
				}
			}
			else
			{
				System.out.println("\n\nError occurred in contentMgrTester.");
				output = output.concat("Error occurred in contentMgrTester.");
			}
		}
		catch (java.rmi.RemoteException remoteEx)
		{
			output =
				output.concat(
					"CM Tester:"
						+ "\nCannot connect to CM."
						+ "\nEnsure that IBM Cognos is running.");
			output = output.concat("\n\n" + remoteEx.getMessage());
		}

		return output;
	}
}
