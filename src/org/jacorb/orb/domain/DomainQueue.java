package org.jacorb.orb.domain;
/**
 * DomainQueue.java
 * An interface for domain queues.
 *
 * Created: Tue Apr 11 14:19:50 2000
 *
 * @author Herbert Kiefer
 * @version $Revision$
 */

public interface DomainQueue  
{
  /** removes the first element of the queue ant returns it. 
   *  @exception org.jacorb.orb.domain.QueueEmpty if the queue ist empty
   */
  public Domain dequeue() throws EmptyQueueException;

  /** adds a domain element at the end of the queue */
  public void   enqueue(Domain aDomain);

 /** returns the first element of the queue without deleting it. 
   *  @exception org.jacorb.orb.domain.QueueEmpty if the queue ist empty
   */
  public Domain front() throws EmptyQueueException;

  /** checks wheter the queue is empty. */
  public boolean isEmpty();
  
  
} // DomainQueue
