import java.sql.Connection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import com.cognos.CAM_AAA.authentication.UnrecoverableException;

public class GroupCache
{

	private final AccountCache accountCache;

	private final Connection connection;

	private final Map< String, Group > groups = new HashMap< String, Group >();

	public GroupCache(final ConnectionManager connectionManager)
	{
		this.connection = connectionManager.getConnection();
		this.accountCache = connectionManager.getAccountCache();
	}

	protected Group createGroup(final String groupID) throws UnrecoverableException
	{
		final Group group = new Group(groupID);
		this.setGroupProperties(group);

		this.groups.put(groupID, group);

		return group;
	}

	public synchronized Group findGroup(final String groupID) throws UnrecoverableException
	{
		Group group = this.groups.get(groupID);

		if (null == group)
			group = this.createGroup(groupID);

		return group;
	}

	protected void setGroupProperties(final Group group) throws UnrecoverableException
	{
		final String groupIDStr = group.getObjectID();
		final Integer groupID = Integer.parseInt(groupIDStr.substring(2));

		//Select GROUPNAME & USERID and exclude any users with a tenantId not public or available in the group.
		final Object[][] data = QueryUtil.query(this.connection, "SELECT g.GROUPNAME, g.USERID FROM GROUPS g INNER JOIN USERS u ON g.USERID=u.USERID WHERE GROUPID = ? AND (u.TENANT = '' OR u.TENANT = g.TENANT)", groupID);
		
		if (0 < data.length)
			group.addName(Locale.getDefault(), (String) data[0][0]);

		for (int i = 0; i < data.length; ++i)
		{
			final Object[] row = data[i];
			group.addMember(this.accountCache.findAccount("u:" + String.valueOf(row[1])));
		}
	}
}
