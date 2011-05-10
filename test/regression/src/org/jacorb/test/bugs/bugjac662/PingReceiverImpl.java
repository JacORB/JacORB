package org.jacorb.test.bugs.bugjac662;

/*
 * JacORB - a free Java ORB
 *
 * Copyright (C) 1997-2011 Gerald Brose / The JacORB Team.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Library General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any
 * later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Library General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Library General Public License
 * along with this library; if not, write to the Free Software Foundation, Inc.,
 * 675 Mass Ave, Cambridge, MA 02139, USA.
 */


public class PingReceiverImpl extends PingReceiverPOA
{
    private static int counter = 0;

    public String ping ()
    {
        counter++;
//         try
//         {
//             Thread.sleep (5000);
//         }
//         catch (InterruptedException e)
//         {
//         }

        System.out.println ("ping() invoked");
        return String.valueOf(counter);
    }

    public String shutdown ()
    {
        Runtime.getRuntime().halt(1);
        return "shutdown";
    }
}
