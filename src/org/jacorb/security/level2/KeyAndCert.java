/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1999-2004 Gerald Brose
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
 *
 */
package org.jacorb.security.level2;

import java.security.PrivateKey;
import java.security.cert.Certificate;

/**
 * KeyAndCert.java
 *
 *
 * Created: Mon Sep  4 16:33:49 2000
 *
 * @author Nicolas Noffke
 * @version $Id$
 */

public class KeyAndCert
{
    //might stay null for received creds
    public PrivateKey key = null; 

    public Certificate[] chain = null;
    
    public KeyAndCert( PrivateKey key,
                       Certificate[] chain )
    {
        this.key = key;
        this.chain = chain;
    }
    
    public KeyAndCert( KeyAndCert source )
    {
        this.key = source.key;
        
        chain = new Certificate[ source.chain.length ];
        System.arraycopy( source.chain, 0, chain, 0, source.chain.length );
    }

    public String toString()
    {
        StringBuffer sb = new StringBuffer();
        for( int i = 0; i < chain.length; i++ )
        {
            sb.append( chain[i].toString() );
        }
        return sb.toString();
    }

    /**
     * return the public key of the first Certificate in the chain, if
     * there are any certs. Return null otherwise 
     */

    public java.security.PublicKey getPublicKey()
    {
        if( chain.length > 0 )
            return chain[0].getPublicKey();
        else
            return null;
    }


} // KeyAndCert






