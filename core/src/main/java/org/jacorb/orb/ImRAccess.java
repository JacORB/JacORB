/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1999-2012 Gerald Brose / The JacORB Team.
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
package org.jacorb.orb;

/**
 * ImRAccess.java
 *
 *
 * Created: Thu Jan 31 20:55:32 2002
 *
 * @author Nicolas Noffke
 */
import java.util.List;
import org.omg.ETF.Profile;

public interface ImRAccess
{
    public String getImRHost();
    public int getImRPort();
    public org.jacorb.orb.etf.ProtocolAddressBase getImRAddress();
    public String getImRCorbaloc ();
    public List<Profile> getImRProfiles();
    public void registerPOA( String name,
                             String server,
                             org.jacorb.orb.etf.ProtocolAddressBase address)
        throws org.omg.CORBA.INTERNAL;
    public void registerPOA( String name,
                             String server,
                             String host,
                             int port)
        throws org.omg.CORBA.INTERNAL;

    public void registerPOA( org.jacorb.orb.ORB orb,
                             org.jacorb.poa.POA poa,
                             org.jacorb.orb.etf.ProtocolAddressBase address,
                             String implname)
        throws org.omg.CORBA.INTERNAL;

    public void setServerDown( String name );

    public void setServerDown( org.jacorb.orb.ORB orb,
                               org.jacorb.poa.POA poa,
                               String implname );
}// ImRAccess
