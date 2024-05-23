/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/parser/CEPRemoveHeartbeatTimeoutNode.java /main/3 2011/05/19 15:28:46 hopark Exp $ */

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
    sbishnoi    12/26/07 - 
    udeshmuk    12/19/07 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/src/oracle/cep/parser/CEPRemoveHeartbeatTimeoutNode.java /main/2 2008/08/25 19:27:24 parujain Exp $
 *  @author  udeshmuk
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.parser;

/**
 * Parse tree node corresponding to remove heartbeat timeout alter statement
 * @author udeshmuk
 */

public class CEPRemoveHeartbeatTimeoutNode implements CEPParseTreeNode{
  
  /** name of stream/relation */
  protected String  name;
  
  /** is this is a stream or a relation */
  protected boolean isStream;
  
  protected int startOffset;
  
  protected int endOffset;
  
  /** 
   * Constructor 
   * @param name name of the source
   * @param isStream true if source is a stream
   */
  public CEPRemoveHeartbeatTimeoutNode(CEPStringTokenNode nameToken, boolean isStream)
  {
    this.name = nameToken.getValue();
    this.isStream = isStream;
    setStartOffset(nameToken.getStartOffset());
    setEndOffset(nameToken.getEndOffset());
  }
  
  public String getName()
  {
    return this.name;
  }
  
  public boolean getIsStream()
  {
    return this.isStream;
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
