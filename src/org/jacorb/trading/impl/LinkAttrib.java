
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

package org.jacorb.trading.impl;

import org.omg.CosTrading.*;

/**
 * Manages the trader's link attributes
 */
public class LinkAttrib
{
  private FollowOption m_maxLinkFollowPolicy;


  public synchronized FollowOption getMaxLinkFollowPolicy()
  {
    return m_maxLinkFollowPolicy;
  }


  public synchronized FollowOption setMaxLinkFollowPolicy(FollowOption value)
  {
    FollowOption result = m_maxLinkFollowPolicy;
    m_maxLinkFollowPolicy = value;
    return result;
  }
}










