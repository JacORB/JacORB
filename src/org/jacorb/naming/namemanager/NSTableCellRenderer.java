package org.jacorb.naming.namemanager;

import javax.swing.*;
import javax.swing.tree.*;
import java.awt.*;
import javax.swing.table.*;

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
 
