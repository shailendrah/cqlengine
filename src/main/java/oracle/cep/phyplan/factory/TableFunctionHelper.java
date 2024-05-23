/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/phyplan/factory/TableFunctionHelper.java /main/1 2010/01/25 00:32:43 sbishnoi Exp $ */

/* Copyright (c) 2010, Oracle and/or its affiliates. All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    sbishnoi    01/03/10 - Creation
 */

package oracle.cep.phyplan.factory;

import oracle.cep.common.Constants;
import oracle.cep.logplan.LogOpt;
import oracle.cep.phyplan.PhyOpt;
import oracle.cep.phyplan.PhyOptTableFunctionRelnSrc;
import oracle.cep.phyplan.TableFunctionInfo;
/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/phyplan/factory/TableFunctionHelper.java /main/1 2010/01/25 00:32:43 sbishnoi Exp $
 *  @author  sbishnoi
 *  @since   release specific (what release of product did this appear in)
 */

public class TableFunctionHelper
{
  public static TableFunctionInfo getTableFunctionInfo(LogOpt logPlan, PhyOpt phyPlan)
  {
    PhyOptTableFunctionRelnSrc sourcePhyOpt = null;
    
    PhyOpt innerInput = phyPlan.getInputs()[Constants.INNER];
    PhyOpt outerInput = phyPlan.getInputs()[Constants.OUTER];
       
    if(innerInput.isExternal() && 
       innerInput instanceof PhyOptTableFunctionRelnSrc)
    {
      sourcePhyOpt = (PhyOptTableFunctionRelnSrc)innerInput;    
    }
    else if(outerInput.isExternal() && 
              outerInput instanceof PhyOptTableFunctionRelnSrc)
    {
      sourcePhyOpt = (PhyOptTableFunctionRelnSrc)outerInput;    
    }
    else
    {
      // Note: This is not a case of join with table function;
      // So return "null" as the table function info object
      return null;
    }
    
    oracle.cep.phyplan.expr.Expr tableFunctionPhyExpr = null;
    oracle.cep.logplan.expr.Expr tableFunctionLogExpr = null;
    
    // Transform the table function logical expression into table function
    // physical expression
    tableFunctionLogExpr = sourcePhyOpt.getTableFunctionLogExpr();
    
    assert tableFunctionLogExpr != null: "Table Function Logical Expr is null";
    LogPlanExprFactoryContext exprCtx
        = new LogPlanExprFactoryContext(tableFunctionLogExpr, logPlan);
      tableFunctionPhyExpr =
        LogPlanExprFactory.getInterpreter(tableFunctionLogExpr, exprCtx);
      
    // Set the table function physical expression into TableFunctionOperator
    // It will be used in calculating the sharing of table function operator. 
    sourcePhyOpt.setTableFunctionPhyExpr(tableFunctionPhyExpr);
    
    // Prepare a table function info object
    TableFunctionInfo tableFunctionInfo = 
       new TableFunctionInfo(sourcePhyOpt.getTableAlias(),
           sourcePhyOpt.getColumnAlias(),
           sourcePhyOpt.getReturnCollectionType(),
           sourcePhyOpt.getComponentType(),
           tableFunctionPhyExpr);
    
    
    return tableFunctionInfo;
  }
}
