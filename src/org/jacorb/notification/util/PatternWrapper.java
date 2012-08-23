package org.jacorb.notification.util;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1999-2012 Gerald Brose / The JacORB Team.
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


/**
 * This is a Wrapper around a PatternMatcher.
 *
 * @author Alphonse Bendt
 */

public abstract class PatternWrapper
{
    private final static Class sDefaultInstance = JDK14PatternWrapper.class;

    public static PatternWrapper init(String patternString)
    {
        try
        {
            PatternWrapper _wrapper;
            _wrapper = (PatternWrapper) sDefaultInstance.newInstance();
            _wrapper.compile(patternString);

            return _wrapper;
        } catch (Exception e)
        {
            throw new RuntimeException(e.toString());
        }
    }

    public abstract void compile(String pattern);

    /**
     * Match the given input against this pattern.
     *
     * @param text
     *            the input to be matched
     * @return the index of the last character matched, plus one or zero if the pattern did not
     *         match.
     */

    public abstract int match(String text);
}