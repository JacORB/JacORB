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
    static final boolean JAVA16;

    static final boolean JAVA14;

    static final boolean JAVA15;

    static final String NL = System.getProperty("line.separator");
    
    static
    {
        final String javaVer = System.getProperty ("java.specification.version");

        if ("1.4".equals(javaVer))
        {
            JAVA14 = true;
            JAVA15 = false;
            JAVA16 = false;
        }
        else if ("1.5".equals(javaVer))
        {
            JAVA15 = true;
            JAVA14 = false;
            JAVA16 = false;

        }
        else if ("1.6".equals(javaVer))
        {
            JAVA16 = true;
            JAVA15 = false;
            JAVA14 = false;
        }
        else
        {
            throw new AssertionError("should never happen");
        }
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
