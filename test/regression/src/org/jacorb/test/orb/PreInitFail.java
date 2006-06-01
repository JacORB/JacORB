package org.jacorb.test.orb;

public class PreInitFail
    extends org.omg.CORBA.LocalObject
    implements org.omg.PortableInterceptor.ORBInitializer
{
    private static int preCount = 0;
    private static int pstCount = 0;

    public void pre_init(org.omg.PortableInterceptor.ORBInitInfo info)
    {
        preCount++;
        throw new RuntimeException("Sorry");
    }

    public void post_init(org.omg.PortableInterceptor.ORBInitInfo info){
        pstCount++;
    }

    public static void reset()
    {
        preCount = 0;
        pstCount = 0;
    }

    public static int getPreCount()
    {
        return preCount;
    }

    public static int getPstCount()
    {
        return pstCount;
    }
}
