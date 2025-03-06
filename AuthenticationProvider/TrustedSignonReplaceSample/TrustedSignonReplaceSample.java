/**
 * Licensed Materials - Property of IBM
 * 
 * IBM Cognos Products: CAMAAA
 * 
 * (C) Copyright IBM Corp. 2005, 2012
 * 
 * US Government Users Restricted Rights - Use, duplication or disclosure restricted by GSA ADP Schedule Contract with
 * IBM Corp.
 */

import com.cognos.CAM_AAA.authentication.INamespaceTrustedSignonProvider;
import com.cognos.CAM_AAA.authentication.ITrustedSignonRequest;
import com.cognos.CAM_AAA.authentication.SystemRecoverableException;
import com.cognos.CAM_AAA.authentication.UnrecoverableException;
import com.cognos.CAM_AAA.authentication.UserRecoverableException;


public class TrustedSignonReplaceSample
	extends Namespace
	implements INamespaceTrustedSignonProvider
{

	public TrustedSignonReplaceSample()
	{
		super();
	}

	public void processLogonRequest(ITrustedSignonRequest theRequest)
		throws
			UserRecoverableException,
			SystemRecoverableException,
			UnrecoverableException
	{

		String[] username = null;
		//	1 - Look for trusted credentials
		username = theRequest.getTrustedEnvVarValue("REMOTE_USER");
		
		if (username == null)
		{
			String[] theRequestedVars = new String[] {"REMOTE_USER"};
			
			SystemRecoverableException e = new SystemRecoverableException(
					"Requesting trusted REMOTE_USER.",
					theRequestedVars);
			throw e;
		}
		int slashPosition = username[0].indexOf('\\');
		
		if (slashPosition > 0)
		{
			username[0] = username[0].substring(slashPosition+1);
		}

		// The namespace ID of the authentication namespace to use.  For the purpose of this sample, it is hardcoded.
		theRequest.setNamespaceID( "TS" );

		//
		// Set the trusted environment variable REMOTE_USER to achieve SSO against the TS namespace.
		//
		theRequest.removeTrustedEnvVar( "REMOTE_USER" );
		theRequest.addTrustedEnvVar( "REMOTE_USER", username[0] );

	}
}
