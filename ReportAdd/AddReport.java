/** 
Licensed Materials - Property of IBM

IBM Cognos Products: DOCS

(C) Copyright IBM Corp. 2005, 2008

US Government Users Restricted Rights - Use, duplication or disclosure restricted by GSA ADP Schedule Contract with
IBM Corp.
*/
/**
 * ReportAdd.java
 *
 * Copyright (C) 2008 Cognos ULC, an IBM Company. All rights reserved.
 * Cognos (R) is a trademark of Cognos ULC, (formerly Cognos Incorporated).
 *
 * Description: This code sample demonstrates how to add reports using the
 *              following methods:
 *              - add(parentPath, object, options)
 *                Use this method to add reports to the content store.
 */

import java.io.*;
import java.util.List;
import org.dom4j.*;
import org.dom4j.io.*;

import com.cognos.developer.schemas.bibus._3.Account;
import com.cognos.developer.schemas.bibus._3.AddOptions;
import com.cognos.developer.schemas.bibus._3.AnyTypeProp;
import com.cognos.developer.schemas.bibus._3.AsynchDetailReportValidation;
import com.cognos.developer.schemas.bibus._3.AsynchReply;
import com.cognos.developer.schemas.bibus._3.AsynchReplyStatusEnum;
import com.cognos.developer.schemas.bibus._3.AsynchSecondaryRequest;
import com.cognos.developer.schemas.bibus._3.BaseClass;
import com.cognos.developer.schemas.bibus._3.Locale;
import com.cognos.developer.schemas.bibus._3.MultilingualToken;
import com.cognos.developer.schemas.bibus._3.MultilingualTokenProp;
import com.cognos.developer.schemas.bibus._3.Option;
import com.cognos.developer.schemas.bibus._3.ParameterValue;
import com.cognos.developer.schemas.bibus._3.Report;
import com.cognos.developer.schemas.bibus._3.ReportServiceSpecification;
import com.cognos.developer.schemas.bibus._3.SearchPathSingleObject;
import com.cognos.developer.schemas.bibus._3.UpdateActionEnum;
import com.cognos.developer.schemas.bibus._3.XmlEncodedXML;

public class AddReport {

	public BaseClass[] namespaceInfo;
	public BaseClass[] groupOrRoleInfo;
	public BaseClass[] memberInfo;
	public String[] namespaceSearchPath;
	public String[] groupOrRoleSearchPath;
	public String[] memberSearchPath;
	public String[] namespaceDefaultName;
	public String[] groupOrRoleDefaultName;
	public String[] memberDefaultName;
	public int selectedNamespace = 0;
	public int selectedGroupOrRole = 0;
	public int selectedMember = 0;

	private static boolean specOkay = false;
	CSHandlers csHandler = new CSHandlers();

	/**
	 * Validate a report/query specification
	 *
	 * @param    connection   Connection to server
	 * @param    reportSpec   A string containing the report specification
	 *
	 * @return   A string indicating the results of the operation
	 *
	 */
	public String validateReportSpec(CRNConnect connection, ReportServiceSpecification reportSpec)
	//throws java.rmi.RemoteException
	{
		AsynchReply asynchReply = null;
		XmlEncodedXML ValidationDefects = null;
		specOkay = true;

		try
		{
			// sn_dg_sdk_method_reportService_validateSpecification_start_1
			asynchReply =
				connection.getReportService().validateSpecification(
					reportSpec,
					new ParameterValue[] {},
					new Option[] {});
			// sn_dg_sdk_method_reportService_validateSpecification_end_1

			// If response is not immediately complete, call wait until complete
			if (!asynchReply.getStatus().equals(AsynchReplyStatusEnum.conversationComplete))
			{
				while (!asynchReply.getStatus().equals(AsynchReplyStatusEnum.conversationComplete))
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

			// sn_dg_sdk_method_reportService_validateSpecification_start_2
			for (int i = 0; i < asynchReply.getDetails().length; i++)
			{
				if (asynchReply.getDetails()[i] instanceof AsynchDetailReportValidation)

				{
					ValidationDefects = ((AsynchDetailReportValidation)asynchReply.getDetails()[i]).getDefects();
				}
			}
			// sn_dg_sdk_method_reportService_validateSpecification_end_2
		}
		catch (java.rmi.RemoteException remoteEx)
		{
			//remoteEx.printStackTrace();
			return "Exception caught during validation:\n" + remoteEx;
		}

		if (asynchReply.equals(null))
		{
			specOkay = false;
			return "Validation Failed";
		}

		Document oDocument;
		List defectsList = null;
		List layoutProblemsList = null;
		List queryProblemsList = null;

		Node defectsNode;
		Node queryProblemsNode;
		Node layoutProblemsNode;

		if (ValidationDefects == null){
			return "Report Specification validates successfully";
		}
		
		String defects = ValidationDefects.toString();

		try
		{
			SAXReader xmlReader = new SAXReader();
			ByteArrayInputStream bais =
				  new ByteArrayInputStream(defects.getBytes("UTF-8"));
			oDocument = xmlReader.read(bais);

			defectsList = oDocument.selectNodes("//defects");
			queryProblemsList = oDocument.selectNodes("//queryProblems");
			layoutProblemsList = oDocument.selectNodes("//layoutProblems");


			String errOutput = "";
			for (int i = 0; i < defectsList.size(); i++)
			{
				defectsNode = (Node) defectsList.get(i);
				if (defectsNode.hasContent())
				{
					//For now, this may be a false error. Need to check
					//the queryProblems and layoutProblems to see if there
					//actually is one. Trakker #432753
					//specOkay = false;
					//errOutput += "defects hasContent() returns true.\n";
				}
			}
			for (int i = 0; i < queryProblemsList.size(); i++)
			{
				queryProblemsNode = (Node) queryProblemsList.get(i);
				if (queryProblemsNode.hasContent())
				{
					errOutput += "\nThe following queryProblems were found:\n";
					List queryProblems = queryProblemsNode.selectNodes("//message[@type='expression']/@title");
					for (int j = 0; j < queryProblems.size(); j++)
					{
						if (queryProblems.get(j) != null)
						{
							errOutput += "\t" + ((Node) queryProblems.get(j)).getStringValue() + "\n";
						}
					}
					specOkay = false;
				}
			}
			for (int i = 0; i < layoutProblemsList.size(); i++)
			{
				layoutProblemsNode = (Node) layoutProblemsList.get(i);
				if (layoutProblemsNode.hasContent())
				{
					errOutput += "\nThe following layoutProblems were found:\n";
					List layoutProblems = layoutProblemsNode.selectNodes("//message[@type='layout']/@title");
					for (int j = 0; j < layoutProblems.size(); j++)
					{
						if (layoutProblems.get(j) != null)
						{
							errOutput += "\t" + ((Node) layoutProblems.get(j)).getStringValue() + "\n";
						}
					}
					specOkay = false;
				}
			}

			if (specOkay)
			{
				return "Report Specification validates successfully";
			}
			else
			{
				return "Report Specification failed to Validate!\n" + errOutput;
			}
		}
		catch (DocumentException docEx)
		{
			return "Exception caught parsing validation response:\n" + docEx.getMessage();
		}
		catch (UnsupportedEncodingException unsuppEncEx)
		{
			return "Exception caught preparing to parse validation response:\n" + unsuppEncEx.getMessage();
		}

		//return validationResults.getDefects();
	}

        /**
	 * Add a Report to the Content Store
	 *
	 * @param    connection   Connection to server.
	 * @param    reportSpec   String containing the report specification in xml format
	 * @return           A string containing successful status information.
	 *
	 */
	public String addSpecToCM(CRNConnect connection, ReportServiceSpecification reportSpec, String reportName)
	throws java.rmi.RemoteException
	{
		String validateOutput = validateReportSpec(connection, reportSpec);
		if (!specOkay)
		{
			return "Add not performed.\n\n" + validateOutput;
		}

		Report newReport = new Report();

		AnyTypeProp reportSpecProperty = new AnyTypeProp();
		reportSpecProperty.setValue(reportSpec.getValue().toString());

		MultilingualToken[] reportNames = new MultilingualToken[1];
		reportNames[0] = new MultilingualToken();
		reportNames[0].setValue(reportName);

		Locale[] locales = csHandler.getConfiguration(connection);
		if (locales == null)
		{
			locales[0] = new Locale();
			locales[0].setLocale("en");
		}
		reportNames[0].setLocale(locales[0].getLocale());
		reportNames[0].setValue(reportName);


		newReport.setName(new MultilingualTokenProp());
		newReport.getName().setValue(reportNames);
		newReport.setSpecification(reportSpecProperty);

		AddOptions addReportOptions = new AddOptions();
		addReportOptions.setUpdateAction(UpdateActionEnum.replace);

		Account currentAccount = Logon.getLogonAccount(connection);
		String searchPath = currentAccount.getSearchPath().getValue() + "/folder[@name=\"My Folders\"]";

		try
		{
			connection.getReportService().add(new SearchPathSingleObject(searchPath), newReport, addReportOptions);
		}
		catch(java.rmi.RemoteException remoteEx)
		{
			return "Exception caught while adding report " + reportName + " to the content store.";
		}

		return "Report " + reportName + " saved in " + searchPath + " in the content store.";

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
