
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
import java.util.*;


public class Preference
{
  private PropertySchema m_schema;
  private Expression m_expr;
  private ValueType m_exprType;
  private int m_prefType = Lex.PREF_FIRST;


  private Preference()
  {
  }


  public Preference(PropertySchema schema)
  {
    m_schema = schema;
  }


  public void parse(String pref)
    throws ParseException
  {
    StringReader reader = new StringReader(pref);
    Lex lex = new Lex(reader);

    m_prefType = lex.getToken();
    switch (m_prefType) {
      case Lex.PREF_MIN:
      case Lex.PREF_MAX: {
          m_expr = new Expression(m_schema);

            // ask the expression object to parse the input from the lexer;
            // any exceptions from the expression object are propagated up
          lex.nextToken();
          m_exprType = m_expr.parse(lex);

            // make sure that the type of the expression is numeric, and is not
            // a sequence
          if (! m_exprType.isNumber() || m_exprType.isSequence())
            throw new ParseException(
              "min/max preference expression must be numeric");
        }
        break;

      case Lex.PREF_WITH: {
          m_expr = new Expression(m_schema);

            // ask the expression object to parse the input from the lexer;
            // any exceptions from the expression object are propagated up
          lex.nextToken();
          m_exprType = m_expr.parse(lex);

            // make sure that the type of the expression is boolean, and is not
            // a sequence
          if (! ValueType.isCompatible(m_exprType.getId(), ValueType.BOOLEAN) ||
              m_exprType.isSequence())
            throw new ParseException(
              "with preference expression must be boolean");
        }
        break;

      case Lex.PREF_RANDOM:
      case Lex.PREF_FIRST:
          // nothing to do - no expression necessary
        break;

      case Lex.END:
          // if preference is empty, 'first' is default
        m_prefType = Lex.PREF_FIRST;
        break;

      default:
        throw new ParseException("invalid preference expression");
    }
  }


  public Vector order(Vector sources)
  {
    Vector result = null;

    switch (m_prefType) {
      case Lex.PREF_MIN:
        result = orderMin(sources);
        break;

      case Lex.PREF_MAX:
        result = orderMax(sources);
        break;

      case Lex.PREF_WITH:
        result = orderWith(sources);
        break;

      case Lex.PREF_RANDOM:
        result = orderRandom(sources);
        break;

      case Lex.PREF_FIRST:
        result = orderFirst(sources);
        break;
    }

    return result;
  }


  protected class SourceValue
  {
    public PropertySource source;
    public Value value;

    public SourceValue(PropertySource src, Value nv)
    {
      source = src;
      value = nv;
    }
  }


  protected Vector orderMin(Vector sources)
  {
    Vector result = new Vector();

      // make a copy of the sources list; when we're done, temp will
      // hold the source objects for which evaluation failed
    Vector temp = (Vector)sources.clone();

      // so that we don't keep re-evaluating sources, we keep a list
      // of source-value mappings
    Vector values = new Vector();

    Enumeration e = sources.elements();
    while (e.hasMoreElements()) {
      PropertySource src = (PropertySource)e.nextElement();
      Value v = m_expr.evaluate(src);

      if (v != null) {
          // evaluation was successful, so find the proper
          // insertion point in values list
        Enumeration n = values.elements();
        int pos = 0;
        while (n.hasMoreElements()) {
          SourceValue sv = (SourceValue)n.nextElement();
          if (v.lessThan(sv.value))
            break;
          pos++;
        }

          // insert new SourceValue struct into values list
        values.insertElementAt(new SourceValue(src, v), pos);

          // remove src from temp list so that we know we've
          // dealt with it
        temp.removeElement(src);
      }
    }

      // iterate through values list, adding each source to result
    e = values.elements();
    while (e.hasMoreElements()) {
      SourceValue sv = (SourceValue)e.nextElement();
      result.addElement(sv.source);
    }

      // now append all remaining sources in temp to result
    e = temp.elements();
    while (e.hasMoreElements())
      result.addElement(e.nextElement());

    return result;
  }


  protected Vector orderMax(Vector sources)
  {
    Vector result = new Vector();

      // make a copy of the sources list; when we're done, temp will
      // hold the source objects for which evaluation failed
    Vector temp = (Vector)sources.clone();

      // so that we don't keep re-evaluating sources, we keep a list
      // of source-value mappings
    Vector values = new Vector();

    Enumeration e = sources.elements();
    while (e.hasMoreElements()) {
      PropertySource src = (PropertySource)e.nextElement();
      Value v = m_expr.evaluate(src);

      if (v != null) {
          // evaluation was successful, so find the proper
          // insertion point in values list
        Enumeration n = values.elements();
        int pos = 0;
        while (n.hasMoreElements()) {
          SourceValue sv = (SourceValue)n.nextElement();
          if (v.greaterThan(sv.value))
            break;
          pos++;
        }

          // insert new SourceValue struct into values list
        values.insertElementAt(new SourceValue(src, v), pos);

          // remove src from temp list so that we know we've
          // dealt with it
        temp.removeElement(src);
      }
    }

      // iterate through values list, adding each source to result
    e = values.elements();
    while (e.hasMoreElements()) {
      SourceValue sv = (SourceValue)e.nextElement();
      result.addElement(sv.source);
    }

      // now append all remaining sources in temp to result
    e = temp.elements();
    while (e.hasMoreElements())
      result.addElement(e.nextElement());

    return result;
  }


  protected Vector orderWith(Vector sources)
  {
    Vector result = new Vector();

      // make a copy of the sources list; when we're done, temp will
      // hold the source objects for which evaluation failed
    Vector temp = (Vector)sources.clone();

    Enumeration e = sources.elements();
    while (e.hasMoreElements()) {
      PropertySource src = (PropertySource)e.nextElement();
      Value v = m_expr.evaluate(src);

      if (v != null) {
        Boolean b = (Boolean)v.getValue();
        if (b.booleanValue()) {
            // constraint evaluated to true, so add source to result
          result.addElement(src);

            // remove src from temp list so that we know we've
            // dealt with it
          temp.removeElement(src);
        }
      }
    }

      // now append all remaining sources in temp to result
    e = temp.elements();
    while (e.hasMoreElements())
      result.addElement(e.nextElement());

    return result;
  }


  protected Vector orderRandom(Vector sources)
  {
    Vector result = new Vector();

      // create an array to hold each source in the list
    int entries = sources.size();
    PropertySource[] arr = new PropertySource[entries];
    sources.copyInto((java.lang.Object[])arr);

    Random rand = new Random();

      // keep looping until we've processed all sources
    int count = 0;
    while (count < entries) {
      int idx = Math.abs(rand.nextInt()) % entries;
        // if we haven't seen this index before
      if (arr[idx] != null) {
        result.addElement(arr[idx]);
        arr[idx] = null;  // clear this entry
        count++;
      }
    }

    return result;
  }


  protected Vector orderFirst(Vector sources)
  {
    return sources;
  }


  /******************** comment out this line to enable main()

  public static void main(String[] args)
  {
    if (args.length < 1) {
      System.err.println("Usage: Preference expr");
      System.exit(1);
    }

    Preference pref = new Preference(null);
    try {
      pref.parse(args[0]);
      //boolean result = constr.evaluate(null);
      //System.out.println("result = " + result);
    }
    catch (ParseException e) {
      System.err.println("Parse error: " + e.getMessage());
    }
  }

  /******************** comment out this line to enable main() */
}




