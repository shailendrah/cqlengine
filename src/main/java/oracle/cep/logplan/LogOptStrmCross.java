/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/logplan/LogOptStrmCross.java /main/10 2011/09/19 22:20:19 anasrini Exp $ */

/* Copyright (c) 2006, 2011, Oracle and/or its affiliates. 
All rights reserved. */

/*
 DESCRIPTION
 <short description of component this file declares/defines>

 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>

 NOTES
 <other useful comments, qualifications, etc.>

 MODIFIED    (MM/DD/YY)
 anasrini    09/19/11 - XbranchMerge anasrini_bug-12934458_ps5 from
                        st_pcbpel_11.1.1.4.0
 anasrini    09/19/11 - bug 12934458
 anasrini    07/07/11 - XbranchMerge anasrini_bug-12640265_ps5 from
                        st_pcbpel_11.1.1.4.0
 anasrini    07/07/11 - handle external entity parallelism when there is no
                        predicate
 sborah      03/28/11 - override canPartitionExprBePushed
 sbishnoi    07/29/10 - XbranchMerge sbishnoi_bug-9947670_ps3_main_11.1.1.4.0
                        from st_pcbpel_11.1.1.4.0
 sbishnoi    07/28/10 - XbranchMerge sbishnoi_bug-9947670_ps3_main from main
 sbishnoi    07/28/10 - passing parameter for error BAD_JOIN_WITH_EXTERNAL_RELN
 sborah      01/07/10 - adding support for multiple external tables in join
 sbishnoi    01/04/10 - table function cleanup
 sbishnoi    10/04/09 - table function support
 sborah      12/16/08 - handle constants
 parujain    11/15/07 - external source
 parujain    10/31/06 - Push Select problem
 najain      05/30/06 - add canSelectBePushed 
 najain      02/26/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/logplan/LogOptStrmCross.java /main/7 2010/07/29 05:51:55 sbishnoi Exp $
 *  @author  najain  
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.logplan;

import java.util.ArrayList;

import oracle.cep.common.CompOp;
import oracle.cep.common.Constants;
import oracle.cep.dataStructures.internal.ExpandableArray;
import oracle.cep.exceptions.LogicalPlanError;
import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;
import oracle.cep.logplan.expr.BaseBoolExpr;
import oracle.cep.logplan.expr.BoolExpr;
import oracle.cep.logplan.expr.Expr;

public class LogOptStrmCross extends LogOpt
{
  /** 
   * boolean expression of the select expression above the join 
   * This will be set by the select operator while evaluating its 
   * canPartitionExprBePushed() 
   */
  private BoolExpr bexpr;
  
  // list of partition parallel expressions for the inputs
  ArrayList<Expr> inputPartitionParallelExprList;
 
  
  public LogOptStrmCross(LogOpt streamInput, int numInputs)
  {
    super(LogOptKind.LO_STREAM_CROSS);

    // We want an input producing a stream.
    assert (streamInput != null);
    assert (streamInput.getIsStream() == true);

    // Output is a stream by defn.
    setIsStream(true);

    // Currently, this is the schema of the streamInput. This will get
    // expanded in the stream_cross_add_rel_input function.
    for (int a = 0; a < streamInput.getNumOutAttrs(); a++)
    {
      setOutAttr(numOutAttrs, streamInput.getOutAttrs().get(a));
      numOutAttrs++;
    }

    setNumInputs(numInputs);
    
    getInputs().set(0, streamInput);  
    
    streamInput.setOutput(this);
  }

  public LogOptStrmCross(LogOpt streamInput, LogOptCross cross)
  {
    super(LogOptKind.LO_STREAM_CROSS);

    // We want an input producing a stream.
    assert (streamInput != null);
    assert (streamInput.getIsStream() == true);

    // Output is a stream by defn.
    setIsStream(true);

    // Currently, this is the schema of the streamInput. This will get
    // expanded in the stream_cross_add_rel_input function.
    for (int a = 0; a < streamInput.getNumOutAttrs(); a++)
    {
      setOutAttr(numOutAttrs, streamInput.getOutAttrs().get(a));
      numOutAttrs++;
    }

    setNumInputs(cross.getNumInputs());
    getInputs().set(0, streamInput);
    
    streamInput.setOutput(this);
    
  }

  public void add_input(LogOpt input, int inputPos)
  {
    assert (getOperatorKind() == LogOptKind.LO_STREAM_CROSS);

    // Add the new input.
    assert inputPos < getNumInputs();
   
    getInputs().set(inputPos, input);
    
    input.setOutput(this);

    // Update the schema.
    for (int a = 0; a < input.getNumOutAttrs(); a++)
    {
      setOutAttr(numOutAttrs, input.getOutAttrs().get(a));
      numOutAttrs++;
    }
    
    if(input.isExternal())
      this.setExternal(true);
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

  public int canSelectBePushed(LogOptSelect select) 
  {

    int childPos;

    // A select can be pushed below a cross/stream-cross if all the
    // attributes referenced in the select come from a single child
    // of the cross/stream-cross
    
    for (childPos = 0 ; childPos < getNumInputs(); childPos++) 
    {
      LogOpt op = getInputs().get(childPos);
      if(select.getBExpr().check_reference(op) && !op.isPullOperator())
        return childPos;
    }
    
    return -1;
  }
  
  @Override
  public void setPartitionParallelExpr(Expr partitionParallelExpr)
  {
    super.setPartitionParallelExpr(partitionParallelExpr);
   
    if(partitionParallelExpr == null)
      return;

    // ASSUMPTION : the cross is binary.
    assert numInputs == 2 
      : "StrmCross should be binary during partitionParallelExpr evaluations";
    
    // First handle the case if one of the inputs is a pull operator
    LogOpt  leftInput        = getInput(0);
    LogOpt  rightInput       = getInput(1);
    boolean isLeftInputPull  = leftInput.isPullOperator();
    boolean isRightInputPull = rightInput.isPullOperator();

    if (isLeftInputPull || isRightInputPull)
    {
      int inputNo;

      if (isLeftInputPull && partitionParallelExpr.check_reference(rightInput))
        inputNo = 1;
      else if (isRightInputPull &&
               partitionParallelExpr.check_reference(leftInput))
        inputNo = 0;
      else
        return;

      // Now, set the "inputPartitionParallelExprList"
      if(inputPartitionParallelExprList == null)
      {
        inputPartitionParallelExprList = new ExpandableArray<Expr>(numInputs);
      }
      inputPartitionParallelExprList.set(inputNo, partitionParallelExpr);
      inputPartitionParallelExprList.set(1 - inputNo, null);
        
      return;
    }

    // At this point, both inputs are push operators

    // first check if the entire expression is composed only of attributes from
    // the given input operator
    int inputNo = 0;
    for(LogOpt op : getInputs())
    {
      if(partitionParallelExpr.check_reference(op))
        break;
      inputNo++;
    }
    
    if(inputNo >= numInputs)
    {
      // no parallel partition expressions possible for any of its inputs
      return;
    }
    
    // partitionParallelExpr contains only references from input = inputNo>.
    
    // Currently, we support join predicate of the form
    // <arith_expr> = <arith_expr>
    if(bexpr == null || !(bexpr instanceof BaseBoolExpr))
      return;
    
    BaseBoolExpr boolExpr = (BaseBoolExpr)bexpr;
    if(boolExpr.getOper() != CompOp.EQ)
      return;
        
    // Now check whether partitionParallelExpr is equal to LHS or RHS
    if(boolExpr.getLeft().equals(partitionParallelExpr))
    {
      if(inputPartitionParallelExprList == null)
      {
        inputPartitionParallelExprList = new ExpandableArray<Expr>(numInputs);
      }
      // partitionParallelExpr = LHS
      inputPartitionParallelExprList.set(inputNo, partitionParallelExpr);
      
      // check if RHS is contains only references from the other input
      // Assumption is that the cross is binary
      LogOpt op =  getInputs().get(numInputs - 1 - inputNo);
      if(boolExpr.getRight().check_reference(op))
        inputPartitionParallelExprList.set(numInputs - 1 - inputNo, 
                                           boolExpr.getRight());
      else
        inputPartitionParallelExprList.set(numInputs - 1 - inputNo, null);
    }
    else if(boolExpr.getRight().equals(partitionParallelExpr))
    {
      if(inputPartitionParallelExprList == null)
      {
        inputPartitionParallelExprList = new ExpandableArray<Expr>(numInputs);
      }
      // partitionParallelExpr = RHS
      inputPartitionParallelExprList.set(inputNo, partitionParallelExpr);
      
      // check if LHS is contains only references from the other input
      // Assumption is that the cross is binary
      LogOpt op =  getInputs().get(numInputs - 1 - inputNo);
      if(boolExpr.getLeft().check_reference(op))
        inputPartitionParallelExprList.set(numInputs - 1 - inputNo, 
                                           boolExpr.getLeft());
      else
        inputPartitionParallelExprList.set(numInputs - 1 - inputNo, null);
    }
    
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
    if(inputPartitionParallelExprList == null || 
        inputPartitionParallelExprList.size() == 0)
      return null;
    
    Expr ppExpr = inputPartitionParallelExprList.get(inputNo);
    
    if(ppExpr != null)
      LogUtil.fine(LoggerType.TRACE,
        "Partition parallelism possible for input " + inputNo
         + " of logical op " + operatorKind 
         + " with expression " + ppExpr);
    
    return ppExpr;
  }
    
  @Override
  protected void validate() throws LogicalPlanException
  {
    validateExternalJoin();
  }
  
  /**
   * Checks to see that a join does not contain external relations 
   * as both its inputs.
   * @throws LogicalPlanException
   */
  private void validateExternalJoin() throws LogicalPlanException
  {
    assert numInputs == 2 ; //at this stage cross should be optimized
  
      //If one of them is an External relation
    if(inputs.get(Constants.OUTER).isPullOperator()) 
    {
      if(inputs.get(Constants.INNER).isPullOperator())
      {
        // ERROR : throw bad join operation
        // If the cross contains a pull operator 
        // then the other input cannot be another pull 
        // operator.
        
        // As both inputs are external source operator, this error will be
        // generated in context of INNER relation (randomly chosen)
        String externalRelName 
          = getExternalRelName(inputs.get(Constants.INNER));
        throw new LogicalPlanException(
            LogicalPlanError.BAD_JOIN_WITH_EXTERNAL_RELN, externalRelName);
      }
      else
         return;
    }
    //... if the other is an External relation
    else if(inputs.get(Constants.INNER).isPullOperator()) 
    {
      if(inputs.get(Constants.OUTER).isPullOperator())
      {
        // ERROR : throw bad join operation
        // If the cross contains a pull operator 
        // then the other input cannot be another pull 
        // operator.
        
        // As both inputs are external source operator, this error will be
        // generated in context of INNER relation (randomly chosen)
        String externalRelName 
          = getExternalRelName(inputs.get(Constants.INNER));
        
        throw new LogicalPlanException(
            LogicalPlanError.BAD_JOIN_WITH_EXTERNAL_RELN, externalRelName);
      }
      else
         return;
    }
  }
  
  /**
   * Helper function to get the external relation name
   * @param inpRelOp input logical operator corresponding to external relation
   * @return name of external relation
   */
  private String getExternalRelName(LogOpt inpRelOp)
  {
    if(inpRelOp == null)
      return "";
    else if(inpRelOp instanceof LogOptRelnSrc)
    {
      return ((LogOptRelnSrc)inpRelOp).getRelationName(); 
    }
    else if(inpRelOp instanceof LogOptTableFunctionRelSource)
    {
      return ((LogOptTableFunctionRelSource)inpRelOp).getTableAlias(); 
    }
    else
      return "";
  }

  public BoolExpr getBexpr()
  {
    return bexpr;
  }

  public void setBexpr(BoolExpr bexpr)
  {
    this.bexpr = bexpr;
  }
  
}
