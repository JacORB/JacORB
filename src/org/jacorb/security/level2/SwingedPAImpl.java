package org.jacorb.security.level2;

import java.io.*;
import java.net.*;
import java.util.*;

import java.security.*;
import java.security.cert.*;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;


import org.omg.SecurityLevel2.*;
import org.omg.Security.*;

import org.jacorb.util.Environment;

import org.jacorb.security.util.*;

/**
 * SwingedPAImpl
 * 
 * This simple authenticator just retrieves X.509v3 certificates
 * from a Java key store
 *
 * @author Nicolas Noffke
 * $Id$
 */

public class SwingedPAImpl
    extends org.jacorb.orb.LocalityConstrainedObject
    implements org.omg.SecurityLevel2.PrincipalAuthenticator
{  
    private LoginData loginData = null;
    private KeyStore keyStore = null;

    private SecAttributeManager attrib_mgr = null;
    
    // rt: orb param removed (this simplyfies the using with java reflection)
    public SwingedPAImpl()
    {
        loginData = new LoginData();
    	loginData.keyStoreLocation = Environment.keyStore();
        loginData.storePassphrase = 
            Environment.getProperty("jacorb.security.keystore_password");
        attrib_mgr = SecAttributeManager.getInstance();
    }  

    public int[] get_supported_authen_methods(java.lang.String mechanism)
    {
	return new int[]{0};
    }

    public AuthenticationStatus authenticate(int method, 
                                             String mechanism, 
                                             String security_name, //user name
                                             byte[] auth_data, //  passwd
                                             SecAttribute[] privileges, 
                                             CredentialsHolder creds, 
                                             OpaqueHolder continuation_data, 
                                             OpaqueHolder auth_specific_data
                                             )
    {
	jacorb.util.Debug.output(3,"starting authentication");
	try 
	{	
	    registerProvider();

            loginData.alias = security_name;

            if ( auth_data != null )
            {
                loginData.password = new String( auth_data );
            }

              
            if (( loginData.keyStoreLocation == null ) || 
                ( loginData.storePassphrase == null ) ||
                ( loginData.alias == null ) || 
                ( loginData.password == null ))
            {
                PAWindow paw = new PAWindow( loginData );
                paw.show();

                loginData = paw.getLoginData();          

                if( loginData == null )
                {
                    return AuthenticationStatus.SecAuthFailure;
                }          
            }
 
            keyStore = KeyStoreUtil.getKeyStore (loginData.keyStoreLocation, 
                                                 loginData.storePassphrase.toCharArray());

            X509Certificate[] cert_chain = (X509Certificate[]) 
                keyStore.getCertificateChain( loginData.alias );

            PrivateKey priv_key = (PrivateKey) 
                keyStore.getKey ( loginData.alias, 
                                  loginData.password.toCharArray() );

            KeyAndCert k_a_c = new KeyAndCert( priv_key, cert_chain );

            AttributeType type = new AttributeType
                ( new ExtensibleFamily( (short) 0,
                                        (short) 1 ),
                  AccessId.value );


            SecAttribute attrib = attrib_mgr.createAttribute( k_a_c,
                                                              type );
        

        
            CredentialsImpl credsImpl = 
                new CredentialsImpl( new SecAttribute[]{ attrib },
                AuthenticationStatus.SecAuthSuccess,
                InvocationCredentialsType.SecOwnCredentials);

            credsImpl.accepting_options_supported( Environment.supportedBySSL() );
            credsImpl.accepting_options_required( Environment.requiredBySSL() );
            credsImpl.invocation_options_supported( Environment.supportedBySSL() );
            credsImpl.invocation_options_required( Environment.requiredBySSL() );

            creds.value = credsImpl;

            org.jacorb.util.Debug.output(3,"authentication succeeded");

            return AuthenticationStatus.SecAuthSuccess;
	}
	catch (Exception e) 
	{
	    org.jacorb.util.Debug.output(2,e);

	    return org.omg.Security.AuthenticationStatus.SecAuthFailure;
	}
    }

    /** 
     * not implemented
     */
  
    public AuthenticationStatus continue_authentication(byte[] response_data, 
							Credentials creds, 
							OpaqueHolder continuation_data, 
							OpaqueHolder auth_specific_data)
    {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }


    private void registerProvider()
    {
        iaik.security.provider.IAIK.addAsProvider();

        org.jacorb.util.Debug.output(3, "added Provider IAIK" );
    }

    private class PAWindow
        extends JDialog
    {
        private JTextField ks_location_tf = null;
        private JPasswordField ks_pwd_tf = null;
        private JTextField alias_tf = null;
        private JPasswordField key_pwd_tf = null;

        private boolean canceled = false;

        public PAWindow( LoginData data )
        {
            super( JOptionPane.getRootFrame(),
                   "Authentication required",
                   true );

            JPanel panel = new JPanel();
            GridBagConstraints constraints = new GridBagConstraints();
            GridBagLayout gbl = new GridBagLayout();
            panel.setLayout( gbl );
        
            JLabel lbl = new JLabel( "KeyStore:" );
            buildConstraints( constraints, 0, 0, 1, 1 );
            constraints.anchor = GridBagConstraints.WEST;
            gbl.setConstraints( lbl, constraints );
            panel.add( lbl );
            
            ks_location_tf = new JTextField();
            if( data.keyStoreLocation != null )
            {
                ks_location_tf.setText( data.keyStoreLocation );
            }
            buildConstraints( constraints, 1, 0, 1, 1 );
            constraints.anchor = GridBagConstraints.WEST;
            constraints.fill = GridBagConstraints.HORIZONTAL;
            constraints.weightx = 100;
            gbl.setConstraints( ks_location_tf, constraints );
            panel.add( ks_location_tf );

            JButton btn = new JButton( "Choose" );
            btn.addActionListener( new ActionListener(){
                public void actionPerformed( ActionEvent evt ){
                    JFileChooser chooser = new JFileChooser();
                    chooser.setFileSelectionMode( JFileChooser.FILES_ONLY );

		    if( chooser.showOpenDialog( PAWindow.this ) == 
			JFileChooser.APPROVE_OPTION )
		    {
                        ks_location_tf.setText( chooser.getSelectedFile().getAbsolutePath() );
                    }
                }
            });
            buildConstraints( constraints, 2, 0, 1, 1 );
            gbl.setConstraints( btn, constraints );
            panel.add( btn );

            lbl = new JLabel( "KeyStore Passphrase:" );
            buildConstraints( constraints, 0, 1, 1, 1 );
            constraints.anchor = GridBagConstraints.WEST;
            gbl.setConstraints( lbl, constraints );
            panel.add( lbl );
            
            ks_pwd_tf = new JPasswordField();
            if( data.storePassphrase != null )
            {
                ks_pwd_tf.setText( data.storePassphrase );
            }
            buildConstraints( constraints, 1, 1, 1, 1 );
            constraints.anchor = GridBagConstraints.WEST;
            constraints.fill = GridBagConstraints.HORIZONTAL;
            constraints.weightx = 100;
            gbl.setConstraints( ks_pwd_tf, constraints );
            panel.add( ks_pwd_tf );

            lbl = new JLabel( "Key Alias:" );
            buildConstraints( constraints, 0, 2, 1, 1 );
            constraints.anchor = GridBagConstraints.WEST;
            gbl.setConstraints( lbl, constraints );
            panel.add( lbl );
            
            alias_tf = new JTextField();
            if( data.alias != null )
            {
                alias_tf.setText( data.alias );
            }
            buildConstraints( constraints, 1, 2, 1, 1 );
            constraints.anchor = GridBagConstraints.WEST;
            constraints.fill = GridBagConstraints.HORIZONTAL;
            constraints.weightx = 100;
            gbl.setConstraints( alias_tf, constraints );
            panel.add( alias_tf );

            lbl = new JLabel( "Alias Passphrase:" );
            buildConstraints( constraints, 0, 3, 1, 1 );
            constraints.anchor = GridBagConstraints.WEST;
            gbl.setConstraints( lbl, constraints );
            panel.add( lbl );
            
            key_pwd_tf = new JPasswordField();
            if( data.password != null )
            {
                key_pwd_tf.setText( data.password );
            }
            buildConstraints( constraints, 1, 3, 1, 1 );
            constraints.anchor = GridBagConstraints.WEST;
            constraints.fill = GridBagConstraints.HORIZONTAL;
            constraints.weightx = 100;
            gbl.setConstraints( key_pwd_tf, constraints );
            panel.add( key_pwd_tf );

            btn = new JButton( "OK" );
            btn.addActionListener( new ActionListener(){
                public void actionPerformed( ActionEvent evt ){
                    String s = ks_location_tf.getText();
                    if( s == null ||
                        s.length() == 0 )
                    {
                        JOptionPane.showMessageDialog( PAWindow.this,
                                                       "Please specify a KeyStore location",
                                                       "Data Missing",
                                                       JOptionPane.ERROR_MESSAGE );
                        return;
                    }

                    char[] pwd = ks_pwd_tf.getPassword();
                    if( pwd.length == 0 )
                    {
                        JOptionPane.showMessageDialog( PAWindow.this,
                                                       "Please specify a KeyStore Passphrase",
                                                       "Data Missing",
                                                       JOptionPane.ERROR_MESSAGE );
                        return;
                    }

                    s = alias_tf.getText();
                    if( s == null ||
                        s.length() == 0 )
                    {
                        JOptionPane.showMessageDialog( PAWindow.this,
                                                       "Please specify a Key Alias",
                                                       "Data Missing",
                                                       JOptionPane.ERROR_MESSAGE );
                        return;
                    }

                    pwd = key_pwd_tf.getPassword();
                    if( pwd.length == 0 )
                    {
                        JOptionPane.showMessageDialog( PAWindow.this,
                                                       "Please specify an Alias Passphrase",
                                                       "Data Missing",
                                                       JOptionPane.ERROR_MESSAGE );
                        return;
                    }

                    setVisible( false );                    
                    dispose();                    

                }
            });
            buildConstraints( constraints, 1, 4, 1, 1 );
            constraints.anchor = GridBagConstraints.EAST;
            gbl.setConstraints( btn, constraints );
            panel.add( btn );

            btn = new JButton( "Cancel" );
            btn.addActionListener( new ActionListener(){
                public void actionPerformed( ActionEvent evt ){
                    setVisible( false );
                    canceled = true;
                    dispose();
                }
            });
            buildConstraints( constraints, 2, 4, 1, 1 );
            constraints.anchor = GridBagConstraints.EAST;
            gbl.setConstraints( btn, constraints );
            panel.add( btn );

            getContentPane().setLayout( new BorderLayout() );
            getContentPane().add( panel, BorderLayout.CENTER );

            pack();
        }

        public LoginData getLoginData()
        {
            if( canceled )
            {
                return null;
            }

            LoginData data = new LoginData();
            data.keyStoreLocation = ks_location_tf.getText();
            data.storePassphrase = new String( ks_pwd_tf.getText() );
            data.alias = alias_tf.getText();
            data.password = new String( key_pwd_tf.getText() );

            return data;
        }

        private void buildConstraints(GridBagConstraints gbc, int gx, int gy, 
                                      int gw, int gh)
        {
            gbc.gridx = gx;
            gbc.gridy = gy;
            gbc.gridwidth = gw;
            gbc.gridheight = gh;
            gbc.weightx = 0;
            gbc.weighty = 0;
            gbc.fill = GridBagConstraints.NONE;
            gbc.anchor = GridBagConstraints.CENTER;
        }

    }
}




