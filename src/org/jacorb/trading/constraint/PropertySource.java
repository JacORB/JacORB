
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


/**
 * Defines an interface to allow a Property node in the syntax tree
 * to obtain the value of a property
 */
public interface PropertySource
{
  public boolean exists(String property);

  public Value getValue(String property);

  public Value[] getSequenceValues(String property);
}




