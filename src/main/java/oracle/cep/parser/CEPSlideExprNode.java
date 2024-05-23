/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/parser/CEPSlideExprNode.java /main/1 2012/06/07 03:24:36 sbishnoi Exp $ */

/* Copyright (c) 2012, Oracle and/or its affiliates. All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    sbishnoi    05/16/12 - Creation
 */

package oracle.cep.parser;

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/parser/CEPSlideExprNode.java /main/1 2012/06/07 03:24:36 sbishnoi Exp $
 *  @author  sbishnoi
 *  @since   release specific (what release of product did this appear in)
 */

public class CEPSlideExprNode implements CEPParseTreeNode
{
  /** slide value (specified in time format) */
  private CEPTimeSpecNode constTimeSpec;
  
  /** slide vale (specified in nanos) */
  private long constTimeVal; 

  /** Start and End Offset of the SLIDE sub-clause */
  private int startOffset;  
  private int endOffset;
  
  /** flag to specify whether the timestamp is numeric or timeformat*/
  private boolean isNumericTsSpecification;
  
  /**
   * Constructor for slide expression
   * @param ts timestamp value
   */
  public CEPSlideExprNode(CEPTimeSpecNode ts)
  {
    constTimeSpec = ts;
    isNumericTsSpecification = false;
  }
  
  /**
   * Constructor for slide expression
   * @param tsVal timestamp value
   */
  public CEPSlideExprNode(long tsVal)
  {
    constTimeVal = tsVal;
    isNumericTsSpecification = true;
  }
  
  /**
   * @return the constTimeSpec
   */
  public CEPTimeSpecNode getConstTimeSpec()
  {
    return constTimeSpec;
  }

  /**
   * @return the constTimeVal
   */
  public long getConstTimeVal()
  {
    return constTimeVal;
  }

  /**
   * @return the isNumericTsSpecification
   */
  public boolean isNumericTsSpecification()
  {
    return isNumericTsSpecification;
  }

  @Override
  public void setStartOffset(int start)
  {
    this.startOffset = start;
  }

  @Override
  public void setEndOffset(int end)
  {
    this.endOffset = end;
  }

  @Override
  public int getStartOffset()
  {
    return this.startOffset;
  }

  @Override
  public int getEndOffset()
  {
    return this.endOffset;
  }
  
}