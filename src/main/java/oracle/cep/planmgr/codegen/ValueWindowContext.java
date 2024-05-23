/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/planmgr/codegen/ValueWindowContext.java /main/2 2011/10/09 22:22:30 udeshmuk Exp $ */

/* Copyright (c) 2011, Oracle and/or its affiliates. All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    sbishnoi    10/03/11 - adding window type
    sbishnoi    02/28/11 - Creation
 */

package oracle.cep.planmgr.codegen;

import oracle.cep.common.Datatype;
import oracle.cep.execution.internals.TupleSpec;
import oracle.cep.metadata.Query;
import oracle.cep.phyplan.PhyOpt;
import oracle.cep.phyplan.window.PhyValueWinSpec;
import oracle.cep.service.ExecContext;

/**
 *  @version $Header: ValueWindowContext.java 28-feb-2011.01:29:29 sbishnoi Exp $
 *  @author  sbishnoi
 *  @since   release specific (what release of product did this appear in)
 */
public class ValueWindowContext extends CodeGenContext
{
  /** a flag to check if the window will be applied on relation */
  boolean isWindowOverRelation;
  
  /** a dummy flag, presently we will not handle null values and will 
   * throw exception */
  boolean isNullFirst;
  
  /** a flag to specify the order in which execution operator will arrange
   * the output tuples in priority queue */
  boolean isAscending;
  
  /** Output tuple specification */
  TupleSpec tupSpec;
  
  /** value window specification */
  PhyValueWinSpec vwspec;
  
  /** value window column datatype */
  Datatype colType;

  
  public ValueWindowContext(ExecContext ec, Query query, PhyOpt phyopt)
  {
    super(ec, query, phyopt);
    isAscending = true;
    isNullFirst = true;
  }
  
  public boolean isWindowOverRelation()
  {
    return isWindowOverRelation;
  }

  public void setWindowOverRelation(boolean isWindowOverRelation)
  {
    this.isWindowOverRelation = isWindowOverRelation;
  }

  public boolean isNullFirst()
  {
    return isNullFirst;
  }

  public boolean isAscending()
  {
    return isAscending;
  }

  public void setTupSpec(TupleSpec tupSpec)
  {   
    this.tupSpec = tupSpec; 
  }
  
  public TupleSpec getTupSpec()
  {
    return tupSpec;
  }

  /**
   * @return the colType
   */
  public Datatype getColType()
  {
    return colType;
  }

  /**
   * @param colType the colType to set
   */
  public void setColType(Datatype colType)
  {
    this.colType = colType;
  }

  /**
   * @return the vwspec
   */
  public PhyValueWinSpec getValueWindowSpec()
  {
    return vwspec;
  }

  /**
   * @param vwspec the vwspec to set
   */
  public void setValueWindowSpec(PhyValueWinSpec vwspec)
  {
    this.vwspec = vwspec;
  }
  
}
