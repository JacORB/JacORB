package org.jacorb.idl;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1997-2004 Gerald Brose.
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
 */

public final class Environment
{
    /**
     * <code>JAVA14</code> denotes whether we are using JDK 1.4 or above.
     */
    static boolean JAVA14 = false;

    /**
     * <code>JAVA15</code> denotes whether we are using JDK 1.5 or above.
     */
    static boolean JAVA15 = false;

    static
    {
        String javaVer = System.getProperty ("java.version");
        int javaVersionAsInt = 0;
        try
        {
            javaVersionAsInt = Integer.parseInt (""+javaVer.charAt(0))*10
                             + Integer.parseInt (""+javaVer.charAt(2));
        }
        catch (Exception javaVersionFormatChanged)
        {
            // I don't think this exception can/would ever occur but for
            // the sake of robustness...
            javaVersionFormatChanged.printStackTrace();
        }
        JAVA14 = javaVersionAsInt >= 14;
        JAVA15 = javaVersionAsInt >= 15;
    }

    /**
     * <code>intToPriority</code> returns the priority level for a given integer.
     * It is a copy of the method in util/LogKitLoggerFactory copied to ensure
     * that the idl compiler can be standalone.
     *
     * @param priority an <code>int</code> value
     * @return an <code>org.apache.log.Priority</code> value
     */
    public static org.apache.log.Priority intToPriority(int priority)
    {
        switch (priority)
        {
            case 4 :
                return org.apache.log.Priority.DEBUG;
            case 3 :
                return org.apache.log.Priority.INFO;
            case 2 :
                return org.apache.log.Priority.WARN;
            case 1 :
                return org.apache.log.Priority.ERROR;
            case 0 :
            default :
                return org.apache.log.Priority.FATAL_ERROR;
        }
    }
}
