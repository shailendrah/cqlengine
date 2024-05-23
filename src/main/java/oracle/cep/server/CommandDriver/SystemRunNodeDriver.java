/* $Header: pcbpel/cep/server/src/oracle/cep/server/CommandDriver/SystemRunNodeDriver.java /main/4 2008/10/24 15:50:11 hopark Exp $ */

/* Copyright (c) 2007, 2008, Oracle and/or its affiliates. All rights reserved.*/

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    hopark      10/07/08 - use execContext to remove statics
    parujain    09/18/08 - multiple schema support
    hopark      03/26/08 - server reorg
    hopark      06/06/07 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/server/CommandDriver/SystemRunNodeDriver.java /main/4 2008/10/24 15:50:11 hopark Exp $
 *  @author  hopark  
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.server.CommandDriver;

import oracle.cep.exceptions.CEPException;
import oracle.cep.parser.CEPParseTreeNode;
import oracle.cep.parser.CEPSystemRunNode;
import oracle.cep.server.Command;
import oracle.cep.server.ICommandDriver;
import oracle.cep.service.ExecContext;

public class SystemRunNodeDriver implements ICommandDriver
{
  public void execute(ExecContext ec, CEPParseTreeNode node, Command c, String schema) 
    throws CEPException
  {
    CEPSystemRunNode n = (CEPSystemRunNode) node;
    ec.runScheduler(n.getDuration(), n.isInitialized());
  }
}

