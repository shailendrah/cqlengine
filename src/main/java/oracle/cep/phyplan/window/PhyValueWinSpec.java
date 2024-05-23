/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/phyplan/window/PhyValueWinSpec.java /main/3 2011/10/01 09:28:39 sbishnoi Exp $ */

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
    sbishnoi    09/24/11 - support for slide in value window
    sbishnoi    09/06/11 - support for current hour and curernt period
    sbishnoi    08/27/11 - adding support for interval year to month
    parujain    07/07/08 - value based windows
    parujain    07/07/08 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/src/oracle/cep/phyplan/window/PhyValueWinSpec.java /main/1 2008/07/14 22:57:01 parujain Exp $
 *  @author  parujain
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.phyplan.window;

import oracle.cep.common.Datatype;
import oracle.cep.common.ValueWindowType;
import oracle.cep.logplan.LogOpt;
import oracle.cep.phyplan.expr.Expr;
import oracle.cep.phyplan.expr.ExprAttr;
import oracle.cep.phyplan.expr.ExprInt;
import oracle.cep.phyplan.expr.ExprFloat;
import oracle.cep.phyplan.expr.ExprInterval;
import oracle.cep.phyplan.expr.ExprDouble;
import oracle.cep.phyplan.expr.ExprTimestamp;
import oracle.cep.phyplan.expr.ExprBigint;
import oracle.cep.phyplan.factory.LogPlanExprFactory;
import oracle.cep.phyplan.factory.LogPlanExprFactoryContext;

public class PhyValueWinSpec extends PhyWinSpec 
{
  /** Column Attribute whose value will be looked upon in evaluating window*/
  ExprAttr column;
  
  /** Flag to check if query has specified value window on element time. Please note that 
   *  this flag will be set only if window is applied on relation because relation doesn't
   *  contain pseudo column so class variable "column" will be null in this case.
   */
  boolean isWindowOnElementTime;
  
  /** Constant value expression which will specify the window size*/
  Expr constValue;
  
  
  long longValue;
  
  double doubleValue;
  
  boolean isLong;
  
  /** type of value window */
  ValueWindowType type;
  
  /** size of value window; not applicable in case of GENERIC*/
  long winSize;
  
  /** start time in CURRENT_PERIOD value window */
  long currentPeriodStartTime;
  
  /** size of slide; default value is 1 unit*/
  long slideAmount;


  public PhyValueWinSpec()
  {
 
  }
  
  /**
   * Constructor of Value Window Specification
   * @param op
   */
  public PhyValueWinSpec(LogOpt op)
  {
    super();
    
    assert op instanceof oracle.cep.logplan.LogOptValueWin;
    
    assert op != null;
    assert op.getNumInputs() == 1;
    
    oracle.cep.logplan.LogOptValueWin src 
      = (oracle.cep.logplan.LogOptValueWin)op;
    
    // Interpret column attribute
    this.isWindowOnElementTime = src.isWindowOnElementTime();
    
    // src.getColumn() will be null only if isWindowOnElementTime is true and Value window
    // is applied on a relation
    if(src.getColumn() != null)
      column = (ExprAttr) LogPlanExprFactory.getInterpreter(src.getColumn(),
                       new LogPlanExprFactoryContext(src.getColumn(), op));
    
    if(src.getConstVal() != null)
    {
      constValue = LogPlanExprFactory.getInterpreter(src.getConstVal(), 
                new LogPlanExprFactoryContext(src.getConstVal(), op));
      
      if((constValue.getType() == Datatype.INT) 
        ||(constValue.getType() == Datatype.BIGINT)
        ||(constValue.getType() == Datatype.INTERVAL)
        ||(constValue.getType() == Datatype.INTERVALYM))
      {
        this.isLong = true;
        if(constValue.getType() == Datatype.INT)
          this.longValue = (long)(((ExprInt)constValue).getIValue());
        else if(constValue.getType() == Datatype.BIGINT)
          this.longValue = ((ExprBigint)constValue).getLValue();        
        else if(constValue.getType() == Datatype.INTERVAL)
          this.longValue = ((ExprInterval)constValue).getVValue();
        else if(constValue.getType() == Datatype.INTERVALYM)
          this.longValue = ((ExprInterval)constValue).getVValue();
        this.doubleValue = -1;
      }
      else if((constValue.getType() == Datatype.FLOAT)
              ||(constValue.getType() == Datatype.DOUBLE))
      {
        this.isLong = false;
        if(constValue.getType() == Datatype.FLOAT)
        this.doubleValue = (double)(((ExprFloat)constValue).getFValue());
        else if(constValue.getType() == Datatype.DOUBLE)
        this.doubleValue = ((ExprDouble)constValue).getDValue();
        this.longValue = -1;
      }
      else
        assert false;
    }
    
    setType(src.getType());
    
    if(src.getType() != ValueWindowType.GENERIC)
    {
      setWinSize(src.getWinSize());
      setCurrentPeriodStartTime(src.getCurrentPeriodStartTime());
      isLong = true;
    }
    
    // Set the Slide Amount
    this.slideAmount = src.getSlideAmount();
    
    setWindowKind(WinKind.VALUE);
  }
  
  public ExprAttr getColumn()
  { 
    return this.column;
  }
  
  public boolean isWindowOnElementTime()
  {
    return isWindowOnElementTime;
  }
  
  public Expr getConstValue()
  {
    return this.constValue;
  }
  
  public boolean isLong()
  {
    return this.isLong;
  }
  
  public long getLongConstVal()
  {
    assert this.isLong == true;
    return this.longValue;
  }
  
  public double getDoubleConstVal()
  {
    assert this.isLong == false;
    return this.doubleValue;
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
   * @return the slideAmount
   */
  public long getSlideAmount()
  {
    return slideAmount;
  }

  @Override
  public boolean equals(Object other) {
    if(this == other)
      return true;
	      
    if(other == null)
      return false;
	      
    if(getClass() != other.getClass())
      return false;
    
   PhyValueWinSpec ospec = (PhyValueWinSpec)other;
    
   if(this.type != ospec.getType())
     return false;
     
   if(this.isWindowOnElementTime != ospec.isWindowOnElementTime)
     return false;
   
   if(!this.isWindowOnElementTime)
   {
    if(!ospec.column.equals(this.column))
      return false;
   }
   
    if((constValue == null && ospec.constValue != null) ||
        (constValue != null && ospec.constValue == null))
      return false;
    
    if(this.winSize != ospec.getWinSize())
      return false;
    
    if(this.slideAmount != ospec.getSlideAmount())
      return false;
    
    if(this.currentPeriodStartTime != ospec.getCurrentPeriodStartTime())
      return false;
    
    return((constValue == null && ospec.constValue == null) ||
        (ospec.constValue.equals(this.constValue)));
  }

  @Override
  public String getXMLPlan2() {
    StringBuilder xml = new StringBuilder();
    xml.append("<name> ValueWin </name>\n");
    xml.append("<lname> Value Based Window </lname>\n");
    xml.append("<type> " + type + "</type>\n");
    if(constValue != null)
      xml.append(constValue.getXMLPlan2());
    xml.append(isWindowOnElementTime ? "<attr>ELEMENT_TIME</attr>\n" :column.getXMLPlan2());
    return xml.toString();
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("<PhysicalValueWindowSpecs>");
	  if(constValue != null)
      sb.append("<Value value=\"" +constValue.toString() +"\" />");
    sb.append("<AttrExpr>");
    sb.append("<Value value=\"" + (isWindowOnElementTime ? "ELEMENT_TIME" : column.getActualName()) +"\" />");    
    sb.append("</AttrExpr>");
    sb.append("<Type value=\"" + type +"\" />");

    sb.append("</PhysicalValueWindowSpecs>");
    return sb.toString();
  }

}
