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

import gnu.regexp.REException;
import gnu.regexp.REMatch;

/**
 * @author Alphonse Bendt
 * @version $Id$
 */
public class GNUPatternWrapper extends PatternWrapper
{
    private gnu.regexp.RE pattern_;

    public GNUPatternWrapper() {}

    public void compile( String patternString )
    {
        try
        {
            pattern_ = new gnu.regexp.RE( patternString );
        }
        catch ( REException e )
        {
            throw new RuntimeException( e.getMessage() );
        }
    }

    public int match( String text )
    {
        REMatch[] _match = pattern_.getAllMatches( text );

        if ( _match.length > 0 )
        {
            int _last = _match.length - 1;
            return _match[ _last ].getEndIndex();
        }
        else
        {
            return 0;
        }
    }

    public String toString()
    {
        return pattern_.toString();
    }
}
