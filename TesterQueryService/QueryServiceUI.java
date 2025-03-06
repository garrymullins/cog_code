/** 
Licensed Materials - Property of IBM

IBM Cognos Products: DOCS

(C) Copyright IBM Corp. 2011

US Government Users Restricted Rights - Use, duplication or disclosure restricted by GSA ADP Schedule Contract with
IBM Corp.
*/
/**
 * QueryServiceUI.java
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

public class QueryServiceUI extends JFrame {

	private CRNConnect connect;

	private static QueryServiceUI frame;

	// The following variables represent the dialog components.
	private JTextArea textAreaPane;

	private JTextField cmURL;

	private JComboBox cubeSelection;

	private JButton runButton;

	private JComboBox queryServiceOptions;

	private static Logon sessionLogon;

	private static final String CUBE_GET_STATE = "Get Cube State";

	private static final String CUBE_START = "Start Cube";

	private static final String CUBE_STOP  = "Stop Cube When Not In Use";

	private static final String CUBE_STOP_IMMEDIATE  = "Stop Cube Immediately";

	private static final String CUBE_RESTART = "Restart Cube";

	private static final String CUBE_REFRESH_DATA = "Refresh Data Cache";

	private static final String CUBE_REFRESH_MEMBERS = "Refresh Member Cache";

	private static final String CUBE_REFRESH_SECURITY = "Refresh Cube Security";

	private static final int CUBE_ENUM_STATE = 1;

	private static final int CUBE_ENUM_START = 2;

	private static final int CUBE_ENUM_STOP = 3;

	private static final int CUBE_ENUM_STOP_IMMEDIATE = 4;

	private static final int CUBE_ENUM_RESTART = 5;

	private static final int CUBE_ENUM_DATA = 6;

	private static final int CUBE_ENUM_MEMBERS = 7;

	private static final int CUBE_ENUM_SECURITY = 8;

	private static int cubeActionType = 0;

	private BaseClassWrapper selectedCube = null;
	
	private String chosenMethod = null;

	public String cubeSearchPath_URL = null;

	QueryServiceTester newQSTester = new QueryServiceTester();

	// This is the constructor.
	public QueryServiceUI(String title, CRNConnect connection) {
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

		//Create the output panel and its layout objects
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

		BaseClassWrapper[] cubeList = newQSTester.getCubes(connect,
				"//rolapDataSource");
		cubeSelection = new JComboBox(cubeList);
		cubeSelection.setSelectedItem(cubeList[0]);
		cubeSelection.addActionListener(new CubeSearchPathHandler());

		// create cube search path panel
		JPanel cubePanel = new JPanel();
		cubePanel.add(new JLabel("Cube:"),
				BorderLayout.WEST);
		cubePanel.add(cubeSelection, BorderLayout.EAST);

		//get dropdown box and button panel
		JPanel optionButtonPanel = createMainButtonPanel();

		//create the main panel and add the components
		JPanel mainPanel = new JPanel(new GridLayout(3, 0));
		// Add the panels to the mainPanel
		mainPanel.add(cmURLPanel);
		mainPanel.add(cubePanel);
		mainPanel.add(optionButtonPanel);

		return mainPanel;
	}

	private JPanel createMainButtonPanel() {
		// Create the button Panel
		JPanel buttonPanel = new JPanel();

		// Create and add the package output type combo box
		String[] cubeOptions = { CUBE_GET_STATE, CUBE_START,
				CUBE_STOP, CUBE_STOP_IMMEDIATE, CUBE_RESTART,
				CUBE_REFRESH_DATA, CUBE_REFRESH_MEMBERS, CUBE_REFRESH_SECURITY };
		queryServiceOptions = new JComboBox(cubeOptions);
		queryServiceOptions.setSelectedItem(null);
		queryServiceOptions.addActionListener(new CubeSelectionHandler());
		buttonPanel.add(new JLabel("Query Service Actions:"), BorderLayout.WEST);
		buttonPanel.add(queryServiceOptions, BorderLayout.WEST);

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
							"Overview for Query Service Sample");
					File explainFile = new File(
							"Java_QueryServiceUI_Explain.html");
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
				
				BaseClassWrapper selectedCube =
					(BaseClassWrapper)cubeSelection.getSelectedItem();
				
				cubeSearchPath_URL = selectedCube.getSearchPath() ;
				if (cubeSearchPath_URL == null || cubeActionType == 0) {
					return;
				}

				textAreaPane.setText("");

				switch (cubeActionType) {
				
				case 1: // Get Cube State
					
					String cubeStateInfo = newQSTester.getCubeState(connect, selectedCube.toString());
					
					if (cubeStateInfo != null) {
						
						textAreaPane.setText("Get Cube State request result: ");						
						textAreaPane.append("\n\n" + cubeStateInfo);
					}
					else {
						textAreaPane.setText("Failed to obtain cube state.");
					}
					
					break;
					
				case 2: // Start Cube
					
					String cubeRunRequestResult = newQSTester.startSingleCube(connect, selectedCube.toString());
					
					if (cubeRunRequestResult != null) {
						
						textAreaPane.setText("Start Cube request result: ");						
						textAreaPane.append("\n\n" + cubeRunRequestResult);
					}
						
					else {
						textAreaPane.setText("Failed to run Start Request.");
					}

					break;
					
				case 3: // Stop Cube when not in use

					String cubeStopRequestResult = newQSTester.stopSingleCube(connect, selectedCube.toString(), false);
					
					if (cubeStopRequestResult != null) {
						
						textAreaPane.setText("Stop Cube request result: ");						
						textAreaPane.append("\n\n" + cubeStopRequestResult);
					}
						
					else {
						textAreaPane.setText("Failed to run Stop Request.");
					}

					break;
					
				case 4: // Stop Cube Immediately

					String cubeForceStopRequestResult = newQSTester.stopSingleCube(connect, selectedCube.toString(), true);
					
					if (cubeForceStopRequestResult != null) {
						
						textAreaPane.setText("Stop Cube request result: ");						
						textAreaPane.append("\n\n" + cubeForceStopRequestResult);
					}
						
					else {
						textAreaPane.setText("Failed to run Stop Immediately Request.");
					}

					break;
					
				case 5: // Restart Cube

					String cubeRestartRequestResult = newQSTester.restartSingleCube(connect, selectedCube.toString());
					
					if (cubeRestartRequestResult != null) {
						
						textAreaPane.setText("Restart Cube request result: ");						
						textAreaPane.append("\n\n" + cubeRestartRequestResult);
					}
						
					else {
						textAreaPane.setText("Failed to run Restart Cube Request.");
					}

					break;
					
				case 6: // Refresh Data Cache
					
					String dataRefreshRequestResult = newQSTester.refreshDataCache(connect, selectedCube.toString());
					
					if (dataRefreshRequestResult != null) {
						
						textAreaPane.setText("Refresh Data Cache request result: ");						
						textAreaPane.append("\n\n" + dataRefreshRequestResult);
					}
						
					else {
						textAreaPane.setText("Failed to run Refresh Data Request.");
					}

					break;
					
				case 7: // Refresh Member Cache

					String memberRefreshRequestResult = newQSTester.refreshMemberCache(connect, selectedCube.toString());
					
					if (memberRefreshRequestResult != null) {
						
						textAreaPane.setText("Refresh Member Cache request result: ");						
						textAreaPane.append("\n\n" + memberRefreshRequestResult);
					}
						
					else {
						textAreaPane.setText("Failed to run Refresh Member Cache Request.");
					}

					break;
					
				case 8: // Refresh Security
					
					String securityRefreshRequestResult = newQSTester.refreshCubeSecurity(connect, selectedCube.toString());
					
					if (securityRefreshRequestResult != null) {
						
						textAreaPane.setText("Refresh Security  request result: ");						
						textAreaPane.append("\n\n" + securityRefreshRequestResult);
					}
						
					else {
						textAreaPane.setText("Failed to run Refresh Security Request.");
					}

					break;
				}
			}
		}
	}

	// This is the the available cube combo box event handler
	private class CubeSearchPathHandler implements ActionListener {
		public void actionPerformed(ActionEvent cubeSearchPathEvent) {
			textAreaPane.setText("");
		}
	}

	// This is the cube option combo box event handler.
	private class CubeSelectionHandler implements ActionListener {
		public void actionPerformed(ActionEvent cubeSelectedEvent) {
			textAreaPane.setText("");
			chosenMethod = (String) queryServiceOptions.getSelectedItem();
			if (chosenMethod == CUBE_GET_STATE) {
				cubeActionType = CUBE_ENUM_STATE;
			} else if (chosenMethod == CUBE_START) {
				cubeActionType = CUBE_ENUM_START;
			} else if (chosenMethod == CUBE_STOP) {
				cubeActionType = CUBE_ENUM_STOP;
			} else if (chosenMethod == CUBE_STOP_IMMEDIATE) {
				cubeActionType = CUBE_ENUM_STOP_IMMEDIATE;
			} else if (chosenMethod == CUBE_RESTART) {
				cubeActionType = CUBE_ENUM_RESTART;
			} else if (chosenMethod == CUBE_REFRESH_DATA) {
				cubeActionType = CUBE_ENUM_DATA;
			} else if (chosenMethod == CUBE_REFRESH_MEMBERS) {
				cubeActionType = CUBE_ENUM_MEMBERS;
			} else if (chosenMethod == CUBE_REFRESH_SECURITY) {
				cubeActionType = CUBE_ENUM_SECURITY;
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

		frame = new QueryServiceUI("IBM Cognos Sample", connection);

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
