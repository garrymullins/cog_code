/** 
Licensed Materials - Property of IBM

IBM Cognos Products: DOCS

(C) Copyright IBM Corp. 2005, 2008

US Government Users Restricted Rights - Use, duplication or disclosure restricted by GSA ADP Schedule Contract with
IBM Corp.
*/
/**
 * GroupsAndRolesUI.java
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


// This Java class extends the JFrame class so that you can display a window.
public class GroupsAndRolesUI extends JFrame
{
	private CRNConnect connect;

	// The following variables represent the dialog components.
	private JTextArea textAreaPane;
	private JTextField cmURL;
	private JButton cmdOption;
	private JComboBox cboOption;

	private static Logon sessionLogon;
	private static GroupsAndRoles groupHandler = new GroupsAndRoles();
	private static CSHandlers csHandler = new CSHandlers();

	private static String createGroup = "Create Group";
	private static String createRole = "Create Role";
	private static String deleteGroup = "Delete Group";
	private static String deleteRole = "Delete Role";
	private static String addUserToGroup = "Add User to Group";
	private static String addUserToRole = "Add User to Role";
	private static String removeFromGroup = "Remove from Group";
	private static String  removeFromRole = "Remove from Role";

	// This is the constructor.
	public GroupsAndRolesUI(String title, CRNConnect connection)
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

		JPanel mainPanel = new JPanel(new GridLayout(2, 0));
		// create a cmURL panel
		JPanel cmURLPanel = new JPanel();

		// Add the URL text field and label
		cmURL = new JTextField(CRNConnect.CM_URL.length() + 10);
		cmURL.setText(CRNConnect.CM_URL);
		cmURL.setEditable(false);
		cmURLPanel.add(new JLabel("Server URL:"), BorderLayout.WEST);
		cmURLPanel.add(cmURL, BorderLayout.EAST);

		// Create the buttonPanel
		JPanel buttonPanel = new JPanel();

		// Create and add the option combo box
		String cmdList[] =
			{	createGroup,
				createRole,
				deleteGroup,
				deleteRole,
				addUserToGroup,
				addUserToRole,
				removeFromGroup,
				removeFromRole };
		cboOption = new JComboBox(cmdList);
		buttonPanel.add(new JLabel("Select Option:"), BorderLayout.WEST);
		buttonPanel.add(cboOption, BorderLayout.CENTER);

		// Create and add the Button
		cmdOption = new JButton("Run Option");
		cmdOption.addActionListener(new allButtonsHandler());
		buttonPanel.add(cmdOption, BorderLayout.EAST);

		// Add the status text pane.
		textAreaPane = new JTextArea();

		// Add the panels to the mainPanel
		mainPanel.add(cmURLPanel);
		mainPanel.add(buttonPanel);
		mainPanel.add(textAreaPane);

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
		panel.add(mainPanel, BorderLayout.NORTH);
		panel.add(outputPanel);

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
					System.out.println(System.getProperties());
				}
				if (menuClicked.getText().compareTo("Overview") == 0)
				{
					JFrame explainWindow =
						new JFrame("Overview for Groups and Roles Sample");
					File explainFile = new File("Java_GroupsAndRolesGUI_Explain.html");
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

			if (buttonPressed == cmdOption)
			{
				String cboOptionText = (String)cboOption.getSelectedItem();
				GroupsAndRoles groupHandler = new GroupsAndRoles();
				try
				{
					String selectedNamespace =
						selectNamespace(connect);

					if (cboOptionText == "Create Group")
					{
						String theName =
							JOptionPane.showInputDialog(
								null,
								"Please enter the name of the group to create",
								"New Group",
								JOptionPane.INFORMATION_MESSAGE);
						if ((theName == null) || (theName.length() == 0))
						{
							output = "The group name is invalid.";
						}
						else
						{
							output = groupHandler.createGroup(connect, theName, selectedNamespace);
						}
					}
					else if (cboOptionText == "Create Role")
					{
						String theName =
							JOptionPane.showInputDialog(
								null,
								"Please enter the name of the role to create",
								"New Role",
								JOptionPane.INFORMATION_MESSAGE);
						if ((theName == null) || (theName.length() == 0))
						{
							output = "The role name is invalid.";
						}
						else
						{
							output = groupHandler.createRole(connect, theName, selectedNamespace);
						}
					}
					else if (cboOptionText == "Delete Group")
					{
						String groupSearchPath = selectGroup(connect, selectedNamespace);
						if ((groupSearchPath == null) || (groupSearchPath.length() == 0))
						{
							output = "Unable to select group in " + selectedNamespace + " .";
						}
						else
						{
							output = groupHandler.deleteGroup(connect, groupSearchPath);
						}
					}
					else if (cboOptionText == "Delete Role")
					{
						String roleSearchPath = selectRole(connect, selectedNamespace);
						if ((roleSearchPath == null) || (roleSearchPath.length() == 0))
						{
							output = "Unable to select role in " + selectedNamespace + " .";
						}
						else
						{
							output = groupHandler.deleteRole(connect, roleSearchPath);
						}
					}
					else if (cboOptionText == "Add User to Group")
					{
						String theName =
							Logon
								.getLogonAccount(connect)
								.getSearchPath()
								.getValue();

						String theGroup = selectGroup(connect, selectedNamespace);
						if ((theGroup == null) || (theGroup.length() == 0))
						{
							output = "The group name is invalid.";
						}
						else
						{
							output =
								groupHandler.addUserToGroup(
									connect,
									theName,
									theGroup);
						}
					}
					else if (cboOptionText == "Add User to Role")
					{
						String theName =
							Logon
								.getLogonAccount(connect)
								.getSearchPath()
								.getValue();

						String theRoleSearchPath = selectRole(connect, selectedNamespace);
						if ((theRoleSearchPath == null) || (theRoleSearchPath.length() == 0))
						{
							output = "Unable to select role in " + selectedNamespace + " .";
						}
						else
						{
							output =
								groupHandler.addUserToRole(
									connect,
									theName,
									theRoleSearchPath);
						}
					}
					else if (cboOptionText == "Remove from Group")
					{
						String groupSearchPath = selectGroup(connect, selectedNamespace);
						if ((groupSearchPath == null) || (groupSearchPath.length() == 0))
						{
							output = "Unable to select group in " + selectedNamespace + " .";
						}
						else
						{
							String userSearchPath = selectMemberInGroup(connect, groupSearchPath);
							if ((userSearchPath == null) || (userSearchPath.length() == 0))
							{
								output = "Unable to select member in " + groupSearchPath + " .";
							}
							else
							{
								output =
									groupHandler.deleteUserFromGroup(connect, groupSearchPath, userSearchPath);
							}
						}
					}
					else if (cboOptionText == "Remove from Role")
					{
						String role = selectRole(connect, selectedNamespace);
						String user = selectMemberInRole(connect, role);
						output =
							groupHandler.deleteUserFromRole(connect,role, user);
					}
					else
					{
						output = "Invalid option selected, please try again.";
					}
				}
				catch (java.rmi.RemoteException remoteEx)
				{
					remoteEx.printStackTrace();
					output = cboOptionText + ":\n\tAn Error occurred.";
				}
			}
			if (output.compareTo("") != 0)
			{
				textAreaPane.setText("");
				textAreaPane.append(output);
			}
		}
	}

	/**
	 * Displays a list of available members to select from.
	 *
	 * @param   connection     connection to server
	 * @param   selectedRole   Name of role to select a member from.
	 *
	 * @return           Search path to selected member
	 *
	 */
	public String selectMemberInRole(
		CRNConnect connection,
		String selectedRole)
		throws java.rmi.RemoteException
	{
		// Get the current role membership.
		BaseClass[] memberInfo =
			groupHandler.getAvailableMembers(connection, selectedRole);

		com.cognos.developer.schemas.bibus._3.Role role = (com.cognos.developer.schemas.bibus._3.Role)memberInfo[0];

		if (role.getMembers().getValue() == null)
		{
			return null;
		}

		String[] memberSearchPaths =
			new String[role.getMembers().getValue().length];
		String[] memberDefaultNames =
			new String[role.getMembers().getValue().length];

		BaseClass obj = null;
		for (int i = 0; i < role.getMembers().getValue().length; i++)
		{
			obj = role.getMembers().getValue()[i];

			BaseClass[] members =
				csHandler.queryObjectInCS(connection, obj.getSearchPath().getValue());
			memberSearchPaths[i] = members[0].getSearchPath().getValue();
			memberDefaultNames[i] = members[0].getDefaultName().getValue();
		}
		// Prompt the user to select a member.
		Object selectedValue =
			JOptionPane.showInputDialog(
				null,
				"Select a member from the " + selectedRole + " role",
				"Select Member from Role",
				JOptionPane.INFORMATION_MESSAGE,
				null,
				memberDefaultNames,
				memberDefaultNames[0]);

		if (selectedValue == null)
		{
			return null;
		}

		boolean found = false;
		int selectedMember = 0;
		while (!found && selectedMember < memberDefaultNames.length)
			for (int i = 0; i < memberDefaultNames.length; i++)
			{
				if (((String)selectedValue)
					.compareToIgnoreCase(memberDefaultNames[i])
					== 0)
				{
					return memberSearchPaths[i];
				}
			}
		return null;
	}

	/**
	 * Displays a list of available members to select from.
	 *
	 * @param   connection		connection to server.
	 * 			selectedGroup	group from which to show members
	 *
	 * @return           		search path for selected member
	 *
	 */
	public String selectMemberInGroup(
		CRNConnect connection,
		String selectedGroup)
		throws java.rmi.RemoteException
	{
		// Get the current group membership.
		BaseClass[] memberInfo =
			groupHandler.getAvailableMembers(connection, selectedGroup);

		com.cognos.developer.schemas.bibus._3.Group group = (com.cognos.developer.schemas.bibus._3.Group)memberInfo[0];

		if (group.getMembers().getValue() == null)
		{
			return null;
		}

		String[] memberSearchPaths =
			new String[group.getMembers().getValue().length];
		String[] memberDefaultNames =
			new String[group.getMembers().getValue().length];

		BaseClass obj = null;
		for (int i = 0; i < group.getMembers().getValue().length; i++)
		{
			obj = group.getMembers().getValue()[i];

			BaseClass[] members =
				csHandler.queryObjectInCS(connection, obj.getSearchPath().getValue());
			memberSearchPaths[i] = members[0].getSearchPath().getValue();
			memberDefaultNames[i] = members[0].getDefaultName().getValue();
		}
		// Prompt the user to select a member.
		Object selectedValue =
			JOptionPane.showInputDialog(
				null,
				"Select a member from the "
					+ selectedGroup
					+ " group",
				"Select Member from Group",
				JOptionPane.INFORMATION_MESSAGE,
				null,
				memberDefaultNames,
				memberDefaultNames[0]);
		if (selectedValue == null)
		{
			return null;
		}

		for (int i = 0; i < memberDefaultNames.length; i++)
		{
			if (((String)selectedValue)
				.compareToIgnoreCase(memberDefaultNames[i])
				== 0)
			{
				return memberSearchPaths[i];
			}
		}

		return null;
	}

	/**
	 * Displays a list of available groups to select from.
	 *
	 * @param   connection     		connection to server
	 * @param   selectedNamespace 	Namespace from which to select a group
	 *
	 */
	public String selectGroup(
		CRNConnect connection,
		String selectedNamespace)
		throws java.rmi.RemoteException
	{
		BaseClass[] groupInfo =
			groupHandler.getAvailableGroups(connection, selectedNamespace);
		String[] groupSearchPath = new String[groupInfo.length];
		String[] groupDefaultName = new String[groupInfo.length];

		for (int i = 0; i < groupInfo.length; i++)
		{
			groupSearchPath[i] = groupInfo[i].getSearchPath().getValue();
			groupDefaultName[i] = groupInfo[i].getDefaultName().getValue();
		}

		Object selectedValue = null;
		// Prompt the user to select a group or role.
		selectedValue =
			JOptionPane.showInputDialog(
				null,
				"Please select a group",
				"Select a Group",
				JOptionPane.INFORMATION_MESSAGE,
				null,
				groupDefaultName,
				groupDefaultName[0]);
		if (selectedValue == null)
		{
			return null;
		}

		for (int i = 0; i < groupDefaultName.length; i++)
		{
			if (((String)selectedValue)
				.compareToIgnoreCase(groupDefaultName[i])
				== 0)
			{
				return groupSearchPath[i];
			}

		}
		return null;
	}

	/**
	 * Displays a list of available roles to select from.
	 *
	 * @param   connection     		connection to server
	 * @param   selectedNamespace 	Namespace from which to select a role
	 *
	 */
	public String selectRole(
		CRNConnect connection,
		String selectedNamespace)
		throws java.rmi.RemoteException
	{
		BaseClass[] roleInfo =
			groupHandler.getAvailableRoles(connection, selectedNamespace);
		if (roleInfo.length == 0)
		{
			return null;
		}

		String[] roleSearchPath = new String[roleInfo.length];
		String[] roleDefaultName = new String[roleInfo.length];

		for (int i = 0; i < roleInfo.length; i++)
		{
			roleSearchPath[i] = roleInfo[i].getSearchPath().getValue();
			roleDefaultName[i] = roleInfo[i].getDefaultName().getValue();
		}

		Object selectedValue = null;
		// Prompt the user to select a group or role.
		selectedValue =
			JOptionPane.showInputDialog(
				null,
				"Please select a role",
				"Select a Role",
				JOptionPane.INFORMATION_MESSAGE,
				null,
				roleDefaultName,
				roleDefaultName[0]);
		if (selectedValue == null)
		{
			return null;
		}

		for (int i = 0; i < roleDefaultName.length; i++)
		{
			if (((String)selectedValue).compareToIgnoreCase(roleDefaultName[i])
				== 0)
			{
				return roleSearchPath[i];
			}
		}
		return null;
	}

	/**
	 * Displays a list of available namespaces to select from.
	 *
	 * @param   connection     		connection to server
	 *
	 */
	public String selectNamespace(CRNConnect connection)
		throws java.rmi.RemoteException
	{
		BaseClass[] namespaceInfo = groupHandler.getAvailableNamespaces(connection);
		String[] namespaceSearchPath = new String[namespaceInfo.length];
		String[] namespaceDefaultName = new String[namespaceInfo.length];

		for (int i = 0; i < namespaceInfo.length; i++)
		{
			namespaceSearchPath[i] =
				namespaceInfo[i].getSearchPath().getValue();
			namespaceDefaultName[i] =
				namespaceInfo[i].getDefaultName().getValue();
		}

		Object selectedValue = null;
		// Prompt the user to select a namespace.
		selectedValue =
			JOptionPane.showInputDialog(
				null,
				"Please select a namespace",
				"Select Namespace",
				JOptionPane.INFORMATION_MESSAGE,
				null,
				namespaceDefaultName,
				namespaceDefaultName[0]);
		if (selectedValue == null)
		{
			return null;
		}

		for (int i = 0; i < namespaceDefaultName.length; i++)
		{
			if (((String)selectedValue)
				.compareToIgnoreCase(namespaceDefaultName[i])
				== 0)
			{
				return namespaceSearchPath[i];
			}
		}
		return null;
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

		GroupsAndRolesUI frame = new GroupsAndRolesUI("IBM Cognos Sample", connection);
		frame.textAreaPane.setText(output);

		// Create a WindowAdapter so the application
		// is exited when the window is closed.
		frame.addWindowListener(new WindowAdapter()
		{
			public void windowClosing(WindowEvent e)
			{
				System.exit(0);
			}
		});

		// Set the size of the frame and display it.
		frame.setSize(750, 440);
		frame.setVisible(true);
		frame.setResizable(true);
	}

}
