package org.jacorb.ir.gui.typesystem;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1997-2014 Gerald Brose / The JacORB Team.
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
        {  // for root it is null
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
            // we are root; our TreeNode was (or should have been) passed to the
            // constructor of DefaultTreeModel, so we no longer need to insert ourselves 
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
     * Constructs Tree for this Node.
     * Cannot be protected unfortunately, otherwise this method would not be visible even for
     * sub-classes in a sub-package.
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
		{   // as long as not everything is implemented, there are null entries
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
	// hack so you can see the nodes coming in right away
        if (this instanceof AbstractContainer)
        {
            DefaultMutableTreeNode treeNode =
                (DefaultMutableTreeNode)modelRepresentants.get(treeModel);
            ModelParticipant[] contents = ((AbstractContainer)this).contents();
            for (int i=0; i<contents.length; i++)
            {
                if (contents[i]!=null)
                {
		    // as long as not everything is implemented, there are null entries
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
