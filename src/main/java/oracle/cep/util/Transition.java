/* $Header: Transition.java 12-mar-2008.07:17:13 rkomurav Exp $ */

/* Copyright (c) 2008, Oracle. All rights reserved.  */

/*
   DESCRIPTION
    Transition details for the automaton

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    rkomurav    03/12/08 - change construcotr comments
    rkomurav    02/21/08 - add equals method
    rkomurav    02/11/08 - Creation
 */

/**
 *  @version $Header: Transition.java 12-mar-2008.07:17:13 rkomurav Exp $
 *  @author  rkomurav
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.util;

public class Transition
{
  /** alphabet index */
  private int alphId;
  
  /** destination state */
  private int destState;
  
  /**
   * constructor
   * @param alphId alphabet id
   * @param destState destination state
   */
  public Transition(int alphId, int destState)
  {
    this.alphId    = alphId;
    this.destState = destState;
  }

  /**
   * @return the alphId
   */
  public int getAlphId()
  {
    return alphId;
  }

  /**
   * @return the destState
   */
  public int getDestState()
  {
    return destState;
  }
  
  /**
   *  equals method
   */
  public boolean equals(Transition other)
  {
    if(other.getAlphId() == alphId && other.getDestState() == destState)
      return true;
    else
      return false;
  }
}
