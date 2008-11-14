package org.jacorb.ir.gui.typesystem;

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

import java.util.Hashtable;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

/**
 *
 */

public abstract class ModelParticipant
{
    private Hashtable modelRepresentants = new Hashtable();
    private static ModelBuilder modelBuilder = ModelBuilder.getSingleton();

    public void addToParent( DefaultTreeModel treeModel, DefaultMutableTreeNode parentTreeNode)
    {
        DefaultMutableTreeNode treeNode;

        if (parentTreeNode!=null)
        {  // bei root ist es null
            treeNode =  new DefaultMutableTreeNode(this);
            int i = 0;
            while ((i<parentTreeNode.getChildCount()) &&
                   ((ModelParticipant)((DefaultMutableTreeNode)treeModel.getChild(parentTreeNode,i)).getUserObject()).compareTo(this) < 0)
            {
                i++;
            }

            treeModel.insertNodeInto(treeNode,parentTreeNode,i);
        }
        else
        {
            // wir sind root; unsere TreeNode wurde (bzw. müßte!) dem Konstruktor
            // von DefaultTreeModel mitgegeben werden, wir müssen uns also nicht mehr inserten
            treeNode = (DefaultMutableTreeNode)treeModel.getRoot();
        }

        setModelRepresentant(treeModel, treeNode);
        if (this instanceof AbstractContainer)
        {
            treeNode.setAllowsChildren(true);
        }
        else
        {
            treeNode.setAllowsChildren(false);
        }
    }

    /**
     * Baut Tree für diese Node auf.
     * Kann leider nicht protected sein, weil Methode sonst selbst für Unterklassen in einem Unter-Package
     * nicht sichtbar ist.
     * @param treeModel TreeModel
     */

    public void buildTree ( DefaultTreeModel treeModel, DefaultMutableTreeNode parentTreeNode )
    {
        addToParent(treeModel,parentTreeNode);
        DefaultMutableTreeNode treeNode =
            (DefaultMutableTreeNode)modelRepresentants.get(treeModel);
        if (this instanceof AbstractContainer)
        {
            ModelParticipant[] contents =
                ((AbstractContainer)this).contents();

            treeNode.setAllowsChildren(true);

            for (int i=0; i<contents.length; i++)
            {
                if (contents[i]!=null)
                {        // solange nicht alles implementiert ist gibt's null-Einträge
                    contents[i].buildTree(treeModel,treeNode);
                }
            }
        }

    }
    /**
     * @return int
     * @param other org.jacorb.ir.gui.typesystem.ModelParticipant
     */
    public abstract int compareTo(ModelParticipant other);

    public synchronized void expand(DefaultTreeModel treeModel)
    {
        boolean jTreeExpanded = false;
        // Hack, damit man gleich sieht, wie die Nodes eintrudeln
        if (this instanceof AbstractContainer)
        {
            DefaultMutableTreeNode treeNode =
                (DefaultMutableTreeNode)modelRepresentants.get(treeModel);
            ModelParticipant[] contents = ((AbstractContainer)this).contents();
            for (int i=0; i<contents.length; i++)
            {
                if (contents[i]!=null)
                {
                    // solange nicht alles implementiert ist gibt's null-Einträge
                    contents[i].addToParent(treeModel,treeNode);
                }
                if (!jTreeExpanded)
                {
                    javax.swing.JTree jTree =
                        (javax.swing.JTree)modelBuilder.treeViewsToUpdate.get(treeNode);
                    if (jTree!=null)
                    {
                        jTree.expandPath(new TreePath(treeNode.getPath()));
                    }
                }
            }
            modelBuilder.expandedModParts.put(treeNode,treeNode);
        }
    }
    /**
     * @return java.lang.Object
     * @param model java.lang.Object
     */

    public Object getModelRepresentant(Object model)
    {
        return modelRepresentants.get(model);
    }

    /**
     * @param model java.lang.Object
     * @param representant java.lang.Object
     */

    protected void setModelRepresentant(Object model, Object representant)
    {
        modelRepresentants.put(model,representant);
    }
}


