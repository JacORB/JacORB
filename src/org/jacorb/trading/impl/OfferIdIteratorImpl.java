
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

package org.jacorb.trading.impl;

import org.omg.CosTrading.*;
//import jtport.ORBLayer;


/**
 * Implementation of CosTrading::OfferIdIterator
 */
public class OfferIdIteratorImpl extends OfferIdIteratorPOA
{
  private String[] m_offerIds;
  private int m_start;


  private OfferIdIteratorImpl()
  {
  }


  public OfferIdIteratorImpl(String[] offerIds)
  {
    m_offerIds = offerIds;
    m_start = 0;
  }


  public int max_left()
    throws UnknownMaxLeft
  {
    return (m_offerIds.length - m_start);
  }


  public boolean next_n(int n, OfferIdSeqHolder offers)
  {
    boolean result;

    int count;
    if (n > (m_offerIds.length - m_start))
      count = m_offerIds.length - m_start;
    else
      count = n;

    offers.value = new String[count];
    for (int i = 0; i < count; i++)
      offers.value[i] = m_offerIds[m_start + i];

    m_start += count;
    result = (m_offerIds.length - m_start > 0);  // any left?

    return result;
  }


  public void destroy()
  {
      // GB:    ORBLayer.instance().getORB().disconnect(this);
  }
}










