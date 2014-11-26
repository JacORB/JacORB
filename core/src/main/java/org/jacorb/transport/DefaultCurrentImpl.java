package org.jacorb.transport;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1997-2014 Gerald Brose / The JacORB Team.
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


import java.util.Iterator;
import java.util.List;

import org.jacorb.config.Configurable;
import org.jacorb.config.Configuration;
import org.jacorb.config.ConfigurationException;
import org.jacorb.orb.giop.TransportListener;
import org.omg.CORBA.LocalObject;
import org.slf4j.Logger;

/**
 *
 * A default implementation of the
 *
 * @see org.jacorb.transport.Current interface. Provides access to information
 *      about a Transport - the Traits.
 *
 * @author Iliyan Jeliazkov
 *
 */
public class DefaultCurrentImpl extends LocalObject implements Current, TransportListener, Configurable {

    private int statistics_provider_index_ = -1;

    /**
     * ctor
     */
    public DefaultCurrentImpl() {

    }

    @Override
    public void configure(Configuration configuration) throws ConfigurationException {

       logger_ = configuration.getLogger("org.jacorb.transport.current");

        // Plug-in a statistics provider, we know how to use
        List statsProviderClassNames =
            configuration.getAttributeList( "jacorb.connection.statistics_providers");
        statsProviderClassNames.add(DefaultStatisticsProvider.class.getName());

        StringBuffer buff = new StringBuffer();
        for (Iterator iter = statsProviderClassNames.iterator (); iter.hasNext ();) {
            buff.append (iter.next ());
            if (iter.hasNext ())
                buff.append (',');
        }
        configuration.setAttribute("jacorb.connection.statistics_providers", buff.toString());

        statistics_provider_index_ = statsProviderClassNames.size () - 1;
    }



    //
    // TCPConnectionListener interface
    //
    @Override
    public void transportSelected(Event event) {

        tss_transport_event_.set (event);

        if (logger_.isInfoEnabled ()) {
            logger_.info ("Transport selected " + event);
        }
    }


    /**
     * Retrieves the last event for the current thread.
     *
     * @return Event
     * @throws NoContext
     */
    protected Event getLatestTransportCurentEvent() throws NoContext {

        Event e = (Event) tss_transport_event_.get ();
        if (e == null) {
            if (logger_.isErrorEnabled ()) {
                logger_.error ("No events were available. Is traits() called outside of an upcall or interceptor?");
            }
            throw new NoContext ();
        }
        return e;
    }

    /**
     * A thread-specific storage for the Transport the thread has chosen. Null,
     * if no transport has been chosen yet.
     */
    private static final ThreadLocal tss_transport_event_ = new ThreadLocal ();


    /**
     * A logger.
     */
    private Logger logger_;

    @Override
    public int id() throws NoContext {
        Event e = getLatestTransportCurentEvent();
        return e.hashCode();
    }

    @Override
    public long bytes_sent() throws NoContext {
        Event e = getLatestTransportCurentEvent();
        DefaultStatisticsProvider p =
            (DefaultStatisticsProvider)e.getStatisticsProvider (statistics_provider_index_);
        return p.bytes_sent_;
    }

    @Override
    public long bytes_received() throws NoContext {
        Event e = getLatestTransportCurentEvent();
        DefaultStatisticsProvider p =
            (DefaultStatisticsProvider)e.getStatisticsProvider (statistics_provider_index_);
        return p.bytes_received_;
    }

    @Override
    public long messages_sent() throws NoContext {
        Event e = getLatestTransportCurentEvent();
        DefaultStatisticsProvider p =
            (DefaultStatisticsProvider)e.getStatisticsProvider (statistics_provider_index_);
        return p.messages_sent_;
    }

    @Override
    public long messages_received() throws NoContext {
        Event e = getLatestTransportCurentEvent();
        DefaultStatisticsProvider p =
            (DefaultStatisticsProvider)e.getStatisticsProvider (statistics_provider_index_);
        return p.messages_received_;
    }

    @Override
    public long open_since() throws NoContext {
        Event e = getLatestTransportCurentEvent();
        DefaultStatisticsProvider p =
            (DefaultStatisticsProvider)e.getStatisticsProvider (statistics_provider_index_);
        return p.created_;
    }
}
