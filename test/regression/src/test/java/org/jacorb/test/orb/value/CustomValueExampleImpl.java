package org.jacorb.test.orb.value;

import org.jacorb.test.harness.TestUtils;

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

/**
 * <code>CustomValueExampleImpl</code> is the valuetype implementation.
 *
 * @author <a href="mailto:rnc@prismtechnologies.com"></a>
 * @version 1.0
 */
public class CustomValueExampleImpl extends CustomValueExample
{
    /**
     * <code>number</code> returns number_state value.
     *
     * @return an <code>int</code> value
     */
    @Override
    public int number()
    {
        return number_state;
    }


    /**
     * <code>print</code> prints out number and name states.
     *
     */
    @Override
    public void print()
    {
        TestUtils.getLogger().debug
            ("CustomValueExampleImpl with " + number_state + " and " + name_state);
    }


    /**
     * <code>marshal</code> marshals the valuetype.
     * If number state has been tagged with a value of -1000 then it will incorrectly
     * marshal it for error checking purposes.
     *
     * @param os an <code>org.omg.CORBA.DataOutputStream</code> value
     */
    public void marshal(org.omg.CORBA.DataOutputStream os)
    {
        TestUtils.getLogger().debug( "Invoke the marshal operation..." + number_state + " and name " + name_state);

        os.write_string( "Here is an additional message in the marshalling" );
        if (number_state == -1000)
        {
            os.write_string( "This will cause the unmarshal to fail." );
        }
        os.write_long( number_state );
        os.write_string( name_state );
    }

    /**
     * <code>unmarshal</code> unmarshals the valuetype.
     *
     * @param is an <code>org.omg.CORBA.DataInputStream</code> value
     */
    public void unmarshal( org.omg.CORBA.DataInputStream is )
    {
        try
        {
            TestUtils.getLogger().debug( "Invoke the unmarshal operation..." );
            TestUtils.getLogger().debug( is.read_string() );
            TestUtils.getLogger().debug( "Extracted extra message..." );

            number_state = is.read_long();
            name_state = is.read_string();
        }
        catch (Exception e)
        {
            throw new org.omg.CORBA.MARSHAL
                ("Caught exception " + e + " when unmarshalling.");
        }
    }
}
