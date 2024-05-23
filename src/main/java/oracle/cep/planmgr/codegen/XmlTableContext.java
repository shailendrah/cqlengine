/* $Header: pcbpel/cep/server/src/oracle/cep/planmgr/codegen/XmlTableContext.java /main/1 2009/03/30 14:46:03 parujain Exp $ */

/* Copyright (c) 2009, Oracle and/or its affiliates. All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    parujain    03/23/09 - XmlTable context
    parujain    03/23/09 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/planmgr/codegen/XmlTableContext.java /main/1 2009/03/30 14:46:03 parujain Exp $
 *  @author  parujain
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.planmgr.codegen;

import oracle.cep.metadata.Query;
import oracle.cep.phyplan.PhyOpt;
import oracle.cep.service.ExecContext;

public class XmlTableContext extends CodeGenContext
{
  int resSetCol;
  
  public XmlTableContext (ExecContext ec, Query query, PhyOpt phyopt)
  {
    super(ec, query, phyopt);
    resSetCol = 0;
  }
  
  public void setResSetCol(int col)
  { 
    this.resSetCol = col;
  }
  
  public int getResSetCol()
  {
    return this.resSetCol;
  }
}
