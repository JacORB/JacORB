package org.jacorb.trading.util;

/**
 * Simple, straight forward implementation of a binary semaphore.  
 *
 * @author Nicolas Noffke
 */
public class Semaphore {
    private int count;
    
    /**
     * Constructor. Sets this Semaphore up as a binary one.
     *
     * @author Nicolas Noffke
     */
    public Semaphore() {
	count = 1;
    }

    /**
     * Constructor. Sets the initial value of this Semaphore 
     * to start_value
     *
     * @author Nicolas Noffke
     */
    public Semaphore(int start_value){
	count = start_value;
    }

    /**
     * P-Operation. Blocks until somebody else calls V().
     *
     * @author Nicolas Noffke
     */
    public synchronized void P() {
	while (count == 0){ 
	    try{
		wait();
	    } catch (InterruptedException e){
		jacorb.util.Debug.output(0, e);
	    }
	}
	count = 0;
    }
 
    /**
     * V-Operation, unblocks this semaphore
     *
     * @author Nicolas Noffke
     */   
    public synchronized void V() {
	count = 1;
	notifyAll();
    }
}




