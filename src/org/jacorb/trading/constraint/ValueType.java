
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


public class ValueType
{
  private int m_id;
  private boolean m_sequence;
  private static String[] s_typeNames = {
    "BOOLEAN",
    "SHORT",
    "USHORT",
    "LONG",
    "ULONG",
    "FLOAT",
    "DOUBLE",
    "CHAR",
    "STRING",
    "OTHER"
  };

  // This array must be in the same order as the enumeration values
  private static String[] s_longTypeNames = {
    "boolean",
    "short",
    "unsigned short",
    "long",
    "unsigned long",
    "float",
    "double",
    "char",
    "string",
    "other"
  };

  public static final int BOOLEAN  = 0;
  public static final int SHORT    = 1;
  public static final int USHORT   = 2;
  public static final int LONG     = 3;
  public static final int ULONG    = 4;
  public static final int FLOAT    = 5;
  public static final int DOUBLE   = 6;
  public static final int CHAR     = 7;
  public static final int STRING   = 8;
  public static final int OTHER    = 9;
  public static final int NUMTYPES = OTHER + 1;


  public ValueType()
  {
    this(OTHER, false);
  }


  public ValueType(int id)
  {
    this(id, false);
  }


  public ValueType(int id, boolean sequence)
  {
    m_id = id;
    m_sequence = sequence;
  }


  public ValueType(ValueType type)
  {
    m_id = type.m_id;
    m_sequence = type.m_sequence;
  }


  public boolean equals(ValueType type)
  {
    return (m_id == type.m_id && m_sequence == type.m_sequence);
  }


  public int getId()
  {
    return m_id;
  }


  public boolean isSequence()
  {
    return m_sequence;
  }


  public boolean isNumber()
  {
    return checkNumeric(m_id);
  }


  public String getTypeName()
  {
    return s_typeNames[m_id];
  }


  public static String getTypeName(int id)
  {
    return s_typeNames[id];
  }


  public String getLongTypeName()
  {
    return s_longTypeNames[m_id];
  }


  public static String getLongTypeName(int id)
  {
    return s_longTypeNames[id];
  }


  public String toString()
  {
    String result;
    if (m_sequence)
      result = "sequence<" + getLongTypeName() + ">";
    else
      result = getLongTypeName();
    return result;
  }


  public static boolean isCompatible(int id1, int id2)
  {
    return (s_promoteTable[id1][id2] != -1);
  }


  public static int promote(int id1, int id2)
  {
    int result = s_promoteTable[id1][id2];

    return result;
  }


  protected boolean checkNumeric(int id)
  {
    boolean result = (id != STRING && id != OTHER && id != BOOLEAN);
    return result;
  }


    // this lookup table defines the compatibility and promotion
    // characteristics of each type; a value of -1 means the types
    // are not compatible, otherwise the table should contain the
    // type to which both types should be promoted

  private static int s_promoteTable[][] = {

      // BOOLEAN
    {
      BOOLEAN,  // BOOLEAN
      -1,       // SHORT
      -1,       // USHORT
      -1,       // LONG
      -1,       // ULONG
      -1,       // FLOAT
      -1,       // DOUBLE
      -1,       // CHAR
      -1,       // STRING
      -1        // OTHER
    },


      // SHORT
    {
      -1,       // BOOLEAN
      LONG,     // SHORT
      LONG,     // USHORT
      LONG,     // LONG
      ULONG,    // ULONG
      FLOAT,    // FLOAT
      DOUBLE,   // DOUBLE
      -1,       // CHAR
      -1,       // STRING
      -1        // OTHER
    },


      // USHORT
    {
      -1,       // BOOLEAN
      LONG,     // SHORT
      LONG,     // USHORT
      LONG,     // LONG
      ULONG,    // ULONG
      FLOAT,    // FLOAT
      DOUBLE,   // DOUBLE
      -1,       // CHAR
      -1,       // STRING
      -1        // OTHER
    },

      // LONG
    {
      -1,       // BOOLEAN
      LONG,     // SHORT
      LONG,     // USHORT
      LONG,     // LONG
      ULONG,    // ULONG
      FLOAT,    // FLOAT
      DOUBLE,   // DOUBLE
      -1,       // CHAR
      -1,       // STRING
      -1        // OTHER
    },

      // ULONG
    {
      -1,       // BOOLEAN
      ULONG,    // SHORT
      ULONG,    // USHORT
      ULONG,    // LONG
      ULONG,    // ULONG
      FLOAT,    // FLOAT
      DOUBLE,   // DOUBLE
      -1,       // CHAR
      -1,       // STRING
      -1        // OTHER
    },

      // FLOAT
    {
      -1,       // BOOLEAN
      FLOAT,    // SHORT
      FLOAT,    // USHORT
      FLOAT,    // LONG
      FLOAT,    // ULONG
      FLOAT,    // FLOAT
      DOUBLE,   // DOUBLE
      -1,       // CHAR
      -1,       // STRING
      -1        // OTHER
    },

      // DOUBLE
    {
      -1,       // BOOLEAN
      DOUBLE,   // SHORT
      DOUBLE,   // USHORT
      DOUBLE,   // LONG
      DOUBLE,   // ULONG
      DOUBLE,   // FLOAT
      DOUBLE,   // DOUBLE
      -1,       // CHAR
      -1,       // STRING
      -1        // OTHER
    },

      // CHAR
    {
      -1,       // BOOLEAN
      -1,       // SHORT
      -1,       // USHORT
      -1,       // LONG
      -1,       // ULONG
      -1,       // FLOAT
      -1,       // DOUBLE
      CHAR,     // CHAR
      STRING,   // STRING
      -1        // OTHER
    },

      // STRING
    {
      -1,       // BOOLEAN
      -1,       // SHORT
      -1,       // USHORT
      -1,       // LONG
      -1,       // ULONG
      -1,       // FLOAT
      -1,       // DOUBLE
      STRING,   // CHAR
      STRING,   // STRING
      -1        // OTHER
    },

      // OTHER
    {
      -1,       // BOOLEAN
      -1,       // SHORT
      -1,       // USHORT
      -1,       // LONG
      -1,       // ULONG
      -1,       // FLOAT
      -1,       // DOUBLE
      -1,       // CHAR
      -1,       // STRING
      -1        // OTHER
    }

  };


  /********************
  public static void main(String[] args)
  {
    int i;
    int id;
    ValueType[] types = new ValueType[ValueType.NUMTYPES];
    for (i = 0; i < ValueType.NUMTYPES; i++)
      types[i] = new ValueType(i);

    for (i = 0; i < ValueType.NUMTYPES; i++)
      System.out.println("Type = " + types[i]);
    System.out.println();

    for (i = 0; i < ValueType.NUMTYPES; i++) {
      for (int j = 0; j < ValueType.NUMTYPES; j++) {
        System.out.print("Types " + types[i].getLongTypeName() + " and " +
          types[j].getLongTypeName() + " are ");

        if (ValueType.isCompatible(types[i].getId(), types[j].getId())) {
          id = ValueType.promote(types[i].getId(), types[j].getId());
          System.out.println("compatible (would be promoted to " +
            ValueType.getLongTypeName(id) + ")");
        }
        else
          System.out.println("not compatible.");
      }
      System.out.println();
    }
  }
  ********************/
}




