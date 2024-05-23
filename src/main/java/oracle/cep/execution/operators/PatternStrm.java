/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/operators/PatternStrm.java /main/29 2011/02/07 03:36:26 sborah Exp $ */

/* Copyright (c) 2007, 2011, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    anasrini    12/19/10 - replace eval() with eval(ec)
    hopark      10/10/08 - remove statics
    hopark      10/09/08 - remove statics
    sbishnoi    06/29/08 - override isHeartbeatPending
    hopark      02/28/08 - resurrect refcnt
    rkomurav    02/01/08 - remove 0 as hard coded state number for 
                           undefined state. use a DFA constant instead
    rkomurav    01/24/08 - add undefined statein DFA
    hopark      12/07/07 - cleanup spill
    hopark      10/30/07 - remove IQueueElement
    hopark      10/21/07 - remve TimeStamp
    rkomurav    09/28/07 - support non mandatory correlation defs
    hopark      09/07/07 - eval refactor
    rkomurav    07/16/07 - uda
    hopark      07/13/07 - dump stack trace on exception
    parujain    07/03/07 - cleanup
    rkomurav    06/27/07 - fix boundary case in handling unsure match
    parujain    06/26/07 - mutable state
    hopark      06/20/07 - cleanup
    rkomurav    06/14/07 - fix transition to unsure state
    rkomurav    06/07/07 - remove unnecessary null checks
    rkomurav    06/06/07 - remove isSimple
    rkomurav    05/15/07 - cleanup
    hopark      05/24/07 - debug logging
    rkomurav    05/29/07 - aggr support
    hopark      05/16/07 - add arguments for OutOfOrderException
    parujain    05/08/07 - monitoring statistics
    rkomurav    04/10/07 - pin and ref counting problem
    rkomurav    04/02/07 - fix output timestamp
    najain      03/29/07 - cleanup
    najain      03/14/07 - cleanup
    rkomurav    03/13/07 - creation
    najain      03/12/07 - bug fix
    rkomurav    03/02/07 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/operators/PatternStrm.java st_pcbpel_anasrini_eval_parallelism_2/1 2010/12/19 07:35:40 anasrini Exp $
 *  @author  rkomurav
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.execution.operators;

import java.util.logging.Level;

import oracle.cep.dataStructures.internal.QueueElement;
import oracle.cep.exceptions.CEPException;
import oracle.cep.execution.ExecException;
import oracle.cep.execution.SoftExecException;
import oracle.cep.execution.internals.IAEval;
import oracle.cep.execution.internals.IBEval;
import oracle.cep.execution.internals.IEvalContext;
import oracle.cep.execution.pattern.Binding;
import oracle.cep.execution.synopses.ExecSynopsis;
import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;
import oracle.cep.memmgr.IAllocator;
import oracle.cep.dataStructures.internal.ITuplePtr;
import oracle.cep.service.ExecContext;
import oracle.cep.util.DFA;

public class PatternStrm extends ExecOpt
{
  /** Length of each binding - number of singleton correlation names */
  int         bindLength;
  
  /** DFA */
  DFA         dfa;
  
  /** Eval context */
  IEvalContext evalContext;
  
  /** Correlation definitions */
  IBEval[]     defs;
  
  /** map from correlation name to binding position in a binding tuple array */
  int[]       map;
  
  /** Starting Role number for binding role */
  int         bindRole;
  
  /** Copy eval frombinding role to output role */
  IAEval       copyEval;
  
  /** Null Input Tuple */
  ITuplePtr   nullInputTuple;
  
  /** Init evals for aggrs */
  IAEval[]     initEvals;
  
  /** Incr evals for aggrs */
  IAEval[]     incrEvals;
  
  /** Null eval for aggrs */
  IAEval       nullEval;
  
  /** alphabet index to State number Map */
  int[]       alphToStateMap;
  
  /** Boolean array to maintain if a particular alphabet is associated 
   * with any aggrs either in measures exprs or correlation defs */
  boolean[]   hasAggrs;
  
  /** aggregation tuple factory 
   *  if no aggregates are referenced, aggrTupleFactory is null*/
  IAllocator<ITuplePtr>  aggrTupleFactory;
  
  /** copy eval for aggr tuple from one binding to another */
  IAEval       aggrCopyEval;
  
  /** eval context for aggrCopyEval */
  IEvalContext aggrCopyEvalContext;
  
  /** boolean flag representing if aggregates are present */
  boolean     aggrsPresent;
  
  IAEval       releaseEval;
  
  
  /**
   * @param bindLength the bindLength to set
   */
  public void setBindLength(int bindLength)
  {
    this.bindLength = bindLength;
  }

  public void setAggrCopyEval(IAEval aggrCopyEval)
  {
    this.aggrCopyEval = aggrCopyEval;
  }

  public void setAggrCopyEvalContext(IEvalContext aggrCopyEvalContext)
  {
    this.aggrCopyEvalContext = aggrCopyEvalContext;
  }

  public void setAggrTupleFactory(IAllocator<ITuplePtr> aggrTupleFactory)
  {
    this.aggrTupleFactory = aggrTupleFactory;
  }

  public void setHasAggrs(boolean[] hasAggrs)
  {
    this.hasAggrs = hasAggrs;
  }

  /**
   * @param defs the defs to set
   */
  public void setDefs(IBEval[] defs)
  {
    this.defs = defs;
  }

  /**
   * @param dfa the dfa to set
   */
  public void setDfa(DFA dfa)
  {
    this.dfa = dfa;
  }

  /**
   * @param evalContext the evalContext to set
   */
  public void setEvalContext(IEvalContext evalContext)
  {
    this.evalContext = evalContext;
  }

  /**
   * @param map the map to set
   */
  public void setMap(int[] map)
  {
    this.map = map;
  }

  /**
   * @param bindRole the bindRole to set
   */
  public void setBindRole(int bindRole) 
  {
    this.bindRole = bindRole;
  }

  /**
   * @param copyEval the copyEval to set
   */
  public void setCopyEval(IAEval copyEval)
  {
    this.copyEval = copyEval;
  }

  /**
   * @param nullTuple the nullTuple to set
   */
  public void setNullInputTuple(ITuplePtr nullInputTuple)
  {
    this.nullInputTuple = nullInputTuple;
  }

  public void setIncrEvals(IAEval[] incrEvals)
  {
    this.incrEvals = incrEvals;
  }

  public void setInitEvals(IAEval[] initEvals)
  {
    this.initEvals = initEvals;
  }

  public void setNullEval(IAEval nullEval)
  {
    this.nullEval = nullEval;
  }
  
  public void setReleaseEval(IAEval releaseEval)
  {
    this.releaseEval = releaseEval;
  }

  public void setAlphToStateMap(int[] alphToStateMap)
  {
    this.alphToStateMap = alphToStateMap;
  }

  public PatternStrm(ExecContext ec)
  {
    super(ExecOptType.EXEC_PATTERN_STRM, new PatternStrmState(ec), ec);
  }
  
  public void initialize() throws ExecException
  {
    assert mut_state instanceof PatternStrmState;
    PatternStrmState s = (PatternStrmState)mut_state;
    s.initialize(dfa.getNumStates(), bindLength, dfa.getNumAlphabets(),
        nullInputTuple, aggrTupleFactory);
    
    if(aggrTupleFactory != null)
      aggrsPresent = true;
    else
      aggrsPresent = false;
  }
  
  /**
   * Returns if the current binding is growable further or not
   * Growable means if on further any input, if this binding develops to
   * a final state binding
   * @param binding position
   */
  private boolean isGrowable(int pos)
  {
    int n = dfa.getNumAlphabets();
    for(int i = 0; i < n; i++)
    {
      //assumption here that a final state either transits to another
      //final state or undefined state
      if(dfa.next(pos, i) != DFA.UNDEFINED_STATE)
        return true;
    }
    return false;
  }
  
  
  /**
   * Returns the minimum of startindices among activestates
   * excluding the given index
   * returns Long.MAX_VALUE if no minimum found(this case arises if the
   * only binding present is the current final state which is being excluded)
   * @param states
   * @param exclude
   * @return
   */
  public long minStartIndex(Binding[] states, boolean[] activeStates, int exclude)
  {
    int len = states.length;
    int i;
    
    if(len == 2 && exclude == 1)
      return Long.MAX_VALUE;
    
    long min = Long.MAX_VALUE;
    //don't check for state 0
    for(i = 1; i < exclude; i++)
    {
      if(activeStates[i])
        min = (states[i].getStartIndex() < min) ? 
            states[i].getStartIndex() : min;
    }
    for(i = exclude + 1; i < len; i++)
    {
      if(activeStates[i])
        min = (states[i].getStartIndex() < min) ? 
            states[i].getStartIndex() : min;
    }
    return min;
  }
  
  private void  applyInitIncrEvals(int currState, int nextState, int evalIndex, Binding binding)
    throws ExecException
  {
    ITuplePtr[] elems = binding.getElems();
    for(int n = 0; n < elems.length; n++)
    {
      evalContext.bind(elems[n], bindRole + n);
    }
    
    //no change in state
    if(nextState == currState)
      incrEvals[evalIndex].eval(evalContext);
    else
      initEvals[evalIndex].eval(evalContext);
  }
  
  private void  applyNullEvals(Binding binding) throws ExecException
  {
    ITuplePtr[] elems = binding.getElems();
    int index = elems.length - 1;
    //Bind the aggregate tuple to the eval context
    evalContext.bind(elems[index], bindRole + index);

    nullEval.eval(evalContext);
  }
  
  private void copyAggrTuple(Binding src, Binding dest) throws ExecException
  {
    //position of aggr tuple inside a binding
    int pos = src.getElems().length - 1;
    aggrCopyEvalContext.bind(src.getTuple(pos), IEvalContext.INPUT_ROLE);
    aggrCopyEvalContext.bind(dest.getTuple(pos), IEvalContext.NEW_OUTPUT_ROLE);
    aggrCopyEval.eval(aggrCopyEvalContext);
  }
  
  private void applyTransition(int curState, int input, PatternStrmState s)
    throws ExecException
  {
    //next transition step
    int step;
    //new sequence value
    long seq;
    
    step = dfa.next(curState, input);
    
    //if transition not possible
    if(step == DFA.UNDEFINED_STATE)
      return;

    //if transitioning from state 0
    if(curState == 0)
      seq = s.sequence;
    else
      seq = s.states[curState].getStartIndex();

    //if activeStates[i] is false
    //it implies that state is not yet active for the next run
    //else it has to be updated after applying preferment rules
    if(s.activeStates[step])
    {
      /* preferment rules:
       * 1) prefer the state with least starting index
       *    eg. among ABC and AC chose ABC
       *    No need to compare starting index for this rule
       *    as while traversing in the decreasing order of
       *    state numbers, the one which is prefered is set
       *    first.
       * 2) if starting index is the same, prefer the state
       *    which had transition from the state with least
       *    starting index
       *    eg. for AB*C*, among ABBC and ABCC, chose ABBC
       *    As we are traversing in the decreasing order of 
       *    state numbers, by just over writing the prev.
       *    binding is the solution when starting index is same
       */
      
      if(s.states[step].getStartIndex() == seq)
      {
        s.states[curState].copyAttrs(s.states[step], inTupleStorageAlloc, true);
        if(aggrsPresent)
          copyAggrTuple(s.states[curState], s.states[step]);
        s.states[step].setStartIndex(seq);
        s.states[step].setCurrTuple(s.inputTuple, map[input], inTupleStorageAlloc);
        if(hasAggrs[input])
          applyInitIncrEvals(curState, step, input, s.states[step]);
      }
    }
    else
    {
      if(s.unsureMatch == s.states[step] && step != curState)
        s.states[curState].copyAttrs(s.states[step], inTupleStorageAlloc, true);
      else
        s.states[curState].copyAttrs(s.states[step], inTupleStorageAlloc, false);
      if(aggrsPresent)
        copyAggrTuple(s.states[curState], s.states[step]);
      s.states[step].setStartIndex(seq);
      s.states[step].setCurrTuple(s.inputTuple, map[input], inTupleStorageAlloc);
      if(hasAggrs[input])
        applyInitIncrEvals(curState, step, input, s.states[step]);
      s.activeStates[step] = true;
    }
  }
  
  public int run(int timeSlice) throws CEPException
  {
    int numElements;
    boolean done = false;
    PatternStrmState s = (PatternStrmState) mut_state;
    boolean exitState = true;
    int numBindings = dfa.getNumStates();
    
    
    assert s.state != ExecState.S_UNINIT;

    // Stats
    s.stats.incrNumExecutions();

    assert defs.length == dfa.getNumAlphabets();
    
    try
    {
      // Number of input elements to process
      numElements = timeSlice;
      while ((s.stats.getNumInputs() < numElements) || (!exitState))
      {
        switch (s.state)
        {
          case S_PRE_INIT:
            
            //evaluate null eval for every binding
            if(aggrsPresent)
            {
              for(int i = 0; i < s.states.length; i++)
              {
                applyNullEvals(s.states[i]);
              }
            }
            s.state = ExecState.S_INIT;
            break;
            
          case S_PROPAGATE_OLD_DATA:
            setExecSynopsis((ExecSynopsis) outSynopsis);
            handlePropOldData();
            break;

          case S_INIT:
            // Get next input element
            s.inputElement = inputQueue.dequeue(s.inputElementBuf);
            s.state = ExecState.S_INPUT_DEQUEUED;
            
          case S_INPUT_DEQUEUED:
            // Determine the next step based on element kind
            if (s.inputElement == null)
            {
              if (s.lastInputTs <= s.lastOutputTs)
              {
                s.state = ExecState.S_INIT;
                done = true;
                break;
              }

              // Output a heartbeat
              s.outputTuple = null;
              s.outputKind = QueueElement.Kind.E_HEARTBEAT;
              s.state = ExecState.S_ALLOCATE_ELEM;
              break;
            }
            else
            {
              // Update our counts
              s.stats.incrNumInputs();

              // Increment the sequence number
              s.incrementSeq();
              
              // Update last input ts
              s.inputTs = s.inputElement.getTs();
              
              // update input tuple
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

              // Update the last input Ts now
              s.lastInputTs = s.inputTs;
              exitState = false;

              // Nothing more to be done for heartbeats
              if (s.inputElement.getKind() == QueueElement.Kind.E_HEARTBEAT)
              {
                s.state = ExecState.S_INPUT_ELEM_CONSUMED;
                break;
              }
              else
              {
                s.outputKind = s.inputElement.getKind();
                if (s.inputElement.getKind() == QueueElement.Kind.E_PLUS)
                {
                  s.state = ExecState.S_PROCESSING1;
                }
                else if (s.inputElement.getKind() == QueueElement.Kind.E_MINUS)
                {
                  //Input to patternStrm operator is a stream
                  assert false;
                  break;
                }
              }
            }
          
          case S_PROCESSING1:
            evalContext.bind(s.inputTuple,IEvalContext.INPUT_ROLE);
            //Bind previous tuple to prev_input_role and update the prevTuple
            evalContext.bind(s.prevTuple, IEvalContext.PREV_INPUT_ROLE);
            
            //calculate the potential matches for the input tuple
            //indicated by matches[i] being true 
            //definitions correspoinding to undefined correlation names are null
            for(int i = 0; i < defs.length; i++)
            {
              if((defs[i] == null) || (defs[i].eval(evalContext)))
                  s.matches[i] = true;
              else 
                s.matches[i] = false;
            }
            s.state = ExecState.S_PROCESSING2;
            
          case S_PROCESSING2:
            
            /* for every active binding
             * traverse in the reverse order so as to
             * get rid of temporary states for holding the 
             * newbindings. This is possible from the fact that
             * statemachine is always forward moving.
             * State i never makes a transition to state < i
             */
            for(int j = numBindings-1; j >= 0; j--)
            {
              if(s.activeStates[j])
              {
                //Initial state is always active
                if(j != 0)
                  s.activeStates[j] = false;
                int skip = -1;
                for(int i = 0; i < defs.length; i++)
                {
                  //for every possible match
                  if(s.matches[i])
                  {
                    if(alphToStateMap[i] == j-1)
                    {
                      skip = i;
                      continue;
                    }
                    applyTransition(j, i, s);
                  }
                }
                if(skip >= 0)
                {
                  applyTransition(j, skip, s);
                }
                /* Loc 1 */
                //current binding is made inactive at start
                //if there is any transition from this state,j, to j itself,
                //state j should have been active by now.
                //for optimisation, transition from j -> j, copyAttrs is not performed.
                //And also the refcount of the tuples in the binding/state is the same except
                //for the current tuple.
                //Hence if the current state/binding is not active, it has to be decRefed.
                if(!s.activeStates[j])
                {
                  if(s.states[j] != s.unsureMatch)
                    s.states[j].decrRef(inTupleStorageAlloc);
                }
              }
            }
            
            //release tuple ptr from the operator
            //it may have references in the bindings
            inTupleStorageAlloc.release(s.prevTuple);
            s.prevTuple = s.inputTuple;
            
            s.uniqueFinal = null;
            boolean[] isFinal = dfa.getFinalStates();
            //starting from State 1
            for(int i = 1; i < numBindings; i++)
            {
              if(s.activeStates[i] && isFinal[i])
              {
                //update s.uniquefinal
                if(s.uniqueFinal == null)
                {
                  s.uniqueFinal    = s.states[i];
                  s.uniqueFinalPos = i;
                }
                else
                {
                  /* apply preferment rules
                   * 1) eg. in AB*C* to chose among AC and ABC check the 
                   *    minimum starting sequence number.
                   * 2) eg. in AB*C*D to chose among ABBC and ABBB, chose
                   *    the one in the least final state
                   *    The check for (2) can be avoided if traversting in
                   *    increasing order and not worrying if start seq. is same
                   */
                  if(s.uniqueFinal.getStartIndex() > s.states[i].getStartIndex())
                  {
                    s.uniqueFinal    = s.states[i];
                    s.uniqueFinalPos = i;
                  }
                }
              }
            }
            if(s.uniqueFinal == null && s.unsureMatch == null)
            {
              s.state = ExecState.S_INIT;
              break;
            }
            else if(s.uniqueFinal != null)
            {
              //either a new unsurematch or an output is obtained
              //discard the previous unsurematch only if the starting indices
              //match else report current unsurematch and move to processing3
              if(s.unsureMatch != null)
              {
                if(s.unsureMatch.getStartIndex() != s.uniqueFinal.getStartIndex())
                {
                  s.outputBinding = s.unsureMatch;
                  s.unsureMatch   = null;
                  s.processCurrentFinal = true;
                  s.state = ExecState.S_OUTPUT_TUPLE;
                  break;
                }
                boolean found = false;
                for(int i = 0; i < s.states.length; i++)
                {
                  if(s.activeStates[i])
                  {
                    if(s.states[i] == s.unsureMatch)
                    {
                      found = true;
                      break;
                    }
                  }
                }
                //decrement refcount for tupleptrs in unsurematch
                //which was deliberately avoided at /* Loc 1 */
                //donot decrement the refcount if the binding is
                //active
                if(!found)
                  s.unsureMatch.decrRef(inTupleStorageAlloc);
              }
              s.unsureMatch = null;
              s.state = ExecState.S_PROCESSING3;
            }
            else
            {
              long min = minStartIndex(s.states, s.activeStates, numBindings);
              if((s.unsureMatch.getStartIndex() < min) 
                  || min == Long.MAX_VALUE)
              {
                s.outputBinding = s.unsureMatch;
                s.unsureMatch   = null;
                s.state = ExecState.S_OUTPUT_TUPLE;
                break;
              }
              else
              {
                s.state = ExecState.S_INIT;
                break;
              }
            }
            
          //proceesing of uniqueFinal and min(B-{uniqueFinal}) and updating of
          //unsureMatch
          case S_PROCESSING3:
            long left = s.uniqueFinal.getStartIndex();
            long min  = minStartIndex(s.states, s.activeStates,
                s.uniqueFinalPos);
            //if left == min or
            //if the current only active binding can be growable further
            if(left == min || isGrowable(s.uniqueFinalPos))
            {
              s.unsureMatch = s.uniqueFinal;
              s.outputTs    = s.inputTs;
              //leave State 0
              for(int i = 1; i < numBindings; i++)
              {
                if(s.activeStates[i])
                {
                  if(s.states[i].getStartIndex() > left)
                  {
                    s.activeStates[i] = false;
                    s.states[i].decrRef(inTupleStorageAlloc);
                  }
                }
              }
              s.state = ExecState.S_INIT;
              break;
            }
            else if(left < min)
            {
              s.outputBinding = s.uniqueFinal;
              s.outputTs      = s.inputTs;
              //leave State 0
              for(int i = 1; i < numBindings; i++)
              {
                if(s.activeStates[i])
                {
                  if(i != s.uniqueFinalPos)
                    s.states[i].decrRef(inTupleStorageAlloc);
                  s.activeStates[i] = false;
                }
              }
              s.state = ExecState.S_OUTPUT_TUPLE;
            }
            else
              assert false;
          
          case S_OUTPUT_TUPLE:
            // Allocate an output tuple
            s.outputTuple = tupleStorageAlloc.allocate();
            s.state = ExecState.S_PROCESSING4;
          
          // copy the outputbinding tuplearray to the bindingrole in evalcontext
          // run the copyeval to copy from bindingrole to output role 
          case S_PROCESSING4:
            ITuplePtr[] elems = s.outputBinding.getElems();
            int i;
            for(i = 0; i < elems.length - 1; i++)
            {
              if(elems[i] != null)
              {
                evalContext.bind(elems[i], bindRole + i);
              }
            }
            //copy aggr tuple without pin
            if(elems[i] != null)
              evalContext.bind(elems[i], bindRole + i);
            
            evalContext.bind(s.outputTuple, IEvalContext.NEW_OUTPUT_ROLE);
            copyEval.eval(evalContext);
            s.state = ExecState.S_ALLOCATE_ELEM;
            
          // output tuple has to be assigned before allocate_elem
          case S_ALLOCATE_ELEM:
            s.state = ExecState.S_OUTPUT_TIMESTAMP;
            
          case S_OUTPUT_TIMESTAMP:
            s.state = ExecState.S_OUTPUT_READY;
            
          case S_OUTPUT_READY:
            if (s.outputKind == QueueElement.Kind.E_HEARTBEAT)
            {
              assert s.inputElement == null;
              s.lastOutputTs = s.lastInputTs;
              s.outputElement.heartBeat(s.lastInputTs);
            }
            else
            {
              s.outputElement.setTuple(s.outputTuple);
              s.outputElement.setTs(s.outputTs);
              s.outputElement.setKind(QueueElement.Kind.E_PLUS);
              // Update last output ts
              s.lastOutputTs = s.inputTs;
            }
            s.state = ExecState.S_OUTPUT_ELEMENT;
            
          case S_OUTPUT_ELEMENT:
            if (outputQueue.isFull())
            {
              done = true;
              break;
            }
            outputQueue.enqueue(s.outputElement);
            s.state = ExecState.S_OUTPUT_ENQUEUED;
            
          case S_OUTPUT_ENQUEUED:
            s.stats.incrNumOutputs();
            s.state = ExecState.S_PROCESSING5;
            
          case S_PROCESSING5:
            if (s.inputElement != null)
              s.state = ExecState.S_BINDING_CONSUMED;
            else
            {
              s.state = ExecState.S_INIT;
              break;
            }
            
          case S_BINDING_CONSUMED:
            s.outputBinding.decrRef(inTupleStorageAlloc);
            exitState = false;
            if(s.processCurrentFinal)
            {
              s.state = ExecState.S_PROCESSING3;
              s.processCurrentFinal = false;
            }
            else
              s.state = ExecState.S_INIT;            
            break;
            
          case S_INPUT_ELEM_CONSUMED:
            assert s.inputElement != null;

            if (s.inputTuple != null)
            {
              inTupleStorageAlloc.release(s.inputTuple);
            }
      
            exitState = false;
            s.state = ExecState.S_INIT;
            break;
      
          default:
            assert false;
        }
        if (done)
          break;
      }
    }
    catch (SoftExecException e1)
    {
      // TODO Ignore them
      LogUtil.logStackTrace(LoggerType.TRACE, Level.SEVERE, e1);
      return 0;
    }
    
    return 0;
  }
  
  public void deleteOp()
  {
    
  }
  
  @Override
  protected boolean isHeartbeatPending()
  {
    return false;
  }
}

