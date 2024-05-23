/* $Header: pcbpel/cep/server/src/oracle/cep/phyplan/codegen/SynBinStreamJoinProjectFactory.java /main/3 2008/10/24 15:50:14 hopark Exp $ */

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
    hopark      10/07/08 - use execContext to remove statics
    parujain    11/16/07 - external source
    najain      05/30/06 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/phyplan/codegen/SynBinStreamJoinProjectFactory.java /main/3 2008/10/24 15:50:14 hopark Exp $
 *  @author  najain  
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.phyplan.codegen;

import oracle.cep.phyplan.PhyOpt;
import oracle.cep.phyplan.PhyOptStrJoinProject;
import oracle.cep.phyplan.PhySynopsis;
import oracle.cep.phyplan.SynopsisKind;
import oracle.cep.service.ExecContext;

/**
 * SynBinStreamJoinProjectFactory
 *
 * @author najain
 */
public class SynBinStreamJoinProjectFactory extends SynGenFactory {
  public void addSynOpt(Object ctx) {
    assert ctx instanceof SynGenFactoryContext;
    SynGenFactoryContext lpctx = (SynGenFactoryContext) ctx;
    ExecContext ec = lpctx.getExecContext();
    PhyOpt op = lpctx.getPhyPlan();
    assert op instanceof PhyOptStrJoinProject;
    PhyOptStrJoinProject opStrJoinProject = (PhyOptStrJoinProject) op;
    PhySynopsis inner = null;
    if(opStrJoinProject.isExternal())
      inner = new PhySynopsis(ec, SynopsisKind.EXT_SYN);
    else
      inner = new PhySynopsis(ec, SynopsisKind.REL_SYN);
    
    opStrJoinProject.setInnerSyn(inner);
    inner.setOwnOp(opStrJoinProject);
  }
}
