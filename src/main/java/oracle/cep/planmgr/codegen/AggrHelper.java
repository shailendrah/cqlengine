/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/planmgr/codegen/AggrHelper.java /main/21 2011/09/05 22:47:27 sbishnoi Exp $ */

/* Copyright (c) 2007, 2011, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    Helper class dealing with code generation related to aggregations

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    sbishnoi    08/29/11 - support for interval year to month based operations
    anasrini    12/20/10 - remove eval.setEvalContext
    sborah      10/14/09 - support for bigdecimal
    sborah      06/24/09 - support for bigdecimal
    hopark      02/17/09 - support boolean as external datatype
    hopark      02/06/09 - add AggrObj
    udeshmuk    10/29/08 - orderby in xmlagg in pattern
    hopark      10/10/08 - remove statics
    hopark      10/09/08 - remove statics
    hopark      10/07/08 - use execContext to remove statics
    skmishra    09/18/08 - adding null check for orderExpr
    skmishra    09/16/08 - adding param to getInitEval
    skmishra    07/17/08 - adding order by
    skmishra    07/15/08 - adding order by for xmlagg
    sbishnoi    08/18/08 - 
    udeshmuk    08/18/08 - add xmlagg case in getNullOutputEval
    sbishnoi    07/14/08 - 
    udeshmuk    07/09/08 - fix for null pointer exception in xmlagg usage in
                           MEASURES clause.
    skmishra    06/16/08 - adding xmlagg
    udeshmuk    01/31/08 - support for double data type.
    udeshmuk    10/18/07 - add check for void before calling getAggrValue.
    udeshmuk    10/17/07 - change getInitEval and getIncrEval to handle
                           multiarg aggr and singleargaggr together.
    hopark      09/04/07 - eval optimize
    rkomurav    09/06/07 - add dynPrevreole
    rkomurav    08/20/07 - add count_corr_star
    rkomurav    07/03/07 - add uda
    rkomurav    06/15/07 - fix init and incr eval construciton
    anasrini    05/28/07 - helper for aggregations
    anasrini    05/28/07 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/planmgr/codegen/AggrHelper.java st_pcbpel_anasrini_eval_parallelism_2/1 2010/12/20 07:47:45 anasrini Exp $
 *  @author  anasrini
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.planmgr.codegen;

import java.util.ArrayList;
import java.util.List;

import oracle.cep.common.AggrFunction;
import oracle.cep.common.AttributeMetadata;
import oracle.cep.common.BaseAggrFn;
import oracle.cep.common.Constants;
import oracle.cep.common.Datatype;
import oracle.cep.common.UserDefAggrFn;
import oracle.cep.exceptions.CEPException;
import oracle.cep.exceptions.MetadataError;
import oracle.cep.execution.ExecException;
import oracle.cep.execution.comparator.ComparatorSpecs;
import oracle.cep.execution.comparator.TupleComparator;
import oracle.cep.execution.internals.AInstr;
import oracle.cep.execution.internals.AOp;
import oracle.cep.execution.internals.BInstr;
import oracle.cep.execution.internals.Column;
import oracle.cep.execution.internals.IAEval;
import oracle.cep.execution.internals.IBEval;
import oracle.cep.execution.internals.IEvalContext;
import oracle.cep.execution.internals.TupleSpec;
import oracle.cep.execution.internals.XMLAggInstr;
import oracle.cep.execution.internals.factory.AEvalFactory;
import oracle.cep.execution.internals.factory.BEvalFactory;
import oracle.cep.extensibility.functions.AggrBigDecimal;
import oracle.cep.extensibility.functions.AggrBigInt;
import oracle.cep.extensibility.functions.AggrBoolean;
import oracle.cep.extensibility.functions.AggrByte;
import oracle.cep.extensibility.functions.AggrChar;
import oracle.cep.extensibility.functions.AggrDouble;
import oracle.cep.extensibility.functions.AggrFloat;
import oracle.cep.extensibility.functions.AggrInteger;
import oracle.cep.extensibility.functions.AggrInterval;
import oracle.cep.extensibility.functions.AggrObj;
import oracle.cep.extensibility.functions.AggrTimestamp;
import oracle.cep.extensibility.functions.AggrValue;
import oracle.cep.extensibility.functions.IAggrFnFactory;
import oracle.cep.extensibility.functions.IAggrFunction;
import oracle.cep.extensibility.functions.IAggrFunctionMetadata;
import oracle.cep.extensibility.functions.IUserFunctionMetadata;
import oracle.cep.memmgr.FactoryManager;
import oracle.cep.memmgr.factory.AggrFunctionFactory;
import oracle.cep.metadata.AggFunction;
import oracle.cep.metadata.MetadataException;
import oracle.cep.parser.CartridgeHelper;
import oracle.cep.phyplan.PhyOptGroupAggr;
import oracle.cep.phyplan.expr.Expr;
import oracle.cep.phyplan.expr.ExprOrderBy;
import oracle.cep.planmgr.codegen.ExprHelper.Addr;
import oracle.cep.service.ExecContext;
import oracle.xml.parser.v2.XMLDocument;


/**
 * Helper class dealing with the code generation (instantiation) related to
 * aggregations
 *
 * @since 1.0
 */

class AggrHelper
{

  static IAEval getNullOutputEval(ExecContext ec, int newOutputRole,
                                 EvalContextInfo evalCtxInfo, 
                                 AggrFunction[] fn, int numAggrParamExprs)
    throws CEPException
  {
    //for every aggregate expression emit a zero or null.
    IAEval  eval = AEvalFactory.create(ec);
    AInstr instr;
      
    for(int i = 0; i < numAggrParamExprs; i++) {
      instr = new AInstr();
      switch(fn[i]) {
      case COUNT:
      case COUNT_STAR:
      case COUNT_CORR_STAR:
        instr.op = AOp.INT_CPY;
        instr.r1 = IEvalContext.CONST_ROLE;
        instr.c1 = evalCtxInfo.ct.addInt(0);
        instr.dr = newOutputRole;
        instr.dc = i;
        eval.addInstr(instr);
        break;
      case SUM:
      case MAX:
      case MIN:
      case AVG:
      case FIRST:
      case LAST:
      case XML_AGG:
        instr.op = AOp.NULL_CPY;
        instr.dr = newOutputRole;
        instr.dc = i;
        eval.addInstr(instr);
        break;
      case USER_DEF:
        //handle next
        break;
      default:
        assert false : fn[i];
      }
    }
    return eval;
  }
  
  /**
   * Creates ComparatorSpecs and adds an object to the TupleSpec when order by exists
   * @param numAggrParamExprs total # of aggr params
   * @param fn enum of aggrexpr types
   * @param orderByExprs list of order by exprs, per xmlagg
   * @param xmlAggIndexPos positions of the SortedTuplePtrArray per xmlagg
   * @param specs comparator specs to be created
   * @param outTupleSpec the spec that this operator outputs, add an object to store the index
   * @param orderBySpec tuplespec for orderby, one tuple used by all xmlaggs
   * @throws CEPException
   */
  
  static void initXmlAgg(int numAggrParamExprs, AggrFunction[] fn,
      ArrayList<ExprOrderBy[]> orderByExprs, int[] xmlAggIndexPos,
      ComparatorSpecs[][] specs, TupleSpec outTupleSpec, TupleSpec orderBySpec) throws CEPException
  {
    for(int i=0;i<numAggrParamExprs;i++)
    {
      if(fn[i] == AggrFunction.XML_AGG)
      {
        //TODO: Anand why does addAttr throw ExecException
        //when it is used at CodeGen layer?
        
        ExprOrderBy[] orderExprs = orderByExprs.get(i);
        
        //order by is optional in xmlagg
        //if order by exists, create a comparator spec
        //that the evaluator can use later
        //refer oracle.cep.execution.comparator
        if(orderExprs!=null)
        {
          assert orderBySpec != null;
          assert orderExprs.length != 0;
          specs[i] = new ComparatorSpecs[orderExprs.length];
          xmlAggIndexPos[i] = outTupleSpec.addAttr(Datatype.OBJECT);
          int j = 0;
          for(ExprOrderBy expr : orderExprs)
          {
            Datatype dt = expr.getType();
            int comparePos = -1;
            
            if(dt == Datatype.CHAR)
            {
              int len = Constants.MAX_CHAR_LENGTH;
              comparePos = orderBySpec.addAttr(
                  new AttributeMetadata(dt,len, dt.getPrecision(),0));
            }
            
            else if(dt == Datatype.BYTE)
            {
              int len = Constants.MAX_BYTE_LENGTH;
              comparePos = orderBySpec.addAttr(
                  new AttributeMetadata(dt,len, dt.getPrecision(),0));
            }
            
            else if(dt == Datatype.BIGDECIMAL)
            {
              int precision = Datatype.BIGDECIMAL.getPrecision();
              comparePos = orderBySpec.addAttr(new AttributeMetadata(dt, 0, precision, 0));
            }
            
            else
              comparePos = orderBySpec.addAttr(dt);
            assert comparePos != -1;
            specs[i][j] = new ComparatorSpecs(comparePos, expr.isNullsFirst(), expr.isAscending());
            j++;
          }
        }
      }
   }
  }
  
  static AInstr getXmlIndexAllocInstr(int role, int[] xmlAggIndexPos, ComparatorSpecs[][] specs, TupleSpec orderBySpec)
  {
    
    assert xmlAggIndexPos != null;
    
    AInstr instr = new AInstr();
    instr.r1 = role;
    instr.op = AOp.ALLOC_XMLAGG_INDEX;
    instr.setXmlAggIndexPos(xmlAggIndexPos);
    TupleComparator[] tc = new TupleComparator[xmlAggIndexPos.length];
  
    for(int i=0; i < xmlAggIndexPos.length; i++)
    {
      //if there is a slot for the index
      if(xmlAggIndexPos[i] != -1)
      {
        assert specs[i] != null;
        tc[i] = new TupleComparator(specs[i], orderBySpec);
      }
   }
    instr.setComparators(tc);
    return instr;
  }
  
  static IAEval getAllocIndexEval(ExecContext ec, int role, int[] xmlAggIndexPos, ComparatorSpecs[][] specs, TupleSpec orderBySpec) throws CEPException
  {
    IAEval  allocEval;
    AInstr instr;
    allocEval = AEvalFactory.create(ec);
    instr = getXmlIndexAllocInstr(role, xmlAggIndexPos, specs, orderBySpec);

    allocEval.addInstr(instr);
    return allocEval;
  }
  
  static IAEval getResetIndexEval(ExecContext ec, int[] xmlAggIndexPos) throws ExecException
  {
    IAEval  resetEval;
    AInstr instr;
    
    resetEval = AEvalFactory.create(ec);
    instr     = new AInstr();
    instr.r1  = IEvalContext.AGGR_ROLE;
    instr.op  = AOp.RESET_XMLAGG_INDEX;
    instr.setXmlAggIndexPos(xmlAggIndexPos);
    
    resetEval.addInstr(instr);
    return resetEval;
  }
  
  static AInstr getXmlIndexReleaseInstr(int[] xmlAggIndexPos)
  {
    AInstr instr = new AInstr();
    instr.r1 = IEvalContext.AGGR_ROLE;
    instr.op = AOp.RELEASE_XMLAGG_INDEX;
    instr.setXmlAggIndexPos(xmlAggIndexPos);
    return instr;
  }
  
  static IAEval getReleaseIndexEval(ExecContext ec, int[] xmlAggIndexPos) throws CEPException
  {
    IAEval  releaseEval;
    AInstr instr;
    
    releaseEval = AEvalFactory.create(ec);
    instr = getXmlIndexReleaseInstr(xmlAggIndexPos);
    releaseEval.addInstr(instr);
    return releaseEval;
  }
  
  static void initUDA(ExecContext ec, int numUDA, BaseAggrFn[] aggFns,
      UserDefAggrFn[] uda, int[] udaPos, IAggrFnFactory[] udaFactory,
      IAggrFunction[] udaHandler, int numAggrParamExprs, AggrFunction[] fn,
      boolean oneGroup, boolean processGroups, TupleSpec ts,
      ArrayList<Datatype[]> inputdts) throws CEPException {
    int fnId;
    AggFunction afn = null;
    IAggrFnFactory factory = null;

    FactoryManager factoryMgr = ec.getServiceManager().getFactoryManager();
    for (int i = 0; i < numAggrParamExprs; i++) {
      if (fn[i] == AggrFunction.USER_DEF) {
        uda[i] = (UserDefAggrFn) aggFns[i];
        udaPos[i] = ts.addAttr(Datatype.OBJECT);

        fnId = uda[i].getFnId();
        try {
          afn = ec.getUserFnMgr().getAggrFunction(fnId);
          factory = afn.getAggrFactory();
        } catch (MetadataException e) {
          if (e.getErrorCode() == MetadataError.INVALID_FUNCTION_IDENTIFIER && inputdts != null) {

            IUserFunctionMetadata iufmd = CartridgeHelper.findFunctionMetadata(
                ec, uda[i].getCartridgeLinkName(), uda[i].getFunctionName(),
                inputdts.get(i));
            if (iufmd instanceof IAggrFunctionMetadata) {
              factory = ((IAggrFunctionMetadata) iufmd).getAggrFactory();
            }
          } else
            throw e;
        }

        if (factory == null)
        {
          factory = new AggrFunctionFactory(factoryMgr, afn.getImplClassName(), afn.getImplInstanceName());
          afn.setAggrFactory(factory);
        }

        udaFactory[i] = factory;
        if(processGroups)
        {
          if (oneGroup)
          {
            udaHandler[i] = factory.newAggrFunctionHandler();
          }
        }
        else
          udaHandler[i] = null;
      }
    }
  }
  
  static IAEval getInitEval(ExecContext ec, IAEval initEval, int inputRole, int oldOutputRole, 
      int newOutputRole,
      EvalContextInfo evalCtxInfo,
      IEvalContext evalContext,
      AggrFunction[] fn,
      ArrayList<Expr[]> aggrParamExprs, 
      int numAggrParamExprs,
      ArrayList<Datatype[]> aggrInpTypes,
      Datatype[] types, int startPos,
      BaseAggrFn[] baseFns, int[] udaPos, int udaStartPos,
      IAggrFnFactory[] udaFactory,
      IAggrFunction[] udaHandler, boolean includeAlloc, boolean includeIndexAlloc,
      int[] xmlAggIndexPos, List<ExprOrderBy[]> orderByExprs, 
      ComparatorSpecs[][] compareSpecs, TupleSpec ts) throws CEPException
  {
    return getInitEval(ec, initEval, inputRole, oldOutputRole, newOutputRole, -1,
        evalCtxInfo, evalContext, fn, aggrParamExprs, numAggrParamExprs,
        aggrInpTypes, types, startPos, baseFns, udaPos, udaStartPos,
        udaFactory, udaHandler, includeAlloc, -1, includeIndexAlloc, xmlAggIndexPos, orderByExprs, compareSpecs, ts);
  }

  static IAEval getInitEval(ExecContext ec, IAEval initEval, int inputRole, int oldOutputRole,
                           int newOutputRole,
                           int bindRoleStart,
                           EvalContextInfo evalCtxInfo,
                           IEvalContext evalContext,
                           AggrFunction[] fn,
                           ArrayList<Expr[]> aggrParamExprs, 
                           int numAggrParamExprs,
                           ArrayList<Datatype[]> aggrInpTypes,
                           Datatype[] types, int startPos,
                           BaseAggrFn[] baseFns, int[] udaPos, int udaStartPos,
                           IAggrFnFactory[] udaFactory,
                           IAggrFunction[] udaHandler, boolean includeAlloc,
                           int prevRole, boolean includeIndexAlloc, 
                           int[] xmlAggIndexPos, List<ExprOrderBy[]> orderByExprs,
                           ComparatorSpecs[][] compareSpecs, TupleSpec orderBySpec)
    throws CEPException {

    if(evalCtxInfo.getDummyDoc() == null)
    {
      XMLDocument parentDoc = new XMLDocument();
      evalCtxInfo.dummydoc = parentDoc;
    }
    
    AInstr                    instr;
    AInstr                    instr1;
    AInstr                    allocInstr;
    AOp                       op;
    int                       aggrIndex;
    int                       udaIndex;
    boolean                   hasUda;
    boolean                   xmlAggIndexAllocated;
    ArrayList<Integer>        collectUdaPos;
    ArrayList<IAggrFnFactory> collectUdaFactory;
    ArrayList<IAggrFunction>  collectUdaHandler;
    
    collectUdaPos     = new ArrayList<Integer>();
    collectUdaFactory = new ArrayList<IAggrFnFactory>();
    collectUdaHandler = new ArrayList<IAggrFunction>();
    hasUda = false;
    // to avoid duplicate allocation of xmlagg index in the aggrs referred by same corr
    xmlAggIndexAllocated = false; 
    
    // Initialization corresponding to the aggregation attributes
    for (int i=0; i<numAggrParamExprs; i++) 
    {
      aggrIndex = i + startPos;
      udaIndex  = i + udaStartPos;
      instr = new AInstr();
      int[] inpRoles = new int[1];
      inpRoles[0] = inputRole;
      ExprHelper.Addr addr;
      switch (fn[i]) {
      case SUM:
      case MAX:
      case MIN:
      case FIRST:
      case LAST:
        addr =
          ExprHelper.instExpr(ec, aggrParamExprs.get(i)[0],initEval,evalCtxInfo,
                              inpRoles, bindRoleStart, prevRole);
        op = ExprHelper.getCopyOp(types[i]);

        instr.op = op;
        instr.r1 = addr.role;
        instr.c1 = addr.pos;
        instr.dr = newOutputRole;
        instr.dc = aggrIndex;
        initEval.addInstr(instr);
        break;
      case XML_AGG:
        ExprOrderBy[] orderExprs = null;
        if(orderByExprs != null)
          orderExprs = orderByExprs.get(i);
        //if order by exprs exist, add instr to copy them to order by role
        if(orderExprs != null)
        {          
          assert compareSpecs[i] != null;
          assert orderBySpec != null;
          assert orderExprs.length == compareSpecs[i].length;
          
          //for each order by expr, copy expr into orderByTuple.
          for(int j=0; j< orderExprs.length ; j++)
            ExprHelper.instExprDest(ec, orderExprs[j], initEval, evalCtxInfo,
              IEvalContext.XML_AGG_INDEX_ROLE, compareSpecs[i][j].getColNum(),
              inpRoles, bindRoleStart, prevRole);
        } 
        
        //allocate xml index
        if(orderExprs!=null && includeIndexAlloc && !xmlAggIndexAllocated)
        {
          AInstr inst = getXmlIndexAllocInstr(newOutputRole, xmlAggIndexPos,
                                              compareSpecs, orderBySpec);
          initEval.addInstr(inst);
          xmlAggIndexAllocated = true;
        }
        
        //validation check
        if(compareSpecs != null)
        {
          //Both need to exist, or both should be null.
          if(compareSpecs[i] != null)
            assert xmlAggIndexPos[i] != -1;
          
          if(xmlAggIndexPos[i] != -1)
            assert compareSpecs[i] != null;
        }
        
        //the xmlagg instr... Instantiate the argument and then call init instruction
        instr.op = AOp.XML_AGG_INIT_GROUP;
        addr =
          ExprHelper.instExpr(ec, aggrParamExprs.get(i)[0],initEval,evalCtxInfo,
                              inpRoles, bindRoleStart, prevRole);
        instr.xmlAggInstr = new XMLAggInstr(addr.pos, addr.role, aggrIndex,
                                            oldOutputRole, newOutputRole, 
                                            xmlAggIndexPos[i]);
        
        initEval.addInstr(instr);
        break;
        
      case COUNT_CORR_STAR:
      case COUNT_STAR:
        instr.op = AOp.INT_CPY;
        instr.r1 = IEvalContext.CONST_ROLE;
        instr.c1 = evalCtxInfo.ct.addInt(1);
        instr.dr = newOutputRole;
        instr.dc = aggrIndex;
        initEval.addInstr(instr);
        break;
      case COUNT:
        IBEval bEval = BEvalFactory.create(ec);
        IAEval aEval = AEvalFactory.create(ec);
        
        addr = 
          ExprHelper.instExpr(ec, aggrParamExprs.get(i)[0],aEval,evalCtxInfo,
                              inpRoles, bindRoleStart, prevRole);
        BInstr bInstr = new BInstr();
        aEval.compile();
        bInstr.op = ExprHelper.getNullOp(aggrInpTypes.get(i)[0], aEval);
        bInstr.r1 = addr.role;
        bInstr.c1 = new Column(addr.pos);
        bInstr.e1 = aEval;
        bEval.addInstr(bInstr);
        bEval.compile();
        
        instr.countCond = bEval;
        instr.op        = AOp.COUNT_INIT;
        instr.dr        = newOutputRole;
        instr.dc        = aggrIndex;
        initEval.addInstr(instr);
        break;
      case AVG:
        addr = ExprHelper.instExpr(ec, aggrParamExprs.get(i)[0],initEval,
                                   evalCtxInfo,
                                   inpRoles, bindRoleStart, prevRole);
        instr.op = ExprHelper.getAvgOp(aggrInpTypes.get(i)[0]);
        
        instr.r1 = addr.role;
        instr.c1 = addr.pos;
        instr.r2 = IEvalContext.CONST_ROLE;
        instr.c2 = evalCtxInfo.ct.addInt(1);
        instr.dr = newOutputRole;
        instr.dc = aggrIndex;
        initEval.addInstr(instr);
        break;
      case USER_DEF:
        // handle in separate iteration but collect uda information
        if(includeAlloc)
        {
          hasUda = true;
          collectUdaPos.add(udaPos[udaIndex]);
          collectUdaFactory.add(udaFactory[udaIndex]);
          collectUdaHandler.add(udaHandler[udaIndex]);
        }
        break;
      default:
        assert false;
      }
    }
    
    // create alloc instruction
    if(hasUda && includeAlloc)
    {
      Integer[] temp = collectUdaPos.toArray(new Integer[1]);
      int[] posArr;
      posArr = new int[temp.length];
      for(int i = 0; i <  temp.length; i++)
      {
        posArr[i] = temp[i].intValue();
      }
      
      allocInstr = getAllocHandlerInstr(newOutputRole, posArr,
          collectUdaFactory.toArray(new IAggrFnFactory[1]),
          collectUdaHandler.toArray(new IAggrFunction[1]));

      initEval.addInstr(allocInstr);
    }
    
    // Handle user defined aggregations
    for (int i=0; i<numAggrParamExprs; i++)
    {
      aggrIndex = i + startPos;
      udaIndex  = i + udaStartPos;
      instr = new AInstr();
      instr1 = new AInstr();
      switch (fn[i]) {
      case USER_DEF:
        
        int[] inpRoles = new int[1];
        inpRoles[0] = inputRole;
        ExprHelper.Addr addr = ExprHelper.instExpr(ec, (aggrParamExprs.get(i))[0],
            initEval,evalCtxInfo,inpRoles, bindRoleStart, prevRole);
        
        int fId = ((UserDefAggrFn)baseFns[i]).getFnId();
        
        instr.op = AOp.UDA_INIT;
        instr.r1 = oldOutputRole;
        instr.c1 = udaPos[udaIndex];
        instr.dr = newOutputRole;
        instr.dc = udaPos[udaIndex];
        instr.setFunctionId(fId);

        Expr[] args = aggrParamExprs.get(i);
        int numArgs = aggrParamExprs.get(i).length;
        UserDefAggrFn aggrFn;
        Addr argAddr;
        int aggrFnId ;
        aggrFn      = (UserDefAggrFn)baseFns[i];
        aggrFnId    = aggrFn.getFnId();
        instr1.setAggrFunctionInstr(numArgs, aggrFn.getReturnType(types[i]), true);
        AggrValue in;
        AggrValue out;
        //for 1st argument instExpr is done earlier
        instr1.addFunctionArg(0, args[0].getType(), addr.role, addr.pos);
        in = getAggrValue((aggrInpTypes.get(i))[0]);
        instr1.setAggrInput(in, 0);
        //set the function id in the instruction
        instr1.setFunctionId(aggrFnId);
        for (int k = 1; k< numArgs; k++)
        {
          argAddr = ExprHelper.instExpr(ec, args[k], initEval, evalCtxInfo,
              inpRoles, bindRoleStart, prevRole);
          instr1.addFunctionArg(k, args[k].getType(), argAddr.role, argAddr.pos);
          in = getAggrValue((aggrInpTypes.get(i))[k]);
          instr1.setAggrInput(in, k);
        }
        
        out = getAggrValue(types[i]);
        instr1.setAggrOutput(out);
        instr1.r1 = newOutputRole;
        instr1.c1 = udaPos[udaIndex];
        instr1.dr = newOutputRole;
        instr1.dc = aggrIndex;
        initEval.addInstr(instr);
        initEval.addInstr(instr1);
      default:
        break;
      }
    }
    return initEval;
  }

  static void getIncrEval(ExecContext ec, IAEval eval, 
                          int inputRole, int oldOutputRole, 
                          int newOutputRole, boolean plus, 
                          EvalContextInfo evalCtxInfo, IEvalContext evalContext,
                          AggrFunction[] fn, ArrayList<Expr[]> aggrParamExprs, 
                          int numAggrParamExprs,
                          ArrayList<Datatype[]> aggrInpTypes, Datatype[] types,
                          int[] sumPos, int[]countPos, int startPos,
                          int[] udaPos, BaseAggrFn[] baseFns,
                          int sumCountUdaStartPos, int[] xmlAggIndexPos,
                          ComparatorSpecs[][] compareSpecs, 
                          List<ExprOrderBy[]> orderByExprs,
                          TupleSpec orderBySpec)  
  throws CEPException {
       
    getIncrEval(ec, eval, inputRole, oldOutputRole, newOutputRole, -1, plus,
        evalCtxInfo, evalContext, fn, aggrParamExprs, numAggrParamExprs,
        aggrInpTypes, types, sumPos, countPos, startPos, udaPos, baseFns,
        sumCountUdaStartPos, -1, xmlAggIndexPos, compareSpecs, orderByExprs, 
        orderBySpec);
    
  }
  static void getIncrEval(ExecContext ec, IAEval eval, 
                          int inputRole, int oldOutputRole, 
                          int newOutputRole, int bindRoleStart,
                          boolean plus, 
                          EvalContextInfo evalCtxInfo, IEvalContext evalContext,
                          AggrFunction[] fn, ArrayList<Expr[]> aggrParamExprs, 
                          int numAggrParamExprs,
                          ArrayList<Datatype[]> aggrInpTypes, Datatype[] types,
                          int[] sumPos, int[]countPos, int startPos,
                          int[] udaPos, BaseAggrFn[] baseFns,
                          int sumCountUdaStartPos, int prevRole,
                          int[] xmlAggIndexPos, ComparatorSpecs[][] compareSpecs,
                          List<ExprOrderBy[]> orderByExprs, TupleSpec orderBySpec)  
                          throws CEPException 
  {
    
    if(evalCtxInfo.getDummyDoc() == null)
    {
      XMLDocument parentDoc = new XMLDocument();
      evalCtxInfo.dummydoc = parentDoc;
    }
    
    AInstr instr;
    int    aggrIndex;
    int    sumCountUdaIndex;
    
    // Generate  the aggr.  attributes.   In this  "pass"  we only  handle
    // non-avg. aggregates.  Avg. aggregates are handled in the next pass.
    // The reason for this is that avg. aggr. are dependent on SUM & COUNT
    // aggr	
    for (int i=0; i<numAggrParamExprs; i++) {
      aggrIndex        = i + startPos;
      sumCountUdaIndex = i + sumCountUdaStartPos;
      instr = new AInstr();
      
      int[] inpRoles = new int[1];
      inpRoles[0] = inputRole;
      ExprHelper.Addr addr = ExprHelper.instExpr(ec, (aggrParamExprs.get(i))[0], eval, 
                                                 evalCtxInfo, inpRoles,
                                                 bindRoleStart, prevRole);
      
      switch (fn[i]) {
      case SUM:
        instr.op = ExprHelper.getSumOp(aggrInpTypes.get(i)[0],plus);
        
        instr.r1 = oldOutputRole;
        instr.c1 = aggrIndex;
        instr.r2 = addr.role;
        instr.c2 = addr.pos;
        instr.dr = newOutputRole;
        instr.dc = aggrIndex;
        eval.addInstr(instr);
        break;
        
      case COUNT_CORR_STAR:
      case COUNT_STAR:
        instr.op = ExprHelper.getAddSubOp(Datatype.INT, plus);
        
        instr.r1 = oldOutputRole;
        instr.c1 = aggrIndex;
        instr.r2 = IEvalContext.CONST_ROLE;
        instr.c2 = evalCtxInfo.ct.addInt(1);
        instr.dr = newOutputRole;
        instr.dc = aggrIndex;
        eval.addInstr(instr);
        break;
                
      case COUNT:
        IBEval bEval = BEvalFactory.create(ec);
        IAEval aEval = AEvalFactory.create(ec);
        
        addr = ExprHelper.instExpr(ec, aggrParamExprs.get(i)[0],
                                   aEval,evalCtxInfo,
                                   inpRoles, bindRoleStart, prevRole);
        BInstr bInstr = new BInstr();
        aEval.compile();
        bInstr.op = ExprHelper.getNullOp(aggrInpTypes.get(i)[0], aEval);
        bInstr.r1 = addr.role;
        bInstr.c1 = new Column(addr.pos);
        bInstr.e1 = aEval;
        bEval.addInstr(bInstr);
        bEval.compile();
              
        instr.countCond = bEval;
        instr.op        = plus ? AOp.COUNT_ADD : AOp.COUNT_SUB;
        instr.r1        = oldOutputRole;
        instr.c1        = aggrIndex;
        instr.dr        = newOutputRole;
        instr.dc        = aggrIndex;
        eval.addInstr(instr);
        break;

      case FIRST:
        // Nothing to do here
        break;

      case LAST:
        instr.op = ExprHelper.getCopyOp(types[i]);
        instr.r1 = addr.role;
        instr.c1 = addr.pos;
        instr.dr = newOutputRole;
        instr.dc = aggrIndex;
        eval.addInstr(instr);
        break;

      case AVG:
        // handled in the next iteration
        break;
        
      case MAX:
        if (plus) {
          instr.op = ExprHelper.getMaxOp(aggrInpTypes.get(i)[0]);
          
          instr.r2 = addr.role;
          instr.c2 = addr.pos;
        } 
        else {
          instr.op = ExprHelper.getCopyOp(types[i]);
        }
        instr.r1 = oldOutputRole;
        instr.c1 = aggrIndex;
        instr.dr = newOutputRole;
        instr.dc = aggrIndex;
        eval.addInstr(instr);
        break;
        
      case MIN:
        if (plus) {
          instr.op = ExprHelper.getMinOp(aggrInpTypes.get(i)[0]);
          
          instr.r2 = addr.role;
          instr.c2 = addr.pos;
        } 
        else {
          instr.op = ExprHelper.getCopyOp(types[i]);
        }
        instr.r1 = oldOutputRole;
        instr.c1 = aggrIndex;
        instr.dr = newOutputRole;
        instr.dc = aggrIndex;
        eval.addInstr(instr);
        break;
        
      case USER_DEF:
        Expr[] args = aggrParamExprs.get(i);
        int numArgs = aggrParamExprs.get(i).length;
        UserDefAggrFn aggrFn;
        Addr argAddr;
        int aggrFnId;
        aggrFn      = (UserDefAggrFn)baseFns[i];
        aggrFnId    = aggrFn.getFnId();
        instr.setAggrFunctionInstr(numArgs, aggrFn.getReturnType(types[i]), plus);
        AggrValue in;
        AggrValue out;
        //For 1st argument instExpr is handled at the top of switch
        instr.addFunctionArg(0, args[0].getType(), addr.role, addr.pos);
        in = getAggrValue((aggrInpTypes.get(i))[0]);
        instr.setAggrInput(in, 0);
        //set the function id in the instruction
        instr.setFunctionId(aggrFnId);
        //start from 2nd argument
        for (int k = 1; k< numArgs; k++)
        {
          argAddr = ExprHelper.instExpr(ec, args[k], eval, evalCtxInfo,
                                        inpRoles,bindRoleStart,prevRole);
          instr.addFunctionArg(k, args[k].getType(), argAddr.role,
                               argAddr.pos);
          in = getAggrValue((aggrInpTypes.get(i))[k]);
          instr.setAggrInput(in, k);
        }
        
        out = getAggrValue(types[i]);
        instr.setAggrOutput(out);
        instr.r1 = oldOutputRole;
        instr.c1 = udaPos[sumCountUdaIndex];
        instr.dr = newOutputRole;
        instr.dc = aggrIndex;
        eval.addInstr(instr);
        break;
      case XML_AGG:
        if(plus)
        {
          ExprOrderBy[] orderExprs = null;
          if(orderByExprs != null)
            orderExprs = orderByExprs.get(i);
          //if order by exprs exist, add instr to copy them to order by role
          if(orderExprs != null)
          {          
            assert compareSpecs[i] != null;
            assert orderBySpec != null;
            assert orderExprs.length == compareSpecs[i].length;
            
            //for each order by expr, copy expr into orderByTuple.
            for(int j=0; j< orderExprs.length ; j++)
              ExprHelper.instExprDest(ec, orderExprs[j], eval, evalCtxInfo,
                                      IEvalContext.XML_AGG_INDEX_ROLE,
                                      compareSpecs[i][j].getColNum(),
                                      inpRoles, bindRoleStart, prevRole);
          }       
         
          if(compareSpecs != null)
          {
            //Both need to exist, or both should be null.
            if(compareSpecs[i] != null)
              assert xmlAggIndexPos[i] != -1;
          
            if(xmlAggIndexPos[i] != -1)
              assert compareSpecs[i] != null;
          }
          //the xmlagg instr.
          instr.op = AOp.XML_AGG;
          instr.xmlAggInstr = new XMLAggInstr(addr.pos, addr.role, aggrIndex, 
                                              oldOutputRole, newOutputRole, 
                                              xmlAggIndexPos[i]);
          eval.addInstr(instr);
        }
        break;
      default:
        System.out.println("failing here");
        assert false;
      }
    }

    // Handle AVG here
    for (int i=0; i<numAggrParamExprs; i++) {
      aggrIndex = i + startPos;
      if (fn[i] == AggrFunction.AVG) {
        instr = new AInstr();
        
        // Operation: Note that INT_AVG & FLT_AVG are different from
        // INT_DIV and FLT_DIV.  For example, INT_DIV divides two integers
        // and produces an integer.  INT_AVG divides two integer &
        // produces a float
        
        instr.op = ExprHelper.getAvgOp(aggrInpTypes.get(i)[0]);
        
        instr.r1 = newOutputRole;
        instr.c1 = sumPos[i + sumCountUdaStartPos];
        instr.r2 = newOutputRole;
        instr.c2 = countPos[i + sumCountUdaStartPos];
        instr.dr = newOutputRole;
        instr.dc = aggrIndex;
        eval.addInstr(instr);
      }
    }
    //eval.compile();
  }

  static void getSumCountPos(int[] sumPos, int[] countPos, AggrFunction[] fn,
                             ArrayList<Expr[]> aggrParamExprs,
                             int numAggrParamExprs, int startPos) {
    boolean foundSum;
    boolean foundCount;
    boolean found;
    
    for (int i=0; i<numAggrParamExprs; i++) {
      if (fn[i] == AggrFunction.AVG) {
        foundSum   = false;
        foundCount = false;
        found      = foundSum && foundCount;
        for (int j=0; j<numAggrParamExprs && !found; j++) {
          if ( (fn[j] == AggrFunction.SUM || fn[j] == AggrFunction.COUNT) &&
               aggrParamExprs.get(j)[0].equals(aggrParamExprs.get(i)[0])) {

            if (fn[j] == AggrFunction.SUM) {
              foundSum = true;
              sumPos[i] = j + startPos;
            }
            else if (fn[j] == AggrFunction.COUNT) {
              foundCount  = true;
              countPos[i] = j + startPos;
            }
          }
          found = foundSum && foundCount;
        }
        assert found;
      }
    }
  }
  
  static AggrValue getAggrValue(Datatype dt)
  {
    AggrValue av = null;
    switch(dt.getKind())
    {
    case INT:
      av = new AggrInteger();
      break;
    case FLOAT:
      av = new AggrFloat();
      break;
    case DOUBLE:
      av = new AggrDouble();
      break;
    case BIGDECIMAL:
      av = new AggrBigDecimal();
      break;
    case BIGINT:
      av = new AggrBigInt();
      break;
    case CHAR:
      av = new AggrChar();
      break;
    case BYTE:
      av = new AggrByte();
      break;
    case TIMESTAMP:
      av = new AggrTimestamp();
      break;
    case INTERVAL:
      av = new AggrInterval();
      break;
    case INTERVALYM:
      av = new AggrInterval();
      break;
    case OBJECT:
      av = new AggrObj();
      break;
    case BOOLEAN:
      av = new AggrBoolean();
      break;
    default:
      assert false;
    }
    return av;
  }
  
  static void compressUdaInfo(int[] udaPos, IAggrFnFactory[] udaFactory,
      IAggrFunction[] udaHandler, ArrayList<Integer>compressUdaPos,
      ArrayList<IAggrFnFactory>compressUdaFactory,
      ArrayList<IAggrFunction>compressUdaHandler,  AggrFunction[] fn)
  {
    int len;
    len = udaPos.length;
    for(int i = 0; i < len; i++)
    {
      if(fn[i] == AggrFunction.USER_DEF)
      {
        compressUdaPos.add(udaPos[i]);
        compressUdaHandler.add(udaHandler[i]);
        compressUdaFactory.add(udaFactory[i]);
      }
    }
  }
  
  static AInstr getAllocHandlerInstr(int role, int[] udaPos,
      IAggrFnFactory[] aggrFactory, IAggrFunction[] aggrHandler)
  {
    AInstr allocInstr;
    
    allocInstr = new AInstr();
    
    allocInstr.r1 = role;
    allocInstr.setAllocAggrHandlersInstr(
        udaPos, aggrFactory, aggrHandler);

    return allocInstr;
  }
  
  static IAEval getAllocHandlerEval(ExecContext ec, int role, int[] udaPos,
      IAggrFnFactory[] aggrFactory, IAggrFunction[] aggrHandler)
      throws ExecException
  {
    IAEval  initEval;
    AInstr instr;
    
    initEval = AEvalFactory.create(ec);
    instr = getAllocHandlerInstr(role, udaPos, aggrFactory, aggrHandler);

    initEval.addInstr(instr);
    return initEval;
  }

  static IAEval getResetHandlerEval(ExecContext ec, int[] posArr) throws ExecException
  {
    IAEval  resetEval;
    AInstr instr;
    
    resetEval = AEvalFactory.create(ec);
    instr     = new AInstr();
    instr.r1  = IEvalContext.AGGR_ROLE;
    
    instr.setResetAggrHandlersInstr(posArr);
    resetEval.addInstr(instr);
    
    return resetEval;
  }

  static IAEval getReleaseHandlerEval(ExecContext ec, int[] posArr,
      IAggrFnFactory[] compressUdaFactory) throws ExecException
  {
    IAEval  releaseEval;
    AInstr instr;
    
    releaseEval = AEvalFactory.create(ec);
    instr       = new AInstr();
    instr.r1    = IEvalContext.AGGR_ROLE;
    instr.setReleaseAggrHandlersInstr(posArr, compressUdaFactory);
    releaseEval.addInstr(instr);
    
    return releaseEval;
  }
  
}
