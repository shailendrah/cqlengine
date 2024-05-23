/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/internals/stored/HEval.java /main/5 2011/02/07 03:36:26 sborah Exp $ */

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
    anasrini    12/20/10 - remove setEvalContext
    anasrini    12/19/10 - remove eval() and setEvalContext
    anasrini    12/13/10 - eval parallelism
    sborah      05/21/09 - remove max_instrs limit
    hopark      12/07/07 - cleaup spill
    hopark      09/06/07 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/internals/stored/HEval.java st_pcbpel_anasrini_eval_parallelism_2/2 2010/12/20 07:47:45 anasrini Exp $
 *  @author  hopark  
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.execution.internals.stored;

import java.util.Arrays;

import oracle.cep.dataStructures.internal.ITuple;
import oracle.cep.dataStructures.internal.ITuplePtr;
import oracle.cep.execution.ExecException;
import oracle.cep.execution.internals.HInstr;
import oracle.cep.execution.internals.Hash;
import oracle.cep.execution.internals.IEvalContext;
import oracle.cep.memmgr.IPinnable;

public class HEval extends oracle.cep.execution.internals.memory.HEval
{
  
  public HEval()
  {
    super();
  }

  public void compile()
  {
    super.compile();
  }
  
  public void eval(Hash h, IEvalContext ec) throws ExecException
  {
    assert isCompiled() : "Invalid call to eval before compiling instructions.";
   
    ITuplePtr m_rolePtrs[] = ec.getRolePtrs();
    ITuple    m_roles[]    = new ITuple[m_rolePtrs.length];
    int[]     m_pinModes   = null;

    if (m_pinModes == null)
    {
      m_pinModes = new int[m_rolePtrs.length];
      Arrays.fill(m_pinModes, -1);
      for (int i = 0; i < numInstrs; i++)
      {
        HInstr inst = instrs[i];
        m_pinModes[inst.r] = IPinnable.READ;
      }
    }

    for (int j = 0; j < m_pinModes.length; j++)
    {
      int mode = m_pinModes[j];
      ITuple t = null;
      if (mode >= 0 &&  m_rolePtrs[j] != null)
      {
        t = m_rolePtrs[j].pinTuple(mode);
      } 
      m_roles[j] = t;
    }
    
    try {
      super.eval(h, ec, m_roles);
    }
    finally
    {
      for (int j = 0; j < m_pinModes.length; j++)
      {
        if (m_roles[j] != null)
        {
          m_rolePtrs[j].unpinTuple();
        }
      }
    }
  }
}

