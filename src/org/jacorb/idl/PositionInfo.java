/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1997-2002  Gerald Brose.
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

package org.jacorb.idl;

/**
 * PositionInfo are used to group information about the current
 * position in the input file. It is created by the lexer but
 * also includes information about the current input stream that
 * is stored here to make switching between input stream easier
 * for GlobalInputStream (when including another file or returning
 * to the including file)
 */

import java.io.File;
import java.io.InputStream;

public class PositionInfo
{

    public String line = "";
    public int line_no = 0;
    public int line_pos = 0;
    public String pragma_prefix = "";
    public File file;
    public InputStream stream;


    public PositionInfo( int _line_no, int _char_pos,
                         String prefix, String _line )
    {
        line_no = _line_no;
        line_pos = _char_pos;
        pragma_prefix = prefix;
        line = _line;
    }

}





















