package org.jacorb.security.sas;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 2002-2002 Gerald Brose
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

import sun.security.jgss.spi.*;
import org.ietf.jgss.*;
import java.security.*;
import java.io.*;

/**
 * This is the GSS-API Sercurity Provider Interface (SPI) for the GSSUP Context
 *
 * @author David Robison
 * @version $Id$
 */

public final class GSSUPContextSpi implements GSSContextSpi
{
    private Provider myProvider = null;
    private Oid myMechOid = null;
    private int lifetime;
    private int initContextState = 0;
    private byte[] innerToken = null;
    private boolean mutualAuth = false;
    private boolean relayDet = false;
    private boolean sequenceDet = false;
    private boolean credDeleg = false;
    private boolean anonymity = false;
    private boolean conf = false;
    private boolean integ = false;
    private ChannelBinding channelBinding = null;

    public GSSUPContextSpi (Provider myProvider, Oid myMechOid, int lifetime)
    {
        this.myProvider = myProvider;
        this.myMechOid = myMechOid;
        this.lifetime = lifetime;
    }

    public Provider getProvider()
    {
        return myProvider;
    }

    public void requestLifetime(int lifetime) throws GSSException
    {
        this.lifetime = lifetime;
    }

    public void requestMutualAuth(boolean tf) throws GSSException
    {
        mutualAuth = tf;
    }

    public void requestReplayDet(boolean tf) throws GSSException
    {
        relayDet = tf;
    }

    public void requestSequenceDet(boolean tf) throws GSSException
    {
        sequenceDet = false;
    }

    public void requestCredDeleg(boolean tf) throws GSSException
    {
        credDeleg = tf;
    }

    public void requestAnonymity(boolean tf) throws GSSException
    {
        anonymity = tf;
    }

    public void requestConf(boolean tf) throws GSSException
    {
        conf = tf;
    }

    public void requestInteg(boolean tf) throws GSSException
    {
        integ = tf;
    }

    public void setChannelBinding(ChannelBinding cb) throws GSSException
    {
        channelBinding = cb;
    }

    public boolean getCredDelegState()
    {
        return credDeleg;
    }

    public boolean getMutualAuthState()
    {
        return mutualAuth;
    }

    public boolean getReplayDetState()
    {
        return relayDet;
    }

    public boolean getSequenceDetState()
    {
        return sequenceDet;
    }

    public boolean getAnonymityState()
    {
        return anonymity;
    }

    public boolean isTransferable() throws GSSException
    {
        //System.out.println("GSSUPContextSpi.isTransferable");
        return false;
    }

    public boolean isProtReady()
    {
        //System.out.println("GSSUPContextSpi.isProtReady");
        return false;
    }

    public boolean getConfState()
    {
        return conf;
    }

    public boolean getIntegState()
    {
        return integ;
    }

    public int getLifetime()
    {
        return lifetime;
    }

    public boolean isEstablished()
    {
        return (initContextState >= 4);
    }

    public GSSNameSpi getSrcName() throws GSSException
    {
        //System.out.println("GSSUPContextSpi.getSrcName");
        return null;
    }

    public GSSNameSpi getTargName() throws GSSException
    {
        //System.out.println("GSSUPContextSpi.getTargName");
        return null;
    }

    public Oid getMech() throws GSSException
    {
        return myMechOid;
    }

    public GSSCredentialSpi getDelegCred() throws GSSException
    {
        System.out.println("GSSUPContextSpi.getDelegCred");
        return null;
    }

    public byte[] initSecContext(InputStream inStream, int inLen) throws GSSException {
        byte[] newBytes = null;
        initContextState++;
        switch (initContextState) {
        case 1: newBytes = myMechOid.getDER(); break;
        case 2: newBytes = GSSUPProvider.getDefaultSubject().username; break;
        case 3: newBytes = GSSUPProvider.getDefaultSubject().password; break;
        case 4: newBytes = GSSUPProvider.getDefaultSubject().target_name; break;
        }
        byte[] outBytes = new byte[inLen + newBytes.length];
        try
        {
            inStream.read(outBytes, 0, inLen);
        }
        catch (java.io.IOException e)
        {
            System.out.println("Error reading context: "+e);
        }
        System.arraycopy(outBytes, inLen, newBytes, 0, newBytes.length);
        innerToken = outBytes;
        return outBytes;
    }

    public byte[] acceptSecContext(InputStream inStream, int i1) throws GSSException
    {
        //System.out.println("GSSUPContextSpi.acceptSecContext");
        return null;
    }

    public int getWrapSizeLimit(int i1, boolean b1, int i2) throws GSSException
    {
        //System.out.println("GSSUPContextSpi.getWrapSizeLimit");
        return 0;
    }

    public void wrap(InputStream inStream, OutputStream outStream, MessageProp mp) throws GSSException
    {
        //System.out.println("GSSUPContextSpi.wrap");
    }

    public byte[] wrap(byte[] b, int i1, int i2, MessageProp mp) throws GSSException
    {
        //System.out.println("GSSUPContextSpi.wrap");
        return null;
    }

    public int wrap(byte[] b1, int i1, int i2, byte[] b2, int i3, MessageProp mp) throws GSSException
    {
        //System.out.println("GSSUPContextSpi.wrap");
        return 0;
    }

    public void wrap(byte[] b, int i1, int i2, OutputStream outStream, MessageProp mp) throws GSSException
    {
        //System.out.println("GSSUPContextSpi.wrap");
    }

    public void unwrap(InputStream inStream, OutputStream outStream, MessageProp mp) throws GSSException
    {
        //System.out.println("GSSUPContextSpi.unwrap");
    }

    public byte[] unwrap(byte[] b, int i1, int i2, MessageProp mp) throws GSSException
    {
        //System.out.println("GSSUPContextSpi.unwrap");
        return null;
    }

    public int unwrap(byte[] b1, int i1, int i2, byte[] b2, int i3, MessageProp mp) throws GSSException
    {
        //System.out.println("GSSUPContextSpi.unwrap");
        return 0;
    }

    public int unwrap(InputStream inStream, byte[] b, int i1, MessageProp mp) throws GSSException
    {
        //System.out.println("GSSUPContextSpi.unwrap");
        return 0;
    }

    public void getMIC(InputStream inStream, OutputStream outStream, MessageProp mp) throws GSSException
    {
        //System.out.println("GSSUPContextSpi.getMIC");
    }

    public byte[] getMIC(byte[] b1, int i1, int i2, MessageProp mp) throws GSSException
    {
        //System.out.println("GSSUPContextSpi.getMIC");
        return null;
    }

    public void verifyMIC(InputStream inStream1, InputStream inStream2, MessageProp mp) throws GSSException
    {
        //System.out.println("GSSUPContextSpi.verifyMIC");
    }

    public void verifyMIC(byte[] b1, int i1, int i2, byte[] b2, int i3, int i4, MessageProp mp) throws GSSException
    {
        //System.out.println("GSSUPContextSpi.verifyMIC");
    }

    public byte[] export() throws GSSException
    {
        return null;
    }

    public void dispose() throws GSSException
    {
        channelBinding = null;
        myProvider = null;
        myMechOid = null;
        //System.out.println("GSSUPContextSpi.dispose");
    }
}

