package org.jacorb.notification;

import org.omg.CosNotifyFilter.FilterFactoryPOA;
import org.omg.CosNotifyFilter.Filter;
import org.omg.CosNotifyFilter.InvalidGrammar;
import org.omg.CosNotifyFilter.MappingFilter;
import org.omg.CORBA.Any;
import org.omg.PortableServer.POA;
import org.omg.CosNotifyFilter.FilterHelper;
import org.omg.PortableServer.POAPackage.ServantNotActive;
import org.omg.PortableServer.POAPackage.WrongPolicy;
import org.omg.CORBA.ORB;
import org.omg.DynamicAny.DynAnyFactory;
import org.jacorb.notification.evaluate.ResultExtractor;
import org.jacorb.notification.evaluate.DynamicEvaluator;
import org.omg.DynamicAny.DynAnyFactoryHelper;
import org.omg.CORBA.ORBPackage.InvalidName;

/*
 *        JacORB - a free Java ORB
 */

/**
 * FilterFactoryImpl.java
 *
 *
 * Created: Sat Oct 12 17:25:43 2002
 *
 * @author <a href="mailto:bendt@inf.fu-berlin.de">Alphonse Bendt</a>
 * @version $Id$
 */

public class FilterFactoryImpl extends FilterFactoryPOA {

    public static String CONSTRAINT_GRAMMAR = "EXTENDED_TCL";
    
    protected ORB orb_;
    protected POA poa_;

    protected DynAnyFactory dynAnyFactory_;
    protected ResultExtractor resultExtractor_;
    protected DynamicEvaluator dynamicEvaluator_;

    public FilterFactoryImpl(ORB orb, POA poa) throws InvalidName {
	super();
	orb_ = orb;
	poa_ = poa;

	dynAnyFactory_ = DynAnyFactoryHelper.narrow(orb.resolve_initial_references("DynAnyFactory"));
	resultExtractor_ = new ResultExtractor(dynAnyFactory_);
	dynamicEvaluator_ = new DynamicEvaluator(orb_, dynAnyFactory_);
    }
    
    public FilterFactoryImpl(ORB orb, POA poa, DynAnyFactory dynAnyFactory) {
	super();

	orb_ = orb;
	poa_ = poa;
	dynAnyFactory_ = dynAnyFactory;
	resultExtractor_ = new ResultExtractor(dynAnyFactory_);
	dynamicEvaluator_ = new DynamicEvaluator(orb_, dynAnyFactory_);
    }

    public Filter create_filter(String grammar) throws InvalidGrammar {
	if (CONSTRAINT_GRAMMAR.equals(grammar)) {
	    Filter _filter;
	    FilterImpl _filterServant = new FilterImpl(CONSTRAINT_GRAMMAR, 
						       orb_, 
						       dynAnyFactory_, 
						       resultExtractor_, 
						       dynamicEvaluator_);
	    _filter = _filterServant._this(orb_);
	    
	    return _filter;
	}
	throw new InvalidGrammar();
    }

    public MappingFilter create_mapping_filter(String grammar, 
					       Any any) throws InvalidGrammar {
	return null;
    }

}// FilterFactoryImpl
