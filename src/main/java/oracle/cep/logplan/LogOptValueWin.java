/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/logplan/LogOptValueWin.java /main/4 2011/10/01 09:28:39 sbishnoi Exp $ */

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
    sbishnoi    09/23/11 - support for slide in value window
    sbishnoi    09/06/11 - support for currenthour and currentperiod based
                           value window
    sbishnoi    02/24/11 - allow value window to work upon relation
    sborah      12/16/08 - handle constants
    parujain    07/01/08 - value based windows
    parujain    07/01/08 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/logplan/LogOptValueWin.java /main/2 2009/02/23 06:47:35 sborah Exp $
 *  @author  parujain
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.logplan;

import oracle.cep.common.ValueWindowType;
import oracle.cep.exceptions.LogicalPlanError;
import oracle.cep.logplan.expr.Expr;
import oracle.cep.logplan.expr.ExprAttr;


/**
 * Value Based Window Logical Operator
 */
public class LogOptValueWin extends LogOpt implements Cloneable
{
  /** Column Attribute whose value will be looked upon in evaluating window*/
  private ExprAttr column;

  /** Flag to check if query has specified value window on element time. Please note that 
   *  this flag will be set only if window is applied on relation because relation doesn't
   *  contain pseudo column so class variable "column" will be null in this case.
   */
  private boolean isWindowOnElementTime;
  
  /** Constant value expression which will specify the window size*/
  private Expr constVal;
  
  /** type of value window */
  private ValueWindowType type;
  
  /** startTime in CURRENT_PERIOD value window */
  private long currentPeriodStartTime;
  
  /** size of value window; not applicable in case of GENERIC*/
  private long winSize;
  
  /** size of slide window */
  private long slideAmount;

  public LogOptValueWin()
  {
    super();
  }
  
  /**
   * Constructor
   * @param input
   * @param col
   * @param val
   */
  public LogOptValueWin(LogOpt input, ExprAttr col, Expr val)
  {
    super(LogOptKind.LO_VALUE_WIN);
    assert input != null;
    
    copy(input);
    
    setNumInputs(1);
    setInput(0, input);
    setIsStream(false);
    
    this.column = col;
    this.constVal = val;
    
    input.setOutput(this);
    // By Default, we construct GEENERIC value window
    type = ValueWindowType.GENERIC;
  }
  
  public ExprAttr getColumn()
  {
    return this.column;
  }
  
  public Expr getConstVal()
  {
    return this.constVal;
  }
  
  public boolean isWindowOnElementTime()
  {
    return isWindowOnElementTime;
  }

  public void setWindowOnElementTime(boolean isWindowOnElementTime)
  {
    this.isWindowOnElementTime = isWindowOnElementTime;
  }

  /**
   * @return the type
   */
  public final ValueWindowType getType()
  {
    return type;
  }

  /**
   * @param type the type to set
   */
  public final void setType(ValueWindowType type)
  {
    this.type = type;
  }

  /**
   * @return the currentPeriodStartTime
   */
  public final long getCurrentPeriodStartTime()
  {
    return currentPeriodStartTime;
  }

  /**
   * @param currentPeriodStartTime the currentPeriodStartTime to set
   */
  public final void setCurrentPeriodStartTime(long currentPeriodStartTime)
  {
    this.currentPeriodStartTime = currentPeriodStartTime;
  }

  /**
   * @return the winSize
   */
  public final long getWinSize()
  {
    return winSize;
  }

  /**
   * @param winSize the winSize to set
   */
  public final void setWinSize(long winSize)
  {
    this.winSize = winSize;
  }

  /**
   * @return the slideAmount
   */
  public long getSlideAmount()
  {
    return slideAmount;
  }

  /**
   * @param slideAmount the slideAmount to set
   * @throws LogicalPlanException 
   */
  public void setSlideAmount(long slideAmount) throws LogicalPlanException
  {
    this.slideAmount = slideAmount;
    assert this.getNumInputs() == 1;
    LogOpt inp = this.getInput(0);
    if(this.slideAmount > 1 && inp.getIsStream())
    {
      throw new LogicalPlanException(LogicalPlanError.INVALID_SLIDE_USAGE);
    }
  }

  public void updateSchemaStreamCross()
  {
    numOutAttrs = 0;
    for (int i = 0; i < getNumInputs(); i++)
    {
      LogOpt inp = getInputs().get(i);
      assert (inp != null);

      for (int a = 0; a < inp.getNumOutAttrs(); a++)
      {
        setOutAttr(numOutAttrs, inp.getOutAttrs().get(a));
        numOutAttrs++;
      }
    }
  }
  
  public LogOptValueWin clone() throws CloneNotSupportedException
  {
    LogOptValueWin op = (LogOptValueWin)super.clone();
    op.column = this.column.clone();
    if(this.constVal != null)
      op.constVal = this.constVal.clone();
    op.currentPeriodStartTime = this.currentPeriodStartTime;
    op.winSize = this.winSize;
    op.type    = this.type;
    op.slideAmount = this.slideAmount;
    return op;
  }
  
//toString method override
  public String toString()
  {

    StringBuilder sb = new StringBuilder();

    sb.append("<ValueWindowLogicalOperator>");

    // Dump the common fields
    sb.append(super.toString());

    sb.append("<Column column=\"" + (isWindowOnElementTime ? "ELEMENT_TIME" : column.toString()) + "\" />");
    
    if(this.constVal != null)
      sb.append("<ConstValue value=\"" + constVal.toString() + "\" />");
    
    sb.append("<ValueWindowType typet=\"" + type.toString() + "\" />");

    sb.append("</ValueWindowLogicalOperator>");

    return sb.toString();
  }

}
