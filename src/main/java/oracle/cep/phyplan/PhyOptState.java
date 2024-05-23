/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/phyplan/PhyOptState.java /main/2 2011/04/03 09:24:45 anasrini Exp $ */

/* Copyright (c) 2006, 2011, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    anasrini    03/28/11 - add state REINST
    najain      05/04/06 - Creation
 */

/**
 *  @version $Header: PhyOptState.java 04-may-2006.11:44:05 najain Exp $
 *  @author  najain  
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.phyplan;

/**
 * PhyOptState
 *
 * @author najain
 */
public enum PhyOptState
{
  // Initialized
  INIT,
  // Add Aux done
  ADDAUX,
  // Instantiated
  INST,
  // Requires re-instantiation
  REINST
};


