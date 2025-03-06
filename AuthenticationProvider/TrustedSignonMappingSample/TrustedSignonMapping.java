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

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import com.cognos.CAM_AAA.authentication.INamespaceConfiguration;
import com.cognos.CAM_AAA.authentication.INamespaceTrustedSignonProvider;
import com.cognos.CAM_AAA.authentication.ITrustedSignonRequest;
import com.cognos.CAM_AAA.authentication.SystemRecoverableException;
import com.cognos.CAM_AAA.authentication.UnrecoverableException;
import com.cognos.CAM_AAA.authentication.UserRecoverableException;

public class TrustedSignonMapping
	extends Namespace
	implements INamespaceTrustedSignonProvider
{
	private static String CONFIG_FILE = "domainMapping.xml";
	private static String DOMAIN_SPLIT_DELIMITER = "domainSplitDelimiter";
	private static String DOMAIN_SPLIT_POSITION = "domainSplitPosition";
	private static String REMOTE_USER_VALUE_SPLIT = "remoteUserSplitPosition";
	
	private static String BEFORE = "before";
	private static String AFTER = "after";
	private HashMap <String, String>domainMapping;
	private String domainSplitDelimiter;
	private String domainSplitPosition;
	private String remoteUserSplitPosition;
	private String passedRemoteUserName;

	public TrustedSignonMapping()
	{
		super();
		// read the mapping of the domain information
		this.domainMapping =  new  HashMap <String, String> ();
		
	}
	
	@Override
	public void init(final INamespaceConfiguration theNamespaceConfiguration) throws UnrecoverableException
	{
		super.init(theNamespaceConfiguration);

		//
		// Read our configuration from this IBM Cognos 8 install's
		// configuration directory.
		//
		final String configPath = theNamespaceConfiguration.getInstallLocation() + "/configuration";
		final String configFile = configPath + "/" + TrustedSignonMapping.CONFIG_FILE;
		this.readConfigInfo(configFile);
	}
	
	private void readConfigInfo(final String configFile )
	{
		try
		{
			  // Open the configuration file 
			  FileInputStream fstream = new FileInputStream(configFile);
			  // Get the object of DataInputStream
			  DataInputStream in = new DataInputStream(fstream);
			  BufferedReader br = new BufferedReader(new InputStreamReader(in));
			
			  String strLine;
			  //Read File Line By Line
			  while ((strLine = br.readLine()) != null)
			  {
				// Print the content on the console
				// the configuration format is like: domain=namespaceID
				  
				// System.out.println("----Read configure: " + strLine + "---");
				
		  		final String[] splitObject = strLine.split("=");
				
				final String domainName  = splitObject[0];
				final String namespaceID = splitObject[1].replaceAll("\\p{Cntrl}", "");

				
				if ( domainName.equalsIgnoreCase(TrustedSignonMapping.DOMAIN_SPLIT_DELIMITER))
					this.domainSplitDelimiter = namespaceID;
				else if ( domainName.equalsIgnoreCase(TrustedSignonMapping.DOMAIN_SPLIT_POSITION))
					this.domainSplitPosition = namespaceID;
				else if ( domainName.equalsIgnoreCase(TrustedSignonMapping.REMOTE_USER_VALUE_SPLIT))
					this.remoteUserSplitPosition = namespaceID;
				else
					// since namespaceID is case sensitive, so we don't normalize it.
					// but we do normalize the domain name
					this.domainMapping.put(domainName.toUpperCase(), namespaceID);
				// System.out.println("domain: " + domainName + " namespace: " + namespaceID);
				
		  }
			  //Close the input stream
			  in.close();
		}
		catch (Exception e)
		{//Catch exception if any
			System.err.println("Error: " + e.getMessage());
		}
		
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
	
		// extract the domain information
		String domainName = this.extractUserDomainInformation(username);
		
		// System.out.println("--Extrated domain is: " + domainName + " from username: " + username[0]);
		String namespaceID = null;
		if ( domainName != null )
		{
			namespaceID = this.mapToNamespaceID(domainName);
		}
		// Map the namespace information
		// The namespace ID of the authentication namespace to use.  For the purpose of this sample, it is hardcoded.
		
		if ( namespaceID == null )
		{
			// Sorry, we need to know the namespaceID to be able to redirect to the the right namespace.
			UnrecoverableException e = new UnrecoverableException("Could not redirect to the specific namespace", "Missing domain information. Please check the system setting");
			throw e;	 
		}
		if ( namespaceID != null )
			theRequest.setNamespaceID( namespaceID );
	
		
		//
		// Set the trusted environment variable REMOTE_USER to achieve SSO against the TS namespace.
		//
		theRequest.removeTrustedEnvVar( "REMOTE_USER" );
		
		theRequest.addTrustedEnvVar("REMOTE_USER", this.passedRemoteUserName);
		
		
		// System.out.println("Set namespace: " + namespaceID);
		// System.out.println("Set Remote_user: " + this.passedRemoteUserName);
		
	}
	
	private String extractUserDomainInformation(final String[] username )
	{
		String userDomain = null;
		this.passedRemoteUserName = username[0];
	
		if ( this.domainSplitDelimiter != null )
		{
			int slashPosition = username[0].indexOf(this.domainSplitDelimiter);
			if (slashPosition > 0)
			{
				userDomain = this.splitValueBasedOnPosition(username, slashPosition, this.domainSplitPosition);
				
				
				if ( this.remoteUserSplitPosition != null)
				{
					this.passedRemoteUserName = this.splitValueBasedOnPosition(username, slashPosition, this.remoteUserSplitPosition);
			
				}
				else
					this.passedRemoteUserName = username[0];
			}
		}
		return userDomain;
	}

	private String splitValueBasedOnPosition(final String[] username, final int slashPosition, final String  splitPosition)
	{
		String userDomain;
		if ( splitPosition.equalsIgnoreCase(TrustedSignonMapping.BEFORE))
			userDomain = username[0].substring(0, slashPosition);
		else if ( splitPosition.equalsIgnoreCase(TrustedSignonMapping.AFTER))
			userDomain = username[0].substring(slashPosition+1);
		else
			userDomain = username[0];
		return userDomain;
	}
	
	
	private final String mapToNamespaceID(final String domainName)
	{
		final String  namespaceID = (String) this.domainMapping.get(domainName.toUpperCase());
		return namespaceID;
	}
}
