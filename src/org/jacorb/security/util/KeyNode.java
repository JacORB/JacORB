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

public class KeyNode 
    implements KSNode
{
    /** if this entry represents a key entry or a trusted cert entry, 
	this is its alias */

    private String alias;
    private java.security.PrivateKey key;
    private char[] password;
    Vector children; /* certificate Nodes */
    private String label;   
    private String keytype = "<unknown key type>";
    private TreeNode root = null;
    private KeyStore ks;

    private boolean dirty = false;

    /**
     * constructor for key entries
     */

    public KeyNode(String alias, java.security.PrivateKey key, char[] password, KeyStore ks)
    {
	this.alias = alias;
	this.key = key;
	this.password = password;
	this.ks = ks;
	children = new Vector();
	if( key != null ) 
	{
	    if( key instanceof java.security.interfaces.RSAPrivateKey ||
		key instanceof iaik.security.rsa.RSAPrivateKey)
		keytype = "RSA";
	    else if( key instanceof java.security.interfaces.DSAPrivateKey ||
		     key instanceof iaik.security.dsa.DSAPrivateKey)
		keytype = "DSA";
	    else
		keytype = key.getClass().getName();
	} 
	else 
	{
	    try 
	    {
		    java.security.cert.Certificate cert = ks.getCertificate ( alias );
		    java.security.PublicKey pubKey = cert.getPublicKey ();
		    if( pubKey instanceof java.security.interfaces.RSAPublicKey ||
			pubKey instanceof iaik.security.rsa.RSAPublicKey)
			keytype = "RSA";
		    else if( pubKey instanceof java.security.interfaces.DSAPublicKey ||
			     pubKey instanceof iaik.security.dsa.DSAPublicKey)
			keytype = "DSA";
		    else
			keytype = pubKey.getClass().getName();
	    } 
	    catch ( Exception ex ) {}
	} 
	label = alias + " (" + keytype + " key)";
    }

    public iaik.x509.X509Certificate getCert()
    {
	return ((CertNode)children.elementAt(0)).getCert();
    }

    public iaik.x509.X509Certificate[] getCertificateChain()
    {
	iaik.x509.X509Certificate[] certs = 
	    new iaik.x509.X509Certificate[getChildCount()];

	int ci = 0;
	for( Enumeration e = children(); e.hasMoreElements(); ci++ )
	{
	    CertNode certNode = (CertNode)e.nextElement();
	    certs[ci] = certNode.getCert();
	}
	return certs;
    }


     /* MutableTreeNode interface: */

    public void insert(MutableTreeNode child, int index)
    {
	if( !checkAccess())
	    return;	

	children.insertElementAt(child, index);
	child.setParent( this );
	dirty = true;
    }

    public void remove(int index)
    {
	if( !checkAccess())
	    return;	

	children.removeElementAt(index);
	dirty = true;
    }

    public void remove(MutableTreeNode child)
    {	
	if( !checkAccess())
	    return;	
	
	children.remove(child);
	dirty = true;
    }


    public void setParent(MutableTreeNode root)
    {
	this.root = root;
    }

    public void setUserObject(Object o)
    {}

    /* TreeNode interface: */

    public Enumeration children()
    {
	return children.elements();
    }

    public boolean getAllowsChildren()
    {
	return true;
    }

    public TreeNode getChildAt(int index)
    {
	return (TreeNode)children.elementAt(index);
    }

    public int getChildCount()
    {
	return children.size();
    }

    public int getIndex(TreeNode node)
    {
	return children.indexOf(node);
    }

    public TreeNode getParent()
    {
	return root;
    }
    
    public String getAlias()
    {
	return alias;
    }

    private boolean charsEqual ( char [] a1, char [] a2 )
    {
    	if ( a1.length != a2.length ) return false;
    	else for ( int i = 0; i < a1.length; i++ )
    		if ( a1[ i ] != a2[ i ] ) return false;
    	return true;
    }

    public void setKey(java.security.PrivateKey key, char[] password) // bnv: added password param
    {
	if ( this.password == null ) 
	    this.key = key;
	else if ( charsEqual ( this.password, password )) 
	{ 
	    this.key = key; this.password = password; 
	}
	else 
	    return;
	if( key != null )
	{
	    if( key instanceof java.security.interfaces.RSAPrivateKey ||
		key instanceof iaik.security.rsa.RSAPrivateKey)
		keytype = "RSA";
	    else if( key instanceof java.security.interfaces.DSAPrivateKey ||
		     key instanceof iaik.security.dsa.DSAPrivateKey)
		keytype = "DSA";
	    else
		keytype = key.getClass().getName();
	    label = alias + " (" + keytype + " key)";
	}
    }

    public java.security.PrivateKey getKey( char[] password )
    { 
	// bnv
	if (( this.password == null ) || 
	    ! ( charsEqual ( this.password, password ))) 
	{
	    System.out.println ( "coucou getKey wrong password >" + this.password + "<>" + password + "<" );
	    return null;
	} 
	else 
	    return key;
    }

    java.security.PrivateKey getKey()
	throws IllegalAccessException
    {
	if( !checkAccess())
	    throw new IllegalAccessException("Wrong password.");

	return key;
    }


    public boolean isLeaf()
    {
	return false;
    }

    /**
     * @returns true if successful
     */

    public void removeFromParent()
    {
	if( !checkAccess())
	    return;	

	try
	{
	    ks.deleteEntry( alias );
	}
	catch( java.security.KeyStoreException kse )
	{
	    kse.printStackTrace();
	}
    }


    public void store()
    {
	if( !dirty || !checkAccess())
	    return;
	
	iaik.x509.X509Certificate[] chain = 
	    new iaik.x509.X509Certificate[children.size()];
	
	for( int i = 0; i < chain.length; i++)
	{
	    chain[i] = ((CertNode)children.elementAt(i)).getCert();
	}

	try
	{
	    ks.setKeyEntry( alias, key, password, chain );       
	    dirty = false;
	}
	catch( KeyStoreException kse )
	{
	    kse.printStackTrace();
	}
    }

    public boolean checkAccess()
    {
	if( key != null && password != null )
	    return true;

	try	   
	{
	    password = UserSponsor.getPasswd("Please enter password for key alias " + alias );
	    if( password != null )
	    {
		key = (java.security.PrivateKey)ks.getKey( alias, password );
		return true;
	    }
	    else
		return false;
	}
	catch( java.security.UnrecoverableKeyException kse )
	{
	    kse.printStackTrace();
	    return false;
	}
	catch(java.security.NoSuchAlgorithmException kse )
	{
	    kse.printStackTrace();
	    return false;
	}
	catch( java.security.KeyStoreException kse )
	{
	    kse.printStackTrace();
	    return false;
	}	
    }


    public String toString()
    {
	if( label.indexOf("(") > 0 )
	{
	    if( key != null )
	    {
		if( key instanceof java.security.interfaces.RSAPrivateKey ||
		    key instanceof iaik.security.rsa.RSAPrivateKey)
		    keytype = "RSA";
		else if( key instanceof java.security.interfaces.DSAPrivateKey ||
			 key instanceof iaik.security.dsa.DSAPrivateKey)
		    keytype = "DSA";
		else
		    keytype = key.getClass().getName();
	    }
	    label = alias + " (" + keytype + " key)";
	}
	return label;
    }

}

