package demo.sas;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.TextOutputCallback;
import javax.security.auth.callback.UnsupportedCallbackException;

public class JaasTxtCalbackHandler implements CallbackHandler {

	private String myUsername = "";
	private char[] myPassword = null;
	
	/**
	 * <p>Creates a callback handler that prompts and reads from the
	 * command line for answers to authentication questions.
	 * This can be used by JAAS applications to instantiate a
	 * CallbackHandler.
	
	 */
	public JaasTxtCalbackHandler() {
	}

	public void setMyUsername(String username) {
		myUsername = username;
	}

	public void setMyPassword(char[] password) {
		myPassword = password;
	}
	
	/**
	 * Handles the specified set of callbacks.
	 *
	 * @param callbacks the callbacks to handle
	 * @throws IOException if an input or output error occurs.
	 * @throws UnsupportedCallbackException if the callback is not an
	 * instance of NameCallback or PasswordCallback
	 */
	public void handle(Callback[] callbacks) throws UnsupportedCallbackException {

		for (int i = 0; i < callbacks.length; i++) {
			if (callbacks[i] instanceof TextOutputCallback) {
				TextOutputCallback tc = (TextOutputCallback) callbacks[i];

				String text;
				switch (tc.getMessageType()) {
					case TextOutputCallback.INFORMATION :
						text = "";
						break;
					case TextOutputCallback.WARNING :
						text = "Warning: ";
						break;
					case TextOutputCallback.ERROR :
						text = "Error: ";
						break;
					default :
						throw new UnsupportedCallbackException(
							callbacks[i],
							"Unrecognized message type");
				}

				String message = tc.getMessage();
				if (message != null) {
					text += message;
				}
				if (text != null) {
					System.err.println(text);
				}

			} else if (callbacks[i] instanceof NameCallback) {
				NameCallback nc = (NameCallback) callbacks[i];
				nc.setName(myUsername);

			} else if (callbacks[i] instanceof PasswordCallback) {
				PasswordCallback pc = (PasswordCallback) callbacks[i];
				pc.setPassword(myPassword);

			} else {
				throw new UnsupportedCallbackException(callbacks[i], "Unrecognized Callback");
			}
		}
	}

}
