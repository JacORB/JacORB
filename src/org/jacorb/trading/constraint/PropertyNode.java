
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

package org.jacorb.trading.constraint;

import java.io.*;


/** Represents a property */
public class PropertyNode extends ExprNode
{
  private String m_name;


  private PropertyNode()
  {
  }


  public PropertyNode(String name, ValueType type)
  {
    m_name = name;
    setType(type);
  }


  public void print(PrintStream ps)
  {
    ps.println("PropertyNode: name = " + m_name + " type = " + getType());
  }


  public Value evaluate(PropertySource source)
    throws MissingPropertyException
  {
    Value result;

    result = source.getValue(m_name);
    if (result == null)
      throw new MissingPropertyException(m_name);

    return result;
  }


  public boolean exists(PropertySource source)
  {
      // the mere presence of the property is not enough; we also need
      // to make sure the property's value is obtainable;  this may force
      // the evaluation of a dynamic property
    boolean result = (source.exists(m_name) && source.getValue(m_name) != null);
    return result;
  }


  public boolean inSequence(Value value, PropertySource source)
  {
    boolean result = false;

    int id = ValueType.promote(getType().getId(), value.getTypeId());

    Value v = value.convert(id);

    Value[] seq = source.getSequenceValues(m_name);
    for (int i = 0; i < seq.length; i++) {
      Value nv = seq[i].convert(id);
      if (v.equals(nv)) {
        result = true;
        break;
      }
    }

    return result;
  }
}










