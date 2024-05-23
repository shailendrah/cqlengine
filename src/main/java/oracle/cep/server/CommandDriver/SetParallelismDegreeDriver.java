/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/server/CommandDriver/SetParallelismDegreeDriver.java /main/1 2011/03/18 04:41:41 sborah Exp $ */

/* Copyright (c) 2011, Oracle and/or its affiliates. All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    sborah      03/17/11 - set parallelism degree driver
    sborah      03/17/11 - Creation
 */

/**
 *  @version $Header: SetParallelismDegreeDriver.java 17-mar-2011.08:01:37 sborah   Exp $
 *  @author  sborah  
 *  @since   release specific (what release of product did this appear in)
 */


package oracle.cep.server.CommandDriver;

import oracle.cep.exceptions.CEPException;
import oracle.cep.parser.CEPParseTreeNode;
import oracle.cep.parser.CEPSetParallelismDegreeNode;
import oracle.cep.server.Command;
import oracle.cep.server.ICommandDriver;
import oracle.cep.service.ExecContext;

public class SetParallelismDegreeDriver implements ICommandDriver
{
  public void execute(ExecContext ec, CEPParseTreeNode node, Command c, String schema) 
    throws CEPException
  {
    assert node instanceof CEPSetParallelismDegreeNode;
    CEPSetParallelismDegreeNode n = (CEPSetParallelismDegreeNode) node;

    ec.getTableMgr().setParallelismDegree(n, schema);
  }
}
