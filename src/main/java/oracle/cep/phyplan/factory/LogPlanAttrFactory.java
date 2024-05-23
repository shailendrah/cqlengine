/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/phyplan/factory/LogPlanAttrFactory.java /main/4 2012/05/02 03:06:00 pkali Exp $ */

/* Copyright (c) 2007, 2012, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    Attr factory

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    pkali       03/30/12 - added AttGroupBy class
    udeshmuk    06/05/08 - support for xmlagg
    rkomurav    06/18/07 - cleanup
    rkomurav    03/05/07 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/phyplan/factory/LogPlanAttrFactory.java /main/4 2012/05/02 03:06:00 pkali Exp $
 *  @author  rkomurav
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.phyplan.factory;

import java.util.HashMap;

import oracle.cep.logplan.LogOpt;
import oracle.cep.logplan.attr.Attr;

public class LogPlanAttrFactory
{
  // As CorrAttr needs to be processed differently in different contexts,
  // [Differences:
  // 1) When a CorrAttr is converted from logical to physical as part of 
  //    Pattern Operator, it needs to capture the binding position
  // 2) But the same CorrAttr as part of Project Operator for example, need
  //    not capture the binding position and behave same as AttrNamed.
  // 3) Comparision among AttrNamed and CorrAttr don't consider varId
  //    due to the assumption that all the attributes for the current Op come
  //    from its input operators, which is not true in case of PatternOperator
  // 4) A CorrAttr in Project cannot be an AttrNamed as the comparison among
  //    CorrAttr and AttrNamed doesn't consider varId comparison
  // HashMap cannot be static]
  
  private static HashMap<String, PhyPlanAttrFactory> map1;
  private static HashMap<String, PhyPlanAttrFactory> map2;

  static
  {
    populateInterpMap();
  }
  
  static void populateInterpMap()
  {
    map1 = new HashMap<String, PhyPlanAttrFactory>();
    map2 = new HashMap<String, PhyPlanAttrFactory>();
    
    map1.put(oracle.cep.logplan.attr.AttrAggr.class.getName(),
        new PhyPlanOtherAttrFactory());
    map1.put(oracle.cep.logplan.attr.AttrNamed.class.getName(),
        new PhyPlanOtherAttrFactory());
    map1.put(oracle.cep.logplan.attr.AttrUnNamed.class.getName(),
        new PhyPlanOtherAttrFactory());
    map1.put(oracle.cep.logplan.attr.CorrAttr.class.getName(),
        new PhyPlanOtherAttrFactory());
    map1.put(oracle.cep.logplan.attr.AttrXMLAgg.class.getName(),
        new PhyPlanOtherAttrFactory());
    map1.put(oracle.cep.logplan.attr.AttrGroupBy.class.getName(),
            new PhyPlanOtherAttrFactory());
    
    map2.put(oracle.cep.logplan.attr.AttrNamed.class.getName(),
        new PhyPlanOtherAttrFactory());
    map2.put(oracle.cep.logplan.attr.AttrUnNamed.class.getName(),
        new PhyPlanOtherAttrFactory());
    map2.put(oracle.cep.logplan.attr.CorrAttr.class.getName(), 
        new PhyPlanCorrAttrFactory());
    map2.put(oracle.cep.logplan.attr.AttrAggr.class.getName(), 
        new PhyPlanCorrAttrFactory());
    map2.put(oracle.cep.logplan.attr.AttrXMLAgg.class.getName(),
        new PhyPlanCorrAttrFactory());
    map2.put(oracle.cep.logplan.attr.AttrGroupBy.class.getName(), 
            new PhyPlanCorrAttrFactory());
  }
  
  public static oracle.cep.phyplan.attr.Attr getInterpreter(LogOpt logOp,
      Attr attr, boolean makeCorr)
  {
    PhyPlanAttrFactory o;
    if(makeCorr)
      o = map2.get(attr.getClass().getName());
    else
      o = map1.get(attr.getClass().getName());

    assert o != null;
    return (oracle.cep.phyplan.attr.Attr) o.newAttr(logOp, attr);
  }
  
  public static oracle.cep.phyplan.attr.Attr getInterpreter(LogOpt logOp, Attr attr)
  {
    return getInterpreter(logOp, attr, false);
  }
}