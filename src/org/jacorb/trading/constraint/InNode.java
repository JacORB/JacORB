
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


/** Represents set-membership */
public class InNode extends ExprNode
{
  private ExprNode m_left;
  private ExprNode m_right;


  private InNode()
  {
  }


  public InNode(ExprNode left, ExprNode right)
  {
    m_left = left;
    m_right = right;

    setType(new ValueType(ValueType.BOOLEAN));
  }


  public void print(PrintStream ps)
  {
    ps.println("InNode: type = " + getType());
    ps.println("Left node:");
    m_left.print(ps);
    ps.println("Right node:");
    m_right.print(ps);
  }


  public Value evaluate(PropertySource source)
    throws MissingPropertyException
  {
    Value result = null;

      // the right side must be a sequence property
    PropertyNode prop = (PropertyNode)m_right;

    Value left = m_left.evaluate(source);

      // ask the property if it contains the value
    result = ValueFactory.createBoolean(prop.inSequence(left, source));

    return result;
  }
}




