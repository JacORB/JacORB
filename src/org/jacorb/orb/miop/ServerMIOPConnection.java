package org.jacorb.orb.miop;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.DatagramPacket;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import org.jacorb.config.Configuration;
import org.jacorb.config.ConfigurationException;
import org.jacorb.orb.CDRInputStream;
import org.omg.CORBA.COMM_FAILURE;
import org.omg.CORBA.MARSHAL;
import org.omg.CORBA.SystemException;
import org.omg.ETF.BufferHolder;
import org.omg.ETF.Profile;


/**
 * Listen to a specified group port and class D IP address for messages
 * addressed to this group.
 *
 * @author Alysson Neves Bessani
 * @author Nick Cross
 * @version 1.0
 * @see MIOPConnection MIOPListener
 */
public class ServerMIOPConnection extends MIOPConnection implements Runnable
{
   /** This socket */
   private MulticastSocket socket             = null;

   /** This thread */
   private Thread          groupListener      = null;

   /** the complete messages (of byte[] arrays] */
   private LinkedList      fullMessages       = new LinkedList ();

   /**
    * Incomplete messages. When a message is complete it is transfered to
    * the fullMessage list
    */
   private HashMap         incompleteMessages = null;

   /** Current read message */
   private byte[]          current            = null;

   /** read pos in the current message */
   private int             currentPos         = 0;

   /** socket and connection configuration parameters */
   private int socketTimeout;
   private int timeToLive;

   private int incompleteMessagesThreshold;

   private short packetMaxSize;

   /**
    * Creates a new server MIOP connection that listen to the specified group.
    *
    * @param profile group UIPMC profile
    */
   public ServerMIOPConnection ()
   {
      super ();

      groupListener = new Thread (this);
      groupListener.setDaemon (true);
   }


   /**
    * Connect the socket. Called by MIOPListener.
    *
    * @param unused unused because we use the one passed in constructor
    * @param time_out unused, we use SO_TIMEOUT
    */
   public void connect (Profile profile, long time_out)
   {
      if ( ! is_connected())
      {
         if (profile instanceof MIOPProfile)
         {
            this.profile = (MIOPProfile) profile;
         }
         else
         {
            throw new org.omg.CORBA.BAD_PARAM
            ( "attempt to connect an MIOP connection "
              + "to a non-MIOP profile: " + profile.getClass());
         }

         try
         {
            socket = new MulticastSocket (((MIOPProfile)profile).getUIPMCProfile ().the_port);

            socket.setSoTimeout (socketTimeout);
            socket.setTimeToLive (timeToLive);
            socket.joinGroup (((MIOPProfile)profile).getGroupInetAddress ());

            connection_info = socket.toString ();
         }
         catch (Exception e)
         {
            if (socket != null)
            {
               socket.close ();
            }
            throw new RuntimeException ("Can't create multicast socket: " + profile);
         }

         connected = true;

         groupListener.start ();
      }
   }


   /**
    * Tests if there is data available on this connection.
    *
    * @return true if there are data available.
    */
   public boolean is_data_available ()
   {
      return (current != null && currentPos < current.length) || !fullMessages.isEmpty ();
   }


   /**
    * Wait until there is some complete message ready data to be read from
    * connection.
    *
    * @param timeout unused.
    * @return always true.
    */
   public synchronized boolean wait_next_data (long timeout)
   {
      if (fullMessages.isEmpty ())
      {
         try
         {
            wait ();
         }
         catch (InterruptedException ie)
         {
         }
      }
      current = (byte[])fullMessages.removeFirst ();
      currentPos = 0;
      return true;
   }


   /**
    * Read data from the connection.
    *
    *
    * @param buffer the buffer holder.
    * @param offset the buffer offset.
    * @param minLength the minimum length to be read.
    * @param maxLength the maximum length to be read.
    * @param timeout unused
    */
   public synchronized int read (BufferHolder buffer, int offset, int minLength, int maxLength,
            long timeout)
   {

      if (current == null)
      {
         wait_next_data (0);
      }

      int writen = 0;

      do
      {
         int toRead = Math.min (current.length - currentPos, maxLength);

         System.arraycopy (current, currentPos, buffer.value, offset + writen, toRead);

         writen += toRead;
         currentPos += toRead;

         if (currentPos == current.length)
         {
            current = null;
         }
         if (current == null && writen < minLength)
         {
            wait_next_data (0);
         }
      }
      while (writen < minLength);

      return writen;
   }


   /**
    * Close this connection.
    */
   public synchronized void close ()
   {
      if (!connected)
      {
         return;
      }

      // Finish with the multicast socket
      try
      {
         socket.leaveGroup (((MIOPProfile)profile).getGroupInetAddress ());
      }
      catch (IOException ex)
      {
         if (logger.isDebugEnabled())
         {
            logger.debug ("Exception when closing the socket", ex);
         }
      }
      try
      {
         socket.close ();

         //this will cause exceptions when trying to read from
         //the streams. Better than "nulling" them.
         if( in_stream != null )
         {
            in_stream.close();
         }
         if( out_stream != null )
         {
            out_stream.close();
         }
      }
      catch (IOException ex)
      {
         if (logger.isDebugEnabled())
         {
            logger.debug ("Exception when closing the socket", ex);
         }
      }
      connected = false;
   }


   /**
    * Run method. Inherited from runnable, this method is used to stay listening
    * to the socket for new messages.
    */
   public void run ()
   {
      // create incomplete table if doesn't exist
      if (incompleteMessages == null)
      {
          incompleteMessages = new HashMap ();
      }

      // allocates a buffer
      byte[] buffer = new byte [packetMaxSize];

      while (connected)
      {
         try
         {
            // if the number of messages in incomplete table is greater than a
            // specified threshold we inspect this table to clear incomplete packets
            // collections.
            if (incompleteMessages.size () > incompleteMessagesThreshold)
            {
               dropIncompleteMessages ();
            }

            // creates a new datagram to be read
            DatagramPacket packet = new DatagramPacket (buffer, buffer.length);

            try
            {
               // wait for the datagram
               socket.receive (packet);
            }
            catch (SocketTimeoutException ste)
            {
               continue;
            }
            catch( InterruptedIOException e )
            {
                throw new org.omg.CORBA.TRANSIENT ("Interrupted I/O: " + e);
            }
            catch (IOException se)
            {
               throw to_COMM_FAILURE (se);
            }
            // the packet was received successfully.
            CDRInputStream in = new CDRInputStream (configuration.getORB (), packet.getData ());

            // Read the header
            //
            // Manually read in the stream rather than using the generated
            // PacketHeader_1_0Helper
            // as we may need to alter endian half way through.
            org.omg.MIOP.PacketHeader_1_0 header = new org.omg.MIOP.PacketHeader_1_0 ();
            header.magic = new char[4];
            in.read_char_array (header.magic, 0, 4);

            // Verify the message is MIOP
            if ( ! MulticastUtil.matchMIOPMagic (header.magic))
            {
               // if it isn't a MIOP message I can ignore it
               continue;
            }

            // We know it is MIOP from now on.
            header.hdr_version = in.read_octet ();
            header.flags = in.read_octet ();

            // Set endian for the stream
            in.setLittleEndian ((0x01 & header.flags) != 0);

            header.packet_length = in.read_ushort ();
            header.packet_number = in.read_ulong ();
            header.number_of_packets = in.read_ulong ();
            header.Id = org.omg.MIOP.UniqueIdHelper.read (in);

            int pos = in.get_pos ();
            // difference to next MulticastUtil.BOUNDARY (which is an 8 byte boundary)
            int header_padding = MulticastUtil.BOUNDARY - (pos % MulticastUtil.BOUNDARY);
            header_padding = (header_padding == MulticastUtil.BOUNDARY) ? 0 : header_padding;

            // skip header_padding bytes anyway, because if no body is
            // present, nobody will try to read it
            in.skip (header_padding);

            // read the GIOP data
            byte data[] = new byte[header.packet_length];
            if (in.available () < data.length)
            {
               throw new MARSHAL
               (
                  "Impossible length in MIOP header. Header denotes length of " +
                  header.packet_length +
                  " but only " +
                  in.available () +
                  " is available."
               );
            }
            in.read_octet_array (data, 0, header.packet_length);

            String messageId = new String (header.Id);
            FragmentedMessage message = (FragmentedMessage)incompleteMessages.get (messageId);

            // verify if it's the first message to arrive
            if (message == null)
            {
               // If this is the first fragment of the message create a  fragmented message
               message = new FragmentedMessage ();
               try
               {
                   message.configure (configuration);
               }
               catch (ConfigurationException e)
               {
                   logger.error("couldn't create a Fragmented message", e);
                   throw new IllegalArgumentException("wrong configuration: " + e);
               }
               incompleteMessages.put (messageId, message);
            }

            if (logger.isDebugEnabled ())
            {
               logger.debug ("Received message number " + (header.packet_number + 1) + " out of " + header.number_of_packets + " and adding fragment of size " + data.length);
            }
            message.addFragment (header, data);

            // verify if it's the last message to arrive
            if (message.isComplete ())
            {
               synchronized (this)
               {
                  incompleteMessages.remove (messageId);
                  fullMessages.addLast (message.buildMessage ());
                  notifyAll ();
               }
            }
         }
         catch (COMM_FAILURE e)
         {
            if (logger.isDebugEnabled())
            {
                logger.debug("Transport to " + connection_info +
                             ": stream closed " + e.getMessage() );
            }
            if (connected)
            {
               close();
            }
         }
         catch (SystemException e)
         {
            if (logger.isWarnEnabled ())
            {
               logger.warn ("ServerMIOPConnection caught exception.", e);
            }
         }
         catch (Throwable e)
         {
            if (logger.isErrorEnabled ())
            {
               logger.error ("ServerMIOPConnection caught exception.", e);
            }
         }
      }
   }


   /**
    * Remove all incomplete messages that can be deleted from the incomplete
    * messages table.
    */
   private final synchronized void dropIncompleteMessages ()
   {
      Iterator ids = incompleteMessages.keySet ().iterator ();

      while (ids.hasNext ())
      {
         Object id = ids.next ();
         if (((FragmentedMessage)incompleteMessages.get (id)).canBeDiscarded ())
         {
            incompleteMessages.remove (id);
         }
      }
   }


   /*
    * (non-Javadoc)
    *
    * @see org.jacorb.orb.etf.ConnectionBase#getTimeout()
    */
   protected int getTimeout ()
   {
       try
       {
           return socket.getSoTimeout();
       }
       catch (SocketException se)
       {
           throw to_COMM_FAILURE (se);
       }
   }


   /*
    * (non-Javadoc)
    *
    * @see org.jacorb.orb.etf.ConnectionBase#setTimeout(int)
    */
   protected void setTimeout (int timeout)
   {
       if (socket != null)
       {
           try
           {
               if (logger.isInfoEnabled())
               {
                   logger.info ("Socket timeout set to " + timeout + " ms");
               }
               socket.setSoTimeout(timeout);
           }
           catch( SocketException se )
           {
               if (logger.isInfoEnabled())
               {
                   logger.info("SocketException", se);
               }
           }
       }
   }


   public void configure(Configuration config) throws ConfigurationException
   {
       super.configure(config);

       socketTimeout = config.getAttributeAsInteger("jacorb.miop.timeout",
               MulticastUtil.SO_TIMEOUT);
       timeToLive = config.getAttributeAsInteger("jacorb.miop.time_to_live",
               MulticastUtil.TIME_TO_LIVE);
       incompleteMessagesThreshold = config.getAttributeAsInteger("jacorb.miop.incomplete_messages_threshold",
               MulticastUtil.INCOMPLETE_MESSAGES_THRESHOULD);
       packetMaxSize = (short) config.getAttributeAsInteger ("jacorb.miop.packet_max_size",
               MulticastUtil.PACKET_MAX_SIZE);
   }
}
