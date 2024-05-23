/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/logplan/LogOptSelect.java /main/6 2012/10/22 14:42:18 vikshukl Exp $ */

/* Copyright (c) 2006, 2012, Oracle and/or its affiliates. 
All rights reserved. */

/*
 DESCRIPTION
 Describes Select logical operator in the package oracle.cep.logplan

 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>

 NOTES
 <other useful comments, qualifications, etc.>

 MODIFIED    (MM/DD/YY)
 vikshukl    08/07/12 - propagate archived dimension
 sborah      03/29/11 - override canPartitionExprBePushed()
 parujain    02/20/09 - outer join for external relation
 sborah      12/16/08 - handle constants
 parujain    12/18/07 - validate external relations for outer join
 najain      05/30/06 - add pushSelect 
 najain      05/25/06 - add updateSchemaStreamCross 
 anasrini    03/29/06 - add setBExpr 
 najain      02/08/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/logplan/LogOptSelect.java /main/6 2012/10/22 14:42:18 vikshukl Exp $
 *  @author  najain  
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.logplan;

import oracle.cep.exceptions.LogicalPlanError;
import oracle.cep.logplan.expr.BoolExpr;
import oracle.cep.logplan.expr.Expr;

/**
 * Select Logical Operator
 */
public class LogOptSelect extends LogOpt implements Cloneable
{

  /** boolean expression */
  private BoolExpr bexpr;

  public LogOptSelect(LogOpt input)
  {
    super(LogOptKind.LO_SELECT);

    assert input != null;

    // Retains +/- effect of input
    setIsStream(input.getIsStream());
    setArchivedDim(input.isArchivedDim()); // input is dependent on archived dim

    setNumInputs(1);
    setInput(0, input);
    input.setOutput(this);

    // Set the out attributes for this operator
    setOutAttrs(input.getOutAttrs());
    setNumOutAttrs(input.getNumOutAttrs());
  }

  public BoolExpr getBExpr()
  {
    return bexpr;
  }

  /**
   * Set the predicate.
   * <p>
   * For the moment, the predicate is essentially an arithmetic exprssion. In
   * particular, it does not contain logical operators like AND.
   * 
   * @param pred
   *          the predicate to set
   */
  public void setBExpr(BoolExpr pred)
  {
    this.bexpr = pred;
  }

  // clone has been re-implemented to perform a deep copy instead of the
  // default shallow copy
  public LogOptSelect clone() throws CloneNotSupportedException
  {
    LogOptSelect op = (LogOptSelect) super.clone();
    op.bexpr = (BoolExpr) this.bexpr.clone();
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

  public void updateStreamPropertyStreamCross()
  {
    LogOpt inp = getInputs().get(0);
    assert (inp != null);
    setIsStream(inp.getIsStream());
  }

  public int canSelectBePushed(LogOptSelect select)
  {
    // A select can always be pushed below another select
    return 0;
  }

  /**
   * Push select below a cross. Does not delete the original select - just makes
   * a copy below the cross if possible
   */
  public boolean pushSelect()
  {
    boolean bPushed = false; // We haven't pushed the select yet ...

    // op is used to iterate down from the select operator
    LogOpt op = getInputs().get(0);

    // siblingPos: position of op among its siblings
    int siblingPos = 0;

    // bUsefulPush = true <--> there is non-select operator (cross)
    // between select and op.
    boolean bUsefulPush = false;

    // loop invariant: select can always be pushed from its present
    // position to just above op.
    while (op != null)
    {
      // Selects can be pushed below certain operators. This is
      // determined by the canBePushed function, which also returns the
      // path along which the select can be pushed (siblingPos)

      int childPos = op.canSelectBePushed(this);
      if (childPos != -1)
      {
        // We are pushing below a non-select, so the overall pushing
        // of the select is "useful"
        if (op.getOperatorKind() != LogOptKind.LO_SELECT)
          bUsefulPush = true;

        // We should not be able to push below source operators.
        assert (op.getNumInputs() > 0);

        op = op.getInputs().get(childPos);
        siblingPos = childPos;
      }
      else
        break;
    }

    // If the push is useful, make a copy of the select operator above 'op'
    if (bUsefulPush)
    {
      // insert a copy of 'select' between op and its parent
      LogOpt op_parent = op.getOutput();

      assert (op_parent != null);
      assert (op_parent != this);
      assert (op_parent.getNumInputs() > siblingPos);

      LogOptSelect pushedSelect = new LogOptSelect(op);
      pushedSelect.setBExpr(getBExpr());
     
      op_parent.getInputs().set(siblingPos, pushedSelect);
      
      pushedSelect.setOutput(op_parent);

      bPushed = true;
    }

    return bPushed;
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
    Expr ppExpr = super.canPartitionExprBePushed(inputNo);
    
    if(ppExpr == null)
      return null;
    
    // TEMP HACK :
    // set the predicate in it's input join operator if present. 
    // Assumption : the join condition has only one select predicate
    LogOpt op = getInput(0);
    
    if(op instanceof LogOptCross)
    {
      LogOptCross cross = (LogOptCross)op;
      cross.setBexpr(bexpr);
    }
    else if(op instanceof LogOptStrmCross)
    {
      LogOptStrmCross cross = (LogOptStrmCross)op;
      cross.setBexpr(bexpr);
    }
    
    return ppExpr;
  }
  
 
  protected void validate() throws LogicalPlanException 
  {
    validateExternalRelnAndOuterJoin();
  }
  
  private void validateExternalRelnAndOuterJoin() throws LogicalPlanException
  {
    if(!this.bexpr.isValidOuterJoin())
      throw new LogicalPlanException(LogicalPlanError.NOT_VALID_OUTER_JOIN_WITH_EXTERAL_RELATION);
  }
  
  public boolean isOuterJoin()
  {
    return this.bexpr.isOuterJoin();
  }
  
  // toString method override
  public String toString()
  {

    StringBuilder sb = new StringBuilder();
    sb.append("<Select>");

    // Dump the common fields
    sb.append(super.toString());
    sb.append(bexpr.toString());
    sb.append("</Select>");
    return sb.toString();
  }
}
