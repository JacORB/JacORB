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
    protected ApplicationContext applicationContext_;

    public FilterFactoryImpl(ApplicationContext applicationContext) throws InvalidName {
	super();
	orb_ = applicationContext.getOrb();
	poa_ = applicationContext.getPoa();
	applicationContext_ = applicationContext;

	dynAnyFactory_ = DynAnyFactoryHelper.narrow(orb_.resolve_initial_references("DynAnyFactory"));
	resultExtractor_ = new ResultExtractor(dynAnyFactory_);
	dynamicEvaluator_ = new DynamicEvaluator(orb_, dynAnyFactory_);
    }
    
    public FilterFactoryImpl(ApplicationContext appContext, DynAnyFactory dynAnyFactory) {
	super();

	orb_ = appContext.getOrb();
	poa_ = appContext.getPoa();
	applicationContext_ = appContext;
	dynAnyFactory_ = dynAnyFactory;
	resultExtractor_ = new ResultExtractor(dynAnyFactory_);
	dynamicEvaluator_ = new DynamicEvaluator(orb_, dynAnyFactory_);
    }

    public Filter create_filter(String grammar) throws InvalidGrammar {
	if (CONSTRAINT_GRAMMAR.equals(grammar)) {
	    Filter _filter;
	    FilterImpl _filterServant = new FilterImpl(CONSTRAINT_GRAMMAR, 
						       applicationContext_, 
						       dynAnyFactory_, 
						       resultExtractor_, 
						       dynamicEvaluator_);
	    _filterServant.init();
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
