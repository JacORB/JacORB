package org.jacorb.security.jsse;
/**
 * X509KeyManagerImpl.java
 *
 *
 * Created: Wed Mar  7 17:08:06 2001
 *
 * @author Nicolas Noffke
 * $Id$
 */
import java.security.*;
import java.security.cert.*;

import org.jacorb.security.level2.KeyAndCert;

import com.sun.net.ssl.*;

public class X509KeyManagerImpl  
    implements X509KeyManager
{
    private KeyAndCert[] kac = null;
    private String[] aliases = null;

    public X509KeyManagerImpl( KeyAndCert[] kac ) 
    {
        this.kac = kac;

        aliases = new String[ kac.length ];
        for( int i = 0; i < kac.length; i++ )
        {
            aliases[ i ] = "" + i;
        }
    }
    
    public String[] getClientAliases( String keyType,
                                      Principal[] issuers )
    {
        return aliases;
    }

    public String chooseClientAlias( String keyType,
                                     Principal[] issuers )
    {
        throw new RuntimeException( "NO IMPLEMENT" );
    }

    public String[] getServerAliases( String keyType,
                                      Principal[] issuers )
    {
        return aliases;
    }

    public String chooseServerAlias( String keyType,
                                     Principal[] issuers )
    {
        throw new RuntimeException( "NO IMPLEMENT" );
    }

    public X509Certificate[] getCertificateChain( String alias )
    {
        return kac[ Integer.parseInt( alias ) ].chain;
    }

    public PrivateKey getPrivateKey( String alias )
    {
        return kac[ Integer.parseInt( alias ) ].key;
    }
} // X509KeyManagerImpl






