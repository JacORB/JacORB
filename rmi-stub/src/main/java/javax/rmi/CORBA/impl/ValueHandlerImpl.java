/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1997-2014 Gerald Brose / The JacORB Team.
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

package javax.rmi.CORBA.impl;

import org.omg.CORBA.TCKind;
import org.omg.CORBA.portable.IndirectionException;
import org.omg.CORBA.portable.InputStream;
import org.omg.CORBA.portable.OutputStream;

/**
 * Mininal RMI stub for JacORB
 */
public class ValueHandlerImpl implements javax.rmi.CORBA.ValueHandlerMultiFormat
{
     public ValueHandlerImpl()
    {
    }

    public ValueHandlerImpl(boolean isInputStream)
    {
        this();
    }

    /**
     * Mininal RMI stub for JacORB
     */
    public byte getMaximumStreamFormatVersion()
    {
        return 0;
    }

    /**
     * Mininal RMI stub for JacORB
     */
    public void writeValue(org.omg.CORBA.portable.OutputStream out, java.io.Serializable value, byte streamFormatVersion)
    {
    }

   /**
     * Mininal RMI stub for JacORB
     */
    public void writeValue(org.omg.CORBA.portable.OutputStream _out, java.io.Serializable value)
    {
    }

    /**
     * Mininal RMI stub for JacORB
     */
    public java.io.Serializable readValue(org.omg.CORBA.portable.InputStream _in, int offset, Class<?> clazz,
            String repositoryID, org.omg.SendingContext.RunTime _sender)
    {
        return null;
    }

    /**
     * Mininal RMI stub for JacORB
     */
    public String getRMIRepositoryID(Class<?> clz)
    {
        return null;
    }

    /**
     * Mininal RMI stub for JacORB
     */
    public boolean isCustomMarshaled(Class<?> clz)
    {
        return false;
    }

    /**
     * Mininal RMI stub for JacORB
     */
    public org.omg.SendingContext.RunTime getRunTimeCodeBase()
    {
    	return null;
    }

    /**
     * Mininal RMI stub for JacORB
     */
    public java.io.Serializable writeReplace(java.io.Serializable value)
    {
        return null;
    }
}
