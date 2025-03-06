/** 
Licensed Materials - Property of IBM

IBM Cognos Products: DOCS

(C) Copyright IBM Corp. 2005, 2010

US Government Users Restricted Rights - Use, duplication or disclosure restricted by GSA ADP Schedule Contract with
IBM Corp.
*/
/**
 * CreateAgent.java
 *
 * Copyright (C) 2008 Cognos ULC, an IBM Company. All rights reserved.
 * Cognos (R) is a trademark of Cognos ULC, (formerly Cognos Incorporated).
 *
 * Description: This code sample demonstrates how to create and run an agent
 */

import java.rmi.RemoteException;
import com.cognos.developer.schemas.bibus._3.AddOptions;
import com.cognos.developer.schemas.bibus._3.AgentDefinition;
import com.cognos.developer.schemas.bibus._3.AgentOptionBoolean;
import com.cognos.developer.schemas.bibus._3.AgentOptionEnum;
import com.cognos.developer.schemas.bibus._3.AgentTaskDefinition;
import com.cognos.developer.schemas.bibus._3.AnyTypeProp;
import com.cognos.developer.schemas.bibus._3.AsynchReply;
import com.cognos.developer.schemas.bibus._3.AsynchReplyStatusEnum;
import com.cognos.developer.schemas.bibus._3.BaseClass;
import com.cognos.developer.schemas.bibus._3.BaseClassArrayProp;
import com.cognos.developer.schemas.bibus._3.BaseParameterAssignment;
import com.cognos.developer.schemas.bibus._3.BaseParameterAssignmentArrayProp;
import com.cognos.developer.schemas.bibus._3.DataEnum;
import com.cognos.developer.schemas.bibus._3.MetadataModelItemName;
import com.cognos.developer.schemas.bibus._3.Option;
import com.cognos.developer.schemas.bibus._3.OptionArrayProp;
import com.cognos.developer.schemas.bibus._3.ParameterAssignmentDataItem;
import com.cognos.developer.schemas.bibus._3.ParameterValue;
import com.cognos.developer.schemas.bibus._3.ParmValueItem;
import com.cognos.developer.schemas.bibus._3.PropEnum;
import com.cognos.developer.schemas.bibus._3.QueryOptions;
import com.cognos.developer.schemas.bibus._3.Report;
import com.cognos.developer.schemas.bibus._3.ReportServiceReportSpecification;
import com.cognos.developer.schemas.bibus._3.RunOptionBoolean;
import com.cognos.developer.schemas.bibus._3.RunOptionData;
import com.cognos.developer.schemas.bibus._3.RunOptionEnum;
import com.cognos.developer.schemas.bibus._3.RunOptionStringArray;
import com.cognos.developer.schemas.bibus._3.SearchPathMultipleObject;
import com.cognos.developer.schemas.bibus._3.SearchPathSingleObject;
import com.cognos.developer.schemas.bibus._3.SimpleParmValueItem;
import com.cognos.developer.schemas.bibus._3.Sort;
import com.cognos.developer.schemas.bibus._3.Specification;
import com.cognos.developer.schemas.bibus._3.StringProp;
import com.cognos.developer.schemas.bibus._3.TokenProp;
import com.cognos.developer.schemas.bibus._3.UpdateActionEnum;

public class CreateAgent {

	// This is the constructor.
	public CreateAgent()
	{
		super();
	}

	//This method creates a new agent
	public boolean addNewAgent(CRNConnect connection, String parentSearchPath, String anAgentName, String runReportName)
	{
		String reportSearchPath=new String("/content/folder[@name='Samples']/folder[@name='Models']/package[@name='GO Data Warehouse (query)']/folder[@name='SDK Report Samples']/report[@name='Product Quantity and Price']");
		String taskReportSearchPath=new String("/content/folder[@name='Samples']/folder[@name='Models']/package[@name='GO Data Warehouse (query)']/folder[@name='SDK Report Samples']/report[@name='Retailer Contact']");
		String targetPath="/folder[@name='My Folders']";

		BaseClass parent[];

		AddOptions myAddOptions = new AddOptions();
		myAddOptions.setUpdateAction(UpdateActionEnum.replace);

		//Create new agent definition object
		AgentDefinition myAgent = new AgentDefinition();
		TokenProp myToken = new TokenProp();
		myToken.setValue(anAgentName);
		myAgent.setDefaultName(myToken);

		try {
			parent = connection.getCMService().query(
						new SearchPathMultipleObject("~"),
						new PropEnum[]{PropEnum.searchPath}, new Sort[]{}, new QueryOptions());

			parentSearchPath = parent[0].getSearchPath().getValue() + targetPath;
			BaseClassArrayProp agentParent = new BaseClassArrayProp();
			agentParent.setValue(parent);
			myAgent.setParent(agentParent);
		} catch (RemoteException e1) {
			e1.printStackTrace();
			return false;
		}
		SearchPathMultipleObject searchPath = new SearchPathMultipleObject();
		searchPath.set_value(parentSearchPath);

		BaseClass[] bc = new BaseClass[] {myAgent};
		// Add Agent Definition to the content store
		 try {
			bc = connection.getCMService().add(new SearchPathSingleObject(parentSearchPath),bc,myAddOptions);
		} catch (RemoteException e) {
			e.printStackTrace();
			return false;
		}
		String agentDefinitionSearchPath = ((AgentDefinition)bc[0]).getSearchPath().getValue();
		String spec = "";
		 try {
			 BaseClass myBaseClass[] = connection.getCMService().query(
						new SearchPathMultipleObject (reportSearchPath),
						new PropEnum[]{PropEnum.metadataModel, PropEnum.metadataModelPackage, PropEnum.storeID}, new Sort[]{}, new QueryOptions());

			spec = "<report expressionLocale=\"en-us\" xmlns=\"http://developer.cognos.com/schemas/report/14.1/\"> <!--RS:14.1--> <modelPath>/content/folder[@name='Samples']/folder[@name='Models']/package[@name='GO Data Warehouse (query)']/model[@name='model']</modelPath> <queries> <query name=\"Query1\"> <source><model/></source> <selection><dataItem name=\"Reason description\" label=\"Reason description\" aggregate=\"none\"><expression>[Returned items (query)].[Return reason].[Reason description]</expression></dataItem><dataItem name=\"Retailer Name\" label=\"Retailer Name\" aggregate=\"none\"><expression>[Returned items (query)].[Retailers].[Retailer name]</expression></dataItem></selection> <detailFilters><detailFilter use=\"required\" postAutoAggregation=\"false\"><filterExpression>[Returned items (query)].[Returned items fact].[Return quantity] &gt; ?total_return_value? and [Returned items (query)].[Return reason].[Reason description] in ('Incompleteproduct','Unsatisfactoryproduct','Defective product') </filterExpression></detailFilter></detailFilters><queryHints><localCache value=\"false\"/></queryHints> </query> </queries> <layouts> <layout> <reportPages> <page name=\"Page1\"> <style> <defaultStyles> <defaultStyle refStyle=\"pg\"/> </defaultStyles> </style> <pageBody> <style> <defaultStyles> <defaultStyle refStyle=\"pb\"/> </defaultStyles> </style> <contents> <list refQuery=\"Query1\"> <style> <CSS value=\"border-collapse:collapse\"/> <defaultStyles> <defaultStyle refStyle=\"ls\"/> </defaultStyles> </style><listColumns> <listColumn> <listColumnTitle> <style> <defaultStyles> <defaultStyle refStyle=\"lt\"/> </defaultStyles> </style> <contents> <textItem> <dataSource> <dataItemLabel refDataItem=\"Reason description\"/> </dataSource> </textItem> </contents> </listColumnTitle> <listColumnBody> <style> <defaultStyles> <defaultStyle refStyle=\"lc\"/> </defaultStyles> </style> <contents> <textItem> <dataSource> <dataItemValue refDataItem=\"Reason description\"/> </dataSource> </textItem> </contents> </listColumnBody> </listColumn> <listColumn> <listColumnTitle> <style> <defaultStyles> <defaultStyle refStyle=\"lt\"/> </defaultStyles> </style> <contents> <textItem> <dataSource> <dataItemLabel refDataItem=\"Retailer Name\"/> </dataSource> </textItem> </contents> </listColumnTitle> <listColumnBody> <style> <defaultStyles> <defaultStyle refStyle=\"lc\"/> </defaultStyles> </style> <contents> <textItem> <dataSource> <dataItemValue refDataItem=\"Retailer Name\"/> </dataSource> </textItem> </contents> </listColumnBody> </listColumn> </listColumns> </list> </contents> </pageBody> </page> </reportPages> </layout> </layouts> <classStyles> <classStyle name=\"hideItem\" > <CSS value=\"display:none\" /> </classStyle> </classStyles> </report>";
			AnyTypeProp myProperty = new AnyTypeProp();
			myProperty.setValue(spec);
			((Report)myBaseClass[0]).setSpecification(myProperty);

			// add model, package and event specification information to Agent Definition
            connection.getCMService().add(new SearchPathSingleObject(agentDefinitionSearchPath),myBaseClass,myAddOptions);

			 // add searchPath to BaseClass object
			StringProp mySearchPath = new StringProp();
			mySearchPath.setValue(agentDefinitionSearchPath);
			myBaseClass[0].setSearchPath(mySearchPath);

			// get a 'Retailer Contact' report object from CM and set it as a task report
			BaseClass bcPromptReport[] = connection.getCMService().query( new SearchPathMultipleObject (taskReportSearchPath),new PropEnum[]{PropEnum.searchPath, PropEnum.defaultName,PropEnum.storeID}, new Sort[]{}, new QueryOptions());

			//assign the report execution to a new taskDefinition of the agent
			AgentTaskDefinition myAgentTaskDefinition = new AgentTaskDefinition();

			//set searchPath the taskObject to a storeID of the report
			BaseClassArrayProp taskObject = new BaseClassArrayProp();
			BaseClass bcObject[] = new BaseClass[1];
			StringProp myStringProp = new StringProp();

			String storeIDString = bcPromptReport[0].getStoreID().getValue().get_value();
			myStringProp.setValue("storeID(\"" + storeIDString + "\")");
			bcObject[0] = new Report();
			bcObject[0].setSearchPath(myStringProp);
			taskObject.setValue(bcObject);
			myAgentTaskDefinition.setTaskObject(taskObject);

			//set defaultName of agentTaskDefinition to defaultName of the report
			TokenProp myTokenProp = new TokenProp();
			myTokenProp.setValue(bcPromptReport[0].getDefaultName().getValue());
			myAgentTaskDefinition.setDefaultName(myTokenProp);

			//set options
			OptionArrayProp myOptionArrayProp = new OptionArrayProp();
			Option options[] = new Option[5];
			AgentOptionBoolean myAgentOptionBoolean = new AgentOptionBoolean();
			myAgentOptionBoolean.setName(AgentOptionEnum.fromString("availableAsEmailAttachment"));
			myAgentOptionBoolean.setValue(true);
			options[0] = myAgentOptionBoolean;

			RunOptionBoolean myRunOptionBoolean = new RunOptionBoolean();
			myRunOptionBoolean.setName(RunOptionEnum.saveOutput);
			myRunOptionBoolean.setValue(true);
			options[1] = myRunOptionBoolean;

			RunOptionBoolean myRunOption = new RunOptionBoolean();
			myRunOption.setName(RunOptionEnum.prompt);
			myRunOption.setValue(false);
			options[2] = myRunOption;

			RunOptionData myRunOptionData = new RunOptionData();
			myRunOptionData.setName(RunOptionEnum.data);
			myRunOptionData.setValue(DataEnum.runWithAllData);
			options[3] = myRunOptionData;

			RunOptionStringArray myRunOptionStringArray = new RunOptionStringArray();
			myRunOptionStringArray.setName(RunOptionEnum.outputFormat);
			myRunOptionStringArray.setValue( new String[] {"PDF"} );
			options[4] = myRunOptionStringArray;

			myOptionArrayProp.setValue(options);
			myAgentTaskDefinition.setOptions(myOptionArrayProp);

			// set parameter Assignment
			BaseParameterAssignmentArrayProp myBaseParameterAssignmentArrayProp = new BaseParameterAssignmentArrayProp();
			BaseParameterAssignment myBaseParameterAssignment[] = new BaseParameterAssignment[1];
			ParameterAssignmentDataItem myParameterAssignmentDataItem = new ParameterAssignmentDataItem();
			MetadataModelItemName mmin = new MetadataModelItemName();
			mmin.set_value("[Retailer Name]");
			myParameterAssignmentDataItem.setDataItemName(mmin);
			myParameterAssignmentDataItem.setParameterName("Parameter1");
			myBaseParameterAssignment[0] = myParameterAssignmentDataItem;
			myBaseParameterAssignmentArrayProp.setValue(myBaseParameterAssignment);

			myAgentTaskDefinition.setParameterAssignments(myBaseParameterAssignmentArrayProp);

			// set parameters
			ParameterValue[] myParameterValue = new ParameterValue[1];
			myParameterValue[0] = new ParameterValue();
			myParameterValue[0].setName("Parameter1");
			ParmValueItem[] myParmValueItem = new ParmValueItem[1];
			SimpleParmValueItem mySimpleParmValueItem = new SimpleParmValueItem();
			mySimpleParmValueItem.setInclusive(true);
			mySimpleParmValueItem.setDisplay("Retailer Name");
			mySimpleParmValueItem.setUse("Retailer Name");
			myParmValueItem[0] = mySimpleParmValueItem;
			myParameterValue[0].setValue( myParmValueItem );

			BaseClass bc1[] = new BaseClass[]{myAgentTaskDefinition};

			//add the agentTaskDefinition object to the agent object
			bc1 = connection.getCMService().add(new SearchPathSingleObject(bc[0].getSearchPath().getValue()),bc1,myAddOptions);
		} catch (RemoteException e) {
			e.printStackTrace();
			return false;
		}
        
		// Run the agent
		AsynchReply myAsynchReply = null;

		//set parameter value before running the agent
		ParameterValue[] myParameterValue = new ParameterValue[1];
		myParameterValue[0] = new ParameterValue();
		myParameterValue[0].setName("total_return_value");
		ParmValueItem[] myParmValueItem = new ParmValueItem[1];
		SimpleParmValueItem mySimpleParmValueItem = new SimpleParmValueItem();
		mySimpleParmValueItem.setInclusive(true);
		mySimpleParmValueItem.setDisplay("1500");
		mySimpleParmValueItem.setUse("1500");
		myParmValueItem[0] = mySimpleParmValueItem;
		myParameterValue[0].setValue(myParmValueItem);
		try {
		  ReportServiceReportSpecification rspec = new ReportServiceReportSpecification();
	      rspec.setValue( new Specification(spec));
		  myAsynchReply = connection.getMonitorService().run(new SearchPathSingleObject(agentDefinitionSearchPath), myParameterValue, new Option[]{});
		  if (!myAsynchReply.getStatus().equals(AsynchReplyStatusEnum.conversationComplete))
		  {
			while (!myAsynchReply.getStatus().equals(AsynchReplyStatusEnum.conversationComplete))
			{
				myAsynchReply =
                    connection.getMonitorService().wait(
						myAsynchReply.getPrimaryRequest(),
						new ParameterValue[] {},
						new Option[] {});
			}
		  }
		} catch (RemoteException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
}
