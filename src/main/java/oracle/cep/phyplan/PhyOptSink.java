/* $Header: pcbpel/cep/server/src/oracle/cep/phyplan/PhyOptSink.java /main/2 2008/10/24 15:50:17 hopark Exp $ */

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
    rkomurav    09/13/06 - PhySynPos OO restructuring
    rkomurav    08/29/06 - add genXMLPlan2
    dlenkov     04/26/06 - 
    najain      04/06/06 - cleanup
    najain      03/24/06 - cleanup
    najain      02/26/06 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/phyplan/PhyOptSink.java /main/2 2008/10/24 15:50:17 hopark Exp $
 *  @author  najain  
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.phyplan;

import oracle.cep.exceptions.CEPException;
import oracle.cep.service.ExecContext;

/**
 * Select Physical Operator
 *
 * @since 1.0 
 */
public class PhyOptSink extends PhyOpt {

  private int id;

  public PhyOptSink(ExecContext ec) {
    super( ec, PhyOptKind.PO_SINK);
  }

  public int getId() {
    return id;
  }

  public void setId( int idp) {
    id = idp;
  }
  
  //Create and return visualiser compatible XML Plan
  public String getXMLPlan2() throws CEPException {
    StringBuilder xml = new StringBuilder();
    xml.append("<name> Sink </name>\n");
    xml.append("<lname> Sink </lname>\n");
    xml.append(super.getXMLPlan2());
    return xml.toString();
  }
  
  //get the Synopsis Position
  public String getRelnSynPos(PhySynopsis syn) {
    // Sink has no Relation Synopsis
    assert(false);
    return null;
  }
}
