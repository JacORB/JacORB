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

import javax.swing.table.*;
import org.jacorb.imr.*;
import javax.swing.event.*;
/**
 * This is the model for the POA table. It does
 * not write back data since the entries in the POA 
 * table are not editable.
 *
 * @author Nicolas Noffke
 * 
 * $Log$
 * Revision 1.5  2002/07/01 07:54:16  nicolas
 * updated or inserted Copyright notice
 *
 * Revision 1.4  2002/03/19 09:25:11  nicolas
 * updated copyright to 2002
 *
 * Revision 1.3  2002/03/19 11:08:01  brose
 * *** empty log message ***
 *
 * Revision 1.2  2002/03/17 18:44:01  brose
 * *** empty log message ***
 *
 * Revision 1.4  1999/11/25 16:05:48  brose
 * cosmetics
 *
 * Revision 1.3  1999/11/21 20:15:52  noffke
 * GUI data is now updated periodically by a thread
 *
 * Revision 1.2  1999/11/14 17:15:40  noffke
 * Cosmetics and commenting
 *
 *
 */

public class ImRPOATableModel extends AbstractTableModel {
    private static final String[] m_columns = new String[] {"Name", "Host", "Port", "active"};

    private POAInfo[] m_poas = null;

    /**
     * Pass in the POAs the POA table should display.
     * Notify the JTable of this event.
     *
     * @param poas an array containing the POAs to display.
     */
    public void setPOAs(POAInfo[] poas){
	if (m_poas != poas){
	    m_poas = poas;
	    fireTableChanged(new TableModelEvent(this));
	}
    }

    /**
     * Get the number of rows.
     *
     * @return int the number of rows of this table.
     */
    public int getRowCount(){
	if (m_poas == null)
	    return 0;
	else
	    return m_poas.length;
    }

    /**
     * Get the number of columns.
     *
     * @return int the number of columns of this table.
     */
    public int getColumnCount(){
	return m_columns.length;
    }

    /**
     * Get the name of a specific column.
     *
     * @param column the columns number.
     * @return the columns name.
     */
    public String getColumnName(int column){
	return m_columns[column];
    }

    /**
     * Get the class of a specific column.
     *
     * @param index the columns index.
     * @return the Class object for the column.
     */
    public Class getColumnClass(int index){
	if (index == 0 || index == 1)
	    return String.class;

	else if (index == 2)
	    return  Integer.class;

	else if (index == 3)
	    return Boolean.class;

	else    
	    return Object.class;
    }

    /**
     * Get the value of a specific table cell.
     *
     * @param row the cells row.
     * @param column the cells column.
     * @return Object the cells value.
     */
    public Object getValueAt(int row, int column){
	if (column == 0)
	    return m_poas[row].name;

	else if (column == 1)
	    return m_poas[row].host;

	else if (column == 2)
	    return new Integer(m_poas[row].port);

	else if (column == 3)
	    return new Boolean(m_poas[row].active);

	return new Object();
    }

    /**
     * Get the name of the server these POAs are associated with.
     *
     * @return a server name.
     */
    public String getServerName(){
	if (m_poas == null || m_poas.length == 0)
	    return null;
	else
	    //all POAs in one array have the same server
	    return m_poas[0].server;
    }
} // ImRPOATableModel








