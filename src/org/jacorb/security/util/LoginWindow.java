package org.jacorb.security.util;

import java.awt.*;
import java.awt.event.*;
import java.security.*;
import org.jacorb.security.ssl.*;

public class LoginWindow
    //    extends java.awt.Dialog
    extends javax.swing.JDialog
    implements ActionListener
{   
    LoginData result;

    TextField keyStore = new TextField();
    TextField passwdKeyStore = new TextField();
    TextField alias = new TextField();
    TextField passwdAlias = new TextField();
    
    Button submit = new Button("Submit");
    Button clearTextfields = new Button("Clear Textfields");
    Button search = new Button("Search");
    
    public LoginWindow( String title, LoginData out, String ks, String sp, String a, String p)
    {
        super(new java.awt.Frame());
        setTitle( title );
        setModal( true );

        addWindowListener(
                  new WindowAdapter() {
                      public void windowClosing(WindowEvent event) {
                          try {
                              dispose();
                              getOwner().removeAll();
                              getOwner().removeNotify();
                              getOwner().dispose();
                              super.finalize();
                          } catch (java.lang.Throwable e) { e.printStackTrace(); }
                      }
                  }
               );
        
        result = out;
        
        keyStore.setText(ks);
        passwdKeyStore.setText(sp);
        alias.setText(a);
        passwdAlias.setText(p);
        
        //Layout setzen und Komponenten hinzufügen

        //Panel 1
        Panel panel1 = new Panel();
        panel1.setLayout(new GridLayout(4,1));
        panel1.add(new Label("Location of the key store:", Label.LEFT));
        panel1.add(new Label("Password of the key store:", Label.LEFT));
        panel1.add(new Label("Alias:", Label.LEFT));
        panel1.add(new Label("Password of the alias:", Label.LEFT));
        
        //Panel 2
        Panel panel2 = new Panel();
        panel2.setLayout(new GridLayout(4,1));
        panel2.add(keyStore);
        passwdKeyStore.setEchoChar('*');
        panel2.add(passwdKeyStore);
        panel2.add(alias);
        passwdAlias.setEchoChar('*');
        panel2.add(passwdAlias);
        
        //Panel 3
        Panel panel3 = new Panel();
        panel3.setLayout(new FlowLayout(FlowLayout.CENTER));
        submit.addActionListener(this);
        panel3.add(submit);
        clearTextfields.addActionListener(this);
        panel3.add(clearTextfields);
        
        //Panel 4
        Panel panel4 = new Panel();
        panel4.setLayout(new GridLayout(4,1));
        search.addActionListener(this);
        panel4.add(search);
        
        //Hauptfenster
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add("West",panel1);
        getContentPane().add("Center", panel2);
        getContentPane().add("South", panel3);
        getContentPane().add("East", panel4);
        
        pack();
        setSize(500,180);
        show();        
    }

    private void emptyTextFields() 
    {
        keyStore.setText("");
        passwdKeyStore.setText("");
        alias.setText("");
        passwdAlias.setText("");
    }
    
    public void actionPerformed(ActionEvent event) 
    {
        String cmd = event.getActionCommand();
        if (cmd.equals("Submit"))
        {
            result.keyStoreLocation = keyStore.getText();
            result.storePassphrase = passwdKeyStore.getText();
            result.alias = alias.getText();
            result.password = passwdAlias.getText();
            dispose();
        }

        if( cmd.equals("Clear Textfields")) 
        {
            emptyTextFields();
        }

        if( cmd.equals("Search")) 
        {
            Frame f = new Frame();
            FileDialog fd = new FileDialog(f);
            fd.show();
            keyStore.setText(fd.getDirectory()+fd.getFile());
        }
    }

    public void finalize()
    {
        try
        {
                              dispose();
                              getOwner().removeAll();
                              getOwner().removeNotify();
                              getOwner().dispose();
                              super.finalize();
        }
        catch ( Throwable t )
        {}
    }

}
