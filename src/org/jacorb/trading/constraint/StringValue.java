
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


public class StringValue implements Value
{
  private String m_value;


  public StringValue()
  {
    this(null);
  }


  public StringValue(String value)
  {
    m_value = value;
  }


  public StringValue(Object value)
  {
    m_value = (String)value;
  }


  public void setValue(Object value)
  {
    m_value = (String)value;
  }


  public int getTypeId()
  {
    return ValueType.STRING;
  }


  public Object getValue()
  {
    return m_value;
  }


  public boolean equals(Value nv)
  {
    boolean result = false;

    if (nv.getTypeId() == ValueType.STRING)
      result = m_value.equals(nv.getValue());
    else
      throw new IllegalArgumentException();

    return result;
  }


  public boolean lessThan(Value nv)
  {
    boolean result = false;

    if (nv.getTypeId() == ValueType.STRING) {
      String s = (String)nv.getValue();
      result = (m_value.compareTo(s) < 0);
    }
    else
      throw new IllegalArgumentException();

    return result;
  }


  public boolean lessThanEqual(Value nv)
  {
    return (lessThan(nv) || equals(nv));
  }


  public boolean greaterThan(Value nv)
  {
    return (! lessThan(nv) && ! equals(nv));
  }


  public boolean greaterThanEqual(Value nv)
  {
    return (! lessThan(nv));
  }


  public Value plus(Value nv)
  {
    throw new ArithmeticException();
  }


  public Value minus(Value nv)
  {
    throw new ArithmeticException();
  }


  public Value multiply(Value nv)
  {
    throw new ArithmeticException();
  }


  public Value divide(Value nv)
  {
    throw new ArithmeticException();
  }


  public Value negate()
  {
    throw new ArithmeticException();
  }


  public Value convert(int typeId)
  {
    Value result = null;

    if (typeId == ValueType.STRING)
      result = new StringValue(m_value);
    else
      throw new IllegalArgumentException();

    return result;
  }


  public String toString()
  {
    return m_value;
  }
}




