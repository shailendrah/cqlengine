/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/internals/IEvalContext.java /main/5 2011/02/24 08:23:34 alealves Exp $ */

/* Copyright (c) 2007, 2011, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    udeshmuk    10/02/08 - add another version of bind.
    skmishra    08/05/08 - adding role for xmlagg index
    rkomurav    09/19/07 - add null input role
    hopark      09/07/07 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/execution/internals/IEvalContext.java /main/4 2008/10/17 05:15:15 udeshmuk Exp $
 *  @author  hopark  
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.execution.internals;

import oracle.cep.dataStructures.internal.ITuple;
import oracle.cep.dataStructures.internal.ITuplePtr;
import oracle.cep.execution.ExecException;
import oracle.cep.execution.internals.IEvalContext;

public interface IEvalContext extends Cloneable
{
  /** Various eval context roles */
  public static final int SCRATCH_ROLE    = 0;

  public static final int CONST_ROLE      = 1;

  public static final int NEW_OUTPUT_ROLE = 2;

  public static final int OLD_OUTPUT_ROLE = 3;

  public static final int INPUT_ROLE      = 4;

  public static final int LIN_ROLE        = 5;

  public static final int UPDATE_ROLE     = 6;

  public static final int SCAN_ROLE       = 7;
  
  public static final int INNER_ROLE      = 8;

  public static final int OUTER_ROLE      = 9;

  public static final int SYN_ROLE        = 10; 

  public static final int LEFT_ROLE       = 11; 

  public static final int RIGHT_ROLE      = 12; 
  
  public static final int PREV_INPUT_ROLE = 13;
  
  public static final int AGGR_ROLE       = 14;
  
  public static final int NULL_INPUT_ROLE = 15;

  public static final int XML_AGG_INDEX_ROLE = 16;
 
  public static final int END_ROLES       = 17;
  

  int addRoles(int num);
  void bind(ITuplePtr t, int rolenum) throws ExecException;
  int  bind(ITuplePtr[] t, int startRole, int length, boolean isPrev, 
            ITuplePtr nullInputTuple) throws ExecException;
  ITuple[] getRoles();
  ITuplePtr[] getRolePtrs();
  
  /**
   * Return a shallow copy of roles and rolePtrs.
   * This is useful when eval context is setup with static roles during compilation,
   *  and later needs to be modified dynamically during operation execution.
   * By cloning it, the operators can execute their own copy and therefore in parallel.
   *  
   * @return A shallow copy of the evaluation context.
   * @throws CloneNotSupportedException if evaluation context cannot be cloned and 
   *  therefore cannot be run in parallel.
   */
  Object clone() throws CloneNotSupportedException;
  
}
