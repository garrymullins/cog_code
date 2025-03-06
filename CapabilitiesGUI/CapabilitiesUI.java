/** 
Licensed Materials - Property of IBM

IBM Cognos Products: DOCS

(C) Copyright IBM Corp. 2005, 2008

US Government Users Restricted Rights - Use, duplication or disclosure restricted by GSA ADP Schedule Contract with
IBM Corp.
*/
/**
 * CapabilitiesUI.java
 *
 * Copyright (C) 2008 Cognos ULC, an IBM Company. All rights reserved.
 * Cognos (R) is a trademark of Cognos ULC, (formerly Cognos Incorporated).
 *
 */

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import java.io.File;

import java.net.URL;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import com.cognos.developer.schemas.bibus._3.BaseClass;
import com.cognos.developer.schemas.bibus._3.OrderEnum;
import com.cognos.developer.schemas.bibus._3.PropEnum;
import com.cognos.developer.schemas.bibus._3.QueryOptions;
import com.cognos.developer.schemas.bibus._3.SearchPathMultipleObject;
import com.cognos.developer.schemas.bibus._3.Sort;
import com.cognos.developer.schemas.bibus._3.UserCapabilityEnum;

// This Java class extends the JFrame class so that you can 
// display a window.
public class CapabilitiesUI extends JFrame
{
	private CRNConnect connect;

	// The following variables represent the dialog components.
	private JTextArea textAreaPane;
	private JTextField cmURL;
	private JButton capabilityButton;
	private JComboBox secObjSelectOption;
	private JComboBox packSelectOption;
	
	private static Logon sessionLogon;
	private static BaseClassWrapper selectedSecurityObject = null;
	private static BaseClassWrapper selectedPackage = null;
	
	//Set capability to update to CanUseReportStudio
	private static String secFuncPath =
		"/capability/securedFunction[@name='Report Studio']";
	private static UserCapabilityEnum capToUpdate = UserCapabilityEnum.canUseReportStudio;
	
	// This is the constructor.
	public CapabilitiesUI(String title, CRNConnect connection)
	{
		// Set the title of the frame, even before the variables are declared.
		super(title);
		connect = connection;
		addComponents();
	}

	// Add all components to the frame's panel.
	private void addComponents()
	{
		JMenuBar mBar = new JMenuBar();
		this.setJMenuBar(mBar);

		//declare menuItems
		JMenuItem exit;
		JMenuItem about;
		JMenuItem overview;

		//Add and populate the File menu. 
		JMenu fileMenu = new JMenu("File");
		mBar.add(fileMenu);

		exit = new JMenuItem("Exit");
		fileMenu.add(exit);
		exit.addActionListener(new MenuHandler());

		//Add and populate the Help menu.
		JMenu helpMenu = new JMenu("Help");
		mBar.add(helpMenu);

		about = new JMenuItem("About");
		helpMenu.add(about);
		about.addActionListener(new MenuHandler());

		overview = new JMenuItem("Overview");
		helpMenu.add(overview);
		overview.addActionListener(new MenuHandler());

		// Create the select security object and packages combo boxes
		BaseClassWrapper listOfPackages[] = getListOfPackages(connect);
		BaseClassWrapper listOfSecObjects[] = getListOfSecObjects(connect);
		
		packSelectOption = new JComboBox(listOfPackages);
		packSelectOption.setSelectedItem(null);
		packSelectOption.addActionListener(new PackageSelectionHandler());
		
		secObjSelectOption = new JComboBox(listOfSecObjects);
		secObjSelectOption.setSelectedItem(null);
		secObjSelectOption.addActionListener(new SecObjSelectionHandler());
		
		
		// create a cmURL panel, Account panel, Package panel
		JPanel cmURLPanel = new JPanel();
		JPanel accountPanel = new JPanel();
		JPanel packagePanel = new JPanel();

		// Add the URL text field and label
		cmURL = new JTextField(CRNConnect.CM_URL.length()-10);
		cmURL.setText(CRNConnect.CM_URL);
		cmURL.setEditable(false);

		// Create the Button and Button Panel
		capabilityButton = new JButton("Set Capabilities");
		capabilityButton.addActionListener(new allButtonsHandler());

		cmURLPanel.add(new JLabel("Server URL:"), BorderLayout.WEST);
		cmURLPanel.add(cmURL, BorderLayout.CENTER);
		cmURLPanel.add(capabilityButton, BorderLayout.EAST);
		
		//set up account panel, package panel
		accountPanel.add(new JLabel("Account or Role:"), BorderLayout.WEST);
		accountPanel.add(secObjSelectOption, BorderLayout.CENTER);

		packagePanel.add(new JLabel("Package:"), BorderLayout.WEST);
		packagePanel.add(packSelectOption, BorderLayout.CENTER);
		
		//set up the command panel
		JPanel commandPane = new JPanel(new BorderLayout());
		commandPane.add(cmURLPanel, BorderLayout.NORTH);
		commandPane.add(accountPanel, BorderLayout.CENTER);
		commandPane.add(packagePanel, BorderLayout.SOUTH);
			
		// Add the status text pane.
		textAreaPane = new JTextArea();

		//Add the ScrollPane to outputPanel
		JScrollPane areaScrollPane = new JScrollPane(textAreaPane);
		areaScrollPane.setVerticalScrollBarPolicy(
			JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		areaScrollPane.setPreferredSize(new Dimension(300, 275));
		areaScrollPane.setBorder(
			BorderFactory.createCompoundBorder(
				BorderFactory.createCompoundBorder(
					BorderFactory.createTitledBorder("Output"),
					BorderFactory.createEmptyBorder(5, 5, 5, 5)),
				areaScrollPane.getBorder()));

		JPanel outputPanel = new JPanel(new GridLayout(0, 1));
		outputPanel.add(areaScrollPane);

		JPanel panel = new JPanel(new BorderLayout());
		panel.add(commandPane, BorderLayout.NORTH);
		panel.add(outputPanel, BorderLayout.CENTER);

		setContentPane(panel);
	}

	private class MenuHandler implements ActionListener
	{
		public void actionPerformed(ActionEvent e)
		{
			if (e.getActionCommand().startsWith("http://"))
			{
				connect.connectionChange(e.getActionCommand());
			}
			try
			{
				JMenuItem menuClicked = (JMenuItem)e.getSource();
				if (menuClicked.getText() == "Exit")
				{
					System.exit(0);
				}
				if (menuClicked.getText() == "About")
				{
					JOptionPane.showMessageDialog(
						((JMenuItem)e.getSource()).getParent(),
						"IBM Cognos Sample Application\n\n"
							+ "Version 1.0.0\n"
							+ "This application uses the IBM Cognos Software Development Kit",
						"About IBM Cognos Samples",
						JOptionPane.INFORMATION_MESSAGE,
						new ImageIcon("../Common/about.gif"));
				}
				if (menuClicked.getText().compareTo("Overview") == 0)
				{
					JFrame explainWindow =
						new JFrame("Overview for Capabilities Sample");
					File explainFile = new File("Java_CapabilitiesGUI_Explain.html");
					if (! explainFile.exists())
					{
						JOptionPane.showMessageDialog(null, "Explain file not found");
						return;
					}
					URL explainURL =
						new URL("file:///" + explainFile.getAbsolutePath());
					JEditorPane explainPane = new JEditorPane();
					explainPane.setPage(explainURL);
					explainPane.setEditable(false);

					JScrollPane explainScroll =
						new JScrollPane(
							explainPane,
							JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
							JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
					explainWindow.getContentPane().add(explainScroll);
					explainWindow.setSize(640, 480);
					explainWindow.setVisible(true);
				}

			}
			catch (Exception ex)
			{}
		}
	}

	// The following is the button event handler.
	// Note: A SWITCH statement cannot be used here because we are comparing 
	//       objects.
	private class allButtonsHandler implements ActionListener
	{
		public void actionPerformed(ActionEvent e)
		{
			if (!Logon.loggedIn(connect))
			{
				try
				{
					sessionLogon.logon(connect);
				}
				catch (Exception logonException)
				{}
			}

			JButton buttonPressed = ((JButton)e.getSource());
			String output = new String();

			if (buttonPressed == capabilityButton)
			{
				if(selectedSecurityObject != null & selectedPackage != null){
				Capabilities capability = new Capabilities();

				output = capability.updateSecuredFunction(connect, selectedSecurityObject, selectedPackage, secFuncPath, capToUpdate);
				}
				else output = "Security Object and Package not selected.";
			}
			if (output.compareTo("") != 0)
			{
				textAreaPane.setText("");
				textAreaPane.append(output);
			}
		}
	}
	
	private class PackageSelectionHandler implements ActionListener
	{
		public void actionPerformed(ActionEvent repSelectedEvent)
		{
			selectedPackage = (BaseClassWrapper) packSelectOption.getSelectedItem();
		}
	}
	
	private class SecObjSelectionHandler implements ActionListener
	{
		public void actionPerformed(ActionEvent repSelectedEvent)
		{
			selectedSecurityObject = (BaseClassWrapper) secObjSelectOption.getSelectedItem();
		}
	}

	//This is a method for retrieving a list of the available packages
	protected BaseClassWrapper[] getListOfPackages(CRNConnect connection)
	{
		BaseClassWrapper packagesList[] = null;
		BaseClass packages[] = new BaseClass[0];
		int packageIndex = 0;

		if (connection == null)
		{
			System.out.println(
				"Invalid parameter passed to getListOfPackages()\n");
			return null;
		}

		// The userCapabilityPolicies property specifies which users, groups, or
		// roles have specific capabilities for the package.
		// The effectiveUserCapabilities property specifies what capabilities the
		// current user has for the package and at the global level
		PropEnum props[] =
			new PropEnum[] { PropEnum.searchPath, PropEnum.defaultName, PropEnum.userCapabilityPolicies, PropEnum.effectiveUserCapabilities };
		Sort sortOptions[] = { new Sort()};
		sortOptions[0].setOrder(OrderEnum.ascending);
		sortOptions[0].setPropName(PropEnum.defaultName);

		if (!Logon.loggedIn(connect))
		{
			try
			{
				sessionLogon.logon(connect);
			}
			catch (Exception logonException)
			{}
		}
		try
		{
			SearchPathMultipleObject packagesPath = new SearchPathMultipleObject();
			packagesPath.set_value("content//package");
			
			packages =
				connection.getCMService().query(
						packagesPath,
					props,
					sortOptions,
					new QueryOptions());
		}
		catch (java.rmi.RemoteException remoteEx)
		{
			System.out.println("Caught Remote Exception:\n");
			remoteEx.printStackTrace();
		}

		packagesList = new BaseClassWrapper[packages.length];

		if ((packages != null) && (packages.length > 0))
		{
			for (packageIndex = 0; packageIndex < packages.length; packageIndex++)
			{
				packagesList[packageIndex] = new BaseClassWrapper(packages[packageIndex]);
			}
		}
		return packagesList;
	}
	//This is a method for retrieving a list of the available accounts and roles
	protected BaseClassWrapper[] getListOfSecObjects(CRNConnect connection)
	{
		BaseClassWrapper secObjectList[] = null;
		BaseClass accounts[] = new BaseClass[0];
		BaseClass roles[] = new BaseClass[0];
		int secObjectIndex = 0;
		int accountIndex = 0;
		int roleIndex = 0;

		if (connection == null)
		{
			System.out.println(
				"Invalid parameter passed to getListOfSecObjects()\n");
			return null;
		}

		PropEnum props[] =
			new PropEnum[] { PropEnum.searchPath, PropEnum.defaultName };
		Sort sortOptions[] = { new Sort()};
		sortOptions[0].setOrder(OrderEnum.ascending);
		sortOptions[0].setPropName(PropEnum.defaultName);

		if (!Logon.loggedIn(connect))
		{
			try
			{
				sessionLogon.logon(connect);
			}
			catch (Exception logonException)
			{}
		}
		try
		{
			SearchPathMultipleObject accountsPath = new SearchPathMultipleObject();
			accountsPath.set_value("//account");
			SearchPathMultipleObject rolesPath = new SearchPathMultipleObject();
			rolesPath.set_value("//role");
			
			accounts =
				connection.getCMService().query(
						accountsPath,
					props,
					sortOptions,
					new QueryOptions());
			roles =
				connection.getCMService().query(
						rolesPath,
					props,
					sortOptions,
					new QueryOptions());
		}
		catch (java.rmi.RemoteException remoteEx)
		{
			System.out.println("Caught Remote Exception:\n");
			remoteEx.printStackTrace();
		}

		secObjectList = new BaseClassWrapper[accounts.length + roles.length];

		if ((accounts != null) && (accounts.length > 0))
		{
			for (accountIndex = 0; accountIndex < accounts.length; accountIndex++)
			{
				secObjectList[secObjectIndex++] = new BaseClassWrapper(accounts[accountIndex]);
			}
		}
		if ((roles != null) && (roles.length > 0))
		{
			for (roleIndex = 0; roleIndex < roles.length; roleIndex++)
			{
				secObjectList[secObjectIndex++] =
					new BaseClassWrapper(roles[roleIndex]);
			}
		}
		return secObjectList;
	}
		
	// Create the main method to execute the application.
	public static void main(String args[])
	{
		
		CRNConnect connection = new CRNConnect();
		connection.connectToCognosServer();
		sessionLogon = new Logon();
		String output = "";

		while (!Logon.loggedIn(connection))
		{
			output = sessionLogon.logon(connection);

			if (!Logon.loggedIn(connection))
			{
				int retry =
					JOptionPane.showConfirmDialog(
						null,
						"Login Failed. Please try again.",
						"Login Failed",
						JOptionPane.OK_CANCEL_OPTION);
				if (retry != JOptionPane.OK_OPTION)
				{
					System.exit(0);
				}
			}

		}

		CapabilitiesUI frame = new CapabilitiesUI("IBM Cognos Sample", connection);


		// Create a WindowAdapter so the application
		// is exited when the window is closed.
		frame.addWindowListener(new WindowAdapter()
		{
			public void windowClosing(WindowEvent e)
			{
				System.exit(0);
			}
		});

		frame.textAreaPane.setText(output);		

		// Set the size of the frame and display it.
		frame.setSize(680, 440);
		frame.setVisible(true);
		frame.setResizable(true);
	}

}
