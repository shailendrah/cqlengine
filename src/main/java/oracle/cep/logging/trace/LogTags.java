/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/logging/trace/LogTags.java /main/3 2010/01/06 20:33:11 parujain Exp $ */

/* Copyright (c) 2007, 2009, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    parujain    12/07/09 - synonym
    skmishra    08/22/08 - moving attrvalue tags to AttributeValue
    hopark      12/26/07 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/logging/trace/LogTags.java /main/3 2010/01/06 20:33:11 parujain Exp $
 *  @author  hopark  
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.logging.trace;

public class LogTags
{
  // Event arguments (Arg0, Arg1, etc)
  public static final String ARG_POS = "Arg";

  // Dump Error
  public static final String DUMP_ERR = "DumpErr";

  // Operator
  public static final String OPERATOR = "Operator";
  
  // Operator attributes
  public static final String OPERATOR_ATTRIBS[] = {"Name", "Id", "Length"};
  
  // Operator related
  public static final String OPERATOR_STATE = "State";
  public static final String OPERATOR_OUTQUE = "OutputQueue";
  
  // Index
  public static final String INDEX = "Index";

  // Index entry attributes
  public static final String INDEX_ENTRY_ATTRIB[] = {"Hash"};
  
  // Index entries
  public static final String INDEX_ENTRY = "IndexEntry";
  
  // Default array attributes
  public static final String ARRAY_ATTRIBS[] = {"Length"};

  // Hash entries
  public static final String HASH_ENTRY = "HashEntry";

  // Hash entry attributes
  public static final String HASH_ENTRY_ATTRIB[] = {"Hash"};
  
  // Value
  public static final String VALUE = "Value";

  // Statistics
  public static final String STAT = "Stat";

  public static final String TAG_QUERIES = "Queries";
  public static final String[] TAG_QUERIES_ATTRIBS = { "Length" };
  
  public static final String TAG_OPERATORS = "Operators";
  public static final String[] TAG_OPERATORS_ATTRIBS = TAG_QUERIES_ATTRIBS;

  public static final String TAG_WINDOWS = "Windows";
  public static final String TAG_VIEWS = "Views";
  public static final String TAG_SYNONYMS = "Synonyms";
  public static final String TAG_TABLES = "Tables";
  public static final String TAG_USERFUNCS = "UserFunctions";
  public static final String TAG_SYSTEMOBJS = "SystemObjects";
  public static final String TAG_SINGLEFUNCS = "SingleFunctions";
  public static final String TAG_SIMPLEFUNCS = "SimpleFunctions";
  public static final String TAG_AGGRFUNCS = "AggrFunctions";
}
