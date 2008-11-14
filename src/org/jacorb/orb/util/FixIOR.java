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

package org.jacorb.orb.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import org.jacorb.orb.CDRInputStream;
import org.jacorb.orb.CDROutputStream;
import org.jacorb.orb.ORB;
import org.jacorb.orb.ParsedIOR;
import org.omg.IIOP.ProfileBody_1_0;
import org.omg.IIOP.ProfileBody_1_0Helper;
import org.omg.IIOP.ProfileBody_1_1;
import org.omg.IIOP.ProfileBody_1_1Helper;
import org.omg.IOP.IOR;
import org.omg.IOP.TAG_INTERNET_IOP;
import org.omg.IOP.TaggedProfile;

/**
 * Utility class to patch host and port information into an IOR.
 *
 * @author Steve Osselton
 */

public class FixIOR
{
    public static void main (String args[])
        throws IOException
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
        iorString = br.readLine();
        br.close ();

        if (iorString == null)
        {
            System.err.println("cannot read IOR from " + iorFile);
            System.exit(1);
        }

        if (!iorString.startsWith("IOR:"))
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

        pior = new ParsedIOR((ORB) orb, iorString);
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
                os.close();
            }
        }

        pior = new ParsedIOR ((org.jacorb.orb.ORB)orb, ior);

        // Write out new IOR to file

        bw = new BufferedWriter (new OutputStreamWriter (new FileOutputStream (iorFile)));
        bw.write (pior.getIORString ());
        bw.close ();
    }

    private FixIOR ()
    {
    }
}
