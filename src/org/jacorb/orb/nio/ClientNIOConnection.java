/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1997-2008 Gerald Brose.
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

package org.jacorb.orb.nio;

import java.nio.channels.SocketChannel;
import java.io.IOException;
import java.net.Socket;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.net.InetSocketAddress;
import java.lang.Thread;

import org.jacorb.config.*;
import org.jacorb.orb.factory.SocketFactory;
import org.omg.CORBA.TIMEOUT;
import org.jacorb.util.SelectorRequest;
import org.jacorb.util.SelectorRequestCallback;
import org.jacorb.orb.iiop.IIOPProfile;
import org.jacorb.orb.iiop.IIOPAddress;
import org.jacorb.util.Time;
import org.jacorb.orb.iiop.IIOPProfile;
import org.jacorb.orb.iiop.IIOPAddress;

public class ClientNIOConnection
  extends NIOConnection
  implements Configurable
{
  private int noOfRetries  = 5;
  private int retryInterval = 0;

  public void configure(Configuration configuration)
    throws ConfigurationException
  {
    super.configure(configuration);

    noOfRetries = configuration.getAttributeAsInteger("jacorb.retries", 5);
    retryInterval = configuration.getAttributeAsInteger("jacorb.retry_interval",500);
  }

  // time_out is in milliseconds
  public synchronized void connect(org.omg.ETF.Profile server_profile, long timeout) {

    long nanoDeadline = (timeout == 0 ? Long.MAX_VALUE : System.nanoTime() + timeout*1000000);

    if( !connected ) {
      if (server_profile instanceof IIOPProfile) {
        this.profile = (IIOPProfile) server_profile;
      }
      else {
        throw new org.omg.CORBA.BAD_PARAM
          ( "attempt to connect an IIOP connection "
            + "to a non-IIOP profile: " + server_profile.getClass());
      }

      if (logger.isDebugEnabled()) {
        logger.debug("Trying to establish client connection with timeout " + timeout);
      }

      int retryCount = 0;
      for (retryCount = 0; retryCount < noOfRetries; retryCount++) {

        try {
          connectChannel (nanoDeadline);

          // in_stream = Channels.newInputStream (channel);
          // out_stream = Channels.newOutputStream (channel);

          if (logger.isInfoEnabled()) {
            logger.info("Connected to " + connection_info +
                        " from local port " +
                        channel.socket().getLocalPort() +
                        ( (timeout == 0) ? "" : " Timeout: " + timeout));
          }

          connected = true;
          return;
        }
        catch (TIMEOUT ex) {
          profile = null;
          throw ex;
        }
        catch (IOException ex) {
          logger.debug("Exception", ex);

          //only sleep and print message if we're actually
          //going to retry
          if (retryCount < noOfRetries-1) {
            if (logger.isInfoEnabled()) {
              logger.info("Retrying to connect to " +
                          connection_info );
            }

            try {
              Thread.sleep( retryInterval );
            }
            catch( InterruptedException i ) {
            }
          }
        }
      }

      if (retryCount == noOfRetries) {
        profile = null;
        throw new org.omg.CORBA.TRANSIENT
          ( "Retries exceeded, couldn't reconnect to " +
            connection_info );
      }
    }
  }

  public synchronized void close() {

    if (!connected) {
      return;
    }

    try {
      if (channel != null) {
        channel.close();
      }

      // this was copied from ClientIIOPConnection. Don't see why
      //  this woudl be required, but its included anyways
      if (in_stream != null) {
        in_stream.close();
      }
      if (out_stream != null) {
        out_stream.close();
      }

      if (logger.isInfoEnabled()) {
        logger.info("Client-side TCP transport to " +
                    connection_info + " closed.");
      }

      connected = false;
    }
    catch (IOException ex) {
      if (logger.isDebugEnabled()) {
        logger.debug ("Exception when closing the channel", ex);
      }

      throw handleCommFailure(ex);
    }
  }

  private void connectChannel (long nanoDeadline)
    throws IOException {

    List addressList = new ArrayList();
    addressList.add(((IIOPProfile)profile).getAddress());
    addressList.addAll(((IIOPProfile)profile).getAlternateAddresses());

    Iterator addressIterator = addressList.iterator();

    Exception exception = null;
    // initialize channel

    while (addressIterator.hasNext()) {
      try {
        channel = SocketChannel.open();
        channel.configureBlocking(false);

        IIOPAddress address = (IIOPAddress)addressIterator.next();

        final String ipAddress = address.getIP();
        final int port = address.getPort();
        if (ipAddress.indexOf(':') == -1) {
          connection_info = ipAddress + ":" + port;
        }
        else {
          connection_info = "[" + ipAddress + "]:" + port;
        }

        if (logger.isDebugEnabled()) {
          logger.debug("Trying to connect to " + connection_info);
        }

        channel.connect (new InetSocketAddress (ipAddress, port));

        SelectorRequest request = new SelectorRequest (SelectorRequest.Type.CONNECT, channel,
                                                       new ConnectCallback (), nanoDeadline);
        logger.info ("Thread " + Thread.currentThread().getId() + ": Adding connect request.");
        selectorManager.add (request);
        logger.info ("Thread " + Thread.currentThread().getId() + ": Wait for request completion.");
        request.waitOnCompletion (nanoDeadline);

        logger.info ("Thread " + Thread.currentThread().getId() + ": Finished waitOnCompletion()");
        logger.info ("Thread " + Thread.currentThread().getId() + ": Request status: " +
                      request.status.toString());

        if (request.status == SelectorRequest.Status.EXPIRED) {
          throw new TIMEOUT("connection timeout expired");
        }
        else if (request.status == SelectorRequest.Status.FAILED ||
                 request.status == SelectorRequest.Status.SHUTDOWN ||
                 request.status == SelectorRequest.Status.CLOSED) {
          // Somethings wrong with SelectorManager. Unwise to proceed
          throw new IOException ("SelectorManager is corrupted");
        }
        else if (channel.isConnected()) {
          // we are connected
          break;
        }
      }
      catch (Exception ex) {
        exception = ex;
      }

      // TBD: selectorManager.remove (request);
      channel.close ();
    }

    if (exception != null) {
      if (exception instanceof TIMEOUT) {
        throw (TIMEOUT) exception;
      }
      else if( exception instanceof IOException ) {
        throw (IOException) exception;
      }
      else {
        //not expected, because all used methods just throw IOExceptions or TIMEOUT
        //but... never say never ;o)
        throw new IOException ( "Unexpected exception occured: " + exception.toString() );
      }
    }
  }

  private class ConnectCallback extends SelectorRequestCallback {

    public boolean call (SelectorRequest action) {

      try {
        action.channel.finishConnect ();
      }
      catch (Exception ex) {
        logger.error ("Exception while finishing connection: " + ex.toString());
      }

    return false;
    }
  }

}