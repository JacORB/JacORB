/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1997-2004 Gerald Brose.
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

package org.jacorb.naming.namemanager;

import java.util.Vector;

public class NSTableModel
    extends javax.swing.table.DefaultTableModel
{
    /**
     * NSTableModel constructor.
     */
    public NSTableModel()
    {
    super();
    String [] colNames = {"Name", "Kind", "Type", "Host", "Port"};
    super.setColumnIdentifiers(convertToVector(colNames));
    }

    /**
     * getColumnCount method comment.
     */
    public int getColumnCount()
    {
    return 5;
    }

    /**
     * always returns false to make this table non-editable
     * @return boolean
     * @param r int
     * @param c int
     */
    public boolean isCellEditable(int r, int c)
    {
    return false;
    }

    public void setDataVector(Vector newData)
    {
        if (newData == null)
            throw new IllegalArgumentException("setDataVector() - Null parameter");

        // Add the new rows.
        dataVector = newData;

        // Make all the new rows the right length and generate a notification.
        newRowsAdded(new javax.swing.event.TableModelEvent(this, 0, getRowCount()-1,
                                                           javax.swing.event.TableModelEvent.ALL_COLUMNS,
                                                           javax.swing.event.TableModelEvent.INSERT));
    }
}




