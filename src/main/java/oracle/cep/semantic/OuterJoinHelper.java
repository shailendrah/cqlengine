/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/semantic/OuterJoinHelper.java /main/6 2014/10/14 06:35:33 udeshmuk Exp $ */

/* Copyright (c) 2009, 2014, Oracle and/or its affiliates. 
All rights reserved.*/

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    udeshmuk    09/18/14 - set partitioned stream context
    vikshukl    08/01/12 - archived dimension
    vikshukl    07/13/11 - subquery support
    udeshmuk    03/22/11 - propagate isArchived flag
    sbishnoi    09/25/09 - support for table function
    sbishnoi    05/20/09 - Creation
 */

package oracle.cep.semantic;

import oracle.cep.common.SplRangeType;
import oracle.cep.exceptions.CEPException;
import oracle.cep.exceptions.SemanticError;

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/semantic/OuterJoinHelper.java /main/6 2014/10/14 06:35:33 udeshmuk Exp $
 *  @author  sbishnoi
 *  @since   release specific (what release of product did this appear in)
 */
public class OuterJoinHelper
{

  public static void handleException(CEPException e, SemContext ctx)
    throws CEPException
  {
    if (e.getErrorCode() == SemanticError.NOT_A_RELATION_ERROR) 
    {
      // convert into a relation by adding the UNBOUNDED window
      // operator
      WindowSpec winspec = new TimeWindowSpec(SplRangeType.UNBOUNDED);
      ctx.setWindowSpec(winspec);
    }
    else
      throw e;
  }
  
  public static void updateFromClauseTables(SemContext ctx)
    throws CEPException
  {
    assert ctx.getSemQuery() instanceof SFWQuery;
    ((SFWQuery)ctx.getSemQuery()).addRelation(
        ctx.getRelationSpec(), 
        ctx.getSubquerySpec(),
        ctx.getWindowSpec(), 
        ctx.getPatternSpec(), 
        ctx.getXmlTableSpec(),
        ctx.getDerivedTimeSpec(),
        ctx.getTableFunctionSpec(),
        ctx.isArchived(),
        ctx.isDimension(),
        ctx.isPartnStream());  
   
    ctx.setIsArchived(false); //reset
    ctx.setIsDimension(false);
    ctx.setIsPartnStream(false);
  }
}
