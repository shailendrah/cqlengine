/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/parser/CEPPatternDefinitionNode.java /main/5 2011/05/19 15:28:46 hopark Exp $ */

/* Copyright (c) 2007, 2011, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    Parse tree node corresponding to the DEFINE sub clause of the pattern
    recognition clause

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    hopark      04/21/11 - make public to be reused in cqservice
    skmishra    01/30/09 - adding toVisXML
    parujain    08/11/08 - error offset
    sbishnoi    06/07/07 - fix xlint warning
    anasrini    01/09/07 - Creation
    anasrini    01/09/07 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/parser/CEPPatternDefinitionNode.java /main/4 2009/02/19 11:21:29 skmishra Exp $
 *  @author  anasrini
 *  @since   1.0
 */

package oracle.cep.parser;

import java.util.List;

import oracle.cep.util.VisXMLHelper;
import oracle.cep.util.XMLHelper;

/**
 * Parse tree node corresponding to the DEFINE sub clause
 *
 * @since 1.0
 */

public class CEPPatternDefinitionNode implements CEPParseTreeNode {
  
  /** Array of correlation name definitions **/
  protected CEPCorrNameDefNode[] definitions;
  
  protected int startOffset = 0;
  
  protected int endOffset = 0;

  /**
   * Constructor
   * @param definitions list of correlation name definitions
   */
  public CEPPatternDefinitionNode(List<CEPCorrNameDefNode> definitions) {
    this.definitions = 
      (CEPCorrNameDefNode[])(definitions.toArray(new CEPCorrNameDefNode[0]));
    if(!definitions.isEmpty())
    {
      setStartOffset(definitions.get(0).getStartOffset());
      setEndOffset(definitions.get(definitions.size()-1).getEndOffset());
    }
  }

  // getter methods

  /**
   * Get the array of correlation name definitions
   * @return the array of correlation name definitions
   */
  public CEPCorrNameDefNode[] getCorrNameDefinitions() {
    return definitions;
  }
  
  /**
   * Sets startoffset corresponding to ddl
   */
  public void setStartOffset(int start)
  {
    this.startOffset = start;
  }
  
  /**
   * Gets the start offset
   */
  public int getStartOffset()
  {
    return this.startOffset;
  }
  
  /**
   * Sets the EndOffset corresponding to DDL
   */
  public void setEndOffset(int end)
  {
    this.endOffset = end;
  }
  
  /**
   * Gets the endoffset
   */
  public int getEndOffset()
  {
    return this.endOffset;
  }
  
  public String toString()
  {
    StringBuilder myString = new StringBuilder(50);
    myString.append(" DEFINE ");
    for(CEPCorrNameDefNode c : definitions)
    {
      myString.append(c.toString() + ",");
    }
    myString.deleteCharAt(myString.length() - 1);
    return myString.toString();
  }

  /**
   * @return
   */
  public String toVisualizerXML()
  {
    StringBuilder myXmlString = new StringBuilder(25);
    for(CEPCorrNameDefNode c: definitions)
    {
      myXmlString.append(XMLHelper.buildElement(true, VisXMLHelper.defineAttrTag, c.toString().trim(), null, null));
    }
    String defXml = XMLHelper.buildElement(true, VisXMLHelper.defineListTag, myXmlString.toString().trim(), null, null);
    return defXml;
  }
}



