/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/internals/IHEval.java /main/3 2011/02/07 03:36:25 sborah Exp $ */

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
    anasrini    12/19/10 - remove eval() and setEvalContext
    anasrini    12/13/10 - eval parallelism
    hopark      09/07/07 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/internals/IHEval.java st_pcbpel_anasrini_eval_parallelism_2/2 2010/12/20 07:47:44 anasrini Exp $
 *  @author  hopark  
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.execution.internals;

import oracle.cep.execution.ExecException;
import oracle.cep.execution.internals.IEvalContext;

public interface IHEval
{
  void compile();
  boolean isCompiled();
  void addInstr(HInstr instr) throws ExecException;
  void eval(Hash h, IEvalContext ec) throws ExecException;
}
