/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1999-2003 Gerald Brose
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
 *
 */
package org.jacorb.imr.util;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.tree.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.table.*;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import org.jacorb.imr.*;

/**
 * The GUI'ed version of the ImRManager.
 *
 * @author Nicolas Noffke
 * 
 * $Log$
 * Revision 1.5  2002/07/01 07:54:16  nicolas
 * updated or inserted Copyright notice
 *
 * Revision 1.4  2002/03/19 09:25:10  nicolas
 * updated copyright to 2002
 *
 * Revision 1.3  2002/03/19 11:08:00  brose
 * *** empty log message ***
 *
 * Revision 1.2  2002/03/17 18:44:00  brose
 * *** empty log message ***
 *
 * Revision 1.7  1999/11/25 16:05:47  brose
 * cosmetics
 *
 * Revision 1.6  1999/11/21 20:15:51  noffke
 * GUI data is now updated periodically by a thread
 *
 * Revision 1.5  1999/11/14 17:15:39  noffke
 * Cosmetics and commenting
 *
 *
 */

public class ImRManagerGUI extends JFrame{
    private JMenuItem m_exit_mi;
    private JMenuItem m_about_mi;
    private JMenuItem m_refresh_all_mi;
    private JMenuItem m_connect_mi;
    private JMenuItem m_shutdown_mi;    
    private JMenuItem m_forcedown_mi;
    private JMenuItem m_save_mi;
    private JMenuItem m_add_mi;
    private JMenuItem m_remove_mi;
    private JMenuItem m_hold_mi;
    private JMenuItem m_release_mi;
    private JMenuItem m_setdown_mi;
    private JMenuItem m_refresh_mi;
    private JMenuItem m_auto_refresh_mi;

    private EventHandler m_handler;
    private ImRModel m_model;

    private DefaultMutableTreeNode m_top_node;
    private JTree m_tree;

    private JPanel m_table_panel;
    private JScrollPane m_server_view;
    private JScrollPane m_poa_view;
    private JScrollPane m_tree_view;

    private JTable m_server_table;
    private JTable m_poa_table;

    private JPopupMenu m_context_menu;
    private JMenuItem m_add_cmi;
    private JMenuItem m_remove_cmi;
    private JMenuItem m_hold_cmi;
    private JMenuItem m_release_cmi;
    private JMenuItem m_setdown_cmi;
    private JMenuItem m_refresh_cmi;
    

    /**
     * The constructor. Instanciates all components and brings up the window.
     */
    public ImRManagerGUI() {
	super("ImR Manager");

	m_handler = new EventHandler();
	m_model = new ImRModel();
	
	try {
	    UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
	} catch (Exception e) { }
	
	GridBagConstraints _constraints = new GridBagConstraints();

	//Build menu bar
	JMenuBar _menu_bar = new JMenuBar();
	setJMenuBar(_menu_bar);

	JMenu _manager_menu = new JMenu("Manager");
	_menu_bar.add(_manager_menu);

	JMenu _imr_menu = new JMenu("ImR");
	_menu_bar.add(_imr_menu);
	
	JMenu _server_menu = new JMenu("Server");
	_menu_bar.add(_server_menu);

	//build manager menu entries
	m_about_mi = new JMenuItem("About...");
	m_about_mi.addActionListener(m_handler);
	_manager_menu.add(m_about_mi);

	m_auto_refresh_mi = new JMenuItem("Automatic Refresh...");
	m_auto_refresh_mi.addActionListener(m_handler);
	_manager_menu.add(m_auto_refresh_mi);

	m_exit_mi = new JMenuItem("Exit");
	m_exit_mi.addActionListener(m_handler);
	_manager_menu.add(m_exit_mi);

	//build ImR menu entries
	m_shutdown_mi = new JMenuItem("Shutdown");
	m_shutdown_mi.addActionListener(m_handler);
	_imr_menu.add(m_shutdown_mi);

	m_forcedown_mi = new JMenuItem("Force Down");
	m_forcedown_mi.addActionListener(m_handler);
	_imr_menu.add(m_forcedown_mi);

	m_save_mi = new JMenuItem("Save Server Table");
	m_save_mi.addActionListener(m_handler);
	_imr_menu.add(m_save_mi);

	_imr_menu.addSeparator();

	m_connect_mi = new JMenuItem("Connect...");
	m_connect_mi.addActionListener(m_handler);
	_imr_menu.add(m_connect_mi);

	m_refresh_all_mi = new JMenuItem("Refresh all");
	m_refresh_all_mi.addActionListener(m_handler);
	_imr_menu.add(m_refresh_all_mi);

	//build server menu entries
	m_add_mi = new JMenuItem("Add...");
	m_add_mi.addActionListener(m_handler);
	_server_menu.add(m_add_mi);

	m_remove_mi = new JMenuItem("Remove");
	m_remove_mi.addActionListener(m_handler);
	_server_menu.add(m_remove_mi);

	m_hold_mi = new JMenuItem("Hold");
	m_hold_mi.addActionListener(m_handler);
	_server_menu.add(m_hold_mi);

	m_release_mi = new JMenuItem("Release");
	m_release_mi.addActionListener(m_handler);
	_server_menu.add(m_release_mi);

	m_setdown_mi = new JMenuItem("Set Down");
	m_setdown_mi.addActionListener(m_handler);
	_server_menu.add(m_setdown_mi);

	_server_menu.addSeparator();

	m_refresh_mi = new JMenuItem("Refresh");
	m_refresh_mi.addActionListener(m_handler);
	_server_menu.add(m_refresh_mi);

	//build popup menu 
	m_context_menu = new JPopupMenu("Server manipulation");
	m_context_menu.setVisible(true);

	m_add_cmi = new JMenuItem("Add...");
	m_add_cmi.addActionListener(m_handler);
	m_context_menu.add(m_add_cmi);

	m_remove_cmi = new JMenuItem("Remove");
	m_remove_cmi.addActionListener(m_handler);
	m_context_menu.add(m_remove_cmi);

	m_context_menu.addSeparator();

	m_hold_cmi = new JMenuItem("Hold");
	m_hold_cmi.addActionListener(m_handler);
	m_context_menu.add(m_hold_cmi);

	m_release_cmi = new JMenuItem("Release");
	m_release_cmi.addActionListener(m_handler);
	m_context_menu.add(m_release_cmi);

	m_setdown_cmi = new JMenuItem("Set Down");
	m_setdown_cmi.addActionListener(m_handler);
	m_context_menu.add(m_setdown_cmi);

	m_context_menu.addSeparator();

	m_refresh_cmi = new JMenuItem("Refresh");
	m_refresh_cmi.addActionListener(m_handler);
	m_context_menu.add(m_refresh_cmi);
       

	JPanel _top_panel = new JPanel();
	GridBagLayout _top_gbl = new GridBagLayout();
	_top_panel.setLayout(_top_gbl);

	m_tree = m_model.getTree();
	m_tree.setEditable(false);
	m_tree.setCellRenderer(new ImRTreeCellRenderer());
	m_tree.addTreeSelectionListener(m_handler);
	m_tree.addMouseListener(m_handler);
        
	ToolTipManager.sharedInstance().registerComponent(m_tree);
	m_tree_view = new JScrollPane(m_tree);
	m_tree_view.addMouseListener(m_handler);
	
	m_table_panel = new JPanel();

	GridBagLayout _table_gbl = new GridBagLayout();
	m_table_panel.setLayout(_table_gbl);

	TableModel _server_model = m_model.getServerTableModel();
	m_server_table = new JTable(_server_model);
	m_server_table.addMouseListener(m_handler);

	ListSelectionModel _selection_model = m_server_table.getSelectionModel();
	_selection_model.addListSelectionListener(m_handler);

	TableColumn _host_col = m_server_table.getColumnModel().getColumn(1);
	_host_col.setCellEditor(new ImRTableCellEditor(m_model.getHostSelector()));

	m_server_view = new JScrollPane(m_server_table);
	m_server_view.setVisible(true);
	buildConstraints(_constraints, 0, 0, 1, 1, 1, 1);
	_constraints.fill = GridBagConstraints.BOTH;	
	_table_gbl.setConstraints(m_server_view, _constraints);
	m_table_panel.add(m_server_view);


	TableModel _poa_model = m_model.getPOATableModel();
	m_poa_table = new JTable(_poa_model);
	m_poa_view = new JScrollPane(m_poa_table);
	m_poa_view.setVisible(false);
	buildConstraints(_constraints, 0, 0, 1, 1, 1, 1);
	_constraints.fill = GridBagConstraints.BOTH;	
	_table_gbl.setConstraints(m_poa_view, _constraints);
	m_table_panel.add(m_poa_view);


	JSplitPane _split_pane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
						m_tree_view, m_table_panel);
	_split_pane.setOneTouchExpandable(true);
	_split_pane.setDividerLocation(150);

	buildConstraints(_constraints, 0, 0, 1, 1, 1, 1);
	_constraints.fill = GridBagConstraints.BOTH;
	_top_gbl.setConstraints(_split_pane, _constraints);	
	_top_panel.add(_split_pane);

	getContentPane().add(_top_panel);
	pack();
	setVisible(true);
    }

    /**
     * Shows the POA Table in right part of SplitPane.
     */
    private void showPOATable(){
	if (! m_poa_view.isVisible()){
	    m_server_view.setVisible(false);
	    m_poa_view.setVisible(true);
	    pack();
	    m_table_panel.repaint();
	}
    }

    /**
     * Shows the Server Table in right part of SplitPane.
     */
    private void showServerTable(){
	if (! m_server_view.isVisible()){
	    m_poa_view.setVisible(false);
	    m_server_view.setVisible(true);
	    pack();
	    m_table_panel.repaint();
	}
    }

    /**
     * Convenience method for setting up the GridBagConstraints.
     *
     * @param gbc a GridBagConstraint object.
     * @param gx x value.
     * @param gy y value.
     * @param gw width.
     * @param gh height.
     * @param wx weight x.
     * @param wy weight y.
     */
    private void buildConstraints(GridBagConstraints gbc, int gx, int gy, 
				  int gw, int gh, int wx, int wy){
	gbc.gridx = gx;
	gbc.gridy = gy;
	gbc.gridwidth = gw;
	gbc.gridheight = gh;
	gbc.weightx = wx;
	gbc.weighty = wy;
    }
    
    /**
     * Main method. Brings the GUI up.
     */
    public static void main(String[] args) {
	new ImRManagerGUI();
    }
    
    /**
     * This class handles all occuring events.
     */
    private class EventHandler 
	implements MouseListener, ListSelectionListener, TreeSelectionListener, ActionListener{
	
	// implementation of java.awt.event.ActionListener interface
	/**
	 * This method responds to menu selections.
	 *
	 * @param event
	 */
	public void actionPerformed(ActionEvent event) {
	    Object _source =  event.getSource();
	    
	    if (_source instanceof JMenuItem){
		if ((JMenuItem) _source == m_exit_mi)
		    System.exit(0);

		else if ((JMenuItem) _source == m_about_mi)
		    JOptionPane.showMessageDialog(new JFrame(), "FUB - Institute for Computer Sciences\n\n" +
						  "The man with the plan: Gerald Brose\n" +
						  "The imp to get along with the plans: Nicolas Noffke\n");

		
		else if ((JMenuItem) _source == m_auto_refresh_mi)
		    new RefreshWindow(m_model);

		else if ((JMenuItem) _source == m_refresh_all_mi){
		    m_model.fetchServers();
		    showServerTable();
		}

		else if ((JMenuItem) _source == m_connect_mi)
		    new ConnectWindow(m_model);

		else if ((JMenuItem) _source == m_shutdown_mi)
		    m_model.imrShutdown(true);

		else if ((JMenuItem) _source == m_forcedown_mi)
		    m_model.imrShutdown(false);

		else if ((JMenuItem) _source == m_save_mi)
		    m_model.saveTable();

		else if ((JMenuItem) _source == m_add_mi ||
			 (JMenuItem) _source == m_add_cmi)
		    new AddServerWindow(m_model);

		else if ((JMenuItem) _source == m_remove_mi ||
			 (JMenuItem) _source == m_remove_cmi){
		    int _select = m_server_table.getSelectedRow();
		    if (_select > -1)
			m_model.removeServer(_select);
		    else{
			TreePath _selected_path = m_tree.getSelectionPath();
			if (_selected_path != null){
			    DefaultMutableTreeNode _node = (DefaultMutableTreeNode) 
				_selected_path.getLastPathComponent();
			    Object _imr_node = _node.getUserObject();

			    if (_imr_node instanceof ServerInfo)
				m_model.removeServer(((ServerInfo) _imr_node).name);
			    else
				System.out.println("Please select a server node!");
			}
			else
			    System.out.println("Please select a server to remove!");
		    }			
		}
		else if ((JMenuItem) _source == m_hold_mi ||
			 (JMenuItem) _source == m_hold_cmi){		    
		    int _select = m_server_table.getSelectedRow();
		    if (_select > -1)
			m_model.holdServer(_select);
		    else{
			TreePath _selected_path = m_tree.getSelectionPath();
			if (_selected_path != null){
			    DefaultMutableTreeNode _node = (DefaultMutableTreeNode) 
				_selected_path.getLastPathComponent();
			    Object _imr_node = _node.getUserObject();
			    
			    if (_imr_node instanceof ServerInfo)
				m_model.holdServer(((ServerInfo) _imr_node).name);
			    else
				System.out.println("Please select a server node!");
			}
			else
			    System.out.println("Please select a server to hold!");
		    }
		}
		else if ((JMenuItem) _source == m_refresh_mi ||
			 (JMenuItem) _source == m_refresh_cmi){		    
		    int _select = m_server_table.getSelectedRow();
		    if (_select > -1)
			m_model.refreshServer(_select);
		    else{
			TreePath _selected_path = m_tree.getSelectionPath();
			if (_selected_path != null){
			    DefaultMutableTreeNode _node = (DefaultMutableTreeNode) 
				_selected_path.getLastPathComponent();
			    Object _imr_node = _node.getUserObject();
			    
			    if (_imr_node instanceof ServerInfo)
				m_model.refreshServer(((ServerInfo) _imr_node).name);
			    else
				System.out.println("Please select a server node!");
			}
			else
			    System.out.println("Please select a server to refresh!");
		    }
		}
		else if ((JMenuItem) _source == m_release_mi ||
			 (JMenuItem) _source == m_release_cmi){		    
		    int _select = m_server_table.getSelectedRow();
		    if (_select > -1)
			m_model.releaseServer(_select);
		    else{
			TreePath _selected_path = m_tree.getSelectionPath();
			if (_selected_path != null){
			    DefaultMutableTreeNode _node = (DefaultMutableTreeNode) 
				_selected_path.getLastPathComponent();
			    Object _imr_node = _node.getUserObject();
			    
			    if (_imr_node instanceof ServerInfo)
				m_model.releaseServer(((ServerInfo) _imr_node).name);
			    else
				System.out.println("Please select a server node!");
			}
			else
			    System.out.println("Please select a server to release!");
		    }
		}
		else if ((JMenuItem) _source == m_setdown_mi ||
			 (JMenuItem) _source == m_setdown_cmi){		    
		    int _select = m_server_table.getSelectedRow();
		    if (_select > -1)
			m_model.setServerDown(_select);
		    else{
			TreePath _selected_path = m_tree.getSelectionPath();
			if (_selected_path != null){
			    DefaultMutableTreeNode _node = (DefaultMutableTreeNode) 
				_selected_path.getLastPathComponent();
			    Object _imr_node = _node.getUserObject();

			    if (_imr_node instanceof ServerInfo)
				m_model.setServerDown(((ServerInfo) _imr_node).name);
			    else
				System.out.println("Please select a server node!");
			}
			else
			    System.out.println("Please select a server to set down!");
		    }
		}
	    }
	}

	// implementation of javax.swing.event.TreeSelectionListener interface
	/**
	 * Respond to node selections in tree. Shows brings up desired table,
	 * and deletes the previous selection in the tables.
	 *
	 * @param event 
	 */
	public void valueChanged(TreeSelectionEvent event) {
	    DefaultMutableTreeNode _select =(DefaultMutableTreeNode) (event.getPath().getLastPathComponent());
	    Object _node_obj = _select.getUserObject();

	    if (_node_obj instanceof ServerInfo){
		m_poa_table.clearSelection();
		m_server_table.clearSelection();
		showServerTable();
	    }
	    else if (_node_obj instanceof ImRInfo){
		m_server_table.clearSelection();
		showServerTable();
	    }
	    else if (_node_obj instanceof POAInfo){
		m_server_table.clearSelection();
		ServerInfo _parent = (ServerInfo) ((DefaultMutableTreeNode) 
						   _select.getParent()).getUserObject();
		m_model.poaTableForServer(_parent);

		if (m_poa_table.getRowCount() > 1){
		    int _row = m_model.getRow(_parent, (POAInfo) _node_obj);
		    m_poa_table.clearSelection();
		    m_poa_table.setRowSelectionInterval(_row, _row);
		}

		showPOATable();
	    }
	}

	// implementation of javax.swing.event.ListSelectionListener interface
	/**
	 * Delete selection in tree, if a table row has been selected.
	 *
	 * @param event
	 */
	public void valueChanged(ListSelectionEvent event) {
	    if (! ((ListSelectionModel)event.getSource()).isSelectionEmpty())
		m_tree.clearSelection();
	}
	

	// implementation of java.awt.event.MouseListener interface
	/**
	 * Bring up popup menu.
	 *
	 * @param event
	 */
	public void mouseClicked(MouseEvent event) {
	     maybeShowContextMenu(event);
	}
	
	/**
	 * NOT IMPLEMENTED
	 */
	public void mouseEntered(MouseEvent param1) {
	    // NOT IMPLEMENTED
	}
	
	/**
	 * NOT IMPLEMENTED
	 */
	public void mouseExited(MouseEvent param1) {
	    // NOT IMPLEMENTED
	}
	
	/**
	 * Bring up popup menu.
	 */
	public void mousePressed(MouseEvent event){
	    maybeShowContextMenu(event);
	}

	
	/**
	 * Bring up popup menu.
	 */
	public void mouseReleased(MouseEvent event) {
	    maybeShowContextMenu(event);
	}
	
	/**
	 * Test whether to bring up popup menu, or not.
	 * This is the case, when a server is selected
	 * (in the tree or in the table).
	 */
	private void maybeShowContextMenu(MouseEvent event){
	    if (event.isPopupTrigger()){
		Component _source =  event.getComponent();
		if (_source == m_server_table){
		    if (m_server_table.getSelectedRow() > -1)
			m_context_menu.show(_source, event.getX(), event.getY());
		}
		else if (_source == m_tree){
		    TreePath _selected_path = m_tree.getSelectionPath();
		    if (_selected_path != null){
			DefaultMutableTreeNode _node = (DefaultMutableTreeNode) 
			    _selected_path.getLastPathComponent();
		
			if (_node.getUserObject() instanceof ServerInfo)
			    m_context_menu.show(_source, event.getX(), event.getY());
		    }
		}
	    }
	}
    }//EventHandler
	
} // ImRManagerGUI








