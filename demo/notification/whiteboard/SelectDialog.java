package demo.notification.whiteboard;

import java.util.Enumeration;
import java.awt.Frame;

/**
 * @author
 * @version
 */

public class SelectDialog extends java.awt.Dialog {

    /** Initializes the Form */
    public SelectDialog(Frame parentFrame, WorkgroupController controller) {
        super (parentFrame, "Select a Whiteboard");
        initComponents ();
	controller_ = controller;
        pack ();
        refresh();
    }

    private void initComponents () {
        addWindowListener (new java.awt.event.WindowAdapter () {
                               public void windowClosing (java.awt.event.WindowEvent evt) {
                                   closeDialog (evt);
                               }
                           }
                          );
        setLayout (new java.awt.BorderLayout ());

        panel1 = new java.awt.Panel ();
        panel1.setName ("buttonPanel");
        panel1.setLayout (new java.awt.FlowLayout ());

        joinButton = new java.awt.Button ();
        joinButton.setLabel ("Join");
        joinButton.setName ("joinButton");
        joinButton.addActionListener (new java.awt.event.ActionListener () {
                                          public void actionPerformed (java.awt.event.ActionEvent evt) {
                                              joinButtonActionPerformed (evt);
                                          }
                                      }
                                     );
        panel1.add (joinButton);

        refreshButton = new java.awt.Button ();
        refreshButton.setLabel ("Refresh");
        refreshButton.setName ("refreshButton");
        refreshButton.addActionListener (new java.awt.event.ActionListener () {
                                             public void actionPerformed (java.awt.event.ActionEvent evt) {
                                                 refreshButtonActionPerformed (evt);
                                             }
                                         }
                                        );
        panel1.add (refreshButton);

        add (panel1, "South");

        availableList = new java.awt.List ();
        availableList.addActionListener( new java.awt.event.ActionListener() {
                                             public void actionPerformed(java.awt.event.ActionEvent e) {
                                                 selectionTField.setText(availableList.getSelectedItem() );
                                             }
                                         }
                                       );
        availableList.setName ("availableList");
        add (availableList, "Center");

        selectionTField = new java.awt.TextField ();
        add (selectionTField, "North");

    }

    private void refreshButtonActionPerformed (java.awt.event.ActionEvent evt) {
        refresh();
    }

    private void joinButtonActionPerformed (java.awt.event.ActionEvent evt) {
        String result = selectionTField.getText();
        if ( result.length() > 0 ) {
            controller_.selectWhiteboard(result);
            close();
        }
    }

    void refresh() {
        availableList.removeAll();
        String[] _list = controller_.getListOfWhiteboards();
	for (int x=0; x<_list.length; ++x) {
	    availableList.add(_list[x]);
	}
    }

    /** Closes the dialog */
    private void closeDialog(java.awt.event.WindowEvent evt) {
        close();
    }

    void close() {
        setVisible (false);
        dispose ();
    }

    private java.awt.Panel panel1;
    private java.awt.Button joinButton;
    private java.awt.Button refreshButton;
    private java.awt.List availableList;
    private java.awt.TextField selectionTField;
    private WorkgroupController controller_;
}
