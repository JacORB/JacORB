
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


/** Represents arithmetic unary-minus */
public class NegNode extends ExprNode
{
  private ExprNode m_child;


  private NegNode()
  {
  }


  public NegNode(ExprNode child)
  {
    m_child = child;

    setType(child.getType());
  }


  public void print(PrintStream ps)
  {
    ps.println("NegNode: type = " + getType());
    ps.println("Child node:");
    m_child.print(ps);
  }


  public Value evaluate(PropertySource source)
    throws MissingPropertyException
  {
    Value result = null;

    Value v;
    v = m_child.evaluate(source);
    result = v.negate();

    return result;
  }
}










