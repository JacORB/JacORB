/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 2000-2003  Gerald Brose.
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
 * This class manages the tree view and model for key store 
 * entries. These are represented as KeyNodes, CertNodes and 
 * TrustNodes
 *
 * @author Gerald Brose, FU Berlin
 * @version $Id$
 */

import java.awt.*;
import javax.swing.tree.*;
import javax.swing.event.*;
import javax.swing.*;

import java.util.*;
import java.security.*;

public class KSEntryTree
{
    /** the root node */
    private DefaultMutableTreeNode root;

    /** the tree */
    private JTree tree;

    /** the pane the tree is displayed on */
    JScrollPane pane;

    /** our tree model */
    private DefaultTreeModel model;

    private KeyStore ks;

    public KSEntryTree()
    {     
    root = new DefaultMutableTreeNode("<empty>");
    model = new DefaultTreeModel( root );

    tree = new JTree( model );
    tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION) ;
    tree.setShowsRootHandles(true);
    tree.setVisible(true);
    pane = new JScrollPane( tree );
    }

    public JScrollPane getPane()
    {
    return pane;
    }

    public String listNodes()
    {
        String out = "";
        for( Enumeration e = root.children(); e.hasMoreElements(); )
        {
            KSNode node = (KSNode)e.nextElement();
            if(node instanceof KeyNode)
                out = out + "KeyNode " +((KeyNode)node).getAlias() + "\n";
            else if(node instanceof TrustNode)
                out = out + "TrustNode " +((TrustNode)node).getAlias() + "\n";
            else
                out = out + "unknown node type\n";
        }
        return out;
    }
    
    public KSNode getNode(String alias)
    {
        for( Enumeration e = root.children(); e.hasMoreElements(); )
        {
            KSNode node = (KSNode)e.nextElement();
            if( node instanceof KeyNode && ((KeyNode)node).getAlias().equals( alias ))
            return node;
            if( node instanceof TrustNode && ((TrustNode)node).getAlias().equals( alias ))
            return node;
        }
        return null;
    }
    
    /**
     * @param alias - the key alias
     * @return the KeyNode representing the key entry for the alias, null if not found
     */

    public KeyNode getKeyNode(String alias)
    {    
    for( Enumeration e = root.children(); e.hasMoreElements(); )
    {
        KSNode node = (KSNode)e.nextElement();
        if( node instanceof KeyNode && ((KeyNode)node).getAlias().equals( alias ))
        return (KeyNode)node;
    }
    return null;
    }

    /** 
     * load new keystore entries into the tree 
     *
     * @param ks - the key store.
     */

    public void load(KeyStore ks )
    {
    this.ks = ks;
    try
    {
        root.setUserObject("KeyStore");
        for( Enumeration aliases = ks.aliases(); aliases.hasMoreElements();)
        {
        String alias = (String) aliases.nextElement();
        if( ks.isKeyEntry( alias ))// || ks.isCertificateEntry ( alias ))
        {

System.out.println("bnv: iserted key entry for " + alias);
            java.security.cert.Certificate[] certChain = 
            ks.getCertificateChain( alias );

            KeyNode keyNode = new KeyNode( alias, null, null, ks);
            model.insertNodeInto( keyNode, root, root.getChildCount() );

            for( int i = 0; i < certChain.length; i++ )
            {
            CertNode child = 
                new CertNode((iaik.x509.X509Certificate)certChain[i],i);            
            model.insertNodeInto( child, keyNode, keyNode.getChildCount());
            }
            tree.scrollPathToVisible(new TreePath( new TreeNode[]{ root, keyNode }));
        } 
        else if( ks.isCertificateEntry( alias ))
        {
            iaik.x509.X509Certificate cert =
                ( iaik.x509.X509Certificate )ks.getCertificate ( alias );

            model.insertNodeInto( new TrustNode( alias, cert, ks ), root, root.getChildCount() );
        }
        }
    }
    catch( Exception e)
    {
        e.printStackTrace();
    }
    }

    /** 
     * Add a new keystore entry to the tree, in this case a new 
     * key alias
     * @param alias - the alias for the entry
     */

    public void addKey(String alias, java.security.PrivateKey key, char [] password)
    {
    if( containsAlias( alias ))
        return;

    KeyNode child = 
        new KeyNode(new String(alias), key, password, ks);
    model.insertNodeInto(child, root, root.getChildCount());
    tree.scrollPathToVisible(new TreePath( new TreeNode[]{ root, child }));
    }

    /** 
     * Add a new keystore entry, in this case add a trusted certificate 
     * @param alias - the alias for the entry
     * @param cert - the trusted certificate
     */

    public void addTrustedCert(String alias, java.security.cert.Certificate cert)
    {
    if( containsAlias( alias ))
        return;

    TrustNode child = 
        new TrustNode(new String(alias), (iaik.x509.X509Certificate)cert, ks);
    model.insertNodeInto(child, root, root.getChildCount());
    tree.scrollPathToVisible(new TreePath(new TreeNode[]{ root, child }));
    }

    /** 
     * Add a new certificate in the entry ofr alias
     * @param alias - the alias for the entry
     * @param idx - index of the certificate in the chain of certificates of alias,
     *              if idx = -1, cert will be appended to the end of the chain
     * @param cert - a new certificate for alias
     */

    public void addCert(String alias, int idx, java.security.cert.Certificate cert)
    {
    for( Enumeration e = root.children(); e.hasMoreElements(); )
    {
        MutableTreeNode node = (MutableTreeNode)e.nextElement();
        if( node instanceof KeyNode && ((KeyNode)node).getAlias().equals( alias ))
        {
        if( idx == -1 )
            idx = node.getChildCount();
        CertNode child = new CertNode((iaik.x509.X509Certificate)cert, idx );
        model.insertNodeInto(child, node, idx);
        tree.scrollPathToVisible(new TreePath(child.getPath()));
        return;
        }
    }
    throw new RuntimeException("Alias " + alias + " not found!");
    }

    /** 
     * Add a new certificate in the entry ofr alias
     * @param alias - the alias for the entry
     * @param certs - a new certificate chain for alias
     */

    public void addCerts( String alias, java.security.cert.Certificate [] certs )
    {
    for( Enumeration e = root.children(); e.hasMoreElements(); )
    {
        MutableTreeNode node = (MutableTreeNode)e.nextElement();
        if( node instanceof KeyNode && ((KeyNode)node).getAlias().equals( alias ))
        {
        CertNode child;
        for ( int i = 0; i < certs.length; i ++ ) {
            child = new CertNode((iaik.x509.X509Certificate)certs [ i ], i );
            model.insertNodeInto(child, node, i);
            if ( i == ( certs.length - 1 ))
                tree.scrollPathToVisible(new TreePath(child.getPath()));
        }
        return;
        }
    }
    throw new RuntimeException("Alias " + alias + " not found!");
    }
    
    /** 
     * Remove all nodes from the tree. 
     */

    public void clean()
    {
    root.removeAllChildren();
    root.setUserObject("<empty>");
    model.reload();
    }
     
    /** 
     * @return the key store alias for the selected node.
     */

    public String getSelectedAlias()
    {
    TreePath path = tree.getSelectionPath();
    if( path != null)
    { 
        if( path.getLastPathComponent() instanceof KeyNode )
        return ((KeyNode)path.getLastPathComponent()).getAlias();
        else if( path.getLastPathComponent() instanceof TrustNode )
        return ((TrustNode)path.getLastPathComponent()).getAlias();
    }
    return null;
    }

    /** 
     * @return the key store alias for the selected node.
     */

    public KSNode getSelectedNode()
    {
    TreePath path = tree.getSelectionPath();
    if( path != null )
    {
        return (KSNode)path.getLastPathComponent();    
    }
    else
    {
        return null;
    }
    }

    /** 
     * Remove the selected node.
     *
     * @return the key store alias for the selected node.
     */

    public String removeSelectedNode()
    {
    TreePath path = tree.getSelectionPath();
    if( path != null )
    {
        MutableTreeNode currentNode = (MutableTreeNode)path.getLastPathComponent();
        MutableTreeNode parent = (MutableTreeNode)currentNode.getParent();
        if( parent != null )
        {
        String date = (String)currentNode.toString();
        model.removeNodeFromParent(currentNode);
        return date;
        }
    }
    return null;
    }

    /** 
     * Remove the childs from KeyNode.
     * @param alias - the alias for the entry
     */

    public void removeCerts( String alias )
    {
        for( Enumeration e = root.children(); e.hasMoreElements(); )
        {
            MutableTreeNode node = (MutableTreeNode)e.nextElement();
            if( node instanceof KeyNode && ((KeyNode)node).getAlias().equals( alias ))
            {
                KeyNode parent = (KeyNode)node;
                CertNode child;
                for ( int i = parent.getChildCount (); i > 0; i-- )
                {
                    child = (CertNode)parent.getChildAt ( i - 1 );
                    model.removeNodeFromParent ( child );
                }
                return;
            }
        }
        throw new RuntimeException("Alias " + alias + " not found!");
    }

    public void reload()
    {
        model.reload(root);
    }

    public Enumeration getNodes()
    {
        return root.children();
    }


    public boolean isTrusted(iaik.x509.X509Certificate check)
    {
    for( Enumeration e = getNodes(); e.hasMoreElements(); )
    {
        KSNode node = (KSNode)e.nextElement();
        if( node instanceof TrustNode && ((KSNode)node).getCert().equals(check))
        return true;        
    }
    return false;
    }

    public boolean containsAlias(String alias)
    {
    for( Enumeration e = root.children(); e.hasMoreElements(); )
    {
        MutableTreeNode node = (MutableTreeNode)e.nextElement();
        if( node instanceof KeyNode && ((KeyNode)node).getAlias().equals( alias ) ||
        node instanceof TrustNode && ((TrustNode)node).getAlias().equals( alias )
        ) 
        return true;
    }
    return false;
    }

}










