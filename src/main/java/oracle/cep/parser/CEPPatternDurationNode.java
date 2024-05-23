/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/parser/CEPPatternDurationNode.java /main/5 2011/05/19 15:28:46 hopark Exp $ */

/* Copyright (c) 2008, 2011, Oracle and/or its affiliates. 
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
    sbishnoi    05/09/11 - Using TimeSpec node for both constant and variable
                           time duration
    skmishra    02/05/09 - override toString
    udeshmuk    02/02/09 - support for arith_expr in duration clause
    udeshmuk    09/06/08 - add offsets
    udeshmuk    07/12/08 - 
    rkomurav    07/04/08 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/parser/CEPPatternDurationNode.java /main/3 2009/02/19 11:21:29 skmishra Exp $
 *  @author  rkomurav
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.parser;

import oracle.cep.common.TimeUnit;

import oracle.cep.util.VisXMLHelper;
import oracle.cep.util.XMLHelper;

public class CEPPatternDurationNode implements CEPParseTreeNode
{
  /** Timespec for duration subclause */
  protected CEPTimeSpecNode duration;
  
  /** If the duration is recurring */
  protected boolean         multiples;
  
  /** duration expression */
  protected CEPExprNode     durationExpr;
  
  /** TimeUnit for the duration expression */
  protected TimeUnit        durationUnit;
  
  /** true if duration is expression */
  protected boolean         isExpr;

  private int startOffset = 0;
 
  private int endOffset = 0;
  
  public CEPPatternDurationNode(CEPTimeSpecNode duration, boolean multiples)
  {
    this.duration     = duration;
    this.multiples    = multiples;
    this.isExpr       = duration.isVariableDuration();
    this.durationExpr = duration.getAmountExpr();
    this.durationUnit = duration.getTimeUnit();
  }
  
  public CEPPatternDurationNode(CEPTimeSpecNode duration)
  {
    this.duration     = duration;
    this.multiples    = false;
    this.isExpr       = duration.isVariableDuration();
    this.durationExpr = duration.getAmountExpr();
    this.durationUnit = duration.getTimeUnit();
  }
  
  public CEPPatternDurationNode(CEPExprNode durationExpr, TimeUnit unit)
  {
    this.durationExpr = durationExpr;
    this.duration     = null;
    this.multiples    = false;
    this.isExpr       = true;
    this.durationUnit = unit;
  }
  
  /**
   * @return the duration
   */
  public CEPTimeSpecNode getDuration()
  {
    return duration;
  }

  /**
   * @return the multiples
   */
  public boolean isMultiples()
  {
    return multiples;
  }

  /**
   * @return the duration clause expression
   */
  public CEPExprNode getDurationExpr()
  {
    return durationExpr;
  }
  
  /**
   * @return time unit of duration clause expression
   */
  public TimeUnit getDurationUnit()
  {
    return durationUnit;
  }
  
  /**
   * @return true if the duration clause has expression
   */
  public boolean isExpr()
  {
    return isExpr;
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
  
  public String toVisualizerXml()
  {
    StringBuilder myXml = new StringBuilder(30);
    myXml.append("\n\t\t" + XMLHelper.buildElement(true, VisXMLHelper.durationTag, duration.toString(), null, null));
    myXml.append("\n\t\t" + XMLHelper.buildElement(true, VisXMLHelper.multipleDurationTag, String.valueOf(multiples), null, null));
    return myXml.toString().trim();
  }
  
  public String toString()
  {
    if(!isExpr())
    {
      if(multiples)
        return " DURATION MULTIPLES OF " + duration.toString();
      else
        return " DURATION " + duration.toString();
    }
    
    else
    {
      if(multiples)
        return " DURATION MULTIPLES OF " + durationExpr.toString() + " " + durationUnit.toString();
      else
        return " DURATION " + durationExpr.toString() + durationUnit.toString();
    }
  }
  
}
