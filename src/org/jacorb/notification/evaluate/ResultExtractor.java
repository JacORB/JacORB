package org.jacorb.notification.evaluate;

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
	throws TypeMismatch, InconsistentTypeCode, InvalidValue
    {
	EvaluationResult _ret = new EvaluationResult();

	DynAny _dynAny = dynAnyFactory_.create_dyn_any(any);
	switch (_dynAny.type().kind().value()) {
	case TCKind._tk_boolean:
	    debug("bool");
	    _ret.setBool(_dynAny.get_boolean());
	    break;
	case TCKind._tk_string:
	    debug("string");
	    _ret.setString(_dynAny.get_string());
	    break;
	case TCKind._tk_long:
	    debug("long");
	    _ret.setInt(_dynAny.get_long());
	    break;
	case TCKind._tk_short:
	    _ret.setInt(_dynAny.get_short());
	    break;
	default:
	    _ret = null;
	    break;
	}
	return _ret;
    }

    void debug(String msg) {
	if (DEBUG) {
	    System.err.println("[ResultExtractor] " + msg);
	}
    }

}// ResultExtractor
