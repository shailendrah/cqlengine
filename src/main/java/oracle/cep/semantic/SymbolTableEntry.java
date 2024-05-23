/* $Header: SymbolTableEntry.java 27-may-2007.23:50:25 rkomurav Exp $ */

/* Copyright (c) 2007, Oracle. All rights reserved.  */

/*
   DESCRIPTION
     The base class for a symbol table entry

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    rkomurav    05/27/07 - 
    anasrini    05/18/07 - Creation
    anasrini    05/18/07 - Creation
 */

/**
 *  @version $Header: SymbolTableEntry.java 27-may-2007.23:50:25 rkomurav Exp $
 *  @author  anasrini
 *  @since   1.0
 */

package oracle.cep.semantic;

/**
 * This class is an encapsulation of an entry in the symbol table.
 * Each symbol table entry consists of the following -
 * <ul>
 * <li> The name of the variable </li>
 * <li> The information associated with the variable name </li>
 * </ul>
 * 
 * Note that the format for the information entries may vary. That is there
 * may be many types of symbol table entries.
 *
 * This class is the base class for all these types of entries.
 *
 */
abstract class SymbolTableEntry {

  /** The name for this symbol table entry **/
  protected String varName;
  
  /** The internal id for this entry **/
  protected int    varId;

  /** To ensure that varId can be set only once **/
  private boolean idset;
    
  /**
   * Constructor
   * @param the name for this symbol table entry
   */
  SymbolTableEntry(String varName)
  {
    this.varName = varName;
    this.idset   = false;
  }

  // Setters

  void setVarId(int varId) {
    assert (!idset);
    this.varId = varId;
    idset = true;
  }

  // Getters
  
  /**
   * Get the name associated with the entry
   * @return the name associated with the entry
   */
  public String getVarName() {
    return varName;
  }

  /**
   * Get the internal id associated with the entry
   * @return the internal id associated with the entry
   */
  public int getVarId() {
    assert idset;
    return varId;
  }

  abstract SymTableEntryType getEntryType();
}
