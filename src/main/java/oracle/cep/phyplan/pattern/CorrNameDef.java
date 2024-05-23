/* $Header: pcbpel/cep/server/src/oracle/cep/phyplan/pattern/CorrNameDef.java /main/8 2008/11/07 23:08:44 udeshmuk Exp $ */

/* Copyright (c) 2007, 2008, Oracle and/or its affiliates.
 All rights reserved. */

/*
   DESCRIPTION
    Physical level description of pattern correlation name definitions

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    udeshmuk    10/16/08 - support for xmlagg orderby in pattern.
    rkomurav    05/13/08 - rename isSimple
    rkomurav    03/19/08 - support subest by creating a hierarchy
    rkomurav    07/03/07 - uda
    anasrini    05/29/07 - types for aggregates
    anasrini    05/28/07 - aggregates support
    rkomurav    04/02/07 - add equals
    rkomurav    03/13/07 - add getters
    rkomurav    02/27/07 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/phyplan/pattern/CorrNameDef.java /main/8 2008/11/07 23:08:44 udeshmuk Exp $
 *  @author  rkomurav
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.phyplan.pattern;

import java.util.ArrayList;

import oracle.cep.common.BaseAggrFn;
import oracle.cep.phyplan.expr.Expr;
import oracle.cep.phyplan.expr.BoolExpr;
import oracle.cep.phyplan.expr.ExprOrderBy;

public class CorrNameDef extends CorrName
{
  /** Physical level boolean expression for correlation definition */
  BoolExpr bExpr;

  /** is the correlation name a SINGLETON */
  boolean  isSingleton;
  
  /** Subset ids - bindPos of the subsets which reference the correlation name */
  int[]    subsetPos;
  
  /**
   * Constructor
   * @param expr predicate defining the correlation name
   * @param isSimple true iff this correlation name is SINGLETON
   * @param bindPos position in the binding
   * @param aggrFns aggregate functions
   * @param aggrParamExprs aggregate fucntions' parameters
   * @param orderByExprs orderBy expresssions related to xmlagg
   * @param subsetPos bindPos of the subsets which reference this corrname
   */
  public CorrNameDef(BoolExpr expr, boolean isSingleton, int bindPos,
                     BaseAggrFn[] aggrFns, ArrayList<Expr[]> aggrParamExprs,
                     ArrayList<ExprOrderBy[]> orderByExprs, int[] subsetPos)
  {
    super(bindPos, aggrFns, aggrParamExprs, orderByExprs);
    bExpr            = expr;
    this.isSingleton = isSingleton;
    this.subsetPos   = subsetPos;
  }

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

  public boolean equals(CorrNameDef other)
  {
    BoolExpr otherExpr;
    int[]    otherSubsetPos;
    
    if(other == null)
      return false;
    
    otherExpr = other.getBExpr();
    if((bExpr == null && otherExpr != null) || (bExpr != null && otherExpr == null) )
      return false;
    
    if(bExpr != null && otherExpr != null)
    {
      if(!otherExpr.equals(bExpr))
        return false;
    }
    
    otherSubsetPos = other.getSubsetPos();
    if((subsetPos == null && otherSubsetPos != null) || 
        (subsetPos != null && otherSubsetPos == null) )
      return false;
    
    if(subsetPos != null && otherSubsetPos != null)
    {
      if(subsetPos.length != otherSubsetPos.length)
        return false;
      for(int i = 0; i < subsetPos.length; i++)
      {
        if(subsetPos[i] != otherSubsetPos[i])
          return false;
      }
    }
    
    if(other.isSingleton != isSingleton)
      return false;

    return super.equals(other);
  }
}

