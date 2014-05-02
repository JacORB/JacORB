package org.jacorb.orb.miop;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.jacorb.orb.etf.StreamConnectionBase;
import org.omg.CORBA.COMM_FAILURE;

/**
 * Abstract MIOP connection. Some default methods are implemented.
 *
 * @author  Alysson Neves Bessani
 * @version 1.0
 * @see ClientMIOPConnection ServerMIOPConnection
 */
public abstract class MIOPConnection extends StreamConnectionBase
{
    /**
     * Creates a new MIOP Connection for a specified group profile
     */
    public MIOPConnection()
    {
       super();
       out_stream = new ByteArrayOutputStream();
    }

    /**
     * Plugin doesn't support BiDir.
     *
     * @return false.
     */
    public boolean supports_callback()
    {
        return false;
    }

    /**
     * By default, this method return false (client behaviour).
     *
     * @return false.
     */
    public boolean is_data_available()
    {
        return false;
    }


    /**
     * Not supporting SSL over MIOP transport
     */
    public boolean isSSL()
    {
        return false;
    }


    protected COMM_FAILURE handleCommFailure(IOException e)
    {
        return to_COMM_FAILURE(e);
    }
}
