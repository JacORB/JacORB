
// Copyright (C) 1998-2001
// Object Oriented Concepts, Inc.

// **********************************************************************
//
// Copyright (c) 1997
// Mark Spruiell (mark@intellisoft.com)
//
// See the COPYING file for more information
//
// **********************************************************************

package org.jacorb.trading.util;

/**
 * MessageQueue is a thread-safe queue suitable for inter-thread communication
 */
public class MessageQueue
{
  private boolean m_deactivated = false;
  private Element m_head = null;
  private Element m_tail = null;


  private static class Element
  {
    public Object value;
    public Element next;

    public Element(Object val)
    {
      value = val;
      next = null;
    }
  }


  public MessageQueue()
  {
  }


  public synchronized void deactivate()
  {
    m_deactivated = true;
    notifyAll();
  }


  public synchronized boolean getDeactivated()
  {
    return m_deactivated;
  }


  public synchronized boolean getEmpty()
  {
    return (m_head == null);
  }


  public synchronized void enqueue(Object value)
  {
    Element elem = new Element(value);
    if (m_tail == null)
      m_head = m_tail = elem;
    else {
      m_tail.next = elem;
      m_tail = elem;
    }

      // notify any waiting threads that a value is available
    notifyAll();
  }


  public synchronized Object dequeue()
  {
    Object result = null;

    while (result == null && ! m_deactivated) {
        // if there are no elements available, then block until there are
      if (m_head == null) {
        try {
          wait();
        }
        catch (InterruptedException e) {
        }
      }
      else {
          // otherwise remove the first element on the queue
        result = m_head.value;
        m_head = m_head.next;
        if (m_head == null)
          m_tail = null;
      }
    }

    return result;
  }


  /******************* comment out to enable main()

  public static void main(String[] args)
  {
    MessageQueue queue = new MessageQueue();

      // create a few reader threads
    for (int i = 1; i <= 3; i++) {
      Thread t = new Reader(i, queue);
      t.setPriority(Thread.currentThread().getPriority() + 1);
      t.start();
    }

    for (int i = 0; i < 10; i++) {
      Integer value = new Integer(i);
      System.out.println("Main - enqueuing " + value);
      queue.enqueue(value);
    }

      // wait for queue to empty
    while (! queue.getEmpty()) {
      try {
        Thread.sleep(500);
      }
      catch (InterruptedException e) {
      }
    }

      // shut off the queue, which should stop all threads
    queue.deactivate();
  }


  protected static class Reader extends Thread
  {
    private int m_id;
    private MessageQueue m_queue;

    public Reader(int id, MessageQueue queue)
    {
      m_id = id;
      m_queue = queue;
      System.out.println("Thread " + m_id + " starting up...");
    }

    public void run()
    {
      Object value;
      while ((value = m_queue.dequeue()) != null) {
        Integer i = (Integer)value;
        System.out.println("Thread " + m_id + " received " + i);
        try {
          Thread.sleep(500);
        }
        catch (InterruptedException e) {
        }
      }
      System.out.println("Thread " + m_id + " done...");
    }
  }

  /******************* comment out to enable main() */
}










