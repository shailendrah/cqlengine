/* $Header: pcbpel/cep/server/src/oracle/cep/logplan/factory/SemQueryInterpreterFactoryContext.java /main/3 2008/10/24 15:50:16 hopark Exp $ */

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
    parujain    03/11/08 - derived timestamp
    najain      04/06/06 - cleanup
    najain      03/24/06 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/logplan/factory/SemQueryInterpreterFactoryContext.java /main/3 2008/10/24 15:50:16 hopark Exp $
 *  @author  najain  
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.logplan.factory;

import oracle.cep.semantic.DerivedTimeSpec;
import oracle.cep.semantic.SemQuery;
import oracle.cep.service.ExecContext;

/**
 * Context for logical operators generation from Semantic Query
 *
 * @author najain
 */
public class SemQueryInterpreterFactoryContext {
  ExecContext execContext;
  /** current query */
  SemQuery     query;
  int varId;
  int tableId;
  DerivedTimeSpec dSpec;

  public SemQueryInterpreterFactoryContext(ExecContext ec, SemQuery query, int varId, int tableId) {
    this.execContext = ec;
    this.query = query;
    this.varId = varId;
    this.tableId = tableId;
    this.dSpec = null;
  }

  public void setDerivedTimeSpec(DerivedTimeSpec dspec)
  {
    this.dSpec = dspec;
  }
  
  public DerivedTimeSpec getDerivedTimeSpec()
  {
    return dSpec;
  }
  
  public SemQuery getQuery() {
    return query;
  }

  public int getVarId() {
    return varId;
  }

  public int getTableId() {
    return tableId;
  }

  public ExecContext getExecContext() {
    return execContext;
  }
  
}

