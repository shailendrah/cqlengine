/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/parser/CEPReplaySpecNode.java /main/1 2012/02/02 19:27:26 udeshmuk Exp $ */

/* Copyright (c) 2011, 2012, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    udeshmuk    12/27/11 - Creation
 */

/**
 *  @version $Header: CEPReplaySpecNode.java 27-dec-2011.02:06:45 udeshmuk Exp $
 *  @author  udeshmuk
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.parser;

public class CEPReplaySpecNode
{
  private boolean isRange;

  private int numRows;

  private CEPTimeSpecNode timeRange;

  public CEPReplaySpecNode(CEPTimeSpecNode tRange)
  {
    this.isRange = true;
    this.numRows = -1;
    this.timeRange = tRange;
  }

  public CEPReplaySpecNode(CEPIntTokenNode num)
  {
    this.isRange = false;
    this.numRows = num.getValue();
    this.timeRange = null;
  }

  public boolean isReplayRange()
  {
    return isRange;
  }

  public int getNumRows()
  {
    return numRows;
  }
  
  public CEPTimeSpecNode getRange()
  {
    return timeRange;
  }
  
}
