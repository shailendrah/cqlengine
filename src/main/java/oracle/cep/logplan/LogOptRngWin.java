/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/logplan/LogOptRngWin.java /main/3 2011/05/09 23:12:07 sbishnoi Exp $ */

/* Copyright (c) 2006, 2011, Oracle and/or its affiliates. 
All rights reserved. */

/*
 DESCRIPTION
 Describes Range Window logical operator in the package oracle.cep.logplan

 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>

 NOTES
 <other useful comments, qualifications, etc.>

 MODIFIED    (MM/DD/YY)
 sbishnoi    03/16/11 - adding rangeExpr to support variable duration range
                        window
 sborah      12/16/08 - handle constants
 rkomurav    08/09/06 - slide and cleanup
 najain      05/25/06 - add updateSchemaStreamCross 
 najain      04/06/06 - cleanup
 najain      02/23/06 - add constructors etc.
 anasrini    02/21/06 - timeUnits should be long 
 najain      02/08/06 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/logplan/LogOptRngWin.java /main/2 2009/02/23 06:47:35 sborah Exp $
 *  @author  najain  
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.logplan;

import oracle.cep.common.TimeUnit;
import oracle.cep.logplan.expr.Expr;
import oracle.cep.semantic.WindowSpec;
import oracle.cep.semantic.TimeWindowSpec;

/**
 * Range Window Logical Operator
 */
public class LogOptRngWin extends LogOpt implements Cloneable
{

  /** range value */
  private long rangeUnits;

  /** slide value*/
  private long slideUnits;
  
  /** Expression which will evaluates to range */
  private Expr rangeExpr;
  
  /** Flag to check if the window is a variable duration window */
  private boolean isVariableDurationWindow;
  
  /** unit of range value */
  private TimeUnit rangeUnit;

  public LogOptRngWin()
  {
    // super(LogOptKind.LO_RANGE_WIN);
    super();
  }

  public LogOptRngWin(LogOpt input, WindowSpec win)
  {
    super(LogOptKind.LO_RANGE_WIN);
    assert input != null;
    assert input.getIsStream() == true;
    assert win instanceof TimeWindowSpec;

    copy(input);

    setNumInputs(1);
    setInput(0, input);
    setIsStream(false);
    
    rangeUnits = ((TimeWindowSpec) win).getRangeUnits();
    slideUnits = ((TimeWindowSpec) win).getSlideUnits();

    input.setOutput(this);
  }

  public void setRangeUnits(long rangeUnits)
  {
    this.rangeUnits = rangeUnits;
  }

  public long getRangeUnits()
  {
    return rangeUnits;
  }

  public void setSlideUnits(long slideUnits)
  {
    this.slideUnits = slideUnits;
  }

  public long getSlideUnits()
  {
    return slideUnits;
  }

  public Expr getRangeExpr()
  {
    return rangeExpr;
  }

  public boolean isVariableDurationWindow()
  {
    return isVariableDurationWindow;
  }

  // clone has been re-implemented to perform a deep copy instead of the
  // default shallow copy
  public LogOptRngWin clone() throws CloneNotSupportedException
  {
    LogOptRngWin op = (LogOptRngWin) super.clone();
    return op;
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

  // toString method override
  public String toString()
  {

    StringBuilder sb = new StringBuilder();

    sb.append("<RangeWindowLogicalOperator>");

    // Dump the common fields
    sb.append(super.toString());

    if(isVariableDurationWindow)
      sb.append("<RangeUnits rangeUnits=\"" + rangeExpr.toString() + "\" />");
    else
      sb.append("<RangeUnits rangeUnits=\"" + rangeUnits + "\" />");

    sb.append("<SlideUnits slideUnits=\"" + slideUnits + "\" />");

    sb.append("</RangeWindowLogicalOperator>");

    return sb.toString();
  }

  public void setRangeExpr(Expr rangeExpr)
  {
    this.rangeExpr = rangeExpr;
    setVariableDurationWindow(true);
  }

  public void setVariableDurationWindow(boolean isVariableDurationWindow)
  {
    this.isVariableDurationWindow = isVariableDurationWindow;
  }

  /**
   * @param rangeUnit the rangeUnit to set
   */
  public void setRangeUnit(TimeUnit rangeUnit)
  {
    this.rangeUnit = rangeUnit;
  }

  /**
   * @return the rangeUnit
   */
  public TimeUnit getRangeUnit()
  {
    return rangeUnit;
  }

}
