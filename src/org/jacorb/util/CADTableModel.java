/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1999-2002 Gerald Brose
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
package org.jacorb.util;

import javax.swing.table.*;
/**
 * CADTableModel.java
 *
 *
 * Created: Tue Jun 27 10:47:27 2000
 *
 * @author Nicolas Noffke
 * $Id$
 */

public class CADTableModel 
    extends AbstractTableModel
{
    private String[][] bits = null;
    
    public CADTableModel()
    {
        bits = new String[4][8];

        String entry = "0";
        for(int i = 0; i < 4; i++)
            for(int j = 0; j < 8; j++)
                bits[i][j] = entry;
    }

    public int getRowCount()
    {
        return 4;
    }

    public int getColumnCount()
    {
        return 8;
    }
    
    public Class getColumnClass(int index)
    {
        return String.class;
    }

    public String getColumnName(int index)
    {
        return "" + (7 - index);
    }

    public Object getValueAt(int row, int column)
    {
        return bits[row][column];
    }
    
    protected void setBit(int index, int value)
    {   
        int row = (31 - index) / 8;
        int col = (31 - index) % 8;

        bits[row][col] = "" + value;

        fireTableCellUpdated(row, col);
    }
} // CADTableModel






