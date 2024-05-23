/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/parser/CEPTimeSpecNode.java /main/6 2012/02/09 17:57:06 alealves Exp $ */

/* Copyright (c) 2005, 2012, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    Parse tree node for a time specification

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    hopark      04/21/11 - make public to be reused in cqservice
    sbishnoi    03/07/11 - supporting variable window
    skmishra    02/05/09 - adding toString
    parujain    08/11/08 - error offset
    anasrini    02/21/06 - timeUnits should be long 
    anasrini    12/22/05 - parse tree node a time specification (amount and 
                           units) 
    anasrini    12/22/05 - parse tree node a time specification (amount and 
                           units) 
    anasrini    12/22/05 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/parser/CEPTimeSpecNode.java /main/3 2009/02/19 11:21:29 skmishra Exp $
 *  @author  anasrini
 *  @since   1.0
 */

package oracle.cep.parser;

import oracle.cep.common.TimeUnit;

/**
 * Parse tree node for a time specification
 *
 * @since 1.0
 */

public class CEPTimeSpecNode implements CEPParseTreeNode {

  public static final int INFINITE = -1;
  
  /** The amount */
  private long amount;
  
  /** The amount expression */
  private CEPExprNode amountExpr;

  /** The units */
  private TimeUnit timeUnit;
  
  private int startOffset = 0;
  
  private int endOffset = 0;
  
  /** flag to check if the time is a variable or constant */
  private boolean isVariableDuration = false;

  /**
   * Constructor
   * @param timeUnit the time unit
   * @param amount the amount
   */
  public CEPTimeSpecNode(TimeUnit timeUnit, CEPIntTokenNode amount) {
    this.amount   = (long)amount.getValue();
    this.timeUnit = timeUnit;
    setStartOffset(amount.getStartOffset());
    setEndOffset(amount.getEndOffset());
  }
  
  /**
   * Constructor
   * @param timeUnit the time unit
   * @param amount the amount
   */
  public CEPTimeSpecNode(TimeUnit timeUnit, CEPBigIntTokenNode amount) {
    this.amount   = (long) amount.getValue();
    this.timeUnit = timeUnit;
    setStartOffset(amount.getStartOffset());
    setEndOffset(amount.getEndOffset());
  }
  
  /**
   * Constructor
   * @param timeUnit unit of the time expression
   * @param amountExpr time expression
   */
  public CEPTimeSpecNode(TimeUnit timeUnit, CEPExprNode amountExpr)
  {
    this.amountExpr = amountExpr;
    this.timeUnit   = timeUnit;
    this.isVariableDuration = true;
    setStartOffset(amountExpr.getStartOffset());
    setEndOffset(amountExpr.getEndOffset());
  }
  

  public CEPTimeSpecNode(TimeUnit timeunit, long amt) {
    this.amount = amt;
    this.timeUnit = timeunit;
  } 

  /**
   * Getter method for the amount
   * @return the amount
   */
  public long getAmount() {
    return amount;
  }

  /**
   * Getter method for the time unit
   * @return the time unit
   */
  public TimeUnit getTimeUnit() {
    return timeUnit;
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

  public String toString()
  {
    if(isVariableDuration)
      return " " + this.amountExpr+ " "+ this.timeUnit.toString() + " ";
    else
      return " " + this.amount + " " +this.timeUnit.toString() + " ";
  }

  public CEPExprNode getAmountExpr()
  {
    return amountExpr;
  }

  public boolean isVariableDuration()
  {
    return isVariableDuration;
  }
}
