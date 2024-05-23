/* $Header: pcbpel/cep/server/src/oracle/cep/planmgr/codegen/OrderByTopContext.java /main/1 2009/03/30 14:46:02 parujain Exp $ */

/* Copyright (c) 2009, Oracle and/or its affiliates. All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    parujain    03/20/09 - stateless server
    parujain    03/20/09 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/planmgr/codegen/OrderByTopContext.java /main/1 2009/03/30 14:46:02 parujain Exp $
 *  @author  parujain
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.planmgr.codegen;

import oracle.cep.execution.indexes.HashIndex;
import oracle.cep.execution.internals.TupleSpec;
import oracle.cep.metadata.Query;
import oracle.cep.phyplan.PhyOpt;
import oracle.cep.service.ExecContext;

public class OrderByTopContext extends CodeGenContext
{
  /** Output tuple specification */
  TupleSpec tupSpec;
	  
  /** Partition header tuple specification */
  TupleSpec partitionHdrTupSpec;
	  
  /** number of partition by attributes*/
  int numPartitionByAttrs;
	  
  /** position of PartitionByContext attribute*/
  int pByContextAttrPos;
	  
  /** index over PartitionHdrTuples */
  HashIndex partitionHdrIndex = null;
  
  public OrderByTopContext(ExecContext ec, Query query, PhyOpt phyopt)
  {
    super(ec, query, phyopt);
    this.tupSpec = null;
    this.partitionHdrTupSpec = null;
    this.numPartitionByAttrs = 0;
    this.partitionHdrIndex = null;
    this.pByContextAttrPos = 0;
  }
  
  public void setTupSpec(TupleSpec tup)
  {
    this.tupSpec = tup;
  }
  
  public TupleSpec getTupSpec()
  {
    return this.tupSpec;
  }
  
  public void setPartitionHdrTupSpec(TupleSpec tup)
  {
    this.partitionHdrTupSpec = tup;
  }
  
  public TupleSpec getPartitionHdrTupSpec()
  {
    return this.partitionHdrTupSpec;
  }
  
  public void setNumPartitionByAttrs(int num)
  {
    this.numPartitionByAttrs = num;
  }
  
  public int getNumPartitionByAttrs()
  {
    return this.numPartitionByAttrs;
  }
  
  public void setPByContextAttrPos(int pos)
  {
    this.pByContextAttrPos = pos;
  }
  
  public int getPByContextAttrPos()
  {
    return this.pByContextAttrPos;
  }
  
  public void setPartitionHdrIndex(HashIndex index)
  {
    this.partitionHdrIndex = index;
  }
  
  public HashIndex getPartitionHdrIndex()
  {
    return this.partitionHdrIndex;
  }
}
