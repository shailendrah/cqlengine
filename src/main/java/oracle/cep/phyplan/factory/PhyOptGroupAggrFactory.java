/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/phyplan/factory/PhyOptGroupAggrFactory.java /main/14 2012/05/17 06:50:33 udeshmuk Exp $ */

/* Copyright (c) 2006, 2012, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    Factory for physical operator Group Aggregation. Contains the code
    that transform the logical Group Aggregation operator to the physical
    Group Aggregation operator

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    udeshmuk    05/12/12 - exempt the project for aggr distinct from useless
                           project opt
    sborah      10/12/09 - support for bigdecimal
    sborah      04/24/09 - pass phyChildren to constructor
    sborah      12/17/08 - handle constants
    hopark      10/09/08 - remove statics
    udeshmuk    05/27/08 - add qryId while introducing operator at phy level
                           directly
    udeshmuk    04/13/08 - support for aggr distinct
    sbishnoi    06/09/07 - support for multi-arg UDAs
    rkomurav    06/18/07 - cleanup
    rkomurav    03/05/07 - rework on attr factory
    rkomurav    12/18/06 - count to count_star
    rkomurav    11/28/06 - shift implicit addition of aggregate exprs to
                           logical level
    rkomurav    09/27/06 - expr in aggr
    anasrini    07/12/06 - support for user defined aggregations 
    anasrini    05/02/06 - Creation
    anasrini    05/02/06 - Creation
    anasrini    05/02/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/phyplan/factory/PhyOptGroupAggrFactory.java /main/14 2012/05/17 06:50:33 udeshmuk Exp $
 *  @author  anasrini
 *  @since   1.0
 */

package oracle.cep.phyplan.factory;

import java.util.LinkedList;
import java.util.Vector;

import oracle.cep.logplan.LogOpt;
import oracle.cep.logplan.attr.AttrAggr;
import oracle.cep.logplan.attr.AttrXMLAgg;
import oracle.cep.logplan.LogOptGrpAggr;
import oracle.cep.phyplan.expr.BaseBoolExpr;
import oracle.cep.phyplan.expr.ComplexBoolExpr;
import oracle.cep.phyplan.expr.BoolExpr;
import oracle.cep.phyplan.expr.Expr;
import oracle.cep.phyplan.expr.ExprAttr;
import oracle.cep.phyplan.expr.ExprOrderBy;
import oracle.cep.phyplan.PhyOpt;
import oracle.cep.phyplan.PhyOptDistinct;
import oracle.cep.phyplan.PhyOptGroupAggr;
import oracle.cep.phyplan.PhyOptJoinProject;
import oracle.cep.phyplan.PhyOptProject;
import oracle.cep.phyplan.attr.Attr;
import oracle.cep.service.ExecContext;
import oracle.cep.common.AggrFunction;
import oracle.cep.common.AttributeMetadata;
import oracle.cep.common.BaseAggrFn;
import oracle.cep.common.CompOp;
import oracle.cep.common.Datatype;
import oracle.cep.common.LogicalOp;
import oracle.cep.common.UnaryOp;
import oracle.cep.exceptions.CEPException;
import java.util.ArrayList;

/**
 * Factory for physical operator Group Aggregation.
 * <p>
 * Contains the code that transform the logical Group Aggregation operator 
 * to the physical Group Aggregation operator
 *
 * @since 1.0
 */

class PhyOptGroupAggrFactory extends PhyOptFactory {

  PhyOpt newPhyOpt(Object ctx) throws CEPException {
    
    assert ctx instanceof LogPlanInterpreterFactoryContext;

    LogPlanInterpreterFactoryContext ctx1 = 
      (LogPlanInterpreterFactoryContext) ctx;
    ExecContext ec = ctx1.getExecContext();

    PhyOptGroupAggr                          phyGroupAggr;
    PhyOpt[]                                 phyChildren;
    PhyOpt                                   phyChild;
    LogOptGrpAggr                            logGroupAggr;
    LogOpt                                   logop;
    int                                      numGroupAttrs;
    int                                      numAggrAttrs;
    ArrayList<oracle.cep.logplan.attr.Attr>  logGroupAttrs;
    ArrayList<oracle.cep.logplan.expr.Expr>  logGroupExprs; 
    ArrayList<AttrAggr>                      logAggrAttrs;
    Attr[]                                   phyGroupAttrs;
    Expr[]                                   phyGroupExprs;
    ArrayList<Expr[]>                        phyAggrParamExprs;
    ArrayList<ExprOrderBy[]>                 phyOrderByExprs;
    BaseAggrFn[]                             aggrFns;
    BaseAggrFn                               fn;
    Vector<BaseAggrFn>                       fnsVec;
    Vector<Expr[]>                           aggrParamExprsVec;
    Vector<ExprOrderBy[]>                    orderByExprsVec;
    
    logop       = ctx1.getLogPlan();
    phyChildren = ctx1.getPhyChildPlans();

    assert logop != null;
    assert logop instanceof LogOptGrpAggr : logop.getClass().getName();
    logGroupAggr = (LogOptGrpAggr)logop;

    assert logop.getNumInputs() == 1 : logop.getNumInputs();
    assert phyChildren != null;
    assert phyChildren.length == 1 : phyChildren.length;
    phyChild = phyChildren[0];
    assert phyChild != null;

    numGroupAttrs = logGroupAggr.getNumGroupAttrs();
    numAggrAttrs  = logGroupAggr.getNumAggrAttrs();
    logGroupAttrs = logGroupAggr.getGroupAttrs();
    logGroupExprs = logGroupAggr.getGroupExprs();
    logAggrAttrs  = logGroupAggr.getAggrAttrs();   
    
    /*
     * Convert the groupAttrs to physical form
     * Also convert the groupByExpressions to physical form (used only in case 
     * of distinct) 
     */
    phyGroupAttrs = new Attr[numGroupAttrs];
    phyGroupExprs = new Expr[numGroupAttrs];
    for (int i=0; i<numGroupAttrs; i++) {
      // transform logical attribute to physical attribute
      phyGroupAttrs[i] = LogPlanAttrFactory.getInterpreter(logop,
          logGroupAttrs.get(i));
      // transform logical expr to physical expr
      phyGroupExprs[i] = LogPlanExprFactory.getInterpreter(logGroupExprs.get(i),
          new LogPlanExprFactoryContext(logGroupExprs.get(i), ctx1.getLogPlan()
                                      , phyChildren));
    }
    
    /*
     * Convert the logAggrAttrs to physical form:
     * 1. Creates a vector of aggregate functions
     * 2. Creates a vector of Expr[] containing parameters of the corresponding
     * aggregate function
     * 3. Create a vector of ExprOrderBy[] containing order by exprs of the
     * corresponding function. If not present that particular entry will be null.
     */
    fnsVec            = new Vector<BaseAggrFn>(numAggrAttrs);
    aggrParamExprsVec = new Vector<Expr[]>(numAggrAttrs);
    orderByExprsVec   = new Vector<ExprOrderBy[]>(numAggrAttrs);
    
    for (int i=0; i<numAggrAttrs; i++) {
      fn = logAggrAttrs.get(i).getFunction();
      fnsVec.add(fn);
      if(isBuiltInAggrFn(fn, AggrFunction.COUNT_STAR)) {
        if (!logAggrAttrs.get(i).getIsDistinct())
        {
          ExprAttr e = new ExprAttr(Datatype.INT);
          e.setAValue(new Attr(0,0));
          
          Expr[] countAggrExpr = new Expr[1];
          countAggrExpr[0]     = e;
          aggrParamExprsVec.add(countAggrExpr);
        }
        else 
        { //distinct expr
          assert logAggrAttrs.get(i).getAggrParamExprLength() == 1;
          oracle.cep.logplan.expr.Expr countExpr = null;
          Expr[] countAggrExpr = new Expr[1];
          countExpr = (logAggrAttrs.get(i).getAggrParamExpr())[0];
          countAggrExpr[0] = LogPlanExprFactory.getInterpreter(countExpr,
            new LogPlanExprFactoryContext(countExpr, ctx1.getLogPlan(), phyChildren));
          aggrParamExprsVec.add(countAggrExpr);
        }
      }
      else
      {
        Expr[] aggrParamExprs = new Expr[logAggrAttrs.get(i).getAggrParamExprLength()];
        
        //convert the logical param expression to the physical expression
        for(int j=0 ; j< logAggrAttrs.get(i).getAggrParamExprLength(); j++)
        {
          aggrParamExprs[j] = (LogPlanExprFactory.getInterpreter(
                                (logAggrAttrs.get(i).getAggrParamExpr())[j],
                                new LogPlanExprFactoryContext(
                                  (logAggrAttrs.get(i).getAggrParamExpr())[j],
                                  ctx1.getLogPlan(), phyChildren)));
        }
        aggrParamExprsVec.add(aggrParamExprs);
      }
      
      if(isBuiltInAggrFn(fn, AggrFunction.XML_AGG)){
        //XMLAGG handling
        ExprOrderBy[] orderExprs;
        assert logAggrAttrs.get(i) instanceof AttrXMLAgg;
        AttrXMLAgg xmlAggAttr = (AttrXMLAgg) logAggrAttrs.get(i);
        if(xmlAggAttr.getOrderByExprs() == null)
          orderExprs = null;
        else
        { 
          int numOrderExprs = xmlAggAttr.getOrderByExprs().length;
          orderExprs = new ExprOrderBy[numOrderExprs];
          //convert the logical order by expr to physical form
          for(int idx=0; idx < numOrderExprs; idx++)
          {
            orderExprs[idx] = (ExprOrderBy)LogPlanExprFactory.getInterpreter(
                              (xmlAggAttr.getOrderByExprs())[idx],
                              new LogPlanExprFactoryContext(
                              (xmlAggAttr.getOrderByExprs())[idx],
                              ctx1.getLogPlan(), phyChildren));
          }
        }
        orderByExprsVec.add(orderExprs);
      }
      else //non-XMLAGG function
        orderByExprsVec.add(null);
    }

    /*
     * Determine if any special processing (creation of operator tree) is needed.
     * If at least one distinct expression is there then speciall processing
     * is needed.
     */
    boolean atLeastOneDistinct = false;
    for (int i=0; ((i < numAggrAttrs) && !atLeastOneDistinct) ; i++)
    {
      atLeastOneDistinct = atLeastOneDistinct || logAggrAttrs.get(i).getIsDistinct();
    }
    
    if (!atLeastOneDistinct)
    {
      /*
       * No distinct is present i.e. no aggrexpr of form aggrfn(distinct expr).
       * Create a single PhyOptGroupAggr object from the data collected earlier.
       */
      phyAggrParamExprs = new ArrayList<Expr[]>();
      for(int i=0; i < aggrParamExprsVec.size(); i++)
        phyAggrParamExprs.add(aggrParamExprsVec.elementAt(i));
      
      phyOrderByExprs = new ArrayList<ExprOrderBy[]>();
      for(int i=0; i < orderByExprsVec.size(); i++)
        phyOrderByExprs.add(orderByExprsVec.elementAt(i));
      
      aggrFns           = fnsVec.toArray(new BaseAggrFn[0]);
      phyGroupAggr      = new PhyOptGroupAggr(ec, phyChild, phyGroupAttrs, 
                                              phyAggrParamExprs, aggrFns, phyOrderByExprs);
      phyGroupAggr.setContainGroupByExpr(logGroupAggr.isContainGroupByExpr());
      return phyGroupAggr;
    }
    else
    {
      /*
       * Distinct clause present inside aggregate function.
       * Need to do special processing.
       */
      // Query id to be added in every newly created operator's qryIds list.
      int qryId = ((LogPlanInterpreterFactoryContext)ctx).getQuery().getId();
      // declared here and not in for loop so that start of non-distinct exprs can by found
      int i=0;
      // All the PhyOptGroupAggr objects created will be collected in this list
      ArrayList<PhyOptGroupAggr> grpAggrObjects   = new ArrayList<PhyOptGroupAggr>();
      // AggrFns for a particular PhyOptGroupAggr object
      ArrayList<BaseAggrFn> fnsList = new ArrayList<BaseAggrFn>();
      // Param exprs for the AggrFns of a particular PhyOptGroupAggr object
      ArrayList<Expr[]> exprsList;
      // Order by exprs for the AggrFns of a particular PhyOptGroupAggr object
      ArrayList<ExprOrderBy[]> orderExprsList;
      // List of project exprs for PhyOptProject
      Expr[] projList;
      // Array of Group By attributes for the PhyOptGroupAggr object
      Attr[] phyGroupAttrsForNewGrpAggr = new Attr[numGroupAttrs];
      // Create groupByAttrs array for the new phyOptGrpAggr
      for (int j=0; j < numGroupAttrs; j++)
      {
        phyGroupAttrsForNewGrpAggr[j] = new Attr(0,j);
      }

      /* 
       * The logAggrAttrs having isDistinct as true will appear ahead of those 
       * who have it set to false.
       * The logAggrAttrs having same param exprs for the AggrFn will appear one
       * after the other in logAggrAttrs array.
       * In the for loop below we process only those which have isDistinct true.
       * We find the groups of logAggrAttrs having same param exprs.
       * orderByExprs will have null entries for all logAggrAttrs whose isDistinct is true
       * since xmlagg(distinct..) is not allowed.
       */
      // If the control comes here we are guaranteed that first logAggrAttr will
      // have isDistinct true
      oracle.cep.logplan.expr.Expr[] prev = logAggrAttrs.get(0).getAggrParamExpr();
      
      // The loop should start from 0 and not 1 so that the processing of collecting
      // aggrFns works correctly
      for (i=0; ((i < numAggrAttrs) && (logAggrAttrs.get(i).getIsDistinct())); i++)
      {
        oracle.cep.logplan.expr.Expr[] curr = logAggrAttrs.get(i).getAggrParamExpr();
        //Compare logical exprs.
        //Need to check only first pos in the array as when distinct appears 
        //we cannot have multiple args.
        if (curr[0].equals(prev[0]))
        {
          fnsList.add(fnsVec.elementAt(i));
          prev = curr;
        }
        else
        { // One distinct grp ends so create the corresponding operators
          
          // Construct project exprs list for PhyOptProject.
          // Copy the physical layer group by exprs.
          projList = new Expr[numGroupAttrs + 1];
          for (int index=0; index < numGroupAttrs; index++)
          {
            /*
            Attr grpAttr = new Attr(0,phyGroupAttrs[index].getPos());
            projList[index] = new ExprAttr(grpAttr, phyGroupExprs[index].getType());
            */
            projList[index] = phyGroupExprs[index];
          }
          
          // Fill the common param expr as the last entry
          assert i > 0;
          projList[numGroupAttrs] = ((Expr[])aggrParamExprsVec.elementAt(i-1))[0];
          Datatype commExprType = projList[numGroupAttrs].getType();
          
          // Construct PhyOptProject
          PhyOptProject proj = new PhyOptProject(ec, phyChild, projList);
          proj.addQryId(qryId);
          proj.setIsExemptFromUselessOpt(true);
  
          // Construct PhyOptDistinct 
          PhyOptDistinct dist = new PhyOptDistinct(ec, proj);
          dist.addQryId(qryId);

          // Create parameter expressions list for AggrFns in PhyOptGroupAggr
          // It will be the common expression.
          // Also create the orderbyexprs list
          exprsList      = new ArrayList<Expr[]>();
          orderExprsList = new ArrayList<ExprOrderBy[]>();
          for (int j=0; j < fnsList.size(); j++)
          {
            Expr[] params = new Expr[1];
            params[0] = new ExprAttr(new Attr(0,numGroupAttrs), commExprType);
            exprsList.add(params);
            orderExprsList.add(null); 
          }
          // Create the new PhyOptGroupAggr object
          phyGroupAggr = new PhyOptGroupAggr(ec,dist,
                             phyGroupAttrsForNewGrpAggr, exprsList,
                             fnsList.toArray(new BaseAggrFn[fnsList.size()]), orderExprsList);
          phyGroupAggr.setContainGroupByExpr(logGroupAggr.isContainGroupByExpr());
          // Add the PhyOptGroupAggr into the array list
          grpAggrObjects.add(phyGroupAggr);
          // Clear the temporary data structures
          fnsList.clear();
          // Populate fnsList with the next group's first AggrFn
          fnsList.add(fnsVec.elementAt(i));
          // Update prev
          prev = curr;
        }
      }
      
      /*
       * Processing for the last distinct group
       */ 
      // Construct project exprs list for PhyOptProject.
      // Copy the physical layer group by exprs.
      projList = new Expr[numGroupAttrs + 1];
      for (int index=0; index < numGroupAttrs; index++)
      {
        projList[index] = phyGroupExprs[index];
      }
      projList[numGroupAttrs] = ((Expr[])aggrParamExprsVec.elementAt(i-1))[0];
      Datatype commExprType = projList[numGroupAttrs].getType();
      // Construct PhyOptProject
      PhyOptProject proj = new PhyOptProject(ec, phyChild, projList);
      proj.addQryId(qryId);
      proj.setIsExemptFromUselessOpt(true);

      // Construct PhyOptDistinct
      PhyOptDistinct dist = new PhyOptDistinct(ec, proj);
      dist.addQryId(qryId);

      // Create parameter expressions list for AggrFns in PhyOptGroupAggr
      // It will be the common expression.
      // Also create orderByExprs list
      exprsList      = new ArrayList<Expr[]>();
      orderExprsList = new ArrayList<ExprOrderBy[]>();
      for (int j=0; j < fnsList.size(); j++)
      {
        Expr[] params = new Expr[1];
        params[0] = new ExprAttr(new Attr(0,numGroupAttrs), commExprType);
        exprsList.add(params);
        orderExprsList.add(null);
      }
      phyGroupAggr = new PhyOptGroupAggr(ec, dist,
                         phyGroupAttrsForNewGrpAggr,exprsList,
                         fnsList.toArray(new BaseAggrFn[fnsList.size()]), orderExprsList);
      phyGroupAggr.setContainGroupByExpr(logGroupAggr.isContainGroupByExpr());
      // add the PhyOptGroupAggr into the array list
      grpAggrObjects.add(phyGroupAggr);
      // clear temporary data structures
      fnsList.clear();
      
      /*
       * Construct PhyOptGroupAggr for non-distinct expressions, if present.
       * Here no need to create new ExprAttrs as params and the earlier converted
       * physical exprs can be used as this will take input directly from
       * the operator which was below LogOptGroupAggr in logical plan.
       * Same goes for group attrs.
       * Also the converted order by exprs need to be used
       */ 
      if (i < numAggrAttrs)//using i from the previous for loop
      { //some non-distinct exprs present
        exprsList      = new ArrayList<Expr[]>();
        orderExprsList = new ArrayList<ExprOrderBy[]>();
        for (int j=i; j < numAggrAttrs; j++) 
        {
          fnsList.add(fnsVec.elementAt(j));
          exprsList.add(aggrParamExprsVec.elementAt(j));
          orderExprsList.add(orderByExprsVec.elementAt(j));
        }
        assert exprsList.size() == fnsList.size();
        assert orderExprsList.size() == fnsList.size();
        phyGroupAggr = new PhyOptGroupAggr(ec, phyChild, phyGroupAttrs,
                                           exprsList,
                                           fnsList.toArray(new BaseAggrFn[fnsList.size()]), orderExprsList);
        phyGroupAggr.setContainGroupByExpr(logGroupAggr.isContainGroupByExpr());
        grpAggrObjects.add(phyGroupAggr);
      }
      
      //if only one PhyOptGroupAggr then it should be returned.
      if (grpAggrObjects.size()==1)
        return grpAggrObjects.get(0);
        
      /*
       * Now we have created the PhyOptGroupAggr objects for all distinct groups
       * and one for non-distinct group if it exists.
       * We need to join the root of these branches using PhyOptJoinProject.
       */
      LinkedList<BoolExpr> predList = null;
      PhyOptJoinProject phyJoin = null;
      PhyOpt inputs[]; 
      
      for (int k=1; k < grpAggrObjects.size(); k++)
      {
        PhyOptJoinProject root = new PhyOptJoinProject(ec);
        inputs = new PhyOpt[2];
        if(k==1)
          inputs[0] = grpAggrObjects.get(0);
        else
          inputs[0] = phyJoin;
        inputs[1] = grpAggrObjects.get(k);
        /* 
         * adding the qryId to the children operators.
         * For group aggr operators the qryId is added here and not when they are created since
         * we want to be make sure that we have more than one group aggrs.
         * In case of single group aggr that will be returned and query id will get populated in PhyPlanGen.
         * For the same reason, we don't add qryId in the topmost joinproject as well.
         */
        inputs[0].addQryId(qryId);
        inputs[1].addQryId(qryId);

        /*
         * Set up the operator PhyOptJoinProject
         */
        root.setNumInputs(2);
        root.setInputs(inputs);
        root.setIsStream(false);
        root.setExternal(false);
        int numAttrs = inputs[0].getNumAttrs()+(inputs[1].getNumAttrs()
                                              - numGroupAttrs);
        root.setNumAttrs(numAttrs);
        
        inputs[0].addOutput(root);
        inputs[1].addOutput(root);
        /*
         * Construct the predicate list. One predicate per group attribute. 
         */
        predList = new LinkedList<BoolExpr>();
        for (int j=0; j < numGroupAttrs; j++)
        {
          Attr leftAttr  = new Attr(0,j);
          Attr rightAttr = new Attr(1,j);
          Expr leftExpr  = new ExprAttr(leftAttr, phyGroupExprs[j].getType());
          Expr rightExpr = new ExprAttr(rightAttr, phyGroupExprs[j].getType());
          BoolExpr boolExpr = new BaseBoolExpr(CompOp.EQ,leftExpr,rightExpr,null,
                                               Datatype.BOOLEAN);
          
          BoolExpr isLeftAttrNull = new BaseBoolExpr(UnaryOp.IS_NULL,leftExpr,Datatype.BOOLEAN);
          BoolExpr isRightAttrNull = new BaseBoolExpr(UnaryOp.IS_NULL,rightExpr,Datatype.BOOLEAN);
          ComplexBoolExpr isLeftNullANDisRightNull = new ComplexBoolExpr(LogicalOp.AND,isLeftAttrNull,isRightAttrNull,Datatype.BOOLEAN);
          
          ComplexBoolExpr finalBoolExpr = new ComplexBoolExpr(LogicalOp.OR,boolExpr,isLeftNullANDisRightNull, Datatype.BOOLEAN);
          predList.add(finalBoolExpr);
        }
        root.setPreds(predList);
        
        /*
         * Create projExprs for PhyOptJoinProject
         */
        int j=0;
        Expr[]     projs     = new Expr[numAttrs];
        Datatype attrTypes;
        int      attrLen;  
        int      precision;
        int      scale;
        
        // Copying the left input attrs
        for (j = 0; j < inputs[0].getNumAttrs(); j++)
        {
          if (j < numGroupAttrs)
          {
            projs[j]  = new ExprAttr(new Attr(0,j),phyGroupExprs[j].getType());
            attrTypes = phyGroupExprs[j].getType();
            attrLen   = inputs[0].getAttrLen(j);
            precision = inputs[0].getAttrPrecision(j);
            scale     = inputs[0].getAttrScale(j);
            
          }
          else
          {
            projs[j]  = new ExprAttr(new Attr(0,j),
                                    logAggrAttrs.get(j-numGroupAttrs).getReturnDt());
            attrTypes = logAggrAttrs.get(j-numGroupAttrs).getReturnDt();
            attrLen   = inputs[0].getAttrLen(j);
            precision = inputs[0].getAttrPrecision(j);
            scale     = inputs[0].getAttrScale(j);
          }
          
          root.setAttrMetadata(j, new AttributeMetadata(attrTypes, 
                                                        attrLen, precision, 
                                                        scale));
        }
        // Copying the right input attrs
        for (int l = j; l < numAttrs; l++)
        {
          projs[l] = new ExprAttr(new Attr(1,l-j+numGroupAttrs),
                                  logAggrAttrs.get(l-numGroupAttrs).getReturnDt());
          attrTypes = logAggrAttrs.get(l-numGroupAttrs).getReturnDt();
          attrLen   = inputs[1].getAttrLen(l-j+numGroupAttrs);
          precision = inputs[1].getAttrPrecision(l-j+numGroupAttrs);
          scale     = inputs[1].getAttrScale(l-j+numGroupAttrs);
          
          root.setAttrMetadata(l, new AttributeMetadata(attrTypes, 
                                                        attrLen, precision, 
                                                        scale));
        }
        root.setProjs(projs);
        
        phyJoin = root;
      }
      return phyJoin;
    }
  }
  
  private boolean isBuiltInAggrFn(BaseAggrFn fn, AggrFunction fnCode) {
    return fn.getFnCode() == fnCode;
  }
}
