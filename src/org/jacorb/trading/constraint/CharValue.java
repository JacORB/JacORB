
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


public class CharValue implements Value
{
  private Character m_value;


  public CharValue()
  {
    this((char)0);
  }


  public CharValue(char value)
  {
    m_value = new Character(value);
  }


  public CharValue(Object value)
  {
    m_value = (Character)value;
  }


  public void setValue(Object value)
  {
    m_value = (Character)value;
  }


  public int getTypeId()
  {
    return ValueType.CHAR;
  }


  public Object getValue()
  {
    return m_value;
  }


  public boolean equals(Value nv)
  {
    boolean result = false;

    if (nv.getTypeId() == ValueType.CHAR)
      result = m_value.equals(nv.getValue());
    else
      throw new IllegalArgumentException();

    return result;
  }


  public boolean lessThan(Value nv)
  {
    boolean result = false;

    if (nv.getTypeId() == ValueType.CHAR) {
      Character c = (Character)nv.getValue();
      result = (m_value.charValue() < c.charValue());
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

    if (typeId == ValueType.CHAR)
      result = new CharValue(m_value);
    else if (typeId == ValueType.STRING) {
      char[] arr = new char[1];
      arr[0] = m_value.charValue();
      String s = new String(arr);
      result = ValueFactory.createString(s);
    }
    else
      throw new IllegalArgumentException();

    return result;
  }


  public String toString()
  {
    return m_value.toString();
  }
}









