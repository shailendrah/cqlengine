/* $Header: pcbpel/cep/server/src/oracle/cep/planmgr/codegen/BinStreamJoinContext.java /main/2 2009/04/01 12:08:16 sborah Exp $ */

/* Copyright (c) 2009, Oracle and/or its affiliates. All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    sborah      04/01/09 - removing unreferenced variables .Bug : 8399697
    parujain    03/19/09 - BinJoin context
    parujain    03/19/09 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/planmgr/codegen/BinStreamJoinContext.java /main/2 2009/04/01 12:08:16 sborah Exp $
 *  @author  parujain
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.planmgr.codegen;

import oracle.cep.metadata.Query;
import oracle.cep.phyplan.PhyOpt;
import oracle.cep.service.ExecContext;

public class BinStreamJoinContext extends CodeGenContext
{
  // Outer input layout
  int numLeftCols;
  
  public BinStreamJoinContext(ExecContext ec, Query query, PhyOpt phyopt)
  {
     super(ec, query, phyopt);
     numLeftCols = 0;   
  }
  
  public void setNumLeftCols(int left)
  {
    this.numLeftCols = left;
  }
  
  public int getNumLeftCols()
  {
    return this.numLeftCols;
  }
  
}