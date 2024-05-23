/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/operators/ExecSourceOpt.java /main/3 2011/02/24 08:23:34 alealves Exp $ */

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
    sbishnoi    05/20/10 - adding setHbtTimeOut as an abstract method
    sbishnoi    05/12/09 - making this as an abstract class
    anasrini    05/08/09 - Interface for an execution source operator
    anasrini    05/08/09 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/operators/ExecSourceOpt.java st_pcbpel_alealves_9261513/2 2010/07/08 16:53:11 alealves Exp $
 *  @author  anasrini
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.execution.operators;

import oracle.cep.dataStructures.external.TupleValue;
import oracle.cep.dataStructures.internal.memory.Tuple;
import oracle.cep.exceptions.CEPException;
import oracle.cep.interfaces.input.TableSource;
import oracle.cep.service.ExecContext;

/**
 * This is an abstract class for the (currently 2) base execution source operators
 * 
 * @author anasrini
 */
public abstract class ExecSourceOpt extends ExecOpt
{

  private long             minTupleId;

  private boolean          restoreTupleId;
  
  ExecSourceOpt(ExecOptType typ, MutableState m, ExecContext ec)
  {
    super(typ,m,ec);
  }
  
  /**
   * Get the tableSource interface object associated with this source operator
   * @return the associated tableSource interface object 
   */
  public TableSource getSource()
  {
    assert false;
    return null;
  }

  /**
   * @return true iff heartbeat timeouts have been requested for this
   *              source operator
   */
  public boolean requiresHbtTimeout()
  {
    assert false;
    return false;
  }

  /**
   * Set the heartbeat timeout duration for this operator
   */  
  public abstract void setTimeoutDuration(long duration);
  
  /**
   * Executes source operator passinga along input tuple value.
   * 
   * @param input
   * @return
   * @throws CEPException
   */
  abstract public int run(TupleValue input) throws CEPException;
  
  public void setMinTupleId(long minTupleId)
  {
    this.minTupleId = minTupleId;
  }
  
  public boolean isRestoreTupleId()
  {
    return restoreTupleId;
  }

  public void setRestoreTupleId(boolean restoreTupleId)
  {
    this.restoreTupleId = restoreTupleId;
  }

  public long getMinTupleId()
  {
    return minTupleId;
  }
  
  public void restoreTupleId()
  {
    // Once Restored, It should be set to false so that on every execution
    // sequence of tuple id doesn't get reset to an earlier value.
    // Restore is only required after recovering from snapshot
    setRestoreTupleId(false);
    if(Tuple.getNextTupleId() < minTupleId)
     Tuple.setNextTupleId(minTupleId);
  }
}
