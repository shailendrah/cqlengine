import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;
import oracle.cep.logging.trace.LogArea;
import oracle.cep.logging.trace.LogEvent;
%type <ival> logging_enable
%type  <obj> logging_area
%type <ival> logging_type
%type  <obj> logging_types
%type  <obj> logging_ids
%type  <obj> non_mt_num_list
%type  <obj> mt_logtype_list
%type  <obj> non_mt_logevent_list
%type  <obj> logid
%type  <obj> logtype_list
%type  <obj> logging_events
%type  <obj> logging_levels
%type  <obj> non_mt_name_list

setsystempars
: 
    RW_ALTER URW_SYSTEM logging_enable RW_LOGGING
     {$$ = new CEPLoggingNode(($3), null, null, null, null, null);}

   | RW_ALTER URW_SYSTEM logging_enable RW_LOGGING logging_area logging_levels
     {$$ = new CEPLoggingNode(($3), (LogArea)($5), null, null, null, (List)($6));}

   | RW_ALTER URW_SYSTEM logging_enable RW_LOGGING logging_area logging_events logging_levels
     {$$ = new CEPLoggingNode(($3), (LogArea)($5), null, null, (List)($6), (List)($7));}

   | RW_ALTER URW_SYSTEM logging_enable RW_LOGGING logging_area logging_types logging_levels
     {$$ = new CEPLoggingNode(($3), (LogArea)($5), (List)($6), null, null, (List)($7));}

   | RW_ALTER URW_SYSTEM logging_enable RW_LOGGING logging_area logging_types logging_events logging_levels
     {$$ = new CEPLoggingNode(($3), (LogArea)($5), (List)($6), null, (List)($7), (List)($8));}

   | RW_ALTER URW_SYSTEM logging_enable RW_LOGGING logging_area logging_ids logging_levels
     {$$ = new CEPLoggingNode(($3), (LogArea)($5), null, (List)($6), null, (List)($7));}

   | RW_ALTER URW_SYSTEM logging_enable RW_LOGGING logging_area logging_ids logging_events logging_levels
     {$$ = new CEPLoggingNode(($3), (LogArea)($5), null, (List)($6), (List)($7), (List)($8));}

logging_types
   : RW_TYPE mt_logtype_list
     {$$ = (List)($2);}
   ;

logging_ids
   : RW_IDENTIFIED RW_BY non_mt_name_list 
     {$$ = (List)($3);}
   ;

logging_events
   : RW_EVENT non_mt_logevent_list
     {$$ = (List)($2);}
   ;

logging_levels
   : RW_LEVEL non_mt_num_list
     {$$ = (List)($2);}

logging_enable
   : RW_ENABLE
     {$$ = CEPLoggingNode.ENABLE;}
   
   | RW_DISABLE
     {$$ = CEPLoggingNode.DISABLE;}

   | RW_DUMP
     {$$ = CEPLoggingNode.DUMP;}

   | RW_CLEAR
     {$$ = CEPLoggingNode.CLEAR;}
   ;

non_mt_num_list
   : T_INT ',' non_mt_num_list
     {((LinkedList)($3)).addFirst(new Integer($1)); $$ = $3;}

   | T_INT
     {numList = new LinkedList(); numList.add(new Integer($1)); $$ = numList;}
   ;

non_mt_name_list 
   : logid ',' non_mt_name_list
     {((LinkedList)($3)).addFirst(($1)); $$ = $3;}

   | logid
     {numList = new LinkedList(); numList.add(($1)); $$ = numList;}
   ;

logid
   : identifier
     {$$ = ((CEPStringTokenNode)($1)).getValue();}

   | T_INT
     {$$ = Integer.toString($1);}
   ;

mt_logtype_list
   : logtype_list ',' mt_logtype_list
     {((LinkedList)($3)).addFirst(($1)); $$ = $3;}

   | logtype_list
     {numList = new LinkedList(); numList.add(($1)); $$ = numList;}
   ;

logtype_list
   : logging_type 
     {$$ = new Integer($1);}
   ;

non_mt_logevent_list
   : T_INT ',' non_mt_logevent_list
     {((LinkedList)($3)).addFirst(LogEvent.fromValue($1)); $$ = $3;}

   | T_INT
     {numList = new LinkedList(); numList.add(LogEvent.fromValue($1)); $$ = numList;}
   ;
   

logging_area
   : RW_OPERATOR
     {$$ = LogArea.OPERATOR;}
   
   | RW_QUERY
     {$$ = LogArea.QUERY;}

   | RW_SYSTEMSTATE
     {$$ = LogArea.SYSTEMSTATE;}

   | RW_QUEUE
     {$$ = LogArea.QUEUE;}
     
   | RW_SYNOPSIS
     {$$ = LogArea.SYNOPSIS;}

   | RW_STORE
     {$$ = LogArea.STORE;}
     
   | RW_INDEX
     {$$ = LogArea.INDEX;}

   | RW_METADATA_QUERY
     {$$ = LogArea.METADATA_QUERY;}

   | RW_METADATA_TABLE
     {$$ = LogArea.METADATA_TABLE;}

   | RW_METADATA_WINDOW
     {$$ = LogArea.METADATA_WINDOW;}

   | RW_METADATA_USERFUNC
     {$$ = LogArea.METADATA_USERFUNC;}

   | RW_METADATA_VIEW
     {$$ = LogArea.METADATA_VIEW;}

   | RW_METADATA_SYSTEM
     {$$ = LogArea.METADATA_SYSTEM;}

   | RW_METADATA_SYNONYM
     {$$ = LogArea.METADATA_SYNONYM;}

   | RW_STORAGE
     {$$ = LogArea.STORAGE;}

   | RW_SPILL
     {$$ = LogArea.SPILL;}

   ;

logging_type
   : RW_BINJOIN
     {$$ = PhyOptKind.PO_JOIN.ordinal();}

   | RW_BINSTREAMJOIN
     {$$ = PhyOptKind.PO_STR_JOIN.ordinal();}

   | RW_DISTINCT
     {$$ = PhyOptKind.PO_DISTINCT.ordinal();}

   | RW_DSTREAM
     {$$ = PhyOptKind.PO_DSTREAM.ordinal();}

   | RW_EXCEPT
     {$$ = PhyOptKind.PO_EXCEPT.ordinal();}
     
   | RW_MINUS
     {$$ = PhyOptKind.PO_MINUS.ordinal();}

   | RW_GROUPAGGR
     {$$ = PhyOptKind.PO_GROUP_AGGR.ordinal();}

   | RW_ISTREAM
     {$$ = PhyOptKind.PO_ISTREAM.ordinal();}

   | RW_OUTPUT
     {$$ = PhyOptKind.PO_OUTPUT.ordinal();}

   | RW_PARTITIONWINDOW
     {$$ = PhyOptKind.PO_PARTN_WIN.ordinal();}

   | RW_PATTERNSTRM
     {$$ = PhyOptKind.PO_PATTERN_STRM.ordinal();}

   | RW_PATTERNSTRMB
     {$$ = PhyOptKind.PO_PATTERN_STRM_CLASSB.ordinal();}

   | RW_PROJECT
     {$$ = PhyOptKind.PO_PROJECT.ordinal();}

   | RW_RANGEWINDOW
     {$$ = PhyOptKind.PO_RANGE_WIN.ordinal();}

   | RW_RELSOURCE
     {$$ = PhyOptKind.PO_RELN_SOURCE.ordinal();}

   | RW_ROWWINDOW
     {$$ = PhyOptKind.PO_ROW_WIN.ordinal();}

   | RW_RSTREAM
     {$$ = PhyOptKind.PO_RSTREAM.ordinal();}

   | RW_SELECT
     {$$ = PhyOptKind.PO_SELECT.ordinal();}

   | RW_SINK
     {$$ = PhyOptKind.PO_SINK.ordinal();}

   | RW_STREAMSOURCE
     {$$ = PhyOptKind.PO_STREAM_SOURCE.ordinal();}

   | RW_UNION
     {$$ = PhyOptKind.PO_UNION.ordinal();}

   | RW_VIEWRELNSRC
     {$$ = PhyOptKind.PO_VIEW_RELN_SRC.ordinal();}

   | RW_VIEWSTRMSRC
     {$$ = PhyOptKind.PO_VIEW_STRM_SRC.ordinal();}

   | RW_ORDERBY
     {$$ = PhyOptKind.PO_ORDER_BY.ordinal();}

   | RW_ORDERBYTOP
     {$$ = PhyOptKind.PO_ORDER_BY_TOP.ordinal();}

   | RW_LINEAGE
     {$$ = PhyStoreKind.PHY_LIN_STORE.ordinal();}

   | RW_PARTNWINDOW
     {$$ = PhyStoreKind.PHY_PARTN_WIN_STORE.ordinal();}

   | RW_RELATION
     {$$ = PhyStoreKind.PHY_REL_STORE.ordinal();}

   | RW_WINDOW
     {$$ = PhyStoreKind.PHY_WIN_STORE.ordinal();}

   | RW_BIND
     {$$ = PhyStoreKind.PHY_BIND_STORE.ordinal();}
   
   | RW_EXTERNAL
     {$$ = PhyStoreKind.PHY_EXT_STORE.ordinal();}
     
   ;
