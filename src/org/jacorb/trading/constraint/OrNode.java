
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

package org.jacorb.trading.constraint;

import java.io.*;


/** Represents logical "OR" */
public class OrNode extends ExprNode
{
  private ExprNode m_left;
  private ExprNode m_right;


  private OrNode()
  {
  }


  public OrNode(ExprNode left, ExprNode right)
  {
    m_left = left;
    m_right = right;

    setType(new ValueType(ValueType.BOOLEAN));
  }


  public void print(PrintStream ps)
  {
    ps.println("OrNode: type = " + getType());
    ps.println("Left node:");
    m_left.print(ps);
    ps.println("Right node:");
    m_right.print(ps);
  }


  public Value evaluate(PropertySource source)
    throws MissingPropertyException
  {
    Value result = null;

    int id = getType().getId();

    Value left, right;

    left = m_left.evaluate(source);
    Boolean l = (Boolean)left.getValue();

      // only evaluate the right side if the answer isn't already
      // known after evaluating the left side
    if (! l.booleanValue()) {
      right = m_right.evaluate(source);
      Boolean r = (Boolean)right.getValue();
      result = ValueFactory.createBoolean(r.booleanValue());
    }
    else
      result = ValueFactory.createBoolean(true);

    return result;
  }
}










