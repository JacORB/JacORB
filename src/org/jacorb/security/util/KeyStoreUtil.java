package org.jacorb.security.util;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 2000-2004 Gerald Brose.
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

import java.util.*;

import iaik.asn1.structures.*;
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
     * @return - a fully loaded and operational KeyStore
     * @param file_name - a keystore file name to be loaded
     * @param storepass - the password for managing the keystore
     */

    public static KeyStore getKeyStore(String file_name, char[] storepass )
        throws 	java.io.IOException, KeyStoreException, NoSuchAlgorithmException, CertificateException
    {
        //try unchanged name first
        File f = new File( file_name );

        if( ! f.exists() )
        {
            //try to prepend home dir
            String name =
                System.getProperty( "user.home" ) +
                System.getProperty( "file.separator" ) +
                file_name;

            f = new File( name );
        }

        FileInputStream in = new FileInputStream( f );

        KeyStore ks;
        //	ks = KeyStore.getInstance("jks");

        try
        {
            ks = KeyStore.getInstance( "IAIKKeyStore", "IAIK" );
        }
        catch ( java.security.NoSuchProviderException ex )
        {
            System.err.println ( ex.toString ());
            ks = KeyStore.getInstance("JKS");
        }
        ks.load(in, storepass);
        in.close();
        return ks;
    }


    /**
     * @return - a key pair, if the alias refers to a valid key entry, null otherwise
     * @param keystore - a keystore file name to be loaded
     * @param alias - the alias for the key entry
     * @param storepass - the password for managing the keystore
     * @param password - the password used to protect the private key
     */

    private static java.security.KeyPair getKeyPair(String keystore,
                                                   String alias,
                                                   char[] storepass,
                                                   char[] password)
        throws 	IOException,
                KeyStoreException,
                NoSuchAlgorithmException,
                UnrecoverableKeyException,
                CertificateException
    {
        KeyStore ks  = getKeyStore( keystore, storepass );

        if(! ks.isKeyEntry(alias))
        {
            return null;
        }

        java.security.PrivateKey privateKey =
            (java.security.PrivateKey)ks.getKey(alias,password);
        java.security.cert.X509Certificate c =
            (java.security.cert.X509Certificate)ks.getCertificate(alias);
        java.security.PublicKey publicKey = c.getPublicKey();

        return new java.security.KeyPair( publicKey, privateKey);
    }

    /**
     * @return - a key pair, if the alias refers to a valid key entry, null otherwise
     *
     * @param ks - a keystore file name to be loaded
     * @param alias - the alias for the key entry
     * @param password - the password used to protect the private key
     */

    public static java.security.KeyPair getKeyPair(KeyStore ks,
            String alias,
            char[] password)
        throws KeyStoreException,
               NoSuchAlgorithmException,
               UnrecoverableKeyException
    {
        if(! ks.isKeyEntry(alias))
        {
            return null;
        }

        java.security.PrivateKey privateKey =
            (java.security.PrivateKey)ks.getKey(alias,password);
        java.security.cert.X509Certificate c =
            (java.security.cert.X509Certificate)ks.getCertificate(alias);

        java.security.PublicKey publicKey = c.getPublicKey();
        return new java.security.KeyPair( publicKey, privateKey);
    }

    /**
     * retrieve all X509Certficates in a key store for a given alias that qualify
     * a role certs, i.e. that have a V.3 extension of type SubjectAltName with
     * a rfc 822 name component that starts with "role:"
     */

    public static java.security.cert.X509Certificate [] getRoleCerts(KeyStore ks,
                                                                     String alias,
                                                                     java.security.PublicKey[] trustees )
        throws java.security.KeyStoreException
    {
        if(! ks.isKeyEntry(alias))
        {
            return null;
        }

        List list = new ArrayList();

        java.security.cert.Certificate[] chain =
            ks.getCertificateChain( alias );
        for( int i = 0; i < chain.length; i++ )
        {
            try
            {
                iaik.x509.X509Certificate c = (iaik.x509.X509Certificate)chain[i];
                if( !c.hasExtensions())
                {
                    continue;
                }

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
                                            list.add(c);
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
            (X509Certificate[]) list.toArray(new java.security.cert.X509Certificate[list.size()]);

        return result;
    }
}
