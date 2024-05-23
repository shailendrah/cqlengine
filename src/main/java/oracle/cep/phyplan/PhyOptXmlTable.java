/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/phyplan/PhyOptXmlTable.java /main/5 2009/11/09 10:10:59 sborah Exp $ */

/* Copyright (c) 2007, 2009, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    sborah      10/12/09 - support for bigdecimal
    sborah      04/20/09 - reorganize sharing hash
    sborah      03/18/09 - define sharingHash
    hopark      10/09/08 - remove statics
    mthatte     12/26/07 - 
    najain      12/11/07 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/phyplan/PhyOptXmlTable.java /main/5 2009/11/09 10:10:59 sborah Exp $
 *  @author  najain  
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.phyplan;

import oracle.cep.common.Datatype;
import oracle.cep.logplan.LogOpt;
import oracle.cep.logplan.LogOptKind;
import oracle.cep.logplan.LogOptXmlTable;
import oracle.cep.phyplan.expr.ExprXQryFunc;
import oracle.cep.phyplan.factory.LogPlanExprFactory;
import oracle.cep.phyplan.factory.LogPlanExprFactoryContext;
import oracle.cep.service.ExecContext;

public class PhyOptXmlTable extends PhyOpt 
{
  /** project expressions */
  ExprXQryFunc expr;

  public PhyOptXmlTable(ExecContext ec, LogOpt logPlan, PhyOpt[] input)
      throws PhysicalPlanException {

    super(ec, PhyOptKind.PO_XMLTABLE, input[0], false, true);

	  assert input.length == 1;
    assert logPlan != null;
    assert logPlan.getNumInputs() == 1;
    assert logPlan.getOperatorKind() == LogOptKind.LO_XMLTABLE;
    
    LogOptXmlTable logOptXmlTable = (LogOptXmlTable)logPlan;
    setNumAttrs(1);
    setAttrTypes(0, Datatype.XMLTYPE);
    setAttrLen(0, getAttrMetadata(0).getLength());

    oracle.cep.logplan.expr.ExprXQryFunc logExpr 
      = logOptXmlTable.getXQryExpr();
    
    expr = (ExprXQryFunc) 
        LogPlanExprFactory.getInterpreter(logExpr, 
	  new LogPlanExprFactoryContext(logExpr, logPlan));
   
  }

  /**
   * Method to calculate a concise String representation
   * of the Physical operator based on its attributes.
   * @return 
   *      A concise String representation of the Physical operator.
   */
  protected String  getSignature()
  { 
    StringBuilder expr;
    expr = new StringBuilder();
    ExprXQryFunc fn = getExprXQryFunc();
    expr.append(this.getOperatorKind() + "#");
    expr.append(fn.getFuncId() + "#");
    expr.append(getExpressionList(fn.getParams()) + "#");
    expr.append(fn.getXQryStr());
    
    return expr.toString();
  }
  
  public String getRelnSynPos(PhySynopsis syn) 
  {
	return null;
  }
  
  /**
   * This method tells whether the two operators are partially equivalent or not
   */
  public boolean isPartialEquivalent(PhyOpt opt)
  {
	if (!(opt instanceof PhyOptXmlTable))
      return false;
	  
	// this is to avoid finding the same operator in PlanManager list
	if (opt.getId() == this.getId())
      return false;
	 
    PhyOptXmlTable xOpt = (PhyOptXmlTable)opt;
	  
	assert xOpt.getOperatorKind() == PhyOptKind.PO_XMLTABLE;
	  
	if (xOpt.getNumInputs() != this.getNumInputs())
	  return false;
	 
	return expr.equals(xOpt.expr);
  }

  public ExprXQryFunc getExprXQryFunc()
  {
    return expr;
  }
}
