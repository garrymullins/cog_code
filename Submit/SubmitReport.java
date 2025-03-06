/** 
Licensed Materials - Property of IBM

IBM Cognos Products: DOCS

(C) Copyright IBM Corp. 2005, 2012

US Government Users Restricted Rights - Use, duplication or disclosure restricted by GSA ADP Schedule Contract with
IBM Corp.
*/
/**
 * SubmitReport.java
 *
 * Copyright (C) 2008 Cognos ULC, an IBM Company. All rights reserved.
 * Cognos (R) is a trademark of Cognos ULC, (formerly Cognos Incorporated).
 *
 * Description: This file contains methods for executing different types of
 * report
 */

import java.util.Date;
import java.util.GregorianCalendar;

import com.cognos.developer.schemas.bibus._3.AddOptions;
import com.cognos.developer.schemas.bibus._3.AsynchDetailEventID;
import com.cognos.developer.schemas.bibus._3.AsynchReply;
import com.cognos.developer.schemas.bibus._3.AsynchReplyStatusEnum;
import com.cognos.developer.schemas.bibus._3.AsynchSecondaryRequest;
import com.cognos.developer.schemas.bibus._3.BaseClass;
import com.cognos.developer.schemas.bibus._3.BaseClassArrayProp;
import com.cognos.developer.schemas.bibus._3.JobDefinition;
import com.cognos.developer.schemas.bibus._3.JobStepDefinition;
import com.cognos.developer.schemas.bibus._3.NmtokenProp;
import com.cognos.developer.schemas.bibus._3.Option;
import com.cognos.developer.schemas.bibus._3.OptionArrayProp;
import com.cognos.developer.schemas.bibus._3.ParameterValue;
import com.cognos.developer.schemas.bibus._3.PropEnum;
import com.cognos.developer.schemas.bibus._3.QueryOptions;
import com.cognos.developer.schemas.bibus._3.Report;
import com.cognos.developer.schemas.bibus._3.RunOptionBoolean;
import com.cognos.developer.schemas.bibus._3.RunOptionEnum;
import com.cognos.developer.schemas.bibus._3.RunOptionStringArray;
import com.cognos.developer.schemas.bibus._3.SearchPathMultipleObject;
import com.cognos.developer.schemas.bibus._3.SearchPathSingleObject;
import com.cognos.developer.schemas.bibus._3.Sort;
import com.cognos.developer.schemas.bibus._3.StringProp;
import com.cognos.developer.schemas.bibus._3.TokenProp;
import com.cognos.developer.schemas.bibus._3.UpdateActionEnum;

public class SubmitReport
{

	/**
		* submitReport will submit a query/report for execution as a job
		*
		* @param connect		Connection to Server
		* @param report			Report to be added
		* @param submitAtFlag	If true, set to run later
		*
		* @return
		*/
	public String submitReport(
		CRNConnect connect,
		String report,
		boolean submitAtFlag)
	{
		AsynchReply asynchReply = null;
		String output = new String();
		int i = 0;

		if ((connect != null) && (report != null))
		{
			PropEnum props[] =
				new PropEnum[] { PropEnum.searchPath, PropEnum.defaultName };
			ParameterValue reportParameters[] = new ParameterValue[] {};

			BaseClass repPth[] = new BaseClass[] {};
			String quotChar = "\'";
			if (report.indexOf(quotChar) >= 0)
			{
				quotChar = "\"";
			}

			try
			{
				repPth =
					connect.getCMService().query(
						new SearchPathMultipleObject("/content//report[@name="
							+ quotChar
							+ report
							+ quotChar
							+ "]"),
						props,
						new Sort[] {},
						new QueryOptions());
			}
			catch (org.apache.axis.AxisFault axisEx)
			{
				return ("Caught Axis Fault:\n");
				//return ("Caught Axis Fault:\n" + axisEx.toString());
			}
			catch (java.rmi.RemoteException remoteEx)
			{
				return ("Caught Remote Exception:\n");
				//return ("Caught Remote Exception:\n" + remoteEx.toString());
			}

			Option runOpts[] = new Option[3];
			RunOptionBoolean roSaveOutput = new RunOptionBoolean();
			RunOptionBoolean roPrompt = new RunOptionBoolean();
			RunOptionStringArray roOutputFormat = new RunOptionStringArray();

			// We want to save this output
			roSaveOutput.setName(RunOptionEnum.saveOutput);
			roSaveOutput.setValue(true);

			//Set the report not to prompt as we pass the parameter if any
			roPrompt.setName(RunOptionEnum.prompt);
			roPrompt.setValue(false);

			//What format do we want the report in: PDF, HTML, or XML?
			roOutputFormat.setName(RunOptionEnum.outputFormat);
			roOutputFormat.setValue(new String[] { "HTML" });

			// Fill the array with the run options.
			runOpts[0] = roSaveOutput;
			runOpts[1] = roOutputFormat;
			runOpts[2] = roPrompt;

			String jobName = "NewJobJ";
			quotChar = "\'";
			if (jobName.indexOf(quotChar) >= 0)
			{
				quotChar = "\"";
			}
			String jobPath =
				"/content/folder[@name='Samples']/folder[@name='Models']/package"
					+ "[@name='GO Sales (query)']"
					+ "/jobDefinition[@name="
					+ quotChar
					+ jobName
					+ quotChar
					+ "]";

			// set submit time to now + 15 minutes
			Date submitAtDate = new Date();
			long submitAtTimeInMillis = submitAtDate.getTime();
			submitAtDate.setTime(submitAtTimeInMillis + (15 * 60000));
			GregorianCalendar startTime = new GregorianCalendar();
			startTime.setTime(submitAtDate);

			if ((repPth != null) && (repPth.length > 0))
			{
				String reportName = repPth[0].getSearchPath().getValue();
				System.out.println("Submitting report: " + reportName);
				try
				{
					createJob(
						connect,
						jobPath,
						jobName,
						new String[] { reportName });

					// submit the job for execution
					String reportEventID = "-1";
					if (submitAtFlag)
					{
						reportEventID =
							connect.getEventMgmtService().runAt(
								startTime,
								new SearchPathSingleObject(jobPath),
								reportParameters,
								runOpts);
					}
					else
					{
						// sn_dg_sdk_method_jobService_run_start_1
						asynchReply =
							connect.getJobService().run(
								new SearchPathSingleObject(jobPath),
								reportParameters,
								runOpts);
						// sn_dg_sdk_method_jobService_run_end_1

						// If response is not immediately complete, call wait until complete
						if (!asynchReply.getStatus().equals(AsynchReplyStatusEnum.complete) &&
						!asynchReply.getStatus().equals(AsynchReplyStatusEnum.conversationComplete))
						{
							while (!asynchReply.getStatus().equals(AsynchReplyStatusEnum.complete))
							{
								//before calling wait, double check that it is okay
								if (hasSecondaryRequest(asynchReply, "wait"))
								{
									// sn_dg_sdk_method_jobService_wait_start_1
									asynchReply =
									connect.getJobService().wait(
										asynchReply.getPrimaryRequest(),
											new ParameterValue[] {},
											new Option[] {});
									// sn_dg_sdk_method_jobService_wait_end_1
								}
								else
								{
									return "Error: Wait method not available as expected.";
								}
							}
						}

						// sn_dg_sdk_method_jobService_run_start_2
						for (i = 0; i < asynchReply.getDetails().length; i++)
						{
							if (asynchReply.getDetails()[i] instanceof AsynchDetailEventID)

							{
								reportEventID = ((AsynchDetailEventID)asynchReply.getDetails()[i]).getEventID();
							}
						}
						// sn_dg_sdk_method_jobService_run_end_2
					}

					PropEnum propsQ[] = new PropEnum[2];
					propsQ[0] = PropEnum.searchPath;
					propsQ[1] = PropEnum.defaultName;
					connect.getCMService().query(
						new SearchPathMultipleObject(jobPath),
						propsQ,
						new Sort[] {},
						new QueryOptions());

					//  show the eventID returned for the submitted report
					output =
						"The eventID for this job is:    "
							+ reportEventID
							+ "\n";

				}
				catch (java.rmi.RemoteException remoteEx)
				{
					output = output.concat("Error occurred in submitReport\n");
					remoteEx.printStackTrace();
				}
			}
			else
			{
				quotChar = "\'";
				if (jobName.indexOf(quotChar) >= 0)
				{
					quotChar = "\"";
				}

				// check to see if the selected report is a query.
				try
				{
					repPth =
						connect.getCMService().query(
							new SearchPathMultipleObject("/content//query[@name="
								+ quotChar
								+ report
								+ quotChar
								+ "]"),
							props,
							new Sort[] {},
							new QueryOptions());
				}
				catch (java.rmi.RemoteException remoteEx)
				{
					System.out.println("Caught Remote Exception:\n");
					remoteEx.printStackTrace();
				}

				String reportName = repPth[0].getSearchPath().getValue();
				System.out.println("Submitting query: " + reportName);
				try
				{
					createJob(
						connect,
						jobPath,
						jobName,
						new String[] { reportName });
					// submit the job for execution

					String reportEventID = "-1";
					if (submitAtFlag)
					{
						reportEventID =
							connect.getEventMgmtService().runAt(
								startTime,
								new SearchPathSingleObject(jobPath),
								reportParameters,
								runOpts);
					}
					else
					{
						asynchReply =
							connect.getJobService().run(
								new SearchPathSingleObject(jobPath),
								reportParameters,
								runOpts);

						// If response is not immediately complete, call wait until complete
						if (!asynchReply.getStatus().equals(AsynchReplyStatusEnum.complete))
						{
							while (!asynchReply.getStatus().equals(AsynchReplyStatusEnum.complete))
							{
								//before calling wait, double check that it is okay
								if (hasSecondaryRequest(asynchReply, "wait"))
								{
									asynchReply =
									connect.getJobService().wait(
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

						for (i = 0; i < asynchReply.getDetails().length; i++)
						{
							if (asynchReply.getDetails()[i] instanceof AsynchDetailEventID)

							{
								reportEventID = ((AsynchDetailEventID)asynchReply.getDetails()[i]).getEventID();
							}
						}
					}

					BaseClass bcQuery[];
					PropEnum propsQ[] = new PropEnum[2];
					propsQ[0] = PropEnum.searchPath;
					propsQ[1] = PropEnum.defaultName;
					bcQuery =
						connect.getCMService().query(
							new SearchPathMultipleObject(jobPath),
							propsQ,
							new Sort[] {},
							new QueryOptions());

					if ( bcQuery == null || bcQuery.length == 0 )
					{
						output = "The new job was not created in the Content Store";
						return output;
					}
					//  show the eventID returned for the submitted report
					output =
						"The eventID for this job is: " + reportEventID + ".\n";

				}
				catch (java.rmi.RemoteException remoteEx)
				{
					output =
						output.concat("Error occurred in submitReport()\n");
					remoteEx.printStackTrace();
				}
			}
		}
		else
		{
			output =
				output.concat("Invalid parameter(s) passed to submitReport.\n");
		}
		return output;
	}

	/**
	 * Create a new job.
	 *
	 * @param connect	Connection to Server
	 * @param jobPath   Search path for the new job.
	 * @param jobName   Name of the new job.
	 * @param reports   Search paths for the reports to run during this job.
	 */

	public void createJob(
		CRNConnect connect,
		String jobPath,
		String jobName,
		String[] reports)
	{
		JobDefinition myJob = new JobDefinition();
		RunOptionBoolean saveOutput = new RunOptionBoolean();
		TokenProp jobNameProp = new TokenProp();
		OptionArrayProp jobRunOptions = new OptionArrayProp();
		BaseClass jobsToAdd[] = new BaseClass[1];
		BaseClass bcJob[] = new BaseClass[1];
		Option runOptions[] = new Option[1];
		BaseClass parents[] = new BaseClass[] {};
		String sPath;
		PropEnum requestedProperties[] = new PropEnum[3];
		AddOptions addOpts = new AddOptions();
		NmtokenProp stepSequenceType = new NmtokenProp();

		saveOutput.setName(RunOptionEnum.saveOutput);
		saveOutput.setValue(true);
		runOptions[0] = saveOutput;

		jobRunOptions.setValue(runOptions);
		myJob.setOptions(jobRunOptions);

		jobNameProp.setValue(jobName);
		myJob.setDefaultName(jobNameProp);

		//Identify the parent object.
		sPath = "/content/folder[@name='Samples']/folder[@name='Models']/package[@name='GO Sales (query)']";

		requestedProperties[0] = PropEnum.searchPath;
		requestedProperties[1] = PropEnum.defaultName;
		requestedProperties[2] = PropEnum.parent;

		stepSequenceType.setValue("parallel");

		try
		{
			parents =
				connect.getCMService().query(
					new SearchPathMultipleObject(sPath),
					requestedProperties,
					new Sort[] {},
					new QueryOptions());
		}
		catch (java.rmi.RemoteException remoteEx)
		{
			System.out.println("Caught remote exception:\n");
			remoteEx.printStackTrace();
			return;
		}

		if (parents.length <= 0)
		{
			System.out.println(
				"Error: Unable to retrieve parent objects.\n"
					+ "Failed to create job in content store.");
			return;
		}

		myJob.setSequencing(stepSequenceType);
		myJob.setParent(parents[0].getParent());
		jobsToAdd[0] = myJob;

		addOpts.setUpdateAction(UpdateActionEnum.replace);
		try
		{
			bcJob[0] = connect.getCMService().add(new SearchPathSingleObject(sPath), jobsToAdd, addOpts)[0];
		}
		catch (java.rmi.RemoteException remoteEx)
		{
			System.out.println("Caught remote exception:\n");
			remoteEx.printStackTrace();
			return;
		}

		//now, add one jobStepDefinition for each report
		JobStepDefinition steps[] = new JobStepDefinition[reports.length];

		for (int i = 0; i < reports.length; i++)
		{
			BaseClassArrayProp bcap = new BaseClassArrayProp();
			steps[i] = new JobStepDefinition();
			StringProp searchPath = new StringProp();
			BaseClass temp = (BaseClass)new Report();

			searchPath.setValue(reports[i]);

			temp.setSearchPath(searchPath);
			BaseClass bca[] = new BaseClass[1];
			bca[0] = temp;
			bcap.setValue(bca);

			steps[i].setStepObject(bcap);

			//now, add each definition to the job
			addHelper(connect, steps[i], jobPath);
		}
	}

	/**
	 * Add an object to the Content Store.
	 *
	 * @param 	connect	Connection to Server
	 * @param   bc  	An object that extends baseClass, such as a Report.
	 * @param 	path 	Search path for the job
	 *
	 * @return      	The new object.
	 */
	public BaseClass[] addHelper(
		CRNConnect connect,
		JobStepDefinition bc,
		String path)
	{
		AddOptions addOpts = new AddOptions();
		BaseClass jobSteps[] = new BaseClass[1];
		addOpts.setUpdateAction(UpdateActionEnum.replace);
		jobSteps[0] = bc;
		BaseClass newbc[] = new BaseClass[1];
		try
		{
			newbc[0] = connect.getCMService().add(new SearchPathSingleObject(path), jobSteps, addOpts)[0];
		}
		catch (java.rmi.RemoteException remoteEx)
		{
			System.out.println("Caught remote exception:\n");
			remoteEx.printStackTrace();
		}
		return newbc;
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
