// $Id$
package demo.sas;

/*
 * Rudely hacked from Sun's original Jaas example by Andy. The original
 * contained these copyright notices, so here they are:
 *
 * Copyright 2000 Sun Microsystems, Inc. All rights reserved.
 * Copyright 2000 Sun Microsystems, Inc. Tous droits reserves.
 *
 * The application must implement the CallbackHandler.
 *
 * <p> This application is text-based.  Therefore it displays information
 * to the user using the OutputStreams System.out and System.err,
 * and gathers input from the user using the InputStream, System.in.
 */

import java.io.*;
import java.util.*;
import java.security.Principal;
import javax.security.auth.*;
import javax.security.auth.callback.*;
import javax.security.auth.login.*;
import javax.security.auth.spi.*;
import com.sun.security.auth.*;
import com.tagish.auth.*;

public class LoginCallbackHandler implements CallbackHandler
{
        public String username;
        public String password;
        public String domain;

	/**
	 * Invoke an array of Callbacks.
	 *
	 * <p>
	 *
	 * @param callbacks an array of <code>Callback</code> objects which contain
	 *			the information requested by an underlying security
	 *			service to be retrieved or displayed.
	 *
	 * @exception java.io.IOException if an input or output error occurs. <p>
	 *
	 * @exception UnsupportedCallbackException if the implementation of this
	 *			method does not support one or more of the Callbacks
	 *			specified in the <code>callbacks</code> parameter.
	 */
	public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException
	{
		for (int i = 0; i < callbacks.length; i++)
		{
			if (callbacks[i] instanceof TextOutputCallback)
			{
				TextOutputCallback toc = (TextOutputCallback)callbacks[i];
				switch (toc.getMessageType())
				{
				case TextOutputCallback.INFORMATION:
					System.out.println(toc.getMessage());
					break;
				case TextOutputCallback.ERROR:
					System.out.println("ERROR: " + toc.getMessage());
					break;
				case TextOutputCallback.WARNING:
					System.out.println("WARNING: " + toc.getMessage());
					break;
				default:
					throw new IOException("Unsupported message type: " + toc.getMessageType());
				}

			}
			else if (callbacks[i] instanceof NameCallback)
			{
				// prompt the user for a username
				NameCallback nc = (NameCallback) callbacks[i];

				// ignore the provided defaultName
				System.err.print(nc.getPrompt());
				System.err.flush();
				nc.setName((new BufferedReader(new InputStreamReader(System.in))).readLine());
                                username = nc.getName();

			}
			else if (callbacks[i] instanceof TextInputCallback)
			{
				// prompt the user for a username
				TextInputCallback tc = (TextInputCallback) callbacks[i];

				// ignore the provided defaultName
				System.err.print(tc.getPrompt());
				System.err.flush();
				tc.setText((new BufferedReader(new InputStreamReader(System.in))).readLine());
                                domain = tc.getText();

			}
			else if (callbacks[i] instanceof PasswordCallback)
			{
				// prompt the user for sensitive information
				PasswordCallback pc = (PasswordCallback)callbacks[i];
				System.err.print(pc.getPrompt());
				System.err.flush();
				pc.setPassword(readPassword(System.in));
                                password = new String(pc.getPassword());
			}
			else
			{
				throw new UnsupportedCallbackException(callbacks[i], "Unrecognized Callback");
			}
		}
	}

	// Reads user password from given input stream.
	private char[] readPassword(InputStream in) throws IOException
	{
		char[] lineBuffer;
		char[] buf;
		int i;

		buf = lineBuffer = new char[128];

		int room = buf.length;
		int offset = 0;
		int c;

		loop: while (true)
		{
			switch (c = in.read())
			{
			case -1:
			case '\n':
				break loop;

			case '\r':
				int c2 = in.read();
				if ((c2 != '\n') && (c2 != -1))
				{
					if (!(in instanceof PushbackInputStream))
					{
						in = new PushbackInputStream(in);
					}

					((PushbackInputStream)in).unread(c2);
				}
				else
				{
					break loop;
				}

			default:
				if (--room < 0)
				{
					buf = new char[offset + 128];
					room = buf.length - offset - 1;
					System.arraycopy(lineBuffer, 0, buf, 0, offset);
					Arrays.fill(lineBuffer, ' ');
					lineBuffer = buf;
				}
				buf[offset++] = (char) c;
				break;
			}
		}

		if (offset == 0)
			return null;

		char[] ret = new char[offset];
		System.arraycopy(buf, 0, ret, 0, offset);
		Arrays.fill(buf, ' ');

		return ret;
	}
}
