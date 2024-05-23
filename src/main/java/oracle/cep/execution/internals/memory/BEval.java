/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/internals/memory/BEval.java /main/7 2011/05/26 19:23:39 vikshukl Exp $ */

/* Copyright (c) 2006, 2011, Oracle and/or its affiliates. 
All rights reserved. */

/*
 DESCRIPTION
 Declares BEval in package oracle.cep.execution.internals.
 
 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>
 
 NOTES
 <other useful comments, qualifications, etc.>
 
 MODIFIED    (MM/DD/YY)
 vikshukl  05/26/11 - XbranchMerge vikshukl_bug-11736605_ps5 from
                      st_pcbpel_11.1.1.4.0
 vikshukl  05/11/11 - change eval() to account for conditional jumps
 anasrini  12/19/10 - remove eval() and setEvalContext
 anasrini  12/13/10 - eval parallelism
 sborah    05/21/09 - remove max_instrs limit
 hopark    02/05/08 - parameterized error
 hopark    09/04/07 - optimize
 hopark    06/19/07 - cleanup
 najain    05/11/07 - variable length support
 hopark    04/20/07 - change pinTuple semantics
 hopark    03/24/07 - optimize pin
 najain    03/14/07 - cleanup
 najain    03/12/07 - bug fix
 najain    01/10/07 - spill-over support
 rkomurav  12/14/06 - add complex operators for is_null
 hopark    11/16/06 - add bigint datatype
 hopark    11/09/06 - bug 5505056, turn off null = null
 parujain  11/20/06 - XOR implementation
 parujain  11/16/06 - OR/NOT implementation
 parujain  11/09/06 - Logical Operator execution
 dlenkov   10/20/06 - byte data type fixes
 parujain  10/09/06 - Interval datatype
 parujain  10/02/06 - Support for Like
 parujain  09/21/06 - to_timestamp implementation
 parujain  09/28/06 - is null implementation
 parujain  09/27/06 - Complex Timestamp handling
 parujain  09/21/06 - to_timestamp implementation
 parujain  08/30/06 - Handle null values
 parujain  08/04/06 - Timestamp datastructure
 anasrini  03/30/06 - add default constructor 
 skaluska  02/12/06 - Creation
 skaluska  02/12/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/internals/memory/BEval.java st_pcbpel_anasrini_eval_parallelism_2/2 2010/12/20 07:47:45 anasrini Exp $
 *  @author  skaluska
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.execution.internals.memory;

import java.util.LinkedList;

import oracle.cep.dataStructures.internal.ITuple;
import oracle.cep.execution.ExecException;
import oracle.cep.execution.internals.BInstr;
import oracle.cep.execution.internals.BOp;
import oracle.cep.execution.internals.IBEval;
import oracle.cep.execution.internals.IEvalContext;
import oracle.cep.util.BitVectorUtil;

/**
 * Binary Evaluator
 *
 * @author skaluska
 */
public class BEval implements IBEval
{
  protected int numInstrs;
  protected BInstr instrs[];
  protected LinkedList<BInstr> instrsList;
  
  // by default (null = null) is true
  // However, we are using the same BEval for expression evaluation
  // and for HashIndex and should treat the BEvals for HashIndex specially. 
  // When an operator uses a HashIndex and needs to treat (null = null) false,
  // it needs to set n2ntrue=false by the codegen.
  protected boolean n2ntrue = true;
  
  protected boolean isCompiled;

  /**
   * Default Constructor
   */
  public BEval()
  {
    super();
    
    numInstrs  = 0;
    instrs     = null; 
    instrsList = new LinkedList<BInstr>();
    isCompiled = false;
  }
  
  public boolean isCompiled()
  {
    return isCompiled;
  }

  /**
   * Set (null = null) mode
   * 
   * @param b false to set (null = null) is false.
   * 
   */
  public void setNullEqNull(boolean b) 
  {
    n2ntrue = b;
  }
  
  public void addInstr(BInstr instr) throws ExecException
  {
    assert !isCompiled() : "Invalid attempt to add instructions after compilation.";
    instrsList.add(instr);
    numInstrs++;
  }
  
  /**
   * @return number of instructions added so far
   */
  public int getNumInstrs() 
  {
    return numInstrs;
  }

  /** 
   * @param index 
   *        index of the instruction   
   * @return 
   *        BInstr at the location passed in.
   *    
   * For example: 
   * This function is used to locate the most recently added jump instriction,
   * i.e. the one after the left operand tree.  It is used in
   * processComplexBoolExpr to backpatch the jump address in the jump
   * instruction.
   */
  public BInstr getInstrAtLoc(int index)
  {
    return instrsList.get(index);
  }  
  
  public void compile()
  {
    assert !isCompiled() : "Invalid attempt to compile Instructions twice";
    
    // initialize the instrs array with the contents on the arraylist
    if(this.instrs == null || this.instrs.length < numInstrs)
    {
      this.instrs = this.instrsList.toArray(new BInstr[numInstrs]);
    }
    
    this.isCompiled = true; 

  }
  
  private void setBooleanLoc(BInstr a, ITuple[] roles) throws ExecException
  {
    int r   = a.dr;
    int pos = a.dc;
    
    ITuple t = roles[r];
    byte[] arr = t.bValueGet(pos);
    arr = BitVectorUtil.setBit(arr, a.db);
    t.bValueSet(pos, arr, arr.length);
  }
  
  /**
   * 
   * @param  a 
   *         instruction location of the root of the left operator
   *         subtree of the OR/AND operator.
   * @param  roles
   * @return computed boolean value of the instruction at the root
   *         of the left subtree. 
   * @throws ExecException
   */
  private boolean getBooleanLoc(BInstr a, ITuple[] roles) throws ExecException
  {
    int r   = a.dr;
    int pos = a.dc;
    
    ITuple t = roles[r];
    byte[] arr = t.bValueGet(pos);
    return BitVectorUtil.checkBit(arr, a.db);
  }  
  
  private boolean ifValidBooleanLoc(BInstr a) throws ExecException
  {
    return a.valid;
  }
  
  private void clearBooleanLoc(BInstr a, ITuple[] roles) throws ExecException
  {
    int r   = a.dr;
    int pos = a.dc;
    
    ITuple t = roles[r];
    byte[] arr = t.bValueGet(pos);
    arr = BitVectorUtil.clearBit(arr, a.db);
    t.bValueSet(pos, arr, arr.length);
  }
  
  // By default, (null = null) is true.
  public boolean eval(IEvalContext ec) throws ExecException
  {
    ITuple[] roles = ec.getRoles();

    return eval(ec, roles);
  }

  protected boolean eval(IEvalContext ec, ITuple[] roles) throws ExecException
  {
    boolean  result = true;
    int i = 0;
   
    while (i < numInstrs)
    {
      boolean flag = true;
      BInstr inst = instrs[i];
      BOp op = inst.op;
      
      if (op == BOp.JUMP_IF_FALSE) {
        // AND predicate: short circuit of false.
        // a. extract the return value of left substree
        // b. if it is false 
        //    - skip computation of the entire right substree
        //    - set the return value of the root of the entire tree 
        //      (found at the jump address) to false
        //      UNLESS we're computing intermediate result of the 
        //      predicates ANDed together.
        //    - increment instruction counter.
        // TODO: add more notes about how predicates are broken down.

        if (!getBooleanLoc(inst, roles)) 
        {          
          int jumploc = inst.addr;
          assert jumploc != -1 ||   // something is amiss, can't be -1
                 jumploc <= i;      
                    
          if (ifValidBooleanLoc(instrs[jumploc])) 
          {
            /* it is an intermediate results of one of the AND predicates in
             * the chain, so set the result to false and break.  this happens
             * when a bunch of OR predicates as a group are ANDed
             */
            clearBooleanLoc(instrs[jumploc], roles);
            i  = jumploc + 1;
            continue;
          }
          else 
          {
            /* this is not an intermediate result */
            result = false;
            break;
          }    
        }
        else 
        {
          /* true, we still need to evaluate right hand side and can't jump */
          i += 1;
          continue; 
        }
      }
      else if (op == BOp.JUMP_IF_TRUE) 
      {
        /* OR predicate: short circuit on true.
         *
         * a. extract the return value of the left substree
         * b. if it is true 
         *    - skip computation of the entire right substree,
         *    - set the return value of the root of of the entire tree 
         *      (found at the jump address) to true
         *    - set the index to jumploc
         */       
        if (getBooleanLoc(inst, roles)) 
        {
          int jumploc = inst.addr;
          assert jumploc != -1 ||   // something is amiss, can't be -1
                 jumploc <= i;      
          
          if (ifValidBooleanLoc(instrs[jumploc])) 
          {
            /* it is an intermediate result but has true value so far. This
             * happens when bunch of complex OR predicates are ANDed together
             * and we're computing the final return value of one of AND
             * branches. So the evaluation will move onto next AND branch.
             */
            setBooleanLoc(instrs[jumploc], roles);
            i = jumploc + 1;
            continue;
          }
          else 
          {
            /* this is not a intermediate result also note that the default
             * result is true
             * Remember the value is still true, so safely go past right tree
             * (Used to be i++)
             */
            i = jumploc + 1;
            continue;
          }
        }
        else 
        {
          /* false, we still need to evaluate right hand side and can't jump */
          i += 1; 
          continue;
        }
      }
      
      ITuple t1 = roles[inst.r1];
      ITuple t2 = roles[inst.r2];
      int col1 =  inst.c1.getColnum();
      int col2 = (inst.c2 != null ? inst.c2.getColnum() : -1);
      
      if (op.convert >= 1)
      {  
        if (inst.e1 != null)
          inst.e1.eval(ec);
      }
      if (op.convert >= 2)
      {
        if (inst.e2 != null)
          inst.e2.eval(ec);
      }  
      
      if (op.op != ITuple.Op.NOOP)
      {
        if (!t1.beval(op.type, op.op, col1, inst.b1,
            t2, col2, inst.b2, inst.pattern, n2ntrue))
          flag = false;
      } 
      else
      {
        // Handle ops not supported by Tuple eval.
      }
      if(ifValidBooleanLoc(inst))
      {
        if(!flag)
          clearBooleanLoc(inst, roles);
        else
          setBooleanLoc(inst, roles);
      }
      else
      {
        if(!flag) 
        {
          result = false;
          break;
        }
      }
      i++; 
    }
    return result;
  }
  
  // toString
  public String toString()
  {
    StringBuffer sb = new StringBuffer();
    sb.append("<BEval numInstrs=\"" + numInstrs + "\" >");
    for (int i = 0; i < numInstrs; i++)
    {
      sb.append(instrs[i].toString());
    }
    sb.append("</BEval>");
    return sb.toString();
  }
}

