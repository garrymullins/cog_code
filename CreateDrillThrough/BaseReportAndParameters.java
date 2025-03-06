/** 
Licensed Materials - Property of IBM

IBM Cognos Products: DOCS

(C) Copyright IBM Corp. 2005, 2008

US Government Users Restricted Rights - Use, duplication or disclosure restricted by GSA ADP Schedule Contract with
IBM Corp.
*/
/**
 * BaseReportAndParameters.java
 *
 * Copyright (C) 2008 Cognos ULC, an IBM Company. All rights reserved.
 * Cognos (R) is a trademark of Cognos ULC, (formerly Cognos Incorporated).
 *
*/

import java.rmi.RemoteException;
import java.util.Hashtable;
import java.util.Set;

import com.cognos.developer.schemas.bibus._3.BaseClass;
import com.cognos.developer.schemas.bibus._3.BaseParameter;
import com.cognos.developer.schemas.bibus._3.OrderEnum;
import com.cognos.developer.schemas.bibus._3.PropEnum;
import com.cognos.developer.schemas.bibus._3.QueryOptions;
import com.cognos.developer.schemas.bibus._3.SearchPathMultipleObject;
import com.cognos.developer.schemas.bibus._3.Sort;

public class BaseReportAndParameters {

	ReportParameters myReportParameters = new ReportParameters();

	DrillThrough myDrillThrough = new DrillThrough();

	// Default constructor
	public BaseReportAndParameters() {
	}

	/**
	 * Hash report name and its parameters
	 */
	public Hashtable hashReportNameAndPara(CRNConnect conn, String mySearchPath) {

		Hashtable myHashTable = null;
		BaseClassWrapper[] myReport = null;

		// retrieve objects from the content store
		BaseClass[] reportList = getCSObject(conn, mySearchPath);
		myReport = new BaseClassWrapper[reportList.length];

		if (reportList != null && reportList.length > 0) {
			myHashTable = new Hashtable();
			for (int num = 0; num < reportList.length; num++) {
				BaseParameter[] myBasePara = null;
				myReport[num] = new BaseClassWrapper(reportList[num]);
				String aReportName = myReport[num].toString();
				try {
					// get report's parameters
					myBasePara = myReportParameters.getReportParameters(
							myReport[num], conn);
				} catch (RemoteException reEx) {
					System.out.println("Error getting '"
							+ myReport[num].getBaseClassObject()
									.getSearchPath().getValue() + aReportName
							+ "' parameters: "
							+ reEx.getMessage());
					return null;
				}

				if (myBasePara.length > 0) {
					myHashTable.put(myReport[num], myBasePara);
				}
			}
		}
		return myHashTable;
	}

	/**
	 * Use to get all the packages from content store
	 */
	public BaseClassWrapper[] getListOfPackages(CRNConnect myCon) {
		BaseClassWrapper[] listOfPackages = null;

		// package search path
		String mySearchPath = "/content//package";

		BaseClass[] packageList = getCSObject(myCon, mySearchPath);

		if (packageList != null && packageList.length > 0) {
			int numOfPackage = packageList.length;
			listOfPackages = new BaseClassWrapper[numOfPackage];

			for (int i = 0; i < numOfPackage; i++) {
				listOfPackages[i] = new BaseClassWrapper(packageList[i]);
			}
		}
		return listOfPackages;
	}

	/**
	 * Use this method to retrieve objects from the content store
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
					.println("Error retrieving object from Content Store according to specified search path: "
							+ remoteEx.getMessage());
		}

		return myCMObject;
	}

	/**
	 * Use this method to retrieve report from a hash table
	 */
	public BaseClassWrapper[] getReports(Hashtable myHash) {

		BaseClassWrapper[] listOfReport = null;

		Set setOfReport = myHash.keySet();

		Object[] myReports = setOfReport.toArray();

		if (myReports.length > 0) {
			listOfReport = new BaseClassWrapper[myReports.length];
			for (int n = 0; n < myReports.length; n++) {
				listOfReport[n] = (BaseClassWrapper) myReports[n];
			}
		}
		return listOfReport;
	}

}