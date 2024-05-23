/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/parser/CEPCorrNameDefNode.java /main/6 2011/05/19 15:28:46 hopark Exp $ */

/* Copyright (c) 2007, 2011, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    Parse tree node for a correlation name definition

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    hopark      04/21/11 - make public to be reused in cqservice
    skmishra    01/30/09 - overriding toString
    parujain    08/11/08 - 
    rkomurav    09/26/07 - support string correlation anems
    anasrini    01/09/07 - Creation
    anasrini    01/09/07 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/parser/CEPCorrNameDefNode.java /main/5 2009/02/23 00:45:57 skmishra Exp $
 *  @author  anasrini
 *  @since   1.0
 */

package oracle.cep.parser;

import oracle.cep.util.XMLHelper;

/**
 * Parse tree node for a correlation name definition
 *
 * @since 1.0
 */

public class CEPCorrNameDefNode implements CEPParseTreeNode {

  /** The correlation name being defined **/
  protected String corrName;

  /** The predicate defining the correlation name **/
  protected CEPBooleanExprNode predicate;
  
  protected int startOffset;
  
  protected int endOffset;

  /**
   * Constructor
   * @param corrName the correlation name being defined
   * @param predicate the predicate defining the correlation name
   */
  public CEPCorrNameDefNode(CEPStringTokenNode corrname, CEPBooleanExprNode predicate) {
    this.corrName  = corrname.getValue();
    this.predicate = predicate;
    setStartOffset(corrname.getStartOffset());
    setEndOffset(predicate.getEndOffset());
  }

  // getter methods
  
  /**
   * Get the name of the correlation variable being defined
   * @return the name of the correlation variable being defined
   */
  public String getCorrName() {
    return corrName;
  }

  /**
   * Get the predicate defining the correlation name
   * @return the predicate defining the correlation name
   */
  public CEPBooleanExprNode getDefinition() {
    return predicate;
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
    String xmlpredicate =  XMLHelper.toHTMLString(predicate.toString());
  //  System.out.println("XMLPREDICATE from CorrNameDef: "  + xmlpredicate);
    return " " + corrName +" AS " + xmlpredicate.trim() + " ";
  }
}

