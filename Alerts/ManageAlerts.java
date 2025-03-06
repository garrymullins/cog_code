/** 
Licensed Materials - Property of IBM

IBM Cognos Products: DOCS

(C) Copyright IBM Corp. 2005, 2008

US Government Users Restricted Rights - Use, duplication or disclosure restricted by GSA ADP Schedule Contract with
IBM Corp.
*/
/**
 * ManageAlerts.java
 *
 * Copyright (C) 2008 Cognos ULC, an IBM Company. All rights reserved.
 * Cognos (R) is a trademark of Cognos ULC, (formerly Cognos Incorporated).
 *
  */

import com.cognos.developer.schemas.bibus._3.AgentNotificationStatusEnum;
import com.cognos.developer.schemas.bibus._3.AsynchDetailAgentNotificationStatus;
import com.cognos.developer.schemas.bibus._3.AsynchReply;
import com.cognos.developer.schemas.bibus._3.AsynchReplyStatusEnum;
import com.cognos.developer.schemas.bibus._3.AsynchRequest;
import com.cognos.developer.schemas.bibus._3.AsynchSecondaryRequest;
import com.cognos.developer.schemas.bibus._3.Option;
import com.cognos.developer.schemas.bibus._3.ParameterValue;
import com.cognos.developer.schemas.bibus._3.SearchPathSingleObject;

public class ManageAlerts
{
	/**
	* Add the current user to the alert list for a report.
	*
	* @param connection
	* 		  Connection to Server
	* @param csobject
	*        Specifies the report. 
	*/
	public String addNotificationForUser(CRNConnect connection, BaseClassWrapper csobject)
	{
		AsynchRequest response = null;
		AsynchReply asynchReply = null;
		String reportPath = csobject.getBaseClassObject().getSearchPath().getValue();

		if (csobject == null)
		{
			return "No valid report selected";
		}

		try
		{
			// sn_dg_sdk_method_deliveryService_addNotification_start_0
			asynchReply =
				connection.getDeliveryService().addNotification(new SearchPathSingleObject(reportPath));
			// sn_dg_sdk_method_deliveryService_addNotification_end_0

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

			return "Add notification request complete.";

		}
		catch (Exception e)
		{
			//System.out.println("An error occurred in the addNotificationForUser Java method.");
			//e.printStackTrace();
			return "An Error occurred. You may not have an email address defined, or you may already be on the alert list.";
		}

	}

	/**
	* Delete the current user from the alert list for a report.
	*
	* @param connection
	* 		  Connection to Server
	* @param csobject
	*        Specifies the report. 
	*/
	public String deleteSingleNotification(CRNConnect connection, BaseClassWrapper csobject)
	{
		AsynchRequest response = null;
		AsynchReply asynchReply = null;
		String reportPath = csobject.getBaseClassObject().getSearchPath().getValue();

		if (csobject == null)
		{
			return "No valid report selected";
		}

		try
		{

			// sn_dg_sdk_method_deliveryService_deleteNotification_start_0
			asynchReply =
				connection.getDeliveryService().deleteNotification(new SearchPathSingleObject(reportPath));
			// sn_dg_sdk_method_deliveryService_deleteNotification_end_0

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

			return "Delete notification request complete.";

		}
		catch (Exception e)
		{
			//System.out.println("An error occurred in the deleteSingleNotification Java method.");
			//e.printStackTrace();
			return "Error. You may not be on the alert list for this report.";
		}

	}

	/**
	* Delete the current user all alert lists.
	*
	* @param connection
	* 		  Connection to Server
	*/
	public String deleteAllNotifications(CRNConnect connection)
	{
		AsynchRequest response = null;
		AsynchReply asynchReply = null;

		try
		{

			// sn_dg_sdk_method_deliveryService_deleteAllNotifications_start_0
			asynchReply =
				connection.getDeliveryService().deleteAllNotifications();
			// sn_dg_sdk_method_deliveryService_deleteAllNotifications_end_0

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

			return "Delete all notifications request complete";

		}
		catch (Exception e)
		{
			//System.out.println("An error occurred in the deleteAllNotifications Java method.");
			//e.printStackTrace();
			return "An error occurred in the deleteAllNotifications Java method.";
		}

	}

	/**
	* Remove all users from the alert list for a report.
	*
	* @param connection
	* 		  Connection to Server
	* @param csobject
	*        Specifies the report. 
	*/
	public String clearNotifications(CRNConnect connection, BaseClassWrapper csobject)
	{
		AsynchRequest response = null;
		AsynchReply asynchReply = null;
		String reportPath = csobject.getBaseClassObject().getSearchPath().getValue();

		if (csobject == null)
		{
			return "No valid report selected";
		}

		try
		{

			// sn_dg_sdk_method_deliveryService_clearNotifications_start_0
			asynchReply =
				connection.getDeliveryService().clearNotifications(new SearchPathSingleObject(reportPath));
			// sn_dg_sdk_method_deliveryService_clearNotifications_end_0

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

			return "Clear notifications request complete.";

		}
		catch (Exception e)
		{
			//System.out.println("An error occurred in the clearNotifications Java method.");
			//e.printStackTrace();
			return "An error occurred in the clearNotifications Java method.";
		}

	}

	/**
	* Query whether the current user is on the alert list for a report.
	*
	* @param connection
	* 		  Connection to Server
	* @param csobject
	*        Specifies the report. 
	*/
	public String queryNotification(CRNConnect connection, BaseClassWrapper csobject)
	{
		AsynchReply asynchReply = null;
		String reportPath = csobject.getBaseClassObject().getSearchPath().getValue();
		String unknownResult = new String("No details returned.");
				
		if (csobject == null)
		{
			return "No valid report selected";
		}

		try
		{
			// sn_dg_sdk_method_deliveryService_queryNotification_start_0
			asynchReply =
				connection.getDeliveryService().queryNotification(new SearchPathSingleObject(reportPath));
			// sn_dg_sdk_method_deliveryService_queryNotification_end_0

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
				
			if (asynchReply != null)
			{
				for (int i = 0; i < asynchReply.getDetails().length; i++)
				{
					if ((asynchReply.getDetails()[i] instanceof AsynchDetailAgentNotificationStatus))
					{
						AgentNotificationStatusEnum currentStatus =((AsynchDetailAgentNotificationStatus)asynchReply.getDetails()[i]).getStatus();
						if (currentStatus == AgentNotificationStatusEnum.disabled)
						{
							return "Alert lists are not enabled for this report.";
						}
						else if (currentStatus == AgentNotificationStatusEnum.off)
						{
							return "The current user is not on the alert list for this report.";
						}
						else if (currentStatus == AgentNotificationStatusEnum.on)
						{
							return "The current user is on the alert list for this report.";
						}
						else
						{
							return "Unrecognized alert status returned.";
						}
					}
				}
				return "Query notification request returned no alert status information.";
			}
		}
		catch (Exception e)
		{
			//System.out.println("An error occurred in the queryNotification Java method.");
			//e.printStackTrace();
			return "An error occurred in the queryNotification Java method.";
		}
		
		return unknownResult;
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
