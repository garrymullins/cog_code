/** 
Licensed Materials - Property of IBM

IBM Cognos Products: DOCS

(C) Copyright IBM Corp. 2005, 2008

US Government Users Restricted Rights - Use, duplication or disclosure restricted by GSA ADP Schedule Contract with
IBM Corp.
*/
/**
 * DispatcherUI.java
 *
 * Copyright (C) 2008 Cognos ULC, an IBM Company. All rights reserved.
 * Cognos (R) is a trademark of Cognos ULC, (formerly Cognos Incorporated).
 *
 */

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
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

import com.cognos.developer.schemas.bibus._3.Dispatcher_Type;

public class DispatcherUI extends JFrame {

	private CRNConnect connect;

	private static DispatcherUI frame;

	// The following variables represent the dialog components.
	private JTextArea textAreaPane;

	private JTextField cmURL;

	private JComboBox dispSearchPath;

	private JButton runButton;

	private JComboBox dispatcherOptions;

	private static Logon sessionLogon;

	private static final String DISPATCHER_PING = "Ping Dispatcher";

	private static final String DISPATCHER_PROPERTY = "Dispatcher Properties";

	private static final String DISPATCHER_START_SERVICE = "Start Service";

	private static final String DISPATCHER_STOP_SERVICE = "Stop Service";

	private static final String DISPATCHER_SET_LOGGING_LEVEL = "Set Logging Level";

	private static final String DISPATCHER_SET_MAX_PROCESSES = "Set Maximum Process";

	private static final int DISPATCHER_ENUM_PING = 1;

	private static final int DISPATCHER_ENUM_PROPERTY = 2;

	private static final int DISPATCHER_ENUM_START_SERVICE = 3;

	private static final int DISPATCHER_ENUM_STOP_SERVICE = 4;

	private static final int DISPATCHER_ENUM_SET_LOGGING_LEVEL = 5;

	private static final int DISPATCHER_ENUM_SET_MAX_PROCESSES = 6;

	private static int dispatcherType = 0;

	private String chosenDispMethod = null;

	public String DispSearchPath_URL = null;

	Dispatcher newDispatcher = new Dispatcher();

	// This is the constructor.
	public DispatcherUI(String title, CRNConnect connection) {
		// Set the title of the frame, even before the variables are declared.
		super(title);
		connect = connection;
		addComponents();
	}

	// Add all components to the frame's panel.
	private void addComponents() {
		JMenuBar mBar = new JMenuBar();
		this.setJMenuBar(mBar);

		// declare menuItems
		JMenuItem exit;
		JMenuItem about;
		JMenuItem overview;

		// Add and populate the File menu.
		JMenu fileMenu = new JMenu("File");
		mBar.add(fileMenu);

		exit = new JMenuItem("Exit");
		fileMenu.add(exit);
		exit.addActionListener(new MenuHandler());

		// Add and populate the Help menu.
		JMenu helpMenu = new JMenu("Help");
		mBar.add(helpMenu);

		about = new JMenuItem("About");
		helpMenu.add(about);
		about.addActionListener(new MenuHandler());

		overview = new JMenuItem("Overview");
		helpMenu.add(overview);
		overview.addActionListener(new MenuHandler());

		JPanel mainPanel = createMainPanel();
		JPanel outputPanel = createOutputPanel();
		JPanel panel = new JPanel(new BorderLayout());
		panel.add(mainPanel, BorderLayout.NORTH);
		panel.add(outputPanel);

		setContentPane(panel);
	}

	private JPanel createOutputPanel() {
		// Add the status text pane.
		textAreaPane = new JTextArea();

		// Add the ScrollPane to outputPanel
		JScrollPane areaScrollPane = new JScrollPane(textAreaPane);
		areaScrollPane
				.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		areaScrollPane.setPreferredSize(new Dimension(500, 275));

		//Create the output panel and it's layout objects
		GridBagLayout layout = new GridBagLayout(); //GridLayout(0, 1)
		JPanel outputNavPanel = new JPanel(layout);
		GridBagConstraints layoutConstraints = new GridBagConstraints();

		//Set the layout for the scroll pane and add it
		layoutConstraints.weightx = 1.0;
		layoutConstraints.weighty = 1.0;
		layoutConstraints.fill = GridBagConstraints.BOTH;
		layout.setConstraints(areaScrollPane, layoutConstraints);
		outputNavPanel.add(areaScrollPane);

		outputNavPanel.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createCompoundBorder(BorderFactory
						.createTitledBorder("Output"), BorderFactory
						.createEmptyBorder(5, 5, 5, 5)), outputNavPanel
						.getBorder()));

		return outputNavPanel;
	}

	private JPanel createMainPanel() {
		// Add the URL text field and label
		cmURL = new JTextField(CRNConnect.CM_URL.length() + 10);
		cmURL.setText(CRNConnect.CM_URL);
		cmURL.setEditable(false);

		// Put together a panel for the URl
		JPanel cmURLPanel = new JPanel();
		cmURLPanel.add(new JLabel("Server URL:"));
		cmURLPanel.add(cmURL);

		String[] dispatcherList = newDispatcher.getAvailableDispatcher(connect,
				"//dispatcher");
		dispSearchPath = new JComboBox(dispatcherList);
		dispSearchPath.setSelectedItem(dispatcherList[0]);
		dispSearchPath.addActionListener(new dispatcherSearchPathHandler());

		// create dispatcher search path panel
		JPanel dispatcherPanel = new JPanel();
		dispatcherPanel.add(new JLabel("Dispatcher Search Path:"),
				BorderLayout.WEST);
		dispatcherPanel.add(dispSearchPath, BorderLayout.EAST);

		//get dropdown box and button panel
		JPanel optionButtonPanel = createMainButtonPanel();

		//create the main panel and add the components
		JPanel mainPanel = new JPanel(new GridLayout(3, 0));
		// Add the panels to the mainPanel
		mainPanel.add(cmURLPanel);
		mainPanel.add(dispatcherPanel);
		mainPanel.add(optionButtonPanel);

		return mainPanel;
	}

	private JPanel createMainButtonPanel() {
		// Create the button Panel
		JPanel buttonPanel = new JPanel();

		// Create and add the package output type combo box
		String[] dispOptions = { DISPATCHER_PING, DISPATCHER_PROPERTY,
				DISPATCHER_START_SERVICE, DISPATCHER_STOP_SERVICE,
				DISPATCHER_SET_LOGGING_LEVEL, DISPATCHER_SET_MAX_PROCESSES };
		dispatcherOptions = new JComboBox(dispOptions);
		dispatcherOptions.setSelectedItem(null);
		dispatcherOptions.addActionListener(new dispatcherSelectionHandler());
		buttonPanel.add(new JLabel("Dispatcher Options:"), BorderLayout.WEST);
		buttonPanel.add(dispatcherOptions, BorderLayout.WEST);

		// Create and add the Button
		runButton = new JButton("Run Option");
		runButton.addActionListener(new allButtonsHandler());
		buttonPanel.add(runButton, BorderLayout.EAST);

		return buttonPanel;
	}

	// handle menu bar buttons
	private class MenuHandler implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			if (e.getActionCommand().startsWith("http://")) {
				connect.connectionChange(e.getActionCommand());
			}
			try {
				JMenuItem menuClicked = (JMenuItem) e.getSource();
				if (menuClicked.getText() == "Exit") {
					System.exit(0);
				}
				if (menuClicked.getText() == "About") {
					JOptionPane.showMessageDialog(((JMenuItem) e.getSource())
							.getParent(), "IBM Cognos Sample Application\n\n"
							+ "Version 1.0.0\n"
							+ "This application uses the IBM Cognos Software Development Kit",
							"About IBM Cognos Samples",
							JOptionPane.INFORMATION_MESSAGE, new ImageIcon(
									"../Common/about.gif"));
				}
				if (menuClicked.getText().compareTo("Overview") == 0) {
					JFrame explainWindow = new JFrame(
							"Overview for Dispatcher Sample");
					File explainFile = new File(
							"Java_DispatcherUI_Explain.html");
					if (!explainFile.exists()) {
						JOptionPane.showMessageDialog(null,
								"Explain file not found");
						return;
					}
					URL explainURL = new URL("file:///"
							+ explainFile.getAbsolutePath());
					JEditorPane explainPane = new JEditorPane();
					explainPane.setPage(explainURL);
					explainPane.setEditable(false);

					JScrollPane explainScroll = new JScrollPane(explainPane,
							JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
							JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
					explainWindow.getContentPane().add(explainScroll);
					explainWindow.setSize(640, 480);
					explainWindow.setVisible(true);
				}
			} catch (Exception ex) {
			}
		}
	}

	// The following is the button event handler.
	private class allButtonsHandler implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			if (!Logon.loggedIn(connect)) {
				try {
					sessionLogon.logon(connect);
				} catch (Exception logonException) {
					System.out.println("The error: "
							+ logonException.getMessage());
				}
			}

			JButton buttonPressed = ((JButton) e.getSource());
			// Click 'Run Option' button
			if (buttonPressed == runButton) {

				textAreaPane.setText("");
				DispSearchPath_URL = (String) dispSearchPath.getSelectedItem();
				if (DispSearchPath_URL == null || dispatcherType == 0) {
					return;
				}

				textAreaPane.setText("");

				switch (dispatcherType) {
				case 1: // Ping Dispatcher
					String dispatcherVersion = newDispatcher
							.getDispatcherVersion(connect, DispSearchPath_URL);
					dispatcherOptions.setSelectedItem(null);
					if (dispatcherVersion != null)
						textAreaPane.setText("The Dispatcher version is: "
								+ dispatcherVersion);
					else
						textAreaPane
								.setText("Failed to access and run a dispatcher."
										+ "\nPlease check to make sure the dispatcher search path is valid.");
					dispatcherType = 0;
					break;
				case 2: // Dispatcher status
					Dispatcher_Type dispatcherStatus = newDispatcher
							.getDispatcherStatus(connect, DispSearchPath_URL);
					dispatcherOptions.setSelectedItem(null);
					if (dispatcherStatus != null) {
						textAreaPane
								.setText("The following lists some of Dispatcher properties: ");
						textAreaPane
								.append("\n\nThe Dispatcher search path:   "
										+ dispatcherStatus.getSearchPath()
												.getValue()
										+ "\nThe number of connections that can use to execute high-affinity requests:   "
										+ dispatcherStatus
												.getRsAffineConnections()
												.getValue()
										+ "\nThe maximum number of processes for the Batch Report Service:   "
										+ dispatcherStatus
												.getBrsMaximumProcesses()
												.getValue()
										+ "\nThe Dispatcher path:   "
										+ dispatcherStatus.getDispatcherPath()
												.getValue());
					} else {
						textAreaPane
								.setText("Failed to get Dispatcher status."
										+ "\nPlease check to make sure the dispatcher search path is valid.");
					}
					dispatcherType = 0;
					break;
				case 3: // Start Service
					String mySearchPath = null;
					BaseClassWrapper[] serviceList = newDispatcher.getService(
							connect, "//dispatcher/*");

					// the Service list
					Object selectedService = JOptionPane.showInputDialog(null,
							"Select the service", null,
							JOptionPane.INFORMATION_MESSAGE, null, serviceList,
							null);

					if (selectedService != null) {
						BaseClassWrapper serviceName = (BaseClassWrapper) selectedService;

						mySearchPath = serviceName.getBaseClassObject()
								.getSearchPath().getValue();

						boolean startService = newDispatcher.startService(
								connect, mySearchPath);
						dispatcherOptions.setSelectedItem(null);
						if (startService) {
							textAreaPane.setText("The "
									+ serviceName.toString()
									+ " has been successfully started.");
						} else {
							textAreaPane.setText(serviceName.toString()
									+ " could not be started.");
						}
						dispatcherType = 0;
					}
					break;
				case 4: // Stop Service
					String mySearchPath1 = null;
					BaseClassWrapper[] serviceList1 = newDispatcher.getService(
							connect, "//dispatcher/*");

					// the Service list
					Object selectedService1 = JOptionPane.showInputDialog(null,
							"Select the service", null,
							JOptionPane.INFORMATION_MESSAGE, null,
							serviceList1, null);

					if (selectedService1 != null) {
						BaseClassWrapper serviceName1 = (BaseClassWrapper) selectedService1;

						mySearchPath1 = serviceName1.getBaseClassObject()
								.getSearchPath().getValue();

						boolean stopService = newDispatcher.stopService(
								connect, mySearchPath1);
						dispatcherOptions.setSelectedItem(null);
						if (stopService) {
							textAreaPane.setText("The "
									+ serviceName1.toString()
									+ " has been successfully stopped.");
						} else {
							textAreaPane.setText(serviceName1.toString()
									+ " could not be stopped.");
						}
						dispatcherType = 0;
					}
					break;
				case 5: // Set Logging Level
					String setLoggingResult = null;

					String[] myServices = { "AgentService",
							"BatchReportService", "ContentManagerService",
							"DataIntegrationService", "dispatcher",
							"DeliveryService", "EventManagementService",
							"IndexDataService", "IndexSearchService",
							"IndexUpdateService", "JobService",
							"MobileService", "MetadataService",
							"MetricsManagerService", "MonitorService",
							"PlanningAdministrationConsoleService",
							"PlanningRuntimeService", "PresentationService",
							"PlanningTaskService", "reportDataService",
							"ReportService", "SystemService" };

					Object selectedService2 = JOptionPane.showInputDialog(null,
							"Please select the service",
							"Services for setting logging level",
							JOptionPane.INFORMATION_MESSAGE, null, myServices,
							null);

					if (selectedService2 != null) {
						String myLoggingLevel;
						String myServiceName = (String) selectedService2;

						Object[] levelOptions = { "Minimal", "Basic",
								"Request", "Trace", "Full" };
						Object selectedLevel = JOptionPane.showInputDialog(
								null, "Please choose setting level",
								"Setting Level",
								JOptionPane.INFORMATION_MESSAGE, null,
								levelOptions, null);

						if (selectedLevel == null) {
							return;
						} else {
							myLoggingLevel = (String) selectedLevel;
						}

						if (myLoggingLevel != null) {
							// setting logging level as 'basic' level
							setLoggingResult = newDispatcher.setLoggingLevel(
									connect, DispSearchPath_URL, myServiceName,
									myLoggingLevel);
						}
						dispatcherOptions.setSelectedItem(null);
						if (setLoggingResult != null) {
							textAreaPane.setText("The logging level for "
									+ myServiceName
									+ " has been successfully set to '"
									+ myLoggingLevel + "' level"
									+ "\nThe Event ID:  " + setLoggingResult);
						} else {
							textAreaPane
									.setText("Failed to set logging level for "
											+ myServiceName
											+ "."
											+ "\nPlease check to make sure the dispatcher search path is valid.");
						}
						dispatcherType = 0;
					}
					break;
				case 6: // Set Maximum Processes
					String maxProcessStr = null;

					String[] availableServices = { "BatchReportService",
							"ReportService" };

					// the Service list
					Object chosenService = JOptionPane.showInputDialog(null,
							"Please select the service",
							"Services for setting max processes",
							JOptionPane.INFORMATION_MESSAGE, null,
							availableServices, null);

					if (chosenService != null) {
						String serviceName = (String) chosenService;

						Object[] numList = { "2", "3", "4", "5" };

						Object selectedMaxProcessNum = JOptionPane
								.showInputDialog(null,
										"Please choose the maximum process",
										"Setting maximum processes",
										JOptionPane.INFORMATION_MESSAGE, null,
										numList, null);

						maxProcessStr = (String) selectedMaxProcessNum;
						if (maxProcessStr != null) {
							// setting the maximum number of processes
							String setMaxProcesses = newDispatcher
									.setMaxProcesses(connect,
											DispSearchPath_URL, serviceName,
											maxProcessStr);
							dispatcherOptions.setSelectedItem(null);
							if (setMaxProcesses != null) {
								textAreaPane
										.setText("The maximum number of processes for the "
												+ serviceName
												+ " is successfully set to "
												+ maxProcessStr
												+ "\nThe Event ID:  "
												+ setMaxProcesses);
							} else {
								textAreaPane
										.setText("Failed to set maximum number of processesfor the Batch Report Service."
												+ "\nPlease check to make sure the dispatcher search path is valid.");
							}
						} else {
							return;
						}
						dispatcherType = 0;
					}
					break;
				}
			}
		}
	}

	// This is the the available dispatcher combo box event handler
	private class dispatcherSearchPathHandler implements ActionListener {
		public void actionPerformed(ActionEvent dispatcherSearchPathEvent) {
			textAreaPane.setText("");
			DispSearchPath_URL = (String) dispSearchPath.getSelectedItem();
		}
	}

	// This is the dispatcher option combo box event handler.
	private class dispatcherSelectionHandler implements ActionListener {
		public void actionPerformed(ActionEvent dispatcherSelectedEvent) {
			textAreaPane.setText("");
			chosenDispMethod = (String) dispatcherOptions.getSelectedItem();
			if (chosenDispMethod == DISPATCHER_PING) {
				dispatcherType = DISPATCHER_ENUM_PING;
			} else if (chosenDispMethod == DISPATCHER_PROPERTY) {
				dispatcherType = DISPATCHER_ENUM_PROPERTY;
			} else if (chosenDispMethod == DISPATCHER_START_SERVICE) {
				dispatcherType = DISPATCHER_ENUM_START_SERVICE;
			} else if (chosenDispMethod == DISPATCHER_STOP_SERVICE) {
				dispatcherType = DISPATCHER_ENUM_STOP_SERVICE;
			} else if (chosenDispMethod == DISPATCHER_SET_LOGGING_LEVEL) {
				dispatcherType = DISPATCHER_ENUM_SET_LOGGING_LEVEL;
			} else if (chosenDispMethod == DISPATCHER_SET_MAX_PROCESSES) {
				dispatcherType = DISPATCHER_ENUM_SET_MAX_PROCESSES;
			}
		}
	}

	// Create the main method to execute the application.
	public static void main(String args[]) {
		CRNConnect connection = new CRNConnect();
		connection.connectToCognosServer();

		sessionLogon = new Logon();
		String logonOutput = "";

		while (!Logon.loggedIn(connection)) {
			logonOutput = sessionLogon.logon(connection);

			if (!Logon.loggedIn(connection)) {
				int retry = JOptionPane.showConfirmDialog(null,
						"Login Failed. Please try again.", "Login Failed",
						JOptionPane.OK_CANCEL_OPTION);
				if (retry != JOptionPane.OK_OPTION) {
					System.exit(0);
				}
			}
		}

		frame = new DispatcherUI("IBM Cognos Sample", connection);

		// Create a WindowAdapter so the application
		// is exited when the window is closed.
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});

		frame.textAreaPane.setText(logonOutput);

		// Set the size of the frame and display it.
		frame.setSize(750, 440);
		frame.setVisible(true);
		frame.setResizable(true);
	}
}
