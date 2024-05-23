/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/logplan/LogOptPrtnWin.java /main/9 2012/05/02 03:05:56 pkali Exp $ */

/* Copyright (c) 2006, 2012, Oracle and/or its affiliates. 
All rights reserved. */

/*
 DESCRIPTION
 Partition Window Logical Operator

 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>

 NOTES
 <other useful comments, qualifications, etc.>

 MODIFIED    (MM/DD/YY)
 pkali       04/04/12 - included datatype arg in AttrNamed instance
 sbishnoi    12/04/11 - support of variable duration partition window
 sborah      04/11/11 - use getAllReferencedAttrs()
 sborah      03/28/11 - override canPartitionExprBePushed
 sborah      12/16/08 - handle constants
 sbishnoi    07/28/08 - support for nanosecond
 anasrini    05/25/07 - inline view support
 hopark      12/15/06 - add range
 ayalaman    07/29/06 - add implementation
 najain      05/25/06 - add updateSchemaStreamCross 
 najain      02/08/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/logplan/LogOptPrtnWin.java /main/9 2012/05/02 03:05:56 pkali Exp $
 *  @author  najain  
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.logplan;

import java.util.ArrayList;

import oracle.cep.common.TimeUnit;
import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;
import oracle.cep.logplan.attr.Attr;
import oracle.cep.logplan.attr.AttrNamed;
import oracle.cep.logplan.expr.Expr;
import oracle.cep.semantic.PartnWindowSpec;
import oracle.cep.semantic.WindowSpec;

/**
 * Partition Window Logical Operator
 */
public class LogOptPrtnWin extends LogOpt implements Cloneable {

  /** parition attributes */
  private Attr[] partnAttrs;

  private int    numPartnAttrs;

  /** number of rows */
  private int    numRows;

  /** rangeUnits in nanoseconds */
  private long rangeUnits = -1;

  /** Time Units for the slide */
  private long slideUnits;
  
  /** Expression which will evaluates to range */
  private Expr rangeExpr;
  
  /** Flag to check if the window is a variable duration window */
  private boolean isVariableDurationWindow;
  
  /** unit of range value */
  private TimeUnit rangeUnit;

  /**
   *  Set the number of partition attributes 
   *
   *  @param  numPartnAttrs   number of partition attributes
   */
  public void setNumPartnAttrs(int numPartnAttrs) 
  {
    this.numPartnAttrs = numPartnAttrs;
  }

  /**
   *  Set the partition size in terms of number of rows 
   *
   *  @param  numRows   number of rows in each partition.
   */
  public void setNumRows(int numRows) 
  {
    this.numRows = numRows;
  }

  /**
   * Set the range units
   * 
   * @param rangeUnits     range units  
   */
  public void setRangeUnits(long rangeUnits)
  {
    this.rangeUnits = rangeUnits; 
  }
  
  /**
   *  Get the number of partition attributes 
   *
   *  @return   number of partition attributes
   */
  public int getNumPartnAttrs() 
  {
    return numPartnAttrs;
  }

  /**
   *  Get the partition attributes 
   *
   *  @return   partition attributes as an array 
   */
  public Attr[] getPartnAttrs() {
    return partnAttrs; 
  }

  /**
   *  Get the partition size in terms of number of rows 
   *
   *  @return   number of rows in each partition.
   */
  public int getNumRows() 
  {
    return numRows;
  }

  /**
   * Get the range units in nanoseconds
   * @return the range units in nanoseconds
   */
  public long getRangeUnits() {
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
  
  public boolean hasRange() 
  {
      return rangeUnits >= 0;
  }

  /**
   * @return the rangeExpr
   */
  public Expr getRangeExpr()
  {
    return rangeExpr;
  }

  /**
   * @param rangeExpr the rangeExpr to set
   */
  public void setRangeExpr(Expr rangeExpr)
  {
    this.rangeExpr = rangeExpr;
  }

  /**
   * @return the isVariableDurationWindow
   */
  public boolean isVariableDurationWindow()
  {
    return isVariableDurationWindow;
  }

  /**
   * @param isVariableDurationWindow the isVariableDurationWindow to set
   */
  public void setVariableDurationWindow(boolean isVariableDurationWindow)
  {
    this.isVariableDurationWindow = isVariableDurationWindow;
  }

  /**
   * @return the rangeUnit
   */
  public TimeUnit getRangeUnit()
  {
    return rangeUnit;
  }

  /**
   * @param rangeUnit the rangeUnit to set
   */
  public void setRangeUnit(TimeUnit rangeUnit)
  {
    this.rangeUnit = rangeUnit;
  }

  /**
   *  Default constructor
   */
  public LogOptPrtnWin() 
  {
    super(LogOptKind.LO_PARTN_WIN);
  }

  /**
   *  Constructor with complete partition window specification
   */
  public LogOptPrtnWin(LogOpt input, WindowSpec win)
  {
    super(LogOptKind.LO_PARTN_WIN);
    oracle.cep.semantic.Attr[] pbySemAttrs; 
    Attr                       pbyLogAttr; 
    
    assert input != null; 
    assert input.getIsStream() == true;
    // input could be any valid stream 
    // assert input instanceof LogOptStrmSrc; 
    assert win instanceof PartnWindowSpec; 

    setNumInputs(1);
    setInput(0, input);
    setIsStream(false);
    // structure of the output for partition window is same as input
    copy(input);

    PartnWindowSpec pwin = (PartnWindowSpec) win;
    pbySemAttrs = pwin.getPartnAttrs(); 

    rangeUnits = pwin.getRangeUnits();
    // Time unit of range expression
    rangeUnit = pwin.getRangeUnit();
    
    slideUnits = pwin.getSlideUnits();
    numRows =pwin.getNumRows();

    numPartnAttrs = pbySemAttrs.length;
    partnAttrs = new Attr[numPartnAttrs]; 

    // transform the partition by attribute into logical 
    for (int aidx = 0; aidx < numPartnAttrs; aidx++)
    {
      // to check that the attribute specified are valid 
      pbyLogAttr = transformAttr(pbySemAttrs[aidx]); 
      // add it to the list of partition attributes 
      partnAttrs[aidx] = pbyLogAttr; 
    }

    input.setOutput(this);
  }
  
  /**
   * Can this partitionParallel expression be pushed down to the 
   * specified input 
   * @param inputNo the specified input number 
   * @return the Logical layer form of the expression that can serve as
   *         the partitionParallel expression of input inputNo
   */
  public Expr canPartitionExprBePushed(int inputNo)
  {
    Expr ppExpr = getPartitionParallelExpr();

    if(ppExpr == null)
      return null;
    
    // get a list of all the attributes in the parallel partitioning expression
    ArrayList<Attr> attrs = new ArrayList<Attr>();
    ppExpr.getAllReferencedAttrs(attrs);
    
    // check if all attributes in the parallel paritioning expression
    // are present in the partition attributes.
    for(Attr attr : attrs)
    {
      boolean found = false;
      for(int i = 0; i < partnAttrs.length; i++)
      {
        if(attr.equals(partnAttrs[i]))
        {
          found = true;
          break;
        }
      }
      if(!found)
        return null;
    }
    
    LogUtil.fine(LoggerType.TRACE,
        "Partition parallelism possible for input " + inputNo
        + " of logical op " + operatorKind 
        + " with expression " + ppExpr);
    
    return ppExpr;
  }

  /**
   * Get the logical attribute for the semantic attribute passed in
   * 
   * @param semAttr    semantic attribute 
   * 
   * @return  the equivalent logical attribute
   */
  private AttrNamed transformAttr(oracle.cep.semantic.Attr semAttr)
  {
     return new AttrNamed(semAttr.getVarId(), semAttr.getAttrId(), 
                               semAttr.getDatatype()); 
  }

  /**
   *  clone has been re-implemented to perform a deep copy instead of the
   *  default shallow copy
   */
  public LogOptPrtnWin clone() throws CloneNotSupportedException 
  {
    LogOptPrtnWin op = (LogOptPrtnWin) super.clone();

    // TODO: this can be further optimized to allocate the whole
    // array in a batch
    for (int i = 0; i < this.partnAttrs.length; i++)
      op.partnAttrs[i] = (Attr) this.partnAttrs[i].clone();

    return op;
  }

  /**
   *  updateSchemaStreamCross
   */
  public void updateSchemaStreamCross()
  {
    numOutAttrs = 0;
    for (int i = 0 ; i < getNumInputs(); i++) {
      LogOpt inp = getInputs().get(i);
      assert (inp != null);

      for (int a = 0 ; a < inp.getNumOutAttrs(); a++) {
        setOutAttr(numOutAttrs, inp.getOutAttrs().get(a));
        numOutAttrs++;
      }
    }
  }

  /**
   * String reprsentation of the operator
   */
  public String toString()
  {
    StringBuilder sb = new StringBuilder();
    sb.append("<PartitionWindow>");
    // Dump the common fields
    sb.append(super.toString());
    sb.append("</PartitionWindow>");
    return sb.toString();
  }

}
