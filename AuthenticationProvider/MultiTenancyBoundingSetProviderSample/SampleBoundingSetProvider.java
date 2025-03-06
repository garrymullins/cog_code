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
import com.cognos.CAM_AAA.authentication.IBoundingSetProvider;
import com.cognos.CAM_AAA.authentication.UnrecoverableException;

/**
 * Sample implementation of the {@link IBoundingSetProvider} interface. The user to tenant mapping is defined in a properties file.
 */
public class SampleBoundingSetProvider implements IBoundingSetProvider
{

	private static final String USERS_TO_BOUNDING_SETS_FILE_PROPERTY_NAME = "usersToBoundingSetsFile";
	private static final String INITIALIZATION_EXCEPTION_CAPTION = "Error initializing bounding set provider.";

	private final Properties usersToBoundingSets;

	public SampleBoundingSetProvider()
	{
		this.usersToBoundingSets = new Properties();
	}

	public void destroy()
	{

	}

	@Override
	public String[] getBoundingSet(final IAccount account) throws UnrecoverableException
	{
		final String userName = account.getUserName();
		final String BoundingSet = this.usersToBoundingSets.getProperty(userName);

		if (BoundingSet == null)
       		     throw new UnrecoverableException("Unknown user", "User '" + userName + "' does not belong to any bounding set.");

	        final String[] BoundingSetArray = BoundingSet.split(",");

	
       		return BoundingSetArray;
	}

	public void init(final Map< String, String > advancedConfigurations, final String namespaceId) throws UnrecoverableException
	{
		final String fileLocation = advancedConfigurations.get(SampleBoundingSetProvider.USERS_TO_BOUNDING_SETS_FILE_PROPERTY_NAME);
		if (fileLocation == null)
			throw new UnrecoverableException(SampleBoundingSetProvider.INITIALIZATION_EXCEPTION_CAPTION, "The advanced property '"
					+ SampleBoundingSetProvider.USERS_TO_BOUNDING_SETS_FILE_PROPERTY_NAME + "' must be set.");

		final InputStream inStream = this.getClass().getResourceAsStream(fileLocation);
		if (inStream == null)
			throw new UnrecoverableException(SampleBoundingSetProvider.INITIALIZATION_EXCEPTION_CAPTION,
					"Unable to locate user to bounding sets mapping file '" +  fileLocation + "'");

		try
		{
			//
			// Java Properties files only support ISO 8859-1 character encoding, therefore user names or tenant IDs of certain
			// locales are not supported by the sample. They are supported in the product.
			//
			this.usersToBoundingSets.load(inStream);
		}
		catch (final IOException ioe)
		{
			throw new UnrecoverableException(SampleBoundingSetProvider.INITIALIZATION_EXCEPTION_CAPTION,
					"Unable to load user to bounding sets mapping.");
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
}
