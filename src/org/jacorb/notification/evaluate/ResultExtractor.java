package org.jacorb.notification.evaluate;

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

import org.omg.DynamicAny.DynAnyFactory;
import org.omg.DynamicAny.DynAny;
import org.omg.CORBA.TCKind;
import org.jacorb.notification.node.EvaluationResult;
import org.omg.CORBA.Any;
import org.omg.DynamicAny.DynAnyPackage.TypeMismatch;
import org.omg.DynamicAny.DynAnyFactoryPackage.InconsistentTypeCode;
import org.omg.DynamicAny.DynAnyPackage.InvalidValue;

/**
 * ResultExtractor.java
 *
 *
 * Created: Tue Sep 17 15:19:18 2002
 *
 * @author <a href="mailto:a.bendt@berlin.de">Alphonse Bendt</a>
 * @version
 */

public class ResultExtractor {
    static boolean DEBUG = false;

    DynAnyFactory dynAnyFactory_;

    public ResultExtractor(DynAnyFactory factory) {
	dynAnyFactory_ = factory;
    }

    public EvaluationResult extractFromAny(Any any)
	throws TypeMismatch, InconsistentTypeCode, InvalidValue {
	debug("extractFromAny(Any)");

	EvaluationResult _ret = new EvaluationResult();

	//	DynAny _dynAny = dynAnyFactory_.create_dyn_any(any);

	switch(any.type().kind().value()) {
	    //	switch (_dynAny.type().kind().value()) {
	case TCKind._tk_boolean:
	    debug("bool");
	    _ret.setBool(any.extract_boolean());
	    break;
	case TCKind._tk_string:
	    debug("string");
	    _ret.setString(any.extract_string());
	    break;
	case TCKind._tk_long:
	    debug("long");
	    _ret.setInt(any.extract_long());
	    break;
	case TCKind._tk_short:
	    _ret.setInt(any.extract_short());
	    break;
	case TCKind._tk_any:
	    debug("again");
	    return extractFromAny(any.extract_any());
	default:
	    _ret.addAny(any);
	    break;
	}
	return _ret;
    }

    static void debug(String msg) {
	if (DEBUG) {
	    System.err.println("[ResultExtractor] " + msg);
	}
    }

}// ResultExtractor
