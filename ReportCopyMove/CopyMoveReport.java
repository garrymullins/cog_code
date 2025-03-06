/** 
Licensed Materials - Property of IBM

IBM Cognos Products: DOCS

(C) Copyright IBM Corp. 2005, 2006

US Government Users Restricted Rights - Use, duplication or disclosure restricted by GSA ADP Schedule Contract with
IBM Corp.
*/
/**
 * CopyMoveReport.java
 *
 * Copyright (C) 2006 Cognos ULC, an IBM Company. All rights reserved.
 * Cognos (R) is a trademark of Cognos ULC, (formerly Cognos Incorporated).
 *
 */
import java.rmi.RemoteException;

import com.cognos.developer.schemas.bibus._3.Account;
import com.cognos.developer.schemas.bibus._3.BaseClass;
import com.cognos.developer.schemas.bibus._3.CopyOptions;
import com.cognos.developer.schemas.bibus._3.MoveOptions;
import com.cognos.developer.schemas.bibus._3.OrderEnum;
import com.cognos.developer.schemas.bibus._3.PropEnum;
import com.cognos.developer.schemas.bibus._3.QueryOptions;
import com.cognos.developer.schemas.bibus._3.Report;
import com.cognos.developer.schemas.bibus._3.SearchPathMultipleObject;
import com.cognos.developer.schemas.bibus._3.SearchPathSingleObject;
import com.cognos.developer.schemas.bibus._3.Sort;
import com.cognos.developer.schemas.bibus._3.StringProp;
import com.cognos.developer.schemas.bibus._3.UpdateActionEnum;

public class CopyMoveReport {

	// This is a method for retrieving a list of the available reports to run
	protected BaseClassWrapper[] getListOfReports(CRNConnect connection) {
		BaseClassWrapper reportAndQueryList[] = null;
		BaseClass reports[] = new BaseClass[0];
		BaseClass queries[] = new BaseClass[0];
		int reportAndQueryIndex = 0;
		int reportIndex = 0;
		int queryIndex = 0;

		if (connection == null) {
			System.out
					.println("Invalid parameter passed to getListOfReports()\n");
			return null;
		}

		PropEnum props[] = new PropEnum[] { PropEnum.searchPath,
				PropEnum.defaultName };
		Sort sortOptions[] = { new Sort() };
		sortOptions[0].setOrder(OrderEnum.ascending);
		sortOptions[0].setPropName(PropEnum.defaultName);

		try {
			SearchPathMultipleObject reportsPath = new SearchPathMultipleObject(
					"/content//report");
			SearchPathMultipleObject queriesPath = new SearchPathMultipleObject(
					"/content//query");
			reports = connection.getCMService().query(reportsPath, props,
					sortOptions, new QueryOptions());
			queries = connection.getCMService().query(queriesPath, props,
					sortOptions, new QueryOptions());
		} catch (java.rmi.RemoteException remoteEx) {
			System.out.println("Caught Remote Exception:\n");
			remoteEx.printStackTrace();
		}

		reportAndQueryList = new BaseClassWrapper[reports.length
				+ queries.length];

		if ((reports != null) && (reports.length > 0)) {
			for (reportIndex = 0; reportIndex < reports.length; reportIndex++) {
				reportAndQueryList[reportAndQueryIndex++] = new BaseClassWrapper(
						reports[reportIndex]);
			}
		}
		if ((queries != null) && (queries.length > 0)) {
			for (queryIndex = 0; queryIndex < queries.length; queryIndex++) {
				reportAndQueryList[reportAndQueryIndex++] = new BaseClassWrapper(
						queries[queryIndex]);
			}
		}
		return reportAndQueryList;
	}

	// Get current account information to assign search path to target report
	public Account getCurrentAccountInfo(CRNConnect myConn) {
		BaseClass[] bcAccountInfo = null;
		Account currentAccount = null;

		PropEnum propEnum[] = new PropEnum[] { PropEnum.searchPath,
				PropEnum.defaultName };
		SearchPathMultipleObject searchPathObject = new SearchPathMultipleObject("~");

		try {

			bcAccountInfo = myConn.getCMService().query(searchPathObject,
					propEnum, new Sort[] {}, new QueryOptions());

			if (bcAccountInfo != null && bcAccountInfo.length > 0) {
				currentAccount = (Account) bcAccountInfo[0];
			}
		} catch (RemoteException ex) {
			System.out.println("Failed to get current Account information."
					+ "\n" + "The error: " + ex.getMessage());
		}

		return currentAccount;
	}

	// Copy or move a report to the target path
	public String copyMoveReport(CRNConnect myCon, BaseClassWrapper myReport,
			String optionName, String myTargetReportName) {

		String results = null;
		BaseClass[] bcCopyMove = null;

		Report targetReport = null;
		SearchPathSingleObject targetSearchPath = null;
		StringProp mySearchPath = null;

		// Get the source report search path and specifies the target report
		String myReportPath = myReport.getBaseClassObject().getSearchPath()
				.getValue();
		mySearchPath = new StringProp();
		mySearchPath.setValue(myReportPath);

		targetReport = new Report();
		targetReport.setSearchPath(mySearchPath);
		bcCopyMove = new BaseClass[1];
		bcCopyMove[0] = targetReport;

		// Specifies the target location for the copied report
		targetSearchPath = new SearchPathSingleObject();
		// get current account information
		Account currentAccount = this.getCurrentAccountInfo(myCon);
		String targetPath = currentAccount.getSearchPath().getValue()
				+ "/folder[@name='My Folders']";
		targetSearchPath.set_value(targetPath);

		if (optionName.equalsIgnoreCase("Copy")) {
			results = doCopyOrMove(myCon, bcCopyMove, targetSearchPath,
					optionName);
		} else if (optionName.equalsIgnoreCase("Copy Rename")) {
			results = doCopyOrMoveRename(myCon, myReport, bcCopyMove,
					targetSearchPath, optionName, myTargetReportName);
		} else if (optionName.equalsIgnoreCase("Move")) {
			results = doCopyOrMove(myCon, bcCopyMove, targetSearchPath,
					optionName);
		} else if (optionName.equalsIgnoreCase("Move Rename")) {
			results = doCopyOrMoveRename(myCon, myReport, bcCopyMove,
					targetSearchPath, optionName, myTargetReportName);
		}
		return results;
	}

	// Use 'copyRename' or 'moveRename' method to copy or move reports to
	// another location in the
	// content store under a different name
	public String doCopyOrMoveRename(CRNConnect Conn,
			BaseClassWrapper selectedReport, BaseClass[] bcCopy,
			SearchPathSingleObject copyTargetSearchPath, String runOptionName,
			String targetReportName) {

		String copyMoveRenameResults = null;
		CopyOptions cpyOption = null;
		MoveOptions moveOption = null;
		BaseClass[] bcCopyMoveRename = null;

		// Specifies the target report name for the copies or moves of the
		// report
		String[] targetName = new String[1];
		targetName[0] = targetReportName;

		// Set the copy options to replace so it overwrites an existing
		// report
		cpyOption = new CopyOptions();
		cpyOption.setUpdateAction(UpdateActionEnum.replace);

		// Set the move options to replace so it overwrites an existing
		// report
		moveOption = new MoveOptions();
		moveOption.setUpdateAction(UpdateActionEnum.replace);

		try {

			if (runOptionName.equalsIgnoreCase("Copy Rename")) {
				// Copy report to target path in the content store under a
				// different name
				bcCopyMoveRename = Conn.getCMService().copyRename(bcCopy,
						copyTargetSearchPath, targetName, cpyOption);
			} else if (runOptionName.equalsIgnoreCase("Move Rename")) {
				// Move report to target path in the content store under a
				// different name
				bcCopyMoveRename = Conn.getCMService().moveRename(bcCopy,
						copyTargetSearchPath, targetName, moveOption);
			}

			if (bcCopyMoveRename != null && bcCopyMoveRename.length > 0) {
				copyMoveRenameResults = bcCopyMoveRename[0].getStoreID()
						.getValue().get_value();
			} else {
				System.out.println("Failed to " + runOptionName + " a report.");
				return null;
			}
		} catch (Exception e) {
			System.out.println("Failed to " + runOptionName + " a report"
					+ "\n" + "The error: " + e.getMessage());
			return null;
		}
		return copyMoveRenameResults;
	}

	// Use 'copy' or 'move' method to copy or move objects within the content
	// store
	public String doCopyOrMove(CRNConnect Conn, BaseClass[] bcCopyMove,
			SearchPathSingleObject copyMoveTargetSearchPath,
			String runOptionName) {

		String copyMoveResults = null;
		BaseClass[] bcCopyMoveResults = null;

		CopyOptions copyOptions = new CopyOptions();
		copyOptions.setUpdateAction(UpdateActionEnum.replace);

		MoveOptions moveOptions = new MoveOptions();
		moveOptions.setUpdateAction(UpdateActionEnum.replace);

		try {
			if (runOptionName.equalsIgnoreCase("Copy")) {
				// copy report to target path in the content store
				bcCopyMoveResults = Conn.getCMService().copy(bcCopyMove,
						copyMoveTargetSearchPath, copyOptions);
			} else if (runOptionName.equalsIgnoreCase("Move")) {
				// Move report to target path in the content store
				bcCopyMoveResults = Conn.getCMService().move(bcCopyMove,
						copyMoveTargetSearchPath, moveOptions);
			}

			if (bcCopyMoveResults != null && bcCopyMoveResults.length > 0) {
				copyMoveResults = bcCopyMoveResults[0].getStoreID().getValue()
						.get_value();
			} else {
				System.out.println("Failed to " + runOptionName
						+ " a report to target path.");
				return null;
			}
		} catch (Exception e) {
			System.out.println("Failed to " + runOptionName + " a report"
					+ "\n" + "The error: " + e.getMessage());
			return null;
		}
		return copyMoveResults;
	}

	// Get report name
	public String getReportName(BaseClassWrapper aReport) {
		String myReportName = null;

		if (aReport != null) {
			myReportName = ((Report) aReport.getBaseClassObject())
					.getDefaultName().getValue();
		}
		return myReportName;
	}

	public boolean checkReportStatus(CRNConnect myConnect,
			BaseClassWrapper myChosenReport) {
		boolean checkRS = false;

		BaseClass[] checkResult = null;

		String reportSearchPath = myChosenReport.getBaseClassObject()
				.getSearchPath().getValue();

		try {
			checkResult = myConnect.getCMService().query(
					new SearchPathMultipleObject(reportSearchPath),
					new PropEnum[0], new Sort[0], new QueryOptions());
			if (checkResult != null && checkResult.length > 0) {
				checkRS = true;
			}
		} catch (RemoteException re) {
			System.out
					.println("Error retrieving report from the content store: "
							+ re.getMessage());
		}

		return checkRS;
	}
}
