/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/parser/CEPBaseStreamNode.java /main/4 2011/05/19 15:28:46 hopark Exp $ */

/* Copyright (c) 2005, 2011, Oracle and/or its affiliates. 
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
    skmishra    01/26/09 - adding getQCXML
    parujain    08/13/08 - error offset
    anasrini    02/21/06 - inherit the name field 
    anasrini    12/20/05 - parse tree node for a base stream 
    anasrini    12/20/05 - parse tree node for a base stream 
    anasrini    12/20/05 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/parser/CEPBaseStreamNode.java /main/3 2009/02/19 11:21:29 skmishra Exp $
 *  @author  anasrini
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.parser;

import oracle.cep.util.VisXMLHelper;
import oracle.cep.util.XMLHelper;

/**
 * Parse tree node for a base stream
 */

public class CEPBaseStreamNode extends CEPStreamNode {
  
  /**
   * Constructor
   * @param name the name of the base stream
   */
  public CEPBaseStreamNode(CEPStringTokenNode nameToken) {
    setName(nameToken.getValue());
    setStartOffset(nameToken.getStartOffset());
    setEndOffset(nameToken.getEndOffset());
  }

  /**
   * Constructor for name and alias
   * @param name the name of the base stream
   * @param alias the alias for the stream
   */
  public CEPBaseStreamNode(CEPStringTokenNode nameToken, CEPStringTokenNode aliasToken) {
    setName(nameToken.getValue());
    setAlias(aliasToken.getValue());
    setStartOffset(nameToken.getStartOffset());
    setEndOffset(aliasToken.getEndOffset());
  }

  public String toString()
  {
    if(alias != null)
      return " " + name + " as " + alias;
    else
      return " " + name + " ";
  }
  
  public int toQCXML(StringBuffer queryXml, int operatorID)
      throws UnsupportedOperationException
  {
    int id = operatorID;
    StringBuilder myXmlString = new StringBuilder(30);
    
    myXmlString.append("\n\t" + XMLHelper.buildElement(true, VisXMLHelper.cqlPropertyTag, cqlProperty, null, null));
    if(alias != null)
      myXmlString.append("\n\t" + XMLHelper.buildElement(true, VisXMLHelper.aliasTag, alias, null, null));
    myXmlString.append("\n\t" + XMLHelper.buildElement(true, VisXMLHelper.sourceNameTag, this.name, null, null));
    
    queryXml.append(XMLHelper.buildElement(true, VisXMLHelper.operatorTag, myXmlString.toString().trim(), new String[]{VisXMLHelper.operatorIdAttr, VisXMLHelper.operatorTypeAttr}, new String[]{String.valueOf(operatorID), VisXMLHelper.sourceOperator}));
    ++id;
    return id;
  }
}
