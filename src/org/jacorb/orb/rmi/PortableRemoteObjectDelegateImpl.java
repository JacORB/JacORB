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

import javax.rmi.CORBA.Tie;
import javax.rmi.CORBA.Stub;
import javax.rmi.CORBA.Util;

import org.omg.CORBA.ORB;

public class PortableRemoteObjectDelegateImpl implements javax.rmi.CORBA.PortableRemoteObjectDelegate
{
    private static ORB _orb = null;
    
    /**
     * Return the ORB to be used for RMI communications.
     * @return The ORB
     */
    public static synchronized ORB getORB()
    {
        if ( _orb == null )
        {
            System.out.println("Unknwon ORB");
            _orb = ORB.init( new String[0], null );
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
        _orb = orb;
    }

    /**
     * Export an RMI object as a CORBA object
     * @see javax.rmi.CORBA.PortableRemoteObjectDelegate#exportObject(java.rmi.Remote)
     */
    public void exportObject( java.rmi.Remote obj ) throws java.rmi.RemoteException
    {
        if (obj == null) throw new NullPointerException();
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
        
        org.omg.CORBA.Object thisObject = tie.thisObject();
        if ( thisObject instanceof java.rmi.Remote )
        {
            return ( java.rmi.Remote ) thisObject;
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
            throw new ClassCastException("Can't narrow to null class");
        if (obj == null)
            return null;
        
        Class fromClass = obj.getClass();
        Object result = null;
        
        try
        {
            if (newClass.isAssignableFrom(fromClass))
                result = obj;
            else
            {
                Class[] cs = fromClass.getInterfaces();
                Exception e1 = new Exception();
                try
                {
                    throw e1;
                }
                catch(Exception ee)
                {
                    ee.printStackTrace();
                }
                System.exit(2);
            }
        }
        catch(Exception e)
        {
            result = null;
        }
        
        if (result == null)
            throw new ClassCastException("Can't narrow from " + fromClass + " to " + newClass);
        
        return result;
    }
    
    /**
     * @see javax.rmi.CORBA.PortableRemoteObjectDelegate#connect(java.rmi.Remote, java.rmi.Remote)
     */
    public void connect( java.rmi.Remote target, java.rmi.Remote source ) throws java.rmi.RemoteException
    {
        throw new Error("Not implemented for PortableRemoteObjectDelegateImpl");
    }
    
    /**
     * Return the Tie object for an RMI object.
     * @param obj   The RMI object.
     * @return  The Tie object
     * @throws java.rmi.server.ExportException
     */
    static Tie toTie( java.rmi.Remote obj ) throws java.rmi.server.ExportException
    {
        for (Class clz = obj.getClass(); clz != null; clz = clz.getSuperclass()) {
            try
            {
                String clzName = clz.getName();
                String[] clzParts = clzName.split("\\.");
                clzParts[clzParts.length - 1] = "_" + clzParts[clzParts.length - 1] + "_Tie";
                StringBuffer tieClzName = new StringBuffer("org.omg.stub");
                for (int i = 0; i < clzParts.length; i++) tieClzName.append("." + clzParts[i]);
                Class tieClass = Util.loadClass(tieClzName.toString(), Util.getCodebase( clz ), clz.getClassLoader() );
                return ( javax.rmi.CORBA.Tie ) tieClass.newInstance();
            }
            catch ( ClassNotFoundException ex )
            {
                //throw new java.rmi.server.ExportException("ClassNotFoundException: " + e, e );
            }
            catch (InstantiationException e) 
            {
                throw new java.rmi.server.ExportException("InstantiationException: " + e, e );
            }
            catch (IllegalAccessException e) 
            {
                throw new java.rmi.server.ExportException("IllegalAccessException: " + e, e );
            }
        }
        throw new java.rmi.server.ExportException("Tie class not found ");
    }
}
