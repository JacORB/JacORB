package org.jacorb.notification.filter.etcl;

import org.jacorb.notification.filter.EvaluationResult;

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

/**
 * ImmutableEvaluationResult.java
 *
 *
 * Created: Thu Jan 02 15:48:45 2003
 *
 * @author Alphonse Bendt
 * @version $Id$
 */

public class ImmutableEvaluationResult extends EvaluationResult
{
    static void unsupported()
    {
        throw new UnsupportedOperationException();
    }

    public void reset()
    {
        unsupported();
    }

    public void setString( String s )
    {
        unsupported();
    }

    public void setFloat( float f )
    {
        unsupported();
    }

    public void setFloat( Double d )
    {
        unsupported();
    }

    public void setInt( int i )
    {
        unsupported();
    }

    public void setInt( Double i )
    {
        unsupported();
    }

    public void setBool( boolean b )
    {
        unsupported();
    }
}
