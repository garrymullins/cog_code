/**
 * IBM Confidential
 * 
 * OCO Source Materials
 * 
 * IBM Cognos Products: CAMAAA
 * 
 * (C) Copyright IBM Corp. 2011, 2012
 * 
 * The source code for this program is not published or otherwise divested of its trade secrets, irrespective of what has been
 * deposited with the U.S. Copyright Office.
 */

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

import com.cognos.CAM_AAA.authentication.IAccount;
import com.cognos.CAM_AAA.authentication.ITenantProvider;
import com.cognos.CAM_AAA.authentication.UnrecoverableException;

/**
 * Sample implementation of the {@link ITenantProvider} interface. The user to tenant mapping is defined in a properties file.
 */
public class SampleTenantProvider implements ITenantProvider
{

	private static final String USERS_TO_TENANTS_FILE_PROPERTY_NAME = "usersToTenantsFile";
	private static final String INITIALIZATION_EXCEPTION_CAPTION = "Error initializing tenant provider.";

	private final Properties usersToTenants;

	public SampleTenantProvider()
	{
		this.usersToTenants = new Properties();
	}

	public void destroy()
	{

	}

	@Override
	public String getTenantId(final IAccount account) throws UnrecoverableException
	{
		final String userName = account.getUserName();
		final String tenantId = this.usersToTenants.getProperty(userName);

		if (tenantId == null)
			throw new UnrecoverableException("Unknown user", "User '" + userName + "' does not belong to any tenant.");

		if (this.isTenantDisabled(tenantId))
			throw new UnrecoverableException("Tenant disabled", "Tenant '" + tenantId + "' does not have access to the system.");
		return tenantId;
	}

	public void init(final Map< String, String > advancedConfigurations, final String namespaceId) throws UnrecoverableException
	{
		final String fileLocation = advancedConfigurations.get(SampleTenantProvider.USERS_TO_TENANTS_FILE_PROPERTY_NAME);
		if (fileLocation == null)
			throw new UnrecoverableException(SampleTenantProvider.INITIALIZATION_EXCEPTION_CAPTION, "The advanced property '"
					+ SampleTenantProvider.USERS_TO_TENANTS_FILE_PROPERTY_NAME + "' must be set.");

		final InputStream inStream = this.getClass().getResourceAsStream(fileLocation);
		if (inStream == null)
			throw new UnrecoverableException(SampleTenantProvider.INITIALIZATION_EXCEPTION_CAPTION,
					"Unable to locate user to tenants mapping file '" +  fileLocation + "'");

		try
		{
			//
			// Java Properties files only support ISO 8859-1 character encoding, therefore user names or tenant IDs of certain
			// locales are not supported by the sample. They are supported in the product.
			//
			this.usersToTenants.load(inStream);
		}
		catch (final IOException ioe)
		{
			throw new UnrecoverableException(SampleTenantProvider.INITIALIZATION_EXCEPTION_CAPTION,
					"Unable to load user to tenant mapping.");
		}
		finally
		{
			try
			{
				inStream.close();
			}
			catch (final IOException ioe)
			{
				// ignore it
			}
		}
	}

	private boolean isTenantDisabled(final String tenantID)
	{
		//
		// Hard-coded to TenantB, however this could be extended to query an external source to determine if a tenant has been
		// disabled.
		//
		if ("TenantB".equals(tenantID))
			return true;
		return false;
	}

}
