/* $Header: cep/wlevs_cql/modules/cqlengine/common/src/oracle/cep/parser/CEPStringTokenNode.java /main/3 2009/08/31 10:57:24 alealves Exp $ */

/* Copyright (c) 2008, 2009, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    skmishra    04/08/09 - adding isSingleQuote
    parujain    08/12/08 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/common/src/oracle/cep/parser/CEPStringTokenNode.java /main/3 2009/08/31 10:57:24 alealves Exp $
 *  @author  parujain
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.parser;

public class CEPStringTokenNode implements CEPParseTreeNode {

  /** The constant value */
  private String value;

  private int startOffset;

  private int endOffset;
  
  private boolean isSingleQuote;
  
  private boolean isLink;

  /**
   * Constructor
   * @param value the constant value
   */
  public CEPStringTokenNode(String value) {
    this.value = value;
    this.startOffset = 0;
    this.endOffset = 0;
  }

  
  /**
   * @return the isSingleQuote
   */
  public boolean isSingleQuote()
  {
    return isSingleQuote;
  }


  /**
   * @param isSingleQuote the isSingleQuote to set
   */
  public void setSingleQuote(boolean isSingleQuote)
  {
    this.isSingleQuote = isSingleQuote;
  }
  
  /**
   * Get the String value
   * @return the string constant value
   */
  public String getValue() {
    return value;
  }
  
  public String toString()
  {
    if(isSingleQuote)
      return "'" + value + "'";
    else
      return "\"" + value + "\"";
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

  public boolean isLink()
  {
    return isLink;
  }

  public void setIsLink(boolean isLink)
  {
    this.isLink = isLink;
  }
}
