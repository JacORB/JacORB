package org.jacorb.orb.domain.gui;

import java.io.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;
import java.util.*;

import org.omg.CosNaming.*;

import org.jacorb.orb.domain.*;
import org.jacorb.util.*;

/**
 * This class represents a frame of the domain browser.
 * It contains most of the application code of the domain browser.
 * Only the data shared by all browsers is obtained via a SharedData-Object.
 *
 * @author Herbert Kiefer
 * @version 1.0
 * @see org.jacorb.orb.domain.gui.SharedData
 */

public class BrowserFrame 
    extends JFrame 
{
    JPanel contentPane;
    JMenuBar menuBar1 =         new JMenuBar();
    JMenu DomainMenu =          new JMenu();
    JMenuItem ExitMenuItem =    new JMenuItem();
    JMenu HelpMenu =            new JMenu();
    JMenuItem AboutMenuItem =   new JMenuItem();
    BorderLayout borderLayout1 = new BorderLayout();
    JSplitPane outerSplitPane = new JSplitPane();
    JScrollPane TreeScrollPane = new JScrollPane();

    JTree jTree1 = null;
    DefaultTreeModel treeModel= null;

    /** a reference to this frame */
    private BrowserFrame mainFrame;

    /** the used orb */
    private org.omg.CORBA.ORB orb= null;

    /** the root domain display in  the root of the tree, may be other
        than domain server*/
    org.jacorb.orb.domain.Domain theRootDomain= null;

    /** the local orb domain */
    org.jacorb.orb.domain.Domain orbDomain= null;

    /** the buffer where to temporalely store a domain member */
    org.omg.CORBA.Object memberBuffer    = null;
    String               memberNameBuffer= null;

    /** the default tree cell renderer shows no tool tips */
    DefaultTreeCellRenderer defaultTreeCellRenderer= 
        new DefaultTreeCellRenderer();

    /** the domain tree cell renderer shows tool tips */
    DomainTreeCellRenderer domainTreeCellRenderer= 
        new DomainTreeCellRenderer();

    /** the list cell renderer shows tool tips */
    PolicyListCellRenderer policyListCellRenderer= 
        new PolicyListCellRenderer();

    /** the invisible root node of the policy list */
    private PolicyListRootNode policyListRoot;

    /** the invisible root node of the member list */
    private MemberListRootNode memberListRoot;

    /** reference to the data all browser (frames) share. */
    private SharedData theSharedData;

    JMenu ViewMenu = new JMenu();
    JMenuItem jMenuItem1 = new JMenuItem();
    JPopupMenu PolicyPopupMenu = new JPopupMenu();
    JPopupMenu TreePopupMenu = new JPopupMenu();
    JMenuItem UpdateMenuItem = new JMenuItem();
    JSplitPane innerSplitPane = new JSplitPane();
    JScrollPane MemberScrollPane = new JScrollPane();
    JScrollPane PolicyScrollPane = new JScrollPane();
    // JList policyList = new JList();
    JTree policyList = new JTree();
    JPopupMenu MemberPopupMenu = new JPopupMenu();
    JMenuItem PolicyPropertyMenuItem = new JMenuItem();
    JMenuItem PropertiesMenuItem = new JMenuItem();
    JCheckBoxMenuItem showToolTipsMenuItem = new JCheckBoxMenuItem();
    JMenuItem RenameMenuItem = new JMenuItem();
    JMenuItem DomainRenameItem = new JMenuItem();
    JTree memberList = new JTree();
    JMenuItem DomainNewMenuItem = new JMenuItem();
    JMenuItem CutMenuItem = new JMenuItem();
    JMenuItem PolicyCutMenuItem = new JMenuItem();
    JMenuItem DomainCutMenuItem = new JMenuItem();
    JMenuItem CopyMenuItem = new JMenuItem();
    JMenuItem PasteMenuItem = new JMenuItem();
    JMenuItem DomainLoadMenuItem = new JMenuItem();
    JMenuItem CloseMenuItem = new JMenuItem();
    JMenuItem jMenuItem3 = new JMenuItem();
    JMenuItem PolicyCopyMenuItem = new JMenuItem();
    JMenuItem PolicyPasteMenuItem = new JMenuItem();
    JMenu PolicyNewMenu = new JMenu();
    JMenuItem PolicyNewPropertyPolicyMenuItem = new JMenuItem();
    JMenuItem PolicyNewMetaPropertyPolicyMenuItem = new JMenuItem();
    JMenuItem DomainCopyMenuItem = new JMenuItem();
    JMenuItem DomainPasteMenuItem = new JMenuItem();
    JMenuItem PolicyNewConflictResolutionPolicyMenuItem = new JMenuItem();
    // JTree memberList = null;


    /** creates a browser frame.
     *  The domain displayed as root in the domain tree is the domain server.
     */
    public BrowserFrame(SharedData shared)
    {
        mainFrame= this;
        theSharedData= shared;
        orb = theSharedData.getORB();
        Domain rootDomain= null;
        enableEvents(AWTEvent.WINDOW_EVENT_MASK);
        try
        {
            rootDomain= getDomainServer();
            check(rootDomain);
            initTree( rootDomain );

            jbInit();

            theSharedData.registerFrame(this); // callback
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    } // BrowserFrame(SharedData)

    /** 
     * creates a browser frame.
     *  @param rootDomain the domain displayed as root in the domain tree view.
     */
    public BrowserFrame(SharedData shared, Domain rootDomain) 
    {
        mainFrame= this;
        theSharedData= shared;
        enableEvents(AWTEvent.WINDOW_EVENT_MASK);
        try 
        {
            // initPropertyPolicy();
            initTree( rootDomain );
            jbInit();
            theSharedData.registerFrame(this); // callback
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    } // BrowserFrame(SharedData, Domain)


    /** 
     * checks if a  domain reference  is valid.  If the  reference is
     * invalid the program  gets terminated.  Currently only  used to
     * check the domain server.  
     */

    private void check(Domain aDomain)
    {
        if ( aDomain._non_existent() )
        {
            Debug.output(0, "call to domain server failed, reference invalid ?");
            Debug.output(0, "cannot continue, exiting.");
            System.exit(-1);
        }
    } // check


    /** 
     * initializes the tree, member list and policy list structure. 
     */

    private void initTree(org.jacorb.orb.domain.Domain RootDomain)
    {
        if (RootDomain == null)
	{
            Debug.output(Debug.DOMAIN | Debug.IMPORTANT, 
                         " BrowserFrame.initTree: parameter "
                         +" RootDomain is not valid (==null), canceling.");
            return;
	}
        theRootDomain = RootDomain; 
        // theRootDomain is the private class- wide variable
        DomainTreeNode rootNode= new DomainTreeNode( theRootDomain );
        treeModel= new DefaultTreeModel(rootNode);

        jTree1= new JTree(treeModel);
        jTree1.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        jTree1.putClientProperty("JTree.lineStyle", "Angled");


        // setup tool tips
        ToolTipManager.sharedInstance().registerComponent(jTree1);
        ToolTipManager.sharedInstance().registerComponent(policyList);
        jTree1.setCellRenderer(domainTreeCellRenderer);
        policyList.setCellRenderer(policyListCellRenderer);

        updateTreeView(rootNode);

        //////////// listeners //////////////////////

        // tree selection listener
        jTree1.addTreeSelectionListener( new TreeSelectionListener () 
            {
                public void valueChanged(TreeSelectionEvent e)
                {
                    DomainTreeNode node= 
                        (DomainTreeNode) jTree1.getLastSelectedPathComponent();

                    if (node == null) 
                        return;

                    DomainRenameItem.setEnabled(true); // enable renaming after node is selected

                    Domain domain= node.getDomain();
                    try
                    {
                        updateListModels(domain);
                    }
                    catch (org.omg.CORBA.COMM_FAILURE fail)
                    { 
                        // delete domain reference, if no longer valid
                        Debug.output(Debug.DOMAIN | 2,"The selected domain does not reply. "
                                     +" Removing it from domain graph ...");
                        DomainTreeNode parentNode= (DomainTreeNode) node.getParent();
                        if (parentNode == null)
                        {
                            Debug.output(Debug.DOMAIN | Debug.IMPORTANT,
                                         " valueChanged: cannot "
                                         + " remove root domain, cancel removing.");
                            return;
                        }
                        Domain parentDomain = parentNode.getDomain();
                        parentDomain.deleteChild(domain);

                        JOptionPane.showMessageDialog(mainFrame, 
                                                      "The domain does not reply. \n Removing domain",
                                                      "Broken Connection" , 
                                                      JOptionPane.INFORMATION_MESSAGE);
                        updateTreeView(parentNode);
                    }
                }
            }
                                         );
        // tree will expand listener

        jTree1.addTreeWillExpandListener(
            new TreeWillExpandListener () 
                {
                public void treeWillExpand(TreeExpansionEvent e)
                {
                    // org.jacorb.util.Debug.output(2, "tree will expand "+ e.getPath());
                    DomainTreeNode node= 
                        (DomainTreeNode) e.getPath().getLastPathComponent();
                    if (node == null) 
                        return;

                    if (node.viewNeedsUpdate) 
                        updateTreeView(node);
                    // else return;
                }
                public void treeWillCollapse(TreeExpansionEvent e) 
                {}
            });

        // tree model listener (listens for changes in tree node names) TODO
        // treeModel.addTreeModelListener(new TreeModelListener);


        // tree popop listener
        // jTree1.addMouseListener(this);

        // tree cell editor listener
        jTree1.setCellEditor(new DefaultTreeCellEditor(jTree1,
                                                       new DefaultTreeCellRenderer()));
        jTree1.getCellEditor().addCellEditorListener(new CellEditorListener()
            {
                public void editingStopped(ChangeEvent e)
                {
                    DomainTreeNode node= (DomainTreeNode) jTree1.getLastSelectedPathComponent();
                    if (node == null) return;

                    TreeNode parentNode= node.getParent();
                    if (parentNode == null) return;

                    // Debug.output(Debug.DOMAIN | Debug.INFORMATION, "editing Stopped");
                    Domain domain= node.getDomain();
                    DomainTreeNode domainParentNode= (DomainTreeNode) parentNode;
                    Domain parentDomain= domainParentNode.getDomain();

                    // there is a parent domain which has to agree on the new name, check for that
                    String nameBeforeEdit= "???";
                    try
                    {
                        String nameAfterEdit= node.toString();
                        nameBeforeEdit= domain.name();
                        Debug.output(Debug.DOMAIN | 5, "name before edit: " + nameBeforeEdit);
                        Debug.output(Debug.DOMAIN | 5, "name after  edit: " + nameAfterEdit);
                        if ( nameAfterEdit.equals(nameBeforeEdit) ) return;


                        parentDomain.renameChildDomain(nameBeforeEdit, nameAfterEdit);
                    }
                    catch (NameAlreadyDefined already)
                    {
                        JOptionPane.showMessageDialog(mainFrame, "Please choose another name.",
                                                      "Name Already in Use" , JOptionPane.ERROR_MESSAGE);

                        // set old name again und inform view
                        node.setUserObject(nameBeforeEdit);
                        ( (DefaultTreeModel) jTree1.getModel()).nodeChanged(node);
                    }
                    catch (InvalidName inv)
                    {
                        Debug.output(1, inv);
                    }
                    catch (org.omg.CORBA.COMM_FAILURE fail)
                    {
                        JOptionPane.showMessageDialog(mainFrame, "The domain "
                                                      +"does not reply. Operation canceled.",
                                                      "Broken Connection" , JOptionPane.INFORMATION_MESSAGE);

                        // set old name again und inform view
                        node.setUserObject(nameBeforeEdit);
                        ( (DefaultTreeModel) jTree1.getModel()).nodeChanged(node);
                    }



                } // editing Stopped
                public void editingCanceled(ChangeEvent e) {}
            }

                                                     ); // CellEditorListener for domain tree

        JMenuItem treeDependants[]={ DomainRenameItem, DomainCutMenuItem,
                                     DomainCopyMenuItem, UpdateMenuItem};
        jTree1.addMouseListener(new PopupMenuActivator
            (TreePopupMenu, jTree1, treeDependants) );


        //// init lists

        updateListModels( theRootDomain.createEmptyDomain() );

        // init member list
        //memberList.setCellRenderer(new MemberListCellRenderer());

        JMenuItem dependants[]={ PropertiesMenuItem, CutMenuItem, CopyMenuItem,
                                 RenameMenuItem};
        memberList.addMouseListener(new PopupMenuActivator
            (MemberPopupMenu, memberList, dependants) );

        memberList.getSelectionModel().setSelectionMode
            (TreeSelectionModel.SINGLE_TREE_SELECTION);
        memberList.setCellEditor(new DefaultTreeCellEditor(memberList,
                                                           new DefaultTreeCellRenderer()));
        memberList.getCellEditor().addCellEditorListener(new CellEditorListener()
            {
                public void editingStopped(ChangeEvent e)
                {
                    org.omg.CORBA.Object obj= null;
                    String nameBeforeEdit= "???";
                    Debug.myAssert(1, memberListRoot != null, "root of member list is null");

                    int i= memberList.getMinSelectionRow();
                    MemberListLeafNode node= (MemberListLeafNode) memberListRoot.getChildAt(i);
                    obj= node.getObject();
                    try
                    {
                        String nameAfterEdit= node.toString();
                        nameBeforeEdit= memberListRoot.getDomain().getNameOf(obj);
                        Debug.output(Debug.DOMAIN | 5, "name before edit: " + nameBeforeEdit);
                        Debug.output(Debug.DOMAIN | 5, "name after  edit: " + nameAfterEdit);
                        if ( nameAfterEdit.equals(nameBeforeEdit) ) return;


                        memberListRoot.getDomain().renameMember(nameBeforeEdit, nameAfterEdit);
                    }
                    catch (NameAlreadyDefined already)
                    {
                        JOptionPane.showMessageDialog(mainFrame, "Please choose another name.",
                                                      "Name Already in Use" , JOptionPane.ERROR_MESSAGE);

                        // set old name again und inform view
                        node.setName(nameBeforeEdit);
                        int indices[]= new int[1];
                        indices[0]= i;
                        ( (DefaultTreeModel) memberList.getModel()).nodesChanged
                            (memberListRoot, indices);
                    }
                    catch (org.omg.CORBA.COMM_FAILURE fail)
                    {
                        JOptionPane.showMessageDialog(mainFrame, "The domain containing the member "
                                                      +"does not reply. Operation canceled.",
                                                      "Broken Connection" , JOptionPane.INFORMATION_MESSAGE);

                        // set old name again und inform view
                        node.setName(nameBeforeEdit);
                        int indices[]= new int[1];
                        indices[0]= i;
                        ( (DefaultTreeModel) memberList.getModel()).nodesChanged
                            (memberListRoot, indices);
                    }
                    catch (InvalidName inv)
                    {
                        Debug.output(1, inv);
                    }


                } // editing Stopped
                public void editingCanceled(ChangeEvent e) {}
            }

                                                         ); // CellEditorListener for member list


        MemberScrollPane.setColumnHeaderView(new JLabel("Members", JLabel.CENTER));

        // init policy list

        // policyList.setCellRenderer(new PolicyListCellRenderer());
        JMenuItem policyDependants[]={ PolicyPropertyMenuItem, PolicyCutMenuItem,
                                       PolicyCopyMenuItem };
        policyList.addMouseListener(new PopupMenuActivator
            (PolicyPopupMenu, policyList, policyDependants) );
        policyList.getSelectionModel().setSelectionMode
            (TreeSelectionModel.SINGLE_TREE_SELECTION);
        PolicyScrollPane.setColumnHeaderView(new JLabel("Policies", JLabel.CENTER));

    } // initTree



    /** updates the models of the two lists, the member list and the policy list
     *  @param parent the domain which holds the data for both views
     */

    private void updateListModels(Domain parent)
    {
        memberListRoot = new MemberListRootNode(parent);
        DefaultTreeModel model = new DefaultTreeModel(memberListRoot);
        memberList.setModel(model);

        policyListRoot = new PolicyListRootNode(parent);
        DefaultTreeModel policyModel= new DefaultTreeModel(policyListRoot);
        policyList.setModel(policyModel);
    } // initListViews


    public org.omg.CORBA.ORB getORB()
    { 
        // delegate
        return theSharedData.getORB();
    }


    public org.jacorb.orb.domain.Domain getDomainServer()
    {
        org.jacorb.orb.domain.Domain result= null;
        try
        {
            result = 
                DomainHelper.narrow(getORB().resolve_initial_references("DomainService"));

            org.jacorb.util.Debug.myAssert(1, result != null, "domain server not running");
        }
        catch (org.omg.CORBA.ORBPackage.InvalidName inv)
        {
            org.jacorb.util.Debug.output(1, "domain service not found at orb, cannot continue.");
            System.exit(-1);
        }
        catch (Exception e)
        {
            org.jacorb.util.Debug.output(1, e);
            System.exit(-1);
        }

        return result;
    } // getDomainServer

    public org.jacorb.orb.domain.Domain getORBDomain()
    { // delegate
        return theSharedData.getORBDomain();
    } // getORBDomain

    /** updates the tree view. This is done by first deleting all child nodes
     * and then updating new childs from the underlying domain.
     */
    public void updateTreeView(DomainTreeNode root)
    {

        Domain domain= root.getDomain();
        Domain childs[]= domain.getChilds();

        org.jacorb.util.Debug.output(Debug.DOMAIN | Debug.DEBUG1, "updating node " + domain.name());

        // Note: deleting all old childs in the view and inserting all new childs
        // runs faster than checking new childs against old childs
        org.jacorb.util.Debug.output(Debug.DOMAIN | Debug.DEBUG1, "before remove " + root.getChildCount());

        int childIndices[]= new int[root.getChildCount()];
        Object removedChildren[]= new Object[root.getChildCount()];
        for (int i= 0; i < root.getChildCount(); i++)
        { // prepare call of operatoin nodesWereRemoved (see below)
            childIndices[i]= i;
            removedChildren[i]= root.getChildAt(i);
        }
        root.removeAllChildren(); // remove in node

        // remove in tree model
        treeModel.nodesWereRemoved(root, childIndices, removedChildren);

        org.jacorb.util.Debug.output(5, "after remove " + root.getChildCount());

        for (int j=0; j < childs.length; j++) // insert new childs
            treeModel.insertNodeInto(new DomainTreeNode(childs[j]), root, j);

        treeModel.nodeChanged(root);
        org.jacorb.util.Debug.output(5, "after insertion " + root.getChildCount());



        root.viewNeedsUpdate= false;

    } // updateTreeView


    /** release resource before shutdown */
    private void releaseRessources()
    {
        theSharedData.deregisterFrame(this);
    }

    //Component initialization
    private void jbInit() throws Exception  {
        contentPane = (JPanel) this.getContentPane();
        contentPane.setLayout(borderLayout1);
        this.setSize(new Dimension(400, 300));
        this.setTitle("Domain Browser");
        DomainMenu.setMnemonic('D');
        DomainMenu.setText("Domain");
        ExitMenuItem.setToolTipText("exit program");
        ExitMenuItem.setMnemonic('X');
        ExitMenuItem.setText("Exit");
        ExitMenuItem.addActionListener(new ActionListener()  {

                public void actionPerformed(ActionEvent e) {
                    fileExit_actionPerformed(e);
                }
            });
        HelpMenu.setMnemonic('H');
        HelpMenu.setText("Help");
        AboutMenuItem.setToolTipText("show about box");
        AboutMenuItem.setMnemonic('A');
        AboutMenuItem.setText("About");
        AboutMenuItem.addActionListener(new ActionListener()  {

                public void actionPerformed(ActionEvent e) {
                    OnHelpAbout(e);
                }
            });
        ViewMenu.setMnemonic('V');
        ViewMenu.setText("View");
        jMenuItem1.setToolTipText("update view of current domain");
        jMenuItem1.setMnemonic('U');
        jMenuItem1.setText("Update");
        jMenuItem1.addActionListener(new java.awt.event.ActionListener()
            {

                public void actionPerformed(ActionEvent e)
                {
                    OnViewUpdate(e);
                }
            });
        UpdateMenuItem.setText("Update");
        UpdateMenuItem.addActionListener(new java.awt.event.ActionListener()
            {

                public void actionPerformed(ActionEvent e)
                {
                    OnViewUpdate(e);
                }
            });
        outerSplitPane.setOneTouchExpandable(true);
        innerSplitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
        innerSplitPane.setOneTouchExpandable(true);
        PolicyPropertyMenuItem.setText("Properties");
        PolicyPropertyMenuItem.addActionListener(new java.awt.event.ActionListener()
            {

                public void actionPerformed(ActionEvent e)
                {
                    OnPolicyPopupProperties(e);
                }
            });
        PropertiesMenuItem.setText("Properties ...");
        PropertiesMenuItem.addActionListener(new java.awt.event.ActionListener()
            {

                public void actionPerformed(ActionEvent e)
                {
                    OnMemberPopupProperties(e);
                }
            });
        jTree1.setToolTipText("");
        jTree1.setEditable(true);
        showToolTipsMenuItem.setToolTipText("enable / disable tool tips");
        showToolTipsMenuItem.setSelected(true);
        showToolTipsMenuItem.setMnemonic('T');
        showToolTipsMenuItem.setText("Tool Tips");
        showToolTipsMenuItem.addActionListener(new java.awt.event.ActionListener()
            {

                public void actionPerformed(ActionEvent e)
                {
                    OnViewToolTips(e);
                }
            });
        RenameMenuItem.setText("Rename");
        RenameMenuItem.addActionListener(new java.awt.event.ActionListener()
            {

                public void actionPerformed(ActionEvent e)
                {
                    OnMemberPopupRename(e);
                }
            });
        treeModel.addTreeModelListener(new javax.swing.event.TreeModelListener()
            {

                public void treeNodesChanged(TreeModelEvent e)
                {
                    OnTreeNodesChanged(e);
                }

                public void treeNodesInserted(TreeModelEvent e)
                {
                }

                public void treeNodesRemoved(TreeModelEvent e)
                {
                }

                public void treeStructureChanged(TreeModelEvent e)
                {
                }
            });
        DomainRenameItem.setEnabled(false);
        DomainRenameItem.setText("Rename");
        DomainRenameItem.addActionListener(new java.awt.event.ActionListener()
            {

                public void actionPerformed(ActionEvent e)
                {
                    OnTreePopupRename(e);
                }
            });
        memberList.setEditable(true);
        memberList.setRootVisible(false);
        policyList.setRootVisible(false);
        DomainNewMenuItem.setToolTipText("create a new empty domain");
        DomainNewMenuItem.setMnemonic('N');
        DomainNewMenuItem.setText("New");
        DomainNewMenuItem.addActionListener(new java.awt.event.ActionListener()
            {

                public void actionPerformed(ActionEvent e)
                {
                    OnDomainNew(e);
                }
            });
        CutMenuItem.setToolTipText("delete member");
        CutMenuItem.setMnemonic('T');
        CutMenuItem.setText("Cut");
        CutMenuItem.addActionListener(new java.awt.event.ActionListener()
            {

                public void actionPerformed(ActionEvent e)
                {
                    OnMemberPopupCut(e);
                }
            });
        PolicyCutMenuItem.setToolTipText("delete policy");
        PolicyCutMenuItem.setMnemonic('T');
        PolicyCutMenuItem.setText("Cut");
        PolicyCutMenuItem.addActionListener(new java.awt.event.ActionListener()
            {

                public void actionPerformed(ActionEvent e)
                {
                    OnPolicyPopupCut(e);
                }
            });
        DomainCutMenuItem.setToolTipText("remove domain");
        DomainCutMenuItem.setMnemonic('T');
        DomainCutMenuItem.setText("Cut");
        DomainCutMenuItem.addActionListener(new java.awt.event.ActionListener()
            {

                public void actionPerformed(ActionEvent e)
                {
                    OnDomainPopupCut(e);
                }
            });
        CopyMenuItem.setToolTipText("copy into buffer");
        CopyMenuItem.setMnemonic('C');
        CopyMenuItem.setText("Copy");
        CopyMenuItem.addActionListener(new java.awt.event.ActionListener()
            {

                public void actionPerformed(ActionEvent e)
                {
                    OnMemberPopupCopy(e);
                }
            });
        PasteMenuItem.setEnabled(false);
        PasteMenuItem.setToolTipText("paste object");
        PasteMenuItem.setMnemonic('P');
        PasteMenuItem.setText("Paste");
        PasteMenuItem.addActionListener(new java.awt.event.ActionListener()
            {

                public void actionPerformed(ActionEvent e)
                {
                    OnMemberPopupPaste(e);
                }
            });
        DomainLoadMenuItem.setToolTipText("load root domain from file");
        DomainLoadMenuItem.setMnemonic('O');
        DomainLoadMenuItem.setText("Load ...");
        DomainLoadMenuItem.addActionListener(new java.awt.event.ActionListener()
            {

                public void actionPerformed(ActionEvent e)
                {
                    OnDomainLoad(e);
                }
            });
        CloseMenuItem.setToolTipText("close the current frame");
        CloseMenuItem.setMnemonic('C');
        CloseMenuItem.setText("Close");
        CloseMenuItem.addActionListener(new java.awt.event.ActionListener()
            {

                public void actionPerformed(ActionEvent e)
                {
                    OnDomainClose(e);
                }
            });
        jMenuItem3.setToolTipText("create a new browser frame");
        jMenuItem3.setMnemonic('F');
        jMenuItem3.setText("new Frame");
        jMenuItem3.addActionListener(new java.awt.event.ActionListener()
            {

                public void actionPerformed(ActionEvent e)
                {
                    OnViewNewFrame(e);
                }
            });
        PolicyCopyMenuItem.setToolTipText("copy polic into policy buffer");
        PolicyCopyMenuItem.setMnemonic('C');
        PolicyCopyMenuItem.setText("Copy");
        PolicyCopyMenuItem.addActionListener(new java.awt.event.ActionListener()
            {

                public void actionPerformed(ActionEvent e)
                {
                    OnPolicyPopupCopy(e);
                }
            });
        PolicyPasteMenuItem.setEnabled(false);
        PolicyPasteMenuItem.setToolTipText("paste policy from policy buffer");
        PolicyPasteMenuItem.setMnemonic('P');
        PolicyPasteMenuItem.setText("Paste");
        PolicyPasteMenuItem.addActionListener(new java.awt.event.ActionListener()
            {

                public void actionPerformed(ActionEvent e)
                {
                    OnPolicyPopupPaste(e);
                }
            });
        PolicyNewMenu.setToolTipText("create new policy");
        PolicyNewMenu.setText("New");
        PolicyNewPropertyPolicyMenuItem.setToolTipText("(name, value) pairs");
        PolicyNewPropertyPolicyMenuItem.setMnemonic('P');
        PolicyNewPropertyPolicyMenuItem.setText("Property Policy");
        PolicyNewPropertyPolicyMenuItem.addActionListener(new java.awt.event.ActionListener()
            {

                public void actionPerformed(ActionEvent e)
                {
                    OnPolicyPopupNewPropertyPolicy(e);
                }
            });
        PolicyNewMetaPropertyPolicyMenuItem.setToolTipText("(name, value) + meta");
        PolicyNewMetaPropertyPolicyMenuItem.setMnemonic('M');
        PolicyNewMetaPropertyPolicyMenuItem.setText("MetaPropertyPolicy");
        PolicyNewMetaPropertyPolicyMenuItem.addActionListener(new java.awt.event.ActionListener()
            {

                public void actionPerformed(ActionEvent e)
                {
                    OnPolicyPopupNewMetaPropertyPolicy(e);
                }
            });
        DomainCopyMenuItem.setToolTipText("copy domain into domain buffer");
        DomainCopyMenuItem.setMnemonic('C');
        DomainCopyMenuItem.setText("Copy");
        DomainCopyMenuItem.addActionListener(new java.awt.event.ActionListener()
            {

                public void actionPerformed(ActionEvent e)
                {
                    OnDomainPopupCopy(e);
                }
            });
        DomainPasteMenuItem.setEnabled(false);
        DomainPasteMenuItem.setToolTipText("paste domain from domain buffer");
        DomainPasteMenuItem.setMnemonic('P');
        DomainPasteMenuItem.setText("Paste");
        DomainPasteMenuItem.addActionListener(new java.awt.event.ActionListener()
            {

                public void actionPerformed(ActionEvent e)
                {
                    OnDomainPopupPaste(e);
                }
            });
        PolicyNewConflictResolutionPolicyMenuItem.setText("ConflictResolutionPolicy");
        PolicyNewConflictResolutionPolicyMenuItem.addActionListener(new java.awt.event.ActionListener()
            {

                public void actionPerformed(ActionEvent e)
                {
                    OnPolicyPopupNewConflictResolutionPolicy(e);
                }
            });
        DomainMenu.add(DomainNewMenuItem);
        DomainMenu.add(DomainLoadMenuItem);
        DomainMenu.addSeparator();
        DomainMenu.add(CloseMenuItem);
        DomainMenu.add(ExitMenuItem);

        HelpMenu.add(AboutMenuItem);
        menuBar1.add(DomainMenu);
        menuBar1.add(ViewMenu);
        menuBar1.add(HelpMenu);

        ViewMenu.add(jMenuItem3);
        ViewMenu.add(jMenuItem1);
        ViewMenu.add(showToolTipsMenuItem);

        TreePopupMenu.add(UpdateMenuItem);
        TreePopupMenu.add(DomainCutMenuItem);
        TreePopupMenu.add(DomainCopyMenuItem);
        TreePopupMenu.add(DomainPasteMenuItem);
        TreePopupMenu.add(DomainRenameItem);

        contentPane.add(outerSplitPane, BorderLayout.CENTER);
        outerSplitPane.add(TreeScrollPane, JSplitPane.LEFT);
        outerSplitPane.add(innerSplitPane, JSplitPane.RIGHT);
        innerSplitPane.add(MemberScrollPane, JSplitPane.LEFT);
        MemberScrollPane.getViewport().add(memberList, null);
        innerSplitPane.add(PolicyScrollPane, JSplitPane.RIGHT);
        PolicyScrollPane.getViewport().add(policyList, null);
        TreeScrollPane.getViewport().add(jTree1, null);

        PolicyPopupMenu.add(PolicyPropertyMenuItem);
        PolicyPopupMenu.add(PolicyCutMenuItem);
        PolicyPopupMenu.add(PolicyCopyMenuItem);
        PolicyPopupMenu.add(PolicyPasteMenuItem);
        PolicyPopupMenu.add(PolicyNewMenu);

        MemberPopupMenu.add(PropertiesMenuItem);
        MemberPopupMenu.add(CutMenuItem);
        MemberPopupMenu.add(CopyMenuItem);
        MemberPopupMenu.add(PasteMenuItem);
        MemberPopupMenu.add(RenameMenuItem);

        PolicyNewMenu.add(PolicyNewPropertyPolicyMenuItem);
        PolicyNewMenu.add(PolicyNewMetaPropertyPolicyMenuItem);
        PolicyNewMenu.add(PolicyNewConflictResolutionPolicyMenuItem);
        this.setJMenuBar(menuBar1);
    }

    //File | Exit action performed
    public void fileExit_actionPerformed(ActionEvent e) 
    {
        releaseRessources(); 
        // if this is the last frame, releaseRessources will call System.exit()
        // else clean up ourselfes
        try
        {
            getDomainServer().deleteChild( this.getORBDomain() );
        }
        catch (org.omg.CORBA.COMM_FAILURE fail)
        {
            Debug.output(Debug.DOMAIN | Debug.INFORMATION, "BrowserFrame.OnExit: "
                         +" unmounting of gui orb domain not possible, skipping.");
        }
        ((org.jacorb.orb.ORB) getORB()).shutdown(false);
        System.exit(0);
    }

    //Help | About action performed

    //Overridden so we can exit when window is closed

    protected void processWindowEvent(WindowEvent e) {
        super.processWindowEvent(e);
        if (e.getID() == WindowEvent.WINDOW_CLOSING) {
            fileExit_actionPerformed(null);
        }
    }

    void OnViewUpdate(ActionEvent e)
    {
        // org.jacorb.util.Debug.output(2, "View Update");
        DomainTreeNode node= (DomainTreeNode) jTree1.getLastSelectedPathComponent();
        if (node == null)
            node= (DomainTreeNode) treeModel.getRoot(); // if none is selected default to root node
        updateTreeView(node);
    }

    //    /**
    //       * Invoked when the mouse has been clicked on a component.
    //       */
    //      public void mouseClicked(MouseEvent e){}

    //      /**
    //       * Invoked when a mouse button has been pressed on a component.
    //       */
    //      public void mousePressed(MouseEvent e)
    //      { mayBeShowPopup(e); }

    //      /**
    //       * Invoked when a mouse button has been released on a component.
    //       */
    //      public void mouseReleased(MouseEvent e)
    //      { mayBeShowPopup(e); }

    //      /**
    //       * Invoked when the mouse enters a component.
    //       */
    //      public void mouseEntered(MouseEvent e){}

    //      /**
    //       * Invoked when the mouse exits a component.
    //       */
    //      public void mouseExited(MouseEvent e){}

    //      private void mayBeShowPopup(MouseEvent e)
    //      {
    //        // org.jacorb.util.Debug.output(2, e.getComponent().toString());
    //        if (e.isPopupTrigger() )
    //              TreePopupMenu.show(e.getComponent(), e.getX(), e.getY() );
    //      }

    void OnPolicyPopupProperties(ActionEvent e)
    {
        org.omg.CORBA.Policy pol;
        Debug.myAssert(1, policyListRoot != null,
                     "root of policy list is null");

        // extract policy from selection, take first of all if more than one is selected
        int i = policyList.getMinSelectionRow();
        if (i < 0) 
            return; // nothing selected

        PolicyListLeafNode node = 
            (PolicyListLeafNode) policyListRoot.getChildAt(i);
        pol = node.getPolicy();

        // String name= node.toString();
        if (pol == null) 
            return;

        try
        {
            // create dialog box with IOR tab
            PropertyDialog box = 
                new PropertyDialog(pol, this, "Policy Properties", true);

            // add description tab only for ManagementPolicies
            ManagementPolicy manage = ManagementPolicyHelper.narrow(pol);
            if( manage != null)
            { 
                // display description for a org.jacorb.orb.domain.ManagePolicy object
                box.setTitle( manage.short_description() + " Properties");
                box.add("Description", new ScrollableTextPane(manage.long_description()));
            }

            if ( theSharedData.getPolicyEditors() == null)
            { 
                // only show with IOR and Reflection tab, skip rest
                box.show();
                return;
            }

            // add user defined editor according to policy type
            int type = pol.policy_type();
            String key = Integer.toString(type);
            String EditorClassName = 
                theSharedData.getPolicyEditors().getValueOfProperty( key );

            Debug.output(Debug.DOMAIN | 3, "class name for policy type " + type
                         + " is "+ EditorClassName);

            if( EditorClassName.length() == 0 )
            { 
                // only show with IOR and Reflection tab, skip rest
                Debug.output(Debug.DOMAIN | 2,
                             "no editor for policy type "+ type + 
                             " available");

                box.show();
                return;
            }

            // ok, we have a valid class name for the policy type, load the class dynamically
            try
            {
                PolicyEditor editor = 
                    (PolicyEditor)Class.forName(EditorClassName).newInstance();
                editor.setORB( orb );
                editor.setEditorPolicy( pol );
                box.add( editor.getTitle(), 
                         editor.getGraphicalComponent() );
            }
            catch (Exception ex)
            {
                ex.printStackTrace();
                Debug.output(Debug.DOMAIN | 1, ex.toString() );
            }

            box.pack();
            box.show();
        } // try pol, domain
        catch (org.omg.CORBA.COMM_FAILURE fail)
        {
            JOptionPane.showMessageDialog(mainFrame, "The policy domain does not reply. Operation "
                                          + "canceled." ,"Broken Connection" , JOptionPane.INFORMATION_MESSAGE);
        }

    } // OnPolicyPopupProperties

    void OnViewToolTips(ActionEvent e)
    {
        JCheckBoxMenuItem toolTips= (JCheckBoxMenuItem) e.getSource();
        if ( toolTips.isSelected() )
        {
	    jTree1.setCellRenderer(domainTreeCellRenderer);
            policyList.setCellRenderer(policyListCellRenderer);
        }
        else
        {
	    jTree1.setCellRenderer(defaultTreeCellRenderer);
            policyList.setCellRenderer(defaultTreeCellRenderer);

        }
    }

    void OnMemberPopupProperties(ActionEvent e)
    {

        org.omg.CORBA.Object obj= null;
        Debug.myAssert(1, memberListRoot != null, "root of member list is null");

        int i= memberList.getMinSelectionRow();

        if (i < 0) return; // nothing selected
        Debug.output(Debug.DOMAIN | 4, 
                     "selected index is " + i);

        MemberListLeafNode node =
            (MemberListLeafNode) memberListRoot.getChildAt(i);
        obj= node.getObject();

        String name= node.toString();
        // Debug.myAssert(1, name.equals(memberListRoot.getDomain().getNameOf(obj)),
        // 		 "name of object in list and name of object in domain are not the same");
        if (obj == null) 
            return;

        PropertyDialog box= 
            new PropertyDialog(obj, this, "Member Properties", true);
        box.show();
    }

    void OnMemberPopupRename(ActionEvent e)
    {
        // activate member list tree cell editor
        ((DefaultTreeCellEditor) memberList.getCellEditor()).actionPerformed(e);
    }

    void OnTreeNodesChanged(TreeModelEvent e)
    {

        DomainTreeNode node= (DomainTreeNode) e.getTreePath().getLastPathComponent();
        if (node == null) return;
        // org.jacorb.util.Debug.output(2, "TreeNodeChanged: " + e);

        // get modified node
        try
        {
            int i= e.getChildIndices()[0];
            node= (DomainTreeNode) node.getChildAt(i);
        }
        catch (NullPointerException nullE)
        {} // no childrens, so the modified node is the node got before

        // editing was done by DefaultTreeCellEditor, name of domain
        // is now in the user object of the node, write it to domain
        // node.getDomain().name( (String) node.getUserObject() );
    }

    void OnTreePopupRename(ActionEvent e)
    {
        // activate tree cell editor
        ((DefaultTreeCellEditor) jTree1.getCellEditor()).actionPerformed(e);
    }

    void OnDomainNew(ActionEvent e)
    {
        DomainTreeNode node= (DomainTreeNode) jTree1.getLastSelectedPathComponent();
        if (node == null) return;

        Domain domain= node.getDomain();
        Domain newDomain= domain.createEmptyDomain();
        newDomain.name("new Domain");
        try
        {
            domain.insertChild(newDomain);
        }
        catch (org.jacorb.orb.domain.GraphNodePackage.ClosesCycle cc)
        { Debug.output(Debug.DOMAIN |1, "could not insert new domain because of"
                       +" closing cycle");
        }
        catch (org.jacorb.orb.domain.NameAlreadyDefined already)
        { 
            // should not happen
            JOptionPane.showMessageDialog(mainFrame, 
                                          "Please choose another name.",
                                          "Name Already in Use" , 
                                          JOptionPane.ERROR_MESSAGE);
        }
        this.updateTreeView(node);
    }

    void OnDomainPopupRemove(ActionEvent e)
    {

    } // OnDomainPopupRemove


    void OnPolicyPopupCut(ActionEvent e)
    {
        org.omg.CORBA.Policy pol;
        Debug.myAssert(1, policyListRoot != null, "root of policy list is null");

        // extract policy from selection, take first of all if more than one is selected
        int i= policyList.getMinSelectionRow();
        if (i < 0) return; // nothing selected

        PolicyListLeafNode node= (PolicyListLeafNode) policyListRoot.getChildAt(i);
        pol= node.getPolicy();

        try
        {
            Domain domain= policyListRoot.getDomain();
            domain.deletePolicyOfType(pol.policy_type());

            // copy into buffer and enable pasting
            theSharedData.setPolicyBuffer(pol);
            enablePolicyPasteMenuItem();

            // update view,
            policyListRoot= new PolicyListRootNode(domain);
            DefaultTreeModel policyModel= new DefaultTreeModel(policyListRoot);
            policyList.setModel(policyModel);
        }
        catch (org.omg.CORBA.COMM_FAILURE fail)
        {
            JOptionPane.showMessageDialog(this, "The policy / domain does not reply. "
                                          +"Operation canceled.", "Broken Connection" , JOptionPane.INFORMATION_MESSAGE);
        }
    } // OnPolicyPopupCut


    void OnMemberPopupCut(ActionEvent e)
    {
        org.omg.CORBA.Object obj= null;
        Debug.myAssert(1, memberListRoot != null, "root of member list is null");

        int i = memberList.getMinSelectionRow();
        if( i < 0 ) 
            return; // nothing selected

        Debug.output( Debug.DOMAIN | 4, "selected index is " + i);

        MemberListLeafNode node = (MemberListLeafNode)memberListRoot.getChildAt(i);
        obj = node.getObject();

        try
        {
            Domain domain = memberListRoot.getDomain();
            domain.deleteMember(obj);

            // if the above call fails, the buffer is not written
            // copy into buffer
            theSharedData.setMemberBuffer(obj, node.toString() );

            PasteMenuItem.setEnabled(true);

            // update view
            memberListRoot= new MemberListRootNode(domain);
            DefaultTreeModel model= new DefaultTreeModel(memberListRoot);
            memberList.setModel(model);
        }
        catch (org.omg.CORBA.COMM_FAILURE fail)
        {
            JOptionPane.showMessageDialog(mainFrame, 
                                          "The domain containing the member does not reply."
                                          +" Operation canceled.",
                                          "Broken Connection" ,
                                          JOptionPane.INFORMATION_MESSAGE);
        }
    } // OnMemberPopupCut

    void OnMemberPopupCopy(ActionEvent e)
    {
        org.omg.CORBA.Object obj= null;
        Debug.myAssert(1, memberListRoot != null, "root of member list is null");

        int i= memberList.getMinSelectionRow();
        if (i < 0) return; // nothing selected
        Debug.output(Debug.DOMAIN | 4, "selected index is " + i);

        MemberListLeafNode node= (MemberListLeafNode) memberListRoot.getChildAt(i);
        obj= node.getObject();
        // copy into buffer
        theSharedData.setMemberBuffer(obj, node.toString() );

        PasteMenuItem.setEnabled(true);
    } // OnMemberPopupCopy


    void OnMemberPopupPaste(ActionEvent e)
    {
        Debug.myAssert(1, memberListRoot != null, 
                     "root of member list is null");
        Debug.myAssert(1, ! theSharedData.MemberBufferIsEmpty(), 
                     "member buffer is empty");

        Domain domain= memberListRoot.getDomain();
        StringBuffer memberNameBuffer= new StringBuffer();
        org.omg.CORBA.Object member;
        // get from buffer
        member= theSharedData.getMemberBuffer(memberNameBuffer);
        String memberName= new String(memberNameBuffer);
        try
        {
            domain.insertMemberWithName( memberName, member);
        }
        catch (NameAlreadyDefined already)
        {
            JOptionPane.showMessageDialog(this, 
                                          "The member name " + memberName
                                          +" is already in use in the target domain. \nPlease rename member "
                                          + "before copying / cuting.",
                                          "Member Name Already in Use" , 
                                          JOptionPane.ERROR_MESSAGE);
            return;
        }
        catch (org.omg.CORBA.COMM_FAILURE fail)
        {
            JOptionPane.showMessageDialog(mainFrame, 
                                          "The target domain does not reply."
                                          +" Operation canceled.",
                                          "Broken Connection" , 
                                          JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        // update view
        memberListRoot= new MemberListRootNode(domain);
        DefaultTreeModel model= new DefaultTreeModel(memberListRoot);
        memberList.setModel(model);
    } // OnMemberPopupPaste

    void OnDomainLoad(ActionEvent e)
    {
        // show file chooser
        JFileChooser dialog= new JFileChooser();
        int answer= dialog.showOpenDialog(this);
        if (answer == JFileChooser.APPROVE_OPTION)
        {
            Domain domain= readDomainFromFile( dialog.getSelectedFile(), getORB() );
            if (domain != null)
            {
                // provide the tree view with a new date model which represents new structure
                // *reset* theRootDomain and treeModel
                theRootDomain              =                       domain;
                DomainTreeNode rootNode = new DomainTreeNode  ( domain );
                treeModel               = new DefaultTreeModel(rootNode);
                // treeModel.reload();
                jTree1.setModel(treeModel);
                updateListModels(theRootDomain);
            }
            else Debug.output(2, "OnDomainLoad: domain is null");
        }
    } // OnDomainLoad

    /**
     *  reads  a IOR  from the  file "filename" and  converts it  to a
     *  domain reference with the help of the supplied orb.
     *  @return a domain reference on success, null if an exception occured 
     */

    private Domain readDomainFromFile(File file, org.omg.CORBA.ORB orb)
    {
        String iorString= null;

        String line= null;
        try
        {
            BufferedReader br = new BufferedReader (new FileReader(file), 2048 );
            line = br.readLine();
            //		System.out.print ( line );
            if ( line != null )
            {
                iorString = line;
                while ( line != null )
                {
                    line = br.readLine();
                    if ( line != null ) iorString = iorString + line;
                    // System.out.print ( line );
                }
            }
        }
        catch ( IOException ioe )
        {
            JOptionPane.showMessageDialog(this , "Reading of file was not possible."
                                          +"\n Reason: " + ioe.toString(),
                                          "Error while reading file" , JOptionPane.INFORMATION_MESSAGE);
            return null;
        }

        try
        {
            return DomainHelper.narrow(orb.string_to_object(iorString));
        }
        catch (java.lang.RuntimeException run)
        {
            JOptionPane.showMessageDialog(this , "Parsing of file was not possible."
                                          +"\n Reason: " + run.toString(),
                                          "Error while parsing file" , JOptionPane.INFORMATION_MESSAGE);
            return null;
        }


    }

    void OnViewNewFrame(ActionEvent e)
    {
        new Browser(theSharedData, theRootDomain);
        // the SharedData object is a singleton and is used by all frames
    }

    void OnDomainClose(ActionEvent e)
    {
        this.releaseRessources();
        // if this was the last frame, deregisterFrame will exit the program
        // if not, close and destroy frame
        this.hide();
        this.dispose();
    }

    void OnHelpAbout(ActionEvent e)
    {
        JDialog dialog= new AboutDialog(this, "about the domain browser", false);
        dialog.show();
    } // OnHelpAbout


    /** enables the paste member menu item. Called from the shared data when
     *  the member buffer gets filled by an other browser frame instance. */
    public void enableMemberPasteMenuItem()
    {
        PasteMenuItem.setEnabled(true);

    } // enableMemberPasteMenuItem

    /** enables the paste domain menu item. Called from the shared data when
     *  the domain buffer gets filled by an other browser frame instance. */
    public void enableDomainPasteMenuItem()
    {
        DomainPasteMenuItem.setEnabled(true);
    } // enableDomainPasteMenuItem

    /** enables the paste policy menu item. Called from the shared data when
     *  the policy buffer gets filled by an other browser frame instance. */
    public void enablePolicyPasteMenuItem()
    {
        PolicyPasteMenuItem.setEnabled(true);
    }

    void OnDomainPopupCopy(ActionEvent e)
    {
        DomainTreeNode node= (DomainTreeNode) jTree1.getLastSelectedPathComponent();
        if (node == null) return;

        Domain domain= node.getDomain();
        theSharedData.setDomainBuffer(domain);
    } // OnDomainPopupCopy

    /** cut domain */
    void OnDomainPopupCut(ActionEvent e)
    {
        DomainTreeNode node= (DomainTreeNode) jTree1.getLastSelectedPathComponent();
        if (node == null) return;

        Domain domain= node.getDomain();
        if (domain.getChildCount() != 0)
        {
            int answer= JOptionPane.showConfirmDialog(this, "The domain to remove has childs."
                                                      +" Please confirm  removing of domain by pressing yes. ",
                                                      "Confirm Remove" , JOptionPane.YES_NO_OPTION);
            if ( answer != JOptionPane.YES_OPTION) return;
        }
        TreeNode parentNode= node.getParent();
        if (parentNode == null) return;
        DomainTreeNode domParentNode= (DomainTreeNode) parentNode;
        Domain parentDomain= domParentNode.getDomain();

        parentDomain.deleteChild(domain);

        theSharedData.setDomainBuffer(domain);
        this.updateTreeView(domParentNode);
    }

    void OnDomainPopupPaste(ActionEvent e)
    {
        DomainTreeNode node= (DomainTreeNode) jTree1.getLastSelectedPathComponent();
        if (node == null)
        {
            // if none is selected default to root node
            node= (DomainTreeNode) treeModel.getRoot();
        }

        Domain domain= node.getDomain();
        try
        {
            domain.insertChild(theSharedData.getDomainBuffer());
        }
        catch (org.jacorb.orb.domain.GraphNodePackage.ClosesCycle cc)
        {
            JOptionPane.showMessageDialog(this, "This operation would close a cycle in"
                                          +" in the domain graph and is therefore not permitted.",
                                          "Paste Impossible" , JOptionPane.ERROR_MESSAGE);
            return;
        }
        catch (org.jacorb.orb.domain.NameAlreadyDefined already)
        {
            JOptionPane.showMessageDialog(this, "The name " + already.name + "is already"
                                          +" used in the target domain by another child. Rename that child first.",
                                          "Name already in use" , JOptionPane.ERROR_MESSAGE);
            return;
        }
        this.updateTreeView(node);
    }

    /** copy policy into policy buffer. */
    void OnPolicyPopupCopy(ActionEvent e)
    {
        org.omg.CORBA.Policy pol;
        Debug.myAssert(1, policyListRoot != null, "root of policy list is null");

        // extract policy from selection, take first of all if more than one is selected
        int i= policyList.getMinSelectionRow();
        if (i < 0) return; // nothing selected

        PolicyListLeafNode node= (PolicyListLeafNode) policyListRoot.getChildAt(i);
        pol= node.getPolicy();

        theSharedData.setPolicyBuffer(pol);

    } // OnPolicyPopupCopy

    void OnPolicyPopupPaste(ActionEvent e)
    {
        Debug.myAssert(1, policyListRoot != null, "root of policy list is null");
        Domain domain= policyListRoot.getDomain();
        try
        {
            domain.set_domain_policy( theSharedData.getPolicyBuffer() );
        }
        catch (PolicyTypeAlreadyDefined already)
        {
            int answer= JOptionPane.showConfirmDialog(this, "The policy type " + already.type
                                                      +" is used by another policy of this domain. Override this policy ?",
                                                      "Policy Type Already Defined" , JOptionPane.YES_NO_OPTION);
            if (answer == JOptionPane.YES_OPTION)
                domain.overwrite_domain_policy(theSharedData.getPolicyBuffer());
            else return;
        }

        // update policy (tree) view
        DomainTreeNode node= (DomainTreeNode) jTree1.getLastSelectedPathComponent();
        if (node == null) return;

        policyListRoot= new PolicyListRootNode( node.getDomain() );
        DefaultTreeModel policyModel= new DefaultTreeModel(policyListRoot);
        policyList.setModel(policyModel);
    }

    void OnPolicyPopupNewPropertyPolicy(ActionEvent e)
    {
        Debug.myAssert(1, policyListRoot != null, "root of policy list is null");
        Domain domain= policyListRoot.getDomain();
        PropertyPolicy pol= null;
        String name= "Property", typeString="?";
        int type;
        name= JOptionPane.showInputDialog
            (this, "Step 1 of 2: Enter name for property Policy");
        if (name == null) return;

        typeString= JOptionPane.showInputDialog
            (this, "Step 2 of 2: Enter type for property Policy");
        if (typeString == null) return;

        try
        {
            type= Integer.parseInt(typeString);
        }
        catch (java.lang.NumberFormatException ex)
        {
            JOptionPane.showMessageDialog(this,"Could not convert input to number.",
                                          ex.toString(), JOptionPane.ERROR_MESSAGE);
            return;
        }

        // create policy after successful input reading
        pol= domain.createPropertyPolicy();
        pol.name(name);
        pol.setPolicyType(type);
        try
        {
            domain.set_domain_policy(pol);
        }
        catch (PolicyTypeAlreadyDefined already)
        {
            JOptionPane.showMessageDialog(this, "The policy type." + type
                                          +" is used by another policy of this domain. Choose another type.",
                                          "Policy Type Already in Use" , JOptionPane.ERROR_MESSAGE);
            return;
        }
        // update view
        policyListRoot= new PolicyListRootNode(domain);
        DefaultTreeModel policyModel= new DefaultTreeModel(policyListRoot);
        policyList.setModel(policyModel);
    } // OnPolicyPopupNewPropertyPolicy

    void OnPolicyPopupNewMetaPropertyPolicy(ActionEvent e)
    {
        Debug.myAssert(1, policyListRoot != null, "root of policy list is null");
        Domain domain= policyListRoot.getDomain();
        MetaPropertyPolicy pol= null;
        String name= "Property", typeString="?", types="?";
        int type;
        name= JOptionPane.showInputDialog
            (this, "Step 1 of 3: Enter name for property Policy");
        if (name == null) return;

        typeString= JOptionPane.showInputDialog
            (this, "Step 2 of 3: Enter type for property Policy");
        if (typeString == null) return;

        try
        {
            type= Integer.parseInt(typeString);
        }
        catch (java.lang.NumberFormatException ex)
        {
            JOptionPane.showMessageDialog(this,"Could not convert input to number.",
                                          ex.toString(), JOptionPane.ERROR_MESSAGE);
            return;
        }
        types= JOptionPane.showInputDialog
            (this, "Step 3 of 3: Enter policy types for which this policy shall be meta. "
             +"\n Please separate the types by a comma (\",\").");
        if (name == null) return;


        pol= domain.createMetaPropertyPolicy();
        pol.name(name);
        pol.setPolicyType(type);

        // extract policy types
        StringTokenizer tokenizer= new StringTokenizer(types, ",");
        int typeList[]= new int[ tokenizer.countTokens() ];

        int i= 0;
        while ( tokenizer.hasMoreTokens() )
        {
            try
            {
                typeList[i]= Integer.parseInt( tokenizer.nextToken() );
            }
            catch (Exception ex) { continue; }
            i++;
        }

        // for (int j= 0; j < i; j++) Debug.output(Debug.DOMAIN | 2, "reconized type " + typeList[j]);

        // set policy types
        if (i == tokenizer.countTokens() )
        {
            // ok, no errors
            pol.setManagedTypes(typeList);
        }
        else
        { // some or all tokens could not be parsed to ints, cut list to correct size
            int newList[]= new int[i];
            for (int j= 0; j < i; j++) newList[j]= typeList[j];
            pol.setManagedTypes(newList);
        }

        // create policy after successful input reading

        try
        {
            domain.set_domain_policy(pol);
        }
        catch (PolicyTypeAlreadyDefined already)
        {
            JOptionPane.showMessageDialog(this, "The policy type." + type
                                          +" is used by another policy of this domain. Choose another type.",
                                          "Policy Type Already in Use" , JOptionPane.ERROR_MESSAGE);
            return;
        }
        // update view, TODO ?
        policyListRoot= new PolicyListRootNode(domain);
        DefaultTreeModel policyModel= new DefaultTreeModel(policyListRoot);
        policyList.setModel(policyModel);
    }

    void OnPolicyPopupNewConflictResolutionPolicy(ActionEvent e)
    {
        Debug.myAssert(1, policyListRoot != null, "root of policy list is null");
        Domain domain= policyListRoot.getDomain();
        if ( domain.hasPolicyOfType(CONFLICT_RESOLUTION_POLICY_ID.value) )
        {
            JOptionPane.showMessageDialog(this, 
                                          "There is already a conflict resolution policy"
                                          +" defined in this domain. Delete it first.",
                                          "Conflict Resolution Policy Already Defined" , JOptionPane.ERROR_MESSAGE);
            return;
        }
        Object [] strategies= {"FIRST", "PARENT_RULES", "CHILD_RULES"};
        Object result= JOptionPane.showInputDialog(this, "Select a strategy",
                                                   "Create Conflict Resolution Policy", JOptionPane.QUESTION_MESSAGE,
                                                   null, strategies, "PARENT_RULES");
        if (result == null)
            return; // user canceled

        org.omg.CORBA.Policy pol= null;
        if (result.equals("FIRST"))
        {
            pol= domain.createConflictResolutionPolicy(ConflictResolutionPolicy.FIRST);
        }
        else if (result.equals("PARENT_RULES"))
        {
            pol= domain.createConflictResolutionPolicy(ConflictResolutionPolicy.PARENT_RULES);
        }
        else if (result.equals("CHILD_RULES"))
        {
            pol= domain.createConflictResolutionPolicy(ConflictResolutionPolicy.CHILD_RULES);
        }
        else Debug.output(Debug.DOMAIN | Debug.IMPORTANT, "OnPolicyPopupNewConflictResolutionPolicy:"
                          +" no valid selection made, skipping creation of conflict resolution policy.");

        try  
        { 
            domain.set_domain_policy(pol); 
        }
        catch (PolicyTypeAlreadyDefined already) 
        {};
     
        // update view
        policyListRoot= new PolicyListRootNode(domain);
        DefaultTreeModel policyModel= new DefaultTreeModel(policyListRoot);
        policyList.setModel(policyModel);

    } // OnPolicyPopupNewConflictResolutionPolicy



} // BrowserFrame





