package org.jacorb.orb.dii;

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

import java.util.ArrayList;
import org.omg.CORBA.Bounds;
import org.omg.CORBA.ContextList;


public class ContextListImpl extends ContextList
{
    private ArrayList<String> contexts;

    public ContextListImpl()
    {
        contexts = new ArrayList<String>();
    }

    public int count()
    {
        return contexts.size();
    }

    public void add(String ctx)
    {
        contexts.add(ctx);
    }

    public String item(int index) throws Bounds
    {
        try
        {
            return contexts.get(index);
        }
        catch (ArrayIndexOutOfBoundsException e)
        {
            throw new Bounds();
        }
    }

    public void remove(int index) throws Bounds
    {
        try
        {
            contexts.remove(index);
        }
        catch (ArrayIndexOutOfBoundsException e)
        {
            throw new Bounds();
        }
    }
}
