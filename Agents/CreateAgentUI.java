/** 
Licensed Materials - Property of IBM

IBM Cognos Products: DOCS

(C) Copyright IBM Corp. 2005, 2008

US Government Users Restricted Rights - Use, duplication or disclosure restricted by GSA ADP Schedule Contract with
IBM Corp.
*/
/**
 * CreateAgentUI.java
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

// This Java class extends the JFrame class so that you can
// display a window.
public class CreateAgentUI extends JFrame
{
	private CRNConnect connect;

	// The following variables represent the dialog components.
	private JTextArea textAreaPane;
	private JTextField cmURL;
	private JButton createAgentButton;

	private static Logon sessionLogon;

	private String packageName="GO Data Warehouse (query)";
	private String reportName="Retailer Contact";

	CreateAgent anAgent=new CreateAgent();

	// This is the constructor.
	public CreateAgentUI(String title, CRNConnect connection)
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

		// Create and add the Button
		createAgentButton = new JButton("Create an Agent");
		createAgentButton.addActionListener(new allButtonsHandler());
		buttonPanel.add(createAgentButton, BorderLayout.EAST);

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
		areaScrollPane.setPreferredSize(new Dimension(200, 275));
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
						new JFrame("Overview for Create Agent Sample");
					File explainFile = new File("Java_CreateAgentUI_Explain.html");
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
			String output = new String("");

			if (buttonPressed == createAgentButton)
			{
				textAreaPane.setText("");
				try
				{
                    //Display a prompt asking user to select a package
					String defaultPackageName = null;
					String[] packageOptions = {packageName};
					defaultPackageName =
						(String)JOptionPane.showInputDialog(null,
                                "Choose package name",
                                "Package name",
                                JOptionPane.INFORMATION_MESSAGE,
                                null,
                                packageOptions,
                                packageOptions[0]);

					if ((defaultPackageName == null) || (defaultPackageName.length() == 0))
					{
						textAreaPane.setText("");
						textAreaPane.append("Invalid select / Action cancelled");
						return;
					}

					// Prompt the user for an agent name.
					String agentName = null;
					agentName =
							JOptionPane.showInputDialog(
								"Please input an agent name");
					if ((agentName == null) || (agentName.length() == 0))
					{
						textAreaPane.setText("");
						textAreaPane.append("Invalid input / Action cancelled");
						return;
					}

					//Display a prompt asking user to select a report
					String selectedReport=null;
					String[] reportOptions = {reportName};
	                selectedReport = (String)JOptionPane.showInputDialog(null,
	                                           "Please select a report",
	                                           "Add a task - Run a report...",
	                                           JOptionPane.INFORMATION_MESSAGE,
	                                           null,
	                                           reportOptions,
	                                           reportOptions[0]);
					if((selectedReport==null)||(selectedReport.length()==0))
					{
						textAreaPane.setText("");
						textAreaPane.append("Invalid select / Action cancelled");
						return;
					}

					String parentSearchPath = "/content/folder[@name='Samples']/folder[@name='Models']/package[@name='"+defaultPackageName+"']";

				    boolean createAnAgent=anAgent.addNewAgent(connect, parentSearchPath,agentName,selectedReport);

				    if(createAnAgent)
				    {
						textAreaPane.setText("");
						textAreaPane.append("Agent " + agentName + " successfully created and ran.");
				    }
				    else
				    {
						textAreaPane.setText("");
						textAreaPane.append("Agent " + agentName + " failed to create.");
				    }
				}
				catch (Exception ex)
				{
					System.out.println(ex.getMessage());
					output = "\nCreate agent:\nAn error occurred\n";
				}
			}

			if (output.compareTo("") != 0)
			{
				textAreaPane.append(output);
			}
		}
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

		CreateAgentUI frame = new CreateAgentUI("IBM Cognos Sample", connection);

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
		frame.setSize(780, 440);
		frame.setVisible(true);
		frame.setResizable(true);
	}
}
