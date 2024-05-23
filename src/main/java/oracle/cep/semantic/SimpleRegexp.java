/* $Header: SimpleRegexp.java 16-jan-2008.07:25:31 rkomurav Exp $ */

/* Copyright (c) 2007, 2008, Oracle. All rights reserved.  */

/*
   DESCRIPTION
    Semantic representation for Simple Regular Expression for patterns

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    rkomurav    01/16/08 - add alphIndex corresponding to correlation name
    rkomurav    02/22/07 - replace correlation name with varId
    rkomurav    02/07/07 - Creation
 */

/**
 *  @version $Header: SimpleRegexp.java 16-jan-2008.07:25:31 rkomurav Exp $
 *  @author  rkomurav
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.semantic;

public class SimpleRegexp extends Regexp
{
  /** VarId */
  private int varId;
  
  /** alphabet index of the correlation in DFA */
  private int alphIndex;
  
  /** Constructor
   * @param varId
   */
  public SimpleRegexp(int varId, int alphIndex)
  {
    this.varId     = varId;
    this.alphIndex = alphIndex;
  }

  /**
   * @return the varId
   */
  public int getVarId()
  {
    return varId;
  }

  /**
   * @return the alphIndex
   */
  public int getAlphIndex() {
    return alphIndex;
  }

}

