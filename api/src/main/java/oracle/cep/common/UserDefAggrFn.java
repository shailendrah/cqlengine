/* $Header: UserDefAggrFn.java 17-jul-2006.22:54:47 anasrini Exp $ */

/* Copyright (c) 2006, Oracle. All rights reserved.  */

/*
   DESCRIPTION
    Class representing a user defined aggregation function

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
 *  @version $Header: UserDefAggrFn.java 17-jul-2006.22:54:47 anasrini Exp $
 *  @author  anasrini
 *  @since   1.0
 */

package oracle.cep.common;

/**
 * Class representing a user defined aggregation function
 *
 * @since 1.0
 */

public class UserDefAggrFn extends BaseAggrFn {

  /** 
   * The metadata layer internal identifier of the user defined
   * aggregation function
   */
  private int fnId;

  /** Return type of the aggregation function */
  private Datatype returnType;

  /** Does this function support an incremental style of computation */
  private boolean supportsIncremental;
  
  private String cartridgeLinkName;
  
  private String functionName;

  /**
   * Constructor
   * @param fnId the metadata layer internal id of the function
   * @param returnType the return type of the function
   * @param supportsIncremental true iff this function supports an
   *                           incremental style of computation
   */
  public UserDefAggrFn(int fnId, Datatype returnType, 
                       boolean supportsIncremental) {

    this.fnId                = fnId;
    this.returnType          = returnType;
    this.supportsIncremental = supportsIncremental;
  }

  public UserDefAggrFn(int fnId, Datatype returnType,
      boolean supportsIncremental, String functionName, String cartridgeLinkName) {

    this.fnId = fnId;
    this.returnType = returnType;
    this.supportsIncremental = supportsIncremental;
    this.functionName = functionName;
    this.cartridgeLinkName = cartridgeLinkName;
  }

  // Getter methods

  
  /**
   * Get the metadata layer internal id of the function
   */
  public int getFnId() {
    return fnId;
  }

  public String getCartridgeLinkName() {
    return cartridgeLinkName;
  }

  public String getFunctionName() {
    return functionName;
  }

  public FnType getFnType() {
    return FnType.USER_DEF;
  }

 public AggrFunction getFnCode() {
    return AggrFunction.USER_DEF;
  }

  public Datatype getReturnType(Datatype attrType) {
    return returnType;
  }

  public boolean supportsIncremental() {
    return supportsIncremental;
  }

  // Override equals method
  public boolean equals(Object otherObject) {
    if (this == otherObject)
      return true;
    
    if (otherObject == null)
      return false;
    
    if (getClass() != otherObject.getClass())
      return false;

    UserDefAggrFn other = (UserDefAggrFn) otherObject;

    return (fnId == other.fnId);
  }
  
}

