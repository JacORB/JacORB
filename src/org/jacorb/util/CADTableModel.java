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
