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
 * The <code>JSRandom</code> interface allows developers to plugin their
 * own instantiation of a SecureRandom. For instance this then allows them
 * to, in their class, initialise ahead of time, or set their own seed.
 *
 * @author Nick Cross
 * @version $Id$
 */
public interface JSRandom
{
    /**
     * <code>getSecureRandom</code> returns a SecureRandom object for the SSLContext.
     *
     * @return a <code>SecureRandom</code> value
     */
    SecureRandom getSecureRandom ();
}
