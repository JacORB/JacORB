
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

/*
 * $Log$
 * Revision 1.3  2001/03/19 11:10:03  brose
 * *** empty log message ***
 *
 * Revision 1.2  2001/03/17 18:45:24  brose
 * *** empty log message ***
 *
 * Revision 1.4  1999/11/25 16:08:15  brose
 * cosmetics
 *
 * Revision 1.3  1999/11/08 08:11:48  brose
 * *** empty log message ***
 *
 * Revision 1.2  1999/11/03 18:04:02  brose
 * *** empty log message ***
 *
 * Revision 1.1  1999-10-06 12:06:06+02  brose
 * *** empty log message ***
 *
 * Revision 1.2  1999-10-05 16:08:31+02  brose
 * New directory structure for trading service
 *
 * Revision 1.1.1.1  1999-08-05 12:22:54+02  brose
 * First initial preliminary ... attempt
 *
 * Revision 1.2  1999-08-05 11:28:44+02  brose
 * *** empty log message ***
 *
 */

package org.jacorb.trading.impl;

import org.omg.CosTrading.*;

/**
 * Manages the trader's import attributes
 */
public class ImportAttrib
{
  private int m_defSearchCard;
  private int m_maxSearchCard;
  private int m_defMatchCard;
  private int m_maxMatchCard;
  private int m_defReturnCard;
  private int m_maxReturnCard;
  private int m_maxList;
  private int m_defHopCount;
  private int m_maxHopCount;
  private FollowOption m_defFollowPolicy;
  private FollowOption m_maxFollowPolicy;


  public synchronized int getDefSearchCard()
  {
    return m_defSearchCard;
  }


  public synchronized int setDefSearchCard(int value)
  {
    int result = m_defSearchCard;
    m_defSearchCard = value;
    return result;
  }


  public synchronized int getMaxSearchCard()
  {
    return m_maxSearchCard;
  }


  public synchronized int setMaxSearchCard(int value)
  {
    int result = m_maxSearchCard;
    m_maxSearchCard = value;
    return result;
  }


  public synchronized int getDefMatchCard()
  {
    return m_defMatchCard;
  }


  public synchronized int setDefMatchCard(int value)
  {
    int result = m_defMatchCard;
    m_defMatchCard = value;
    return result;
  }


  public synchronized int getMaxMatchCard()
  {
    return m_maxMatchCard;
  }


  public synchronized int setMaxMatchCard(int value)
  {
    int result = m_maxMatchCard;
    m_maxMatchCard = value;
    return result;
  }


  public synchronized int getDefReturnCard()
  {
    return m_defReturnCard;
  }


  public synchronized int setDefReturnCard(int value)
  {
    int result = m_defReturnCard;
    m_defReturnCard = value;
    return result;
  }


  public synchronized int getMaxReturnCard()
  {
    return m_maxReturnCard;
  }


  public synchronized int setMaxReturnCard(int value)
  {
    int result = m_maxReturnCard;
    m_maxReturnCard = value;
    return result;
  }


  public synchronized int getMaxList()
  {
    return m_maxList;
  }


  public synchronized int setMaxList(int value)
  {
    int result = m_maxList;
    m_maxList = value;
    return result;
  }


  public synchronized int getDefHopCount()
  {
    return m_defHopCount;
  }


  public synchronized int setDefHopCount(int value)
  {
    int result = m_defHopCount;
    m_defHopCount = value;
    return result;
  }


  public synchronized int getMaxHopCount()
  {
    return m_maxHopCount;
  }


  public synchronized int setMaxHopCount(int value)
  {
    int result = m_maxHopCount;
    m_maxHopCount = value;
    return result;
  }


  public synchronized FollowOption getDefFollowPolicy()
  {
    return m_defFollowPolicy;
  }


  public synchronized FollowOption setDefFollowPolicy(FollowOption value)
  {
    FollowOption result = m_defFollowPolicy;
    m_defFollowPolicy = value;
    return result;
  }


  public synchronized FollowOption getMaxFollowPolicy()
  {
    return m_maxFollowPolicy;
  }


  public synchronized FollowOption setMaxFollowPolicy(FollowOption value)
  {
    FollowOption result = m_maxFollowPolicy;
    m_maxFollowPolicy = value;
    return result;
  }
}










