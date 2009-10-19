package org.jacorb.test.notification.perf;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import org.omg.CORBA.Any;
import org.omg.CosNotification.StructuredEvent;

import org.jacorb.test.notification.Timing;
import org.jacorb.test.notification.TimingHelper;
import org.jacorb.test.notification.PerformanceListener;

class PerformanceLogger implements PerformanceListener
{

    private long minimum = Long.MAX_VALUE;
    public long getMinimum()
    {
        calc();
        return minimum;
    }

    private long maximum = 0;
    public long getMaximum()
    {
        calc();
        return maximum;
    }

    private long average = 0;
    public long getAverage()
    {
        calc();
        return average;
    }


    private boolean calced_ = false;

    class LogEntry
    {
        long receiveTime;
        long sendTime;
        long getTotalTime()
        {
            return receiveTime - sendTime;
        }
    }

    Map allEntries_ = new Hashtable();

    public void calc()
    {
        if ( calced_ )
        {
            return ;
        }

        minimum = Long.MAX_VALUE;
        maximum = 0;
        average = 0;
        int _sum = 0;

        Iterator _i = allEntries_.values().iterator();

        while ( _i.hasNext() )
        {
            LogEntry _e = ( LogEntry ) _i.next();
            long _v = _e.getTotalTime();

            if ( _v > maximum )
            {
                maximum = _v;
            }

            if ( _v < minimum )
            {
                minimum = _v;
            }

            _sum += _v;
        }

        average = _sum / allEntries_.size();

        calced_ = true;
    }

    public String toString()
    {
        StringBuffer _b = new StringBuffer();

        _b.append( "Number of Events: " + allEntries_.size() );
        _b.append( "\n" );
        int size = allEntries_.size();

        calc();

        _b.append( "Min: " + minimum );
        _b.append( "\n" );
        _b.append( "Max: " + maximum );
        _b.append( "\n" );
        _b.append( "Avg: " + average );

        return _b.toString();
    }

    public void eventSent( Any event, long currentTime, long took )
    {
        //      Timing _t = TimingHelper.extract(event);
        //      synchronized(allEntries_) {
        //          LogEntry _entry = (LogEntry)allEntries_.get(event);
        //          if (_entry == null) {
        //              _entry = new LogEntry();
        //          }
        //          _entry.sendTime = currentTime;
        //          allEntries_.put(event, _entry);
        //      }
    }

    public void eventReceived( StructuredEvent event, long currentTime )
    {
        eventReceived( event.remainder_of_body, currentTime );
    }

    public void eventReceived( Any event, long currentTime )
    {
        Timing _t = TimingHelper.extract( event );
        Integer _key = new Integer( _t.id );

        synchronized ( allEntries_ )
        {
            LogEntry _entry = ( LogEntry ) allEntries_.get( _key );

            if ( _entry == null )
            {
                _entry = new LogEntry();
            }

            _entry.sendTime = _t.currentTime;
            _entry.receiveTime = ( int ) currentTime;
            allEntries_.put( _key, _entry );
        }
    }

    public void eventFailed( Any event, Exception e )
    {}

}
