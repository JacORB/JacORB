package org.jacorb.test.bugs.bugjac149;


import java.io.Serializable;
import java.rmi.RemoteException;

/**
 * RMI/IIOP equivalent interface for IPing
 * Supplied by Cisco
 */
public interface RemoteIPing
        extends java.rmi.Remote
{
    /**
     * Method for sending/receiving a serializable object
     */
    public Serializable ping(Serializable o)
            throws RemoteException;
}
