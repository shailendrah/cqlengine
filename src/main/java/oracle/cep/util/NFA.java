/* $Header: pcbpel/cep/server/src/oracle/cep/util/NFA.java /main/5 2008/10/01 07:09:22 udeshmuk Exp $ */

/* Copyright (c) 2008, Oracle and/or its affiliates. All rights reserved.*/

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    udeshmuk    09/24/08 - add a method to check if we have a special pattern.
    sbishnoi    07/14/08 - 
    udeshmuk    07/07/08 - support for lazy quantifiers over grouping
    udeshmuk    06/29/08 - support grouping over greedy quantifiers.
    rkomurav    03/12/08 - add getAllTransMethod
    rkomurav    02/29/08 - add add getalphabets method
    rkomurav    02/11/08 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/util/NFA.java /main/5 2008/10/01 07:09:22 udeshmuk Exp $
 *  @author  rkomurav
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.util;

import java.util.ArrayList;
import java.util.ListIterator;

public class NFA
{
  /** Undefined state */
  public static int                           UNDEFINED_STATE       = -1;
  
  /** Final */
  public static int                           FINAL                 = -2;
  
  /** Undefined final index */
  public static int                           UNDEFINED_FINAL_INDEX = -3;
  
  /** Number of states */
  private int                                 n;
  
  /** Number of alphabets */
  private int                                 k;
  
  /** Transitions for each state */
  private ArrayList<ArrayList<Transition>>    stateTransitions;
  
  /** Index of final transition - applicable only for final states */
  private int[]                               finalPrefIndex;
  
  /** Final states - true if ith state is true */
  private boolean[]                           finalStates;
  
  /** Ordered Transitions */
  private ArrayList<ArrayList<Transition>>    orderedTrans;
  
  /** All Transitions */
  private ArrayList<ArrayList<Transition>>    allTrans;
  
  /** local vars */
  private ArrayList<Transition>               transList;
  private ListIterator<ArrayList<Transition>> listOfTransIter;
  
  /**
   * Constructor and initializing transitions list for each state
   * @param numStates number of states in the NFA
   */
  public NFA(int numStates)
  {
    this.n           = numStates;
    stateTransitions = new ArrayList<ArrayList<Transition>>(n);
    listOfTransIter  = stateTransitions.listIterator();
    finalPrefIndex   = new int[n];
    finalStates      = new boolean[n];
    
    //initialize transition list for each state
    for(int i = 0; i < n; i++)
    {
      transList = new ArrayList<Transition>(1);
      stateTransitions.add(transList);
    }
    
    //initialize final preference index
    for(int i = 0; i < n; i++)
    {
      finalPrefIndex[i] = UNDEFINED_FINAL_INDEX;
    }
  }
  
  /** add transition for the given state */
  public void addTransition(int state, Transition trans)
  {
    transList = stateTransitions.get(state);
    transList.add(trans);
  }
  
  /** add two transitions for the given state */
  public void addTransition(int state, Transition trans1, Transition trans2)
  {
    transList = stateTransitions.get(state);
    transList.add(trans1);
    transList.add(trans2);
  }
  
  /** adds a list of transitions for the given state */
  public void addTransition(int state, ArrayList<Transition> list)
  {
    transList = stateTransitions.get(state);
    transList.addAll(list);
  }
  
  /** copy all the state transitions from other NFA to this */
  public void copyTransitions(NFA other)
  {
    int                                 i;
    Transition                          trans1;
    Transition                          trans2;
    ArrayList<Transition>               list;
    ArrayList<Transition>               temp;
    ListIterator<Transition>            iter;
    ListIterator<ArrayList<Transition>> listIter;
    
    listIter  = other.getListOfTransIter();
    
    i = 0;
    while(listIter.hasNext())
    {
      list = listIter.next();
      iter = list.listIterator();
      temp = new ArrayList<Transition>();
      while(iter.hasNext())
      {
        trans1 = iter.next();
        trans2 = new Transition(trans1.getAlphId(), trans1.getDestState());
        temp.add(trans2);
      }
      addTransition(i, temp);
      i++;
    }
  }
  
  /** copy the given transitions for the given state 
      incrementing the destination state by the given offset */
  public void copyTransitions(int state, ArrayList<Transition> trans,
      int offset)
  {
    copyTransitions(state, trans, offset, -1);
  }
  
  /** copy the given transitions for the given state 
      incrementing the destination state by the given offset 
      and skip the skipIndex */
  public void copyTransitions(int state, ArrayList<Transition> trans,
      int offset, int skipIndex)
  {
    int                      i;
    int                      alphId;
    Transition               trans1;
    Transition               trans2;
    ArrayList<Transition>    temp;
    ListIterator<Transition> iter;
    
    iter = trans.listIterator();
    temp = new ArrayList<Transition>();
    
    i = 0;
    while(iter.hasNext())
    {
      trans1 = iter.next();
      if(i == skipIndex)
      {
        i++;
        continue;
      }
      alphId = trans1.getAlphId();
      if(alphId == NFA.FINAL)
      {
        trans2 = new Transition(alphId, trans1.getDestState());
      }
      else
      {
        trans2 = new Transition(alphId, trans1.getDestState() + offset);
      }
      temp.add(trans2);
      i++;
    }
    addTransition(state, temp);
  }
  
  /** copy final states and final preference indices from other NFA to this */
  public void copyFinals(NFA other)
  {
    int       numStates;
    int[]     otherFinalPrefIndices;
    boolean[] otherFinals;
    
    numStates             = other.getNumStates();
    otherFinals           = other.getFinalStates();
    otherFinalPrefIndices = other.getFinalPrefIndices();
    
    for(int i = 0; i < numStates; i++)
    {
      System.arraycopy(otherFinals, 0, finalStates, 0, numStates);
      System.arraycopy(otherFinalPrefIndices, 0, finalPrefIndex, 0, numStates);
    }
  }
  
  /** merge transitions in trans with the transitions for the given state
   *  In the process, the final transition of the state is removed and all
   *  the transitions of trans are inserted at the point of final transition
   *  offset is required for calculating the merged state number of the 
   *  right NFA */
  public void mergeTrans(int state, ArrayList<Transition> trans, int finalIndex,
      int offset)
  {
    int                      alphId;
    Transition               trans1;
    Transition               trans2;
    ArrayList<Transition>    list;
    ListIterator<Transition> iter;
    
    transList = stateTransitions.get(state);
    transList.remove(finalIndex);
    
    list = new ArrayList<Transition>();
    iter = trans.listIterator();
    while(iter.hasNext())
    {
      trans1 = iter.next();
      alphId = trans1.getAlphId();
      if(alphId == NFA.FINAL)
        trans2 = new Transition(alphId, trans1.getDestState());
      else
        trans2 = new Transition(alphId, trans1.getDestState() + offset);
      list.add(trans2);
    }
    transList.addAll(finalIndex, list);
  }
  
  /**
   * Adds the start state transitions to a state, starting at a position where 'final'
   * appears in the list of transitions for that state. In case of duplicate transitions
   * upon addition one which appears earlier is retained and other is deleted.
   * If start state transitions also have 'final' then the original 'final' transition 
   * in the state is deleted.
   * finalprefindex for that state is set appropriately in the end.  
   * @param stateNum The state to which start state transitions need to be added
   * @param trans The list of start state transitions
   * @param finalIndex position of 'final' in stateNum's list of transitions
   */
  public void doGreedyProcessing(int stateNum, ArrayList<Transition> trans,
    int finalIndex)
  {
    int                      index;
    //transAdded - used to keep track of no of transitions in 'trans' that were added before 'final'
    int                      transAdded;
    //used to indicate that transAdded should not be incremented in subsequent additions to 'list'
    boolean                  finalFoundInTrans;
    Transition               trans1;
    Transition               trans2;
    //list is used to maintain the transitions from start state that need to be added in stateNum
    ArrayList<Transition>    list;
    ListIterator<Transition> iter;
    
    transAdded        = 0; 
    finalFoundInTrans = false;
    
    transList = stateTransitions.get(stateNum);
    list      = new ArrayList<Transition>();
    iter      = trans.listIterator();
    
    while(iter.hasNext())
    {
      trans1 = iter.next();
      trans2 = new Transition(trans1.getAlphId(), trans1.getDestState());
      index  = findTrans(trans1, transList);
      if(index > finalIndex)
      { //trans exists after 'final' so delete it and add it to the list of trans to be added
        list.add(trans2);
        transList.remove(index);
        if(!finalFoundInTrans)
          transAdded++;
      }
      else if(index == finalIndex)
      { //start state also has final..
        list.add(trans2);
        finalFoundInTrans = true;
      }
      else if(index == -1)
      { // transition does not exist in the list of transitions for this state. So add it.
        list.add(trans2);
        if(!finalFoundInTrans)
          transAdded++;
      }
      else
        continue; //don't add the trans in the list of transitions to be added
    }
    
    transList.addAll(finalIndex, list);
    setFinalPrefIndex(stateNum, finalIndex+transAdded);
    if(finalFoundInTrans)
      transList.remove(finalIndex+list.size()); //remove the original final
  }
  
  /**
   * Adds the start state transitions to a state, starting at a position immediately
   * after the position where 'final' appears in the list of transitions for that state.
   * In case of duplicate transitions upon addition one which appears earlier is retained 
   * and other is deleted.
   * If start state transitions also have 'final' then it is ignored. The original 'final' 
   * transition in the destination state is kept as is so 'finalprefindex' for the state 
   * does not change.
   * @param stateNum The state to which start state transitions need to be added
   * @param trans List of start state transitions
   * @param finalIndex position of 'final' transition in list of stateNum's transitions
   */
  public void doLazyProcessing(int stateNum, ArrayList<Transition> trans,
    int finalIndex)
  {
    int                      index;
    Transition               trans1;
    Transition               trans2;
    //list is used to maintain the transitions from start state that need to be added in stateNum
    ArrayList<Transition>    list;
    ListIterator<Transition> iter;
    
    transList = stateTransitions.get(stateNum);
    list      = new ArrayList<Transition>();
    iter      = trans.listIterator();
    
    while(iter.hasNext())
    {
      trans1 = iter.next();
      trans2 = new Transition(trans1.getAlphId(), trans1.getDestState());
      index  = findTrans(trans1, transList);
     
      if(index > finalIndex)
      { //trans exists after 'final' so delete it and add it to the list of trans to be added
        list.add(trans2);
        transList.remove(index);
      }
      else if(index == -1)
      { // transition does not exist in the list of transitions for this state. So add it.
        list.add(trans2);
      }
      else // index <= finalIndex so ignore
        continue; //don't add the trans in the list of transitions to be added
    }
    //add transitions immediately after the 'final' transition of that state
    transList.addAll(finalIndex+1, list);
  }
  
  private int findTrans(Transition trans, ArrayList<Transition> transList)
  {
    int                      idx;
    Transition               trans1;
    ListIterator<Transition> iter;
    
    idx = 0;
    iter = transList.listIterator();
    while(iter.hasNext())
    {
      trans1 = iter.next();
      if(trans.equals(trans1))
        return idx;
      idx++;
    }
    return -1;
  }

  /** store the ordered transitions for each state for
      ready access during runtime */
  public void storeOrderedTransitions()
  {
    int                      i;
    int                      num;
    int                      state;
    ArrayList<Transition>    transList;
    ArrayList<Transition>    orderedList;
    ListIterator<Transition> transIter;
    
    state           = 0;
    orderedTrans    = new ArrayList<ArrayList<Transition>>(n);
    listOfTransIter = stateTransitions.listIterator();
    
    while(listOfTransIter.hasNext())
    {
      transList = listOfTransIter.next();
      transIter = transList.listIterator();
      
      orderedList = new ArrayList<Transition>();
      i = 0;
      num = finalStates[state] ? finalPrefIndex[state] : transList.size();
      while(transIter.hasNext() && i < num)
      {
        orderedList.add(transIter.next());
        i++;
      }
      orderedTrans.add(orderedList);
      state++;
    }
  }
  
  /** store all the transitions except FINAL for each state for
      ready access during runtime */
  public void storeAllTransitions()
  {
    int                      i;
    int                      num;
    int                      state;
    Transition               trans;
    ArrayList<Transition>    transList;
    ArrayList<Transition>    allTransList;
    ListIterator<Transition> transIter;
    
    state           = 0;
    allTrans        = new ArrayList<ArrayList<Transition>>(n);
    listOfTransIter = stateTransitions.listIterator();
    
    while(listOfTransIter.hasNext())
    {
      transList = listOfTransIter.next();
      transIter = transList.listIterator();
      
      allTransList = new ArrayList<Transition>();
      i = 0;
      num = finalStates[state] ? finalPrefIndex[state] : -1;
      while(transIter.hasNext())
      {
        trans = transIter.next();
        if (i != num)
          allTransList.add(trans);
        i++;
      }
      allTrans.add(allTransList);
      state++;
    }
  }
  
  /** prints the transitions of the NFA
   *  Transitions are stored and printed 
   *  in the order in which they will be taken
   */
  public void print()
  {
    int                      i;
    Transition               transition;
    ListIterator<Transition> transIter;

    listOfTransIter = getListOfTransIter();

    i = 0;
    System.out.println("Printing NFA");
    while(listOfTransIter.hasNext())
    {
      transList = listOfTransIter.next();
      transIter = transList.listIterator();
      while(transIter.hasNext())
      {
        transition = transIter.next();
        System.out.println("State: " + i + " on " + transition.getAlphId()
            + " to " + transition.getDestState());
      }
      i++;
    }
  }
  
  /** prints the transitions of the NFA without the final state
   *  (from the orderedTrans)
   *  Transitions are stored and printed 
   *  in the order in which they will be taken
   */
  public void orderedPrint()
  {
    int                                 i;
    Transition                          transition;
    ListIterator<ArrayList<Transition>> listOfOrderedTransIter;
    ListIterator<Transition>            transIter;

    listOfOrderedTransIter = orderedTrans.listIterator();

    i = 0;
    System.out.println("Printing NFA");
    while(listOfOrderedTransIter.hasNext())
    {
      transList = listOfOrderedTransIter.next();
      transIter = transList.listIterator();
      while(transIter.hasNext())
      {
        transition = transIter.next();
        System.out.println("State: " + i + " on " + transition.getAlphId()
            + " to " + transition.getDestState());
      }
      i++;
    }
  }
  
  /** if the given state makes a transition to final 
   *  note: current implementation assumes that all final
   *  states make a transition only to a final state
   *  but this will change with grouping over patterns */
  public boolean hasTransitionToFinal(int state)
  {
    ArrayList<Transition> list;
    
    list = getOrderedTransitions(state);
    
    if(list.size() != 0)
      return true;
    else
      return false;
  }
  
  
  /** equals method */
  public boolean equals(NFA other)
  {
    ListIterator<ArrayList<Transition>> listOfListsIter1;
    ListIterator<ArrayList<Transition>> listOfListsIter2;
    ListIterator<Transition> iter1;
    ListIterator<Transition> iter2;
    ArrayList<Transition> list1;
    ArrayList<Transition> list2;
    
    if(other.getNumStates() != n)
      return false;
    
    listOfListsIter1 = getListOfOrderTransIter();
    listOfListsIter2 = other.getListOfOrderTransIter();
    
    while(listOfListsIter1.hasNext())
    {
      list1 = listOfListsIter1.next();
      list2 = listOfListsIter2.next();
      
      if(list1.size() != list2.size())
        return false;
      
      iter1 = list1.listIterator();
      iter2 = list2.listIterator();
      while(iter1.hasNext())
      {
        if(!(iter1.next().equals(iter2.next())))
          return false;
      }
    }
    
    for(int i = 0; i < n; i++)
    {
      if(finalStates[i] != other.isFinal(i))
        return false;
    }
    
    for(int i = 0; i < n; i++)
    {
      if(finalPrefIndex[i] != other.getFinalPrefIndex(i))
        return false;
    }
    
    return true;
  }

  /** return the number of states */
  public int getNumStates()
  {
    return n;
  }
  
  /** returns the iterator to the list of transitions in the NFA */
  public ListIterator<ArrayList<Transition>> getListOfTransIter()
  {
    return stateTransitions.listIterator();
  }
  
  /** returns the iterator to the list of ordered transitions in the NFA 
   *  note: orderedTrans is not available unless the NFA is build completely */
  public ListIterator<ArrayList<Transition>> getListOfOrderTransIter()
  {
    return orderedTrans.listIterator();
  }
  
  /** return transitions for the given state */
  public ArrayList<Transition> getTransitions(int state)
  {
    return stateTransitions.get(state);
  }
  
  /** return ordered transitions for the given state */
  public ArrayList<Transition> getOrderedTransitions(int state)
  {
    return orderedTrans.get(state);
  }
  
  /** return all the transitions for the given state except FINAL transition
   *  while getTransitions method returns transitions including FINAL
   */
  public ArrayList<Transition> getAllTransitions(int state)
  {
    return allTrans.get(state);
  }
  
  /** return the number of transitions for the given state */
  public int getNumTrans(int state)
  {
    return getTransitions(state).size();
  }
  
  /** return the number of alphabets */
  public int getNumAlphabets()
  {
    return this.k;
  }
  
  /** return the number of alphabets */
  public void setNumAlphabets(int numAlphs)
  {
    this.k = numAlphs;
  }
  
  /** return if the given state is final */
  public boolean isFinal(int state)
  {
    return finalStates[state];
  }
  
  /** set the given state as final */
  public void setFinal(int state)
  {
    finalStates[state] = true;
  }
  
  /** get the final states */
  public boolean[] getFinalStates()
  {
    return finalStates;
  }

  /** set final preference index for the given state */
  public void setFinalPrefIndex(int state, int index)
  {
    finalPrefIndex[state] = index;
  }
  
  /** get final preference index for the given state */
  public int getFinalPrefIndex(int state)
  {
    return finalPrefIndex[state];
  }
  
  /** get the final pref index arr */
  public int[] getFinalPrefIndices()
  {
    return finalPrefIndex;
  }
  
  /** set final preference indices for given two states */
  public void setFinalPrefIndices(int state1, int index1,
      int state2, int index2)
  {
    finalPrefIndex[state1] = index1;
    finalPrefIndex[state2] = index2;
  }

  /** Returns true if the pattern is a special pattern i.e. a single corr var. 
      This is used in optimization */
  public boolean isSpecialPattern()
  {
    if((n==2) && (k==1) && (!finalStates[0]))
    {
      assert finalStates[1]==true;
      transList = stateTransitions.get(1);
      if(transList.size()==1) 
        return true;
    }
    return false;
  }
}
