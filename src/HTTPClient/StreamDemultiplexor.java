/*
 * @(#)StreamDemultiplexor.java				0.3-2 18/06/1999
 *
 *  This file is part of the HTTPClient package
 *  Copyright (C) 1996-1999  Ronald Tschalär
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2 of the License, or (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free
 *  Software Foundation, Inc., 59 Temple Place, Suite 330, Boston,
 *  MA 02111-1307, USA
 *
 *  For questions, suggestions, bug-reports, enhancement-requests etc.
 *  I may be contacted at:
 *
 *  ronald@innovation.ch
 *
 */

package HTTPClient;


import java.io.*;
import java.net.Socket;
import java.util.Vector;
import java.util.Enumeration;

/**
 * This class handles the demultiplexing of input stream. This is needed
 * for things like keep-alive in HTTP/1.0, persist in HTTP/1.1 and in HTTP-NG.
 *
 * @version	0.3-2  18/06/1999
 * @author	Ronald Tschalär
 */

class StreamDemultiplexor implements GlobalConstants
{
    /** the protocol were handling request for */
    private int                    Protocol;

    /** the connection we're working for */
    private HTTPConnection         Connection;

    /** the input stream to demultiplex */
    private ExtBufferedInputStream Stream;

    /** the socket this hangs off */
    private Socket                 Sock = null;

    /** signals after the closing of which stream to close the socket */
    private ResponseHandler        MarkedForClose;

    /** timer used to close the socket if unused for a given time */
    private SocketTimeout.TimeoutEntry Timer = null;

    /** timer thread which implements the timers */
    private static SocketTimeout   TimerThread = null;

    /** a Vector to hold the list of response handlers were serving */
    private LinkedList             RespHandlerList;

    /** number of unread bytes in current chunk (if transf-enc == chunked) */
    private int                    chunk_len;

    /** the currently set timeout for the socket */
    private int                    cur_timeout = 0;


    static
    {
	TimerThread = new SocketTimeout(60);
	TimerThread.start();
    }


    // Constructors

    /**
     * a simple contructor.
     *
     * @param protocol   the protocol used on this stream.
     * @param sock       the socket which we're to demux.
     * @param connection the http-connection this socket belongs to.
     */
    StreamDemultiplexor(int protocol, Socket sock, HTTPConnection connection)
	    throws IOException
    {
	this.Protocol   = protocol;
	this.Connection = connection;
	RespHandlerList = new LinkedList();
	init(sock);
    }


    /**
     * Initializes the demultiplexor with a new socket.
     *
     * @param stream   the stream to demultiplex
     */
    private void init(Socket sock)  throws IOException
    {
	if (DebugDemux)
	    System.err.println("Demux: Initializing Stream Demultiplexor (" +
				this.hashCode() + ")");

	this.Sock       = sock;
	this.Stream     = new ExtBufferedInputStream(sock.getInputStream());
	MarkedForClose  = null;
	chunk_len       = -1;

	// start a timer to close the socket after 60 seconds
	Timer = TimerThread.setTimeout(this);
    }


    // Methods

    /**
     * Each Response must register with us.
     */
    void register(Response resp_handler, Request req)  throws RetryException
    {
	synchronized(RespHandlerList)
	{
	    if (Sock == null)
		throw new RetryException();

	    RespHandlerList.addToEnd(
				new ResponseHandler(resp_handler, req, this));
	}
    }

    /**
     * creates an input stream for the response.
     *
     * @param resp the response structure requesting the stream
     * @return an InputStream
     */
    RespInputStream getStream(Response resp)
    {
	ResponseHandler resph;
	for (resph = (ResponseHandler) RespHandlerList.enumerate();
	     resph != null; resph = (ResponseHandler) RespHandlerList.next())
	{
	    if (resph.resp == resp)  break;
	}

	if (resph != null)
	    return resph.stream;
	else
	    return null;
    }


    /**
     * Restarts the timer thread that will close an unused socket after
     * 60 seconds.
     */
    void restartTimer()
    {
	if (Timer != null)  Timer.reset();
    }


    /**
     * reads an array of bytes from the master stream.
     */
    int read(byte[] b, int off, int len, ResponseHandler resph, int timeout)
	    throws IOException
    {
	if (resph.exception != null)
	    throw (IOException) resph.exception.fillInStackTrace();

	if (resph.eof)
	    return -1;


	// read the headers and data for all responses preceding us.

	ResponseHandler head;
	while ((head = (ResponseHandler) RespHandlerList.getFirst()) != null  &&
		head != resph)
	{
	    try
		{ head.stream.readAll(timeout); }
	    catch (IOException ioe)
	    {
		if (ioe instanceof InterruptedIOException)
		    throw ioe;
		else
		    throw (IOException) resph.exception.fillInStackTrace();
	    }
	}


	// Now we can read from the stream.

	synchronized(this)
	{
	    if (resph.exception != null)
		throw (IOException) resph.exception.fillInStackTrace();

	    if (DebugDemux)
	    {
		if (resph.resp.cd_type != CD_HDRS)
		    System.err.println("Demux: Reading for stream " +
				       resph.stream.hashCode() +
				       " (" + Thread.currentThread() + ")");
	    }

	    if (Timer != null)  Timer.hyber();

	    try
	    {
		int rcvd = -1;

		if (timeout != cur_timeout)
		{
		    if (DebugDemux)
		    {
			System.err.println("Demux: Setting timeout to " +
					   timeout + " ms");
		    }

		    try
			{ Sock.setSoTimeout(timeout); }
		    catch (Throwable t)
			{ }
		    cur_timeout = timeout;
		}

		switch (resph.resp.cd_type)
		{
		    case CD_HDRS:
			rcvd = Stream.read(b, off, len);
			if (rcvd == -1)
			    throw new EOFException("Premature EOF encountered");
			break;

		    case CD_0:
			rcvd = -1;
			close(resph);
			break;

		    case CD_CLOSE:
			rcvd = Stream.read(b, off, len);
			if (rcvd == -1)
			    close(resph);
			break;

		    case CD_CONTLEN:
			int cl = resph.resp.ContentLength;
			if (len > cl - resph.stream.count)
			    len = cl - resph.stream.count;

			rcvd = Stream.read(b, off, len);
			if (rcvd == -1)
			    throw new EOFException("Premature EOF encountered");

			if (resph.stream.count+rcvd == cl)
			    close(resph);

			break;

		    case CD_CHUNKED:
			if (chunk_len == -1)	// it's a new chunk
			    chunk_len = Codecs.getChunkLength(Stream);

			if (chunk_len > 0)		// it's data
			{
			    if (len > chunk_len)  len = chunk_len;
			    rcvd = Stream.read(b, off, len);
			    if (rcvd == -1)
				throw new EOFException("Premature EOF encountered");
			    chunk_len -= rcvd;
			    if (chunk_len == 0)	// got the whole chunk
			    {
				Stream.read();	// CR
				Stream.read();	// LF
				chunk_len = -1;
			    }
			}
			else	// the footers (trailers)
			{
			    resph.resp.readTrailers(Stream);
			    rcvd = -1;
			    close(resph);
			    chunk_len = -1;
			}
			break;

		    case CD_MP_BR:
			byte[] endbndry = resph.getEndBoundary(Stream);
			int[]  end_cmp  = resph.getEndCompiled(Stream);

			rcvd = Stream.read(b, off, len);
			if (rcvd == -1)
			    throw new EOFException("Premature EOF encountered");

			int ovf = Stream.pastEnd(endbndry, end_cmp);
			if (ovf != -1)
			{
			    rcvd -= ovf;
			    Stream.reset();
			    close(resph);
			}

			break;

		    default:
			throw new Error("Internal Error in StreamDemultiplexor: " +
					"Invalid cd_type " + resph.resp.cd_type);
		}

		restartTimer();
		return rcvd;

	    }
	    catch (InterruptedIOException ie)	// don't intercept this one
	    {
		restartTimer();
		throw ie;
	    }
	    catch (IOException ioe)
	    {
		if (DebugDemux)
		{
		    System.err.print("Demux: (" + Thread.currentThread() + ") ");
		    ioe.printStackTrace();
		}

		close(ioe, true);
		throw resph.exception;		// set by retry_requests
	    }
	    catch (ParseException pe)
	    {
		if (DebugDemux)
		{
		    System.err.print("Demux: (" + Thread.currentThread() + ") ");
		    pe.printStackTrace();
		}

		close(new IOException(pe.toString()), true);
		throw resph.exception;		// set by retry_requests
	    }
	}
    }

    /**
     * skips a number of bytes in the master stream. This is done via a
     * dummy read, as the socket input stream doesn't like skip()'s.
     */
    synchronized long skip(long num, ResponseHandler resph) throws IOException
    {
	if (resph.exception != null)
	    throw (IOException) resph.exception.fillInStackTrace();

	if (resph.eof)
	    return 0;

	byte[] dummy = new byte[(int) num];
	int rcvd = read(dummy, 0, (int) num, resph, 0);
	if (rcvd == -1)
	    return 0;
	else
	    return rcvd;
    }

    /**
     * Determines the number of available bytes.
     */
    synchronized int available(ResponseHandler resph) throws IOException
    {
	int avail = Stream.available();
	if (resph == null)  return avail;

	if (resph.exception != null)
	    throw (IOException) resph.exception.fillInStackTrace();

	if (resph.eof)
	    return 0;

	switch (resph.resp.cd_type)
	{
	    case CD_0:
		return 0;
	    case CD_HDRS:
		// this is something of a hack; I could return 0, but then
		// if you were waiting for something on a response that
		// wasn't first in line (and you didn't try to read the
		// other response) you'd wait forever. On the other hand,
		// we might be making a false promise here...
		return (avail > 0 ? 1 : 0);
	    case CD_CLOSE:
		return avail;
	    case CD_CONTLEN:
		int cl = resph.resp.ContentLength;
		cl -= resph.stream.count;
		return (avail < cl ? avail : cl);
	    case CD_CHUNKED:
		return avail;	// not perfect...
	    case CD_MP_BR:
		return avail;	// not perfect...
	    default:
		throw new Error("Internal Error in StreamDemultiplexor: " +
				"Invalid cd_type " + resph.resp.cd_type);
	}

    }


    /**
     * Closes the socket and all associated streams. If <var>exception</var>
     * is not null then all active requests are retried.
     *
     * <P>There are five ways this method may be activated. 1) if an exception
     * occurs during read or write. 2) if the stream is marked for close but
     * no responses are outstanding (e.g. due to a timeout). 3) when the
     * markedForClose response is closed. 4) if all response streams up until
     * and including the markedForClose response have been closed. 5) if this
     * demux is finalized.
     *
     * @param exception the IOException to be sent to the streams.
     * @param was_reset if true then the exception is due to a connection
     *                  reset; otherwise it means we generated the exception
     *                  ourselves and this is a "normal" close.
     */
    synchronized void close(IOException exception, boolean was_reset)
    {
	if (Sock == null)	// already cleaned up
	    return;

	if (DebugDemux)
	    System.err.println("Demux: Closing all streams and socket (" +
				this.hashCode() + ")");

	try
	    { Stream.close(); }
	catch (IOException ioe) { }
	try
	    { Sock.close(); }
	catch (IOException ioe) { }
	Sock = null;

	if (Timer != null)
	{
	    Timer.kill();
	    Timer = null;
	}

	Connection.DemuxList.remove(this);


	// Here comes the tricky part: redo outstanding requests!

	if (exception != null)
	    synchronized(RespHandlerList)
		{ retry_requests(exception, was_reset); }
    }


    /**
     * Retries outstanding requests. Well, actually the RetryModule does
     * that. Here we just throw a RetryException for each request so that
     * the RetryModule can catch and handle them.
     *
     * @param exception the exception that led to this call.
     * @param was_reset this flag is passed to the RetryException and is
     *                  used by the RetryModule to distinguish abnormal closes
     *                  from expected closes.
     */
    private void retry_requests(IOException exception, boolean was_reset)
    {
	RetryException  first = null,
			prev  = null;
	ResponseHandler resph = (ResponseHandler) RespHandlerList.enumerate();

	while (resph != null)
	{
	    /* if the application is already reading the data then the
	     * response has already been handled. In this case we must
	     * throw the real exception.
	     */
	    if (resph.resp.got_headers)
	    {
		resph.exception = exception;
	    }
	    else
	    {
		RetryException tmp = new RetryException(exception.getMessage());
		if (first == null)  first = tmp;

		tmp.request    = resph.request;
		tmp.response   = resph.resp;
		tmp.exception  = exception;
		tmp.conn_reset = was_reset;
		tmp.first      = first;
		tmp.addToListAfter(prev);

		prev = tmp;
		resph.exception = tmp;
	    }

	    RespHandlerList.remove(resph);
	    resph = (ResponseHandler) RespHandlerList.next();
	}
    }


    /**
     * Closes the associated stream. If this one has been markedForClose then
     * the socket is closed; else closeSocketIfAllStreamsClosed is invoked.
     */
    synchronized void close(ResponseHandler resph)
    {
	if (resph != (ResponseHandler) RespHandlerList.getFirst())
	    return;

	if (DebugDemux)
	    System.err.println("Demux: Closing stream " +
				resph.stream.hashCode() +
				" (" + Thread.currentThread() + ")");

	resph.eof = true;
	RespHandlerList.remove(resph);

	if (resph == MarkedForClose)
	    close(new IOException("Premature end of Keep-Alive"), false);
	else
	    closeSocketIfAllStreamsClosed();
    }


    /**
     * Close the socket if all the streams have been closed.
     *
     * <P>When a stream reaches eof it is removed from the response handler
     * list, but when somebody close()'s the response stream it is just
     * marked as such. This means that all responses in the list have either
     * not been read at all or only partially read, but they might have been
     * close()'d meaning that nobody is interested in the data. So If all the
     * response streams up till and including the one markedForClose have
     * been close()'d then we can remove them from our list and close the
     * socket.
     *
     * <P>Note: if the response list is emtpy or if no response is
     * markedForClose then this method does nothing. Specifically it does
     * not close the socket. We only want to close the socket if we've been
     * told to do so.
     *
     * <P>Also note that there might still be responses in the list after
     * the markedForClose one. These are due to us having pipelined more
     * requests to the server than it's willing to serve on a single
     * connection. These requests will be retried if possible.
     */
    synchronized void closeSocketIfAllStreamsClosed()
    {
	synchronized(RespHandlerList)
	{
	    ResponseHandler resph = (ResponseHandler) RespHandlerList.enumerate();

	    while (resph != null  &&  resph.stream.closed)
	    {
		if (resph == MarkedForClose)
		{
		    // remove all response handlers first
		    ResponseHandler tmp;
		    do
		    {
			tmp = (ResponseHandler) RespHandlerList.getFirst();
			RespHandlerList.remove(tmp);
		    }
		    while (tmp != resph);

		    // close the socket
		    close(new IOException("Premature end of Keep-Alive"), false);
		    return;
		}

		resph = (ResponseHandler) RespHandlerList.next();
	    }
	}
    }


    /**
     * returns the socket associated with this demux
     */
    synchronized Socket getSocket()
    {
	if (MarkedForClose != null)
	    return null;

	if (Timer != null)  Timer.hyber();
	return Sock;
    }


    /**
     * Mark this demux to not accept any more request and to close the
     * stream after this <var>resp</var>onse or all requests have been
     * processed, or close immediately if no requests are registered.
     *
     * @param response the Response after which the connection should
     *                 be closed.
     */
    synchronized void markForClose(Response resp)
    {
	synchronized(RespHandlerList)
	{
	    if (RespHandlerList.getFirst() == null)	// no active request,
	    {						// so close the socket
		close(new IOException("Premature end of Keep-Alive"), false);
		return;
	    }
	}

	if (Timer != null)
	{
	    Timer.kill();
	    Timer = null;
	}

	ResponseHandler resph, lasth = null;
	for (resph = (ResponseHandler) RespHandlerList.enumerate();
	     resph != null; resph = (ResponseHandler) RespHandlerList.next())
	{
	    if (resph.resp == resp)	// new resp precedes any others
	    {
		MarkedForClose = resph;

		if (DebugDemux)
		    System.err.println("Demux: stream " +
				       resp.inp_stream.hashCode() +
				       " marked for close (" +
				       Thread.currentThread() + ")");

		closeSocketIfAllStreamsClosed();
		return;
	    }

	    if (MarkedForClose == resph)
		return;	// already marked for closing after an earlier resp

	    lasth = resph;
	}

	if (lasth == null)
	    return;

	MarkedForClose = lasth;		// resp == null, so use last resph
	closeSocketIfAllStreamsClosed();

	if (DebugDemux)
	    System.err.println("Demux: stream " + lasth.stream.hashCode() +
			       " marked for close (" +
			       Thread.currentThread() + ")");
    }


    /**
     * Emergency stop. Closes the socket and notifies the responses that
     * the requests are aborted.
     *
     * @since V0.3
     */
    void abort()
    {
	if (DebugDemux)
	    System.err.println("Demux: Aborting socket (" +
				this.hashCode() + ")");


	// notify all responses of abort

	synchronized(RespHandlerList)
	{
	    for (ResponseHandler resph =
				(ResponseHandler) RespHandlerList.enumerate();
		 resph != null;
		 resph = (ResponseHandler) RespHandlerList.next())
	    {
		if (resph.resp.http_resp != null)
		    resph.resp.http_resp.markAborted();
		if (resph.exception == null)
		    resph.exception = new IOException("Request aborted by user");
	    }


	    /* Close the socket.
	     * Note: this duplicates most of close(IOException, boolean). We
	     * do *not* call close() because that is synchronized, but we want
	     * abort() to be asynch.
	     */
	    if (Sock != null)
	    {
		try
		{
		    try
			{ Sock.setSoLinger(false, 0); }
		    catch (Throwable t)
			{ }

		    try
			{ Stream.close(); }
		    catch (IOException ioe) { }
		    try
			{ Sock.close(); }
		    catch (IOException ioe) { }
		    Sock = null;

		    if (Timer != null)
		    {
			Timer.kill();
			Timer = null;
		    }
		}
		catch (NullPointerException npe)
		    { }

		Connection.DemuxList.remove(this);
	    }
	}
    }


    /**
     * A safety net to close the connection.
     */
    protected void finalize() throws Throwable
    {
	close((IOException) null, false);
	super.finalize();
    }


    /**
     * produces a string.
     * @return a string containing the class name and protocol number
     */
    public String toString()
    {
	String prot;

	switch (Protocol)
	{
	    case HTTP:
		prot = "HTTP"; break;
	    case HTTPS:
		prot = "HTTPS"; break;
	    case SHTTP:
		prot = "SHTTP"; break;
	    case HTTP_NG:
		prot = "HTTP_NG"; break;
	    default:
		throw new Error("HTTPClient Internal Error: invalid protocol " +
				Protocol);
	}

	return getClass().getName() + "[Protocol=" + prot + "]";
    }
}


/**
 * This thread is used to implement socket timeouts. It keeps a list of
 * timer entries and expries them after a given time.
 */
class SocketTimeout extends Thread implements GlobalConstants
{
    /**
     * This class represents a timer entry. It is used to close an
     * inactive socket after n seconds. Once running, the timer may be
     * suspended (hyber()), restarted (reset()), or aborted (kill()).
     * When the timer expires it invokes markForClose() on the
     * associated stream demultipexer.
     */
    class TimeoutEntry
    {
	boolean restart = false,
		hyber   = false,
		alive   = true;
	StreamDemultiplexor demux;
	TimeoutEntry next = null,
		     prev = null;

	TimeoutEntry(StreamDemultiplexor demux)
	{
	    this.demux = demux;
	}

	void reset()
	{
	    hyber = false;
	    if (restart)  return;
	    restart = true;

	    synchronized(time_list)
	    {
		if (!alive)  return;

		// remove from current position
		next.prev = prev;
		prev.next = next;

		// and add to end of timeout list
		next = time_list[current];
		prev = time_list[current].prev;
		prev.next = this;
		next.prev = this; 
	    }
	}

	void hyber()
	{
	    if (alive)  hyber = true;
	}

	void kill()
	{
	    alive   = false;
	    restart = false;
	    hyber   = false;

	    synchronized(time_list)
	    {
		if (prev == null)  return;
		next.prev = prev;
		prev.next = next;
		prev = null;
	    }
	}
    }

    private TimeoutEntry[]  time_list;
    private int		current;


    SocketTimeout(int secs)
    {
	super("SocketTimeout");

	try { setDaemon(true); }
	catch (SecurityException se) { }	// Oh well...
	setPriority(MAX_PRIORITY);

	time_list = new TimeoutEntry[secs];
	for (int idx=0; idx<secs; idx++)
	{
	    time_list[idx] = new TimeoutEntry(null);
	    time_list[idx].next = time_list[idx].prev = time_list[idx];
	}
	current = 0;
    }


    public TimeoutEntry setTimeout(StreamDemultiplexor demux)
    {
	TimeoutEntry entry = new TimeoutEntry(demux);
	synchronized(time_list)
	{
	    entry.next = time_list[current];
	    entry.prev = time_list[current].prev;
	    entry.prev.next = entry;
	    entry.next.prev = entry; 
	}

	return entry;
    }


    /**
     * This timer is implemented by sleeping for 1 second and then
     * checking the timer list.
     */
    public void run()
    {
	TimeoutEntry marked = null;

	while (true)
	{
	    try { sleep(1000L); } catch (InterruptedException ie) { }

	    synchronized(time_list)
	    {
		// reset all restart flags
		for (TimeoutEntry entry = time_list[current].next;
		     entry != time_list[current];
		     entry = entry.next)
		{
		    entry.restart = false;
		}

		current++;
		if (current >= time_list.length)
		    current = 0;

		// remove all expired timers 
		for (TimeoutEntry entry = time_list[current].next;
		     entry != time_list[current];
		     entry = entry.next)
		{
		    if (entry.alive  &&  !entry.hyber)
		    {
			TimeoutEntry prev = entry.prev;
			entry.kill();
			/* put on death row. Note: we must not invoke
			 * markForClose() here because it is synch'd
			 * and can therefore lead to a deadlock if that
			 * thread is trying to do a reset() or kill()
			 */
			entry.next = marked;
			marked = entry;
			entry = prev;
		    }
		}
	    }

	    while (marked != null)
	    {
		marked.demux.markForClose(null);
		marked = marked.next;
	    }
	}
    }
}

