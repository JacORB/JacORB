
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

package org.jacorb.trading.util;

/**
 * RWLock is a read-write lock; it allows multiple readers or a single
 * writer; priority is given to writers
 */
public class RWLock
{
  private int m_numWaitingReaders = 0;
  private int m_numWaitingWriters = 0;
  private int m_refCount = 0;


  public RWLock()
  {
  }


  /** Acquire a write lock */
  public synchronized void acquireWrite()
  {
    while (m_refCount != 0) {
      m_numWaitingWriters++;
      try {
        wait();
      }
      catch (InterruptedException e) {
      }
      m_numWaitingWriters--;
    }

      // set m_refCount to indicate a write lock is active
    m_refCount = -1;
  }


  /** Acquire a read lock */
  public synchronized void acquireRead()
  {
      // give preference to waiting writers
    while (m_refCount < 0 || m_numWaitingWriters > 0) {
      m_numWaitingReaders++;
      try {
        wait();
      }
      catch (InterruptedException e) {
      }
      m_numWaitingReaders--;
    }

      // increment m_refCount to indicate a read lock is active
    m_refCount++;
  }


  /** Release a lock */
  public synchronized void release()
  {
    if (m_refCount > 0)  // release a reader
      m_refCount--;
    else if (m_refCount == -1)  // release a writer
      m_refCount = 0;
    else
      throw new RuntimeException("refCount should not be 0");

    notifyAll();
  }
}




