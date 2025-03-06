/** 
Licensed Materials - Property of IBM

IBM Cognos Products: DOCS

(C) Copyright IBM Corp. 2005, 2008

US Government Users Restricted Rights - Use, duplication or disclosure restricted by GSA ADP Schedule Contract with
IBM Corp.
*/
/**
* ViewPackagesAndReports.java
*
* Copyright (C) 2008 Cognos ULC, an IBM Company. All rights reserved.
* Cognos (R) is a trademark of Cognos ULC, (formerly Cognos Incorporated).
*
* Description: This code sample demonstrates how to display all the 
 *	              packages, reports and queries in the content store using the 
 *	             following methods:
 *	             - query (searchPath, properties, sortBy, options)
 *	             Use this method to request objects from the content store.
*/

import com.cognos.developer.schemas.bibus._3.BaseClass;
import com.cognos.developer.schemas.bibus._3.OrderEnum;
import com.cognos.developer.schemas.bibus._3.PropEnum;
import com.cognos.developer.schemas.bibus._3.QueryOptions;
import com.cognos.developer.schemas.bibus._3.SearchPathMultipleObject;
import com.cognos.developer.schemas.bibus._3.Sort;

public class ViewPackagesAndReports
{
	/**
		* Use this method to show the packages, reports and queries in 
		* the content store.
		* 
		* @param         connection
		*                Specifies the object that provides the connection to
		*                the server. 
		*
		* @return        Returns a string that either shows the name of each 
		*                package, report or query in the content store, or displays
		*                a message to indicate that there are no packages, reports, 
		*                or queries to show. 
		*
		*/

	public String viewAll(CRNConnect connection)
	{
		String output = new String();
		PropEnum props[] =
			new PropEnum[] { PropEnum.searchPath, PropEnum.defaultName };

		if (connection.getCMService() != null)
		{
			Sort sortArray[] = { new Sort()};
			sortArray[0].setOrder(OrderEnum.ascending);
			sortArray[0].setPropName(PropEnum.defaultName);

			try
			{
				/**
				 * Use this method to query the packages in 
				 * the content store.
				 * 
				 * @param         "/content//package"
				 *                Specifies the search path string so that 
				 *                Content Manager can locate the requested
				 *                objects, which are packages in this example.
				 * @param         props
				 *                Specifies alternate properties that you want 
				 *                returned for the package object. 
				 *                When no properties are specified, as in this 
				 *                example, the default properties of
				 *                searchPath and defaultName are provided.
				 * @param         sortArray
				 *                Specifies the sort criteria in an array.
				 * @param         QueryOptions
				 *                Specifies any options for this method.
				 * @return        Returns an array of packages.
				 */

				String SearchPathPackages = "/content//package";
								
				BaseClass bc[] =
					connection.getCMService().query(
						new SearchPathMultipleObject(SearchPathPackages),
						props,
						sortArray,
						new QueryOptions());

				// If packages exist in the content store, the output shows the 
				// package name on one line, followed by a second line that shows
				// the search path of the package. Then, list the reports in the 
				// same output format that was used for the packages. 

				if (bc == null)
				{
					System.out.println(
						"\n\nError occurred in function viewAll.");
					output =
						output.concat("Error occurred in function viewAll.");
					return output;
				}

				if (bc.length <= 0)
				{
					output =
						output.concat(
							"There are currently no published"
								+ " packages or reports.");
					return output;
				}

				for (int i = 0; i < bc.length; i++)
				{
					output =
						output.concat(
							"  " + bc[i].getDefaultName().getValue() + "\n");
					output =
						output.concat(
							"      " + bc[i].getSearchPath().getValue() + "\n");
					output = output.concat("\n       Reports:\n");
					//System.out.println(output);

					String quotChar = "\'";
					if (bc[i].getDefaultName().getValue().indexOf(quotChar)
						>= 0)
					{
						quotChar = "\"";
					}
					// Retrieve the list of reports for this package.
					
					String SearchPathReports = bc[i].getSearchPath().getValue()
								+ "//report";
								
					BaseClass bcReports[] =
						connection.getCMService().query(
							new SearchPathMultipleObject(SearchPathReports),
							props,
							sortArray,
							new QueryOptions());
														
					if (bcReports != null && (bcReports.length > 0))
					{
						for (int j = 0; j < bcReports.length; j++)
						{
							output =
								output.concat(
									"         "
										+ bcReports[j]
											.getDefaultName()
											.getValue()
										+ "\n");
							output =
								output.concat(
									"             "
										+ bcReports[j].getSearchPath().getValue()
										+ "\n");
							//System.out.println(output);
						}
					}
					else
					{
						output =
							output.concat(
								"           "
									+ "No reports to view for this package\n");
					}
					output = output.concat("\n       Queries:\n");
					//System.out.println(output);

					quotChar = "\'";
					if (bc[i].getDefaultName().getValue().indexOf(quotChar)
						>= 0)
					{
						quotChar = "\"";
					}
					// Retrieve the list of queries for this package.
					// List the queries in the same output format that 
					// was used for the packages.
					String SearchPathQueries = bc[i].getSearchPath().getValue()
								+ "//query";
								
					BaseClass bcQueries[] =
						connection.getCMService().query(
							new SearchPathMultipleObject(SearchPathQueries),
							props,
							sortArray,
							new QueryOptions());

					if (bcQueries != null && (bcQueries.length > 0))
					{
						for (int j = 0; j < bcQueries.length; j++)
						{
							output =
								output.concat(
									"         "
										+ bcQueries[j]
											.getDefaultName()
											.getValue()
										+ "\n");
							output =
								output.concat(
									"             "
										+ bcQueries[j].getSearchPath().getValue()
										+ "\n");
							//System.out.println(output);
						}
					}
					else
					{
						output =
							output.concat(
								"          "
									+ "No queries to view for this package\n\n");
					}
				}
			}
			catch (Exception e)
			{
				System.out.println(e.getMessage());
				output =
					output.concat(
						"View All:\nCannot connect to CM.\n"
							+ "Ensure that IBM Cognos is running.");
			}
		}
		else
		{
			System.out.println("\n\nInvalid parameter passed to viewAll().");
			output = output.concat("Invalid parameter passed to viewAll().");
		}
		return output;
	}
}
