package org.jacorb.security.ssl;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1999  André Benvenuti.
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


/**
 * SSL configuration for JacORB.
 *
 * Written 09.12.99 by André Benvenuti to support SSL
 *
 */

public class SSLSetup
{
    // only strong cipher suites are accepted and rsa key exchange
    private static final iaik.security.ssl.CipherSuite[] cs = {
        iaik.security.ssl.CipherSuite.SSL_RSA_WITH_IDEA_CBC_SHA,
        iaik.security.ssl.CipherSuite.SSL_RSA_WITH_3DES_EDE_CBC_SHA
    };
    protected static final int[][] cipher_suite_props = {
        {iaik.security.ssl.CipherSuite.SSL_RSA_WITH_IDEA_CBC_SHA.getID(), 0x66},
        {iaik.security.ssl.CipherSuite.SSL_RSA_WITH_3DES_EDE_CBC_SHA.getID(),0x66}};
    protected static final byte[] types = {iaik.security.ssl.ClientTrustDecider.rsa_sign
                                           //    , iaik.security.ssl.ClientTrustDecider.dss_sign};
    };

    /**
     * GB: added to avoid having to access the instance variable
     */

    public static iaik.security.ssl.CipherSuite[] getCipherSuites()
    {
        return cs;
    }

    public static boolean isSSL( java.net.Socket s )
    {
        return ( s instanceof iaik.security.ssl.SSLSocket );
    }

    public static String getMechanismType()
    {
        String sslMechType = "20,"
                           + iaik.security.ssl.CipherSuite.SSL_RSA_WITH_IDEA_CBC_SHA.getID()
                           + ","
                           + iaik.security.ssl.CipherSuite.SSL_RSA_WITH_3DES_EDE_CBC_SHA.getID();
        return sslMechType;
    }

}



