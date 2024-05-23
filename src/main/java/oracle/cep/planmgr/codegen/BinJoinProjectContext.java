/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/planmgr/codegen/BinJoinProjectContext.java /main/4 2009/10/29 21:18:23 udeshmuk Exp $ */

/* Copyright (c) 2009, Oracle and/or its affiliates. All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    udeshmuk    10/06/09 - extend binjoincommoncontext.
    sbishnoi    05/27/09 - ansi syntax support for outer join
    sborah      04/01/09 - removing unreferenced variables .Bug : 8399697
    parujain    03/19/09 - BinJoin context
    parujain    03/19/09 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/planmgr/codegen/BinJoinProjectContext.java /main/4 2009/10/29 21:18:23 udeshmuk Exp $
 *  @author  parujain
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.planmgr.codegen;

import oracle.cep.metadata.Query;
import oracle.cep.phyplan.PhyOpt;
import oracle.cep.service.ExecContext;

public class BinJoinProjectContext extends BinJoinCommonContext
{
  public BinJoinProjectContext(ExecContext ec, Query query, PhyOpt phyopt)
  {
     super(ec, query, phyopt);
     outerJoinType = null;
  }
 
}
