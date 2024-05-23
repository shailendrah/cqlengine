/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/planmgr/codegen/BinJoinCommonContext.java /main/1 2009/10/29 21:18:23 udeshmuk Exp $ */

/* Copyright (c) 2009, Oracle and/or its affiliates. All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    udeshmuk    10/06/09 - superclass for binjoincontext and binjoinproject
                           context.
    udeshmuk    10/06/09 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/planmgr/codegen/BinJoinCommonContext.java /main/1 2009/10/29 21:18:23 udeshmuk Exp $
 *  @author  udeshmuk
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.planmgr.codegen;

import oracle.cep.common.OuterJoinType;
import oracle.cep.metadata.Query;
import oracle.cep.phyplan.PhyOpt;
import oracle.cep.service.ExecContext;

public class BinJoinCommonContext extends CodeGenContext
{
  public BinJoinCommonContext(ExecContext ec, Query query, PhyOpt phyopt)
  {
    super(ec, query, phyopt);
  }
  
  /** outer join type */
  protected OuterJoinType outerJoinType;
  
  /** a flag to check whether the join is ANSI syntax based outer join */
  protected boolean isANSIOuterJoin;
  
  public void setOuterJoinType(OuterJoinType type)
  {
    this.outerJoinType = type;
  }
  
  public OuterJoinType getOuterJoinType()
  {
    return this.outerJoinType;
  }

  /**
   * @return the isANSIOuterJoin
   */
  public boolean isANSIOuterJoin()
  {
    return isANSIOuterJoin;
  }

  /**
   * @param isANSIOuterJoin the isANSIOuterJoin to set
   */
  public void setANSIOuterJoin(boolean isANSIOuterJoin)
  {
    this.isANSIOuterJoin = isANSIOuterJoin;
  }
  
}