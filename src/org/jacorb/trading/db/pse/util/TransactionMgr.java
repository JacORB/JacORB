
// Copyright (C) 1998-1999
// Object Oriented Concepts, Inc.

// **********************************************************************
//
// Copyright (c) 1997
// Mark Spruiell (mark@intellisoft.com)
//
// See the COPYING file for more information
//
// **********************************************************************

package org.jacorb.trading.db.pse.util;

import COM.odi.*;


/**
 * ObjectStore PSE does not allow nested transactions, nor does it allow
 * multiple active sessions, so we have to coordinate access to transactions
 * to prevent multiple threads from entering a transaction.
 */
public class TransactionMgr
{
  private Thread m_mainThread;
  private Thread m_owner = null;
  private int m_refCount = 0;


  public TransactionMgr()
  {
      // remember the main thread
    m_mainThread = Thread.currentThread();
  }


  public synchronized void begin()
  {
      // initialize this thread with the database
    ObjectStore.initialize(m_mainThread);

    Thread currentThread = Thread.currentThread();

      // allow the same thread to acquire more than once
    if (currentThread != m_owner) {
      while (m_refCount != 0) {
        try {
          wait();
        }
        catch (InterruptedException e) {
        }
      }
    }

      // since we allow the same thread to call begin() more than once,
      // we don't want to worry about conflicting transaction modes
    if (m_refCount == 0)
      Transaction.begin(ObjectStore.UPDATE);

    m_owner = currentThread;
    m_refCount++;
  }


  public synchronized void commit(int retain)
  {
    Thread currentThread = Thread.currentThread();
    if (currentThread != m_owner)
      throw new RuntimeException("Thread not owner");

    if (m_refCount > 0)
      m_refCount--;
    else if (m_refCount == 0)
      throw new RuntimeException("refCount should not be 0");

      // commit the transaction if this is the last release()
    if (m_refCount == 0) {
      Transaction.current().commit(retain);
        // disconnect this thread from the database
      ObjectStore.shutdown(false);
      m_owner = null;
      notifyAll();
    }
  }
}




