package org.jacorb.test.orb;

public class PostInitFail
    extends org.omg.CORBA.LocalObject
    implements org.omg.PortableInterceptor.ORBInitializer
{
    private static int preCount;
    private static int pstCount;

    public void pre_init(org.omg.PortableInterceptor.ORBInitInfo info){
        preCount++;
    }

    public void post_init(org.omg.PortableInterceptor.ORBInitInfo info)
    {
        pstCount++;
        throw new RuntimeException("Sorry");
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

