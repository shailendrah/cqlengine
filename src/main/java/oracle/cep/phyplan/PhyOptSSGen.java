/* $Header: pcbpel/cep/server/src/oracle/cep/phyplan/PhyOptSSGen.java /main/2 2008/10/24 15:50:17 hopark Exp $ */

/* Copyright (c) 2006, 2008, Oracle and/or its affiliates. All rights reserved.*/

/*
 DESCRIPTION
 System Stream Generator Physical Operator in the package 
 oracle.cep.phyplan

 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>

 NOTES
 <other useful comments, qualifications, etc.>

 MODIFIED    (MM/DD/YY)
 hopark      10/09/08 - remove statics
 rkomurav    09/13/06 - PhySynPos OO restructuring
 rkomurav    08/29/06 - add genXMLPlan2
 najain      04/06/06 - cleanup
 anasrini    04/06/06 - constructor cleanup 
 najain      03/24/06 - cleanup
 skaluska    02/15/06 - cleanup PhyStore/ExecStore 
 najain      02/10/06 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/phyplan/PhyOptSSGen.java /main/2 2008/10/24 15:50:17 hopark Exp $
 *  @author  najain  
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.phyplan;

import oracle.cep.exceptions.CEPException;
import oracle.cep.execution.queues.Queue;
import oracle.cep.service.ExecContext;

/**
 * System Stream Generator Physical Operator
 */
public class PhyOptSSGen extends PhyOpt {
  PhyStore[] outStores;

  Queue[]    outQueues;

  int        numOutput;

  public PhyOptSSGen(ExecContext ec, PhyOpt op) {
    super(ec, PhyOptKind.PO_SS_GEN);
  }
  
  //Create and return visualiser compatible XML Plan
  public String getXMLPlan2() throws CEPException {
    StringBuilder xml = new StringBuilder();
    xml.append("<name> SSGen </name>\n");
    xml.append("<lname> System Stream Generator </lname>\n");
    xml.append(super.getXMLPlan2());
    return xml.toString();
  }
  
  //get the Synopsis Position
  public String getRelnSynPos(PhySynopsis syn) {
    // System Stream Generator has no Relation Synopsis
    assert(false);
    return null;
  }
}
