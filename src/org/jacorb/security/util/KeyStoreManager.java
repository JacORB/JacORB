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
 * This class manages a key store
 *
 * @author Gerald Brose, FU Berlin
 * @version $Id$
 */

import java.security.*;
import java.security.cert.*;
import java.io.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;
import javax.swing.tree.*;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;
import java.awt.*;
import java.awt.event.*;

import java.math.BigInteger;
import java.util.*;

import iaik.asn1.*;
import iaik.asn1.structures.*;
import iaik.x509.*;
import iaik.x509.extensions.*;

public class KeyStoreManager
    extends JFrame
{
    /** the password for managing the key store */
    private char[] ksPassword;

    /** the current key store */
    private KeyStore ks;

    /** the current key store's file name */
    private String ksFileName;

    private boolean done = false;

    /** whether the current key store has been modified */
    private boolean dirty = false;

    /** the tree represinting the key store contents */
    private KSEntryTree tree;

    /** our handler */
    private EventHandler actionHandler;

    // file menu items
    JMenuItem openMenuItem;
    JMenuItem newMenuItem;
    JMenuItem saveMenuItem;
    JMenuItem saveAsMenuItem;
    JMenuItem closeMenuItem;
    JMenuItem exitMenuItem;

    /** key menu items */
    JMenuItem generateKeyMenuItem;
    JMenuItem deleteKeyMenuItem;
    JMenuItem verifyChainMenuItem;

    /** trustee menu items */
    JMenuItem addTrusteeMenuItem;
    JMenuItem deleteTrusteeMenuItem;

    /** cert menu items */
    JMenuItem createCertMenuItem;
    JMenuItem signMenuItem;
    JMenuItem exportCertMenuItem;
    JMenuItem importCertMenuItem;
    JMenuItem deleteCertMenuItem;

    JFileChooser chooser;
    JPanel topPanel;

    public KeyStoreManager()
    {
	super("KeyStoreManager");
	actionHandler = new EventHandler();
	chooser = new JFileChooser();

	try 
	{
	    UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
	} 
	catch (Exception e) 
	{}

	// Build menu bar
	JMenuBar menuBar = new JMenuBar();
	setJMenuBar(menuBar);

	// file menu

	JMenu fileMenu = new JMenu("File");
	menuBar.add(fileMenu);

	openMenuItem = new JMenuItem("Open...");
	openMenuItem.addActionListener(actionHandler);
	fileMenu.add(openMenuItem);

	newMenuItem = new JMenuItem("New");
	newMenuItem.addActionListener(actionHandler);
	fileMenu.add(newMenuItem);

	saveMenuItem = new JMenuItem("Save");
	saveMenuItem.addActionListener(actionHandler);
	fileMenu.add(saveMenuItem);

	saveAsMenuItem = new JMenuItem("SaveAs...");
	saveAsMenuItem.addActionListener(actionHandler);
	fileMenu.add(saveAsMenuItem);

	closeMenuItem = new JMenuItem("Close");
	closeMenuItem.addActionListener(actionHandler);
	fileMenu.add(closeMenuItem);

	exitMenuItem = new JMenuItem("Exit");
	exitMenuItem.addActionListener(actionHandler);
	fileMenu.add(exitMenuItem);

	// keys menu

	JMenu keyMenu = new JMenu("Keys");
	menuBar.add(keyMenu);

	generateKeyMenuItem = new JMenuItem("New...");
	generateKeyMenuItem.addActionListener(actionHandler);
	keyMenu.add(generateKeyMenuItem);	

	deleteKeyMenuItem = new JMenuItem("Delete");
	deleteKeyMenuItem.addActionListener(actionHandler);
	keyMenu.add(deleteKeyMenuItem);	

	verifyChainMenuItem = new JMenuItem("Verify Chain");
	verifyChainMenuItem.addActionListener(actionHandler);
	keyMenu.add(verifyChainMenuItem);	

	// trustees menu

	JMenu trusteeMenu = new JMenu("Trustees");
	menuBar.add(trusteeMenu);

	addTrusteeMenuItem = new JMenuItem("add...");
	addTrusteeMenuItem.addActionListener(actionHandler);
	trusteeMenu.add(addTrusteeMenuItem);	

	deleteTrusteeMenuItem = new JMenuItem("Delete");
	deleteTrusteeMenuItem.addActionListener(actionHandler);
	trusteeMenu.add(deleteTrusteeMenuItem);	

	// certs menu

	JMenu certMenu = new JMenu("Certificates");
	menuBar.add(certMenu);

	createCertMenuItem = new JMenuItem("Create");
	createCertMenuItem.addActionListener(actionHandler);
	certMenu.add(createCertMenuItem);	

	exportCertMenuItem = new JMenuItem("Export");
	exportCertMenuItem.addActionListener(actionHandler);
	certMenu.add(exportCertMenuItem);	

	importCertMenuItem = new JMenuItem("Import");
	importCertMenuItem.addActionListener(actionHandler);
	certMenu.add(importCertMenuItem);	

	deleteCertMenuItem = new JMenuItem("Delete");
	deleteCertMenuItem.addActionListener(actionHandler);
	certMenu.add(deleteCertMenuItem);	

	tree=new KSEntryTree();
	JScrollPane treeScrollPane = tree.getPane();
		
	getContentPane().setBackground(Color.white);
	getContentPane().add(treeScrollPane);

	pack();
	setVisible(true);
    }

    /**
     * @returns false, if we have to cancel the operation, true otherwise
     */

    public boolean close()
    {
	if( ks == null )
	    return true;

	if( dirty )
	{
	    int option = JOptionPane.showConfirmDialog(null, 
						       "KeyStore modified, save changes?",
						       "Close",
						       JOptionPane.YES_NO_CANCEL_OPTION,
						       JOptionPane.INFORMATION_MESSAGE);
	    if( option == JOptionPane.CANCEL_OPTION)
		return false;
	    if( option == JOptionPane.YES_OPTION)
		save();

	}
	ks = null;
	ksPassword = null;
	tree.clean();	    
	repaint();
	return true;
    }

    private void _exit()
    {
	if( close()) 
	    System.exit(0);
    }

    private void load()
    {
	if( ks != null )
	{
	    if( !close())
		return;
	}

	int returnVal = chooser.showOpenDialog(this);
	if( returnVal == JFileChooser.APPROVE_OPTION )
	    ksFileName = chooser.getSelectedFile().getAbsolutePath();
	else
	    return;

	ksPassword =  UserSponsor.getPasswd("Password for KeyStore " + ksFileName);

	try
	{
	    setCursor(java.awt.Cursor.getPredefinedCursor( java.awt.Cursor.WAIT_CURSOR ));
	    ks = KeyStoreUtil.getKeyStore( ksFileName,ksPassword );
	    setCursor(java.awt.Cursor.getDefaultCursor());
	}
	catch( Exception e )
	{
	    JOptionPane.showMessageDialog(null, 
					  e.getClass().getName() + ":" + e.getMessage(),
					  "Exception", 
					  JOptionPane.ERROR_MESSAGE);
	    setCursor(java.awt.Cursor.getDefaultCursor());
	    return;
	} 

	setCursor(java.awt.Cursor.getPredefinedCursor( java.awt.Cursor.WAIT_CURSOR ));
	tree.load( ks);
	repaint();
	setCursor(java.awt.Cursor.getDefaultCursor());
	dirty = false;
    }

    private void save()
    {
	if( ks == null || !dirty )
	    return;

	if( ksFileName == null )
	    saveAs();
	else
	{
	    try
	    {
		setCursor(java.awt.Cursor.getPredefinedCursor( java.awt.Cursor.WAIT_CURSOR ));
		FileOutputStream out = new FileOutputStream(ksFileName);
		for( Enumeration e = tree.getNodes() ; e.hasMoreElements(); )
		{
		    TreeNode node = (TreeNode)e.nextElement();
		    if( node instanceof KeyNode )
			((KeyNode)node).store();
		    else if ( node instanceof TrustNode )
			((TrustNode)node).store();
		}
		    
		ks.store( out, ksPassword );
	    }
	    catch( Exception io )
	    {
		io.printStackTrace();
	    }
	    dirty = false;
	    setCursor(java.awt.Cursor.getDefaultCursor());
	}
    }

    private void saveAs()
    {
	if( ks == null || !dirty )
	    return;

	int returnVal = chooser.showSaveDialog(this);
	if( returnVal == JFileChooser.APPROVE_OPTION )
	    ksFileName = chooser.getSelectedFile().getAbsolutePath();
	else
	    return;

	if( ksPassword == null )
	{
	    String passwd = JOptionPane.showInputDialog( "Please enter Password to save KeyStore");
	    if( passwd != null )
		ksPassword =  passwd.toCharArray();
	    else
		return;
	}
	setCursor(java.awt.Cursor.getPredefinedCursor( java.awt.Cursor.WAIT_CURSOR ));
	try
	{
	    FileOutputStream out = new FileOutputStream(ksFileName);
	    for( Enumeration e = tree.getNodes() ; e.hasMoreElements(); )
	    {
		TreeNode node = (TreeNode)e.nextElement();
		if( node instanceof KeyNode )
		    ((KeyNode)node).store();
		else if ( node instanceof TrustNode )
		    ((TrustNode)node).store();
	    }
	    ks.store( out, ksPassword );
	    setCursor(java.awt.Cursor.getDefaultCursor());
	}
	catch( Exception e )
	{
	    e.printStackTrace();
	}
	setCursor(java.awt.Cursor.getDefaultCursor());
	dirty = false;
    }

    private void newFile()
    {
	if( ks != null )
	{
	    if( !close())
		return;
	}

	setCursor(java.awt.Cursor.getPredefinedCursor( java.awt.Cursor.WAIT_CURSOR ));
	try
	{
	    try 
	    {
	    	ks = KeyStore.getInstance( "IAIKKeyStore", "IAIK" );
	    } 
	    catch ( java.security.NoSuchProviderException ex ) 
	    {
	    	System.err.println ( ex.toString ());
	    	ks = KeyStore.getInstance("jks");
	    }
	    ks.load( null, new char[0] );
	} 
	catch( Exception e )
	{
	    showException( e );
	    return;
	}

	tree.load(ks);
	repaint();
	setCursor(java.awt.Cursor.getDefaultCursor());
	dirty = false;
    }

    private void exportCert(java.security.cert.Certificate cert)
    {
	if( cert == null )
	    return;
	JFileChooser exporter = new JFileChooser();
	exporter.setDialogTitle("Export Certificate to File");
	exporter.setApproveButtonText("Export");

	int returnVal = exporter.showSaveDialog(this);
	if( returnVal == JFileChooser.APPROVE_OPTION )
	{
	    String fileName = exporter.getSelectedFile().getAbsolutePath();
	    try
	    {
		FileOutputStream out = new FileOutputStream(fileName);
		if( out == null )
		    throw new IOException("File " + fileName + " not found.");
		out.write(cert.getEncoded());
		out.close();
	    }
	    catch( Exception e )
	    {
		e.printStackTrace();
	    }
	}
    }


    private void exportCert()
    {
	java.security.cert.Certificate cert = null;
	try
	{	    
	    KSNode node = tree.getSelectedNode();
            
            if( node != null )
            {
                exportCert( node.getCert() );
            }
	}
	catch ( Exception e )
	{
	    showException( e );
	    return;
	}
    }


    private void createCert()
    {
	if( ks == null )
	    return;

	try
	{
	    iaik.x509.X509Certificate cert;
	    String targetAlias = tree.getSelectedAlias();
	    
	    Vector aliasVector = new Vector();
	    for( Enumeration aliasCandidates= tree.getNodes(); aliasCandidates.hasMoreElements();)
	    {
		TreeNode tn = (TreeNode)aliasCandidates.nextElement();
		if( tn instanceof KeyNode)
		    aliasVector.add( ((KeyNode)tn).getAlias());
	    }

	    String signerAliases[] = new String[aliasVector.size()];
	    for( int i = 0; i < signerAliases.length; i++)
		signerAliases[i]= (String)aliasVector.elementAt(i);

	    String subjectAliases[];

	    if( targetAlias != null )
		subjectAliases = new String[] { targetAlias };
	    else
		subjectAliases = signerAliases;

	    UserSponsor us = new UserSponsor("Create Certificate",
					     "Please select Certificate parameters", 
					     new String[]{"Role Name\n(only needed for role certs)"}, 
					     new String[]{"Subject Key Alias","Signer Key Alias","Certificate Type"}, 
					     new String[][]{subjectAliases, signerAliases, {"Public Key Cert", "Role Cert"}},
					     new String[]{"Password of signer\nprivate key"}
					     );
	    String[] paramHolder = new String[1];
	    String[] typeHolder = new String[3];
	    char[][] passwordHolder= new char[1][];

	    if( us.getInput( paramHolder, typeHolder, passwordHolder))
	    {
		setCursor(java.awt.Cursor.getPredefinedCursor( java.awt.Cursor.WAIT_CURSOR ));

		String value = null;
		String signerAlias = null;
		String certType = null;
		char[] password = new char[0];			
		
		if( paramHolder[0] != null )
		    value = paramHolder[0];
		
		if( targetAlias == null && typeHolder[0] != null )
		    targetAlias = typeHolder[0];

		if( typeHolder[1] != null )
		    signerAlias = typeHolder[1];

		if( typeHolder[2] != null )
		    certType = typeHolder[2];
		
		if( signerAlias == null || signerAlias.length() == 0 )
		    throw new IllegalArgumentException("No alias for key entry!");

		// get selected alias or new alias name and a password
		if( passwordHolder[0] != null )
		    password = passwordHolder[0];
		
		KeyNode snode = ( KeyNode )(tree.getNode(signerAlias)); // bnv: key might not yet be in ks
		KSNode tnode = tree.getNode(targetAlias);

		System.out.println ( "signerAlias is " + signerAlias );
		java.security.PrivateKey signPrivKey = snode.getKey ( password );

		if( certType.equals("Role Cert"))
		{
System.out.println("creating role cert");

		    if ( signPrivKey == null ) 
		    {
			System.out.println ( "signPrivKey is null" );
			java.security.KeyPair kp = KeyStoreUtil.getKeyPair(ks, signerAlias, password);
			signPrivKey = kp.getPrivate();
			snode.setKey ( signPrivKey, password );
		    }
		    iaik.x509.X509Certificate pubKeyCert = tnode.getCert();
		    java.security.PublicKey subjectPubKey = pubKeyCert.getPublicKey();

		    cert = CertUtils.certifyRoleMembership(value,
							   CertUtils.createName(targetAlias),
							   CertUtils.createName(signerAlias),
							   subjectPubKey,
							   signPrivKey 
							   );
		    // bnv: will build a new certificate chain for targetAlias
System.out.println("bnv: will build a new certificate chain for targetAlias");
		    java.security.cert.Certificate [] signerCerts = snode.getCertificateChain ();
		    iaik.x509.X509Certificate[] certs = 
			new iaik.x509.X509Certificate [ 1 + signerCerts.length ];
		    certs[ 0 ] = cert;
		    for ( int i = 1; i <= signerCerts.length; i++ ) {
		    	certs[ i ] = ( iaik.x509.X509Certificate )signerCerts[ i - 1 ];
		    }
		    // ks.setKeyEntry( targetAlias, ks.getKey(targetAlias, password),password, certs);
		    // bnv: old certificate chain for targetAlias is removed and new one inserted
System.out.println("bnv: old certificate chain for targetAlias is removed and new one inserted");
		    tree.removeCerts ( targetAlias );
		    tree.addCerts ( targetAlias, certs );

		    repaint ();
		    // exportCert( cert );
		    
		}
		else if( certType.equals("Public Key Cert"))
		{
System.out.println("creating public key cert");

		    cert = CertUtils.createPublicKeyCert ( CertUtils.createName(targetAlias),
							   CertUtils.createName(signerAlias),
							   tnode.getCert().getPublicKey(),
							   signPrivKey
							 );
		    

		    iaik.x509.X509Certificate[] certs = new iaik.x509.X509Certificate [ 2 ];
		    certs[ 0 ] = cert;
		    certs[ 1 ] = (iaik.x509.X509Certificate)snode.getCert();
		    if( tnode instanceof TrustNode )
		    {
			// act like a CA
			// ks.setCertificateEntry( targetAlias, cert );
			exportCert(cert);
		    }
		    else
		    {
			tree.addCert( targetAlias, -1, certs[0] );
			tree.addCert( targetAlias, -1, certs[1] );
		    }
		    repaint ();
		    // exportCert(pkCert);

		}
		else
		{
		    throw new IllegalArgumentException("Unknown Certificate Type: " + certType);
		}
	    }
	}
	catch ( Exception e )
	{
	    showException( e );
	}
	setCursor(java.awt.Cursor.getDefaultCursor());
    }

    public void importCert()
    {
	if( ks == null )
	    return;

	String fileName = null;
	String alias = null;
 
	MutableTreeNode node = tree.getSelectedNode();
	if( node == null ) 
	    return;

	if( node instanceof CertNode )
	    return;

	if( node instanceof KeyNode )
	{
	    alias = ((KeyNode)node).getAlias();
	    ((KeyNode)node).checkAccess();
	}
	
	if( alias == null )
	    return;

	//  char[] password = null;
//  	char[][] passwordHolder = new char[1][];

//  	UserSponsor us = new UserSponsor("Password", 
//  					 "Please enter password for key alias " + alias , 
//  					 null, new String[]{"Password"}
//  					 );
//  	us.getInput( null, passwordHolder);
      
//  	if( passwordHolder[0] != null )
//  	    password = passwordHolder[0];
//  	else
//  	    return;

	int returnVal = chooser.showOpenDialog(this);

	if( returnVal == JFileChooser.APPROVE_OPTION )
	    fileName = chooser.getSelectedFile().getAbsolutePath();
	else
	    return;

	try
	{
	    setCursor(java.awt.Cursor.getPredefinedCursor( java.awt.Cursor.WAIT_CURSOR ));
	    FileInputStream in = new  FileInputStream(fileName);

	    java.security.cert.CertificateFactory certFactory = 
		java.security.cert.CertificateFactory.getInstance("X.509", "IAIK");
	
	    iaik.x509.X509Certificate cert =
		(iaik.x509.X509Certificate)certFactory.generateCertificate(in);	    

	    //	    java.security.cert.Certificate[] oldCerts = ks.getCertificateChain(alias);

	    iaik.x509.X509Certificate[] oldCerts = ((KeyNode)node).getCertificateChain();

	    int ci = 0;
	    for( Enumeration e = ((KeyNode)node).children(); e.hasMoreElements(); ci++ )
	    {
		CertNode certNode = (CertNode)e.nextElement();
		oldCerts[ci] = certNode.getCert();
	    }

	    iaik.x509.X509Certificate[] newCerts = 
		new iaik.x509.X509Certificate[(oldCerts != null ? oldCerts.length+1: 1 )];

	    for( int i = 0; i < oldCerts.length; i++ )
		newCerts[i] = oldCerts[i];

	    newCerts[newCerts.length-1] = cert;

	    java.security.PrivateKey pk = 
		(java.security.PrivateKey)tree.getKeyNode(alias).getKey();

	    tree.addCert( alias, newCerts.length-1, cert );

	    dirty = true;
	    setCursor(java.awt.Cursor.getDefaultCursor());
	}
	catch( Exception e )
	{
	    showException(e);
	}

	repaint();
    }


    /*
     * Imports a certificate from file; the user is asked for the file location.
     *
     * @author Andre Benvenuti, GST Bern
     * @returns - the certificate stored in the file.
     */

    public iaik.x509.X509Certificate importCertificate()
    {       
	String fileName = null;
	int returnVal = chooser.showOpenDialog ( this );
	if ( returnVal == JFileChooser.APPROVE_OPTION )
	    fileName = chooser.getSelectedFile ().getAbsolutePath ();
	else
	    return null;

	try 
	{
	    FileInputStream in = new  FileInputStream ( fileName );

	    java.security.cert.CertificateFactory certFactory = 
		java.security.cert.CertificateFactory.getInstance ( "X.509", "IAIK" );
	
	    iaik.x509.X509Certificate cert =
		( iaik.x509.X509Certificate )certFactory.generateCertificate ( in );
	    cert.verify( cert.getPublicKey ());
	    return cert;
	} 
	catch ( Exception e ) 
	{
	    showException ( e );
	    return null;
	}
    }


    /*
     * Imports a certificate chain from file; the user is asked for the file location.
     *
     * The certificate of the user is the first one in the list
     * and the top level certificate is the last one.
     * chain[0] = user certificate signed issuer1
     * chain[1] = issuer1 certificate signed issuer2
     * ...
     * chain[n] = self signed CA certificate
     *
     * @author Andre Benvenuti, GST Bern
     * @returns - true if we can verify all the certificates in the chain
     * and if CA is a trusted signer.
     *
     * @param chain the certificate chain to verify
     * @param keyStore - the keyStore to search for trusted signers
     */
    /*
    public iaik.x509.X509Certificate [] importCertificateChain()
    {
	String fileName = null;

	int returnVal = chooser.showOpenDialog(this);
	if( returnVal == JFileChooser.APPROVE_OPTION )
	    fileName = chooser.getSelectedFile().getAbsolutePath();
	else
	    return null;

	try
	{
	    FileInputStream in = new  FileInputStream(fileName);

	    java.security.cert.CertificateFactory certFactory = 
		java.security.cert.CertificateFactory.getInstance( "X.509" );
	
	    Collection c = certFactory.generateCertificates ( in );
	    iaik.x509.X509Certificate [] certs = 
		new iaik.x509.X509Certificate [ c.size () ];
	    Iterator i = c.iterator (); int j = 0;
	    while ( i.hasNext ()) 
	    {
	    	certs[ j ] = ( iaik.x509.X509Certificate ) i.next ();
		System.out.println ( certs [ j ]);
	    }
	    if ( CertUtils.verifyCertificateChain ( certs, ks )) 
		return certs;
	    else return null;
	} 
	catch ( Exception e ) 
	{
	    showException ( e );
	    return null;
	}
    }
    */

    /*
     * Imports a certificate from file; the user is asked for the file location.
     * It then sets this certificate in the KeyStore Entry.
     *
     * @author Andre Benvenuti, GST Bern
     */

    /*
    public void putCertificate()
    {
	if( ks == null ) return;

	MutableTreeNode node = tree.getSelectedNode();
	if( node == null ) 
	    return;

	String alias = node.getAlias();

	java.security.cert.X509Certificate[] newCerts = importCertificateChain ();
	
	try 
	{
	    if( ks.isKeyEntry ( alias )) 
	    {
	    	// get password from user and then the key pair.
		char[] password = null;
		char[][] passwordHolder = new char[1][];
		UserSponsor us = new UserSponsor( "Password", 
						  "Please enter password for key alias " + alias , 
						  null, new String[] { "Password" }
						);
		us.getInput ( null, passwordHolder );
	      
		if ( passwordHolder[ 0 ] != null ) 
		{
		     password = passwordHolder[ 0 ];
		     java.security.Key key = ks.getKey ( alias, password );
		     ks.setKeyEntry ( alias,
				      key, 
				      password,
				      newCerts
				    ); 
		     node.setKey((java.security.PrivateKey)key);
		}
		else 
		    return;
	    } 
	    else 
		if ( newCerts.length == 1 ) 
		    ks.setCertificateEntry ( alias, newCerts[ 0 ] );
	    else 
		return;
	    tree.addCert( alias, 0, newCerts );
	    dirty = true;
	} 
	catch ( Exception e ) 
	{ 
	    showException(e); 
	}
	repaint ();
    }
    */

    /*
     * Imports a certificate from file; the user is asked for the file location.
     * It then sets this certificate in a new KeyStore Trusted Certificate Entry.
     *
     * @author Andre Benvenuti, GST Bern
     */

    private void addTrustee() 
    {
    	iaik.x509.X509Certificate cert = importCertificate();
    	if ( cert == null ) 
	{ 
	    System.out.println ( "cert is null" ); 
	    return; 
	}
    	iaik.asn1.structures.Name subject = (iaik.asn1.structures.Name)cert.getSubjectDN ();
    	String caAlias = subject.getRDN ( ObjectID.commonName );
    	iaik.asn1.structures.Name issuer = (iaik.asn1.structures.Name) cert.getIssuerDN ();
    	String test = issuer.getRDN ( ObjectID.commonName );
    	
	// String caAlias = cert.getSubjectDN ().getName ();
	try 
	{	
	    if ( caAlias.equals ( test )) 
	    {
	    	tree.addTrustedCert( caAlias, cert );
	    }
	    else 
	    { 
		throw new RuntimeException("subject != issuer: " + caAlias + test ); 
	    }

	    repaint ();
	    dirty = true;    
	} 
	catch ( Exception e ) 
	{ 
	    showException(e); 
	}
    	
    }

    private void deleteNode()
    {
	MutableTreeNode node = tree.getSelectedNode();
	if( node == null )
	    return;

	int option = JOptionPane.showConfirmDialog( null,
						    "This will delete the selected entry! Continue?",
						    "Delete",
						    JOptionPane.OK_CANCEL_OPTION,
						    JOptionPane.INFORMATION_MESSAGE);
	if( option == JOptionPane.OK_OPTION )
	{
	    try
	    {
		node.removeFromParent();
		tree.removeSelectedNode();
		tree.reload();
		dirty = true;	
	    }
	    catch ( Exception e )
	    {
		showException( e );
	    }
	}
    }



    private void generateKeys()
	throws NoKeyStoreException, NoSuchAlgorithmException, KeyStoreException
    {	
	if( ks == null )
	    throw new NoKeyStoreException();

	// get algorithm and key size from user
	String [] keyparams = new String [] {"Algorithm", "KeyLength" };
	char[][] passwds= new char[1][];

	String listOptions[][] = {{"RSA","DSA"}, {"1024","2048"}};
	
        UserSponsor us = new UserSponsor("Generate Keys",
					 "Please select Key Generation parameters", 
					 new String[]{"New Key Alias"}, 
					 keyparams, 
					 listOptions,
                                         new String[]{"Password"});

        String[] aliasHolder = new String[1];

	if( us.getInput( aliasHolder, keyparams, passwds) )
	{
	    String algorithm = "DSA";
	    int keylength = 1024;
	    String alias = "<unknown>";

	    /* password to protect key entry */
	    char[] password = new char[0];
       
	    if( keyparams[0] != null )
		algorithm = keyparams[0];

	    if( keyparams[1] != null )
		keylength = Integer.parseInt(keyparams[1]);

	    alias = aliasHolder[0];

	    // get selected alias 
	    if( alias == null || alias.length() == 0 )
		throw new IllegalArgumentException("No alias for key entry!");

	    // get key password
	    if( passwds[0] != null )
		password = passwds[0];

	    // generate keys
	    KeyPairGenerator generator = null;

	    setCursor(java.awt.Cursor.getPredefinedCursor( java.awt.Cursor.WAIT_CURSOR ));
	
	    try 
	    {
		generator = KeyPairGenerator.getInstance(algorithm, "IAIK");
	    } 
	    catch (NoSuchProviderException ex) 
	    {
		throw new NoSuchAlgorithmException("Provider IAIK not found!");
	    }
	
	    generator.initialize(keylength);
	    java.security.KeyPair keyPair = generator.generateKeyPair();
	
	    // write a self-signed cert and store it in the keystore
	    iaik.x509.X509Certificate[] chain = null;
	    iaik.x509.X509Certificate selfsignedCert = null;
	    try
	    {
		iaik.asn1.structures.Name dName = CertUtils.createName(alias);
		selfsignedCert = CertUtils.createPublicKeyCert(dName, dName,
							       keyPair.getPublic(),
							       keyPair.getPrivate());

	    }
	    catch(java.security.InvalidKeyException ike)
	    {
		throw new java.lang.Error("Internal Error: invalid keys generated!");
	    }
	    catch(java.security.cert.CertificateException ext)
	    {
		ext.printStackTrace(); // should not happen
	    }
	    catch(iaik.x509.X509ExtensionException ext)
	    {
		ext.printStackTrace(); // should not happen
	    }
	    dirty = true;
	    tree.addKey(alias, keyPair.getPrivate(), password );

	    repaint();

	    tree.addCert(alias, 0, selfsignedCert);
	    setCursor(java.awt.Cursor.getDefaultCursor());
	    repaint();
	}
    }
    
    private void verifyChain()
    {
	if( ks == null )
	    return;

	String fileName = null;
	String alias = null;
 
	MutableTreeNode node = tree.getSelectedNode();
	if( node == null ) 
	    return;

	if( node instanceof KeyNode )
	{
	    alias = ((KeyNode)node).getAlias();
	    iaik.x509.X509Certificate[] chain =((KeyNode)node).getCertificateChain();
	    int len = chain.length;
	    try 
	    {
		chain[len-1].verify( chain [ len - 1 ].getPublicKey ());

		for ( int i = len - 1; i > 0; i-- )
		    chain[ i - 1 ].verify( chain[ i ].getPublicKey ());

		if( tree.isTrusted( chain[ len - 1 ] ))
		{
		    JOptionPane.showMessageDialog( null,
						    "Verification successful",
						    "Certificate Chain Verification",
						    JOptionPane.INFORMATION_MESSAGE);
		}
		else
		{
		    JOptionPane.showMessageDialog( null,
						    "Verification failed, last chain element not trusted",
						    "Certificate Chain Verification",
						    JOptionPane.ERROR_MESSAGE);
		}
	    } 
	    catch ( Exception ex ) 
	    {
		showException( ex );
	    }
	}
	
    }



    public void showException(Exception e )
    {
	e.printStackTrace();
	JOptionPane.showMessageDialog(null, 
				      e.getClass().getName() + ":" + e.getMessage(),
				      "Exception", 
				      JOptionPane.ERROR_MESSAGE);	
    }

    public static void main(String[]  args)
    {
	iaik.security.provider.IAIK.addAsProvider();
	new KeyStoreManager();
    }


    private class EventHandler 
	implements /* MouseListener, ListSelectionListener, TreeSelectionListener,*/ ActionListener
    {       
	/**
	 * This method responds to menu selections.
	 *
	 * @param event
	 */
	public void actionPerformed(ActionEvent event) 
	{
	    Object source =  event.getSource();
	    
	    if (source instanceof JMenuItem)
	    {
		try
		{
		    if ((JMenuItem) source == exitMenuItem)
			_exit();
		    if ((JMenuItem) source == openMenuItem)
			load();
		    if ((JMenuItem) source == closeMenuItem)
			close();
		    if ((JMenuItem) source == newMenuItem)
			newFile();
		    if ((JMenuItem) source == saveMenuItem)
			save();
		    if ((JMenuItem) source == saveAsMenuItem)
			saveAs();

		    if ((JMenuItem) source == generateKeyMenuItem)
			generateKeys();
		    if ((JMenuItem) source == deleteKeyMenuItem)
			deleteNode();
		    if ((JMenuItem) source == verifyChainMenuItem)
			verifyChain();

		    if ((JMenuItem) source == addTrusteeMenuItem)
			addTrustee();
		    if ((JMenuItem) source == deleteTrusteeMenuItem)
			deleteNode();

		    if ((JMenuItem) source == exportCertMenuItem)
			exportCert();
		    if ((JMenuItem) source == createCertMenuItem)
			createCert();
		    if ((JMenuItem) source == importCertMenuItem)
			importCert();
		    if ((JMenuItem) source == deleteCertMenuItem)
			deleteNode();
		}
		catch( NoKeyStoreException nke)
		{
		    JOptionPane.showMessageDialog(null, 
						  "Please select a valid key store",
						  "No Key Store selected", 
						  JOptionPane.ERROR_MESSAGE);	
		}
		catch( Exception e )
		{
		    e.printStackTrace();
		    JOptionPane.showMessageDialog(null, 
						  e.getClass().getName() + ":" + e.getMessage(),
						  "Exception", 
						  JOptionPane.ERROR_MESSAGE);	
		}
	    }
	}
    }
}






