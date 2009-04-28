
package org.jacorb.test.orb.policies;

import org.jacorb.test.*;

public class SyncScopeServerImpl extends SyncScopeServerPOA
{
    private int onewayCount = 0;

    public void oneway_op(int delay)
    {
        synchronized (this)
        {
            onewayCount++;
            try
            {
                if (delay > 0) this.wait (delay);
            }
            catch (InterruptedException ex)
            {
                System.out.println ("wait interrupted");
            }
        }
    }

    public void operation(int delay)
    {
        synchronized (this)
        {
            try
            {
                if (delay > 0) this.wait (delay);
            }
            catch (InterruptedException ex)
            {
                System.out.println ("wait interrupted");
            }
        }
    }

    public int get_oneway_count()
    {
        synchronized(this)
        {
            return onewayCount;
        }
    }
}
