package org.jacorb.ir.gui;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1997-2001  Gerald Brose.
 *
 *   This library is free software; you can redistribute it and/or
 *   modify it under the terms of the GNU Library General Public
 *   License as published by the Free Software Foundation; either
 *   version 2 of the License, or (at your option) any later version.
 *
 *   This library is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *   Library General Public License for more details.
 *
 *   You should have received a copy of the GNU Library General Public
 *   License along with this library; if not, write to the Free
 *   Software Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */

import javax.swing.*;
import javax.swing.table.*;
import javax.swing.tree.*;
import javax.swing.event.*;

import java.awt.*;
import java.awt.event.*;

import org.jacorb.ir.gui.typesystem.*;

/**
 * @author (c) Joerg von Frantzius, Gerald Brose, FU Berlin
 * @version $Id$
 */

class IRBrowser 
    extends JFrame 
    implements java.awt.event.WindowListener,
    java.awt.event.MouseListener,
    ListSelectionListener,
    TreeSelectionListener,
    ActionListener
{
    JTable contentTable;
    JSplitPane splitPane;
    JTree treeView;
    JTextArea textArea;
    TypeSystem typeSystem;
    TreeModel treeModel;
    JMenuItem followTypeMenu;
    private static final String title = "IRBrowser";

    /**
     * Constructor
     */

    public IRBrowser() 
    {
	super();
	typeSystem = new org.jacorb.ir.gui.typesystem.remote.RemoteTypeSystem();
	initialize();
    }

    /**
     * @param repositoryIOR java.lang.String
     */

    public IRBrowser (String repositoryIOR) 
    {
	super();
	typeSystem = new org.jacorb.ir.gui.typesystem.remote.RemoteTypeSystem(repositoryIOR);
	initialize();	
    }

    /**
     * @param event java.awt.ActionEvent
     */

    public void actionPerformed (java.awt.event.ActionEvent event) 
    {
	NodeMapper nodeMapper = 
            (NodeMapper)contentTable.getModel().getValueAt(contentTable.getSelectedRow(),0);
	TypeSystemNode typeSystemNode = nodeMapper.getNode();
	followTypeOf(typeSystemNode);
	System.out.println("following type of "+typeSystemNode);
    }

    /**
     * conn0:  (IRBrowser.window.windowClosing(java.awt.event.WindowEvent) --> IRBrowser.dispose())
     * @param arg1 java.awt.event.WindowEvent
     */

    private void conn0(java.awt.event.WindowEvent arg1) 
    {
	try {
            // user code begin {1}
            // user code end
            this.dispose();
            // user code begin {2}
            // user code end
	} catch (java.lang.Throwable ivjExc) {
            // user code begin {3}
            // user code end
            handleException(ivjExc);
	}
    }

    /**
     * @param typeSystemNode typesystem.TypeSystemNode
     */

    public void followTypeOf(TypeSystemNode typeSystemNode) 
    {
	DefaultMutableTreeNode treeNode=null;
	
	if (typeSystemNode instanceof TypeAssociator) 
        {
            TypeSystemNode assTypeNode = 
                ((TypeAssociator)typeSystemNode).getAssociatedTypeSystemNode();
            if (assTypeNode.getModelRepresentant(treeModel)!=null) 
            {
                treeNode = 
                    (DefaultMutableTreeNode)assTypeNode.getModelRepresentant(treeModel);
            }
	}		
	if (treeNode!=null ) 
        {
            // wenn Node ein AbstractContainer ist oder eine assoziierte TypeSystemNode besitzt, jeweils im treeView dorthin springen
            System.out.println("expanding Tree: "+treeNode);
            DefaultTreeModel treeModel = 
                (DefaultTreeModel)treeView.getModel();
            TreePath fullTreePath = 
                new TreePath(treeModel.getPathToRoot(treeNode));
	
            treeView.scrollPathToVisible(fullTreePath);
            // Selection auf node setzen
            treeView.setSelectionPath(fullTreePath);
            treeView.validate();
	}
    }	

    /**
     * Called whenever the part throws an exception.
     * @param exception java.lang.Throwable
     */

    private void handleException(Throwable exception) 
    {
	/* Uncomment the following lines to print uncaught exceptions to stdout */
	jacorb.util.Debug.output(2,exception);
    }

    /**
     * Initializes connections
     */
    private void initConnections() 
    {
	this.addWindowListener(this);
    }


    /**
     * Initialize class
     */

    public void initialize() 
    {
        //	setBackground(java.awt.Color.lightGray);
	setTitle(title);

	splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);

	// Create the table
	DefaultTableModel tableModel = typeSystem.getTableModel(null);
	contentTable = new JTable(tableModel);
	contentTable.setAutoCreateColumnsFromModel(true);
        //	contentTable.setModel(tableModel);
	contentTable.setColumnSelectionAllowed(false);
	contentTable.setRowSelectionAllowed(true); 
	contentTable.setCellSelectionEnabled(false);
	contentTable.removeEditor();
	contentTable.setShowGrid(false);
	contentTable.setTableHeader(new javax.swing.table.JTableHeader(contentTable.getColumnModel()));
	contentTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
	//    contentTable.setBackground(java.awt.Color.white);
        //	contentTable.setMinimumSize(new java.awt.Dimension(100,contentTable.getMinimumSize().height));
	contentTable.setMinimumSize(new Dimension(100,100));
	contentTable.getSelectionModel().addListSelectionListener(this);


	this.treeModel = typeSystem.getTreeModel();
        //	this.treeModel = typeSystem.createTreeModelRoot();
	treeView = new JTree(treeModel);
        //	treeView.addTreeExpansionListener(typeSystem.getTreeExpansionListener(this.treeModel));
	treeView.setRootVisible(true);
	treeView.setShowsRootHandles(true);
        //	treeView.setBackground(java.awt.Color.lightGray);
        //	treeView.setMinimumSize(new java.awt.Dimension(200,100));
	treeView.setMinimumSize(new Dimension(100,100));
	treeView.addTreeSelectionListener(this);

	JScrollPane tableScrollPane = JTable.createScrollPaneForTable(contentTable);
	JScrollPane treeScrollPane = new JScrollPane(treeView);
        // tableScrollPane.setBackground(java.awt.Color.white);
	tableScrollPane.setMinimumSize(new java.awt.Dimension(100,100));
	treeScrollPane.setMinimumSize(new java.awt.Dimension(100,100));
	tableScrollPane.setPreferredSize(new java.awt.Dimension(100,100));
	treeScrollPane.setPreferredSize(new java.awt.Dimension(100,100));

	textArea = new JTextArea("Hallo, hallo!");
	textArea.setEditable(false);
	textArea.setFont(new java.awt.Font("sansserif",java.awt.Font.PLAIN,10));
	textArea.setRows(4);
	textArea.setTabSize(15);
	textArea.setBorder(BorderFactory.createLoweredBevelBorder());
	textArea.setMinimumSize(new java.awt.Dimension(100,100));
	textArea.setPreferredSize(new Dimension(600,90));
        //	textArea.setBackground(java.awt.Color.lightGray);
	
	splitPane.setLeftComponent(new JScrollPane(treeView));
	splitPane.setRightComponent(tableScrollPane);
        //	splitPane.setRightComponent(new JScrollPane(contentTable));
	splitPane.setDividerLocation(300);

	// Hinzufügen der Komponenten
	Container contentPane = getContentPane();
        //	contentPane.setBackground(java.awt.Color.lightGray);
        /*	getContentPane().setLayout(new java.awt.BorderLayout());
                getContentPane().add(splitPane, java.awt.BorderLayout.NORTH);
                getContentPane().add(textArea, java.awt.BorderLayout.SOUTH);
        */
        //	contentPane.setLayout(new BorderLayout());
        /*	Container north = new JPanel();
                north.setLayout(new SpringLayout());
                Container south = new JPanel();
                south.setLayout(new SpringLayout());
                north.add(splitPane, SpringLayout.HEIGHT_WIDTH_SPRING);
                south.add(textArea);
                contentPane.add(north,BorderLayout.NORTH);
                contentPane.add(south,BorderLayout.SOUTH);
        */
        //	contentPane.add(splitPane,"North");
        //	contentPane.add(textArea,"South");
	splitPane.setBounds(0,0,600,400-textArea.getPreferredSize().height);
	textArea.setBounds(0,splitPane.bounds().height,600,textArea.getPreferredSize().height);
        //	contentPane.setLayout(layout);
	contentPane.setLayout(new BorderLayout());
	contentPane.add("Center",splitPane);

        //	contentPane.add(textArea);
	contentPane.add("South",textArea);
	contentPane.setBackground(Color.white);

	resize(600,400);
        //	contentPane.setBounds();

        //	JPopupMenu popup = new JPopupMenu(treeView);
        //	popup.add(new JMenuItem("Hallo?"));	
        //	popup.addPopupMenuListener(new TreePopupMenuListener());
        //	treeView.add(popup);

	JMenuBar menuBar = new JMenuBar();
	JMenu menu = new JMenu("Navigate");
	menuBar.add(menu);
	followTypeMenu = new JMenuItem("Follow Type");
	followTypeMenu.setEnabled(false);
	followTypeMenu.addActionListener(this);	
	menu.add(followTypeMenu);
	setJMenuBar(menuBar);

	validate();
	treeView.expandPath(new TreePath(((DefaultMutableTreeNode)treeModel.getRoot()).getPath()));
	
	treeView.addMouseListener((MouseListener)this);
	contentTable.addMouseListener((MouseListener)this);
        //	pack();
        //	validate();
	// validate();
	// user code end
	setName("IRBrowser");
	setName("IRBrowser");
	initConnections();
	// user code begin {2}
	// user code end
    }

    private static void usage()
    {
        System.err.println("usage: IRBrowser [ IOR | -f <ior_file ]");
        System.exit(1);
    }


    /**
     * This method was created by a SmartGuide.
     * @param args java.lang.String[]
     */

    public static void main(String args[]) 
    {
        IRBrowser test = null;
        String ior = null;

        if( args.length > 2 )
        {
            usage();
        }
        else if( args.length == 2 && args[0].equals("-f"))
        {
            try 
            {
                java.io.BufferedReader in = 
                    new java.io.BufferedReader(new java.io.FileReader( args[1] ) ); 
                ior = in.readLine();
                while (ior.indexOf("IOR:") != 0) 
                    ior = in.readLine();					
                in.close();
            } 
            catch ( java.io.IOException io )
            { 
                io.printStackTrace(); 
                usage();
            }
            test = new IRBrowser(ior);
        }
        else if( args.length == 1 )
        {
            test = new IRBrowser(args[0]);
        }
        else 
        {	
            test = new IRBrowser();
        }	
        test.show();
        return;
    }

    public void mouseClicked(MouseEvent event) 
    {
	javax.swing.tree.DefaultMutableTreeNode treeNode = null;
	// bei Doppelklick auf contentTable den treeView auf entsprechende TypeSystemNode setzen
	if (event.getComponent() == contentTable && 
            event.getClickCount()>1 && 
            contentTable.getSelectedRow()!=-1) 
        {
            System.out.println("contentTable doubleClick");
            // im TableModel steckt in jeder Zelle ein NodeMapper, von dem die dazugehörige treeNode zu erfahren ist
            NodeMapper nodeMapper = (NodeMapper)contentTable.getModel().getValueAt(contentTable.getSelectedRow(),0);
            TypeSystemNode typeSystemNode = nodeMapper.getNode();
            if (typeSystemNode instanceof AbstractContainer) 
            {
                treeNode = (DefaultMutableTreeNode)typeSystemNode.getModelRepresentant(treeModel);
            }
            if (typeSystemNode instanceof TypeAssociator) 
            {
                TypeSystemNode assTypeNode = ((TypeAssociator)typeSystemNode).getAssociatedTypeSystemNode();
                if (assTypeNode.getModelRepresentant(treeModel)!=null) {
                    treeNode = (DefaultMutableTreeNode)assTypeNode.getModelRepresentant(treeModel);
                }
            }		
            if (treeNode!=null ) 
            {
                // wenn Node ein AbstractContainer ist oder eine assoziierte TypeSystemNode besitzt, jeweils im treeView dorthin springen
                System.out.println("expanding Tree: "+treeNode);
                DefaultTreeModel treeModel = (DefaultTreeModel)treeView.getModel();
                TreePath fullTreePath = new TreePath(treeModel.getPathToRoot(treeNode));
		
                treeView.scrollPathToVisible(fullTreePath);
                // Selection auf node setzen
                treeView.setSelectionPath(fullTreePath);
                treeView.validate();
            }
	}
    }

    public void mouseEntered(MouseEvent event) {}
    public void mouseExited(MouseEvent event){}
    public void mousePressed(MouseEvent event){}
    public void mouseReleased(MouseEvent event){}

    /**
     * Setze Titel des Frames und enable/disable Menüs je nach selektierter Node
     * (Node kann in TableView oder in TreeView selektiert worden sein)
     * @param node typesystem.TypeSystemNode
     */

    public void setSelectedNode (TypeSystemNode node ) 
    {
	// Node kann TableView oder TreeView selektiert worden sein
	setTitle(title + " - " + node.getAbsoluteName());
	textArea.setText(node.description());
	if (node instanceof TypeAssociator) 
        {
            followTypeMenu.setEnabled(true);
	}	
	else 
        {
            followTypeMenu.setEnabled(false);
	}		
    }


    /**
     */

    public void valueChanged (ListSelectionEvent e ) 
    {
	// contentTable nur bei einfacher Selection ändern
        //	System.out.println("valueChanged (Table...)");
	TypeSystemNode node;
	if (contentTable.getSelectedRow()!=-1) 
        {
            NodeMapper nodeMapper = 
                (NodeMapper)contentTable.getModel().getValueAt(contentTable.getSelectedRow(),0);
            if ((node = ((TypeSystemNode)nodeMapper.getNode()))!=null) 
            {	
                setSelectedNode(node);
            }	
	}
    }

    /**
     */

    public void valueChanged (TreeSelectionEvent e ) 
    {
	// contentTable nur bei einfacher Selection ändern
	jacorb.util.Debug.output(4, "IRBrowser: valueChanged (Tree...)");
        //	if (e.getPaths().length == 1) { // funktioniert nicht
        DefaultMutableTreeNode treeNode = 
            (DefaultMutableTreeNode)e.getPath().getLastPathComponent();
        TypeSystemNode node = (TypeSystemNode)treeNode.getUserObject();

        contentTable.setModel(typeSystem.getTableModel(treeNode));
        contentTable.clearSelection();
        TableColumnModel tabColMod = contentTable.getColumnModel();

        for (int i=0; i<contentTable.getColumnCount(); i++) 
        {
            TableColumn tabCol = tabColMod.getColumn(i);
            tabCol.setCellEditor(null);	// sonst sind die Zeilen editierbar
        }	
        setSelectedNode(node);
        contentTable.validate();
        treeView.validate();
    }

    /**
     * Method to handle events for the WindowListener interface.
     * @param e java.awt.event.WindowEvent
     */

    public void windowActivated(java.awt.event.WindowEvent e) {
	// user code begin {1}
	// user code end
	// user code begin {2}
	// user code end
    }

    /**
     * Method to handle events for the WindowListener interface.
     * @param e java.awt.event.WindowEvent
     */

    /* WARNING: THIS METHOD WILL BE REGENERATED. */

    public void windowClosed(java.awt.event.WindowEvent e) 
    {
	// user code begin {1}
	System.exit(0);
	// user code end
	// user code begin {2}
	// user code end
    
    }

    /**
     * Method to handle events for the WindowListener interface.
     * @param e java.awt.event.WindowEvent
     */
    /* WARNING: THIS METHOD WILL BE REGENERATED. */

    public void windowClosing(java.awt.event.WindowEvent e) 
    {
	// user code begin {1}
	// user code end
	if ((e.getSource() == this) ) 
        {
            conn0(e);
	}
	// user code begin {2}
	// user code end
    }

    /**
     * Method to handle events for the WindowListener interface.
     * @param e java.awt.event.WindowEvent
     */
    /* WARNING: THIS METHOD WILL BE REGENERATED. */

    public void windowDeactivated(java.awt.event.WindowEvent e) {
	// user code begin {1}
	// user code end
	// user code begin {2}
	// user code end
    }

    /**
     * Method to handle events for the WindowListener interface.
     * @param e java.awt.event.WindowEvent
     */
    /* WARNING: THIS METHOD WILL BE REGENERATED. */

    public void windowDeiconified(java.awt.event.WindowEvent e) {
	// user code begin {1}
	// user code end
	// user code begin {2}
	// user code end
    }

    /**
     * Method to handle events for the WindowListener interface.
     * @param e java.awt.event.WindowEvent
     */
    /* WARNING: THIS METHOD WILL BE REGENERATED. */

    public void windowIconified(java.awt.event.WindowEvent e) {
	// user code begin {1}
	// user code end
	// user code begin {2}
	// user code end
    }

    /**
     * Method to handle events for the WindowListener interface.
     * @param e java.awt.event.WindowEvent
     */
    /* WARNING: THIS METHOD WILL BE REGENERATED. */

    public void windowOpened(java.awt.event.WindowEvent e) {
	// user code begin {1}
	// user code end
	// user code begin {2}
	// user code end
    }
}
