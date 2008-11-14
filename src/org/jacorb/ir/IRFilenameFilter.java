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

package org.jacorb.ir;

import java.io.File;


public class IRFilenameFilter
    implements java.io.FilenameFilter
{
    private String suffix = null;

    public IRFilenameFilter( String _suffix)
    {
        suffix = _suffix;
    }

    public boolean accept( File f, String name )
    {
        // file names must either end in .class or
        // have no dot in them

        if( suffix != null )
            return name.endsWith(".class");
        else
        {
            return ( f.isDirectory() && name.indexOf('.') == -1);
        }
    }
}










