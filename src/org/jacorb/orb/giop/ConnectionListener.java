package org.jacorb.orb.connection;

/**
 * ConnectionListener.java
 *
 *
 * Created: Thu Oct  4 15:56:02 2002
 *
 * @author Nicolas Noffke
 * @version $Id$
 */

public interface ConnectionListener 
{   
    public void connectionClosed();
    public void connectionTimedOut();
    public void streamClosed();
}// ConnectionListener
