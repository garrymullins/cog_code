import java.sql.Connection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import com.cognos.CAM_AAA.authentication.UnrecoverableException;

public class AccountCache
{

	private final Map< String, Account > accounts = new HashMap< String, Account >();

	private final Connection connection;
	
	public static final String TENANTID_ACCOUNT_PROPERTY = "tenant";

	public AccountCache(final ConnectionManager connectionManager)
	{
		this.connection = connectionManager.getConnection();
	}

	private Account createAccount(final String userID) throws UnrecoverableException
	{
		final Account account = new Account(userID);
		this.setAccountProperties(account);

		this.accounts.put(userID, account);

		return account;
	}

	public synchronized Account findAccount(final String userID) throws UnrecoverableException
	{
		Account account = this.accounts.get(userID);
		if (null == account)
			account = this.createAccount(userID);

		return account;
	}

	private void setAccountProperties(final Account account) throws UnrecoverableException
	{
		final String userIDStr = account.getObjectID();
		final Integer userID = Integer.parseInt(userIDStr.substring(2));

		final Object[][] data =
				QueryUtil.query(this.connection, "SELECT USERNAME, FULLNAME, LOCALE, TENANT FROM USERS WHERE USERID = ?", userID);
		if (1 > data.length)
			return;

		final Object[] row = data[0];
		account.setUserName((String) row[0]);

		final Locale locale = QueryUtil.getLocale((String) row[2]);
		account.setContentLocale(locale);
		account.setProductLocale(locale);

		account.addName(locale, (String) row[1]);
		
		setTenantId(account, row[3].toString());
	}
	
	/**
	 *	To configure Multi-tenancy, the following advanced properties must be added via the Configuration Tool
	 *
	 *	Name							Value 
	 *  ==============================	======
	 *	multitenancy.TenantPattern		~/parameters/tenant
	 *
	 *	Refer to Tenant Patterns within Multi-Tenancy product documentation for details
	 */
	private void setTenantId(Account account, String tenantId) {
		account.addCustomProperty(TENANTID_ACCOUNT_PROPERTY, tenantId);
	}
}
