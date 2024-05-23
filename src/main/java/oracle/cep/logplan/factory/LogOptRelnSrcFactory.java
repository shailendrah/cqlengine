/* $Header: pcbpel/cep/server/src/oracle/cep/logplan/factory/LogOptRelnSrcFactory.java /main/2 2008/10/24 15:50:16 hopark Exp $ */

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
    najain      04/06/06 - cleanup
    najain      03/01/06 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/logplan/factory/LogOptRelnSrcFactory.java /main/2 2008/10/24 15:50:16 hopark Exp $
 *  @author  najain  
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.logplan.factory;

import oracle.cep.exceptions.LogicalPlanError;
import oracle.cep.logplan.LogOpt;
import oracle.cep.logplan.LogOptRelnSrc;
import oracle.cep.logplan.LogicalPlanException;

public class LogOptRelnSrcFactory extends LogOptFactory {

  /**
   * Constructor for SelectFactory
   */
  public LogOptRelnSrcFactory() {
    // TODO Auto-generated constructor stub
    super();

  }

  public LogOpt newLogOpt(Object ctx) throws LogicalPlanException {
    assert ctx instanceof SemQueryInterpreterFactoryContext;
    SemQueryInterpreterFactoryContext lpctx = (SemQueryInterpreterFactoryContext) ctx;

    LogOpt op;
    try {
      op = new LogOptRelnSrc(lpctx.getExecContext(), lpctx.getQuery(), lpctx.getVarId(), lpctx.getTableId());
    } catch (LogicalPlanException ex) {
      throw new LogicalPlanException(LogicalPlanError.LOGOPT_RELNSRC_NOT_CREATED);
    }
    return op;
  }

}

