package org.jacorb.security.level2;
/**
 * KeyAndCert.java
 *
 *
 * Created: Mon Sep  4 16:33:49 2000
 *
 * @author Nicolas Noffke
 * @version $Id$
 */

import java.security.PrivateKey;
import java.security.cert.X509Certificate;;

public class KeyAndCert
{
    //might stay null for received creds
    public PrivateKey key = null; 

    public X509Certificate[] chain = null;
    
    public KeyAndCert( PrivateKey key,
                       X509Certificate[] chain )
    {
        this.key = key;
        this.chain = chain;
    }
    
    public KeyAndCert( KeyAndCert source )
    {
        this.key = source.key;
        
        chain = new X509Certificate[ source.chain.length ];
        System.arraycopy( source.chain, 0, chain, 0, source.chain.length );
    }
} // KeyAndCert






