package org.jacorb.orb.listener;

import java.util.EventObject;

import javax.security.cert.X509Certificate;

import java.io.IOException;

/**
 * <code>SSLSessionEvent</code> defines an event state object for a
 * SSL Session.
 *
 * @author Nick Cross
 * @version $Id$
 */
public class SSLSessionEvent extends EventObject
{
    /**
     * <code>remoteIP</code> is the remote IP the socket is connecting to.
     */
    private final String remoteIP;


    /**
     * <code>remotePort</code> is the remote port the socket is connecting to.
     */
    private final int remotePort;


    /**
     * <code>localPort</code> is the local port the socket is using.
     */
    private final int localPort;

    /**
     * <code>localIP</code> is the local IP address the socket is using.
     */
    private final String localIP;

    /**
     * <code>remoteDN</code> is the distinguished name from the certificate of
     * the remote connection.
     */
    private String remoteDN = null;

    /**
     * <code>peerCerts</code> is an array of X509Certificates for the remote
     * connection
     */
    private final X509Certificate [] peerCerts;

    /**
     * <code>ex</code> the Exception that caused this event to be created
     */
    private final IOException ex;

    /**
     * Creates a new <code>SSLSessionEvent</code> instance passing in the
     * source of the event and relevant connection data.
     *
     * @param source an <code>Object</code> value
     * @param remoteIP a <code>String</code> value, maybe blank if a Socket has
     *                 just been created and not connected.
     * @param remotePort an <code>int</code> value, maybe -1 if a Socket has
     *                 just been created and not connected.
     * @param peerCerts a <code>X509Certificate</code>[] value
     * @param localPort an <code>int</code> value
     * @param localIP a <code>String</code> value
     */
    public SSLSessionEvent
        (Object source, String remoteIP, int remotePort, X509Certificate [] peerCerts, int localPort, String localIP, IOException ex)
    {
        super (source);

        this.remoteIP   = remoteIP;
        this.remotePort = remotePort;
        this.peerCerts  = peerCerts;
        this.localPort  = localPort;
        this.localIP    = localIP;
        this.ex         = ex;
    }


    /**
     * <code>getRemoteIP</code> is an accessor for the remote IP.
     *
     * @return a <code>String</code> value
     */
    public String getRemoteIP()
    {
        return remoteIP;
    }


    /**
     * <code>getRemotePort</code> is an accessor for the remote port.
     *
     * @return a <code>int</code> value
     */
    public int getRemotePort()
    {
        return remotePort;
    }


    /**
     * <code>getRemoteDN</code> is an accessor for the remote distinguished name
     *
     * @return a <code>String</code> value
     * @deprecated use getPeerCertificateChain
     */
    public String getRemoteDN()
    {
        StringBuffer sb;

        if (remoteDN == null && peerCerts != null)
        {
            sb = new StringBuffer();

            for (int i = 0; i < peerCerts.length; i++)
            {
                sb.append(peerCerts[i].toString());
                sb.append("\n\n");
            }

            remoteDN = sb.toString();
        }

        if (remoteDN == null || remoteDN.length() == 0)
        {
            remoteDN = "[Unable to verify peer certificates]";
        }

        return remoteDN;
    }


    /**
     * <code>getLocalPort</code> is an accessor for the local port.
     *
     * @return a <code>int</code> value
     */
    public int getLocalPort()
    {
        return localPort;
    }

    /**
     * <code>getLocalIP</code> is an accessor for the local IP address.
     *
     * @return a <code>String</code> value
     */
    public String getLocalIP()
    {
        return localIP;
    }

    /**
     * Returns the <code>X509Certificate</code> chain for the remote object.
     * This may return null if the certificates were not available when
     * this event was created
     *
     * @return An array of <code>X509Certificate</code> objects.
     */
    public X509Certificate [] getPeerCertificateChain ()
    {
        return peerCerts;
    }

    /**
     * Returns a String representation of this EventObject.
     *
     * @return  A String representation of this EventObject.
     */
    public String toString()
    {
        String exMsg = "";

        if (ex != null)
        {
            exMsg = " and exception " + ex;
        }

        return
        (
            super.toString() +
            " with local port " + localPort +
            " and local IP " + localIP +
            " and remote port " + remotePort +
            " and remote IP " + remoteIP +
            " and certificates " + getRemoteDN() +
            exMsg
        );
    }

    /**
     * Returns the exception that caused this event to be created
     * @return <code>IOException</code> this can be null
     */
    public IOException getCause()
    {
        return ex;
    }
}
