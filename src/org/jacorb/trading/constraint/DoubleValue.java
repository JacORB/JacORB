
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


public class DoubleValue implements Value
{
  private Double m_value;


  public DoubleValue()
  {
    this(0.0);
  }


  public DoubleValue(double value)
  {
    m_value = new Double(value);
  }


  public DoubleValue(Object value)
  {
    m_value = (Double)value;
  }


  public void setValue(Object value)
  {
    m_value = (Double)value;
  }


  public int getTypeId()
  {
    return ValueType.DOUBLE;
  }


  public Object getValue()
  {
    return m_value;
  }


  public boolean equals(Value nv)
  {
    boolean result = false;

    if (nv.getTypeId() == ValueType.DOUBLE)
      result = m_value.equals(nv.getValue());
    else
      throw new IllegalArgumentException();

    return result;
  }


  public boolean lessThan(Value nv)
  {
    boolean result = false;

    if (nv.getTypeId() == ValueType.DOUBLE) {
      Double d = (Double)nv.getValue();
      result = (m_value.doubleValue() < d.doubleValue());
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

    if (nv.getTypeId() == ValueType.DOUBLE) {
      Double d = (Double)nv.getValue();
      result = new DoubleValue(m_value.doubleValue() + d.doubleValue());
    }
    else
      throw new IllegalArgumentException();

    return result;
  }


  public Value minus(Value nv)
  {
    Value result = null;

    if (nv.getTypeId() == ValueType.DOUBLE) {
      Double d = (Double)nv.getValue();
      result = new DoubleValue(m_value.doubleValue() - d.doubleValue());
    }
    else
      throw new IllegalArgumentException();

    return result;
  }


  public Value multiply(Value nv)
  {
    Value result = null;

    if (nv.getTypeId() == ValueType.DOUBLE) {
      Double d = (Double)nv.getValue();
      result = new DoubleValue(m_value.doubleValue() * d.doubleValue());
    }
    else
      throw new IllegalArgumentException();

    return result;
  }


  public Value divide(Value nv)
  {
    Value result = null;

    if (nv.getTypeId() == ValueType.DOUBLE) {
      Double d = (Double)nv.getValue();
      result = new DoubleValue(m_value.doubleValue() / d.doubleValue());
    }
    else
      throw new IllegalArgumentException();

    return result;
  }


  public Value negate()
  {
    Value result = null;

    result = new DoubleValue(-1 * m_value.doubleValue());

    return result;
  }


  public Value convert(int typeId)
  {
    Value result = null;

    switch (typeId) {
      case ValueType.DOUBLE:
        result = new DoubleValue(m_value);
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










