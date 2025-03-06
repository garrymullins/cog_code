import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import com.cognos.CAM_AAA.authentication.INamespaceConfiguration;

public class ConnectionManager
{
	// A singleton instance of this class
	private final static ConnectionManager INSTANCE = new ConnectionManager();

	private ConnectionManager()
	{
	}

	/**
	 * Returns the instance of the connection manager
	 */
	public final static ConnectionManager get()
	{
		return INSTANCE;
	}

	public static String getSqlExceptionDetails(final SQLException e)
	{
		final StringBuffer buffer = new StringBuffer();

		for (SQLException se = e; null != se; se = se.getNextException())
		{
			buffer.append("SQL STATE: " + se.getSQLState());
			buffer.append("ERROR CODE: " + se.getErrorCode());
			buffer.append("MESSAGE: " + se.getMessage());
			buffer.append("\n");
		}

		return buffer.toString();
	}

	private AccountCache accountCache;

	private final ThreadLocal< Connection > connections = new ThreadLocal< Connection >()
	{
		@Override
		protected Connection initialValue()
		{
			return ConnectionManager.this.openConnection();
		}
	};

	private String connectionString;

	private GroupCache groupCache;

	private String password;

	private String username;

	private String singleSignon;

	public void init(final INamespaceConfiguration configuration) throws IOException, ClassNotFoundException
	{
		this.loadProperties(configuration);
	}

	private void createAccountCache()
	{
		this.accountCache = new AccountCache(this);
	}

	private void createGroupCache()
	{
		this.groupCache = new GroupCache(this);
	}

	public AccountCache getAccountCache()
	{
		if (null == this.accountCache)
			this.createAccountCache();

		return this.accountCache;
	}

	public Connection getConnection()
	{
		return this.connections.get();
	}

	public GroupCache getGroupCache()
	{
		if (null == this.groupCache)
			this.createGroupCache();

		return this.groupCache;
	}

	public boolean singleSignOnEnabled()
	{
		if (singleSignon != null && singleSignon.equalsIgnoreCase("true"))
			return true;
		else
			return false;
	}

	protected void loadDriver(final String driverClass) throws ClassNotFoundException
	{
		Class.forName(driverClass);
	}

	private void loadProperties(final INamespaceConfiguration configuration) throws IOException, ClassNotFoundException
	{
		final Properties props = new Properties();

		final String installLocation = configuration.getInstallLocation();
		final File file =
				new File(installLocation + File.separator + "configuration" + File.separator + "JDBC_Config_"
						+ configuration.getID() + ".properties");
		if (file.exists())
			props.load(new FileInputStream(file));

		this.connectionString = props.getProperty("connectionString");
		this.loadDriver(props.getProperty("driverClass"));
		this.username = props.getProperty("username");
		this.password = props.getProperty("password");
		this.singleSignon = props.getProperty("singleSignon");
	}

	private Connection openConnection()
	{
		try
		{
			return DriverManager.getConnection(this.connectionString, this.username, this.password);
		}
		catch (final SQLException ex)
		{
			return null;
		}
	}
}
