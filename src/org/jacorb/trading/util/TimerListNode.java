package org.jacorb.trading.util;
/**
 * TimerListNode.java
 *
 *
 * Created: Sat Feb  5 11:41:42 2000
 *
 * @author Nicolas Noffke
 * @version $Id$
 */

public class TimerListNode{
  private TimerListNode next = null;
  private boolean do_interrupt = true;
  private boolean interrupt_sent = false;

  public Thread interruptee = null;
  public long wakeup_time;

  public TimerListNode(){}

  public TimerListNode(Thread interruptee, long wakeup_time){
    this.interruptee = interruptee;
    this.wakeup_time = wakeup_time;
  }

  public boolean hasNext(){
    return next != null;
  }

  /**
   * Get the next node of this list. Blocks until
   * a node is available.
   *
   * @return the next node in this list.
   */
  public synchronized TimerListNode getNext(){
    while (next == null)
      try{
	wait();
      }catch(Exception _e){
      }
	
    return next;
  }

  /**
   * Set the following node for this node.
   * Will notify all threads blocked on this node.
   *
   * @param node the next node.
   */
  public synchronized void setNext(TimerListNode node){
    next = node;
    notifyAll();
  }    


  public synchronized void doInterrupt(){
    if (do_interrupt){
      interrupt_sent = true;
      interruptee.interrupt();
    }
  }

  public synchronized void stopTimer(){

    if (! interrupt_sent)
      do_interrupt = false;
    else
      try{
	wait(); //wait for interrupt to come
      }catch(Exception _e){
      }	
  }
} // TimerListNode






