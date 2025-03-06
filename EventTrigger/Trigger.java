/** 
Licensed Materials - Property of IBM

IBM Cognos Products: DOCS

(C) Copyright IBM Corp. 2005, 2013

US Government Users Restricted Rights - Use, duplication or disclosure restricted by GSA ADP Schedule Contract with
IBM Corp.
*/
/**
 *
 * Copyright (C) 2008 Cognos ULC, an IBM Company. All rights reserved.
 * Cognos (R) is a trademark of Cognos ULC, (formerly Cognos Incorporated).
 *
 */

import java.rmi.RemoteException;
import java.util.StringTokenizer;
import javax.xml.namespace.QName;

import org.apache.axis.client.Stub;
import org.apache.axis.message.SOAPHeaderElement;

import com.cognos.developer.schemas.bibus._3.BiBusHeader;
import com.cognos.developer.schemas.bibus._3.CAM;
import com.cognos.developer.schemas.bibus._3.ContentManagerService_PortType;
import com.cognos.developer.schemas.bibus._3.ContentManagerService_ServiceLocator;
import com.cognos.developer.schemas.bibus._3.EventManagementService_PortType;
import com.cognos.developer.schemas.bibus._3.EventManagementService_ServiceLocator;
import com.cognos.developer.schemas.bibus._3.HdrSession;
import com.cognos.developer.schemas.bibus._3.PropEnum;
import com.cognos.developer.schemas.bibus._3.QueryOptions;
import com.cognos.developer.schemas.bibus._3.SearchPathMultipleObject;
import com.cognos.developer.schemas.bibus._3.Sort;
import com.cognos.developer.schemas.bibus._3.XmlEncodedXML;

/**
 *
 */
public class Trigger
{
	private String endpoint;

	private ContentManagerService_PortType cmService;
	private ContentManagerService_ServiceLocator cmServiceLocator;
	private EventManagementService_PortType eventService;
	private EventManagementService_ServiceLocator eventServiceLocator;
	private XmlEncodedXML credentialXEX;

	public Trigger(String serverURLString)
	{
		this.endpoint = serverURLString;
		if (endpoint == null || endpoint.equals(""))
			return;
		try
		{
			//initialize the service locators
			eventServiceLocator = new EventManagementService_ServiceLocator();
			cmServiceLocator = new ContentManagerService_ServiceLocator();

			//get the service objects from the locators
			eventService = eventServiceLocator.geteventManagementService(new java.net.URL(endpoint));

			cmService = cmServiceLocator.getcontentManagerService(new java.net.URL(endpoint));
			//Set the axis timeout to 0 (infinite)
			//There may be many, many actions due to this trigger
			((Stub)eventService).setTimeout(0);

			credentialXEX = new XmlEncodedXML();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	public boolean loginAnonymous()
	{
		if (! loginAnonymousEnabled() )
		{
			return false;
		}

		CAM cam = new CAM();
		cam.setAction("logon");

		HdrSession header = new HdrSession();

		BiBusHeader bibus = new BiBusHeader();
		bibus.setCAM(cam);
		bibus.setHdrSession(header);

        ((Stub)cmService).setHeader("http://developer.cognos.com/schemas/bibus/3/", "biBusHeader", bibus);
        
		return true;
	}

	public boolean login(String namespace, String uid, String passwd)
	  {
		try
		{
			StringBuffer credentialXML = new StringBuffer();
			credentialXML.append("<credential>");
			credentialXML.append("<namespace>" + namespace + "</namespace>");
			credentialXML.append("<password>" + passwd + "</password>");
			credentialXML.append("<username>" + uid + "</username>");
			credentialXML.append("</credential>");

			credentialXEX.set_value(credentialXML.toString());
			cmService.logon(credentialXEX, null);
		}
		catch (RemoteException e)
		{
			e.printStackTrace();
			return false;
		}
        
        try {
            
            SOAPHeaderElement temp = ((Stub)cmService).getResponseHeader("http://developer.cognos.com/schemas/bibus/3/", "biBusHeader");
            BiBusHeader bibus = (BiBusHeader)temp.getValueAsType(new QName("http://developer.cognos.com/schemas/bibus/3/", "biBusHeader"));

            if (bibus != null)
            {
               ((Stub)eventService).setHeader("http://developer.cognos.com/schemas/bibus/3/", "biBusHeader", bibus);
                return true;
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }


		return false;
	}

	public void logoff()
	{
	  	try
		{
			cmService.logoff();
		}
	  	catch (RemoteException e)
		{
			e.printStackTrace();
		}

	}

	public boolean loginAnonymousEnabled()
	{
		SearchPathMultipleObject cmSearch = new SearchPathMultipleObject("~");
		try
		{
			cmService.query(
				cmSearch,
				new PropEnum[] {},
				new Sort[] {},
				new QueryOptions());
		}
		catch (java.rmi.RemoteException remoteEx)
		{
			return false;
		}
        
        try {
            
            SOAPHeaderElement temp = ((Stub)cmService).getResponseHeader("http://developer.cognos.com/schemas/bibus/3/", "biBusHeader");
            BiBusHeader bibus = (BiBusHeader)temp.getValueAsType(new QName("http://developer.cognos.com/schemas/bibus/3/", "biBusHeader"));

            if (bibus != null)
            {
               ((Stub)eventService).setHeader("http://developer.cognos.com/schemas/bibus/3/", "biBusHeader", bibus);
                return true;
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

		return false;
	}

	public int fireTrigger(String triggerName)
	{
		try
		{
			// sn_dg_sdk_method_eventManagementService_trigger_start_1
			return eventService.trigger(triggerName);
			// sn_dg_sdk_method_eventManagementService_trigger_end_1
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return 0;
		}
	}

	public static void usage()
	{
		System.out.println("");
		System.out.println("");
		System.out.println("Command Line Parameters:");
		System.out.println("");
		System.out.println("<URL> [ <userName> <password> <nameSpace> ] triggerList [ <delimiter> ]");
		System.out.println("");
		System.out.println("  Required arguments:");
		System.out.println("");
		System.out.println("          URL - Server URL");
		System.out.println("			eg. \"http://localhost:9300/p2pd/servlet/dispatch\"");
		System.out.println("  triggerList - comma separated list of trigger names");
		System.out.println("			eg. \"triggerName1,triggerName2,triggerName3\"");
		System.out.println("");
		System.out.println("  NOTE: If using the optional delimiter override parameter defined below, use the delimiter specified in that parameter to separate the trigger names");
		System.out.println("			eg. \"triggerName1|triggerName2|triggerName3\"");
		System.out.println("");	
		System.out.println("  Optional arguments: for use with secured namespace (Anonymous disabled)");
		System.out.println("");
		System.out.println("     userName - username, valid within the namespace, to run the utility");
		System.out.println("     password - password for the given user");
		System.out.println("    nameSpace - namespace for the desired user");
		System.out.println("");
		System.out.println("  Optional argument:");
		System.out.println("");
		System.out.println("     delimiter - Use a different triggerList delimiter character to override the comma delimiter if trigger names contain commas");
		System.out.println("			eg. \"|\"");
		System.out.println("");

	}

	public static void main(String args[])
	{
		if ((args.length != 2) &&		// URL triggerList 
				(args.length != 3) &&   // URL triggerList delimiter
				(args.length != 5) &&   // URL userName password nameSpace triggerList
				(args.length != 6))     // URL userName password nameSpace triggerList delimiter

		{
			usage();
			System.exit(-1);
		}
		String nameSpace = "";
		String passwd = "";
		String userName = "";
		String triggers = "";
		String url;
		url = args[0];
		String delimiter = ",";
		
		boolean cmIsReady = false;
		Trigger myTrigger = new Trigger(url);

                if (args.length == 5 || args.length == 6) {
			userName = args[1];
			passwd = args[2];
			nameSpace = args[3];
			triggers = args[4];
			
			if (args.length == 6) {
		    	  delimiter = args[5];
			}
		
			cmIsReady = myTrigger.login(nameSpace,userName,passwd);
		}
		else
		{
			triggers = args[1];
		
			if (args.length == 3) {
		    	  delimiter = args[2];
			}
			
			cmIsReady = myTrigger.loginAnonymous();
		}
                
		StringTokenizer triggerTokens = new StringTokenizer(triggers, delimiter);

		if (cmIsReady)
		{
			int totalTriggersFired = 0;
			int triggerFired = 0;

			while(triggerTokens.hasMoreTokens())
			{
				String triggerName = triggerTokens.nextToken();
				triggerFired = myTrigger.fireTrigger(triggerName);
				if ( triggerFired > 0 )
				{
					System.out.println("Trigger: " + triggerName + " fired successfully");
					totalTriggersFired += 1;
				}
				else
				{
					System.out.println("");
					System.out.println("Failed to fire trigger " + triggerName + ".");
					System.out.println("");
					System.out.println("Note that if a schedule was not triggered, it may be ");
					System.out.println("because the current user does not have sufficient permissions ");
					System.out.println("to access the schedule. For more information, see the ");
					System.out.println("Administration and Security Guide.");
					System.out.println("");
				}
			}
		       	System.exit(totalTriggersFired);
		}
		else
		{
			System.out.println("Error: Login Failure - please try again.");
			usage();
			System.exit(-2);
		}
	}

}
