/* $Header: pcbpel/cep/src/oracle/cep/phyplan/window/factory/LogPlanWinSpecFactory.java /main/3 2008/07/14 22:57:01 parujain Exp $ */

/* Copyright (c) 2007, 2008, Oracle. All rights reserved.  */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    parujain    07/07/08 - value based windows
    hopark      10/12/07 - support partnwinspec
    parujain    03/07/07 - Window Spec Factory
    parujain    03/07/07 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/src/oracle/cep/phyplan/window/factory/LogPlanWinSpecFactory.java /main/3 2008/07/14 22:57:01 parujain Exp $
 *  @author  parujain
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.phyplan.window.factory;

import java.util.HashMap;

import oracle.cep.logplan.LogOpt;
import oracle.cep.phyplan.factory.LogPlanInterpreterFactoryContext;


public class LogPlanWinSpecFactory {
  private static HashMap<String, PhyPlanWinSpecFactory> interpMap;

  static {
    populateInterpMap();
  }

  static void populateInterpMap() {
    interpMap = new HashMap<String, PhyPlanWinSpecFactory>();
    interpMap.put(oracle.cep.logplan.LogOptRngWin.class.getName(), 
        new PhyPlanRngWinSpecFactory());
    interpMap.put(oracle.cep.logplan.LogOptNowWin.class.getName(), 
        new PhyPlanRngWinSpecFactory());
    interpMap.put(oracle.cep.logplan.LogOptPrtnWin.class.getName(), 
        new PhyPlanRowRangeWinSpecFactory());
    interpMap.put(oracle.cep.logplan.LogOptExtensibleWin.class.getName(),
        new PhyPlanExtensibleWinSpecFactory());
    interpMap.put(oracle.cep.logplan.LogOptValueWin.class.getName(), 
        new PhyPlanValueWinSpecFactory());
  }
  
  public static oracle.cep.phyplan.window.PhyWinSpec getWinSpec(LogOpt op,LogPlanInterpreterFactoryContext ctx)
  {
    PhyPlanWinSpecFactory o = interpMap.get(op.getClass().getName());
    assert o != null;
    return o.newWinSpec(ctx);
  }
}
