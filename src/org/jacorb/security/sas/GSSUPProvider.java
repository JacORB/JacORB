package org.jacorb.security.sas;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 2002-2004 Gerald Brose
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

import java.security.Provider;

/**
 * This is the GSS-API Sercurity Provider Interface (SPI) Provider for the GSSUP Name
 *
 * @author David Robison
 * @version $Id$
 */

public class GSSUPProvider 
    extends Provider
{
    //private static InitialContextToken defaultSubject = new InitialContextToken();
    private org.omg.CORBA.ORB orb = null;
    private org.omg.IOP.Codec codec = null;

    /**
     * Returns the default GSSManager implementation.
     *
     */
    public GSSUPProvider(org.omg.CORBA.ORB orb)
    {
        super("GSSUP", 1.0, "JacORB GSSUP provider v1.0");
        GSSUPMechFactory.myProvider = this;
        this.put("GssApiMechanism.2.23.130.1.1.1", 
                 "org.jacorb.security.sas.GSSUPMechFactory");
        this.orb = orb;
    }
   
    public org.omg.CORBA.ORB getORB()
    {
        return orb;
    }

    public void setORB(org.omg.CORBA.ORB orb)
    {
        this.orb = orb;
    }
    
    public org.omg.IOP.Codec getCodec()
    {
        return codec;
    }

    public void setCodec(org.omg.IOP.Codec codec)
    {
        this.codec = codec;
    }
    

    /*
    public static void setDefaultSubject(String username, String password, String target)
    {
        defaultSubject.username = username.getBytes();
        defaultSubject.password = password.getBytes();
        defaultSubject.target_name = target.getBytes();
    }

    public static InitialContextToken getDefaultSubject()
    {
        return defaultSubject;
    }
    */

}
