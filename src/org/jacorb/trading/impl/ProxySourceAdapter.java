
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

import org.omg.CORBA.*;
import org.omg.CosTrading.*;
import org.omg.CosTrading.ProxyPackage.*;


/**
 * Represents a proxy offer
 */
public class ProxySourceAdapter extends SourceAdapter
{
  private ProxyInfo m_info;


  private ProxySourceAdapter()
  {
  }


  public ProxySourceAdapter(ProxyInfo info)
  {
    super(null, info.properties);
    m_info = info;
  }


  public ProxyInfo getInfo()
  {
    return m_info;
  }
}




