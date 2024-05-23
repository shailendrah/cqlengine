/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/parser/CEPBigIntTokenNode.java /main/2 2011/05/19 15:28:46 hopark Exp $ */

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
    sborah      06/23/10 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/parser/CEPBigIntTokenNode.java /main/1 2010/07/19 02:36:41 sborah Exp $
 *  @author  sborah  
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.parser;

public class CEPBigIntTokenNode implements CEPParseTreeNode {

  /** The constant value */
  private long value;

  private int startOffset;

  private int endOffset;

  /**
   * Constructor
   * @param value the constant value
   */
  public CEPBigIntTokenNode(long value) {
    this.value = value;
  }

  /**
   * Get the int value
   * @return the int value
   */
  public long getValue() {
    return value;
  }
  
  public String toString()
  {
    return Long.toString(value);
  }

  public void setStartOffset(int start)
  {
    this.startOffset = start;
  }

  public int getStartOffset()
  {
    return this.startOffset;
  }

  public void setEndOffset(int end)
  {
    this.endOffset = end;
  }

  public int getEndOffset()
  {
    return this.endOffset;
  }
}


