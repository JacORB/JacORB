package org.jacorb.proxy;

import org.jacorb.orb.ParsedIOR;
import org.jacorb.orb.connection.ClientConnection;

public class MiniStub
{

    private ClientConnection con = null;
    private ParsedIOR pIOR = null;

    MiniStub( ClientConnection c,
              ParsedIOR p )
    {
        con = c;
        pIOR = p;
    }

    public ClientConnection getConnection()
    {
        return  con;
    }

    public ParsedIOR getParsedIOR()
    {
        return pIOR;
    }
}






