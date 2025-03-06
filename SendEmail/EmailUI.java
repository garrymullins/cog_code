/** 
Licensed Materials - Property of IBM

IBM Cognos Products: DOCS

(C) Copyright IBM Corp. 2005, 2008

US Government Users Restricted Rights - Use, duplication or disclosure restricted by GSA ADP Schedule Contract with
IBM Corp.
*/
/**
 * EmailUI.java
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

import com.cognos.developer.schemas.bibus._3.AddressSMTP;
import com.cognos.developer.schemas.bibus._3.BaseClass;
import com.cognos.developer.schemas.bibus._3.OrderEnum;
import com.cognos.developer.schemas.bibus._3.PropEnum;
import com.cognos.developer.schemas.bibus._3.QueryOptions;
import com.cognos.developer.schemas.bibus._3.SearchPathMultipleObject;
import com.cognos.developer.schemas.bibus._3.Sort;

// This Java class extends the JFrame class so that you can
// display a window.
public class EmailUI extends JFrame
{
	private CRNConnect connect;
	private CSHandlers csh = new CSHandlers();

	// The following variables represent the dialog components.
	private JTextArea textAreaPane;
	private JTextField cmURL;
	private JButton emailButton;
	private JComboBox emailTypeOption;
	private JComboBox emailSelectOption;

	private static Logon sessionLogon;
	private static final int REP_TYPE_ENUM_HTML = 0;

	private static int reportType = 0;

	private static final String EMAIL_ONE_USER = "Email Selected User";
	private static final String EMAIL_ALL_CONTACTS =
		"Email All IBM Contacts";

	private static final int EMAIL_ENUM_ONE_USER = 1;
	private static final int EMAIL_ENUM_ALL_CONTACTS = 2;
	private static int emailType = 1;

	private static BaseClassWrapper selectedReport = null;

	// This is the constructor.
	public EmailUI(String title, CRNConnect connection)
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
		String emailTypes[] = { EMAIL_ONE_USER, EMAIL_ALL_CONTACTS };
		emailTypeOption = new JComboBox(emailTypes);
		emailTypeOption.setSelectedItem(null);
		emailTypeOption.addActionListener(new EmailTypeSelectionHandler());
		buttonPanel.add(emailTypeOption, BorderLayout.WEST);

		// Create and add the select report combo box
		BaseClassWrapper listOfReports[] = getListOfReports(connect);
		emailSelectOption = new JComboBox(listOfReports);
		emailSelectOption.setSelectedItem(null);
		emailSelectOption.addActionListener(new ReportSelectionHandler());
		buttonPanel.add(emailSelectOption, BorderLayout.CENTER);

		// Create and add the Button
		emailButton = new JButton("Email");
		emailButton.addActionListener(new allButtonsHandler());
		buttonPanel.add(emailButton, BorderLayout.EAST);

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
						new JFrame("Overview for Email Sample");
					File explainFile = new File("Java_EmailUI_Explain.html");
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

			if (buttonPressed == emailButton)
			{
				Email newEmail = new Email();
				//For this sample, hard wire HTML format
				reportType = REP_TYPE_ENUM_HTML;

				try
				{

					if (emailType == EMAIL_ENUM_ONE_USER)
					{
						String selectedValue =
							JOptionPane.showInputDialog(
								"Provide e-mail address:");

						if (selectedValue != null)
						{
							AddressSMTP[] emailAddress = { new AddressSMTP(selectedValue) };
								newEmail.emailReport(
								connect,
								selectedReport,
								"message body",
								"subject",
								reportType,
								emailAddress,
								null);
						}
					}
					else if (emailType == EMAIL_ENUM_ALL_CONTACTS)
					{
						newEmail.emailReport(
							connect,
							selectedReport,
							"message body",
							"subject",
							reportType,
							null,
							null);
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

	// This is the emailType combo box event handler.
	private class EmailTypeSelectionHandler implements ActionListener
	{
		public void actionPerformed(ActionEvent emailTypeSelectedEvent)
		{
			String chosenType = (String)emailTypeOption.getSelectedItem();
			if (chosenType == EMAIL_ONE_USER)
			{
				emailType = EMAIL_ENUM_ONE_USER;
			}
			else if (chosenType == EMAIL_ALL_CONTACTS)
			{
				emailType = EMAIL_ENUM_ALL_CONTACTS;
			}
			else
			{
				//error, force ONE_USER (default) ??
				emailType = EMAIL_ENUM_ONE_USER;
			}
		}
	}

	private class ReportSelectionHandler implements ActionListener
	{
		public void actionPerformed(ActionEvent repSelectedEvent)
		{
			selectedReport = (BaseClassWrapper)emailSelectOption.getSelectedItem();
		}
	}

	//This is a method for retrieving a list of the available reports to run
	protected BaseClassWrapper[] getListOfReports(CRNConnect connection)
	{
		BaseClassWrapper reportAndQueryList[] = null;
		BaseClass reports[] = new BaseClass[0];
		BaseClass queries[] = new BaseClass[0];
		int reportAndQueryIndex = 0;
		int reportIndex = 0;
		int queryIndex = 0;

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
			queries =
				connection.getCMService().query(
					new SearchPathMultipleObject("/content//query"),
					props,
					sortOptions,
					new QueryOptions());
		}
		catch (java.rmi.RemoteException remoteEx)
		{
			System.out.println("Caught Remote Exception:\n");
			remoteEx.printStackTrace();
		}

		reportAndQueryList = new BaseClassWrapper[reports.length + queries.length];

		if ((reports != null) && (reports.length > 0))
		{
			for (reportIndex = 0; reportIndex < reports.length; reportIndex++)
			{
				reportAndQueryList[reportAndQueryIndex++] = new BaseClassWrapper(reports[reportIndex]);
			}
		}
		if ((queries != null) && (queries.length > 0))
		{
			for (queryIndex = 0; queryIndex < queries.length; queryIndex++)
			{
				reportAndQueryList[reportAndQueryIndex++] =
					new BaseClassWrapper(queries[queryIndex]);
			}
		}
		return reportAndQueryList;
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

		EmailUI frame = new EmailUI("IBM Cognos Sample", connection);

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
