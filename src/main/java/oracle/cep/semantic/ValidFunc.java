/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/semantic/ValidFunc.java /main/4 2009/08/31 10:57:07 alealves Exp $ */

/* Copyright (c) 2006, 2009, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    udeshmuk    03/12/08 - remove returntype in staticmetadata.
    udeshmuk    02/14/08 - support for all nulls in function arguments.
    dlenkov     11/09/06 - fixed
    parujain    11/06/06 - remove tabs
    parujain    10/12/06 - validated Expressions
    parujain    10/12/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/semantic/ValidFunc.java /main/4 2009/08/31 10:57:07 alealves Exp $
 *  @author  parujain
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.semantic;

import oracle.cep.extensibility.functions.IUserFunctionMetadata;

public class ValidFunc{

  IUserFunctionMetadata fn;
  Expr[] validExprs;
  boolean isResultNull;

  public ValidFunc(IUserFunctionMetadata fn, Expr[] exprs) {
    this.fn = fn;
    this.validExprs = exprs;
    this.isResultNull = false;
  }
  
  public ValidFunc(IUserFunctionMetadata fn, Expr[] exprs, boolean isResultNull)
  {
    this.fn = fn;
    this.validExprs = exprs;
    this.isResultNull = isResultNull;
  }

  public IUserFunctionMetadata getFn() {
    return fn;
  }

  public Expr[] getExprs() {
    return validExprs;
  }
    
  public boolean getIsResultNull()
  {
    return this.isResultNull;
  }
}
