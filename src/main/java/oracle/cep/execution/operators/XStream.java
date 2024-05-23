/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/operators/XStream.java /main/25 2011/02/07 03:36:26 sborah Exp $ */

/* Copyright (c) 2006, 2011, Oracle and/or its affiliates. 
All rights reserved. */

/*
 DESCRIPTION
 Abstract class for DStream and IStream 

 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>

 NOTES
 <other useful comments, qualifications, etc.>

 MODIFIED    (MM/DD/YY)
 anasrini    12/19/10 - replace eval() with eval(ec)
 parujain    04/06/09 - total ordering
 parujain    04/03/09 - ordering fix
 hopark      10/09/08 - remove statics
 najain      04/04/08 - silent reln support
 hopark      02/28/08 - resurrect refcnt
 hopark      12/07/07 - cleanup spill
 hopark      10/30/07 - remove IQueueElement
 hopark      10/22/07 - remove TimeStamp
 parujain    10/04/07 - delete op
 hopark      09/07/07 - eval refactor
 parujain    06/26/07 - mutable state
 hopark      06/11/07 - logging - remove ExecContext
 hopark      05/28/07 - logging support
 hopark      04/20/07 - change pinTuple semantics
 hopark      04/05/07 - memmgr reorg
 hopark      03/21/07 - add TuplePtr pin
 najain      03/16/07 - cleanup
 najain      03/14/07 - cleanup
 najain      03/12/07 - bug fix
 najain      01/05/07 - spill over support
 parujain    12/19/06 - fullScanId for RelationSynopsis
 najain      12/04/06 - stores are not storage allocators
 najain      11/14/06 - set synStoreFactory
 najain      08/25/06 - bug fix 
 najain      07/19/06 - ref-count tuples 
 najain      07/06/06 - cleanup
 ayalaman    05/09/06 - fix javadoc errors 
 ayalaman    04/28/06 - create new tuple 
 ayalaman    04/26/06 - Abstract class for DStream and IStream 
 ayalaman    04/26/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/operators/XStream.java st_pcbpel_anasrini_eval_parallelism_2/1 2010/12/19 07:35:42 anasrini Exp $
 *  @author  ayalaman
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.execution.operators;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.logging.Level;

import oracle.cep.exceptions.CEPException;
import oracle.cep.exceptions.ExecutionError;
import oracle.cep.execution.ExecException;
import oracle.cep.execution.internals.IAEval;
import oracle.cep.execution.internals.IBEval;
import oracle.cep.execution.internals.IEvalContext;
import oracle.cep.execution.internals.TupleIterator;
import oracle.cep.dataStructures.internal.QueueElement;
import oracle.cep.execution.snapshot.IPersistenceContext;
import oracle.cep.execution.snapshot.SynopsisPersistenceContext;
import oracle.cep.execution.synopses.RelationSynopsis;
import oracle.cep.dataStructures.internal.ITuplePtr;
import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;
import oracle.cep.memmgr.IAllocator;
import oracle.cep.service.ExecContext;

/**
 * XStream - abstract class for common method used by both DStream and IStream
 *
 * @author ayalaman
 */
public abstract class XStream extends ExecOpt
{
  /**
   * Synopsis storing the tuples that appeared in the current time instant and a
   * count for each tuple. The count for a tuple is the number of copies of the
   * tuple that appears in the PLUS tuples minus the number of copies of the
   * tuple in the MINUS tuples.
   */
  protected RelationSynopsis synopsis;

  /** scan id to get a tuple and its count from the synopsis */
  protected int              countScanId;

  /** scan to get all the tuples in a synopsys */
  protected int              fullScanId;

  /** Storage allocator for the tuples in the synopsis */
  protected IAllocator<ITuplePtr>     synStoreTupleFactory;

  /** evaluation context in which all computations are performed */
  protected IEvalContext      evalContext;

  /** Evaluator to increment the count for a tuple */
  protected IAEval            incrEval;

  /** Evaluator to decrement the count for a tuple */
  protected IAEval            decrEval;

  /** Evaluator to initialize the count to 0 */
  protected IAEval            initEval;

  /** Evaluator to check if the counts for a tuple is 0 */
  protected IBEval            zeroEval;

  /** Evaluator to produce the output tuple */
  protected IAEval            outEval;

  /* does the operator only depend on silent Relations */
  boolean                    silentRelns;

  // The list of silent Relations that the operator depends on: This is needed
  // to propagate the heartbeat in case of a stall or a silent relation.
  // Currently, silent streams/relations are not handled, only static relations
  // (one for which the time is not specifed) and handled appropriately.
  LinkedList<RelSource>      silentRelnsList;

  public void setSilentRelns(boolean silentRelns) 
  {
    this.silentRelns = silentRelns;
  }

  public void addInputRelns(RelSource execOp)
  {
    if (silentRelnsList == null)
      silentRelnsList= new LinkedList<RelSource>();

    silentRelnsList.add(execOp);
  }
  
  /**
   * Dummy constructor - never instantiated
   * @param ec TODO
   */
  public XStream(ExecContext ec)
  {
    super(null, null, ec);
  }

  /**
   * Constructor
   * @param ec TODO
   */
  public XStream(ExecOptType typ, MutableState m, ExecContext ec)
  {
    super(typ, m, ec);
  }

  /**
   * Getter for synopsis in IStream and DStream
   * 
   * @return Returns the synopsis
   */
  public RelationSynopsis getSynopsis()
  {
    return synopsis;
  }

  /**
   * Setter for synopsis in IStream and DStream
   * 
   * @param synopsis
   *          The outSynopsis to set.
   */
  public void setSynopsis(RelationSynopsis synopsis)
  {
    this.synopsis = synopsis;
  }

  /**
   * Getter for the count of a tuple in synopsis
   * 
   * @return the count scan
   */
  public int getCountScan()
  {
    return this.countScanId;
  }

  /**
   * Setter for the count scan in synopsis
   * 
   * @param countScan
   *          the count scan value to set
   */
  public void setCountScan(int countScan)
  {
    this.countScanId = countScan;
  }

  /**
   * Getter for the full scan status of the synopsis
   * 
   * @return the count scan
   */
  public int getFullScan()
  {
    return this.fullScanId;
  }

  /**
   * Setter for the full scan in synopsis
   * 
   * @param fullScan
   *          the count scan value to set
   */
  public void setFullScan(int fullScan)
  {
    this.fullScanId = fullScan;
    this.propScanId = fullScan;
  }

  /**
   * Setter for the synopsis storage tuple allocator
   * 
   * @param store
   *          the storage allocator instance
   */
  public void setSynStoreTupleFactory(IAllocator<ITuplePtr> factory)
  {
    this.synStoreTupleFactory = factory;
  }

  /**
   * Getter for evalContext in IStream and DStream
   * 
   * @return Returns the evalContext
   */
  public IEvalContext getEvalContext()
  {
    return evalContext;
  }

  /**
   * Setter for evalContext in IStream and DStream
   * 
   * @param evalContext
   *          The evalContext to set.
   */
  public void setEvalContext(IEvalContext evalContext)
  {
    this.evalContext = evalContext;
  }

  /**
   * Getter for the tuple count initilization evaluator
   * 
   * @return the evaluator instance
   */
  public IAEval getInitEval()
  {
    return this.initEval;
  }

  /**
   * Setter for the tuple count initialization evaluator
   * 
   * @param initEval
   *          the evaluator instance
   */
  public void setInitEval(IAEval initEval)
  {
    this.initEval = initEval;
  }

  /**
   * Getter for the tuple count increment evaluator
   * 
   * @return the evaluator instance
   */
  public IAEval getIncrEval()
  {
    return this.incrEval;
  }

  /**
   * Setter for the tuple count increment evaluator
   * 
   * @param incrEval
   *          the evaluator instance
   */
  public void setIncrEval(IAEval incrEval)
  {
    this.incrEval = incrEval;
  }

  /**
   * Getter for the tuple count decrement evaluator
   * 
   * @return the evaluator instance
   */
  public IAEval getDecrEval()
  {
    return this.decrEval;
  }

  /**
   * Setter for the tuple count decrement evaluator
   * 
   * @param decrEval
   *          the evaluator instance
   */
  public void setDecrEval(IAEval decrEval)
  {
    this.decrEval = decrEval;
  }

  /**
   * Getter for the zero tuple count evaluator
   * 
   * @return the evaluator instance
   */
  public IBEval getZeroEval()
  {
    return this.zeroEval;
  }

  /**
   * Setter for the zero tuple count evaluator
   * 
   * @param zeroEval
   *          the evaluator instance
   */
  public void setZeroEval(IBEval zeroEval)
  {
    this.zeroEval = zeroEval;
  }

  /**
   * Getter for the Evaluator that produces the output
   * 
   * @return the evaluator instance
   */
  public IAEval getOutEval()
  {
    return this.outEval;
  }

  /**
   * Setter for the Evaluator that produces the output
   * 
   * @param outEval
   *          the evaluator instance
   */
  public void setOutEval(IAEval outEval)
  {
    this.outEval = outEval;
  }

  /*
   * (non-Javadoc)
   * 
   * @see oracle.cep.execution.operators.ExecOpt#run(int)
   */
  @Override
  abstract public int run(int timeSlice) throws ExecException;

  /*
   * (non-Javadoc)
   * 
   * @see oracle.cep.execution.operators.ExecOpt#deleteOp()
   */
  @Override
  public void deleteOp()
  {
    // TODO Auto-generated method stub

  }

  /**
   * Handle the addition (PLUS) of a tuple
   * 
   * @param inputElem
   *          input element
   */
  protected void handlePlus(QueueElement inputElem)
      throws ExecException
  {
    ITuplePtr inTuple;  
    ITuplePtr synTuple;
    TupleIterator tupIter;

    inTuple = inputElem.getTuple();
    evalContext.bind(inTuple, IEvalContext.INPUT_ROLE);

    // if we have seen a copy of the input tuple before, we add 1 to the
    // count corresponding to inTuple. Otherwise we initialize the count
    // corresponding to inTuple to 1.

    // scan the synopsys
    tupIter = synopsis.getScan(countScanId);
    synTuple = tupIter.getNext();
    if (synTuple != null) // has to be only one if any
    {
      evalContext.bind(synTuple, IEvalContext.SYN_ROLE);

      // count at this time is non-zero; negative numbers are possible.

      incrEval.eval(evalContext);

      // release the iterator before modifying the tuple linked list
      synopsis.releaseScan(countScanId, tupIter);

      // first insert tuple after a delete tuple
      boolean b = zeroEval.eval(evalContext);
      if (b) {
        synopsis.deleteTuple(synTuple);
        synStoreTupleFactory.release(synTuple);
        synStoreTupleFactory.release(synTuple);
      }
    }
    else
    // we have not seen this IN tuple before
    {
      // release the iterator before modifying the tuple linked list
      synopsis.releaseScan(countScanId, tupIter);

      // create a new countTuple
      // TODO: If this is always a RelStoreImpl, make it of that type
      synTuple = synStoreTupleFactory.allocate();

      // initialize the count to zero
      evalContext.bind(synTuple, IEvalContext.SYN_ROLE);

      initEval.eval(evalContext);
      incrEval.eval(evalContext);

      // insert the tuple into synopsis
      synopsis.insertTuple(synTuple);
    }
  }

  /**
   * Handle the deletion (MINUS) of a tuple
   * 
   * @param inputElem
   *          input element
   */
  protected void handleMinus(QueueElement inputElem)
      throws ExecException
  {
    ITuplePtr inTuple;  
    ITuplePtr synTuple;
    TupleIterator tupIter;

    inTuple = inputElem.getTuple();
    evalContext.bind(inTuple, IEvalContext.INPUT_ROLE);

    // if we have seen a copy of the inTuple before, we subtract 1 from the
    // count corresponding to inTuple. Otherwise, we initialize the count
    // corresponding to inTuple to -1

    // scan
    tupIter = synopsis.getScan(countScanId);

    synTuple = tupIter.getNext();
    if (synTuple != null)
    {
      evalContext.bind(synTuple, IEvalContext.SYN_ROLE);

      decrEval.eval(evalContext);

      synopsis.releaseScan(countScanId, tupIter);

      boolean b = zeroEval.eval(evalContext);
      if (b)
      {
        synopsis.deleteTuple(synTuple);
        synStoreTupleFactory.release(synTuple);
        synStoreTupleFactory.release(synTuple);
      } 
    }
    else
    // we have not seen this IN Tuple before
    {
      synopsis.releaseScan(countScanId, tupIter);

      // create a new countTuple
      // TODO: If this is always a RelStoreImpl, make it of that type
      synTuple = synStoreTupleFactory.allocate();

      // initialize the count to zero
      evalContext.bind(synTuple, IEvalContext.SYN_ROLE);

      initEval.eval(evalContext);
      decrEval.eval(evalContext);

      // insert the tuple into synopsis
      synopsis.insertTuple(synTuple);
    }
  }
  
  /**
   * Create snapshot of IStream operator by writing the IStream operator state.
   */
  @Override
  protected void createSnapshot(ObjectOutputStream output) throws CEPException
  {
    try
    {
      //snapshot mutable state
      output.writeObject((XStreamState)mut_state);
      //snapshot synopsis.
      synopsis.writeExternal(output, new SynopsisPersistenceContext(fullScanId));
    } catch (IOException e)
    {
      LogUtil.logStackTrace(LoggerType.TRACE, Level.FINE,e);
      throw new CEPException(ExecutionError.SNAPSHOT_CREATION_ERROR, e, e.getLocalizedMessage(), this.getOptName());
    }
  }
  
  @Override
  protected void loadSnapshot(ObjectInputStream input) throws ExecException
  {
    try
    {
      //read mutable state
      XStreamState mutable_state = (XStreamState)input.readObject();
      ((XStreamState)mut_state).copyFrom(mutable_state);
      //read synopsis
      IPersistenceContext persistenceContext = new SynopsisPersistenceContext();
      persistenceContext.setCache(new HashSet());
      synopsis.readExternal(input,persistenceContext);
    } catch (ClassNotFoundException | IOException e)
    {
      LogUtil.logStackTrace(LoggerType.TRACE, Level.FINE,e);
      throw new ExecException(ExecutionError.SNAPSHOT_LOAD_ERROR, e, e.getLocalizedMessage(), getOptName());
    }
  }
}
