/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/logplan/LogOpt.java /main/16 2012/10/22 14:42:18 vikshukl Exp $ */

/* Copyright (c) 2006, 2012, Oracle and/or its affiliates. 
All rights reserved. */

/*
 DESCRIPTION
 Logical Operators in the plan

 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>

 NOTES
 <other useful comments, qualifications, etc.>

 MODIFIED    (MM/DD/YY)
 vikshukl    08/07/12 - LogOpt is dependent on archived dimension
 vikshukl    08/25/11 - subquery support
 sborah      04/11/11 - use getAllReferencedAttrs()
 sborah      03/28/11 - override canPartitionExprBePushed
 anasrini    03/23/11 - support to analyse if partition parallelism is possible
 sborah      12/28/09 - support for multiple external joins
 sborah      12/15/08 - handle constants
 mthatte     04/10/08 - moving isDerivedTS to LogOptStrmSrc
 mthatte     04/01/08 - adding isDerivedTS
 rkomurav    02/28/08 - parametereize errors
 parujain    12/14/07 - update schema external reln
 parujain    11/09/07 - external source
 mthatte     10/23/07 - adding isOnDemand()
 hopark      07/13/07 - dump stack trace on exception
 rkomurav    02/22/07 - cleanup applyWindow_n
 hopark      12/06/06 - add validate
 anasrini    06/03/06 - make operatorKind protected 
 najain      05/30/06 - add canSelectBePushed 
 najain      05/25/06 - add updateSchemaStreamCross 
 najain      04/06/06 - cleanup
 najain      02/16/06 - add setters/getters etc. 
 najain      02/10/06 - add kind of logical opeator 
 najain      02/08/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/logplan/LogOpt.java /main/16 2012/10/22 14:42:18 vikshukl Exp $
 *  @author  najain  
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.logplan;

import java.util.ArrayList;
import java.util.logging.Level;

import oracle.cep.common.CompOp;
import oracle.cep.common.Constants;
import oracle.cep.common.OrderingKind;
import oracle.cep.dataStructures.internal.ExpandableArray;
import oracle.cep.exceptions.LogicalPlanError;
import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;
import oracle.cep.logplan.attr.Attr;
import oracle.cep.logplan.expr.BaseBoolExpr;
import oracle.cep.logplan.expr.ComplexBoolExpr;
import oracle.cep.logplan.expr.Expr;


/**
 * Logical Operator Class Definition
 */
public abstract class LogOpt implements Cloneable {
  /** Kind of logical operator */
  protected LogOptKind     operatorKind;

  /** Output schema of the operator */
  private ArrayList<Attr>  outAttrs;

  int                      numOutAttrs;

  /** Does the operator produce a stream */
  private boolean          isStream;
  
  /** Does the operator produce an instantaneous relation S[now] */
  private boolean          isInstantaneous;
  
  /** Output */
  private LogOpt           output;

  /** Input */
  ArrayList<LogOpt>        inputs;

  int                      numInputs;

  /** Operator itself or its input(join) is external relation? */
  private boolean          isExternal;
  
  /** Operator itself or its input(join) is dependent on archived dimension */
  private boolean          isArchivedDim;
  
  /** Specifies whether this operator pushes the tuples to its output 
   *  or it requires the output operator to pull the tuples from this 
   *  operator
   *  By Default , all operators are Push operators. 
   *  Only external relations are Pull operators.
   */
  private boolean          isPullOperator;

  private OrderingKind     orderingConstraint = 
    OrderingKind.TOTAL_ORDER;

  /**
   * The partition parallel expression associated with this logical op.
   * This applies only if user ordering constraint is PARTITION_ORDERED.
   *
   * This logical layer form of expression is an expression over the
   * output attributes of this logical operator
   *
   * NULL implies there is no partitioning expression for parallelism.
   */
  private Expr             partitionParallelExpr;

  /** List of stream or relation sources which will be the input for 
   * the subtree rooted at this node */
  protected ArrayList<LogOpt> sourceLineage;
  
  public LogOpt(LogOptKind operatorKind) {
    // Since outAttrs are added dynamically, allocate the array in the
    // beginning itself
    outAttrs = new ExpandableArray<Attr>(Constants.INITIAL_ATTRS_NUMBER);
    this.operatorKind = operatorKind;
    setDefaults();
  }

  private void setDefaults() {
    numOutAttrs = 0;
    numInputs = 0;
    isInstantaneous = false;
    isExternal = false;
    isArchivedDim = false;
    isPullOperator = false;
    partitionParallelExpr = null;
    orderingConstraint = OrderingKind.TOTAL_ORDER;
  }
  
  public boolean isExternal() {
    return isExternal;
  }

  public void setExternal(boolean isExternal) {
    this.isExternal = isExternal;
  }

  public boolean isArchivedDim() {
    return isArchivedDim;
  }

  public void setArchivedDim(boolean isArchivedDim) {
    this.isArchivedDim = isArchivedDim;
  }

  public LogOptKind getOperatorKind() {
    return operatorKind;
  }

  public void setOperatorKind(LogOptKind kind) {
    operatorKind = kind;
  }

  public Attr getOutAttr(int num) {
    return outAttrs.get(num);
  }

  public boolean getIsStream() {
    return isStream;
  }

  public void setIsStream(boolean isStream) {
    this.isStream = isStream;
  }

  public ArrayList<LogOpt> getInputs() {
    return inputs;
  }

  public LogOpt getInput(int i) {
    return inputs.get(i);
  }

  public void setInputs(ArrayList<LogOpt> inputs) {
    this.inputs = inputs;
  }

  public void setInput(int i, LogOpt input)
  {
    this.inputs.set(i, input);
  }

  public ArrayList<Attr> getOutAttrs() {
    return outAttrs;
  }

  public void setOutAttrs(ArrayList<Attr> outAttrs) {
    this.outAttrs = outAttrs;
  }

  public void setOutAttr(int i, Attr outAttr) {
    // only the last attribute can be set
    assert i == numOutAttrs;
    
    this.outAttrs.set(i, outAttr);
  }
  
  public LogOpt getOutput() {
    return output;
  }

  public void setOutput(LogOpt output) {
    this.output = output;
  }

  public void copy(LogOpt input) {
    numOutAttrs = input.getNumOutAttrs();
    // outAttrs has already been allocated in the constructor
    
    int i = 0;
    try 
    {
      // TODO: this can be further optimized to allocate the whole
      // array in a batch
      for (i = 0; i < numOutAttrs; i++)
      {
        outAttrs.set(i, input.getOutAttr(i).clone());
   
      }
    } 
    catch (CloneNotSupportedException ex) {
      LogUtil.logStackTrace(LoggerType.TRACE, Level.FINE, ex);
      
      outAttrs.set(i, null);
      
    }

    /*
     * TODO: not sure if the input needs to be copied over. op.output =
     * this.output.clone(); for (int i = 0; i < inputs.length; i++) op.inputs[i] =
     * this.inputs[i].clone();
     */
  }

  // clone has been re-implemented to perform a deep copy instead of the
  // default shallow copy
  public LogOpt clone() throws CloneNotSupportedException {
    LogOpt op = (LogOpt) super.clone();

    // TODO: this can be further optimized to allocate the whole
    // array in a batch
    for (int i = 0; i < outAttrs.size(); i++)
    { 
      op.outAttrs.set(i, this.outAttrs.get(i).clone());
    }
    op.output = this.output.clone();
    for (int i = 0; i < inputs.size(); i++)
    {
      op.inputs.set(i, this.inputs.get(i).clone());
     
    }
    op.partitionParallelExpr = partitionParallelExpr;

    return op;
  }

  public int getNumOutAttrs() {
    return numOutAttrs;
  }

  public void setNumOutAttrs(int numOutAttrs) {
    this.numOutAttrs = numOutAttrs;
  }

  public int getNumInputs() {
    return numInputs;
  }

  public void setNumInputs(int numInputs) {
    this.numInputs = numInputs;
    this.inputs = new ExpandableArray<LogOpt>(numInputs);
  }

  public LogOpt() {
      // Since outAttrs are added dynamically, allocate the array in the
      // beginning itself
      outAttrs = new ExpandableArray<Attr>(Constants.INITIAL_ATTRS_NUMBER);
      setDefaults();
    return;
  }

  
  public void updateSchemaStreamCross()
  {
    
  }
  
  public void updateStreamPropertyStreamCross()
  {
    return;
  }

  public int canSelectBePushed(LogOptSelect select) 
  {
    return -1;
  }

  protected void validate() throws LogicalPlanException
  {
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
    Expr ppExpr = partitionParallelExpr;
    
    if(ppExpr == null)
      return null;
    
    // it's output attributes should be same as the output attributes of it's
    // input 
    if(numOutAttrs != getInput(inputNo).getNumOutAttrs())
      return null;
    
    ArrayList<Attr> inputOutAttrs = getInput(inputNo).getOutAttrs();
    
    for(int i = 0; i < numOutAttrs ; i++)
    {
      if(!inputOutAttrs.get(i).equals(outAttrs.get(i)))
        return null;
    }
   
    LogUtil.fine(LoggerType.TRACE,
                 "Partition parallelism possible for input " + inputNo
                 + " of logical op " + operatorKind 
                 + " with expression " + ppExpr);

    return ppExpr;
  }
  
  
  /**
   * Returns true if the expression contains only equality predicates if any.
   * Returns true for normal arithmetic expressions.
   */
  protected boolean hasOnlyEqualityPredicates(Expr expr)
  {
    if(expr instanceof ComplexBoolExpr)
    {
      ComplexBoolExpr e = (ComplexBoolExpr)expr;
      return hasOnlyEqualityPredicates(e.getLeft()) 
             && hasOnlyEqualityPredicates(e.getRight());
    }
    if(expr instanceof BaseBoolExpr)
    {
      BaseBoolExpr bool = (BaseBoolExpr)expr;
      return (bool.getOper() == CompOp.EQ) 
             && hasOnlyEqualityPredicates(bool.getLeft()) 
             && hasOnlyEqualityPredicates(bool.getRight());
    }
    
    return true;
  }
    
  protected void checkUnboundStream() throws LogicalPlanException
  {
    for (int i = 0; i < getNumInputs(); i++) {
        LogOpt opt = getInput(i);
        if (opt.getIsStream()) 
        {
            throw new LogicalPlanException(LogicalPlanError.UNBOUND_STREAM_NOT_ALLOWED,
                                         new Object[]{this.operatorKind});
        }
    }  
  }
  
  // toString method override
  public String toString() {
    StringBuilder sb = new StringBuilder();

    sb.append("<LogicalOperator>");
    sb.append("<OperatorKind operatorKind=\"" + operatorKind + "\" />");

    // Process Output schema
    if (numOutAttrs != 0) {
      sb.append("<OutputAttributes>");
      for (int i = 0; i < numOutAttrs; i++)
        sb.append(outAttrs.get(i).toString());
      sb.append("</OutputAttributes>");
    }

    if (isStream == true)
      sb.append("<Stream>true</Stream>");
    else
      sb.append("<Stream>false</Stream>");

    /*
     * TODO: LATER Process output if (output != null) { sb.append("<Output>");
     * sb.append(output.toString()); sb.append("</Output>"); }
     */

    // Process Inputs
    if (numInputs != 0) {
      sb.append("<Inputs>");
      for (int i = 0; i < numInputs; i++)
        sb.append(inputs.get(i).toString());
      sb.append("</Inputs>");
    }

    sb.append("</LogicalOperator>");
    return sb.toString();
  }

  public boolean isInstantaneous() 
  { 
    return isInstantaneous;
  }
  
  public void setInstantaneous(boolean isInstantaneous) 
  {
    this.isInstantaneous = isInstantaneous;
  }
  
  public boolean isPullOperator() 
  {
    return isPullOperator;
  }
  
  public void setPullOperator(boolean isPullOperator) 
  {
    this.isPullOperator = isPullOperator;
  }

  public void setOrderingConstraint(OrderingKind orderingConstraint)
  {
    this.orderingConstraint = orderingConstraint;
  }
  
  public OrderingKind getOrderingConstraint()
  {
    return this.orderingConstraint;
  }

  public Expr getPartitionParallelExpr()
  {
    return partitionParallelExpr;
  }

  public void setPartitionParallelExpr(Expr partitionParallelExpr)
  {
    this.partitionParallelExpr = partitionParallelExpr;
  }

  public ArrayList<LogOpt> getSourceLineage()
  {
    return sourceLineage;
  }

  public void setSourceLineage(ArrayList<LogOpt> sourceLineage)
  {
    this.sourceLineage = sourceLineage;
  }

  /**
   * Set source lineages
   */
  public void setSourceLineages()
  {
    if(sourceLineage == null)
    {
      sourceLineage = new ArrayList<LogOpt>();
      for(int i=0; i <numInputs; i++)
      {
        this.getInput(i).setSourceLineages();
        sourceLineage.addAll(getInput(i).getSourceLineage());
      } 
    }
  }
}
