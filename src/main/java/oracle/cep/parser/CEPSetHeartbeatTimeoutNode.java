/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/parser/CEPSetHeartbeatTimeoutNode.java /main/4 2011/05/19 15:28:46 hopark Exp $ */

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
    sbishnoi    03/15/11 - check if the timespec is specified as constant
    parujain    08/11/08 - error offset
    sbishnoi    12/26/07 - 
    udeshmuk    12/19/07 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/src/oracle/cep/parser/CEPSetHeartbeatTimeoutNode.java /main/2 2008/08/25 19:27:24 parujain Exp $
 *  @author  udeshmuk
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.parser;

import oracle.cep.exceptions.CEPException;
import oracle.cep.exceptions.ParserError;

/**
 * Parse tree node corresponding to set heartbeat timeout alter statement
 * @author udeshmuk
 */

public class CEPSetHeartbeatTimeoutNode implements CEPParseTreeNode{
  
  /** name of stream/relation */
  protected String  name;
  
  /** is this is a stream or a relation */
  protected boolean isStream;
  
  /** timeout duration specification for the heartbeat */
  protected CEPTimeSpecNode timeoutSpec;
  
  protected int startOffset;
  
  protected int endOffset;
  
  /** 
   * Constructor 
   * @param name name of the source
   * @param timeoutSpec CEPTimeSpecNode for the timeout duration specification
   * @param isStream true if source is a stream
   */
  
  public CEPSetHeartbeatTimeoutNode(CEPStringTokenNode nameToken, 
                                    CEPTimeSpecNode timeoutSpec, 
                                    boolean isStream)
    throws CEPException
  {
    // Check if the time specification is a constant
    if(timeoutSpec.isVariableDuration())
    {
      throw new CEPException(ParserError.INVALID_TIME_SPEC,
                             timeoutSpec.getStartOffset(),
                             timeoutSpec.getEndOffset());
    }
    this.name = nameToken.getValue();
    this.isStream = isStream;
    this.timeoutSpec = timeoutSpec;
    setStartOffset(nameToken.getStartOffset());
    setEndOffset(timeoutSpec.getEndOffset());    
  }
  
  public String getName()
  {
    return this.name;  
  }
  
  public boolean getIsStream()
  {
    return this.isStream;
  }
  
  public CEPTimeSpecNode getTimeoutSpec()
  {
    return this.timeoutSpec;
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
