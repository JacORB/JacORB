/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1997-2003  Gerald Brose.
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

package org.jacorb.orb.util;

import org.jacorb.orb.giop.CodeSet;
import org.jacorb.orb.ParsedIOR;
import org.jacorb.orb.*;

import org.omg.IOP.*;
import org.omg.GIOP.*;
import org.omg.IIOP.*;
import org.omg.SSLIOP.*;
import org.omg.CSIIOP.*;
import org.omg.CONV_FRAME.*;

import java.io.*;

/**
 * Utility class to patch host and port information into an IOR.
 *
 * @author Steve Osselton
 */

public class FixIOR
{
    public static void main (String args[])
        throws Exception
    {
        org.omg.CORBA.ORB orb;
        String iorString;
        String iorFile;
        String host;
        BufferedReader br;
        BufferedWriter bw;
        CDRInputStream is;
        CDROutputStream os;
        ParsedIOR pior;
        IOR ior;
        TaggedProfile[] profiles;
        ProfileBody_1_0 body10;
        ProfileBody_1_1 body11;
        short port;
        int iport;

        if (args.length != 3)
        {
            System.err.println ("Usage: fixior host port ior_file");
            System.exit( 1 );
        }
        host = args[0];

        // Read in IOR from file

        iorFile = args[2];
        br = new BufferedReader (new FileReader (iorFile));
        iorString = br.readLine ();
        br.close ();

        if (! iorString.startsWith ("IOR:"))
        {
            System.err.println ("IOR must be in the standard IOR URL format");
            System.exit (1);
        }

        iport = Integer.parseInt (args[1]);
        if (iport > 32767)
        {
           iport = iport - 65536;
        }
        port = (short) iport;

        orb = org.omg.CORBA.ORB.init (args, null);

        // Parse IOR

        pior = new ParsedIOR (iorString, orb);
        ior = pior.getIOR ();

        // Iterate through IIOP profiles setting host and port

        profiles = ior.profiles;
        for (int i = 0; i < profiles.length; i++)
        {
            if (profiles[i].tag == TAG_INTERNET_IOP.value)
            {
                is = new CDRInputStream (orb, profiles[i].profile_data);
                is.openEncapsulatedArray ();
                body10 = ProfileBody_1_0Helper.read (is);
                is.close ();

                os = new CDROutputStream ();
                os.beginEncapsulatedArray ();

                if (body10.iiop_version.minor > 0)
                {
                    is = new CDRInputStream (orb, profiles[i].profile_data);
                    is.openEncapsulatedArray ();
                    body11 = ProfileBody_1_1Helper.read (is);
                    is.close ();

                    body11.host = host;
                    body11.port = port;

                    ProfileBody_1_1Helper.write (os, body11);
                }
                else
                {
                    body10.host = host;
                    body10.port = port;

                    ProfileBody_1_0Helper.write (os, body10);
                }
                profiles[i].profile_data = os.getBufferCopy ();
            }
        }

        pior = new ParsedIOR (ior, (org.jacorb.orb.ORB)orb);

        // Write out new IOR to file

        bw = new BufferedWriter (new OutputStreamWriter (new FileOutputStream (iorFile)));
        bw.write (pior.getIORString ());
        bw.close ();
    }

    private FixIOR ()
    {
    }
}
