/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/parser/CEPWindowExprNode.java /main/7 2011/05/19 15:28:46 hopark Exp $ */

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
    sbishnoi    02/23/11 - adding flag isWindowOnRelationAllowed
    skmishra    02/20/09 - adding toExprXml
    skmishra    01/26/09 - adding toQCXML()
    parujain    08/11/08 - error offset
    anasrini    12/21/05 - parse tree node corresponding to a window 
                           expression 
    anasrini    12/21/05 - parse tree node corresponding to a window 
                           expression 
    anasrini    12/21/05 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/parser/CEPWindowExprNode.java /main/5 2009/02/27 14:19:31 skmishra Exp $
 *  @author  anasrini
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.parser;

/**
 * Parse tree node corresponding to a window expression 
 */

public abstract class CEPWindowExprNode implements CEPParseTreeNode {
	
  protected int startOffset;
  
  protected int endOffset;
	
  String cql_property;
  
  boolean isInfinite = false;
  
  /** a flag to check if the current window should allow relation as an input*/
  protected boolean isWindowOverRelationAllowed = false;
  
  public boolean isInfinite()
  {
    return this.isInfinite;
  }
  
  public void setCqlProperty(String arg)
  {
    this.cql_property = arg;
  }
  
  public String getCqlProperty()
  {
    return cql_property;
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
  
  /**
   * Get the isWindowOverRelationAllowed flag
   * @return true if current window allows relation as an input
   */
  public boolean isWindowOverRelationAllowed()
  {
    return isWindowOverRelationAllowed;
  }

  /**
   * Sets the isWindowOverRelationAllowed flag
   * @param isWindowOverRelationAllowed
   */
  public void setWindowOverRelationAllowed(boolean isWindowOverRelationAllowed)
  {
    this.isWindowOverRelationAllowed = isWindowOverRelationAllowed;
  }

  @Override
  public boolean equals(Object obj)
  {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    CEPWindowExprNode other = (CEPWindowExprNode) obj;
    
    // Simple comparison based on the window clause
    if(!this.toString().equals(other.toString()))
      return false;
    
    return true;
  }

  //used by toQCXML
  public abstract String toString();

  //creates xml for vis. query constructor
  public abstract int toQCXML(StringBuffer qXml, int operatorID) throws UnsupportedOperationException;
}
