package org.jacorb.notification.queue;

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

import java.util.Comparator;

import org.jacorb.notification.interfaces.Message;

/**
 * @author Alphonse Bendt
 * @version $Id$
 */

class QueueUtil
{

    private QueueUtil() {}

    ////////////////////////////////////////

    static final Message[] NOTIFICATION_EVENT_ARRAY_TEMPLATE =
        new Message[ 0 ];

    static final HeapEntry[] HEAP_ENTRY_ARRAY_TEMPLATE =
        new HeapEntry[ 0 ];


    static Comparator ASCENDING_TIMEOUT_COMPARATOR = new Comparator()
        {
            public int compare( Object o1, Object o2 )
            {
                Message n1;
                Message n2;

                if ( o1 instanceof HeapEntry )
                    {
                        n1 = ( ( HeapEntry ) o1 ).event_;
                    }
                else if ( o1 instanceof Message )
                    {
                        n1 = ( Message ) o1;
                    }
                else
                    {
                        throw new IllegalArgumentException();
                    }

                if ( o2 instanceof HeapEntry )
                    {
                        n2 = ( ( HeapEntry ) o2 ).event_;
                    }
                else if ( o2 instanceof Message )
                    {
                        n2 = ( Message ) o2;
                    }
                else
                    {
                        throw new IllegalArgumentException();
                    }


                if ( n1.hasTimeout() )
                    {
                        if ( !n2.hasTimeout() )
                            {
                                return -1;
                            }
                        else
                            {
                                if ( n1.getTimeout() < n2.getTimeout() )
                                    {
                                        return -1;
                                    }
                                else if ( n1.getTimeout() > n2.getTimeout() )
                                    {
                                        return 1;
                                    }
                                else
                                    {
                                        return 0;
                                    }
                            }
                    }
                else if ( n2.hasTimeout() )
                    {
                        return 1;
                    }
                else
                    {
                        return 0;
                    }
            }
        };

    static Comparator ASCENDING_AGE_COMPARATOR = new Comparator()
        {
            public int compare( Object o1, Object o2 )
            {
                HeapEntry e1 = ( HeapEntry ) o1;
                HeapEntry e2 = ( HeapEntry ) o2;

                if ( e1.order_ < e2.order_ )
                    {
                        return -1;
                    }
                else if ( e1.order_ > e2.order_ )
                    {
                        return 1;
                    }
                else
                    {
                        return 0;
                    }
            }
        };

    static Comparator DESCENDING_AGE_COMPARATOR = new Comparator()
        {
            public int compare( Object o1, Object o2 )
            {
                return -ASCENDING_AGE_COMPARATOR.compare( o1, o2 );
            }
        };


    static Comparator ASCENDING_PRIORITY_COMPARATOR = new Comparator()
        {
            public int compare( Object o1, Object o2 )
            {

                Message n1;
                Message n2;

                if ( o1 instanceof HeapEntry )
                    {
                        n1 = ( ( HeapEntry ) o1 ).event_;
                    }
                else if ( o1 instanceof Message )
                    {
                        n1 = ( Message ) o1;
                    }
                else
                    {
                        throw new IllegalArgumentException();
                    }

                if ( o2 instanceof HeapEntry )
                    {
                        n2 = ( ( HeapEntry ) o2 ).event_;
                    }
                else if ( o2 instanceof Message )
                    {
                        n2 = ( Message ) o2;
                    }
                else
                    {
                        throw new IllegalArgumentException();
                    }


                if ( n1.getPriority() < n2.getPriority() )
                    {
                        return -1;
                    }
                else if ( n1.getPriority() > n2.getPriority() )
                    {
                        return 1;
                    }
                else
                    {
                        return 0;
                    }
            }
        };


    static Comparator DESCENDING_PRIORITY_COMPARATOR = new Comparator()
        {
            public int compare( Object o1, Object o2 )
            {
                return -ASCENDING_PRIORITY_COMPARATOR.compare( o1, o2 );
            }
        };
}
