/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/parser/CEPPatternWithinNode.java /main/3 2011/05/19 15:28:46 hopark Exp $ */

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
    sbishnoi    03/15/11 - check if the timespec is specified as constant
    udeshmuk    03/05/10 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/parser/CEPPatternWithinNode.java /main/1 2010/03/23 01:50:14 udeshmuk Exp $
 *  @author  udeshmuk
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.parser;

import oracle.cep.exceptions.CEPException;
import oracle.cep.exceptions.ParserError;

public class CEPPatternWithinNode implements CEPParseTreeNode
{
  protected CEPTimeSpecNode withinDuration;
  
  protected boolean isInclusive;
  
  private int startOffset = 0;
  
  private int endOffset = 0;

  public CEPPatternWithinNode(CEPTimeSpecNode withinDuration)
    throws CEPException
  {
    if(withinDuration.isVariableDuration())
    {
      throw new CEPException(ParserError.INVALID_TIME_SPEC,
                             withinDuration.getStartOffset(), 
                             withinDuration.getEndOffset());
    }
    
    this.withinDuration = withinDuration;
    this.isInclusive    = false;
  }
  
  public CEPPatternWithinNode(CEPTimeSpecNode withinDuration,
                              boolean isInclusive)
    throws CEPException
  {
    if(withinDuration.isVariableDuration())
    {
      throw new CEPException(ParserError.INVALID_TIME_SPEC,
                             withinDuration.getStartOffset(),
                             withinDuration.getEndOffset());
    }
    
    this.withinDuration = withinDuration;
    this.isInclusive    = isInclusive;
  }
  
  public CEPTimeSpecNode getWithinDuration()
  {
    return this.withinDuration;
  }
  
  public boolean getIsInclusive()
  {
    return this.isInclusive;
  }
  
  @Override
  public int getEndOffset()
  {
    return endOffset;
  }

  @Override
  public int getStartOffset()
  {
    return startOffset;
  }

  @Override
  public void setEndOffset(int end)
  {
    this.endOffset = end;    
  }

  @Override
  public void setStartOffset(int start)
  {
    this.startOffset = start;  
  }
  
  public String toString()
  {
    if(isInclusive)
      return " WITHIN INCLUSIVE " + withinDuration.toString();
    else
      return " WITHIN " + withinDuration.toString();
  }
}
