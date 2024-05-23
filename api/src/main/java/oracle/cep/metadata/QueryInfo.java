/* $Header: cep/wlevs_cql/modules/cqlengine/common/src/oracle/cep/metadata/QueryInfo.java /main/1 2012/03/05 15:31:20 alealves Exp $ */

/* Copyright (c) 2012, Oracle and/or its affiliates. All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    sbishnoi    03/01/12 - Creation
 */
package oracle.cep.metadata;

import java.util.Map;

import oracle.cep.common.Datatype;
import oracle.cep.common.OrderingConstraint;

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/common/src/oracle/cep/metadata/QueryInfo.java /main/1 2012/03/05 15:31:20 alealves Exp $
 *  @author  sbishnoi
 *  @since   release specific (what release of product did this appear in)
 */
public class QueryInfo
{
  /** A flag which shows the query status whether it is running or stopped */
  private boolean isRunning;

  /** Derived Ordering Constraints of a query after analyzing logical plan.*/
  private Map<String,OrderingConstraint> orderingConstraint;
  
  /** Map of output attributes with their data types*/
  private Map<String, Datatype> outputAttributes;

  /**
   * @return the isRunning
   */
  public boolean isRunning()
  {
    return isRunning;
  }

  /**
   * @param isRunning the isRunning to set
   */
  public void setRunning(boolean isRunning)
  {
    this.isRunning = isRunning;
  }

  public Map<String,OrderingConstraint> getOrderingConstraint()
  {
    return orderingConstraint;
  }

  public void setOrderingConstraint(Map<String,OrderingConstraint> orderingConstraint)
  {
    this.orderingConstraint = orderingConstraint;
  }
  
  public Map<String, Datatype> getOutputAttributes()
  {
    return outputAttributes;
  }

  public void setOutputAttributes(Map<String, Datatype> outputAttributes)
  {
    this.outputAttributes = outputAttributes;
  }
}
