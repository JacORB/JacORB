package org.jacorb.test.bugs.bugjac149;

import java.io.Serializable;
import java.rmi.RemoteException;
import javax.rmi.PortableRemoteObject;


/**
 * Server-side impl of RemoteIPing. Adapts RemoteIPing to IPing
 * Supplied by Cisco
 */
public class RemoteIPingImpl
        extends PortableRemoteObject
        implements RemoteIPing
{
    private IPing worker;

    /**
     * Creates a new RemoteIPingImpl object.
     */
    public RemoteIPingImpl(IPing w)
            throws RemoteException
    {
        worker = w;
    }

    /**
     * ping() impl
     *
     */
    public Serializable ping(Serializable o)
            throws RemoteException
    {
        return worker.ping(o);
    }
}
