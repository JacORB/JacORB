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
 * A class with utility methods that help managing certificates
 *
 * @author Gerald Brose, FU Berlin
 * @version $Id$
 */

public class CertUtils
{

    /**
     * @returns - a self signed X509v3 public key certificate 
     *
     * @param subjectKey - the public key to be signed
     * @param privKey - the signature key
     * The signature algorithm will be selected accoring to
     * the type of the private key, i.e. dsaWithSHA1 for DSA keys
     * and md5WithRSAEncryption for RSA keys
     */
    
    public static iaik.x509.X509Certificate createPublicKeyCert(
								iaik.asn1.structures.Name subject,
								iaik.asn1.structures.Name issuer,
								java.security.PublicKey subjectKey, 
								java.security.PrivateKey privKey
								)
	throws iaik.x509.X509ExtensionException, java.security.cert.CertificateException, 
	java.security.NoSuchAlgorithmException, java.security.InvalidKeyException
    {
	iaik.x509.X509Certificate cert = new iaik.x509.X509Certificate();
	
	if( subject == null )
	    subject = emptyName();
	if( issuer == null )
	    issuer = emptyName();
	
	cert.setIssuerDN( issuer );
	cert.setSubjectDN( subject );
	cert.setPublicKey( subjectKey );
	
	java.util.Date now = new java.util.Date();
	
	cert.setSerialNumber( new BigInteger( Long.toString( now.getTime() )));
	cert.setValidNotBefore( now );
	
	java.util.Calendar cal = java.util.Calendar.getInstance();
	cal.add(java.util.Calendar.MONTH, 12);
	cert.setValidNotAfter(cal.getTime());

	if( privKey  instanceof java.security.interfaces.DSAPrivateKey )	
	    cert.sign(iaik.asn1.structures.AlgorithmID.dsaWithSHA1, 
		      privKey);
	else if ( privKey instanceof java.security.interfaces.RSAPrivateKey )
	    cert.sign(iaik.asn1.structures.AlgorithmID.md5WithRSAEncryption, 
		      privKey);
	else
	    throw new java.security.InvalidKeyException("Unknown private key: " + privKey.getClass().getName());

	return cert;
    }


    /**
     * @returns - an X509v3 certificate with an SubjectAltName extension that 
     * represents a role name. The format of the extension is an ASN.1 string
     * "role:<rolename>".
     */

    public static iaik.x509.X509Certificate certifyRoleMembership(
					      String rolename,
					      iaik.asn1.structures.Name subject,
					      iaik.asn1.structures.Name issuer,
					      java.security.PublicKey subjectKey, 
					      java.security.PrivateKey privKey)
	throws iaik.x509.X509ExtensionException, java.security.cert.CertificateException, 
	java.security.NoSuchAlgorithmException, java.security.InvalidKeyException
    {

	    iaik.x509.X509Certificate cert = new iaik.x509.X509Certificate();
	
	    if( subject == null )
		subject = emptyName();
	    if( issuer == null )
		issuer = emptyName();

	    cert.setIssuerDN( issuer );
	    cert.setSubjectDN( subject );
	    cert.setPublicKey( subjectKey );

	    java.util.Date now = new java.util.Date();

	    cert.setSerialNumber( new BigInteger( Long.toString( now.getTime() )));
	    cert.setValidNotBefore( now );

	    java.util.Calendar cal = java.util.Calendar.getInstance();
	    cal.add(java.util.Calendar.MONTH, 12);
	    cert.setValidNotAfter(cal.getTime());

	    /* add an extension */

	    GeneralName gn = new GeneralName( GeneralName.rfc822Name,"role:" + rolename );
	    GeneralNames generalNames = new GeneralNames(gn );

	    iaik.x509.extensions.SubjectAltName ext = new SubjectAltName( generalNames );
	    cert.addExtension(ext);

	    if( privKey instanceof java.security.interfaces.DSAPrivateKey )	
		cert.sign(iaik.asn1.structures.AlgorithmID.dsaWithSHA1, 
			  (java.security.PrivateKey)privKey);
	    else if ( privKey instanceof java.security.interfaces.RSAPrivateKey )
		cert.sign(iaik.asn1.structures.AlgorithmID.md5WithRSAEncryption, 
			  (java.security.PrivateKey)privKey);

	    System.out.println("Cert signed");
	    return cert;
    }

    public static iaik.asn1.structures.Name emptyName()
    {
	iaik.asn1.structures.Name subject = new iaik.asn1.structures.Name();
	subject.addRDN(ObjectID.commonName, "");
	subject.addRDN(ObjectID.organizationalUnit, "");
	subject.addRDN(ObjectID.organization, "");
	subject.addRDN(ObjectID.locality, "");
	subject.addRDN(ObjectID.country, "");
	return subject;
    }

    public static iaik.asn1.structures.Name createName(String alias)
    {
	iaik.asn1.structures.Name subject = new iaik.asn1.structures.Name();
	subject.addRDN(ObjectID.commonName, alias);
	subject.addRDN(ObjectID.organizationalUnit, "AGSS");
	subject.addRDN(ObjectID.organization, "FU Berlin");
	subject.addRDN(ObjectID.locality, "Berlin");
	subject.addRDN(ObjectID.country, "DE");
	return subject;
    }

    public static String getCertLabel(java.security.cert.X509Certificate cert)
    {
	String label = getRoleName(cert);
	if( label == null )
	{
	    label = cert.getSubjectDN() + ", signer: " + cert.getIssuerDN();
	}
	return label;
    }

    public static String getRoleName(java.security.cert.X509Certificate cert)
    {
	try
	{
	    iaik.x509.X509Certificate c;

	    try
	    {
		c = (iaik.x509.X509Certificate)cert;
	    }
	    catch( ClassCastException ccce )
	    {
		c = new iaik.x509.X509Certificate( cert.getEncoded());
	    }

	    if( !c.hasExtensions())
		return null;

	    c.checkValidity();
	    
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
				return value.substring(5);;
			}
		    }
		}
	    }
	    // nothing found
	    return null;
	}
	catch( Exception e)
	{
	    e.printStackTrace();
	    return null;
	}
    }

    public static boolean isRoleCert(java.security.cert.X509Certificate cert)
    {	
	try
	{
	    iaik.x509.X509Certificate c;

	    try
	    {
		c = (iaik.x509.X509Certificate)cert;
	    }
	    catch( ClassCastException ccce )
	    {
		c = new iaik.x509.X509Certificate( cert.getEncoded());
	    }

	    if( !c.hasExtensions())
		return false;

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
				return true;
			}
		    }
		}
	    }
	    // nothing found
	    return false;

	}
	catch( Exception e)
	{
	    e.printStackTrace();
	    return false;
	}
    }

    /*
     * Verifies a certificate chain.
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

    public static boolean verifyCertificateChain ( java.security.cert.X509Certificate[] chain,
						   java.security.KeyStore keyStore
						 )
    {
	int len = chain.length;
	try 
	{
	    chain[ len - 1 ].verify( chain [ len - 1 ].getPublicKey ());

	    for ( int i = len - 1; i > 0; i-- ) {
		org.jacorb.util.Debug.output( 3, "verifying chain[ " + (i-1) + " ]");
		chain[ i - 1 ].verify( chain[ i ].getPublicKey ());
	    }

	    // this won't work: the name is not an alias.
	    String alias = chain[ len - 1 ].getIssuerDN ().getName();
	    int index = alias.indexOf ( "CN=" ) + 3;
	    int l = alias.length ();
	    org.jacorb.util.Debug.output(4, "index = " + index + " l = " + l );
	    alias = alias.substring ( index, l  ); 
	    org.jacorb.util.Debug.output(4, "check if " + alias + " is a trusted certificate entry in key store" );
	    return keyStore.isCertificateEntry( alias );
	} 
	catch ( Exception ex ) 
	{ 
	    org.jacorb.util.Debug.output(3, "exection: " + ex.toString ());
	    return false; 
	}
    }

    public static java.security.cert.X509Certificate readCertificate(String fileName)
    {
	try 
	{        
            java.security.cert.CertificateFactory factory = 
                java.security.cert.CertificateFactory.getInstance("X.509", "IAIK") ;
            return (java.security.cert.X509Certificate)factory.generateCertificate( new FileInputStream( fileName ));
        }
	catch ( Exception ex ) 
	{ 
	    org.jacorb.util.Debug.output(3, "exception: " + ex.toString ());
	    return null; 
	}
    }


}






