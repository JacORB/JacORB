
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


public class FloatValue implements Value
{
  private Float m_value;


  public FloatValue()
  {
    this((float)0.0);
  }


  public FloatValue(float value)
  {
    m_value = new Float(value);
  }


  public FloatValue(Object value)
  {
    m_value = (Float)value;
  }


  public void setValue(Object value)
  {
    m_value = (Float)value;
  }


  public int getTypeId()
  {
    return ValueType.FLOAT;
  }


  public Object getValue()
  {
    return m_value;
  }


  public boolean equals(Value nv)
  {
    boolean result = false;

    if (nv.getTypeId() == ValueType.FLOAT)
      result = m_value.equals(nv.getValue());
    else
      throw new IllegalArgumentException();

    return result;
  }


  public boolean lessThan(Value nv)
  {
    boolean result = false;

    if (nv.getTypeId() == ValueType.FLOAT) {
      Float f = (Float)nv.getValue();
      result = (m_value.floatValue() < f.floatValue());
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

    if (nv.getTypeId() == ValueType.FLOAT) {
      Float f = (Float)nv.getValue();
      result = new FloatValue(m_value.floatValue() + f.floatValue());
    }
    else
      throw new IllegalArgumentException();

    return result;
  }


  public Value minus(Value nv)
  {
    Value result = null;

    if (nv.getTypeId() == ValueType.FLOAT) {
      Float f = (Float)nv.getValue();
      result = new FloatValue(m_value.floatValue() - f.floatValue());
    }
    else
      throw new IllegalArgumentException();

    return result;
  }


  public Value multiply(Value nv)
  {
    Value result = null;

    if (nv.getTypeId() == ValueType.FLOAT) {
      Float f = (Float)nv.getValue();
      result = new FloatValue(m_value.floatValue() * f.floatValue());
    }
    else
      throw new IllegalArgumentException();

    return result;
  }


  public Value divide(Value nv)
  {
    Value result = null;

    if (nv.getTypeId() == ValueType.FLOAT) {
      Float f = (Float)nv.getValue();
      result = new FloatValue(m_value.floatValue() / f.floatValue());
    }
    else
      throw new IllegalArgumentException();

    return result;
  }


  public Value negate()
  {
    Value result = null;

    result = new FloatValue(-1 * m_value.floatValue());

    return result;
  }


  public Value convert(int typeId)
  {
    Value result = null;

    switch (typeId) {
      case ValueType.FLOAT:
        result = new FloatValue(m_value);
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










