
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


public class LongValue implements Value
{
  private Integer m_value;


  public LongValue()
  {
    this(0);
  }


  public LongValue(int value)
  {
    m_value = new Integer(value);
  }


  public LongValue(Object value)
  {
    m_value = (Integer)value;
  }


  public void setValue(Object value)
  {
    m_value = (Integer)value;
  }


  public int getTypeId()
  {
    return ValueType.LONG;
  }


  public Object getValue()
  {
    return m_value;
  }


  public boolean equals(Value nv)
  {
    boolean result = false;

    if (nv.getTypeId() == ValueType.LONG)
      result = m_value.equals(nv.getValue());
    else
      throw new IllegalArgumentException();

    return result;
  }


  public boolean lessThan(Value nv)
  {
    boolean result = false;

    if (nv.getTypeId() == ValueType.LONG) {
      Integer i = (Integer)nv.getValue();
      result = (m_value.intValue() < i.intValue());
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
    Value result = null;

    if (nv.getTypeId() == ValueType.LONG) {
      Integer i = (Integer)nv.getValue();
      result = new LongValue(m_value.intValue() + i.intValue());
    }
    else
      throw new IllegalArgumentException();

    return result;
  }


  public Value minus(Value nv)
  {
    Value result = null;

    if (nv.getTypeId() == ValueType.LONG) {
      Integer i = (Integer)nv.getValue();
      result = new LongValue(m_value.intValue() - i.intValue());
    }
    else
      throw new IllegalArgumentException();

    return result;
  }


  public Value multiply(Value nv)
  {
    Value result = null;

    if (nv.getTypeId() == ValueType.LONG) {
      Integer i = (Integer)nv.getValue();
      result = new LongValue(m_value.intValue() * i.intValue());
    }
    else
      throw new IllegalArgumentException();

    return result;
  }


  public Value divide(Value nv)
  {
    Value result = null;

    if (nv.getTypeId() == ValueType.LONG) {
      Integer i = (Integer)nv.getValue();
      result = new LongValue(m_value.intValue() / i.intValue());
    }
    else
      throw new IllegalArgumentException();

    return result;
  }


  public Value negate()
  {
    Value result = null;

    result = new LongValue(-1 * m_value.intValue());

    return result;
  }


  public Value convert(int typeId)
  {
    Value result = null;

    switch (typeId) {
      case ValueType.LONG:
        result = new LongValue(m_value);
        break;

      case ValueType.ULONG:
        result = ValueFactory.createULong(m_value.longValue());
        break;

      case ValueType.FLOAT:
        result = ValueFactory.createFloat(m_value.floatValue());
        break;

      case ValueType.DOUBLE:
        result = ValueFactory.createDouble(m_value.doubleValue());
        break;

      default:
        throw new IllegalArgumentException();
    }

    return result;
  }


  public String toString()
  {
    return m_value.toString();
  }
}










