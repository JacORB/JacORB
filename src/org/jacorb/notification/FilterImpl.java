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

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.jacorb.notification.filter.DynamicEvaluator;
import org.jacorb.notification.filter.EvaluationContext;
import org.jacorb.notification.filter.EvaluationException;
import org.jacorb.notification.filter.FilterConstraint;
import org.jacorb.notification.interfaces.Disposable;
import org.jacorb.notification.interfaces.Message;
import org.jacorb.notification.util.CachingWildcardMap;
import org.jacorb.notification.util.WildcardMap;
import org.jacorb.util.Debug;

import org.omg.CORBA.Any;
import org.omg.CORBA.NO_IMPLEMENT;
import org.omg.CORBA.ORB;
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
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAPackage.ObjectNotActive;
import org.omg.PortableServer.POAPackage.WrongPolicy;

import EDU.oswego.cs.dl.util.concurrent.ReadWriteLock;
import EDU.oswego.cs.dl.util.concurrent.Sync;
import EDU.oswego.cs.dl.util.concurrent.WriterPreferenceReadWriteLock;
import org.apache.avalon.framework.logger.Logger;

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
 * @author Alphonse Bendt
 * @version $Id$
 */

public class FilterImpl extends FilterPOA implements Disposable
{
    static Logger logger_ = Debug.getNamedLogger( FilterImpl.class.getName() );

    final static RuntimeException NOT_SUPPORTED =
        new UnsupportedOperationException();

    /**
     * contains a number of callbacks, which are notified each time there is a
     * change to the list of constraints.
     */
    protected Map callbacks_;

    /**
     * contains the associated constraints.
     */
    protected Map constraints_;

    protected WildcardMap wildcardMap_;

    protected ReadWriteLock constraintsLock_;

    private String constraintGrammar_;

    protected int constraintIdPool_ = 0;

    public static final int NO_CONSTRAINT = Integer.MIN_VALUE;

    protected ApplicationContext applicationContext_;

    protected DynamicEvaluator dynamicEvaluator_;

    protected DynAnyFactory dynAnyFactory_;

    protected MessageFactory messageFactory_;

    public FilterImpl(ApplicationContext applicationContext, String constraintGrammar)
    {
        super();

        if (logger_.isInfoEnabled()) {
            logger_.info("Created filter for Grammar: " + constraintGrammar);
        }

        constraintGrammar_ = constraintGrammar;

        applicationContext_ = applicationContext;

        messageFactory_ =
            applicationContext.getMessageFactory();

        dynAnyFactory_ = applicationContext.getDynAnyFactory();

        dynamicEvaluator_ = applicationContext.getDynamicEvaluator();

        // as access to constraints_ is controlled by
        // constraintsLock_ its safe to use unsynchronized HashMap here
        constraints_ = new HashMap();

        constraintsLock_ = new WriterPreferenceReadWriteLock();

        wildcardMap_ = new CachingWildcardMap(4);
    }

    public void init()
    {}

    protected int getConstraintId()
    {
        return ( ++constraintIdPool_ );
    }

    protected void releaseConstraintId( int id )
    {}

    /**
     * The constraint_grammar attribute is a readonly attribute which
     * identifies the particular grammar within which the constraint
     * expressions encapsulated by the target filter object have
     * meaning.
     */
    public String constraint_grammar()
    {
        return constraintGrammar_;
    }

    /**
     * The <code>add_constraints</code> operation is invoked by a
     * client in order
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
     * expressions, the <code>add_constraints</code> operation returns
     * a sequence
     * in which each element will be a structure including one of the
     * input constraint expressions, along with the unique identifier
     * assigned to it by the target filter object. <br>
     * Note that the semantics of the <code>add_constraints</code>
     * operation are
     * such that its sideeffects are performed atomically upon the
     * target filter object. Once <code>add_constraints</code> is
     * invoked by a
     * client, the target filter object is temporarily disabled from
     * usage by any proxy object it may be associated with. The
     * operation is then carried out, either successfully adding all
     * of the input constraints to the target object or none of them
     * (in the case one of the input expressions was invalid). Upon
     * completion of the operation, the target filter object is
     * effectively re-enabled and can once again be used by associated
     * filter objects in order to make event forwarding decisions.
     */
    public ConstraintInfo[] add_constraints( ConstraintExp[] constraintExp )
        throws InvalidConstraint
    {
        FilterConstraint[] _arrayConstraintEvaluator =
            new FilterConstraint[ constraintExp.length ];

        // creation of the FilterConstraint's may cause a
        // InvalidConstraint Exception. Note that the State of the
        // Filter has not been changed yet.
        for ( int _x = 0; _x < constraintExp.length; _x++ )
        {
            _arrayConstraintEvaluator[ _x ] =
                new FilterConstraint( constraintExp[ _x ] );
        }

        ConstraintInfo[] _arrayConstraintInfo =
            new ConstraintInfo[ constraintExp.length ];

        try
        {
            // access writeonly lock
            constraintsLock_.writeLock().acquire();

            try
            {
                for ( int _x = 0; _x < constraintExp.length; _x++ )
                {
                    // we did not create the constraint id's in the
                    // loop above. therefor no constraint id gets
                    // created if one constraint is invalid.
                    int _constraintId = getConstraintId();

                    _arrayConstraintInfo[ _x ] =
                        new ConstraintInfo( constraintExp[ _x ], _constraintId );

                    ConstraintEntry _entry =
                        new ConstraintEntry( _constraintId,
                                             _arrayConstraintEvaluator[ _x ],
                                             _arrayConstraintInfo[ _x ] );

                    int _eventTypeCount = _entry.getEventTypeCount();

                    for ( int _y = 0; _y < _eventTypeCount; ++_y )
                    {
                        EventTypeIdentifier _eventTypeIdentifier =
                            _entry.getEventTypeIdentifier( _y );

                        List _listOfConstraintEvaluator =
                            ( List ) wildcardMap_.getNoExpansion( _eventTypeIdentifier.getConstraintKey() );

                        if ( _listOfConstraintEvaluator == null )
                        {
                            _listOfConstraintEvaluator = new LinkedList();

                            wildcardMap_.put( _eventTypeIdentifier,
                                              _listOfConstraintEvaluator );
                        }

                        _listOfConstraintEvaluator.add( _entry );
                    }

                    constraints_.put( new Integer( _constraintId ), _entry );
                }

                return _arrayConstraintInfo;
                // end of protected section
            }
            finally
            {
                // give up the lock
                constraintsLock_.writeLock().release();
            }
        }
        catch ( InterruptedException ie )
        {
            // propagate without throwing
            Thread.currentThread().interrupt();
            return null;
        }
    }

    public void modify_constraints( int[] deleteIds,
                                    ConstraintInfo[] constraintInfo )
        throws ConstraintNotFound,
               InvalidConstraint
    {
        try
        {
            // write lock
            constraintsLock_.writeLock().acquire();

            try
            {
                // check if all id's that should be deleted exist
                Integer[] _deleteKeys = new Integer[ deleteIds.length ];

                for ( int _x = 0; _x < deleteIds.length; ++_x )
                {
                    _deleteKeys[ _x ] = new Integer( deleteIds[ _x ] );

                    if ( !constraints_.containsKey( _deleteKeys[ _x ] ) )
                    {
                        throw new ConstraintNotFound( deleteIds[ _x ] );
                    }
                }

                // create update constraints and check if the id exists
                Integer[] _constraintKeys =
                    new Integer[ constraintInfo.length ];

                FilterConstraint[] _arrayConstraintEvaluator =
                    new FilterConstraint[ constraintInfo.length ];

                for ( int _x = 0; _x < constraintInfo.length; ++_x )
                {
                    _constraintKeys[ _x ] =
                        new Integer( constraintInfo[ _x ].constraint_id );

                    if ( constraints_.containsKey( _constraintKeys[ _x ] ) )
                    {
                        _arrayConstraintEvaluator[ _x ] =
                            new FilterConstraint(
                                constraintInfo[ _x ].constraint_expression );
                    }
                    else
                    {
                        throw new ConstraintNotFound( constraintInfo[ _x ].constraint_id );
                    }

                    int _length =
                        constraintInfo[ _x ].constraint_expression.event_types.length;
                }

                // delete some constraints
                for ( int _x = 0; _x < deleteIds.length; ++_x )
                {
                    ConstraintEntry _deletedEntry =
                        ( ConstraintEntry ) constraints_.remove( _deleteKeys[ _x ] );

                    int _eventTypeCount = _deletedEntry.getEventTypeCount();

                    for ( int _y = 0; _y < _eventTypeCount; ++_y )
                    {
                        EventTypeIdentifier _eventTypeIdentifier =
                            _deletedEntry.getEventTypeIdentifier( _y );

                        List _listOfConstraintEvaluator =
                            ( List ) wildcardMap_.getNoExpansion( _eventTypeIdentifier.getConstraintKey() );

                        Iterator _i = _listOfConstraintEvaluator.iterator();

                        while ( _i.hasNext() )
                        {
                            ConstraintEntry _c = ( ConstraintEntry ) _i.next();

                            if ( _c.getConstraintId() == _deleteKeys[ _x ].intValue() )
                            {
                                _i.remove();
                                break;
                            }
                        }
                    }
                }

                // update some constraints
                for ( int _x = 0; _x < constraintInfo.length; _x++ )
                {
                    ConstraintEntry _entry =
                        new ConstraintEntry( _constraintKeys[ _x ].intValue(),
                                             _arrayConstraintEvaluator[ _x ],
                                             constraintInfo[ _x ] );

                    constraints_.put( _constraintKeys[ _x ], _entry );

                    int _eventTypeCount = _entry.getEventTypeCount();

                    for ( int _y = 0; _y < _eventTypeCount; ++_y )
                    {
                        EventTypeIdentifier _eventTypeIdentifier =
                            _entry.getEventTypeIdentifier( _y );

                        List _listOfConstraintEvaluator =
                            ( List ) wildcardMap_.getNoExpansion( _eventTypeIdentifier.getConstraintKey() );

                        //    if (_listOfConstraintEvaluator == null) {
                        //        _listOfConstraintEvaluator = new LinkedList();
                        //        wildcardMap_.put(_eventTypeIdentifier, _listOfConstraintEvaluator);
                        //    }
                        _listOfConstraintEvaluator.add( _entry );
                    }
                }
                return;
            }
            finally
            {
                constraintsLock_.writeLock().release();
            }
        }
        catch ( InterruptedException ie )
        {
            Thread.currentThread().interrupt();
        }
    }

    public ConstraintInfo[] get_constraints( int[] ids )
        throws ConstraintNotFound
    {
        ConstraintInfo[] _constraintInfo = new ConstraintInfo[ ids.length ];
        Sync _lock = constraintsLock_.readLock();
        try
        {
            _lock.acquire();

            try
            {
                for ( int _x = 0; _x < ids.length; ++_x )
                {
                    Integer _key = new Integer( ids[ _x ] );

                    if ( constraints_.containsKey( _key ) )
                    {
                        _constraintInfo[ _x ] =
                            ( ( ConstraintEntry ) constraints_.get( _key )).getConstraintInfo();
                    }
                    else
                    {
                        throw new ConstraintNotFound( ids[ _x ] );
                    }
                }

                return _constraintInfo;
            }
            finally
            {
                _lock.release();
            }
        }
        catch ( InterruptedException ie )
        {
            Thread.currentThread().interrupt();
            return null;
        }
    }

    public ConstraintInfo[] get_all_constraints()
    {
        try
        {
            constraintsLock_.readLock().acquire();

            try
            {
                ConstraintInfo[] _constraintInfo =
                    new ConstraintInfo[ constraints_.size() ];

                Iterator _i = constraints_.values().iterator();

                int _x = -1;

                while ( _i.hasNext() )
                {
                    _constraintInfo[ ++_x ] =
                        ( ( ConstraintEntry ) _i.next() ).getConstraintInfo();
                }

                return _constraintInfo;
            }
            finally
            {
                constraintsLock_.readLock().release();
            }
        }
        catch ( InterruptedException ie )
        {
            Thread.currentThread().interrupt();
            return null;
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
                return ;
            }
            finally
            {
                constraintsLock_.writeLock().release();
            }
        }
        catch ( InterruptedException ie )
        {
            Thread.currentThread().interrupt();
        }
    }

    public void destroy()
    {
        dispose();
    }

    /**
     * call and use this Iterator inside a acquired read lock section
     * only.
     */
    Iterator getConstraintsForEvent( Message event )
    {
        String _key = event.getConstraintKey();

        return getIterator( _key );
    }

    public Iterator getIterator( Object key )
    {
        Object[] _entries = wildcardMap_.getWithExpansion( key );

        return new ConstraintIterator( _entries );
    }

    /**
     * Iterator over an Array of Lists. If a List is depleted
     * this Iterator will switch transparently to the next available
     * list.
     */
    static private class ConstraintIterator implements Iterator
    {
        Object[] arrayOfLists_;
        Iterator current_;
        int currentListIdx_ = 0;

        ConstraintIterator( Object[] arrayOfLists )
        {
            arrayOfLists_ = arrayOfLists;

            if ( logger_.isDebugEnabled() )
            {
                for ( int x = 0; x < arrayOfLists_.length; ++x )
                {
                    logger_.debug( x + ": " + arrayOfLists_[ x ] );
                }
            }

            current_ = ( ( List ) arrayOfLists_[ currentListIdx_ ] ).iterator();
        }

        public boolean hasNext()
        {
            return current_.hasNext();
        }

        public Object next()
        {
            Object _ret = current_.next();

            if ( !current_.hasNext() && currentListIdx_ < arrayOfLists_.length - 1 )
            {
                current_ = ( ( List ) arrayOfLists_[ ++currentListIdx_ ] ).iterator();
            }

            return _ret;
        }

        public void remove
            ()
        {
            throw NOT_SUPPORTED;
        }
    }

    /**
     * generic version of the match operation
     */
    private int match(EvaluationContext evaluationContext,
                      Message event )
        throws UnsupportedFilterableData
    {
        try {
            constraintsLock_.readLock().acquire();

            try {
                if ( !constraints_.isEmpty() )
                    {
                        Iterator _entries = getConstraintsForEvent( event );

                        while ( _entries.hasNext() )
                            {
                                ConstraintEntry _entry =
                                    ( ConstraintEntry ) _entries.next();
                                try
                                    {
                                        boolean _result =
                                            _entry
                                            .getFilterConstraint()
                                            .evaluate( evaluationContext, event )
                                            .getBool();

                                        if ( _result )
                                            {
                                                return _entry.getConstraintId();
                                            }
                                    }
                                catch ( EvaluationException e )
                                    {
                                        logger_.fatalError("Error evaluating filter", e);
                                    }
                            }
                    }
                else
                    {
                        logger_.info( "Filter has no Expressions" );
                    }

                return NO_CONSTRAINT;
            }
            finally
                {
                    constraintsLock_.readLock().release();
                }
        }
        catch ( InterruptedException ie )
            {
                Thread.currentThread().interrupt();
                return NO_CONSTRAINT;
            }
    }


    public boolean match( Any anyEvent ) throws UnsupportedFilterableData
    {
        return match_internal( anyEvent ) != NO_CONSTRAINT;
    }


    /**
     * match Any to associated constraints. return the id of the
     * first matching filter or NO_CONSTRAINT.
     */
    protected int match_internal( Any anyEvent ) throws UnsupportedFilterableData
    {
        EvaluationContext _evaluationContext = null;
        Message _event = null;

        try
        {
            _evaluationContext = applicationContext_.newEvaluationContext();

            _event =
                messageFactory_.newMessage( anyEvent );

            return match(_evaluationContext, _event );
        }
        finally
        {
            try
            {
                _event.dispose();
            }
            catch (Exception e)
                {
                    logger_.fatalError("Error disposing event", e);
                }

            try
            {
                _evaluationContext.dispose();
            }
            catch (Exception e)
            {
                logger_.fatalError("Error disposing EvaluationContext", e);
            }
        }
    }

    public boolean match_structured( StructuredEvent structuredevent)
        throws UnsupportedFilterableData
    {
        return match_structured_internal(structuredevent) != NO_CONSTRAINT;
    }

    /**
     * match the StructuredEvent to the associated constraints. return
     * the id of the first matching filter or NO_CONSTRAINT.
     */
    protected int match_structured_internal( StructuredEvent structuredEvent )
        throws UnsupportedFilterableData
    {
        EvaluationContext _evaluationContext = null;
        Message _event = null;

        try {
            _evaluationContext = applicationContext_.newEvaluationContext();

            _event =
                messageFactory_.newMessage( structuredEvent );

            return match(_evaluationContext, _event );
        }
        finally {
            try {
                _event.dispose();
            }
            catch (Exception e) {
                logger_.fatalError("Error disposing event", e);
            }

            try {
                _evaluationContext.dispose();
            }
            catch (Exception e) {
                logger_.fatalError("Error releasing EvaluationContext", e);
            }
        }
    }

    /**
     * not implemented yet.
     */
    public boolean match_typed( Property[] properties )
        throws UnsupportedFilterableData
    {
        throw new NO_IMPLEMENT();
    }

    public int attach_callback( NotifySubscribe notifySubscribe )
    {
        throw new NO_IMPLEMENT();
    }

    public void detach_callback( int id )
    {
        throw new NO_IMPLEMENT();
    }

    public int[] get_callbacks()
    {
        throw new NO_IMPLEMENT();
    }

    public POA _default_POA()
    {
        return applicationContext_.getPoa();
    }

    public void dispose()
    {
        try
        {
            _poa().deactivate_object( _object_id() );
        }
        catch ( WrongPolicy e )
        {
            logger_.fatalError("error deactivating object", e);
        }
        catch ( ObjectNotActive e )
        {
            logger_.fatalError("error deactivating object", e);
        }
    }
}
