/* $Header: BuiltInAggrFn.java 28-may-2007.23:06:35 rkomurav Exp $ */

/* Copyright (c) 2006, 2007, Oracle. All rights reserved.  */

/*
   DESCRIPTION
    Class representing a built in aggregation function

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    rkomurav    05/28/07 - cleanup
    anasrini    07/12/06 - Creation
    anasrini    07/12/06 - Creation
    anasrini    07/12/06 - Creation
 */

/**
 *  @version $Header: BuiltInAggrFn.java 28-may-2007.23:06:35 rkomurav Exp $
 *  @author  anasrini
 *  @since   1.0
 */

package oracle.cep.common;

import java.util.HashMap;

/**
 * Class representing a built in aggregation function
 *
 * @since 1.0
 */

public class BuiltInAggrFn extends BaseAggrFn {

  /** The code for the built in aggregation function */
  private AggrFunction fnCode;

  /** Hash map */
  private static HashMap<AggrFunction, BuiltInAggrFn> map;
  
//  static
//  {
//    AggrFunction[] aggrFns = AggrFunction.values();
//    for(int i = 0; i < aggrFns.length; i++)
//    {
//      map.put(aggrFns[i], new BuiltInAggrFn(aggrFns[i]));
//    }
//  }
//  
//  public static BuiltInAggrFn get(AggrFunction fnCode)
//  {
//    return map.get(fnCode);
//  }
  
  public static BuiltInAggrFn get(AggrFunction fnCode)
  {
    return new BuiltInAggrFn(fnCode);
  }
  
  /**
   * Constructor
   * @param fnCode the function enum code for the built in aggregation function
   */
  private BuiltInAggrFn(AggrFunction fnCode) {
    this.fnCode = fnCode;
  }

  // Getter methods

  public AggrFunction getFnCode() {
    return fnCode;
  }

  public FnType getFnType() {
    return FnType.BUILT_IN;
  }

  public Datatype getReturnType(Datatype attrType) {
    return fnCode.getReturnType(attrType);
  }

  public boolean supportsIncremental() {
    return fnCode.supportsIncremental();
  }

  // Override equals method
  public boolean equals(Object otherObject) {
    if (this == otherObject)
      return true;
    
    if (otherObject == null)
      return false;
    
    if (getClass() != otherObject.getClass())
      return false;

    BuiltInAggrFn other = (BuiltInAggrFn) otherObject;

    return fnCode == other.fnCode;
  }

}

