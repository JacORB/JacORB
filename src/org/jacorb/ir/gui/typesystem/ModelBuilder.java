/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1999-2004 Gerald Brose
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

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

/**
 * @author Joerg von Frantzius
 */

public class ModelBuilder
    implements Runnable, TreeExpansionListener, TreeModelListener
{
    private Hashtable threadArguments = new Hashtable();
    // (mit der Hashtable und dem Threadnamen als Key sind
    // mehrere buildTreeModelAsync()-Threads möglich)

    protected Hashtable expandedModParts = new Hashtable();
    // key: DefaultMutableTreeNode
    // hier stehen die TreeNodes drin, auf deren ModelParticipant
    // bereits expand() aufgerufen und komplett abgearbeitet wurde

    protected Hashtable treeViewsToUpdate = new Hashtable();
    // key: DefaultMutableTreeNode; value: JTree

    protected Hashtable treeNodesAndTableModels = new Hashtable();
    // key: TreeNode; value: Vector[TableModel]
    // welche TableModels gehören zu einer TreeNode

    private Hashtable treeModelsListenedTo = new Hashtable();
    // key: TreeModel
    // zu welchen TreeModels sind wir als Listener eingetragen
    // (mehrfaches addTreeModelListener() auf dem selben
    // TreeModel würde mehrfache Events geben!)

    private static ModelBuilder singleton = new ModelBuilder();

    // wir brauchen eine ModelBuilder-Instanz, um einen Thread starten zu können
    // (es wäre eine eigene Klasse nötig, wenn die Methoden static wären;
    // inner classes gibts bei VisualAge leider noch nicht)

    /**
     * @return javax.swing.tree.TreeModel
     */
    public DefaultTreeModel buildTreeModel(ModelParticipant rootModPart)
    {
    DefaultMutableTreeNode root = new DefaultMutableTreeNode(rootModPart);
    DefaultTreeModel treeModel = new DefaultTreeModel(root);
    treeModel.setAsksAllowsChildren(true);
    rootModPart.buildTree(treeModel,null);
    return treeModel;
    }
    /**
     * @return javax.swing.tree.TreeModel
     */
    public DefaultTreeModel buildTreeModelAsync(ModelParticipant rootModPart)
    {
    DefaultMutableTreeNode root = new DefaultMutableTreeNode(rootModPart);
    DefaultTreeModel treeModel = new DefaultTreeModel(root);
    treeModel.setAsksAllowsChildren(true);
    Thread thread = new Thread(this);
    threadArguments.put(thread.getName(),treeModel);
        // run() entnimmt das Model der Hashtable
    thread.start();	// asynchronen Aufbau des TreeModels starten
    return treeModel;
    }
    /**
     * Erzeugt TreeModel, das nur root enthält. Um Nodes zu
     * expandieren, muß der von getTreeExpansionListener(treeModel)
     * zurückgegebene TreeExpansionListener bei JTree angemeldet werden.
     * @return javax.swing.tree.DefaultTreeModel
     */
    public DefaultTreeModel createTreeModelRoot(ModelParticipant rootModPart) {
    DefaultMutableTreeNode root = new DefaultMutableTreeNode(rootModPart);
    DefaultTreeModel treeModel = new DefaultTreeModel(root);
    rootModPart.addToParent(treeModel,null);
    treeModel.setAsksAllowsChildren(true);
    return treeModel;
    }
    /**
     * Dummy nur aus Synchronisierungsgründen
     * @param modPart org.jacorb.ir.gui.typesystem.ModelParticipant
     */
    private synchronized void expandModPart(ModelParticipant modPart,
                                            DefaultTreeModel treeModel)
    {
    modPart.expand(treeModel);
    return;
    }

    public static ModelBuilder getSingleton()
    {
    return singleton;
    }

    /**
     * @return TableModel
     */
    public synchronized DefaultTableModel getTableModel(DefaultTreeModel treeModel,
                                                        DefaultMutableTreeNode treeNode)
    {
    DefaultTableModel tableModel = new DefaultTableModel();
    java.lang.Object[] colIdentifiers = {"Item","Type","Name"};
    tableModel.setColumnIdentifiers(colIdentifiers);
        //	tableModel.setSortColumn("Name"); // gibt's nicht mehr in Swing 0.7

    if (treeNode!=null &&
            (treeNode.getUserObject() instanceof AbstractContainer))
        {
            if (!treeModelsListenedTo.containsKey(treeModel)) {
                treeModel.addTreeModelListener(this);
                treeModelsListenedTo.put(treeModel,treeModel);
            }
            if (treeNodesAndTableModels.containsKey(treeNode)) {
                Vector tableModels = (Vector)treeNodesAndTableModels.get(treeNode);
                tableModels.addElement(tableModel);
            }
            else {
                Vector tableModels = new Vector();
                tableModels.addElement(tableModel);
                treeNodesAndTableModels.put(treeNode,tableModels);
            }
            if (!expandedModParts.containsKey(treeNode)) {
                // Unterbaum muß erst geladen werden
                startExpandNode(treeModel,treeNode);
                // wir sind synchronized, desgleichen expandModPart(),
                // das vom Thread aufgerufen wird
                // => wir kriegen alle Events mit, die generiert werden,
                // wenn treeModel sich ändert
            }
            else
            {
                // alles klar, Unterbaum ist schon da
                for (int i=0; i<treeModel.getChildCount(treeNode); i++) {
                    insertTableRow(tableModel,(DefaultMutableTreeNode)treeNode.getChildAt(i),i);
                }
            }	// if (!expandedModParts.containsKey(treeNode))
    }	// if (treeNode!=null...)
    return tableModel;
    }

    /**
     * @return javax.swing.event.TreeExpansionListener
     * @param treeModel javax.swing.tree.DefaultTreeModel
     */

    public TreeExpansionListener getTreeExpansionListener(TreeModel treeModel)
    {
    return this;	// treeModel kann ignoriert werden
    }

    /**
     * @param tableModel javax.swing.table.DefaultTableModel
     * @param treeNode javax.swing.tree.DefaultMutableTreeNode
     */
    private void insertTableRow(DefaultTableModel tableModel,
                                DefaultMutableTreeNode treeNode,
                                int index)
    {
        TypeSystemNode typeSystemNode = (TypeSystemNode)treeNode.getUserObject();
        String type = "";
        if (typeSystemNode instanceof TypeAssociator) {
            type = ((TypeAssociator)typeSystemNode).getAssociatedType();
        }
        java.lang.Object[] row =
        {new NodeMapper(typeSystemNode,typeSystemNode.getInstanceNodeTypeName()),
         new NodeMapper(typeSystemNode,type),
         new NodeMapper(typeSystemNode,typeSystemNode.getName())};
        tableModel.insertRow(index,row);
    }

    /**
     */
    public void run ( )
    {
    Object argument = threadArguments.get(Thread.currentThread().getName());
    if (argument instanceof DefaultTreeModel)
        {
            DefaultTreeModel treeModel = (DefaultTreeModel)argument;
            ModelParticipant root =
                (ModelParticipant)((DefaultMutableTreeNode)treeModel.getRoot()).getUserObject();
            root.buildTree(treeModel,null);
    }
    if (argument instanceof Object[]) {
            Object[] args = (Object[])argument;
            DefaultTreeModel treeModel = (DefaultTreeModel)args[0];
            ModelParticipant modPart = (ModelParticipant)args[1];
            expandModPart(modPart,treeModel);
    }
    }

    /**
     * Startet Thread, der expand() auf ModelParticipant der TreeNode aufruft
     * @param treeModel javax.swing.tree.DefaultTreeModel
     * @param treeNode javax.swing.tree.DefaultMutableTreeNode
     */
    private void startExpandNode(DefaultTreeModel treeModel,
                                 DefaultMutableTreeNode treeNode)
    {
    ModelParticipant modPart = (ModelParticipant)treeNode.getUserObject();

    if (!expandedModParts.containsKey(treeNode)) {
            System.out.println("expanding node: "+treeNode);
            Thread thread = new Thread(this);
            Object[] args = new Object[2];
            args[0] = treeModel;
            args[1] = modPart;
            threadArguments.put(thread.getName(),args);
            // run() entnimmt das Model der Hashtable
            thread.start();	// asynchrones expand starten
            //		modPart.expand(treeModel);
    }
    }

    /**
     * @param e javax.swing.event.TreeExpansionEvent
     */
    public void treeCollapsed(TreeExpansionEvent e)
    {
    return;
    }

    /**
     * @param e javax.swing.event.TreeExpansionEvent
     */

    public synchronized void treeExpanded(TreeExpansionEvent e)
    {
    javax.swing.JTree jTree = (javax.swing.JTree)e.getSource();
    DefaultTreeModel treeModel = (DefaultTreeModel)jTree.getModel();
    TreePath path = e.getPath();
    DefaultMutableTreeNode treeNode =
            (DefaultMutableTreeNode)path.getPathComponent(path.getPathCount()-1);
    treeViewsToUpdate.put(treeNode,jTree);	// TreeView hinterlegen, der sofort hinzugefügte Nodes anzeigen soll
    startExpandNode(treeModel,treeNode);
    }

    public void treeNodesChanged(TreeModelEvent te) {}

    public void treeNodesInserted(TreeModelEvent te)
    {
        //	System.out.println("treeNodesInserted()");
    DefaultMutableTreeNode treeNode =
            (DefaultMutableTreeNode)te.getTreePath().getLastPathComponent();
    Vector tableModels = (Vector)treeNodesAndTableModels.get(treeNode);
    DefaultTableModel tableModel;
    int[] indices = te.getChildIndices();

    if (tableModels!=null)
        {
            // else: zu der TreeNode gibt es keine TableModels
            for (int i=0; i<indices.length; i++) {
                for (Enumeration e = tableModels.elements(); e.hasMoreElements(); ) {
                    tableModel = (DefaultTableModel)e.nextElement();
                    insertTableRow(tableModel,(DefaultMutableTreeNode)treeNode.getChildAt(indices[i]),indices[i]);
                }
            }
    }
    }

    public void treeNodesRemoved(TreeModelEvent te) {}

    public void treeStructureChanged(TreeModelEvent te) {}
}








