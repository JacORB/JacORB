/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1997-2001  Gerald Brose.
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

package org.jacorb.security.util;

/**
 * This class represents key store entries
 *
 *	@author Gerald Brose, FU Berlin
 *	@version $Id$
 */

import java.security.*;
import java.security.cert.*;
import java.util.*;
import javax.swing.tree.*;
import iaik.asn1.*;
import iaik.asn1.structures.*;
import iaik.x509.*;
import iaik.x509.extensions.*;

public class TrustNode 
    implements KSNode
{
    private String alias;
    private String label;   
    private TreeNode root;
    private KeyStore ks;
    private iaik.x509.X509Certificate cert;

    /**
     * constructor
     */

    public TrustNode(String alias, iaik.x509.X509Certificate cert, KeyStore ks)
    {
	this.alias = alias;
	this.cert = cert;
	this.ks = ks;
	label = "trusted: " + alias;
    }

    /* TreeNode interface: */

    public Enumeration children()
    {
	return null;
    }

    public boolean getAllowsChildren()
    {
	return false;
    }

    public TreeNode getChildAt(int index)
    {
	return null;
    }

    public int getChildCount()
    {
	return 0;
    }

    public int getIndex(TreeNode node)
    {
	return -1;
    }

    public TreeNode getParent()
    {
	return root;
    }

    public boolean isLeaf()
    {
	return true;
    }

    /* MutableTreeNode interface: */

    public void insert(MutableTreeNode child, int index)
    {
    }

    public void remove(int index)
    {
    }

    public void remove(MutableTreeNode child)
    {	
    }

    public void setParent(MutableTreeNode root)
    {
	this.root = root;
    }

    public void setUserObject(Object o)
    {}

    public void removeFromParent()
    {	
	try
	{
	    ks.deleteEntry( alias );
	}
	catch( java.security.KeyStoreException kse )
	{
	    kse.printStackTrace();
	}
    }   

    public iaik.x509.X509Certificate getCert()
    {
	return cert;
    }

    public String getAlias()
    {
	return alias;
    }

    public String toString()
    {	
	return label;
    }


    public void store()
    {
	try
	{
	    ks.setCertificateEntry ( alias, cert );
	}
	catch( java.security.KeyStoreException kse )
	{
	    kse.printStackTrace();
	}   
    }

}







