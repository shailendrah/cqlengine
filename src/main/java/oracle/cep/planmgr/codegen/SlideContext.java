/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/planmgr/codegen/SlideContext.java /main/1 2012/06/07 03:24:37 sbishnoi Exp $ */

/* Copyright (c) 2012, Oracle and/or its affiliates. All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    sbishnoi    05/29/12 - Creation
 */

package oracle.cep.planmgr.codegen;

import oracle.cep.metadata.Query;
import oracle.cep.phyplan.PhyOpt;
import oracle.cep.service.ExecContext;

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/planmgr/codegen/SlideContext.java /main/1 2012/06/07 03:24:37 sbishnoi Exp $
 *  @author  sbishnoi
 *  @since   release specific (what release of product did this appear in)
 */

public class SlideContext extends CodeGenContext
{
  /** slide interval specified in nanoseconds */
  private long numSlideNanos;
  
  /**
   * Construct Slide Context
   * @param ec
   * @param query
   * @param phyopt
   */
  public SlideContext(ExecContext ec, Query query, PhyOpt phyopt)
  {
    super(ec, query, phyopt);
  }

  /**
   * @return the numSlideNanos
   */
  public long getNumSlideNanos()
  {
    return numSlideNanos;
  }

  /**
   * @param numSlideNanos the numSlideNanos to set
   */
  public void setNumSlideNanos(long numSlideNanos)
  {
    this.numSlideNanos = numSlideNanos;
  }
  
}
