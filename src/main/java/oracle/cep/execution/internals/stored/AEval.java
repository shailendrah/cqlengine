/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/internals/stored/AEval.java /main/9 2011/02/07 03:36:26 sborah Exp $ */

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
    hopark      10/07/08 - use execContext to remove statics
    hopark      10/16/08 - fix NPE
    parujain    05/19/08 - xmlinstr
    parujain    05/15/08 - fix problem for XML pub funcs
    hopark      12/07/07 - cleaup spill
    parujain    12/07/07 - support for ExternalInstr
    hopark      09/06/07 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/internals/stored/AEval.java st_pcbpel_anasrini_eval_parallelism_2/2 2010/12/20 07:47:45 anasrini Exp $
 *  @author  hopark  
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.execution.internals.stored;

import java.util.Arrays;

import oracle.cep.dataStructures.internal.ITuple;
import oracle.cep.dataStructures.internal.ITuplePtr;
import oracle.cep.execution.ExecException;
import oracle.cep.execution.internals.AInstr;
import oracle.cep.execution.internals.ExternalInstr;
import oracle.cep.execution.internals.IEvalContext;
import oracle.cep.execution.internals.XMLAggInstr;
import oracle.cep.execution.internals.XMLElementInstr;
import oracle.cep.execution.internals.XMLParseInstr;
import oracle.cep.memmgr.IPinnable;
import oracle.cep.service.ExecContext;

public class AEval extends oracle.cep.execution.internals.memory.AEval
{
  
  public AEval(ExecContext ec)
  {
    super(ec);
  }
  
  public void compile()
  {
    super.compile();
  }
  
  @SuppressWarnings("incomplete-switch")
public void eval(IEvalContext ec) throws ExecException
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
        AInstr inst = instrs[i];
        ExternalInstr einst = inst.extrInstr;
        XMLElementInstr xmlinst = inst.xmlInstr;
       
        int pos = inst.dr;
        if (pos >= 0)
          m_pinModes[pos] = IPinnable.WRITE;
       
        pos = inst.r1;
        if (pos >= 0 && m_pinModes[pos] != IPinnable.WRITE)
          m_pinModes[pos] = IPinnable.READ;
        pos = inst.r2;
        if (pos >= 0 && m_pinModes[pos] != IPinnable.WRITE)
          m_pinModes[pos] = IPinnable.READ;
        switch(inst.op)
        {
          case RELEASE_AGGR_HANDLERS:
          case RESET_AGGR_HANDLERS:
          case ALLOC_AGGR_HANDLERS:
            m_pinModes[inst.r1] = IPinnable.WRITE;
            break;
            /*
          case USR_FNC:
          case UDA_PLUS_MULTIARG_HANDLE:
          case UDA_MINUS_MULTIARG_HANDLE:
            for (int p : inst.argRoles)
            {
              if (m_pinModes[p] != IPinnable.WRITE)
                m_pinModes[p] = IPinnable.READ;
            }
            break;
            */
        }
        if (inst.argRoles != null)
        {
          for (int p : inst.argRoles)
          {
            if (m_pinModes[p] != IPinnable.WRITE)
              m_pinModes[p] = IPinnable.READ;
          }
        }
        if(einst != null)
        {
          for(int k=0; k<einst.numArgs; k++)
          {
            int p = einst.getArgRoles().get(k);
            if(m_pinModes[p] !=  IPinnable.WRITE)
              m_pinModes[p] = IPinnable.READ;
          }
        }
        if(xmlinst != null)
        {
          if(xmlinst.elemNameRole != -1)
          {
            int p = xmlinst.elemNameRole;
            if(m_pinModes[p] !=  IPinnable.WRITE)
              m_pinModes[p] = IPinnable.READ;
          }
          for(int a=0; a<xmlinst.numAttributs; a++)
          {
            int p = xmlinst.attrRoles[a];
            if(m_pinModes[p] !=  IPinnable.WRITE)
               m_pinModes[p] = IPinnable.READ;
            int rol = xmlinst.attrNames[a].getAttrRole();
            if(rol != -1)
            {
              if(m_pinModes[rol] != IPinnable.WRITE)
                m_pinModes[rol] = IPinnable.READ;
            }
          }
          for(int b=0; b<xmlinst.numChild; b++)
          {
            int p = xmlinst.childRoles[b];
            if(m_pinModes[p] !=  IPinnable.WRITE)
              m_pinModes[p] = IPinnable.READ;
          }
        }
        if (inst.xmlParseInstr != null)
        {
          XMLParseInstr pinst = inst.xmlParseInstr;
          int p = pinst.getArgRole();
          if(m_pinModes[p] !=  IPinnable.WRITE)
            m_pinModes[p] = IPinnable.READ;
        }
        if (inst.xmlAggInstr != null)
        {
          XMLAggInstr pinst = inst.xmlAggInstr;
          int p = pinst.argRole;
          if(m_pinModes[p] !=  IPinnable.WRITE)
            m_pinModes[p] = IPinnable.READ;
          p = pinst.oldOutputRole;
          if(m_pinModes[p] !=  IPinnable.WRITE)
            m_pinModes[p] = IPinnable.READ;
          p = pinst.newOutputRole;
          m_pinModes[p] = IPinnable.WRITE;
        }
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
      super.eval(ec, m_roles, m_rolePtrs);
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
