/** 
Licensed Materials - Property of IBM

IBM Cognos Products: DOCS

(C) Copyright IBM Corp. 2005, 2008

US Government Users Restricted Rights - Use, duplication or disclosure restricted by GSA ADP Schedule Contract with
IBM Corp.
*/
/**
 * DeleteReport.java
 *
 * Copyright (C) 2008 Cognos ULC, an IBM Company. All rights reserved.
 * Cognos (R) is a trademark of Cognos ULC, (formerly Cognos Incorporated).
 *
 * Description: This code sample demonstrates how to delete reports using the 
 *	           following methods:
 *	           - query 
 *	             Use this method to request objects from Content Manager.
 *	           - delete
 *	             Use this method to delete objects from the content store.
 */

import com.cognos.developer.schemas.bibus._3.BaseClass;
import com.cognos.developer.schemas.bibus._3.DeleteOptions;

public class DeleteReport
{
	/**
		* This Java method returns a string that contains either the 
		* information about the specified objects if the request succeeded 
		* or an error message if the request failed.
		*
		* @param connection
		*        Specifies the object that provides the connection to 
		*        the service.
		* @param reportToBeDeleted
		*        Specifies the search path of the report.     
		*
		* @return output
		*        Returns a message that indicates whether the request 
		*        succeeded or failed. 
		*/
	public String deleteReport(
		CRNConnect connection,
		BaseClassWrapper reportToBeDeleted)
	{
		String output = new String();
		if (connection != null)
		{
		
			// Set the options for the delete method.                       
			DeleteOptions delOptions = new DeleteOptions();

			// Set the force option to true. When the force option is true,
			// a selected object will be deleted if the current user has either 
			// write or setPolicy permission for the following: 
			//   - the selected object
			//   - the parent of the selected object
			//   - every descendant of the selected object
			
			// sn_dg_prm_smpl_deletereport_start_0
			delOptions.setForce(true);
			delOptions.setFaultIfObjectReferenced(false);
			delOptions.setRecursive(true);

			try
			{
				if (reportToBeDeleted != null)
				{
					System.out.println("Deleting report: " + reportToBeDeleted);

					BaseClass reportsForDeletion[] =
						new BaseClass[] { reportToBeDeleted.getBaseClassObject()};
					int delReturnCode =
						connection.getCMService().delete(reportsForDeletion, delOptions);
					// sn_dg_prm_smpl_deletereport_end_0
					if (delReturnCode > 0)
					{
						output = "The report was deleted successfully.\n";
					}
					else
					{
						output =
							"An error occurred while deleting the report.\n";
					}
				}
			}
			//catch unhandled exceptions
			catch (java.rmi.RemoteException remoteEx)
			{
				remoteEx.printStackTrace();
			}

		}

		return output;
	}

}
