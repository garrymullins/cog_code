/** 
Licensed Materials - Property of IBM

IBM Cognos Products: DOCS

(C) Copyright IBM Corp. 2005, 2008

US Government Users Restricted Rights - Use, duplication or disclosure restricted by GSA ADP Schedule Contract with
IBM Corp.
*/
/**
 * Cancel.java
 *
 *
 * Copyright (C) 2008 Cognos ULC, an IBM Company. All rights reserved.
 * Cognos (R) is a trademark of Cognos ULC, (formerly Cognos Incorporated).
 *
 * Description: This code sample demonstrates how to run different types of reports using the following
 *	           methods:
 *	           - run
 *	             Use this method to run a report, query, or report view.
 *	           - cancel
 *	             Use this method to cancel a Content Manager request
 *		   	   - getOutput
 *		     	 Use this method to request that the output be sent to the issuer
 *		     	 of the request.
 *	           - query
 *	             Use this method to request objects from Content Manager.
 *	           - wait
 *	             Use this method to notify the server that the issuer of the request is still
 *	             waiting for the output, and to request that the processing be continued.
 */

import com.cognos.developer.schemas.bibus._3.AsynchOptionEnum;
import com.cognos.developer.schemas.bibus._3.AsynchOptionInt;
import com.cognos.developer.schemas.bibus._3.AsynchReply;
import com.cognos.developer.schemas.bibus._3.AsynchReplyStatusEnum;
import com.cognos.developer.schemas.bibus._3.BaseParameter;
import com.cognos.developer.schemas.bibus._3.Option;
import com.cognos.developer.schemas.bibus._3.ParameterValue;
import com.cognos.developer.schemas.bibus._3.RunOptionBoolean;
import com.cognos.developer.schemas.bibus._3.RunOptionEnum;
import com.cognos.developer.schemas.bibus._3.RunOptionStringArray;
import com.cognos.developer.schemas.bibus._3.SearchPathSingleObject;

public class Cancel
{
	/**
		* runAndCancelReport will execute a report or query in the content store
		* and then cancel the request
		*
		* @param connect
		*	     Specifies the Connection to Server.
		* @param report
		*		 Specifies the report or query.
		* @return
		*		 Returns the text message indicating whether the report or query ran successfully.
		*/
	public String runAndCancelReport(
		CRNConnect connect,
		BaseClassWrapper report)
	{
		String output = new String();
		if ((connect != null)
			&& (report != null)
			&& (connect.getDefaultSavePath() != null))
		{
			ParameterValue reportParameters[] = new ParameterValue[] {};
			ReportParameters repParms = new ReportParameters();
			BaseParameter[] prm = null;

			try
			{
				prm = repParms.getReportParameters(report, connect);
			}
			catch (java.rmi.RemoteException remoteEx)
			{
				System.out.println("Caught Remote Exception:\n");
				remoteEx.printStackTrace();
			}

			if (prm != null && prm.length > 0)
			{
				reportParameters = ReportParameters.setReportParameters(prm);
			}

			if ((reportParameters == null) || (reportParameters.length <= 0))
			{
				reportParameters = new ParameterValue[] {};
			}

			output += execAndCancel(report, connect, reportParameters);
		}
		else
		{
			output += "Invalid parameter(s) passed to runReport."
				+ System.getProperty("line.separator");
		}
		return output;
	}

	/**
	* This Java method executes and then cancels the specified report and returns a String
	* indicating whether the request was cancelled successfully.
	* @param report
	*		 Specifies the report.
	* @param connect
	*	     Specifies the Connection to Server.
	* @param pv
	* 		 Specifies the parameter values, if any, to use for the report
	* @return
	*		 Returns the text message indicating whether the report ran successfully.
	*/
	public String execAndCancel(
		BaseClassWrapper report,
		CRNConnect connect,
		ParameterValue pv[])
	{
		Option execReportRunOptions[] = new Option[5];

		RunOptionBoolean saveOutputRunOption = new RunOptionBoolean();
		RunOptionStringArray outputFormat = new RunOptionStringArray();
		RunOptionBoolean promptFlag = new RunOptionBoolean();
		AsynchOptionInt primaryWaitThreshold = new AsynchOptionInt();
		AsynchOptionInt secondaryWaitThreshold = new AsynchOptionInt();
		AsynchReply rsr = null;

		// We do not want to save this output
		saveOutputRunOption.setName(RunOptionEnum.saveOutput);
		saveOutputRunOption.setValue(false);

		//Output format is not important, but we have to pick one
		String[] reportFormat = new String[] { "HTML" };
		outputFormat.setName(RunOptionEnum.outputFormat);
		outputFormat.setValue(reportFormat);

		//Set the report not to prompt, as we pass the parameter if any
		promptFlag.setName(RunOptionEnum.prompt);
		promptFlag.setValue(false);

		//To ensure the wait loop, set the wait thresholds unreasonably short
		primaryWaitThreshold.setName(AsynchOptionEnum.primaryWaitThreshold);
		primaryWaitThreshold.setValue(1);
		secondaryWaitThreshold.setName(AsynchOptionEnum.secondaryWaitThreshold);
		secondaryWaitThreshold.setValue(1);

		// Fill the array with the run options.
		execReportRunOptions[0] = saveOutputRunOption;
		execReportRunOptions[1] = outputFormat;
		execReportRunOptions[2] = promptFlag;
		execReportRunOptions[3] = primaryWaitThreshold;
		execReportRunOptions[4] = secondaryWaitThreshold;

		SearchPathSingleObject reportSearchPath = new SearchPathSingleObject();
		reportSearchPath.set_value(
			report.getBaseClassObject().getSearchPath().getValue());

		try
		{
			rsr =
				connect.getReportService().run(
					reportSearchPath,
					pv,
					execReportRunOptions);

			// cancel() is only available when status is working or stillWorking
			if (!rsr.getStatus().equals(AsynchReplyStatusEnum.complete))
			{
				//We should be able to call cancel but we should check before calling it
				if (!RunReport.hasSecondaryRequest(rsr, "cancel"))
				{
					return "The conversation didn't lead to a valid state to call cancel()";
				}
				// sn_dg_sdk_method_reportService_cancel_start_1
				connect.getReportService().cancel(rsr.getPrimaryRequest());
				// sn_dg_sdk_method_reportService_cancel_end_1
				return "Call to cancel() succeeded.";
			}
			System.out.println("Secondary requests at complete:");
			for (int i = 0; i < rsr.getSecondaryRequests().length; i++)
			{
				System.out.println(rsr.getSecondaryRequests()[i].getName());
			}
			return "The conversation didn't lead to a valid state to call cancel().";

		}
		catch (java.rmi.RemoteException remoteEx)
		{
			return ("Caught Remote Exception:\n" + remoteEx.toString());
		}

	}
}
