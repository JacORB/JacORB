/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1997-2000  Gerald Brose.
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

/**
 * 
 *	@author Gerald Brose, FU Berlin
 *	@version $Id$
 */

import java.awt.*;
import javax.swing.tree.*;
import javax.swing.event.*;
import javax.swing.*;
import org.omg.CosNaming.*;
import org.omg.CosNaming.NamingContextPackage.*;
import org.jacorb.naming.*;

public class NSTree
    extends JTree 
{
    public static final int MAX_BIND = 40;
    private NamingContextExt rootContext;
    private ContextNode rootNode;
    private Dimension size;
    private boolean created;

    public static NSTable nsTable;
    public NSTree(int width, int height, NSTable theTable, NamingContextExt rootCntxt)
    {
	DefaultMutableTreeNode root=new DefaultMutableTreeNode("RootContext");
	root.setAllowsChildren(true);
	setModel(new DefaultTreeModel(root,true));
	created=false;
	size=new Dimension(width,height);
	nsTable = theTable;
	rootContext=rootCntxt;
	ContextNode cn = new ContextNode(rootContext,(DefaultTreeModel)getModel());
	cn.setNode(root);
	root.setUserObject(cn);	
    }
    /**
     * Bind a new name context and insert it
     */

    public void bind(String name)
	throws NotFound,CannotProceed,InvalidName, AlreadyBound
    {
	TreePath path=null;
	int length=0;
	try 
	{ 
	    path=getSelectionPath(); 
	    length=path.getPathCount();
	}
	catch (Exception e)
	{
	    JOptionPane.showMessageDialog(this,"Nothing selected",
					  "Selection error",JOptionPane.ERROR_MESSAGE);
	    return;
	}
	DefaultMutableTreeNode node=(DefaultMutableTreeNode) getModel().getRoot();
	NamingContextExt context=rootContext;

	if (length>1)
	{
	    for (int i=1;i<length;i++)
	    {
		node=(DefaultMutableTreeNode) path.getPathComponent(i);
		ContextNode bind=(ContextNode) node.getUserObject();
		context=NamingContextExtHelper.narrow(context.resolve(bind.getName()));
		if( context == null )
		{
		    System.err.println("Naming context narrow failed!");
		    System.exit(1);
		}
	    }
	}
	if (node.getAllowsChildren())
	{
	    Name bindname=new Name(name);
	    if( context == null )
		System.err.println("context null ");

	    if( bindname.components() == null )
		System.err.println("name is null ");

	    context.bind_new_context(bindname.components());
	    update();
	}
	else 
	{
	    JOptionPane.showMessageDialog(this,"Please select a naming context",
					  "Selection error",JOptionPane.ERROR_MESSAGE);
	}
    }


    public Dimension getPreferredSize() 
    { 
	if (!created) 
	{ 
	    created=true; 
	    return size; 
	}
	else 
	    return super.getPreferredSize();
    }

    /**
     * unbind a context and remove it from this tree
     */
	
    public void unbind()
    {
	DefaultMutableTreeNode node;
	NamingContextExt context=rootContext;
	TreePath path=null;
	int length=0;
	try 
	{ 
	    path=getSelectionPath(); 
	    length=path.getPathCount();
	    if (length>1) 
	    {
		for (int i=1;i<length-1;i++)
		{
		    node=(DefaultMutableTreeNode) path.getPathComponent(i);
		    ContextNode bind=(ContextNode) node.getUserObject();
		    context=NamingContextExtHelper.narrow(context.resolve(bind.getName()));
		}
	    } 
	    if (length>0)
	    {
		node=(DefaultMutableTreeNode) path.getPathComponent(length-1);
		ContextNode binding=(ContextNode) node.getUserObject();
		context.unbind(binding.getName());
		DefaultTreeModel model=(DefaultTreeModel) getModel();
		model.removeNodeFromParent(node);
	    }
	}
	catch (Exception e) 
	{
	    JOptionPane.showMessageDialog(this,
					  "Nothing selected or invalid selection",
					  "Selection error",
					  JOptionPane.ERROR_MESSAGE);
	}
    }


    /**
     * update the entire tree of contexts
     */
	 
    public void update()
    {
	DefaultTreeModel model=(DefaultTreeModel) getModel();
	((ContextNode)((DefaultMutableTreeNode) model.getRoot()).getUserObject()).update();
	nsTable.update();
    }
}



