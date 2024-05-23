/* $Header: pcbpel/cep/server/src/oracle/cep/server/CommandDriver/ViewDropNodeDriver.java /main/5 2008/10/24 15:50:12 hopark Exp $ */

/* Copyright (c) 2007, 2008, Oracle and/or its affiliates. All rights reserved.*/

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    hopark      10/09/08 - remove statics
    hopark      10/07/08 - use execContext to remove statics
    parujain    10/01/08 - drop schema support
    parujain    09/15/08 - multiple schema support
    parujain    09/05/08 - support offset
    hopark      06/06/07 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/server/CommandDriver/ViewDropNodeDriver.java /main/5 2008/10/24 15:50:12 hopark Exp $
 *  @author  hopark  
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.server.CommandDriver;

import oracle.cep.exceptions.CEPException;
import oracle.cep.parser.CEPParseTreeNode;
import oracle.cep.parser.CEPViewDropNode;
import oracle.cep.server.Command;
import oracle.cep.server.ICommandDriver;
import oracle.cep.service.ExecContext;

public class ViewDropNodeDriver implements ICommandDriver
{
  public void execute(ExecContext ec, CEPParseTreeNode node, Command c, String schema) 
    throws CEPException
  {
    CEPViewDropNode n = (CEPViewDropNode) node;

    try{
      ec.getViewMgr().dropView(n.getName(), schema);
    }catch(CEPException ce)
    {
      ce.setStartOffset(n.getStartOffset());
      ce.setEndOffset(n.getEndOffset());
      throw ce;
    }
  }
}

