/* $Header: PhyPlanCorrAttrFactory.java 19-mar-2008.07:18:07 rkomurav Exp $ */

/* Copyright (c) 2007, 2008, Oracle. All rights reserved.  */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    rkomurav    03/19/08 - support subset
    anasrini    05/28/07 - handle aggregations
    rkomurav    03/05/07 - Creation
 */

/**
 *  @version $Header: PhyPlanCorrAttrFactory.java 19-mar-2008.07:18:07 rkomurav Exp $
 *  @author  rkomurav
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.phyplan.factory;

import oracle.cep.logplan.LogOpt;
import oracle.cep.logplan.LogOptPatternStrm;
import oracle.cep.logplan.attr.AttrAggr;
import oracle.cep.logplan.attr.AttrKind;
import oracle.cep.logplan.pattern.CorrNameDef;
import oracle.cep.logplan.pattern.SubsetCorr;
import oracle.cep.phyplan.attr.Attr;
import oracle.cep.phyplan.attr.CorrAttr;

public class PhyPlanCorrAttrFactory extends PhyPlanAttrFactory
{
  public Attr newAttr(LogOpt logop, 
                      oracle.cep.logplan.attr.Attr logattr) 
  {
    
    assert logop instanceof oracle.cep.logplan.LogOptPatternStrm;
    LogOptPatternStrm patternOp = (LogOptPatternStrm) logop;
    
    AttrKind attrKind = logattr.getAttrKind();
    
    if (attrKind == AttrKind.CORR)
      return handleCorr(patternOp, logattr);
    else if (attrKind == AttrKind.AGGR)
      return handleAggr(patternOp , logattr);
    else {
      // should not come here
      assert false;
      return null;
    }
  }

  private Attr handleCorr(LogOptPatternStrm patternOp, 
                          oracle.cep.logplan.attr.Attr logattr) {

    LogOpt childOp;
    int    numInputs = patternOp.getNumInputs();
    int    numAttrs;
      
    assert logattr instanceof oracle.cep.logplan.attr.CorrAttr;
    oracle.cep.logplan.attr.CorrAttr corr = 
      (oracle.cep.logplan.attr.CorrAttr)logattr;
    
    CorrNameDef[] logCorrs       = patternOp.getCorrDefs();
    SubsetCorr[]  logSubsetCorrs = patternOp.getSubsetCorrs();
    int bindPos = -1;
    int k;
    boolean found;
    
    for (int i=0; i<numInputs; i++)
    {
      childOp = patternOp.getInput(i);
      assert childOp != null;
      
      numAttrs = childOp.getNumOutAttrs();
      for (int j=0; j<numAttrs; j++)
      {
        if (childOp.getOutAttr(j).equals(logattr))
        {
          // popluate the bindpos for this correlation attribute
          // this is to find the tuple associated with this 
          // correlation name at runtime
          found = false;
          for(k = 0; k < logCorrs.length; k++)
          {
            if(logCorrs[k].getVarId() == corr.getVarId())
            {
              bindPos = logCorrs[k].getBindPos();
              found   = true;
              break;
            }
          }
          if(!found && (logSubsetCorrs != null))
          {
            for(k = 0; k < logSubsetCorrs.length; k++)
            {
              if(logSubsetCorrs[k].getVarId() == corr.getVarId())
              {
                bindPos = logSubsetCorrs[k].getBindPos();
                found = true;
                break;
              }
            }
          }
          assert found;
          return new CorrAttr(i, j, bindPos);
        }
      }
    }
    
    // Should never come here
    assert false;
    return null;
  }

  private Attr handleAggr(LogOptPatternStrm patternOp, 
                          oracle.cep.logplan.attr.Attr logattr) {

    int        numAggrs   = patternOp.getNumAggrAttrs();
    AttrAggr[] aggrAttrs  = patternOp.getAggrAttrs();
    int        bindLength = patternOp.getBindLength();

    for (int i=0; i<numAggrs; i++) {
      if (logattr.equals(aggrAttrs[i])) {
        // For an aggregate that is to be picked up from the bindings
        //
        //    input   --- meaningless, so set to -1
        //    pos     --- i,  the aggregate attr that matches
        //    bindPos --- bindLength-1, the last slot in the binding
        return new CorrAttr(-1, i, bindLength-1);
      }
    }

    // should not come here
    assert false;
    return  null;
  }

}

