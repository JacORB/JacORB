
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


public interface PropertySchema
{
  /** Determines whether the property exists */
  public boolean exists(String property);

  /**
   * Returns the type of the property, or null if the property
   * wasn't found
   */
  public ValueType getPropertyType(String property);
}




