/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/parser/CEPOrderByTopExprNode.java /main/3 2011/05/19 15:28:46 hopark Exp $ */

/* Copyright (c) 2009, 2011, Oracle and/or its affiliates. 
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
    sbishnoi    03/05/09 - adding support for partition by
    sbishnoi    02/09/09 - Creation
 */

package oracle.cep.parser;

import java.util.List;

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/parser/CEPOrderByTopExprNode.java /main/2 2009/03/16 08:27:28 sbishnoi Exp $
 *  @author  sbishnoi
 *  @since   release specific (what release of product did this appear in)
 */

public class CEPOrderByTopExprNode implements CEPParseTreeNode
{  
  /** Number of rows in ordered relation  
   * e.g. select c1 from S[range 1 hour] order by c1 asc rows 100
   * So here numRows = 100
   */
  protected int numRows; 
  
  /**
   * Partition by clause for the Partition specification 
   */  
  protected CEPAttrNode[] partByClause = null; 
  
  protected int startOffset = 0;
  
  protected int endOffset = 0;  
  
  /**
   * Constructor
   * @param paramNumRows number of rows in ouput ordered relation
   */
  public CEPOrderByTopExprNode(int paramNumRows)
  {
    numRows = paramNumRows;
  }
   
  /**
   * Constructor
   * @param paramNumRowsNode number of rows in top output relation
   * @param paramStartOffset
   * @param paramEndOffset
   */
  public CEPOrderByTopExprNode(CEPIntTokenNode paramNumRowsNode, 
                                  int paramStartOffset, 
                                  int paramEndOffset)
  {
    numRows     = paramNumRowsNode.getValue();
    startOffset = paramStartOffset;
    endOffset   = paramEndOffset;
  }
  
  /**
   * Constructor
   * @param paramNumRowsNode number of rows in top output relation
   * @param partitionByClause list of partition by attributes
   * @param paramStartOffset
   * @param paramEndOffset
   */
  public CEPOrderByTopExprNode(CEPIntTokenNode paramNumRowsNode,
                               List<CEPAttrNode> partitionByClause,
                               int paramStartOffset,
                               int paramEndOffset)
  {
    this(paramNumRowsNode, paramStartOffset, paramEndOffset);    
    if (partitionByClause != null)
    {
      int numAttrs = partitionByClause.size();    
      this.partByClause 
       = (CEPAttrNode[])(partitionByClause.toArray(new CEPAttrNode[numAttrs])); 
    }
  }
  
  /**
   * Get number of top rows in output relation 
   * @return number of rows
   */
  public int getNumRows()
  {
    return numRows;
  }
  
  /**
   * Get array of partition by attributes
   * @return array of CEPAttrNode
   */
  public CEPAttrNode[] getPartitionByClause()
  {
    return partByClause;
  }
  
  /**
   * Check if partition by attributes exist or not
   * @return true if exists
   */
  public boolean isPartitionByClauseExist()
  {
    return partByClause != null; 
  }
  
  public int getStartOffset()
  {
    return startOffset;
  }
  
  public int getEndOffset()
  {
    return endOffset;
  }
  
  public void setStartOffset(int paramStartOffset)
  {
    startOffset = paramStartOffset;
  }
  
  public void setEndOffset(int paramEndOffset)
  {
    endOffset = paramEndOffset;
  }
  
}
