package org.jacorb.security.ssl.sun_jsse;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 2000-2012 Gerald Brose / The JacORB Team.
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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import org.jacorb.util.ObjectUtil;

/**
 * A class with utility methods that help managing a key store.
 *
 * @author Gerald Brose
 */
public class KeyStoreUtil
{
    /**
     * @return - a fully loaded and operational KeyStore
     * @param file_name - a keystore file name to be loaded
     * @param storepass - the password for managing the keystore
     * @param keystoreType
     * @param keystoreProvider
     */
    static KeyStore getKeyStore (String file_name, char[] storepass, String keystoreType)
        throws IOException, java.security.GeneralSecurityException
    {
        return getKeyStore (file_name, storepass, keystoreType, null);
    }

        /**
     * @return - a fully loaded and operational KeyStore
     * @param file_name - a keystore file name to be loaded
     * @param storepass - the password for managing the keystore
     * @param keystoreType
     * @param keystoreProvider
     */
    static KeyStore getKeyStore (String file_name, char[] storepass, String keystoreType, String keystoreProvider)
        throws IOException, java.security.GeneralSecurityException
    {
        KeyStore ks = KeyStore.getInstance(keystoreType);

        // Bugzilla #883: PKCS 11 and CRL support for SSL
        if ("PKCS11".equalsIgnoreCase (keystoreType)) 
        {
            // create key store with specified provider
            ks = KeyStore.getInstance (keystoreType, keystoreProvider);
        }
        else
        {
            ks = KeyStore.getInstance(keystoreType);
        }

        InputStream in = null;
        
        // Process key store file strictly for the JKS key store type
        if ("JKS".equalsIgnoreCase (keystoreType))
        {
            java.net.URL url =  ObjectUtil.getResource(file_name);
            if (url != null)
            {
                in = url.openStream();
            }
            else
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

                    if(f.exists())
                    {
                        in = new FileInputStream( f );
                    }
                }
                else
                {
                    in = new FileInputStream( f );
                }
            }

            if (in == null)
            {
                throw new IOException("Unable to find keystore file " +
                                      file_name);
            }
        }

        // Bugzilla #883: PKCS 11 and CRL support for SSL
        // Store password for "WINDOWS-MY" store type doesn't need to be set
        if ("WINDOWS-MY".equalsIgnoreCase (keystoreType))
        {
            storepass = null;
        }
        
        ks.load( in, storepass );
        
        if (in != null)
        {
            in.close();
        }
        return ks;
    }
}
