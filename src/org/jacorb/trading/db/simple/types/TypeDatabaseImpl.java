
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

package org.jacorb.trading.db.simple.types;

import java.io.*;
import java.util.*;
import org.omg.CORBA.*;
import org.omg.CosTradingRepos.ServiceTypeRepositoryPackage.*;
import org.jacorb.trading.db.TypeDatabase;
import org.jacorb.trading.util.RWLock;


public class TypeDatabaseImpl implements TypeDatabase
{
  private Incarnation m_incarnation;
  private Hashtable m_types;
  private RWLock m_lock;
  private File m_dbFile;
  private boolean m_dirty = false;

  private static final String DB_FILE = "types.dat";


  private TypeDatabaseImpl()
  {
  }


  public TypeDatabaseImpl(String dirName)
  {
    m_dbFile = new File(dirName, DB_FILE);

    if (m_dbFile.exists())
      readObjects();
    else {
      m_incarnation = new Incarnation();
      m_types = new Hashtable();
      writeObjects();
    }

    m_lock = new RWLock();
  }


  public void begin(int mode)
  {
    if (mode == TypeDatabase.READ)
      m_lock.acquireRead();
    else if (mode == TypeDatabase.WRITE)
      m_lock.acquireWrite();
    else
      throw new RuntimeException("Invalid lock mode");
  }


  public void end()
  {
      // flush all of our dirty objects
    if (m_dirty) {
      writeObjects();
      m_dirty = false;
    }

    m_lock.release();
  }


  public TypeStruct describeType(String name)
  {
    TypeStruct result = null;

    Type impl = (Type)m_types.get(name);
    if (impl != null)
      result = impl.describe();

    return result;
  }


  public boolean maskType(String name)
  {
    boolean result = false;

    Type impl = (Type)m_types.get(name);
    if (impl != null) {
      impl.mask();
      m_dirty = true;
      result = true;
    }

    return result;
  }


  public boolean unmaskType(String name)
  {
    boolean result = false;

    Type impl = (Type)m_types.get(name);
    if (impl != null) {
      impl.unmask();
      m_dirty = true;
      result = true;
    }

    return result;
  }


  public String[] getTypes()
  {
    String[] result = new String[m_types.size()];

    Enumeration e = m_types.keys();
    int count = 0;
    while (e.hasMoreElements())
      result[count++] = (String)e.nextElement();

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
    Type type = new Type(name, interfaceName, props, superTypes, result);
    m_types.put(name, type);
    m_incarnation.increment();
    m_dirty = true;

    return result;
  }


  public boolean removeType(String name)
  {
    boolean result = false;

    if (m_types.containsKey(name)) {
      m_types.remove(name);
      m_dirty = true;
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


  protected void readObjects()
  {
    try {
      FileInputStream fileIn = new FileInputStream(m_dbFile);
      ObjectInputStream objIn = new ObjectInputStream(fileIn);
      m_incarnation = (Incarnation)objIn.readObject();
      m_types = (Hashtable)objIn.readObject();
      fileIn.close();
    }
    catch (IOException e) {
      throw new RuntimeException(e.getMessage());
    }
    catch (ClassNotFoundException e) {
      throw new RuntimeException(e.getMessage());
    }
  }


  protected void writeObjects()
  {
    try {
      FileOutputStream fileOut = new FileOutputStream(m_dbFile);
      ObjectOutputStream objOut = new ObjectOutputStream(fileOut);
      objOut.writeObject(m_incarnation);
      objOut.writeObject(m_types);
      fileOut.close();
    }
    catch (IOException e) {
      throw new RuntimeException(e.getMessage());
    }
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










