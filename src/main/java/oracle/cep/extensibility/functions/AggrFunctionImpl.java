/* $Header: AggrFunctionImpl.java 24-oct-2007.06:13:59 udeshmuk Exp $ */

/* Copyright (c) 2006, 2007, Oracle. All rights reserved.  */

/*
   DESCRIPTION
    Default implementation of the IAggrFunction interface

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    udeshmuk    10/17/07 - Remove input and return type specific versions of
                           handlePlus and handleMinus and add generic version.
    udeshmuk    10/17/07 - add new handlePlus and handleMinus methods for
                           supporting all data types in UDA.
    sbishnoi    06/12/07 - support for multi-arg UDAs
    hopark      03/07/07 - add Serializable for Tuple spilling
    rkomurav    01/04/07 - null support UDA
    hopark      11/28/06 - add bigint datatype
    anasrini    07/16/06 - Creation
 */

/**
 *  @version $Header: AggrFunctionImpl.java 24-oct-2007.06:13:59 udeshmuk Exp $
 *  @author  anasrini
 *  @since   1.0
 */

package oracle.cep.extensibility.functions;

import java.io.Serializable;

/**
 * This class provides a default implementation of the IAggrFunction interface.
 * <p>
 * User Defined Aggregation functions need to override the implementation of methods
 * from this class to handle arguments of different types appropriately.
 * <p>
 * @since 1.0
 */

public abstract class AggrFunctionImpl implements IAggrFunction, Serializable
{

  public abstract void initialize() throws UDAException;

  public void handlePlus(AggrValue[] args, AggrValue result) throws UDAException {
    assert false;
  }

  public void handleMinus(AggrValue[] args, AggrValue result) throws UDAException {
    assert false;
  }
}
