
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

package org.jacorb.trading.client.proxy;

import org.omg.CosTrading.*;
// GB:import jtport.ORBLayer;


public class OfferIteratorImpl extends OfferIteratorPOA
{
  private Offer[] m_offers;
  private int m_start;


  private OfferIteratorImpl()
  {
  }


  public OfferIteratorImpl(Offer[] offers, int start)
  {
    m_offers = offers;
    m_start = start;
  }


  public int max_left()
    throws UnknownMaxLeft
  {
    return (m_offers.length - m_start);
  }


  public boolean next_n(int n, OfferSeqHolder offers)
  {
    boolean result;

    int count;
    if (n > (m_offers.length - m_start))
      count = m_offers.length - m_start;
    else
      count = n;

    offers.value = new Offer[count];
    for (int i = 0; i < count; i++)
      offers.value[i] = m_offers[m_start + i];

    m_start += count;
    result = (m_offers.length - m_start > 0);  // any left?

    return result;
  }


  public void destroy()
  {
      // GB:    ORBLayer.instance().getORB().disconnect(this);
  }
}




