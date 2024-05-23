/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/logplan/LogOptRowWin.java /main/4 2011/04/06 04:30:41 sborah Exp $ */

/* Copyright (c) 2006, 2011, Oracle and/or its affiliates. 
All rights reserved. */

/*
 DESCRIPTION
 Describes Row Window logical operator in the package oracle.cep.logplan

 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>

 NOTES
 <other useful comments, qualifications, etc.>

 MODIFIED    (MM/DD/YY)
 sborah      03/28/11 - override canPartitionExprBePushed
 sborah      12/16/08 - handle constants
 parujain    06/17/08 - slide support
 najain      05/25/06 - add updateSchemaStreamCross 
 dlenkov     05/22/06 - ROW window support
 najain      02/08/06 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/logplan/LogOptRowWin.java /main/3 2009/02/23 06:47:35 sborah Exp $
 *  @author  najain  
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.logplan;

import oracle.cep.logplan.expr.Expr;
import oracle.cep.semantic.WindowSpec;
import oracle.cep.semantic.RowWindowSpec;

/**
 * Row Window Logical Operator
 */
public class LogOptRowWin extends LogOpt implements Cloneable
{

  /** Rows for the window */
  private int numRows;
  
  /** slide size */
  private int slide;

  public LogOptRowWin()
  {
    // super(LogOptKind.LO_ROW_WIN);
    super();
    this.setOperatorKind( LogOptKind.LO_ROW_WIN);
  }

  public LogOptRowWin(LogOpt input, WindowSpec win) {
    super(LogOptKind.LO_ROW_WIN);
    assert input != null;
    assert input.getIsStream() == true;
    assert win instanceof RowWindowSpec;

    copy(input);

    setNumInputs(1);
    setInput(0, input);
    setIsStream(false);
    numRows = ((RowWindowSpec) win).getNumRows();
    slide = ((RowWindowSpec)win).getSlide();

    input.setOutput(this);
  }

  public void setNumRows(int numRows)
  {
    this.numRows = numRows;
  }

  public int getNumRows()
  {
    return numRows;
  }
  
  public int getSlide()
  {
    return slide;
  }

  // clone has been re-implemented to perform a deep copy instead of the
  // default shallow copy
  public LogOptRowWin clone() throws CloneNotSupportedException
  {
    LogOptRowWin op = (LogOptRowWin) super.clone();
    return op;
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
    // for row windows , partition expression cannot be used. This is 
    // because a partition created on a row window of size N will create separate 
    // row windows for each unique partition value with each one holding 
    // N rows. The results will be totally different and erroneous as the original
    // condition of a single row window means that all values, with or without
    // partition should sum upto N rows whereas after creating a partition, 
    // they would sum upto number_of_partitions * N.
    return null;
  }
  
  
  // toString method override
  public String toString() {

    StringBuilder sb = new StringBuilder();

    sb.append("<RowWindowLogicalOperator>");

    // Dump the common fields
    sb.append(super.toString());

    sb.append("<Rows numRows=\"" + numRows + "\" />");
    sb.append("<Slide slide=\"" + slide + "\" />");

    sb.append("</RowWindowLogicalOperator>");

    return sb.toString();
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
}
