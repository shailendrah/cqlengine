/* $Header: pcbpel/cep/server/src/oracle/cep/server/CommandDriver/QueryNodeDriver.java /main/5 2009/02/06 15:51:03 parujain Exp $ */

/* Copyright (c) 2007, 2009, Oracle and/or its affiliates.
All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    parujain    01/28/09 - transaction mgmt
    skmishra    12/26/08 - adding validate flag
    hopark      10/09/08 - remove statics
    hopark      10/07/08 - use execContext to remove statics
    parujain    09/15/08 - multiple schema support
    hopark      06/06/07 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/server/CommandDriver/QueryNodeDriver.java /main/5 2009/02/06 15:51:03 parujain Exp $
 *  @author  hopark  
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.server.CommandDriver;

import oracle.cep.exceptions.CEPException;
import oracle.cep.parser.CEPParseTreeNode;
import oracle.cep.parser.CEPQueryDefnNode;
import oracle.cep.server.Command;
import oracle.cep.server.ICommandDriver;
import oracle.cep.service.ExecContext;

public class QueryNodeDriver implements ICommandDriver
{
  public void execute(ExecContext ec, CEPParseTreeNode node, Command c, String schema) 
    throws CEPException
  {
    String qName = null;
    if (node instanceof CEPQueryDefnNode)
      qName = ((CEPQueryDefnNode)node).getName();

    c.setQueryId( ec.getQueryMgr().addNamedQuery(qName,
                          c.getCql(), schema, node));
  }
}

