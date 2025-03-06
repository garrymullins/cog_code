/** 
Licensed Materials - Property of IBM

IBM Cognos Products: DOCS

(C) Copyright IBM Corp. 2005, 2008

US Government Users Restricted Rights - Use, duplication or disclosure restricted by GSA ADP Schedule Contract with
IBM Corp.
*/
/**
 * CopyMoveReportUI.java
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
import javax.swing.DefaultComboBoxModel;
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

public class CopyMoveReportUI extends JFrame {

	private CRNConnect connect;

	private static CopyMoveReportUI frame;

	// The following variables represent the dialog components.
	private JTextArea textAreaPane;

	private JTextField cmURL;

	private JButton copyMoveButton;

	private JComboBox runOption;

	private JComboBox reportOption;

	private static Logon sessionLogon;

	private static final String COPY = "Copy";

	private static final String COPYRENAME = "Copy Rename";

	private static final String MOVE = "Move";

	private static final String MOVERENAME = "Move Rename";

	private String chosenRunOption = null;

	private BaseClassWrapper chosenReport = null;

	CopyMoveReport newCopyMoveReport = new CopyMoveReport();

	// This is the constructor.
	public CopyMoveReportUI(String title, CRNConnect connection) {
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
		JPanel outputNavPanel = createOutputPanel();
		JPanel panel = new JPanel(new BorderLayout());
		panel.add(mainPanel, BorderLayout.NORTH);
		panel.add(outputNavPanel);

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

		//get dropdown box and button panel
		JPanel optionButtonPanel = createMainButtonPanel();

		//create the main panel and add the components
		JPanel mainPanel = new JPanel(new GridLayout(2, 0));

		// Add the panels to the mainPanel
		mainPanel.add(cmURLPanel);
		mainPanel.add(optionButtonPanel);

		return mainPanel;
	}

	private JPanel createMainButtonPanel() {
		// Create the button Panel
		JPanel buttonPanel = new JPanel();

		// Create and add the run option combo box
		String commandList[] = { COPY, COPYRENAME, MOVE, MOVERENAME };
		runOption = new JComboBox(commandList);
		runOption.setSelectedItem(null);
		runOption.addActionListener(new RunOptionHandler());
		buttonPanel.add(runOption, BorderLayout.WEST);

		// Create and add the package output type combo box
		BaseClassWrapper[] reportList = newCopyMoveReport
				.getListOfReports(connect);
		reportOption = new JComboBox(reportList);
		reportOption.setSelectedItem(null);
		reportOption.addActionListener(new reportSelectionHandler());
		buttonPanel.add(reportOption, BorderLayout.CENTER);

		// Create and add the Run Option Button
		copyMoveButton = new JButton("Run Option");
		copyMoveButton.addActionListener(new allButtonsHandler());
		buttonPanel.add(copyMoveButton, BorderLayout.EAST);

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
							"Overview for Copy and Move Sample");
					File explainFile = new File(
							"Java_CopyMoveReportUI_Explain.html");
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

			if (buttonPressed == copyMoveButton) {

				textAreaPane.setText("");
				String selectedReportName = null;

				if (chosenRunOption == null || chosenReport == null) {
					return;
				} else {
					selectedReportName = chosenReport.toString();
				}

				if (chosenRunOption.compareToIgnoreCase(COPY) == 0) {
					String copyResults = newCopyMoveReport.copyMoveReport(
							connect, chosenReport, chosenRunOption, null);

					runOption.setSelectedItem(null);
					reportOption.setSelectedItem(null);
					if (copyResults != null) {
						textAreaPane
								.setText("The report - "
										+ selectedReportName
										+ " has been successfully copied to the 'My Folders' in the content store."
										+ "\n" + "The Event ID: " + copyResults);
					} else {
						textAreaPane
								.setText("The report - "
										+ selectedReportName
										+ " was not successfully copied to the target location.");
					}
				} else if (chosenRunOption.compareToIgnoreCase(COPYRENAME) == 0
						&& chosenReport != null) {

					String inputValue = null;

					do {
						inputValue = JOptionPane
								.showInputDialog("Please type in the target report name \nSource report is '"
										+ selectedReportName + "'");
						if (inputValue == null) {
							return;
						}
					} while (inputValue.length() == 0);

					String copyRenameResults = newCopyMoveReport
							.copyMoveReport(connect, chosenReport,
									chosenRunOption, inputValue);

					runOption.setSelectedItem(null);
					reportOption.setSelectedItem(null);
					if (copyRenameResults != null) {
						textAreaPane
								.setText("The report - "
										+ selectedReportName
										+ " has been successfully copied to the 'My Folders' in the content store"
										+ "\nwith the different name - "
										+ inputValue + "\n" + "The Event ID: "
										+ copyRenameResults);
					} else {
						textAreaPane
								.setText("The report - "
										+ selectedReportName
										+ " was not successfully copied with a new name");
					}
				} else if (chosenRunOption.compareToIgnoreCase(MOVE) == 0
						&& chosenReport != null) {

					String moveResults = newCopyMoveReport.copyMoveReport(
							connect, chosenReport, chosenRunOption, null);

					if (moveResults != null) {
						reportOption.removeAllItems();
						BaseClassWrapper[] newReportList = newCopyMoveReport
								.getListOfReports(connect);
						reportOption.setModel(new DefaultComboBoxModel(
								newReportList));
						reportOption.setSelectedItem(null);
						runOption.setSelectedItem(null);
						textAreaPane
								.setText("The report - "
										+ selectedReportName
										+ " has been successfully moved to the 'My Folders' in the content store."
										+ "\n" + "The Event ID: " + moveResults);
					} else {
						runOption.setSelectedItem(null);
						reportOption.setSelectedItem(null);
						textAreaPane
								.setText("The report - "
										+ selectedReportName
										+ " was not successfully moved to the target location.");
					}

				} else if (chosenRunOption.compareToIgnoreCase(MOVERENAME) == 0) {
					String inputValue = null;

					do {
						inputValue = JOptionPane
								.showInputDialog("Please give the target report name: \nSource report is called '"
										+ selectedReportName + "'");
						if (inputValue == null) {
							return;
						}

					} while (inputValue.length() == 0);

					String moveRenameResults = newCopyMoveReport
							.copyMoveReport(connect, chosenReport,
									chosenRunOption, inputValue);
					if (moveRenameResults != null) {
						reportOption.removeAllItems();
						BaseClassWrapper[] newReportList = newCopyMoveReport
								.getListOfReports(connect);
						reportOption.setModel(new DefaultComboBoxModel(
								newReportList));
						reportOption.setSelectedItem(null);
						runOption.setSelectedItem(null);
						textAreaPane
								.setText("The report - "
										+ selectedReportName
										+ " has been successfully moved to the 'My Folders' in the content store"
										+ "\nwith the different name - "
										+ inputValue + "\n" + "The Event ID: "
										+ moveRenameResults);
						reportOption.removeItem(chosenReport);
					} else {
						runOption.setSelectedItem(null);
						reportOption.setSelectedItem(null);
						textAreaPane.setText("The report - "
								+ selectedReportName
								+ " was not successfully move with a new name");
					}
				}
			}
		}
	}

	// This is the report select combo box event handler.
	private class reportSelectionHandler implements ActionListener {
		public void actionPerformed(ActionEvent reportSelectedEvent) {
			textAreaPane.setText("");
			chosenReport = (BaseClassWrapper) reportOption.getSelectedItem();
		}
	}

	// This is the run option combo box event handler.
	private class RunOptionHandler implements ActionListener {
		public void actionPerformed(ActionEvent runOptionSelectedEvent) {
			textAreaPane.setText("");
			chosenRunOption = (String) runOption.getSelectedItem();
		}
	}

	// Create the main method to execute the application.
	public static void main(String args[]) {
		CRNConnect connection = new CRNConnect();
		connection.connectToCognosServer();

		sessionLogon = new Logon();
		String Output = "";

		while (!Logon.loggedIn(connection)) {
			Output = sessionLogon.logon(connection);

			if (!Logon.loggedIn(connection)) {
				int retry = JOptionPane.showConfirmDialog(null,
						"Login Failed. Please try again.", "Login Failed",
						JOptionPane.OK_CANCEL_OPTION);
				if (retry != JOptionPane.OK_OPTION) {
					System.exit(0);
				}
			}
		}

		frame = new CopyMoveReportUI("IBM Cognos Sample", connection);

		// Create a WindowAdapter so the application
		// is exited when the window is closed.
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});

		frame.textAreaPane.setText(Output);

		// Set the size of the frame and display it.
		frame.setSize(880, 440);
		frame.setVisible(true);
		frame.setResizable(true);
	}
}
