
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


public interface Value
{
  public void setValue(Object value);
  public int getTypeId();
  public Object getValue();

  public boolean equals(Value nv);
  public boolean lessThan(Value nv);
  public boolean lessThanEqual(Value nv);
  public boolean greaterThan(Value nv);
  public boolean greaterThanEqual(Value nv);
  public Value plus(Value nv);
  public Value minus(Value nv);
  public Value multiply(Value nv);
  public Value divide(Value nv);
  public Value negate();

  public Value convert(int typeId);

  public String toString();
}










