/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/phyplan/codegen/SynGenFactory.java /main/3 2012/07/16 08:14:06 udeshmuk Exp $ */

/* Copyright (c) 2006, 2012, Oracle and/or its affiliates. 
All rights reserved. */

/*
 DESCRIPTION
 <short description of component this file declares/defines>

 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>

 NOTES
 <other useful comments, qualifications, etc.>

 MODIFIED    (MM/DD/YY)
 udeshmuk    07/10/12 - remove areOutputsDependentOnChildSynStore
 udeshmuk    10/21/11 - add method to iterate through the output operators of
                        an operator to find out if they use its synopsis
 najain      03/20/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/phyplan/codegen/SynGenFactory.java /main/3 2012/07/16 08:14:06 udeshmuk Exp $
 *  @author  najain  
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.phyplan.codegen;

import oracle.cep.phyplan.PhyOpt;
import oracle.cep.phyplan.PhyOptOutputIter;
import oracle.cep.phyplan.PhysicalPlanException;

/**
 * SynGenFactory
 *
 * @author najain
 */
public abstract class SynGenFactory {
  public abstract void addSynOpt(Object ctx);
  
}
