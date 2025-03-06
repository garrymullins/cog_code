/** 
Licensed Materials - Property of IBM

IBM Cognos Products: DOCS

(C) Copyright IBM Corp. 2005, 2008

US Government Users Restricted Rights - Use, duplication or disclosure restricted by GSA ADP Schedule Contract with
IBM Corp.
*/
/**
 * Dispatcher.java
 *
 * Copyright (C) 2008 Cognos ULC, an IBM Company. All rights reserved.
 * Cognos (R) is a trademark of Cognos ULC, (formerly Cognos Incorporated).
 *
*/
import java.math.BigInteger;
import java.rmi.RemoteException;

import com.cognos.developer.schemas.bibus._3.AuditLevelEnum;
import com.cognos.developer.schemas.bibus._3.AuditLevelEnumProp;
import com.cognos.developer.schemas.bibus._3.BaseClass;
import com.cognos.developer.schemas.bibus._3.Dispatcher_Type;
import com.cognos.developer.schemas.bibus._3.NonNegativeIntegerProp;
import com.cognos.developer.schemas.bibus._3.OrderEnum;
import com.cognos.developer.schemas.bibus._3.PingReply;
import com.cognos.developer.schemas.bibus._3.PropEnum;
import com.cognos.developer.schemas.bibus._3.QueryOptions;
import com.cognos.developer.schemas.bibus._3.SearchPathMultipleObject;
import com.cognos.developer.schemas.bibus._3.SearchPathSingleObject;
import com.cognos.developer.schemas.bibus._3.Sort;
import com.cognos.developer.schemas.bibus._3.UpdateOptions;

public class Dispatcher {

	// get available Dispatchers according to the specified search path
	public BaseClassWrapper[] getDispatcherInfo(CRNConnect Con,
			String searchPath) {
		BaseClassWrapper[] dispatcherInfo = null;
		if ((searchPath == null) || (searchPath.length() == 0)
				|| (searchPath.compareTo("") == 0)) {
			System.out.println("Invalid searchPath"
					+ System.getProperty("line.separator"));
			return null;
		}

		// Search properties: we need the defaultName and the searchPath.
		PropEnum[] properties = { PropEnum.defaultName, PropEnum.searchPath };

		// Sort options: ascending sort on the defaultName property.
		Sort[] sortBy = { new Sort() };
		sortBy[0].setOrder(OrderEnum.ascending);
		sortBy[0].setPropName(PropEnum.defaultName);

		// Query options; use the defaults.
		QueryOptions options = new QueryOptions();

		try {
			BaseClass[] results = Con.getCMService().query(
					new SearchPathMultipleObject(searchPath), properties,
					sortBy, options);

			if (results != null && results.length > 0) {
				dispatcherInfo = new BaseClassWrapper[results.length];

				for (int i = 0; i < results.length; i++) {
					dispatcherInfo[i] = new BaseClassWrapper(results[i]);
				}
			}

		} catch (RemoteException remoteEx) {
			System.out.println("The request threw an RMI exception:");
			System.out.println(remoteEx.getMessage());
			System.out.println("Stack trace:");
			remoteEx.printStackTrace();
			return null;
		}

		return dispatcherInfo;
	}

	// Get available dispatchers' search path
	public String[] getAvailableDispatcher(CRNConnect Con, String searchPath) {
		String[] dispatcherList = null;

		BaseClassWrapper[] availableDispatcher = getDispatcherInfo(Con,
				searchPath);

		if (availableDispatcher != null && availableDispatcher.length > 0) {
			dispatcherList = new String[availableDispatcher.length];

			for (int n = 0; n < availableDispatcher.length; n++) {
				dispatcherList[n] = availableDispatcher[n].getBaseClassObject()
						.getSearchPath().getValue();
			}
		}
		return dispatcherList;

	}

	// get Services name
	public BaseClassWrapper[] getService(CRNConnect Con, String searchPath) {
		BaseClassWrapper[] serviceList = null;
		int count = 0;

		BaseClassWrapper[] myServiceList = getDispatcherInfo(Con, searchPath);

		if (myServiceList != null && myServiceList.length > 0) {
			serviceList = new BaseClassWrapper[myServiceList.length];

			for (int i = 0; i < myServiceList.length; i++) {
				String serviceName = myServiceList[i].toString();
				// filter some components that are not Service
				if (serviceName.indexOf("installedComponent") == -1
						&& serviceName.indexOf("Service") != -1) {
					serviceList[count] = myServiceList[i];
					count++;
				}
			}
		}
		return serviceList;
	}

	// method uses to ping dispatcher
	public String getDispatcherVersion(CRNConnect Con, String dispSearchPathURL) {

		// sn_dg_sdk_method_dispatcher_ping_start_0
		String pingResult = null;

		SearchPathSingleObject myDispSearchPath = new SearchPathSingleObject(
				dispSearchPathURL);
		try {
			PingReply pingReplyResult = Con.getDispatcherService().ping(
					myDispSearchPath);

			if (pingReplyResult != null) {
				pingResult = pingReplyResult.getVersion();
			}
		// sn_dg_sdk_method_dispatcher_ping_end_0

		} catch (RemoteException re) {
			System.out
					.println("Error calling Ping method: "
							+ re.getMessage());
		}
		return pingResult;
	}

	// method uses to get dispatcher status
	public Dispatcher_Type getDispatcherStatus(CRNConnect Con,
			String dispSearchPathURL) {
		BaseClass[] bcDispatcher = null;
		Dispatcher_Type dispatcherStatus = null;
		SearchPathMultipleObject myDispSearchPath = new SearchPathMultipleObject(
				dispSearchPathURL);
		// set properties
		PropEnum[] props = new PropEnum[5];
		props[0] = PropEnum.searchPath;
		props[1] = PropEnum.dispatcherPath;
		props[2] = PropEnum.rsAffineConnections;
		props[3] = PropEnum.asAuditLevel;
		props[4] = PropEnum.brsMaximumProcesses;

		try {
			bcDispatcher = Con.getCMService().query(myDispSearchPath, props,
					new Sort[] {}, new QueryOptions());
			if (bcDispatcher != null && bcDispatcher.length > 0) {
				dispatcherStatus = (Dispatcher_Type) bcDispatcher[0];
			}
		} catch (RemoteException re) {
			System.out
					.println("Error getting Dispatcher status: "
							+ re.getMessage());
			return null;
		}
		return dispatcherStatus;
	}

	// method uses to start service
	public boolean startService(CRNConnect Con, String dispSearchPathURL) {

		// sn_dg_sdk_method_dispatcher_startService_start_0
		SearchPathSingleObject myDispSearchPath = new SearchPathSingleObject(
				dispSearchPathURL);

		try {
			Con.getDispatcherService().startService(myDispSearchPath);
		// sn_dg_sdk_method_dispatcher_startService_end_0
		} catch (RemoteException re) {
			System.out.println("Error starting service: "
					+ re.getMessage());
			return false;

		}
		return true;
	}

	// method uses to stop service
	public boolean stopService(CRNConnect Con, String dispSearchPathURL) {

		// sn_dg_sdk_method_dispatcher_stopService_start_0
		SearchPathSingleObject myDispSearchPath = new SearchPathSingleObject(
				dispSearchPathURL);

		try {
			Con.getDispatcherService().stopService(myDispSearchPath, true);
		// sn_dg_sdk_method_dispatcher_stopService_end_0
		} catch (RemoteException re) {
			System.out.println("Error stopping service: "
					+ re.getMessage());
			return false;
		}
		return true;
	}

	// method uses to set logging level
	public String setLoggingLevel(CRNConnect Con, String dispSearchPathURL,
			String serviceName, String mySettingLevel) {
		String updatedLevelResult = null;
		BaseClass[] bcLoggingLevel = null;
		BaseClass[] setLoggingLevelResult = null;

		// Array of available services
		String[] arrOfServices = { "AgentService", "BatchReportService",
				"ContentManagerService", "DataIntegrationService",
				"dispatcher", "DeliveryService", "EventManagementService",
				"IndexDataService", "IndexSearchService", "IndexUpdateService",
				"JobService", "MobileService", "MetadataService",
				"MetricsManagerService", "MonitorService",
				"PlanningAdministrationConsoleService",
				"PlanningRuntimeService", "PresentationService",
				"PlanningTaskService", "reportDataService", "ReportService",
				"SystemService" };

		// set the logging level
		AuditLevelEnumProp auditLevelProp = new AuditLevelEnumProp();
		if (mySettingLevel.equalsIgnoreCase("Minimal")) {
			auditLevelProp.setValue(AuditLevelEnum.minimal);
		} else if (mySettingLevel.equalsIgnoreCase("Basic")) {
			auditLevelProp.setValue(AuditLevelEnum.basic);
		} else if (mySettingLevel.equalsIgnoreCase("Request")) {
			auditLevelProp.setValue(AuditLevelEnum.request);
		} else if (mySettingLevel.equalsIgnoreCase("Trace")) {
			auditLevelProp.setValue(AuditLevelEnum.trace);
		} else if (mySettingLevel.equalsIgnoreCase("Full")) {
			auditLevelProp.setValue(AuditLevelEnum.full);
		}

		Dispatcher_Type myDispatcherType = this.getDispatcherStatus(Con,
				dispSearchPathURL);
		if (myDispatcherType != null) {
			// setting logging level for different Services
			if (serviceName.compareToIgnoreCase(arrOfServices[0]) == 0) {
				myDispatcherType.setAsAuditLevel(auditLevelProp);
			} else if (serviceName.compareToIgnoreCase(arrOfServices[1]) == 0) {
				myDispatcherType.setBrsAuditLevel(auditLevelProp);
			} else if (serviceName.compareToIgnoreCase(arrOfServices[2]) == 0) {
				myDispatcherType.setCmsAuditLevel(auditLevelProp);
			} else if (serviceName.compareToIgnoreCase(arrOfServices[3]) == 0) {
				myDispatcherType.setDisAuditLevel(auditLevelProp);
			} else if (serviceName.compareToIgnoreCase(arrOfServices[4]) == 0) {
				myDispatcherType.setDispatcherAuditLevel(auditLevelProp);
			} else if (serviceName.compareToIgnoreCase(arrOfServices[5]) == 0) {
				myDispatcherType.setDsAuditLevel(auditLevelProp);
			} else if (serviceName.compareToIgnoreCase(arrOfServices[6]) == 0) {
				myDispatcherType.setEmsAuditLevel(auditLevelProp);
			} else if (serviceName.compareToIgnoreCase(arrOfServices[7]) == 0) {
				myDispatcherType.setIdsAuditLevel(auditLevelProp);
			} else if (serviceName.compareToIgnoreCase(arrOfServices[8]) == 0) {
				myDispatcherType.setIssAuditLevel(auditLevelProp);
			} else if (serviceName.compareToIgnoreCase(arrOfServices[9]) == 0) {
				myDispatcherType.setIusAuditLevel(auditLevelProp);
			} else if (serviceName.compareToIgnoreCase(arrOfServices[10]) == 0) {
				myDispatcherType.setJsAuditLevel(auditLevelProp);
			} else if (serviceName.compareToIgnoreCase(arrOfServices[11]) == 0) {
				myDispatcherType.setMbsAuditLevel(auditLevelProp);
			} else if (serviceName.compareToIgnoreCase(arrOfServices[12]) == 0) {
				myDispatcherType.setMdsAuditLevel(auditLevelProp);
			} else if (serviceName.compareToIgnoreCase(arrOfServices[13]) == 0) {
				myDispatcherType.setMmsAuditLevel(auditLevelProp);
			} else if (serviceName.compareToIgnoreCase(arrOfServices[14]) == 0) {
				myDispatcherType.setMsAuditLevel(auditLevelProp);
			} else if (serviceName.compareToIgnoreCase(arrOfServices[15]) == 0) {
				myDispatcherType.setPacsAuditLevel(auditLevelProp);
			} else if (serviceName.compareToIgnoreCase(arrOfServices[16]) == 0) {
				myDispatcherType.setPrsAuditLevel(auditLevelProp);
			} else if (serviceName.compareToIgnoreCase(arrOfServices[17]) == 0) {
				myDispatcherType.setPsAuditLevel(auditLevelProp);
			} else if (serviceName.compareToIgnoreCase(arrOfServices[18]) == 0) {
				myDispatcherType.setPtsAuditLevel(auditLevelProp);
			} else if (serviceName.compareToIgnoreCase(arrOfServices[19]) == 0) {
				myDispatcherType.setRdsAuditLevel(auditLevelProp);
			} else if (serviceName.compareToIgnoreCase(arrOfServices[20]) == 0) {
				myDispatcherType.setRsAuditLevel(auditLevelProp);
			} else if (serviceName.compareToIgnoreCase(arrOfServices[21]) == 0) {
				myDispatcherType.setSsAuditLevel(auditLevelProp);
			}
		} else {
			return null;
		}

		bcLoggingLevel = new BaseClass[1];
		bcLoggingLevel[0] = myDispatcherType;

		try {
			// update logging level for selected Service
			setLoggingLevelResult = Con.getCMService().update(bcLoggingLevel,
					new UpdateOptions());
			if (setLoggingLevelResult != null
					&& setLoggingLevelResult.length > 0) {
				updatedLevelResult = setLoggingLevelResult[0].getStoreID()
						.getValue().get_value();
			}

		} catch (RemoteException re) {
			System.out
					.println("Error setting logging level: "
							+ re.getMessage());
			return null;
		}
		return updatedLevelResult;
	}

	// method uses to set maximum number of processes for Batch Report Service
	public String setMaxProcesses(CRNConnect Con, String dispSearchPathURL,
			String myServiceName, String maxProcessesNum) {

		String updatedProcessNumResult = null;
		BaseClass[] bcMaxProcesses = null;
		BaseClass[] setMaxProcessesResult = null;

		// set maximum number of processes
		NonNegativeIntegerProp processNum = new NonNegativeIntegerProp();
		BigInteger myNum = new BigInteger(maxProcessesNum);
		processNum.setValue(myNum);

		Dispatcher_Type myDispatcherType = this.getDispatcherStatus(Con,
				dispSearchPathURL);
		if (myDispatcherType != null) {
			if (myServiceName.equalsIgnoreCase("BatchReportService")) {
				myDispatcherType.setBrsMaximumProcesses(processNum);
			} else if (myServiceName.equalsIgnoreCase("ReportService")) {
				myDispatcherType.setRsMaximumProcesses(processNum);
			}
		} else {
			return null;
		}

		bcMaxProcesses = new BaseClass[1];
		bcMaxProcesses[0] = myDispatcherType;

		try {

			setMaxProcessesResult = Con.getCMService().update(bcMaxProcesses,
					new UpdateOptions());
			if (setMaxProcessesResult != null
					&& setMaxProcessesResult.length > 0) {
				updatedProcessNumResult = setMaxProcessesResult[0].getStoreID()
						.getValue().get_value();
			}

		} catch (RemoteException re) {
			System.out
					.println("Error setting Maximum Processes: "
							+ re.getMessage());
			return null;
		}
		return updatedProcessNumResult;
	}
}
