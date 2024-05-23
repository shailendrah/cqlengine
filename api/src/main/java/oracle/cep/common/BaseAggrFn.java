/* $Header: BaseAggrFn.java 12-jul-2006.04:35:41 anasrini Exp $ */

/* Copyright (c) 2006, Oracle. All rights reserved.  */

/*
   DESCRIPTION
    Base class for all types of aggregations functions

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    anasrini    07/12/06 - Creation
    anasrini    07/12/06 - Creation
    anasrini    07/12/06 - Creation
 */

/**
 *  @version $Header: BaseAggrFn.java 12-jul-2006.04:35:41 anasrini Exp $
 *  @author  anasrini
 *  @since   1.0
 */

package oracle.cep.common;

/**
 * Base class for all types of aggregations functions
 * <p>
 * Currently there are two types of aggregations functions -
 * <ol>
 * <li> Built in aggregation functions which are supported natively </li>
 * <li> User Defined aggregation functions whose definition is external
 *      to the CEP core </li>
 * </ol>
 *
 * @since 1.0
 */

public abstract class BaseAggrFn {

  /**
   * Enumeration for the various types of aggregation functions supported
   */
  public enum FnType { BUILT_IN, USER_DEF; };

  /**
   * Get the type of aggregation function
   * @return the type of aggregation function
   */
  public abstract FnType getFnType();

  /**
   * Get the enum code for the built in aggregation function
   */
  public abstract AggrFunction getFnCode();
  
  /**
   * Get the return type of the aggregation function given its input
   * input attribute datatype. 
   * <p>
   * This assumes a single argument aggregation function.
   * @param attrType the datatype of the input attribute
   * @return the return datatype of the aggregation function
   */
  public abstract Datatype getReturnType(Datatype attrType);

  /**
   * Does this aggregation function support an incremental style of
   * computation
   * @return true iff this aggregation function supports an incremental 
   *         style of computation
   */
  public abstract boolean supportsIncremental();
  
}
