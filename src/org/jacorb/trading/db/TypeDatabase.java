
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

package org.jacorb.trading.db;

import java.util.*;
import org.omg.CosTradingRepos.ServiceTypeRepositoryPackage.*;


/**
 * An abstract interface to the service type database
 */
public interface TypeDatabase
{
  /** One of these must be supplied to the begin() method */
  public static final int READ = 0;
  public static final int WRITE = 1;

  /**
   * Must precede any use of the database - may block if the
   * requested lock is not available
   */
  public void begin(int mode);

  /** Must follow any use of the database */
  public void end();


  /**
   * Returns the TypeStruct for the type, or null if the
   * the type doesn't exist
   */
  public TypeStruct describeType(String name);

  /** Returns true if the type was found, false otherwise */
  public boolean maskType(String name);

  /** Returns true if the type was found, false otherwise */
  public boolean unmaskType(String name);

  /** Returns the names of all of the service types */
  public String[] getTypes();

  /**
   * Returns the names of all of the service types whose incarnation
   * is the same or greater than the given one
   */
  public String[] getTypesSince(IncarnationNumber inc);

  /**
   * Returns the current incarnation number (the one that will be
   * assigned to the next service type)
   */
  public IncarnationNumber getIncarnation();

  /** Creates a new service type */
  public IncarnationNumber createType(
    String name,
    String interfaceName,
    PropStruct[] props,
    String[] superTypes);

  /** Returns true if the type was found, false otherwise */
  public boolean removeType(String name);

  /**
   * Returns the name of a subtype of the given type, or null if
   * no subtypes were found
   */
  public String findSubType(String name);

  /**
   * Returns the names of all supertypes of the given type,
   * or null if the type wasn't found
   */
  public String[] getAllSuperTypes(String name);
}










