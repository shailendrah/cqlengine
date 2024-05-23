/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/semantic/SymbolTable.java /main/22 2014/11/24 23:35:35 sbishnoi Exp $ */

/* Copyright (c) 2006, 2014, Oracle and/or its affiliates. 
All rights reserved.*/

/*
   DESCRIPTION
    This class encapsulates the symbol table used during semantic analysis

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    sbishnoi    11/21/14 - bug 18547041
    sbishnoi    12/09/13 - support of new keyword for pseudo column QUERYNAME
    pkali       07/28/13 - add pseudo column attributes only if it is present 
                           in the select clause of inline view (bug 13797149, 17154369)
    udeshmuk    01/15/13 - XbranchMerge udeshmuk_bug-15962424_ps6 from
                           st_pcbpel_11.1.1.4.0
    udeshmuk    12/11/12 - add a method to lookup attr only in source entries
    sbishnoi    09/28/09 - support for table function
    sborah      07/15/09 - support for bigdecimal
    hopark      02/04/09 - start varId
    sborah      02/02/09 - fix for bug 7693965
    parujain    11/21/08 - handle constants
    hopark      10/07/08 - use execContext to remove statics
    parujain    09/15/08 - multiple schema support
    parujain    09/04/08 - maintain offsets
    parujain    08/26/08 - semantic exception offset
    rkomurav    03/21/08 - fix for 6899427 - ASSERTION ERROR WHEN USING
                           PATTERNS WITH AGGREGATES
    rkomurav    03/12/08 - change signaure of corrEntry add method
    udeshmuk    02/05/08 - paramterize errors.
    rkomurav    01/14/08 - add numCorrEntries
    parujain    10/26/07 - on demand relation
    parujain    10/25/07 - db join
    anasrini    08/23/07 - support for pseudo columns, ELEMENT_tIMEsupport for
                           pseudo columns, ELEMENT_TIME
    anasrini    05/25/07 - add getAllAttrs, lookupAttr(varId, attrId)
    anasrini    05/18/07 - Support for multiple types of entries
    rkomurav    03/05/07 - add table type
    rkomurav    02/08/07 - add pattern correlations
    anasrini    08/30/06 - add method getVarName(varId)
    najain      05/16/06 - support views 
    najain      04/06/06 - cleanup
    anasrini    02/27/06 - addSymbolTableEntry should return varId 
    anasrini    02/22/06 - add more lookup methods 
    anasrini    02/20/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/semantic/SymbolTable.java /main/22 2014/11/24 23:35:35 sbishnoi Exp $
 *  @author  anasrini
 *  @since   1.0
 */

package oracle.cep.semantic;

import java.util.ArrayList;

import oracle.cep.dataStructures.internal.ExpandableArray;
import oracle.cep.exceptions.CEPException;
import oracle.cep.exceptions.SemanticError;
import oracle.cep.exceptions.MetadataError;
import oracle.cep.service.ExecContext;
import oracle.cep.common.Constants;
import oracle.cep.common.Datatype;
import oracle.cep.common.StreamPseudoColumn;


/**
 * This class encapsulates the symbol table used during semantic analysis.
 * <p>
 * This class is private to the semantic analysis module.
 *
 * @since 1.0
 */

class SymbolTable {
  private static final int            MAX_SYMBOLS 
                                         = Constants.INITIAL_TABLES_CAPACITY;
    
  private ArrayList<SymbolTableEntry> symbolTable;
  private int                         numSymbolTableEntries;
  
  /**
   * Default Constructor
   */
  SymbolTable() {
    symbolTable           = new ExpandableArray<SymbolTableEntry>(MAX_SYMBOLS);
    numSymbolTableEntries = 0;
  }
  
  SymbolTable(int startVarId) {
    symbolTable           = new ExpandableArray<SymbolTableEntry>(MAX_SYMBOLS);
    numSymbolTableEntries = startVarId;
  }
    
  /**
   * This method will reset the symbol table to an initial state.
   * <p>
   * This method should be called before starting a fresh semantic 
   * analysis phase.
   */
  void reset() {
    numSymbolTableEntries = 0;
    symbolTable.clear();
  }

  /// ENTRY METHODS
  
  /**
   * This method is to be used to add an entry into the symbol table.
   * @param entry the symbol table entry to be added
   * @return the internal identifier for the entry
   * @throws CEPException in case of an error
   *
   */
  int addSymbolTableEntry(SymbolTableEntry entry) 
    throws CEPException {

    int varId;

    symbolTable.set(numSymbolTableEntries, entry);
    //symbolTable[numSymbolTableEntries] = entry;
    varId = numSymbolTableEntries;
    numSymbolTableEntries++;

    entry.setVarId(varId);

    return varId;
  }
  
  /**
   * This method is to be used to add a source entry into the symbol table.
   * @param entry the source entry to be added
   * @return the internal identifier of the table
   * @throws CEPException in case of an error
   *
   */
  private int addSourceEntry(SymbolTableSourceEntry entry)
    throws CEPException {

    String varName = entry.getVarName();
    // is varName already in the symbol table ?
    try {
      lookupSource(varName);
      throw new SemanticException(SemanticError.AMBIGUOUS_TABLE_ERROR,
                             new Object[] {varName});
    }
    catch (CEPException e) {
      if (e.getErrorCode() == SemanticError.UNKNOWN_VAR_ERROR) {
        return addSymbolTableEntry(entry);
      }
      else
        throw e;
    }
  }

  /**
   * This method is to be used to add a persistent source entry into the 
   * symbol table.
   * @param tableName name of the relation/stream to be registered
   * @param varName   name of the unique variable(alias) that refers to
   *                  the table. 
   * @return the internal identifier of the table
   * @throws CEPException in case of an error
   *
   */
  int addPersistentSourceEntry(ExecContext ec, String tableName, String varName, String schema)
    throws CEPException {

    int                    objId;
    int                    numAttrs;
    boolean                isStream;
    boolean                isExternal;
    SymbolTableSourceEntry entry;
    
    try {
      objId      = ec.getSourceMgr().getId(tableName, schema);
      numAttrs   = ec.getSourceMgr().getNumAttrs(objId);
      isStream   = ec.getSourceMgr().isStream(objId);
      isExternal = ec.getSourceMgr().isExternal(objId);
      
    }
    catch (CEPException e) {
      throw new SemanticException(SemanticError.UNKNOWN_TABLE_ERROR,
                             new Object[] {tableName});
    }

    entry = new SymbolTableSourceEntry(varName, tableName, objId, 
                                       SymbolTableSourceType.PERSISTENT,
                                       isStream, isExternal);
    entry.setNumAttrs(numAttrs);

    return addSourceEntry(entry);
  }

  /**
   * This method is to be used to add an inline view source entry into the 
   * symbol table.
   * @param tableName name of the relation/stream to be registered
   * @param varName   name of the unique variable(alias) that refers to
   *                  the table. 
   * @param isStream  true iff this inline view evaluates to a stream
   * @return the internal identifier of the table
   * @throws CEPException in case of an error
   *
   */
  int addInlineSourceEntry(String tableName, String varName, boolean isStream) 
    throws CEPException
  {
    return addInlineSourceEntry(tableName, varName, isStream, false);
  }
  
  /**
   * This method is to be used to add an inline view source entry into the 
   * symbol table.
   * @param tableName name of the relation/stream to be registered
   * @param varName   name of the unique variable(alias) that refers to
   *                  the table. 
   * @param isStream  true iff this inline view evaluates to a stream
   * @param isExternal true iff the entry belongs to external relations
   * @return the internal identifier of the table
   * @throws CEPException in case of an error
   *
   */
  int addInlineSourceEntry(String tableName, String varName, boolean isStream,
                           boolean isExternal)
    throws CEPException {

    SymbolTableSourceEntry entry;

    entry = new SymbolTableSourceEntry(varName, tableName, -1, 
                                       SymbolTableSourceType.INLINE_VIEW,
                                       isStream, isExternal);
    entry.setNumAttrs(0);
    return addSourceEntry(entry);
  } 

  /**
   * This method is to be used to add an ATTR entry into the symbol table.
   * @param varName        name of the attribute
   * @param inlineViewName the name of the inline view of which this is an
   *                       attribute
   * @param attrId id of the attribute. It will be a value between 0 .. n-1
   *               where n is the number of attributes of the inline view
   * @param dt the datatype of this attribute
   * @param len the maximum length of this attribute if this is a variable
   *            length attribute
   * @return the internal identifier of the table
   * @throws CEPException in case of an error
   *
   */
  int addAttrEntry(String varName, String inlineViewName, int attrId,
                   Datatype dt, int len)
    throws CEPException {

    SymbolTableAttrEntry   entry;
    SymbolTableAttrEntry   prevAttrEntry;
    SymbolTableSourceEntry sourceEntry;
    int                    inlineViewId;
    int                    attrVarId;
    int                    lastAttrVarId;
    
    sourceEntry  = lookupSource(inlineViewName);
    inlineViewId = sourceEntry.getVarId();

    assert sourceEntry.getSourceType() == SymbolTableSourceType.INLINE_VIEW;
    
    entry = new SymbolTableAttrEntry(varName, inlineViewId, attrId, dt, len);

    // is varName already in the symbol table?
    try {
      lookupAttr(varName, inlineViewId);
      throw new CEPException(MetadataError.ATTRIBUTE_ALREADY_EXISTS,
                             new Object[] {varName});
    }
    catch (CEPException e) {
      if (e.getErrorCode() == SemanticError.UNKNOWN_VAR_ERROR) {
        attrVarId = addSymbolTableEntry(entry);
        sourceEntry.incrNumAttrs();
        if (sourceEntry.getNumAttrs() == 1) {
          sourceEntry.setFirstAttrVarId(attrVarId);
        }
        else {
          // Update the next attr var id for the previous attr
          lastAttrVarId = sourceEntry.getLastAttrVarId();
          assert symbolTable.get(lastAttrVarId) instanceof SymbolTableAttrEntry;
          prevAttrEntry = (SymbolTableAttrEntry)symbolTable.get(lastAttrVarId);
          prevAttrEntry.setNextAttrVarId(attrVarId);
        }

        // update the last attrVarId in the INLINVE_VIEW entry
        sourceEntry.setLastAttrVarId(attrVarId);
        return attrVarId;
      }
      else
        throw e;
    }
  }

  /**
   * This method is to be used to add an ATTR entry into the symbol table.
   * @param varName       name of the attribute
   * @param streamName    name of the source stream
   * @param alphabetIndex index of the input alphabet
   * @return the internal identifier of the table
   * @throws CEPException in case of an error
   */
  int addCorrEntry(String varName, String streamName, int alphabetIndex)
    throws CEPException {

    int                  streamVarId;
    SymbolTableCorrEntry entry;
    SymbolTableSourceEntry sourceEntry = null;
    SymbolTableCorrEntry corrEntry;
    
    sourceEntry = lookupSource(streamName);
    streamVarId = sourceEntry.getVarId();
    // is varName already in the symbol table?
    try {
      corrEntry = lookupCorr(varName);
      assert (corrEntry.getTableVarId() == streamVarId);
      throw new SemanticException(SemanticError.CORR_VAR_ALREADY_EXISTS,
                             new Object[] {varName});
    }
    catch (CEPException e) {
      if (e.getErrorCode() == SemanticError.UNKNOWN_VAR_ERROR) {
        entry = new SymbolTableCorrEntry(varName, streamVarId, alphabetIndex);
        return addSymbolTableEntry(entry);
      }
      else
        throw e;
    }
  }


  // LOOKUP METHODS
  public SymbolTableSourceEntry lookupSource(String varName) 
    throws CEPException {
    
    SymbolTableEntry  entry;
    SymTableEntryType type = SymTableEntryType.SOURCE;

    for (int i=0; i<numSymbolTableEntries; i++)
    {
      entry = symbolTable.get(i);
      if (entry == null) continue;
      // Bug 18547041: In a query, if we specified an alias for a source; then
      // any reference of the source attribute is only using alias.
      // e.g. select A.c1 from Stockdata as A
      // If Query text refers to attribute using source's actual name it throws
      // error.
      // Resolution:
      // We should allow to access attribute using alias as well as source's 
      // actual name.
      if (entry.getEntryType() == type)
      {
        if(entry instanceof SymbolTableSourceEntry)
        {
          SymbolTableSourceEntry sourceEntry = (SymbolTableSourceEntry)entry;
          if(varName.equals(entry.varName) || varName.equals(sourceEntry.getTableName()))
            return sourceEntry; 
        }
      }
    }
    throw new SemanticException(SemanticError.UNKNOWN_VAR_ERROR,
                           new Object[]{varName});
  }

  public SymbolTableSourceEntry lookupSource(int id) {
    SymbolTableEntry entry;
    entry = lookup(id);
    assert entry instanceof SymbolTableSourceEntry;
    return (SymbolTableSourceEntry) entry;
  }

  private int lookupAttr(String attrName, int inlineViewVarId) 
    throws CEPException {

    SymbolTableAttrEntry entry;

    for (int i=0; i<numSymbolTableEntries; i++) {
      if (symbolTable.get(i) == null) continue;
      if (symbolTable.get(i).getEntryType() == SymTableEntryType.ATTR) {
        entry = (SymbolTableAttrEntry) symbolTable.get(i);

        if (entry.getVarName().equals(attrName) &&
            entry.getInlineViewVarId() == inlineViewVarId)
          return i;
      }
    }

    throw new SemanticException(SemanticError.UNKNOWN_VAR_ERROR,
                           new Object[]{attrName});
  }

  /**
   * Lookup the attribute entry in the symbol table.
   * <p>
   * Note that the attrName could be a pseudo column name as well
   * <p>
   * As for tableName, it could be a correlation name as well. We first
   * search among the correlation names before looking at source "table"
   * names.
   *
   * @param attrName the name of the desired attribute. Could be a pseudo 
   *                 column name as well
   * @param tableName the name of the "table" that this attribute is part of.
   *                  This could also be a correlation name
   *                  (as in the MATCH_RECOGNIZE) clause
   * @return the attribute entry in the symbol table corresponding to the
   *         the specified attribute
   * @throws CEPException 
   */
  public SymbolTableAttrEntry lookupAttr(ExecContext ec, String tableName, String attrName)
    throws CEPException {

    SymbolTableCorrEntry   corrEntry = null;
    SymbolTableSourceEntry sourceEntry = null;
    SymbolTableSourceType  sourceType;
    SymbolTableAttrEntry   attrEntry;
    int                    attrVarId;
    int                    varId = -1;

    try {
      corrEntry = lookupCorr(tableName); 
      sourceEntry = (SymbolTableSourceEntry)
      symbolTable.get(corrEntry.getTableVarId());
      varId = corrEntry.getVarId();
    }
    catch (CEPException e) {
      if (e.getErrorCode() != SemanticError.UNKNOWN_VAR_ERROR) 
        throw e;
    }

    if (sourceEntry == null) {
      sourceEntry = lookupSource(tableName);
      varId       = sourceEntry.getVarId();
    }

    sourceType = sourceEntry.getSourceType();  
    
    // Initialize the flag to check if the source is stream or relation
    boolean isStream  = sourceEntry.isStream();
    
    // First, check if it is a pseudo column name 
    if(isStream)
    {
      for (StreamPseudoColumn spc: StreamPseudoColumn.values())
      {
        String   spcName = spc.getColumnName();
        Datatype spcType = spc.getColumnType();
        int      spcLen  = spc.getColumnLen();
        int      attrId  = sourceEntry.getNumAttrs() + spc.getColumnNumber();
  
        // Condition: For Each pseudo column, If the pseudo column is applied 
        // only on stream sources, then check whether the source is a stream or 
        // relation.
        if (attrName.equals(spcName) && 
            ((spc.isStreamSourceRequired() && isStream) || 
             (!spc.isStreamSourceRequired()))
            )
        {
          //inline view does not include the pseudo columns
          //hence add it if it is part of the select clause
          //eg : select ELEMENT_TIME (select ELEMENT_TIME, c1 from S) as S1
          if (sourceType == SymbolTableSourceType.INLINE_VIEW) 
          {
            attrVarId = lookupAttr(attrName, sourceEntry.getVarId()); 
            attrEntry = (SymbolTableAttrEntry) symbolTable.get(attrVarId);
            if(spc.isConstant())
              return new SymbolTableConstPseudoAttrEntry(attrName, varId, 
                attrEntry.getAttrId(), attrEntry.getAttrType(), 
                attrEntry.getAttrLen(), attrEntry.getAttrPrecision(),
                attrEntry.getAttrScale(), spc);
            else
              return new SymbolTablePseudoAttrEntry(attrName, varId, 
                attrEntry.getAttrId(), attrEntry.getAttrType(), 
                attrEntry.getAttrLen(), attrEntry.getAttrPrecision(),
                attrEntry.getAttrScale());
          }
          
          if(spc.isConstant())
            return new SymbolTableConstPseudoAttrEntry(attrName, varId, attrId, 
                spcType, spcLen, spc);
          else 
            return new SymbolTablePseudoAttrEntry(attrName, varId, attrId, spcType, 
                                          spcLen);
        }
      }
    }
   
    // It is not a pseudo column, look up "regular" attributes

    sourceType = sourceEntry.getSourceType();    
    if (sourceType == SymbolTableSourceType.PERSISTENT) {
      int      tableId;
      int      attrId;
      Datatype attrType;
      int      attrLen;
      int      attrPrecision;
      int      attrScale;

      tableId       = sourceEntry.getTableId();
      attrId        = ec.getSourceMgr().getAttrId(tableId, attrName);
      attrType      = ec.getSourceMgr().getAttrType(tableId, attrId);
      attrLen       = ec.getSourceMgr().getAttrLen(tableId, attrId);
      attrPrecision = ec.getSourceMgr().getAttrPrecision(tableId, attrId);
      attrScale     = ec.getSourceMgr().getAttrScale(tableId, attrId);
      
      attrEntry = new SymbolTableAttrEntry(attrName, varId, attrId,
                                           attrType, attrLen, attrPrecision,
                                           attrScale);
    }
    else {
      assert (sourceType == SymbolTableSourceType.INLINE_VIEW);
      // this is an inline view, hence the attributes must be present 
      // in the symbol table too, unlike persistent entities.
      attrVarId = lookupAttr(attrName, sourceEntry.getVarId());      
      assert (symbolTable.get(attrVarId) instanceof SymbolTableAttrEntry);      
      attrEntry = (SymbolTableAttrEntry) symbolTable.get(attrVarId);
      if (corrEntry != null && attrVarId != varId) {
        // We have a got a conflict of attribute name belonging to multiple
        // namespaces?
        // Consider the scenario of a pattern match defined over a subquery 
        // SELECT * FROM (SELECT c1, c2, c3) AS FOO
        // MATCH_RECOGNIZE
        // (
        //   MEASURES A.c3 AS Z
        //   DEFINE A AS A.c3 <op> <expr>
        // )
        // In the symbol table there will two entries for c3, one belonging to
        // FOO and other one as correlation A. We don't need either but need
        // to manufacture a CORR attr with varid of A.c3 and properies from
        // FOO.c3 (type, scale, precision).
        attrEntry = new SymbolTableAttrEntry(attrName, varId, attrEntry.getAttrId(),
            attrEntry.getAttrType(), attrEntry.getAttrLen(), attrEntry.getAttrPrecision(),
            attrEntry.getAttrScale());        
      }      
    }
    return attrEntry;
  }

  /**
   * Lookup the attribute entry in the symbol table but consider only source
   * entries. No corr entries.
   * <p>
   * Note that the attrName could be a pseudo column name as well
   * <p>
   *
   * @param attrName the name of the desired attribute. Could be a pseudo 
   *                 column name as well
   * @param tableName the name of the "table" that this attribute is part of.
   * @return the attribute entry in the symbol table corresponding to the
   *         the specified attribute
   * @throws CEPException 
   */
  public SymbolTableAttrEntry lookupAttrInSource(ExecContext ec,
                                                 String tableName, 
                                                 String attrName)
                                                 throws CEPException 
  {
    SymbolTableSourceEntry sourceEntry = null;
    SymbolTableSourceType  sourceType;
    SymbolTableAttrEntry   attrEntry;
    int                    attrVarId;
    int                    varId = -1;
    boolean                isStream;

    sourceEntry = lookupSource(tableName);
    varId       = sourceEntry.getVarId();

    // First, check if it is a pseudo column name 
    isStream = sourceEntry.isStream();
    if (isStream) 
    {
      for (StreamPseudoColumn spc: StreamPseudoColumn.values())
      {
        String   spcName = spc.getColumnName();
        Datatype spcType = spc.getColumnType();
        int      spcLen  = spc.getColumnLen();
        int      attrId  = sourceEntry.getNumAttrs() + spc.getColumnNumber();

        if (attrName.equals(spcName))
        {
          return new SymbolTableAttrEntry(attrName, varId, attrId, spcType, 
                                          spcLen);
        }
      }
    }
   
    // It is not a pseudo column, look up "regular" attributes

    sourceType = sourceEntry.getSourceType();    
    if (sourceType == SymbolTableSourceType.PERSISTENT) {
      int      tableId;
      int      attrId;
      Datatype attrType;
      int      attrLen;
      int      attrPrecision;
      int      attrScale;

      tableId       = sourceEntry.getTableId();
      attrId        = ec.getSourceMgr().getAttrId(tableId, attrName);
      attrType      = ec.getSourceMgr().getAttrType(tableId, attrId);
      attrLen       = ec.getSourceMgr().getAttrLen(tableId, attrId);
      attrPrecision = ec.getSourceMgr().getAttrPrecision(tableId, attrId);
      attrScale     = ec.getSourceMgr().getAttrScale(tableId, attrId);
      
      attrEntry = new SymbolTableAttrEntry(attrName, varId, attrId,
                                           attrType, attrLen, attrPrecision,
                                           attrScale);
    }
    else {
      assert (sourceType == SymbolTableSourceType.INLINE_VIEW);
      attrVarId = lookupAttr(attrName, sourceEntry.getVarId());
      assert (symbolTable.get(attrVarId) instanceof SymbolTableAttrEntry);
      attrEntry = (SymbolTableAttrEntry) symbolTable.get(attrVarId);
    }
    return attrEntry;
  }

  public SymbolTableAttrEntry lookupAttr(ExecContext ec, String attrName)
    throws CEPException {

    SymbolTableEntry     entry;
    SymbolTableAttrEntry attrEntry = null;
    boolean              bfound = false;

    for (int i=0; i<numSymbolTableEntries; i++) {
      entry = symbolTable.get(i);
      if (entry == null) continue;
      if (entry.getEntryType() == SymTableEntryType.SOURCE ||
          entry.getEntryType() == SymTableEntryType.CORR) {

        try {
          attrEntry = lookupAttr(ec, entry.getVarName(), attrName);

          // Ambiguous attribute
          if(bfound) {
            throw new SemanticException(SemanticError.AMBIGUOUS_ATTR_ERROR,
                                   new Object[] {attrName});
          }
          bfound = true;
        }
        catch (CEPException e) {
          if (e.getErrorCode() != SemanticError.UNKNOWN_VAR_ERROR &&
              e.getErrorCode() != MetadataError.ATTRIBUTE_NOT_FOUND)
            throw e;
        }
      }
    }
    
    //This case is applicable for non-qualified attribute names,
    //like "c1" instead of "R1.c1"
    //For example, when joining two streams S1 and S2, for an attribute in S2,
    //exceptions from S1 are to be eaten - which the above code does.
    //But if the attribute is not found in both S1 and S2, attrEntry is null
    //instead of an assertion error, it should throw an exception.
    if(attrEntry == null)
      throw new CEPException(MetadataError.ATTRIBUTE_NOT_FOUND,
                           new Object[] {attrName});
    return attrEntry;
  }

  /**
   * Lookup the attr only among entries of type SOURCE in symbol table.
   */
  public SymbolTableAttrEntry lookupAttrInSource(ExecContext ec, String attrName)
    throws CEPException {

    SymbolTableEntry     entry;
    SymbolTableAttrEntry attrEntry = null;
    boolean              bfound = false;

    for (int i=0; i<numSymbolTableEntries; i++) {
      entry = symbolTable.get(i);
      if (entry == null) continue;
      if (entry.getEntryType() == SymTableEntryType.SOURCE)
      {
        try {
          attrEntry = lookupAttr(ec, entry.getVarName(), attrName);

          // Ambiguous attribute
          if(bfound) {
            throw new SemanticException(SemanticError.AMBIGUOUS_ATTR_ERROR,
                                   new Object[] {attrName});
          }
          bfound = true;
        }
        catch (CEPException e) {
          if (e.getErrorCode() != SemanticError.UNKNOWN_VAR_ERROR &&
              e.getErrorCode() != MetadataError.ATTRIBUTE_NOT_FOUND)
            throw e;
        }
      }
    }
    
    //This case is applicable for non-qualified attribute names,
    //like "c1" instead of "R1.c1"
    //For example, when joining two streams S1 and S2, for an attribute in S2,
    //exceptions from S1 are to be eaten - which the above code does.
    //But if the attribute is not found in both S1 and S2, attrEntry is null
    //instead of an assertion error, it should throw an exception.
    if(attrEntry == null)
      throw new CEPException(MetadataError.ATTRIBUTE_NOT_FOUND,
                           new Object[] {attrName});
    return attrEntry;
  }

  public SymbolTableAttrEntry lookupAttr(int id) {
    SymbolTableEntry entry;
    entry = lookup(id);
    assert entry instanceof SymbolTableAttrEntry;
    return (SymbolTableAttrEntry) entry;
  }

  public SymbolTableAttrEntry[] getAllAttrs(ExecContext ec, int varId) 
    throws CEPException {

    SymbolTableEntry                entry;
    SymbolTableCorrEntry            corrEntry;
    SymbolTableSourceEntry          sourceEntry = null;
    SymbolTableAttrEntry            attrEntry;
    ArrayList<SymbolTableAttrEntry> attrEntries;
    SymTableEntryType               entryType;
    SymbolTableSourceType           sourceType;

    attrEntries = new ArrayList<SymbolTableAttrEntry>();
    entry     = lookup(varId);
    entryType = entry.getEntryType();

    if (entryType == SymTableEntryType.CORR) {
      assert entry instanceof SymbolTableCorrEntry;
      corrEntry   = (SymbolTableCorrEntry) entry;
      sourceEntry = (SymbolTableSourceEntry) 
        symbolTable.get(corrEntry.getTableVarId());
    }
    else if (entryType == SymTableEntryType.SOURCE) {
      assert entry instanceof SymbolTableSourceEntry;
      sourceEntry = (SymbolTableSourceEntry) entry;
    }
    else {
      // should not come here
      assert false;
    }
    sourceType = sourceEntry.getSourceType();

    // attribute ids are from 0 ... (numAttrs-1)
    int numAttrs = sourceEntry.getNumAttrs();
    if (sourceType == SymbolTableSourceType.PERSISTENT) {
      int      tableId  = sourceEntry.getTableId();
      String   attrName;
      Datatype attrType;
      int      attrLen;
      int      attrPrecision;
      int      attrScale;

      for (int i=0; i<numAttrs; i++) {
        attrName = ec.getSourceMgr().getAttrName(tableId, i);
        attrType = ec.getSourceMgr().getAttrType(tableId, i);
        attrLen  = ec.getSourceMgr().getAttrLen(tableId, i);
        attrPrecision = ec.getSourceMgr().getAttrPrecision(tableId, i);
        attrScale     = ec.getSourceMgr().getAttrScale(tableId, i);
        
        attrEntry = new SymbolTableAttrEntry(attrName, varId, i, attrType,
                                             attrLen, attrPrecision, attrScale);
        attrEntries.add(attrEntry);
      }
    }
    else if (sourceType == SymbolTableSourceType.INLINE_VIEW) {
      int i         = 0;
      int attrVarId = sourceEntry.getFirstAttrVarId();
      while (i < numAttrs) {
        assert symbolTable.get(attrVarId) instanceof SymbolTableAttrEntry;
        attrEntry = (SymbolTableAttrEntry)symbolTable.get(attrVarId);
        attrEntries.add(attrEntry);
        attrVarId = attrEntry.getNextAttrVarId();
        i++;
      }
    }
    else {
      // should not come here
      assert false;
    }

    return attrEntries.toArray(new SymbolTableAttrEntry[0]);
  }

  public SymbolTableCorrEntry lookupCorr(String varName) 
    throws CEPException {

    SymbolTableCorrEntry entry;

    for (int i=0; i<numSymbolTableEntries; i++) {
      if (symbolTable.get(i) == null) continue;
      if (symbolTable.get(i).getEntryType() == SymTableEntryType.CORR) {
        if (symbolTable.get(i).getVarName().equals(varName)) {
          assert (symbolTable.get(i) instanceof SymbolTableCorrEntry);
          entry = (SymbolTableCorrEntry)symbolTable.get(i);
          return (SymbolTableCorrEntry) entry;
        }
      }
    }

    throw new SemanticException(SemanticError.UNKNOWN_VAR_ERROR,
                           new Object[]{varName});
  }

  public SymbolTableCorrEntry lookupCorr(int id) {
    SymbolTableEntry entry;
    entry = lookup(id);
    assert entry instanceof SymbolTableCorrEntry;
    return (SymbolTableCorrEntry) entry;
  }

  public SymbolTableEntry lookup(int varId) {
    SymbolTableEntry entry;

    assert varId < numSymbolTableEntries;
    entry = symbolTable.get(varId);
    
    assert (entry != null);
    assert varId == entry.getVarId();
    
    return entry;
  }
  
  public String getTableName(int varId) {
    SymbolTableEntry entry = lookup(varId);
    if(entry instanceof SymbolTableSourceEntry)
      return ((SymbolTableSourceEntry)entry).getTableName();
    else
      return entry.getVarName();
  }
}
