package org.jacorb.notification.util;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1999-2003 Gerald Brose
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
 *
 */

import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * @version $Id$
 */

class JDK14PatternWrapper extends PatternWrapper
{

    private Pattern pattern_;

    public void compile( String patternString )
    {
        pattern_ = java.util.regex.Pattern.compile( patternString );
    }

    public int match( String text )
    {
        Matcher _m = pattern_.matcher( text );

        if ( _m.find() )
        {
            return _m.end();
        }
        else
        {
            return 0;
        }
    }

    public String toString()
    {
        return pattern_.pattern();
    }
}
