
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

import java.io.*;
import java.util.*;
import org.omg.CORBA.*;
import org.omg.CosTrading.*;
import org.omg.CosTradingRepos.ServiceTypeRepositoryPackage.*;
import org.jacorb.trading.db.TypeDatabase;
import org.jacorb.trading.util.*;


/**
 * Implementation of CosTradingRepos::ServiceTypeRepository
 */
public class RepositoryImpl
  extends org.omg.CosTradingRepos.ServiceTypeRepositoryPOA
{
  private TypeDatabase m_database;
  private org.omg.CORBA.Repository m_interfaceRepos;


  public RepositoryImpl(
    TypeDatabase db,
    org.omg.CORBA.Repository interfaceRepos)
  {
    m_database = db;
    m_interfaceRepos = interfaceRepos;
  }


  public IncarnationNumber incarnation()
  {
    IncarnationNumber result;

    m_database.begin(TypeDatabase.READ);
    result = m_database.getIncarnation();
    m_database.end();

    return result;
  }


  public IncarnationNumber add_type(
    String name,
    String if_name,
    PropStruct[] props,
    String[] super_types)
    throws IllegalServiceType,
           ServiceTypeExists,
           InterfaceTypeMismatch,
           IllegalPropertyName,
           DuplicatePropertyName,
           ValueTypeRedefinition,
           UnknownServiceType,
           DuplicateServiceTypeName
  {
    IncarnationNumber result = null;

    try {
      m_database.begin(TypeDatabase.WRITE);

      checkTypeName(name);

        // make sure another type doesn't already exist with the same name
      if (m_database.describeType(name) != null)
        throw new ServiceTypeExists(name);

        // we'll build up a hashtable of type descriptions for later use
      Hashtable typeInfo = new Hashtable();

        // check for duplicate super types
      for (int i = 0; i < super_types.length; i++) {
        TypeStruct ts = findType(super_types[i]);
        if (typeInfo.containsKey(super_types[i]))
          throw new DuplicateServiceTypeName(super_types[i]);
        typeInfo.put(super_types[i], ts);
      }

        // collect descriptions of all super types

      for (int i = 0; i < super_types.length; i++) {
        String[] names = m_database.getAllSuperTypes(super_types[i]);
        for (int n = 0; n < names.length; n++) {
          if (! typeInfo.containsKey(names[n])) {
            TypeStruct ts = findType(names[n]);
            if (ts == null)
              throw new UnknownServiceType(names[n]);
            typeInfo.put(names[n], ts);
          }
        }
      }

        // check for interface compatibility
      validateInterface(name, if_name, super_types, typeInfo);

        // check for duplicate properties and for value type redefinition
      validateProperties(name, props, super_types, typeInfo);

      result = m_database.createType(name, if_name, props, super_types);
    }
    finally {
      m_database.end();
    }

    return result;
  }


  public void remove_type(String name)
    throws IllegalServiceType,
           UnknownServiceType,
           HasSubTypes
  {
    try {
      m_database.begin(TypeDatabase.WRITE);

      checkTypeName(name);

      String subTypeName = m_database.findSubType(name);

      if (subTypeName != null)
        throw new HasSubTypes(name, subTypeName);

      if (! m_database.removeType(name))
        throw new UnknownServiceType(name);
    }
    finally {
      m_database.end();
    }
  }


  public String[] list_types(
    SpecifiedServiceTypes which_types)
  {
    String[] result = null;

    try {
      m_database.begin(TypeDatabase.READ);

      if (which_types.discriminator() == ListOption.all)
        result = m_database.getTypes();
      else {
        IncarnationNumber inc = which_types.incarnation();
        result = m_database.getTypesSince(inc);
      }
    }
    finally {
      m_database.end();
    }

    return result;
  }


  public TypeStruct describe_type(String name)
    throws IllegalServiceType,
           UnknownServiceType
  {
    TypeStruct result;

    try {
      m_database.begin(TypeDatabase.READ);

      checkTypeName(name);

      result = findType(name);
    }
    finally {
      m_database.end();
    }

    return result;
  }


  public TypeStruct fully_describe_type(String name)
    throws IllegalServiceType,
           UnknownServiceType
  {
    TypeStruct result = null;
    Enumeration e;

    try {
      m_database.begin(TypeDatabase.READ);

      checkTypeName(name);

      result = findType(name);

      String[] superTypeNames = m_database.getAllSuperTypes(name);

        // replace the super_types member
      result.super_types = superTypeNames;

        // get descriptions of all the supertypes
      Vector desc = new Vector();
      for (int i = 0; i < superTypeNames.length; i++) {
        TypeStruct ts = findType(superTypeNames[i]);
        desc.addElement(ts);
      }

        // build up the complete list of properties, starting with the
        // properties in the requested type
      Vector props = new Vector();
      for (int i = 0; i < result.props.length; i++)
        props.addElement(result.props[i]);

      e = desc.elements();
      while (e.hasMoreElements()) {
        TypeStruct ts = (TypeStruct)e.nextElement();
        for (int i = 0; i < ts.props.length; i++) {
            // iterate through the current list, making sure we don't
            // add a property that already exists
          boolean found = false;
          Enumeration p = props.elements();
          while (p.hasMoreElements() && ! found) {
            PropStruct ps = (PropStruct)p.nextElement();
            if (ts.props[i].name.equals(ps.name))
              found = true;
          }

          if (! found)
            props.addElement(ts.props[i]);
        }
      }

      result.props = new PropStruct[props.size()];
      props.copyInto((java.lang.Object[])result.props);
    }
    finally {
      m_database.end();
    }

    return result;
  }


  public void mask_type(String name)
    throws IllegalServiceType,
           UnknownServiceType,
           AlreadyMasked
  {
    try {
      m_database.begin(TypeDatabase.WRITE);

      checkTypeName(name);

      TypeStruct ts = findType(name);

      if (ts.masked)
        throw new AlreadyMasked(name);
      else if (! m_database.maskType(name))
        throw new UnknownServiceType(name);
    }
    finally {
      m_database.end();
    }
  }


  public void unmask_type(String name)
    throws IllegalServiceType,
           UnknownServiceType,
           NotMasked
  {
    try {
      m_database.begin(TypeDatabase.WRITE);

      checkTypeName(name);

      TypeStruct ts = findType(name);

      if (! ts.masked)
        throw new NotMasked(name);
      else if (! m_database.unmaskType(name))
        throw new UnknownServiceType(name);
    }
    finally {
      m_database.end();
    }
  }


  protected void checkTypeName(String name)
    throws IllegalServiceType
  {
    if (name == null || name.trim().length() == 0)
      throw new IllegalServiceType("");

    StringTokenizer tokenizer = new StringTokenizer(name, ":", true);
    boolean seenIdent = false;
    String lastToken = null;
    int colonCount = 0;

    while (tokenizer.hasMoreTokens()) {
      String tok = tokenizer.nextToken();
      if (tok.equals(":")) {
        colonCount++;
        if (colonCount > 2)
          throw new IllegalServiceType(name);
      }
      else {
        colonCount = 0;
        seenIdent = true;

          // must start with letter
        if (! Character.isLetter(tok.charAt(0)))
          throw new IllegalServiceType(name);

        for (int i = 1; i < tok.length(); i++) {
          char ch = tok.charAt(i);
          if (! Character.isLetterOrDigit(ch) && ch != '_')
            throw new IllegalServiceType(name);
        }
      }

      lastToken = tok;
    }

    if (! seenIdent)
      throw new IllegalServiceType(name);

    if (lastToken.equals(":"))
      throw new IllegalServiceType(name);
  }


  protected TypeStruct findType(String name)
    throws UnknownServiceType
  {
    TypeStruct result = m_database.describeType(name);

    if (result == null)
      throw new UnknownServiceType(name);

    return result;
  }


  protected void validateInterface(
    String name,
    String interfaceName,
    String[] superTypes,
    Hashtable typeInfo)
    throws InterfaceTypeMismatch
  {
      // if we have an interface repository, then validate the type's
      // interface against the interface of each of the supertypes
    if (m_interfaceRepos != null) {
      org.omg.CORBA.InterfaceDef def = null;

        // retrieve the InterfaceDef object for the interface
      try {
        org.omg.CORBA.Contained c = m_interfaceRepos.lookup(interfaceName);
        if (c != null)
          def = org.omg.CORBA.InterfaceDefHelper.narrow(c);
      }
      catch (org.omg.CORBA.SystemException e) {
        // ignore
      }

      if (def != null) {
        for (int i = 0; i < superTypes.length; i++) {
            // retrieve the InterfaceDef object for the supertype
          try {
            TypeStruct ts = (TypeStruct)typeInfo.get(superTypes[i]);
            org.omg.CORBA.Contained c = m_interfaceRepos.lookup(ts.if_name);
            if (c != null) {
              String id = c.id();
              if (! def.is_a(id))
                throw new InterfaceTypeMismatch(superTypes[i], ts.if_name,
                  name, interfaceName);
            }
          }
          catch (org.omg.CORBA.SystemException e) {
            // ignore
          }
        }
      }
    }
  }


  protected void validateProperties(
    String name,
    PropStruct[] props,
    String[] superTypes,
    Hashtable typeInfo)
    throws IllegalPropertyName,
           DuplicatePropertyName,
           ValueTypeRedefinition
  {
    Vector goodProps = new Vector();

      // for each property, we need to validate it against the
      // property with the same name in all supertypes
    for (int i = 0; i < props.length; i++) {
        // first make sure we haven't seen this property already
      if (goodProps.contains(props[i].name))
        throw new DuplicatePropertyName(props[i].name);

        // now ask each supertype if they have a property of the same name;
        // we don't stop after the first match - we have to ask ALL of the
        // super types
      for (int s = 0; s < superTypes.length; s++) {
        TypeStruct ts = (TypeStruct)typeInfo.get(superTypes[s]);

        Vector superProps = new Vector();
        findProperties(ts, props[i].name, superProps, typeInfo);

        Enumeration e = superProps.elements();
        while (e.hasMoreElements()) {
          PropStruct ps = (PropStruct)e.nextElement();

            // make sure the type codes are equivalent and the modes
            // are compatible
          if (! props[i].value_type.equal(ps.value_type) ||
              ! validateMode(props[i].mode, ps.mode))
            throw new ValueTypeRedefinition(name, props[i], superTypes[s], ps);
        }
      }

        // if we've made it here without throwing an exception, add this
        // property to our running list of validated properties
      goodProps.addElement(props[i].name);
    }
  }


  protected boolean validateMode(PropertyMode subMode, PropertyMode superMode)
  {
    boolean result = false;

      // if both are equal, we're done
    if (superMode == subMode)
      result = true;
      // if mode of superMode is normal, subMode mode can be anything
    else if (superMode == PropertyMode.PROP_NORMAL)
      result = true;
    else if (superMode == PropertyMode.PROP_READONLY &&
      (subMode == PropertyMode.PROP_READONLY ||
       subMode == PropertyMode.PROP_MANDATORY_READONLY))
      result = true;
    else if (superMode == PropertyMode.PROP_MANDATORY &&
      (subMode == PropertyMode.PROP_MANDATORY ||
       subMode == PropertyMode.PROP_MANDATORY_READONLY))
      result = true;
    else if ( // redundant, but included for completeness
      superMode == PropertyMode.PROP_MANDATORY_READONLY &&
      subMode == PropertyMode.PROP_MANDATORY_READONLY)
      result = true;

    return result;
  }


  /**
   * This function is called recursively
   */
  protected void findProperties(
    TypeStruct ts,
    String propName,
    Vector v,
    Hashtable typeInfo)
  {
    PropStruct prop = findProperty(ts, propName);
    if (prop != null)
      v.addElement(prop);
    else {
        // ask all of our supertypes to add to the list - typeInfo should
        // contain the descriptions for the complete set of supertypes
      for (int i = 0; i < ts.super_types.length; i++) {
        TypeStruct superTS = (TypeStruct)typeInfo.get(ts.super_types[i]);
        findProperties(superTS, propName, v, typeInfo);
      }
    }
  }


  protected PropStruct findProperty(TypeStruct ts, String name)
  {
    PropStruct result = null;

    for (int i = 0; i < ts.props.length && result == null; i++)
      if (name.equals(ts.props[i].name))
        result = ts.props[i];

    return result;
  }
}




