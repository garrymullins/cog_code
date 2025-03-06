/** 
Licensed Materials - Property of IBM

IBM Cognos Products: DOCS

(C) Copyright IBM Corp. 2005, 2008

US Government Users Restricted Rights - Use, duplication or disclosure restricted by GSA ADP Schedule Contract with
IBM Corp.
*/
/**
 * AlertsUI.java
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
import javax.swing.JEditorPane;
import javax.swing.JComboBox;
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

// This Java class extends the JFrame class so that you can
// display a window.
public class AlertsUI extends JFrame
{
	private CRNConnect connect;
	private CSHandlers csh = new CSHandlers();

	// The following variables represent the dialog components.
	private JTextArea textAreaPane;
	private JTextField cmURL;
	private JButton notificationButton;
	private JComboBox actionTypeOption;
	private JComboBox reportSelectOption;

	private static Logon sessionLogon;
	private static final int REP_TYPE_ENUM_HTML = 0;

	private static int reportType = 0;

	private static final String ADD_NOTIFICATION = "Add Notification";
	private static final String DELETE_NOTIFICATION =
		"Delete Notification";
	private static final String CLEAR_NOTIFICATION = "Clear Notifications";
	private static final String DELETE_ALL_NOTIFICATION = "Delete All Notifications";
	private static final String QUERY_NOTIFICATION = "Query Notification";

	private static final int ADD_USER_TO_NOTIFICATION_LIST = 1;
	private static final int REMOVE_USER_FROM_NOTIFICATION_LIST = 2;
	private static final int REMOVE_USER_FROM_ALL_LISTS = 3;
	private static final int CLEAR_ALL_NOTIFICATIONS = 4;
	private static final int QUERY_NOTIFICATION_LIST = 5;
	private static int actionType = 1;

	private static BaseClassWrapper selectedReport = null;

	// This is the constructor.
	public AlertsUI(String title, CRNConnect connection)
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

		// Create the button Panel
		JPanel buttonPanel = new JPanel();

		//Create and add the report output type combo box
		String requestTypes[] = { ADD_NOTIFICATION, DELETE_NOTIFICATION, DELETE_ALL_NOTIFICATION, CLEAR_NOTIFICATION, QUERY_NOTIFICATION};
		actionTypeOption = new JComboBox(requestTypes);
		actionTypeOption.setSelectedItem(null);
		actionTypeOption.addActionListener(new AlertOptionSelectionHandler());
		buttonPanel.add(actionTypeOption, BorderLayout.WEST);

		// Create and add the select report combo box
		BaseClassWrapper listOfReports[] = getListOfReports(connect);
		reportSelectOption = new JComboBox(listOfReports);
		reportSelectOption.setSelectedItem(null);
		reportSelectOption.addActionListener(new ReportSelectionHandler());
		buttonPanel.add(reportSelectOption, BorderLayout.CENTER);

		// Create and add the Button
		notificationButton = new JButton("Send Request");
		notificationButton.addActionListener(new allButtonsHandler());
		buttonPanel.add(notificationButton, BorderLayout.EAST);

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
				}
				if (menuClicked.getText().compareTo("Overview") == 0)
				{
					JFrame explainWindow =
						new JFrame("Overview for Alerts Sample");
					File explainFile = new File("Java_AlertsUI_Explain.html");
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

			if (buttonPressed == notificationButton)
			{
				ManageAlerts alertManager = new ManageAlerts();

				try
				{

					switch (actionType){
						
						case ADD_USER_TO_NOTIFICATION_LIST:
							output = alertManager.addNotificationForUser(connect, selectedReport);
							break;
						
						case REMOVE_USER_FROM_NOTIFICATION_LIST :
							output = alertManager.deleteSingleNotification(connect, selectedReport);
							break;
							
						case REMOVE_USER_FROM_ALL_LISTS :
							output = alertManager.deleteAllNotifications(connect);
							break;
							
						case CLEAR_ALL_NOTIFICATIONS :
							output = alertManager.clearNotifications(connect, selectedReport);
							break;
							
						case QUERY_NOTIFICATION_LIST :
							output = alertManager.queryNotification(connect, selectedReport);
							
					}
				}
				catch (Exception ex)
				{
					System.out.println(ex.getMessage());
					output =
						"An error occurred.\nMake sure a "
							+ "Report Name is selected and IBM Cognos is running";
				}
			}
			if (output.compareTo("") != 0)
			{
				textAreaPane.setText("");
				textAreaPane.append(output);
			}
		}
	}

	// This is the actionType combo box event handler.
	private class AlertOptionSelectionHandler implements ActionListener
	{
		public void actionPerformed(ActionEvent alertOptionSelectedEvent)
		{
			String chosenType = (String)actionTypeOption.getSelectedItem();
			if (chosenType == ADD_NOTIFICATION)
			{
				actionType = ADD_USER_TO_NOTIFICATION_LIST;
			}
			else if (chosenType == DELETE_NOTIFICATION)
			{
				actionType = REMOVE_USER_FROM_NOTIFICATION_LIST;
			}
			else if (chosenType == DELETE_ALL_NOTIFICATION)
			{
				actionType = REMOVE_USER_FROM_ALL_LISTS;
			}
			else if (chosenType == CLEAR_NOTIFICATION)
			{
				actionType = CLEAR_ALL_NOTIFICATIONS;
			}
			else if (chosenType == QUERY_NOTIFICATION)
			{
				actionType = QUERY_NOTIFICATION_LIST ;
			}
			else
			{
				//error, default to query notification
				actionType = QUERY_NOTIFICATION_LIST;
			}
		}
	}

	private class ReportSelectionHandler implements ActionListener
	{
		public void actionPerformed(ActionEvent repSelectedEvent)
		{
			selectedReport = (BaseClassWrapper)reportSelectOption.getSelectedItem();
		}
	}

	//This is a method for retrieving a list of the available reports for which to manage alerts
	protected BaseClassWrapper[] getListOfReports(CRNConnect connection)
	{
		BaseClassWrapper reportList[] = null;
		BaseClass reports[] = new BaseClass[0];
		int reportIndex = 0;

		if (connection.getCMService() == null)
		{
			System.out.println(
				"Invalid parameter passed to getListOfReports()\n");
			return null;
		}

		PropEnum props[] =
			new PropEnum[] { PropEnum.searchPath, PropEnum.defaultName };
		Sort sortOptions[] = { new Sort()};
		sortOptions[0].setOrder(OrderEnum.ascending);
		sortOptions[0].setPropName(PropEnum.defaultName);

		if (!Logon.loggedIn(connection))
		{
			try
			{
				sessionLogon.logon(connection);
			}
			catch (Exception logonException)
			{}
		}
		try
		{
			reports =
				connection.getCMService().query(
					new SearchPathMultipleObject("/content//report"),
					props,
					sortOptions,
					new QueryOptions());
		}
		catch (java.rmi.RemoteException remoteEx)
		{
			System.out.println("Caught Remote Exception:\n");
			remoteEx.printStackTrace();
		}

		reportList = new BaseClassWrapper[reports.length];

		if ((reports != null) && (reports.length > 0))
		{
			for (reportIndex = 0; reportIndex < reports.length; reportIndex++)
			{
				reportList[reportIndex] = new BaseClassWrapper(reports[reportIndex]);
			}
		}
		return reportList;
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

		AlertsUI frame = new AlertsUI("IBM Cognos Sample", connection);

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
		frame.setSize(850, 440);
		frame.setVisible(true);
		frame.setResizable(true);
	}

}
