/** 
Licensed Materials - Property of IBM

IBM Cognos Products: DOCS

(C) Copyright IBM Corp. 2005, 2008

US Government Users Restricted Rights - Use, duplication or disclosure restricted by GSA ADP Schedule Contract with
IBM Corp.
*/
/**
 * Email.java
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
 *    - deliver
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

import com.cognos.developer.schemas.bibus._3.AddressSMTP;
import com.cognos.developer.schemas.bibus._3.AsynchReply;
import com.cognos.developer.schemas.bibus._3.AsynchReplyStatusEnum;
import com.cognos.developer.schemas.bibus._3.AsynchRequest;
import com.cognos.developer.schemas.bibus._3.AsynchSecondaryRequest;
import com.cognos.developer.schemas.bibus._3.BaseClass;
import com.cognos.developer.schemas.bibus._3.BaseParameter;
import com.cognos.developer.schemas.bibus._3.Contact;
import com.cognos.developer.schemas.bibus._3.DeliveryOptionAddressSMTPArray;
import com.cognos.developer.schemas.bibus._3.DeliveryOptionEnum;
import com.cognos.developer.schemas.bibus._3.DeliveryOptionMemoPart;
import com.cognos.developer.schemas.bibus._3.DeliveryOptionString;
import com.cognos.developer.schemas.bibus._3.MemoPartString;
import com.cognos.developer.schemas.bibus._3.Option;
import com.cognos.developer.schemas.bibus._3.ParameterValue;
import com.cognos.developer.schemas.bibus._3.PropEnum;
import com.cognos.developer.schemas.bibus._3.QueryOptions;
import com.cognos.developer.schemas.bibus._3.RunOptionBoolean;
import com.cognos.developer.schemas.bibus._3.RunOptionEnum;
import com.cognos.developer.schemas.bibus._3.RunOptionStringArray;
import com.cognos.developer.schemas.bibus._3.SearchPathSingleObject;
import com.cognos.developer.schemas.bibus._3.SearchPathMultipleObject;
import com.cognos.developer.schemas.bibus._3.SmtpContentDispositionEnum;
import com.cognos.developer.schemas.bibus._3.Sort;

public class Email
{

	private static final int REP_HTML = 0;
	private static final int REP_XML = 1;
	private static final int REP_PDF = 2;
	private static final int REP_CSV = 3;

	/**
	 * @param connection
	 * 		  Connection to Server
	 * @param report
	 *        Specifies the report.
	 * @param bodyText
	 *        Specifies the text for the body of the email message.
	 * @param emailSubject
	 *        Specifies the subject of the email message.
	 * @param emailFormat
	 *        Specifies the format of the email message.
	 * @param emails
	 *        An array of email addresses.
	 * @param response
	 *        Specifies the primary request. If no primary request is passed
	 *        to this method, this method calls the run method.
	 *        A primary request is necessary to issue a secondary request, such
	 *        as an email request, because a secondary request can only continue
	 *        a conversation established by a primary request.
	*/
	public String emailReport(
		CRNConnect connection,
		BaseClassWrapper report,
		String bodyText,
		String emailSubject,
		int emailFormat,
		AddressSMTP[] emails,
		AsynchRequest response)
	{
		AsynchReply asynchReply = null;
		String reportPath = report.getBaseClassObject().getSearchPath().getValue();

		if (report == null)
		{
			return "No valid report selected";
		}

		try
		{
			// Get the list of parameters used by the report, including
			// optional parameters.
			ParameterValue reportParameters[] = new ParameterValue[] {};
			ReportParameters repParms = new ReportParameters();
			BaseParameter[] prm = repParms.getReportParameters(report, connection);

			if (prm != null && prm.length > 0)
			{
				reportParameters =
					ReportParameters.setReportParameters(prm);
			}

			// Set the run options for the execute method.
			Option[] execRunOptions = new Option[2];
			Option[] emailRunOptions = new Option[6];

			if (response == null)
			{
				//Execute the report, specify output format
				//set the continueConversation option, to allow
				//subsequent requests
				execRunOptions[0] = setEmailFormat(emailFormat);
				execRunOptions[1] = setNoPrompt();

				asynchReply =
					connection.getReportService().run(new SearchPathSingleObject(reportPath), reportParameters, execRunOptions);

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

				response = asynchReply.getPrimaryRequest();
			}

			if (response != null)
			{
				//Set the required fields for generating the email output
				emailRunOptions[0] = setDeliveryMethodEmail();
				emailRunOptions[1] = setEmailAttach();
				emailRunOptions[2] = setEmailSubject(emailSubject);

				// If no email addresses are specified, send the email
				// message to all contacts.
				if (emails != null && emails.length > 0)
				{
					emailRunOptions[3] = setEmailAddresses(emails);
				}
				else
				{
					emailRunOptions[3] = getContactEmails(connection);
				}
				emailRunOptions[4] = setEmailBody(bodyText);
				emailRunOptions[5] = setContinueConversation();

				//call email
				// sn_dg_sdk_method_reportService_deliver_start_1
				asynchReply = connection.getReportService().deliver(response, new ParameterValue[] {}, emailRunOptions);
				// sn_dg_sdk_method_reportService_deliver_end_1

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
					"Response null, unable to issue secondary request.");
			}

			System.out.println("The email Java method completed successfully.");
			return "Email method complete";

		}
		catch (Exception e)
		{
			System.out.println("An error occurred in the email Java method.");
			e.printStackTrace();
			return "An error occurred in the email Java method.";
		}

	}

	public DeliveryOptionAddressSMTPArray setEmailAddresses(AddressSMTP[] emails)
	{
		//The emails can also be taken from CM as group, user, etc.(see Email class)
		DeliveryOptionAddressSMTPArray emailAddress = new DeliveryOptionAddressSMTPArray();

		emailAddress.setName(DeliveryOptionEnum.toAddress);
		emailAddress.setValue(emails);

		return emailAddress;
	}

	public DeliveryOptionMemoPart setEmailBody(String text)
	{
		DeliveryOptionMemoPart bodyText = new DeliveryOptionMemoPart();
		MemoPartString memoText = new MemoPartString();

		memoText.setName("Body");
		memoText.setText(text);
		memoText.setContentDisposition(SmtpContentDispositionEnum.inline);

		bodyText.setName(DeliveryOptionEnum.memoPart);
		bodyText.setValue(memoText);
		return bodyText;
	}

	public DeliveryOptionString setEmailSubject(String myEmailSubject)
	{
		DeliveryOptionString subjectText = new DeliveryOptionString();
		subjectText.setName(DeliveryOptionEnum.subject);
		subjectText.setValue(myEmailSubject);
		return subjectText;
	}

	public RunOptionBoolean setEmailAttach()
	{
		RunOptionBoolean attach = new RunOptionBoolean();
		attach.setName(RunOptionEnum.emailAsAttachment);
		attach.setValue(true);
		return attach;
	}

	public RunOptionStringArray setEmailFormat(int emailFormat)
	{
		RunOptionStringArray rof = new RunOptionStringArray();
		rof.setName(RunOptionEnum.outputFormat);
		rof.setValue(this.getReportFormat(emailFormat));
		return rof;
	}

	public RunOptionBoolean setNoPrompt()
	{
		//Set the report not to prompt
		RunOptionBoolean promptFlag = new RunOptionBoolean();
		promptFlag.setName(RunOptionEnum.prompt);
		promptFlag.setValue(false);
		return promptFlag;
	}

	public String[] getReportFormat(int reportType)
	{
		switch (reportType)
		{
			case REP_HTML :
				return new String[] { "HTML" };

			case REP_XML :
				return new String[] { "XML" };

			case REP_PDF :
				return new String[] { "PDF" };

			case REP_CSV :
				return new String[] { "CSV" };

			default :
				System.out.println(
					"Invalid report output format."
						+ " Must be one of: HTML, XML, PDF, CSV.");
				return null;
		}
	}

	public RunOptionBoolean setDeliveryMethodEmail()
	{

		RunOptionBoolean sendEmail = new RunOptionBoolean();

		sendEmail.setName(RunOptionEnum.email);
		sendEmail.setValue(true);

		return sendEmail;
	}

	public RunOptionBoolean setContinueConversation()
	{
		RunOptionBoolean continueConversation = new RunOptionBoolean();

		continueConversation.setName(RunOptionEnum.continueConversation);
		continueConversation.setValue(true);

		return continueConversation;
	}

	public Contact[] getContacts(CRNConnect connection)
	{

		BaseClass baseClassArray[] = new BaseClass[] {};
		PropEnum props[] =
			new PropEnum[] {
				PropEnum.searchPath,
				PropEnum.defaultName,
				PropEnum.email };
		try
		{
			baseClassArray =
				connection.getCMService().query(
					new SearchPathMultipleObject("CAMID(\":\")/contact"),
					props,
					new Sort[] {},
					new QueryOptions());
		}
		catch (java.rmi.RemoteException remoteEx)
		{
			remoteEx.printStackTrace();
		}

		Contact contacts[] = new Contact[baseClassArray.length];
		for (int i = 0; i < baseClassArray.length; i++)
		{
			contacts[i] = (Contact)baseClassArray[i];
		}

		return contacts;
	}

	public DeliveryOptionAddressSMTPArray getContactEmails(CRNConnect connection)
	{
		Contact[] contacts = getContacts(connection);
		AddressSMTP[] emailAddress = new AddressSMTP[contacts.length];

		for (int i = 0; i < contacts.length; i++)
		{
			if (contacts[i].getEmail().getValue() != null)
			{
				emailAddress[i] = new AddressSMTP(contacts[i].getEmail().getValue());
			}
			else
			{
				emailAddress[i] = new AddressSMTP("");
			}
		}

		DeliveryOptionAddressSMTPArray emails = new DeliveryOptionAddressSMTPArray();
		emails.setValue(emailAddress);
		emails.setName(DeliveryOptionEnum.toAddress);

		return emails;
	}

	// sn_dg_sdk_task_hasSecondaryRequest_start_0
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
	// sn_dg_sdk_task_hasSecondaryRequest_end_0

}
