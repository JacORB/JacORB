
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

package org.jacorb.trading.db.simple;

import org.jacorb.trading.db.DatabaseMgr;
import org.jacorb.trading.db.OfferDatabase;
import org.jacorb.trading.db.TypeDatabase;
import org.jacorb.trading.db.simple.offers.OfferDatabaseImpl;
import org.jacorb.trading.db.simple.types.TypeDatabaseImpl;


public class SimpleDatabaseMgr implements DatabaseMgr
{
  private OfferDatabaseImpl m_offerDb;
  private TypeDatabaseImpl m_typeDb;


  protected SimpleDatabaseMgr()
  {
  }


  public SimpleDatabaseMgr(String dirPath)
  {
    m_offerDb = new OfferDatabaseImpl(dirPath);
    m_typeDb = new TypeDatabaseImpl(dirPath);
  }


  public OfferDatabase getOfferDatabase()
  {
    return m_offerDb;
  }


  public TypeDatabase getTypeDatabase()
  {
    return m_typeDb;
  }


  public void shutdown()
  {
  }
}










