package org.jacorb.imr.util;

import javax.swing.table.*;
import org.jacorb.imr.*;
import javax.swing.event.*;
/**
 * This class is the model for the server table.
 * On user changes, it writes back its edited cells
 * via the IMRModel class.
 *
 * @author Nicolas Noffke
 * 
 * $Id$
 */

public class ImRServerTableModel extends AbstractTableModel {
    private ImRModel m_model;
    private static final String[] m_columns = new String[] {"Name", "Host", "Command", "active", "holding"};

    private ServerInfo[] m_servers;

    /**
     * The constructor.
     *
     * @param model the ImRModel to write changes via.
     */
    public ImRServerTableModel(ImRModel model){
	m_model = model;
    }

    /**
     * Pass in the servers the table should display.
     * Notify the JTable of that.
     *
     * @param servers an array containing the ServerInfo structs of the 
     * servers to display.
     */
    public void setServers(ServerInfo[] servers){
	m_servers = servers;
	fireTableChanged(new TableModelEvent(this));
    }

    /**
     * Notify the JTable that a server has been updated.
     *
     * @param index the servers index in the table.
     */
    public void serverRefreshed(int index){
	fireTableRowsUpdated(index, index);
    }

    /**
     * Get the number of rows of this table.
     *
     * @return the number of rows.
     */
    public int getRowCount(){
	return m_servers.length;
    }

    /**
     * Get the number of columns of this table.
     *
     * @return the number of columns.
     */
    public int getColumnCount(){
	return m_columns.length;
    }

    /**
     * Get the name of a specific column.
     *
     * @param index the columns index.
     * @return String the columns name.
     */
    public String getColumnName(int column){
	return m_columns[column];
    }

    /**
     * Get the class of a specific column.
     *
     * @param index the columns index.
     * @return the columns Class object.
     */
    public Class getColumnClass(int index){
	if (index == 0 || index == 1 || index == 2)
	    return String.class;

	else if (index == 3 || index == 4)
	    return Boolean.class;

	else    
	    return  Object.class;
    }

    /**
     * Get the value of a specific cell.
     *
     * @param row the cells row.
     * @param column the cells column.
     * @return the cells value.
     */
    public Object getValueAt(int row, int column){
	if (column == 0)
	    return m_servers[row].name;

	else if (column == 1)
	    return m_servers[row].host;

	else if (column == 2)
	    return m_servers[row].command;

	else if (column == 3)
	    return new Boolean(m_servers[row].active);
	
	else if (column == 4)
	    return new Boolean(m_servers[row].holding);
	
	return new Object();
    }

    /**
     * Test, wheter a cell is editable.
     * 
     * @param row the cells row.
     * @param column the cells column.
     *
     * @return true, if the cell is editable.
     */
    public boolean isCellEditable(int row, int column){
	if (column >= 1)
	    return true;
	else
	    return false;
    }

    /**
     * Set the value of a specific cell, i.e. the user has edited a cell.
     *
     * @param value the new value.
     * @param row the cells row.
     * @param column the cells column.
     */
    public void setValueAt(Object value, int row, int column){
	m_model.updateServer(row, m_columns[column], value); 
    }
} // ImRServerTableModel








