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

package org.jacorb.security.util;

/**
 * This class represents key store entries
 *
 *	@author Gerald Brose, FU Berlin
 *	@version $Id$
 */

import java.util.*;
import javax.swing.tree.*;

public class CertNode
    implements KSNode
{
    /** if this entry represents a key entry or a trusted cert entry,
    this is its alias */

    private KeyNode parent;
    private iaik.x509.X509Certificate cert;

    /**
     * constructor for cert entries
     */

    public CertNode(iaik.x509.X509Certificate cert, int index)
    {
        this.cert = cert;
    }

    public iaik.x509.X509Certificate getCert()
    {
        return cert;
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
    return parent;
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

    public void setParent(MutableTreeNode parent)
    {
    this.parent = (KeyNode)parent;
    }

    public void setUserObject(Object o)
    {}


    public void removeFromParent()
    {
    parent.remove(this);
    parent = null;
    }

    public String toString()
    {
    return "subject: " + cert.getSubjectDN().getName() + " issuer: " + cert.getIssuerDN().getName();
    }

    public TreeNode[] getPath()
    {
    if( parent == null )
    {
        return null;
    }

    return new TreeNode[] { getParent().getParent(),  getParent() };
    }

}







