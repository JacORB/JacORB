package org.jacorb.orb.domain.gui;

import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.JFrame;
import javax.swing.JTextArea;
/**
 * This class implements a scrollable text pane. The text is not editable. It
 * can be set via the get/set functions.
 * @author Herbert Kiefer
 * @version 1.0
 */
public class ScrollableTextPane extends JScrollPane
{
  private JTextPane _textPane;

  public ScrollableTextPane(String text)
  {
    super();
    _textPane= new JTextPane();
    _textPane.setText(text);
    _textPane.setEditable(false);
    this.setViewportView(_textPane);
  }

  public void setText(String text)
  { _textPane.setText(text); }

  public String getText() { return _textPane.getText(); }
}
