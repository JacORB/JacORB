
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

package org.jacorb.trading.db.pse.types;

import java.io.*;
import java.util.*;
import COM.odi.*;
import COM.odi.util.*;
import org.omg.CORBA.*;
import org.omg.CosTradingRepos.ServiceTypeRepositoryPackage.*;
import org.jacorb.trading.db.TypeDatabase;
import org.jacorb.trading.db.pse.util.TransactionMgr;


public class TypeDatabaseImpl implements TypeDatabase
{
  private Database m_database;
  private OSHashtable m_types;
  private Incarnation m_incarnation;
  private TransactionMgr m_txnMgr;

  private static final String TYPES_ROOT = "types_root";
  private static final String TYPESINC_ROOT = "typesinc_root";


  private TypeDatabaseImpl()
  {
  }


  public TypeDatabaseImpl(COM.odi.Database database, TransactionMgr txnMgr)
  {
    m_database = database;
    m_txnMgr = txnMgr;

    boolean foundRoots = false;
    Transaction tr = null;

    try {
      tr = Transaction.begin(ObjectStore.READONLY);
      m_types = (OSHashtable)m_database.getRoot(TYPES_ROOT);
      m_incarnation = (Incarnation)m_database.getRoot(TYPESINC_ROOT);
      tr.commit(ObjectStore.RETAIN_HOLLOW);
      foundRoots = true;
    }
    catch (DatabaseRootNotFoundException e) {
      tr.abort(ObjectStore.RETAIN_HOLLOW);
    }

    if (! foundRoots) {
      tr = Transaction.begin(ObjectStore.UPDATE);
      m_types = new OSHashtable();
      m_incarnation = new Incarnation();
      m_database.createRoot(TYPES_ROOT, m_types);
      m_database.createRoot(TYPESINC_ROOT, m_incarnation);
      tr.commit(ObjectStore.RETAIN_HOLLOW);
    }
  }


  public void begin(int mode)
  {
    m_txnMgr.begin();
  }


  public void end()
  {
    m_txnMgr.commit(ObjectStore.RETAIN_HOLLOW);
  }


  public TypeStruct describeType(String name)
  {
    TypeStruct result = null;

    Type t = (Type)m_types.get(name);
    if (t != null)
      result = t.describe();

    return result;
  }


  public boolean maskType(String name)
  {
    boolean result = false;

    Type t = (Type)m_types.get(name);
    if (t != null) {
      t.mask();
      result = true;
    }

    return result;
  }


  public boolean unmaskType(String name)
  {
    boolean result = false;

    Type t = (Type)m_types.get(name);
    if (t != null) {
      t.unmask();
      result = true;
    }

    return result;
  }


  public String[] getTypes()
  {
    String[] result = new String[m_types.size()];

    int count = 0;
    Enumeration e = m_types.keys();
    while (e.hasMoreElements()) {
      String name = (String)e.nextElement();
      result[count] = name;
      count++;
    }

    return result;
  }


  public String[] getTypesSince(IncarnationNumber inc)
  {
    String[] result;

    Vector types = new Vector();
    Incarnation i = new Incarnation(inc);

    Enumeration e = m_types.elements();
    while (e.hasMoreElements()) {
      Type type = (Type)e.nextElement();
      if (type.getIncarnation().compareTo(i) >= 0)
        types.addElement(type.getName());
    }

    result = new String[types.size()];
    types.copyInto((java.lang.Object[])result);

    return result;
  }


  public IncarnationNumber getIncarnation()
  {
    return m_incarnation.getIncarnationNumber();
  }


  public IncarnationNumber createType(
    String name,
    String interfaceName,
    PropStruct[] props,
    String[] superTypes)
  {
    IncarnationNumber result = null;

      // assign a new incarnation number and add the type to our list
    result = m_incarnation.getIncarnationNumber();
    m_incarnation.increment();
    Type type =
      new Type(name, interfaceName, props, superTypes, result);
    m_types.put(name, type);

    return result;
  }


  public boolean removeType(String name)
  {
    boolean result = false;

    if (m_types.containsKey(name)) {
      m_types.remove(name);
      result = true;
    }

    return result;
  }


  public String findSubType(String name)
  {
    String result = null;

    Enumeration e = m_types.elements();
    while (e.hasMoreElements() && result == null) {
      Type type = (Type)e.nextElement();
      String[] superTypes = type.getSuperTypes();
      for (int t = 0; t < superTypes.length; t++) {
        if (name.equals(superTypes[t])) {
          result = type.getName();
          break;
        }
      }
    }

    return result;
  }


  public String[] getAllSuperTypes(String name)
  {
    String[] result = null;

    Type type = (Type)m_types.get(name);
    if (type != null) {
      Vector vec = findAllSuperTypes(type);

      result = new String[vec.size()];
      vec.copyInto((java.lang.Object[])result);
    }

    return result;
  }


  /**
   * Returns a vector of strings representing all the super types of
   * this type; this method is called recursively
   */
  protected Vector findAllSuperTypes(Type type)
  {
    Vector result = new Vector();

      // start with our immediate super types
    String[] supers = type.getSuperTypes();

      // merge in all of the super types of our super types
    for (int i = 0; i < supers.length; i++) {
      result.addElement(supers[i]);
      Type impl = (Type)m_types.get(supers[i]);
      Vector v = findAllSuperTypes(impl);
      Enumeration e = v.elements();
      while (e.hasMoreElements()) {
        String s = (String)e.nextElement();
        if (! result.contains(s))
          result.addElement(s);
      }
    }

    return result;
  }
}










