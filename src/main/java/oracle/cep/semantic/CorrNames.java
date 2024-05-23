/* $Header: CorrNames.java 27-mar-2008.01:47:06 rkomurav Exp $ */

/* Copyright (c) 2008, Oracle. All rights reserved.  */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    rkomurav    03/14/08 - Creation
 */

/**
 *  @version $Header: CorrNames.java 27-mar-2008.01:47:06 rkomurav Exp $
 *  @author  rkomurav
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.semantic;

import java.util.ArrayList;

public abstract class CorrNames
{
  /** ID of the corrleation name */
  int                 varId;
  
  /** Aggregations that reference this correlation name */
  ArrayList<AggrExpr> aggrs;
  
  /**
   * Constructor
   */
  protected CorrNames(int varId)
  {
    this.varId = varId;
    this.aggrs = new ArrayList<AggrExpr>();
  }
  
  /**
   * Get the varID of the correlation name
   * @return varId varid of the correlation name
   */
  public int getVarId()
  {
    return varId;
  }

  /**
   * Add an aggregate to the list of aggregates that reference this
   * correlation name
   * @param aggr aggregate that references this correlation name
   */
  void addAggr(AggrExpr aggr)
  {
    aggrs.add(aggr);
  }

  /**
   * Get the number of aggregations that reference this correlation name
   * @return the number of aggregations that reference this correlation name
   */
  public int getNumAggrs()
  {
    return aggrs.size();
  }

  /**
   * Get the aggregates whose param reference this correlation
   * @return the aggregates whose param reference this correlation
   */
  public AggrExpr[] getAggrs()
  {
    return aggrs.toArray(new AggrExpr[0]);
  }
}