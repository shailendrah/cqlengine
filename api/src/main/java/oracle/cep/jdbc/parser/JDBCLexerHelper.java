/* $Header: JDBCLexerHelper.java 18-apr-2008.04:30:14 rkomurav Exp $ */

/* Copyright (c) 2007, 2008, Oracle. All rights reserved.  */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    rkomurav    04/18/08 - add explain plan
    udeshmuk    01/15/08 - add 'null' as reserved keyword.
    sbishnoi    12/13/07 - 
    mthatte     10/08/07 - Adding select from where
    mthatte     09/04/07 - Adding tokens 'heartbeat' & 'as'
    sbishnoi    06/20/07 - 
    parujain    05/09/07 - 
    najain      04/27/07 - Creation
 */

/**
 *  @version $Header: JDBCLexerHelper.java 18-apr-2008.04:30:14 rkomurav Exp $
 *  @author  najain  
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.jdbc.parser;

class JDBCLexerHelper {

  static int getId(Parser yyparser, String text) {
    int id;

    // Check for reserved words
    id = checkReservedWord(yyparser, text);
    if (id != Parser.T_STRING)
      return id;

    // Pass String value for unreserved keywords and unresolved lexemes
    yyparser.yylval = new ParserVal(text);

    return id;
  }

  static int checkReservedWord(Parser yyparser, String text) {

    String s = text.toLowerCase();
    if(s.equals("insert"))
      return Parser.RW_INSERT;    
    if(s.equals("into"))
      return Parser.RW_INTO;    
    if(s.equals("values"))
      return Parser.RW_VALUES;
    if(s.equalsIgnoreCase("interval"))
      return Parser.RW_INTERVAL;
    if(s.equals("day") || s.equals("days"))
      return Parser.RW_DAY;
    if(s.equalsIgnoreCase("to"))
      return Parser.RW_TO;
    if(s.equalsIgnoreCase("at"))
        return Parser.RW_AT;
    if(s.equalsIgnoreCase("heartbeat"))
        return Parser.RW_HEARTBEAT;
    if(s.equals("second") || s.equals("seconds"))
      return Parser.RW_SECOND;
    if(s.equals("select"))
      return Parser.RW_SELECT;    
    if(s.equals("from"))
      return Parser.RW_FROM;    
    if(s.equals("where"))
      return Parser.RW_WHERE;
    if(s.equals("null"))
      return Parser.RW_NULL;
    if(s.equals("explain"))
      return Parser.RW_EXPLAIN;
    if(s.equals("plan"))
      return Parser.RW_PLAN;
      
    
    /*  unresolved lexemes are strings */
    return Parser.T_STRING;
  }
}
