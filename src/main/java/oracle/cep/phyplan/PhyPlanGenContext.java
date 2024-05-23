/* $Header: pcbpel/cep/server/src/oracle/cep/phyplan/PhyPlanGenContext.java /main/2 2008/10/24 15:50:17 hopark Exp $ */

/* Copyright (c) 2006, 2008, Oracle and/or its affiliates. All rights reserved.*/

/*
 DESCRIPTION
 <short description of component this file declares/defines>

 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>

 NOTES
 <other useful comments, qualifications, etc.>

 MODIFIED    (MM/DD/YY)
 hopark      10/09/08 - remove statics
 najain      06/05/06 - add Query 
 najain      04/02/06 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/phyplan/PhyPlanGenContext.java /main/2 2008/10/24 15:50:17 hopark Exp $
 *  @author  najain  
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.phyplan;

import oracle.cep.metadata.Query;
import oracle.cep.service.ExecContext;

/**
 * Context for generating physical operators from logical operators
 * 
 * @author najain
 */
public class PhyPlanGenContext
{
  ExecContext execContext;
  Query query;

  public PhyPlanGenContext(ExecContext ec, Query query)
  {
    this.query = query;
    this.execContext = ec;
  }

  public Query getQuery()
  {
    return query;
  }
  
  public ExecContext getExecContext()
  {
    return execContext;
  }
}
