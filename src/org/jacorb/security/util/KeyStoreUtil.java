package org.jacorb.security.util;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 2000  Gerald Brose.
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


import java.security.*;
import java.security.cert.*;
import java.io.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.*;

import java.math.BigInteger;
import java.util.*;

import iaik.asn1.*;
import iaik.asn1.structures.*;
import iaik.x509.*;
import iaik.x509.extensions.*;

/**
 * A class with utility methods that help managing a key store.
 *
 * @author Gerald Brose, FU Berlin
 * @version $Id$
 */

public class KeyStoreUtil
{
    /**
     * @returns - a fully loaded and operational KeyStore
     * @param file_name - a keystore file name to be loaded
     * @param storepass - the password for managing the keystore
     */

    public static KeyStore getKeyStore(String file_name, char[] storepass )
	throws 	java.io.IOException, KeyStoreException, NoSuchAlgorithmException,
	CertificateException
    {
	FileInputStream in = new FileInputStream(file_name);

	KeyStore ks;
	//	ks = KeyStore.getInstance("jks");
	
	try 
	{
	    ks = KeyStore.getInstance( "IAIKKeyStore", "IAIK" );
	} 
	catch ( java.security.NoSuchProviderException ex ) 
	{
	    System.err.println ( ex.toString ());
	    ks = KeyStore.getInstance("jks");
	}
	ks.load(in, storepass);
	in.close();
	return ks;
    }


    /**
     * @returns - a key pair, if the alias refers to a valid key entry, null otherwise
     * @param keystore - a keystore file name to be loaded
     * @param alias - the alias for the key entry
     * @param storepass - the password for managing the keystore
     * @param password - the password used to protect the private key
     */

    public static java.security.KeyPair getKeyPair(String keystore, 
						   String alias, 
						   char[] storepass, 
						   char[] password)
	throws 	java.io.IOException,
	KeyStoreException, NoSuchAlgorithmException,
	UnrecoverableKeyException, CertificateException
    {
	KeyStore ks  = getKeyStore( keystore, storepass );

	if(! ks.isKeyEntry(alias))
	   return null;

	java.security.PrivateKey privateKey = 
	    (java.security.PrivateKey)ks.getKey(alias,password);
	java.security.cert.X509Certificate c = 
	    (java.security.cert.X509Certificate)ks.getCertificate(alias);       
	java.security.PublicKey publicKey = c.getPublicKey();

	return new java.security.KeyPair( publicKey, privateKey);
    }

    /**
     * @returns - a key pair, if the alias refers to a valid key entry, null otherwise
     *
     * @param ks - a keystore file name to be loaded
     * @param alias - the alias for the key entry
     * @param password - the password used to protect the private key
     */

    public static java.security.KeyPair getKeyPair(KeyStore ks,
						   String alias, 
						   char[] password)
	throws 	java.io.IOException,
	KeyStoreException, NoSuchAlgorithmException,
	UnrecoverableKeyException, CertificateException
    {
	if(! ks.isKeyEntry(alias))
	   return null;

	java.security.PrivateKey privateKey = 
	    (java.security.PrivateKey)ks.getKey(alias,password);
	java.security.cert.X509Certificate c = 
	    (java.security.cert.X509Certificate)ks.getCertificate(alias);

	java.security.PublicKey publicKey = c.getPublicKey();
	return new java.security.KeyPair( publicKey, privateKey);
    }

    /**
     * @returns -  a key pair retrieved from a  keystore, asking for user
     * input with GUI support, null if user input is invalid or the key
     * pair could not be found
     */

    public static java.security.KeyPair getKeyPair(KeyStore ks)
	throws java.io.IOException,
	KeyStoreException, NoSuchAlgorithmException, 
	UnrecoverableKeyException, CertificateException
    {
	String[] clear_input = new String[]{  "Entry Alias"};
	char[][] opaque_input = new char[1][];

	UserSponsor us = new UserSponsor("", 
					 "Please authenticate to retrieve key pair", 
					 clear_input, 
					 new String[]{ "Entry Password" }
					 );

	if( !us.getInput(clear_input, opaque_input))
	{
	    System.err.println("Input cancelled");
	    System.exit(1);
	}

	String alias = clear_input[0];
	char [] password = opaque_input[0];

	if( alias == null || password == null  )
	{
	    return null;
	}

	return getKeyPair( ks, alias, password);
    }


    /**
     * retrieve a key pair from a  keystore, asking for user
     * input with GUI support
     */

    public static java.security.KeyPair getKeyPair()
	throws java.io.IOException,
	KeyStoreException, NoSuchAlgorithmException, 
	UnrecoverableKeyException, CertificateException
    {
	String[] clear_input = new String[]{ "Keystore file", "Entry Alias"};
	char[][] opaque_input = new char[2][];
	
	UserSponsor us = new UserSponsor("",
					 "Please authenticate to retrieve key pair", 
					 clear_input,
					 new String[]{ "Keystore Password",  "Entry Password" });
	
	if( !us.getInput(clear_input, opaque_input))
	{
	    System.err.println("Input cancelled");
	    System.exit(1);
	}

	String fname = clear_input[0];
	String name = clear_input[1];
	char [] ksPassword = opaque_input[0];
	char [] entryPassword = opaque_input[1];

	if( fname == null || name == null || ksPassword == null || entryPassword == null )
	{
	    System.err.println("no input");
	    System.exit(1);
	}

	return getKeyPair( fname, name, ksPassword, entryPassword);
    }

    /**
     * retrieve all Certficates from a key store file, prompt user for
     * name and password if input is invalid
     */

    public static java.security.cert.X509Certificate [] getCerts(String fileName, 
							     String name, 
							     char[] password)
	throws java.io.IOException,
	KeyStoreException, NoSuchAlgorithmException, 
	UnrecoverableKeyException, CertificateException
    {
	if( name == null || name.length() == 0 || password == null )
	{
	    return getCerts( fileName );
	}

	KeyStore ks = getKeyStore(fileName, password );
	if(! ks.isKeyEntry(name))
	   return null;

	return (java.security.cert.X509Certificate[])ks.getCertificateChain( name );

    }


    /**
     * retrieve all Certficates from a key store file,prompt user for
     * name and password
     */

    public static java.security.cert.X509Certificate [] getCerts(String fileName)
	throws IOException, KeyStoreException, NoSuchAlgorithmException, 
	UnrecoverableKeyException, CertificateException
    {
	String[] clear_input = new String[]{ "User name"};
	char[][] opaque_input = new char[1][];
	
	UserSponsor us = new UserSponsor("Authentication",
					 "Please authenticate to retrieve certificates", 
					 clear_input,
					 new String[]{ "Password"});
	
	if( !us.getInput(clear_input, opaque_input))
	{
	    System.err.println("Input cancelled");
	    System.exit(1);
	}

	String name = clear_input[0];
	char [] password = opaque_input[0];

	if( name == null || password == null )
	{
	    System.err.println("no input");
	    System.exit(1);
	}

	
	KeyStore ks = getKeyStore(fileName, password );

	if(! ks.isKeyEntry(name))
	    return null;

	return (java.security.cert.X509Certificate[])ks.getCertificateChain( name );

    }


    /**
     * retrieve all X509Certficates in a key store for a given alias that qualify
     * a role certs, i.e. that have a V.3 extension of type SubjectAltName with
     * a rfc 822 name component that starts with "role:"
     */

    public static java.security.cert.X509Certificate [] getRoleCerts(KeyStore ks, 
								     String alias,
								     java.security.PublicKey[] trustees )
	throws java.security.KeyStoreException,java.security.cert.CertificateEncodingException
    {
	if(! ks.isKeyEntry(alias))
	   return null;

	java.util.Vector vector = new java.util.Vector();

	java.security.cert.Certificate[] chain = 
	    (java.security.cert.Certificate[])ks.getCertificateChain( alias );
	for( int i = 0; i < chain.length; i++ )
	{
	    try
	    {
		iaik.x509.X509Certificate c = (iaik.x509.X509Certificate)chain[i];
		if( !c.hasExtensions())
		    continue;

		for( Enumeration extensions = c.listExtensions(); extensions.hasMoreElements();)
		{
		    iaik.x509.V3Extension e = (iaik.x509.V3Extension)extensions.nextElement();
		    if( e instanceof SubjectAltName )
		    {
			SubjectAltName san = (SubjectAltName)e;
			GeneralNames gn = san.getGeneralNames();
			for( Enumeration g = gn.getNames(); g.hasMoreElements(); )
			{
			    GeneralName generalName = (GeneralName)g.nextElement();
			    if( generalName.getType() == GeneralName.rfc822Name )
			    {
				String value = (String)generalName.getName();
				if( value.startsWith("role:"))
				{
				    c.checkValidity();
				    java.security.Signature sig = 
					java.security.Signature.getInstance( c.getSigAlgName());

				    for( int ii = 0; ii < trustees.length; ii++)
				    {
					try
					{
					    sig.initVerify( trustees[ii] );
					    sig.verify( c.getSignature() );
					    vector.addElement(c);
					}
					catch( SignatureException se )
					{
					    continue;
					}
					catch( InvalidKeyException se )
					{
					    continue;
					}
				    }
				}
			    }
			}
		    }
		}
				
	    }
	    catch(Exception e)
	    {
		e.printStackTrace();
		continue;
	    }
	}

	java.security.cert.X509Certificate[] result = 
	    new java.security.cert.X509Certificate[vector.size()];
	for( int i = 0; i < result.length; i++)
	{
	    result[i] = (java.security.cert.X509Certificate)vector.elementAt(i);
	}
	return result;
    }



}
