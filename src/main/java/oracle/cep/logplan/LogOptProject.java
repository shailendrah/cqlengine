/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/logplan/LogOptProject.java /main/6 2011/04/06 04:30:41 sborah Exp $ */

/* Copyright (c) 2006, 2011, Oracle and/or its affiliates. 
All rights reserved. */

/*
 DESCRIPTION
 Describes Project logical operator in the package oracle.cep.logplan

 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>

 NOTES
 <other useful comments, qualifications, etc.>

 MODIFIED    (MM/DD/YY)
 sborah      03/29/11 - override canPartitionExprBePushed()
 sborah      12/16/08 - handle constants
 rkomurav    02/28/08 - parameterize errors
 najain      12/11/07 - xmltable support
 anasrini    05/24/07 - support named attributes for the expressions
 najain      05/26/06 - add isUseless 
 najain      05/25/06 - add updateStreamPropertyStreamCross 
 najain      02/08/06 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/logplan/LogOptProject.java /main/5 2009/02/23 06:47:35 sborah Exp $
 *  @author  najain  
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.logplan;

import java.util.ArrayList;

import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;
import oracle.cep.logplan.expr.Expr;
import oracle.cep.logplan.attr.Attr;


/**
 * Project Logical Operator
 */
public class LogOptProject extends LogOpt implements Cloneable
{

  /** projection expressions */
  private ArrayList<Expr> bexpr;

  /**
   * @return Returns the bexpr.
   */
  public ArrayList<Expr> getBexpr()
  {
    return bexpr;
  }

  public LogOptProject()
  {
    super(LogOptKind.LO_PROJECT);
    // Allocate the memory for the expressions
    bexpr = new ArrayList<Expr>();
  }

  /**
   * @param input
   *          The input logical operator for the projection
   */
  public LogOptProject(LogOpt input)
  {
    super(LogOptKind.LO_PROJECT);

    // Allocate the memory for the expressions
    bexpr = new ArrayList<Expr>();
    assert input != null;

    // Retains +/- effect of input
    setIsStream(input.getIsStream());

    setNumInputs(1);
    setInput(0, input);

    input.setOutput(this);
  }

  // clone has been re-implemented to perform a deep copy instead of the
  // default shallow copy
  public LogOptProject clone() throws CloneNotSupportedException
  {
    LogOptProject op = (LogOptProject) super.clone();

    for (int i = 0; i < bexpr.size(); i++)
      op.bexpr.add(this.bexpr.get(i).clone());

    return op;
  }

  public void addAttr(Attr attr) throws LogicalPlanException
  {
    setOutAttr(numOutAttrs, attr);
    numOutAttrs++;    
  }
  
  public void add_project_expr_noattr(Expr expr)
  {
    assert expr != null;
    bexpr.add(expr); 
  }
  
  public void add_project_expr(Expr expr)
  {
    assert expr != null;

    bexpr.add(expr);

    // Type of the (new) output attribute
    setOutAttr(numOutAttrs, expr.getAttr());

    numOutAttrs++;
  }

  public void add_project_expr(Expr expr, Attr attr)
  {
    assert expr != null;

    bexpr.add(expr);

    // Type of the (new) output attribute
    setOutAttr(numOutAttrs, attr);

    numOutAttrs++;
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
    // for project operator , simply pass the expression through
    Expr ppExpr = getPartitionParallelExpr();

    if(ppExpr == null)
      return null;

    LogUtil.fine(LoggerType.TRACE,
                 "Partition parallelism possible for input " + inputNo
                 + " of logical op " + operatorKind 
                 + " with expression " + ppExpr);

    return ppExpr;
  }
  

  public void updateStreamPropertyStreamCross()
  {
    LogOpt inp = getInputs().get(0);
    assert (inp != null);
    setIsStream(inp.getIsStream());
  }

  public boolean isUseless()
  {
    LogOpt input = getInputs().get(0);
    assert (input != null);
    
    if (numOutAttrs != input.getNumOutAttrs())
      return false;
    
    for (int a = 0 ; a < numOutAttrs; a++)
      if (!getOutAttrs().get(a).isSame(input.getOutAttrs().get(a)))
       return false;
    
    return true;
  }

  // toString method override
  public String toString()
  {

    StringBuilder sb = new StringBuilder();

    sb.append("<ProjectLogicalOperator>");

    // Dump the common fields
    sb.append(super.toString());

    if (bexpr.size() != 0)
    {
      sb.append("<NumberOfExprs numExprs=\"" + bexpr.size() + "\" />");

      for (int i = 0; i < bexpr.size(); i++)
        sb.append(bexpr.get(i).toString());
    }

    sb.append("</ProjectLogicalOperator>");

    return sb.toString();
  }
}
