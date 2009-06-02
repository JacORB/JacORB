package org.jacorb.test.bugs.bugjac417;

import java.io.IOException;
import java.net.Socket;

import org.jacorb.orb.factory.SocketFactory;
import org.omg.CORBA.TIMEOUT;

public class SocketFactoryJacORB implements SocketFactory
{
    public org.jacorb.orb.ORB orb;

    public SocketFactoryJacORB(org.jacorb.orb.ORB orb)
    {
        this.orb = orb;
    }

    public Socket createSocket(String host, int port) throws IOException
    {
        return null;
    }

    public Socket createSocket(String host, int port, int timeout)
                                                                  throws IOException,
                                                                  TIMEOUT
    {
        return null;
    }

    public boolean isSSL(Socket socket)
    {
        return false;
    }
}
