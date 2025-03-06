/** 
Licensed Materials - Property of IBM

IBM Cognos Products: DOCS

(C) Copyright IBM Corp. 2005, 2010

US Government Users Restricted Rights - Use, duplication or disclosure restricted by GSA ADP Schedule Contract with
IBM Corp.
*/
/**
 * Render.java
 *
 * Copyright (C) 2008 Cognos ULC, an IBM Company. All rights reserved.
 * Cognos (R) is a trademark of Cognos ULC, (formerly Cognos Incorporated).
 *
 * Description: This code sample demonstrates how to run a report and send
 * the output to a specific user using the following methods:
 *    - run
 *      Use this method to run a report, query, or report view.
 *    - wait
 *      Use this method to notify the server that the issuer of the request
 *      is still waiting for the output, and to request that the processing
 *      be continued.
 *    - email
 *      Use this method to request that the output of a report be emailed
 *      to a user.
 *    - query
 *      Use this method to request objects from Content Manager.
 *    - release
 *      Use this method to remove inactive requests from the report service
 *      cache earlier than they would be removed automatically by the system.
 *      Removing abandoned requests makes resources available for
 *      other requests, improving performance.
 */

import java.io.File;
import java.io.FileOutputStream;

import com.cognos.developer.schemas.bibus._3.AsynchDetailReportOutput;
import com.cognos.developer.schemas.bibus._3.AsynchReply;
import com.cognos.developer.schemas.bibus._3.AsynchReplyStatusEnum;
import com.cognos.developer.schemas.bibus._3.BaseParameter;
import com.cognos.developer.schemas.bibus._3.Option;
import com.cognos.developer.schemas.bibus._3.ParameterValue;
import com.cognos.developer.schemas.bibus._3.RunOptionBoolean;
import com.cognos.developer.schemas.bibus._3.RunOptionEnum;
import com.cognos.developer.schemas.bibus._3.RunOptionStringArray;
import com.cognos.developer.schemas.bibus._3.SearchPathSingleObject;

public class Render
{

	private static final int REP_HTML = 0;
	private static final int REP_XML = 1;
	private static final int REP_PDF = 2;
	private static final int REP_CSV = 3;
	private static final int REP_HTML_FRAG = 4;
	private static final int REP_MHT = 5;
	private static final int REP_SINGLEXLS = 6;
	private static final int REP_XLS = 7;
	private static final int REP_XLWA = 8;

	/**
	 * @param connect
	 *        Specifies the object that provides the connection to
	 *        the server.
	 * @param reportPath
	 *        Specifies the search path of the report.
	 * @param response
	 *        Specifies the primary response. If no primary response is passed
	 *        to this method, this method calls the run method.
	 *        A primary response is necessary to issue a secondary request, such
	 *        as an render, because a secondary request can only continue
	 *        a conversation established by a primary request.
	*/
	public String renderReport(
		CRNConnect connect,
		BaseClassWrapper report,
		AsynchReply response)
	{

		String output = "";

		try
		{
			// Get the list of parameters used by the report, including
			// optional parameters.
			ParameterValue reportParameters[] = new ParameterValue[] {};
			ReportParameters repParms = new ReportParameters();
			BaseParameter[] prm = repParms.getReportParameters(report, connect);

			if (prm != null && prm.length > 0)
			{
				reportParameters = ReportParameters.setReportParameters(prm);
			}

			SearchPathSingleObject reportSearchPath =
				new SearchPathSingleObject();
			reportSearchPath.set_value(
				report.getBaseClassObject().getSearchPath().getValue());

			// Set the run options for the execute method.
			Option[] runOptions = new Option[3];

			if (response == null)
			{
				//Execute the report, specify HTML output format
				//set the continueConversation option, to allow
				//subsequent requests
				runOptions[0] = setFormat(REP_HTML);
				runOptions[1] = setContinueConversation();
                runOptions[2] = setPrompting();

				response =
					connect.getReportService().run(
						reportSearchPath,
						reportParameters,
						runOptions);

			}

			output = renderResponse(connect, response);

			// sn_dg_sdk_method_reportService_release_start_1

			// release the conversation to free resources.
			connect.getReportService().release(response.getPrimaryRequest());

			// sn_dg_sdk_method_reportService_release_end_1

			response = null;

			return output;

		}
		catch (java.rmi.RemoteException remoteEx)
		{
			remoteEx.printStackTrace();
			return ("An error occurred in renderReport().");
		}
	}

	private RunOptionStringArray setFormat(int format)
	{
		RunOptionStringArray rof = new RunOptionStringArray();
		rof.setName(RunOptionEnum.outputFormat);
		rof.setValue(this.getReportFormat(format));
		return rof;
	}

	private String[] getReportFormat(int reportType)
	{
		switch (reportType)
		{
			case REP_HTML :
				return new String[] { "HTML" };

			case REP_HTML_FRAG :
				return new String[] { "HTMLFragment" };

			case REP_MHT :
				return new String[] { "MHT" };

			case REP_SINGLEXLS :
				return new String[] { "SingleXLS" };

			case REP_XLS :
				return new String[] { "XLS" };

			case REP_XLWA :
				return new String[] { "XLWA" };

			case REP_XML :
				return new String[] { "XML" };

			case REP_PDF :
				return new String[] { "PDF" };

			case REP_CSV :
				return new String[] { "CSV" };

			default :
				System.out.println("Invalid report output format.");
				return null;
		}
	}

	private RunOptionBoolean setContinueConversation()
	{
		RunOptionBoolean continueConversation = new RunOptionBoolean();

		continueConversation.setName(RunOptionEnum.continueConversation);
		continueConversation.setValue(true);

		return continueConversation;
	}

    private RunOptionBoolean setPrompting()
    {
        RunOptionBoolean promptFlag = new RunOptionBoolean();
        
        promptFlag.setName(RunOptionEnum.prompt);
        promptFlag.setValue(false);

        return promptFlag;
    }
    
	private String renderResponse(CRNConnect connect, AsynchReply response)
	{
		Option renderOpts[] = new Option[1];
		RunOptionStringArray roFormat = setFormat(REP_XML);
		renderOpts[0] = roFormat;
		AsynchReply renderedResp = null;
		try
		{

			if (RunReport.hasSecondaryRequest(response, "render"))
			{
				// sn_dg_sdk_method_reportService_render_start_1
				renderedResp =
					connect.getReportService().render(
						response.getPrimaryRequest(),
						new ParameterValue[] {},
						renderOpts);
				// sn_dg_sdk_method_reportService_render_end_1
			}
			else
			{
				return "Response status is not valid for sending a render request.\n";
			}

			if (!renderedResp
				.getStatus()
				.equals(AsynchReplyStatusEnum.complete))
			{
				while (!renderedResp
					.getStatus()
					.equals(AsynchReplyStatusEnum.complete))
				{
					renderedResp =
						connect.getReportService().wait(
							renderedResp.getPrimaryRequest(),
							new ParameterValue[] {},
							new Option[] {});
				}

				if (RunReport.hasSecondaryRequest(renderedResp, "getOutput"))
				{

				renderedResp =
					connect.getReportService().getOutput(
						renderedResp.getPrimaryRequest(),
						new ParameterValue[] {},
						new Option[] {});
				}
			}
		}
		catch (java.rmi.RemoteException remoteEx)
		{
			remoteEx.printStackTrace();
			return ("Caught Remote Exception:\n");
		}

		AsynchDetailReportOutput reportOutput = null;
		for (int i = 0; i < renderedResp.getDetails().length; i++)
		{
			if (renderedResp.getDetails()[i]
				instanceof AsynchDetailReportOutput)
			{
				reportOutput =
					(AsynchDetailReportOutput)renderedResp.getDetails()[i];
				break;
			}
		}

		if (reportOutput == null)
		{
			return "Server failed to return a valid report in this format.";
		}

		try
		{
			File oFile =
				new File(
					connect.getDefaultSavePath()
						+ System.getProperty("file.separator")
						+ "cmOut.xml");
			FileOutputStream fos = new FileOutputStream(oFile);

			fos.write(reportOutput.getOutputPages()[0].getBytes());
			fos.flush();
			fos.close();
			return ("Report Output written to file " + oFile + ".\n");
		}
		catch (java.io.FileNotFoundException fileNotFoundEx)
		{
			fileNotFoundEx.printStackTrace();
			return ("Unable to open/create file to save report output.\n");
		}
		catch (java.io.IOException ioEx)
		{
			ioEx.printStackTrace();
			return ("Caught IO Exception:\n" + ioEx.toString() + "\n");
		}

	}

}
