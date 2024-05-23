/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/phyplan/PhyOptGroupAggr.java /main/33 2013/05/07 18:03:18 sbishnoi Exp $ */

/* Copyright (c) 2006, 2013, Oracle and/or its affiliates. 
All rights reserved. */

/*
  DESCRIPTION
  Group Aggregate Physical Operator in the package oracle.cep.phyplan

  PRIVATE CLASSES
  <list of private classes defined - with one-line descriptions>
  
  NOTES
  <other useful comments, qualifications, etc.>

  MODIFIED    (MM/DD/YY)
  vikshukl    04/17/13 - pass input operator to isDependentOnChildSynAndStore()
  udeshmuk    08/13/12 - implement canConstructQuery()
  udeshmuk    05/10/12 - use cql query aliases whenever available in archiver
                         query generation
  udeshmuk    10/20/11 - API for knowing if this operator uses child's synopsis
  sbishnoi    08/27/11 - support for interval year to month
  udeshmuk    08/26/11 - propagate event identifier col name for archived rel
  udeshmuk    06/23/11 - archived relation support
  udeshmuk    06/20/11 - reflect the changed method names of archived relation
  udeshmuk    03/28/11 - archived reln support
  sborah      07/07/09 - support for bigdecimal
  sborah      03/11/09 - altering sharingHash
  hopark      02/17/09 - support boolean as external datatype
  hopark      02/16/09 - objtype support
  hopark      10/09/08 - remove statics
  udeshmuk    06/05/08 - support for xmlagg
  udeshmuk    01/30/08 - support for double data type.
  hopark      10/25/07 - set synopsis
  hopark      10/25/07 - set synopsis
  mthatte     11/01/07 - adding Datatype.getLength() to constructor
  udeshmuk    10/11/07 - allow char and byte as inputs to aggregate functions.
  sbishnoi    09/25/07 - add dirtySyn
  sbishnoi    06/09/07 - support for multi-arg UDAs
  sbishnoi    03/11/07 - support for interval aggr 
  najain      02/27/07 - bug fix
  parujain    12/20/06 - operator sharing
  hopark      11/27/06 - add bigint datatype
  rkomurav    09/28/06 - expr in aggr
  rkomurav    09/13/06 - physynpos OO restructuring
  rkomurav    08/28/06 - add genXMLPlan2
  rkomurav    08/20/06 - adding toString
  parujain    08/10/06 - max/min timestamp datatype
  anasrini    07/12/06 - support for user defined aggregations 
  najain      06/16/06 - cleanup
  anasrini    05/02/06 - support for aggr operator 
  najain      04/06/06 - cleanup
  anasrini    04/06/06 - constructor cleanup 
  najain      05/25/06 - bug fix 
  najain      03/24/06 - cleanup
  skaluska    02/15/06 - Cleanup Phy/Exec Synopsis 
  najain      02/10/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/phyplan/PhyOptGroupAggr.java /main/33 2013/05/07 18:03:18 sbishnoi Exp $
 *  @author  najain  
 *  @since   1.0
 */
package oracle.cep.phyplan;

import oracle.cep.extensibility.expr.ExprKind;

import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;
import oracle.cep.metadata.DependencyType;
import oracle.cep.metadata.Query;
import oracle.cep.phyplan.attr.Attr;
import oracle.cep.phyplan.expr.Expr;
import oracle.cep.phyplan.expr.ExprAttr;
import oracle.cep.phyplan.expr.ExprOrderBy;
import oracle.cep.service.ExecContext;
import oracle.cep.common.AggrFunction;
import oracle.cep.common.BaseAggrFn;
import oracle.cep.common.Constants;
import oracle.cep.common.Datatype;
import oracle.cep.exceptions.CEPException;

import java.util.ArrayList;
import java.util.LinkedList;

/**
 * Group Aggregation Physical Operator
 *
 * @since 1.0
 */
public class PhyOptGroupAggr extends PhyOpt {
  /** Grouping Attributes */
  Attr[] groupAttrs;

  /** Aggregated Parameter Expressions */
  ArrayList<Expr[]> aggrParamExprs;

  /** 
   * Datatype of operand of aggregation function, i.e. datatype of aggrParamExprs
   */
  ArrayList<Datatype[]> aggrInputTypes;

  /** Aggregation Function */
  BaseAggrFn[] fn;
  
  /**
   * The exprs in the orderby clause of XMLAGG. If absent or if not applicable that
   * particular entry will be null.
   */
  ArrayList<ExprOrderBy[]> orderByExprs;
  
  int numGroupAttrs;

  int numAggrParamExprs;

  StringBuffer select = null;
  
  StringBuffer grpBy  = null;
    
  /** true if at least one expression in group by clause */
  boolean isContainGroupByExpr = false;
 
  /** Synopsis for input */
  public static final int INSYN_INDEX = 0;
  /** Synopsis for output */
  public static final int OUTSYN_INDEX = 1;
  /** Synopsis for dirty tuples */
  public static final int DIRTYSYN_INDEX = 2;

  /**
   * Constructor
   * @param ec TODO
   * @param input the physical operator that is the input to this operator
   * @param groupAttrs the GROUP BY attributes
   * @param aggrParamExprs the aggregation Parameter Expressions
   * @param aggrFns the aggregation functions
   */
  public PhyOptGroupAggr(ExecContext ec, PhyOpt input, Attr[] groupAttrs,
                         ArrayList<Expr[]> aggrParamExprs, BaseAggrFn[] aggrFns, ArrayList<ExprOrderBy[]> orderByExprs)
    throws PhysicalPlanException {

    super(ec, PhyOptKind.PO_GROUP_AGGR, input, false, false);

    // output is always a relation
    setIsStream(false);

    // Needs timeout heart for progression if input goes silent
    setHbtTimeoutRequired(true);
    
    this.numGroupAttrs      = groupAttrs.length;
    this.numAggrParamExprs  = aggrParamExprs.size();
    this.groupAttrs         = groupAttrs;
    this.aggrParamExprs     = aggrParamExprs;
    this.fn                 = aggrFns;
    this.orderByExprs       = orderByExprs;
    aggrInputTypes          = new ArrayList<Datatype[]>();

    // Output Schema
    int numAttrs      = numGroupAttrs + numAggrParamExprs;
    setNumAttrs(numAttrs);

    // The type information for grouping attributes can be copied over
    // from the type information of the child operator.
    for (int a=0; a<numGroupAttrs; a++)    
    {
      setAttrMetadata(a, input.getAttrMetadata()[groupAttrs[a].getPos()]);
    }
    
    int        index;
    Datatype   aggrAttrType;
    Datatype[] aggrMultiInputTypes;
    
        
    // The type information of an aggregated attribute can be derived from
    // the aggregating function and the type information of the aggregated
    // param expression
    for (int a=numGroupAttrs; a<numAttrs; a++) {
      index               = a-numGroupAttrs;
      aggrMultiInputTypes = new Datatype[aggrParamExprs.get(index).length];
      
      for(int i=0; i < aggrParamExprs.get(index).length; i++)
      {
        aggrAttrType           = (aggrParamExprs.get(index))[i].getType();
        aggrMultiInputTypes[i] = aggrAttrType;
      
        assert aggrAttrType == Datatype.INT || 
               aggrAttrType == Datatype.BOOLEAN || 
               aggrAttrType == Datatype.BIGINT || 
               aggrAttrType == Datatype.FLOAT ||
               aggrAttrType == Datatype.DOUBLE ||
               aggrAttrType == Datatype.BIGDECIMAL ||
               aggrAttrType == Datatype.CHAR ||
               aggrAttrType == Datatype.BYTE ||
               aggrAttrType == Datatype.INTERVAL ||
               aggrAttrType == Datatype.INTERVALYM ||
               aggrAttrType == Datatype.TIMESTAMP ||
               aggrAttrType.getKind() == Datatype.Kind.OBJECT ||
               aggrAttrType == Datatype.XMLTYPE : aggrAttrType;
        //This declaration will be repeated each time for an expr of Expr Array
        setAttrTypes(a, aggrFns[index].getReturnType(aggrAttrType));

        if ((aggrAttrType == Datatype.CHAR)||(aggrAttrType == Datatype.BYTE))
        {
          ExprKind kind = aggrParamExprs.get(index)[i].getKind();
          assert kind == ExprKind.ATTR_REF || kind == ExprKind.USER_DEF
              || kind == ExprKind.CONST_VAL || kind == ExprKind.COMP_EXPR 
              || kind == ExprKind.SEARCH_CASE || kind == ExprKind.SIMPLE_CASE: kind;

          if (kind == ExprKind.ATTR_REF)
          {
            assert (aggrParamExprs.get(index))[i] instanceof ExprAttr 
              : (aggrParamExprs.get(index))[i].getClass().getName();
            ExprAttr exprAttr = (ExprAttr) (aggrParamExprs.get(index))[i];
            Attr attr = exprAttr.getAValue();
            assert attr.getInput() == 0 : attr.getInput();
            int attrPos = attr.getPos();
            assert attrPos < input.getNumAttrs() : attrPos;
            setAttrLen(a, input.getAttrLen(attrPos));
          }
          else if (kind == ExprKind.USER_DEF || kind == ExprKind.SIMPLE_CASE 
                   || kind == ExprKind.SEARCH_CASE)
            setAttrLen(a, (getAttrTypes(a) == Datatype.CHAR ? Constants.MAX_CHAR_LENGTH
                : Constants.MAX_BYTE_LENGTH));
          // use max length for now - this should be revisited later
          else
          {
            assert (getAttrTypes(a) == Datatype.CHAR);
            setAttrLen(a, Constants.MAX_CHAR_LENGTH);
          }
        }
        //Other lengths are defined as constants, getter defined in Datatype.java
        else 
          setAttrLen(a, aggrAttrType.getLength());
      }
      aggrInputTypes.add(aggrMultiInputTypes);
    }
  }

  // Getter methods
  
  /**
   * Get the number of attributes in the GROUP BY clause
   * @return the number of attributes in the GROUP BY clause
   */
  public int getNumGroupAttrs() {
    return numGroupAttrs;
  }

  /**
   * Get the number of aggregation expressions
   * @return the number of aggregation expressions
   */
  public int getNumAggrParamExprs() {
    return numAggrParamExprs;
  }

  /**
   * Get the GROUP BY attributes
   * @return the GROUP BY attributes
   */
  public Attr[] getGroupAttrs() {
    return groupAttrs;
  }

  /**
   * Get the List of attributes in the aggregation parameter expressions
   * @return the List of attributes in the aggregation parameter expressions
   */
  public ArrayList<Expr[]> getAggrParamExprs() {
    return aggrParamExprs;
  }

  /**
   * Get the aggregation functions
   * @return the aggregation functions
   */
  public BaseAggrFn[] getAggrFunctions() {
    return fn;
  }

  /**
   * Get the array of type of the operand to the aggregation function
   * @return array of the type of the operand to the aggregation function,
   *         one array element per aggregation function
   */
  public ArrayList<Datatype[]> getAggrInputTypes() {
    return aggrInputTypes;
  }

  /**
   * Get the list of orderby exprs that might occur in XMLAGG.
   * If orderby exprs are not applicable or they are not present then that 
   * entry will be null.
   * @return list of order by expressions
   */
  public ArrayList<ExprOrderBy[]> getOrderByExprs() {
    return orderByExprs;
  }
  
  // Get the synopsis
  /**
   * Get the input synopsis
   * @return the input synopsis
   */
  public PhySynopsis getInSyn() {
    return getSynopsis(INSYN_INDEX);
  }

  /**
   * Get the output synopsis
   * @return the output synopsis
   */
  public PhySynopsis getOutSyn() {
    return getSynopsis(OUTSYN_INDEX);
  }
  
  /**
   * Get the dirty synopsis
   * @return the dirty Synopsis
   */
  public PhySynopsis getDirtySyn() {
    return getSynopsis(DIRTYSYN_INDEX);
  }

  // Setter Methods

  /**
   * Set the output synopsis
   * @param outSyn the output synopsis to set
   */
  public void setOutSyn(PhySynopsis outSyn) {
    setSynopsis(OUTSYN_INDEX, outSyn);
  }

  /**
   * Set the input synopsis
   * @param inSyn the input synopsis to set
   */
  public void setInSyn(PhySynopsis inSyn) {
    setSynopsis(INSYN_INDEX, inSyn);
  }

  /**
   * Set the dirty synopsis
   * @param dirtySyn the dirty synopsis to set
   */
  public void setDirtySyn(PhySynopsis dirtySyn) {
    setSynopsis(DIRTYSYN_INDEX, dirtySyn);
  }
  
  // Related to Store Sharing and Store requirements for synopsis 
  // owned by this operator
  
  public boolean getSharedSynType(int idx) {
    if (getInSyn() != null)
      return true;
    else
      return false;
  }

  public void synStoreReq() {
    PhySynopsis inSyn = getInSyn();
    if (inSyn != null) {
      PhyStore inputStore = getInputs()[0].getSharedRelStore();
      assert inputStore != null;
      inSyn.makeStub(inputStore);
    }
    PhySynopsis dirtySyn = getDirtySyn();
    assert dirtySyn != null;
    PhyStore dirtyStore = this.getStore();
    assert dirtyStore != null;
    dirtySyn.makeStub(dirtyStore);
  }
  
  private boolean isInpSynRequired() 
  {

    // This is needed only if input is a relation and non-incremental
    // aggregation functions MAX or MIN are present
    // In case of unbounded streams where the behavior of 
    // tuples is only incremental , no input synopsis is required 
    // for both incremental and non-incremental aggregation functions.

    if (this.getInputs()[0].getIsStream())
      return false;

    int          numAggrAttrs = this.getNumAggrParamExprs();
    BaseAggrFn[] fns          = this.getAggrFunctions();

    for (int i = 0; i< numAggrAttrs; i++) 
    {
      if (!(fns[i].supportsIncremental()))
      {
        return true;
      }
    }

    return false;
  }
  
  @Override
  public boolean isDependentOnChildSynAndStore(PhyOpt input)
  {
    if(isInpSynRequired())
      return true;
    else
      return false;
  }
  
  /**
   * Method to calculate a concise String representation
   * of the Physical operator based on its attributes.
   * @return 
   *      A concise String representation of the Physical operator.
   */
  protected String getSignature()
  {
    StringBuilder expr = new StringBuilder();
    
    Expr[]        tempExpr;
    ExprOrderBy[] tempOrderExpr;
    Datatype[]    tempAggrInputType;
    
    expr.append(this.getOperatorKind());
    
    if (groupAttrs.length != 0)
    {
      expr.append("#GrpAttrs:");
      for (int i = 0; i < groupAttrs.length; i++)
      {
        expr.append(groupAttrs[i].getSignature());
      }
    }
    if (aggrParamExprs.size() != 0)
    {
      expr.append("#Exprs:");
      for (int i = 0; i < aggrParamExprs.size(); i++)
      {
        expr.append("{");
        
        tempExpr = aggrParamExprs.get(i);
        tempAggrInputType = aggrInputTypes.get(i);
        
        for (int j = 0; j < tempExpr.length; j++)
        {
          expr.append("(");
          expr.append(tempExpr[j].getSignature());
          
          expr.append(",Input:");
          expr.append(tempAggrInputType[j].toString());
          
          expr.append(")");
        }
        
        tempOrderExpr = orderByExprs.get(i);
        if (tempOrderExpr != null)
        {
          for (int j = 0; j < tempOrderExpr.length; j++)
          {
            expr.append(tempOrderExpr[j].getSignature());
          }
        }
        expr.append("#Fn:");
        expr.append(fn[i].getFnCode().toString());
        
        expr.append("," + fn[i].getFnType().toString());
        
        expr.append("}");
      }
    }
    LogUtil.info(LoggerType.TRACE, "GrpExpr : ["+expr.toString()+"]");
    
    return expr.toString();
  }
  
  
  public String toString() {
    StringBuilder sb = new StringBuilder();
    Expr[]        tempExpr;
    ExprOrderBy[] tempOrderExpr;
    Datatype[]    tempAggrInputType;
    sb.append("<PhysicalOperatorGroupAggregate>");
    sb.append(super.toString());
 
    if (groupAttrs.length != 0) {
      sb.append("<GroupAttributes>");
      for ( int i = 0; i < groupAttrs.length; i++) {
        sb.append(groupAttrs[i].toString());
      }
      sb.append("</GroupAttributes>");
    }
    
    if (aggrParamExprs.size() != 0) {
      sb.append("<Expressions>");
      for ( int i = 0; i < aggrParamExprs.size(); i++) {       
        sb.append("<Expression>");
        
        tempExpr          = aggrParamExprs.get(i);
        tempAggrInputType = aggrInputTypes.get(i);
        
        for(int j=0; j < tempExpr.length; j++)
        {
          sb.append("<ParameterExpression>");
          sb.append(tempExpr[j].toString());
        
          sb.append("<InputType>");
          sb.append(tempAggrInputType[j].toString());
          sb.append("</InputType>");
          sb.append("</ParameterExpression>");
        } 
        
        // print the orderby Exprs if they exist
        tempOrderExpr = orderByExprs.get(i);
        if(tempOrderExpr != null)
        {
          for(int j=0; j < tempOrderExpr.length; j++)
          {
            sb.append("<OrderByExpression>");
            sb.append(tempOrderExpr[j].toString());
            sb.append("</OrderByExpression>");
          }
        }
        sb.append("<AggregateFunction>");
        sb.append("<FunctionCode>");
        sb.append(fn[i].getFnCode().toString());
        sb.append("</FunctionCode>");
        sb.append("<FunctionType>");
        sb.append(fn[i].getFnType().toString());
        sb.append("</FunctionType>");
        sb.append("</AggregateFunction>");
        
        sb.append("</Expression>");
      }
      sb.append("</Expressions>");
    }
    
    sb.append("<NumberOfGroupAttributes>");
    sb.append(numGroupAttrs);
    sb.append("</NumberOfGroupAttributes>");
    
    sb.append("<NumberOfAggregateAttributes>");
    sb.append(numAggrParamExprs);
    sb.append("</NumberOfAggregateAttributes>");

    
    PhySynopsis inSyn = getInSyn();
    if (inSyn != null) {
      sb.append("<InnerSynopsis>");
      sb.append(inSyn.toString());
      sb.append("</InnerSynopsis>");
    }
    
    PhySynopsis dirtySyn = getDirtySyn();
    sb.append("<DirtySynopsis>");
    sb.append(dirtySyn.toString());
    sb.append("</DirtySynopsis>");
   
                                                                                                                             
    PhySynopsis outSyn = getOutSyn();
    sb.append("<OuterSynopsis>");
    sb.append(outSyn.toString());
    sb.append("</OuterSynopsis>");
    
    sb.append("</PhysicalOperatorGroupAggregate>");
    return sb.toString();
  }
  
  //Create and return visualiser compatible XML Plan
  public String getXMLPlan2() throws CEPException {
    StringBuilder xml = new StringBuilder();
    int i = 0;
    int exprArrayLength = 0;
    int orderArrayLength = 0;
    xml.append("<name> Aggr </name>\n");
    xml.append("<lname> Group By Aggregation </lname>\n");
    xml.append(super.getXMLPlan2());
    xml.append("<property name = \"Grouping Attrs\" value = \"");
    if(groupAttrs.length != 0) {
      for(i = 0; i < (groupAttrs.length - 1); i++) {
        xml.append(groupAttrs[i].getXMLPlan2());
        xml.append(",");
      }
      xml.append(groupAttrs[i].getXMLPlan2());
    }
    else {
      xml.append("(null)");
    }
    xml.append("\"/>\n");
    
    xml.append("<property name = \"Aggrs\" value = \"");
    if(aggrParamExprs.size() != 0) {
      for(i = 0; i < aggrParamExprs.size(); i++) {
        xml.append(fn[i].getFnCode().toString());
        xml.append("(");
        exprArrayLength = aggrParamExprs.get(i).length;
        
        for(int j=0 ; j<exprArrayLength; j++)
        {
          xml.append((aggrParamExprs.get(i))[j].getXMLPlan2());
          if(j < exprArrayLength - 1)
            xml.append(",");
        }
        //dump orderby exprs as well
        if(orderByExprs.get(i) != null)
        {
          xml.append(" order by ");
          orderArrayLength = orderByExprs.get(i).length;
          for(int j=0; j < orderArrayLength; j++)
          {
            xml.append((orderByExprs.get(i))[j].getXMLPlan2());
            if(j < orderArrayLength - 1)
              xml.append(",");
          }
        }
        xml.append(")");
        if(i < aggrParamExprs.size() - 1)
          xml.append(",");
      }
    }
    else {
      xml.append("(null)");
    }
    xml.append("\"/>\n");
    
    return xml.toString();
  }
  
  //get the Synopsis Position
  public String getRelnSynPos(PhySynopsis syn) {
    PhySynopsis inSyn = getInSyn();
    PhySynopsis dirtySyn = getDirtySyn();
    assert(inSyn == syn || getOutSyn() == syn || dirtySyn == syn);
    if(inSyn == syn)
      return PhySynPos.CENTER.getName();
    else if(dirtySyn == syn)
      return PhySynPos.LEFT.getName();
    else
      return PhySynPos.OUTPUT.getName();
  }

  
  /**
   * This method tells whether the two operators are partially equivalent or not
   */
  public boolean isPartialEquivalent(PhyOpt opt)
  {
    if(!(opt instanceof PhyOptGroupAggr))
      return false;
  
   // this is to avoid finding the same operator in PlanManager list
    if(opt.getId() == this.getId())
      return false;
 
    PhyOptGroupAggr groupOpt = (PhyOptGroupAggr)opt;
  
    assert groupOpt.getOperatorKind() == PhyOptKind.PO_GROUP_AGGR;
  
    if(groupOpt.getNumInputs() != this.getNumInputs())
      return false;
  
    if(groupOpt.getNumAttrs() != this.getNumAttrs())
      return false;
   
    if(numAggrParamExprs != groupOpt.numAggrParamExprs)
      return false;    
    
    if(numGroupAttrs != groupOpt.numGroupAttrs)
      return false;
    
    if(fn.length != groupOpt.fn.length)
      return false;
    
    if(!compareFunction(groupOpt))
      return false;

    if(!compareGroupAttrs(groupOpt))
      return false;
 
    if (!compareAggrExpr(groupOpt))
      return false;
    
    return (compareOrderByExprs(groupOpt));
    
  }
 
  private boolean compareFunction(PhyOptGroupAggr opt)
  {
    for(int i=0; i < fn.length; i++)
    {
      if(!fn[i].equals(opt.fn[i]))
        return false;
    }
    return true;
  }

  private boolean compareGroupAttrs(PhyOptGroupAggr opt)
  {
    for(int i=0; i < numGroupAttrs; i++)
    {
      if(! groupAttrs[i].equals(opt.groupAttrs[i]))
        return false;
    }
    
    return true;
  }
 
  private boolean compareAggrExpr(PhyOptGroupAggr opt)
  {
    Expr[]     sourceTempExpr;
    Datatype[] sourceTempInputTypes;
    Expr[]     targetTempExpr;
    Datatype[] targetTempInputTypes;
    
    for(int i=0; i < numAggrParamExprs; i++)
    {
      sourceTempExpr       = aggrParamExprs.get(i);
      targetTempExpr       = opt.aggrParamExprs.get(i);
      sourceTempInputTypes = aggrInputTypes.get(i);
      targetTempInputTypes = opt.aggrInputTypes.get(i);
      
      if(sourceTempExpr.length != targetTempExpr.length)
        return false;
      for(int j=0; j < sourceTempExpr.length;  j++)
      {
        if(sourceTempExpr[j].getKind() != targetTempExpr[j].getKind())
          return false;
 
        if(sourceTempInputTypes[j] != targetTempInputTypes[j])
          return false;
        
        if(! sourceTempExpr[j].equals(targetTempExpr[j]))
          return false;
      }
  
      if(getAttrLen(i) != opt.getAttrLen(i))
        return false;
    }
    return true;
  }
  
  private boolean compareOrderByExprs(PhyOptGroupAggr opt)
  {
    ExprOrderBy[] sourceExpr;
    ExprOrderBy[] targetExpr;
    //for every aggregation attr
    for(int i=0; i < orderByExprs.size(); i++)
    {
      sourceExpr = orderByExprs.get(i);
      targetExpr = opt.orderByExprs.get(i);
      
      if(sourceExpr != null)
      {
        if(targetExpr == null)
          return false;
        else
        {
          if(sourceExpr.length != targetExpr.length)
            return false;
          for(int j=0; j < sourceExpr.length; j++)
          {
            if(sourceExpr[j].getKind() != targetExpr[j].getKind())
              return false;
            
            if(! sourceExpr[j].equals(targetExpr[j]))
              return false;
          }
        }
      }
      else
      { //orderbyExprs.get(i) == null
        if(targetExpr != null)
          return false;
      }
    }
    
    return true;
  }  
  
  //archived relation support related
  
  public boolean isStateFul()
  {
    return true;
  }
 
  public boolean canBeQueryOperator() throws CEPException
  {
    //Non-incremental aggrs expect synopsis from child.
    //So if we have a non-incremental aggr, this opt cannot be query operator.
    for(int i=0; i < fn.length; i++)
    {
      switch(fn[i].getFnCode())
      {
        case COUNT_CORR_STAR:
        case FIRST:
        case LAST:
        case USER_DEF:
        case MAX:
        case MIN:
        case XML_AGG: return false;
      }
    }
    
    return true;
  }
  
  public boolean canConstructQuery(Query q) throws CEPException
  {
   
    //first check if input SQL is null, if so return false.
    PhyOpt[] inputs = this.getInputs();
    if(inputs[0].getOutputSQL() == null)
      return false;
    
    select = new StringBuffer();
    grpBy  = new StringBuffer();
    if(isContainGroupByExpr)
    {
      assert this.getInputs()[0] instanceof PhyOptProject;
      String[] projAliases = ((PhyOptProject)this.getInputs()[0]).getAliases();
      for(int i=0; i < groupAttrs.length; i++)
      {
        int pos = groupAttrs[i].getPos();
        //set the alias of the project expr as the name of the attribute
        groupAttrs[i].setActualName(projAliases[pos]);
      }
    }
    
    this.projEntries = new LinkedList<String>();
    this.projTypes = new LinkedList<Datatype>();
    
    //First go through the functions :
    //for max and min opt can construct query, but cannot be query operator
    for(int i=0; i < fn.length; i++)
    {
      switch(fn[i].getFnCode())
      {
        case COUNT_CORR_STAR:
        case FIRST:
        case LAST:
        case USER_DEF:
        case XML_AGG: return false;
      }
    }
    
    //Go through the group by attrs to check if sqlEquivalent is possible for each
    //simultaneously construct group by clause
    if(numGroupAttrs > 0)
    {
      boolean commaRequired = false;
      for(int k=0; k < groupAttrs.length; k++)
      {
        if(commaRequired)
          grpBy.append(", ");
        //if any of the groupby attr doesn't have sqlequivalent then return false.
        String temp = groupAttrs[k].getSQLEquivalent(this.execContext);
        if(temp == null)
        {
          grpBy = null;
          return false; 
        }
        else
        {
          grpBy.append(temp+" as "+temp);
          projEntries.add(temp+" as "+temp);
          projTypes.add(groupAttrs[k].getType());
        }
        
        commaRequired = true;
      }
    }
    else
      grpBy = null;
    
    //Go through parameters of the aggrs and check if sqlequivalent is possible for each
    //simultaneously construct select clause
    boolean commaRequired = false;
    int aliasIndex = 0;    
    for(int i=0; i < fn.length; i++)
    {
      if(commaRequired)
        select.append(", ");
      
      if(fn[i].getFnCode() == AggrFunction.COUNT_STAR)
      {
        select.append("count(*) as "+this.getOptName()+"_alias"+aliasIndex++);
        projEntries.add("count(*) as "+this.getOptName()+"_alias"+(aliasIndex - 1));
        projTypes.add(Datatype.INT);
      }
      else
      {
        StringBuffer projEntry = new StringBuffer();
        select.append(fn[i].getFnCode().toString().toLowerCase()+"(");
        projEntry.append(fn[i].getFnCode().toString().toLowerCase()+"(");
        
        Expr[] params = aggrParamExprs.get(i);
        if(params != null)
        {
          boolean comRequired = false;
          for(int j=0; j < params.length; j++)
          {
            if(comRequired)
            {
              select.append(", ");
              projEntry.append(", ");
            }
            String param = params[j].getSQLEquivalent(this.execContext);
            //if parameter does not have sqlEquivalent this cannot be query operator
            if(param == null)
            {
              select = null;
              return false;
            }
              
            select.append(param);
            projEntry.append(param);
            comRequired = true;
          }
         
        }
        select.append(") as "+this.getOptName()+"_alias"+aliasIndex++);
        projEntry.append(") as "+this.getOptName()+"_alias"+(aliasIndex - 1));
        projEntries.add(projEntry.toString());
        if(params != null)
          projTypes.add(fn[i].getFnCode().getReturnType(params[0].getType()));
      }
      
      commaRequired = true;
    }
    
    //group by attrs first since this is the order followed in outattrs
    //insert grpby attrs before aggrs
    if(grpBy != null)
    {
      select.insert(0, grpBy);
      select.insert(grpBy.length(), ", ");
    }
     
    return true;
  }
  
  public void updateArchiverQuery() throws CEPException
  {
    StringBuffer query = new StringBuffer("select ");
    
    if((select != null) && (select.length() > 0))
      query.append(select.toString());
    
    query.append(" from ( ");

    PhyOpt[] children = this.getInputs();
 
    this.setArchiverName(children[0].getArchiverName());
    if(!this.isView)
    {
      //if this operator were a view root event id fields would be set in 
      //canConstructQuery().
      this.setEventIdColName(children[0].getEventIdColName());
      //GroupAggr doesn't add the event id col explicitly
      this.setEventIdColNum(-1);
      this.setEventIdColAddedToProjClause(false);
    }
    
    query.append(children[0].getOutputSQL());
    query.append(" ) "+this.getOptName());
    
    if((grpBy != null) && (grpBy.length() > 0))
    {
      StringBuffer groupBy = new StringBuffer("");
      boolean commaRequired = false;
      for(int i=0; i < numGroupAttrs; i++)
      {
        if(commaRequired)
          groupBy.append(", ");
        groupBy.append((projEntries.get(i).split(" as "))[0].trim());
        commaRequired = true;
      }
      query.append(" group by " + groupBy.toString());
    }
    this.setOutputSQL(query.toString());
  }

  public boolean isContainGroupByExpr()
  {
    return isContainGroupByExpr;
  }

  public void setContainGroupByExpr(boolean isContainGroupByExpr)
  {
    this.isContainGroupByExpr = isContainGroupByExpr;
  }
}

