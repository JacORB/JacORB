package org.jacorb.util;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 2002-2012 Gerald Brose / The JacORB Team.
 *
 *   This library is free software; you can redistribute it and/or
 *   modify it under the terms of the GNU Library General Public
 *   License as published by the Free Software Foundation; either
 *   version 2 of the License, or (at your option) any later version.
 *
 *   This library is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *   Library General Public License for more details.
 *
 *   You should have received a copy of the GNU Library General Public
 *   License along with this library; if not, write to the Free
 *   Software Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 *
 */

import org.jacorb.orb.CDRInputStream;
import org.jacorb.orb.CDROutputStream;
import org.omg.TimeBase.UtcT;
import org.omg.TimeBase.UtcTHelper;

/**
 * Contains static methods to handle CORBA time values.
 *
 * @author Andre Spiegel <spiegel@gnu.org>
 */
public final class Time
{
    private Time()
    {
    }

    /**
     * Difference between the CORBA Epoch and the Unix Epoch: the time
     * from 1582/10/15 00:00 until 1970/01/01 00:00 in 100 ns units.
     */
    public static final long UNIX_OFFSET = 122192928000000000L;

    /**
     * Returns the current time as a CORBA UtcT.
     */
    public static UtcT corbaTime()
    {
        return corbaTime(System.currentTimeMillis());
    }

    /**
     * Converts the given unixTime into a CORBA UtcT.
     * @param unixTime the number of milliseconds since 1970/01/01 00:00 UTC.
     */
    public static UtcT corbaTime(long unixTime)
    {
        UtcT result = new UtcT();

        result.time = (unixTime * 10000) + UNIX_OFFSET;

        // unixTime is always UTC.
        // Therefore, no time zone offset.
        result.tdf  = 0;

        // nothing reasonable to put here
        result.inacchi = 0;
        result.inacclo = 0;

        return result;
    }

    /**
     * Converts the given Java date into a CORBA UtcT.
     */
    public static UtcT corbaTime(java.util.Date date)
    {
        return corbaTime(date.getTime());
    }

    /**
     * Returns a CORBA UtcT that represents an instant that lies
     * a given number of CORBA time units (100 ns) in the future.
     * If the argument is negative, returns null.
     */
    public static UtcT corbaFuture(long corbaUnits)
    {
        if (corbaUnits < 0)
        {
            return null;
        }

        UtcT result = corbaTime();
        result.time = result.time + corbaUnits;
        return result;
    }

    /**
     * Returns the number of milliseconds between now and the given CORBA
     * time.  The value is positive if that time is in the future, and
     * negative otherwise.
     */
    public static long millisTo(UtcT time)
    {
        long unixTime = (time.time - UNIX_OFFSET) / 10000;

        // if the time is not UTC, correct time zone
        if (time.tdf != 0)
        {
            unixTime = unixTime - (time.tdf * 60000);
        }

        return unixTime - System.currentTimeMillis();
    }

    /**
     * Returns true if the instant represented by the given UtcT is
     * already in the past, false otherwise.  As a special convenience,
     * this method also returns false if the argument is null.
     */
    public static boolean hasPassed(UtcT time)
    {
        if (time != null)
        {
            return millisTo(time) < 0;
        }

        return false;
    }

    /**
     * Compares two UtcT time values and returns that which is earlier.
     * Either argument can be null; this is considered as a time that
     * lies indefinitely in the future.  If both arguments are null,
     * this method returns null itself.
     */
    public static UtcT earliest(UtcT timeA, UtcT timeB)
    {
        if (timeA == null)
        {
            if (timeB == null)
            {
                return null;
            }
            return timeB;
        }

        if (timeB == null || timeA.time <= timeB.time)
        {
            return timeA;
        }
        return timeB;
    }

    /**
     * Returns a CDR encapsulation of the given UtcT.
     */
    public static byte[] toCDR(UtcT time)
    {
        final CDROutputStream out = new CDROutputStream();
        try
        {
            out.beginEncapsulatedArray();
            UtcTHelper.write(out, time);
            return out.getBufferCopy();
        }
        finally
        {
            out.close();
        }
    }

    /**
     * Decodes a CDR encapsulation of a UtcT.
     */
    public static UtcT fromCDR(byte[] buffer)
    {
        final CDRInputStream in = new CDRInputStream(buffer);
        try
        {
            in.openEncapsulatedArray();
            return UtcTHelper.read(in);
        }
        finally
        {
            in.close();
        }
    }

    /**
     * This method blocks until the given time has been reached.
     * If the time is null, or it has already passed,
     * then this method returns immediately.
     */
    public static void waitFor(UtcT time)
    {
        if (time != null)
        {
            long now = System.currentTimeMillis();
            long delta = Time.millisTo(time);
            long then = now + delta;

            while (delta > 0)
            {
                try
                {
                    Thread.sleep(delta);
                }
                catch (InterruptedException e)
                {
                    // ignored
                }

                delta = then - System.currentTimeMillis();
            }
        }
    }
}
