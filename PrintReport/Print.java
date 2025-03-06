/** 
Licensed Materials - Property of IBM

IBM Cognos Products: DOCS

(C) Copyright IBM Corp. 2005, 2008

US Government Users Restricted Rights - Use, duplication or disclosure restricted by GSA ADP Schedule Contract with
IBM Corp.
*/
/**
 * Print.java
 *
 * Copyright (C) 2008 Cognos ULC, an IBM Company. All rights reserved.
 * Cognos (R) is a trademark of Cognos ULC, (formerly Cognos Incorporated).
 *
 * Description: This code sample demonstrates how to run and print a report,
 * and how to get, add, and delete printers from the content store using the
 * following methods:
 *     - run
 *       Use this method to run a report, query, or report view.
 *     - wait
 *       Use this method to notify the server that the issuer of the request
 *       is still waiting for the output, and to request that the processing
 *       be continued.
 *     - deliver
 *       Use this method to request that the output of a report be printed
 *     - query
 *       Use this method to request objects from Content Manager.
 *     - update
 *       Use this method to modify existing objects in the content store.
 *     - release
 *       Use this method to remove inactive requests from the report service
 *       cache earlier than they would be removed automatically by the system.
 *       Removing abandoned requests makes resources available for
 *       other requests, improving performance.
 *     - delete
 *       Use this method to delete objects from the content store.
 */

import com.cognos.developer.schemas.bibus._3.AsynchReply;
import com.cognos.developer.schemas.bibus._3.AsynchReplyStatusEnum;
import com.cognos.developer.schemas.bibus._3.AsynchRequest;
import com.cognos.developer.schemas.bibus._3.AsynchSecondaryRequest;
import com.cognos.developer.schemas.bibus._3.BaseClass;
import com.cognos.developer.schemas.bibus._3.BaseParameter;
import com.cognos.developer.schemas.bibus._3.DeleteOptions;
import com.cognos.developer.schemas.bibus._3.Option;
import com.cognos.developer.schemas.bibus._3.ParameterValue;
import com.cognos.developer.schemas.bibus._3.Printer;
import com.cognos.developer.schemas.bibus._3.PropEnum;
import com.cognos.developer.schemas.bibus._3.QueryOptions;
import com.cognos.developer.schemas.bibus._3.RunOptionBoolean;
import com.cognos.developer.schemas.bibus._3.RunOptionEnum;
import com.cognos.developer.schemas.bibus._3.RunOptionString;
import com.cognos.developer.schemas.bibus._3.RunOptionStringArray;
import com.cognos.developer.schemas.bibus._3.SearchPathMultipleObject;
import com.cognos.developer.schemas.bibus._3.SearchPathSingleObject;
import com.cognos.developer.schemas.bibus._3.Sort;
import com.cognos.developer.schemas.bibus._3.StringProp;
import com.cognos.developer.schemas.bibus._3.TokenProp;
import com.cognos.developer.schemas.bibus._3.UpdateOptions;

public class Print
{

	/**
	 * This Java method returns a boolean that indicates whether this report
	 * printed successfully on the specified printer.
	 * @param connection
	 * 		  Connection to Server
	 * @param report
	 *        Specifies the report.
	 * @param printerPath
	 *        Specifies the location of the printer. Use the getAvailablePrinters
	 *        Java method to select the location of the printer.
	 * @param asynchRequest
	 *        Specifies the primary request. If no primary response is passed
	 *        to this method, this method calls the report service run method.
	 *        A primary request is necessary to issue a secondary request, such
	 *        as a print request, because a secondary request can only continue a
	 *        conversation established by a primary request.
	 */
	public String print(
		CRNConnect connection,
		BaseClassWrapper report,
		String printerPath,
		AsynchRequest asynchRequest)
	{
		AsynchReply asynchReply = null;
		String reportPath = report.getBaseClassObject().getSearchPath().getValue();

		try
		{
			Option[] options = setPrintRunOptions(printerPath);
			ParameterValue parameterValues[] = new ParameterValue[] {};
			ReportParameters reportParameters = new ReportParameters();
			BaseParameter[] baseParameters = reportParameters.getReportParameters(report, connection);

			//if no current request is passed in, we need to run the report
			if (asynchRequest == null)
			{
				if (baseParameters != null && baseParameters.length > 0)
				{
					parameterValues = ReportParameters.setReportParameters(baseParameters);
				}

				if (parameterValues == null || parameterValues.length <= 0)
				{
					parameterValues = new ParameterValue[] {};
				}

				//set options for running report
				Option execReportRunOptions[] = new Option[2];
				RunOptionBoolean saveOutputRunOption = new RunOptionBoolean();
				RunOptionBoolean promptFlag = new RunOptionBoolean();

				//Set the option for saving the output to false
				saveOutputRunOption.setName(RunOptionEnum.saveOutput);
				saveOutputRunOption.setValue(false);

				//Set the report not to prompt as we pass the parameter (if any)
				promptFlag.setName(RunOptionEnum.prompt);
				promptFlag.setValue(false);


				// Fill the array with the rest of the run options.
				execReportRunOptions[0] = saveOutputRunOption;
				execReportRunOptions[1] = promptFlag;

				//Run report

				asynchReply =
				connection.getReportService().run(
						new SearchPathSingleObject(reportPath),
						parameterValues,
						execReportRunOptions);

				// If response is not immediately complete, call wait until complete
				if (!asynchReply.getStatus().equals(AsynchReplyStatusEnum.complete))
				{
					while (!asynchReply.getStatus().equals(AsynchReplyStatusEnum.complete))
					{
						//before calling wait, double check that it is okay
						if (hasSecondaryRequest(asynchReply, "wait"))
						{
							asynchReply =
							connection.getReportService().wait(
								asynchReply.getPrimaryRequest(),
									new ParameterValue[] {},
									new Option[] {});
						}
						else
						{
							return "Error: Wait method not available as expected.";
						}
					}

				}

				asynchRequest = asynchReply.getPrimaryRequest();

			}

			if (asynchRequest != null)
			{
				//call print
				asynchReply = connection.getReportService().deliver(asynchRequest, parameterValues, options);

				// If response is not immediately complete, call wait until complete
				if ((!asynchReply.getStatus().equals(AsynchReplyStatusEnum.complete)) && (!asynchReply.getStatus().equals(AsynchReplyStatusEnum.conversationComplete)))
				{
					while ((!asynchReply.getStatus().equals(AsynchReplyStatusEnum.complete))
							&& (!asynchReply.getStatus().equals(AsynchReplyStatusEnum.conversationComplete)))
					{
						//before calling wait, double check that it is okay
						if (hasSecondaryRequest(asynchReply, "wait"))
						{
							asynchReply =
								connection.getReportService().wait(
									asynchReply.getPrimaryRequest(),
									new ParameterValue[] {},
									new Option[] {});
						}
						else
						{
							return "Error: Wait method not available as expected.";
						}
					}
				}
			}
			else
			{
				System.out.println(
					"AsynchRequest null, unable to issue secondary request.");
			}
			// release the conversation to free resources.
			connection.getReportService().release(asynchRequest);

		}
		catch (Exception e)
		{
			System.out.println(
				"An error occurred in the print Java method.\n" + e);
			return "An error occurred in the print Java method.";
		}

		return "Print method complete";
	}

	/**
	 * This Java method returns an array that specifies the run options
	 * required by the print Java method.
	 * @param printerPath
	 *        Specifies the location of the printer. Use the getAvailablePrinters
	 *        Java method to select the location of the printer.
	 */
	private Option[] setPrintRunOptions(String printerPath)
	{
		try
		{
			Option[] options = new Option[4];

			RunOptionString printer = new RunOptionString();

			printer.setName(RunOptionEnum.printer);
			printer.setValue(printerPath);

			RunOptionBoolean print = new RunOptionBoolean();
			print.setName(RunOptionEnum.print);
			print.setValue(true);

			// Set the output format for the report to PDF.
			RunOptionStringArray format = new RunOptionStringArray();
			format.setName(RunOptionEnum.outputFormat);

			format.setValue(new String[] { "PDF" });

			//Set the report not to prompt as we pass the parameter (if any)
			RunOptionBoolean promptFlag = new RunOptionBoolean();
			promptFlag.setName(RunOptionEnum.prompt);
			promptFlag.setValue(false);

			options[0] = printer;
			options[1] = print;
			options[2] = format;
			options[3] = promptFlag;

			return options;
		}
		catch (Exception e)
		{
			System.out.println("Exception in Print.class\n" + e);
			return null;
		}
	}

	/**
	 * This Java method gets information for all available printers from
	 * the content store and returns a BaseClass array of printer objects.
	 *
	 * @param connection
	 * 		  Connection to Server
	 *
	 *  @return printers[]
	 *        Returns an array containing the searchPath and defaultName
	 *        properties for each printer.
	 */
	public BaseClass[] getAvailablePrinters(CRNConnect connection)
	{
		BaseClass printers[] = new BaseClass[] {};
		PropEnum props[] =
			new PropEnum[] { PropEnum.searchPath, PropEnum.defaultName };
		try
		{
			printers =
				connection.getCMService().query(
					new SearchPathMultipleObject("CAMID(\":\")/printer"),
					props,
					new Sort[] {},
					new QueryOptions());
		}
		catch (Exception e)
		{
			System.out.println(e);
		}
		return printers;
	}

	/**
	 * This Java method adds a new printer to the content store.
	 * @param connection
	 * 		  Connection to Server
	 * @param name
	 *        Specifies a unique name for the printer.
	 * @param networkAddress
	 *        Specifies the network address of the new printer.
	 * @return
	 *        Returns an integer with a value of 1 if the printer was created
	 *        successfully in the content store or 0 if the request failed.
	 */
	public String addPrinter(
		CRNConnect connection,
		String name,
		String networkAddress)
	{
		PropEnum props[] =
			new PropEnum[] {
				PropEnum.searchPath,
				PropEnum.defaultName,
				PropEnum.parent };
		try
		{
			BaseClass[] printers =
				connection.getCMService().query(
					new SearchPathMultipleObject("CAMID(\":\")/printer[@name=\'" + name + "\']"),
					props,
					new Sort[] {},
					new QueryOptions());

			if ((printers != null) && (printers.length > 0))
			{
				return ("A printer with this name already exists.");
			}

			StringProp location = new StringProp();
			String printerPath = "CAMID(\":\")";

			TokenProp printerName = new TokenProp();
			printerName.setValue(name);

			location.setValue(networkAddress);
			// DO NOT set searchPath when you add an object to the content store.

			Printer newPrinter = new Printer();
			newPrinter.setDefaultName(printerName);
			newPrinter.setPrinterAddress(location);

			// Identify the printer parent object.
			printers =
				connection.getCMService().query(
				new SearchPathMultipleObject(printerPath),
					props,
					new Sort[] {},
					new QueryOptions());

			newPrinter.setParent(printers[0].getParent());

			CSHandlers csh = new CSHandlers();
			csh.addObjectToCS(connection, newPrinter, printerPath);

			return "Printer added successfully";
		}
		catch (Exception e)
		{
			System.out.println(e);
			return "Failed to add new printer";
		}
	}

	/**
	 * This Java method updates the network address of a printer in
	 * the content store.
	 *
	 * @param connection
	 * 		  Connection to Server
	 * @param name
	 *        Specifies a unique name for the printer.
	 * @param networkAddress
	 *        Specifies the network address of the new printer.
	 * @return
	 *        Returns an integer with a value of 1 if the printer's network
	 *        address was updated successfully or 0 if the request failed.
	 */
	public String changePrinterAddress(
		CRNConnect connection,
		String name,
		String newNetworkAddress)
	{
		PropEnum props[] =
			new PropEnum[] { PropEnum.searchPath, PropEnum.defaultName };
		Printer prt = new Printer();
		try
		{
			BaseClass[] printers =
				connection.getCMService().query(
					new SearchPathMultipleObject("CAMID(\":\")/printer"),
					props,
					new Sort[] {},
					new QueryOptions());

			for (int i = 0; i < printers.length; i++)
			{
				String printerName = printers[i].getDefaultName().getValue();
				if (printerName.compareToIgnoreCase(name) == 0)
				{
					prt = (Printer)printers[i];
				}
			}

			StringProp networkAddress = new StringProp();
			networkAddress.setValue(newNetworkAddress);
			prt.setPrinterAddress(networkAddress);

			BaseClass[] bc = new BaseClass[1];
			bc[0] = prt;

			PropEnum[] updateReturnProps =  new PropEnum[] { PropEnum.printerAddress };
			UpdateOptions updateOpts = new UpdateOptions();
			updateOpts.setReturnProperties(updateReturnProps);

			BaseClass[] newBc = connection.getCMService().update(bc, updateOpts);

			return (
				"Network Address for printer "
					+ name
					+ " set to "
					+ ((Printer)newBc[0]).getPrinterAddress().getValue()
					+ ".");
		}
		catch (Exception e)
		{
			System.out.println(e);
			return ("Exception occurred attempting to change printer address");
		}
	}

	/**
	 * This Java method updates the name of a printer in the content store.
	 * @param connection
	 * 		  Connection to Server
	 * @param oldName
	 *        Specifies the current name of the printer.
	 * @param newName
	 *        Specifies a new unique name for the printer.
	 * @return
	 *        Returns an integer with a value of 1 if the printer's name was
	 *        updated successfully or 0 if the request failed.
	 */
	public String changePrinterName(
		CRNConnect connection,
		String oldName,
		String newName)
	{
		PropEnum props[] =
			new PropEnum[] { PropEnum.searchPath, PropEnum.defaultName };
		Printer prt = new Printer();
		try
		{
			BaseClass[] printers =
				connection.getCMService().query(
					new SearchPathMultipleObject("CAMID(\":\")/printer"),
					props,
					new Sort[] {},
					new QueryOptions());

			for (int i = 0; i < printers.length; i++)
			{
				String printerName = printers[i].getDefaultName().getValue();
				if (printerName.compareToIgnoreCase(oldName) == 0)
				{
					prt = (Printer)printers[i];
				}
			}

			TokenProp newNameT = new TokenProp();
			newNameT.setValue(newName);
			prt.setDefaultName(newNameT);

			BaseClass[] bc = new BaseClass[1];
			bc[0] = prt;

			BaseClass[] newBc = connection.getCMService().update(bc, new UpdateOptions());

			return (
				"Printer "
					+ oldName
					+ " renamed to "
					+ ((Printer)newBc[0]).getDefaultName().getValue()
					+ ".");
		}
		catch (Exception e)
		{
			System.out.println(e);
			return ("Exception occurred attempting to change printer name");
		}
	}

	/**
	 * This Java method deletes a printer from the content store.
	 * @param connection
	 * 		  Connection to Server
	 * @param name
	 *        Specifies the unique name of the printer.
	 * @return
	 *        Returns an integer with a value of 1 if the printer was deleted
	 *        successfully or 0 if the request failed.
	 */
	public String deletePrinter(CRNConnect connection, String name)
	{
		PropEnum props[] =
			new PropEnum[] {
				PropEnum.searchPath,
				PropEnum.defaultName,
				PropEnum.parent };
		String toBeDeleted = new String();
		//Printer newPrinter = new Printer();
		try
		{
			BaseClass[] printers =
				connection.getCMService().query(
					new SearchPathMultipleObject("CAMID(\":\")/printer"),
					props,
					new Sort[] {},
					new QueryOptions());
			boolean doesExist = false;
			for (int i = 0; i < printers.length; i++)
			{
				String printerName = printers[i].getDefaultName().getValue();
				if (printerName.compareToIgnoreCase(name) == 0)
				{
					toBeDeleted = name;
					doesExist = true;
					break;
				}
			}

			if (!doesExist)
			{
				return ("Printer does not exist!");
			}

			// Set the options for the delete method.
			DeleteOptions del = new DeleteOptions();

			// Set the force option to true. When the force option is true, a
			// selected object will be deleted if the current user has either
			// write or setPolicy permission for the following:
			//    - the selected object
			//    - the parent of the selected object
			//    - every descendant of the selected object
			del.setForce(true);

			if (toBeDeleted != null)
			{
				System.out.println("Deleting printer: " + toBeDeleted);

				BaseClass bc[] =
					connection.getCMService().query(
						new SearchPathMultipleObject("CAMID(\":\")/printer[@name=\'" + toBeDeleted + "\']"),
						props,
						new Sort[] {},
						new QueryOptions());

				if ((bc != null) && (bc.length > 0))
				{
					int i = connection.getCMService().delete(bc, del);
					if (i > 0)
					{
						return (
							"The printer "
								+ toBeDeleted
								+ " was deleted successfully");
					}
					else
					{
						return (
							"Error occurred while deleting the printer "
								+ toBeDeleted);
					}
				}
				else
				{
					return ("Printer " + name + " was not found");
				}
			}
		}
		catch (Exception e)
		{
			System.out.println(e.getMessage());
			return ("Caught Exception while deleting printer.");
		}
		return "Error occurred in deletePrinter.";
	}

	public boolean hasSecondaryRequest(
		AsynchReply response,
		String secondaryRequest)
	{
		AsynchSecondaryRequest[] secondaryRequests =
			response.getSecondaryRequests();
		for (int i = 0; i < secondaryRequests.length; i++)
		{
			if (secondaryRequests[i].getName().compareTo(secondaryRequest)
				== 0)
			{
				return true;
			}
		}
		return false;
	}

}
