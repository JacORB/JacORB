package org.jacorb.util.threadpool;
/**
 * ConsumerTie.java
 *
 *
 * Created: Fri Jun  9 15:44:26 2000
 *
 * @author Nicolas Noffke
 * $Id$
 */
import org.jacorb.util.Debug;

public  class ConsumerTie
  implements Runnable
{

    private boolean run = true;
    private ThreadPool pool = null;
    private Consumer delegate = null;

    public ConsumerTie( ThreadPool pool,
                        Consumer delegate )
    {
        this.pool = pool;
        this.delegate = delegate;
    }
	
    public void run()
    {
        try
        {
            while( run )
            {
                Object job = pool.getJob();
      
                if( job == null )
                {
                    /*
                     * job == null is sent by the pool, if there are
                     * too much idle threads. Therefore we exit.
                     */
                    return;
                }
                else
                {
                    delegate.doWork( job );
                }
            }
        }
        catch( Exception e )
        {
            Debug.output( Debug.IMPORTANT | Debug.TOOLS, e );
            return;
        }
    }
} // ConsumerTie
