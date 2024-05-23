/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/parser/CEPExtensibleWindowExprNode.java /main/6 2011/05/19 15:28:46 hopark Exp $ */

/* Copyright (c) 2007, 2011, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    hopark      04/21/11 - make public to be reused in cqservice
    skmishra    02/20/09 - adding toExprXml
    skmishra    01/26/09 - adding toQCXML()
    parujain    08/13/08 - error offset
    sbishnoi    06/07/07 - fix xlint warning
    parujain    03/06/07 - Extensible Windows
    parujain    03/06/07 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/parser/CEPExtensibleWindowExprNode.java /main/5 2009/02/23 00:45:57 skmishra Exp $
 *  @author  parujain
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.parser;

import java.util.ArrayList;
import java.util.List;

import oracle.cep.util.VisXMLHelper;
import oracle.cep.util.XMLHelper;

public class CEPExtensibleWindowExprNode extends CEPWindowExprNode {
  /** The window name */
  protected String name;

  /** The extensible window implementation parameters */
  protected CEPConstExprNode[] params;
  
  /**
   * Constructor for a extensible window with zero parameters
   * @param name name of the window
   */
  public CEPExtensibleWindowExprNode(CEPStringTokenNode name) {
    this(name, new ArrayList<CEPConstExprNode>());
  }
  
  /**
   * Constructor for a extensible window with at least one parameter
   * @param name name of the extensible window
   * @param paramList list of parameters. Each parameter is an 
   *                  expression
   */
  public CEPExtensibleWindowExprNode(CEPStringTokenNode nameToken, List<CEPConstExprNode> paramList) {
    this.name   = nameToken.getValue();
    this.params = (CEPConstExprNode[])paramList.toArray(new CEPConstExprNode[0]);
    setStartOffset(nameToken.getStartOffset());
    if(paramList.isEmpty())
      setEndOffset(nameToken.getEndOffset());
    else
      setEndOffset(paramList.get(paramList.size()-1).getEndOffset());
  }
  
  /**
   * Get the name of the extensible window
   * @return the name of the window
   */
  public String getName() {
    return name;
  }

  /**
   * Get the array of parameters
   * @return the array of parameters
   */
  public CEPConstExprNode[] getParams() {
    return params;
  }

  public String toString()
  {
    StringBuffer myString = new StringBuffer(24);
    myString.append("[" + name + "(");
    for(CEPConstExprNode param:params)
      myString.append(param.toString() + ",");
    myString.deleteCharAt(myString.length() - 1);
    myString.append(")]");
    return myString.toString();
  }
  
  public int toQCXML(StringBuffer xml, int operatorID)
      throws UnsupportedOperationException
  {
    int myID = operatorID;
    int inpID = operatorID - 1;
    StringBuilder myXml = new StringBuilder(30);

    String inputsXml = "\n\t\t" + XMLHelper.buildElement(true, VisXMLHelper.inputTag, String.valueOf(inpID), null, null);
    inputsXml = "\n\t" + XMLHelper.buildElement(true, VisXMLHelper.inputsTag, inputsXml, null, null) + "\n";

    myXml.append(inputsXml);
    myXml.append("\n\t" + XMLHelper.buildElement(true, VisXMLHelper.cqlPropertyTag, cql_property, null, null));
    myXml.append("\nt\t" + XMLHelper.buildElement(true, VisXMLHelper.windowTypeTag, VisXMLHelper.windowTypeExtensible, null, null));

    xml.append(XMLHelper.buildElement(true, VisXMLHelper.operatorTag, myXml.toString().trim(), new String[]{VisXMLHelper.operatorIdAttr, VisXMLHelper.operatorTypeAttr}, new String[]{String.valueOf(myID),VisXMLHelper.windowOperator}));
    ++myID;
    return myID;
  }
}
