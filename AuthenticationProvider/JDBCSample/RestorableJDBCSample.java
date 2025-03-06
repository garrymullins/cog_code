/**
 * Licensed Materials - Property of IBM
 * 
 * IBM Cognos Products: CAMAAA
 * 
 * (C) Copyright IBM Corp. 2012
 * 
 * US Government Users Restricted Rights - Use, duplication or disclosure restricted by GSA ADP Schedule Contract with IBM Corp.
 */

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.sql.SQLException;
import java.util.Locale;

import com.cognos.CAM_AAA.authentication.IBiBusHeader;
import com.cognos.CAM_AAA.authentication.IBiBusHeader2;
import com.cognos.CAM_AAA.authentication.INamespaceAuthenticationProvider2;
import com.cognos.CAM_AAA.authentication.INamespaceConfiguration;
import com.cognos.CAM_AAA.authentication.IQuery;
import com.cognos.CAM_AAA.authentication.IQueryResult;
import com.cognos.CAM_AAA.authentication.ISearchExpression;
import com.cognos.CAM_AAA.authentication.ISearchFilter;
import com.cognos.CAM_AAA.authentication.ISearchStep;
import com.cognos.CAM_AAA.authentication.ISearchStep.SearchAxis;
import com.cognos.CAM_AAA.authentication.IVisa;
import com.cognos.CAM_AAA.authentication.QueryResult;
import com.cognos.CAM_AAA.authentication.ReadOnlyDisplayObject;
import com.cognos.CAM_AAA.authentication.SystemRecoverableException;
import com.cognos.CAM_AAA.authentication.TextDisplayObject;
import com.cognos.CAM_AAA.authentication.TextNoEchoDisplayObject;
import com.cognos.CAM_AAA.authentication.UnrecoverableException;
import com.cognos.CAM_AAA.authentication.UserRecoverableException;

/**
 * This is a modified version of the JDBCSample that demonstrates how to implement a custom namespace that can 
 * handle failover of the user session.
 * 
 * Note that IBM Cognos must be properly configured to support authentication failover for this to work.
 * 
 * @See {@link JDBCSample}
 *
 */
public class RestorableJDBCSample extends Namespace implements INamespaceAuthenticationProvider2
{

	/**
	 * The logon method is virtually identical to {@link JDBCSample#logon(IBiBusHeader2)
	 * 
	 * The main difference is that now we need to use a RestorableJDBCVisa instead of a JDBCVisa.
	 * @see INamespaceAuthenticationProvider2#logon(IBiBusHeader2)
	 */
	public IVisa logon(IBiBusHeader2 theAuthRequest) throws UserRecoverableException, SystemRecoverableException,
			UnrecoverableException
	{
		RestorableJDBCVisa visa = null;
		ConnectionManager mgr = ConnectionManager.get();

		// 1 - Look for trusted credentials
		JDBCSample.Credential credential = JDBCSample.getTrustedCredentialValues(theAuthRequest);
		if (credential.isEmpty())
			credential = JDBCSample.getCredentialValues(theAuthRequest);

		if (credential.isEmpty())
			credential = JDBCSample.getFormFieldValues(theAuthRequest);

		// here, should be something for the single signon case
		if (credential.isEmpty() && mgr.singleSignOnEnabled())
			credential = JDBCSample.getTrustedEnvironmentVaribleValue(theAuthRequest);

		if (credential.isEmpty() && mgr.singleSignOnEnabled())
		{
			// null implies the provider has to start the dance so throw a SysRecov.
			// the SysRecov needs to have the name of the variable we look for in
			// the second parameter
			SystemRecoverableException e = new SystemRecoverableException("Challenge for REMOTE_USER", "REMOTE_USER");
			throw e;
		}

		if (credential.isEmpty())
		{
			// Assume this is the initial logon and pass null for errorDetails
			generateAndThrowExceptionForLogonPrompt(null);
		}

		try
		{
			//
			// Create a Visa for the new user. Unlike the JDBCVisa, we will not pas a reference to 
			// the ConnectionManager. Instead, the Visa will use a static accessor to get the singleton
			// reference to the ConnectionManager
			//
			visa = new RestorableJDBCVisa();
			visa.init(credential.getUsername(), credential.getPassword());
		}
		catch (final UnrecoverableException ex)
		{
			final String errorDetails = RestorableJDBCSample.getErrorDetails(ex);

			// Something went wrong, probably because the user's credentials
			// are invalid.
			generateAndThrowExceptionForLogonPrompt(errorDetails);
		}
		return visa;
	}

	/**
	 * This method is implemented the same as in {@link JDBCSample#logoff(IVisa, IBiBusHeader)
	 * 
	 * @see INamespaceAuthenticationProvider2#logoff(IVisa, IBiBusHeader)
	 */
	public void logoff(IVisa theVisa, IBiBusHeader theBiBusHeader)
	{
		try
		{
			// We can safely assume that we'll get back the same Visa that we
			// issued.
			final RestorableJDBCVisa visa = (RestorableJDBCVisa) theVisa;
			visa.destroy();
		}
		catch (UnrecoverableException e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * The search method only has minor modifications from {@link JDBCSample#search(IVisa, IQuery), as we no longer
	 * need to hold a reference to the ConnectionManager and we are using a RestorableJDBCVIsa
	 * 
	 * @see com.cognos.CAM_AAA.authentication.INamespaceAuthenticationProviderBase#search(com.cognos.CAM_AAA.authentication.IVisa, com.cognos.CAM_AAA.authentication.IQuery)
	 */
	public IQueryResult search(IVisa theVisa, IQuery theQuery) throws UnrecoverableException
	{

		// We can safely assume that we'll get back the same Visa that we
		// issued.
		final RestorableJDBCVisa visa = (RestorableJDBCVisa) theVisa;
		final QueryResult result = new QueryResult();
		try
		{
			final ISearchExpression expression = theQuery.getSearchExpression();
			final String objectID = expression.getObjectID();
			final ISearchStep[] steps = expression.getSteps();
			// It doesn't make sense to have multiple steps for this provider
			// since the objects are not hierarchical.
			if (steps.length != 1)
				throw new UnrecoverableException("Internal Error",
						"Invalid search expression. Multiple steps is not supported for this namespace.");
			final StringBuffer sqlCondition = new StringBuffer();
			final int searchType = steps[0].getAxis();
			final ISearchFilter filter = steps[0].getPredicate();
			String tenantId = QueryUtil.getTenantId(visa.getAccount());
			switch (searchType)
			{
				case SearchAxis.Self:
				case SearchAxis.DescendentOrSelf:
					if (objectID == null)
					{
						if (filter == null || this.matchesFilter(filter))
							result.addObject(this);
						// Add current namespace
						if (searchType == SearchAxis.Self)
							return result;
						else
							sqlCondition.append(QueryUtil.getSqlCondition(filter));
					}
					else if (objectID.startsWith("u:") && objectID.equals(visa.getAccount().getObjectID()))
					{
						if (filter == null || this.matchesFilter(filter))
							result.addObject(visa.getAccount());
						// Add current user
						return result;
					}
					else if (objectID.startsWith("u:"))
					{
						final String sqlID = objectID.substring(2);
						sqlCondition.append(QueryUtil.getSqlCondition(filter));
						if (sqlCondition.length() > 0)
							sqlCondition.append(" AND ");
						sqlCondition.append("ID = " + sqlID + " AND ISUSER = 1");
					}
					else if (objectID.startsWith("g:"))
					{
						final String sqlID = objectID.substring(2);
						sqlCondition.append(QueryUtil.getSqlCondition(filter));
						if (sqlCondition.length() > 0)
							sqlCondition.append(" AND ");
						sqlCondition.append("ID = " + sqlID + " AND ISGROUP = 1");
					}

					break;
				default:
					sqlCondition.append(QueryUtil.getSqlCondition(filter));

					break;
			}
			sqlCondition.append(getTenantSql(tenantId));
			QueryUtil.searchQuery(ConnectionManager.get(), sqlCondition.toString(), theQuery.getQueryOption(),
					theQuery.getProperties(), theQuery.getSortProperties(), result, this);
		}
		catch (final SQLException e)
		{
			e.printStackTrace();
		}
		catch (final UnrecoverableException e)
		{
			e.printStackTrace();
		}

		return result;
	}

	/** 
	 * This method requires only minor modifications from {@link JDBCSample#init(INamespaceConfiguration)}, due to the fact
	 * that the {@link ConnectionManager} is now a singleton
	 * 
	 * @see Namespace#init(com.cognos.CAM_AAA.authentication.INamespaceConfiguration)
	 */
	@Override
	public void init(INamespaceConfiguration theNamespaceConfiguration) throws UnrecoverableException
	{
		try
		{
			super.init(theNamespaceConfiguration);
			addName(theNamespaceConfiguration.getServerLocale(), theNamespaceConfiguration.getDisplayName());
			ConnectionManager.get().init(theNamespaceConfiguration);
		}
		catch (final IOException e)
		{
			throw new UnrecoverableException("Configuration Error", "Provider initialization failure. Reason: " + e.toString());
		}
		catch (final ClassNotFoundException e)
		{
			throw new UnrecoverableException("Configuration Error", "Provider initialization failure. Reason: " + e.toString());
		}

	}
	
	//
	// The following methods require no modification compared to their implementations in JDBCSample.
	//

	private static String getErrorDetails(final UnrecoverableException e)
	{
		final ByteArrayOutputStream out = new ByteArrayOutputStream();
		final PrintStream ps = new PrintStream(out);

		ps.println(e.getClass().getName() + " : ");
		final String[] messages = e.getMessages();
		for (int i = 0; i < messages.length; i++)
			ps.println(messages[i]);
		ps.println();

		e.printStackTrace(ps);
		ps.close();
		return out.toString();
	}

	private static String getTenantSql(String tenantId)
	{
		return " AND (TENANT='' OR TENANT='" + tenantId + "'" + ")";
	}

	/*
	 * Generate an exception with applicable display objects for the login prompt Note: If this is the initial logon, to avoid an
	 * empty logon log, be sure to throw either: a) a UserRecoverableException with null errorDetails b) a
	 * SystemRecoverableException
	 */
	private void generateAndThrowExceptionForLogonPrompt(String errorDetails) throws UserRecoverableException
	{
		final UserRecoverableException e =
				new UserRecoverableException("Please type your credentials for authentication.", errorDetails);
		e.addDisplayObject(new ReadOnlyDisplayObject("Namespace:", "CAMNamespaceDisplayName", this.getName(Locale.getDefault())));
		e.addDisplayObject(new TextDisplayObject("User ID:", "CAMUsername"));
		e.addDisplayObject(new TextNoEchoDisplayObject("Password:", "CAMPassword"));
		throw e;
	}
}
