
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


/** Represents a literal value */
public class LiteralNode extends ExprNode
{
  private Value m_value;


  public static final int BOOLEAN = 0;
  public static final int NUMBER  = 1;
  public static final int STRING  = 2;


  private LiteralNode()
  {
  }


  public LiteralNode(int literalType, String value)
  {
    determineType(literalType, value);

    switch (getType().getId()) {
      case ValueType.BOOLEAN:
        m_value = ValueFactory.createBoolean(value.equals("TRUE"));
        break;

      case ValueType.DOUBLE: {
          try {
            Double d = Double.valueOf(value);
            m_value = ValueFactory.createDouble(d.doubleValue());
          }
          catch (NumberFormatException e) {
          }
        }
        break;

      case ValueType.LONG: {
          try {
            Integer i = Integer.valueOf(value);
            m_value = ValueFactory.createLong(i.intValue());
          }
          catch (NumberFormatException e) {
          }
        }
        break;

      case ValueType.STRING:
        m_value = ValueFactory.createString(value);
        break;
    }
  }


  public void print(PrintStream ps)
  {
    ps.println("LiteralNode: type = " + getType() + " value = " + m_value);
  }


  public Value evaluate(PropertySource source)
    throws MissingPropertyException
  {
    return m_value;
  }


  protected void determineType(int literalType, String value)
  {
    if (literalType == BOOLEAN)
      setType(new ValueType(ValueType.BOOLEAN));
    else if (literalType == STRING)
      setType(new ValueType(ValueType.STRING));
    else { // NUMBER
      if (value.indexOf('.') >= 0 || value.indexOf('E') >= 0 ||
          value.indexOf('e') >= 0)
        setType(new ValueType(ValueType.DOUBLE));
      else
        setType(new ValueType(ValueType.LONG));
    }
  }
}










