/* $Header: pcbpel/cep/server/src/oracle/cep/logplan/LogOptMinus.java /main/3 2008/10/20 21:33:40 sborah Exp $ */

/* Copyright (c) 2007, 2008, Oracle and/or its affiliates. All rights reserved.*/

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    sborah      10/17/08 - setting numComparisonAttrs
    udeshmuk    10/30/07 - remove setters and add a new constructor.
    udeshmuk    10/20/07 - add function to set comparison attrlist.
    sbishnoi    09/26/07 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/logplan/LogOptMinus.java /main/3 2008/10/20 21:33:40 sborah Exp $
 *  @author  sbishnoi
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.logplan;

import oracle.cep.logplan.attr.Attr;
import oracle.cep.semantic.SetOpQuery;

public class LogOptMinus extends LogOpt {
  
  boolean isNotInSetOp = false; 
  
  /** 
   * V1(c1 integer, c2 float, c3 integer) IN / NOT IN V2(d1 integer, c2 float)
   * Here leftComparisonAttrs = {V1.c2}
   * and rightComparisonAttrs = {V2.c2}
   */
  private Attr[]   leftComparisonAttrs;
  private Attr[]   rightComparisonAttrs;
  private int      numComparisonAttrs;
  

  public LogOptMinus( LogOpt left, LogOpt right) {

    super( LogOptKind.LO_MINUS);

    // Always false
    setIsStream( false);
    
    // if RelSetOp = MINUS then
    //   Schema is the schema of the left input (which should be identical
    //   to the schema of the right input
    
    assert (left.getNumOutAttrs() == right.getNumOutAttrs());
    doSetup(left, right);
    this.numComparisonAttrs   = left.getNumOutAttrs();
    this.leftComparisonAttrs  = null;
    this.rightComparisonAttrs = null;
  }
  
  public LogOptMinus(LogOpt left, LogOpt right, oracle.cep.semantic.Attr[] leftCompAttrs,
      oracle.cep.semantic.Attr[] rightCompAttrs, int numCompAttrs)
  {
    super(LogOptKind.LO_MINUS);
    this.isNotInSetOp = true;
    doSetup(left, right);
    
    // Set Comparison Attributes
    
    this.numComparisonAttrs   = numCompAttrs;
    this.leftComparisonAttrs  = new Attr[numComparisonAttrs];
    this.rightComparisonAttrs = new Attr[numComparisonAttrs];
      
    for(int i=0; i < numComparisonAttrs; i++)
    {
      this.leftComparisonAttrs[i]  = 
        LogPlanHelper.transformAttr(leftCompAttrs[i]);
      this.rightComparisonAttrs[i] = 
        LogPlanHelper.transformAttr(rightCompAttrs[i]);
    }
    
  }
  
  private void doSetup(LogOpt left, LogOpt right)
  {
    numOutAttrs = left.getNumOutAttrs();
    setOutAttrs( left.getOutAttrs());

    setNumInputs( 2);
    setInput( 0, left);
    setInput( 1, right);

    setOutput( null);

    left.setOutput( this);
    right.setOutput( this);

  }
  @Override
  protected void validate() throws LogicalPlanException
  {
    checkUnboundStream();
  }

  public LogOptMinus() {
    super();
  }
  
  /**
   * Return true  if it is NOT IN Set operation
   * Return false if it is MINUS  Set operation
   * @return isNotInSetOp
   */
  public boolean getIsNotInSetOp()
  {
    return this.isNotInSetOp;
  }
  
  /**
   * Get Number of comparison attributes
   * @return numComparisonAttrs
   */
  public int getNumComparisonAttrs()
  {
    return this.numComparisonAttrs;
  }
  
  /**
   * Get Left Comparison Attribute array
   * @return leftComparisonAttrs
   */
  public Attr[] getLeftComparisonAttrs()
  {
    return this.leftComparisonAttrs;
  }
  
  /**
   * Get Right Comparison Attribute Array
   * @return rightComparisonAttrs
   */
  public Attr[] getRightComparisonAttrs()
  {
    return this.rightComparisonAttrs;
  }
 
}
