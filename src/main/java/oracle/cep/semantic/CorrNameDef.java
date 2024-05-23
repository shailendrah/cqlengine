/* $Header: CorrNameDef.java 27-mar-2008.01:47:07 rkomurav Exp $ */

/* Copyright (c) 2007, 2008, Oracle. All rights reserved.  */

/*
   DESCRIPTION
    Semantic representation of correlation definition

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    rkomurav    03/14/08 - restructure to suport aggrs for default correlation
                           names, which are not defined in define clause
    anasrini    05/27/07 - addAggr support
    anasrini    05/23/07 - id based constructor
    rkomurav    02/07/07 - Creation
 */

/**
 *  @version $Header: CorrNameDef.java 27-mar-2008.01:47:07 rkomurav Exp $
 *  @author  rkomurav
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.semantic;

import java.util.ArrayList;
import java.util.ListIterator;

public class CorrNameDef extends CorrNames
{
  /** Semantic level Boolean Expression of Correlation Definition */
  BExpr              expr;
  
  /** Subset ids - varIds of the subsets which reference the correlatin name */
  ArrayList<Integer> subsetIds;

  /** Constructor */
  protected CorrNameDef(int varId, BExpr expr) {
    super(varId);
    this.expr      = expr;
    this.subsetIds = new ArrayList<Integer>();
  }

  CorrNameDef(int varId) {
    this(varId, null);
  }

  /** get the boolean expression of correlation definition */
  public BExpr getExpr() {
    return expr;
  }

  /** set the boolean expression of correlation definition */
  void setExpr(BExpr expr) {
    this.expr = expr;
  }
  
  /** add subset ID to the list of subsetIds which refer this correlatin name */
  public void addSubsetId(int id)
  {
    subsetIds.add(id);
  }
  
  public int[] getSubsetIds()
  {
    int                   i;
    int                   length;
    int[]                 subsetArr;
    ListIterator<Integer> iter;
    
    length    = subsetIds.size();
    subsetArr = new int[length];
    iter      = subsetIds.listIterator();
    
    i = 0;
    while(iter.hasNext())
    {
      subsetArr[i] = iter.next().intValue();
      i++;
    }
    
    return subsetArr;
  }
}
