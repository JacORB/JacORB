package org.jacorb.security.sas;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 2002-2003 Gerald Brose
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

import java.io.InputStream;
import java.io.OutputStream;
import java.security.Provider;

import org.apache.avalon.framework.logger.Logger;
import org.ietf.jgss.ChannelBinding;
import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSException;
import org.ietf.jgss.MessageProp;
import org.ietf.jgss.Oid;

import sun.security.jgss.spi.GSSContextSpi;
import sun.security.jgss.spi.GSSCredentialSpi;
import sun.security.jgss.spi.GSSNameSpi;

/**
 * This is the GSS-API Sercurity Provider Interface (SPI) for the GSSUP Context
 *
 * @author David Robison
 * @version $Id$
 */

public final class GSSUPContextSpi implements GSSContextSpi
{
	/** the logger used by the naming service implementation */
	private static Logger logger = org.jacorb.util.Debug.getNamedLogger("jacorb.SAS");

    private Provider provider = null;
    private Oid mechOid = null;
    private int lifetime;
    private boolean mutualAuth = false;
    private boolean relayDet = false;
    private boolean sequenceDet = false;
    private boolean credDeleg = false;
    private boolean anonymity = false;
    private boolean conf = false;
    private boolean integ = false;
    private boolean established = false;
    private ChannelBinding channelBinding = null;

    private GSSNameSpi targetName;
    private GSSCredentialSpi sourceCred;

    public GSSUPContextSpi (Provider provider, Oid mechOid, GSSNameSpi nameSpi, GSSCredentialSpi credSpi, int lifetime)
    {
        this.provider = provider;
        this.mechOid = mechOid;
        this.targetName = nameSpi;
        this.sourceCred = credSpi;
        this.lifetime = lifetime;
    }

    public Provider getProvider()
    {
        return provider;
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
        return true;
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
        return established;
    }

    public GSSNameSpi getSrcName() throws GSSException
    {
        //System.out.println("GSSUPContextSpi.getSrcName");
        return sourceCred.getName();
    }

    public GSSNameSpi getTargName() throws GSSException
    {
        //System.out.println("GSSUPContextSpi.getTargName");
        return targetName;
    }

    public Oid getMech() throws GSSException
    {
        return mechOid;
    }

    public GSSCredentialSpi getDelegCred() throws GSSException
    {
        //System.out.println("GSSUPContextSpi.getDelegCred");
        return null;
    }

    public byte[] initSecContext(InputStream inStream, int inLen) throws GSSException {
        established = true;
        return sourceCred.getName().toString().getBytes();
    }

    public byte[] acceptSecContext(InputStream inStream, int inLen) throws GSSException
    {
        //System.out.println("GSSUPContextSpi.acceptSecContext");
        established = true;
        try
        {
            byte[] inBytes = new byte[inStream.available()];
            inStream.read(inBytes);
            GSSNameSpi sourceName = new GSSUPNameSpi(provider, mechOid, inBytes, null);
            sourceCred = new GSSUPCredentialSpi(provider, mechOid, sourceName, GSSCredential.DEFAULT_LIFETIME, GSSCredential.DEFAULT_LIFETIME, GSSCredential.ACCEPT_ONLY);
        }
        catch (Exception e)
        {
            logger.error("Error acceptSecContext: " + e);
        }
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
        //System.out.println("GSSUPContextSpi.export");
        return null;
    }

    public void dispose() throws GSSException
    {
        //System.out.println("GSSUPContextSpi.dispose");
        channelBinding = null;
        provider = null;
        mechOid = null;
    }
}
