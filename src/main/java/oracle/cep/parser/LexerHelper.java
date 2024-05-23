package oracle.cep.parser;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class LexerHelper {

  static HashMap<String, Short> s_reservedWords;
  static HashMap<String, Short> s_unreservedWords;
  static {
      s_unreservedWords = new HashMap<String, Short>();
      s_unreservedWords.put("name", Parser.URW_NAME); 
      s_unreservedWords.put("supports", Parser.URW_SUPPORTS); 
      s_unreservedWords.put("incremental", Parser.URW_INCREMENTAL); 
      s_unreservedWords.put("computation", Parser.URW_COMPUTATION);
      s_unreservedWords.put("use", Parser.URW_USE);
      s_unreservedWords.put("xmlnamespaces", Parser.URW_XMLNAMESPACES);
      s_unreservedWords.put("unit", Parser.URW_UNIT);
      s_unreservedWords.put("units", Parser.URW_UNIT);
      s_unreservedWords.put("propagate", Parser.URW_PROPAGATE);
      s_unreservedWords.put("archived", Parser.URW_ARCHIVED);
      s_unreservedWords.put("archiver", Parser.URW_ARCHIVER);
      s_unreservedWords.put("entity", Parser.URW_ENTITY);
      s_unreservedWords.put("start_time", Parser.URW_STARTTIME);
      s_unreservedWords.put("identifier", Parser.URW_IDENTIFIER);
      s_unreservedWords.put("column", Parser.URW_COLUMN);
      s_unreservedWords.put("replay", Parser.URW_REPLAY);
      s_unreservedWords.put("worker", Parser.URW_WORKER);
      s_unreservedWords.put("transaction", Parser.URW_TRANSACTION);
      s_unreservedWords.put("dimension", Parser.URW_DIMENSION);
      s_unreservedWords.put("coalesce", Parser.URW_COALESCE);

      /* data cartridge specific keywords*/
      s_unreservedWords.put("table", Parser.URW_TABLE);
      s_unreservedWords.put("system", Parser.URW_SYSTEM);
      s_unreservedWords.put("within", Parser.URW_WITHIN);
      s_unreservedWords.put("inclusive", Parser.URW_INCLUSIVE);
      
      /* Ordering Constraint specific keywords */
      s_unreservedWords.put("ordering", Parser.URW_ORDERING);
      s_unreservedWords.put("total", Parser.URW_TOTAL);
      s_unreservedWords.put("threshold", Parser.URW_THRESHOLD);
      s_unreservedWords.put("degree", Parser.URW_DEGREE);
      s_unreservedWords.put("parallelism", Parser.URW_PARALLELISM);

      /** Special type of value windows */
      s_unreservedWords.put("currenthour", Parser.URW_CURRENTHOUR);
      s_unreservedWords.put("currentperiod", Parser.URW_CURRENTPERIOD);
      
      /** words for timezone based timestamp type */
      s_unreservedWords.put("with", Parser.URW_WITH);
      s_unreservedWords.put("local", Parser.URW_LOCAL);
      s_unreservedWords.put("zone", Parser.URW_ZONE);

      /** words for slide without window feature */
      s_unreservedWords.put("evaluate", Parser.URW_EVALUATE);
      s_unreservedWords.put("every", Parser.URW_EVERY);    

     // General keywords
      s_reservedWords = new HashMap<String, Short>();
      s_reservedWords.put("register", Parser.RW_REGISTER);
      s_reservedWords.put("create", Parser.RW_REGISTER);    
      s_reservedWords.put("stream", Parser.RW_STREAM); 
      s_reservedWords.put("relation", Parser.RW_RELATION); 
      s_reservedWords.put("synonym", Parser.RW_SYNONYM); 
      s_reservedWords.put("external", Parser.RW_EXTERNAL);
      s_reservedWords.put("view", Parser.RW_VIEW); 
      s_reservedWords.put("function", Parser.RW_FUNCTION); 
      s_reservedWords.put("query", Parser.RW_QUERY); 
      s_reservedWords.put("alter", Parser.RW_ALTER);
      s_reservedWords.put("drop", Parser.RW_DROP); 
 
      s_reservedWords.put("integer", Parser.RW_INTEGER);
      s_reservedWords.put("int", Parser.RW_INTEGER);
      s_reservedWords.put("bigint", Parser.RW_BIGINT);
      s_reservedWords.put("float", Parser.RW_FLOAT);
      s_reservedWords.put("real", Parser.RW_FLOAT);
      s_reservedWords.put("double", Parser.RW_DOUBLE);
      s_reservedWords.put("number", Parser.RW_NUMBER);
      s_reservedWords.put("char", Parser.RW_CHAR);
      s_reservedWords.put("timestamp", Parser.RW_TIMESTAMP);
      s_reservedWords.put("interval", Parser.RW_INTERVAL);
      s_reservedWords.put("time", Parser.RW_TIMESTAMP);
      s_reservedWords.put("byte", Parser.RW_BYTE);
      s_reservedWords.put("boolean", Parser.RW_BOOLEAN);
      s_reservedWords.put("true", Parser.RW_TRUE);
      s_reservedWords.put("false", Parser.RW_FALSE);
      s_reservedWords.put("xmltype", Parser.RW_XMLTYPE);
      s_reservedWords.put("object", Parser.RW_OBJECT);
    
    // Xstream(Select from Where clause)
      s_reservedWords.put("istream", Parser.RW_ISTREAM);
      s_reservedWords.put("dstream", Parser.RW_DSTREAM);
      s_reservedWords.put("rstream", Parser.RW_RSTREAM);   
      s_reservedWords.put("select", Parser.RW_SELECT);

      s_reservedWords.put("distinct", Parser.RW_DISTINCT);
      s_reservedWords.put("from", Parser.RW_FROM);
      s_reservedWords.put("for", Parser.RW_FOR);
      s_reservedWords.put("where", Parser.RW_WHERE);   
      s_reservedWords.put("group", Parser.RW_GROUP);
      s_reservedWords.put("by", Parser.RW_BY);   
      s_reservedWords.put("having", Parser.RW_HAVING);
      s_reservedWords.put("and", Parser.RW_AND);
      s_reservedWords.put("xor", Parser.RW_XOR);
      s_reservedWords.put("or", Parser.RW_OR);
      s_reservedWords.put("between", Parser.RW_BETWEEN);
      s_reservedWords.put("not", Parser.RW_NOT);
      s_reservedWords.put("as", Parser.RW_AS);   
      s_reservedWords.put("union", Parser.RW_UNION);
      s_reservedWords.put("all", Parser.RW_ALL);
      s_reservedWords.put("except", Parser.RW_EXCEPT);
      s_reservedWords.put("minus", Parser.RW_MINUS);
      s_reservedWords.put("intersect", Parser.RW_INTERSECT);
      s_reservedWords.put("start", Parser.RW_START);
      s_reservedWords.put("stop", Parser.RW_STOP);
      s_reservedWords.put("add", Parser.RW_ADD);
      s_reservedWords.put("destination", Parser.RW_DEST);
      s_reservedWords.put("source", Parser.RW_SOURCE);
      s_reservedWords.put("push", Parser.RW_PUSH);
      s_reservedWords.put("like", Parser.RW_LIKE);
      s_reservedWords.put("in", Parser.RW_IN);
      s_reservedWords.put("is", Parser.RW_IS);
      s_reservedWords.put("nulls", Parser.RW_NULLS);
      s_reservedWords.put("order", Parser.RW_ORDER);
      s_reservedWords.put("asc", Parser.RW_ASC);
      s_reservedWords.put("desc", Parser.RW_DESC);
      s_reservedWords.put("null", Parser.RW_NULL);
      s_reservedWords.put("set", Parser.RW_SET);
      s_reservedWords.put("timestamped", Parser.RW_TS);
      s_reservedWords.put("application", Parser.RW_APP);
      s_reservedWords.put("silent", Parser.RW_SILENT);
      s_reservedWords.put("heartbeat", Parser.RW_HEARTBEAT);
      s_reservedWords.put("timeout", Parser.RW_TIMEOUT);	
      s_reservedWords.put("remove", Parser.RW_REMOVE);
      s_reservedWords.put("run", Parser.RW_RUN);
      s_reservedWords.put("run_time", Parser.RW_RUNTIME);
      s_reservedWords.put("sched_threaded", Parser.RW_THREADED);
      s_reservedWords.put("sched_name", Parser.RW_SCHEDNAME);
      s_reservedWords.put("time_slice", Parser.RW_TIMESLICE);
      s_reservedWords.put("logging", Parser.RW_LOGGING);
      s_reservedWords.put("dump", Parser.RW_DUMP);
      s_reservedWords.put("type", Parser.RW_TYPE);
      s_reservedWords.put("identified", Parser.RW_IDENTIFIED);
      s_reservedWords.put("event", Parser.RW_EVENT);
      s_reservedWords.put("level", Parser.RW_LEVEL);
      s_reservedWords.put("systemstate", Parser.RW_SYSTEMSTATE);
      s_reservedWords.put("operator", Parser.RW_OPERATOR);
      s_reservedWords.put("queue", Parser.RW_QUEUE);
      s_reservedWords.put("store", Parser.RW_STORE);
      s_reservedWords.put("synopsis", Parser.RW_SYNOPSIS);
      s_reservedWords.put("index", Parser.RW_INDEX);
      s_reservedWords.put("metadata_query", Parser.RW_METADATA_QUERY);
      s_reservedWords.put("metadata_table", Parser.RW_METADATA_TABLE);
      s_reservedWords.put("metadata_window", Parser.RW_METADATA_WINDOW);
      s_reservedWords.put("metadata_userfunc", Parser.RW_METADATA_USERFUNC);
      s_reservedWords.put("metadata_view", Parser.RW_METADATA_VIEW);
      s_reservedWords.put("metadata_system", Parser.RW_METADATA_SYSTEM);
      s_reservedWords.put("metadata_synonym", Parser.RW_METADATA_SYNONYM);
      s_reservedWords.put("storage", Parser.RW_STORAGE);
      s_reservedWords.put("spill", Parser.RW_SPILL);
      s_reservedWords.put("clear", Parser.RW_CLEAR);
      s_reservedWords.put("element_time", Parser.RW_ELEMENT_TIME);
      s_reservedWords.put("ora_query_id", Parser.RW_QUERY_ID);
      s_reservedWords.put("trusted", Parser.RW_TRUSTED);
      s_reservedWords.put("callout", Parser.RW_CALLOUT);
      s_reservedWords.put("derived", Parser.RW_DERIVED);
      s_reservedWords.put("difference", Parser.RW_DIFFERENCE);
      
      s_reservedWords.put("left", Parser.RW_LEFT);
      s_reservedWords.put("right", Parser.RW_RIGHT);
      s_reservedWords.put("full", Parser.RW_FULL);
      s_reservedWords.put("outer", Parser.RW_OUTER);
      s_reservedWords.put("join", Parser.RW_JOIN);

      s_reservedWords.put("lineage", Parser.RW_LINEAGE);
      s_reservedWords.put("partnwin", Parser.RW_PARTNWINDOW);
      s_reservedWords.put("binding", Parser.RW_BIND);

      s_reservedWords.put("duration", Parser.RW_DURATION);
      s_reservedWords.put("enable", Parser.RW_ENABLE);
      s_reservedWords.put("disable", Parser.RW_DISABLE);
      s_reservedWords.put("monitoring", Parser.RW_MONITORING);

    // aggregation functions 
      s_reservedWords.put("avg", Parser.RW_AVG);
      s_reservedWords.put("mean", Parser.RW_AVG);
      s_reservedWords.put("min", Parser.RW_MIN);
      s_reservedWords.put("max", Parser.RW_MAX);
      s_reservedWords.put("count", Parser.RW_COUNT);
      s_reservedWords.put("sum", Parser.RW_SUM);
      s_reservedWords.put("first", Parser.RW_FIRST);
      s_reservedWords.put("last", Parser.RW_LAST);

    // xml support
    /*
      s_reservedWords.put("xmlcdata", Parser.RW_XMLCDATA);
      s_reservedWords.put("xmlcomment", Parser.RW_XMLCOMMENT);
    */
      s_reservedWords.put("xmlparse", Parser.RW_XMLPARSE);
      s_reservedWords.put("xmlconcat", Parser.RW_XMLCONCAT);
      s_reservedWords.put("xmlquery", Parser.RW_XMLQUERY);
      s_reservedWords.put("xmlexists", Parser.RW_XMLEXISTS);
      s_reservedWords.put("xmlelement", Parser.RW_XMLELEMENT);
      s_reservedWords.put("xmlattributes", Parser.RW_XMLATTRIBUTES);
      s_reservedWords.put("xmlforest", Parser.RW_XMLFOREST);
      s_reservedWords.put("passing", Parser.RW_PASSING);
      s_reservedWords.put("value", Parser.RW_VALUE);
      s_reservedWords.put("returning", Parser.RW_RETURNING);
      s_reservedWords.put("content", Parser.RW_CONTENT);
      s_reservedWords.put("xmldata", Parser.RW_XMLDATA);
      s_reservedWords.put("xmltable", Parser.RW_XMLTABLE);
      s_reservedWords.put("default", Parser.RW_DEFAULT);
      s_reservedWords.put("xmlcolattval", Parser.RW_XMLCOLATTVAL);
      s_reservedWords.put("columns", Parser.RW_COLUMNS);
      s_reservedWords.put("path", Parser.RW_PATH);
      s_reservedWords.put("xmlagg", Parser.RW_XMLAGG);
      s_reservedWords.put("wellformed", Parser.RW_WELLFORMED);
      s_reservedWords.put("document", Parser.RW_DOCUMENT);
      s_reservedWords.put("evalname", Parser.RW_EVALNAME);
    
    // window clause
      s_reservedWords.put("rows", Parser.RW_ROWS);   
      s_reservedWords.put("range", Parser.RW_RANGE);      
      s_reservedWords.put("now", Parser.RW_NOW);
      s_reservedWords.put("partition", Parser.RW_PARTITION);
      s_reservedWords.put("unbounded", Parser.RW_UNBOUNDED);
      s_reservedWords.put("slide", Parser.RW_SLIDE);
      s_reservedWords.put("on", Parser.RW_ON);
      s_reservedWords.put("window", Parser.RW_WINDOW);
      s_reservedWords.put("implement", Parser.RW_IMPLEMENT);
    
    // time expressions
      s_reservedWords.put("nanosecond", Parser.RW_NANOSECOND);
      s_reservedWords.put("nanoseconds", Parser.RW_NANOSECOND);
      s_reservedWords.put("microsecond", Parser.RW_MICROSECOND);
      s_reservedWords.put("microseconds", Parser.RW_MICROSECOND);
      s_reservedWords.put("millisecond", Parser.RW_MILLISECOND);
      s_reservedWords.put("milliseconds", Parser.RW_MILLISECOND);
      s_reservedWords.put("second", Parser.RW_SECOND);
      s_reservedWords.put("seconds", Parser.RW_SECOND);
      s_reservedWords.put("minute", Parser.RW_MINUTE);
      s_reservedWords.put("minutes", Parser.RW_MINUTE);
      s_reservedWords.put("hour", Parser.RW_HOUR);
      s_reservedWords.put("hours", Parser.RW_HOUR);
      s_reservedWords.put("day", Parser.RW_DAY);
      s_reservedWords.put("days", Parser.RW_DAY);
      s_reservedWords.put("to", Parser.RW_TO);
      s_reservedWords.put("years", Parser.RW_YEAR);
      s_reservedWords.put("year", Parser.RW_YEAR);
      s_reservedWords.put("months", Parser.RW_MONTH);
      s_reservedWords.put("month", Parser.RW_MONTH);

    // user defined functions
      s_reservedWords.put("language", Parser.RW_LANGUAGE);
      s_reservedWords.put("java", Parser.RW_JAVA);
      s_reservedWords.put("return", Parser.RW_RETURN);
      s_reservedWords.put("aggregate", Parser.RW_AGGREGATE);
      s_reservedWords.put("using", Parser.RW_USING);
      s_unreservedWords.put("instance", Parser.URW_INSTANCE);

    // Pattern Matching
      s_reservedWords.put("match_recognize", Parser.RW_MATCH_RECOGNIZE);
      s_reservedWords.put("pattern", Parser.RW_PATTERN);
      s_reservedWords.put("subset", Parser.RW_SUBSET);
      s_reservedWords.put("measures", Parser.RW_MEASURES);
      s_reservedWords.put("define", Parser.RW_DEFINE);
      s_reservedWords.put("matches", Parser.RW_MATCHES);
      s_reservedWords.put("duration", Parser.RW_DURATION);
      s_reservedWords.put("include", Parser.RW_INCLUDE);
      s_reservedWords.put("timer", Parser.RW_TIMER);
      s_reservedWords.put("events", Parser.RW_EVENTS);
      s_reservedWords.put("multiples", Parser.RW_MULTIPLES);
      s_reservedWords.put("of", Parser.RW_OF);

    // PREV function
      s_reservedWords.put("prev", Parser.RW_PREV);
    
    // Case statement
      s_reservedWords.put("case", Parser.RW_CASE);
      s_reservedWords.put("when", Parser.RW_WHEN);
      s_reservedWords.put("then", Parser.RW_THEN);
      s_reservedWords.put("else", Parser.RW_ELSE);
      s_reservedWords.put("end", Parser.RW_END);
    
    //Decode statement
      s_reservedWords.put("decode", Parser.RW_DECODE);
    
    // Operator types
      s_reservedWords.put("binjoin", Parser.RW_BINJOIN);
      s_reservedWords.put("binstreamjoin", Parser.RW_BINSTREAMJOIN);
      s_reservedWords.put("groupaggr", Parser.RW_GROUPAGGR);
      s_reservedWords.put("output", Parser.RW_OUTPUT);
      s_reservedWords.put("partitionwin", Parser.RW_PARTITIONWINDOW);
      s_reservedWords.put("patternstrm", Parser.RW_PATTERNSTRM);
      s_reservedWords.put("patternstrmb", Parser.RW_PATTERNSTRMB);
      s_reservedWords.put("project", Parser.RW_PROJECT);
      s_reservedWords.put("rangewin", Parser.RW_RANGEWINDOW);
      s_reservedWords.put("relsrc", Parser.RW_RELSOURCE);
      s_reservedWords.put("rowwin", Parser.RW_ROWWINDOW);
      s_reservedWords.put("sink", Parser.RW_SINK);
      s_reservedWords.put("strmsrc", Parser.RW_STREAMSOURCE);
      s_reservedWords.put("viewrelnsrc", Parser.RW_VIEWRELNSRC);
      s_reservedWords.put("viewstrmsrc", Parser.RW_VIEWSTRMSRC);
      s_reservedWords.put("orderbytop", Parser.RW_ORDERBYTOP);

    /* constraint specific keywords*/
      s_reservedWords.put("constraint", Parser.RW_CONSTRAINT);
      s_reservedWords.put("primary", Parser.RW_PRIMARY);
      s_reservedWords.put("key", Parser.RW_KEY);
      s_reservedWords.put("update", Parser.RW_UPDATE);
      s_reservedWords.put("semantics", Parser.RW_SEMANTICS);
      s_reservedWords.put("batch", Parser.RW_BATCH);

  }

  public static String getReservedWord()
  {
    StringBuilder b = new StringBuilder();
    Set<String> reserveds = s_reservedWords.keySet();
    int i = 0;
    for (String r : reserveds) {
      if (i > 0) b.append(",");
      i++;
      b.append(r);
    }
    return b.toString();
  }
 
  static int getId(Parser yyparser, String text) {
    int id;

    // Check for reserved words
    id = checkReservedWord(yyparser, text);
    
    // Pass String value for all ids (reserved, unreserved, unresolved lexemas)
    yyparser.yylval = new ParserVal(text);

    if (id != Parser.T_STRING)
      return id;

    // Check for unreserved keywords
    id = checkUnreservedKeyword(yyparser, text);
    return id;
  }

  static int checkReservedWord(Parser yyparser, String text) {

    String s = text.toLowerCase();
    Short token = s_reservedWords.get(s);
    if (token != null)
      return token.shortValue();

    /*  unresolved lexemes are strings */
    return Parser.T_STRING;
  }

  public static boolean verifyReservedWord(String text) {
    String s = text.toLowerCase();
    Short token = s_reservedWords.get(s);
    if(token != null)
      return true;
    return false;
  }

  static int checkUnreservedKeyword(Parser yyparser, String text) {

    String s = text.toLowerCase();
    Short token = s_unreservedWords.get(s);
    if (token != null)
      return token.shortValue();
      
    /*  unresolved lexemes are strings */
    return Parser.T_STRING;   
  }
  
  public static String getTokenName(int token)
  {
    Set<Map.Entry<String, Short>> reserveds = s_reservedWords.entrySet();
    for (Map.Entry<String,Short> r : reserveds) {
      if (r.getValue() == token) return r.getKey();
    }
    Set<Map.Entry<String, Short>> unreserveds = s_unreservedWords.entrySet();
    for (Map.Entry<String,Short> r : unreserveds) {
      if (r.getValue() == token) return r.getKey();
    }
    if ((token == Parser.T_STRING) || (token == Parser.T_QSTRING)
        || (token == Parser.T_SQSTRING))
      return "STRING";
    return null;
  }
  
}


