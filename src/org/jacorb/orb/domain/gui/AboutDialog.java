package org.jacorb.orb.domain.gui;

import java.awt.*;
import javax.swing.*;
import java.awt.event.*;

/**
 * A dialog box for displaying program infromation.
 * @author Herbert Kiefer
 * @version 1.0
 */


public class AboutDialog extends JDialog
{
  JPanel MainPanel = new JPanel();
  JPanel ButtonPanel = new JPanel();
  JButton OkButton = new JButton();
  JLabel jLabel1 = new JLabel();
  JLabel jLabel2 = new JLabel();
  JLabel jLabel3 = new JLabel();
  GridLayout gridLayout1 = new GridLayout();

  public AboutDialog(Frame frame, String title, boolean modal)
  {
    super(frame, title, modal);
    try
    {
      jbInit();
      pack();
      setLocationRelativeTo(frame);
    }
    catch(Exception ex)
    {
      ex.printStackTrace();
    }
  }

  public AboutDialog()
  {
    this(null, "", false);
  }

  void jbInit() throws Exception
  {
    MainPanel.setLayout(gridLayout1);
    OkButton.setToolTipText("");
    OkButton.setText("OK");
    OkButton.addActionListener(new java.awt.event.ActionListener()
    {

      public void actionPerformed(ActionEvent e)
      {
        OnOk(e);
      }
    });
    jLabel1.setHorizontalAlignment(SwingConstants.CENTER);
    jLabel1.setText("Domain Browser Version 1.0");
    jLabel2.setHorizontalAlignment(SwingConstants.CENTER);
    jLabel2.setHorizontalTextPosition(SwingConstants.LEADING);
    jLabel2.setText("written by Herb (kiefer@inf.fu-berlin.de)");

    //  java.net.URL imageURL= ClassLoader.getSystemResource("jacorb/orb/domain/gui/Herb.gif");
    //  if (imageURL != null) jLabel2.setIcon( new ImageIcon(imageURL) );


    jLabel3.setHorizontalAlignment(SwingConstants.CENTER);
    jLabel3.setText("Please report bugs and comments to "
		    +"jacorb-developer@lists.spline.inf.fu-berlin.de ");
    gridLayout1.setColumns(1);
    gridLayout1.setRows(0);
    getContentPane().add(MainPanel);
    MainPanel.add(jLabel1, null);
    MainPanel.add(jLabel2, null);
    MainPanel.add(jLabel3, null);
    this.getContentPane().add(ButtonPanel, BorderLayout.SOUTH);
    ButtonPanel.add(OkButton, null);
    ButtonPanel.getRootPane().setDefaultButton(OkButton);
  }

  void OnOk(ActionEvent e)
  {
    // this.hide();
    this.dispose();
  }
}
