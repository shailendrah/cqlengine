/* $Header: StaticMetadata.java 12-mar-2008.02:57:06 udeshmuk Exp $ */

/* Copyright (c) 2008, Oracle. All rights reserved.  */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    udeshmuk    02/12/08 - Creation
 */

/**
 *  @version $Header: StaticMetadata.java 12-mar-2008.02:57:06 udeshmuk Exp $
 *  @author  udeshmuk
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.metadata;

import oracle.cep.common.Datatype;

/**
 * StaticMetadata object contains information necessary to handle the situation
 * when all arguments to a function are NULL. This information cannot be made 
 * available while creating the function as this would require extending the 
 * language in an undesirable way. So this information is supplied statically.
 * It can be queried later through appropriate interface provided for that 
 * purpose in UserFunctionManager.
 * This is mandatory for overloaded functions and that too overloading 
 * involving the same number of parameters.  
 * For non-overloaded functions the unique function signature can be used.
 * @author udeshmuk
 */

public class StaticMetadata {
   
  /** 
   * The function signature (input data types) to be used when all arguments
   * to a function are null
   */
  private Datatype allNullSignature[];
  
  /**
   * True if function evaluates to null when all arguments to it are null,
   * false otherwise
   */
  private boolean  isResultNull;
  
  /**
   * Constructor for static meta-data object
   * @param dts - input parameter type array to be used when all args are null
   * @param isResultNull - true if function evaluates to null when all args are null
   */
  public StaticMetadata(Datatype[] dts, boolean isResultNull)
  {
    allNullSignature = dts;
    this.isResultNull = isResultNull;
  }  
  
  /**
   * Get the signature to be used when all arguments are null
   * @return datatype array corresponding to input types when all inputs are null
   */
  public Datatype[] getSignature()
  {
    return this.allNullSignature;
  }
  
  /**
   * Get value of boolean member isResultNull
   * @return boolean value which is true if the function evaluates to null when all 
   * arguments to it are null
   */
  public boolean getIsResultNull()
  {
    return this.isResultNull;
  }
}
