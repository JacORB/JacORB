package org.jacorb.notification.util;

/*
 *        JacORB - a free Java ORB
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

import org.apache.regexp.RESyntaxException;

/**
 * @author Alphonse Bendt
 * @version $Id$
 */
public class JakartaRegexpPatternWrapper extends PatternWrapper {

    private org.apache.regexp.RE pattern_;

    public JakartaRegexpPatternWrapper() {
        super();
    }


    public void compile(String patternString) {
        try
            {
                pattern_ = new org.apache.regexp.RE( "(" + patternString + ")" );
            }
        catch ( RESyntaxException e )
            {
                throw new RuntimeException( e.getMessage() );
            }
    }


    public int match(String string) {
        boolean _matched = pattern_.match(string);

        if (!_matched) {
            return 0;
        }

        return pattern_.getParenEnd(1);
    }


    public String toString() {
        return pattern_.toString();
    }
}
