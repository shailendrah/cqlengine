/* $Header: pcbpel/cep/server/src/oracle/cep/planmgr/codegen/DistinctContext.java /main/1 2009/03/30 14:46:02 parujain Exp $ */

/* Copyright (c) 2009, Oracle and/or its affiliates. All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    parujain    03/19/09 - BinJoin context
    parujain    03/19/09 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/planmgr/codegen/DistinctContext.java /main/1 2009/03/30 14:46:02 parujain Exp $
 *  @author  parujain
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.planmgr.codegen;

import oracle.cep.execution.internals.TupleSpec;
import oracle.cep.metadata.Query;
import oracle.cep.phyplan.PhyOpt;
import oracle.cep.service.ExecContext;

public class DistinctContext extends CodeGenContext
{
  TupleSpec tupSpec;
  TupleSpec oldSpec;
  int       countCol;
	
  public DistinctContext(ExecContext ec, Query query, PhyOpt phyopt)
  {
    super(ec, query, phyopt);
    this.tupSpec = null;
    this.oldSpec = null;
    this.countCol = 0;
  }
  
  public void setTupSpec(TupleSpec tup)
  {
    this.tupSpec = tup;
  }
  
  public TupleSpec getTupSpec()
  {
    return this.tupSpec;
  }
  
  public void setOldSpec(TupleSpec old)
  {
    this.oldSpec = old;
  }
  
  public TupleSpec getOldSpec()
  {
    return this.oldSpec;
  }
  
  public void setCountCol(int col)
  {
    this.countCol = col;
  }
  
  public int getCountCol()
  {
    return this.countCol;
  }
}
