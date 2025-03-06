/** 
Licensed Materials - Property of IBM

IBM Cognos Products: DOCS

(C) Copyright IBM Corp. 2005, 2008

US Government Users Restricted Rights - Use, duplication or disclosure restricted by GSA ADP Schedule Contract with
IBM Corp.
*/
/**
 * cmQueryUI.java
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
public class cmQueryUI extends JFrame
{
	private CRNConnect connect;

	// The following variables represent the dialog components.
	private JTextArea textAreaPane;
	private JButton cmSampleQueryButton;
	private JButton clearButton;
	private JTextField searchPathString;
	private String defaultSearchPath = "/*";
	private JTextField cmURL;

	private static Logon sessionLogon;

	// This is the constructor.
	public cmQueryUI(String title, CRNConnect connection)
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

		JPanel mainPanel = new JPanel(new GridLayout(3,0));
		mainPanel.setPreferredSize(new Dimension(150, 100));

		JPanel queryDataPanel = new JPanel();
		JPanel buttonPanel = new JPanel();

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

		// Add the status text pane.
		textAreaPane = new JTextArea();
		//mainPanel.add(textAreaPane);

		// create a cmURL panel
		JPanel cmURLPanel = new JPanel();

		// Add the URL text field and label
		cmURL = new JTextField(CRNConnect.CM_URL.length());
		cmURL.setText(CRNConnect.CM_URL);
		cmURL.setEditable(false);
		JLabel cmURLLabel = new JLabel("Server URL:");
		cmURLLabel.setLabelFor(cmURL);
		cmURLPanel.add(cmURLLabel, BorderLayout.WEST);
		cmURLPanel.add(cmURL, BorderLayout.EAST);

        // Add the searchPath field and label
		JLabel searchPathLabel = new JLabel("Search Path:");
		queryDataPanel.add(searchPathLabel, BorderLayout.CENTER);

        searchPathString = new JTextField(CRNConnect.CM_URL.length());
		searchPathString.setText(defaultSearchPath);
		queryDataPanel.add(searchPathString, BorderLayout.EAST);

		// Add the Buttons
		cmSampleQueryButton = new JButton("Execute Query");
		cmSampleQueryButton.addActionListener(new allButtonsHandler());
		buttonPanel.add(cmSampleQueryButton, BorderLayout.EAST);

		mainPanel.add(cmURLPanel, BorderLayout.NORTH);
		mainPanel.add(queryDataPanel, BorderLayout.CENTER);
		mainPanel.add(buttonPanel, BorderLayout.SOUTH);

		//Add the ScrollPane to panel2
		JScrollPane areaScrollPane = new JScrollPane(textAreaPane);
		areaScrollPane.setVerticalScrollBarPolicy(
			JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		areaScrollPane.setPreferredSize(new Dimension(300, 275));
		areaScrollPane.setBorder(
			BorderFactory.createCompoundBorder(
				BorderFactory.createCompoundBorder(
					BorderFactory.createTitledBorder("Results Display Window"),
					BorderFactory.createEmptyBorder(5, 5, 5, 5)),
				areaScrollPane.getBorder()));

		JPanel panel2 = new JPanel(new GridLayout(0, 1));
		panel2.add(areaScrollPane);

		JPanel panel = new JPanel(new BorderLayout(5, 5));
		panel.add(mainPanel, BorderLayout.NORTH);
		panel.add(panel2);

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
						new JFrame("Overview for CM Query sample");
					File explainFile = new File("Java_cmQueryUI_Explain.html");
					if (!explainFile.exists())
					{
						JOptionPane.showMessageDialog(
							null,
							"Explain file not found");
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

			if (buttonPressed == cmSampleQueryButton)
			{
				cmQuerySample sampleQuery = new cmQuerySample();
				output
					+= sampleQuery.prepareQuery(
						connect,
						searchPathString.getText());
			}
			if (output.compareTo("") != 0 || buttonPressed == clearButton)
			{
				textAreaPane.setText("");
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

		cmQueryUI frame = new cmQueryUI("IBM Cognos Sample", connection);

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
