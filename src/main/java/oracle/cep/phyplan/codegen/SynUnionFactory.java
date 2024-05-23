/* $Header: pcbpel/cep/server/src/oracle/cep/phyplan/codegen/SynUnionFactory.java /main/3 2008/10/24 15:50:13 hopark Exp $ */

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
    sbishnoi    04/12/07 - countsyn code cleanup
    sbishnoi    04/06/07 - support for union all
    dlenkov     07/05/06 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/phyplan/codegen/SynUnionFactory.java /main/3 2008/10/24 15:50:13 hopark Exp $
 *  @author  dlenkov 
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.phyplan.codegen;

import oracle.cep.phyplan.PhyOpt;
import oracle.cep.phyplan.PhyOptUnion;
import oracle.cep.phyplan.PhySynopsis;
import oracle.cep.phyplan.SynopsisKind;
import oracle.cep.service.ExecContext;

/**
 * SynUnionFactory
 *
 * @author dlenkov
 */
public class SynUnionFactory extends SynGenFactory {

  public void addSynOpt( Object ctx) {

    assert ctx instanceof SynGenFactoryContext;
    SynGenFactoryContext lpctx = (SynGenFactoryContext) ctx;
    ExecContext ec = lpctx.getExecContext();

    PhyOpt op = lpctx.getPhyPlan();
    assert op instanceof PhyOptUnion;
    PhyOptUnion opu = (PhyOptUnion) op;

    if(opu.isUnionAll())
    {
      if(opu.getIsStream() == false) {
    	PhySynopsis lin = new PhySynopsis(ec, SynopsisKind.LIN_SYN);
    	opu.setOutSynopsis( lin);
    	lin.setOwnOp( opu);
      }
      else 
    	opu.setOutSynopsis( null);
    }    	   	
    else
    {
      PhySynopsis rel = new PhySynopsis(ec, SynopsisKind.REL_SYN);
      opu.setOutSynopsis( rel);
      rel.setOwnOp( opu);
      
    }
       
  }
}

