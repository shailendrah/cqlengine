/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/planmgr/codegen/PartitionWindowContext.java /main/1 2011/12/15 01:06:31 sbishnoi Exp $ */

/* Copyright (c) 2011, Oracle and/or its affiliates. All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    sbishnoi    12/08/11 - Creation
 */

/**
 *  @version $Header: PartitionWindowContext.java 08-dec-2011.10:45:41 sbishnoi Exp $
 *  @author  sbishnoi
 *  @since   release specific (what release of product did this appear in)
 */


package oracle.cep.planmgr.codegen;

import oracle.cep.execution.internals.TupleSpec;
import oracle.cep.metadata.Query;
import oracle.cep.phyplan.PhyOpt;
import oracle.cep.service.ExecContext;

public class PartitionWindowContext extends CodeGenContext
{
  private boolean isVariableDurationWindow = false;
  
  /** Output tuple specification */
  TupleSpec tupSpec;
  
  /** a dummy flag, presently we will not handle null values and will 
   * throw exception */
  boolean isNullFirst;
  
  public boolean isNullFirst()
  {
    return isNullFirst;
  }

  public boolean isAscending()
  {
    return isAscending;
  }

  /** a flag to specify the order in which execution operator will arrange
   * the output tuples in priority queue */
  boolean isAscending;

  public TupleSpec getTupSpec()
  {
    return tupSpec;
  }

  public void setTupSpec(TupleSpec tupSpec)
  {
    this.tupSpec = tupSpec;
  }

  public boolean isVariableDurationWindow()
  {
    return isVariableDurationWindow;
  }

  public void setVariableDurationWindow(boolean isVariableDurationWindow)
  {
    this.isVariableDurationWindow = isVariableDurationWindow;
  }

  public PartitionWindowContext(ExecContext ec, Query query, PhyOpt phyopt)
  {
    super(ec, query, phyopt);    
    isAscending = true;
    isNullFirst = true;
  }
}
