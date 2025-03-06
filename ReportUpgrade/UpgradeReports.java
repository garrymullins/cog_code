/** 
Licensed Materials - Property of IBM

IBM Cognos Products: DOCS

(C) Copyright IBM Corp. 2005, 2008

US Government Users Restricted Rights - Use, duplication or disclosure restricted by GSA ADP Schedule Contract with
IBM Corp.
*/
/**
 * UpgradeReports.java
 *
 * Copyright (C) 2008 Cognos ULC, an IBM Company. All rights reserved.
 * Cognos (R) is a trademark of Cognos ULC, (formerly Cognos Incorporated).
 *
 */

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

// This Java class extends the JPanel class so that you can 
// display a window.
public class UpgradeReports 
extends JFrame
{
	
	public static final String UPGRADE_SPEC_STRING = "Upgrade and replace";
	public static final String UPGRADE_COPY_SPEC_STRING = "Upgrade and Copy";
	public static final String EXTRACT_UPGRADED_SPEC_STRING = "Save to file (no upgrade)";
	public static final String EXTRACT_SPEC_STRING = "Upgrade and Save to file";
		
	private CRNConnect connect;

	// The following variables represent the dialog components.
	private UpgradeReportsTree outputPane;
	private JTextField cmURL;
	private JTextField selectedSearchPath;

	private static Logon sessionLogon;

	// This is the constructor.
	public UpgradeReports(String title, CRNConnect connection)
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

		//
		//Add and populate the File menu. 
		JMenu fileMenu = new JMenu("File");
		mBar.add(fileMenu);

		JMenuItem exit = new JMenuItem("Exit");
		fileMenu.add(exit);
		exit.addActionListener(new MenuHandler());

		JMenuItem refresh = new JMenuItem("Refresh Tree");
		fileMenu.add(refresh);
		refresh.addActionListener(new MenuHandler());

		//
		//Add and populate the Upgrade menu
		JMenu upgMenu = new JMenu("Upgrade");
		mBar.add(upgMenu);
		JMenuItem contentStoreUpgrade = new JMenuItem(UPGRADE_SPEC_STRING);
		upgMenu.add(contentStoreUpgrade);
		contentStoreUpgrade.addActionListener(new MenuHandler());
		JMenuItem contentStoreCopy = new JMenuItem(UPGRADE_COPY_SPEC_STRING);
		upgMenu.add(contentStoreCopy);
		contentStoreCopy.addActionListener(new MenuHandler());
		
		//
		//Add and populate the Extract menu
		JMenu extractMenu = new JMenu("Extract");
		mBar.add(extractMenu);
		JMenuItem localUpgrade = new JMenuItem(EXTRACT_UPGRADED_SPEC_STRING);
		extractMenu.add(localUpgrade);
		localUpgrade.addActionListener(new MenuHandler());
		JMenuItem localCopy = new JMenuItem(EXTRACT_SPEC_STRING);
		extractMenu.add(localCopy);
		localCopy.addActionListener(new MenuHandler());
		
		//
		//Add and populate the Help menu.
		JMenu helpMenu = new JMenu("Help");
		mBar.add(helpMenu);
		JMenuItem about = new JMenuItem("About");
		helpMenu.add(about);
		about.addActionListener(new MenuHandler());
		JMenuItem overview = new JMenuItem("Overview");
		helpMenu.add(overview);
		overview.addActionListener(new MenuHandler());

		JPanel mainPanel = new JPanel(new GridLayout(2, 0));
		// create a cmURL panel
		JPanel cmURLPanel = new JPanel();

		// Add the URL text field and label
		cmURL = new JTextField(CRNConnect.CM_URL.length() + 5);
		cmURL.setText(CRNConnect.CM_URL);
		cmURL.setEditable(false);
		cmURLPanel.add(new JLabel("Server URL:"), BorderLayout.WEST);
		cmURLPanel.add(cmURL, BorderLayout.EAST);

		// Create the Button and Button Panel
		selectedSearchPath = new JTextField(CRNConnect.CM_URL.length() + 5);
		JPanel searchPathPanel = new JPanel();
		searchPathPanel.add(new JLabel("Search Path:"), BorderLayout.WEST);
		searchPathPanel.add(selectedSearchPath, BorderLayout.EAST);

		// Add the status text pane.
		outputPane = new UpgradeReportsTree(connect);

		// Add the panels to the mainPanel
		mainPanel.add(cmURLPanel);
		mainPanel.add(searchPathPanel);

		JPanel panel = new JPanel(new BorderLayout());
		panel.add(mainPanel, BorderLayout.NORTH);
		panel.add(outputPane);

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
				if (menuClicked.getText() == "Refresh Tree")
				{
					recreateTree();
				}
				if (menuClicked.getText().compareTo("Overview") == 0)
				{
					JFrame explainWindow =
						new JFrame("Overview for Report Upgrade Sample");
					File explainFile = new File("Java_ReportUpgrade_Explain.html");
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
				
				if ( menuClicked.getText() == UpgradeReports.UPGRADE_SPEC_STRING )
				{
					outputPane.showUpgradeDialog(null, connect);
				}
				if (menuClicked.getText() == UpgradeReports.UPGRADE_COPY_SPEC_STRING)
				{
					outputPane.showUpgradeCopyDialog(null, connect);
				}
				if (menuClicked.getText() == UpgradeReports.EXTRACT_SPEC_STRING)
				{
					outputPane.showExtractDialog(null, connect);
				}
				if (menuClicked.getText() == UpgradeReports.EXTRACT_UPGRADED_SPEC_STRING)
				{
					outputPane.showExtractUpgradedDialog(null,connect);
				}

			}
			catch (Exception ex)
			{}
		}
	}

	public void updateSelectedSearchPath(String newSearchPath)
	{
		selectedSearchPath.setText(newSearchPath);
	}
	
	public void recreateTree()
	{
		outputPane.setVisible(false);
		getContentPane().remove(outputPane);
		outputPane = new UpgradeReportsTree(connect);
		getContentPane().add(outputPane);
		outputPane.setVisible(true);
	}
	
	// Create the main method to execute the application.
	public static void main(String args[])
	{
		
		CRNConnect connection = new CRNConnect();
		connection.connectToCognosServer();
		sessionLogon = new Logon();

		while (!Logon.loggedIn(connection))
		{
			sessionLogon.logon(connection);

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
		
		UpgradeReports frame = new UpgradeReports("IBM Cognos Sample", connection);

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
		frame.setSize(680, 440);
		frame.setVisible(true);
		frame.setResizable(true);
	}

}
