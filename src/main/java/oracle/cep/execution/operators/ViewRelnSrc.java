/* $Header: pcbpel/cep/server/src/oracle/cep/execution/operators/ViewRelnSrc.java /main/22 2008/10/24 15:50:18 hopark Exp $ */

/* Copyright (c) 2006, 2008, Oracle and/or its affiliates. All rights reserved.*/

/*
 DESCRIPTION
 <short description of component this file declares/defines>

 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>

 NOTES
 <other useful comments, qualifications, etc.>

 MODIFIED    (MM/DD/YY)
 hopark      10/10/08 - remove statics
 hopark      10/09/08 - remove statics
 hopark      12/07/07 - cleanup spill
 hopark      10/30/07 - remove IQueueElement
 hopark      10/22/07 - remove TimeStamp
 parujain    10/04/07 - delete op
 hopark      07/13/07 - dump stack trace on exception
 parujain    07/03/07 - cleanup
 parujain    06/26/07 - mutable state
 hopark      05/24/07 - debug logging
 hopark      05/16/07 - add arguments for OutOfOrderException
 parujain    05/08/07 - monitoring statistics
 najain      04/12/07 - bug fix
 hopark      04/06/07 - fix pincoun
 hopark      03/21/07 - add TuplePtr pin
 najain      03/14/07 - cleanup
 najain      03/12/07 - bug fix
 najain      02/15/07 - bug fix
 najain      01/05/07 - spill over support
 parujain    12/19/06 - fullScanId for RelationSynopsis
 parujain    12/07/06 - propagating relation
 najain      11/10/06 - bug fix
 anasrini    09/21/06 - for temp debugging
 najain      08/02/06 - refCounting optimizations
 najain      08/10/06 - add asserts
 najain      08/02/06 - use underlying store
 anasrini    08/02/06 - set lastInputTs
 najain      07/19/06 - ref-count tuples 
 najain      07/13/06 - ref-count timestamps 
 najain      07/12/06 - ref-count elements 
 najain      06/16/06 - bug fix 
 najain      06/13/06 - bug fix 
 najain      06/12/06 - bug fix 
 najain      06/08/06 - query addition re-entrant 
 najain      06/05/06 - add full scan 
 najain      05/24/06 - bug fix 
 najain      05/22/06 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/execution/operators/ViewRelnSrc.java /main/22 2008/10/24 15:50:18 hopark Exp $
 *  @author  najain  
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.execution.operators;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.SimpleDateFormat;
import java.util.HashSet;
import java.util.logging.Level;

import oracle.cep.exceptions.CEPException;
import oracle.cep.exceptions.ExecutionError;
import oracle.cep.execution.ExecException;
import oracle.cep.execution.SoftExecException;
import oracle.cep.dataStructures.internal.QueueElement;
import oracle.cep.execution.snapshot.IPersistenceContext;
import oracle.cep.execution.snapshot.SynopsisPersistenceContext;
import oracle.cep.execution.synopses.ExecSynopsis;
import oracle.cep.execution.synopses.RelationSynopsis;
import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;
import oracle.cep.service.ExecContext;

/**
 * RelSource is the execution operator that reads input tuples for registered
 * relations
 * 
 * @author najain
 */
public class ViewRelnSrc extends ExecOpt
{
  private SimpleDateFormat sdf;

  /** relation id. */
  int                      relnId;

  /** Synopsis storing the output */
  private RelationSynopsis synopsis;

  /** Full Scan identifier */
  private int              fullScanId;

  /**
   * @return Returns the synopsis.
   */
  public RelationSynopsis getSynopsis()
  {
    return synopsis;
  }

  /**
   * @param synopsis
   *          The synopsis to set.
   */
  public void setSynopsis(RelationSynopsis synopsis)
  {
    this.synopsis = synopsis;
  }

  /**
   * @param relnId
   *          The relnId to set.
   */
  public void setRelnId(int relnId)
  {
    this.relnId = relnId;
  }

  /**
   * @return Returns the fullScanId.
   */
  public int getFullScanId()
  {
    return fullScanId;
  }

  /**
   * @param fullScanId
   *          The fullScanId to set.
   */
  public void setFullScanId(int fullScanId)
  {
    this.fullScanId = fullScanId;
    this.propScanId = fullScanId;
  }

  /**
   * Constructor for RelSource
   * @param ec TODO
   */
  public ViewRelnSrc(ExecContext ec)
  {
    super(ExecOptType.EXEC_VIEW_RELN_SRC, new ViewRelnSrcState(ec), ec);

    sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
    sdf.setLenient(false);
  }

  /*
   * (non-Javadoc)
   * 
   * @see oracle.cep.execution.operators.ExecOpt#run(int)
   */
  @Override
  public int run(int timeSlice) throws CEPException
  {
    int numElements;
    boolean done = false;
    ViewRelnSrcState s = (ViewRelnSrcState) mut_state;
    boolean exitState = true;

    assert s.state != ExecState.S_UNINIT;

    // Stats
    s.stats.incrNumExecutions();

    try
    {
      numElements = timeSlice;
      while ((s.stats.getNumInputs() < numElements) || (!exitState))
      {
        switch (s.state)
        {
          case S_PROPAGATE_OLD_DATA:
            setExecSynopsis((ExecSynopsis) synopsis);
            handlePropOldData();
            break;

          case S_INIT:
            // Get next input element
            s.inputElement = inputQueue.dequeue(s.inputElementBuf);
            s.state = ExecState.S_INPUT_DEQUEUED;
          case S_INPUT_DEQUEUED:
            if (s.inputElement == null)
            {
              // we might still need to output a heartbeat
              if (s.lastInputTs <= s.lastOutputTs)
              {
                s.state = ExecState.S_INIT;
                done = true;
                break;
              }

              // Output a heartbeat
              s.outputKind = QueueElement.Kind.E_HEARTBEAT;
              s.state = ExecState.S_ALLOCATE_ELEM;
              break;
            }
            else
            {
              // Bump up our counts
	      exitState = false;
             if(s.inputElement.getKind() == QueueElement.Kind.E_HEARTBEAT)
                s.stats.incrNumInputHeartbeats();
             else
                s.stats.incrNumInputs();

              // Update input ts
              s.inputTs = s.inputElement.getTs();

              // // FOR DEBUGGING - TEMPORARY
              /*
               * LogUtil.fine(LoggerType.TRACE, optName + "," + sdf.format(new
               * Timestamp(s.inputTs.getValue())) + "," +
               * s.inputElement.getKind());
               */

              s.inputTuple = s.inputElement.getTuple();

              // We should have a progress of time.
              if (s.lastInputTs > s.inputTs)
              {
                throw ExecException.OutOfOrderException(
                        this,
                        s.lastInputTs, 
                        s.inputTs, 
                        s.inputElement.toString());
              }

              s.lastInputTs = s.inputTs;
              // Ignore heartbeats
              if (s.inputElement.getKind() == QueueElement.Kind.E_HEARTBEAT)
              {
                s.state = ExecState.S_INPUT_ELEM_CONSUMED;
                break;
              }
            }
            s.state = ExecState.S_PROCESSING1;
          case S_PROCESSING1:
            assert s.inputTuple != null;
            if (s.inputElement.getKind() == QueueElement.Kind.E_PLUS)
            {
              s.state = ExecState.S_PROCESS_PLUS;
              s.outputKind = QueueElement.Kind.E_PLUS;
              break;
            }
            else
            {
              assert (s.inputElement.getKind() == QueueElement.Kind.E_MINUS);
              s.outputKind = QueueElement.Kind.E_MINUS;
              s.state = ExecState.S_PROCESS_MINUS;
              break;
            }

          case S_PROCESS_PLUS:
            synopsis.insertTuple(s.inputTuple);
            s.state = ExecState.S_OUTPUT_READY;
            break;

          case S_PROCESS_MINUS:
            synopsis.deleteTuple(s.inputTuple);
            s.state = ExecState.S_OUTPUT_READY;
            break;

          case S_ALLOCATE_ELEM:
            assert s.inputElement == null;
            s.state = ExecState.S_OUTPUT_TIMESTAMP;
          case S_OUTPUT_TIMESTAMP:
          {
            s.state = ExecState.S_OUTPUT_READY;
          }
          case S_OUTPUT_READY:
            if (s.outputKind == QueueElement.Kind.E_HEARTBEAT)
            {
              s.lastOutputTs = s.lastInputTs;
              s.inputElement.heartBeat(s.lastInputTs);
            }
            else
              s.lastOutputTs = s.inputTs;
            s.state = ExecState.S_OUTPUT_ELEMENT;

          case S_OUTPUT_ELEMENT:
            if (outputQueue.isFull())
            {
              done = true;
              break;
            }
            outputQueue.enqueue(s.inputElement);
            s.state = ExecState.S_OUTPUT_ENQUEUED;

          case S_OUTPUT_ENQUEUED:
            if(s.inputElement.getKind() == QueueElement.Kind.E_HEARTBEAT)
              s.stats.incrNumOutputHeartbeats();
            else
              s.stats.incrNumOutputs();
	    exitState = true;
	    s.state = ExecState.S_INIT;
	    break;

  	  case S_INPUT_ELEM_CONSUMED:
	    assert s.inputElement.getKind() == QueueElement.Kind.E_HEARTBEAT;
	    //assert s.inputElement.getTuple() == null; getTuple will pin the tuple.

	    exitState = true;
	    s.state = ExecState.S_INIT;
	    break;

          default:
            assert false;
        }
        if (done)
          break;
      }
    }
    catch (SoftExecException e)
    {
      // Ignore it for now
      LogUtil.logStackTrace(LoggerType.TRACE, Level.SEVERE, e);
      return 0;
    }
    
    return 0;
  }

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
   * Create snapshot of View Relation Source (ViewRelnSrc) operator by writing the operator state into output stream.
   * State of ViewRelnSrc operator consists of following:
   * 1. Mutable State (RelSourceState)
   * 2. synopsis
   * 
   * Please note that we will write the state of operator in above sequence, so
   * the loadSnapshot should also read the state in the same sequence.
   * @param output
   * @throws CEPException 
   */
  @Override
  protected void createSnapshot(ObjectOutputStream output) throws CEPException
  {
    try
    {
      //snapshot mutable state
      output.writeObject((ViewRelnSrcState)mut_state);
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
      ViewRelnSrcState mutable_state = (ViewRelnSrcState)input.readObject();
      ((ViewRelnSrcState)mut_state).copyFrom(mutable_state);
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
