/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/planmgr/codegen/PositionHelper.java /main/2 2011/06/02 13:25:39 mjames Exp $ */

/* Copyright (c) 2007, 2011, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    parujain    07/02/07 - get Position of expr in input tuplespec
    parujain    07/02/07 - Creation
 */

/**
 *  @version $Header: PositionHelper.java 02-jul-2007.10:44:30 parujain Exp $
 *  @author  parujain
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.planmgr.codegen;

import oracle.cep.extensibility.expr.ExprKind;
import oracle.cep.phyplan.expr.Expr;
import oracle.cep.phyplan.expr.ExprAttr;
import oracle.cep.phyplan.attr.Attr;

public class PositionHelper{

 // Today we are supporting only AttrExpr
 // Later when we will reuse the expression then we will need to expand
 // this. Ex: if a+b is present in both order by or group aggr and project,
 // we might want to reuse the output a+b in the output schema
 // Then we will need to find out the position of the expression in the
 // output schema of the input operator. 
 static int getExprPos(Expr expr)
 {
   ExprKind kind;
   Attr     attr;
   kind = expr.getKind();
   assert kind == ExprKind.ATTR_REF;
   attr = ((ExprAttr) expr).getAValue();
   return attr.getPos();

 }
}
