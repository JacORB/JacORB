
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

package org.jacorb.trading.db.pse;

import java.io.*;
import COM.odi.*;
import org.jacorb.trading.db.DatabaseMgr;
import org.jacorb.trading.db.OfferDatabase;
import org.jacorb.trading.db.TypeDatabase;
import org.jacorb.trading.db.pse.offers.OfferDatabaseImpl;
import org.jacorb.trading.db.pse.types.TypeDatabaseImpl;
import org.jacorb.trading.db.pse.util.TransactionMgr;


public class PSEDatabaseMgr implements DatabaseMgr
{
  private TransactionMgr m_txnMgr;
  private OfferDatabaseImpl m_offerDb;
  private TypeDatabaseImpl m_typeDb;


  protected PSEDatabaseMgr()
  {
  }


  public PSEDatabaseMgr(String dirPath)
  {
    COM.odi.ObjectStore.initialize(null, null);

    String dbPath = dirPath + File.separator + "jtrader.odb";
    Database db = null;

    try {
      db = Database.open(dbPath, ObjectStore.OPEN_UPDATE);
    }
    catch (DatabaseNotFoundException e) {
      db = Database.create(dbPath,
        ObjectStore.ALL_READ | ObjectStore.ALL_WRITE);
    }

    if (db == null) {
      System.out.println("Unable to create database: " + dbPath);
      System.exit(1);
    }

    m_txnMgr = new TransactionMgr();
    m_offerDb = new OfferDatabaseImpl(db, m_txnMgr);
    m_typeDb = new TypeDatabaseImpl(db, m_txnMgr);
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
    COM.odi.ObjectStore.shutdown(true);
  }
}




