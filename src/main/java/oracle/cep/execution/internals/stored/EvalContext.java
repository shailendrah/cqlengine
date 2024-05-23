/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/internals/stored/EvalContext.java /main/4 2011/02/24 08:23:34 alealves Exp $ */

/* Copyright (c) 2006, 2011, Oracle and/or its affiliates. 
All rights reserved. */

/*
 DESCRIPTION
 Declares EvalContext in package oracle.cep.execution.internals.
 
 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>
 
 NOTES
 
 MODIFIED    (MM/DD/YY)
 udeshmuk  10/02/08 - new signature of bind method.
 hopark    02/05/08 - parameterized error
 hopark    09/04/07 - optimize
 rkomurav  07/12/07 - uda changes
 najain    03/14/07 - cleanup
 rkomurav  03/16/07 - add Prev input role
 rkomurav  03/15/07 - add addRoles() for binding roles
 najain    03/12/07 - bug fix
 hopark    03/06/07 - spill-over support
 najain    02/06/07 - coverage
 rkomurav  11/13/06 - add getTuple
 dlenkov   06/23/06 - 
 ayalaman  04/20/06 - add SYN_ROLE 
 najain    04/13/06 - add more roles
 anasrini  03/21/06 - add LIN_ROLE 
 skaluska  02/12/06 - Creation
 skaluska  02/12/06 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/execution/internals/stored/EvalContext.java /main/3 2008/10/17 05:15:15 udeshmuk Exp $
 *  @author  skaluska
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.execution.internals.stored;

import oracle.cep.dataStructures.internal.ITuple;
import oracle.cep.dataStructures.internal.ITuplePtr;
import java.util.Arrays;
import oracle.cep.execution.ExecException;
import oracle.cep.execution.internals.IEvalContext;
import oracle.cep.exceptions.ExecutionError;

/**
 * @author skaluska
 */
public class EvalContext implements IEvalContext
{
  int MAX_ROLES       = END_ROLES;

  ITuplePtr  rolePtrs[];

  /**
   * Constructor for EvalContext
   */
  public EvalContext()
  {
    this.rolePtrs = new ITuplePtr[MAX_ROLES];
  }
  
  private EvalContext(ITuplePtr [] rolePtrs)
  {
    // Array clone makes a shallow copy.
    this.rolePtrs = rolePtrs.clone();
  }

  public int addRoles(int num)
  {
    this.rolePtrs = (ITuplePtr[]) Arrays.asList(this.rolePtrs).toArray(new ITuplePtr[MAX_ROLES + num]);
    int cur_max = MAX_ROLES;
    MAX_ROLES = MAX_ROLES + num;
    return cur_max;
  }
  /**
   * Bind a tuple to a specified role
   * 
   * @param t
   *          Tuple
   * @param rolenum
   *          Role
   * @throws ExecException
   */
  public void bind(ITuplePtr t, int rolenum) throws ExecException
  {
    if (rolenum >= rolePtrs.length)
      throw new ExecException(ExecutionError.INVALID_ROLE, 
              oracle.cep.execution.internals.memory.EvalContext.getRoleName(rolenum), 
              rolePtrs.length);
    rolePtrs[rolenum] = t;
  }

  /**
   * Bind a tuple array to consecutive roles
   * @param t tuple array
   * @param startRole The role to which the first array element should be bound
   * @param length The number of elements of the array that need to be bound
   * @param isPrev True if this function is called from prev functionality 
   *                    processing in pattern operator
   * @param nullInputTuple used in prev functionality specific processing in 
   *                       in pattern operator
   * @throws ExecException
   */

  public int bind(ITuplePtr[] t, int startRole, int length, boolean isPrev,
                  ITuplePtr nullInputTuple) throws ExecException
  { 
    int cnt = 0;
    for(int i=0; i < length; i++)
    {
      if(isPrev && t[i]==null)
      {
        rolePtrs[startRole+i] = nullInputTuple;
      }
      else
      {
        rolePtrs[startRole+i] = t[i];
        if(t[i] != null) cnt++;
      }
    }
    return cnt;
  }
  
  public ITuple[] getRoles()
  {
    return null;
  }
  
  public ITuplePtr[] getRolePtrs()
  {
    return rolePtrs;
  }
  
  public Object clone() throws CloneNotSupportedException
  {
    return new EvalContext(rolePtrs);
  }
}
