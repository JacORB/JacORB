/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1997-2002  Gerald Brose.
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

import java.io.*;
import java.security.*;
import java.util.*;
import java.net.*;
import java.math.BigInteger;
import java.security.cert.CertificateException;

import iaik.security.rsa.*;
import iaik.x509.*;
import iaik.asn1.structures.*;
import iaik.asn1.*;
import iaik.x509.extensions.*;
import iaik.security.provider.IAIK;
import iaik.pkcs.pkcs8.*;
import iaik.utils.KeyAndCertificate;

/**
 * @author André Benvenuti ( bnv ), UGFU, Generalstab, Bern
 * @created 04.11.99
 */

public class CertificatesManager {

  /* -----------------05.12.98 19:16------------------------------*
   * Genarates rsa key pair and a certificate for rsa public key. *
   *                                                              *
   * It also has utility methods called by the trust deciders.    *
   *                                                              *
   * Author: André Benvenuti                                      *
   * -------------------------------------------------------------*/

  public static int saveFormat     = ASN1.PEM;
  private static final String pass_phrase = "bnv_8.12.99_passpass_find_out_ORB_JacORB1_0/beta14_SSL";
  private static Hashtable signers = new Hashtable (); // the trusted signers

  /**
   * Add a trusted signer to our signers hash table.
   *
   * @param certificate the certificate to be added as trusted
   */
  public static Principal addTrustedSigner(
                 java.security.cert.X509Certificate certificate
                                      )
  {

    if ( certificate == null ) {
      System.out.println( "certificate is empty." );
      System.exit ( 0 );
    }
    Principal dn = certificate.getSubjectDN ();
    if ( dn == null ) {
      System.out.println( "Unable to get dn from certificate." );
      System.exit ( 0 );
    }
    Vector certs = ( Vector ) signers.get ( dn );
    if( certs == null ) {
      certs = new Vector( 4 );
      signers.put( dn, certs );
    }
    certs.addElement( certificate );
    if ( dn == null ) {
      System.out.println( "Unable to add dn." );
      System.exit ( 0 );
    }
    return dn;
  }

  /*
   * Set the list with the certificates of the trusted signers.
   * Previous trusted signers are deleted.
   *
   * @param certificateList the list of certificates
   */
  public static void setTrustedSigners ( X509Certificate[] certificateList ) {
    signers = new Hashtable ();
    for ( int i = 0; i < certificateList.length; i++ ) {
      addTrustedSigner ( certificateList[ i ] );
    }
  }

  /*
   * This method checks every certificate in the chain if it was signed
   * from an signer in our trusted signers list. This method also verifies that
   * the certificate chain from the client has a certificate signed by a
   * trusted signer.
   */
  public static boolean hasTrustedRoot (
               java.security.cert.X509Certificate [] certificateChain
                                       )
  {
    try {
    // check all certificates for a trusted root
      for ( int i = 0; i < certificateChain.length; i++ )
      {
        if ( i > 0 ) {
        // we are looking for a trusted root for a certificate in the chain
          certificateChain[ i - 1 ].verify (
                           certificateChain[ i ].getPublicKey ()
                                           );
        }
        Vector certs = ( Vector )signers.get(
                            certificateChain[ i ].getSubjectDN ()
                                            );
        if ( certs == null ) {
          continue;
        } else 
            for( int j = 0; j < certs.size (); j++ ) {
              X509Certificate currentCert = ( X509Certificate )certs.elementAt ( j );
              if ( currentCert.equals ( certificateChain[ i ] )) return true;
          
            }
      }
    } catch ( Exception ex ) { return false; }
    return false;
  }

  /*
   * Encrypt the private key with a password according to PKCS#5 and PKCS#8.
   * We encode a value of type PrivateKeyInfo keyPair.getPrivate()
   * according to PKCS#8 to represent the private key in an
   * algorithm-independent manner, which subsequently will be encrypted
   * using the PbeWithMD5AndDES_CBC ( PKCS#5 ) algorithm and encoded
   * as PKCS#8 and save the key and the certificate chain to a file.
   */
  public static void saveKeyAndCert ( KeyPair keyPair,
                                      X509Certificate[] chain,
                                      String fileName
                                    ) throws IOException
  {

    EncryptedPrivateKeyInfo epki = new
      EncryptedPrivateKeyInfo(( PrivateKeyInfo )keyPair.getPrivate ());
    try {
      epki.encrypt ( pass_phrase,
                     AlgorithmID.pbeWithMD5AndDES_CBC,  // PKCS#5 standard
                     null
                   );
    } catch ( NoSuchAlgorithmException ex ) {
      throw new RuntimeException( "No implementation for pbeWithMD5AndDES_CBC!" );
    }
    // append the correct extension
    fileName = fileName + ( saveFormat == ASN1.DER ? ".der" : ".pem" );
    System.out.println( "save private key and certificate chain to file "
                      + fileName
                      + "..."
                      );
    new KeyAndCertificate( epki, chain ).saveTo( fileName, saveFormat );
  }


  /**
   * Generate a KeyPair.
   *
   * @param bits the length of the key ( modulus ) in bits
   * @return the KeyPair
   */
  public static KeyPair generateKeyPair( String algorithm,
                                         int bits
                                       )
  {
		try {
      KeyPairGenerator generator =
         KeyPairGenerator.getInstance( algorithm, "IAIK" );
      generator.initialize( bits );
      KeyPair kp = generator.generateKeyPair ();
      return kp;
    } catch ( NoSuchProviderException ex ) {
      System.out.println ( "Provider IAIK not found!" );
      return null;
    } catch ( NoSuchAlgorithmException ex ) {
      System.out.println ( "Algorithm " + algorithm + " not found!" );
      return null;
    }
	}

  /*
   * Verifies a certificate chain.
   *
   * The certificate of the user is the first one in the list
   * ( chain[0] ), and the top level certificate is the last one.
   * chain[0] = user certificate.
   * chain[x] = self signed CA certificate
   *
   * @chain the certificate chain to verify
   * @return true if the certificate chain is o.k., false otherwise
   */
  public static boolean verifyCertificateChain ( java.security.cert.X509Certificate[] chain )
  {
    int len = chain.length;
    try {
      chain[ len - 1 ].verify( chain [ len - 1  ].getPublicKey ());
      for ( int i = len - 1; i > 0; i-- )
        chain[ i - 1 ].verify( chain[ i ].getPublicKey ());
    } catch ( Exception ex ) { return false; }
    return hasTrustedRoot ( chain );
  }

  /**
   * Create a certificate.
   */
  public static X509Certificate createCertificate( Name subject,
                                                   PublicKey pk,
                                                   Name issuer,
                                                   PrivateKey sk,
                                                   AlgorithmID algorithm
                                                  )
  {

    X509Certificate cert = new X509Certificate ();

    try {
      cert.setSerialNumber( new BigInteger( 20, new Random ()));
      cert.setSubjectDN( subject );
      cert.setPublicKey( pk );
      cert.setIssuerDN( issuer );

      // not before yesterday
      GregorianCalendar date = new GregorianCalendar ();
      date.add( Calendar.DATE, -1 );
      cert.setValidNotBefore( date.getTime ());

      // valid six monthe
      date.add( Calendar.MONTH, 6 );
      cert.setValidNotAfter( date.getTime ());

      cert.sign( algorithm, sk );
    } catch ( CertificateException ex ) {
      System.out.println( "CertificateException: " + ex.getMessage ());
      return null;
    } catch ( InvalidKeyException ex ) {
      System.out.println( "InvalidKeyException: " + ex.getMessage ());
      return null;
    } catch ( NoSuchAlgorithmException ex ) {
      System.out.println( "NoSuchAlgorithmException: " + ex.getMessage ());
      return null;
    }

    return cert;
  }

  /**
   * Creates the server and client certificates.
   */
  public static void main( String arg[] ) throws IOException
  {
    boolean           caFound    = false;
    boolean           selfSigned = false;
    String            userName   = null;
    String            caName     = null;
    KeyAndCertificate caKAC;
    PrivateKey        caRSA      = null;
    X509Certificate   caCert     = null;
    X509Certificate   cert;
    KeyPair           clientRSA;
    KeyPair           serverRSA;
    iaik.pkcs.pkcs8.EncryptedPrivateKeyInfo epki;

    if ( arg.length == 3 ) {
      caFound = true;
      if ( arg[ 0 ].equals ( "-ca" )) {
        caName = arg[ 1 ]; userName = arg[ 2 ];
      } else if ( arg[ 1 ].equals ( "-ca" )) {
        System.out.println ( "caFound at 1" );
        caName = arg[ 2 ]; userName = arg[ 0 ];
      } else {
        System.out.println ( "caNotFound" );
        System.out.println ( "Usage: CerttificatesManager [-ca caName] userName" );
        return;
      }
    } else if ( arg.length != 1 ) {
      System.out.println ( "bad # params" );
      System.out.println ( "Usage: CertificatesManager [-ca caName] userName" );
      return;
    } else {
      userName = arg[ 0 ];
      selfSigned = true;
    }

    try {
      IAIK.addAsProvider();

      File certsDir         = new File( "certs" );
      String caKeyAndCertFileName = null;
      if ( certsDir.exists ()) {
        if ( caFound ) {
          caKeyAndCertFileName  = "certs/"
                                + caName
                                + "KeyAndCert"
                                + ".pem";
          caKAC  = new KeyAndCertificate ( caKeyAndCertFileName );
          epki   = ( EncryptedPrivateKeyInfo ) caKAC.getPrivateKey ();
          epki.decrypt ( pass_phrase );
          caRSA  = epki.getPrivateKeyInfo ();
          caCert = caKAC.getCertificateChain ()[ 0 ];
        }
      } else if ( caName  != null ) {
        System.out.println ( "No certificate file "
                           + caKeyAndCertFileName
                           );
            return;
      } else certsDir.mkdir();

      // enquire local host name for user
      BufferedReader reader = new
         BufferedReader ( new InputStreamReader ( System.in ));
      String line;
      String localhost =  InetAddress.getLocalHost().getHostName ();
      System.out.print( "Give server host name [" + localhost + "]:");
      line = reader.readLine();
      if ( line.length () != 0 ) {
        localhost = line;
      }

      X509Certificate[] chain;

      if ( !selfSigned && caFound ) {
        System.out.println ( "Creating server certificates for "
                           + userName + " at " + localhost + "\n"
                           );
        // First create the private keys
        clientRSA = generateKeyPair ( "RSA", 1024 );
        serverRSA = generateKeyPair ( "RSA", 1024 );

        Name issuer = new Name();
        issuer.addRDN ( ObjectID.country,             "CH" );
        issuer.addRDN ( ObjectID.organization ,       "EPFL" );
        issuer.addRDN ( ObjectID.organizationalUnit , "SSLCertificateManager" );
        issuer.addRDN ( ObjectID.commonName ,         caName );

        Name serverSubject = new Name();
        serverSubject.addRDN ( ObjectID.country,       "CH");
        serverSubject.addRDN ( ObjectID.organization , "EPFL");
        serverSubject.addRDN ( ObjectID.commonName , userName
                                                   + localhost
                             );
        // create server certificates
        chain = new X509Certificate[ 2 ];
        serverSubject.addRDN ( ObjectID.organizationalUnit,
                               "RSA Server Certificate"
                             );
        chain[ 0 ] = createCertificate ( serverSubject,
                                         serverRSA.getPublic(),
                                         issuer,
                                         caRSA,
                                         AlgorithmID.md5WithRSAEncryption
                                       );
        chain[ 1 ] = caCert;
        verifyCertificateChain( chain );
        saveKeyAndCert ( serverRSA, chain,
                         "certs/server" + userName + "KeyAndCert"
                       );

        System.out.println ( "Creating client test certificates for "
                           + userName + " at " + localhost + "\n"
                           );
        Name clientSubject = new Name();
        clientSubject.addRDN ( ObjectID.country,       "CH");
        clientSubject.addRDN ( ObjectID.organization , "EPFL");
        clientSubject.addRDN ( ObjectID.commonName ,   "Client"
                                                     + userName
                                                     + localhost
                             );
      // create client certificates
        chain = new X509Certificate[ 2 ];
        clientSubject.addRDN ( ObjectID.organizationalUnit,
                               "RSA Client Certificate"
                             );
        chain[ 0 ] = createCertificate ( clientSubject,
                                         clientRSA.getPublic(),
                                         issuer,
                                         caRSA,
                                         AlgorithmID.md5WithRSAEncryption
                                       );
        chain[ 1 ] = caCert;
        verifyCertificateChain( chain );
        saveKeyAndCert ( clientRSA, chain,
                         "certs/client" + userName + "KeyAndCert"
                       );

      } else {
        //
        // create self signed CA certs
        //

        X509Certificate   userCert = null;
        chain                      = new X509Certificate[ 1 ];

        System.out.println ( "create self signed RSA CA certificate..."
                           + " for " + userName
                           );
        Name user = new Name();
        user.addRDN ( ObjectID.country,             "CH" );
        user.addRDN ( ObjectID.organization ,       "EPFL" );
        user.addRDN ( ObjectID.organizationalUnit , "SSLCertificateManager" );
        user.addRDN ( ObjectID.commonName ,         userName );

        KeyPair userRSA  = generateKeyPair ( "RSA", 1024 );
        userCert = createCertificate( user,
                                      userRSA.getPublic(),
                                      user,
                                      userRSA.getPrivate(),
                                      AlgorithmID.md5WithRSAEncryption
                                    );
        chain[ 0 ] = userCert;
        OutputStream certFile = new FileOutputStream( "certs/"
                                                    + userName
                                                    + "Cert.pem"
                                                    );
        userCert.writeTo ( certFile ); certFile.close ();
        saveKeyAndCert( userRSA,
                        chain, "certs/"
                      + userName
                      + "KeyAndCert"
                      );
      }

      System.out.println("\nServer and Client certificates created.");

    } catch ( NoSuchAlgorithmException ex ) {
      System.out.println( "NoSuchAlgorithmException: "+ex.toString());
    } catch ( UnknownHostException ex ) {
      System.out.print( "UnknownHostException: " + ex.getMessage ());
    } catch ( CertificateException ex ) {
      System.out.print( "CertificateException: " + ex.getMessage ());
    } catch (Exception ex) {
      System.out.println( "Other Exception: " + ex);
    }

	}
}






