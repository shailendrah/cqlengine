/* $Header: IAggrFnFactory.java 17-jul-2006.03:56:01 anasrini Exp $ */

/* Copyright (c) 2006, Oracle. All rights reserved.  */

/*
   DESCRIPTION
    Factory Interface for non-native aggregation functions

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    anasrini    07/15/06 - Creation
    anasrini    07/15/06 - Creation
    anasrini    07/15/06 - Creation
 */

/**
 *  @version $Header: IAggrFnFactory.java 17-jul-2006.03:56:01 anasrini Exp $
 *  @author  anasrini
 *  @since   1.0
 */

package oracle.cep.extensibility.functions;

/**
 * This is a factory interface for non-native aggregation functions.
 * <p>
 * The implementation class name that is mentioned in the DDL syntax for
 * creating aggregation functions is expected to implement this
 * factory interface
 *
 * @since 1.0
 */

public interface IAggrFnFactory {

  /**
   * Factory method for creating a stateful handler for the corresponding
   * aggregate function
   * @return a new handler corresponding to the aggregate function
   */
  public IAggrFunction newAggrFunctionHandler() throws UDAException;

  /**
   * Release an already instantiated handler 
   * @param handler the already instantiated aggregate function handler 
   */
  public void freeAggrFunctionHandler(IAggrFunction handler) throws UDAException;

}

