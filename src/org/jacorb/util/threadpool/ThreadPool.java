package org.jacorb.util.threadpool;
/**
 * ThreadPool.java
 *
 *
 * Created: Fri Jun  9 15:09:01 2000
 *
 * @author Nicolas Noffke
 * $Id$
 */
import org.jacorb.util.Debug;

public class ThreadPool
{
    private int max_threads = 0;
    private int max_idle_threads = 0;

    private int total_threads = 0;
    private int idle_threads = 0;

    private ThreadPoolQueue job_queue = null;
    private ConsumerFactory factory = null;

    public ThreadPool( ConsumerFactory factory ) 
    {
        this( new LinkedListQueue(),
              factory, 
              10,
              10 );
    }

    public ThreadPool( ConsumerFactory factory,
                       int max_threads,
                       int max_idle_threads) 
    {
        this.job_queue = new LinkedListQueue();
        this.factory = factory;
        this.max_threads = max_threads;
        this.max_idle_threads = max_idle_threads;        
    }

    public ThreadPool( ThreadPoolQueue job_queue,
                       ConsumerFactory factory,
                       int max_threads,
                       int max_idle_threads) 
    {
        this.job_queue = job_queue;
        this.factory = factory;
        this.max_threads = max_threads;
        this.max_idle_threads = max_idle_threads;        
    }

    protected synchronized Object getJob()
    {
        /*
         * This tells the newly idle thread to exit,
         * because there are already too much idle 
         * threads.
         */
        if (idle_threads >= max_idle_threads)
        {
            Debug.output( Debug.DEBUG1 | Debug.TOOLS,
                          "(Pool)[" + idle_threads + "/" + total_threads + 
                          "] Telling thread to exit (too many idle)" );
            
            total_threads--;
            return null;
        }
    
        idle_threads++;
    
        Debug.output( Debug.DEBUG1 | Debug.TOOLS,
                      "(Pool)[" + idle_threads + "/" + total_threads + 
                      "] added idle thread" );

        while( job_queue.isEmpty() )
        {
            try
            {
                Debug.output( Debug.DEBUG1 | Debug.TOOLS,
                              "(Pool)[" + idle_threads + "/" + total_threads + 
                              "] job queue empty" );
                wait();
            }
            catch( InterruptedException e )
            {
                Debug.output( Debug.IMPORTANT | Debug.TOOLS, e );
            }
        }

        idle_threads--;

        Debug.output( Debug.DEBUG1 | Debug.TOOLS,
                      "(Pool)[" + idle_threads + "/" + total_threads + 
                      "] removed idle thread (job scheduled)" );

        return job_queue.removeFirst();
    }

    public synchronized void putJob( Object job )
    {
        job_queue.add(job);
        notifyAll();
    
        if ((idle_threads == 0) && 
            (total_threads < max_threads))
        {
            createNewThread();
        }
    }

    private void createNewThread()
    {
        Debug.output( Debug.DEBUG1 | Debug.TOOLS,
                      "(Pool)[" + idle_threads + "/" + total_threads + 
                      "] creating new thread" );

        (new Thread( new ConsumerTie( this, factory.create() ))).start();

        total_threads++;
    }
} // ThreadPool
