/** 
Licensed Materials - Property of IBM

IBM Cognos Products: DOCS

(C) Copyright IBM Corp. 2005, 2008

US Government Users Restricted Rights - Use, duplication or disclosure restricted by GSA ADP Schedule Contract with
IBM Corp.
*/
/**
 * Deployment.java
 *
 * Copyright (C) 2008 Cognos ULC, an IBM Company. All rights reserved.
 * Cognos (R) is a trademark of Cognos ULC, (formerly Cognos Incorporated).
 *
*/

import java.rmi.RemoteException;
import java.util.HashMap;

import com.cognos.developer.schemas.bibus._3.AddOptions;
import com.cognos.developer.schemas.bibus._3.AsynchReply;
import com.cognos.developer.schemas.bibus._3.BaseClass;
import com.cognos.developer.schemas.bibus._3.ConflictResolutionEnum;
import com.cognos.developer.schemas.bibus._3.DeploymentImportRule;
import com.cognos.developer.schemas.bibus._3.DeploymentObjectInformation;
import com.cognos.developer.schemas.bibus._3.DeploymentOption;
import com.cognos.developer.schemas.bibus._3.DeploymentOptionBoolean;
import com.cognos.developer.schemas.bibus._3.DeploymentOptionEnum;
import com.cognos.developer.schemas.bibus._3.DeploymentOptionImportRuleArray;
import com.cognos.developer.schemas.bibus._3.DeploymentOptionObjectInformationArray;
import com.cognos.developer.schemas.bibus._3.DeploymentOptionResolution;
import com.cognos.developer.schemas.bibus._3.DeploymentOptionSearchPathSingleObjectArray;
import com.cognos.developer.schemas.bibus._3.DeploymentOptionString;
import com.cognos.developer.schemas.bibus._3.ExportDeployment;
import com.cognos.developer.schemas.bibus._3.ImportDeployment;
import com.cognos.developer.schemas.bibus._3.MultilingualString;
import com.cognos.developer.schemas.bibus._3.MultilingualToken;
import com.cognos.developer.schemas.bibus._3.MultilingualTokenProp;
import com.cognos.developer.schemas.bibus._3.Option;
import com.cognos.developer.schemas.bibus._3.OptionArrayProp;
import com.cognos.developer.schemas.bibus._3.OrderEnum;
import com.cognos.developer.schemas.bibus._3.ParameterValue;
import com.cognos.developer.schemas.bibus._3.PropEnum;
import com.cognos.developer.schemas.bibus._3.QueryOptions;
import com.cognos.developer.schemas.bibus._3.SearchPathMultipleObject;
import com.cognos.developer.schemas.bibus._3.SearchPathSingleObject;
import com.cognos.developer.schemas.bibus._3.Sort;
import com.cognos.developer.schemas.bibus._3.UpdateActionEnum;
import com.cognos.developer.schemas.bibus._3.UpdateOptions;

public class Deployment {

	String strLocale = "en";

	static HashMap packageInformation=null;

	private static final String DEPLOY_OPTION_NAME = "com.cognos.developer.schemas.bibus._3.DeploymentOptionObjectInformationArray";
	/**
	 * Constructor
	 */
	public Deployment() {
		super();
	}

	/**
	 * use to retrieve the list of archives
	 */
	 // sn_dg_sdk_method_contentManagerService_listArchives_start_0
	public String[] getListOfArchives(CRNConnect connection) {
		String[] listOfArchives = null;
		try {
			listOfArchives = connection.getCMService().listArchives();
	// sn_dg_sdk_method_contentManagerService_listArchives_end_0
		} catch (RemoteException ex) {
			System.out
					.println("An error occurred while retrieving archive list:"
							+ "\n" + ex.getMessage());
		}

		return listOfArchives;
	}

	/**
	 * use to retrieve all the public folder content
	 */
	public String[] getAllFolders(CRNConnect myCon) {
		BaseClassWrapper[] listOfFolders = null;
		String[] allFolders = null;
		int count = 0;
		String packName=null;
		String packSearchPath=null;
		SearchPathSingleObject myPackSearchPath=null;


		String[] mySearchPath = new String[2];
		mySearchPath[0] = "/content//package";
		mySearchPath[1] = "/content/folder";
		// get available packages
		BaseClass[] packageList = getCSObject(myCon, mySearchPath[0]);
		// get available folders
		BaseClass[] folderList = getCSObject(myCon, mySearchPath[1]);

		packageInformation=new HashMap(packageList.length+folderList.length);

		for(int i=0;i<packageList.length;i++)
		{
			packName=packageList[i].getDefaultName().getValue();
			packSearchPath=packageList[i].getSearchPath().getValue();
			myPackSearchPath=new SearchPathSingleObject(packSearchPath);
			packageInformation.put(packName, myPackSearchPath);
		}

		for(int j=0;j<folderList.length;j++)
		{
			packName=folderList[j].getDefaultName().getValue();
			packSearchPath=folderList[j].getSearchPath().getValue();
			myPackSearchPath=new SearchPathSingleObject(packSearchPath);
			packageInformation.put(packName, myPackSearchPath);
		}

		listOfFolders = new BaseClassWrapper[packageList.length
				+ folderList.length];
		allFolders = new String[packageList.length + folderList.length];

		if (packageList != null && packageList.length > 0) {
			for (int j = 0; j < packageList.length; j++) {
				listOfFolders[count] = new BaseClassWrapper(packageList[j]);
				allFolders[count] = listOfFolders[count].toString();
				count++;
			}
		}

		if (folderList != null && folderList.length > 0) {
			for (int i = 0; i < folderList.length; i++) {
				listOfFolders[count] = new BaseClassWrapper(folderList[i]);
				allFolders[count] = listOfFolders[count].toString();
				count++;
			}
		}
		return allFolders;
	}

	/**
	 * use to get CM object
	 */
	public BaseClass[] getCSObject(CRNConnect con, String myPathStr) {
		SearchPathMultipleObject cmSearchPath = new SearchPathMultipleObject(
				myPathStr);
		BaseClass[] myCMObject = null;

		PropEnum props[] = new PropEnum[] { PropEnum.searchPath,
				PropEnum.defaultName };
		Sort sortOptions[] = { new Sort() };
		sortOptions[0].setOrder(OrderEnum.ascending);
		sortOptions[0].setPropName(PropEnum.defaultName);

		try {
			myCMObject = con.getCMService().query(cmSearchPath, props,
					sortOptions, new QueryOptions());
		} catch (RemoteException remoteEx) {
			System.out
					.println("An error occurred while querying CM object:"
							+ "\n" + remoteEx.getMessage());
		}

		return myCMObject;
	}

	/**
	 * use this method to return all the public folder content associated with
	 * one specific archive
	 */
	// sn_dg_sdk_method_contentManagerService_getDeploymentOptions_start_0
	public HashMap getPubFolderContent(String myArchive,
			CRNConnect myConnection) {

		Option[] deployOptEnum = new Option[] {};
		HashMap arrOfPublicFolder = new HashMap();

		try {
			deployOptEnum = myConnection.getCMService().getDeploymentOptions(
					myArchive, new Option[] {});
	// sn_dg_sdk_method_contentManagerService_getDeploymentOptions_end_0

			for (int i = 0; i < deployOptEnum.length; i++) {
				if (deployOptEnum[i].getClass().getName() == DEPLOY_OPTION_NAME) {

					DeploymentObjectInformation[] packDeployInfo = ((DeploymentOptionObjectInformationArray) deployOptEnum[i])
							.getValue();
					int packLen = packDeployInfo.length;

					for (int j = 0; j < packLen; j++) {
					    String packFolderName=packDeployInfo[j].getDefaultName();
					    SearchPathSingleObject packagePath=packDeployInfo[j].getSearchPath();

					    arrOfPublicFolder.put(packFolderName, packagePath);
					}
				}
			}
		} catch (RemoteException e) {
			System.out
					.println("An error occurred in getting Deployment options."
							+ "\n" + "The error: " + e.getMessage());
		}
		packageInformation=new HashMap(arrOfPublicFolder);

		return arrOfPublicFolder;
	}

	/**
	 * Deploying content
	 *
	 * @param myDeployType
	 *            a Deployment specification
	 * @param strArchiveName
	 *            an Archive name
	 * @param strDeployArchive
	 *            a Deployment archive name
	 * @param selectedPubContent
	 *            selected public folder contents which are associated with one
	 *            specific archive
	 * @param connection
	 *            Connection to Server
	 * @return String indicate the operation successed,failed or cancel
	 */
	public String deployContent(String myDeployType, String strArchiveName, String strDeployArchive,
			String[] selectedPubContent, CRNConnect connection) {
		AsynchReply asynchReply = null;
		String reportEventID = "-1";

		String deployPath;
		SearchPathSingleObject searchPathObject = new SearchPathSingleObject();

		// Add an archive name to the content store
		BaseClass[] ArchiveInfo = addArchive(myDeployType, strArchiveName,
				connection);

		if ((ArchiveInfo != null) && (ArchiveInfo.length == 1)) {
			deployPath = ArchiveInfo[0].getSearchPath().getValue();
			searchPathObject.set_value(deployPath);
		} else {
			return reportEventID;
		}

		Option[] myDeploymentOptionsEnum=null;
		if (myDeployType.equalsIgnoreCase("import")) {
			myDeploymentOptionsEnum = setDeploymentOptionEnum(myDeployType, strDeployArchive, selectedPubContent, connection);
		}else{
			myDeploymentOptionsEnum = setDeploymentOptionEnum(myDeployType, strArchiveName, selectedPubContent, connection);
		}

		OptionArrayProp deploymentOptionsArray = new OptionArrayProp();
		deploymentOptionsArray.setValue(myDeploymentOptionsEnum);
		if (myDeployType.equalsIgnoreCase("import")) {
			((ImportDeployment) ArchiveInfo[0])
					.setOptions(deploymentOptionsArray);
		} else {
			((ExportDeployment) ArchiveInfo[0])
					.setOptions(deploymentOptionsArray);
		}

		try {
			connection.getCMService().update(ArchiveInfo, new UpdateOptions());

			asynchReply = connection.getMonitorService().run(searchPathObject,
					new ParameterValue[] {}, new Option[] {});

		} catch (RemoteException remoteEx) {
			System.out.println("An error occurred while deploying content:"
					+ "\n" + remoteEx.getMessage());
		}

		if (asynchReply != null) {
			reportEventID = "Success";
		} else {
			reportEventID = "Failed";
		}
		return reportEventID;
	}

	/**
	 * use this method to add a Deployment object to the content store
	 */
	private BaseClass[] addArchive(String deploySpec, String nameOfArchive,
			CRNConnect con) {

		ImportDeployment importDeploy = null;
		ExportDeployment exportDeploy = null;
		BaseClass[] addedDeploymentObjects = null;
		BaseClass[] bca = new BaseClass[1];
		AddOptions addOpts = null;

		SearchPathSingleObject objOfSearchPath = new SearchPathSingleObject("/adminFolder");
		
		MultilingualTokenProp multilingualTokenProperty = new MultilingualTokenProp();
		MultilingualToken[] multilingualTokenArr = new MultilingualToken[1];
		MultilingualToken myMultilingualToken = new MultilingualToken();

		myMultilingualToken.setLocale(strLocale);
		myMultilingualToken.setValue(nameOfArchive);
		multilingualTokenArr[0] = myMultilingualToken;
		multilingualTokenProperty.setValue(multilingualTokenArr);

		if (deploySpec.equalsIgnoreCase("import")) {
			importDeploy = new ImportDeployment();
			addOpts = new AddOptions();
			importDeploy.setName(multilingualTokenProperty);
			addOpts.setUpdateAction(UpdateActionEnum.replace);
			bca[0] = importDeploy;
		} else {
			exportDeploy = new ExportDeployment();
			addOpts = new AddOptions();
			exportDeploy.setName(multilingualTokenProperty);
			addOpts.setUpdateAction(UpdateActionEnum.replace);
			bca[0] = exportDeploy;
		}

		try {
			addedDeploymentObjects = con.getCMService().add(objOfSearchPath,
					bca, addOpts);
		} catch (RemoteException remoEx) {
			System.out
					.println("An error occurred when adding a deployment object:"
							+ "\n" + remoEx.getMessage());
		}
		if ((addedDeploymentObjects != null)
				&& (addedDeploymentObjects.length > 0)) {
			return addedDeploymentObjects;
		} else {
			return null;
		}
	}

	/**
	 * use this method to define the deployment options
	 */
	private Option[] setDeploymentOptionEnum(String deploymentType, String nameOfArchive,
			String[] listOfSelectedFolders, CRNConnect con) {
		Option[] deploymentOptions = null;
		int num = 0;
		int eOptionCount=0;

		String[] deployOptionEnumBoolean = { "archiveOverwrite",
				"dataSourceSelect", "namespaceSelect", "namespaceThirdParty",
				"objectPolicies", "packageHistories", "packageOutputs",
				"packageSchedules", "packageSelect", "recipientsSelect",
				"takeOwnership" };

		String[] deployOptionEnumResolution = { "dataSourceConflictResolution",
				"namespaceConflictResolution",
				"objectPoliciesConflictResolution",
				"ownershipConflictResolution",
				"packageHistoriesConflictResolution",
				"packageOutputsConflictResolution",
				"packageSchedulesConflictResolution",
				"recipientsConflictResolution" };

		if(deploymentType.equalsIgnoreCase("import"))
		{
			eOptionCount=2;
		}
		else
		{
			eOptionCount=3;
		}

		deploymentOptions = new DeploymentOption[eOptionCount
				+ deployOptionEnumBoolean.length
				+ deployOptionEnumResolution.length];

		// Define the deployment options
		if(deploymentType.equalsIgnoreCase("import"))
		{
			deploymentOptions[num] = this
				.setImportDeploymentOptionPackageInfo(listOfSelectedFolders);
		}
		if (deploymentType.equalsIgnoreCase("export"))
		{
			deploymentOptions[num] = this
			.setImportDeploymentOptionPackageInfo(listOfSelectedFolders);

			deploymentOptions[++num] = this
				.setExportDeploymentOptionPackageInfo(listOfSelectedFolders);
		}
		deploymentOptions[++num] = this
				.setDeploymentOptionString(nameOfArchive);
		// change default value into 'true'
		deploymentOptions[++num] = this.setArchiveOverWrite(false);
		// use default value
		deploymentOptions[++num] = this.setDataSourceSelect(true);
		// use default value
		deploymentOptions[++num] = this.setNameSpaceSelect(true);
		// change default value into 'false'
		deploymentOptions[++num] = this.setNameSpaceThirdParty(false);
		// use default value
		deploymentOptions[++num] = this.setObjectPolicies(true);
		// use default value
		deploymentOptions[++num] = this.setPackageHistories(true);
		// use default value
		deploymentOptions[++num] = this.setPackageOutputs(true);
		// use default value
		deploymentOptions[++num] = this.setPackageSchedules(true);
		// use default value
		deploymentOptions[++num] = this.setPackageSelect(true);
		// use default value
		deploymentOptions[++num] = this.setRecipientsSelect(true);
		// change default value into 'true'
		deploymentOptions[++num] = this.setTakeOwnership(false);
		// use default value
		deploymentOptions[++num] = this.setDataSourceConflictResolution(true);
		// use default value
		deploymentOptions[++num] = this.setNamespaceConflictResolution(true);
		// use default value
		deploymentOptions[++num] = this
				.setObjectPoliciesConflictResolution(true);
		// use default value
		deploymentOptions[++num] = this.setOwnershipConflictResolution(true);
		// use default value
		deploymentOptions[++num] = this
				.setPackageHistoriesConflictResolution(true);
		// use default value
		deploymentOptions[++num] = this
				.setPackageOutputsConflictResolution(true);
		// use default value
		deploymentOptions[++num] = this
				.setPackageSchedulesConflictResolution(true);
		// use default value
		deploymentOptions[++num] = this.setRecipientsConflictResolution(true);

		return deploymentOptions;
	}


	// set import deployment option property (mandatory)
	private DeploymentOptionImportRuleArray setImportDeploymentOptionPackageInfo(String[] arrOfFolders) {
		DeploymentImportRule[] pkgDeployInfoArr = new DeploymentImportRule[arrOfFolders.length];
		DeploymentImportRule pkgDeployInfo;
		MultilingualToken[] multilingualTokenArr;
		MultilingualToken multilingualToken;
		SearchPathSingleObject packSearchPath=null;

		for (int i = 0; i < arrOfFolders.length; i++) {
			multilingualToken = new MultilingualToken();
			multilingualTokenArr = new MultilingualToken[1];

			pkgDeployInfo = new DeploymentImportRule();

			multilingualToken.setLocale(strLocale);
			multilingualToken.setValue(arrOfFolders[i]);
			multilingualTokenArr[0] = multilingualToken;

			String myPackageName=arrOfFolders[i];
			HashMap myPackInfo=new HashMap(packageInformation);

			if (myPackInfo.containsKey(myPackageName))
			{
				packSearchPath=(SearchPathSingleObject)myPackInfo.get(myPackageName);
			}

		    pkgDeployInfo.setArchiveSearchPath(packSearchPath);
			pkgDeployInfo.setName(multilingualTokenArr);
			pkgDeployInfo.setParent(new SearchPathSingleObject("/content"));
			pkgDeployInfoArr[i] = pkgDeployInfo;
		}
		DeploymentOptionImportRuleArray deployOptionPkgInfo = new DeploymentOptionImportRuleArray();
		deployOptionPkgInfo.setName(DeploymentOptionEnum.fromString("import"));
		deployOptionPkgInfo.setValue(pkgDeployInfoArr);

		return deployOptionPkgInfo;
	}


	// set export deployment option property (mandatory)
	private DeploymentOptionSearchPathSingleObjectArray setExportDeploymentOptionPackageInfo(String[] arrOfFolders) {
		SearchPathSingleObject[] exportPkgDeployInfoArr = new SearchPathSingleObject[arrOfFolders.length];
		SearchPathSingleObject exportPkgDeployInfo;
		String packSearchPath=null;

		for (int i = 0; i < arrOfFolders.length; i++) {
			exportPkgDeployInfo = new SearchPathSingleObject();

			String myPackageName=arrOfFolders[i];
			HashMap myExPackInfo=new HashMap(packageInformation);

			if (myExPackInfo.containsKey(myPackageName))
			{
				packSearchPath=((SearchPathSingleObject)myExPackInfo.get(myPackageName)).get_value();
			}

			exportPkgDeployInfo.set_value(packSearchPath);
			exportPkgDeployInfoArr[i] = exportPkgDeployInfo;
		}
		DeploymentOptionSearchPathSingleObjectArray exportDeployOptionPkgInfo = new DeploymentOptionSearchPathSingleObjectArray();
		exportDeployOptionPkgInfo.setName(DeploymentOptionEnum.fromString("export"));
		exportDeployOptionPkgInfo.setValue(exportPkgDeployInfoArr);

		return exportDeployOptionPkgInfo;
	}


	// set DeploymentOptionString property (mandatory)
	private DeploymentOptionString setDeploymentOptionString(String archiveName) {
		MultilingualString archiveDefault = new MultilingualString();
		archiveDefault.setLocale(strLocale);
		archiveDefault.setValue(archiveName);

		DeploymentOptionString deployOptionStr = new DeploymentOptionString();
		deployOptionStr.setName(DeploymentOptionEnum.fromString("archive"));
		deployOptionStr.setValue(archiveDefault.getValue());

		return deployOptionStr;
	}

	// allow the deployment overwrites the archive
	private DeploymentOptionBoolean setArchiveOverWrite(boolean setValue) {
		DeploymentOptionBoolean deployOptionBool = new DeploymentOptionBoolean();
		deployOptionBool.setName(DeploymentOptionEnum
				.fromString("archiveOverwrite"));
		if (setValue) {
			deployOptionBool.setValue(false);
		} else {
			deployOptionBool.setValue(true);
		}
		return deployOptionBool;
	}

	// set dataSourceSelect as default value - 'false'
	private DeploymentOptionBoolean setDataSourceSelect(boolean setValue) {
		DeploymentOptionBoolean deployOptionBool = new DeploymentOptionBoolean();
		deployOptionBool.setName(DeploymentOptionEnum
				.fromString("dataSourceSelect"));
		if (setValue) {
			deployOptionBool.setValue(false);
		} else {
			deployOptionBool.setValue(true);
		}
		return deployOptionBool;
	}

	// set namespaceSelect as default value - 'false'
	private DeploymentOptionBoolean setNameSpaceSelect(boolean setValue) {
		DeploymentOptionBoolean deployOptionBool = new DeploymentOptionBoolean();
		deployOptionBool.setName(DeploymentOptionEnum
				.fromString("namespaceSelect"));
		if (setValue) {
			deployOptionBool.setValue(false);
		} else {
			deployOptionBool.setValue(true);
		}
		return deployOptionBool;
	}

	// Not include references to external namespaces - value is false
	private DeploymentOptionBoolean setNameSpaceThirdParty(boolean setValue) {
		DeploymentOptionBoolean deployOptionBool = new DeploymentOptionBoolean();
		deployOptionBool.setName(DeploymentOptionEnum
				.fromString("namespaceThirdParty"));
		if (setValue) {
			deployOptionBool.setValue(true);
		} else {
			deployOptionBool.setValue(false);
		}
		return deployOptionBool;
	}

	// set objectPolicies as default value - 'false'
	private DeploymentOptionBoolean setObjectPolicies(boolean setValue) {
		DeploymentOptionBoolean deployOptionBool = new DeploymentOptionBoolean();
		deployOptionBool.setName(DeploymentOptionEnum
				.fromString("objectPolicies"));
		if (setValue) {
			deployOptionBool.setValue(false);
		} else {
			deployOptionBool.setValue(true);
		}
		return deployOptionBool;
	}

	// set packageHistories as default value - 'false'
	private DeploymentOptionBoolean setPackageHistories(boolean setValue) {
		DeploymentOptionBoolean deployOptionBool = new DeploymentOptionBoolean();
		deployOptionBool.setName(DeploymentOptionEnum
				.fromString("packageHistories"));
		if (setValue) {
			deployOptionBool.setValue(false);
		} else {
			deployOptionBool.setValue(true);
		}
		return deployOptionBool;
	}

	// set packageOutputs as default value - 'false'
	private DeploymentOptionBoolean setPackageOutputs(boolean setValue) {
		DeploymentOptionBoolean deployOptionBool = new DeploymentOptionBoolean();
		deployOptionBool.setName(DeploymentOptionEnum
				.fromString("packageOutputs"));
		if (setValue) {
			deployOptionBool.setValue(false);
		} else {
			deployOptionBool.setValue(true);
		}
		return deployOptionBool;
	}

	// set packageSchedules as default value - 'false'
	private DeploymentOptionBoolean setPackageSchedules(boolean setValue) {
		DeploymentOptionBoolean deployOptionBool = new DeploymentOptionBoolean();
		deployOptionBool.setName(DeploymentOptionEnum
				.fromString("packageSchedules"));
		if (setValue) {
			deployOptionBool.setValue(false);
		} else {
			deployOptionBool.setValue(true);
		}
		return deployOptionBool;
	}

	// set packageSelect as default value - 'true'
	private DeploymentOptionBoolean setPackageSelect(boolean setValue) {
		DeploymentOptionBoolean deployOptionBool = new DeploymentOptionBoolean();
		deployOptionBool.setName(DeploymentOptionEnum
				.fromString("packageSelect"));
		if (setValue) {
			deployOptionBool.setValue(true);
		} else {
			deployOptionBool.setValue(false);
		}
		return deployOptionBool;
	}

	// set recipientsSelect as default value - 'false'
	private DeploymentOptionBoolean setRecipientsSelect(boolean setValue) {
		DeploymentOptionBoolean deployOptionBool = new DeploymentOptionBoolean();
		deployOptionBool.setName(DeploymentOptionEnum
				.fromString("recipientsSelect"));
		if (setValue) {
			deployOptionBool.setValue(false);
		} else {
			deployOptionBool.setValue(true);
		}
		return deployOptionBool;
	}

	// set the owner to the owner from the source - the value is 'true'
	private DeploymentOptionBoolean setTakeOwnership(boolean setValue) {
		DeploymentOptionBoolean deployOptionBool = new DeploymentOptionBoolean();
		deployOptionBool.setName(DeploymentOptionEnum
				.fromString("takeOwnership"));
		if (setValue) {
			deployOptionBool.setValue(false);
		} else {
			deployOptionBool.setValue(true);
		}
		return deployOptionBool;
	}

	// set dataSourceConflictResolution as default value - 'replace'
	private DeploymentOptionResolution setDataSourceConflictResolution(
			boolean setValue) {
		DeploymentOptionResolution deployOptionResolute = new DeploymentOptionResolution();
		deployOptionResolute.setName(DeploymentOptionEnum
				.fromString("dataSourceConflictResolution"));
		if (setValue) {
			deployOptionResolute.setValue(ConflictResolutionEnum.replace);

		} else {
			deployOptionResolute.setValue(ConflictResolutionEnum.keep);
		}
		return deployOptionResolute;
	}

	// set namespaceConflictResolution as default value - 'replace'
	private DeploymentOptionResolution setNamespaceConflictResolution(
			boolean setValue) {
		DeploymentOptionResolution deployOptionResolute = new DeploymentOptionResolution();
		deployOptionResolute.setName(DeploymentOptionEnum
				.fromString("namespaceConflictResolution"));
		if (setValue) {
			deployOptionResolute.setValue(ConflictResolutionEnum.replace);

		} else {
			deployOptionResolute.setValue(ConflictResolutionEnum.keep);
		}
		return deployOptionResolute;
	}

	// set objectPoliciesConflictResolution as default value - 'replace'
	private DeploymentOptionResolution setObjectPoliciesConflictResolution(
			boolean setValue) {
		DeploymentOptionResolution deployOptionResolute = new DeploymentOptionResolution();
		deployOptionResolute.setName(DeploymentOptionEnum
				.fromString("objectPoliciesConflictResolution"));
		if (setValue) {
			deployOptionResolute.setValue(ConflictResolutionEnum.replace);

		} else {
			deployOptionResolute.setValue(ConflictResolutionEnum.keep);
		}
		return deployOptionResolute;
	}

	// set ownershipConflictResolution as default value - 'replace'
	private DeploymentOptionResolution setOwnershipConflictResolution(
			boolean setValue) {
		DeploymentOptionResolution deployOptionResolute = new DeploymentOptionResolution();
		deployOptionResolute.setName(DeploymentOptionEnum
				.fromString("ownershipConflictResolution"));
		if (setValue) {
			deployOptionResolute.setValue(ConflictResolutionEnum.replace);

		} else {
			deployOptionResolute.setValue(ConflictResolutionEnum.keep);
		}
		return deployOptionResolute;
	}

	// set packageHistoriesConflictResolution as default value - 'replace'
	private DeploymentOptionResolution setPackageHistoriesConflictResolution(
			boolean setValue) {
		DeploymentOptionResolution deployOptionResolute = new DeploymentOptionResolution();
		deployOptionResolute.setName(DeploymentOptionEnum
				.fromString("packageHistoriesConflictResolution"));
		if (setValue) {
			deployOptionResolute.setValue(ConflictResolutionEnum.replace);

		} else {
			deployOptionResolute.setValue(ConflictResolutionEnum.keep);
		}
		return deployOptionResolute;
	}

	// set packageOutputsConflictResolution as default value - 'replace'
	private DeploymentOptionResolution setPackageOutputsConflictResolution(
			boolean setValue) {
		DeploymentOptionResolution deployOptionResolute = new DeploymentOptionResolution();
		deployOptionResolute.setName(DeploymentOptionEnum
				.fromString("packageOutputsConflictResolution"));
		if (setValue) {
			deployOptionResolute.setValue(ConflictResolutionEnum.replace);

		} else {
			deployOptionResolute.setValue(ConflictResolutionEnum.keep);
		}
		return deployOptionResolute;
	}

	// set packageSchedulesConflictResolution as default value - 'replace'
	private DeploymentOptionResolution setPackageSchedulesConflictResolution(
			boolean setValue) {
		DeploymentOptionResolution deployOptionResolute = new DeploymentOptionResolution();
		deployOptionResolute.setName(DeploymentOptionEnum
				.fromString("packageSchedulesConflictResolution"));
		if (setValue) {
			deployOptionResolute.setValue(ConflictResolutionEnum.replace);

		} else {
			deployOptionResolute.setValue(ConflictResolutionEnum.keep);
		}
		return deployOptionResolute;
	}

	// set recipientsConflictResolution as default value - 'replace'
	private DeploymentOptionResolution setRecipientsConflictResolution(
			boolean setValue) {
		DeploymentOptionResolution deployOptionResolute = new DeploymentOptionResolution();
		deployOptionResolute.setName(DeploymentOptionEnum
				.fromString("recipientsConflictResolution"));
		if (setValue) {
			deployOptionResolute.setValue(ConflictResolutionEnum.replace);

		} else {
			deployOptionResolute.setValue(ConflictResolutionEnum.keep);
		}
		return deployOptionResolute;
	}

}
