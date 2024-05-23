/* $Header: SymbolTableCorrEntry.java 14-jan-2008.05:50:52 rkomurav Exp $ */

/* Copyright (c) 2007, 2008, Oracle. All rights reserved.  */

/*
   DESCRIPTION
    This corresponds to an entry for a correlation name that occurs in
    the MATCH_RECOGNIZE clause

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    rkomurav    01/14/08 - add alphabetIndex field
    rkomurav    05/27/07 - 
    anasrini    05/22/07 - Creation
    anasrini    05/22/07 - Creation
 */

/**
 *  @version $Header: SymbolTableCorrEntry.java 14-jan-2008.05:50:52 rkomurav Exp $
 *  @author  anasrini
 *  @since   1.0
 */

package oracle.cep.semantic;

/**
 * This corresponds to an entry for a correlation name that occurs in
 * the MATCH_RECOGNIZE clause
 */

public class SymbolTableCorrEntry extends SymbolTableEntry {

  private int tableVarId;
  
  private int alphabetIndex;

  /**
   * Constructor
   * @param varName       name of the correlation variable
   * @param tableVarId    the symbol table entry id of the stream on which
   *                      the MATCH_RECOGNIZE clause containing this 
   *                      correlation name occurs
   * @param alphabetIndex index of the alphabet in the DFA
   */
  SymbolTableCorrEntry(String varName, int tableVarId, int alphabetIndex) {
    super(varName);
    this.tableVarId    = tableVarId;
    this.alphabetIndex = alphabetIndex;
  }

  // Getters
  
  /**
   * Get the symbol table entry id of the stream on which the 
   * MATCH_RECOGNIZE clause containing this correlation name occurs
   * @return the symbol table entry id of the stream on which the 
   * MATCH_RECOGNIZE clause containing this correlation name occurs
   */
  public int getTableVarId() {
    return tableVarId;
  }

  public SymTableEntryType getEntryType() {
    return SymTableEntryType.CORR;
  }

  public int getAlphabetIndex() {
    return alphabetIndex;
  }
  
}
