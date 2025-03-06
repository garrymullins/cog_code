/**
 * Licensed Materials - Property of IBM
 * 
 * IBM Cognos Products: CAMAAA
 * 
 * (C) Copyright IBM Corp. 2012
 * 
 * US Government Users Restricted Rights - Use, duplication or disclosure restricted by GSA ADP Schedule Contract with IBM Corp.
 */
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.sql.SQLException;

import com.cognos.CAM_AAA.authentication.IAccount;
import com.cognos.CAM_AAA.authentication.IBiBusHeader;
import com.cognos.CAM_AAA.authentication.ICredential;
import com.cognos.CAM_AAA.authentication.IRestorableVisa;
import com.cognos.CAM_AAA.authentication.ITrustedCredential;
import com.cognos.CAM_AAA.authentication.ReadOnlyDisplayObject;
import com.cognos.CAM_AAA.authentication.SystemRecoverableException;
import com.cognos.CAM_AAA.authentication.TextNoEchoDisplayObject;
import com.cognos.CAM_AAA.authentication.UnrecoverableException;
import com.cognos.CAM_AAA.authentication.UserRecoverableException;

/**
 * This is a modified version of {@link JDBCVisa} that supports the {@link IRestorableVisa}
 * interfaces. This allows the session to be restored on failover.
 * 
 */
public class RestorableJDBCVisa extends Visa implements IRestorableVisa
{

	// This flag should be true if the state information changes. The cognos
	// server will update it's cached copy of this visa when true.
	private boolean hasChanged = false;
	private Account account = null;
	private String username;
	private String password;

	/**
	 * Initializes this visa
	 */
	public void init(String username, String password) throws UnrecoverableException
	{
		try
		{
			ConnectionManager connectionManager = ConnectionManager.get();
			// Create account object for the user.
			account = QueryUtil.createAccount(connectionManager, username, password);
			QueryUtil.updateMembership(connectionManager, this);
			this.username = username;
			this.password = password;
			
			// This is very important. You must flag this Visa as changed whenever it's state (i.e. credentials)
			// is modified
			hasChanged = true;
		}
		catch (final SQLException e)
		{
			throw new UnrecoverableException("Connection Error", "Database connection failure. Reason: "
					+ ConnectionManager.getSqlExceptionDetails(e));
		}
	}

	@Override
	public ITrustedCredential generateTrustedCredential(IBiBusHeader theAuthRequest) throws UserRecoverableException,
			SystemRecoverableException, UnrecoverableException
	{
		// 1 - Look for credentials coming from SDK request
		JDBCSample.Credential credential = JDBCSample.getCredentialValues(theAuthRequest);
		if (credential.isEmpty())
		{
			// 2 - Look for credentials in formfield
			credential = JDBCSample.getFormFieldValues(theAuthRequest);
		}

		if (credential.isEmpty() || !credential.getUsername().equals(username))
		{
			credential.setUsername(username);
			credential.setPassword(password);
		}

		if (!validateConnection(credential))
		{
			final UserRecoverableException e =
					new UserRecoverableException("Please type your credentials for authentication.",
							"The provided credentials are invalid.");
			e.addDisplayObject(new ReadOnlyDisplayObject("User ID:", "CAMUsername", this.username));
			e.addDisplayObject(new TextNoEchoDisplayObject("Password:", "CAMPassword"));
			throw e;
		}
		final TrustedCredential tc = new TrustedCredential();
		tc.addCredentialValue("username", this.username);
		tc.addCredentialValue("password", this.password);
		return tc;
	}

	@Override
	public ICredential generateCredential(IBiBusHeader theBiBusHeader) throws UserRecoverableException, SystemRecoverableException,
			UnrecoverableException
	{
		if (!this.validateConnection(this.username, this.password))
		{
			final UnrecoverableException e =
					new UnrecoverableException("Could not generate credentials for the user.", "Visa contains invalid credentials.");
			throw e;
		}
		final Credential credentials = new Credential();
		credentials.addCredentialValue("username", this.username);
		credentials.addCredentialValue("password", this.password);
		return credentials;
	}

	@Override
	public IAccount getAccount()
	{
		return account;
	}

	/**
	 * Validates the credentials against the database
	 */
	private boolean validateConnection(final JDBCSample.Credential credential)
	{
		return validateConnection(credential.getUsername(), credential.getPassword());
	}

	/**
	 * Validates the credentials against the database
	 */
	private boolean validateConnection(final String theUsername, final String thePassword)
	{
		try
		{
			QueryUtil.createAccount(ConnectionManager.get(), theUsername, thePassword);
			username = theUsername;
			password = thePassword;
		}
		catch (UnrecoverableException ex)
		{
			return false;
		}

		return true;
	}

	/**
	 * To be able to restore on failover, we need to externalize this Visa. In this
	 * case, the only pieces of info we need to recreate the Visa is the username
	 * and password
	 * 
	 * For simplicity, we'll externalize them in plain-text. For your implementation, you should
	 * encrypt the credentials on the way out.
	 * 
	 * @see java.io.Externalizable#writeExternal(java.io.ObjectOutput)
	 */
	public void writeExternal(ObjectOutput out) throws IOException
	{
		out.writeUTF(username);
		out.writeUTF(password);
	}

	/** 
	 * In a failover scenario, the new server will try to recreate this Visa.
	 * We'll just generate a new account from the saved username and password
	 * 
	 * @see Externalizable#readExternal(ObjectInput)
	 */
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException
	{
		try
		{
			String username = in.readUTF();
			String password = in.readUTF();
			init(username, password);
		}
		catch (UnrecoverableException e)
		{
			throw new IOException(e);
		}
	}

	/* (non-Javadoc)
	 * @see com.cognos.CAM_AAA.authentication.IRestorableVisa#hasStateChanged()
	 */
	public boolean hasStateChanged()
	{
		return hasChanged;
	}

	/* (non-Javadoc)
	 * @see com.cognos.CAM_AAA.authentication.IRestorableVisa#resetChangeIndicator()
	 */
	public void resetChangeIndicator()
	{
		hasChanged = false;
	}

}
