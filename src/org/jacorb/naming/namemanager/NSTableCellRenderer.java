package org.jacorb.naming.namemanager;

import java.awt.Color;
import java.awt.Component;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

/**
 * 
 */
public class NSTableCellRenderer 
    extends DefaultTableCellRenderer
{
    public Component getTableCellRendererComponent(
                            JTable table, Object color,
                            boolean isSelected, boolean hasFocus,
                            int row, int column) 
    {
        super.getTableCellRendererComponent(table, color, isSelected, hasFocus, row, column);
        setForeground(Color.blue);
        return this;
    }
}
 
