/* $Header: pcbpel/cep/server/src/oracle/cep/planmgr/codegen/CodeGenContext.java /main/3 2009/03/30 14:46:01 parujain Exp $ */

/* Copyright (c) 2006, 2009, Oracle and/or its affiliates. 
All rights reserved. */

/*
 DESCRIPTION
 Declares CodeGenContext in package oracle.cep.planmgr.codegen.
 
 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>
 
 NOTES
 <other useful comments, qualifications, etc.>
 
 MODIFIED    (MM/DD/YY)
 parujain  03/19/09 - stateless server
 hopark    10/07/08 - use execContext to remove statics
 najain    06/13/06 - bug fix 
 najain    06/06/06 - add query
 najain    04/03/06 - cleanup
 najain    03/30/06 - bug fix 
 anasrini  03/22/06 - remove childplans 
 anasrini  03/13/06 - add planMgr to the context 
 skaluska  02/28/06 - Creation
 skaluska  02/28/06 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/planmgr/codegen/CodeGenContext.java /main/3 2009/03/30 14:46:01 parujain Exp $
 *  @author  skaluska
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.planmgr.codegen;

import oracle.cep.execution.operators.ExecOpt;
import oracle.cep.execution.stores.ExecStore;
import oracle.cep.metadata.Query;
import oracle.cep.phyplan.PhyOpt;
import oracle.cep.service.ExecContext;

/**
 * Context for code generation passed into all the individual
 * methods. This is an alternative to operator specific context,
 * and different signatures at the high-level.
 *
 * @author skaluska
 */
public class CodeGenContext
{
  /** current execution context */
  ExecContext execContext;
  
  /** current physical operator */
  PhyOpt  phyopt;

  /** current query */
  Query   query;

  /** execution operator */
  ExecOpt execOpt;

  /** tuple storage allocator */
  ExecStore tupleStorage;

  /**
   * Constructor for CodeGenContext
   * @param phyopt Physical Operator
   */
  public CodeGenContext(ExecContext ec, Query query, PhyOpt phyopt)
  {
    this.execContext = ec;
    this.query    = query;
    this.phyopt   = phyopt;
    this.execOpt  = null;
    this.tupleStorage = null;
  }


  /**
   * @return Returns the execution context.
   */
  public ExecContext getExecContext() {
    return execContext;
  }

  /**
   * @return Returns the phyopt.
   */
  public PhyOpt getPhyopt() {
    return phyopt;
  }

  /**
   * @return Returns the query.
   */
  public Query getQuery() {
    return query;
  }

  /**
   * @return Returns the execOpt.
   */
  public ExecOpt getExecOpt() {
    return execOpt;
  }

  /**
   * @param execOpt the value of the execution operator
   */
  public void setExecOpt(ExecOpt execOpt) {
    this.execOpt = execOpt;
  }

  /**
   * @return Returns the tuple storage allocator
   */
  public ExecStore getTupleStorage() {
    return tupleStorage;
  }

  /**
   * @param tupleStorage the value of the tuple storage allocator
   */
  public void setTupleStorage(ExecStore tupleStorage) {
    this.tupleStorage = tupleStorage;
  }
}
