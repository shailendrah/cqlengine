/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/planmgr/codegen/OrderByTopFactory.java /main/6 2011/02/07 03:36:26 sborah Exp $ */

/* Copyright (c) 2009, 2011, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    anasrini    12/19/10 - remove eval.setEvalContext
    sborah      10/14/09 - support for bigdecimal
    sbishnoi    03/18/09 - fixing scanHash
    parujain    03/19/09 - stateless server
    sbishnoi    03/09/09 - adding support for partition by clause in order by
    sbishnoi    02/10/09 - Creation
 */

package oracle.cep.planmgr.codegen;

import java.util.ArrayList;

import oracle.cep.common.Datatype;
import oracle.cep.dataStructures.internal.ITuplePtr;
import oracle.cep.exceptions.CEPException;
import oracle.cep.execution.ExecException;
import oracle.cep.execution.comparator.ComparatorSpecs;
import oracle.cep.execution.comparator.TupleComparator;
import oracle.cep.execution.indexes.HashIndex;
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
import oracle.cep.execution.internals.factory.EvalContextFactory;
import oracle.cep.execution.internals.factory.HEvalFactory;
import oracle.cep.execution.operators.ExecOpt;
import oracle.cep.execution.operators.OrderByTop;
import oracle.cep.execution.stores.ExecStore;
import oracle.cep.execution.stores.LineageStore;
import oracle.cep.execution.synopses.LineageSynopsisImpl;
import oracle.cep.memmgr.FactoryManager;
import oracle.cep.memmgr.IAllocator;
import oracle.cep.memmgr.ObjectFactoryContext;
import oracle.cep.memmgr.ObjectManager;
import oracle.cep.metadata.Query;
import oracle.cep.phyplan.PhyOpt;
import oracle.cep.phyplan.PhyOptOrderByTop;
import oracle.cep.phyplan.PhyStoreKind;
import oracle.cep.phyplan.PhySynopsis;
import oracle.cep.phyplan.SynopsisKind;
import oracle.cep.phyplan.attr.Attr;
import oracle.cep.phyplan.expr.Expr;
import oracle.cep.phyplan.expr.ExprOrderBy;
import oracle.cep.service.ExecContext;

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/planmgr/codegen/OrderByTopFactory.java st_pcbpel_anasrini_eval_parallelism_2/1 2010/12/20 07:47:45 anasrini Exp $
 *  @author  sbishnoi
 *  @since   release specific (what release of product did this appear in)
 */

public class OrderByTopFactory extends ExecOptFactory
{
  @Override
  public CodeGenContext createCodeGenContext(ExecContext ec, Query query, 
                                             PhyOpt phyopt)
  {
     return new OrderByTopContext(ec, query, phyopt); 
  }
  
  /**
   * Create and return an instance of OrderByTop execution operator
   */
  @Override
  ExecOpt newExecOpt(CodeGenContext ctx) throws CEPException 
  {
    assert ctx instanceof OrderByTopContext;
    OrderByTopContext octx = (OrderByTopContext)ctx;
    // tuple spec will be same as its input
    PhyOpt op = ctx.getPhyopt();
    TupleSpec tupSpec = CodeGenHelper.getTupleSpec(ctx.getExecContext(), op);
    octx.setTupSpec(tupSpec);
    return new OrderByTop(ctx.getExecContext());
  }

  /**
   * Setup the OrderByTop execution operator
   */
  @Override
  void setupExecOpt(CodeGenContext ctx) throws CEPException 
  {
    // Check if corresponding ExecOpt has created correctly    
    assert ctx.getExecOpt() instanceof OrderByTop;
    assert ctx instanceof OrderByTopContext;
    OrderByTopContext octx = (OrderByTopContext)ctx;
    
    PhyOpt op = ctx.getPhyopt();
    // Check if PhyOpt is an instance of PhyOptOrderByTop
    assert op instanceof PhyOptOrderByTop;
    
    // initialize few variables
    OrderByTop execOpt             = (OrderByTop)ctx.getExecOpt();
    ExecContext ec                 = ctx.getExecContext();
    PhyOptOrderByTop phyOrderByTop = (PhyOptOrderByTop)op;
    
    IEvalContext evalContext     = EvalContextFactory.create(ec);
    
    // Set number of rows in execution operator
    execOpt.setNumOrderByRows(phyOrderByTop.getNumOrderByRows());
    
    // Set the flag whether input to this operator is stream or relation
    assert phyOrderByTop.getInputs()[0] != null;
    execOpt.setIsInputStream(phyOrderByTop.getInputs()[0].getIsStream());
    
    // Prepare TupleComparators and do necessary initializations
    Expr[] phyOrderExprs = phyOrderByTop.getExprs();
    assert phyOrderExprs != null;
    
    // get the number of order-by expressions
    int  numOrderByExprs = phyOrderExprs.length;
    
    // instantiate array of ComparatorSpec one for each priority queue 
    ComparatorSpecs[] comparatorSpecs_final 
      = new ComparatorSpecs[numOrderByExprs];
    
    ComparatorSpecs[] comparatorSpecs_backup 
      = new ComparatorSpecs[numOrderByExprs];
    
    // initialize array of ComparatorSpec
    for(int i=0; i < numOrderByExprs; i++)
    {
      assert phyOrderExprs[i] instanceof ExprOrderBy;
      ExprOrderBy currentOrderByExpr = (ExprOrderBy)phyOrderExprs[i];
      
      int pos = PositionHelper.getExprPos(currentOrderByExpr.getOrderbyExpr());
      
      // comparator for final queue with inverted isNullFirst and isAscending
      // Reason to invert flags: to make final queue as Max Priority queue      
      comparatorSpecs_final[i] = new ComparatorSpecs(pos, 
                                     !currentOrderByExpr.isNullsFirst(),
                                     !currentOrderByExpr.isAscending());
      
      // comparator for backup queue
      // backup queue is Min Priority queue
      comparatorSpecs_backup[i] = new ComparatorSpecs(pos, 
                                      currentOrderByExpr.isNullsFirst(),
                                      currentOrderByExpr.isAscending());
    }
    
    ArrayList<Attr> partitionByAttrs = phyOrderByTop.getPartitionByAttrs();
      
    // Handle partition by attributes (if any)
    if(partitionByAttrs != null)
    {
      int numPartitionByAttrs = partitionByAttrs.size();
      octx.setNumPartitionByAttrs(numPartitionByAttrs);
      
      FactoryManager factoryMgr = ec.getServiceManager().getFactoryManager();
      
      // construct tuple specification where number of max ATTRS will be 
      // numPartitionByAttrs + 1
      // TupleSpec = {partition by attributes, PartitionByContext}
      TupleSpec partitionHdrTupSpec 
        = new TupleSpec(factoryMgr.getNextId(), numPartitionByAttrs + 1);
      
      Attr phyPartitionAttr = null;
      TupleSpec tupSpec = octx.getTupSpec();
      
      // Add all partition attributes to tuple specification
      for(int i = 0; i < numPartitionByAttrs; i++)
      {        
        phyPartitionAttr = partitionByAttrs.get(i);
        partitionHdrTupSpec.addAttr(tupSpec.getAttrMetadata(phyPartitionAttr.getPos()));
      }
      
      octx.setPartitionHdrTupSpec(partitionHdrTupSpec);
      
      // Add another attribute PartitionByContext to keep pointers to the
      // final queue and backup queue.
      int pByContextAttrPos = partitionHdrTupSpec.addAttr(Datatype.OBJECT);
      octx.setPByContextAttrPos(pByContextAttrPos);
      
      IAllocator<ITuplePtr> pHeaderTupFactory = factoryMgr.get(partitionHdrTupSpec);
      
      // initialize partition header index
      HashIndex partitionHdrIndex 
        = getIndex(ec, evalContext, octx, partitionHdrTupSpec, 
                   pHeaderTupFactory, partitionByAttrs);
      
      octx.setPartitionHdrIndex(partitionHdrIndex);
      
      // construct copy eval which will copy partition attributes from input
      // tuple at INPUT_ROLE to partition header tuple at UPDATE_ROLE
      IAEval copyEval = getCopyEval(ec, tupSpec, partitionByAttrs);
      
      // set partition by attributes in execution operator
      execOpt.setNumPartitionByAttrs(numPartitionByAttrs);
      
      // set partition header index in execution operator
      execOpt.setPartitionHdrIndex(partitionHdrIndex);
      
      // set position of PartitionByContext object inside partition hdr tuple
      execOpt.setPartitionContextPos(pByContextAttrPos);
      
      // set factory for partition tuple
      execOpt.setPartitionTupleFactory(pHeaderTupFactory);
      
      // set copyEval in execution operator
      execOpt.setCopyEval(copyEval);
    }
    
    // Set final queue comparator
    TupleComparator tupleComparator = 
      new TupleComparator(comparatorSpecs_final, octx.getTupSpec());
    
    execOpt.setFinalQueueTupleComparator(tupleComparator);
    
    // Set Backup queue comparator
    tupleComparator = new TupleComparator(comparatorSpecs_backup, octx.getTupSpec());
    execOpt.setBackUpQueueTupleComparator(tupleComparator);
    
    // Note: Two cases related with number of partition by attributes
    //  Case 1: if no partition by attributes
    //          then only one instance of each queue (final and backup) will
    //          exist for all input tuples
    //  Case 2: If some partition by attributes exist
    //          then there will be one instance of each queue(final and backup)
    //          for each symbol so we cannot initialize queues at compile time
    
    // initialize final and backup queues if there is no partition by ATTRS
    if(octx.getNumPartitionByAttrs() == 0)
      execOpt.initializeQueues();
    
    // Construct outEval which will copy attribute value from tuple at 
    // INPUT_ROLE to tuple at NEW_OUTPUT_ROLE
    IAEval outEval = getOutEval(ec, op);
    execOpt.setOutEval(outEval);
    
    // Initialize Execution Store and Synopsis
    assert (op.getStore() != null);
    assert (op.getStore().getStoreKind() == PhyStoreKind.PHY_LIN_STORE);
    
    PhySynopsis outSyn = phyOrderByTop.getOutputSyn();
    
    LineageSynopsisImpl  linSyn   = null;
    ExecStore            linStore = null;
    
    assert outSyn != null;
    assert (outSyn.getStwstore() == op.getStore());
    assert outSyn.getKind()  == SynopsisKind.LIN_SYN;
    
    ObjectFactoryContext allCtx = new ObjectFactoryContext(ec);
    allCtx.setOpt(op);
    allCtx.setObjectType(LineageSynopsisImpl.class.getName());
    linSyn = (LineageSynopsisImpl) ObjectManager.allocate(allCtx);
    outSyn.setSyn(linSyn);
    linStore = ctx.getTupleStorage();
    assert linStore instanceof LineageStore;
    linSyn.setStore((LineageStore) linStore);
    linSyn.setStubId(linStore.addStub());
    
    execOpt.setOutSynopsis(linSyn);
    execOpt.setEvalContext(evalContext);
    
  }
  
  /**
   * Get output eval which will copy all the tuple attribute values from 
   * input tuple to output tuple
   * @param ec
   * @param op
   * @return
   * @throws ExecException
   */
  private IAEval getOutEval(ExecContext ec, PhyOpt op) throws ExecException
  {
      IAEval eval = AEvalFactory.create(ec);

      for (int i = 0; i < op.getNumAttrs(); i++)
      {
          AInstr instr = new AInstr();
          instr.op = ExprHelper.getCopyOp(op.getAttrTypes(i));

          // Source
          instr.r1 = IEvalContext.INPUT_ROLE;
          instr.c1 = i;

          // Destination
          instr.dr = IEvalContext.NEW_OUTPUT_ROLE;
          instr.dc = i;

          eval.addInstr(instr);
      }
      eval.compile();

      return eval;
  }
  
  /**
   * Get copy eval to copy all partition attributes from input tuple to newly
   * created partition header tuple
   * @param ec
   * @param op
   * @return
   * @throws ExecException
   */
  private IAEval getCopyEval(ExecContext ec, 
                             TupleSpec tupSpec, 
                             ArrayList<Attr> partitionByAttrs)
    throws ExecException
  {
      IAEval eval = AEvalFactory.create(ec);

      int numPartitionByAttrs = partitionByAttrs.size();
      Attr phyPartitionAttr = null;
      for (int i = 0; i < numPartitionByAttrs; i++)
      {
          AInstr instr = new AInstr();
          phyPartitionAttr = partitionByAttrs.get(i);
          
          instr.op = ExprHelper.getCopyOp(
               tupSpec.getAttrType(phyPartitionAttr.getPos()));

          // Source
          instr.r1 = IEvalContext.INPUT_ROLE;
          instr.c1 = phyPartitionAttr.getPos();

          // Destination
          instr.dr = IEvalContext.UPDATE_ROLE;
          instr.dc = i;

          eval.addInstr(instr);
      }
      eval.compile();

      return eval;
  }
  
  private HashIndex getIndex(ExecContext ec, 
                             IEvalContext evalCtx, 
                             OrderByTopContext octx,
                             TupleSpec partitionHdrTupSpec, 
                             IAllocator<ITuplePtr> factory,
                             ArrayList<Attr> partitionByAttrs)
    throws CEPException
  {
    HashIndex partitionHdrIndex = new HashIndex(ec);
    HInstr hInstr;
    IHEval updHash, scanHash;
    IBEval keyEqual;
    BInstr bInstr;
    int numAttrs = partitionHdrTupSpec.getNumAttrs();
    TupleSpec tupSpec = octx.getTupSpec();

    updHash = HEvalFactory.create(ec, numAttrs);
    // obtain update hash on partition by attributes only
    for (int attr = 0; attr < numAttrs-1; attr++)
    {
      hInstr = new HInstr(partitionHdrTupSpec.getAttrType(attr), 
                          IEvalContext.UPDATE_ROLE,
                          new Column(attr));
      updHash.addInstr(hInstr);
    }
    updHash.compile();

    scanHash = HEvalFactory.create(ec, partitionByAttrs.size());
    // obtain scan hash on partition by attributes only
    for (int attr = 0; attr < partitionByAttrs.size(); attr++)
    {
      hInstr =
        new HInstr(tupSpec.getAttrType(partitionByAttrs.get(attr).getPos()), 
                   IEvalContext.INPUT_ROLE, 
                   new Column(partitionByAttrs.get(attr).getPos()));

      scanHash.addInstr(hInstr);
    }
    scanHash.compile();

    // construct keyEqual BEval only for partition by attributes
    keyEqual = BEvalFactory.create(ec);
    for (int attr = 0; attr < numAttrs-1; attr++)
    {
      bInstr = new BInstr();

      bInstr.op = ExprHelper.getEqOp(partitionHdrTupSpec.getAttrType(attr));
      bInstr.r1 = IEvalContext.INPUT_ROLE;
      bInstr.c1 = new Column(partitionByAttrs.get(attr).getPos());

      bInstr.r2 = IEvalContext.SCAN_ROLE;
      bInstr.c2 = new Column(attr);

      keyEqual.addInstr(bInstr);
    }
    keyEqual.compile();
  
    partitionHdrIndex.setUpdateHashEval(updHash);
    partitionHdrIndex.setScanHashEval(scanHash);
    partitionHdrIndex.setKeyEqual(keyEqual);
    partitionHdrIndex.setEvalContext(evalCtx);
    partitionHdrIndex.setFactory(factory);
    partitionHdrIndex.initialize();
  
    return partitionHdrIndex;   
  }
  
  //@Override
  protected ExecStore instStore(CodeGenContext ctx) throws CEPException
  {
    assert ctx != null;
    assert ctx instanceof OrderByTopContext;
    OrderByTopContext octx = (OrderByTopContext)ctx;
    PhyOpt op = ctx.getPhyopt();
    assert op != null;
    PhyOptOrderByTop opd = (PhyOptOrderByTop)op;
    PhySynopsis outSyn = opd.getOutputSyn();
    ExecStore outStore = null;
    
    outStore = StoreInst.instStore(new StoreGenContext(ctx.getExecContext(),
                                   outSyn.getStwstore(),
                                   octx.getTupSpec()));
    ctx.setTupleStorage(outStore);
    return outStore;    
  }
}
