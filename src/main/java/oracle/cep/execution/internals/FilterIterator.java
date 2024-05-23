/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/internals/FilterIterator.java /main/14 2011/02/07 03:36:25 sborah Exp $ */

/* Copyright (c) 2006, 2011, Oracle and/or its affiliates. 
All rights reserved. */

/*
 DESCRIPTION
 Declares FilterIterator in package oracle.cep.execution.internals.
 
 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>
 
 NOTES
 <other useful comments, qualifications, etc.>
 
 MODIFIED    (MM/DD/YY)
 anasrini  12/19/10 - replace eval() with eval(ec)
 hopark    12/02/08 - move LogLevelManager to ExecContext
 hopark    02/28/08 - resurrect refcnt
 hopark    01/03/08 - remove refcnt
 hopark    12/07/07 - cleanup spill
 hopark    09/07/07 - refactor eval
 hopark    06/11/07 - logging - remove ExecContext
 hopark    05/28/07 - logging support
 najain    04/11/07 - bug fix
 najain    04/03/07 - bug fix
 najain    03/14/07 - cleanup
 najain    03/12/07 - bug fix
 najain    01/05/07 - spill over support
 najain    08/16/06 - concurrency issues
 skaluska  03/01/06 - Creation
 skaluska  03/01/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/internals/FilterIterator.java st_pcbpel_anasrini_eval_parallelism_2/1 2010/12/19 07:35:43 anasrini Exp $
 *  @author  skaluska
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.execution.internals;

import oracle.cep.exceptions.ExecutionError;
import oracle.cep.execution.ExecException;
import oracle.cep.dataStructures.internal.ITuplePtr;
import oracle.cep.logging.ILoggable;
import oracle.cep.memmgr.IAllocator;

/**
 * FilterIterator
 *
 * @author skaluska
 */
public class FilterIterator extends StoreImplIter
{
  /** source */
  private TupleIterator    source;
  private IAllocator<ITuplePtr>       factory;

  /** pred */
  private IBEval            pred;
  /** evalContext */
  private IEvalContext      evalContext;
  /** role for evaluation context */
  private static final int FI_SCAN_ROLE = IEvalContext.SCAN_ROLE;

  /**
   * Constructor for FilterIterator
   * 
   * @param pred
   *          Filter predicate
   * @param evalContext
   *          Evaluation context
   */
  public FilterIterator(IBEval pred, IEvalContext evalContext, 
                        IAllocator<ITuplePtr> factory)
  {
    this.pred = pred;
    this.evalContext = evalContext;
    this.factory = factory;
  }

  /**
   * Initialize
   * 
   * @param src
   *          Tuple iterator source
   */
  public void initialize(ILoggable target, TupleIterator src)
  {
    source = src;
    super.initialize(target);
  }

  /*
   * (non-Javadoc)
   * 
   * @see oracle.cep.execution.internals.TupleIterator#getNext()
   */
  public ITuplePtr getNext() throws ExecException
  {
    if (!isInitialized())
      throw new ExecException(ExecutionError.ITERATOR_UNINIT);
    
    ITuplePtr tuple;
    while ((tuple = source.getNext()) != null) 
    {
      evalContext.bind(tuple, FI_SCAN_ROLE);
  
      if (pred.eval(evalContext))
        break;
      else
      {
        factory.release(tuple);
      }
   }

    return tuple;
  }

}
