package org.jacorb.test.orb.rmi;

import org.jacorb.test.orb.rmi.Boo;
import org.jacorb.test.orb.rmi.Foo;

/*
 *        JacORB  - a free Java ORB
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

public class RMITestUtil
{
    public final static String STRING =
        "the quick brown fox jumps over the lazy dog";

    public static String primitiveTypesToString(boolean flag, char c, byte b,
                                                short s, int i, long l, 
                                                float f, double d)
    {
        String str = "flag:\t" + flag + "\n"
                    + "c:\t" + c + "\n"
                    + "b:\t" + b + "\n"
                    + "s:\t" + s + "\n"
                    + "i:\t" + i + "\n"
                    + "l:\t" + l + "\n"
                    + "f:\t" + f + "\n"
                    + "d:\t" + d + "\n";
        return str;
    }

    public static String echo(String s)
    {
        return s + " (echoed back)";
    }

    public static Foo echoFoo(Foo f)
    {
        Foo newFoo = new Foo(f.i, f.s);
        newFoo.i++;
        newFoo.s += " <";
        return newFoo;
    }

    public static Boo echoBoo(Boo f)
    {
        Boo newBoo = new Boo(f.id, f.name);
        newBoo.id += "+";
        newBoo.name += " <";
        return newBoo;
    }

}
