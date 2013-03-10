package org.jacorb.test.listenendpoints.echo;

import java.net.InetAddress;
import org.omg.CORBA.*;

public class EchoMessageImpl
    extends EchoMessagePOA
{
    private String location = "";

    public EchoMessageImpl(String location)
    {
        this.location = location;
    }

    public void ping()
    {
        return;
    }

    public String echo_simple()
    {

        try
        {
            String hostname =  InetAddress.getLocalHost().getHostName();
            return new String("Simple greeting from " + location + " " + hostname);
        }
        catch (Exception e)
        {
            // ignore unknown host exception
            return new String("Simple greeting from " + location);
        }
    }

    public String echo_wide(String wide_msg)
    {
        return new String(wide_msg);
    }
}
