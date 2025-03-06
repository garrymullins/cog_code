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


public class TrustedSignonSample
	extends Namespace
	implements INamespaceTrustedSignonProvider
{

	public TrustedSignonSample()
	{
		super();
	}

	public void processLogonRequest(ITrustedSignonRequest theRequest)
		throws
			UserRecoverableException,
			SystemRecoverableException,
			UnrecoverableException
	{
		String cookieValue[] = null;

		cookieValue = theRequest.getCookieValue( "TRUSTED_SIGNON_USER" );
		if( cookieValue == null )
		{
			throw new UnrecoverableException( "Authentication error", "The user is not authenticated or the cookie TRUSTED_SIGNON_USER is not set"); 
		}
		
		// The namespace ID of the authentication namespace to use.  For the purpose of this sample, it is hardcoded.
		theRequest.setNamespaceID( "TS" );

		//
		// Set the trusted environment variable REMOTE_USER to achieve SSO against the TS namespace.
		//
		theRequest.removeTrustedEnvVar( "REMOTE_USER" );
		theRequest.addTrustedEnvVar( "REMOTE_USER", cookieValue[0] );

		//
		//	The following are required because the NTLM provider triggers on those to enable NTCR.
		//
		theRequest.removeEnvVar( "AUTH_TYPE" );
		theRequest.addEnvVar( "AUTH_TYPE", "NTLM" );
		theRequest.removeEnvVar( "REMOTE_USER" );
		theRequest.addEnvVar( "REMOTE_USER", cookieValue[0] );
	}
}
