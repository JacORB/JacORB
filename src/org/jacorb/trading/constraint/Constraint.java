
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


public class Constraint
{
  private PropertySchema m_schema;
  private Expression m_expr;
  private String m_constraint;


  private Constraint()
  {
  }


  public Constraint(PropertySchema schema)
  {
    m_schema = schema;
  }


  public void parse(String constraint)
    throws ParseException
  {
      if( constraint.length() == 0 )
	  constraint = "TRUE";

    StringReader reader = new StringReader(constraint);
    Lex lex = new Lex(reader);

    m_constraint = constraint;

    m_expr = new Expression(m_schema);

    ValueType type = m_expr.parse(lex);

      // make sure that the type of the expression is boolean, and is not
      // a sequence
    if (! ValueType.isCompatible(type.getId(), ValueType.BOOLEAN) ||
        type.isSequence())
      throw new ParseException("constraint expression must be boolean");
  }


  public String getConstraint()
  {
    return m_constraint;
  }


  public boolean evaluate(PropertySource source)
  {
    boolean result = false;

    Value nv = m_expr.evaluate(source);
    if (nv != null) {
      Boolean b = (Boolean)nv.getValue();
      result = b.booleanValue();
    }

    return result;
  }


  /************** comment out this line to enable main()

  public static void main(String[] args)
  {
    if (args.length < 1) {
      System.err.println("Usage: Constraint expr");
      System.exit(1);
    }

    Constraint constr = new Constraint(null);
    try {
      constr.parse(args[0]);
      boolean result = constr.evaluate(null);
      System.out.println("result = " + result);
    }
    catch (ParseException e) {
      System.err.println("Parse error: " + e.getMessage());
    }
  }

  /************** comment out this line to enable main() */
}











