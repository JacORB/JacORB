
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

package org.jacorb.trading.impl;

import java.util.*;
import org.omg.CORBA.*;
import org.omg.CosTradingRepos.ServiceTypeRepositoryPackage.*;
import org.jacorb.trading.constraint.*;


/**
 * Provides property "schema" information to the expression evaluators
 */
public class SchemaAdapter implements PropertySchema
{
  private Hashtable m_props;
  private TypeStruct m_type;


  private SchemaAdapter()
  {
  }


  public SchemaAdapter(TypeStruct ts)
  {
    m_type = ts;
    m_props = new Hashtable();

      // build a hashtable of property name and ValueType
    try {
      for (int i = 0; i < ts.props.length; i++) {
        PropStruct ps = ts.props[i];
        boolean seq = false;
        int id;

        TCKind kind = ps.value_type.kind();

        if (kind == TCKind.tk_sequence) {
          seq = true;
          TypeCode tc = ps.value_type.content_type();
          kind = tc.kind();
        }

        id = convertKind(kind);
        ValueType type = new ValueType(id, seq);

        m_props.put(ts.props[i].name, type);
      }
    }
    catch (org.omg.CORBA.TypeCodePackage.BadKind e) {
      throw new RuntimeException();
    }
  }


  /** Determines whether the property exists */
  public boolean exists(String property)
  {
    return m_props.containsKey(property);
  }


  /**
   * Returns the type of the property, or null if the property
   * wasn't found
   */
  public ValueType getPropertyType(String property)
  {
    return (ValueType)m_props.get(property);
  }


  /** Translates a TCKind into a ValueType ID */
  protected int convertKind(TCKind kind)
  {
    int result;

    switch (kind.value()) {
      case TCKind._tk_short:
        result = ValueType.SHORT;
        break;

      case TCKind._tk_long:
        result = ValueType.LONG;
        break;

      case TCKind._tk_ushort:
        result = ValueType.USHORT;
        break;

      case TCKind._tk_ulong:
        result = ValueType.ULONG;
        break;

      case TCKind._tk_float:
        result = ValueType.FLOAT;
        break;

      case TCKind._tk_double:
        result = ValueType.DOUBLE;
        break;

      case TCKind._tk_boolean:
        result = ValueType.BOOLEAN;
        break;

      case TCKind._tk_char:
        result = ValueType.CHAR;
        break;

      case TCKind._tk_string:
        result = ValueType.STRING;
        break;

      default:
        result = ValueType.OTHER;
        break;
    }

    return result;
  }
}










