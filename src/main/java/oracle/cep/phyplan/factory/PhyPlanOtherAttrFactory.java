/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/phyplan/factory/PhyPlanOtherAttrFactory.java /main/6 2012/06/18 06:29:08 udeshmuk Exp $ */

/* Copyright (c) 2007, 2012, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    Factory producing a physical representation of an attribute by 
    transforming from a logical representation

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    udeshmuk    06/15/12 - propagate datatype
    vikshukl    08/24/11 - subquery support
    udeshmuk    04/05/11 - propagate attrname
    mthatte     04/01/08 - checking for derived TS
    anasrini    08/28/07 - 
    rkomurav    03/05/07 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/phyplan/factory/PhyPlanOtherAttrFactory.java /main/6 2012/06/18 06:29:08 udeshmuk Exp $
 *  @author  rkomurav
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.phyplan.factory;

import oracle.cep.logplan.LogOpt;
import oracle.cep.logplan.LogOptStrmSrc;
import oracle.cep.phyplan.attr.Attr;

public class PhyPlanOtherAttrFactory extends PhyPlanAttrFactory
{
  public Attr newAttr(LogOpt logop, 
                             oracle.cep.logplan.attr.Attr logattr) {
    LogOpt childOp;
    int    numInputs = logop.getNumInputs();
    int    numAttrs;

    for (int i=0; i<numInputs; i++) {
      childOp = logop.getInput(i);
      assert childOp != null;

      numAttrs = childOp.getNumOutAttrs();
      for (int j=0; j<numAttrs; j++) {
        if (childOp.getOutAttr(j).equals(logattr))
        {
          Attr attr = new Attr(i, j);
          attr.setActualName(logattr.getActualName());
          attr.setType(logattr.getDatatype());
          return attr;
        }
      }
    }
    
    
    assert logop instanceof LogOptStrmSrc;
    
    //For now if this is a derived TS expr, we return an attr with 
    //input = 0 
    //position = position in stream/reln. schema.
    if(((LogOptStrmSrc)logop).isDerivedTS()) 
    {
      numAttrs = logop.getNumOutAttrs();
      for (int j=0; j < numAttrs; j++) {
        if (logop.getOutAttr(j).equals(logattr))
          return new Attr(0, j);
      }
    }
    // Should never come here
    assert false : logattr;
    return null;
  }
}
