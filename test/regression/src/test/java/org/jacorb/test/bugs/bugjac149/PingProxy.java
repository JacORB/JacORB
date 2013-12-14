package org.jacorb.test.bugs.bugjac149;

import java.io.Serializable;
import java.rmi.RemoteException;

/**
 * Client-side proxy adapter from IPing to RemoteIPing
 * Supplied by Cisco
 */
public class PingProxy
        implements IPing
{
    private RemoteIPing remoteRef;

    /**
     * Creates a new PingProxy object.
     *
     * @param remoteRef +
     */
    public PingProxy(RemoteIPing remoteRef)
    {
        this.remoteRef = remoteRef;
    }

    /**
     * ping() impl.
     *
     */
    public Serializable ping(Serializable o)
    {
        try
        {
            return remoteRef.ping(o);
        }
        catch (RemoteException e)
        {
            e.printStackTrace();
        }
        return null;
    }
}
