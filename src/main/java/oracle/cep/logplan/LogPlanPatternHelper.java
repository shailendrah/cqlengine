/* $Header: pcbpel/cep/server/src/oracle/cep/logplan/LogPlanPatternHelper.java /main/13 2009/03/08 23:44:09 udeshmuk Exp $ */

/* Copyright (c) 2007, 2009, Oracle and/or its affiliates.
All rights reserved. */

/*
   DESCRIPTION
    Logical Plan Generation. Helper methods for MATCH_RECOGNIZE clause
    processing.

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    udeshmuk    03/05/09 - remove project for measures.
    udeshmuk    10/15/08 - support for xmlagg orderby in pattern
    sbishnoi    07/14/08 - 
    udeshmuk    07/07/08 - support for lazy quantifiers over group.
    udeshmuk    06/29/08 - support grouping over greedy quantifiers.
    rkomurav    05/15/08 - add method t b to modf modify NFA for non event
                           detection case
    rkomurav    05/14/08 - reorg code for regList to semantic layer
    rkomurav    03/19/08 - support for subset
    rkomurav    03/18/08 - add default correlation defs in semantic layer
                           instead of logical layer
    rkomurav    02/29/08 - add support for operations on groupuings
    rkomurav    02/12/08 - add getNFA method
    rkomurav    01/30/08 - support alternation
    rkomurav    01/16/08 - support lazy quantifiers.
    rkomurav    07/03/07 - uda
    anasrini    07/02/07 - support for partition by clause
    sbishnoi    06/08/07 - support for multi-arg UDAs
    rkomurav    05/28/07 - cleanup
    anasrini    05/28/07 - 
    rkomurav    05/27/07 - 
    anasrini    05/26/07 - classify aggregations by corr name referenced
    anasrini    05/24/07 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/logplan/LogPlanPatternHelper.java /main/13 2009/03/08 23:44:09 udeshmuk Exp $
 *  @author  anasrini
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.logplan;

import java.util.ArrayList;
import java.util.ListIterator;

import oracle.cep.common.BaseAggrFn;
import oracle.cep.semantic.BExpr;
import oracle.cep.semantic.CorrNames;
import oracle.cep.semantic.OrderByExpr;
import oracle.cep.semantic.Regexp;
import oracle.cep.semantic.SimpleRegexp;
import oracle.cep.semantic.ComplexRegexp;
import oracle.cep.semantic.AggrExpr;
import oracle.cep.semantic.PatternSpec;
import oracle.cep.semantic.XMLAggExpr;

import oracle.cep.logplan.pattern.CorrName;
import oracle.cep.logplan.pattern.CorrNameDef;
import oracle.cep.logplan.pattern.SubsetCorr;
import oracle.cep.semantic.SFWQuery;
import oracle.cep.logplan.attr.Attr;
import oracle.cep.logplan.attr.CorrAttr;
import oracle.cep.logplan.expr.Expr;
import oracle.cep.logplan.expr.BoolExpr;
import oracle.cep.logplan.expr.ExprOrderBy;
import oracle.cep.logplan.expr.factory.SemQueryExprFactory;
import oracle.cep.logplan.expr.factory.SemQueryExprFactoryContext;
import oracle.cep.util.DFA;
import oracle.cep.util.NFA;
import oracle.cep.util.Transition;


/**
 * Logical Plan Generation. Helper methods for MATCH_RECOGNIZE clause 
 * processing.
 */

class LogPlanPatternHelper {

  /**
   * This class should not be instantiated.
   * Contains only static methods.
   */
  private LogPlanPatternHelper() {
  }

  // Process the partition by clause
  static Attr[] processPartnBy(PatternSpec pSpec) {

    oracle.cep.semantic.Attr[] pbySemAttrs = pSpec.getPartitionByAttrs();
    Attr[]                     partnAttrs  = null;
    int                        numPartnAttrs;
    Attr                       pbyLogAttr;

    if (pbySemAttrs != null) {
      numPartnAttrs = pbySemAttrs.length;
      partnAttrs    = new Attr[numPartnAttrs]; 

      // transform the partition by attribute into logical 
      for (int aidx = 0; aidx < numPartnAttrs; aidx++) {
        // to check that the attribute specified are valid 
        pbyLogAttr = LogPlanHelper.transformAttr(pbySemAttrs[aidx]); 
        // add it to the list of partition attributes 
        partnAttrs[aidx] = pbyLogAttr; 
      }
    }

    return partnAttrs;
  }

  //convert the correlation definitions
  static CorrNameDef[] convertCorrDefs(
                                  oracle.cep.semantic.CorrNameDef[] semCorrs, 
                                  SFWQuery query, SubsetCorr[] subsetCorrs)
  {
    int           id;
    BExpr         semExpr;
    BoolExpr      expr;
    CorrNameDef[] logCorrs = new CorrNameDef[semCorrs.length];
    int[]         subsetIds;
    int[]         subsetPos;
    boolean       found;
    
    for(int i = 0; i < semCorrs.length; i++)
    {
      id      = semCorrs[i].getVarId();
      semExpr = semCorrs[i].getExpr();
      //process the boolean expression
      if(semExpr != null)
      {
        expr = (BoolExpr)SemQueryExprFactory.getInterpreter(
            semExpr,
            new SemQueryExprFactoryContext(semCorrs[i].getExpr(), query));
      }
      else
      {
        expr = null;
      }
      logCorrs[i] = new CorrNameDef(id, expr);
      
      //process the subset ids and replace them with bind pos of the subsets
      if(subsetCorrs != null)
      {
        subsetIds = semCorrs[i].getSubsetIds();
        subsetPos = new int[subsetIds.length];
        for(int j = 0; j < subsetIds.length; j++)
        {
          found = false;
          for(int k = 0; k < subsetCorrs.length; k++)
          {
            if(subsetCorrs[k].getVarId() == subsetIds[j])
            {
              subsetPos[j] = subsetCorrs[k].getBindPos();
              found = true;
              break;
            }
          }
          assert found;
        }
        logCorrs[i].setSubsetPos(subsetPos);
      }

      // Specify the postion of the correlation name in the DEFINE clause
      logCorrs[i].setBindPos(i);
      convertCorrAggrs(logCorrs[i], semCorrs[i], query);
    }
    return logCorrs;
  }
  
  //convert the subset correlations
  static SubsetCorr[] convertSubsetCorrs(
                                    oracle.cep.semantic.SubsetCorr[] semCorrs,
                                    SFWQuery query, int startBindPos)
  {
    SubsetCorr[] logCorrs = new SubsetCorr[semCorrs.length];
    for(int i = 0; i < semCorrs.length; i++)
    {
      logCorrs[i] = new SubsetCorr(semCorrs[i].getVarId());
      logCorrs[i].setBindPos(startBindPos);
      startBindPos++;
      convertCorrAggrs(logCorrs[i], semCorrs[i], query);
    }
    return logCorrs;
  }
  
  // convert the associated aggrs of the correlations
  // (either corrDef or subsetCorr)
  static void convertCorrAggrs(CorrName logCorr, CorrNames semCorr,
                          SFWQuery query)
  {
    int        numAggrs = semCorr.getNumAggrs();
    AggrExpr[] aggrs    = semCorr.getAggrs();
    BaseAggrFn fn;
    Expr[]        logAggrParamExpr;
    ExprOrderBy[] logOrderByExpr;

    ArrayList<BaseAggrFn> aggrFns        = new ArrayList<BaseAggrFn>();
    ArrayList<Expr[]>     aggrParamExprs = new ArrayList<Expr[]>();
    ArrayList<ExprOrderBy[]> orderExprs  = new ArrayList<ExprOrderBy[]>();

    for (int j=0; j<numAggrs; j++) {
      fn = aggrs[j].getAggrFunction();
      logAggrParamExpr = new Expr[aggrs[j].getNumParamExprs()];
      for(int k=0; k<aggrs[j].getNumParamExprs(); k++){
        logAggrParamExpr[k] = SemQueryExprFactory.getInterpreter(
        (aggrs[j].getExprs())[k],
        new SemQueryExprFactoryContext((aggrs[j].getExprs())[k], query));
      } 
      if(aggrs[j] instanceof XMLAggExpr)
      {
        OrderByExpr[] orderByExprs = ((XMLAggExpr)aggrs[j]).getOrderByExprs();
        if(orderByExprs != null)
        {
          logOrderByExpr = new ExprOrderBy[orderByExprs.length];
          for(int k=0; k < orderByExprs.length; k++)
          {
            logOrderByExpr[k] = (ExprOrderBy)SemQueryExprFactory.getInterpreter(
              orderByExprs[k], new SemQueryExprFactoryContext(orderByExprs[k], query));
          }
          orderExprs.add(logOrderByExpr);
        }
        else
          orderExprs.add(null);
      }
      else 
        orderExprs.add(null);
      aggrFns.add(fn);
      aggrParamExprs.add(logAggrParamExpr);
    }

    int oldNumAggrs = numAggrs;
    // Augment the collected aggregates
    // Need SUM and COUNT for incremental computation of average
    LogPlanAggrHelper.augmentAggrs(aggrFns, aggrParamExprs);

    // Get the number again since new aggregations might have been added
    numAggrs = aggrParamExprs.size();
    
    //add null to the orderExprs list for the entries added in augmentAggrs
    for(int k=0; k < numAggrs - oldNumAggrs; k++)
      orderExprs.add(null);
    
    // Set these in logical corr defs
    for (int j=0; j<numAggrs; j++)
      logCorr.addAggr(aggrFns.get(j), aggrParamExprs.get(j), orderExprs.get(j));
  }
  
  /*
   * Basic working of construction of DFA:
   * At any non leaf node of the regular expression tree,
   * Build left and right DFAs independently and merge them.
   * Merge rules vary depending on the node operator.
   * 
   * Building a leaf DFA(cases: A, A*, A+, A?):
   * Construct DFA with two states(including init state) and 
   * number of alphabets equal to number of alphabets of the final DFA
   */
  static DFA getDFA(Regexp regexp, int numAlphs)
  {
    ComplexRegexp complexRegexp;
    SimpleRegexp  simpleRegexp;
    Regexp        unaryRegexp;
    Regexp        leftRegexp;
    Regexp        rightRegexp;
    DFA           baseDFA;
    DFA           simpleDFA;
    DFA           leftDFA;
    DFA           rightDFA;
    DFA           mergedDFA;
    DFA           dfa;
    int           numLeftStates;
    int           numRightStates;
    int           numMergedStates;
    boolean[]     leftFinal;
    boolean[]     rightFinal;
    boolean       rightStartStateFinal;
    int[]         transitions;
    int[]         startStateTransitions;
    int           numStartStateTrans;
    int[]         numRightTransArr;
    int           index;
    int           mergedState;
    boolean       skipFinal;
    
    dfa = null;
    
    if(regexp instanceof ComplexRegexp)
    {
      complexRegexp = (ComplexRegexp) regexp;
      
      //Build DFA for base units like A* A+ A?
      if(complexRegexp.getUnaryOperand() != null)
      {
        unaryRegexp = complexRegexp.getUnaryOperand();
        if (unaryRegexp instanceof SimpleRegexp)
        {
          simpleRegexp = (SimpleRegexp)unaryRegexp;
          index        = simpleRegexp.getAlphIndex();
          simpleDFA    = new DFA(2, numAlphs);
          
          simpleDFA.addTransition(0, index, 1);
          simpleDFA.setFinal(1);
          switch(complexRegexp.getRegexpOp())
          {
          case GREEDY_PLUS:
            simpleDFA.addTransition(1, index, 1);
            simpleDFA.addAlphPref(0, index);
            simpleDFA.addAlphPref(1, index);
            simpleDFA.addAlphPref(1, DFA.FINAL);
            simpleDFA.setFinalPrefIndex(1, 1);
            break;
          case GREEDY_STAR:
            simpleDFA.addTransition(1, index, 1);
            simpleDFA.addAlphPref(1, index);
            simpleDFA.addAlphPref(1, DFA.FINAL);
            simpleDFA.addAlphPref(0, index);
            simpleDFA.addAlphPref(0, DFA.FINAL);
            simpleDFA.setFinalPrefIndices(0, 1, 1, 1);
            simpleDFA.setFinal(0);
            break;
          case GREEDY_QUESTION:
            simpleDFA.addAlphPref(1, DFA.FINAL);
            simpleDFA.addAlphPref(0, index);
            simpleDFA.addAlphPref(0, DFA.FINAL);
            simpleDFA.setFinalPrefIndices(0, 1, 1, 0);
            simpleDFA.setFinal(0);
            break;
          case LAZY_PLUS:
            simpleDFA.addTransition(1, index, 1);
            simpleDFA.addAlphPref(0, index);
            simpleDFA.addAlphPref(1, DFA.FINAL);
            simpleDFA.addAlphPref(1, index);
            simpleDFA.setFinalPrefIndex(1, 0);
            break;
          case LAZY_STAR:
            simpleDFA.addTransition(1, index, 1);
            simpleDFA.addAlphPref(1, DFA.FINAL);
            simpleDFA.addAlphPref(1, index);
            simpleDFA.addAlphPref(0, DFA.FINAL);
            simpleDFA.addAlphPref(0, index);
            simpleDFA.setFinalPrefIndices(0, 0, 1, 0);
            simpleDFA.setFinal(0);
            break;
          case LAZY_QUESTION:
            simpleDFA.addAlphPref(1, DFA.FINAL);
            simpleDFA.addAlphPref(0, DFA.FINAL);
            simpleDFA.addAlphPref(0, index);
            simpleDFA.setFinalPrefIndices(0, 0, 1, 0);
            simpleDFA.setFinal(0);
            break;
          default:
            assert false;
          }
          dfa = simpleDFA;
        }
        else
        {
          System.out.println("unknown case for class A and class B patterns");
          assert false;
        }
      }
      else
      {
        leftRegexp  = complexRegexp.getLeftOperand();
        rightRegexp = complexRegexp.getRightOperand();
        
        assert leftRegexp  != null;
        assert rightRegexp != null;
        
        leftDFA              = getDFA(leftRegexp, numAlphs);
        rightDFA             = getDFA(rightRegexp, numAlphs);
        numLeftStates        = leftDFA.getNumStates();
        numRightStates       = rightDFA.getNumStates();
        leftFinal            = leftDFA.getFinalStates();
        rightFinal           = rightDFA.getFinalStates();
        rightStartStateFinal = rightFinal[0];
        mergedDFA            = null;
        
        switch(complexRegexp.getRegexpOp())
        {
        case CONCAT:
          //for every final state in the left DFA, add all the transitions
          //from state 0 of the right DFA
          numMergedStates       = (numLeftStates + numRightStates) - 1;
          mergedDFA             = new DFA(numMergedStates, numAlphs);
          numRightTransArr      = rightDFA.getNumTransArr();
          startStateTransitions = rightDFA.getAlphPrefs(0);
          numStartStateTrans    = numRightTransArr[0];
          
          mergedDFA.copyTransPrefsTables(leftDFA);
          
          for(int i = 0; i < numLeftStates; i++)
          {
            if(leftFinal[i])
            {
              //add ordered transitions at the point where 
              //final appears in the left DFA
              for(int j = 0; j < numStartStateTrans; j++)
              {
                if(startStateTransitions[j] == DFA.FINAL)
                  continue;
                mergedDFA.addTransition(i, startStateTransitions[j], 
                    rightDFA.next(0, 
                        startStateTransitions[j]) + numLeftStates - 1);
              }
              
              mergedDFA.mergePrefs(i, startStateTransitions, 
                  numStartStateTrans, leftDFA.getFinalPrefIndex(i));
              
              if(rightStartStateFinal)
              {
                mergedDFA.setFinal(i);
                mergedDFA.setFinalPrefIndex(i, leftDFA.getFinalPrefIndex(i) + 
                    rightDFA.getFinalPrefIndex(0));
              }
            }
          }
          
          for(int i = 1; i < numRightStates; i++)
          {
            mergedState = numLeftStates + i - 1;
            transitions = rightDFA.getAlphPrefs(i);
            for(int j = 0; j < numRightTransArr[i]; j++)
            {
              if(transitions[j] == DFA.FINAL)
                continue;
              mergedDFA.addTransition(mergedState, transitions[j], 
                rightDFA.next(i, transitions[j]) + numLeftStates - 1);
            }
            mergedDFA.addAlphPref(mergedState, transitions, 
                numRightTransArr[i]);
            if(rightFinal[i])
            {
              mergedDFA.setFinal(mergedState);
              mergedDFA.setFinalPrefIndex(mergedState,
                  rightDFA.getFinalPrefIndex(i));
            }
          }
          break;

        case ALTERNATION:
          //create a new state for initial state and add all transtitions from
          //initial states of both left and right DFAs
          //make the new initial state final if atleast one of the
          //initial states of left and right DFAs is final.
          numMergedStates       = (numLeftStates + numRightStates) - 1;
          mergedDFA             = new DFA(numMergedStates, numAlphs);
          numRightTransArr      = rightDFA.getNumTransArr();
          startStateTransitions = rightDFA.getAlphPrefs(0);
          numStartStateTrans    = numRightTransArr[0];
          
          mergedDFA.copyTransPrefsTables(leftDFA);
          mergedDFA.copyFinals(leftDFA);
          
          skipFinal = mergedDFA.isFinal(0);
          //add transitions from initial state of right DFA to the
          //initial state of the merged DFA
          for(int j = 0; j < numStartStateTrans; j++)
          {
            if(startStateTransitions[j] == DFA.FINAL)
              continue;
            mergedDFA.addTransition(0, startStateTransitions[j], 
                rightDFA.next(0, 
                    startStateTransitions[j]) + numLeftStates - 1);
          }
          index = rightDFA.getFinalPrefIndex(0);
          //initial states of both left and right DFAs are final
          //case like (A*|B*)
          //do not copy the final preference into the merged DFA but
          //(final takes precedence from left DFA)
          //copy all the other transition preferences
          if(skipFinal && rightStartStateFinal)
          {
            mergedDFA.addAlphPref(0, startStateTransitions, index);
            //add further alph prefs skipping the final pref.
            for(int i = index + 1; i < numStartStateTrans; i++)
            {
              mergedDFA.addAlphPref(0, startStateTransitions[i]);
            }
          }
          //cases like (A*|BC), (BC|A*), (A|BC)
          else
          {
            if(rightStartStateFinal)
            {
              mergedDFA.setFinal(0);
              mergedDFA.setFinalPrefIndex(0, index + mergedDFA.getNumTrans(0));
            }
            mergedDFA.addAlphPref(0, startStateTransitions, numStartStateTrans);
          }
          
          //copy transition and prefs for all the states on right DFA
          //except initial state
          for(int i = 1; i < numRightStates; i++)
          {
            mergedState = numLeftStates + i - 1;
            transitions = rightDFA.getAlphPrefs(i);
            for(int j = 0; j < numRightTransArr[i]; j++)
            {
              if(transitions[j] == DFA.FINAL)
                continue;
              mergedDFA.addTransition(mergedState, transitions[j], 
                rightDFA.next(i, transitions[j]) + numLeftStates - 1);
            }
            mergedDFA.addAlphPref(mergedState, transitions, 
                numRightTransArr[i]);
            if(rightFinal[i])
            {
              mergedDFA.setFinal(mergedState);
              mergedDFA.setFinalPrefIndex(mergedState,
                  rightDFA.getFinalPrefIndex(i));
            }
          }
          break;
        default:
          assert false;
        }
        dfa = mergedDFA;
      }
    }
    else
    {
      //Build DFA for base unit like 'A'
      simpleRegexp = (SimpleRegexp) regexp;
      index        = simpleRegexp.getAlphIndex();
      baseDFA      = new DFA(2, numAlphs);
      
      baseDFA.addTransition(0, index, 1);
      baseDFA.addAlphPref(0, index);
      baseDFA.addAlphPref(1, DFA.FINAL);
      
      baseDFA.setFinal(1);
      baseDFA.setFinalPrefIndex(1, 0);
      
      dfa = baseDFA;
    }
    
    assert dfa != null;
    return dfa;
  }
  
  static NFA getNFA(Regexp regexp)
  {
    int                                 index;
    int                                 numLeftStates;
    int                                 numRightStates;
    int                                 numMergedStates;
    int                                 mergedState;
    NFA                                 nfa;
    NFA                                 baseNFA;
    NFA                                 simpleNFA;
    NFA                                 leftNFA;
    NFA                                 rightNFA;
    NFA                                 mergedNFA;
    NFA                                 groupNFA;
    Regexp                              unaryRegexp;
    Regexp                              leftRegexp;
    Regexp                              rightRegexp;
    Regexp                              groupRegexp;
    boolean[]                           rightFinal;
    boolean                             rightStartStateFinal;
    Transition                          trans;
    Transition                          trans1;
    Transition                          trans2;
    Transition                          trans3;
    SimpleRegexp                        simpleRegexp;
    ComplexRegexp                       complexRegexp;
    ArrayList<Transition>               startStateTrans;
    ArrayList<Transition>               transitions;
    ListIterator<ArrayList<Transition>> listOfTransIter;
    
    nfa = null;
    
    if(regexp instanceof ComplexRegexp)
    {
      complexRegexp = (ComplexRegexp) regexp;
      unaryRegexp   = complexRegexp.getUnaryOperand();
      
      //Build NFA for base units like A* A+ A? A*? A+? A??
      if(unaryRegexp != null)
      {
        if (unaryRegexp instanceof SimpleRegexp)
        {
          simpleRegexp = (SimpleRegexp) unaryRegexp;
          index        = simpleRegexp.getAlphIndex();
          simpleNFA    = new NFA(2);
          
          trans  = new Transition(index, 1);
          trans1 = new Transition(index, 1);
          //for state 0
          trans2 = new Transition(NFA.FINAL, NFA.UNDEFINED_STATE);
          //for state 1
          trans3 = new Transition(NFA.FINAL, NFA.UNDEFINED_STATE);
          
          simpleNFA.setFinal(1);
          switch(complexRegexp.getRegexpOp())
          {
          case GREEDY_PLUS:
            simpleNFA.addTransition(0, trans);
            simpleNFA.addTransition(1, trans1, trans3);
            simpleNFA.setFinalPrefIndex(1, 1);
            break;
          case GREEDY_STAR:
            simpleNFA.addTransition(0, trans, trans2);
            simpleNFA.addTransition(1, trans1, trans3);
            simpleNFA.setFinalPrefIndices(0, 1, 1, 1);
            simpleNFA.setFinal(0);
            break;
          case GREEDY_QUESTION:
            simpleNFA.addTransition(0, trans, trans2);
            simpleNFA.addTransition(1, trans3);
            simpleNFA.setFinalPrefIndices(0, 1, 1, 0);
            simpleNFA.setFinal(0);
            break;
          case LAZY_PLUS:
            simpleNFA.addTransition(0, trans);
            simpleNFA.addTransition(1, trans3, trans1);
            simpleNFA.setFinalPrefIndex(1, 0);
            break;
          case LAZY_STAR:
            simpleNFA.addTransition(0, trans2, trans);
            simpleNFA.addTransition(1, trans3, trans1);
            simpleNFA.setFinalPrefIndices(0, 0, 1, 0);
            simpleNFA.setFinal(0);
            break;
          case LAZY_QUESTION:
            simpleNFA.addTransition(0, trans2, trans);
            simpleNFA.addTransition(1, trans3);
            simpleNFA.setFinalPrefIndices(0, 0, 1, 0);
            simpleNFA.setFinal(0);
            break;
          default:
            assert false;
          }
          nfa = simpleNFA;
        }
        //Build NFA for units involving groups like (ABCD)*, etc
        else
        {
          assert unaryRegexp instanceof ComplexRegexp;
          groupRegexp = (ComplexRegexp) unaryRegexp;
          //Get the NFA for the group expression
          groupNFA        = getNFA(groupRegexp);
          boolean startStateFinal = groupNFA.isFinal(0);    
          startStateTrans = groupNFA.getTransitions(0);
          switch(complexRegexp.getRegexpOp())
          {
          case GREEDY_PLUS:
            for(int i=1; i < groupNFA.getNumStates(); i++)
            { //for every non-start final state add the start state transitions
              //in between the 'final' and its predecessor
              if(groupNFA.isFinal(i))
              {
                int finalPrefIndex = groupNFA.getFinalPrefIndex(i);
                groupNFA.doGreedyProcessing(i, startStateTrans, finalPrefIndex);
              }
            }
            nfa = groupNFA;
            break;
          case GREEDY_STAR:
            for(int i=1; i < groupNFA.getNumStates(); i++)
            { //for every non-start final state add the start state transitions
              //in between the 'final' and its predecessor
              if(groupNFA.isFinal(i))
              {
                int finalPrefIndex = groupNFA.getFinalPrefIndex(i);
                groupNFA.doGreedyProcessing(i, startStateTrans, finalPrefIndex);
              }
            }
            if(!startStateFinal)
            { //make the start state final as epsilon can also be accepted
              trans = new Transition(NFA.FINAL, NFA.UNDEFINED_STATE);
              groupNFA.addTransition(0, trans);
              groupNFA.setFinal(0);
              groupNFA.setFinalPrefIndex(0, startStateTrans.size()-1);
            }
            nfa = groupNFA;
            break;
          case GREEDY_QUESTION:
            if(!startStateFinal)
            { //make the start state final as epsilon can also be accepted
              trans = new Transition(NFA.FINAL, NFA.UNDEFINED_STATE);
              groupNFA.addTransition(0, trans);
              groupNFA.setFinal(0);
              groupNFA.setFinalPrefIndex(0, startStateTrans.size()-1);
            }
            nfa = groupNFA;
            break;
          case LAZY_PLUS:
            for(int i=1; i < groupNFA.getNumStates(); i++)
            { //for every non-start final state add the start state transitions
              //excluding 'final' in between the 'final' and its immediate successor
              if(groupNFA.isFinal(i))
              {
                int finalPrefIndex = groupNFA.getFinalPrefIndex(i);
                groupNFA.doLazyProcessing(i, startStateTrans, finalPrefIndex);
              }
            }
            nfa = groupNFA;
            break;
          case LAZY_STAR:
            for(int i=1; i < groupNFA.getNumStates(); i++)
            {
              //for every non-start final state add the start state transitions 
              //excluding 'final' in between the 'final' and its immediate successor
              if(groupNFA.isFinal(i))
              {
                int finalPrefIndex = groupNFA.getFinalPrefIndex(i);
                groupNFA.doLazyProcessing(i, startStateTrans, finalPrefIndex);
              }
            }
            if(startStateFinal)
            { //remove the original final transition
              groupNFA.getTransitions(0).remove(groupNFA.getFinalPrefIndex(0));
            }
            //make the start state final as epsilon can also be accepted and put it 
            //in first place
            trans = new Transition(NFA.FINAL, NFA.UNDEFINED_STATE);
            groupNFA.getTransitions(0).add(0, trans);
            groupNFA.setFinal(0);
            groupNFA.setFinalPrefIndex(0, 0);
            nfa = groupNFA;
            break;
          case LAZY_QUESTION:
            if(startStateFinal)
            { //remove the original final transition
              groupNFA.getTransitions(0).remove(groupNFA.getFinalPrefIndex(0));
            }
            //make the start state final as epsilon can also be accepted and put it 
            //in first place
            trans = new Transition(NFA.FINAL, NFA.UNDEFINED_STATE);
            groupNFA.getTransitions(0).add(0, trans);
            groupNFA.setFinal(0);
            groupNFA.setFinalPrefIndex(0, 0);
            nfa = groupNFA;
            break;
          default:
            assert false;
          }
        }
      }
      else
      {
        leftRegexp  = complexRegexp.getLeftOperand();
        rightRegexp = complexRegexp.getRightOperand();
       
        assert leftRegexp  != null;
        assert rightRegexp != null;
       
        leftNFA              = getNFA(leftRegexp);
        rightNFA             = getNFA(rightRegexp);
        numLeftStates        = leftNFA.getNumStates();
        numRightStates       = rightNFA.getNumStates();
        rightFinal           = rightNFA.getFinalStates();
        rightStartStateFinal = rightFinal[0];
        mergedNFA            = null;
        
        switch(complexRegexp.getRegexpOp())
        {
        case CONCAT:
          nfa = concatNFA(leftNFA, rightNFA, numLeftStates, numRightStates,
              rightStartStateFinal, rightFinal);          
          break;
        case ALTERNATION:
          //create a new state for initial state and add all transtitions from
          //initial states of both left and right NFAs
          //make the new initial state final if atleast one of the
          //initial states of left and right NFAs is final.
          boolean skipFinal;
          
          numMergedStates = (numLeftStates + numRightStates) - 1;
          mergedNFA       = new NFA(numMergedStates);
          startStateTrans = rightNFA.getTransitions(0);
          
          mergedNFA.copyTransitions(leftNFA);
          mergedNFA.copyFinals(leftNFA);
          
          skipFinal = mergedNFA.isFinal(0);
          index     = rightNFA.getFinalPrefIndex(0);
          
          //add transitions from initial state of right NFA to the
          //initial state of the merged NFA
          
          //initial states of both left and right NFAs are final
          //case like (A*|B*)
          //do not copy the final preference into the merged NFA but
          //(final takes precedence from left NFA)
          //copy all the other transition preferences
          if(skipFinal && rightStartStateFinal)
          {
            mergedNFA.copyTransitions(0, startStateTrans, numLeftStates - 1,
                index);
          }
          //cases like (A*|BC), (BC|A*), (A|BC)
          else
          {
            if(rightStartStateFinal)
            {
              mergedNFA.setFinal(0);
              mergedNFA.setFinalPrefIndex(0, index + mergedNFA.getNumTrans(0));
            }
            mergedNFA.copyTransitions(0, startStateTrans, numLeftStates - 1);
          }
          //copy transitions for all the states on right NFA
          //except initial state
          listOfTransIter = rightNFA.getListOfTransIter();
          listOfTransIter.next();
          for(int i = 1; i < numRightStates; i++)
          {
            mergedState = numLeftStates + i - 1;
            transitions = listOfTransIter.next();
            mergedNFA.copyTransitions(mergedState, transitions, 
                numLeftStates - 1);
            
            if(rightFinal[i])
            {
              mergedNFA.setFinal(mergedState);
              mergedNFA.setFinalPrefIndex(mergedState,
                  rightNFA.getFinalPrefIndex(i));
            }
          }
          nfa = mergedNFA;
          break;
        default:
          assert false;
        }
      }
    }
    else
    {
      //Build NFA for base unit like 'A'
      simpleRegexp = (SimpleRegexp) regexp;
      index        = simpleRegexp.getAlphIndex();
      baseNFA      = new NFA(2);
      
      trans  = new Transition(index, 1);
      trans1 = new Transition(NFA.FINAL, NFA.UNDEFINED_STATE);
      
      baseNFA.addTransition(0, trans);
      baseNFA.addTransition(1, trans1);
      
      baseNFA.setFinal(1);
      baseNFA.setFinalPrefIndex(1, 0);
     
      nfa = baseNFA;
    }
    
    assert nfa != null;
    return nfa;
  }
  
  //concat of nfa left and nfa right
  static private NFA concatNFA(NFA leftNFA, NFA rightNFA, int numLeftStates,
      int numRightStates, boolean rightStartStateFinal, boolean[] rightFinal)
  {
    int                                 numMergedStates;
    int                                 mergedState;
    NFA                                 mergedNFA;
    boolean[]                           leftFinal;
    ArrayList<Transition>               startStateTrans;
    ArrayList<Transition>               transitions;
    ListIterator<ArrayList<Transition>> listOfTransIter;
    
    //for every final state in the left NFA, add all the transitions
    //from state 0 of the right NFA
    numMergedStates = (numLeftStates + numRightStates) - 1;
    mergedNFA       = new NFA(numMergedStates);
    startStateTrans = rightNFA.getTransitions(0);
    leftFinal       = leftNFA.getFinalStates();
    
    mergedNFA.copyTransitions(leftNFA);
    
    for(int i = 0; i < numLeftStates; i++)
    {
      if(leftFinal[i])
      {
        //add ordered transitions at the point where 
        //final appears in the left NFA
        mergedNFA.mergeTrans(i, startStateTrans, 
            leftNFA.getFinalPrefIndex(i), numLeftStates - 1);
        if(rightStartStateFinal)
        {
          mergedNFA.setFinal(i);
          mergedNFA.setFinalPrefIndex(i, leftNFA.getFinalPrefIndex(i) +
              rightNFA.getFinalPrefIndex(0));
        }
      }
    }
    listOfTransIter = rightNFA.getListOfTransIter();
    listOfTransIter.next();
    for(int i = 1; i < numRightStates; i++)
    {
      mergedState = numLeftStates + i - 1;
      transitions = listOfTransIter.next();
      mergedNFA.copyTransitions(mergedState, transitions, numLeftStates - 1);
      if(rightFinal[i])
      {
        mergedNFA.setFinal(mergedState);
        mergedNFA.setFinalPrefIndex(mergedState,
            rightNFA.getFinalPrefIndex(i));
      }
    }
    return mergedNFA;
  }
  
  // Specify the aggregations to the operator
  static void setupAggrs(CorrName[] logCorrs,LogOptPatternStrm patternOp) 
  throws LogicalPlanException {
    
    int               numCorrs = logCorrs.length;
    int               numAggrs;
    BaseAggrFn[]      aggrFns;
    ArrayList<Expr[]> aggrParamExprs;
    ArrayList<ExprOrderBy[]> orderByExprs;
    
    for(int i = 0; i < numCorrs ; i++) {
      numAggrs       = logCorrs[i].getNumAggrs();
      aggrFns        = logCorrs[i].getAggrFns();
      aggrParamExprs = logCorrs[i].getAggrParamExprs();
      orderByExprs   = logCorrs[i].getOrderByExprs();
      
      for (int a=0; a < numAggrs; a++)
        patternOp.addAggr(aggrFns[a], aggrParamExprs.get(a), orderByExprs.get(a));
    }
  }
  
  //construct NFA for non event detection case
  //the new NFA can be imagined to be a CONCAT of old NFA and
  //durationSymbol
  static NFA getNonEventNFA(NFA leftNFA, int durationSymbolAlphIndex)
  {
    NFA        rightNFA;
    boolean[]  rightFinal;
    Transition trans;
    Transition trans1;
    
    rightNFA = new NFA(2);
    trans    = new Transition(durationSymbolAlphIndex, 1);
    trans1   = new Transition(NFA.FINAL, NFA.UNDEFINED_STATE);
    
    rightNFA.addTransition(0, trans);
    rightNFA.addTransition(1, trans1);
    
    rightNFA.setFinal(1);
    rightNFA.setFinalPrefIndex(1, 0);
   
    rightFinal = rightNFA.getFinalStates();
    return concatNFA(leftNFA, rightNFA, leftNFA.getNumStates(),
        rightNFA.getNumStates(), rightFinal[0], rightFinal);
  }

}
