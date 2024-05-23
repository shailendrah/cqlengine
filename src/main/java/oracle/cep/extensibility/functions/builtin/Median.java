/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/extensibility/functions/builtin/Median.java /main/4 2013/08/04 09:44:08 sbishnoi Exp $ */

/* Copyright (c) 2010, 2013, Oracle and/or its affiliates. 
All rights reserved.*/

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    udeshmuk    10/12/11 - XbranchMerge udeshmuk_bug-13060688_ps5 from
                           st_pcbpel_11.1.1.4.0
    udeshmuk    10/10/11 - XbranchMerge udeshmuk_bug-11933156_ps5 from
                           st_pcbpel_11.1.1.4.0
    udeshmuk    10/02/11 - add non-null check before cloning
    udeshmuk    09/29/11 - implement clone
    sbishnoi    02/06/11 - cleanup
    sborah      11/29/10 - median function using Augmented RBTree
    sborah      11/29/10 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/extensibility/functions/builtin/Median.java /main/4 2013/08/04 09:44:08 sbishnoi Exp $
 *  @author  sborah  
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.extensibility.functions.builtin;

import java.io.Serializable;

import java.math.BigDecimal;
import java.math.RoundingMode;

import oracle.cep.exceptions.UDAError;
import oracle.cep.extensibility.functions.AggrFunctionImpl;
import oracle.cep.extensibility.functions.AggrValue;
import oracle.cep.extensibility.functions.IAggrFnFactory;
import oracle.cep.extensibility.functions.IAggrFunction;
import oracle.cep.extensibility.functions.UDAException;
import oracle.cep.util.RBTree;

public class Median extends AggrFunctionImpl implements IAggrFnFactory , Cloneable, Serializable
{
  /** Augmented RBTree data structure */
  private RBTree tree;
  
  /**
   Constructor
   */
  public Median()
  {
    tree = new RBTree();
  }
  
  @Override
  public IAggrFunction newAggrFunctionHandler() throws UDAException
  {
    return new Median();
  }

  @Override
  public void freeAggrFunctionHandler(IAggrFunction handler)
      throws UDAException
  {
    tree = null;    
  }

  @Override
  public void initialize() throws UDAException
  {
    tree = new RBTree();
  }
  
  public void handlePlus(AggrValue[] args, AggrValue result) 
  throws UDAException 
  {
    try
    {
      processInput(args, result, true);
    }
    catch(UDAException e) {throw e;}    
  }
  
  public void handleMinus(AggrValue[] args, AggrValue result) 
  throws UDAException 
  {
    try
    {
      processInput(args, result, false);
    }
    catch(UDAException e) {throw e;}    
  }
  
  public void processInput(AggrValue[] args, AggrValue result, boolean isInsert) 
    throws UDAException
  {
    // Get the Comparable value from AggrValue node
    // Update the tree data structure
    // Get the middle element and tree size
    // Update aggregate results
    
    AggrValue  inpVal  = args[0];    
    
    if(!inpVal.isNull())
    {
      Object val = inpVal.getValue();
      assert val != null;
      if(val instanceof Comparable)
        updateTree((Comparable)val, isInsert);
      else
        throw new UDAException(UDAError.INVALID_OBJECT_PARAM_IN_MEDIAN);
    }
    
    Comparable medianVal = getMedian(inpVal.isNumeric());
    
    if(medianVal == null)
      result.setNull(true);
    else
      result.setValue(medianVal);
  }

      
  private Comparable getMedian(boolean isNumeric)
  {
    long size = tree.getCount();
    Comparable medianVal = null;
    
    if(isNumeric)
    {
      // In case of even number of nodes, 
      // Return the average of middle two elements  
      if(size % 2 == 1)
      {
        medianVal = tree.getPercentageRankElement(tree.getRoot(), 50);  
      }
      else
      {
        // Calculate the average of middle two elements
        Comparable firstMedianVal 
          = tree.getRankElement(tree.getRoot(), size/2);
        Comparable secondMedianVal 
          = tree.getRankElement(tree.getRoot(), size/2 + 1);
        return getAverage(firstMedianVal, secondMedianVal);
      }
    }
    else
    {    
      if(size % 2 == 1)
      {
        medianVal = tree.getPercentageRankElement(tree.getRoot(), 50);     
      }
      else
        medianVal = tree.getRankElement(tree.getRoot(), size/2);
    }
    return medianVal;
  }
  
  /**
   * Helper method to update the tree data structure
   * @param data
   * @param isInsert
   */
  private void updateTree(Comparable data, boolean isInsert)
  {
    if(isInsert)
      tree.insert(data);
    else
      tree.delete(data);
  }
  
  /**
   * Helper method to compute average between two values
   * @param data1 input value 1
   * @param data2 input value 2
   * @param type datatype 
   * @return average of two input values
   */
  private Comparable getAverage(Comparable data1, Comparable data2)
  {    
    if(data1 == null)
      return data2;
    else if(data2 == null)
      return data1;
    
    //Note: Both data1 and data2 are of same type
    
    if(data1 instanceof BigDecimal)
    {
      BigDecimal val1 = (BigDecimal)data1;
      BigDecimal val2 = (BigDecimal)data2;
      int scale1 = val1.scale();
      int scale2 = val2.scale();
      BigDecimal avgVal 
       = val1.divide(new BigDecimal(2.0), scale1, RoundingMode.HALF_DOWN).add
         (val2.divide(new BigDecimal(2.0), scale2, RoundingMode.HALF_DOWN));
      return avgVal;
    }
    else if(data1 instanceof Double)
    {
      double val1 = ((Double)data1);
      double val2 = ((Double)data2);
      double avgVal = val1/2.0d + val2/2.0d;
      return avgVal;
    }
    else if(data1 instanceof Float)
    {
      float val1 = (Float)data1;
      float val2 = (Float)data2;
      float avgVal = val1/2.0f + val2/2.0f;
      return avgVal;
    }
    else if(data1 instanceof Integer)
    {
      int val1 = (Integer)data1;
      int val2 = (Integer)data2;
      float avgFloatVal = val1/2.0f + val2/2.0f;      
      int avgVal = (int) (avgFloatVal);
      return avgVal;
    }
    else if(data1 instanceof Long)
    {
      long val1 = (Long)data1;
      long val2 = (Long)data2;
      double avgDoubleVal = val1/2.0d + val2/2.0d;
      long avgVal = (long) avgDoubleVal;
      return avgVal;
    }
    else
      assert false;
         
    return null;
  }

  public Object clone()
  {
    Median myClone = new Median();
    if(this.tree != null)
      myClone.tree = (RBTree)this.tree.clone();
    else
      myClone.tree = null;
    return myClone;
  }
}
