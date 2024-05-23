package oracle.cep.driver.view.dialog;

import javax.swing.*;

import java.awt.*;
import java.awt.event.*;

public class StartNamedQuery extends JDialog
implements ActionListener {
  
  /// The frame who owns us
  JFrame owner;
  
  private JTextField queryNameField;
  
  private JButton startButton;
  
  private JButton cancelButton;

  String queryName;
  
  private static final String emptyNameMsg =
    "Empty Query Name";
  
  public StartNamedQuery(JFrame owner) {
    
    super (owner,"Start Named Query");
    
    this.owner = owner;
    this.queryName = null;
    
    setModal(true);
    Component mainPane = createMainPane();
    Component buttonPane = createButtonPane ();
    
    Container contentPane = getContentPane ();
    contentPane.setLayout (new BorderLayout ());
    contentPane.add (mainPane, BorderLayout.NORTH);
    contentPane.add (buttonPane, BorderLayout.SOUTH);
    pack();
    
    setLocationRelativeTo (owner);
  }
  
  public String getQueryName() {
    return queryName;
  }
  
  public void reset() {
    queryName = null;
    queryNameField.setText("");
  }
  
  private JPanel createMainPane() {
    JLabel txtFieldTitle;
    txtFieldTitle = new JLabel("Query Name:");
    
    txtFieldTitle.setFont(txtFieldTitle.getFont().deriveFont(Font.BOLD));
    
    queryNameField = new JTextField(10);
    JPanel pane = new JPanel(new GridLayout(1,2));
    pane.add(txtFieldTitle);
    pane.add(queryNameField);
    return pane;
  }
  
  private JPanel createButtonPane() {
    startButton = new JButton ("Start");
    cancelButton = new JButton ("Cancel");
    
    getRootPane().setDefaultButton (startButton);
    startButton.addActionListener (this);
    cancelButton.addActionListener (this);
    
    JPanel pane = new JPanel ();
    pane.add (startButton);
    pane.add (cancelButton);
    
    return pane;
  }
  
  public void actionPerformed (ActionEvent e) {
    Object source = e.getSource ();
    
    if (source == startButton) {
        // If we succeed in getting query details we can make
        // ourselves invisible
        if (getBindDetails())
        setVisible (false);
    }
    
    else if (source == cancelButton) {
        queryName = null;
        setVisible (false);
    }
  }
  
  private boolean getBindDetails() {
    queryName = queryNameField.getText().trim();
    
    if(queryName.length() == 0) {
      JOptionPane.showMessageDialog(owner, emptyNameMsg, "Error",
          JOptionPane.ERROR_MESSAGE);
      queryName = null;
      return false;
    }
    return true;
  }
}
