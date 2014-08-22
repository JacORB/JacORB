package org.jacorb.test.sas;

/*
 *        JacORB  - a free Java ORB
 *
 *   Copyright (C) 1997-2014 Gerald Brose / The JacORB Team.
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

import org.jacorb.security.sas.GssUpContext;
import org.jacorb.test.harness.TestUtils;
import org.omg.CORBA.ORB;
import org.omg.IOP.Codec;

public final class ListGssUpContext extends GssUpContext
{
    private final String[][] auth_data = { { "jay", "test"} };

    @Override
    public boolean validateContext(ORB orb, Codec codec, byte[] contextToken)
    {
        boolean b = super.validateContext(orb, codec, contextToken);
        if (b)
        {
            return validateUsernamePassword(initialContextToken.username, initialContextToken.password);
        }
        return b;
    }

    private boolean validateUsernamePassword(byte[] uname, byte[] pswd)
    {
        TestUtils.getLogger().debug("validating...");

        // Get username
        StringBuffer ubuff = new StringBuffer();
        for (int i=0; i < uname.length; i++) ubuff.append((char)uname[i]);
        String username = ubuff.toString();

        // Get Password
        StringBuffer buff = new StringBuffer();
        for (int i=0; i < pswd.length; i++) buff.append((char)pswd[i]);
        String password = buff.toString();

        TestUtils.getLogger().debug("---------> " + username + ", " + password);

        // Verify versus cached data
        boolean valid = false;
        for (int i=0; i < auth_data.length; i++)
        {
            if (auth_data[i][0].equals(username))
            {
                if (auth_data[i][1].equals(password)) valid = true;
                break;
            }
        }
        return valid;
    }
}
