/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/parser/CEPViewDropNode.java /main/3 2011/05/19 15:28:46 hopark Exp $ */

/* Copyright (c) 2006, 2011, Oracle and/or its affiliates. 
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
    dlenkov     09/13/06 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/src/oracle/cep/parser/CEPViewDropNode.java /main/2 2008/08/25 19:27:24 parujain Exp $
 *  @author  dlenkov 
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.parser;

/**
 * Parse tree node corresponding to a DDL to drop a view
 *
 * @since 1.0
 */
public class CEPViewDropNode implements CEPParseTreeNode {

  /** The name of the view */
  protected String name;

  protected int startOffset;
  
  protected int endOffset;
  
  /**
   * Constructor 
   *
   * @param name of the view
   */
  public CEPViewDropNode( CEPStringTokenNode nameToken) {
    this.name     = nameToken.getValue();
    setStartOffset(nameToken.getStartOffset());
    setEndOffset(nameToken.getEndOffset());
  }

  /**
   * @return Returns the name.
   */
  public String getName()
  {
    return name;
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
