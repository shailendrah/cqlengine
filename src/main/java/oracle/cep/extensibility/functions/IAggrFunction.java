/* $Header: IAggrFunction.java 24-oct-2007.06:25:28 udeshmuk Exp $ */

/* Copyright (c) 2006, 2007, Oracle. All rights reserved.  */

/*
   DESCRIPTION
    This is the interface that a handler for an aggregate function should
    implement

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    udeshmuk    10/17/07 - Remove input and return type specific versions of
                           handlePlus and handleMinus and add generic version
                           of handlePlusVoid.
    sbishnoi    06/12/07 - support for multi-arg UDAs
    rkomurav    01/03/07 - null support for UDA
    hopark      11/28/06 - add bigint datatype
    anasrini    07/15/06 - Creation
 */

/**
 *  @version $Header: IAggrFunction.java 24-oct-2007.06:25:28 udeshmuk Exp $
 *  @author  anasrini
 *  @since   1.0
 */

package oracle.cep.extensibility.functions;

/**
 * This is the interface that a handler for an aggregate function should
 * implement.
 * <p>
 * The following is the sequence in which these methods will be called
 * <ul>
 * <li> initalize </li>
 * <li> (handlePlus* + hanldeMinus*)+ - called one or more times </li>
 * </ul>
 * <p>
 * Between successive calls to initialize, the handler class implementing
 * this interface can maintain required state.
 *
 * @since 1.0
 */

public interface IAggrFunction extends UserDefinedFunction{

  /**
   * Method to initialize the context for a fresh round of aggregate
   * computation
   */
  public void initialize() throws UDAException;
 
  /**
   * Method to handle the next element of the group. The input type
   * is an array of AggrValue objects and the return type is an AggrValue object 
   * Object can be either of INT, FLOAT, BIGINT, CHAR, BYTE, TIMESTAMP, INTERVAL
   * @param args input value
   * @param result of the aggregation function so far
   */
  
  public void handlePlus(AggrValue[] args, AggrValue result) throws UDAException;
  
  /**
   * Method to handle the removal of an element from the group. The input type
   * is an array of AggrValue objects and the return type is an AggrValue object
   * Object can be either of INT, FLOAT, BIGINT, CHAR, BYTE, TIMESTAMP, INTERVAL
   * @param args input value of element removed
   * @param result of the aggregation function so far
   */
  public void handleMinus(AggrValue[] args, AggrValue result) throws UDAException;
}

