package org.jacorb.orb.factory;

/*
 *        Written for JacORB - a free Java ORB
 *
 *   Copyright (C) 2000-2003 Nicolas Noffke, Gerald Brose.
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

import java.net.*;
import java.io.IOException;
import org.jacorb.util.*;
import org.jacorb.orb.*;

public abstract class PortRangeFactory
{
    protected int portMin = 0;
    protected int portMax = 0;

    public PortRangeFactory (String minProp, String maxProp)
    {
        // Get configured max and min port numbers

        portMin = getPortProperty (minProp);
        portMax = getPortProperty (maxProp);

        // Check min < max

        if (portMin >= portMax)
        {
            throw new RuntimeException ("PortRangeFactory: minimum port number not less than maximum");
        }
    }                

    private int getPortProperty (String name)
    {
        String val = Environment.getProperty (name);
        int port;

        // Get configured port number

        if ((val == null) || (val.length () == 0))
        {
            throw new RuntimeException ("PortRangeFactory: " + name + " property not set");
        }
        else
        {
            try
            {
                port = Integer.parseInt (val);
            }
            catch (NumberFormatException ex)
            {
	        throw new RuntimeException ("PortRangeFactory: " + name + " invalid port number");
            }
        }

        // Check sensible port number

        if (port < 0)
        {
           port += 65536;
        }
        if ((port <= 0) || (port > 65535))
        {
	    throw new RuntimeException ("PortRangeFactory: " + name + " invalid port number");
        }

        return port;
    }
}
