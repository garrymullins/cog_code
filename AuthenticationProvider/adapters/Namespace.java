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

import java.util.Locale;

import com.cognos.CAM_AAA.authentication.INamespace;
import com.cognos.CAM_AAA.authentication.INamespaceConfiguration;
import com.cognos.CAM_AAA.authentication.ISearchFilter;
import com.cognos.CAM_AAA.authentication.ISearchFilterConditionalExpression;
import com.cognos.CAM_AAA.authentication.ISearchFilterFunctionCall;
import com.cognos.CAM_AAA.authentication.ISearchFilterRelationExpression;
import com.cognos.CAM_AAA.authentication.UnrecoverableException;


public class Namespace extends UiClass implements INamespace
{
	/**
	 * 
	 */
	public Namespace()
	{
		super(null);
		capabilities = new String[6];
		capabilities[0] = CapabilityCaseSensitive;
		capabilities[1] = CapabilityContains;
		capabilities[2] = CapabilityEquals;
		capabilities[3] = CapabilitySort;
		capabilities[4] = CapabilityStartsWith;
		capabilities[5] = CapabilityEndsWith;
		namespaceFormat = "http://developer.cognos.com/schemas/CAM/AAANamespaceFormat/2/";
	}


	/**
	 * @param theObjectID
	 */
	public Namespace(String theObjectID)
	{
		super(theObjectID);
		capabilities = new String[6];
		capabilities[0] = CapabilityCaseSensitive;
		capabilities[1] = CapabilityContains;
		capabilities[2] = CapabilityEquals;
		capabilities[3] = CapabilitySort;
		capabilities[4] = CapabilityStartsWith;
		capabilities[5] = CapabilityEndsWith;
		namespaceFormat = "http://developer.cognos.com/schemas/CAM/AAANamespaceFormat/2/";
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see com.cognos.CAM_AAA.authentication.INamespace#init(com.cognos.CAM_AAA.authentication.INamespaceConfiguration)
	 */
	public void init(INamespaceConfiguration theNamespaceConfiguration)
			throws UnrecoverableException
	{
		setObjectID(theNamespaceConfiguration.getID());
		addName(theNamespaceConfiguration.getServerLocale(),
				theNamespaceConfiguration.getDisplayName());
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see com.cognos.CAM_AAA.authentication.INamespace#destroy()
	 */
	public void destroy()
	{
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see com.cognos.CAM_AAA.authentication.INamespace#getNamespaceFormat()
	 */
	public String getNamespaceFormat()
	{
		return namespaceFormat;
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see com.cognos.CAM_AAA.authentication.INamespace#setNamespaceFormat(java.lang.String)
	 */
	public void setNamespaceFormat(String theNamespaceFormat)
	{
		namespaceFormat = theNamespaceFormat;
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see com.cognos.CAM_AAA.authentication.INamespace#getCapabilities()
	 */
	public String[] getCapabilities()
	{
		return capabilities;
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see com.cognos.CAM_AAA.authentication.IBaseClass#getHasChildren()
	 */
	public boolean getHasChildren()
	{
		return true;
	}


	/**
	 * @param theFilter
	 * @return
	 */
	public boolean matchesFilter(ISearchFilter theFilter)
	{
		if (theFilter == null)
		{
			return true;
		}
		switch (theFilter.getSearchFilterType())
		{
			case ISearchFilter.ConditionalExpression :
				{
					ISearchFilterConditionalExpression item = (ISearchFilterConditionalExpression) theFilter;
					String operator = item.getOperator();
					ISearchFilter[] filters = item.getFilters();
					if (filters.length > 0)
					{
						boolean retval = false;
						for (int i = 1; i < filters.length; i++)
						{
							retval = this.matchesFilter(filters[i]);
							if (operator
									.equals(ISearchFilterConditionalExpression.ConditionalAnd))
							{
								if (retval == false)
								{
									return false;
								}
							}
							else if (operator
									.equals(ISearchFilterConditionalExpression.ConditionalOr))
							{
								if (retval == true)
								{
									return true;
								}
							}
							else
							{
								return false;
							}
						}
					}
				}
				break;
			case ISearchFilter.FunctionCall :
				{
					ISearchFilterFunctionCall item = (ISearchFilterFunctionCall) theFilter;
					String functionName = item.getFunctionName();
					if (functionName.equals(ISearchFilterFunctionCall.Contains))
					{
						String[] parameter = item.getParameters();
						String propertyName = parameter[0];
						String value = parameter[1];
						if (propertyName.compareTo("@objectClass") == 0)
						{
							return ("namespace".indexOf(value) > 0);
						}
						else if (propertyName.equals("@defaultName")
								|| propertyName.equals("@name"))
						{
							Locale[] locales = this.getAvailableNameLocales();
							if (locales != null)
							{
								for (int i = 0; i < locales.length; i++)
								{
									if (this.getName(locales[i]).indexOf(value) != -1)
									{
										return true;
									}
								}
							}
						}
						return false;
					}
					else if (functionName
							.compareTo(ISearchFilterFunctionCall.StartsWith) == 0)
					{
						String[] parameter = item.getParameters();
						String propertyName = parameter[0];
						String value = parameter[1];
						if (propertyName.compareTo("@objectClass") == 0)
						{
							return ("namespace".startsWith(value));
						}
						else if (propertyName.compareTo("@defaultName") == 0
								|| propertyName.compareTo("@name") == 0)
						{
							Locale[] locales = this.getAvailableNameLocales();
							if (locales != null)
							{
								for (int i = 0; i < locales.length; i++)
								{
									if (this.getName(locales[i]).startsWith(
											value))
									{
										return true;
									}
								}
							}
						}
						return false;
					}
					else if (functionName
							.compareTo(ISearchFilterFunctionCall.EndsWith) == 0)
					{
						String[] parameter = item.getParameters();
						String propertyName = parameter[0];
						String value = parameter[1];
						if (propertyName.compareTo("@objectClass") == 0)
						{
							return ("namespace".endsWith(value));
						}
						else if (propertyName.compareTo("@defaultName") == 0
								|| propertyName.compareTo("@name") == 0)
						{
							Locale[] locales = this.getAvailableNameLocales();
							if (locales != null)
							{
								for (int i = 0; i < locales.length; i++)
								{
									if (this.getName(locales[i])
											.endsWith(value))
									{
										return true;
									}
								}
							}
						}
						return false;
					}
					else
					{
						return false;
					}
				}
			case ISearchFilter.RelationalExpression :
				{
					ISearchFilterRelationExpression item = (ISearchFilterRelationExpression) theFilter;
					String propertyName = item.getPropertyName();
					String constraint = item.getConstraint();
					String operator = item.getOperator();
					if (propertyName.equals("@objectClass"))
					{
						if (constraint.equals("namespace"))
						{
							return (operator
									.equals(ISearchFilterRelationExpression.EqualTo));
						}
						else
						{
							return false;
						}
					}
					else if (propertyName.equals("@defaultName")
							|| propertyName.equals("@name"))
					{
						if (operator
								.equals(ISearchFilterRelationExpression.EqualTo))
						{
							Locale[] locales = this.getAvailableNameLocales();
							if (locales != null)
							{
								for (int i = 0; i < locales.length; i++)
								{
									if (this.getName(locales[i]).compareTo(
											constraint) == 0)
									{
										return true;
									}
								}
							}
							return false;
						}
						else if (operator
								.equals(ISearchFilterRelationExpression.NotEqual))
						{
							Locale[] locales = this.getAvailableNameLocales();
							if (locales != null)
							{
								for (int i = 0; i < locales.length; i++)
								{
									if (this.getName(locales[i]).compareTo(
											constraint) != 0)
									{
										return true;
									}
								}
							}
							return false;
						}
					}
				}
		}
		return false;
	}

	private String[]	capabilities;
	private String		namespaceFormat;
}
