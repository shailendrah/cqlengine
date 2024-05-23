/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/logplan/LogOptCross.java /main/17 2015/02/16 09:40:11 udeshmuk Exp $ */

/* Copyright (c) 2006, 2015, Oracle and/or its affiliates. 
All rights reserved.*/

/*
 DESCRIPTION
 <short description of component this file declares/defines>

 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>

 NOTES
 <other useful comments, qualifications, etc.>

 MODIFIED    (MM/DD/YY)
 vikshukl    06/11/13 - binary join enable dimension flag here
 vikshukl    04/17/13 - take care of the case where f x d x nd
 vikshukl    08/07/12 - is any input dependent on archived dimension(s)
 anasrini    09/19/11 - XbranchMerge anasrini_bug-12934458_ps5 from
                        st_pcbpel_11.1.1.4.0
 anasrini    09/19/11 - bug 12934458
 anasrini    07/07/11 - XbranchMerge anasrini_bug-12640265_ps5 from
                        st_pcbpel_11.1.1.4.0
 anasrini    07/07/11 - handle external entity parallelism when there is no
                        predicate
 anasrini    05/17/11 - XbranchMerge anasrini_bug-12560613_ps5 from main
 sborah      04/11/11 - allow outer join for parallel partitioning
 sborah      03/28/11 - override canPartitionExprBePushed
 sbishnoi    07/29/10 - XbranchMerge sbishnoi_bug-9947670_ps3_main_11.1.1.4.0
                        from st_pcbpel_11.1.1.4.0
 sbishnoi    07/28/10 - XbranchMerge sbishnoi_bug-9947670_ps3_main from main
 sbishnoi    07/28/10 - passing parameter for error BAD_JOIN_WITH_EXTERNAL_RELN
 sborah      12/15/09 - support for multiple external tables in join
 sbishnoi    01/04/10 - table function cleanup
 sbishnoi    09/30/09 - table function support
 sbishnoi    05/27/09 - validate external joins
 parujain    05/20/09 - ansi outer join support
 sborah      12/16/08 - handle constants
 parujain    11/09/07 - external source
 mthatte     10/22/07 - checking join condition for onDemand reln.
 hopark      12/06/06 - check unbounded stream
 parujain    10/31/06 - Push Select problem
 najain      05/30/06 - add canSelectBePushed 
 najain      05/25/06 - add updateSchemaStreamCross 
 najain      02/26/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/logplan/LogOptCross.java /main/17 2015/02/16 09:40:11 udeshmuk Exp $
 *  @author  najain  
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.logplan;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import oracle.cep.common.CompOp;
import oracle.cep.common.Constants;
import oracle.cep.common.OuterJoinType;
import oracle.cep.dataStructures.internal.ExpandableArray;
import oracle.cep.exceptions.LogicalPlanError;
import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;
import oracle.cep.logplan.attr.Attr;
import oracle.cep.logplan.expr.BaseBoolExpr;
import oracle.cep.logplan.expr.BoolExpr;
import oracle.cep.logplan.expr.Expr;

public class LogOptCross extends LogOpt
{
  /** 
   * a list of outer join predicates 
   * Note: predicates will be non-null only if this LogOptCross represents
   * an ANSI syntax based outer join; otherwise null; 
   */
  List<BoolExpr>  predicates;
  
  /** 
   * type of outer join
   * Note: joinType will be non-null only if this LogOptCross represents
   * an outer join; otherwise null; 
   */
  OuterJoinType   joinType;
  
  /** 
   * boolean expression of the select expression above the join 
   * This will be set by the select operator while evaluating its 
   * canPartitionExprBePushed() 
   */
  private BoolExpr bexpr;
  
  // list of partition parallel expressions for the inputs
  ArrayList<Expr> inputPartitionParallelExprList;
 
  /**
   * Constructor
   */
  public LogOptCross()
  {
    super(LogOptKind.LO_CROSS);
    this.inputs     = new ExpandableArray<LogOpt>(Constants.MAX_INPUT_OPS);
    this.predicates = new LinkedList<BoolExpr>();
    this.joinType   = null;
  }

  // For this Logical Operator, inputs can be added dynamically,
  // so dont allocate the inputs array based on numInputs. It should
  // be done always in the constructor.
  // This is needed for only two logical operators:: LogOptCross and
  // LogOptStrmCross.
  public void setNumInputs(int numInputs)
  {
    this.numInputs = numInputs;

    // Inputs should have been allocated by this time
    assert inputs != null;
  }
  
  /**
   * Set OuterJoinType
   * @param type
   */
  public void setOuterJoinType(OuterJoinType type)
  {
    this.joinType = type;
  }
  
  /** 
   * Get OuterJoinType
   * @return
   */
  public OuterJoinType getOuterJoinType()
  {
    return this.joinType;
  }

  public void addInput(LogOpt inp)
  {
    // This should be changed to throw external errors in the future.
    assert inp != null;
    
    inputs.add(inp);
    numInputs++;

    // Append the schema of the new input to the existing schema
    ArrayList<Attr> inpAttrs = inp.getOutAttrs();
    ArrayList<Attr> outAttrs = getOutAttrs();

    for (int a = 0; a < inp.getNumOutAttrs(); a++)
    {
      outAttrs.set(numOutAttrs, inpAttrs.get(a));      
      numOutAttrs++;
    }
    // Even one input that has '-' makes the output of cross contain '-'
    if (inp.getIsStream() == false)
      setIsStream(false);
    
    // If the input is an External relation
    if(inp.isExternal())
      this.setExternal(true);

    inp.setOutput(this);
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
    setIsStream(true);
    for (int i = 0; i < getNumInputs(); i++)
    {
      LogOpt inp = getInputs().get(i);
      if (!inp.getIsStream())
      {
        setIsStream(false);
        return;
      }
    }
  }

  public int canSelectBePushed(LogOptSelect select)
  {    
    int childPos;
    
    // A select cannot be pushed below a cross if the cross is
    // outer join
    if(this.getOuterJoinType() != null)
      return -1;

    // A select can be pushed below a cross/stream-cross if all the
    // attributes referenced in the select come from a single child
    // of the cross/stream-cross
    // Select will not be pushed below join if it belongs to an external 
    // relation, i.e a pull operator.
    for (childPos = 0; childPos < getNumInputs(); childPos++)
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
      : "Cross should be binary during partitionParallelExpr evaluations";

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
      inputPartitionParallelExprList.set(numInputs - 1 - inputNo, null);

      return;
    }

    // At this point, both inputs are push operators

    // first check if the entire expression is composed only of attributes from
    // one of the given inputs operator
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
    Expr ppExpr = null;
    
    if(this.getOuterJoinType() != null && predicates != null)
    {
      ppExpr = computePartitionExprForOuterJoin(inputNo);
    }
    else
    {
      if(inputPartitionParallelExprList == null || 
          inputPartitionParallelExprList.size() == 0)
        return null;
      
      ppExpr = inputPartitionParallelExprList.get(inputNo);
    }
    
    if(ppExpr != null)
      LogUtil.fine(LoggerType.TRACE,
        "Partition parallelism possible for input " + inputNo
         + " of logical op " + operatorKind 
         + " with expression " + ppExpr);
    
    return ppExpr;
  }
  
  /**
   * Compute the partition parallel expression for outer joins
   * @param inputNo
   * @return
   */
  private Expr computePartitionExprForOuterJoin(int inputNo)
  {

    assert this.getOuterJoinType() != null :
    "OuterJoinType should not be null";

    assert this.predicates != null :
    "Outer Join Predicates shoud not be null ";
    
    Expr partitionParallelExpr = getPartitionParallelExpr();
    if(partitionParallelExpr == null)
      return null;
    
    int input = 0;
    
    // ASSUMPTION : the cross is binary.
    assert numInputs == 2 
    : "Cross should be binary during partitionParallelExpr evaluations";
    
    // first check if the entire expression is composed only of attributes from
    // the given input operator
    for(LogOpt op : getInputs())
    {
      if(partitionParallelExpr.check_reference(op) && !op.isPullOperator())
        break;
      input++;
    }
    
    if(input >= numInputs)
    {
      // no parallel partition expressions possible for any of its inputs
      return null;
    }
    
    // Currently, we support join predicate of the form
    // <arith_expr> = <arith_expr>
    if(predicates.size() > 1 || !(predicates.get(0) instanceof BaseBoolExpr))
      return null;
    
    BaseBoolExpr boolExpr = (BaseBoolExpr)predicates.get(0);
    if(boolExpr.getOper() != CompOp.EQ)
      return null;
    
    // Now check whether partitionParallelExpr is equal to LHS or RHS
    if(boolExpr.getLeft().equals(partitionParallelExpr))
    {
      if(input == inputNo)
        return partitionParallelExpr;
      LogOpt op =  getInputs().get(inputNo);
      if(boolExpr.getRight().check_reference(op) && !op.isPullOperator())
        return boolExpr.getRight();
    }
    // Now check whether partitionParallelExpr is equal to LHS or RHS
    if(boolExpr.getRight().equals(partitionParallelExpr))
    {
      if(input == inputNo)
        return partitionParallelExpr;
      LogOpt op =  getInputs().get(inputNo);
      if(boolExpr.getLeft().check_reference(op) && !op.isPullOperator())
        return boolExpr.getLeft();
    }
  
    return null;
  }
  
  /**
   * Checks to see that a join does not contain external relations 
   * as both its inputs. Also an outer join should not be allowed 
   * on an external relation.
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
        // generated in context of OUTER relation (randomly chosen)
        String externalRelName 
          = getExternalRelName(inputs.get(Constants.OUTER));
        
        throw new LogicalPlanException(
            LogicalPlanError.BAD_JOIN_WITH_EXTERNAL_RELN, externalRelName);
      }
      // Left is External relation
      // In this case only RIGHT_OUTER is permitted i.e 
      // not on the side of the external relation
      if(this.getOuterJoinType() != null && 
              (this.getOuterJoinType() == OuterJoinType.LEFT_OUTER ||
              this.getOuterJoinType() == OuterJoinType.FULL_OUTER))
      {
        // Note: This Check is applicable when outer join type is mentioned by
        // using ANSI syntax
        // The similar check for Oracle's outer join syntax lies in 
        // LogOptSelect.validate()
         throw new LogicalPlanException(
           LogicalPlanError.NOT_VALID_OUTER_JOIN_WITH_EXTERAL_RELATION);
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
      // Right is External relation 
      // In this case only LEFT_OUTER is permitted , i.e 
      // not on the side of the external relation
      if(this.getOuterJoinType() != null &&
              (this.getOuterJoinType() == OuterJoinType.RIGHT_OUTER ||
               this.getOuterJoinType() == OuterJoinType.FULL_OUTER))
      {
        // Note: This Check is applicable when outer join type is mentioned by
        // using ANSI syntax
        // The similar check for Oralce's outer join syntax lies in 
        // LogOptSelect.validate()
        throw new LogicalPlanException(
            LogicalPlanError.NOT_VALID_OUTER_JOIN_WITH_EXTERAL_RELATION);
      }
      else
         return;
    }
  }
  
  @Override
  protected void validate() throws LogicalPlanException
  {
    checkUnboundStream();
    validateExternalJoin();
  }
  
  /**
   * Add the outer join predicate in LogOptCross
   * @param pred
   */
  public void addPredicate(BoolExpr pred)
  {
    this.predicates.add(pred);
  }
  
  /**
   * Get the list representation of outer join predicate
   * @return List of predicates
   */
  public List<BoolExpr> getPredicates()
  {
    return this.predicates;
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
