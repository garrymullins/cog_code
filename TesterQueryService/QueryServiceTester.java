/** 
Licensed Materials - Property of IBM

IBM Cognos Products: DOCS

(C) Copyright IBM Corp. 2011

US Government Users Restricted Rights - Use, duplication or disclosure restricted by GSA ADP Schedule Contract with
IBM Corp.
*/
/**
 * QueryServiceTester.java
 *
*/
import java.rmi.RemoteException;

import com.cognos.developer.schemas.bibus._3.AsynchDetailROLAPDataSourceState;
import com.cognos.developer.schemas.bibus._3.AsynchDetailMessages;
import com.cognos.developer.schemas.bibus._3.AsynchReply;
import com.cognos.developer.schemas.bibus._3.AsynchReplyStatusEnum;
import com.cognos.developer.schemas.bibus._3.AsynchSecondaryRequest;
import com.cognos.developer.schemas.bibus._3.BaseClass;
import com.cognos.developer.schemas.bibus._3.FaultDetail;
import com.cognos.developer.schemas.bibus._3.GenericOptionBoolean;
import com.cognos.developer.schemas.bibus._3.Option;
import com.cognos.developer.schemas.bibus._3.OrderEnum;
import com.cognos.developer.schemas.bibus._3.ParameterValue;
import com.cognos.developer.schemas.bibus._3.PropEnum;
import com.cognos.developer.schemas.bibus._3.QueryOptions;
import com.cognos.developer.schemas.bibus._3.QueryTaskOptionEnum;
import com.cognos.developer.schemas.bibus._3.SearchPathMultipleObject;
import com.cognos.developer.schemas.bibus._3.Sort;


public class QueryServiceTester {

	static String queryTaskOptionEnum_stopROLAPCubesImmediately =
		"http://developer.cognos.com/ceba/constants/queryTaskOptionEnum#stopROLAPCubesImmediately";

	// get available Cubes according to the specified search path
	public BaseClassWrapper[] getCubes(CRNConnect Con,
			String searchPath) {
		BaseClassWrapper[] cubeList = null;
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
				cubeList = new BaseClassWrapper[results.length];

				for (int i = 0; i < results.length; i++) {
					cubeList[i] = new BaseClassWrapper(results[i]);
				}
			}

		} catch (RemoteException remoteEx) {
			System.out.println("The request threw an RMI exception:");
			System.out.println(remoteEx.getMessage());
			System.out.println("Stack trace:");
			remoteEx.printStackTrace();
			return null;
		}

		return cubeList;
	}

	// Retrieve the current state for one cube
	public String getCubeState(CRNConnect Con, String cubeName) {
		

		String cubeStateRequestResult = null;
		String[] cubeNames = {cubeName};

		try {

			ParameterValue[] parameters = new ParameterValue[0];
			Option[] options = new Option[0];
			
			AsynchReply getStateResult = Con.getQueryService().getCubeState(cubeNames, parameters, options);
			
			// If response is not immediately complete, call wait until complete
			if (!getStateResult.getStatus().equals(AsynchReplyStatusEnum.conversationComplete))
			{
				while (!getStateResult.getStatus().equals(AsynchReplyStatusEnum.conversationComplete))
								{
					//before calling wait, double check that it is okay
					if (hasSecondaryRequest(getStateResult, "wait"))
					{
						getStateResult =
						Con.getQueryService().wait(
								getStateResult.getPrimaryRequest(),
								new ParameterValue[] {},
								new Option[] {});
					}
					else
					{
						return "Error: Wait method not available as expected.";
					}
				}
			}
			

			if (getStateResult != null) {
				
				cubeStateRequestResult = "Query returned no state information for the requested cube.";
				
				for (int i = 0; i < getStateResult.getDetails().length; i++)
				{
					if ((getStateResult.getDetails()[i] instanceof AsynchDetailROLAPDataSourceState))
					{
						cubeStateRequestResult =((AsynchDetailROLAPDataSourceState)getStateResult.getDetails()[i]).getState();
					}
				}
				
			}

		
		} catch (RemoteException re) {
			System.out
					.println("Error getting cube state: "
							+ re.getMessage());
			
			cubeStateRequestResult = "Error running getCubeState.";
		}
		
		return cubeStateRequestResult;
		
		
	}

	// Start a cube
	public String startSingleCube(CRNConnect Con, String cubeName) {

		String requestResult = null;
		
		String[] cubeNames = {cubeName};

		try {

			ParameterValue[] parameters = new ParameterValue[0];
			Option[] options = new Option[0];
			
			AsynchReply result = Con.getQueryService().startCubes(cubeNames, parameters, options);
			
			// If response is not immediately complete, call wait until complete
			if (!result.getStatus().equals(AsynchReplyStatusEnum.conversationComplete))
			{
				while (!result.getStatus().equals(AsynchReplyStatusEnum.conversationComplete))
								{
					//before calling wait, double check that it is okay
					if (hasSecondaryRequest(result, "wait"))
					{
						result =
						Con.getQueryService().wait(
								result.getPrimaryRequest(),
								new ParameterValue[] {},
								new Option[] {});
					}
					else
					{
						return "Error: Wait method not available as expected.";
					}
				}
			}
			

			if (result != null) {
				
				requestResult = "No messages in response to stop request.";
				
				for (int i = 0; i < result.getDetails().length; i++)
				{
					if ((result.getDetails()[i] instanceof AsynchDetailMessages))
					{
						FaultDetail[] faultMessages = ((AsynchDetailMessages)result.getDetails()[i]).getMessages();
						
						requestResult = "Response from server: ";
							
						for (int j = 0; j < faultMessages.length; j++)
						{
							requestResult = requestResult + "\n\n" +faultMessages[j].getMessage()[0].getMessage();
						}
							
					}
				}
				
			}

		
		} catch (RemoteException re) {
			System.out
					.println("Error stopping cube: "
							+ re.getMessage());
			
			requestResult = "Error running stopCubes.";
		}
		
		return requestResult;
	}

	// stop a cube - if forceStop true, stop immediately, otherwise wait until cube is not in use
	public String stopSingleCube(CRNConnect Con, String cubeName, boolean forceStop) {

		String cubeStopRequestResult = null;
		
		String[] cubeNames = {cubeName};
		
		ParameterValue[] parameters = new ParameterValue[0];
		
		Option[] options = new Option[1];
	
		GenericOptionBoolean immediateStop = new GenericOptionBoolean();
		
		immediateStop.setName(queryTaskOptionEnum_stopROLAPCubesImmediately);
		
		immediateStop.setValue(forceStop);
		
		options[0] = immediateStop;
		

		try {

			AsynchReply result = Con.getQueryService().stopCubes(cubeNames, parameters, options);
			
			// If response is not immediately complete, call wait until complete
			if (!result.getStatus().equals(AsynchReplyStatusEnum.conversationComplete))
			{
				while (!result.getStatus().equals(AsynchReplyStatusEnum.conversationComplete))
								{
					//before calling wait, double check that it is okay
					if (hasSecondaryRequest(result, "wait"))
					{
						result =
						Con.getQueryService().wait(
								result.getPrimaryRequest(),
								new ParameterValue[] {},
								new Option[] {});
					}
					else
					{
						return "Error: Wait method not available as expected.";
					}
				}
			}
			

			if (result != null) {
				
				cubeStopRequestResult = "No messages in response to stop request.";
				
				for (int i = 0; i < result.getDetails().length; i++)
				{
					if ((result.getDetails()[i] instanceof AsynchDetailMessages))
					{
						FaultDetail[] faultMessages = ((AsynchDetailMessages)result.getDetails()[i]).getMessages();
						
						cubeStopRequestResult = "Response from server: ";
							
						for (int j = 0; j < faultMessages.length; j++)
						{
							cubeStopRequestResult = cubeStopRequestResult + "\n\n" +faultMessages[j].getMessage()[0].getMessage();
						}
							
					}
				}
				
			}

		
		} catch (RemoteException re) {
			System.out
					.println("Error stopping cube: "
							+ re.getMessage());
			
			cubeStopRequestResult = "Error running stopCubes.";
		}
		
		//return "Stop Cube Request";
		return cubeStopRequestResult;
	}

	// send request to restart cube
	public String restartSingleCube(CRNConnect Con, String cubeName) {

		String requestResult = null;
		
		String[] cubeNames = {cubeName};

		try {

			ParameterValue[] parameters = new ParameterValue[0];
			Option[] options = new Option[0];
			
			AsynchReply result = Con.getQueryService().restartCubes(cubeNames, parameters, options);
			
			// If response is not immediately complete, call wait until complete
			if (!result.getStatus().equals(AsynchReplyStatusEnum.conversationComplete))
			{
				while (!result.getStatus().equals(AsynchReplyStatusEnum.conversationComplete))
								{
					//before calling wait, double check that it is okay
					if (hasSecondaryRequest(result, "wait"))
					{
						result =
						Con.getQueryService().wait(
								result.getPrimaryRequest(),
								new ParameterValue[] {},
								new Option[] {});
					}
					else
					{
						return "Error: Wait method not available as expected.";
					}
				}
			}
			

			if (result != null) {
				
				requestResult = "No messages in response to restart request.";
				
				for (int i = 0; i < result.getDetails().length; i++)
				{
					if ((result.getDetails()[i] instanceof AsynchDetailMessages))
					{
						FaultDetail[] faultMessages = ((AsynchDetailMessages)result.getDetails()[i]).getMessages();
						
						requestResult = "Response from server: ";
							
						for (int j = 0; j < faultMessages.length; j++)
						{
							requestResult = requestResult + "\n\n" +faultMessages[j].getMessage()[0].getMessage();
						}
							
					}
				}
				
			}

		
		} catch (RemoteException re) {
			System.out
					.println("Error restarting cube: "
							+ re.getMessage());
			
			requestResult = "Error running restartCubes.";
		}
		
		return requestResult;
	}

	// request refresh of the data cache for a specific cube
	public String refreshDataCache(CRNConnect Con, String cubeName) {

		String requestResult = null;
		
		String[] cubeNames = {cubeName};

		try {

			ParameterValue[] parameters = new ParameterValue[0];
			Option[] options = new Option[0];
			
			AsynchReply result = Con.getQueryService().refreshCubeDataCache(cubeNames, parameters, options);
			
			// If response is not immediately complete, call wait until complete
			if (!result.getStatus().equals(AsynchReplyStatusEnum.conversationComplete))
			{
				while (!result.getStatus().equals(AsynchReplyStatusEnum.conversationComplete))
								{
					//before calling wait, double check that it is okay
					if (hasSecondaryRequest(result, "wait"))
					{
						result =
						Con.getQueryService().wait(
								result.getPrimaryRequest(),
								new ParameterValue[] {},
								new Option[] {});
					}
					else
					{
						return "Error: Wait method not available as expected.";
					}
				}
			}
			

			if (result != null) {
				
				requestResult = "No messages in response to refresh data cache request.";
				
				for (int i = 0; i < result.getDetails().length; i++)
				{
					if ((result.getDetails()[i] instanceof AsynchDetailMessages))
					{
						FaultDetail[] faultMessages = ((AsynchDetailMessages)result.getDetails()[i]).getMessages();
						
						requestResult = "Response from server: ";
							
						for (int j = 0; j < faultMessages.length; j++)
						{
							requestResult = requestResult + "\n\n" +faultMessages[j].getMessage()[0].getMessage();
						}
							
					}
				}
				
			}

		
		} catch (RemoteException re) {
			System.out
					.println("Error refreshing data cache: "
							+ re.getMessage());
			
			requestResult = "Error running refreshCubeDataCache.";
		}
		
		return requestResult;
	}

	// request refresh of the member cache for a specific cube
	public String refreshMemberCache(CRNConnect Con, String cubeName) {

		String requestResult = null;
		
		String[] cubeNames = {cubeName};

		try {

			ParameterValue[] parameters = new ParameterValue[0];
			Option[] options = new Option[0];
			
			AsynchReply result = Con.getQueryService().refreshCubeMemberCache(cubeNames, parameters, options);
			
			// If response is not immediately complete, call wait until complete
			if (!result.getStatus().equals(AsynchReplyStatusEnum.conversationComplete))
			{
				while (!result.getStatus().equals(AsynchReplyStatusEnum.conversationComplete))
								{
					//before calling wait, double check that it is okay
					if (hasSecondaryRequest(result, "wait"))
					{
						result =
						Con.getQueryService().wait(
								result.getPrimaryRequest(),
								new ParameterValue[] {},
								new Option[] {});
					}
					else
					{
						return "Error: Wait method not available as expected.";
					}
				}
			}
			

			if (result != null) {
				
				requestResult = "No messages in response to refresh member cache request.";
				
				for (int i = 0; i < result.getDetails().length; i++)
				{
					if ((result.getDetails()[i] instanceof AsynchDetailMessages))
					{
						FaultDetail[] faultMessages = ((AsynchDetailMessages)result.getDetails()[i]).getMessages();
						
						requestResult = "Response from server: ";
							
						for (int j = 0; j < faultMessages.length; j++)
						{
							requestResult = requestResult + "\n\n" +faultMessages[j].getMessage()[0].getMessage();
						}
							
					}
				}
				
			}

		
		} catch (RemoteException re) {
			System.out
					.println("Error refreshing cube member cache: "
							+ re.getMessage());
			
			requestResult = "Error running refreshCubeMemberCache.";
		}
		
		return requestResult;
	}

	// request refresh of security for a specific cube
	public String refreshCubeSecurity(CRNConnect Con, String cubeName) {

		String requestResult = null;
		
		String[] cubeNames = {cubeName};

		try {

			ParameterValue[] parameters = new ParameterValue[0];
			Option[] options = new Option[0];
			
			AsynchReply result = Con.getQueryService().refreshCubeSecurity(cubeNames, parameters, options);
			
			// If response is not immediately complete, call wait until complete
			if (!result.getStatus().equals(AsynchReplyStatusEnum.conversationComplete))
			{
				while (!result.getStatus().equals(AsynchReplyStatusEnum.conversationComplete))
								{
					//before calling wait, double check that it is okay
					if (hasSecondaryRequest(result, "wait"))
					{
						result =
						Con.getQueryService().wait(
								result.getPrimaryRequest(),
								new ParameterValue[] {},
								new Option[] {});
					}
					else
					{
						return "Error: Wait method not available as expected.";
					}
				}
			}
			

			if (result != null) {
				
				requestResult = "No messages in response to security refresh request.";
				
				for (int i = 0; i < result.getDetails().length; i++)
				{
					if ((result.getDetails()[i] instanceof AsynchDetailMessages))
					{
						FaultDetail[] faultMessages = ((AsynchDetailMessages)result.getDetails()[i]).getMessages();
						
						requestResult = "Response from server: ";
							
						for (int j = 0; j < faultMessages.length; j++)
						{
							requestResult = requestResult + "\n\n" +faultMessages[j].getMessage()[0].getMessage();
						}
							
					}
				}
				
			}

		
		} catch (RemoteException re) {
			System.out
					.println("Error refreshing cube security: "
							+ re.getMessage());
			
			requestResult = "Error running refreshCubeSecurity.";
		}
		
		return requestResult;
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
