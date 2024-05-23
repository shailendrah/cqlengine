/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/planmgr/codegen/JoinHelper.java /main/13 2015/11/04 04:57:19 udeshmuk Exp $ */

/* Copyright (c) 2009, 2015, Oracle and/or its affiliates. 
All rights reserved.*/

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    udeshmuk    08/31/15 - set recovery context in external synopsis
    sbishnoi    10/09/12 - XbranchMerge
                           sbishnoi_bug-13251101_ps6_pt.11.1.1.7.0_11.1.1.7.0
                           from st_pcbpel_11.1.1.4.0
    sbishnoi    10/08/12 - XbranchMerge sbishnoi_bug-13251101_ps6_pt.11.1.1.7.0
                           from st_pcbpel_pt-11.1.1.7.0
    sbishnoi    10/02/12 - setting external source name
    anasrini    12/20/10 - remove eval.setEvalContext
    sborah      07/18/10 - XbranchMerge sborah_bug-9536720_ps3_11.1.1.4.0 from
                           st_pcbpel_11.1.1.4.0
    sborah      07/17/10 - XbranchMerge sborah_bug-9536720_ps3 from main
    sbishnoi    06/22/10 - setting external synopsis params
    sbishnoi    04/08/10 - adding evalCtx param to new function call
    sbishnoi    03/03/10 - reorg of join factories;
    udeshmuk    11/09/09 - allow exprs other than ATTR_REF in function
                           arguments
    sborah      10/14/09 - support for bigdecimal
    udeshmuk    09/29/09 - extensible indexing support in binjoin
    udeshmuk    09/14/09 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/planmgr/codegen/JoinHelper.java /main/13 2015/11/04 04:57:19 udeshmuk Exp $
 *  @author  udeshmuk
 *  @since   release specific (what release of product did this appear in)
 */

/**
 * This class is a helper class for all the join operator factories.
 * This has the methods related to index(extensible/hash) handling. 
 */
package oracle.cep.planmgr.codegen;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import oracle.cep.common.CompOp;
import oracle.cep.common.Constants;
import oracle.cep.common.Datatype;
import oracle.cep.common.OuterJoinType;
import oracle.cep.dataStructures.internal.ITuple;
import oracle.cep.dataStructures.internal.ITuplePtr;
import oracle.cep.exceptions.CEPException;
import oracle.cep.exceptions.MetadataError;
import oracle.cep.execution.ExecException;
import oracle.cep.execution.indexes.ExtensibleIndexProxy;
import oracle.cep.execution.indexes.HashIndex;
import oracle.cep.execution.indexes.Index;
import oracle.cep.execution.internals.AInstr;
import oracle.cep.execution.internals.BInstr;
import oracle.cep.execution.internals.Column;
import oracle.cep.execution.internals.HInstr;
import oracle.cep.execution.internals.IAEval;
import oracle.cep.execution.internals.IBEval;
import oracle.cep.execution.internals.IEvalContext;
import oracle.cep.execution.internals.IHEval;
import oracle.cep.execution.internals.TupleSpec;
import oracle.cep.execution.internals.factory.AEvalFactory;
import oracle.cep.execution.internals.factory.BEvalFactory;
import oracle.cep.execution.internals.factory.HEvalFactory;
import oracle.cep.execution.operators.BinJoinBase;
import oracle.cep.execution.operators.RelSource;
import oracle.cep.execution.queues.Queue;
import oracle.cep.execution.stores.ExecStore;
import oracle.cep.execution.stores.RelStore;
import oracle.cep.execution.synopses.ExternalSynopsisImpl;
import oracle.cep.execution.synopses.RelationSynopsisImpl;
import oracle.cep.extensibility.cartridge.CartridgeException;
import oracle.cep.extensibility.cartridge.ICartridge;
import oracle.cep.extensibility.cartridge.ICartridgeContext;
import oracle.cep.extensibility.datasource.IExternalConnection;
import oracle.cep.extensibility.datasource.IExternalPreparedStatement;
import oracle.cep.extensibility.datasource.Predicate;
import oracle.cep.extensibility.expr.ExprKind;
import oracle.cep.extensibility.functions.IUserFunctionMetadata;
import oracle.cep.extensibility.functions.IUserFunctionMetadataLocator;
import oracle.cep.extensibility.indexes.IIndex;
import oracle.cep.extensibility.indexes.IIndexInfo;
import oracle.cep.extensibility.indexes.IIndexInfoLocator;
import oracle.cep.extensibility.indexes.IIndexTypeFactory;
import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;
import oracle.cep.memmgr.FactoryManager;
import oracle.cep.memmgr.IAllocator;
import oracle.cep.memmgr.IPinnable;
import oracle.cep.memmgr.ObjectFactoryContext;
import oracle.cep.memmgr.ObjectManager;
import oracle.cep.metadata.MetadataException;
import oracle.cep.parser.CartridgeHelper;
import oracle.cep.phyplan.PhyOpt;
import oracle.cep.phyplan.PhyOptJoinBase;
import oracle.cep.phyplan.PhyOptRelnSrc;
import oracle.cep.phyplan.PhyOptTableFunctionRelnSrc;
import oracle.cep.phyplan.PhySynopsis;
import oracle.cep.phyplan.SynopsisKind;
import oracle.cep.phyplan.attr.Attr;
import oracle.cep.phyplan.expr.BaseBoolExpr;
import oracle.cep.phyplan.expr.BoolExpr;
import oracle.cep.phyplan.expr.Expr;
import oracle.cep.phyplan.expr.ExprAttr;
import oracle.cep.phyplan.expr.ExprBoolean;
import oracle.cep.phyplan.expr.ExprUserDefFunc;
import oracle.cep.planmgr.codegen.ExprHelper.Addr;
import oracle.cep.service.ExecContext;

class JoinHelper
{
  
  static class IndexMapKey
  {
    IIndexTypeFactory indexTypeFactory;
    
    Expr indexCollectionArgExpr;
    
    public IndexMapKey(IIndexTypeFactory indexTypeFactory,
                       Expr indexCollectionArgExpr)
    {
      this.indexTypeFactory = indexTypeFactory;
      this.indexCollectionArgExpr = indexCollectionArgExpr;
    }
    
    @Override
    public boolean equals(Object obj)
    {
      IndexMapKey other = (IndexMapKey) obj;
      
      if((this.indexTypeFactory.equals(other.indexTypeFactory))
         && (this.indexCollectionArgExpr.equals(other.indexCollectionArgExpr)))
      {
        return true;
      }
       
      return false;
    }
    
    public int hashCode()
    {
      //FIXME: any better way for hashCode?
      return indexTypeFactory.hashCode()*indexCollectionArgExpr.getKind().hashCode();
    }
  }
  
  /**
   * Initializes hash index
   * @param ec Execution context
   * @param op Physical operator
   * @param eqPred List of equality predicates
   * @param evalCtx Evaluation context
   * @param idx HashIndex instance
   * @param inputToBeIndexed The input (left/right) of the join operator to
   *                         which the index should be associated
   * @param probeKeyRole The role in evaluation context which contains the
   *                     tuple used for probing 
   * @throws CEPException
   */
  static void initHashIndex(ExecContext ec, PhyOpt op,
                            List<BoolExpr> eqPred,
                            IEvalContext evalCtx, HashIndex idx, 
                            int inputToBeIndexed, int probeKeyRole)
                            throws CEPException
  {
    PhyOpt child = op.getInputs()[inputToBeIndexed];
    
    if(eqPred == null)
      return;
    
    int predSize = eqPred.size();
    
    if(predSize == 0)
      return;
    
    //the leftJoinPos array stores the indexes of the outer attrs
    //which take part in the equality predicate
    int[] leftJoinPos = new int[predSize];
    
    //the rightJoinPos array stores the indexes of the inner attrs
    //which take part in the equality predicate
    int[] rightJoinPos = new int[predSize];
    
    //Equality condition:
    //leftJoinPos[i] == rightJoinPos[i] for all 0<= i < predSize
    
    Iterator<BoolExpr> iter = eqPred.iterator();
    int predNo = 0;
    while (iter.hasNext())
    {
      BaseBoolExpr p = (BaseBoolExpr)iter.next();

      // Expressions on joins are not supported currently
      assert (p.getLeft().getKind() == ExprKind.ATTR_REF);
      assert (p.getRight().getKind() == ExprKind.ATTR_REF);
      assert (p.getOper() == CompOp.EQ);

      ExprAttr left = (ExprAttr) p.getLeft();
      ExprAttr right = (ExprAttr) p.getRight();

      assert (left.getAValue().getInput() == Constants.OUTER);
      assert (right.getAValue().getInput() == Constants.INNER);

      leftJoinPos[predNo] = left.getAValue().getPos();
      rightJoinPos[predNo] = right.getAValue().getPos();
      predNo++;
    }
    
    int[] probeKeyPos = null;
    int[] dmlPos      = null;
    
    if(inputToBeIndexed == Constants.OUTER)
    {
      probeKeyPos = rightJoinPos;
      dmlPos      = leftJoinPos;
    }
    else
    {
      probeKeyPos = leftJoinPos;
      dmlPos      = rightJoinPos;
    }

    IHEval updateHash = HEvalFactory.create(ec, Constants.MAX_INSTRS);
    for (int a = 0; a < predSize; a++)
    {     
      HInstr hinstr = new HInstr(child.getAttrTypes(dmlPos[a]),
        IEvalContext.UPDATE_ROLE, new Column(dmlPos[a]));

      updateHash.addInstr(hinstr);  
    }
    updateHash.compile();

    IHEval scanHash = HEvalFactory.create(ec, Constants.MAX_INSTRS);
    for (int a = 0; a < predSize; a++)
    {
      HInstr hinstr = new HInstr(child.getAttrTypes(dmlPos[a]),
        probeKeyRole, new Column(probeKeyPos[a]));
      
      scanHash.addInstr(hinstr);     
    }
    scanHash.compile();

    // Boolean evaluator used by index during scans.
    IBEval keyEqual = BEvalFactory.create(ec);
    for (int a = 0; a < predSize; a++)
    {
       BInstr binstr = new BInstr();
       
       binstr.op = ExprHelper.getEqOp(child.getAttrTypes(dmlPos[a]));
               
       binstr.r1 = IEvalContext.SCAN_ROLE;
       binstr.c1 = new Column(dmlPos[a]);
       binstr.e1 = null;

       binstr.r2 = probeKeyRole;
       binstr.c2 = new Column(probeKeyPos[a]);
       binstr.e2 = null;

       keyEqual.addInstr(binstr);
    }
    keyEqual.setNullEqNull(false);
    keyEqual.compile();

    idx.setUpdateHashEval(updateHash);
    idx.setScanHashEval(scanHash);
    idx.setKeyEqual(keyEqual);
    idx.setEvalContext(evalCtx);
  }
  
  /**
   * 
   * @param ec Execution context
   * @param pred Predicate to be used for index creation
   * @param indexInfo Index information returned by getIndexInfo call 
   * @param indexCollectionArgPos Position (in the arguments to the function) 
   *                              that has reference to the input which is to 
   *                              be indexed
   * @param inIdx ExtensibleIndexProxy instance
   * @param evalContext Evaluation context
   * @param evalCtxInfo Eval context information
   * @param outerRole Role where tuple from OUTER side will be found
   * @param innerRole Role where tuple from INNER side will be found 
   * @throws CEPException
   */
  private static void initExtensibleIndex(ExecContext ec, BaseBoolExpr pred,
                                          IIndexInfo indexInfo,
                                          Integer indexCollectionArgPos,
                                          ExtensibleIndexProxy inIdx,
                                          IEvalContext evalContext, 
                                          EvalContextInfo evalCtxInfo,
                                          int outerRole, int innerRole
                                          )
                                          throws CEPException
  {     
    //get domain index factory
    IIndexTypeFactory indexTypeFactory = indexInfo.getIndexTypeFactory();
    //create domain index. send null as args currently.
    //FIXME: check what args should be sent
    IIndex domainIndex = indexTypeFactory.create(null);
    //setup domainIndex in the proxy 
    inIdx.setDomainIndex(domainIndex);
    //set eval context
    inIdx.setEvalContext(evalContext);
    //set key expr type, eval and addr where the result of key evaluation will 
    //be found
    ExprUserDefFunc funcExpr = (ExprUserDefFunc)pred.getLeft();
    Expr[] args = funcExpr.getArgs();
    Expr argExpr = args[indexCollectionArgPos];
    
    int[] inpRoles = new int[2];
    inpRoles[Constants.OUTER] = outerRole;
    inpRoles[Constants.INNER] = innerRole;
     
    IAEval indexCollectionArgEval = null;
    Addr   indexCollectionArgEvalAddr = null;
    
    indexCollectionArgEval = AEvalFactory.create(ec);
    indexCollectionArgEvalAddr = ExprHelper.instExpr(ec, argExpr,
                                                     indexCollectionArgEval,
                                                     evalCtxInfo, inpRoles);
    
    indexCollectionArgEval.compile();
    
    inIdx.setIndexCollectionArgExprType(argExpr.getType());
    inIdx.setIndexCollectionArgEval(indexCollectionArgEval);
    inIdx.setIndexCollectionArgEvalAddr(indexCollectionArgEvalAddr);
    
    //update predicate specific information in the index
    updatePredicateSpecificInfo(ec, pred, indexInfo, indexCollectionArgPos,
                                inIdx, evalContext, evalCtxInfo, outerRole,
                                innerRole);
  }
  
  /**
   * Update predicate specific information.
   * This method is called from initExtensibleIndex and can be called directly 
   * as well. Owing to index memory optimization, it is possible that multiple 
   * predicates are associated to a single ExtensibleIndexProxy instance.
   * In such cases this method is used to update the information for a new pred
   * that is being associated with the index instance.
   * @param ec Execution context
   * @param pred Predicate to be associated
   * @param indexInfo Index information
   * @param indexcollectionArgPos Position (in the arguments to the function)
   *                              that has reference to the input which is to
   *                              be indexed
   * @param inIdx ExtensibleIndexProxy instance
   * @param evalContext Evaluation context
   * @param evalCtxInfo Eval context information
   * @param outerRole Role where tuple from OUTER side will be found
   * @param innerRole Role where tuple from INNER side will be found
   * @throws CEPException
   */
  private static void updatePredicateSpecificInfo(ExecContext ec, BoolExpr pred,
                                                  IIndexInfo indexInfo, 
                                                  Integer indexCollectionArgPos,
                                                  ExtensibleIndexProxy inIdx,
                                                  IEvalContext evalContext, 
                                                  EvalContextInfo evalCtxInfo,
                                                  int outerRole, int innerRole
                                                  )
                                                  throws CEPException
  { 
    //add indexinfo structure
    inIdx.addIndexInfo(indexInfo);
   
    //create necessary evals
    ExprUserDefFunc funcExpr = (ExprUserDefFunc) ((BaseBoolExpr)pred).getLeft();
    Expr[] args = funcExpr.getArgs();
    
    IAEval argsEval = null;
    List<Addr>     addrList = new LinkedList<Addr>();
    List<Datatype> typeList = new LinkedList<Datatype>();
    Addr temp = null;
    
    //For update operation - insert, delete etc where we need to find 
    //the key from the tuple received on the inner side use update_role.
    //Bind the tuple received to the UPDATE_ROLE and look up in keyPos.
    //For argsEval - used for scan. args can refer tuple in outer input
    //(outer_role). The inner input will not be bound to any role 
    //(including SCAN_ROLE) at the time of argument evaluation before
    //calling index scan. However, the fact that the index is being used
    //itself means that there are no references to the inner input's 
    //attributes, except for the collection position, where there is no
    //argument instantiation (null is passed)

    int[] inpRoles = new int[2];
    inpRoles[Constants.OUTER] = outerRole;
    inpRoles[Constants.INNER] = innerRole;
    
    //create args Eval.
    argsEval = AEvalFactory.create(ec);
    for(int i = 0; i < args.length; i++)
    {
      //skip the collection argument
      if(i != indexCollectionArgPos)
      {
        temp = ExprHelper.instExpr(ec, args[i], argsEval, evalCtxInfo,
                                   inpRoles);
        addrList.add(temp);
      }
      else
        addrList.add(null);
      
      typeList.add(args[i].getType());
    }
    
    argsEval.compile();
    
    //set eval in the proxy
    inIdx.addArgsEval(argsEval);
    inIdx.addArgsAddrList(addrList);
    inIdx.addArgsTypeList(typeList);    
  }
  
  /**
   * Method to check the feasibility of an extensibleIndex creation 
   * for a give predicate
   * @param pred Predicate whose feasibility needs to be checked
   * @param ec Execution context
   * @param paramPos Position(in the arguments of the function) which
   *                 has the argument referencing an input on which
   *                 index can be created. 
   * @return Array of IIndexInfo. The possible indexing schemes that
   *         can be used for index creation.
   * @throws CEPException
   */
  static IIndexInfo[] canExtensibleIndexBeCreated(BaseBoolExpr pred, 
                                                  ExecContext ec, 
                                                  Integer paramPos)
                                                  throws CEPException
  {
    ExprUserDefFunc func = (ExprUserDefFunc) pred.getLeft();
    
    ICartridge cartridge = null;
    IUserFunctionMetadata fn = null;
    String cartridgeName = func.getCartridgeLinkName();
    Expr[] args = func.getArgs();
    
    //Function cannot be ordinary internal CQL function
    assert cartridgeName != null: "Function referenced in the predicate is " +
      "not an external/catridge function";
    //Iterate through args to create dts array
    
    Datatype[] dts = new Datatype[args.length];
    for(int i=0; i < args.length; i++)
    {
      dts[i] = args[i].getType();
    }
    
    cartridge = 
      CartridgeHelper.findCartridge(ec, cartridgeName, -1, -1);

    ICartridgeContext context = 
      CartridgeHelper.createCartridgeContext(ec);
    
    try
    {
      IUserFunctionMetadataLocator funcLocator = 
        cartridge.getFunctionMetadataLocator();
      
      
      fn = funcLocator.getFunction(func.getFuncName(), dts, context);
      
      if (fn == null) 
        throw new MetadataException(MetadataError.FUNCTION_NOT_FOUND, 
          new String [] {func.getFuncName()});
      
    } catch (CartridgeException e)
    {
      throw new MetadataException(MetadataError.FUNCTION_NOT_FOUND, 
          new String [] {func.getFuncName()});
    }
    
    IIndexInfoLocator indexLocator = cartridge.getIndexInfoLocator();
    IIndexInfo[] indexInfos = indexLocator.getIndexInfo(fn, paramPos.intValue(), context);
    
    return indexInfos;
  }
  
  /**
   * Determines if the predicate is a hash index predicate
   * @param ctx BinJoinCommonContext. Null if called for stream join.
   * @param pred predicate to be checked
   * @return true if the predicate is hash indexing predicate, false otherwise.
   */
  private static boolean isHashIndexPredicate(BinJoinCommonContext ctx, 
                                       BaseBoolExpr pred)
  {
    if((pred.getOper() != null)
        && (pred.getLeft().getKind() == ExprKind.ATTR_REF)
        && (pred.getRight().getKind() == ExprKind.ATTR_REF)
        && (pred.getOper() == CompOp.EQ)
        && (((ExprAttr) pred.getLeft()).getAValue().getInput() != 
            ((ExprAttr) pred.getRight()).getAValue().getInput()))
    {
      assert ((((ExprAttr) pred.getLeft()).getAValue().getInput() ==
               Constants.OUTER) 
             || (((ExprAttr) pred.getLeft()).getAValue().getInput() == 
                 Constants.INNER)
             );

      assert ((((ExprAttr) pred.getRight()).getAValue().getInput() == 
               Constants.OUTER)
             || (((ExprAttr) pred.getRight()).getAValue().getInput() == 
                 Constants.INNER)
             );

      // Bring the predicates to normalized form to help later processing
      normalizePredicates(ctx, pred);
      
      return true;
    }
    return false;
  }
  
  /**
   * Split the join predicate into different categories. This function is
   * called only for StreamJoin variants. 
   * @param preds List of join predicates
   * @param extensibleIndexPreds List which will have extensible indexing preds
   * @param hashIndexPreds List which will have hashindexing preds
   * @param nonIndexPreds List which will have preds that cannot be indexed
   * @param posList List which will have one argument position corresponding to
   *                every entry in extensibleIndexing preds list
   */
  static void splitPred(LinkedList<BoolExpr> preds, 
                        List<BaseBoolExpr> extensibleIndexPreds,
                        List<BoolExpr> hashIndexPreds,
                        List<BoolExpr> nonIndexPreds,
                        List<Integer>  posList)
  {
    Iterator<BoolExpr> iter = preds.iterator();
    
    while (iter.hasNext())
    {
      BoolExpr bPred = iter.next();
      
      //set nonIndex to true initially
      boolean nonIndex = true;
      
      if(bPred.getKind().equals(ExprKind.BASE_BOOL_EXPR))
      {
        BaseBoolExpr pred = (BaseBoolExpr)bPred;
        
        if(isHashIndexPredicate(null, pred))
        {
          hashIndexPreds.add(pred);
          nonIndex = false;
        }
        else if(
                 ((pred.getOper() != null) && (pred.getOper() == CompOp.EQ))
                 && 
                 (
                   (
                     (pred.getLeft().getKind() == ExprKind.USER_DEF)
                     && (pred.getLeft().getType() == Datatype.BOOLEAN)
                     && (((ExprUserDefFunc) pred.getLeft()).
                        getCartridgeLinkName() != null)
                     && (pred.getRight().getKind() == ExprKind.CONST_VAL)
                     && (pred.getRight() instanceof ExprBoolean)
                     && (((ExprBoolean)pred.getRight()).getBValue())
                   )
                   ||
                   (
                     (pred.getRight().getKind() == ExprKind.USER_DEF)
                     && (pred.getRight().getType() == Datatype.BOOLEAN)
                     && (((ExprUserDefFunc) pred.getRight()).
                        getCartridgeLinkName() != null)
                     && (pred.getLeft().getKind() == ExprKind.CONST_VAL)
                     && (pred.getLeft() instanceof ExprBoolean)
                     && (((ExprBoolean)pred.getLeft()).getBValue())
                   )
                 )
               )
        {
          normalizeExtensiblePredicate(pred);
          
          ExprUserDefFunc func = (ExprUserDefFunc) pred.getLeft();
          Expr[] args = func.getArgs();
          
          int paramPos = -1;
          if(args != null)
          {
            for(int i=0; i < args.length; i++)
            {             
              //get the attrs referenced by the argument expression
              List<Attr> attrs = new LinkedList<Attr>();
              args[i].getAllReferencedAttrs(attrs);
              
              int leftSideAttrsReferredByArg = 0;
              int rightSideAttrsReferredByArg = 0;
              
              for(Attr a : attrs)
              {
                if(a.getInput() == Constants.OUTER) 
                  leftSideAttrsReferredByArg++;
                else
                  rightSideAttrsReferredByArg++;
              }
              
              if((leftSideAttrsReferredByArg > 0)
                 && (rightSideAttrsReferredByArg > 0))
              {
                nonIndex = true;
                break;
              }
              else if((leftSideAttrsReferredByArg == 0)
                      && (rightSideAttrsReferredByArg == 0))
                continue;
              else if(rightSideAttrsReferredByArg > 0)
              {
                if(nonIndex)
                {
                  nonIndex = false;
                  paramPos = i;
                }
                else
                {
                  nonIndex = true;
                  break;
                }
              }
            }//end for
          }
          if(!nonIndex)
          {
            extensibleIndexPreds.add(pred);
            posList.add(new Integer(paramPos));
          }
        } //end func related check
      } //end base bool expr
  
      if(nonIndex)
        nonIndexPreds.add(bPred);
    }//end while
  }
  
  /**
   * Split the join predicates into different categories
   * @param preds List of join predicates
   * @param leftExtenPreds List of extensible indexing preds for left input
   * @param rightExtenPreds List of extensible indexing preds for right input
   * @param hashIndexPreds List of hash indexing preds
   * @param nonIndexPreds List of predicates which cannot be indexed
   * @param leftPosList Position list corresponding to extensible preds for
   *                    left input
   * @param rightPosList Position list corresponding to extensible preds for 
   *                     right input
   * @param ctx Join context
   */
  
  static void splitPred(List<BoolExpr> preds, 
                        List<BaseBoolExpr> leftExtenPreds,
                        List<BaseBoolExpr> rightExtenPreds,
                        List<BoolExpr> hashIndexPreds,
                        List<BoolExpr> nonIndexPreds,
                        List<Integer> leftPosList,
                        List<Integer> rightPosList,
                        BinJoinCommonContext ctx)
  {
    Iterator<BoolExpr> iter = preds.iterator();
    
    while (iter.hasNext())
    {
      BoolExpr bPred = iter.next();
      
      //set nonIndex to true initially
      boolean nonIndex = true;
      
      if(bPred.getKind().equals(ExprKind.BASE_BOOL_EXPR))
      {
        BaseBoolExpr pred = (BaseBoolExpr)bPred;
      
        if(isHashIndexPredicate(ctx, pred))
        {
          hashIndexPreds.add(pred);
          nonIndex = false;
        }
        else if(
                 ((pred.getOper() != null) && (pred.getOper() == CompOp.EQ))
                 && 
                 (
                   (
                     (pred.getLeft().getKind() == ExprKind.USER_DEF)
                     && (pred.getLeft().getType() == Datatype.BOOLEAN)
                     && (((ExprUserDefFunc) pred.getLeft()).
                        getCartridgeLinkName() != null)
                     && (pred.getRight().getKind() == ExprKind.CONST_VAL)
                     && (pred.getRight() instanceof ExprBoolean)
                     && (((ExprBoolean)pred.getRight()).getBValue())
                   )
                   ||
                   (
                     (pred.getRight().getKind() == ExprKind.USER_DEF)
                     && (pred.getRight().getType() == Datatype.BOOLEAN)
                     && (((ExprUserDefFunc) pred.getRight()).
                        getCartridgeLinkName() != null)
                     && (pred.getLeft().getKind() == ExprKind.CONST_VAL)
                     && (pred.getLeft() instanceof ExprBoolean)
                     && (((ExprBoolean)pred.getLeft()).getBValue())
                   )
                 )
               )
        {
          normalizeExtensiblePredicate(pred);
          
          ExprUserDefFunc func = (ExprUserDefFunc) pred.getLeft();
          Expr[] args = func.getArgs();
          
          /* Possible cases:
           * 1. Exactly one arg refers left input, all others refer right 
           *    and/or are constant
           *    - Create index on left (outer) synopsis, if possible
           * 2. Exactly one arg refers right input, all others refer left 
           *    and/or are constant
           *    - Create index on right (inner) synopsis, if possible
           * 3. Exactly one arg refers left input, and exactly other refers 
           *    right input, Others if present are constants
           *    - Create two indexes, one on each synopsis, if possible
           * 4. Exactly one arg refers either of the two inputs, none refers 
           *    the other. Remaining args if present are constants
           *    - Create an index on that input synopsis, if possible 
           * 5. Among the arguments of the predicate, more than one arg refers 
           *    left input and more than one refers right input.
           *    e.g f(arg1,arg2,arg3,arg4...) where arg1 and arg2 refer left 
           *    input and the other two arg3 and arg4 refer right input.
           *    - Not an extensible indexing predicate
           * 6. Among the arguments of the predicate, more than one arg refers 
           *    one input and no argument refers the other input.
           *    e.g f(arg1,arg2,arg3,arg4...) where all arguments refer left
           *    inputs and none refers the right input. 
           *    - Not an extensible indexing predicate
           * 7. None of the argument refers either of the inputs
           *    - Not an extensible indexing predicate
           */
          int numOfLeftSideReferringArgs  = 0;
          int numOfRightSideReferringArgs = 0;
          int paramLeftPos  = -1;
          int paramRightPos = -1;
          
          //set to false here
          nonIndex = false;
          
          if(args != null)
          {
            for(int i=0; i < args.length; i++)
            {
              //Get the attrs referenced by the argument expression
              List<Attr> attrs = new LinkedList<Attr>();
              args[i].getAllReferencedAttrs(attrs);
              
              int leftSideAttrsReferredByArg  = 0;
              int rightSideAttrsReferredByArg = 0;
              
              //calculate the number of attrs from left and right side that 
              //are referenced by the argument expression.
              for(Attr a : attrs)
              {
                if(a.getInput() == Constants.OUTER)
                 leftSideAttrsReferredByArg++;
                else
                  rightSideAttrsReferredByArg++;
              }
              
              /*
               * 1. If an argument expression refers attrs from both sides then
               *    this is NOT an extensible indexing predicate
               * 2. If an argument expression does not refer any attr then 
               *    just continue checking for next argument
               * 3. If only left side attrs are referred in arg expression then
               *    then update numLeftReferringArgs
               * 4. If only right side attrs are referred in arg expression then
               *    then update numRightReferrringArgs
               */
              
              if((leftSideAttrsReferredByArg > 0)
                 && (rightSideAttrsReferredByArg > 0))
              {
                nonIndex = true;
                break;
              }
              else if((leftSideAttrsReferredByArg == 0)
                      && (rightSideAttrsReferredByArg == 0))
              {
                continue;
              }
              else if(leftSideAttrsReferredByArg > 0)
              {
                if(numOfLeftSideReferringArgs == 0)
                {
                  numOfLeftSideReferringArgs++;
                  paramLeftPos = i;
                }
                else
                {
                  if(numOfRightSideReferringArgs > 1)
                  { //Among the arguments to the function call in the pred,
                    //more than one argument refers left side attr and 
                    //more than one argument refers right side attr. ex.
                    //f(arg1, arg2, arg3, arg4,..) where arg1 and arg2 refer
                    //left side attrs and the other two refer right side attrs.
                    //So not an extensible indexing predicate.
                    
                    nonIndex = true;
                    break;
                  }
                  numOfLeftSideReferringArgs++;
                }
              }
              else //Only right side attrs are referenced in the arg expression
              {
                if(numOfRightSideReferringArgs == 0)
                {
                  numOfRightSideReferringArgs++;
                  paramRightPos = i;
                }
                else
                {
                  if(numOfLeftSideReferringArgs > 1)
                  {
                    ////Among the arguments to the function call in the pred,
                    //more than one argument refers left side attr and 
                    //more than one argument refers right side attr. ex.
                    //f(arg1, arg2, arg3, arg4,..) where arg1 and arg2 refer
                    //left side attrs and the other two refer right side attrs.
                    //So not an extensible indexing predicate.
                    nonIndex = true;
                    break;
                  }
                  numOfRightSideReferringArgs++;
                }
              }
                
            } //end for
          }
          
          //more than one arg refers an input, none refers other OR
          //none of the args refer any of the inputs 
          if(((numOfLeftSideReferringArgs > 1) 
              && (numOfRightSideReferringArgs == 0)) 
             ||
             ((numOfRightSideReferringArgs > 1) 
              && (numOfLeftSideReferringArgs == 0))
             ||
             ((numOfLeftSideReferringArgs == 0)
               && (numOfRightSideReferringArgs == 0))
            )
          {
            nonIndex = true;
          }
          
          if(!nonIndex) 
          {
            //if 
            if((numOfLeftSideReferringArgs != 1)
                && (numOfRightSideReferringArgs != 1))
              nonIndex = true;
            else
            {
              if(numOfLeftSideReferringArgs == 1)
              {
                leftExtenPreds.add(pred);
                leftPosList.add(paramLeftPos);
              }
              
              if(numOfRightSideReferringArgs == 1)
              {
                rightExtenPreds.add(pred);
                rightPosList.add(paramRightPos);                               
              }
            }  
          }
        } //end func related check
      } //end base bool expr
      
      if(nonIndex)
      {
        nonIndexPreds.add(bPred);
      }
    }//end while
  }
  
  /**
   * Normalize predicate.
   * If ATTR_REF on left side of predicate refers RIGHT input then swap
   * the LEFT and RIGHT sides of predicate and vice versa. 
   * @param ctx Join context. Null if called for strm join cases.
   * @param pred Predicate to be normalized
   */
  private static void normalizePredicates(BinJoinCommonContext ctx,
                                          BaseBoolExpr pred)
  {
    // Bring the predicates to normalized form to help later processing
    if (((ExprAttr) pred.getLeft()).getAValue().getInput() == Constants.INNER)
    {
      Expr temp = pred.getLeft();
      pred.setLeft(pred.getRight());
      pred.setRight(temp);
      // Note: On normalization, OuterJoinType will be changed only if the
      // join predicate specifies outer join type info (using + operator)
      // Otherwise we will not modify the outer join type.
      if(ctx != null)
      { 
        if(!ctx.isANSIOuterJoin())
        {
          if(ctx.getOuterJoinType() == OuterJoinType.LEFT_OUTER)
            ctx.setOuterJoinType(OuterJoinType.RIGHT_OUTER);
          else if(ctx.getOuterJoinType() == OuterJoinType.RIGHT_OUTER)
            ctx.setOuterJoinType(OuterJoinType.LEFT_OUTER);
        }
      }
    }
  }
  
  /**
   * Normalize extensible indexing predicate.
   * If left side of predicate is EXPR_BOOLEAN and right side is
   * EXPRUSERDEFFUNC then swap the the two sides. 
   * @param pred Predicate to be normalized.
   */
  private static void normalizeExtensiblePredicate(BaseBoolExpr pred)
  {
    if(pred.getRight().getKind() == ExprKind.USER_DEF)
    {
      Expr temp = pred.getLeft();
      assert temp.getKind()==ExprKind.CONST_VAL && temp instanceof ExprBoolean;
      pred.setLeft(pred.getRight());
      pred.setRight(temp);
    }
  }
  
  
  /**
   * Returns the NULL tuple
   * @param ec Execution context
   * @param op Physical operator
   * @return Null tuple
   * @throws ExecException
   */
  @SuppressWarnings("unchecked")
  static ITuplePtr getNullTuple(ExecContext ec, PhyOpt op)
                                throws ExecException
  {
    FactoryManager factoryMgr = ec.getServiceManager().getFactoryManager();
    TupleSpec spec = new TupleSpec(factoryMgr.getNextId(), op);
    IAllocator stf = factoryMgr.get(spec);
    ITuplePtr tPtr = (ITuplePtr)stf.allocate(); //SCRATCH_TUPLE
    
    // remove assertion as part of isUseless Project optimization
    // Datatype[] types = op.getAttrTypes();
    // assert (types.length == numLeftCols);
    ITuple t = tPtr.pinTuple(IPinnable.WRITE);
    t.init(spec, true /* nullValue */);
    tPtr.unpinTuple();
    return tPtr;
  }
  
  /**
   * Method to iterate over a list of candidate extensible indexing predicates
   * and create the indexes wherever feasible.
   * Also takes care of index memory optimization.
   * @param extenPreds List of extensible indexing predicates
   * @param indexedCollectionPosList Position list corresponding to 
   *                                 extensibleIndexing preds
   * @param nonIndexPreds Predicates that cannot be indexed for that input side
   * @param indexes List of indexes for a input side. This will get populated.
   * @param ec Execution context
   * @param evalContext Evaluation context
   * @param evalCtxInfo Eval context information
   * @param outRole Role in the evaluation context where tuple from OUTER side 
   *                can be found
   * @param inRole Role in the evaluation context where tuple from INNER side 
   *               can be found 
   * @return true if at least one extensible index is created during invocation
   * @throws CEPException
   */
  static boolean createExtensibleIndexes(List<BaseBoolExpr> extenPreds,
                                         List<Integer>   indexedCollectionPosList,
                                         List<BoolExpr>  nonIndexPreds,
                                         List<Index>     indexes,
                                         ExecContext     ec,
                                         IEvalContext    evalContext,
                                         EvalContextInfo evalCtxInfo,
                                         int             outRole,
                                         int             inRole
                                         ) throws CEPException
  {
    HashMap<IndexMapKey, ExtensibleIndexProxy> indexMap =
      new HashMap<IndexMapKey, ExtensibleIndexProxy>();
    
    boolean extensibleIndexCreated = false;
    ListIterator<BaseBoolExpr> iterator = extenPreds.listIterator();
    Iterator<Integer> posIterator = indexedCollectionPosList.iterator();
    
    BaseBoolExpr          cur = null;
    int indexCollectionArgPos = -1; 
    Index                 idx = null;
    
    //Iterate through the list 
    
    while(iterator.hasNext())
    {
      cur = iterator.next();
      indexCollectionArgPos = posIterator.next();   
      IIndexInfo[] indexInfos = null;
      
      indexInfos = JoinHelper.canExtensibleIndexBeCreated(cur, ec, 
                                                         indexCollectionArgPos);
      
      if(indexInfos != null && indexInfos.length > 0)
      {
        //FIXME
        //What happens if multiple IIndexInfo are returned? 
        //For the time being, we choose the first.
        IIndexInfo indexInfo = indexInfos[0];
       
        ExprUserDefFunc userDefExpr = 
          (ExprUserDefFunc)cur.getLeft();
        Expr indexCollectionArgExpr =
          userDefExpr.getArgs()[indexCollectionArgPos];
       
        IndexMapKey curKey = new IndexMapKey(indexInfo.getIndexTypeFactory(),
                                             indexCollectionArgExpr);
       
        idx = indexMap.get(curKey);
        
        //lookup indexmap to see if an instance for this indexmapkey was created
        
        if(idx != null)
        {
          ExtensibleIndexProxy extIdx = (ExtensibleIndexProxy) idx;
          JoinHelper.updatePredicateSpecificInfo(ec, cur, indexInfo, 
                                                 indexCollectionArgPos, 
                                                 extIdx, evalContext,
                                                 evalCtxInfo, outRole,
                                                 inRole
                                                 );
        }
        else
        {
          //create proxy and domain index
          idx = new ExtensibleIndexProxy();
        
          //initialize
          JoinHelper.initExtensibleIndex(ec, cur, indexInfo, 
                                         indexCollectionArgPos,
                                         (ExtensibleIndexProxy)idx,
                                         evalContext, evalCtxInfo,
                                         outRole, inRole
                                         );
          
          //put the index in the map
          indexMap.put(curKey, (ExtensibleIndexProxy)idx);
        
          //add to the list of indexes for this synopsis 
          indexes.add(idx);
          
          extensibleIndexCreated = true;
        }
        
        if(nonIndexPreds.contains(cur))
        {
          nonIndexPreds.remove(cur);
        }
        
        //If results are exact no need to call the functional form
        //so remove from the extensibleIndexPred list as those
        //remaining here at the end of this for loop are added to 
        //nonIndexPreds list

        if(indexInfo.areResultsExact())
        {
          iterator.remove();             
        }
      
      }       
    } //end while
    
    if(extenPreds.size() > 0)
    {
      //add remaining to nonIndexPreds (those for which index cannot 
      //be created or the results returned by index are inexact)
      nonIndexPreds.addAll(extenPreds);
      extenPreds.clear();
    }
    
    //clear out the index map
    indexMap.clear();
    
    return extensibleIndexCreated;
  }
  
  /**
   * Create and return evaluator for predicates which cannot be indexed
   * @param nonIndexPreds List of preds which cannot be indexed
   * @param ec Execution context
   * @param outRole Role where tuple from OUTER side will be found
   * @param inRole Role where tuple from INNER side will be found
   * @param evalContext Evaluation context
   * @param evalCtxInfo Eval context information
   * @return Evaluator for non-indexed preds 
   * @throws CEPException
   */
  static IBEval getNonIndexPredsEval(List<BoolExpr> nonIndexPreds,
                                     ExecContext ec, int outRole, int inRole,
                                     IEvalContext evalContext,
                                     EvalContextInfo evalCtxInfo
                                     )
                                     throws CEPException
  {
    IBEval neEval = null;
    if(nonIndexPreds.size() != 0)
    {
      // Construct evaluator to check non-equality predicates
      neEval = BEvalFactory.create(ec);

      int[] roleMap = new int[2];
      roleMap[0] = outRole;
      roleMap[1] = inRole;

      Iterator<BoolExpr> iter = nonIndexPreds.iterator();
      while (iter.hasNext())
      {
        BoolExpr neExpr = iter.next();
        ExprHelper.instBoolExpr(ec, neExpr, neEval, evalCtxInfo, false,
                                roleMap);
      }
    }
    
    return neEval; 
  }
  
  /**
   * Create and return synopsis
   * @param ec Execution context
   * @param op Physical operator
   * @param p_Syn Physical synopsis
   * @return Relational synopsis instance
   * @throws CEPException
   */
  static RelationSynopsisImpl createSynopsis(ExecContext ec,                      
                                             PhyOpt op,
                                             PhySynopsis p_Syn
                                             )
                                             throws CEPException
  {
    ObjectFactoryContext allCtx = new ObjectFactoryContext(ec);
    allCtx.setOpt(op);
    allCtx.setObjectType(RelationSynopsisImpl.class.getName());
    RelationSynopsisImpl e_Syn = 
      (RelationSynopsisImpl)ObjectManager.allocate(allCtx);
    p_Syn.setSyn(e_Syn);
    
    return e_Syn;
  }
  
  /**
   * Initialize the relation synopsis
   * @param e_Syn Relation synopsis instance
   * @param neEval non-indexed preds evaluator
   * @param evalContext Evaluation context
   * @param execStore Execution store instance
   * @param indexes List of indexes created
   * @return Scan id
   * @throws CEPException
   */
  static int setUpSynopsis(RelationSynopsisImpl e_Syn,
                           IBEval neEval, IEvalContext evalContext,
                           ExecStore execStore,
                           List<Index> indexes
                           )
                           throws CEPException
  {
    int scanId = -1;
    //if index created associate it with synopsis
    if(indexes != null && indexes.size() > 0)
      scanId = e_Syn.setIndexScan(neEval, indexes);
    else if(neEval != null)
      scanId = e_Syn.setScan(neEval);
    else
      scanId = e_Syn.setFullScan();
    
    e_Syn.setEvalContext(evalContext);
    e_Syn.setStore((RelStore)execStore);
    e_Syn.initialize();
    e_Syn.setStubId(execStore.addStub());
    return scanId;
  }
  
  public static void processExternalRelation(PhyOpt op,
      LinkedList<BoolExpr> preds,
      IEvalContext evalContext,
      EvalContextInfo evalCtxInfo,
      ExecContext ec,
      FactoryManager factoryMgr,
      BinJoinBase execOptJoin,
      IAllocator<ITuplePtr> outerTupleAllocator,
      boolean isStreamJoin,
      Queue outerInputQueue
      ) 
    throws CEPException
  {
    IAllocator ialloc = null;
   
    assert op instanceof PhyOptJoinBase;
    PhyOptJoinBase phyOptJoin = (PhyOptJoinBase)op;
    
    // Right Input must be External Relation Source
    assert op.getInputs()[Constants.INNER] instanceof PhyOptRelnSrc ||
           op.getInputs()[Constants.INNER] instanceof PhyOptTableFunctionRelnSrc;
    
    // Setting external source name
    String sourceName = null;
    PhyOpt extPhyOpt = op.getInputs()[Constants.INNER];
    if(extPhyOpt instanceof PhyOptRelnSrc)
    {
      int tableId = ((PhyOptRelnSrc)extPhyOpt).getRelId();
      sourceName = ec.getTableMgr().getTableName(tableId);
    }
    else if(extPhyOpt instanceof PhyOptTableFunctionRelnSrc)
    {
      sourceName = ((PhyOptTableFunctionRelnSrc)extPhyOpt).getTableAlias();
    }
    
    IExternalPreparedStatement extPreparedStatement = null;
    
    /** Predicate object will be initialized to non-null object if there are 
     * predicates which can be computed by external connection 
     */
    Predicate externalPredicate = null;      
    AInstr preparedInstr        = null;
    
    
    /** Represents a subset of predicates whose computation is supported
     *  by external connection */
    LinkedList<BoolExpr> supportedPredicates = new LinkedList<BoolExpr>();
    
    /** Represents a subset of predicates whose computation is not supported
     *  by external connection */
    LinkedList<BoolExpr> nonSupportedPredicates = new LinkedList<BoolExpr>();
    
    boolean isRunAwayPredicate = false;
    long externalRowsThreshold = Long.MIN_VALUE;
    
    // Split the predicates into the above two categories:
    // 1) supported & 2) non-supported
    if(preds != null && preds.size() > 0)
    { 
      // External Relation will always be INNER Relation
      IExternalConnection connection 
          = op.getInputs()[Constants.INNER].getExtConnection();
      
      // ExternalQryHelper splits predicates into two categories
      ExternalQryHelper.splitPredicates(preds, 
         supportedPredicates, nonSupportedPredicates, connection, evalCtxInfo,ec);
      
      // Initialize the "externalPredicate" object only if there are non-zero
      // predicates which can be computed by External connection
      if(supportedPredicates.size() > 0)
        externalPredicate = new Predicate();
      
      LogUtil.fine(LoggerType.TRACE, 
          "Number of supported predicates by external connection:" +
          supportedPredicates.size());
      
      LogUtil.fine(LoggerType.TRACE, 
          "Number of non-supported predicates by external connection:" +
          nonSupportedPredicates.size());
      
    }
    
    // Set if the given predicate is a runaway predicate
    // Condition: If no predicate subclause runs on external relation
    if(supportedPredicates.isEmpty())
      isRunAwayPredicate = true;
    
    if(isRunAwayPredicate)
    {
      PhyOpt extPhyOp = op.getInputs()[Constants.INNER];
      if(extPhyOp instanceof PhyOptTableFunctionRelnSrc)
        externalRowsThreshold = Constants.DEFAULT_EXTERNAL_ROWS_THRESHOLD;
      else
      {
        assert extPhyOp instanceof PhyOptRelnSrc;
        int tableId = ((PhyOptRelnSrc)extPhyOp).getRelId();
        externalRowsThreshold 
          = ec.getTableMgr().getExternalRowsThreshold(tableId);
        // Option-1 Check if the external rows threshold is set by EPN config
        // (Threshold configured through EPN will be set in Table Metadata)
        // Assumption: Consider Long.MIN_VALUE is invalid threshold.
        if(externalRowsThreshold == Long.MIN_VALUE)
        {
          // Option-2 (If not set using Option-1)
          // Check if external rows threshold is set by Server config
          // (Threshold configured through server config will be available in
          // config manager)
          externalRowsThreshold = 
            ec.getServiceManager().getConfigMgr().getExternalRowsThreshold();
          
          if(externalRowsThreshold == Long.MIN_VALUE)
          {
            externalRowsThreshold = Constants.DEFAULT_EXTERNAL_ROWS_THRESHOLD; 
          }
        }
      }
     
      // Threshold should always be a positive long value
      assert externalRowsThreshold >= 0;
    }
    
    // External Connection will evaluate the predicates "supportedPredicates"
    // Remaining set of predicates "nonSupportedPredicates" will be evaluated
    // by a binary evaluator IBEval
   
    // Prepare the binary evaluators for "nonSupportedPredicates"
    IBEval nsPredEval_out = null;  // calculated when a tuple comes on OUTER
    IBEval nsPredEval_in  = null;  // calculated when a tuple comes on INNER
    
    if(nonSupportedPredicates != null && nonSupportedPredicates.size() > 0)
    {
      if(!isStreamJoin)
      {
        nsPredEval_out
          = JoinHelper.getNonIndexPredsEval(nonSupportedPredicates, ec,
            IEvalContext.SCAN_ROLE, IEvalContext.INNER_ROLE, evalContext, 
            evalCtxInfo);
        nsPredEval_out.setNullEqNull(false);
        nsPredEval_out.compile();
      }
     
     /**
      Note: nsPredEval_in will be never used in evaluation as no tuple will
      arrive at INNER(External) side; INNER side will be pull only
      TODO: Need to cleanup this code
      */ 
      nsPredEval_in 
        = JoinHelper.getNonIndexPredsEval(nonSupportedPredicates, ec,
            IEvalContext.OUTER_ROLE, IEvalContext.SCAN_ROLE, evalContext,
            evalCtxInfo);
      nsPredEval_in.setNullEqNull(false);
      nsPredEval_in.compile();
    }
    
    // AEval for evaluating Prepared statement
    IAEval stmtEval = AEvalFactory.create(ec);
   
    // Construct Inner Synopsis      
    PhySynopsis p_inSyn = phyOptJoin.getInnerSyn();
    assert p_inSyn != null;
    assert p_inSyn.getKind() == SynopsisKind.EXT_SYN : p_inSyn.getKind();
    
    ObjectFactoryContext allCtx = new ObjectFactoryContext(ec);
    allCtx.setOpt(op);      
    allCtx.setObjectType(ExternalSynopsisImpl.class.getName());
    ExternalSynopsisImpl e_inSyn
      = (ExternalSynopsisImpl)ObjectManager.allocate(allCtx);
    p_inSyn.setSyn(e_inSyn);     
    e_inSyn.setTupleSpec(new TupleSpec(factoryMgr.getNextId(), 
        phyOptJoin.getInputs()[Constants.INNER]));     
   ialloc = factoryMgr.get(e_inSyn.getTupleSpec());
   e_inSyn.setFactory(ialloc);
   
   // Set the binary evaluator to compute "nonsupportedPredicates"
   e_inSyn.setScan(nsPredEval_in);
   
   // Set whether the given predicate is runaway
   e_inSyn.setRunAwayPredicate(isRunAwayPredicate);
   // Set the maximum allowed rows to be fetched from external relation
   // when joined with stream
   e_inSyn.setExternalRowsThreshold(externalRowsThreshold);
   // Set the external relation source name
   e_inSyn.setExtSourceName(sourceName);
    
   ConnectionRecoveryContext connRecContext = null;
   
   //CASE: When External relation is Table function expression
   if(phyOptJoin.isTableFunctionExternalJoin())
    {
      TableFunctionExternalQryHelper.prepareStmtEval(stmtEval,
          evalCtxInfo,
          phyOptJoin.getTableFunctionInfo().getTableFunctionExpr(),
          ec
          );       
      
      phyOptJoin.initTableFunctionDataSource(stmtEval,
          phyOptJoin.getTableFunctionInfo());
      
      preparedInstr = TableFunctionExternalQryHelper.getPreparedInstr();
      
      extPreparedStatement 
        = TableFunctionExternalQryHelper.getPreparedStmt(op,
            ec,
            phyOptJoin.getTableFunctionInfo());
      
      preparedInstr.extrInstr.setPreparedStmt(extPreparedStatement);
      preparedInstr.extrInstr.setExtSourceName(sourceName);
      
      // Prepare a tuple allocator which will create tuples where
      // output of table function will be stored
      TupleSpec tupSpec = new TupleSpec(factoryMgr.getNextId());
      tupSpec.addAttr(
          phyOptJoin.getTableFunctionInfo().getReturnCollectionType());
      IAllocator<ITuplePtr> srcFactory = factoryMgr.get(tupSpec);
      e_inSyn.setSourceFactory(srcFactory);
    }
    else
    {
      int[] inpRoles = new int[2];
      inpRoles[Constants.OUTER] = IEvalContext.OUTER_ROLE;
      inpRoles[Constants.INNER] = IEvalContext.INNER_ROLE;
      
      // Setup externalPredicate, stmtEval and Get ExternalInst
      preparedInstr
       = ExternalQryHelper.getPreparedInstr(ec, 
                                           supportedPredicates, 
                                           stmtEval, 
                                           inpRoles, 
                                           evalCtxInfo, 
                                           externalPredicate,
                                           Constants.INNER);
      
      // Get and Set Prepared statement
      connRecContext = 
    	new ConnectionRecoveryContext(externalPredicate, op, preparedInstr);
      
      extPreparedStatement 
        = ExternalQryHelper.getPreparedStmt(op, 
                                            ec, 
                                            externalPredicate,
                                            connRecContext,
                                            null,
                                            null);    
      
      preparedInstr.extrInstr.setPreparedStmt(extPreparedStatement);  
      preparedInstr.extrInstr.setExtSourceName(sourceName);
    }
    stmtEval.compile(); 
    PhyOptRelnSrc phyoptrelSrc = (PhyOptRelnSrc)op.getInputs()[Constants.INNER];
    RelSource relSource = (RelSource) phyoptrelSrc.getInstOp();
    relSource.setPstmt(extPreparedStatement);
    e_inSyn.setExternalPreparedStatement(extPreparedStatement);
    e_inSyn.setEval(stmtEval);
    e_inSyn.setConnectionRecoveryContext(connRecContext);
    execOptJoin.setInnerExtSyn(e_inSyn);
    execOptJoin.setIsExternal(true);
    
    if(!isStreamJoin)
    {
      // Construct outer synopsis
      PhySynopsis p_outSyn = phyOptJoin.getOuterSyn();
      assert p_outSyn != null;
      assert p_outSyn.getKind() == SynopsisKind.REL_SYN : p_outSyn.getKind();
  
      allCtx.setOpt(op);
      allCtx.setObjectType(RelationSynopsisImpl.class.getName());
      RelationSynopsisImpl e_outSyn = 
        (RelationSynopsisImpl)ObjectManager.allocate(allCtx);
      p_outSyn.setSyn(e_outSyn);
      
      if(nsPredEval_out != null)
      {
        int outerScanId = e_outSyn.setScan(nsPredEval_out);        
        execOptJoin.setOuterScanId(outerScanId);        
      }
      int outerFullScanId = e_outSyn.setFullScan();
      execOptJoin.setOuterFullScanId(outerFullScanId);
      e_outSyn.setEvalContext(evalContext);
      ExecStore outerStore = p_outSyn.getStwstore().getInstStore();
      e_outSyn.setStore((RelStore)outerStore);
      e_outSyn.initialize();
      e_outSyn.setStubId(outerStore.addStub());
      execOptJoin.setOuterSyn(e_outSyn);
    }
      // Setup the input stores
      execOptJoin.setOuterTupleStorageAlloc(outerTupleAllocator);
      
      execOptJoin.setInnerTupleStorageAlloc(ialloc);
      // This is done since there will be only one input queue if external
      // If input queue is set then canbeScheduled will access only outer queue
      // We assume that only inner is External relation
      execOptJoin.setInputQueue(outerInputQueue);
    
  }
}
