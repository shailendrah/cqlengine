/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/server/CommandDriver/ExternalRelationDriver.java /main/1 2010/07/19 02:36:41 sborah Exp $ */

/* Copyright (c) 2010, Oracle and/or its affiliates. All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    sborah      06/23/10 - set max rows in external relations
    sborah      06/23/10 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/server/CommandDriver/ExternalRelationDriver.java /main/1 2010/07/19 02:36:41 sborah Exp $
 *  @author  sborah  
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.server.CommandDriver;

import oracle.cep.exceptions.CEPException;
import oracle.cep.parser.CEPExternalRelationNode;
import oracle.cep.parser.CEPParseTreeNode;
import oracle.cep.server.Command;
import oracle.cep.server.ICommandDriver;
import oracle.cep.service.ExecContext;

public class ExternalRelationDriver implements ICommandDriver
{
  public void execute(ExecContext ec, CEPParseTreeNode node, Command c, 
                      String schema) throws CEPException
  {
    CEPExternalRelationNode n = (CEPExternalRelationNode) node;
    
    // Tablemanager's method to update the max rows value for external relations
    try 
    {
      ec.getTableMgr().alterMaxRowsForExternalRelation(n.getName(),
                                                       schema, n.getMaxRows());
    }
    catch(CEPException e)
    {
      e.setStartOffset(n.getStartOffset());
      e.setEndOffset(n.getEndOffset());
      throw e;
    }
  }
}
