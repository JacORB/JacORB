package org.jacorb.orb.miop;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.MulticastSocket;
import java.net.SocketException;
import org.jacorb.config.*;
import org.jacorb.orb.CDROutputStream;
import org.omg.ETF.Profile;
import org.omg.MIOP.PacketHeader_1_0;
import org.omg.MIOP.PacketHeader_1_0Helper;


/**
 * A client side one way miop connection. Send data via multicast sockets.
 *
 * @author Alysson Neves Bessani
 * @author Nick Cross
 * @version 1.0
 * @see MIOPConnection MIOPFactories
 */
public class ClientMIOPConnection extends MIOPConnection implements Configurable
{
   private static short nextMessage = 0;

   private MulticastSocket socket;
   private DatagramPacket packet;

   /** connection's configuration parameters */
   private short packetDataMaxSize;

   public ClientMIOPConnection ()
   {
      super ();
   }


   /**
    * Connect with the specified profile.
    *
    * @param profile a group MIOP profile.
    * @param timeout unused.
    */
   public void connect (Profile profile, long timeout)
   {
      if (is_connected ())
      {
         close ();
      }

      this.profile = (MIOPProfile)profile;

      try
      {
         socket = new MulticastSocket ();
      }
      catch (IOException ioe)
      {
         throw new RuntimeException ("Error while creating and setting multicast socket " + profile);
      }
   }


   /**
    * Test if this connection is connected.
    *
    * @return true if connected and false otherwise.
    */
   public boolean is_connected ()
   {
      return (socket != null) && socket.isConnected ();
   }


   /**
    * Close this connection (if it is opened).
    */
   public void close ()
   {
      if (socket != null)
      {
         socket.close ();
      }
   }


   /**
    * Send data to multicast group.
    *
    * @param is_first unused.
    * @param is_last unused.
    * @param data the data array.
    * @param offset array's offset.
    * @param length amount of data to be send.
    * @param time_out unused.
    */
   public void flush ()
   {
      super.flush();

      byte data[] = ((ByteArrayOutputStream)out_stream).toByteArray ();
      ((ByteArrayOutputStream)out_stream).reset();

      int length = data.length;
      int offset = 0;

      // handle the size of the last packet that is rest of the division between
      // the message's size and the maximum packet's size
      short lastPacketSize = (short)(length % packetDataMaxSize);

      // number of packages filled: the message size divided by the maximum size of a package
      int numberOfFullPackets = length / packetDataMaxSize;

      // number of packages: number of packages filled + (1 or 0)
      int numberOfPackets = numberOfFullPackets + ((lastPacketSize > 0) ? 1 : 0);

      // creates an id
      byte[] messageId = generateNewId ();

      // Here is the case (very rare) when size of data to be sent fit to
      // a collection of full packets without debris. But in any case we need
      // a final packet.
      if (lastPacketSize == 0)
      {
         numberOfFullPackets--;
         lastPacketSize = packetDataMaxSize;
      }

      PacketHeader_1_0 header;

      // Send all the packets except the last
      //
      int i = 0; // is out because it will be used later
      for (; i < numberOfFullPackets; i++)
      {
         header = new PacketHeader_1_0 (MulticastUtil.MAGIC, MulticastUtil.HDR_VERSION,
                  MulticastUtil.BIG_ENDIAN, packetDataMaxSize, i, numberOfPackets,
                  messageId);

         sendMIOPPacket (header, data, offset + i * packetDataMaxSize,
                         packetDataMaxSize);
      }

      // now the last ...
      header = new PacketHeader_1_0 (MulticastUtil.MAGIC, MulticastUtil.HDR_VERSION,
               (byte)(MulticastUtil.BIG_ENDIAN | MulticastUtil.STOP_FLAG), lastPacketSize, i,
               numberOfPackets, messageId);

      sendMIOPPacket (header, data, offset + i * packetDataMaxSize, lastPacketSize);
   }


   /**
    * Send a MIOP packet given the header and the bytes to send.
    *
    * @param header the MIOP packet header.
    * @param data the packet data array.
    * @param offset the data array offset.
    * @param length the amount of data to send.
    */
   private void sendMIOPPacket (PacketHeader_1_0 header, byte[] data, int offset, int length)
   {
      CDROutputStream os = new CDROutputStream (orb);

      PacketHeader_1_0Helper.write (os, header);
      os.check (length, MulticastUtil.BOUNDARY);
      os.write_octet_array (data, offset, length);

      try
      {
         byte[] buffer = os.getBufferCopy ();

         if (packet == null)
         {
            packet = new DatagramPacket
            (
               buffer,
               0,
               buffer.length,
               ((MIOPProfile)profile).getGroupInetAddress (),
               ((MIOPProfile)profile).getUIPMCProfile ().the_port
            );
         }
         else
         {
            packet.setData (buffer, 0, buffer.length);
         }
         socket.send (packet);
      }
      catch (IOException se)
      {
         if (logger.isDebugEnabled())
         {
             logger.debug("Transport to " + connection_info +
                          ": stream closed " + se.getMessage() );
         }
         throw to_COMM_FAILURE (se);
      }
   }


   /**
    * Generates an Id for a message.
    *
    * @param numberOfPackets the number of packets of the message.
    * @return the 12 byte Id
    */
   private byte[] generateNewId ()
   {
      byte[] id = new byte[MulticastUtil.ID_SIZE];
      byte serverId[] = orb.getServerId ();

      // copy the key to the current host
      System.arraycopy (serverId, 0, id, 0, serverId.length);

      // number of message
      synchronized (ClientMIOPConnection.class)
      {
         id[10] = (byte)((nextMessage >>  8) & 0xFF);
         id[11] = (byte)(nextMessage & 0xFF);

         nextMessage++;
      }

      return id;
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

   public void configure (Configuration config) throws ConfigurationException
   {
       super.configure (config);

       int packetMax = config.getAttributeAsInteger ("jacorb.miop.packet_max_size",
                                                     MulticastUtil.PACKET_MAX_SIZE);
       packetDataMaxSize = (short)(packetMax - MulticastUtil.PACKET_DATA_MAX_SIZE);
   }
}
