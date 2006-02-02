package test.servantscaling;

import org.omg.CORBA.*;
import org.omg.CORBA.ORBPackage.*;
import org.omg.PortableServer.Current;

public class SessionServant extends SessionPOA
{
    private org.omg.PortableServer.Current poaCurrent;
    private byte id[];

    public SessionServant (org.omg.PortableServer.Current c)
    {
        poaCurrent = c;
        id = null;
    }

    public SessionServant ()
    {
        this.id = null;
        poaCurrent = null;
    }

    public SessionServant (byte id[])
    {
        this.id = id;
        poaCurrent = null;
    }

    public void setID (byte id[] )
    {
        this.id = id;
    }

    public String getID()
    {
        try {
            return new String(id == null ? poaCurrent.get_object_id() : id);
        } catch (Exception e) {}
        return null;
    }
}
