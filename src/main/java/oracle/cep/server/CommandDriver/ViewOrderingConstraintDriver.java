/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/server/CommandDriver/ViewOrderingConstraintDriver.java /main/1 2011/03/16 06:54:51 sborah Exp $ */

/* Copyright (c) 2011, Oracle and/or its affiliates. All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    sborah      03/15/11 - view ordering constraint
    sborah      03/15/11 - Creation
 */

/**
 *  @version $Header: ViewOrderingConstraintDriver.java 15-mar-2011.03:11:48 sborah   Exp $
 *  @author  sborah  
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.server.CommandDriver;

import oracle.cep.exceptions.CEPException;
import oracle.cep.parser.CEPParseTreeNode;
import oracle.cep.parser.CEPViewOrderingConstraintNode;
import oracle.cep.server.Command;
import oracle.cep.server.ICommandDriver;
import oracle.cep.service.ExecContext;

public class ViewOrderingConstraintDriver implements ICommandDriver
{
  public void execute(ExecContext ec, CEPParseTreeNode node, Command c, String schema) 
    throws CEPException
  {
    CEPViewOrderingConstraintNode n = (CEPViewOrderingConstraintNode) node;

    ec.getViewMgr().alterViewOrderingConstraint(ec, n, schema);
  }
}
