package org.jacorb.orb;

/**
 * ImRAccess.java
 *
 *
 * Created: Thu Jan 31 20:55:32 2002
 *
 * @author Nicolas Noffke
 * @version $Id$
 */

public interface ImRAccess 
{
    public void connect( org.omg.CORBA.ORB orb )
        throws org.omg.CORBA.INTERNAL;

    public String getImRHost();
    public int getImRPort();

    public void registerPOA( String name, 
                             String server,
                             String host, 
                             int port)
        throws org.omg.CORBA.INTERNAL;

    public void setServerDown( String name )
        throws org.omg.CORBA.INTERNAL;
    
}// ImRAccess
