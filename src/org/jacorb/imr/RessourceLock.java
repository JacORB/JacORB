package org.jacorb.imr;

import org.jacorb.util.Debug;
/**
 * This class provides shared or exclusive access to a ressource.
 * It preferes the exclusive access, i.e. if threads are waiting for 
 * exclusive access, shared locks can't be gained.
 *
 * @author Nicolas Noffke
 * 
 * $Id$
 *
 */

public class RessourceLock implements java.io.Serializable {
    private int shared;
    private int exclusive;
    private boolean exclusives_waiting = false;

    /**
     * The constructor.
     **/
    public RessourceLock() {
	shared = 0;
	exclusive = 0;
    }

    /**
     * This method tries to aquire a shared lock. It blocks
     * until the exclusive lock is released.
     **/
    public synchronized void gainSharedLock(){
	while(exclusive > 0 && exclusives_waiting){
	    try{
		wait();
	    }catch (java.lang.Exception _e){
		Debug.output(Debug.IMR | Debug.INFORMATION, _e);
	    }
	}
	shared++;
    }

    /**
     * Release the shared lock. Unblocks threads waiting for
     * access.
     **/
    public synchronized void releaseSharedLock(){
	if (--shared == 0)
	    notifyAll();
    }

    /**
     * This method tries to aquire an exclusive lock. It blocks until
     * all shared locks have been released.
     **/
    public synchronized void gainExclusiveLock(){
	while(shared > 0 || exclusive > 0){
	    try{
		exclusives_waiting = true;
		wait();
	    }catch (java.lang.Exception _e){
		Debug.output(Debug.IMR | Debug.INFORMATION, _e);
	    }
	}
	exclusive++;
	exclusives_waiting = false;
    }

    /**
     * Releases the exclusive lock. Unblocks all threads waiting
     * for access.
     **/
    public synchronized void releaseExclusiveLock(){
	if (--exclusive == 0)
	    notifyAll();
    }

} // RessourceLock


