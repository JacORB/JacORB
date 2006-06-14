package org.jacorb.security.ssl.sun_jsse;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 2000-2006 Gerald Brose
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

import java.security.SecureRandom;

/**
 * <code>JSRandomImpl</code> is a basic example showing an initialization of
 * SecureRandom.
 *
 * @author Nick Cross
 * @version $Id$
 */
public class JSRandomImpl implements JSRandom
{
    /**
     * <code>getSecureRandom</code> returns a new SecureRandom initialized
     * with a fixed seed. Note that using such a simplistic seed is a security risk.
     *
     * @return a <code>SecureRandom</code> value
     */
    public SecureRandom getSecureRandom()
    {
        // Explicitely initialize SecureRandom with a fixed seed
        SecureRandom rnd = new SecureRandom();
        rnd.setSeed(4711);

        return rnd;
    }


    /**
     * <code>toString</code> override for debug.
     *
     * @return a <code>String</code> value
     */
    public String toString()
    {
        return "JacORB example JSRandom (" + super.toString() + ')';
    }
}