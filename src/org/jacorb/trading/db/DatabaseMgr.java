
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

package org.jacorb.trading.db;


/**
 * An abstract interface to the trader databases
 */
public interface DatabaseMgr
{
  public OfferDatabase getOfferDatabase();

  public TypeDatabase getTypeDatabase();

  public void shutdown();
}










