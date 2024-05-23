/* $Header: PhyExtensibleWinSpec.java 18-jun-2007.23:01:13 rkomurav Exp $ */

/* Copyright (c) 2007, Oracle. All rights reserved.  */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    rkomurav    06/18/07 - cleanup
    parujain    03/07/07 - Extensible Window spec
    parujain    03/07/07 - Creation
 */

/**
 *  @version $Header: PhyExtensibleWinSpec.java 18-jun-2007.23:01:13 rkomurav Exp $
 *  @author  parujain
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.phyplan.window;

import oracle.cep.logplan.LogOpt;
import oracle.cep.phyplan.expr.Expr;
import oracle.cep.phyplan.factory.LogPlanExprFactory;
import oracle.cep.phyplan.factory.LogPlanExprFactoryContext;

public class PhyExtensibleWinSpec extends PhyWinSpec {
  /** Window id */
  int winId;
  
  /** Parameters passed by the user while making a call */
  Expr[] params;
  
  public PhyExtensibleWinSpec(LogOpt op)
  {
    assert op instanceof oracle.cep.logplan.LogOptExtensibleWin;
    
    assert op != null;
    assert op.getNumInputs() == 1;
    
    oracle.cep.logplan.LogOptExtensibleWin src = (oracle.cep.logplan.LogOptExtensibleWin)op;
    this.winId = src.getWindowId();
    if(src.getNumParams() == 0)
      params = null;
    else
    {
      params = new Expr[src.getNumParams()];
      oracle.cep.logplan.expr.Expr[] exprs = src.getParams();
      for(int i=0; i<src.getNumParams(); i++)
      {
        params[i] = LogPlanExprFactory.getInterpreter(exprs[i],
                     new LogPlanExprFactoryContext(exprs[i],op));
      }
    }
    setWindowKind(WinKind.EXTENSIBLE);
  }
  
  public PhyExtensibleWinSpec(int id, Expr[] exprs)
  {
    super();
    this.winId = id;
    this.params = exprs;
  }
  
  public int getWindowId()
  {
    return winId;
  }
  
  public Expr[] getParams()
  {
    return params;
  }
  
  public int getNumParams()
  {
    if(params == null)
      return 0;
    
    return params.length;
  }

  @Override
  public boolean equals(Object other) {
    if(this == other)
      return true;
    
    if(other == null)
      return false;
    
    if(getClass() != other.getClass())
      return false;
    
    PhyExtensibleWinSpec otherWin = (PhyExtensibleWinSpec)other;
    if(winId != otherWin.winId)
      return false;
    
    if(getNumParams() != otherWin.getNumParams())
      return false;
    
    for(int i=0; i<getNumParams(); i++)
    {
      if(!params[i].equals(otherWin.params[i]))
        return false;
    }
    
    return true;
  }
  
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("<PhysicalExtensibleWindowSpecs>");
    
    sb.append("<WindowId>");
    sb.append(winId);
    sb.append("</WindowId>");
    sb.append("<InputParameters>");
    for(int i=0; i<getNumParams(); i++)
    {
      sb.append("<paramNo" +i + "=\""+params[i].toString() +"\" />");
    }
    sb.append("</InputParameters>");

    sb.append("</PhysicalExtensibleWindowSpecs>");
    return sb.toString();
  }
  
  public String getXMLPlan2(){
    StringBuilder xml = new StringBuilder();
    xml.append("<name> ExtensibleWin </name>\n");
    xml.append("<lname> Time Based Extensible Window </lname>\n");
    for(int i=0; i<getNumParams(); i++)
      xml.append(params[i].getXMLPlan2());
    return xml.toString();
  }
}

