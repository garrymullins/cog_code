/** 
Licensed Materials - Property of IBM

IBM Cognos Products: DOCS

(C) Copyright IBM Corp. 2008, 2009

US Government Users Restricted Rights - Use, duplication or disclosure restricted by GSA ADP Schedule Contract with
IBM Corp.
*/
/**
 * DimensionManagementServiceTest.java
 *
 * Copyright (C) 2008 Cognos ULC, an IBM Company. All rights reserved.
 * Cognos (R) is a trademark of Cognos ULC, (formerly Cognos Incorporated).
 *
 * Description:
 * 
 * This is a sample test application for the IBM Cognos
 * Dimension Management Service.
 *  
 */

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.rmi.RemoteException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
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
import javax.swing.border.CompoundBorder;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

import com.cognos.developer.schemas.bibus._3.AsynchDetail;
import com.cognos.developer.schemas.bibus._3.AsynchDetailMIMEAttachment;
import com.cognos.developer.schemas.bibus._3.AsynchOptionEnum;
import com.cognos.developer.schemas.bibus._3.AsynchOptionInt;
import com.cognos.developer.schemas.bibus._3.AsynchReply;
import com.cognos.developer.schemas.bibus._3.DimensionManagementServiceSpecification;
//import com.cognos.developer.schemas.bibus._3.DimensionManagementService_Port;
import com.cognos.developer.schemas.bibus._3.Option;
import com.cognos.developer.schemas.bibus._3.ParameterValue;
import com.cognos.developer.schemas.bibus._3.Specification;


final class DimensionManagementServiceTest extends JFrame
{
    private static final long serialVersionUID = 1L;

    private static final String APPLICATION_TITLE = "Dimension Management Service Testbench";

    private static DimensionManagementServiceTest theApp;
    
    private CRNConnect connection;

    private Logon sessionLogon;

    private String logonStatus;

    private JTextArea requestTextArea;

    private JTextArea responseTextArea;
    
    private JFileChooser fileChooser;
        
    
    public static void main( String args[] )
    {
        theApp = new DimensionManagementServiceTest();

        // exit the application on window closing.
        theApp.addWindowListener( new WindowAdapter()
        {
            public void windowClosing( WindowEvent e )
            {
                System.exit( 0 );
            }
        } );

        theApp.run();
    }

    private DimensionManagementServiceTest()
    {
        super( APPLICATION_TITLE );
    }

    private void run()
    {
        connectToCognosServer();
        logon();

        createUI();
    }

    private void connectToCognosServer()
    {
        connection = new CRNConnect();
        connection.connectToCognosServer();
    }

    private void logon()
    {
 //       assert connection != null;

        sessionLogon = new Logon();
        logonStatus = "";

        while ( !Logon.loggedIn( connection ) )
        {
            logonStatus = sessionLogon.logon( connection );

            if ( !Logon.loggedIn( connection ) )
            {
                int retry = JOptionPane.showConfirmDialog( null, "Login Failed. Please try again.", "Login Failed", JOptionPane.OK_CANCEL_OPTION );
                if ( retry != JOptionPane.OK_OPTION )
                {
                    System.exit( 0 );
                }
            }
        }
    }

    private void createUI()
    {
        fileChooser = new JFileChooser();
        fileChooser.setCurrentDirectory( new File( "C:\\") );
        
        createMenuBar();
        createTestPanel();

        // show main window 
        this.setSize( 640, 480 );
        this.setResizable( true );
        this.setVisible( true );
    }

    private void createMenuBar()
    {
        JMenuBar menuBar = new JMenuBar();
        this.setJMenuBar( menuBar );

        menuBar.add( createFileMenu() );
        menuBar.add( createHelpMenu() );
    }

    private JMenu createFileMenu()
    {
        JMenu fileMenu = new JMenu( "File" );

        JMenuItem open = new JMenuItem( "Open" );

        open.addActionListener( new ActionListener()
        {
            public void actionPerformed( ActionEvent e )
            {
                int returnVal = fileChooser.showOpenDialog( theApp );
                if ( returnVal != JFileChooser.APPROVE_OPTION )
                {
                    return;
                }

                File file = fileChooser.getSelectedFile();
                try
                {
                    requestTextArea.setText( "" );
                    responseTextArea.setText( "" );

                    FileInputStream is = new FileInputStream( file );
                    InputStreamReader reader = new InputStreamReader( is, "UTF-8" );
                    requestTextArea.read( reader, null );
                }
                catch ( IOException ex )
                {
                    responseTextArea.setText( "Failed to read '" + file.getName() + "'" );
                }
            }
        } );

        JMenuItem exit = new JMenuItem( "Exit" );
        exit.addActionListener( new ActionListener()
        {
            public void actionPerformed( ActionEvent e )
            {
                System.exit( 0 );
            }
        } );

        fileMenu.add( open );
        fileMenu.add( exit );
        return fileMenu;
    }

    private JMenu createHelpMenu()
    {
        JMenu helpMenu = new JMenu( "Help" );

        helpMenu.add( createAboutMenuItem() );

        return helpMenu;
    }

    private JMenuItem createAboutMenuItem()
    {
        JMenuItem about = new JMenuItem( "About" );
        about.addActionListener( new ActionListener()
        {
            public void actionPerformed( ActionEvent e )
            {
                JOptionPane.showMessageDialog( ((JMenuItem) e.getSource()).getParent(), APPLICATION_TITLE + "\n\n" + "Version 1.0.0\n" + "This application is used to test Dimension Management Service.", "About " + APPLICATION_TITLE, JOptionPane.INFORMATION_MESSAGE, null );
            }
        } );

        return about;
    }

    private void createTestPanel()
    {
        JPanel testPanel = new JPanel( new BorderLayout() );

        testPanel.add( createControlPanel(), BorderLayout.NORTH );
        testPanel.add( createRequestResponsePanel() );

        // Set the content of the JFrame
        setContentPane( testPanel );
    }

    private JPanel createControlPanel()
    {
        JPanel mainPanel = new JPanel( new GridLayout( 2, 0 ) );

        mainPanel.add( createURLPanel() );
        mainPanel.add( createButtonPanel() );

        return mainPanel;
    }

    private JPanel createURLPanel()
    {
        String url = CRNConnect.CM_URL;
        
        JTextField urlTextField = new JTextField( url.length() );
        urlTextField.setText( url );
        urlTextField.setEditable( false );

        JPanel urlPanel = new JPanel();
        urlPanel.add( new JLabel( "IBM Cognos URL:" ) );
        urlPanel.add( urlTextField );

        return urlPanel;
    }

    private JPanel createButtonPanel()
    {
        JPanel buttonPanel = new JPanel();

        buttonPanel.add( createRunButton(), BorderLayout.EAST );
        buttonPanel.add( createClearButton(), BorderLayout.WEST );

        return buttonPanel;
    }

    private JButton createRunButton()
    {
        JButton button = new JButton( "Run Request" );

        button.addActionListener( new ActionListener()
        {
            public void actionPerformed( ActionEvent event )
            {
                try
                {
                    responseTextArea.setText( "" );
                    
                    String runRequestResult = runRequest( requestTextArea.getText() );
                    
                    responseTextArea.setText( runRequestResult );
                    responseTextArea.setCaretPosition(0);
                }
                catch ( Exception e )
                {
                    StringWriter sw = new StringWriter();
                    PrintWriter pw = new PrintWriter( sw );
                    e.printStackTrace( pw );
                    responseTextArea.setText( sw.toString() );
                }
            }
        } );

        return button;
    }

    private JButton createClearButton()
    {
        JButton button = new JButton( "Clear" );

        button.addActionListener( new ActionListener()
        {
            public void actionPerformed( ActionEvent e )
            {
                requestTextArea.setText( "" );
                responseTextArea.setText( "" );
            }
        } );

        return button;
    }

    private JPanel createRequestResponsePanel()
    {
        JPanel panel = new JPanel( new GridLayout( 2, 0 ) );

        panel.add( createRequestPanel() );
        panel.add( createResponsePanel() );

        return panel;
    }

    /**
     * Create a panel for:
     * IBM Cognos Business Viewpoint XML API Request
     */
    private JPanel createRequestPanel()
    {
        requestTextArea = createTextArea();
        return createTextPanel( "XML API Request", requestTextArea );
    }

    /**
     * Create a panel for:
     * IBM Cognos Business Viewpoint XML API Response
     */
    private JPanel createResponsePanel()
    {
        responseTextArea = createTextArea();
        responseTextArea.setText( logonStatus ); // Show logon status upon startup
        return createTextPanel( "XML API Response", responseTextArea );
    }

    /**
     * Creates a text area for either Request or Response panels. 
     */
    private JTextArea createTextArea()
    {
        JTextArea textArea = new JTextArea();

        textArea.setEditable( true );
        textArea.setLineWrap( true );
        textArea.setWrapStyleWord( false );
        textArea.setFont( new Font( "Monospaced", Font.PLAIN, 16 ) );

        return textArea;
    }

    /**
     * Creates the panel to hold the supplied text area.
     * 
     * We call this function twice, to create two identical panels:
     * one for Request, another one for Response.
     *
     * @param panelName     Panel name.
     * @param textArea      Text area to put inside the panel.
     * @return
     */
    private JPanel createTextPanel( String panelName, JTextArea textArea )
    {
        JScrollPane textScrollPane = new JScrollPane( textArea );
        textScrollPane.setVerticalScrollBarPolicy( JScrollPane.VERTICAL_SCROLLBAR_ALWAYS );

        //Create the output panel and it's layout objects
        GridBagLayout layout = new GridBagLayout();
        JPanel textPanel = new JPanel( layout );
        GridBagConstraints layoutConstraints = new GridBagConstraints();

        //Set the layout for the scroll pane and add it
        layoutConstraints.weightx = 1.0;
        layoutConstraints.weighty = 1.0;
        layoutConstraints.fill = GridBagConstraints.BOTH;
        layout.setConstraints( textScrollPane, layoutConstraints );
        textPanel.add( textScrollPane );

        // put a border around the output and navigation buttons
        CompoundBorder border = BorderFactory.createCompoundBorder( BorderFactory.createCompoundBorder( BorderFactory.createTitledBorder( panelName ), BorderFactory.createEmptyBorder( 5, 5, 5, 5 ) ), textPanel.getBorder() );

        textPanel.setBorder( border );

        return textPanel;
    }

    /** 
     * Submit XML API request to Dimension Management Service.
     * 
     * @param request      XML API request.
     * @return XMl API response.
     */
    private String runRequest( String request )
    {
        DimensionManagementServiceSpecification dimsSpec = new DimensionManagementServiceSpecification();
        dimsSpec.setValue( new Specification( request ) );

        ParameterValue[] parameters = new ParameterValue[] {};
        Option[] options = getRequestOptions();

        AsynchReply reply = null;
        
        try
        {
            reply = connection.getDimensionManagementService().runSpecification( dimsSpec, parameters, options );
        }
        catch ( RemoteException e )
        {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter( sw );
            e.printStackTrace( pw );
            return sw.toString();
        }

        return getReplyAsString( reply );
    }

    /**
     * Return the options to pass on to runSpecification method.  The options become part of
     * BI bus SOAP command, where they can be accessed by dispatcher and the services.
     *     
     * @return array of Options.
     */
    private Option[] getRequestOptions()
    {
        AsynchOptionInt primaryThreshold = new AsynchOptionInt();
        primaryThreshold.setName( AsynchOptionEnum.primaryWaitThreshold );
        primaryThreshold.setValue( 0 );

        AsynchOptionInt secondaryThreshold = new AsynchOptionInt();
        secondaryThreshold.setName( AsynchOptionEnum.secondaryWaitThreshold );
        secondaryThreshold.setValue( 0 );

        Option[] options = new Option[2];
        options[0] = primaryThreshold;
        options[1] = secondaryThreshold;

        return options;
    }

    /**
     * Converts AsynchReply to XML.
     * 
     * @param reply     DimensionManagementService reply.
     * @return XML string.
     */
    private String getReplyAsString( AsynchReply reply )
    {
        if ( reply == null )
        {
            return "No reply.";
        }

        AsynchDetail[] details = reply.getDetails();       
        
		for (int i = 0; i < details.length; i++)
       
        {
			
            if ( details[i] instanceof AsynchDetailMIMEAttachment )
            {
                try
                {
                    AsynchDetailMIMEAttachment replyDetail = (AsynchDetailMIMEAttachment) details[i];
                    String replyXML = new String( replyDetail.getData(), "UTF-8" );
                    
                    return prettyPrint( replyXML );
                }
                catch ( UnsupportedEncodingException e )
                {
                    return "UnsupportedEncodingException occurred when trying to decode the response";
                }
            }
        }

        return "No reply details.";
    }
    
    /**
     * Pretty-print XML returned by Dimension Management Service.
     * 
     * @param xml   XML response.   
     * @return Pretty-printed XML.
     */
    private String prettyPrint( String xml )
    {
        try
        {
            Document document = DocumentHelper.parseText( xml ); 
            OutputFormat format = OutputFormat.createPrettyPrint();
            StringWriter stringWriter = new StringWriter();
            XMLWriter writer = new XMLWriter( stringWriter, format );
            writer.write( document );
            writer.close();
            return stringWriter.toString();            
        }
        catch ( Exception e )
        {
            // Failed to pretty-print. Return XML as is.
            return xml;
        }
    }
}
