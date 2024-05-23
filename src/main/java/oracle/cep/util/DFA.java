/* $Header: DFA.java 20-feb-2008.03:49:13 rkomurav Exp $ */

/* Copyright (c) 2007, 2008, Oracle. All rights reserved.  */

/*
   DESCRIPTION
    General DFA data structure

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    rkomurav    02/20/08 - fix the orderedPrint
    rkomurav    02/12/08 - cleanup
    rkomurav    01/31/08 - add some copy functions
    rkomurav    01/16/08 - support lazy quantifiers
    rkomurav    01/03/08 - add orderedtransitions
    rkomurav    04/02/07 - add equals
    rkomurav    03/23/07 - add comments
    rkomurav    03/13/07 - add numstates
    rkomurav    02/20/07 - Creation
 */

/**
 *  @version $Header: DFA.java 20-feb-2008.03:49:13 rkomurav Exp $
 *  @author  rkomurav
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.util;

public class DFA
{
  /** Undefined state */
  public static int UNDEFINED_STATE = -1;
  
  /** Final */
  public static int FINAL = -2;
  
  /** Undefined final index */
  public static int UNDEFINED_FINAL_INDEX = -3;

  /** Number of states */
  int       n;
  
  /** Number of letters */
  int       k;
  
  /** Transition table */
  int[][]   table;
  
  /** Alphabet preference table */
  int[][]   alphPrefs;
  
  /** Index of the final pref - applicable only for final states */
  int[]     finalPrefIndex;
  
  /** Number of possible transitions for each state */
  int[]     numTrans;
  
  /** Final State Array - if state i is true, its final */
  boolean[] finalStates;
  
  /** Ordered transitions pre computed and stored */
  int[][]   orderedTrans;
  
  /**
   * DFA is defined as (S,E,T,s,F)
   * S is the set of states(starting from 0 to n-1)
   * E is the set of alphabets(from 0 to k-1)
   * T is the transition function(represented here as a transition table)
   * s is the start state(always 0 is the start state)
   * F is the set of final states(it is represented by a boolean array)
   * @param n
   * @param k
   */
  public DFA(int n, int k)
  {
    this.n         = n;
    this.k         = k;
    table          = new int[n][k];
    
    //alphprefs n X k+1
    //additional column to accomodate FINAL in each state too
    alphPrefs      = new int[n][k+1];
    numTrans       = new int[n];
    finalStates    = new boolean[n];
    finalPrefIndex = new int[n];
    
    //initialize the transition table with Undefined state
    for(int i = 0; i < n; i++)
      for(int j = 0; j < k; j++)
        table[i][j] = UNDEFINED_STATE;
    
    //initialize final preference index array
    for(int i = 0; i < n; i++)
      finalPrefIndex[i] = UNDEFINED_FINAL_INDEX;
  }
  
  /** add a transition to the transition table */
  public void addTransition(int src, int symbol, int target)
  {
    table[src][symbol] = target;
  }
  
  /** print the transition table considering ordered transitions */
  public void orderedPrint()
  {
    int[] trans;
    System.out.println("Printing DFA with ordered transitions");
    for(int i = 0; i < n; i++)
    {
      trans = getOrderedTransitions(i);
      for(int j = 0; j < trans.length; j++)
      {
        System.out.println("State: " + i + " on " + alphPrefs[i][j] + " to " + table[i][alphPrefs[i][j]]);
      }
    }
  }
  
  /** dump of DFA */
  public void print()
  {
    System.out.println("Printing DFA");
    for(int i = 0; i < n; i++)
      for(int j = 0; j < k; j++)
      {
        System.out.println("State: " + i + " on " + j + " to " + table[i][j]);
      }
  }
  
  /** return the next transition */
  public int next(int src, int symbol)
  {
    return table[src][symbol];
  }
  
  //return next possible transitions in order
  //ordering is based on the order in which transitions
  //are added for a particular state
  public int[] getOrderedTransitions(int state)
  {
    return orderedTrans[state];
  }
  
  //store the ordered transitions for each state for ready
  //access during runtime
  public void storeOrderedTransitions()
  {
    int num;
    orderedTrans = new int[n][];
    for(int i = 0; i < n; i++)
    {
      num = finalStates[i] ? finalPrefIndex[i] : numTrans[i];
      orderedTrans[i] = new int[num];
      System.arraycopy(alphPrefs[i], 0, orderedTrans[i], 0, num);
    }
  }
  
  //add an alphabet preference to the current state
  public void addAlphPref(int state, int alphIndex)
  {
    alphPrefs[state][numTrans[state]] = alphIndex;
    numTrans[state]++;
  }
  
  //add an array of alphabet preferences to the current state
  public void addAlphPref(int state, int[] prefArr, int len)
  {
    System.arraycopy(prefArr, 0, alphPrefs[state], numTrans[state], len);
    numTrans[state] += len;
  }
  
  //copy transition and prefs tables along with 
  //number of transitions for each state
  public void copyTransPrefsTables(DFA other)
  {
    int     numStates;
    int[]   numOtherTrans;
    int[][] otherTransTable;
    int[][] otherPrefsTable;
    
    numStates       = other.getNumStates();
    otherTransTable = other.getTransitionTable();
    otherPrefsTable = other.getAlphPrefsTable();
    numOtherTrans   = other.getNumTransArr();
    
    for(int i = 0; i < numStates; i++)
    {
      System.arraycopy(otherTransTable[i], 0, table[i], 0, k);
      System.arraycopy(otherPrefsTable[i], 0, alphPrefs[i], 0, numOtherTrans[i]);
      numTrans[i] = numOtherTrans[i];
    }
  }
  
  //copy final states and final pref index from the given DFA
  public void copyFinals(DFA other)
  {
    int       numOtherStates;
    int[]     otherFinalPrefIndices;
    boolean[] otherFinals;
    
    numOtherStates        = other.getNumStates();
    otherFinals           = other.getFinalStates();
    otherFinalPrefIndices = other.getFinalPrefIndices();
    
    for(int i = 0; i < numOtherStates; i++)
    {
      System.arraycopy(otherFinals, 0, finalStates, 0, numOtherStates);
      System.arraycopy(otherFinalPrefIndices, 0, finalPrefIndex, 0, numOtherStates);
    }
  }
  
  //called only for a state which is final in the left DFA =>
  //it has a valid final state
  public void mergePrefs(int state, int[] prefs, int len, int finalIndex)
  {
    int curPrefsLength = numTrans[state];
    
    //assert that FINAL appears in the prefs list for this state
    boolean found = false;
    for(int i = 0; i < curPrefsLength; i++)
    {
      if(alphPrefs[state][i] == FINAL)
      {
        found = true;
        break;
      }
    }
    assert found;
    assert (curPrefsLength - 1) + len <= k+1;
    
    for(int i = curPrefsLength - 1; i > finalIndex; i--)
    {
      alphPrefs[state][i + (len - 1)] = alphPrefs[state][i];
    }
    
    for(int i = finalIndex,j = 0; j < len; i++,j++)
    {
      alphPrefs[state][i] = prefs[j];
    }
    numTrans[state] = numTrans[state] + len - 1;
  }
  
  /**
   * Set final preference index for the given state
   */
  public void setFinalPrefIndex(int state, int index)
  {
    finalPrefIndex[state] = index;
  }
  
  //get the final pref index for the given state
  public int getFinalPrefIndex(int state)
  {
    return finalPrefIndex[state];
  }
  
  //get the final pref index arr
  public int[] getFinalPrefIndices()
  {
    return finalPrefIndex;
  }
  
  //set final preference indices for given two states
  public void setFinalPrefIndices(int state1, int index1,
      int state2, int index2)
  {
    finalPrefIndex[state1] = index1;
    finalPrefIndex[state2] = index2;
  }
  
  /**
   * @param state state to be set as final
   */
  public void setFinal(int state)
  {
    this.finalStates[state] = true;
  }
  
  /**
   * @param state number
   * @return if given state is final or not
   */
  public boolean isFinal(int state)
  {
    return finalStates[state];
  }

  /**
   * @return the finalStates
   */
  public boolean[] getFinalStates()
  {
    return finalStates;
  }

  /**
   * @return the number of states
   */
  public int getNumStates()
  {
    return n;
  }
  
  public int getNumAlphabets()
  {
    return k;
  }
  
  /**
   * @return the table
   */
  public int[][] getTransitionTable()
  {
    return table;
  }

  /**
   * @param table the table to set
   */
  public void setTransitionTable(int[][] table)
  {
    this.table = table;
  }

  /**
   * @return the alphPrefs
   */
  public int[][] getAlphPrefsTable()
  {
    return alphPrefs;
  }
  
  //get alphabet prefs for the given state
  public int[] getAlphPrefs(int state)
  {
    return alphPrefs[state];
  }
   
  /**
   * @return the numTrans
   */
  public int[] getNumTransArr()
  {
    return numTrans;
  }
  
  //get number of possible transitions for the given state
  public int getNumTrans(int state)
  {
    return numTrans[state];
  }
  
  public boolean equals(DFA other)
  {
    if(other.getNumAlphabets() != k)
      return false;
    
    if(other.getNumStates() != n)
      return false;
    
    for(int i = 0; i < n; i++)
      for(int j = 0; j < k; j++)
      {
        if(table[i][j] != other.next(i, j))
          return false;
      }
    
    int[] otherOrderedArr;
    for(int i = 0; i < n; i++)
    {
      otherOrderedArr = other.getOrderedTransitions(i);
      if(orderedTrans[i].length != otherOrderedArr.length)
        return false;
      for(int j = 0; j < otherOrderedArr.length; j++)
      {
        if(orderedTrans[i][j] != otherOrderedArr[j])
          return false;
      }
    }
    
    for(int i = 0; i < n; i++)
    {
      if(finalStates[i] != other.isFinal(i))
        return false;
    }
    
    return true;
  }
}
