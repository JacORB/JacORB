package org.jacorb.test.bugs.bugjac774;

import org.jacorb.config.JacORBConfiguration;
import org.jacorb.orb.BufferManager;

/**
 * <code>ServerImpl</code> is a simple server for testing BufferManager.
 */
public class ServerImpl extends MyServerPOA
{
    public int testBuffer(int size)
    {
        try
        {
            BufferManager bm = new BufferManager(JacORBConfiguration.getConfiguration(null, org.omg.CORBA.ORB.init (new String[]{}, null), false));
            bm.getBuffer(size);
        }
        catch (OutOfMemoryError e)
        {
            e.printStackTrace();
            return 1;
        }
        catch (org.omg.CORBA.NO_MEMORY e)
        {
            e.printStackTrace();
            return 1;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return -1;
        }
        return 0;
    }

    public int testExpandedBuffer(int size)
    {
        try
        {
            System.out.print ("JAC774: requesting buffer with size: " + size);
            BufferManager bm = new BufferManager(JacORBConfiguration.getConfiguration(null, org.omg.CORBA.ORB.init (new String[]{}, null), false));
            byte buffer[] = bm.getExpandedBuffer (size);
            System.out.println (" done. Returned size is:" + buffer.length);
            return buffer.length;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return -1;
        }
    }
}
