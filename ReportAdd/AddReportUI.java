/** 
Licensed Materials - Property of IBM

IBM Cognos Products: DOCS

(C) Copyright IBM Corp. 2005, 2008

US Government Users Restricted Rights - Use, duplication or disclosure restricted by GSA ADP Schedule Contract with
IBM Corp.
*/
/**
 * AddReportUI.java
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
import java.io.FileInputStream;
import java.io.IOException;

import java.net.URL;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
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
import javax.swing.filechooser.FileFilter;

import com.cognos.developer.schemas.bibus._3.ReportServiceReportSpecification;
import com.cognos.developer.schemas.bibus._3.Specification;

// This Java class extends the JFrame class so that you can
// display a window.
public class AddReportUI extends JFrame
{
	private CRNConnect connect;

	// The following variables represent the dialog components.
	private JTextArea textAreaPane;
	private JTextField cmURL;
	private JButton sampleOptionButton;
	private JComboBox sampleOption;

	private static Logon sessionLogon;

	private static final String VALIDATE_SPEC = "Validate Specification";
	private static final String ADD_SPEC_TO_CM =
		"Add Specification To Content Store";
	private static String selectedSampleOption = VALIDATE_SPEC;

	private static String specificationFile = "";

	// This is the constructor.
	public AddReportUI(String title, CRNConnect connection)
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
		String optionType[] = { VALIDATE_SPEC, ADD_SPEC_TO_CM };

		sampleOption = new JComboBox(optionType);
		sampleOption.setSelectedItem(null);
		sampleOption.addActionListener(new sampleOptionSelectionHandler());
		buttonPanel.add(sampleOption, BorderLayout.WEST);

		// Create and add the Button
		sampleOptionButton = new JButton("Run Option");
		sampleOptionButton.addActionListener(new allButtonsHandler());
		buttonPanel.add(sampleOptionButton, BorderLayout.EAST);

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
						new JFrame("Overview for Add Report Sample");
					File explainFile = new File("Java_AddReportUI_Explain.html");
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
				{
					textAreaPane.setText("");
					textAreaPane.append("Login Failed. Please try again.\n");
					return;
				}
			}

			JButton buttonPressed = ((JButton)e.getSource());
			String output = "";

			if (buttonPressed == sampleOptionButton)
			{
				AddReport reportAdder = new AddReport();
				String reportSpecStr = "";

				final JFileChooser fc =
					new JFileChooser(System.getProperty("user.dir"));
				fc.setFileFilter(new XMLFileFilter());
				fc.setAcceptAllFileFilterUsed(false);

				int fileSelectedOK = fc.showOpenDialog(null);
				if (fileSelectedOK != JFileChooser.APPROVE_OPTION)
				{
					textAreaPane.setText("");
					textAreaPane.append(
						"Please select a Report Specification.\n");
					return;
				}
				specificationFile = fc.getSelectedFile().getName();
				if ((specificationFile == null)
					|| (specificationFile.length() == 0))
				{
					textAreaPane.setText("");
					textAreaPane.append(
						"The selected file name ["
							+ specificationFile
							+ "] is not valid.");
					return;
				}

				//Read the contents of the file
				File theFile = fc.getSelectedFile();
				FileInputStream fileIS = null;

				try
				{
					fileIS = new FileInputStream(theFile);
					int readOK = 0;
					while (readOK >= 0)
					{
						byte inBytes[] = new byte[1024];
						readOK = fileIS.read(inBytes);
						if (readOK != -1)
						{
							reportSpecStr += new String(inBytes, 0, readOK);
						}
					}
				}
				catch (IOException ioEx)
				{
					ioEx.printStackTrace();
					return;
				}
				ReportServiceReportSpecification newReportSpec = new ReportServiceReportSpecification();
				newReportSpec.setValue(new Specification(reportSpecStr));

				try
				{
					if (selectedSampleOption.compareTo(VALIDATE_SPEC) == 0)
					{
						output =
							reportAdder.validateReportSpec(
								connect,
								newReportSpec);
					}
					else if (
						selectedSampleOption.compareTo(ADD_SPEC_TO_CM) == 0)
					{
						String reportName =
							JOptionPane.showInputDialog(
								"Please enter a name for the new report.");

						if (reportName != null)
						{
							output =
								reportAdder.addSpecToCM(
									connect,
									newReportSpec,
									reportName);
						}
						else
						{
							output = "Action cancelled.";
						}
					}
				}
				catch (java.rmi.RemoteException remoteEx)
				{
					output = "Exception Caught:\n" + remoteEx;
					remoteEx.printStackTrace(System.out);
				}

			}

			if (output.compareTo("") != 0)
			{
				textAreaPane.setText("");
				textAreaPane.append(output);
			}
		}
	}

	private class sampleOptionSelectionHandler implements ActionListener
	{
		public void actionPerformed(ActionEvent optionSelectedEvent)
		{
			selectedSampleOption = (String)sampleOption.getSelectedItem();
		}
	}

	private class XMLFileFilter extends FileFilter
	{
		public boolean accept(File f) {
			if (f.isDirectory()) {
				return true;
			}

			String fileExt = null;
			String fileName = f.getName();
			int i = fileName.lastIndexOf(".");
			if (i > 0 &&  i < fileName.length() - 1) {
				fileExt = fileName.substring(i+1).toLowerCase();
			}

			if (fileExt != null) {
				if ( fileExt.equals("xml") )
				{
					return true;
				}
				else
				{
					return false;
				}
			}

			return false;
		}

		public String getDescription()
		{
			return "Only xml files";
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

		AddReportUI frame = new AddReportUI("IBM Cognos Sample", connection);

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
