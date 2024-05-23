/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/parser/CEPSystemRunNode.java /main/4 2011/05/19 15:28:46 hopark Exp $ */

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
    parujain    08/11/08 - error offset
    parujain    02/09/07 - fix system ddls
    dlenkov     01/12/07 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/src/oracle/cep/parser/CEPSystemRunNode.java /main/3 2008/08/25 19:27:24 parujain Exp $
 *  @author  dlenkov 
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.parser;


/**
 * Parse tree node corresponding to system DDL
 *
 * @since 1.0
 */

public class CEPSystemRunNode implements CEPParseTreeNode {

  /** System running duration */
  protected int duration;
  
  /** Duration initialized */
  protected boolean isInitialized;
  
  protected int startOffset;
  
  protected int endOffset;

  /**
   * Constructor 
   * @param paramList list of parameter values
   */
  public CEPSystemRunNode() {
     isInitialized = false;
     duration = -1;
   }

  public CEPSystemRunNode(CEPConstExprNode node)
  {
    assert node instanceof CEPIntConstExprNode;
    CEPIntConstExprNode icn = (CEPIntConstExprNode)node;
    this.duration = icn.getValue();
    isInitialized = true;
    setStartOffset(icn.getStartOffset());
    setEndOffset(icn.getEndOffset());
  }

  public int getDuration()
  {
    return duration;
  }
  
  public boolean isInitialized()
  {
    return isInitialized;
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

  
}
