
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


/** Determines if a property exists */
public class ExistNode extends ExprNode
{
  private ExprNode m_child;


  private ExistNode()
  {
  }


  public ExistNode(ExprNode child)
  {
    m_child = child;

    setType(new ValueType(ValueType.BOOLEAN));
  }


  public void print(PrintStream ps)
  {
    ps.println("ExistNode: type = " + getType());
    ps.println("Child node:");
    m_child.print(ps);
  }


  public Value evaluate(PropertySource source)
    throws MissingPropertyException
  {
    Value result = null;

    PropertyNode node = (PropertyNode)m_child;
    result = ValueFactory.createBoolean(node.exists(source));

    return result;
  }
}










