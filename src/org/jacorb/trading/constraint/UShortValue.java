
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


public class UShortValue implements Value
{
  private Integer m_value;


  public UShortValue()
  {
    this(0);
  }


  public UShortValue(int value)
  {
    m_value = new Integer(value);
  }


  public UShortValue(Object value)
  {
    m_value = (Integer)value;
  }


  public void setValue(Object value)
  {
    m_value = (Integer)value;
  }


  public int getTypeId()
  {
    return ValueType.USHORT;
  }


  public Object getValue()
  {
    return m_value;
  }


  public boolean equals(Value nv)
  {
    boolean result = false;

    if (nv.getTypeId() == ValueType.USHORT)
      result = m_value.equals(nv.getValue());
    else
      throw new IllegalArgumentException();

    return result;
  }


  public boolean lessThan(Value nv)
  {
    boolean result = false;

    if (nv.getTypeId() == ValueType.USHORT) {
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

    if (nv.getTypeId() == ValueType.USHORT) {
      Integer i = (Integer)nv.getValue();
      result = new UShortValue(m_value.intValue() + i.intValue());
    }
    else
      throw new IllegalArgumentException();

    return result;
  }


  public Value minus(Value nv)
  {
    Value result = null;

    if (nv.getTypeId() == ValueType.USHORT) {
      Integer i = (Integer)nv.getValue();
      int val = m_value.intValue() - i.intValue();
      if (val < 0)
        result = ValueFactory.createLong(val);
      else
        result = new UShortValue(val);
    }
    else
      throw new IllegalArgumentException();

    return result;
  }


  public Value multiply(Value nv)
  {
    Value result = null;

    if (nv.getTypeId() == ValueType.USHORT) {
      Integer i = (Integer)nv.getValue();
      result = ValueFactory.createULong(m_value.intValue() * i.intValue());
    }
    else
      throw new IllegalArgumentException();

    return result;
  }


  public Value divide(Value nv)
  {
    Value result = null;

    if (nv.getTypeId() == ValueType.USHORT) {
      Integer i = (Integer)nv.getValue();
      result = new UShortValue(m_value.intValue() / i.intValue());
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
      case ValueType.USHORT:
        result = new UShortValue(m_value);
        break;

      case ValueType.LONG:
        result = ValueFactory.createLong(m_value.intValue());
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










