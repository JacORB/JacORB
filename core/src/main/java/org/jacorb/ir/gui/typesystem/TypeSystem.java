/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1999-2014 Gerald Brose / The JacORB Team.
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
package org.jacorb.ir.gui.typesystem;

import java.util.Hashtable;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;

/**
 * This class was generated by a SmartGuide.
 *
 */
public abstract class TypeSystem
{
    // Mapping from user-readable nodeTypeNames to class names.
    // Subclasses of TypeSystemNode register themselves here
    // (since actually only classes that correspond to the CORBA-IR classes
    // register themselves, there cannot be any name conflicts, since
    // IDL identifiers are unique) 

    private static Hashtable nodeTypes = new Hashtable();

    protected DefaultTreeModel treeModel = null;

    /**
     * Creates node corresponding to the given nodeTypeName
     * (e.g. "module" creates an IRModule object)
     * @return org.jacorb.ir.gui.typesystem.TypeSystemNode
     * @param nodeTypeName java.lang.String
     */
    public static TypeSystemNode createNode (String nodeTypeName)
    throws ClassNotFoundException
    {
        TypeSystemNode node = null;
        Class c = Class.forName( (String)nodeTypes.get(nodeTypeName) );
        try {
            node = (TypeSystemNode)c.newInstance();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return node;
    }

    /**
     * Creates a TreeModel that only contains root. To expand Nodes, the
     * TreeExpansionListener returned by getTreeExpansionListener(treeModel)
     * needs to be registered with JTree.
     * zurückgegebene TreeExpansionListener bei JTree angemeldet werden.
     * @return javax.swing.tree.DefaultTreeModel
     */
    public abstract DefaultTreeModel createTreeModelRoot();

    /**
     * This method was created by a SmartGuide.
     * @return TableModel
     */
    public abstract DefaultTableModel getTableModel(DefaultMutableTreeNode treeNode);

    /**
     * This method was created by a SmartGuide.
     * @return javax.swing.event.TreeExpansionListener
     * @param treeModel javax.swing.tree.DefaultTreeModel
     */
    public abstract javax.swing.event.TreeExpansionListener getTreeExpansionListener(TreeModel treeModel);

    /**
     * This method was created by a SmartGuide.
     * @return javax.swing.tree.TreeModel
     */
    public abstract TreeModel getTreeModel ( );

    /**
     * Called by static initializers of subclasses of TypeSystemNode,
     * in order to register themselves for createNode()
     * @param nodeTypeName java.lang.String
     * @param className java.lang.String
     */
    protected static void registerNodeType(String nodeTypeName, String className) {
        nodeTypes.put(nodeTypeName,className);
    }
}

