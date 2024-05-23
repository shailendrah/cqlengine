package oracle.cep.driver.view.dialog;

import javax.swing.*;

import java.awt.*;
import java.awt.event.*;


public class BindSrcDest extends JDialog
implements ActionListener {
/// The frame who owns us
  JFrame owner;
  
  /// The text field where the user enters name of stream/relation/query
  private JTextField nameTextField;
  
  /// Text Area to get the path
  private JTextArea pathTextArea;
  
  /// Text field to get the scheme
  private JTextField schemeTextField;
  
  /// Button to Bind
  private JButton bindButton;
  
  /// Button to cancel bind
  private JButton cancelButton;

  /// Name entered by the user
  String name;
  
  /// Scheme name
  String scheme;
  
  /// Path name
  String path;
  
  /// Type
  int type=0;

  /// Message to be displayed when stream name is empty and user
  /// hits "Bind"
  private static final String emptyNameMsg = 
    "Empty Name";
  
  private static final String emptyPathMsg =
    "Empty Path";
  
  private static final String emptySchemeMsg =
    "Empty Scheme";
  
  public BindSrcDest(JFrame owner, int type, String title) {
    
    super (owner, title);
    
    this.type = type;
    this.owner = owner;
    this.name = null;
    this.scheme = null;
    this.path = null;
    
    setModal(true);
    Component mainPane = createMainPane();
    Component centerPane = createCenterPane();
    Component buttonPane = createButtonPane ();
    
    Container contentPane = getContentPane ();
    contentPane.setLayout (new BorderLayout ());
    contentPane.add (mainPane, BorderLayout.NORTH);
    contentPane.add(centerPane, BorderLayout.CENTER);
    contentPane.add (buttonPane, BorderLayout.SOUTH);
    pack();
    
    setLocationRelativeTo (owner);
  }
  
  public String getElemName() {
    return name;
  }
  
  public String getPath() {
    return path;
  }
  
  public String getScheme() {
    return scheme;
  }
  
  public void reset() {
    name = null;
    scheme = null;
    path = null;
    nameTextField.setText("");
    schemeTextField.setText("");
    pathTextArea.setText("");
  }
  
  private JPanel createMainPane() {
    JLabel txtFieldTitle;
    
    if(type == 1)
      txtFieldTitle = new JLabel("Stream Name:");
    else if(type == 2)
      txtFieldTitle = new JLabel("Relation Name:");
    else if(type == 3)
      txtFieldTitle = new JLabel("Query Name:");
    else
      return null;
    
    txtFieldTitle.setFont(txtFieldTitle.getFont().deriveFont(Font.BOLD));
    
    JLabel schemeTitle;
    schemeTitle = new JLabel("Scheme:");
    schemeTitle.setFont(schemeTitle.getFont().deriveFont(Font.BOLD));
    
    schemeTextField = new JTextField(10);
    nameTextField = new JTextField(10);
    JPanel pane = new JPanel(new GridLayout(2,4));
    pane.add(txtFieldTitle);
    pane.add(nameTextField);
    pane.add(schemeTitle);
    pane.add(schemeTextField);
    return pane;
  }
  
  private JPanel createButtonPane() {
    bindButton = new JButton ("Bind");
    cancelButton = new JButton ("Cancel");
    
    getRootPane().setDefaultButton (bindButton);
    bindButton.addActionListener (this);
    cancelButton.addActionListener (this);
    
    JPanel pane = new JPanel ();
    pane.add (bindButton);
    pane.add (cancelButton);
    
    return pane;
  }
  
  private JPanel createCenterPane() {
    
    JLabel txtAreaTitle;
    txtAreaTitle = new JLabel("Path:");
    txtAreaTitle.setFont(txtAreaTitle.getFont().deriveFont(Font.BOLD));
    
    pathTextArea = new JTextArea(3,10);
    pathTextArea.setWrapStyleWord(true);
    JScrollPane scroll = new JScrollPane(pathTextArea);
    scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
    JPanel pane = new JPanel(new GridLayout(2,0));
    pane.add(txtAreaTitle);
    pane.add(scroll);
    return pane;
  }
  
  public void actionPerformed (ActionEvent e) {
    Object source = e.getSource ();
    
    if (source == bindButton) {
        // If we succeed in getting query details we can make
        // ourselves invisible
        if (getBindDetails())
        setVisible (false);
    }
    
    else if (source == cancelButton) {
        name = null;
        path = null;
        scheme = null;
        setVisible (false);
    }
  }
  
  private boolean getBindDetails() {
    name = nameTextField.getText().trim();
    path = pathTextArea.getText().trim();
    scheme = schemeTextField.getText().trim();
    
    if(name.length() == 0) {
      JOptionPane.showMessageDialog(owner, emptyNameMsg, "Error",
          JOptionPane.ERROR_MESSAGE);
      name = null;
      return false;
    }
    if(path.length() == 0) {
      JOptionPane.showMessageDialog(owner, emptyPathMsg, "Error",
          JOptionPane.ERROR_MESSAGE);
      path = null;
      return false;
    }
    if(scheme.length() == 0) {
      JOptionPane.showMessageDialog(owner, emptySchemeMsg, "Error",
          JOptionPane.ERROR_MESSAGE);
      scheme = null;
      return false;
    }
    return true;
  }
}
