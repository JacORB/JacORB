// Copyright (C) 1998-2001
// Object Oriented Concepts, Inc.

// **********************************************************************
//
// Copyright (c) 1997
// Mark Spruiell (mark@intellisoft.com)
//
// See the COPYING file for more information
//
// **********************************************************************

package org.jacorb.trading.client.dynprop;

import java.util.*;
import org.omg.CORBA.*;
import org.omg.CosTrading.*;
import org.omg.CosTradingDynamic.*;

/**
 * A simple implementation of the CosTradingDynamic::DynamicPropEval
 * interface used for testing purposes.
 */

public class DynamicPropEvalImpl 
    extends DynamicPropEvalPOA
{
    private Random m_rand = new Random();

    /**
     * Overridden from Visibroker's _DynamicPropEvalImplBase; we do this
     * instead of the unportable super(objectName) we'd have to put in
     * the constructor; the presence of this method should not affect
     * use with other ORBs
     */
    public String _object_name()
    {
	return "DynPropDemo";
    }


    /**
     * Inherited from DynamicPropEval
     *
     * Just return a random value of the requested type
     */
    public Any evalDP(String name, TypeCode returned_type, Any extra_info)
	throws DPEvalFailure
    {
	Any result = ORB.init().create_any();

	System.out.println("evalDP(" + name + ")");

	TCKind kind = returned_type.kind();
	switch (kind.value()) {
	case TCKind._tk_short:
	    result.insert_short((short)m_rand.nextInt());
	    break;
	case TCKind._tk_ushort:
	    result.insert_ushort((short)m_rand.nextInt());
	    break;
	case TCKind._tk_long:
	    result.insert_long(m_rand.nextInt());
	    break;
	case TCKind._tk_ulong:
	    result.insert_ulong(m_rand.nextInt());
	    break;
	case TCKind._tk_double:
	    result.insert_double(m_rand.nextDouble());
	    break;
	case TCKind._tk_float:
	    result.insert_float(m_rand.nextFloat());
	    break;
	case TCKind._tk_boolean:
	    result.insert_boolean((m_rand.nextInt() % 2 == 0));
	    break;
	case TCKind._tk_string:
	    result.insert_string("Next int = " + m_rand.nextInt());
	    break;
	default:
	    throw new DPEvalFailure(name, returned_type, extra_info);
	}

	return result;
    }
}










