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

import org.jacorb.util.Debug;
import org.jacorb.util.Environment;

import org.apache.avalon.framework.logger.Logger;

/**
 * This is a Wrapper around a PatternMatcher.
 */

public abstract class PatternWrapper
{

    static final RuntimeException REGEXP_NOT_AVAILABLE =
        new RuntimeException( "Neither java.util.regex.Pattern nor gnu.regexp available. " +
                              "The package java.util.regex is part of the JDK since v1.4 " +
                              "if you are running an older JDK you'll have to install gnu.regexp " +
                              "to run this NotificationService. Please refer to the documentation " +
                              "for details." );

    static Logger sLogger_ = Debug.getNamedLogger( PatternWrapper.class.getName() );

    static boolean sGnuRegexpAvailable = false;
    static Class sDefaultInstance;

    static {

        if ( isClassAvailable( "java.util.regex.Pattern" ) )
        {

            try
            {
                sDefaultInstance =
                    Environment.classForName( "org.jacorb.notification.util.JDK14PatternWrapper" );
            }
            catch ( ClassNotFoundException e )
            {
                throw new RuntimeException( e.getMessage() );
            }

        }
        else if ( isClassAvailable( "gnu.regexp.RE" ) )
        {

            try
            {
                sDefaultInstance =
                    Environment.classForName( "org.jacorb.notification.util.GNUPatternWrapper" );
            }
            catch ( ClassNotFoundException e )
            {
                throw new RuntimeException( e.getMessage() );
            }

        }
        else
        {
            throw REGEXP_NOT_AVAILABLE;
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
            if ( sDefaultInstance == null )
            {
                throw REGEXP_NOT_AVAILABLE;
            }

            sLogger_.error( "Init of PatternWrapper failed: ", e );

            throw new RuntimeException( e.getMessage() );
        }
    }

    public abstract void compile( String pattern );

    public abstract int match( String text );

    private static boolean isClassAvailable( String name )
    {
        try
        {
            Environment.classForName( name );

            return true;
        }
        catch ( ClassNotFoundException e )
        {}

        return false;
    }
}
