package org.jacorb.notification.filter;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1999-2004 Gerald Brose
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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.logger.Logger;
import org.jacorb.notification.AbstractMessage;
import org.jacorb.notification.EventTypeWrapper;
import org.jacorb.notification.MessageFactory;
import org.jacorb.notification.conf.Attributes;
import org.jacorb.notification.conf.Default;
import org.jacorb.notification.interfaces.Disposable;
import org.jacorb.notification.interfaces.EvaluationContextFactory;
import org.jacorb.notification.interfaces.Message;
import org.jacorb.notification.servant.ManageableServant;
import org.jacorb.notification.util.CachingWildcardMap;
import org.jacorb.notification.util.DisposableManager;
import org.jacorb.notification.util.WildcardMap;
import org.jacorb.util.ObjectUtil;
import org.omg.CORBA.Any;
import org.omg.CORBA.ORB;
import org.omg.CosNotification.EventType;
import org.omg.CosNotification.Property;
import org.omg.CosNotification.StructuredEvent;
import org.omg.CosNotifyComm.NotifySubscribe;
import org.omg.CosNotifyFilter.ConstraintExp;
import org.omg.CosNotifyFilter.ConstraintInfo;
import org.omg.CosNotifyFilter.ConstraintNotFound;
import org.omg.CosNotifyFilter.Filter;
import org.omg.CosNotifyFilter.FilterPOA;
import org.omg.CosNotifyFilter.InvalidConstraint;
import org.omg.CosNotifyFilter.UnsupportedFilterableData;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAPackage.ObjectNotActive;
import org.omg.PortableServer.POAPackage.ServantNotActive;
import org.omg.PortableServer.POAPackage.WrongPolicy;

import EDU.oswego.cs.dl.util.concurrent.ReadWriteLock;
import EDU.oswego.cs.dl.util.concurrent.Sync;
import EDU.oswego.cs.dl.util.concurrent.SynchronizedBoolean;
import EDU.oswego.cs.dl.util.concurrent.SynchronizedInt;
import EDU.oswego.cs.dl.util.concurrent.WriterPreferenceReadWriteLock;

/**
 * The Filter interface defines the behaviors supported by objects which encapsulate constraints
 * used by the proxy objects associated with an event channel in order to determine which events
 * they receive will be forwarded, and which will be discarded. Each object supporting the Filter
 * interface can encapsulate a sequence of any number of constraints. Each event received by a proxy
 * object which has one or more objects supporting the Filter interface associated with it must
 * satisfy at least one of the constraints associated with one of its associated Filter objects in
 * order to be forwarded (either to another proxy object or to the consumer, depending on the type
 * of proxy the filter is associated with), otherwise it will be discarded. <br>
 * Each constraint encapsulated by a filter object is a structure comprised of two main components.
 * The first component is a sequence of data structures, each of which indicates an event type
 * comprised of a domain and a type name. The second component is a boolean expression over the
 * properties of an event, expressed in some constraint grammar (more on this below). For a given
 * constraint, the sequence of event type structures in the first component nominates a set of event
 * types to which the constraint expression in the second component applies. Each element of the
 * sequence can contain strings which will be matched for equality against the domain_name and
 * type_name fields of each event being evaluated by the filter object, or it could contain strings
 * with wildcard symbols (*), indicating a pattern match should be performed against the type
 * contained in each event, rather than a comparison for equality when determining if the boolean
 * expression should be applied to the event, or the event should simply be discarded without even
 * attempting to apply the boolean expression. Note that an empty sequence included as the first
 * component of a constraint implies that the associated expression applies to all types of events,
 * as does a sequence comprised of a single element whose domain and type name are both set to
 * either the empty string or else the wildcard symbol alone contained in quotes. <br>
 * The constraint expressions associated with a particular object supporting the Filter interface
 * are expressed as strings which obey the syntax of a particular constraint grammar (i.e., a BNF).
 * Every conformant implementation of this service must support constraint expressions expressed in
 * the default constraint grammar described in Section 2.4, "The Default Filter Constraint
 * Language," on page 2-23. In addition, implementations may support other constraint grammars,
 * and/or users of this service may implement their own filter objects which allow constraints to be
 * expressed in terms of an alternative constraint grammar. As long as such user-defined filter
 * objects support the Filter interface, they can be attached to Proxy or Admin objects in the same
 * fashion as the default Filter objects supported by the implementation of the service are, and the
 * channel should be able to use them to filter events in the same fashion. <br>
 * The Filter interface supports the operations required to manage the constraints associated with
 * an object instance which supports the interface, along with a readonly attribute which identifies
 * the particular constraint grammar in which the constraints encapsulated by this object have
 * meaning. In addition, the Filter interface supports three variants of the match operation which
 * can be invoked by an associated proxy object upon receipt of an event (the specific variant
 * selected depends upon whether the event is received in the form of an Any, a Structured Event, or
 * a Typed Event), to determine if the event should be forwarded or discarded, based on whether or
 * not the event satisfies at least one criteria encapsulated by the filter object. The Filter
 * interface also supports operations which enable a client to associate with the target filter
 * object any number of "callbacks" which are notified each time there is a change to the list of
 * event types which the constraints encapsulated by the filter object could potentially cause
 * proxies to which the filter is attached to receive. Operations are also defined to support
 * administration of this callback list by unique identifier. <br>
 * 
 * @author Alphonse Bendt
 * @author John Farrell
 * @version $Id$
 */

public abstract class AbstractFilter extends FilterPOA implements Disposable, ManageableServant,
        Configurable
{
    private final static RuntimeException NOT_SUPPORTED = new UnsupportedOperationException(
            "this operation is not supported");

    public static final int NO_CONSTRAINTS_MATCH = -2;

    public static final int CONSTRAINTS_EMPTY = -1;

    private static final String EMPTY_EVENT_TYPE_CONSTRAINT_KEY = AbstractMessage
            .calcConstraintKey("*", "*");

    ////////////////////////////////////////

    private final DisposableManager disposables_ = new DisposableManager();

    private final CallbackManager callbackManager_ = new CallbackManager();

    /**
     * contains the associated constraints. as access to constraints_ is controlled by
     * constraintsLock_ its safe to use unsynchronized HashMap here
     */
    protected final Map constraints_ = new HashMap();

    protected final WildcardMap wildcardMap_;

    protected final ReadWriteLock constraintsLock_;

    private final SynchronizedInt constraintIdPool_ = new SynchronizedInt(0);

    protected final MessageFactory messageFactory_;

    public int matchCalled_ = 0;

    public int matchStructuredCalled_ = 0;

    private final POA poa_;

    private final ORB orb_;

    private Filter thisRef_;

    private final Logger logger_;

    private final org.jacorb.config.Configuration config_;

    private final EvaluationContextFactory evaluationContextFactory_;

    private final SynchronizedBoolean isActivated = new SynchronizedBoolean(false);

    private static final ConstraintInfo[] EMPTY_CONSTRAINT_INFO = new ConstraintInfo[0];

    ////////////////////////////////////////

    protected AbstractFilter(Configuration config,
            EvaluationContextFactory evaluationContextFactory, MessageFactory messageFactory,
            ORB orb, POA poa) throws ConfigurationException
    {
        super();

        orb_ = orb;
        poa_ = poa;

        config_ = ((org.jacorb.config.Configuration) config);
        logger_ = config_.getNamedLogger(getClass().getName());

        if (logger_.isInfoEnabled())
        {
            logger_.info("Created filter for Grammar: " + constraint_grammar());
        }

        messageFactory_ = messageFactory;

        evaluationContextFactory_ = evaluationContextFactory;

        constraintsLock_ = new WriterPreferenceReadWriteLock();

        wildcardMap_ = newWildcardMap(config_);

        disposables_.addDisposable(callbackManager_);
    }

    // //////////////////////////////////////

    private WildcardMap newWildcardMap(Configuration config) throws ConfigurationException
    {
        String wildcardMapImpl = config.getAttribute(Attributes.WILDCARDMAP_CLASS,
                Default.WILDCARDMAP_DEFAULT);

        try
        {
            Class wildcardMapClazz = ObjectUtil.classForName(wildcardMapImpl);

            Constructor ctor = wildcardMapClazz.getConstructor(new Class[0]);

            return (WildcardMap) ctor.newInstance(new Object[0]);
        } catch (ClassNotFoundException e)
        {
            // ignore
        } catch (IllegalArgumentException e)
        {
            //          ignore
        } catch (InstantiationException e)
        {
            //          ignore
        } catch (IllegalAccessException e)
        {
            //          ignore
        } catch (InvocationTargetException e)
        {
            //          ignore
        } catch (SecurityException e)
        {
            //          ignore
        } catch (NoSuchMethodException e)
        {
            //          ignore
        }

        throw new ConfigurationException(wildcardMapImpl
                + " is no valid WildcardMap Implementation");
    }

    public void configure(Configuration conf)
    {
    }

    public void preActivate()
    {
    }

    public org.omg.CORBA.Object activate()
    {
        if (thisRef_ == null)
        {
            thisRef_ = _this(orb_);
        }

        isActivated.set(true);

        return thisRef_;
    }

    public void deactivate()
    {
        try
        {
            poa_.deactivate_object(poa_.servant_to_id(this));
        } catch (WrongPolicy e)
        {
            logger_.fatalError("error deactivating object", e);
        } catch (ObjectNotActive e)
        {
            logger_.fatalError("error deactivating object", e);
        } catch (ServantNotActive e)
        {
            logger_.fatalError("error deactivating object", e);
        }
    }

    protected int newConstraintId()
    {
        return constraintIdPool_.increment();
    }

    /**
     * The <code>add_constraints</code> operation is invoked by a client in order to associate one
     * or more new constraints with the target filter object. The operation accepts as input a
     * sequence of constraint data structures, each element of which consists of a sequence of event
     * type structures (described in Section 3.2.1, "The Filter Interface," on page 3-14) and a
     * constraint expressed within the constraint grammar supported by the target object. Upon
     * processing each constraint, the target object associates a numeric identifier with the
     * constraint that is unique among all constraints it encapsulates. If any of the constraints in
     * the input sequence is not a valid expression within the supported constraint grammar, the
     * InvalidConstraint exception is raised. This exception contains as data the specific
     * constraint expression that was determined to be invalid. Upon successful processing of all
     * input constraint expressions, the <code>add_constraints</code> operation returns a sequence
     * in which each element will be a structure including one of the input constraint expressions,
     * along with the unique identifier assigned to it by the target filter object. <br>
     * Note that the semantics of the <code>add_constraints</code> operation are such that its
     * sideeffects are performed atomically upon the target filter object. Once
     * <code>add_constraints</code> is invoked by a client, the target filter object is
     * temporarily disabled from usage by any proxy object it may be associated with. The operation
     * is then carried out, either successfully adding all of the input constraints to the target
     * object or none of them (in the case one of the input expressions was invalid). Upon
     * completion of the operation, the target filter object is effectively re-enabled and can once
     * again be used by associated filter objects in order to make event forwarding decisions.
     */
    public ConstraintInfo[] add_constraints(ConstraintExp[] constraintExp) throws InvalidConstraint
    {
        FilterConstraint[] _arrayFilterConstraint = newFilterConstraints(constraintExp);

        try
        {
            // access writeonly lock
            constraintsLock_.writeLock().acquire();

            try
            {
                return add_constraint(constraintExp, _arrayFilterConstraint);
            } finally
            {
                // give up the lock
                constraintsLock_.writeLock().release();
            }
        } catch (InterruptedException ie)
        {
            // propagate without throwing
            Thread.currentThread().interrupt();

            return EMPTY_CONSTRAINT_INFO;
        }
    }

    private ConstraintInfo[] add_constraint(ConstraintExp[] constraintExp,
            FilterConstraint[] filterConstraints) throws InterruptedException
    {
        final ConstraintInfo[] _arrayConstraintInfo = new ConstraintInfo[filterConstraints.length];

        for (int _x = 0; _x < constraintExp.length; _x++)
        {
            int _constraintId = newConstraintId();

            _arrayConstraintInfo[_x] = new ConstraintInfo(constraintExp[_x], _constraintId);

            ConstraintEntry _entry = new ConstraintEntry(filterConstraints[_x],
                    _arrayConstraintInfo[_x]);

            addEventTypeMappingsForConstraint(_entry);

            constraints_.put(new Integer(_constraintId), _entry);

            notifyCallbacks();
        }

        return _arrayConstraintInfo;
    }

    private void addEventTypeMappingsForConstraint(ConstraintEntry entry)
    {
        int _eventTypeCount = entry.getEventTypeCount();

        if (_eventTypeCount == 0)
        {
            addConstraintEntryToWildcardMap(EMPTY_EVENT_TYPE_CONSTRAINT_KEY, entry);
        }
        else
        {
            for (int _y = 0; _y < _eventTypeCount; ++_y)
            {
                EventTypeWrapper _eventTypeWrapper = entry.getEventTypeWrapper(_y);

                addConstraintEntryToWildcardMap(_eventTypeWrapper.getConstraintKey(), entry);
            }
        }
    }

    private void addConstraintEntryToWildcardMap(String constraintKey,
            ConstraintEntry constraintEntry)
    {
        List _listOfConstraintEntry = (List) wildcardMap_.getNoExpansion(constraintKey);

        if (_listOfConstraintEntry == null)
        {
            _listOfConstraintEntry = new LinkedList();

            wildcardMap_.put(constraintKey, _listOfConstraintEntry);
        }

        _listOfConstraintEntry.add(constraintEntry);
    }

    private FilterConstraint[] newFilterConstraints(ConstraintExp[] constraintExp)
            throws InvalidConstraint
    {
        FilterConstraint[] _arrayFilterConstraint = new FilterConstraint[constraintExp.length];

        // creation of the FilterConstraint's may cause a
        // InvalidConstraint Exception. Note that the State of the
        // Filter has not been changed yet.
        for (int _x = 0; _x < constraintExp.length; _x++)
        {
            _arrayFilterConstraint[_x] = newFilterConstraint(constraintExp[_x]);
        }

        return _arrayFilterConstraint;
    }

    protected abstract FilterConstraint newFilterConstraint(ConstraintExp constraintExp)
            throws InvalidConstraint;

    ////////////

    public void modify_constraints(int[] deleteIds, ConstraintInfo[] constraintInfo)
            throws ConstraintNotFound, InvalidConstraint
    {
        try
        {
            // write lock
            constraintsLock_.writeLock().acquire();

            try
            {
                Integer[] _deleteKeys = checkConstraintsToBeDeleted(deleteIds);

                FilterConstraint[] _arrayConstraintEvaluator = checkConstraintsToBeModified(constraintInfo);

                deleteConstraints(_deleteKeys);

                modifyConstraints(constraintInfo, _arrayConstraintEvaluator);

                notifyCallbacks();
            } finally
            {
                constraintsLock_.writeLock().release();
            }
        } catch (InterruptedException ie)
        {
            Thread.currentThread().interrupt();
        }
    }

    private void modifyConstraints(ConstraintInfo[] constraintInfo,
            FilterConstraint[] filterConstraints)
    {
        for (int _x = 0; _x < constraintInfo.length; _x++)
        {
            Integer _key = new Integer(constraintInfo[_x].constraint_id);

            ConstraintEntry _updatedEntry = new ConstraintEntry(filterConstraints[_x],
                    constraintInfo[_x]);

            // overwrite existing entry
            constraints_.put(_key, _updatedEntry);

            int _eventTypeCount = _updatedEntry.getEventTypeCount();

            for (int _y = 0; _y < _eventTypeCount; ++_y)
            {
                EventTypeIdentifier _eventTypeIdentifier = _updatedEntry.getEventTypeWrapper(_y);

                List _listOfConstraintEvaluator = (List) wildcardMap_
                        .getNoExpansion(_eventTypeIdentifier.getConstraintKey());

                // list should NEVER be null as the constraint is modified and therefor
                // should have existed before.
                _listOfConstraintEvaluator.add(_updatedEntry);
            }
        }
    }

    private FilterConstraint[] checkConstraintsToBeModified(ConstraintInfo[] constraintInfo)
            throws InvalidConstraint, ConstraintNotFound
    {
        FilterConstraint[] _arrayConstraintEvaluator = new FilterConstraint[constraintInfo.length];

        for (int _x = 0; _x < constraintInfo.length; ++_x)
        {
            if (constraints_.containsKey(new Integer(constraintInfo[_x].constraint_id)))
            {
                _arrayConstraintEvaluator[_x] = newFilterConstraint(constraintInfo[_x].constraint_expression);
            }
            else
            {
                throw new ConstraintNotFound(constraintInfo[_x].constraint_id);
            }
        }
        return _arrayConstraintEvaluator;
    }

    private Integer[] checkConstraintsToBeDeleted(int[] idsToBeDeleted) throws ConstraintNotFound
    {
        final Integer[] _deleteKeys = new Integer[idsToBeDeleted.length];

        for (int _x = 0; _x < idsToBeDeleted.length; ++_x)
        {
            _deleteKeys[_x] = new Integer(idsToBeDeleted[_x]);

            if (!constraints_.containsKey(_deleteKeys[_x]))
            {
                throw new ConstraintNotFound(idsToBeDeleted[_x]);
            }
        }
        return _deleteKeys;
    }

    private void deleteConstraints(Integer[] keys)
    {
        for (int _x = 0; _x < keys.length; ++_x)
        {
            ConstraintEntry _deletedEntry = (ConstraintEntry) constraints_.remove(keys[_x]);

            removeEventTypeMappingForConstraint(keys[_x], _deletedEntry);
        }
    }

    private void removeEventTypeMappingForConstraint(Integer key, ConstraintEntry _deletedEntry)
    {
        int _eventTypeCount = _deletedEntry.getEventTypeCount();

        for (int _y = 0; _y < _eventTypeCount; ++_y)
        {
            EventTypeIdentifier _eventTypeIdentifier = _deletedEntry.getEventTypeWrapper(_y);

            List _listOfConstraintEvaluator = (List) wildcardMap_
                    .getNoExpansion(_eventTypeIdentifier.getConstraintKey());

            Iterator _i = _listOfConstraintEvaluator.iterator();

            // traverse list of wildcard mappings. as a sideeffect mappings for deleted
            // constraints are removed.
            while (_i.hasNext())
            {
                ConstraintEntry _constraint = (ConstraintEntry) _i.next();

                if (_constraint.getConstraintId() == key.intValue())
                {
                    _i.remove();
                    break;
                }
            }
        }
    }

    public ConstraintInfo[] get_constraints(int[] ids) throws ConstraintNotFound
    {
        final Sync _lock = constraintsLock_.readLock();

        try
        {
            _lock.acquire();
            try
            {
                final ConstraintInfo[] _constraintInfo = new ConstraintInfo[ids.length];

                for (int _x = 0; _x < ids.length; ++_x)
                {
                    Integer _key = new Integer(ids[_x]);

                    if (constraints_.containsKey(_key))
                    {
                        _constraintInfo[_x] = ((ConstraintEntry) constraints_.get(_key))
                                .getConstraintInfo();
                    }
                    else
                    {
                        throw new ConstraintNotFound(ids[_x]);
                    }
                }

                return _constraintInfo;
            } finally
            {
                _lock.release();
            }
        } catch (InterruptedException ie)
        {
            Thread.currentThread().interrupt();

            return EMPTY_CONSTRAINT_INFO;
        }
    }

    public ConstraintInfo[] get_all_constraints()
    {
        try
        {
            constraintsLock_.readLock().acquire();

            try
            {
                ConstraintInfo[] _constraintInfo = new ConstraintInfo[constraints_.size()];

                Iterator _i = constraints_.values().iterator();

                for (int i = 0; i < _constraintInfo.length; i++)
                {
                    _constraintInfo[i] = ((ConstraintEntry) _i.next()).getConstraintInfo();
                }

                return _constraintInfo;
            } finally
            {
                constraintsLock_.readLock().release();
            }
        } catch (InterruptedException ie)
        {
            Thread.currentThread().interrupt();

            return EMPTY_CONSTRAINT_INFO;
        }
    }

    public void remove_all_constraints()
    {
        try
        {
            constraintsLock_.writeLock().acquire();

            try
            {
                constraints_.clear();

                wildcardMap_.clear();

                notifyCallbacks();
            } finally
            {
                constraintsLock_.writeLock().release();
            }
        } catch (InterruptedException ie)
        {
            Thread.currentThread().interrupt();
        }
    }

    public void destroy()
    {
        dispose();
    }

    /**
     * call and use this Iterator inside a acquired read lock section only.
     */
    private Iterator getConstraintsForEvent(Message event)
    {
        String _key = event.getConstraintKey();

        return getIterator(_key);
    }

    public Iterator getIterator(Object key)
    {
        Object[] _entries = wildcardMap_.getWithExpansion(key);

        return new ConstraintIterator(_entries);
    }

    /**
     * Iterator over an Array of Lists. If a List is depleted this Iterator will switch
     * transparently to the next available list.
     */
    static private class ConstraintIterator implements Iterator
    {
        final Object[] arrayOfLists_;

        Iterator current_;

        int currentListIdx_ = 0;

        ConstraintIterator(Object[] arrayOfLists)
        {
            arrayOfLists_ = arrayOfLists;

            if (arrayOfLists_.length == 0)
            {
                current_ = null;
            }
            else
            {
                switchIterator();
            }
        }

        private void switchIterator()
        {
            current_ = ((List) arrayOfLists_[currentListIdx_]).iterator();
        }

        public boolean hasNext()
        {
            return current_ != null && current_.hasNext();
        }

        public Object next()
        {
            if (current_ == null)
            {
                throw new NoSuchElementException();
            }

            Object _ret = current_.next();

            if (!current_.hasNext() && currentListIdx_ < arrayOfLists_.length - 1)
            {
                ++currentListIdx_;
                
                switchIterator();
            }

            return _ret;
        }

        public void remove()
        {
            throw NOT_SUPPORTED;
        }
    }

    /**
     * generic version of the match operation
     */
    private int match_ReadLock(EvaluationContext evaluationContext, Message event)
            throws UnsupportedFilterableData
    {
        try
        {
            constraintsLock_.readLock().acquire();

            try
            {
                return match_NoLock(evaluationContext, event);
            } finally
            {
                constraintsLock_.readLock().release();
            }
        } catch (InterruptedException ie)
        {
            Thread.currentThread().interrupt();

            return NO_CONSTRAINTS_MATCH;
        }
    }

    private int match_NoLock(EvaluationContext evaluationContext, Message event)
            throws UnsupportedFilterableData
    {
        if (!constraints_.isEmpty())
        {
            Iterator _entries = getConstraintsForEvent(event);

            while (_entries.hasNext())
            {
                ConstraintEntry _entry = (ConstraintEntry) _entries.next();
                try
                {
                    boolean _result = _entry.getFilterConstraint().evaluate(evaluationContext,
                            event).getBool();

                    if (_result)
                    {
                        return _entry.getConstraintId();
                    }
                } catch (PropertyDoesNotExistException e)
                {
                    // non critical exception. ignore
                    // and continue with next Constraint
                    logger_.info("tried to access non existing Property", e);
                } catch (EvaluationException e)
                {
                    logger_.fatalError("Error evaluating filter", e);

                    throw new UnsupportedFilterableData(e.getMessage());
                }
            }
        }
        else
        {
            logger_.info("Filter has no Expressions");

            return CONSTRAINTS_EMPTY;
        }

        return NO_CONSTRAINTS_MATCH;
    }

    public boolean match(Any anyEvent) throws UnsupportedFilterableData
    {
        ++matchCalled_;

        return match_internal(anyEvent) >= 0;
    }

    /**
     * match Any to associated constraints. return the id of the first matching filter or
     * NO_CONSTRAINT.
     */
    protected int match_internal(Any anyEvent) throws UnsupportedFilterableData
    {
        final EvaluationContext _evaluationContext = evaluationContextFactory_
                .newEvaluationContext();

        try
        {
            final Message _event = messageFactory_.newMessage(anyEvent);

            try
            {
                return match_ReadLock(_evaluationContext, _event);
            } finally
            {
                _event.dispose();
            }
        } finally
        {
            _evaluationContext.dispose();
        }
    }

    public boolean match_structured(StructuredEvent structuredevent)
            throws UnsupportedFilterableData
    {
        ++matchStructuredCalled_;

        return match_structured_internal(structuredevent) >= 0;
    }

    /**
     * match the StructuredEvent to the associated constraints. return the id of the first matching
     * filter or NO_CONSTRAINT.
     */
    protected int match_structured_internal(StructuredEvent structuredEvent)
            throws UnsupportedFilterableData
    {
        final EvaluationContext _evaluationContext = evaluationContextFactory_
                .newEvaluationContext();

        try
        {
            final Message _event = messageFactory_.newMessage(structuredEvent);

            try
            {
                return match_ReadLock(_evaluationContext, _event);
            } finally
            {
                _event.dispose();
            }
        } finally
        {
            _evaluationContext.dispose();
        }
    }

    /**
     * match the TypedEvent to the associated constraints. return the id of the first matching
     * filter or NO_CONSTRAINT.
     */
    protected int match_typed_internal(Property[] typedEvent) throws UnsupportedFilterableData
    {
        final EvaluationContext _evaluationContext = evaluationContextFactory_
                .newEvaluationContext();

        try
        {
            final Message _event = messageFactory_.newMessage(typedEvent);

            try
            {
                return match_ReadLock(_evaluationContext, _event);
            } finally
            {
                _event.dispose();
            }
        } finally
        {
            _evaluationContext.dispose();
        }
    }

    public boolean match_typed(Property[] properties) throws UnsupportedFilterableData
    {
        return match_typed_internal(properties) >= 0;
    }

    public int attach_callback(NotifySubscribe notifySubscribe)
    {
        return callbackManager_.attach_callback(notifySubscribe);
    }

    public void detach_callback(int id)
    {
        callbackManager_.detach_callback(id);
    }

    public int[] get_callbacks()
    {
        return callbackManager_.get_callbacks();
    }

    private void notifyCallbacks() throws InterruptedException
    {
        final Iterator i = constraints_.keySet().iterator();
        final List eventTypes = new ArrayList();

        while (i.hasNext())
        {
            Object key = i.next();

            ConstraintEntry value = (ConstraintEntry) constraints_.get(key);

            int ets = value.getEventTypeCount();

            for (int j = 0; j < ets; ++j)
            {
                EventTypeWrapper et = value.getEventTypeWrapper(j);

                eventTypes.add(et.getEventType());
            }
        }

        callbackManager_.replaceWith((EventType[]) eventTypes
                .toArray(EventTypeWrapper.EMPTY_EVENT_TYPE_ARRAY));
    }

    public POA _default_POA()
    {
        return poa_;
    }

    public void dispose()
    {
        if (isActivated.get())
        {
            deactivate();

            isActivated.set(false);
        }

        disposables_.dispose();
    }

    public void addDisposeHook(Disposable disposeHook)
    {
        disposables_.addDisposable(disposeHook);
    }
}