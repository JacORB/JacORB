
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


/** Represents logical "NOT" */
public class NotNode extends ExprNode
{
  private ExprNode m_child;


  private NotNode()
  {
  }


  public NotNode(ExprNode child)
  {
    m_child = child;

    setType(new ValueType(ValueType.BOOLEAN));
  }


  public void print(PrintStream ps)
  {
    ps.println("NotNode: type = " + getType());
    ps.println("Child node:");
    m_child.print(ps);
  }


  public Value evaluate(PropertySource source)
    throws MissingPropertyException
  {
    Value result = null;

    Value v = m_child.evaluate(source);

    Boolean b = (Boolean)v.getValue();

    result = ValueFactory.createBoolean(! b.booleanValue());

    return result;
  }
}










