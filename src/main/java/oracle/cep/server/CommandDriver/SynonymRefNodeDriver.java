/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/server/CommandDriver/SynonymRefNodeDriver.java /main/1 2010/01/06 20:33:12 parujain Exp $ */

/* Copyright (c) 2009, Oracle and/or its affiliates. All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    parujain    11/24/09 - synonym ref
    parujain    11/24/09 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/server/CommandDriver/SynonymRefNodeDriver.java /main/1 2010/01/06 20:33:12 parujain Exp $
 *  @author  parujain
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.server.CommandDriver;

import oracle.cep.exceptions.CEPException;
import oracle.cep.parser.CEPParseTreeNode;
import oracle.cep.parser.CEPSynonymRefNode;
import oracle.cep.server.Command;
import oracle.cep.server.ICommandDriver;
import oracle.cep.service.ExecContext;

public class SynonymRefNodeDriver implements ICommandDriver
{
  public void execute(ExecContext ec, CEPParseTreeNode node, Command c, String schema) 
    throws CEPException
  {
    CEPSynonymRefNode n = (CEPSynonymRefNode) node;

    ec.getSynonymMgr().dropSynonym(n.getSynonym(), schema);
                                
  }
}

