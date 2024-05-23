/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/operators/Exchange.java /main/10 2014/10/14 06:35:34 udeshmuk Exp $ */

/* Copyright (c) 2011, 2014, Oracle and/or its affiliates. 
All rights reserved.*/

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    sbishnoi    09/24/14 - set isDependentOnPartnStream
    sbishnoi    05/03/14 - support of partitioned stream
    anasrini    10/27/11 - release global planmgr lock before fwding event to
                           partition
    alealves    08/18/11 - XbranchMerge alealves_bug-12888416_cep from main
    anasrini    10/27/11 - XbranchMerge anasrini_bug-13257395_ps5 from
                           st_pcbpel_11.1.1.4.0
    alealves    08/18/11 - logging fix
    anasrini    08/09/11 - XbranchMerge anasrini_bug-12845846_ps5 from
                           st_pcbpel_11.1.1.4.0
    anasrini    06/30/11 - XbranchMerge anasrini_bug-12675151_ps5 from
                           st_pcbpel_11.1.1.4.0
    anasrini    08/08/11 - use ExecManager.insertFast
    anasrini    05/17/11 - XbranchMerge anasrini_bug-12560613_ps5 from main
    sborah      04/11/11 - use ConcurrentHashMap
    vikshukl    05/18/11 - do conditional logging
    anasrini    04/05/11 - handle multi input scenario
    anasrini    03/20/11 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/operators/Exchange.java /main/10 2014/10/14 06:35:34 udeshmuk Exp $
 *  @author  anasrini
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.execution.operators;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

import oracle.cep.dataStructures.external.TupleValue;
import oracle.cep.dataStructures.internal.ITuplePtr;
import oracle.cep.dataStructures.internal.QueueElement;
import oracle.cep.dataStructures.internal.QueueElement.Kind;
import oracle.cep.exceptions.CEPException;
import oracle.cep.exceptions.ExecutionError;
import oracle.cep.execution.ExecException;
import oracle.cep.execution.SoftExecException;
import oracle.cep.execution.internals.Hash;
import oracle.cep.execution.internals.IAEval;
import oracle.cep.execution.internals.IEvalContext;
import oracle.cep.execution.internals.IHEval;
import oracle.cep.execution.queues.QueueReaderContext;
import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;
import oracle.cep.service.ExecContext;
import oracle.cep.service.IServerContext;
import oracle.cep.interfaces.sender.IPartitionAware;
import oracle.cep.interfaces.sender.IPartitionContext;

/**
 * EXCHANGE
 */
public class Exchange extends ExecOpt
{

  /** The evalContext template to be used by the evaluators */
  private IEvalContext evalContext;

  /** 
   * The evaluators for the partitioning expressions.
   * There will be 1 for each input.
   * Further, the eval at array index i, is associated with input i
   */
  private IAEval[] partnExprsEvals;

  /** 
   * The hash evaluators
   * There will be 1 for each input.
   * Further, the eval at array index i, is associated with input i
   *
   * hashEval[i] will hash the result of partnExprsEvals[i]. Then,
   * it will use this Hash as a key into a HashMap and  find one of
   * the 0...(dop-1) query plans (schemas) to use for this partition.
   *
   * hashing will ensure affinity of a partition to a query plan
   */
  private IHEval[] hashEvals;

  /** 
   * The names of the entities involved in this Exchange.
   * The order of the names in this array exactly matches the order
   * of the inputs. That is, entityNames[i] is the entityName
   * associated with the i^th input
   */
  private String[] entityNames;

  /** The degree of parallelism for this Exchange operator */
  private int dop;

  /** The bucket number to assign to a "new" partition */
  private int nextBucket;


  /** 
   * This 2d array contains for each input, its corresponding entity
   * in each of the query plans.
   * So entityContext[i][j], 0 <= i < numInputs and 0 <= j < dop
   * contains the information related to the corresponding entity in 
   * query plan (bucket) "j" for input "i"
   */
  private IServerContext[][] entityContext;

  /**
   * There will be "dop" number of schemas used by this Exchange operator.
   * This is the prefix for each of the schema names
   */
  private String partitionSchemaPrefix;
  
  private boolean isDependentOnPartnStream = false;


  // SETTERS

  /**
   * Setter for evalContext in Exchange
   * 
   * @param evalContext
   *          The evalContext to set.
   */
  public void setEvalContext(IEvalContext evalContext)
  {
    this.evalContext = evalContext;
  }

  public boolean isDependentOnPartnStream() {
    return isDependentOnPartnStream;
  }

  public void setDependentOnPartnStream(boolean isDependentOnPartnStream) {
    this.isDependentOnPartnStream = isDependentOnPartnStream;
  }

  public void setPartitionExprEvals(IAEval[] partnExprsEvals)
  {
    this.partnExprsEvals = partnExprsEvals;
  }

  public void setHashEvals(IHEval[] hashEvals)
  {
    this.hashEvals = hashEvals;
  }

  public void setEntityNames(String[] entityNames)
  {
    this.entityNames = entityNames;
  }

  public void setDOP(int dop)
  {
    this.dop = dop;
  }

  public void setEntityContext(IServerContext[][] entityContext)
  {
    this.entityContext = entityContext;
  }

  public void setPartitionSchemaPrefix(String partitionSchemaPrefix)
  {
    this.partitionSchemaPrefix = partitionSchemaPrefix;
  }


  /**
   * Constructor for Exchange
   * @param ec TODO
   */
  public Exchange(ExecContext ec)
  {
    super(ExecOptType.EXEC_EXCHANGE, new MutableState(ec), ec);
    nextBucket  = 0;
  }

  @Override
  public int run(int timeSlice) throws ExecException
  {
    assert false : "Should come to run(timeSlice) method of Exchange";
    return -1;
  }

  @Override
  public int run(int timeSlice, QueueElement inputElement,
                 QueueReaderContext readerCtx)
    throws CEPException
  {
    IEvalContext evalCtx     = null;
    TupleValue   tupleValue  = inputElement.getTupleValue();
    ITuplePtr    inputTuple  = inputElement.getTuple();
    ITuplePtr    outputTuple = tupleStorageAlloc.allocate();
    Hash         hash        = new Hash(0);
    int          bucket      = Integer.MIN_VALUE;
    int          inputNo     = readerCtx.getInputNo();

    if (LogUtil.isFineEnabled(LoggerType.TRACE))
      LogUtil.fine(LoggerType.TRACE, "prcessing input from input number " 
          + inputNo);

    try
    {
      evalCtx = (IEvalContext) evalContext.clone();
    } 
    catch (CloneNotSupportedException e)
    {
      // This is a programmer's mistake, evaluation contexts used for 
      // operators that support parallelism must be cloneable.
      LogUtil.logStackTrace(LoggerType.TRACE, Level.FINE, e);
      throw new ExecException(ExecutionError.GENERIC_ERROR, e);
    }
    
    evalCtx.bind(inputTuple, IEvalContext.INPUT_ROLE);
    evalCtx.bind(outputTuple, IEvalContext.NEW_OUTPUT_ROLE);

    try 
    {
    	
      // For heartbeats, skip the evaluation of the expression and 
      // simply broadcast it to all the partitions
      if(inputElement.getKind() == Kind.E_HEARTBEAT)
      {
        if (LogUtil.isFineEnabled(LoggerType.TRACE)) 
        {
          LogUtil.fine(LoggerType.TRACE, "Found Heartbeat tuple");
        }
          
        for(int i=0; i<dop; i++)
        {
          bucket = i;
          forwardInputToPartition(tupleValue, entityContext[inputNo][bucket],
                                  partitionSchemaPrefix, bucket, inputNo);
        }
        return 0;
      }

      // Get the partition identifier from thread context if it is set.
      // Please note that partition identifier will be set in thread only when
      // the tuple belongs to a partitioned stream
      // If not set, the partition identifier will have Integer.MIN_VALUE      
      Thread currThread = Thread.currentThread();
      if(currThread instanceof IPartitionAware)
      {
        IPartitionContext context 
          = ((IPartitionAware)currThread).getPartitionContext();
        bucket = context != null ? context.getPartition() : Integer.MIN_VALUE;        
      }

      if(bucket == Integer.MIN_VALUE)
      {
        if(isDependentOnPartnStream)
          bucket = 0;
        else
        {
          // Evalute the partition expression and find its hash
          partnExprsEvals[inputNo].eval(evalCtx);
          hashEvals[inputNo].eval(hash, evalCtx);
  
          // In a serial fashion, determine which bucket (from 0...dop-1)
          // does this tuple map to
          if (LogUtil.isFineEnabled(LoggerType.TRACE))
          {
            LogUtil.fine(LoggerType.TRACE,
                         "Hash value for " + tupleValue + " is " + 
                          hash.getHashValue());
          }
          
          // Compute the bucket for this event
          int val = hash.getHashValue();
          if (val < 0)
            val = val * -1;
          bucket = val % dop;
        }
      }
      
      // Having found the bucket, pass this input to the corresponding
      // entity in the appropriate Query Plan
      forwardInputToPartition(tupleValue, entityContext[inputNo][bucket],
                              partitionSchemaPrefix, bucket, inputNo);
    }
    catch(SoftExecException se)
    {
      tupleStorageAlloc.release(outputTuple);
      return 0;
    }
    return 0;
  }


  @Override
  public void deleteOp()
  {
    String schemaName;
    String oldSchemaName = execContext.getSchemaName();

    try 
    {
      // Drop the schemas created for the partitions
      for(int i=0; i<dop; i++)
      {
        schemaName = partitionSchemaPrefix + i;
        execContext.setSchema(schemaName);
        if (LogUtil.isFineEnabled(LoggerType.TRACE))
        {
          LogUtil.fine(LoggerType.TRACE, "Dropping schema " + schemaName);
        } 
        execContext.dropSchema(schemaName, true);
      }
    }
    catch(Exception e)
    {
      e.printStackTrace();
    }
    finally 
    {
      execContext.setSchema(oldSchemaName);
    }
  }

  private void forwardInputToPartition(TupleValue inputEvent, 
                                       IServerContext serverCtx,
                                       String partitionSchemaPrefix,
                                       int bucket, int inputNo)
    throws CEPException 
  {
    if (LogUtil.isFineEnabled(LoggerType.TRACE)) 
    {     
      String schemaFQN  =
        execContext.getServiceSchema(partitionSchemaPrefix + bucket);

      LogUtil.fine(LoggerType.TRACE,
                   "Routing " + inputEvent + " into bucket " + bucket
                   + " and entity " + schemaFQN + "." +
                   entityNames[inputNo]);
    }
    
    // See bug 13257395
    // Release the global plan manager lock before calling 
    // ExecManager.insertFast
    // This is because between the (ordered) StreamSource execOpTask and
    // the global plan manager lock, the former has to be acquired first, then
    // the latter, else leads to deadlock as in bug mentioned above
    //
    // Treat this fwding of input as follows -
    // The part of the plan with ConcurrentStreamSource and Exchange just
    // determines which partition input event belongs to.
    //
    // Then, it is like a fresh attempt to insert the event into the 
    // appropriate partition
    try {
      execContext.getPlanMgr().getLock().readLock().unlock();
      execContext.getExecMgr().insertFast(inputEvent, serverCtx);
    }
    finally {
      execContext.getPlanMgr().getLock().readLock().lock();
    }
                                        
  }
}
