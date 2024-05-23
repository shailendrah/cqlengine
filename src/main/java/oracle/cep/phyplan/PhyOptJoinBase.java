/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/phyplan/PhyOptJoinBase.java /main/3 2012/09/25 06:20:29 udeshmuk Exp $ */

/* Copyright (c) 2010, 2012, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    udeshmuk    08/30/12 - put list of outer and inner attrs
    sbishnoi    03/03/10 - adding abstract methods into PhyOptJoinBase
    sbishnoi    01/21/10 - Creation
 */

package oracle.cep.phyplan;

import java.util.List;
import java.util.logging.Level;

import oracle.cep.exceptions.CEPException;
import oracle.cep.exceptions.InterfaceError;
import oracle.cep.execution.internals.IAEval;
import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;
import oracle.cep.phyplan.attr.Attr;
import oracle.cep.planmgr.codegen.datasource.table.TableFunctionDataSource;
import oracle.cep.service.ExecContext;

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/phyplan/PhyOptJoinBase.java /main/3 2012/09/25 06:20:29 udeshmuk Exp $
 *  @author  sbishnoi
 *  @since   release specific (what release of product did this appear in)
 */

public abstract class PhyOptJoinBase extends PhyOpt
{
  
  protected List<Attr> outerAttrs;
  
  protected List<Attr> innerAttrs;
  
  protected StringBuffer whereClause = null;

  protected StringBuffer selectClause = null;

  protected String leftSideAlias = null;
  
  protected String rightSideAlias = null;
  
  public List<Attr> getOuterAttrs()
  {
    return outerAttrs;
  }
  
  public List<Attr> getInnerAttrs()
  {
    return innerAttrs;
  }
  
  public void setOuterAttrs(List<Attr> outerAttrs)
  {
    this.outerAttrs = outerAttrs;
  }
  
  public void setInnerAttrs(List<Attr> innerAttrs)
  {
    this.innerAttrs = innerAttrs;
  }
  
  protected PhyOptJoinBase(ExecContext ec, PhyOptKind operatorKind)
  {
    super(ec, operatorKind);
  }
  
  public void initTableFunctionDataSource(IAEval stmtEval, 
      TableFunctionInfo tableFunctionInfo) 
    throws CEPException
  {
    try
    {
      extDataSource 
      = new TableFunctionDataSource(stmtEval, 
          tableFunctionInfo.getReturnCollectionType(),
          tableFunctionInfo.getComponentType()); 
      if(extDataSource != null)
      {
        extConnection = extDataSource.getConnection();  
      }
    }
    catch(Exception e)
    {
      LogUtil.warning(LoggerType.TRACE, "Failed to get dataSource for Table " +
          "Functions " + e.toString());
      LogUtil.logStackTrace(LoggerType.TRACE, Level.FINE, e);

      throw new CEPException(InterfaceError.INVALID_SOURCE, e, 
      " Table Function ");      
    }
  }

   //TODO: We can enhance this class to implement the common code of physical
  // join operators.
  // Presently The declarations(below) helps to access these methods by using
  // a single reference of PhyOptJoinBase
  
  /** Get the inner synopsis */ 
  public abstract PhySynopsis getInnerSyn();
  
  /** Get the outer synopsis */
  public abstract PhySynopsis getOuterSyn();

  /** Check if table function join */
  public abstract boolean isTableFunctionExternalJoin();

  /** Get the table function info */
  public abstract TableFunctionInfo getTableFunctionInfo();
}
