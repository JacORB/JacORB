package org.jacorb.orb.domain;
/**
 * An implementation of the DomainQueue interface with a linked list.
 * Created: Tue Apr 11 14:34:46 2000
 *
 * @author Herbert Kiefer
 * @version $Revision$
 */

public class DomainQueueImpl implements DomainQueue {

  /** points to the head of the list where elements are removed*/
  private Element _head;
 
  /** points to the end of the list where elements are 
   *  inserted */
  private Element _tail;
  
  public DomainQueueImpl() 
  {
    _tail= new Element();
    _head= _tail;
  }

 /** removes the first element of the queue and returns it. 
   *  @exception org.jacorb.orb.domain.QueueEmpty if the queue ist empty
   */
  public Domain dequeue() throws EmptyQueueException
  {
    if (_head == _tail) throw new EmptyQueueException();
    Domain result= _head.el;
    _head= _head.next;
    return result;
  } // dequeue

  /** adds a domain element at the tail of the queue */
  public void   enqueue(Domain aDomain)
  {
    _tail.el= aDomain;
    _tail.next = new Element();
    _tail= _tail.next;
  } // enqueue


 /** returns the first element of the queue without deleting it. 
   *  @exception org.jacorb.orb.domain.QueueEmpty if the queue ist empty
   */
  public Domain front() throws EmptyQueueException
  {
    if (_head == _tail) throw new EmptyQueueException();
    return _head.el;
  } // front


  /** checks wheter the queue is empty. */
  public boolean isEmpty()
  {
    return (_head == _tail);
  }
  
} // DomainQueueImpl



// inner class Element 
/** An element of a domain queue. */
class Element
{
  public Domain el;
  public Element next;
}
