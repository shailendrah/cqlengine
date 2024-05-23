/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/internals/IBEval.java /main/4 2011/05/26 19:23:39 vikshukl Exp $ */

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
    vikshukl    05/26/11 - XbranchMerge vikshukl_bug-11736605_ps5 from
                           st_pcbpel_11.1.1.4.0
    vikshukl    05/16/11 - get instruction count
    anasrini    12/19/10 - remove eval() and setEvalContext
    anasrini    12/13/10 - eval parallelism
    hopark      09/07/07 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/internals/IBEval.java st_pcbpel_anasrini_eval_parallelism_2/2 2010/12/20 07:47:44 anasrini Exp $
 *  @author  hopark  
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.execution.internals;

import oracle.cep.execution.ExecException;
import oracle.cep.execution.internals.IEvalContext;

public interface IBEval
{
  void setNullEqNull(boolean b);
  void addInstr(BInstr instr) throws ExecException;
  void compile();
  boolean isCompiled();
  boolean eval(IEvalContext ec) throws ExecException;
  int getNumInstrs();
  BInstr getInstrAtLoc(int index);
}

