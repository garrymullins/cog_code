/** 
Licensed Materials - Property of IBM

IBM Cognos Products: DOCS

(C) Copyright IBM Corp. 2005, 2008

US Government Users Restricted Rights - Use, duplication or disclosure restricted by GSA ADP Schedule Contract with
IBM Corp.
*/
/**
 *    UpgrageReportsTree.java
 * 
 * Copyright (C) 2008 Cognos ULC, an IBM Company. All rights reserved.
 * Cognos (R) is a trademark of Cognos ULC, (formerly Cognos Incorporated).
 * 
 */

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileOutputStream;

import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import com.cognos.developer.schemas.bibus._3.AddOptions;
import com.cognos.developer.schemas.bibus._3.AnyTypeProp;
import com.cognos.developer.schemas.bibus._3.AsynchDetailReportObject;
import com.cognos.developer.schemas.bibus._3.AsynchReply;
import com.cognos.developer.schemas.bibus._3.AsynchReplyStatusEnum;
import com.cognos.developer.schemas.bibus._3.AuthoredReport;
import com.cognos.developer.schemas.bibus._3.BaseClass;
import com.cognos.developer.schemas.bibus._3.Locale;
import com.cognos.developer.schemas.bibus._3.MultilingualToken;
import com.cognos.developer.schemas.bibus._3.MultilingualTokenProp;
import com.cognos.developer.schemas.bibus._3.Option;
import com.cognos.developer.schemas.bibus._3.ParameterValue;
import com.cognos.developer.schemas.bibus._3.PropEnum;
import com.cognos.developer.schemas.bibus._3.QueryOptions;
import com.cognos.developer.schemas.bibus._3.Report;
import com.cognos.developer.schemas.bibus._3.ReportServiceQueryOptionBoolean;
import com.cognos.developer.schemas.bibus._3.ReportServiceQueryOptionEnum;
import com.cognos.developer.schemas.bibus._3.ReportServiceQueryOptionSpecificationFormat;
import com.cognos.developer.schemas.bibus._3.SearchPathMultipleObject;
import com.cognos.developer.schemas.bibus._3.SearchPathSingleObject;
import com.cognos.developer.schemas.bibus._3.Sort;
import com.cognos.developer.schemas.bibus._3.SpecificationFormatEnum;
import com.cognos.developer.schemas.bibus._3.UpdateActionEnum;
import com.cognos.developer.schemas.bibus._3.UpdateOptions;

public class UpgradeReportsTree
	extends JPanel
	implements
		TreeSelectionListener,
		TreeExpansionListener,
		java.awt.event.MouseListener,
		java.awt.event.ActionListener
{
	private JTree tree;
	private static String defaultRootSearchPath = "/";

	private CRNConnect connection;

	public UpgradeReportsTree(CRNConnect oCrn)
	{
		super(new GridLayout(1, 0));

		connection = oCrn;

		//Create the nodes.
		DefaultMutableTreeNode top = new DefaultMutableTreeNode();
		TreeBrowserNode crnRootNode =
			new TreeBrowserNode(defaultRootSearchPath, connection);
		crnRootNode.setContainer(top);
		createNodes(top);

		for (int i = 0; i < top.getChildCount(); i++)
		{
			createNodes((DefaultMutableTreeNode)top.getChildAt(i));
		}

		((TreeBrowserNode)top.getUserObject()).setChildrenPopulated(true);

		//Create a tree that allows one selection at a time.
		tree = new JTree(top);
		tree.getSelectionModel().setSelectionMode(
			TreeSelectionModel.SINGLE_TREE_SELECTION);

		//Listen for when the selection changes.
		tree.addTreeExpansionListener(this);
		tree.addTreeSelectionListener(this);
		tree.addMouseListener(this);
		tree.setCellRenderer(new TreeBrowserCellRenderer());

		//Create the scroll pane and add the tree to it. 
		JScrollPane treeView = new JScrollPane(tree);

		Dimension minimumSize = new Dimension(100, 50);
		treeView.setMinimumSize(minimumSize);
		add(treeView);

		//Set the root node as the currently selected node in the tree
		tree.setSelectionRow(0);
	}

	/** Required by MouseListener interface. */
	public void mousePressed(MouseEvent mEvent)
	{}
	public void mouseEntered(MouseEvent mEvent)
	{}
	public void mouseExited(MouseEvent mEvent)
	{}
	public void mouseReleased(MouseEvent mEvent)
	{}
	public void mouseClicked(MouseEvent mEvent)
	{
		if (javax.swing.SwingUtilities.isRightMouseButton(mEvent))
		{
			javax.swing.JMenu upgrade = new javax.swing.JMenu("Upgrade");
			javax.swing.JMenu extract = new javax.swing.JMenu("Extract");
			javax.swing.JMenuItem upgradeItem =
				new javax.swing.JMenuItem(UpgradeReports.UPGRADE_SPEC_STRING);
			upgradeItem.addActionListener(this);

			javax.swing.JMenuItem upgradeCopyItem =
				new javax.swing.JMenuItem(UpgradeReports.UPGRADE_COPY_SPEC_STRING);
			upgradeCopyItem.addActionListener(this);

			javax.swing.JMenuItem extractItem =
				new javax.swing.JMenuItem(UpgradeReports.EXTRACT_SPEC_STRING);
			extractItem.addActionListener(this);

			javax.swing.JMenuItem extractUpgradeItem =
				new javax.swing.JMenuItem(UpgradeReports.EXTRACT_UPGRADED_SPEC_STRING);
			extractUpgradeItem.addActionListener(this);

			javax.swing.JPopupMenu dropDownMenu = new javax.swing.JPopupMenu();
			upgrade.add(upgradeItem);
			upgrade.add(upgradeCopyItem);
			dropDownMenu.add(upgrade);
			extract.add(extractItem);
			extract.add(extractUpgradeItem);
			dropDownMenu.add(extract);
			
			dropDownMenu.show(
				mEvent.getComponent(),
				mEvent.getX(),
				mEvent.getY());
		}

		if (javax.swing.SwingUtilities.isLeftMouseButton(mEvent))
		{
			if (mEvent.getClickCount() > 1)
			{
				if (mEvent.getSource().getClass() == JTable.class)
				{
					JTable tableForDBLClick = (JTable)mEvent.getSource();
					TreeBrowserNode nodeForDBLClick =
						(
							(TreeBrowserTableModel)tableForDBLClick
								.getModel())
								.getTbnForRow(
							tableForDBLClick.getSelectedRow());
					exploreNode(nodeForDBLClick);

				}
			}
		}
	}

	public void exploreNode(TreeBrowserNode node)
	{
		tree.setSelectionPath(new TreePath((node.getContainer().getPath())));
	}

	public void treeCollapsed(TreeExpansionEvent teEvent)
	{}

	public void treeExpanded(TreeExpansionEvent teEvent)
	{
		DefaultMutableTreeNode currentExpandedNode =
			(DefaultMutableTreeNode)teEvent.getPath().getLastPathComponent();
		for (int i = 0; i < currentExpandedNode.getChildCount(); i++)
		{
			createNodes(
				(DefaultMutableTreeNode)currentExpandedNode.getChildAt(i));
		}
	}

	/** Required by TreeSelectionListener interface. */
	public void valueChanged(TreeSelectionEvent e)
	{
		DefaultMutableTreeNode node =
			(DefaultMutableTreeNode)tree.getLastSelectedPathComponent();

		if (node != null)
		{
			createNodes(node);

			//This updates the searchPath textBox in the GUI
			if ((this.getParent() != null)
				&& (this.getParent().getParent().getParent().getParent() != null))
			{
				(
					(UpgradeReports)this
						.getParent()
						.getParent()
						.getParent()
						.getParent())
						.updateSelectedSearchPath(
					((TreeBrowserNode)node.getUserObject())
						.getCMObject()
						.getSearchPath()
						.getValue());

			}
		}
	}

	public void actionPerformed(java.awt.event.ActionEvent action)
	{
		JMenuItem actionSource = (JMenuItem)action.getSource();
		TreeBrowserNode nodeForPopup;
		nodeForPopup =
			(TreeBrowserNode) ((DefaultMutableTreeNode)tree
				.getLastSelectedPathComponent())
				.getUserObject();

		if (UpgradeReports.UPGRADE_SPEC_STRING == actionSource.getText())
		{
			showUpgradeDialog(nodeForPopup, connection);
		}
		else if (UpgradeReports.UPGRADE_COPY_SPEC_STRING == actionSource.getText())
		{
			showUpgradeCopyDialog(nodeForPopup, connection);
		}
		else if (UpgradeReports.EXTRACT_SPEC_STRING == actionSource.getText())
		{
			showExtractDialog(nodeForPopup, connection);
		}
		else if (UpgradeReports.EXTRACT_UPGRADED_SPEC_STRING == actionSource.getText())
		{
			showExtractUpgradedDialog(nodeForPopup, connection);
		}

	}

	public TreeBrowserNode getSelectedNode()
	{
		TreeBrowserNode node;
		node =
			(TreeBrowserNode) ((DefaultMutableTreeNode)tree
				.getLastSelectedPathComponent())
				.getUserObject();
		return node;
	}
	
	public void showUpgradeDialog(TreeBrowserNode node, CRNConnect connection)
	{
		Option[] queryOpts = new Option[2];
		ReportServiceQueryOptionBoolean upgradeSpecFlag = new ReportServiceQueryOptionBoolean();
		upgradeSpecFlag.setName(ReportServiceQueryOptionEnum.upgrade);
		upgradeSpecFlag.setValue(true);
				
		ReportServiceQueryOptionSpecificationFormat specFormat = new ReportServiceQueryOptionSpecificationFormat();
		specFormat.setName(ReportServiceQueryOptionEnum.specificationFormat);
		specFormat.setValue(SpecificationFormatEnum.report);
				
		queryOpts[0] = upgradeSpecFlag;
		queryOpts[1] = specFormat;


		if (node == null)
		{
			node = getSelectedNode();
		}
		
		int ok =
			JOptionPane.showConfirmDialog(
				null,
				"This will replace existing report specification(s) with up-to-date version(s) in the content store. Once performed, this action cannot be undone.");
		if (ok == JOptionPane.OK_OPTION)
		{
			ok =
				JOptionPane.showConfirmDialog(
					null,
					"I really, really mean it. You can't go back. Think about it.");
			if (ok == JOptionPane.OK_OPTION)
			{

				AuthoredReport reportToUpdate = null;
				try
				{

					BaseClass[] reports =
						getReportList(node.getCMObject(), connection);
					for (int i = 0; i < reports.length; i++)
					{
						AsynchReply qrResult =
							connection.getReportService().query(
								new SearchPathSingleObject(reports[i].getSearchPath().getValue()),
								new ParameterValue[] {},
								queryOpts);
								
						if ( (qrResult.getStatus() == AsynchReplyStatusEnum.working)
						   || (qrResult.getStatus() == AsynchReplyStatusEnum.stillWorking) )
						{
							while ( (qrResult.getStatus() == AsynchReplyStatusEnum.working)
							|| (qrResult.getStatus() == AsynchReplyStatusEnum.stillWorking) )
							{
								qrResult = connection.getReportService().wait(qrResult.getPrimaryRequest(), new ParameterValue[] {}, new Option[] {});
							}
						}
						
						for (int j = 0; j < qrResult.getDetails().length; j++)
						{
							if ( qrResult.getDetails()[j] instanceof AsynchDetailReportObject)
							{
								reportToUpdate = ( (AsynchDetailReportObject) qrResult.getDetails()[j]).getReport();
							}
						}
						updateReportsInContentStore(
							reportToUpdate,
							reports[i].getParent().getValue()[0],
							connection);
					}
				}
				catch (java.rmi.RemoteException remoteEx)
				{
					remoteEx.printStackTrace();
				}
			}
			else
			{
				JOptionPane.showMessageDialog(null, "Cancelled.");
			}
		}
		else
		{
			JOptionPane.showMessageDialog(null, "Cancelled.");
		}
	}

	public void showUpgradeCopyDialog(
		TreeBrowserNode node,
		CRNConnect connection)
	{
		Option[] queryOpts = new Option[2];
		
		ReportServiceQueryOptionBoolean upgradeSpecFlag = new ReportServiceQueryOptionBoolean();
		upgradeSpecFlag.setName(ReportServiceQueryOptionEnum.upgrade);
		upgradeSpecFlag.setValue(true);
				
		ReportServiceQueryOptionSpecificationFormat specFormat = new ReportServiceQueryOptionSpecificationFormat();
		specFormat.setName(ReportServiceQueryOptionEnum.specificationFormat);
		specFormat.setValue(SpecificationFormatEnum.report);
				
		queryOpts[0] = upgradeSpecFlag;
		queryOpts[1] = specFormat;


		if (node == null)
		{
			node = getSelectedNode();
		}

		int ok =
			JOptionPane.showConfirmDialog(
				null,
				"Retrieve up-to-date version of report specification(s) and add to the content store.");
		if (ok == JOptionPane.OK_OPTION)
		{
			try
			{

				AuthoredReport reportCopy = null;
				BaseClass[] reports =
					getReportList(node.getCMObject(), connection);
				for (int i = 0; i < reports.length; i++)
				{
					AsynchReply qrResult =
						connection.getReportService().query(
							new SearchPathSingleObject(reports[i].getSearchPath().getValue()),
							new ParameterValue[] {},
							queryOpts);
							
					if ( (qrResult.getStatus() == AsynchReplyStatusEnum.working)
					   || (qrResult.getStatus() == AsynchReplyStatusEnum.stillWorking) )
					{
						while ( (qrResult.getStatus() == AsynchReplyStatusEnum.working)
						|| (qrResult.getStatus() == AsynchReplyStatusEnum.stillWorking) )
						{
							qrResult = connection.getReportService().wait(qrResult.getPrimaryRequest(), new ParameterValue[] {}, new Option[] {});
						}
					}
						
					for (int j = 0; j < qrResult.getDetails().length; j++)
					{
						if ( qrResult.getDetails()[j] instanceof AsynchDetailReportObject)
						{
							reportCopy = ( (AsynchDetailReportObject) qrResult.getDetails()[j]).getReport();
						}
					}
					copyReportSpecInContentStore(
						reportCopy,
						reports[i].getParent().getValue()[0],
						connection);
				}
			}
			catch (java.rmi.RemoteException remoteEx)
			{
				remoteEx.printStackTrace();
			}
		}
		else
		{
			JOptionPane.showMessageDialog(null, "Cancelled.");
		}
	}

	public void showExtractDialog(TreeBrowserNode node, CRNConnect connection)
	{
		File saveDir = getFileFolder();

		if (saveDir == null)
		{
			return;
		}

		if (node == null)
		{
			node = getSelectedNode();
		}

		Option[] queryOpts = new Option[2];
		
		ReportServiceQueryOptionBoolean upgradeSpecFlag = new ReportServiceQueryOptionBoolean();
		upgradeSpecFlag.setName(ReportServiceQueryOptionEnum.upgrade);
		upgradeSpecFlag.setValue(false);
				
		ReportServiceQueryOptionSpecificationFormat specFormat = new ReportServiceQueryOptionSpecificationFormat();
		specFormat.setName(ReportServiceQueryOptionEnum.specificationFormat);
		specFormat.setValue(SpecificationFormatEnum.report);
				
		queryOpts[0] = upgradeSpecFlag;
		queryOpts[1] = specFormat;

		try
		{
			
			AuthoredReport reportToExtract = null;

			BaseClass[] reports =
				getReportList(node.getCMObject(), connection);
			for (int i = 0; i < reports.length; i++)
			{
				AsynchReply qrResult =
					connection.getReportService().query(
						new SearchPathSingleObject(reports[i].getSearchPath().getValue()),
						new ParameterValue[]{},
						queryOpts);
						
				if ( (qrResult.getStatus() == AsynchReplyStatusEnum.working)
				   || (qrResult.getStatus() == AsynchReplyStatusEnum.stillWorking) )
				{
					while ( (qrResult.getStatus() == AsynchReplyStatusEnum.working)
					|| (qrResult.getStatus() == AsynchReplyStatusEnum.stillWorking) )
					{
						qrResult = connection.getReportService().wait(qrResult.getPrimaryRequest(), new ParameterValue[] {}, new Option[] {});
					}
				}
						
				for (int j = 0; j < qrResult.getDetails().length; j++)
				{
					if ( qrResult.getDetails()[j] instanceof AsynchDetailReportObject)
					{
						reportToExtract = ( (AsynchDetailReportObject) qrResult.getDetails()[j]).getReport();
					}
				}
				saveReportSpecLocally(reportToExtract, saveDir);
			}
		}
		catch (java.rmi.RemoteException remoteEx)
		{
			remoteEx.printStackTrace();
		}
	}

	public void showExtractUpgradedDialog(
		TreeBrowserNode node,
		CRNConnect connection)
	{
		File saveDir = getFileFolder();

		if (saveDir == null)
		{
			return;
		}

		if (node == null)
		{
			node = getSelectedNode();
		}

		Option[] queryOpts = new Option[2];

		ReportServiceQueryOptionBoolean upgradeSpecFlag = new ReportServiceQueryOptionBoolean();
		upgradeSpecFlag.setName(ReportServiceQueryOptionEnum.upgrade);
		upgradeSpecFlag.setValue(true);
				
		ReportServiceQueryOptionSpecificationFormat specFormat = new ReportServiceQueryOptionSpecificationFormat();
		specFormat.setName(ReportServiceQueryOptionEnum.specificationFormat);
		specFormat.setValue(SpecificationFormatEnum.report);
				
		queryOpts[0] = upgradeSpecFlag;
		queryOpts[1] = specFormat;

		try
		{
			AuthoredReport reportToExtract = null;

			BaseClass[] reports =
				getReportList(node.getCMObject(), connection);
			for (int i = 0; i < reports.length; i++)
			{
				AsynchReply qrResult =
					connection.getReportService().query(
						new SearchPathSingleObject(reports[i].getSearchPath().getValue()),
						new ParameterValue[]{},
						queryOpts);
						
				if ( (qrResult.getStatus() == AsynchReplyStatusEnum.working)
				   || (qrResult.getStatus() == AsynchReplyStatusEnum.stillWorking) )
				{
					while ( (qrResult.getStatus() == AsynchReplyStatusEnum.working)
					|| (qrResult.getStatus() == AsynchReplyStatusEnum.stillWorking) )
					{
						qrResult = connection.getReportService().wait(qrResult.getPrimaryRequest(), new ParameterValue[] {}, new Option[] {});
					}
				}
						
				for (int j = 0; j < qrResult.getDetails().length; j++)
				{
					if ( qrResult.getDetails()[j] instanceof AsynchDetailReportObject)
					{
						reportToExtract = ( (AsynchDetailReportObject) qrResult.getDetails()[j]).getReport();
					}
				}
				
				saveReportSpecLocally(
					reportToExtract,
					saveDir);
			}
		}
		catch (java.rmi.RemoteException remoteEx)
		{
			remoteEx.printStackTrace();
		}
	}

	public BaseClass[] getReportList(
		BaseClass rootObject,
		CRNConnect connection)
	{
		String searchPathExpression;
		if (rootObject instanceof Report)
		{
			searchPathExpression = rootObject.getSearchPath().getValue();
		}
		else
		{
			searchPathExpression =
				rootObject.getSearchPath().getValue() + "//report";
		}

		PropEnum[] props =
			new PropEnum[] {
				PropEnum.searchPath,
				PropEnum.defaultName,
				PropEnum.parent };
		try
		{
			return connection.getCMService().query(
				new SearchPathMultipleObject(searchPathExpression),
				props,
				new Sort[] {},
				new QueryOptions());
		}
		catch (java.rmi.RemoteException remoteEx)
		{
			remoteEx.printStackTrace();
		}

		return null;
	}

	public boolean updateReportsInContentStore(
		AuthoredReport reportToSave,
		BaseClass parent,
		CRNConnect connection)
	{

		reportToSave.setMetadataModelPackage(null);
		reportToSave.setDefaultName(null);
		reportToSave.setDescription(null);

		try
		{			
			connection.getReportService().update(reportToSave, new UpdateOptions());
		}
		catch (java.rmi.RemoteException remoteEx)
		{
			return false;
		}

		return true;
	}

	public boolean copyReportSpecInContentStore(
		AuthoredReport reportToSave,
		BaseClass parent,
		CRNConnect connection)
	{

		String reportName = reportToSave.getDefaultName().getValue();
		Report newReport = new Report();

		AnyTypeProp reportSpecProperty = new AnyTypeProp();
		reportSpecProperty.setValue(reportToSave.getSpecification().getValue());

		MultilingualToken[] reportNames = new MultilingualToken[1];
		reportNames[0] = new MultilingualToken();
		reportNames[0].setValue(reportName);

		CSHandlers csHandler = new CSHandlers();
		Locale[] locales = csHandler.getConfiguration(connection);
		if (locales == null)
		{
			locales[0] = new Locale();
			locales[0].setLocale("en");
		}
		reportNames[0].setLocale(locales[0].getLocale());
		reportNames[0].setValue(reportName);

		newReport.setName(new MultilingualTokenProp());
		newReport.getName().setValue(reportNames);
		newReport.setSpecification(reportSpecProperty);

		AddOptions addReportOptions = new AddOptions();
		addReportOptions.setUpdateAction(UpdateActionEnum.replace);

		String parentPath = parent.getSearchPath().getValue();

		try
		{
			BaseClass[] targetDir =
				csHandler.createDirectoryInCS(
					connection,
					parentPath,
					"Upgrade");
			if (targetDir.length <= 0)
			{
				return false;
			}
			String targetPath = targetDir[0].getSearchPath().getValue();
			connection.getReportService().add(new SearchPathSingleObject(targetPath), newReport, addReportOptions);
		}
		catch (java.rmi.RemoteException remoteEx)
		{
			//return false;
		}

		return true;
	}

	public void saveReportSpecLocally(BaseClass reportToSave, File saveDir)
	{
		String reportFileName = reportToSave.getDefaultName().getValue();
		reportFileName = reportFileName.replace('/', '_');
		reportFileName = reportFileName + ".xml";

		if (!saveDir.isDirectory())
		{
			return;
		}

		try
		{
			File reportFile = new File(saveDir, reportFileName);
			FileOutputStream fos = new FileOutputStream(reportFile);
			fos.write(
				((AuthoredReport)reportToSave)
					.getSpecification()
					.getValue()
					.getBytes());
			fos.flush();
			fos.close();
		}
		catch (java.io.IOException ioEx)
		{
			System.out.println("Failed to write file: " + reportFileName);
			ioEx.printStackTrace();
		}

		return;
	}

	private File getFileFolder()
	{
		final JFileChooser fc =
			new JFileChooser(System.getProperty("user.dir"));
		fc.setFileFilter(new DIRFileFilter());
		fc.setAcceptAllFileFilterUsed(false);
		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

		int fileSelectedOK = fc.showOpenDialog(null);
		if (fileSelectedOK != JFileChooser.APPROVE_OPTION)
		{
			return null;
		}
		return fc.getSelectedFile();
	}

	private void createNodes(DefaultMutableTreeNode top)
	{
		if (((TreeBrowserNode)top.getUserObject()).getChildrenPopulated())
		{
			return;
		}

		DefaultMutableTreeNode subNode = null;

		TreeBrowserNode tmpNode = (TreeBrowserNode)top.getUserObject();
		for (int i = 0; i < tmpNode.getNumChildren(); i++)
		{
			TreeBrowserNode child = tmpNode.getChild(i, connection);
			subNode = new DefaultMutableTreeNode();
			child.setContainer(subNode);
			top.add(subNode);
		}
		((TreeBrowserNode)top.getUserObject()).setChildrenPopulated(true);
	}

	private class DIRFileFilter extends FileFilter
	{
		public boolean accept(File f)
		{
			if (f.isDirectory())
			{
				return true;
			}
			else
			{
				return false;
			}
		}

		public String getDescription()
		{
			return "Only Directories";
		}
	}
}
