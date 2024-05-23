/* $Header: Hash.java 20-mar-2007.15:44:46 najain Exp $ */

/* Copyright (c) 2006, 2007, Oracle. All rights reserved.  */

/*
 DESCRIPTION
 Declares Hash in package oracle.cep.execution.internals.
 
 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>
 
 NOTES
 <other useful comments, qualifications, etc.>
 
 MODIFIED    (MM/DD/YY)
    najain    03/12/07 - bug fix
    skaluska  03/01/06 - Creation
    skaluska  03/01/06 - Creation
 */

/**
 *  @version $Header: Hash.java 20-mar-2007.15:44:46 najain Exp $
 *  @author  skaluska
 *  @since   release specific (what release of product did this appear in)
 */


package oracle.cep.execution.internals;

/**
 * Hash
 *
 * @author skaluska
 */
public class Hash
{
  /** hash value */
  private int hashValue;

  /**
   * Constructor for Hash
   * @param hashValue
   */
  public Hash(int hashValue)
  {
    this.hashValue = hashValue;
  }

  /**
   * Getter for hashValue in Hash
   * @return Returns the hashValue
   */
  public int getHashValue()
  {
    return hashValue;
  }

  /**
   * Setter for hashValue in Hash
   * @param hashValue The hashValue to set.
   */
  public void setHashValue(int hashValue)
  {
    this.hashValue = hashValue;
  }
  
  /* (non-Javadoc)
   * @see java.lang.Object#hashCode()
   */
  public int hashCode()
  {
    return hashValue;
  }
  
  /**
   * Compare for equality with specified Hash
   * @param o Hash to be compared
   * @return true if equal else false
   */
  public boolean equals(Hash o)
  {
    return (hashValue == o.hashValue);
  }
  
}
