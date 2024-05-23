/* $Header: pcbpel/cep/server/src/oracle/cep/phyplan/PhyOptOutputIter.java /main/2 2009/02/23 06:47:36 sborah Exp $ */

/* Copyright (c) 2006, 2009, Oracle and/or its affiliates.
All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    sborah      12/17/08 - handle constants
    najain      06/21/06 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/phyplan/PhyOptOutputIter.java /main/2 2009/02/23 06:47:36 sborah Exp $
 *  @author  najain  
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.phyplan;

import java.util.ArrayList;
import java.util.BitSet;

import oracle.cep.exceptions.PhysicalPlanError;

/**
 * PhyOpt Outputs Iterator
 * 
 * @author najain
 */
public class PhyOptOutputIter 
{
  private PhyOpt            op;
  private ArrayList<PhyOpt> outputs;  
  private BitSet            activeOutputs;
  private int               curPos;

  PhyOptOutputIter(PhyOpt op) 
  {
    this.op            = op;
    this.outputs       = null;
    this.activeOutputs = null;
    curPos             = -1;
  }
  
  PhyOptOutputIter(PhyOpt op, ArrayList<PhyOpt> outputs, BitSet activeOutputs) 
  {
    this.op            = op;
    this.outputs       = outputs;
    this.activeOutputs = activeOutputs;
    curPos             = -1;
  }

  public void initialize() {
    curPos = -1;
  }

  public void initialize(ArrayList<PhyOpt> outputs, BitSet activeOutputs) 
  {
    this.outputs       = outputs;
    this.activeOutputs = activeOutputs;
    curPos             = -1;
  }

  public PhyOpt getNext() throws PhysicalPlanException {
    curPos = activeOutputs.nextSetBit(curPos+1);

    if (curPos == -1)
      return null;

    PhyOpt op = outputs.get(curPos);
    assert op != null;
    return op;
  }

  public void set(PhyOpt op) throws PhysicalPlanException {
    if (curPos == -1)
      throw new PhysicalPlanException(PhysicalPlanError.ITERATOR_NOT_CURRENT);
   
    outputs.set(curPos, op); 
    
  }

  public void remove() throws PhysicalPlanException {
    if (curPos == -1)
      throw new PhysicalPlanException(PhysicalPlanError.ITERATOR_NOT_CURRENT);

    if (activeOutputs.get(curPos) == false)
      throw new PhysicalPlanException(PhysicalPlanError.ITERATOR_NOT_CURRENT);
 
    op.removeOutput(curPos);
  }
}

