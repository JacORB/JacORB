package org.jacorb.test.orb.value;

/*
 *        JacORB  - a free Java ORB
 *
 *   Copyright (C) 1997-2011 Gerald Brose / The JacORB Team.
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

public class FooImpl extends FooPOA 
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public String foo_op(String inarg) throws BadInput 
	{
        String retval = "bad";

        if (inarg.equals("foo_op"))
        {
            retval = "good";
        }
        else
        {
            throw new BadInput("expected \"foo_op\"\n");
        }

        return retval;
	}

	public String base_op(String inarg) throws BadInput 
	{
        String retval = "bad";

        if (inarg.equals("base_op"))
        {
            retval = "good";
        }
        else
        {
            throw new BadInput("expected \"base_op\"\n");
        }

        return retval;
	}

}
