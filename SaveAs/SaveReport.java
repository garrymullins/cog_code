/** 
Licensed Materials - Property of IBM

IBM Cognos Products: DOCS

(C) Copyright IBM Corp. 2005, 2008

US Government Users Restricted Rights - Use, duplication or disclosure restricted by GSA ADP Schedule Contract with
IBM Corp.
*/
/**
 * SaveReport.java
 *
 * Copyright (C) 2008 Cognos ULC, an IBM Company. All rights reserved.
 * Cognos (R) is a trademark of Cognos ULC, (formerly Cognos Incorporated).
 *
 * Description: This file contains methods for executing different types of
 * report
 *
 */

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import com.cognos.developer.schemas.bibus._3.AsynchReply;
import com.cognos.developer.schemas.bibus._3.AsynchReplyStatusEnum;
import com.cognos.developer.schemas.bibus._3.AsynchRequest;
import com.cognos.developer.schemas.bibus._3.AsynchSecondaryRequest;
import com.cognos.developer.schemas.bibus._3.BaseParameter;
import com.cognos.developer.schemas.bibus._3.MultilingualToken;
import com.cognos.developer.schemas.bibus._3.Option;
import com.cognos.developer.schemas.bibus._3.ParameterValue;
import com.cognos.developer.schemas.bibus._3.PropEnum;
import com.cognos.developer.schemas.bibus._3.ReportSaveAsEnum;
import com.cognos.developer.schemas.bibus._3.RunOptionBoolean;
import com.cognos.developer.schemas.bibus._3.RunOptionEnum;
import com.cognos.developer.schemas.bibus._3.RunOptionSaveAs;
import com.cognos.developer.schemas.bibus._3.RunOptionStringArray;
import com.cognos.developer.schemas.bibus._3.SearchPathSingleObject;

public class SaveReport
{

	/**
		* saveReport will execute a query/report and save either the report
		* or the report output back in the content store, as specified by the
		* saveAs parameter.
		*
		* @param connect
		* @param report
		* @param saveAs
		*
		* @return
		*/
	public String saveReport(CRNConnect connect, BaseClassWrapper report, boolean saveAs)
	{
		String output = new String();
		String reportName = report.getBaseClassObject().getSearchPath().getValue();

		if ((connect != null) && (report != null))
		{
			PropEnum props[] =
				new PropEnum[] { PropEnum.searchPath, PropEnum.defaultName };

			boolean success = false;

			// Get the list of parameters used by the report, including
			// optional parameters.
			ParameterValue reportParameters[] = new ParameterValue[] {};
			ReportParameters repParms = new ReportParameters();
			try
			{

				BaseParameter[] prm = repParms.getReportParameters(report, connect);

				if (prm != null && prm.length > 0)
				{
					reportParameters =
						ReportParameters.setReportParameters(prm);
				}
			}
			catch (Exception e)
			{
				System.out.println("An error occurred in the saveReport method.");
				e.printStackTrace();
				return "An error occurred in the saveReport method.";
			}

			output += "Running " + reportName + "\n";

			success =
				executeAndSave(
					reportName,
					connect,
					saveAs,
					reportParameters);
			if (!success)
			{
				output =
					output.concat(
						"Error occurred in saveReport()"
							+ " - trying to execute report\n");
			}

		}
		else
		{
			output =
				output.concat("Invalid parameter(s) passed to saveReport.\n");
		}
		output += "Completed successfully.";
		return output;
	}

	/**
		* boolean executeAndSave( String reportName,
		*                        String reportPath,
		*                        boolean saveAs,
		*                        int reportType)
		*
		* This method executes a report and saves the output
		* or the report spec, as indicated by the saveAs flag
		*
	 	* @param 			 reportName
	 	*        			 Specifies search path of the report.
	 	* @param 			 connect
	 	* 		  			 Connection to Server
		* @param saveAs      flag to indicate whether the report spec
		*                  	 should be saved under a new name, or
		*                  	 the output.
		* @param paramValues array of parameter values
		*
		* @return          if the function succeeded
		*
		*/
	public boolean executeAndSave(
		String reportName,
		CRNConnect connect,
		boolean saveAs,
		ParameterValue paramValues[])
	{
		Option executeRunOptions[] = new Option[3];
		Option saveAsRunOptions[] = new Option[1];
		RunOptionBoolean roSaveOutput = new RunOptionBoolean();
		RunOptionStringArray roOutputFormat = new RunOptionStringArray();
		RunOptionBoolean roPrompt = new RunOptionBoolean();
		AsynchReply asynchReply = null;
		AsynchRequest asynchRequest = null;
		String[] reportFormat = null;

		// We may want to save this output, but not by using
		// runOptions. That would be for another sample.
		roSaveOutput.setName(RunOptionEnum.saveOutput);
		roSaveOutput.setValue(false);

		//What format do we want the report in? HTML.
		reportFormat = new String[] { "HTML" };
		roOutputFormat.setName(RunOptionEnum.outputFormat);
		roOutputFormat.setValue(reportFormat);

		//Set the report not to prompt as we pass the parameter if any
		roPrompt.setName(RunOptionEnum.prompt);
		roPrompt.setValue(false);

		// Fill the array with the run options.
		executeRunOptions[0] = roSaveOutput;
		executeRunOptions[1] = roOutputFormat;
		executeRunOptions[2] = roPrompt;

		try
		{
			// Get the initial response
			if (reportName.startsWith("/content"))
			{
				asynchReply =
					connect.getReportService().run(
						new SearchPathSingleObject(reportName),
						paramValues,
						executeRunOptions);

				// If response is not immediately complete, call wait until complete
				if (!asynchReply.getStatus().equals(AsynchReplyStatusEnum.complete))
				{
					while (!asynchReply.getStatus().equals(AsynchReplyStatusEnum.complete))
					{
						//before calling wait, double check that it is okay
						if (hasSecondaryRequest(asynchReply, "wait"))
						{
							asynchReply =
							connect.getReportService().wait(
								asynchReply.getPrimaryRequest(),
									new ParameterValue[] {},
									new Option[] {});
						}
						else
						{
							//wait method not available as expected
							JFrame message = new JFrame();

							JOptionPane.showMessageDialog(
								message,
								"Error: Wait method not available as expected");
							System.out.println("\n\nError: Wait method not available as expected");
							return false;
						}
					}
				}

				asynchRequest = asynchReply.getPrimaryRequest();
			}
			else
			{
				JFrame message = new JFrame();

				JOptionPane.showMessageDialog(
					message,
					"Please supply a search path.");
				System.out.println("\n\nPlease supply a search path.");
			}

		}
		catch (java.rmi.RemoteException remoteEx)
		{
			System.out.println("Caught Remote Exception:\n");
			remoteEx.printStackTrace();
		}

		if (asynchRequest != null)
		{
			if (saveAs)
			{
				String newReportName = "";
				int position = reportName.indexOf("/report");
				if (position >= 0)
				{
					newReportName = "NewReportJ";
				}
				else
				{
					position = reportName.indexOf("/query");
					newReportName = "NewQueryJ";
				}
				String packagePath = reportName.substring(0, position);

				MultilingualToken nameTokens[] = new MultilingualToken[2];
				nameTokens[0] = new MultilingualToken();
				nameTokens[0].setLocale("en-us");
				nameTokens[0].setValue(newReportName);

				RunOptionSaveAs roSaveAs = new RunOptionSaveAs();
				roSaveAs.setName(RunOptionEnum.saveAs);
				roSaveAs.setObjectClass(ReportSaveAsEnum.reportView);
				roSaveAs.setObjectName(nameTokens);
				roSaveAs.setParentSearchPath(packagePath);

				saveAsRunOptions[0] = roSaveAs;

				try
				{
					//call deliver with saveAs option

					asynchReply = connect.getReportService().deliver(asynchRequest, new ParameterValue[] {}, saveAsRunOptions);

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
									connect.getReportService().wait(
										asynchReply.getPrimaryRequest(),
										new ParameterValue[] {},
										new Option[] {});
							}
							else
							{
								//wait method not available as expected
								JFrame message = new JFrame();

								JOptionPane.showMessageDialog(
									message,
									"Error: Wait method not available as expected");
								System.out.println("\n\nError: Wait method not available as expected");
								return false;
							}
						}
					}

					return true;
				}
				catch (java.rmi.RemoteException remoteEx)
				{
					System.out.println("Caught Remote Exception:\n");
					remoteEx.printStackTrace();
				}
			}
			else
			{
				Option saveRunOptions[] = new Option[1];
				RunOptionBoolean bSaveOutput = new RunOptionBoolean();
				bSaveOutput.setName(RunOptionEnum.saveOutput);
				bSaveOutput.setValue(true);
				saveRunOptions[0] = bSaveOutput;
				try
				{
					//call deliver with option to save
					asynchReply = connect.getReportService().deliver(asynchRequest, new ParameterValue[] {}, saveRunOptions);
	
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
									connect.getReportService().wait(
										asynchReply.getPrimaryRequest(),
										new ParameterValue[] {},
										new Option[] {});
							}
							else
							{
								//wait method not available as expected
								JFrame message = new JFrame();

								JOptionPane.showMessageDialog(
									message,
									"Error: Wait method not available as expected");
								System.out.println("\n\nError: Wait method not available as expected");
								return false;
							}
						}
					}
					return true;
				}
				catch (java.rmi.RemoteException remoteEx)
				{
					System.out.println("Caught Remote Exception:\n");
					remoteEx.printStackTrace();
				}
			}

		}
		else
		{
			System.out.println("execute() failed to return a valid report");
		}
		return false;
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
