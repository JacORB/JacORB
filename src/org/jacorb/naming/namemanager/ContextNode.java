package org.jacorb.naming.namemanager;

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

import org.omg.CosNaming.*;
import org.jacorb.naming.*;
import org.jacorb.orb.*;
import org.jacorb.orb.iiop.*;

import java.util.*;
import javax.swing.tree.*;

/**
 *  @author Gerald Brose, FU Berlin
 *  @version $Id$
 */

public class ContextNode
{
    protected Binding[] contents;
    public boolean matched;
    public boolean used;

    public NamingContextExt context;
    private DefaultMutableTreeNode myDefaultNode;
    private DefaultTreeModel model;
    private Binding binding;
    private Vector bindingData;
    private String myName;

    public ContextNode( NamingContextExt context, DefaultTreeModel model )
    { 
	used = false;
	this.model = model;
	this.context = context;
    }

    public ContextNode( NamingContextExt context, 
                        Binding b,
                        DefaultTreeModel model)
    { 
	used = false;
	this.model = model;
	this.context = context;
	binding = b;
    }

    /**
     * 
     */

    public void display() 
    {
	update();
	if( bindingData != null )
	    NSTree.nsTable.setData( bindingData, this );
    }

    public boolean equals(ContextNode bnode)
    {
	return toString().equals(bnode.toString());
    }

    public NameComponent[] getName() 
    { 
	return binding.binding_name; 
    }

    /**
     * 
     * @param node javax.swing.tree.DefaultMutableTreeNode
     */

    public void setNode(DefaultMutableTreeNode node) 
    {
	this.myDefaultNode = node;
    }
	

    public String toString()
    {
	if( binding == null )
	    return "RootContext";
	else
	{
            if (myName == null )
            {
                NameComponent[] name = binding.binding_name;
                String kind = name[name.length-1].kind;
                myName =  name[name.length-1].id + 
                    ( kind != null && kind.length() > 0 ? "." + kind : "");
            }
            return myName;
	}
    }

    /**
     * 
     * @param name java.lang.String
     */

    public void unbind(NameComponent [] nc) 
	throws org.omg.CosNaming.NamingContextPackage.NotFound,
	org.omg.CosNaming.NamingContextPackage.CannotProceed,
	org.omg.CosNaming.NamingContextPackage.InvalidName
    {
	context.unbind(nc);
    }

    /**
     * update the content of this node and all its children
     */

    public synchronized void update()
    {
	try
	{
	    BindingListHolder blsoh = new BindingListHolder();
	    BindingIteratorHolder bioh = new BindingIteratorHolder();
	    ContextNode context_node;
	
	    context.list( NSTree.MAX_BIND, blsoh, bioh);
	    Binding[] bindings = blsoh.value;
	
	    int childCount = myDefaultNode.getChildCount();
	
            // set up lists of object bindings and subcontext bindings
	
	    int context_count = 0;
	    int object_count = 0;
				
	    for( int i = 0; i < bindings.length; i++ )
	    {				
		if( bindings[i].binding_type == BindingType.ncontext )
		    context_count++;
		else
		    object_count++;
	    }
	
	    ContextNode[] contexts = new ContextNode[context_count];
	    Binding[] objects = new Binding[object_count];
	
	    for( int i = 0; i < bindings.length; i++ )
	    {			
		if( bindings[i].binding_type == BindingType.ncontext)
		    contexts[ --context_count ] = 
			new ContextNode( NamingContextExtHelper.narrow( context.resolve(bindings[i].binding_name)), bindings[i], model );
		else
		    objects[ --object_count ] = bindings[i];
	    }
				
            // Compare this node's sub contexts and mark those found
            // in the list of context bindings as used
				
	    for( int i = 0; i < childCount; i++ )
	    {
		DefaultMutableTreeNode dmtn = 
                    (DefaultMutableTreeNode)myDefaultNode.getChildAt(i);
		context_node = (ContextNode)dmtn.getUserObject();
		for (int j = 0; j < contexts.length; j++)
		{	
		    if (context_node.equals(contexts[j]))
		    {
			context_node.matched = true;
			contexts[j].matched = true;
		    }
		}
	    }
	
            // Delete those child nodes that were not found in the
            // list
	
	    Vector removeList = new Vector();
	    for (int i = 0; i < childCount; i++)
	    {
		DefaultMutableTreeNode node = 
                    (DefaultMutableTreeNode)myDefaultNode.getChildAt(i);
		context_node = (ContextNode)node.getUserObject();
		if (!context_node.matched)
		{
		    removeList.addElement(node);
		}
		else 
		    context_node.matched=false;
	    }
	
	    int rsize = removeList.size();
	    for(int i = 0; i < rsize; i++)
	    {
		model.removeNodeFromParent((DefaultMutableTreeNode)removeList.elementAt(i));
	    }
	
            bindingData = new Vector();

            // Insert new context nodes found in the list as
            // children of this tree node

	    for (int i = 0; i < contexts.length; i++)
	    {
		if (!contexts[i].matched) 
		{
		    contexts[i].used=true;
	
		    DefaultMutableTreeNode node = new DefaultMutableTreeNode();
	
		    // tree node and context node need to know each other:
		    contexts[i].setNode(node);
		    node.setUserObject(contexts[i]);
		    node.setAllowsChildren(true);
		    model.insertNodeInto(node,myDefaultNode,0);
		}
		NameComponent last =
                    contexts[i].binding.binding_name[ contexts[i].binding.binding_name.length-1];
		NameComponent[] ncs = {last};

		org.jacorb.orb.ParsedIOR pior = null;
		try
		{
		    pior = 
                        ((org.jacorb.orb.Delegate)((org.omg.CORBA.portable.ObjectImpl)context.resolve(
		      		       			       ncs ))._get_delegate()).getParsedIOR();
		}
		catch( org.omg.CosNaming.NamingContextPackage.NotFound nf )
		{
		    // the named object could have disappeared from the 
		    // naming context in the meantime. If it has, we simply
		    // continue
		    continue;
		}
		Vector row = new Vector();

		row.addElement( last.id );
		row.addElement( last.kind);
		row.addElement( pior.getTypeId() );
		IIOPProfile p = (IIOPProfile)pior.getEffectiveProfile();
		row.addElement( p.getAddress().getIP() );
					
		bindingData.addElement( row );

	    }
	
	
	    for( int i = 0; i < objects.length; i++ )
	    {
		NameComponent last =
                    objects[i].binding_name[ objects[i].binding_name.length-1];
		NameComponent[] ncs = {last};
		org.jacorb.orb.ParsedIOR pior = null;
		try
		{
		    pior = 
                        ((org.jacorb.orb.Delegate)((org.omg.CORBA.portable.ObjectImpl)context.resolve(
		      		       			       ncs ))._get_delegate()).getParsedIOR();
		}
		catch( org.omg.CosNaming.NamingContextPackage.NotFound nf )
		{
		    // the named object could have disappeared from the 
		    // naming context in the meantime. If it has, we simply
		    // continue
		    continue;
		}
		Vector row = new Vector();

		row.addElement( last.id );
		row.addElement( last.kind);
		row.addElement( pior.getTypeId() );
		IIOPProfile p = (IIOPProfile)pior.getEffectiveProfile();
		row.addElement( p.getAddress().getIP() );
					
		bindingData.addElement( row );
	    }
	
            // recursively update child nodes
				
	    childCount = myDefaultNode.getChildCount();
	    for (int i = 0; i < childCount; i++ )
	    {
		DefaultMutableTreeNode dmtn = 
                    (DefaultMutableTreeNode)myDefaultNode.getChildAt(i);
		context_node = (ContextNode)dmtn.getUserObject();				
                //		Name name = new Name(bindings[i].binding_name);
		context_node.update();
	    }
	}
	catch (Exception e) 
	{
	    e.printStackTrace();
	}
    }
}

