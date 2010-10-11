/*
 * AdaptorListener.java
 *
 * Created on 24 de Outubro de 2003, 10:10
 */

package org.jacorb.orb.miop;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.jacorb.config.Configuration;
import org.jacorb.config.ConfigurationException;
import org.omg.CORBA.BAD_INV_ORDER;
import org.omg.CORBA.NO_IMPLEMENT;
import org.omg.ETF.Connection;
import org.omg.GIOP.Version;


/**
 * This ORB group MIOP listener. Manages the server connections listening to the
 * groups multicast sockets.
 *
 * @author Alysson Neves Bessani
 * @version 1.0
 * @see MIOPFactories ServerMIOPConnection
 */
public class MIOPListener extends org.jacorb.orb.etf.ListenerBase
{
   /**
    * The table with active connections
    */
   private Map connections = new HashMap ();


    /**
    * Configure this object.
    *
    * @param configuration the object configuration
    * @throws ConfigurationException if any problem with the configuration is
    *         detected
    */
   public void configure (Configuration configuration) throws ConfigurationException
   {
      super.configure (configuration);

      profile = new MIOPProfile ("225.0.0.1", (short)10925, "default", 1, new Version ((byte)1,
               (byte)0), 1, null);
   }


   /**
    * Destroy this listener.
    */
   public void destroy ()
   {
      for (Iterator i = connections.keySet ().iterator (); i.hasNext ();)
      {
         removeGroupConnection (((Short)i.next ()).shortValue ());
      }
      super.destroy ();
   }


   /**
    * Wait for a upcoming connection.
    *
    * @return the new server connection
    */
   public Connection accept ()
   {
      if (up != null)
      {
         throw new BAD_INV_ORDER ("Must not call accept() when a Handle has been set");
      }
      else
      {
         Connection result = super.accept ();

         if (result != null)
         {
            MIOPProfile profile = (MIOPProfile)result.get_server_profile ();
            connections.put (new Short (profile.getUIPMCProfile ().the_port), result);
         }

         return result;
      }
   }


   /**
    * Not implemented.
    *
    * @param connection
    */
   public void completed_data (Connection connection)
   {
      throw new NO_IMPLEMENT ();
   }


   /**
    * Create a connection to a specified group.
    *
    * @param profile the group miop profile.
    */
   public void addGroupConnection (MIOPProfile profile)
   {
      ServerMIOPConnection c = new ServerMIOPConnection ();
      try
      {
         c.configure (configuration);
      }
      catch (ConfigurationException ce)
      {
         throw new org.omg.CORBA.INTERNAL ("ConfigurationException: " + ce.toString ());
      }
      c.connect (profile, 0);

      deliverConnection (c);
   }


   /**
    * Destroy a group connection.
    *
    * @param port the group port number.
    */
   public void removeGroupConnection (short port)
   {
      ServerMIOPConnection c = (ServerMIOPConnection)connections.remove (new Short (port));

      if (c != null)
      {
         c.close ();
      }
   }


   /**
    * Tests if this port is used for some group.
    *
    * @param port the port number.
    * @return boolean
    */
   public boolean haveGroupConnection (short port)
   {
      return connections.containsKey (new Short (port));
   }
}