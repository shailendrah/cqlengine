/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/planmgr/codegen/TableFunctionRelSourceFactory.java /main/1 2010/01/25 00:32:43 sbishnoi Exp $ */

/* Copyright (c) 2010, Oracle and/or its affiliates. All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    sbishnoi    01/01/10 - Creation
 */

package oracle.cep.planmgr.codegen;

import oracle.cep.exceptions.CEPException;
import oracle.cep.execution.operators.ExecOpt;
import oracle.cep.execution.operators.TableFunctionRelSource;

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/planmgr/codegen/TableFunctionRelSourceFactory.java /main/1 2010/01/25 00:32:43 sbishnoi Exp $
 *  @author  sbishnoi
 *  @since   release specific (what release of product did this appear in)
 */
public class TableFunctionRelSourceFactory extends ExecOptFactory
{
  public ExecOpt newExecOpt(CodeGenContext ctx) throws CEPException
  {
    return new TableFunctionRelSource(ctx.getExecContext());
  }

  public void setupExecOpt(CodeGenContext ctx) throws CEPException
  {}
}
