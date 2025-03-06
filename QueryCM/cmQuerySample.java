/** 
Licensed Materials - Property of IBM

IBM Cognos Products: DOCS

(C) Copyright IBM Corp. 2005, 2008

US Government Users Restricted Rights - Use, duplication or disclosure restricted by GSA ADP Schedule Contract with
IBM Corp.
*/
/**
 * cmQuerySample.java
 *
 * Copyright (C) 2008 Cognos ULC, an IBM Company. All rights reserved.
 * Cognos (R) is a trademark of Cognos ULC, (formerly Cognos Incorporated).
 *
 * Description: Simple Content Manager query sample
*/

import java.rmi.RemoteException;

import org.apache.axis.AxisFault;
import org.apache.axis.client.Stub;
import org.apache.axis.message.SOAPHeaderElement;

import com.cognos.developer.schemas.bibus._3.BaseClass;
import com.cognos.developer.schemas.bibus._3.BiBusHeader;
import com.cognos.developer.schemas.bibus._3.OrderEnum;
import com.cognos.developer.schemas.bibus._3.PropEnum;
import com.cognos.developer.schemas.bibus._3.QueryOptions;
import com.cognos.developer.schemas.bibus._3.SearchPathMultipleObject;
import com.cognos.developer.schemas.bibus._3.Sort;
import com.cognos.developer.schemas.bibus._3.StringProp;
import com.cognos.developer.schemas.bibus._3.TokenProp;

public class cmQuerySample
{

	public String prepareQuery(CRNConnect connection, String searchPath)
	{
		String output = "";

		try
		{

			if ((searchPath == null) || (searchPath.length() == 0) || (searchPath.compareTo("") == 0) )
			{
				return "Invalid searchPath" + System.getProperty("line.separator");
			}

			// Search properties: we need the defaultName and the searchPath.
			PropEnum[] properties =
				{ PropEnum.defaultName, PropEnum.searchPath };

			// Sort options: ascending sort on the defaultName property.
			//
			// The cmQuery.pl sample doesn't do this, it returns the default unsorted response.
			Sort[] sortBy = { new Sort()};
			sortBy[0].setOrder(OrderEnum.ascending);
			sortBy[0].setPropName(PropEnum.defaultName);

			// Query options; use the defaults.
			QueryOptions options = new QueryOptions();

			try
			{
				// sn_dg_sdk_method_contentManagerService_query_start_1
				BaseClass[] results =
					connection.getCMService().query(
						new SearchPathMultipleObject(searchPath),
						properties,
						sortBy,
						options);
				// sn_dg_sdk_method_contentManagerService_query_end_1

				// Display the results.
				System.out.println("Results:");
				output = output + "Results:\n";

				for (int i = 0; i < results.length; i++)
				{
					TokenProp theDefaultName = results[i].getDefaultName();
					StringProp theSearchPath = results[i].getSearchPath();

					//Results are directed to both the console and the return string
					//as this module may be run in a console or a GUI
					System.out.print("\t");
					output = output + "\t";
					System.out.print(theDefaultName.getValue());
					output = output + theDefaultName.getValue();
					System.out.print("\t");
					output = output + "\t";
					System.out.print(theSearchPath.getValue() + "\n");
					output = output + theSearchPath.getValue() + "\n";
				}
			}

			catch (AxisFault ex)
			{
				// Fault details can be found via ex.getFaultDetails(),
				// which returns an Element array.

				System.out.println("SOAP Fault:");
				System.out.println(ex.toString());
			}

			catch (RemoteException remoteEx)
			{
				SOAPHeaderElement theException =
				((Stub)connection.getCMService()).getHeader(
						"",
						"biBusHeader");

				// You can now use theException to find out more information
				// about the problem.

				System.out.println("The request threw an RMI exception:");
				System.out.println(remoteEx.getMessage());
				System.out.println("Stack trace:");
				remoteEx.printStackTrace();

				return theException.toString();
			}

		}
		catch (Exception ex)
		{}

		return output;
	}

}
