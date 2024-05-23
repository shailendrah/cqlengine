/* $Header: CorrNameDef.java 13-may-2008.22:36:13 rkomurav Exp $ */

/* Copyright (c) 2007, 2008, Oracle. All rights reserved.  */

/*
   DESCRIPTION
    logical level description of correlation name definition

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    rkomurav    05/13/08 - rename issimple to issingleton
    rkomurav    04/22/08 - make default isSimple or isSingleton to false
    rkomurav    03/18/08 - add hierarchy to corrnamedef to asupport subsets
    rkomurav    07/03/07 - uda support
    sbishnoi    06/08/07 - support for Multi-arg UDAs
    anasrini    05/26/07 - addAggr
    anasrini    05/25/07 - inline view support
    rkomurav    03/06/07 - add bindpos and isSimple flag
    rkomurav    02/17/07 - Creation
 */

/**
 *  @version $Header: CorrNameDef.java 13-may-2008.22:36:13 rkomurav Exp $
 *  @author  rkomurav
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.logplan.pattern;

import oracle.cep.logplan.expr.BoolExpr;

public class CorrNameDef extends CorrName
{
  /** Logical level boolean expression for correlation definition */
  BoolExpr bExpr;
  
  /** is the correlation name simple */
  boolean  isSingleton;
  
  /** Subset ids - bindPos of the subsets which reference the correlatin name */
  int[]    subsetPos;
  
  /**
   * @param varId Id of correlation name
   * @param expr boolean expression for correlation definition
   */
  public CorrNameDef(int varId, BoolExpr expr)
  {
    super(varId);
    this.bExpr = expr;

    //this is needed for the case where a corr is not in the pattern clause
    //and is present in the define list
    this.isSingleton = false;
  }

  // Setters

  /**
   * @param isSimple the isSimple to set
   */
  public void setSingleton(boolean isSingleton)
  {
    this.isSingleton = isSingleton;
  }
  
  /**
   * @param subsetPos the subsetPos to set
   */
  public void setSubsetPos(int[] subsetPos)
  {
    this.subsetPos = subsetPos;
  }


  // Getters

  /**
   * @return the bExpr
   */
  public BoolExpr getBExpr()
  {
    return bExpr;
  }

  /**
   * @return the isSimple
   */
  public boolean isSingleton()
  {
    return isSingleton;
  }

  /**
   * @return the subsetPos
   */
  public int[] getSubsetPos()
  {
    return subsetPos;
  } 
}

