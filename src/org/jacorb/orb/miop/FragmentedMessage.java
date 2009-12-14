package org.jacorb.orb.miop;

import java.io.ByteArrayOutputStream;
import org.jacorb.config.*;
import org.omg.MIOP.PacketHeader_1_0;


/**
 * A collection of fragments of a giop message.
 *
 * @author Alysson Neves Bessani
 * @version 1.0
 * @see ServerMIOPConnection
 */
public class FragmentedMessage implements Configurable
{
   public static final int NUMBER_OF_FRAGMENTS = 4;
   public static final int FRAGMENT_INCREMENT  = 2;

   private final long      creationTime        = System.currentTimeMillis ();
   private int             numberOfFragments   = NUMBER_OF_FRAGMENTS;
   private byte[][]        fragments;
   private int             lastPacketArrived   = -1;
   private int             maxPacketArrived    = -1;
   private int             packetsReceived     = 0;
   private boolean         end_flag_dependent  = false;
   private boolean         end_flag_received   = false;

   /** message's configuration parameters */
   private int completionTimeout;
   private short packetDataMaxSize;

   /**
    * Tests if this message can be discarded or not.
    *
    * @return true if it can be discarded.
    */
   boolean canBeDiscarded ()
   {
      return (end_flag_received && !isComplete ())
          || (System.currentTimeMillis () - creationTime > numberOfFragments * completionTimeout);
   }


   /**
    * If all the fragments arrived then the GIOP message can be build. Only called
    * from ServerMIOPConnection is message.isComplete.
    *
    * @return byte[] of a GIOP message.
    */
   byte[] buildMessage ()
   {
      ByteArrayOutputStream out =
          new ByteArrayOutputStream (numberOfFragments * packetDataMaxSize);

      for (int i = 0; i <= maxPacketArrived; i++)
      {
         out.write (fragments[i], 0, fragments[i].length);
      }

      return out.toByteArray ();
   }


   /**
    * It verifies if there is some available position on the packet buffer.
    * Even if the last fragment arrived, there can be some packets that were
    * not delivered yet due to the non FIFO guarantees (of UDP),
    * So, this method verify if all fragments of the message were received.
    *
    * @return true if it contains all message fragments.
    */
   boolean isComplete ()
   {
      if (end_flag_dependent)
      {
         if (end_flag_received)
         {
            for (int i = 0; i < maxPacketArrived; i++)
            {
               if (fragments[i] == null)
               {
                  return false;
               }
            }
            return true;
         }
         else
         {
            return false;
         }
      }
      else
      {
         return packetsReceived == numberOfFragments;
      }
   }


   /**
    * Adds a fragment.
    *
    * @param header the fragment header.
    * @param fragment the fragment data.
    */
   synchronized void addFragment (PacketHeader_1_0 header, byte[] fragment)
   {
      if (fragments == null)
      {
         // Run only on arrival of the first package

         // Get the number of packets that must wait
         // If this number is zero, I have to wait for the flag
         if (header.number_of_packets > 0)
         {
            numberOfFragments = header.number_of_packets;
         }
         else
         {
            end_flag_dependent = true;
         }

         // create the array of fragments
         fragments = new byte[numberOfFragments][];
      }

      // put the fragment in the proper position
      lastPacketArrived = header.packet_number;

      if (lastPacketArrived > maxPacketArrived)
      {
         maxPacketArrived = lastPacketArrived;

         if (maxPacketArrived > fragments.length)
         {
            // the array beyond its size ... we will increase them.
            // Please note that this will never happen if the field
            // number_of_packets were properly completed
            byte[] oldFragments[] = fragments;
            numberOfFragments += FRAGMENT_INCREMENT;
            fragments = new byte[numberOfFragments][];
            System.arraycopy (oldFragments, 0, fragments, 0, fragments.length);
         }
      }

      // Insert here
      if (fragments[lastPacketArrived] == null)
      {
         fragments[lastPacketArrived] = fragment;
         packetsReceived++;
      }

      if (end_flag_dependent)
      {
         end_flag_received = ((header.flags & MulticastUtil.STOP_FLAG) == MulticastUtil.STOP_FLAG);
      }
   }


   public void configure (Configuration config) throws ConfigurationException
   {
       completionTimeout = config.getAttributeAsInteger ("jacorb.miop.message_completion_timeout",
               MulticastUtil.MESSAGE_COMPLETION_TIMEOUT);
       int packetMax = config.getAttributeAsInteger ("jacorb.miop.packet_max_size",
                                                             MulticastUtil.PACKET_MAX_SIZE);
       packetDataMaxSize = (short)(packetMax - MulticastUtil.PACKET_DATA_MAX_SIZE);
   }
}
