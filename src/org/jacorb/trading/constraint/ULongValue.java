
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


public class ULongValue implements Value
{
  private Long m_value;


  public ULongValue()
  {
    this(0);
  }


  public ULongValue(long value)
  {
    m_value = new Long(value);
  }


  public ULongValue(Object value)
  {
    m_value = (Long)value;
  }


  public void setValue(Object value)
  {
    m_value = (Long)value;
  }


  public int getTypeId()
  {
    return ValueType.ULONG;
  }


  public Object getValue()
  {
    return m_value;
  }


  public boolean equals(Value nv)
  {
    boolean result = false;

    if (nv.getTypeId() == ValueType.ULONG)
      result = m_value.equals(nv.getValue());
    else
      throw new IllegalArgumentException();

    return result;
  }


  public boolean lessThan(Value nv)
  {
    boolean result = false;

    if (nv.getTypeId() == ValueType.ULONG) {
      Long l = (Long)nv.getValue();
      result = (m_value.longValue() < l.longValue());
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

    if (nv.getTypeId() == ValueType.ULONG) {
      Long l = (Long)nv.getValue();
      result = new ULongValue(m_value.longValue() + l.longValue());
    }
    else
      throw new IllegalArgumentException();

    return result;
  }


  public Value minus(Value nv)
  {
    Value result = null;

    if (nv.getTypeId() == ValueType.ULONG) {
      Long l = (Long)nv.getValue();
      long val = m_value.longValue() - l.longValue();
      if (val < 0)
        result = ValueFactory.createLong((int)val);
      else
        result = new ULongValue(val);
    }
    else
      throw new IllegalArgumentException();

    return result;
  }


  public Value multiply(Value nv)
  {
    Value result = null;

    if (nv.getTypeId() == ValueType.ULONG) {
      Long l = (Long)nv.getValue();
      result =
        ValueFactory.createULong(m_value.longValue() * l.longValue());
    }
    else
      throw new IllegalArgumentException();

    return result;
  }


  public Value divide(Value nv)
  {
    Value result = null;

    if (nv.getTypeId() == ValueType.ULONG) {
      Long l = (Long)nv.getValue();
      result = new ULongValue(m_value.longValue() / l.longValue());
    }
    else
      throw new IllegalArgumentException();

    return result;
  }


  public Value negate()
  {
    return ValueFactory.createLong(-1 * m_value.intValue());
  }


  public Value convert(int typeId)
  {
    Value result = null;

    switch (typeId) {
      case ValueType.LONG:
        result = ValueFactory.createLong(m_value.intValue());
        break;

      case ValueType.ULONG:
        result = new ULongValue(m_value);
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










