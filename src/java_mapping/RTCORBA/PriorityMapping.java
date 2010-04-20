package org.omg.RTCORBA;


import org.omg.CORBA.ShortHolder;

/**
 * <code>PriorityMapping</code> is taken from the Java Language
 * binding 2.5.4 in Real Time CORBA 05-01-04.
 *
 * @version 1.0
 */
abstract public class PriorityMapping
{
   /**
    * Converts CORBA priority to native priority
    * @param corba_priority
    * @param native_priority
    * @return true if success
    */
   abstract public boolean to_native(short corba_priority,
                                     ShortHolder native_priority);


   /**
    * Converts native priority to CORBA priority
    * @param native_priority
    * @param corba_priority
    * @return true if success
    */
   abstract public boolean to_CORBA(short native_priority,
                                    ShortHolder corba_priority);
}
