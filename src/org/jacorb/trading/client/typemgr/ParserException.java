
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

package org.jacorb.trading.client.typemgr;

public class ParserException extends Exception
{
  private int m_line;

  public ParserException(String message, int line)
  {
    super(message);
    m_line = line;
  }

  public int getLine()
  {
    return m_line;
  }
}










