/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/planmgr/codegen/XStreamFactory.java /main/8 2011/02/07 03:36:26 sborah Exp $ */

/* Copyright (c) 2006, 2011, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    Insert/Delete Stream Factory abstract class 

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    sborah      01/21/11 - remove eval.setContext
    sborah      10/14/09 - support for bigdecimal
    hopark      10/10/08 - remove statics
    hopark      10/09/08 - remove statics
    hopark      09/04/07 - eval optimize
    najain      04/11/07 - bug fix
    najain      12/04/06 - stores are not storage allocators
    parujain    08/11/06 - cleanup planmgr
    parujain    08/07/06 - timestamp datatype
    najain      06/18/06 - cleanup
    najain      05/05/06 - sharing support 
    ayalaman    04/26/06 - Insert/Delete Stream Factory abstract class 
    ayalaman    04/26/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/planmgr/codegen/XStreamFactory.java /main/7 2009/11/09 10:10:59 sborah Exp $
 *  @author  ayalaman
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.planmgr.codegen;

import oracle.cep.dataStructures.internal.ITuplePtr;
import oracle.cep.execution.operators.ExecOpt;
import oracle.cep.execution.indexes.HashIndex; 
import oracle.cep.execution.internals.TupleSpec;
import oracle.cep.execution.internals.IEvalContext;
import oracle.cep.execution.internals.AOp; 
import oracle.cep.execution.internals.BOp; 
import oracle.cep.execution.internals.IAEval;
import oracle.cep.execution.internals.AInstr;
import oracle.cep.execution.internals.IBEval;
import oracle.cep.execution.internals.BInstr;
import oracle.cep.execution.internals.IHEval;
import oracle.cep.execution.internals.HInstr;
import oracle.cep.execution.internals.Column;
import oracle.cep.execution.internals.factory.AEvalFactory;
import oracle.cep.execution.internals.factory.BEvalFactory;
import oracle.cep.execution.internals.factory.HEvalFactory;
import oracle.cep.memmgr.FactoryManager;
import oracle.cep.memmgr.IAllocator;
import oracle.cep.phyplan.PhyOpt;
import oracle.cep.service.ExecContext;
import oracle.cep.exceptions.CEPException;

/**
 * XStreamFactory
 *
 * The XStreamFactor is an abstract class that implements the methods that
 * are common to IStreamFactory and DStreamFactory 
 */
public abstract class XStreamFactory extends ExecOptFactory
{

  /* (non-Javadoc)
   * @see oracle.cep.planmgr.codegen.ExecOptFactory#newExecOpt(oracle.cep.phyplan.PhyOpt)
   * Create a new IStream execution operator 
   */
  @Override
  abstract ExecOpt newExecOpt(CodeGenContext ctx) throws CEPException;

  /**
   * Get the evaluator instance to increment the value in the count 
   * column of the tuple. 
   * @param ec TODO
   * @param     cts       constant tuple specification 
   * @param     countCol  index of the count column in the tuple
   *
   * @return    instance of the Arithmetic Evaluator.
   *
   * @throws    CEPException for errors encounted in creating instruction set
   */
  IAEval getIncrEval(ExecContext ec, ConstTupleSpec cts, int countCol)
                                              throws CEPException 
  {
    AInstr  instr = new AInstr(); 
    IAEval   aEval = AEvalFactory.create(ec); 

    // integer add operation 
    instr.op = AOp.INT_ADD;  

    // Old value of the count  
    instr.r1 = IEvalContext.SYN_ROLE; 
    instr.c1 = countCol;        // index of the count column

    // value to be added to old value 
    instr.r2 = IEvalContext.CONST_ROLE; 
    instr.c2 = cts.addInt(1);   // add a column with fixed value

    // dest - new count 
    instr.dr = IEvalContext.SYN_ROLE; // at the loc of the orig tuple
    instr.dc = countCol;        // in the same count column 

    // add the instruction to the evaluator 
    aEval.addInstr(instr); 

    // Now that last instruction has been added, compile
    aEval.compile();

    return aEval; 
  }

  /**
   * Get the evaluator instance to decrement the value in the count 
   * column of the tuple. 
   * @param ec TODO
   * @param     cts       constant tuple specification 
   * @param     countCol  index of the count column in the tuple
   *
   * @return    instance of the Arithmetic Evaluator. 
   *
   * @throws    CEPException for errors encounted in creating instruction set
   */
  IAEval getDecrEval(ExecContext ec, ConstTupleSpec cts, int countCol)
                                                 throws CEPException 
  {
    AInstr  instr = new AInstr(); 
    IAEval   aEval = AEvalFactory.create(ec); 

    // integer add operation 
    instr.op = AOp.INT_SUB;  

    // Old value of the count  
    instr.r1 = IEvalContext.SYN_ROLE; 
    instr.c1 = countCol;        // index of the count column

    // value to be subtracted from old value 
    instr.r2 = IEvalContext.CONST_ROLE; 
    instr.c2 = cts.addInt(1);   // add a column with fixed value
    
    // dest - new count 
    instr.dr = IEvalContext.SYN_ROLE; // at the loc of the original tuple
    instr.dc = countCol;        // in the same count column 

    // add the instruction to the evaluator 
    aEval.addInstr(instr); 

    // Now that last instruction has been added, compile
    aEval.compile();

    return aEval; 
  }

  /**
   * Get the count initialization evaluator. Also copy the input Tuple 
   * spec into the ConstTupleSpec in the synopsis 
   * @param ec TODO
   * @param     op        physical operator
   * @param     cts       constant tuple specification 
   * @param     countCol  index of the count column in the tuple 
   * 
   * @return    instance of the arithmetic evaluator 
   *
   * @throws    CEPException for errors encounted in creating instruction set
   */
  IAEval getInitEval(ExecContext ec, PhyOpt op, ConstTupleSpec cts, int countCol)
                                        throws    CEPException
  {
    AInstr instr; 
    IAEval  aEval = AEvalFactory.create(ec);
    int    numAttrs = op.getNumAttrs();

    // copy the data columns 
    for (int attr = 0; attr < numAttrs; attr++)
    {
      instr = new AInstr(); 

      instr.op = ExprHelper.getCopyOp(op.getAttrTypes(attr));
      instr.r1 = IEvalContext.INPUT_ROLE;
      instr.c1 = attr;
      instr.r2 = 0;
      instr.c2 = 0;
      instr.dr = IEvalContext.SYN_ROLE;
      instr.dc = attr;

      aEval.addInstr(instr); 
    }

    instr = new AInstr(); 

    // copy 0 into the column column which is known to be an integer. 
    instr.op = AOp.INT_CPY; 

    // value to be copied 0
    instr.r1 = IEvalContext.CONST_ROLE; 
    instr.c1 = cts.addInt(0);   // add a column with fixed value

    // destination : count column 
    instr.dr = IEvalContext.SYN_ROLE; 
    instr.dc = countCol;    

    // add the instruction to the evaluator 
    aEval.addInstr(instr); 

    // Now that last instruction has been added, compile
    aEval.compile();

    return aEval;     
  }

  /**
   * Get the evaluator instance to check if the current value stored
   * in the count column is greater than 0. 
   * @param ec TODO
   * @param     cts       constant tuple specification 
   * @param     countCol  index of the count column in the tuple
   *
   * @return    instance of the Boolean Evaluator. 
   *
   * @throws    CEPException for errors encounted in creating instruction set
   */
  IBEval getPosEval(ExecContext ec, ConstTupleSpec cts, int countCol)
                                        throws    CEPException
  {
    BInstr  instr = new BInstr(); 
    IBEval   bEval = BEvalFactory.create(ec); 

    // lhs > rhs check where rhs is 0
    instr.op = BOp.INT_GT;    // integer greater than operation 

    // lhs value count column 
    instr.r1 = IEvalContext.SYN_ROLE; 
    instr.c1 = new Column(countCol);  // index of the count column

    instr.r2 = IEvalContext.CONST_ROLE; 
    instr.c2 = new Column(cts.addInt(0));   // add a column with fixed value    

    // add the instruction to the evaluator 
    bEval.addInstr(instr); 
    
    // Now that last instruction has been added, compile
    bEval.compile();


    return bEval; 
  }

  /**
   * Get the evaluator instance to check if the current value stored
   * in the count column is less than 0. 
   * @param ec TODO
   * @param     cts       constant tuple specification 
   * @param     countCol  index of the count column in the tuple
   *
   * @return    instance of the Boolean Evaluator.
   *
   * @throws    CEPException for errors encounted in creating instruction set
   */
  IBEval getNegEval(ExecContext ec, ConstTupleSpec cts, int countCol)
                                              throws CEPException 
  {
    BInstr  instr = new BInstr(); 
    IBEval   bEval = BEvalFactory.create(ec); 

    // lhs < rhs check where rhs is 0
    instr.op = BOp.INT_LT;    // integer less than operation 

    // lhs value count column 
    instr.r1 = IEvalContext.SYN_ROLE; 
    instr.c1 = new Column(countCol);  // index of the count column
    instr.e1 = null; 

    instr.r2 = IEvalContext.CONST_ROLE; 
    instr.c2 = new Column(cts.addInt(0)); // add a column with fixed value    
    instr.e2 = null; 

    // add the instruction to the evaluator 
    bEval.addInstr(instr); 
    
    // Now that last instruction has been added, compile
    bEval.compile();
    
    return bEval;
  } 

  /**
   * Get the evaluator instance to check if the current value stored
   * in the count column is equal to 0. 
   * @param ec TODO
   * @param     cts       constant tuple specification 
   * @param     countCol  index of the count column in the tuple
   *
   * @return    instance of the Boolean Evaluator. 
   *
   * @throws    CEPException for errors encounted in creating instruction set
   */
  IBEval getZeroEval(ExecContext ec, ConstTupleSpec cts, int countCol)
      throws CEPException 
  {
    BInstr  instr = new BInstr(); 
    IBEval   bEval = BEvalFactory.create(ec); 

    // lhs = rhs check where rhs is 0
    instr.op = BOp.INT_EQ;    // integer equal to operation 

    // lhs value count column 
    instr.r1 = IEvalContext.SYN_ROLE; 
    instr.c1 = new Column(countCol);  // index of the count column
    instr.e1 = null; 

    instr.r2 = IEvalContext.CONST_ROLE; 
    instr.c2 = new Column(cts.addInt(0)); // add a column with fixed value
    instr.e2 = null; 

    // add the instruction to the evaluator 
    bEval.addInstr(instr); 
    
    // Now that last instruction has been added, compile
    bEval.compile();

    return bEval; 
  } 

  /**
   * Get the output evaluator. Ths method copies the data contents of 
   * ConstantTupleSpec into the output TupleSpec. 
   * @param ec TODO
   * @param     ts        tuple spec
   * 
   * @return    instance of the arithmetic evaluator 
   *
   * @throws    CEPException for errors encounted in creating instruction set
   */
  IAEval getOutEval(ExecContext ec, TupleSpec ts) throws CEPException
  {
    AInstr instr; 
    IAEval  aEval = AEvalFactory.create(ec);
    int    numAttrs = ts.getNumAttrs();

    // copy the data columns 
    for (int attr = 0; attr < numAttrs; attr++)
    {
      instr = new AInstr(); 

      instr.op = ExprHelper.getCopyOp(ts.getAttrType(attr));
      instr.r1 = IEvalContext.SYN_ROLE;
      instr.c1 = attr;
      instr.r2 = 0;
      instr.c2 = 0;
      instr.dr = IEvalContext.NEW_OUTPUT_ROLE;
      instr.dc = attr;

      aEval.addInstr(instr); 
    }
    
    // Now that last instruction has been added, compile
    aEval.compile();

    return aEval;     
  }

  /**
   * Setup and return the index for searching on the synopsis
   * @param ec TODO
   * @param    evalCtx     evaluation context for the operator 
   * @param    tupSpec     tuple specification 
   * 
   * @return  an instance of the HashIndex set up to search the synopsis
   *
   * @throws  CEPException when not able create instructions for the index
   */
  HashIndex getSynCountIndex(ExecContext ec, IEvalContext evalCtx, 
                             TupleSpec tupSpec, IAllocator<ITuplePtr> factory)
      throws CEPException
  {
    FactoryManager factoryMgr = ec.getServiceManager().getFactoryManager();
    HashIndex  synIndex = new HashIndex(ec); // hash index on synopsis
    HInstr     hInstr; 
    IHEval      updHash, scanHash; 
    IBEval      keyEqual; 
    BInstr     bInstr; 
    int        numAttrs = tupSpec.getNumAttrs();

    updHash = HEvalFactory.create(ec, numAttrs); 
    for (int attr = 0; attr < numAttrs; attr++)
    {
      hInstr = new HInstr(tupSpec.getAttrType(attr),
                          IEvalContext.UPDATE_ROLE,
                          new Column(attr));

      updHash.addInstr(hInstr); 
    }
    
    updHash.compile();

    scanHash = HEvalFactory.create(ec, numAttrs); 
    
    for (int attr = 0; attr < numAttrs; attr++)
    {
      hInstr = new HInstr(tupSpec.getAttrType(attr),
                          IEvalContext.INPUT_ROLE,
                          new Column(attr)); 
     
      scanHash.addInstr(hInstr); 
    }
    scanHash.compile();

    keyEqual = BEvalFactory.create(ec); 
    for (int attr = 0; attr < numAttrs; attr++)
    {
      bInstr = new BInstr(); 
      // compare the attributes in the INPUT tuple with the tuple 
      // in the scan role 
      bInstr.op = ExprHelper.getEqOp(tupSpec.getAttrType(attr));
     
      bInstr.r1 = IEvalContext.INPUT_ROLE; 
      bInstr.c1 = new Column(attr); 
   
      bInstr.r2 = IEvalContext.SCAN_ROLE; 
      bInstr.c2 = new Column(attr); 

      keyEqual.addInstr(bInstr); 
    }

    keyEqual.compile();

    synIndex.setUpdateHashEval(updHash); 
    synIndex.setScanHashEval(scanHash); 
    synIndex.setKeyEqual(keyEqual); 
    synIndex.setEvalContext(evalCtx);
    synIndex.setFactory(factory);
    synIndex.initialize(); 
    
    return synIndex; 
  }
}
