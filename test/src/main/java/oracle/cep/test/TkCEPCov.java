package oracle.cep.test;
/* $Header: pcbpel/cep/test/src/TkCEPCov.java /main/22 2009/05/12 19:25:47 parujain Exp $ */

/* Copyright (c) 2007, 2009, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    coverage tests file

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    hopark      12/02/08 - move LogLevelManaer to ExecContext
    hopark      10/23/08 - add cepRegistry
    hopark      10/10/08 - remove statics
    hopark      10/09/08 - remove statics
    hopark      03/18/08 - reorg config
    najain      04/24/08 - 
    hopark      01/31/08 - queue allocation change
    hopark      10/30/07 - remove IQueueElement
    hopark      10/22/07 - remove TimeStamp
    mthatte     10/16/07 - 
    hopark      09/17/07 - use getFullScan
    hopark      09/07/07 - eval refactor
    hopark      09/04/07 - 
    parujain    06/26/07 - mutable state
    parujain    03/22/07 - Configurations problem
    najain      03/15/07 - cleanup
    parujain    03/09/07 - Extensible windows
    najain      03/12/07 - bug fix
    hopark      03/07/07 - Use ITuplePtr
    parujain    02/15/07 - system startup
    najain      02/06/07 - coverage
    rkomurav    01/15/07 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/test/src/TkCEPCov.java /main/22 2009/05/12 19:25:47 parujain Exp $
 *  @author  rkomurav
 *  @since   release specific (what release of product did this appear in)
 */

import java.util.Properties;

import oracle.cep.dataStructures.internal.QueueElement;
import oracle.cep.dataStructures.internal.QueueElementImpl;
import oracle.cep.dataStructures.internal.memory.TimestampAttrVal;
import oracle.cep.execution.*;
import oracle.cep.execution.internals.IAEval;
import oracle.cep.dataStructures.internal.memory.Tuple;
import oracle.cep.userDefined.UserFunc;
import oracle.cep.memmgr.FactoryManager;
import oracle.cep.metadata.ConfigManager;
import oracle.cep.server.CEPServerRegistryImpl;
import oracle.cep.service.CEPManager;
import oracle.cep.service.CEPServerRegistry;
import oracle.cep.service.ExecContext;
import oracle.cep.execution.operators.BinJoin;
import oracle.cep.execution.operators.BinStreamJoin;
import oracle.cep.execution.operators.DStream;
import oracle.cep.execution.operators.Except;
import oracle.cep.execution.operators.ExecStats;
import oracle.cep.execution.operators.GroupAggr;
import oracle.cep.execution.operators.IStream;
import oracle.cep.execution.operators.PartitionWindow;
import oracle.cep.execution.operators.Project;
import oracle.cep.execution.operators.RStream;
import oracle.cep.execution.operators.RelSource;
import oracle.cep.execution.operators.RowWindow;
import oracle.cep.execution.operators.StreamSource;
import oracle.cep.execution.operators.ViewRelnSrc;
import oracle.cep.execution.queues.*;
import oracle.cep.execution.internals.*;
import oracle.cep.execution.internals.factory.AEvalFactory;
import oracle.cep.execution.internals.factory.BEvalFactory;
import oracle.cep.execution.indexes.*;
import oracle.cep.dataStructures.internal.memory.TuplePtr;
import oracle.cep.dataStructures.internal.ITuplePtr;

/**
 *  @version $Header: pcbpel/cep/test/src/TkCEPCov.java /main/22 2009/05/12 19:25:47 parujain Exp $
 *  @author  rkomurav
 *  @since   release specific (what release of product did this appear in)
 */
public class TkCEPCov
{
  public static void main(String[] args) throws Exception
  {
    try {
      ConfigManager cfg = new ConfigManager();
      CEPServerRegistryImpl cepReg = new CEPServerRegistryImpl();
      CEPManager cepMgr = CEPManager.getInstance();
      cepMgr.setConfig(cfg);
      cepMgr.setServerRegistry(cepReg);
      cepMgr.init();
      cepReg.init(cepMgr);
    ExecContext ec = cepMgr.getSystemExecContext();
    FactoryManager factoryMgr = cepMgr.getFactoryManager();
    
    IBEval be = BEvalFactory.create(ec);
    be.toString();
    AInstr ai = new AInstr();
    ai.toString();
    
    TimeDuration td = new TimeDuration(4);
    td.setValue(3);
    TimeDuration td1 = new TimeDuration(4);
    TimeDuration td2 = new TimeDuration(6);
    TimestampAttrVal tav = new TimestampAttrVal(1);
    Tuple t = new Tuple();
    t.isBNull();
    t.setBNull(true);
    
    TupleSpec tsp = new TupleSpec(factoryMgr.getNextId());
    tsp.toString();
    //UserFunc f = ec.getExecMgr().getUserFunc(2);
    HashIndex hi = new HashIndex(ec);
    hi.getEvalContext();
    hi.getKeyEqual();
    hi.getScanHashEval();
    hi.getUpdateHashEval();
    hi.getFullScan();
//    ec.getExecMgr().putUserFunc(2,f);
 //   ec.getExecMgr().putUserFunc(2,null);
    IAEval a = AEvalFactory.create(ec);
    ITuplePtr tref = new TuplePtr(new Tuple());
    QueueElementImpl e = new QueueElementImpl(QueueElement.Kind.E_PLUS, 
				      (TuplePtr)tref, 5);
    QueueElementImpl e1 = new QueueElementImpl();
    e.copy(e1);
    SharedQueueReader sr = new SharedQueueReader(ec);
    sr.initialize();
    SharedQueueReaderStats srs = new SharedQueueReaderStats();
    SharedQueueWriter sw = new SharedQueueWriter(ec);
    sw.getNumReaders();
    sw.isEmpty();
    sw.isFull();
    sw.toString();
    
    //getters and setters
    BinJoin bj = new BinJoin(ec);
    bj.getEvalContext();bj.getInnerScanId();bj.getInnerSyn();bj.getJoinSyn();
    bj.getOuterScanId();bj.getOuterSyn();bj.getOutputConstructor();
    bj.getOuterInputQueue();bj.getInnerInputQueue();
    
    BinStreamJoin bsj = new BinStreamJoin(ec);
    bsj.getEvalContext();bsj.getInnerInputQueue();bsj.getInnerScanId();
    bsj.getInnerSyn();bsj.getOuterInputQueue();bsj.getOutputConstructor();
    
    DStream d = new DStream(ec);
    d.getNegEval();
    
    Except eop = new Except(ec);
    eop.getFullScanId();
    
    ExecStats es = new ExecStats();
    es.setNumExecutions(2);es.setNumInputs(2);
    es.avgInputSize();es.selectivity();es.incrSumInputSizes(0);es.getSumInputSizes();
    es.setNumExecutions(0);es.setNumInputs(0);es.setNumOutputs(0);es.setSumInputSizes(0);
    
    GroupAggr ga = new GroupAggr(ec);
    ga.getArithScanNotReqEval();ga.getEvalContext();ga.getEmptyGroupEval();
    ga.getFullScanId();ga.getId();ga.getInitEval();ga.getInputQueue();
    ga.getInSynopsis();ga.getInSynopsis();ga.getMinusEval();
    ga.getNewReaders();ga.getNumFullUDA();ga.getNumUDA();
    ga.getPlusEval();ga.getScanNotReqEval();ga.getUpdateEval();ga.isOneGroup();
    
    IStream is = new IStream(ec);
    is.getPosEval();
    
    PartitionWindow pw = new PartitionWindow(ec);
    pw.getCopyEval();pw.getEvalContext();pw.getSynopsis();pw.getWindow();
    
    Project po = new Project(ec);
    po.getEvalContext();po.getOutSynopsis();po.getProjEvaluator();
    
    RStream rs = new RStream(ec);
    rs.getEvalContext();rs.getSynopsis();rs.getCopyEval();
    
    RelSource res = new RelSource(ec, 1);
    res.getAttrSpecs();res.getEvalContext();res.getFullScanId();res.getNumAttrs();
    res.getScanId();res.getSource();res.getSynopsis();res.setNumAttrs(0);
    
    RowWindow row = new RowWindow(ec);
    row.getWindowSize();row.getWinSynopsis();
    
    oracle.cep.execution.operators.Select sel = new oracle.cep.execution.operators.Select(ec, 1);
    sel.getEvalContext();sel.getFullScanId();sel.getPredicate();
    
    StreamSource ssrc = new StreamSource(ec, 1);
    ssrc.getSource();
    
    ViewRelnSrc vrs = new ViewRelnSrc(ec);
    vrs.getFullScanId();vrs.getSynopsis();
    cepMgr.close();
    System.exit(1);
   }
    catch(Exception e)
    {
      throw(e);
    }
    
  }
}

