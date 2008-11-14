package org.jacorb.ir;

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

import org.apache.avalon.framework.logger.Logger;
import org.omg.CORBA.TCKind;
import org.omg.CORBA.TypeCode;
import org.omg.PortableServer.POA;

/**
 * Base class and factory for anonymous IDLType objects
 * 
 * Direct instances of this class are only used as place holders
 * in recursive types
 */

public class IDLType
    extends IRObject
    implements org.omg.CORBA.IDLTypeOperations
{
    protected org.omg.CORBA.TypeCode type;

    public org.omg.CORBA.TypeCode type()
    {
        return type;
    }

    public void define()
    {}

    public void destroy()
    {}

    protected IDLType()
    {
    }

    private IDLType( TypeCode tc, 
                     org.omg.CORBA.Repository ir )
    {
        type = tc;
    }

    /**
     * Factory method for IDLType objects
     */
    public static org.omg.CORBA.IDLType create( TypeCode tc, 
                                                org.omg.CORBA.Repository ir,
                                                Logger logger,
                                                POA poa )
    {
        return create( tc, ir, false, logger, poa );
    }


    public static org.omg.CORBA.IDLType create( TypeCode tc, 
                                                org.omg.CORBA.Repository ir,
                                                boolean define,
                                                Logger logger,
                                                POA poa)
    {
        if (logger.isDebugEnabled())
        {
            logger.debug("IDLType create for tc kind " + 
                         tc.kind().value());
        }

        if( tc == null ) // PIDLs
            return null;

        if( org.jacorb.orb.TypeCode.isRecursive(tc) )
        {
            logger.debug("Placeholder for recursive sequence");

            try
            { 
                return org.omg.CORBA.IDLTypeHelper.narrow( 
                    poa.servant_to_reference(
                        new org.omg.CORBA.IDLTypePOATie( new IDLType( tc,ir ))));
            }
            catch( Exception e )
            { 
                logger.error("Caught Exception", e);
            }
        }

        int kind = tc.kind().value();

        switch (kind)
        {
            case TCKind._tk_null: 
            case TCKind._tk_void: 
            case TCKind._tk_short: 
            case TCKind._tk_long: 
            case TCKind._tk_ushort:
            case TCKind._tk_ulong: 
            case TCKind._tk_float:
            case TCKind._tk_double:
            case TCKind._tk_boolean:
            case TCKind._tk_char: 
            case TCKind._tk_longlong: 
            case TCKind._tk_ulonglong: 
            case TCKind._tk_longdouble: 
            case TCKind._tk_wchar: 
            case TCKind._tk_octet:
            case TCKind._tk_any: 
            case TCKind._tk_TypeCode: 
            case TCKind._tk_Principal: 
                try
                { 
                    PrimitiveDef pd = new PrimitiveDef( tc );
                    return org.omg.CORBA.PrimitiveDefHelper.narrow( 
                        poa.servant_to_reference(
                            new org.omg.CORBA.PrimitiveDefPOATie( pd )));
                }
                catch( Exception e )
                { 
                    logger.error("Caught Exception", e);
                    return null;
                }
            case TCKind._tk_alias:
            case TCKind._tk_struct: 
            case TCKind._tk_except: 
            case TCKind._tk_union:
            case TCKind._tk_enum: 
                try
                {  
                    return org.omg.CORBA.IDLTypeHelper.narrow( ir.lookup_id( tc.id() ));
                }
                catch( Exception e )
                { 
                    // does not happen here
                    logger.error("Caught Exception", e);
                    return null;
                }
            case TCKind._tk_string: 
                try
                {
                    if( tc.length() == 0)
                    {
                        return org.omg.CORBA.PrimitiveDefHelper.narrow( 
                            poa.servant_to_reference( 
                                new org.omg.CORBA.PrimitiveDefPOATie( new PrimitiveDef( tc ))));
                    }
                    else
                    {
                        return org.omg.CORBA.StringDefHelper.narrow( 
                            poa.servant_to_reference(
                                new org.omg.CORBA.StringDefPOATie( new StringDef( tc ))));
                    }
                }
                catch( Exception e )
                {
                    logger.error("Caught Exception", e);
                    return null;
                }
            case TCKind._tk_wstring: 
                try
                {
                    if( tc.length() == 0)
                    {
                        return org.omg.CORBA.PrimitiveDefHelper.narrow( 
                            poa.servant_to_reference( 
                                new org.omg.CORBA.PrimitiveDefPOATie( new PrimitiveDef( tc ))));
                    }
                    else
                    {
                        return org.omg.CORBA.WstringDefHelper.narrow( 
                            poa.servant_to_reference(
                                new org.omg.CORBA.WstringDefPOATie( new WstringDef( tc ))));
                    }
                }
                catch( Exception e )
                {
                    logger.error("Caught Exception", e);
                    return null;
                }
            case TCKind._tk_fixed: 
                try
                {
                    return org.omg.CORBA.FixedDefHelper.narrow( 
                        poa.servant_to_reference(
                            new org.omg.CORBA.FixedDefPOATie( new FixedDef( tc ))));
                }
                catch( Exception e )
                {
                    logger.error("Caught Exception", e);
                    return null;
                }
            case TCKind._tk_objref: 
            {
                try
                {
                    if (logger.isDebugEnabled())
                    {
                        logger.debug("IDLType create for " + tc.id());
                    }

                    if( tc.id().equals("IDL:omg.org/CORBA/Object:1.0"))
                    {
                        return org.omg.CORBA.PrimitiveDefHelper.narrow( 
                            poa.servant_to_reference(
                                new org.omg.CORBA.PrimitiveDefPOATie( new PrimitiveDef( tc ))));
                    }
                    else
                    {
                        return org.omg.CORBA.IDLTypeHelper.narrow( ir.lookup_id( tc.id() ) );
                    }
                }
                catch( Exception e )
                { 
                    logger.error("Caught Exception", e);
                    return null;
                }
            }
            case TCKind._tk_sequence: 
                try
                {
                    SequenceDef sd = new SequenceDef( tc, ir, logger, poa );
                    if( define )
                        sd.define();
                    return org.omg.CORBA.SequenceDefHelper.narrow( 
                        poa.servant_to_reference(
                            new org.omg.CORBA.SequenceDefPOATie( sd )));
                }
                catch( Exception e )
                { 
                    logger.error("Caught Exception", e);
                    return null;
                }
            case TCKind._tk_array:
                try
                {
                    ArrayDef ad = new ArrayDef( tc, ir, logger, poa );
                    if( define )
                        ad.define();
                    return org.omg.CORBA.ArrayDefHelper.narrow( 
                        poa.servant_to_reference(
                            new org.omg.CORBA.ArrayDefPOATie( ad )));
                }
                catch( Exception e )
                { 
                    logger.error("Caught Exception", e);
                    return null;
                }
            default: 
                logger.warn("IDL type returns null for tc kind " + kind );
                return null;
        }
    }
}



