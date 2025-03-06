/** 
Licensed Materials - Property of IBM

IBM Cognos Products: DOCS

(C) Copyright IBM Corp. 2005, 2008

US Government Users Restricted Rights - Use, duplication or disclosure restricted by GSA ADP Schedule Contract with
IBM Corp.
*/
/**
 * cmQuery.java
 *
 * Copyright (C) 2008 Cognos ULC, an IBM Company. All rights reserved.
 * Cognos (R) is a trademark of Cognos ULC, (formerly Cognos Incorporated).
 *
 * Description: Simple Content Manager query sample.
*/

import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;

import javax.xml.rpc.ServiceException;

import org.apache.axis.AxisFault;
import org.apache.axis.client.Stub;
import org.apache.axis.message.SOAPHeaderElement;

import com.cognos.developer.schemas.bibus._3.BaseClass;
import com.cognos.developer.schemas.bibus._3.BiBusHeader;
import com.cognos.developer.schemas.bibus._3.CAM;
import com.cognos.developer.schemas.bibus._3.ContentManagerService_PortType;
import com.cognos.developer.schemas.bibus._3.ContentManagerService_ServiceLocator;
import com.cognos.developer.schemas.bibus._3.FormFieldVar;
import com.cognos.developer.schemas.bibus._3.FormatEnum;
import com.cognos.developer.schemas.bibus._3.HdrSession;
import com.cognos.developer.schemas.bibus._3.OrderEnum;
import com.cognos.developer.schemas.bibus._3.PropEnum;
import com.cognos.developer.schemas.bibus._3.QueryOptions;
import com.cognos.developer.schemas.bibus._3.SearchPathMultipleObject;
import com.cognos.developer.schemas.bibus._3.Sort;
import com.cognos.developer.schemas.bibus._3.StringProp;
import com.cognos.developer.schemas.bibus._3.TokenProp;

public class cmQuery
{
	public static void main(String[] args)
	{
		// Attempt a simple CM query.

		String searchPath = new String("/*");
		String userName = null;
		String userPassword = null;
		String userNamespace = null;

		// Process command-line arguments.
		//
		// cmQuery accepts these arguments:
		//
		// --search=searchPath
		// --uid=userName
		// --pwd=userPassword
		// --namespace=userNamespace
		try
		{
			for (int i = 0; i < args.length; i++)
			{
				String[] command =
					{
						args[i].substring(0, args[i].indexOf('=')),
						args[i].substring(args[i].indexOf('=') + 1)};

				if (command[0].compareTo("--search") == 0)
				{
					searchPath = command[1];
				}
				else if (command[0].compareTo("--uid") == 0)
				{
					userName = command[1];
				}
				else if (command[0].compareTo("--pwd") == 0)
				{
					userPassword = command[1];
				}
				else if (command[0].compareTo("--namespace") == 0)
				{
					userNamespace = command[1];
				}
				else
				{
					throw new Exception("Unknown argument: " + args[i]);
				}
			}
		}

		catch (Exception ex)
		{
			System.out.println(ex.getMessage());
			System.exit(-1);
		}

		// Concatenate the read filter to the searchPath this way we
		// ask CM to only return the objects we have read acces on.
		// searchPath += "[permission('read')]";

		try
		{
			// Create the connection to Content Manager Service.
			URL endPointUrl =
				new URL("http://localhost:9300/p2pd/servlet/dispatch");
			ContentManagerService_ServiceLocator service =
				new ContentManagerService_ServiceLocator();
			ContentManagerService_PortType cms =
				service.getcontentManagerService(endPointUrl);

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

			// Add the authentication information, if any.
			//
			// Another option would be to use the logon() and logonAs() methods...
			CAM cam = new CAM();
			cam.setAction("logonAs");

			HdrSession header = new HdrSession();
			if (userName != null)
			{
				FormFieldVar[] vars = new FormFieldVar[3];

				vars[0] = new FormFieldVar();
				vars[0].setName("CAMNamespace");
				vars[0].setValue(userNamespace);
				vars[0].setFormat(FormatEnum.not_encrypted);

				vars[1] = new FormFieldVar();
				vars[1].setName("CAMUsername");
				vars[1].setValue(userName);
				vars[1].setFormat(FormatEnum.not_encrypted);

				vars[2] = new FormFieldVar();
				vars[2].setName("CAMPassword");
				vars[2].setValue(userPassword);
				vars[2].setFormat(FormatEnum.not_encrypted);

				header.setFormFieldVars(vars);
			}
			else
			{
				cam.setAction("logon");
			}

			BiBusHeader bibus = new BiBusHeader();
			bibus.setCAM(cam);
			bibus.setHdrSession(header);

			((Stub)cms).setHeader("http://developer.cognos.com/schemas/bibus/3/", "biBusHeader", bibus);

			// Make the query.
			try
			{
				BaseClass[] results =
					cms.query(
						new SearchPathMultipleObject(searchPath),
						properties,
						sortBy,
						options);

				// Display the results.
				System.out.println("Results:");
				for (int i = 0; i < results.length; i++)
				{
					TokenProp theDefaultName = results[i].getDefaultName();
					StringProp theSearchPath = results[i].getSearchPath();

					System.out.print("\t");
					System.out.print(theDefaultName.getValue());
					System.out.print("\t");
					System.out.println(theSearchPath.getValue());
				}
			}

			catch (AxisFault ex)
			{
				// Fault details can be found via ex.getFaultDetails(),
				// which returns an Element array.

				System.out.println("SOAP Fault:");
				System.out.println(ex.toString());
			}

			catch (RemoteException ex)
			{
				SOAPHeaderElement theException =
					 ((Stub)cms).getHeader(
						"",
						"biBusHeader");

				// You can now use theException to find out more information
				// about the problem.
				System.out.println(theException.toString());

				System.out.println("The request threw an RMI exception:");
				System.out.println(ex.getMessage());
				System.out.println("Stack trace:");
				ex.printStackTrace();
			}
		}

		catch (MalformedURLException ex)
		{
			System.out.println("Malformed URL exception:");
			System.out.println(ex.getMessage());
			System.out.println("Stack trace:");
			ex.printStackTrace();
		}

		catch (ServiceException ex)
		{
			System.out.println("Remote service exception:");
			System.out.println(ex.getMessage());
			System.out.println("Stack trace:");
			ex.printStackTrace();
		}
	}
}
