/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/parser/CEPExternalRelationNode.java /main/2 2011/05/19 15:28:46 hopark Exp $ */

/* Copyright (c) 2010, 2011, Oracle and/or its affiliates. 
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
    sborah      06/23/10 - set max rows in external relations
    sborah      06/23/10 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/parser/CEPExternalRelationNode.java /main/1 2010/07/19 02:36:41 sborah Exp $
 *  @author  sborah  
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.parser;

/**
 * Parse tree node corresponding to set max rows for external relations
 * @author sborah
 */

public class CEPExternalRelationNode implements CEPParseTreeNode{
  
  /** name of relation */
  protected String  name;
  
  /** maximum rows allowed to be fetched from the external relation */
  protected long    maxRows;
  
  protected int     startOffset;
  
  protected int     endOffset;
  
  public CEPExternalRelationNode(CEPStringTokenNode nameToken, 
                                 CEPBigIntTokenNode maxRowsToken)
  {
    this.name    = nameToken.getValue();
    this.maxRows = maxRowsToken.getValue();
    
    setStartOffset(nameToken.getStartOffset());
    setEndOffset(maxRowsToken.getEndOffset());
  }
  
  /**
   * Sets start offset corresponding to ddl
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
   * Sets the End Offset corresponding to DDL
   */
  public void setEndOffset(int end)
  {
    this.endOffset = end;
  }
  
  /**
   * Gets the end offset
   */
  public int getEndOffset()
  {
    return this.endOffset;
  }

  /**
   * Gets the name of the external relation
   */
  public String getName()
  {
    return name;
  }

  /**
   * Gets the maximum rows to be fetched from the external relation
   */
  public long getMaxRows()
  {
    return maxRows;
  }


}