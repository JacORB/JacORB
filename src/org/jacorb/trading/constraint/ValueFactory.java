
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


public class ValueFactory
{
  private ValueFactory()
  {
  }


  public static Value create(int typeId, Object value)
  {
    Value result = null;

    switch (typeId) {
      case ValueType.BOOLEAN: {
          Boolean b = (Boolean)value;
          result = createBoolean(b.booleanValue());
        }
        break;

      case ValueType.SHORT: {
          Integer i = (Integer)value;
          result = createShort(i.intValue());
        }
        break;

      case ValueType.USHORT: {
          Integer i = (Integer)value;
          result = createUShort(i.intValue());
        }
        break;

      case ValueType.LONG: {
          Integer i = (Integer)value;
          result = createLong(i.intValue());
        }
        break;

      case ValueType.ULONG: {
          Long l = (Long)value;
          result = createULong(l.longValue());
        }
        break;

      case ValueType.FLOAT: {
          Float f = (Float)value;
          result = createFloat(f.floatValue());
        }
        break;

      case ValueType.DOUBLE: {
          Double d = (Double)value;
          result = createDouble(d.doubleValue());
        }
        break;

      case ValueType.CHAR: {
          Character c = (Character)value;
          result = createChar(c.charValue());
        }
        break;

      case ValueType.STRING: {
          String s = (String)value;
          result = createString(s);
        }
        break;
    }

    return result;
  }


  public static Value createBoolean(boolean value)
  {
    return new BooleanValue(value);
  }


  public static Value createShort(int value)
  {
    return new ShortValue(value);
  }


  public static Value createUShort(int value)
  {
    return new UShortValue(value);
  }


  public static Value createLong(int value)
  {
    return new LongValue(value);
  }


  public static Value createULong(long value)
  {
    return new ULongValue(value);
  }


  public static Value createFloat(float value)
  {
    return new FloatValue(value);
  }


  public static Value createDouble(double value)
  {
    return new DoubleValue(value);
  }


  public static Value createChar(char value)
  {
    return new CharValue(value);
  }


  public static Value createString(String value)
  {
    return new StringValue(value);
  }
}




