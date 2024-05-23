/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/phyplan/codegen/SynSelectFactory.java /main/6 2012/09/25 06:20:29 udeshmuk Exp $ */

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
 udeshmuk    08/13/12 - use stateinitializationDone flag instead of
                        getOutputSQL
 udeshmuk    07/07/12 - changes after introduction of buffer operator
 udeshmuk    10/21/11 - check if outputs use the syn/store
 udeshmuk    09/02/11 - prevent creation of synopsis in the context of archived
                        reln
 hopark      10/07/08 - use execContext to remove statics
 anasrini    08/03/06 - add an output synopsis for select
 najain      03/20/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/phyplan/codegen/SynSelectFactory.java /main/6 2012/09/25 06:20:29 udeshmuk Exp $
 *  @author  najain  
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.phyplan.codegen;

import oracle.cep.phyplan.PhyOpt;
import oracle.cep.phyplan.PhyOptSelect;
import oracle.cep.phyplan.PhySynopsis;
import oracle.cep.phyplan.SynopsisKind;


/**
 * SynSelectFactory
 *
 * @author najain
 */
public class SynSelectFactory extends SynGenFactory {

  public void addSynOpt(Object ctx) {
    assert ctx instanceof SynGenFactoryContext;
    SynGenFactoryContext lpctx = (SynGenFactoryContext) ctx;
    PhyOpt op = lpctx.getPhyPlan();
    assert op instanceof PhyOptSelect;
    PhyOptSelect opSel = (PhyOptSelect) op;
    
    //isStateInitializationDone would always be false only when 
    //operator is used in archived context and 
    //when it is on the path from source to query operator
    //We don't want to create a synopsis if 
    //1. The operator is on the path from source to query operator OR
    //2. The operator itself is a query operator
    
    if((opSel.getIsStream() == true) || 
       ((!opSel.isStateInitializationDone()) || opSel.isQueryOperator()))
      opSel.setOutSyn(null);
    else {
      PhySynopsis outSyn = new PhySynopsis(lpctx.getExecContext(), SynopsisKind.REL_SYN);

      opSel.setOutSyn(outSyn);
      outSyn.setOwnOp(opSel);
    }
  }
}
