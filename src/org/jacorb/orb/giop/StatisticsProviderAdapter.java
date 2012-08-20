/*
 *        JacORB  - a free Java ORB
 *
 *     Copyright (C) 1997-2012 Gerald Brose / The JacORB Team.
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
 *   Software Foundation, 51 Franklin Street, Fifth Floor, Boston,
 *   MA 02110-1301, USA.
 */

package org.jacorb.orb.giop;

/**
 * Class StatisticsProviderAdapter is responsible for managing
 * the StatisticsProvider instances for collecting transport usage
 * information.
 *
 * @author Iliyan Jeliazkov
 */

final class StatisticsProviderAdapter implements StatisticsProvider
{
    private int cardinality_;
    private StatisticsProvider head_;
    private StatisticsProviderAdapter tail_;

    public StatisticsProviderAdapter (StatisticsProvider p)
    {
        this (p, null);
    }

    public StatisticsProviderAdapter (StatisticsProvider head, StatisticsProviderAdapter tail)
    {
        this.head_ = head;
        this.tail_ = tail;
        this.cardinality_ = (tail == null) ? 0 : tail.cardinality_ + 1;
    }

    public StatisticsProvider find (int cardinality)
    {
        if (this.cardinality_ == cardinality)
            return this.head_;

        if (this.tail_ == null)
            return null;

        return this.tail_.find (cardinality);
    }

    public void messageChunkSent(int size) {
        try {
            if(head_ != null)
                head_.messageChunkSent(size);
        }
        finally {
            if(tail_ != null)
                tail_.messageChunkSent(size);
        }
    }

    public void flushed() {
        try {
            if(head_ != null)
                head_.flushed();
        }
        finally {
            if(tail_ != null)
                tail_.flushed();
        }
    }

    public void messageReceived(int size) {
        try {
            if(head_ != null)
                head_.messageReceived(size);
        }
        finally {
            if(tail_ != null)
                tail_.messageReceived(size);
        }
    }
}
