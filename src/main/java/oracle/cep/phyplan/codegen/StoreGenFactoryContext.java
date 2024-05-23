/* $Header: pcbpel/cep/server/src/oracle/cep/phyplan/codegen/StoreGenFactoryContext.java /main/2 2008/10/24 15:50:17 hopark Exp $ */

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
    anasrini    08/03/06 - bReqStore not required
    najain      03/20/06 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/phyplan/codegen/StoreGenFactoryContext.java /main/2 2008/10/24 15:50:17 hopark Exp $
 *  @author  najain  
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.phyplan.codegen;

import oracle.cep.phyplan.PhyOpt;
import oracle.cep.service.ExecContext;

/**
 * Context for instantiating physical stores from physical operators
 * 
 * @author najain
 */
public class StoreGenFactoryContext {
  ExecContext execContext;
  PhyOpt    phyPlan;

  public PhyOpt getPhyPlan() {
    return phyPlan;
  }

  public StoreGenFactoryContext(ExecContext ec, PhyOpt phyPlan) {
    this.execContext = ec;
    this.phyPlan = phyPlan;
  }

  public ExecContext getExecContext()
  {
    return execContext;
  }
 
}

