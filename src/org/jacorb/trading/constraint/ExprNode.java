
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


/** Base class for nodes */
public abstract class ExprNode
{
  private ValueType m_type;


  public ValueType getType()
  {
    return m_type;
  }


  public abstract void print(PrintStream ostr);


  public abstract Value evaluate(PropertySource source)
    throws MissingPropertyException;


  protected void setType(ValueType type)
  {
    m_type = type;
  }
}










