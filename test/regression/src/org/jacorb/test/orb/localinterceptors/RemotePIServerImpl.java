package org.jacorb.test.orb.localinterceptors;

import org.omg.CORBA.CompletionStatus;
import org.omg.CORBA.UNKNOWN;
import org.omg.PortableServer.POA;

public class RemotePIServerImpl
        extends PIServerPOA
{
    private POA poa;

    private static final int REMOTE_SERVER_MINOR = 0x666;

    RemotePIServerImpl( POA poa )
    {
        this.poa = poa;
    }

    public void sendMessage (String msg)
    {
        System.out.println ("Remote Server got " + msg);
    }

    public String returnMessage (String msg)
    {
        return "Remote Server got message ";
    }

    public void throwException (String msg)
    {
        throw new UNKNOWN (msg,
                           REMOTE_SERVER_MINOR,
                           CompletionStatus.COMPLETED_YES);
    }

    public POA _default_POA()
    {
        return poa;
    }

    public void shutdown()
    {
        RemoteServer.shutdown();
    }
}
