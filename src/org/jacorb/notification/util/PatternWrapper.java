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

import org.apache.log.Hierarchy;
import org.apache.log.Logger;

/**
 * This is a Wrapper around a PatternMatcher.
 */

abstract class PatternWrapper
{

    static Logger sLogger_ =
        Hierarchy.getDefaultHierarchy().getLoggerFor( PatternWrapper.class.getName() );

    static boolean sGnuRegexpAvailable = false;
    static Class sDefaultInstance;

    static {
        try
        {
            Class.forName( "gnu.regexp.RE" );

            sDefaultInstance =
                Class.forName( "org.jacorb.notification.util.GNUPatternWrapper" );

            sGnuRegexpAvailable = true;
        }
        catch ( Exception e )
        {
            // ignore
        }

        if ( !sGnuRegexpAvailable )
        {
            try
            {
                Class.forName( "java.util.regex.Pattern" );

                sDefaultInstance =
                    Class.forName( "org.jacorb.notification.util.JDK14PatternWrapper" );
            }
            catch ( Exception e )
            {
                throw new RuntimeException( "Neither java.util.regex.Pattern nor gnu.regexp available !" );
            }
        }
    }

    static PatternWrapper init( String patternString )
    {
        try
        {
            PatternWrapper _wrapper;
            _wrapper = ( PatternWrapper ) sDefaultInstance.newInstance();
            _wrapper.compile( patternString );
            return _wrapper;
        }
        catch ( Exception e )
        {
	    sLogger_.error("Init of PatternWrapper failed: ", e);

            throw new RuntimeException( e.getMessage() );
        }
    }

    public abstract void compile( String pattern );

    public abstract int match( String text );
}
