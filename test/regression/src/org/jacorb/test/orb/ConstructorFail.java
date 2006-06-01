package org.jacorb.test.orb;

public class ConstructorFail     
    extends org.omg.CORBA.LocalObject
    implements org.omg.PortableInterceptor.ORBInitializer
{
    public ConstructorFail()
        throws Exception
    {
        throw new Exception("Sorry");
    }
    
    public void pre_init(org.omg.PortableInterceptor.ORBInitInfo info){};
    public void post_init(org.omg.PortableInterceptor.ORBInitInfo info){};
}
