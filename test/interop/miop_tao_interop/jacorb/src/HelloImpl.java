package miop_tao_interop;

import org.omg.CORBA.*;

public class HelloImpl extends HelloPOA
{
    private ORB orb;
    private UIPMC_Object obj;

    public HelloImpl (ORB orb, UIPMC_Object obj)
    {
        this.orb = orb;
        this.obj = obj;
    }

    public UIPMC_Object get_object()
    {
        return this.obj;
    }

    public void shutdown()
    {
        this.orb.shutdown(false);
    }
}
