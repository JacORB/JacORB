
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

package org.jacorb.trading.db.simple.types;

import java.io.*;
import org.omg.CosTradingRepos.ServiceTypeRepositoryPackage.*;

public class Incarnation implements Serializable
{
    private long m_high;
    private long m_low;
    private transient boolean m_dirty;
    private static final long MAX_VALUE = 4294967295L;

    static final long serialVersionUID = -6506601918195268342L;


    private Incarnation(long high, long low)  // for testing
    {
	m_high = high;
	m_low = low;
	m_dirty = true;
    }


    public Incarnation()
    {
	m_high = 0;
	m_low = 1;
	m_dirty = true;
    }


    public Incarnation(IncarnationNumber inc)
    {
	m_high = inc.high;
	m_low = inc.low;
	m_dirty = true;
    }


    public IncarnationNumber getIncarnationNumber()
    {
	return new IncarnationNumber((int)m_high, (int)m_low);
    }

  
    public int compareTo(Incarnation inc)
    {
	int result;

	if ((m_high < inc.m_high) || (m_high == inc.m_high && m_low < inc.m_low))
	    result = -1;
	else if (m_high == inc.m_high && m_low == inc.m_low)
	    result = 0;
	else
	    result = 1;

	return result;
    }


    public void increment()
    {
	if (m_low < MAX_VALUE)
	    m_low++;
	else {
	    m_low = 0;
	    m_high++;
	}
	m_dirty = true;
    }


    public boolean getDirty()
    {
	return m_dirty;
    }


    public String toString()
    {
	return "{" + m_high + "," + m_low + "}";
    }


    private void writeObject(ObjectOutputStream out)
	throws IOException
    {
	out.defaultWriteObject();
	m_dirty = false;
    }


    private void readObject(ObjectInputStream in)
	throws IOException, ClassNotFoundException
    {
	in.defaultReadObject();
	m_dirty = false;
    }


    /**************************** comment out this line for testing

				  public static void main(String[] args)
				  {
				  Incarnation i1 = new Incarnation();
				  System.out.println("i1 = " + i1);
				  for (int i = 0; i < 50; i++)
				  i1.increment();
				  System.out.println("i1 = " + i1);
				  i1.m_low = MAX_VALUE;
				  System.out.println("i1 = " + i1);
				  i1.increment();
				  System.out.println("i1 = " + i1);

				  i1 = new Incarnation(0, 1);
				  Incarnation i2 = new Incarnation(0, 2);
				  System.out.println("i1 (" + i1 + ") vs i2 (" + i2 + ") = " +
				  i1.compareTo(i2));
				  Incarnation i3 = new Incarnation(1, 0);
				  System.out.println("i1 (" + i1 + ") vs i3 (" + i3 + ") = " +
				  i1.compareTo(i3));
				  System.out.println("i1 (" + i1 + ") vs i1 (" + i1 + ") = " +
				  i1.compareTo(i1));
				  System.out.println("i2 (" + i2 + ") vs i1 (" + i1 + ") = " +
				  i2.compareTo(i1));
				  }

				  /**************************** comment out this line for testing */
}










