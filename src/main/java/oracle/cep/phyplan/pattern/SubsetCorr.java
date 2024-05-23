/* $Header: pcbpel/cep/server/src/oracle/cep/phyplan/pattern/SubsetCorr.java /main/2 2008/11/07 23:08:44 udeshmuk Exp $ */

/* Copyright (c) 2008, Oracle and/or its affiliates. All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    udeshmuk    10/16/08 - support for xmlagg orderby in pattern.
    rkomurav    03/19/08 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/phyplan/pattern/SubsetCorr.java /main/2 2008/11/07 23:08:44 udeshmuk Exp $
 *  @author  rkomurav
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.phyplan.pattern;

import java.util.ArrayList;

import oracle.cep.common.BaseAggrFn;
import oracle.cep.phyplan.expr.Expr;
import oracle.cep.phyplan.expr.ExprOrderBy;

public class SubsetCorr extends CorrName
{
  public SubsetCorr(int bindPos, BaseAggrFn[] aggrFns,
    ArrayList<Expr[]> aggrParamExprs,
    ArrayList<ExprOrderBy[]> orderByExprs)
  {
    super(bindPos, aggrFns, aggrParamExprs, orderByExprs);
  }
}
