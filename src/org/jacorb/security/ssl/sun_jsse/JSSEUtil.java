package org.jacorb.security.ssl.sun_jsse;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.Provider;
import java.security.Security;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLSocket;

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

/**
 * compatibility wrapper class that allows to
 * use the JSSE included with JDK1.4 or standalone version 1.0.x
 *
 * @author Alphonse Bendt
 * @version $Id$
 */
public class JSSEUtil
{
    private static final boolean isJDK14;

    static
    {
        boolean result;
        try
        {
            Class clazz = Class.forName("javax.net.ssl.SSLSocket");
            clazz.getMethod("setEnabledProtocols", new Class[] {String[].class});
            result = true;
        }
        catch(Exception e)
        {
            result = false;
        }
        isJDK14 = result;
    }

    public static void setEnabledProtocols(SSLSocket socket, String[] enabledProtocols)
    {
        _setEnabledProtocols(socket, enabledProtocols);
    }

    public static void setEnabledProtocols(SSLServerSocket socket, String[] enabledProtocols)
    {
        _setEnabledProtocols(socket, enabledProtocols);
    }

    private static void _setEnabledProtocols(Object socket, String[] enabledProtocols)
    {
        if (!isJDK14)
        {
            // method does not exist in pre JDK 1.4 JSSE
            return;
        }

        try
        {
            Method method = socket.getClass().getMethod("setEnabledProtocols", new Class[] {enabledProtocols.getClass()});
            method.invoke(socket, new Object[] {enabledProtocols});
        }
        catch (InvocationTargetException e)
        {
            throw new RuntimeException(e.getTargetException().toString());
        }
        catch (Exception e)
        {
            // shouldn't happen on JDK1.4
            throw new RuntimeException(e.toString());
        }
    }

    public static void registerSecurityProvider()
    {
        if (isJDK14)
        {
            return;
        }

        try
        {
            Class clazz = Class.forName("com.sun.net.ssl.internal.ssl.Provider");
            Security.addProvider((Provider) clazz.newInstance() );
        }
        catch (Exception e)
        {
            throw new RuntimeException(e.toString());
        }
    }

    public static boolean isJDK14()
    {
        return isJDK14;
    }

    public static boolean wantClientAuth(boolean request_mutual_auth, boolean require_mutual_auth)
    {
        if (isJDK14)
        {
            return request_mutual_auth;
        }

        return request_mutual_auth && !require_mutual_auth;
    }

    public static void setWantClientAuth(SSLServerSocket s, boolean request_mutual_auth)
    {
        if (!isJDK14)
        {
            throw new RuntimeException("Request mutual authentication not supported with JSSE 1.0.x");
        }

        try
        {
            Method method = s.getClass().getMethod("setWantClientAuth", new Class[] {Boolean.TYPE});
            method.invoke(s, new Object[] {Boolean.valueOf(request_mutual_auth)});
        }
        catch(Exception e)
        {
            throw new RuntimeException(e.toString());
        }
    }
}
