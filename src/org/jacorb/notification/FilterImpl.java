package org.jacorb.notification;

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

import EDU.oswego.cs.dl.util.concurrent.ReadWriteLock;
import EDU.oswego.cs.dl.util.concurrent.WriterPreferenceReadWriteLock;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import org.jacorb.notification.evaluate.ConstraintEvaluator;
import org.jacorb.notification.evaluate.DynamicEvaluator;
import org.jacorb.notification.evaluate.EvaluationException;
import org.jacorb.notification.evaluate.ResultExtractor;
import org.jacorb.notification.node.DynamicTypeException;
import org.jacorb.notification.node.EvaluationResult;
import org.omg.CORBA.Any;
import org.omg.CORBA.ORB;
import org.omg.CORBA.ORBPackage.InvalidName;
import org.omg.CORBA.UNKNOWN;
import org.omg.CosNotification.EventType;
import org.omg.CosNotification.Property;
import org.omg.CosNotification.StructuredEvent;
import org.omg.CosNotifyComm.NotifySubscribe;
import org.omg.CosNotifyFilter.ConstraintExp;
import org.omg.CosNotifyFilter.ConstraintInfo;
import org.omg.CosNotifyFilter.ConstraintNotFound;
import org.omg.CosNotifyFilter.FilterPOA;
import org.omg.CosNotifyFilter.InvalidConstraint;
import org.omg.CosNotifyFilter.UnsupportedFilterableData;
import org.omg.DynamicAny.DynAnyFactory;
import org.omg.DynamicAny.DynAnyFactoryPackage.InconsistentTypeCode;
import org.omg.DynamicAny.DynAnyPackage.InvalidValue;
import org.omg.DynamicAny.DynAnyPackage.TypeMismatch;
import org.jacorb.notification.util.WildcardMap;
import java.util.Arrays;
import java.util.List;
import java.util.LinkedList;
import org.jacorb.util.Debug;
import org.jacorb.notification.util.ObjectPoolBase;
import org.jacorb.notification.framework.Poolable;

/**
 * FilterImpl.java
 *
 * The Filter interface defines the behaviors supported by objects
 * which encapsulate constraints used by the proxy objects associated
 * with an event channel in order to determine which events they
 * receive will be forwarded, and which will be discarded. Each object
 * supporting the Filter interface can encapsulate a sequence of any
 * number of constraints. Each event received by a proxy object which
 * has one or more objects supporting the Filter interface associated
 * with it must satisfy at least one of the constraints associated
 * with one of its associated Filter objects in order to be forwarded
 * (either to another proxy object or to the consumer, depending on
 * the type of proxy the filter is associated with), otherwise it will
 * be discarded. <br> Each constraint encapsulated by a filter object is a
 * structure comprised of two main components. The first component is
 * a sequence of data structures, each of which indicates an event
 * type comprised of a domain and a type name. The second component is
 * a boolean expression over the properties of an event, expressed in
 * some constraint grammar (more on this below). For a given
 * constraint, the sequence of event type structures in the first
 * component nominates a set of event types to which the constraint
 * expression in the second component applies. Each element of the
 * sequence can contain strings which will be matched for equality
 * against the domain_name and type_name fields of each event being
 * evaluated by the filter object, or it could contain strings with
 * wildcard symbols (*), indicating a pattern match should be
 * performed against the type contained in each event, rather than a
 * comparison for equality when determining if the boolean expression
 * should be applied to the event, or the event should simply be
 * discarded without even attempting to apply the boolean
 * expression. Note that an empty sequence included as the first
 * component of a constraint implies that the associated expression
 * applies to all types of events, as does a sequence comprised of a
 * single element whose domain and type name are both set to either
 * the empty string or else the wildcard symbol alone contained in
 * quotes. <br>The constraint expressions associated with a particular
 * object supporting the Filter interface are expressed as strings
 * which obey the syntax of a particular constraint grammar (i.e., a
 * BNF). Every conformant implementation of this service must support
 * constraint expressions expressed in the default constraint grammar
 * described in Section 2.4, "The Default Filter Constraint Language,"
 * on page 2-23. In addition, implementations may support other
 * constraint grammars, and/or users of this service may implement
 * their own filter objects which allow constraints to be expressed in
 * terms of an alternative constraint grammar. As long as such
 * user-defined filter objects support the Filter interface, they can
 * be attached to Proxy or Admin objects in the same fashion as the
 * default Filter objects supported by the implementation of the
 * service are, and the channel should be able to use them to filter
 * events in the same fashion. <br> The Filter interface supports the
 * operations required to manage the constraints associated with an
 * object instance which supports the interface, along with a readonly
 * attribute which identifies the particular constraint grammar in
 * which the constraints encapsulated by this object have meaning. In
 * addition, the Filter interface supports three variants of the match
 * operation which can be invoked by an associated proxy object upon
 * receipt of an event (the specific variant selected depends upon
 * whether the event is received in the form of an Any, a Structured
 * Event, or a Typed Event), to determine if the event should be
 * forwarded or discarded, based on whether or not the event satisfies
 * at least one criteria encapsulated by the filter object. The Filter
 * interface also supports operations which enable a client to
 * associate with the target filter object any number of
 * "callbacks" which are notified each time there is a change to
 * the list of event types which the constraints encapsulated by the
 * filter object could potentially cause proxies to which the filter
 * is attached to receive. Operations are also defined to support
 * administration of this callback list by unique identifier. <br>
 *
 * Created: Sat Oct 12 17:30:55 2002
 *
 * @author <a href="mailto:bendt@inf.fu-berlin.de">Alphonse Bendt</a>
 * @version $Id$
 */

public class FilterImpl extends FilterPOA {

    final static RuntimeException NOT_SUPPORTED = new UnsupportedOperationException();

    /**
     * contains a number of callbacks, which are notified each time there is a
     * change to the list of 
     */
    protected Map callbacks_;

    /**
     * contains a number of constraints
     */
    protected Map constraints_;

    protected WildcardMap wildcardMap_;
    
    protected ReadWriteLock constraintsLock_;

    protected Map eventTypeMap_;

    private String constraintGrammar_;

    protected int constraintIdPool_ = 0;

    protected ORB orb_;

    protected ApplicationContext applicationContext_;

    protected ResultExtractor resultExtractor_;

    protected DynamicEvaluator dynamicEvaluator_;

    protected DynAnyFactory dynAnyFactory_;

    protected NotificationEventFactory notificationEventFactory_;


    FilterImpl(String constraintGrammar, 
	       ApplicationContext applicationContext, 
	       DynAnyFactory dynAnyFactory, 
	       ResultExtractor resultExtractor,
	       DynamicEvaluator dynamicEvaluator) {

	super();
	
	applicationContext_ = applicationContext;
	orb_ = applicationContext.getOrb();
	constraintGrammar_ = constraintGrammar;
	
	notificationEventFactory_= applicationContext.getNotificationEventFactory();

	dynAnyFactory_ = dynAnyFactory;
	resultExtractor_ = resultExtractor;
	dynamicEvaluator_ = dynamicEvaluator;

	constraints_ = new Hashtable();
	constraintsLock_ = new WriterPreferenceReadWriteLock();
	wildcardMap_ = new WildcardMap();
	eventTypeMap_ = new Hashtable();
    }
  
    public void init() {
    }

    protected int getConstraintId() {
	return (++constraintIdPool_);
    }

    protected void releaseConstraintId(int id) {
    }

    /**
     * The constraint_grammar attribute is a readonly attribute which
     * identifies the particular grammar within which the constraint
     * expressions encapsulated by the target filter object have
     * meaning.
     */
    public String constraint_grammar() {
	return constraintGrammar_;
    }

    /**
     * The <code>add_constraints</code> operation is invoked by a client in order
     * to associate one or more new constraints with the target filter
     * object. The operation accepts as input a sequence of constraint
     * data structures, each element of which consists of a sequence
     * of event type structures (described in Section 3.2.1, "The
     * Filter Interface," on page 3-14) and a constraint expressed
     * within the constraint grammar supported by the target
     * object. Upon processing each constraint, the target object
     * associates a numeric identifier with the constraint that is
     * unique among all constraints it encapsulates. If any of the
     * constraints in the input sequence is not a valid expression
     * within the supported constraint grammar, the InvalidConstraint
     * exception is raised. This exception contains as data the
     * specific constraint expression that was determined to be
     * invalid. Upon successful processing of all input constraint
     * expressions, the <code>add_constraints</code> operation returns a sequence
     * in which each element will be a structure including one of the
     * input constraint expressions, along with the unique identifier
     * assigned to it by the target filter object. <br>
     * Note that the semantics of the <code>add_constraints</code> operation are
     * such that its sideeffects are performed atomically upon the
     * target filter object. Once <code>add_constraints</code> is invoked by a
     * client, the target filter object is temporarily disabled from
     * usage by any proxy object it may be associated with. The
     * operation is then carried out, either successfully adding all
     * of the input constraints to the target object or none of them
     * (in the case one of the input expressions was invalid). Upon
     * completion of the operation, the target filter object is
     * effectively re-enabled and can once again be used by associated
     * filter objects in order to make event forwarding decisions. 
     */
    public ConstraintInfo[] add_constraints(ConstraintExp[] constraintExp) 
	throws InvalidConstraint {

	ConstraintEvaluator[] _arrayConstraintEvaluator = new ConstraintEvaluator[constraintExp.length];

	for (int x=0; x<constraintExp.length; x++) {
	    _arrayConstraintEvaluator[x] = new ConstraintEvaluator(orb_, constraintExp[x]);
	}

	ConstraintInfo[] _arrayConstraintInfo = new ConstraintInfo[constraintExp.length];

	try {
	    // access writeonly lock
	    constraintsLock_.writeLock().acquire();
	    try {
		// protected section
		
		for (int x=0; x<constraintExp.length; x++) {
		    // we do not create the constraint id's in the upper loop
		    // therefor they are still available if one constraint is invalid			
		    int _constraintId = getConstraintId();
		    
		    _arrayConstraintInfo[x] = new ConstraintInfo(constraintExp[x], _constraintId);
			

		    ConstraintEntry _entry = new ConstraintEntry(_constraintId,
								 _arrayConstraintEvaluator[x],
								 _arrayConstraintInfo[x]);
			
		    int _eventTypeCount = _entry.getEventTypeCount();
		    for (int y=0; y<_eventTypeCount; ++y) {
			EventTypeIdentifier _eventTypeIdentifier = _entry.getEventTypeIdentifier(y);
			List _listOfConstraintEvaluator = (List)wildcardMap_.get(_eventTypeIdentifier, false);
			if (_listOfConstraintEvaluator == null) {
			    _listOfConstraintEvaluator = new LinkedList();
			    wildcardMap_.put(_eventTypeIdentifier, _listOfConstraintEvaluator);
			}
			_listOfConstraintEvaluator.add(_entry);
		    }			
		    constraints_.put(new Integer(_constraintId), _entry);
		}
		return _arrayConstraintInfo;
		// end of protected section
	    } finally {
		// give up the lock
		constraintsLock_.writeLock().release();
	    }
	} catch (InterruptedException ie) {
	    // propagate without throwing
	    Thread.currentThread().interrupt();
	    return null;
	}
    }
    
    public void modify_constraints(int[] delete_id, ConstraintInfo[] constraint_info) 
	throws ConstraintNotFound, 
	       InvalidConstraint {
	
	try {
	    // write lock
	    constraintsLock_.writeLock().acquire();
	    try {
		// first check if all id's that should be deleted exist
		Integer[] _delete_keys = new Integer[delete_id.length];
		for (int x=0; x<delete_id.length; ++x) {
		    _delete_keys[x] = new Integer(delete_id[x]);
		    if (!constraints_.containsKey(_delete_keys[x])) {
			throw new ConstraintNotFound(delete_id[x]);
		    }
		}
		
		// create update constraints and check if the id exists
		Integer[] _constraint_keys = new Integer[constraint_info.length];
		ConstraintEvaluator[] _arrayConstraintEvaluator = new ConstraintEvaluator[constraint_info.length];
		
		for (int x=0; x<constraint_info.length; ++x) {
		    _constraint_keys[x] = new Integer(constraint_info[x].constraint_id);
		    if (constraints_.containsKey(_constraint_keys[x])) {
			_arrayConstraintEvaluator[x] = 
			    new ConstraintEvaluator(orb_, constraint_info[x].constraint_expression);
		    } else {
			throw new ConstraintNotFound(constraint_info[x].constraint_id);
		    }
		    int _length = constraint_info[x].constraint_expression.event_types.length;
		}
		
		// delete some constraints
		for (int x=0; x<delete_id.length; ++x) {
		    ConstraintEntry _deletedEntry = (ConstraintEntry)constraints_.remove(_delete_keys[x]);
		    int _eventTypeCount = _deletedEntry.getEventTypeCount();
		    for (int y=0; y<_eventTypeCount; ++y) {
			EventTypeIdentifier _eventTypeIdentifier = _deletedEntry.getEventTypeIdentifier(y);
			List _listOfConstraintEvaluator = (List)wildcardMap_.get(_eventTypeIdentifier, false);
			Iterator _i = _listOfConstraintEvaluator.iterator();
			while(_i.hasNext()) {
			    ConstraintEntry _c = (ConstraintEntry)_i.next();
			    if (_c.getConstraintId() == _delete_keys[x].intValue()) {
				_i.remove();
				break;
			    }
			}
		    }
		}
		
		// update some constraints
		for (int x=0; x<constraint_info.length; x++) {
		    ConstraintEntry _entry = new ConstraintEntry(_constraint_keys[x].intValue(),
								 _arrayConstraintEvaluator[x], 
								 constraint_info[x]);
		    constraints_.put(_constraint_keys[x], _entry);
		    int _eventTypeCount = _entry.getEventTypeCount();
		    for (int y=0; y<_eventTypeCount; ++y) {
			EventTypeIdentifier _eventTypeIdentifier = _entry.getEventTypeIdentifier(y);
			List _listOfConstraintEvaluator = (List)wildcardMap_.get(_eventTypeIdentifier, false);
// 			if (_listOfConstraintEvaluator == null) {
// 			    _listOfConstraintEvaluator = new LinkedList();
// 			    wildcardMap_.put(_eventTypeIdentifier, _listOfConstraintEvaluator);
// 			}
			_listOfConstraintEvaluator.add(_entry);
		    }
		}
		
		return;
	    } finally {
		constraintsLock_.writeLock().release();
	    }
	} catch (InterruptedException ie) {
	    Thread.currentThread().interrupt();
	}
    }
	
    public ConstraintInfo[] get_constraints(int[] ids) throws ConstraintNotFound {
	ConstraintInfo[] _constraintInfo = new ConstraintInfo[ids.length];

	try {
	    constraintsLock_.readLock().acquire();
	    try {
		for (int x=0; x<ids.length; ++x) {
		    Integer _key = new Integer(ids[x]);
		    if (constraints_.containsKey(_key)) {
			_constraintInfo[x] = 
			    ((ConstraintEntry)constraints_.get(new Integer(ids[x]))).constraintInfo_;
		    } else {
			throw new ConstraintNotFound(ids[x]);
		    }
		}
		return _constraintInfo;
	    } finally {
		constraintsLock_.readLock().release();
	    }
	} catch (InterruptedException ie) {
	    Thread.currentThread().interrupt();
	    return null;
	}
    }

    public ConstraintInfo[] get_all_constraints() {
	try {
	    constraintsLock_.readLock().acquire();
	    try {
		ConstraintInfo[] _constraintInfo = 
		    new ConstraintInfo[constraints_.size()];
		
		Iterator _i = constraints_.values().iterator();
		
		int x = -1;
		while (_i.hasNext()) {
		    _constraintInfo[++x] = 
			((ConstraintEntry)_i.next()).constraintInfo_;
		}
		
		return _constraintInfo;
	    } finally {
		constraintsLock_.readLock().release();
	    }
	} catch (InterruptedException ie) {
	    Thread.currentThread().interrupt();
	    return null;
	}
    }

    public void remove_all_constraints() {
	try {
	    constraintsLock_.writeLock().acquire();
	    try {
		constraints_.clear();
		wildcardMap_.clear();
		return;
	    } finally {
		constraintsLock_.writeLock().release();
	    }
	} catch (InterruptedException ie) {
	    Thread.currentThread().interrupt();
	}
    }

    public void destroy() {
	
    }

    /**
     * call only and use Iterator inside a read access mutex section
     */ 
    Iterator getConstraintsForEvent(NotificationEvent event) {
	String _key = event.getConstraintKey();

	return getIterator(_key);
    }
    
    Iterator getIterator(Object key) {
	Object[] _entries = (Object[])wildcardMap_.get(key);
	return new ConstraintIterator(_entries);
    }

    private class ConstraintIterator implements Iterator {
	Object[] arrayOfLists_;
	Iterator current_;
	int listCursor = 0;
	int arrayCursor_ = 0;

	ConstraintIterator(Object[] arrayOfLists) {
	    arrayOfLists_ = arrayOfLists;
	    if (Debug.canOutput(Debug.DEBUG1)) {
		for (int x=0; x<arrayOfLists_.length; ++x) {
		    debug(x + ": " + arrayOfLists_[x]);
		}
	    }
	    current_ = ((List)arrayOfLists_[arrayCursor_]).iterator();
	}

	public boolean hasNext() {
	    boolean _r = current_.hasNext();
	    return _r;
	}

	public Object next() {
	    Object _ret = current_.next();
	    if (!current_.hasNext() && arrayCursor_ < arrayOfLists_.length-1) {
		current_ = ((List)arrayOfLists_[++arrayCursor_]).iterator();
	    }
	    return _ret;
	}

	public void remove() {
	    throw NOT_SUPPORTED;
	}
    }

    // readers
    boolean match(NotificationEvent event) throws UnsupportedFilterableData {
	try {
	    constraintsLock_.readLock().acquire();
	    try {
		if (!constraints_.isEmpty()) {
		    Iterator _entries = getConstraintsForEvent(event);
   
		    while(_entries.hasNext()) {
			ConstraintEntry _entry = (ConstraintEntry)_entries.next();
			try {
			    EvaluationResult _res = 
				_entry.constraintEvaluator_.evaluate(event);
			    if (_res.getBool()) {
				return true;
			    }
			} catch (DynamicTypeException dt) {
			    dt.printStackTrace();
			} catch (InvalidValue iv) {
			    iv.printStackTrace();
			} catch (InconsistentTypeCode itc) {
			    itc.printStackTrace();
			} catch (TypeMismatch tm) {
			    tm.printStackTrace();
			} catch (EvaluationException ee) {
			    ee.printStackTrace();
			} catch (InvalidName in) {
			    in.printStackTrace();
			}
		    }
		} else {
		    info("Filter has no Expressions");
		}
		return false;
	    } finally {
		constraintsLock_.readLock().release();
	    }
	} catch (InterruptedException ie) {
	    Thread.currentThread().interrupt();
	    return false;
	}
    }

    public boolean match(Any anyEvent) throws UnsupportedFilterableData {
	EvaluationContext _evalutionContext = null;
	NotificationEvent _event = null;

	try {
	    _evalutionContext = applicationContext_.newEvaluationContext();
	    _event = notificationEventFactory_.newEvent(anyEvent, _evalutionContext);
	    return match(_event);
	} finally {
	    _event.release();
	    _evalutionContext.release();
	}
    }

    public boolean match_structured(StructuredEvent structuredEvent) 
	throws UnsupportedFilterableData{

	EvaluationContext _evalutionContext = null;
	NotificationEvent _event = null;

	try {
	    _evalutionContext = applicationContext_.newEvaluationContext();
	    _event = notificationEventFactory_.newEvent(structuredEvent, _evalutionContext);

	    return match(_event);
	} finally {
	    _event.release();
	    _evalutionContext.release();
	}
    }

    public boolean match_typed(Property[] properties) 
	throws UnsupportedFilterableData {

	return false;
    }

    public int attach_callback(NotifySubscribe notifySubscribe) {
	return 0;
    }

    public void detach_callback(int id) {
    }

    public int[] get_callbacks() {
	return null;
    }

    private void debug(Object msg) {
	System.out.println(msg.toString());
	
	//Debug.output(Debug.DEBUG1, msg.toString());
    }

    private void info(Object msg) {
	Debug.output(Debug.INFORMATION, msg.toString());
    }
}// FilterImpl

class ConstraintEntry {

    ConstraintEvaluator constraintEvaluator_;
    ConstraintInfo constraintInfo_;
    int constraintId_;

    EventTypeIdentifier getEventTypeIdentifier(int index) {
	return new EventTypeWrapper(constraintInfo_.constraint_expression.event_types[index]);
    }

    int getEventTypeCount() {
	return constraintInfo_.constraint_expression.event_types.length;
    }

    int getConstraintId() {
	return constraintId_;
    }

    ConstraintEntry(int constraintId,
		    ConstraintEvaluator constraintEvaluator,
		    ConstraintInfo constraintInfo) {

	constraintId_ = constraintId;
	constraintEvaluator_ = constraintEvaluator;
	constraintInfo_ = constraintInfo;
    }

    class EventTypeWrapper implements EventTypeIdentifier {
	EventType et_;
	
	EventTypeWrapper(EventType et) {
	    et_ =et;
	}

	public String toString() {
	    return FilterUtils.calcConstraintKey(et_.domain_name, et_.type_name);
	}
    }
}
