/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/internals/memory/HEval.java /main/6 2011/02/07 03:36:26 sborah Exp $ */

/* Copyright (c) 2006, 2011, Oracle and/or its affiliates. 
All rights reserved. */

/*
 DESCRIPTION
 Declares HEval in package oracle.cep.execution.internals.
 
 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>
 
 NOTES
 <other useful comments, qualifications, etc.>
 
 MODIFIED    (MM/DD/YY)
 anasrini  12/19/10 - remove eval() and setEvalContext
 anasrini  12/13/10 - eval parallelism
 sborah    05/21/09 - remove max_instrs limit
 hopark    02/05/08 - parameterized error
 hopark    09/04/07 - optimize
 hopark    06/19/07 - cleanup
 hopark    04/20/07 - change pinTuple semantics
 hopark    03/24/07 - optimize pin
 najain    03/14/07 - cleanup
 najain    03/12/07 - bug fix
 najain    01/10/07 - spill-over support
 hopark    11/16/06 - add bigint datatype
 parujain  10/09/06 - Interval datatype
 parujain  09/01/06 - Handling of nulls
 anasrini  08/09/06 - bug 5454682
 parujain  08/08/06 - join test
 parujain  08/04/06 - Timestamp datastructure
 skaluska  03/01/06 - Creation
 skaluska  03/01/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/internals/memory/HEval.java st_pcbpel_anasrini_eval_parallelism_2/2 2010/12/20 07:47:45 anasrini Exp $
 *  @author  skaluska
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.execution.internals.memory;

import java.util.LinkedList;

import oracle.cep.dataStructures.internal.ITuple;
import oracle.cep.execution.ExecException;
import oracle.cep.execution.internals.HInstr;
import oracle.cep.execution.internals.Hash;
import oracle.cep.execution.internals.IEvalContext;
import oracle.cep.execution.internals.IHEval;

/**
 * HEval
 *
 * @author skaluska
 */
public class HEval implements IHEval
{
  /** number of instructions */
  protected int    numInstrs;
  /** instructions */
  protected HInstr instrs[];
  protected LinkedList<HInstr> instrsList;
  
  private boolean isCompiled;

  /**
   * Constructor for HEval
   * 
   * @param maxInstrs
   *          Maximum instructions
   */
  public HEval()
  {
    instrs     = null;
    instrsList = new LinkedList<HInstr>();
    isCompiled = false;
  }

  public boolean isCompiled()
  {
    return isCompiled;
  }

  /**
   * Set EvalContext
   * 
   * @param evalContext
   *          EvalContext
   */
  public void compile()
  {
    assert !isCompiled() : "Invalid attempt to compile Instructions twice";
    
    // initialize the instrs array with the contents on the arraylist
    if(this.instrs == null || this.instrs.length < numInstrs)
    {
      this.instrs = this.instrsList.toArray(new HInstr[numInstrs]);
    }
    
    this.isCompiled = true;
  }

  /**
   * add an instruction
   * 
   * @param instr
   *          instruction to be added
   * @throws ExecException
   */
  public void addInstr(HInstr instr) throws ExecException
  {
    assert !isCompiled() : "Invalid attempt to add instructions after compilation.";
    
    instrsList.add(instr);
    numInstrs++;
  }

  /**
   * Evaluate
   * 
   * @param h
   *          Hash value to be populated
   * @throws ExecException
   */
  public void eval(Hash h, IEvalContext ec) throws ExecException
  {
    ITuple[] roles = ec.getRoles();

    eval(h, ec, roles);
  }

  protected void eval(Hash h, IEvalContext ec, ITuple[] roles)
    throws ExecException
  {
    assert isCompiled() : "Invalid call to eval before compiling instructions.";
    
    int hash  = 5381;
    
    for (int i = 0; i < numInstrs; i++) {
      HInstr inst = instrs[i];
      ITuple t = roles[inst.r];
      int col =  inst.c.getColnum();
      hash = t.heval(inst.type, col, hash);
    }

    h.setHashValue(hash);
 }

  // toString
  public String toString()
  {
    StringBuffer sb = new StringBuffer();
    sb.append("<HEval numInstrs=\"" + numInstrs + "\" >");
    for (int i = 0; i < numInstrs; i++)
    {
      sb.append(instrs[i].toString());
    }
    sb.append("</HEval>");
    return sb.toString();
  }
}

