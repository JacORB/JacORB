
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


public class BooleanValue implements Value
{
  private Boolean m_value;


  public BooleanValue()
  {
    this(false);
  }


  public BooleanValue(boolean value)
  {
    m_value = new Boolean(value);
  }


  public BooleanValue(Object value)
  {
    m_value = (Boolean)value;
  }


  public void setValue(Object value)
  {
    m_value = (Boolean)value;
  }


  public int getTypeId()
  {
    return ValueType.BOOLEAN;
  }


  public Object getValue()
  {
    return m_value;
  }


  public boolean equals(Value nv)
  {
    boolean result = false;

    if (nv.getTypeId() == ValueType.BOOLEAN)
      result = m_value.equals(nv.getValue());
    else
      throw new IllegalArgumentException();

    return result;
  }


  public boolean lessThan(Value nv)
  {
    boolean result = false;

    if (nv.getTypeId() == ValueType.BOOLEAN) {
      Boolean b = (Boolean)nv.getValue();
        // FALSE is considered less than TRUE
      result = (m_value.booleanValue() == false && b.booleanValue() == true);
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

    if (typeId == ValueType.BOOLEAN)
      result = new BooleanValue(m_value);
    else
      throw new IllegalArgumentException();

    return result;
  }


  public String toString()
  {
    return m_value.toString();
  }
}











