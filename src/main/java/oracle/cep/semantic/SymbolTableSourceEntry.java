/* $Header: pcbpel/cep/server/src/oracle/cep/semantic/SymbolTableSourceEntry.java /main/4 2009/02/03 05:26:37 sborah Exp $ */

/* Copyright (c) 2007, 2009, Oracle and/or its affiliates.
All rights reserved. */

/*
   DESCRIPTION
    This corresponds to an entry for a relation, stream, view, inline view

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    sborah      02/02/09 - fix for bug 7693965
    parujain    10/25/07 - db join
    anasrini    08/23/07 - add if source is a stream/relation
    rkomurav    05/27/07 - 
    anasrini    05/25/07 - add firstAttrVarId, numAttrs
    anasrini    05/18/07 - Creation
    anasrini    05/18/07 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/semantic/SymbolTableSourceEntry.java /main/4 2009/02/03 05:26:37 sborah Exp $
 *  @author  anasrini
 *  @since   1.0
 */

package oracle.cep.semantic;

/**
 * This corresponds to an entry for a relation, stream, view, inline view
 * While relations, streams and views are persistent, inline view is
 * not. Inline view is to represent a subquery like construct
 *
 */

public class SymbolTableSourceEntry extends SymbolTableEntry {

  private int                   tableId;
  private String                tableName;
  private SymbolTableSourceType sourceType;
  private int                   numAttrs;
  private int                   firstAttrVarId;
  private int                   lastAttrVarId;
  private boolean               isStream;
  private boolean               isExternal;

  /**
   * Constructor
   * @param tableId the metadata identifier for the persistent tables/views
   * @param sourceType the type of the source PERSISTENT / INLINE_VIEW
   * @param isStream true iff this source evaluates to a stream
   * @param isExternal whether the source is external or not
   */
  SymbolTableSourceEntry(String varName, String tblName, int tableId,
                         SymbolTableSourceType sourceType,
                         boolean isStream, boolean isExternal) {
    super(varName);
    this.tableId    = tableId;
    this.tableName  = tblName;
    this.sourceType = sourceType;
    this.isStream   = isStream;
    this.isExternal = isExternal;

    //illegal value to start of with
    this.firstAttrVarId = -1; 
    this.lastAttrVarId = -1; 
  }

  // Setters
  
  /**
   * This sets the varId of the first attr of this inline view.
   * Applies only to INLINE_VIEW. Does not apply to PERSISTENT sources.
   * @param firstAttrvarId varId of the first attr of this inline view
   */
  void setFirstAttrVarId(int firstAttrVarId) {
    assert sourceType == SymbolTableSourceType.INLINE_VIEW;
    this.firstAttrVarId = firstAttrVarId;
  }

  /**
   * This sets the varId of the last attr of this inline view.
   * Applies only to INLINE_VIEW. Does not apply to PERSISTENT sources.
   * @param firstAttrvarId varId of the last attr of this inline view
   */
  void setLastAttrVarId(int lastAttrVarId) {
    assert sourceType == SymbolTableSourceType.INLINE_VIEW;
    this.lastAttrVarId = lastAttrVarId;
  }

  /**
   * Set the number of attributes for this source
   */
  void setNumAttrs(int numAttrs) {
    this.numAttrs = numAttrs;
  }

  /**
   * Increment the number of attributes for this source
   */
  void incrNumAttrs() {
    numAttrs++;
  }

  // Getters
  
  /**
   * Get the tableId for this entry
   * @return the tableId for this entry
   */
  int getTableId() {
    assert sourceType == SymbolTableSourceType.PERSISTENT;
    return tableId;
  }
  
  public String getTableName()
  {
    return this.tableName;
  }

  /**
   * Get the source type for this entry
   * @return the source type for this entry
   */
  SymbolTableSourceType getSourceType() {
    return sourceType;
  }

  /**
   * Get the number of attributes for this source
   * @return the number of attributes for this source
   */
  int getNumAttrs() {
    return numAttrs;
  }

  /**
   * Get the varId of the first attribute of an inline view
   * @return the varId of the first attribute of an inline view
   */
  int getFirstAttrVarId() {
    assert sourceType == SymbolTableSourceType.INLINE_VIEW;
    return firstAttrVarId;
  }

  /**
   * Get the varId of the last attribute of an inline view
   * @return the varId of the last attribute of an inline view
   */
  int getLastAttrVarId() {
    assert sourceType == SymbolTableSourceType.INLINE_VIEW;
    return lastAttrVarId;
  }

  /**
   * Is this source entry correspond to a stream source
   * @return true iff this source entry corresponds to a stream source
   */
  boolean isStream() {
    return isStream;
  }

  /**
   * Returns the Symbol Table Entry type for source. 
   * @return the enum value SOURCE corresponding to 
   * the source type.
   */
  SymTableEntryType getEntryType() {
    return SymTableEntryType.SOURCE;
  }

  /**
   * Whether this source correspond to an external data source
   * @return true iff this source entry is an external data source
   */
  public boolean isExternal()
  {
    return isExternal;
  }

}

