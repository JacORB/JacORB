package org.jacorb.imr.util;

import org.jacorb.imr.*;
import org.jacorb.imr.AdminPackage.*;
import org.jacorb.util.Debug;

import java.util.*;

import javax.swing.tree.*;
import javax.swing.*;
import javax.swing.table.*;
/**
 * This class provides the GUIs functionality
 * communicating with the repository.
 *
 * @author Nicolas Noffke
 * 
 * $Id$
 */

public class ImRModel  {
    private Admin m_admin;
    private org.omg.CORBA.ORB m_orb;
    
    private ServerInfo[] m_servers;
    private ImRInfo m_imr_info;

    private DefaultMutableTreeNode m_top_node;
    private JTree m_tree;
    private DefaultTreeModel m_tree_model;

    private ImRPOATableModel m_poa_model;
    private ImRServerTableModel m_server_model;

    private Hashtable m_server_names;

    private JComboBox m_host_selector;
    private DefaultComboBoxModel m_host_selector_model;
    private Hashtable m_host_names;

    private RefreshThread m_refresh_thread;
    protected int m_current_refresh_interval = 20000;
    protected boolean m_refresh_disabled = false;

    private Vector m_server_nodes;
    private Vector m_poa_nodes; //contains other Vectors

    /**
     * The constructor. Connects to default repository and fetches the servers.
     */
    public ImRModel() {
	m_orb = org.omg.CORBA.ORB.init(new String[0], null);
	m_admin = AdminHelper.narrow(ImplementationRepositoryImpl.getImR(m_orb));
	
	fetchImRInfo();

	m_top_node = new DefaultMutableTreeNode(m_imr_info);
	m_tree_model = new DefaultTreeModel(m_top_node, false);
	m_tree = new JTree(m_tree_model);
	
	m_server_model = new ImRServerTableModel(this);
	m_poa_model = new ImRPOATableModel();
	
	m_server_names = new Hashtable();
	
	m_server_nodes = new Vector();
	m_poa_nodes = new Vector();

	m_host_names = new Hashtable();
	m_host_selector_model = new DefaultComboBoxModel();
	m_host_selector = new JComboBox(m_host_selector_model);
	m_host_selector.setEditable(true);

	fetchServers();
	m_tree.expandRow(0);

	m_refresh_thread = new RefreshThread(m_current_refresh_interval);
    }

    /**
     * Connect the manager to a remote repository.
     *
     * @param ior_url an url pointing to the IOR file of a remote repository.
     */
    public void connectTo(String ior_url){
	m_admin = AdminHelper.narrow(ImplementationRepositoryImpl.getImR(m_orb, ior_url));
	
	fetchImRInfo();

	m_top_node.setUserObject(m_imr_info);

	fetchServers();
	
	setRefreshInterval(m_current_refresh_interval);
    }
	
    /**
     * Get a JComboBox containing all known hostnames.
     *
     * @return a JComboBox.
     */
    public JComboBox getHostSelector(){
	return m_host_selector;
    }

    /**
     * Get the table model for the POA table.
     *
     * @param the model for the POA table.
     */
    public TableModel getPOATableModel(){
	return m_poa_model;
    }

    /**
     * Get the table model for the server table.
     *
     * @param the model for the server table.
     */
    public TableModel getServerTableModel(){
	return m_server_model;
    }

    /**
     * Set the POA table model to the specific server, i.e.
     * the POA table displays this servers poas.
     *
     * @param name the servers name to build the table for.
     */
    public void poaTableForServer(ServerInfo server){
	m_poa_model.setPOAs(server.poas);
    }

    /**
     * Fetch all servers from the repository. Rebuild Tree and HostSelector.
     */
    public void fetchServers(){
	m_servers = m_admin.list_servers();

	m_server_model.setServers(m_servers);

	m_server_names.clear();
	for (int _i = 0; _i < m_servers.length; _i++)
	    m_server_names.put(m_servers[_i].name, new Integer(_i));

	String _server = m_poa_model.getServerName();
	if (_server != null)
	    m_poa_model.setPOAs(m_servers[indexForServerName(_server)].poas);

	buildTree();
	buildHostSelectorModel();
    }

    /**
     * Remove a server from the repository.
     *
     * @param name the servers name.
     */
    public void removeServer(String name){
	removeServer(indexForServerName(name));
    }

    /**
     * Remove a server from the repository.
     *
     * @param server_row the servers row in the table.
     */
    public void removeServer(int server_row){
	try{
	    m_admin.unregister_server(m_servers[server_row].name);
	}catch(Exception _e){
	    handleException (_e);
	}

	fetchServers();
    }

    /**
     * Hold a server.
     *
     * @param name the servers name.
     */
    public void holdServer(String name){
	holdServer(indexForServerName(name));
    }

    /**
     * Hold a server.
     *
     * @param server_row the servers row in the table.
     */
    public void holdServer(int server_row){
	try{
	    m_admin.hold_server(m_servers[server_row].name);
	}catch(Exception _e){
	   handleException (_e);
	}

	refreshServer(server_row);
    }

    /**
     * Release a server.
     *
     * @param name the servers name.
     */
    public void releaseServer(String name){
	releaseServer(indexForServerName(name));
    }

    /**
     * Release a server.
     *
     * @param server_row the servers row in the table.
     */
    public void releaseServer(int server_row){
	try{
	    m_admin.release_server(m_servers[server_row].name);
	}catch(Exception _e){
	   handleException (_e);
	}

	refreshServer(server_row);
    }

    /**
     * Refresh a server.
     *
     * @param name the servers name.
     */
    public void refreshServer(String name){
	refreshServer(indexForServerName(name));
    }

    /**
     * Refresh a server.
     *
     * @param server_row the servers row in the table.
     */
    public void refreshServer(int index){ 
	try{
	    ServerInfo _server = m_admin.get_server_info(m_servers[index].name);

	    m_servers[index] = _server;

	    buildServerNode(index);

	    m_server_model.serverRefreshed(index);

	    if (m_host_names.put(m_servers[index].host, m_servers[index].host) == null)
		m_host_selector_model.addElement(m_servers[index].host);

	    if ( _server.name.equals(m_poa_model.getServerName()))
	      m_poa_model.setPOAs(_server.poas);

	}catch(Exception _e){
	   handleException (_e);
	}
    }

    /**
     * Set a server down.
     *
     * @param name the servers name.
     */
    public void setServerDown(String name){
	setServerDown(indexForServerName(name));
    }

    /**
     * Set a server down.
     *
     * @param server_row the servers row in the table.
     */
    public void setServerDown(int server_row){
	Registration _reg = RegistrationHelper.narrow(m_admin);

	try{
	    _reg.set_server_down(m_servers[server_row].name);
	}catch (Exception _e){
	   handleException (_e);
	}
	
	refreshServer(m_servers[server_row].name);
    }

    /**
     * Add a server to the repository.
     *
     * @param name the servers name.
     * @param command the servers startup command. Leave empty (not null)
     * if automatic startup is not desired.
     * @param host the host the server is running on.
     */
    public void addServer(String name, String command, String host){
	try{
	    m_admin.register_server(name, command, host);
	}catch (Exception _e){
	   handleException (_e);
	}

	fetchServers();
    }

    /**
     * Get the tree representation of the server structure.
     *
     * @return a JTree.
     */
    public JTree getTree(){
	return m_tree;
    }
    
    /**
     * Shut the repository down.
     *
     * @param wait true, if ORB should wait for still open connections to be 
     * closed by clients.
     */
    public void imrShutdown(boolean wait){
	disableRefresh();

	try{
	    m_admin.shutdown(wait);
	    
	    m_top_node.removeAllChildren();
	    m_servers = null;
	}catch (Exception _e){
	   handleException (_e);
	}
    }
       
    /**
     * Make a backup of the server table.
     */
    public void saveTable(){
	try{
	    m_admin.save_server_table();
	}catch (Exception _e){
	   handleException (_e);
	}
    }

    /**
     * Get the row number of a POA in the POA table.
     *
     * @param server the server node the POA belongs to.
     * @param poa the poas poa node.
     */
    public int getRow(ServerInfo server, POAInfo poa){
	for(int _i = 0; _i < server.poas.length; _i++){
	    if (server.poas[_i] == poa)
		return _i;
	}

	return -1;
    }

    /**
     * Set the interval by which the internal data is refreshed.
     *
     * @param intervel refresh interval in ms.
     */
    public void setRefreshInterval(int interval){
	m_current_refresh_interval = interval;
	m_refresh_disabled = false;
	m_refresh_thread.setInterval(interval);
    }
    
    /**
     * Disable the automatic refresh.
     */
    public void disableRefresh(){
	m_refresh_disabled = true;
	m_refresh_thread.setInterval(0);
    }

    /**
     * Update a server in the repository by changes the user made in the server 
     * table of the GUI.
     *
     * @param server_row the row of the server in the table.
     * @param field_name the columns name.
     * @param new_value the cells new value.
     */
    protected void updateServer(int server_row, String field_name, Object new_value){
	String _host = m_servers[server_row].host;
	String _cmd  = m_servers[server_row].command;

	if (new_value instanceof String){
	    if (field_name.equals("Host")){
		_host = (String) new_value;
		if (m_host_names.put(new_value, new_value) == null)
		    m_host_selector_model.addElement(new_value);
	    }
	    else if (field_name.equals("Command"))
		_cmd = (String) new_value;
	    
	    try{
		m_admin.edit_server(m_servers[server_row].name, _cmd, _host);
	    }catch (Exception _e){
		handleException (_e);
	    }
	}
	else if(new_value instanceof Boolean){
	    if (field_name.equals("active")){
		if (! ((Boolean) new_value).booleanValue())
		    setServerDown(m_servers[server_row].name);
	    }
	    else if (field_name.equals("holding")){
		try{
		    if (((Boolean) new_value).booleanValue())
			m_admin.hold_server(m_servers[server_row].name);
		    else
			m_admin.release_server(m_servers[server_row].name);
		    
		}catch (Exception _e){
		    handleException (_e);
		}
	    }
	}
	
	refreshServer(m_servers[server_row].name);
    }

    /**
     * Bring up error message Dialog.
     *
     * @param e the exception that has been thrown.
     */
    private void handleException (Exception e){
      if (e instanceof org.omg.CORBA.UserException){
	String _msg = e.toString();
	if (e instanceof IllegalServerName)
	  _msg = "The specified server name is not allowed";
	else if (e instanceof DuplicateServerName)
	  _msg = "A server with name " + 
	    ((DuplicateServerName) e).name +
	    " has already been registered with the repository";
	else if (e instanceof FileOpFailed)
	  _msg = "The backup operation failed";
	
	JOptionPane.showMessageDialog(new JFrame(), _msg, 
				      "An error occurred",
				      JOptionPane.ERROR_MESSAGE);
      }
      else
	Debug.output(Debug.IMR | Debug.INFORMATION, e);    
    }


    /**
     * Fill the model of the combo box with host names.
     * After fetching hosts from the repository, they are "pinged" in order to
     * see if they are still up.
     */
    private void buildHostSelectorModel(){
	HostInfo[] _hosts = m_admin.list_hosts();

	for (int _i = 0; _i < _hosts.length; _i++){
	    try{
		ServerStartupDaemon _ssd = ServerStartupDaemonHelper.narrow(m_orb.string_to_object(_hosts[_i].ior_string));
		_ssd.get_system_load();
		_ssd._release();
		
		// ssd is up and seems to work
		if (m_host_names.put(_hosts[_i].name, _hosts[_i].name) == null)
		   m_host_selector_model.addElement(_hosts[_i].name);
	    } catch (Exception _e){
		//ignore
	    }
	}

	for (int _i = 0; _i < m_servers.length; _i++)
	    if (m_host_names.put(m_servers[_i].host, m_servers[_i].host) == null)
		m_host_selector_model.addElement(m_servers[_i].host);
    }

    /**
     * Get a servers row by its name.
     *
     * @param name the servers name.
     * @return the servers row
     */
    private int indexForServerName(String name){
	return ((Integer) m_server_names.get(name)).intValue();
    }

    /**
     * Get the ImRInfo struct from the repository.
     */
    private void fetchImRInfo(){
	Registration _reg = RegistrationHelper.narrow(m_admin);

	m_imr_info = _reg.get_imr_info();
    }

    /**
     * Build a tree node for a server, with all its 
     * dependend POAs.
     *
     * @param server the servers ServerInfo struct.
     */
    private void buildServerNode(int index){
	DefaultMutableTreeNode _server_node;
	POAInfo[] _poa_array = m_servers[index].poas;
	Vector _poas;
	if (index < m_server_nodes.size()){
	    // a server node for that index exists
	    _server_node = (DefaultMutableTreeNode) m_server_nodes.elementAt(index);
	    _poas = (Vector) m_poa_nodes.elementAt(index);

	}
	else{
	    // a new server node has to be created
	    _server_node = new DefaultMutableTreeNode(m_servers[index]);
	    m_server_nodes.addElement(_server_node);
	    m_tree_model.insertNodeInto(_server_node, m_top_node, index);
	    m_tree.scrollPathToVisible(new TreePath(_server_node.getPath()));

	    _poas = new Vector();
	    m_poa_nodes.addElement(_poas);
	}

	int _i;
	//update existing nodes
	for(_i = 0; _i < _poas.size(); _i++){
	    if (_i < _poa_array.length){
		DefaultMutableTreeNode _poa = (DefaultMutableTreeNode) _poas.elementAt(_i);
		_poa.setUserObject(_poa_array[_i]);
	    }
	    else
		break;
	}
	if (_i >= _poa_array.length){
	    //remove surplus nodes
	    for (int _j = _poas.size() - 1; _j >= _i; _j--){
		DefaultMutableTreeNode _poa = (DefaultMutableTreeNode) _poas.elementAt(_j);
		_poas.removeElementAt(_j);
		m_tree_model.removeNodeFromParent(_poa);
	    }
	}
	else{
	    // build new nodes
	    for (int _j = _i; _j < _poa_array.length; _j++){
		DefaultMutableTreeNode _poa = new DefaultMutableTreeNode(_poa_array[_j]);
		_poas.addElement(_poa);
		m_tree_model.insertNodeInto(_poa, _server_node, _j);
	    }
	}
    }

    /**
     * Remove a server node from the tree.
     *
     * @param index the servers index in the table.
     */
    private void removeServerNode(int index){
	DefaultMutableTreeNode _server_node = (DefaultMutableTreeNode) m_server_nodes.elementAt(index);
	Vector _poas = (Vector) m_poa_nodes.elementAt(index);
	
	for (int _j = _poas.size() - 1; _j >= 0; _j--){
		DefaultMutableTreeNode _poa = (DefaultMutableTreeNode) _poas.elementAt(_j);
		_poas.removeElementAt(_j);
		m_tree_model.removeNodeFromParent(_poa);
	}
 
	m_server_nodes.removeElementAt(index);
	m_tree_model.removeNodeFromParent(_server_node);
    }
	


    /**
     * Build the tree by building all its server nodes. The
     * root node stays always the same.
     */
    private void buildTree(){
	int _i;
	// update exisiting nodes
	for(_i = 0; _i < m_server_nodes.size(); _i++){
	    if (_i < m_servers.length){
		DefaultMutableTreeNode _server = (DefaultMutableTreeNode) m_server_nodes.elementAt(_i);
		_server.setUserObject(m_servers[_i]);
		buildServerNode(_i);
	    }
	    else
		break;
	}
	
	if (_i >= m_servers.length){
	    //remove surplus nodes
	    for (int _j = m_server_nodes.size() - 1; _j >= _i; _j--)
		removeServerNode(_j);
	}
	else{
	    //add new nodes
	    for (int _j = _i; _j < m_servers.length; _j++)
		buildServerNode(_j);
	}
    }

    private class RefreshThread extends Thread{
	private long m_interval;
	private boolean m_run = true;

	public RefreshThread(long interval){
	    m_interval = interval;

	    start();
	}

	public synchronized void run(){
	    while (true){
		while (m_interval <= 0){
		    try{
			this.wait();
		    }catch (Exception _e){
			handleException(_e);
		    }
		}
		try{
		    fetchServers();
		   }catch (Exception _e){
		       handleException(_e);
		   }

		try{
			this.wait(m_interval);
		}catch (Exception _e){
		    handleException(_e);
		}
	    }
	}

	public synchronized void setInterval(long interval){
	    m_interval = interval;

	    this.notifyAll();
	}
    }
} // ImRModel








