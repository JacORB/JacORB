package test.memoryusage;

import org.omg.CORBA.*;
import org.omg.CORBA.ORBPackage.*;

public class SessionServant extends SessionPOA
{
    private byte[] weight = new byte[10000];

    public String getID()
    {
        return toString();
    }
}
