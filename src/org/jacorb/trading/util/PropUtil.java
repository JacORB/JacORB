
// Copyright (C) 1998-1999
// Object Oriented Concepts, Inc.

// **********************************************************************
//
// Copyright (c) 1997
// Mark Spruiell (mark@intellisoft.com)
//
// See the COPYING file for more information
//
// **********************************************************************

package org.jacorb.trading.util;

import org.omg.CORBA.*;
import org.omg.CosTrading.*;
import org.omg.CosTradingDynamic.*;

public class PropUtil
{
    private PropUtil()
    {
    }


    public static Any getPropertyValue(Property prop)
    {
	Any result = null;

	if (isDynamicProperty(prop.value.type())) {

	    try {
		DynamicProp dp = DynamicPropHelper.extract(prop.value);

		// evaluate dynamic property
		result = dp.eval_if.evalDP(prop.name, dp.returned_type, dp.extra_info);
	    }
	    catch (DPEvalFailure e) {
		// ignore
	    }
	    catch (org.omg.CORBA.SystemException e) {
		// ignore
	    }
	    catch (java.lang.NullPointerException e) {
		// ignore. Is this really correct?
	    }
	}
	else
	    result = prop.value;

	return result;
    }


    public static boolean hasDynamicProperties(Property[] props)
    {
	boolean result = false;

	for (int i = 0; i < props.length && ! result; i++)
	    result = isDynamicProperty(props[i].value.type());

	return result;
    }


    public static boolean isDynamicProperty(TypeCode tc)
    {
	boolean result = false;
	result = tc.equal(DynamicPropHelper.type());

	return result;
    }
}










