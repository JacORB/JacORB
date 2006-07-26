/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1997-2004 Gerald Brose.
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

package org.jacorb.orb.rmi;

/**
 * @author Gerald Brose
 * @version $Id$
 */

import java.rmi.Remote;

import javax.rmi.CORBA.Tie;
import javax.rmi.CORBA.Stub;
import javax.rmi.CORBA.Util;

import org.omg.CORBA.ORB;
import org.omg.CORBA.portable.ObjectImpl;

public class PortableRemoteObjectDelegateImpl implements javax.rmi.CORBA.PortableRemoteObjectDelegate
{
    private static org.jacorb.orb.ORB _orb = null;

    /**
     * Return the ORB to be used for RMI communications.
     * @return The ORB
     */
    public static synchronized ORB getORB()
    {
        if ( _orb == null )
        {
            _orb = (org.jacorb.orb.ORB)ORB.init( new String[0], null );
        }
        return _orb;
    }

    /**
     * Set the ORB to be used for RMI communications.
     * @param orb   The ORB to use
     */
    public static synchronized void setORB( ORB orb )
    {
        if ( _orb != null )
        {
            throw new IllegalStateException( "RMI orb has already been initialized" );
        }
        _orb = (org.jacorb.orb.ORB)orb;
    }

    /**
     * Export an RMI object as a CORBA object
     * @see javax.rmi.CORBA.PortableRemoteObjectDelegate#exportObject(java.rmi.Remote)
     */
    public void exportObject( java.rmi.Remote obj ) throws java.rmi.RemoteException
    {
        if (obj == null)
        {
            throw new IllegalArgumentException();
        }

        if ( obj instanceof Stub )
        {
            throw new java.rmi.server.ExportException( "Attempted to export a stub class" );
        }
        Tie tie = Util.getTie( obj );
        if ( tie != null )
        {
            throw new java.rmi.server.ExportException( "Object already exported" );
        }
        tie = toTie( obj );
        tie.orb( getORB() );
        Util.registerTarget( tie, obj );
    }

    /**
     * Return the Stub for a RMI object.
     * @param obj   The RMI object
     * @return The Stub object
     * @see javax.rmi.CORBA.PortableRemoteObjectDelegate#toStub(java.rmi.Remote)
     */
    public java.rmi.Remote toStub( java.rmi.Remote obj ) throws java.rmi.NoSuchObjectException
    {
        if ( obj instanceof Stub )
        {
            return obj;
        }

        Tie tie = null;
        if ( obj instanceof Tie )
        {
            tie = ( Tie ) obj;
            obj = tie.getTarget();
        }
        else
        {
            tie = Util.getTie( obj );
        }
        if ( tie == null )
        {
            throw new java.rmi.NoSuchObjectException( "Object not exported" );
        }

        final org.omg.CORBA.portable.ObjectImpl thisObject = (ObjectImpl) tie.thisObject();
        final String[] ids = thisObject._ids();

        for (int i = 0; i < ids.length; ++i)
        {
            final String repoID = ids[i];
            final String stubClazzName = newRMIStubName(repoID);

            try
            {
                final Stub stub = newStub(stubClazzName, obj.getClass());

                stub._set_delegate(thisObject._get_delegate());

                return (Remote) stub;
            } catch (ClassNotFoundException e)
            {
                // ignored
            } catch (InstantiationException e)
            {
                // ignored
            } catch (IllegalAccessException e)
            {
                // ignored
            }
        }

        throw new java.rmi.NoSuchObjectException( "Object not exported" );
    }

    /**
     * Deactivate the exported RMI object.
     * @param obj   The RMI object
     * @see javax.rmi.CORBA.PortableRemoteObjectDelegate#unexportObject(java.rmi.Remote)
     */
    public void unexportObject( java.rmi.Remote obj ) throws java.rmi.NoSuchObjectException
    {
        Tie tie = Util.getTie( obj );
        if ( tie == null )
        {
            throw new java.rmi.NoSuchObjectException( "Object not exported" );
        }
        Util.unexportObject( obj );
    }

    /**
     * Narrow the remote object.
     * @param obj   The remote object
     * @param newClass  The class to narrow to
     * @return the narrowed object
     * @see javax.rmi.CORBA.PortableRemoteObjectDelegate#narrow(java.lang.Object, java.lang.Class)
     */
    public Object narrow( Object obj, Class newClass ) throws ClassCastException
    {
        if (newClass == null)
        {
            throw new ClassCastException("Can't narrow to null class");
        }
        if (obj == null)
        {
            return null;
        }

        Class fromClass = obj.getClass();

        try
        {
            if (newClass.isAssignableFrom(fromClass))
            {
                return obj;
            }
        }
        catch(Exception e)
        {
            // ignored
        }

        throw new ClassCastException("Can't narrow from " + fromClass + " to " + newClass);
    }

    /**
     * @see javax.rmi.CORBA.PortableRemoteObjectDelegate#connect(java.rmi.Remote, java.rmi.Remote)
     */
    public void connect( java.rmi.Remote target, java.rmi.Remote source ) throws java.rmi.RemoteException
    {
        throw new UnsupportedOperationException("Not implemented for PortableRemoteObjectDelegateImpl");
    }

    /**
     * Return the Tie object for an RMI object.
     * @param obj   The RMI object.
     * @return  The Tie object
     * @throws java.rmi.server.ExportException
     */
    private Tie toTie(java.rmi.Remote obj) throws java.rmi.server.ExportException
    {
        for (Class clz = obj.getClass(); clz != null; clz = clz.getSuperclass())
        {
            try
            {
                final String tieClzName = newRMIClassName(clz.getName(), "Tie");
                return newTie(tieClzName, clz);
            }
            catch (ClassNotFoundException e)
            {
                throw new java.rmi.server.ExportException("ClassNotFoundException: ", e );
            }
            catch (InstantiationException e)
            {
                throw new java.rmi.server.ExportException("InstantiationException: ", e);
            }
            catch (IllegalAccessException e)
            {
                throw new java.rmi.server.ExportException("IllegalAccessException: ", e);
            }
        }
        throw new java.rmi.server.ExportException("Tie class not found ");
    }

    /**
     * Java Language to IDL Mapping 00-01-06 1.4.6:
     *
     * The stub class corresponding to an RMI/IDL interface or implementation class may
     * either be in the same package as its associated interface or class, or may be further
     * qualified by the org.omg.stub package prefix.
     *
     * When loading a stub class corresponding to an interface or class
     * <packagename>.<typename>, the class <packagename>._<typename>_Stub shall be
     * used if it exists; otherwise, the class org.omg.stub.<packagename>._<typename>_Stub
     * shall be used.
     */
    private Stub newStub(String clazzName, Class source) throws InstantiationException, IllegalAccessException, ClassNotFoundException
    {
        try
        {
            return (Stub) newInstance(clazzName, source);
        }
        catch (ClassNotFoundException e)
        {
            return (Stub) newInstance("org.omg.stub." + clazzName, source);
        }
    }

    private javax.rmi.CORBA.Tie newTie(String clazzName, Class source) throws InstantiationException, IllegalAccessException, ClassNotFoundException
    {
        try
        {
            return (Tie) newInstance(clazzName, source);
        }
        catch (ClassNotFoundException e)
        {
            return (Tie) newInstance("org.omg.stub." + clazzName, source);
        }
    }

    private Object newInstance(String clazzName, Class source) throws InstantiationException, IllegalAccessException, ClassNotFoundException
    {
        return loadClass(clazzName, source).newInstance();
    }

    private Class loadClass(String clazzName, Class source) throws ClassNotFoundException
    {
        return Util.loadClass(clazzName, Util.getCodebase(source), source.getClassLoader());
    }

    private String newRMIStubName(final String repoID)
    {
        final String clazzName = repoID.substring(4, repoID.lastIndexOf(':'));
        return newRMIClassName(clazzName, "Stub");
    }

    private String newRMIClassName(final String name, final String suffix)
    {
        final StringBuffer buffer = new StringBuffer(name.length() + 2 + suffix.length());
        final int idx = name.lastIndexOf('.') + 1;
        buffer.append(name.substring(0, idx));
        buffer.append('_');
        buffer.append(name.substring(idx, name.length()));
        buffer.append('_');
        buffer.append(suffix);

        return buffer.toString();
    }
}
