
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


/** Represents substring matching */
public class SubstrNode extends ExprNode
{
  private ExprNode m_left;
  private ExprNode m_right;


  private SubstrNode()
  {
  }


  public SubstrNode(ExprNode left, ExprNode right)
  {
    m_left = left;
    m_right = right;

    setType(new ValueType(ValueType.BOOLEAN));
  }


  public void print(PrintStream ps)
  {
    ps.println("SubstrNode: type = " + getType());
    ps.println("Left node:");
    m_left.print(ps);
    ps.println("Right node:");
    m_right.print(ps);
  }


  public Value evaluate(PropertySource source)
    throws MissingPropertyException
  {
    Value result = null;

    int id = ValueType.promote(m_left.getType().getId(),
      m_right.getType().getId());

    Value v, left, right;
    v = m_left.evaluate(source);
    left = v.convert(id);
    v = m_right.evaluate(source);
    right = v.convert(id);

    String leftStr = (String)left.getValue();
    String rightStr = (String)right.getValue();

    result = ValueFactory.createBoolean(rightStr.indexOf(leftStr) >= 0);

    return result;
  }
}










