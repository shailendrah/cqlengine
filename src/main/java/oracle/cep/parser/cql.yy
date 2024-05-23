%{
import java.io.*;
import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;

import oracle.cep.common.Constants;
import oracle.cep.common.Datatype;
import oracle.cep.common.CompOp;
import oracle.cep.common.IntervalFormat;
import oracle.cep.common.LogicalOp;
import oracle.cep.common.ArithOp;
import oracle.cep.common.RelSetOp;
import oracle.cep.common.RelToStrOp;
import oracle.cep.common.TimeUnit;
import oracle.cep.common.AggrFunction;
import oracle.cep.common.UnaryOp;
import oracle.cep.common.OrderingKind;
import oracle.cep.common.OuterJoinType;
import oracle.cep.common.RegexpOp;
import oracle.cep.common.StreamPseudoColumn;
import oracle.cep.common.ValueWindowType;
import oracle.cep.common.TimestampFormat;
import oracle.cep.exceptions.CEPException;
import oracle.cep.exceptions.ParserError;
import oracle.cep.exceptions.SyntaxError;
import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;
import oracle.cep.pattern.PatternSkip;
import oracle.cep.metadata.SynonymType;
import oracle.cep.phyplan.PhyOptKind;
import oracle.cep.phyplan.PhyStoreKind;
import oracle.cep.service.ExecContext;

@SuppressWarnings({"unchecked"})

%}

%token RW_REGISTER      
%token RW_STREAM
%token RW_RELATION
%token RW_SYNONYM
%token RW_EXTERNAL
%token RW_VIEW
%token RW_FUNCTION
%token RW_QUERY
%token RW_ALTER
%token RW_DROP
%token RW_WINDOW

%token RW_ISTREAM
%token RW_DSTREAM
%token RW_RSTREAM

%token RW_SELECT
%token RW_DISTINCT
%token RW_FROM
%token RW_WHERE
%token RW_GROUP
%token RW_BY
%token RW_HAVING
%token RW_AND
%token RW_OR
%token RW_XOR
%token RW_NOT
%token RW_AS
%token RW_UNION
%token RW_ALL
%token RW_EXCEPT
%token RW_MINUS
%token RW_INTERSECT
%token <sval> RW_START
%token RW_STOP
%token RW_ADD
%token RW_DEST
%token RW_SOURCE
%token RW_PUSH
%token RW_LIKE
%token RW_SET
%token RW_SILENT
%token RW_TS
%token RW_APP
%token <sval> URW_SYSTEM
%token RW_HEARTBEAT
%token RW_TIMEOUT
%token RW_REMOVE
%token RW_RUN
%token RW_RUNTIME
%token RW_THREADED
%token RW_SCHEDNAME
%token RW_TIMESLICE
%token RW_BETWEEN
%token RW_NULLS
%token RW_ORDER
%token RW_ASC
%token RW_DESC
%token RW_DERIVED
%token RW_FOR

%token RW_LOGGING
%token RW_DUMP
%token RW_IDENTIFIED
%token RW_LEVEL
%token RW_TYPE
%token <sval> RW_EVENT
%token RW_CLEAR

%token RW_LEFT
%token RW_RIGHT
%token RW_FULL
%token RW_OUTER
%token RW_JOIN

%token RW_SYSTEMSTATE
%token RW_OPERATOR
%token RW_QUEUE
%token RW_STORE
%token RW_SYNOPSIS
%token RW_INDEX
%token RW_METADATA_QUERY
%token RW_METADATA_TABLE
%token RW_METADATA_WINDOW
%token RW_METADATA_USERFUNC
%token RW_METADATA_VIEW
%token RW_METADATA_SYSTEM
%token RW_METADATA_SYNONYM
%token RW_STORAGE
%token RW_SPILL

%token RW_BINJOIN
%token RW_BINSTREAMJOIN
%token RW_GROUPAGGR
%token RW_OUTPUT
%token RW_PARTITIONWINDOW
%token RW_PATTERNSTRM
%token RW_PATTERNSTRMB
%token RW_PROJECT
%token RW_RANGEWINDOW
%token RW_RELSOURCE
%token RW_ROWWINDOW
%token RW_SINK
%token RW_STREAMSOURCE
%token RW_VIEWRELNSRC
%token RW_VIEWSTRMSRC
%token RW_ORDERBY
%token RW_ORDERBYTOP
%token RW_DIFFERENCE

%token RW_LINEAGE
%token RW_PARTNWINDOW
%token RW_REL
%token RW_WIN
%token RW_BIND

%token RW_DURATION
%token RW_ENABLE
%token RW_DISABLE
%token RW_MONITORING
%token RW_IN

%token RW_AVG
%token RW_MIN
%token RW_MAX
%token RW_COUNT
%token RW_SUM
%token RW_FIRST
%token RW_LAST
%token RW_IS
%token RW_NULL

%token RW_ROWS
%token RW_RANGE
%token RW_NOW
%token RW_PARTITION
%token RW_UNBOUNDED
%token RW_SLIDE
%token RW_ON

%token URW_UNIT
%token RW_NANOSECOND
%token RW_MICROSECOND
%token RW_MILLISECOND
%token RW_SECOND
%token RW_MINUTE
%token RW_HOUR
%token RW_DAY
%token RW_YEAR
%token RW_MONTH
%token RW_TO

%token RW_RETURN
%token RW_LANGUAGE
%token <sval> RW_JAVA
%token RW_IMPLEMENT
%token RW_AGGREGATE
%token RW_USING

%token RW_MATCH_RECOGNIZE
%token <sval> RW_PATTERN
%token RW_SUBSET
%token RW_DEFINE
%token RW_MEASURES
%token <sval> RW_MATCHES
%token RW_DURATION
%token URW_WITHIN
%token URW_INCLUSIVE
%token RW_INCLUDE
%token RW_TIMER
%token <sval> RW_EVENTS
%token RW_MULTIPLES
%token RW_OF

%token RW_PREV

%token RW_XMLPARSE
%token RW_XMLCONCAT
%token RW_XMLCOMMENT
%token RW_XMLCDATA
%token RW_XMLQUERY
%token RW_XMLEXISTS
%token RW_XMLTABLE
%token URW_XMLNAMESPACES
%token RW_DEFAULT
%token RW_XMLELEMENT
%token RW_XMLATTRIBUTES
%token RW_XMLFOREST
%token RW_XMLCOLATTVAL
%token RW_PASSING
%token RW_VALUE
%token RW_COLUMNS
%token RW_XMLDATA
%token RW_RETURNING 
%token RW_CONTENT
%token RW_PATH
%token RW_XMLAGG
%token RW_WELLFORMED
%token RW_DOCUMENT
%token RW_EVALNAME

%token <sval> URW_ORDERING
%token <sval> URW_TOTAL
%token <sval> URW_DEGREE
%token <sval> URW_PARALLELISM

%token RW_CASE
%token RW_WHEN
%token RW_THEN
%token RW_ELSE
%token <sval> RW_END

%token RW_DECODE

%token <sval> URW_THRESHOLD

%token T_EQ
%token T_LT
%token T_LE
%token T_GT
%token T_GE
%token T_NE
%token T_JPLUS
%token T_DOTSTAR
%token T_CHARAT

%token <sval> RW_INTEGER
%token RW_BIGINT
%token <sval> RW_FLOAT
%token <sval> RW_DOUBLE
%token <sval> RW_NUMBER
%token RW_CHAR
%token <sval> RW_BYTE
%token <sval> RW_TIMESTAMP
%token RW_INTERVAL
%token <sval> RW_BOOLEAN
%token RW_XMLTYPE
%token <sval> RW_OBJECT

%token RW_ELEMENT_TIME
%token RW_QUERY_ID

%token RW_TRUSTED
%token RW_CALLOUT

%token RW_CONSTRAINT
%token RW_PRIMARY
%token RW_KEY
%token RW_UPDATE
%token RW_SEMANTICS

%token <sval> URW_ARCHIVED
%token <sval> URW_ARCHIVER
%token <sval> URW_ENTITY
%token <sval> URW_STARTTIME
%token <sval> URW_IDENTIFIER
%token <sval> URW_WORKER
%token <sval> URW_TRANSACTION
%token <sval> URW_DIMENSION
%token <sval> URW_COLUMN
%token <sval> URW_REPLAY

%token URW_PROPAGATE

%token RW_TRUE
%token RW_FALSE

%token RW_BATCH

%token <sval> URW_NAME
%token <sval> URW_SUPPORTS
%token <sval> URW_INCREMENTAL
%token <sval> URW_COMPUTATION
%token <sval> URW_USE
%token <sval> URW_INSTANCE
%token URW_TABLE

%token URW_CURRENTHOUR
%token URW_CURRENTPERIOD
%token URW_WITH
%token URW_LOCAL
%token URW_ZONE
%token URW_EVALUATE
%token URW_EVERY
%token URW_COALESCE

%token NOTOKEN

%token <ival> T_INT
%token <obj>  T_BIGINT
%token <dval> T_DOUBLE
%token <dval> T_FLOAT
%token <obj>  T_NUMBER
%token <sval> T_STRING
%token <sval> T_SQSTRING
%token <sval> T_QSTRING
%token <sval> T_UPPER_LETTER

%left               '|'
%right		        '.' '@'
%nonassoc           T_STRING '('
%left               '+' '-'
%left               '*' '/'
%nonassoc           UNARYPREC /* used to supply precedence for unary plus and minus */
%left               RW_OR
%left               RW_XOR
%left               RW_AND
%left               RW_MINUS RW_EXCEPT RW_UNION RW_INTERSECT
%right              RW_NOT

%type  <obj> command
%type  <obj> query
%type  <obj> query_n
%type  <obj> named_query
%type  <obj> query_ref
%type  <obj> registerstream
%type  <obj> registerrelation
%type  <obj> registerview
%type  <obj> registerarchivedview
%type  <obj> view_ordering_constraint
%type  <obj> registerfunction
%type  <obj> registersynonym
%type  <obj> registerwindow
%type  <obj> registeraggrfunction
%type  <obj> registerquery
%type  <obj> startquery
%type  <obj> stopquery
%type  <obj> setquerystarttime
%type  <obj> addquerydest
%type  <obj> querydestproperties
%type  <obj> dropquery
%type  <obj> dropfunction
%type  <obj> dropwindow
%type  <obj> droprelorstream
%type  <obj> dropsynonym
%type  <obj> addtablesource
%type  <obj> setparallelismdegree
%type  <obj> alterhbtimeout
%type  <obj> alter_external_relation
%type  <obj> dropview
%type  <obj> incremental_clause
%type  <obj> multi_paramspec_list
%type  <obj> paramspec
%type  <obj> datatype_list
%type  <obj> multi_datatype_list
%type  <obj> non_mt_attrspec_list
%type  <obj> non_mt_relation_attrspec_list
%type  <obj> attrspec
%type  <obj> non_mt_attrname_list
%type  <obj> idstream_clause
%type  <obj> sfw_block
%type  <obj> sfw_block_n
%type  <obj> select_clause
%type  <obj> from_clause
%type  <obj> opt_where_clause
%type  <obj> opt_group_by_clause
%type  <obj> opt_having_clause
%type  <obj> non_mt_projterm_list
%type  <obj> projterm
%type  <obj> aggr_expr
%type  <obj> aggr_distinct_expr
%type  <obj> attr
%type  <obj> pseudo_column
%type  <obj> non_mt_attr_list
%type  <obj> non_mt_relation_list
%type  <obj> relation_variable
%type  <obj> generic_relation_variable
%type  <obj> outer_join_relation_variable
%type  <obj> outer_relation_list
%type  <obj> outer_join_type
%type  <obj> window_type 
%type  <obj> user_window_type
%type  <obj> time_spec
%type  <obj> time_spec_without_timeunit
%type  <obj> time_spec_with_timeunit
%type  <obj> time_unit
%type  <obj> src_identifier_variable
%type  <obj> non_mt_src_identifier_list
%type  <obj> non_mt_double_src_identifier_list
%type  <obj> pattern_recognition_clause
%type  <obj> pattern_recognition_clause1
%type  <obj> pattern_clause
%type  <obj> duration_clause
%type  <obj> within_clause
%type  <obj> xmltable_clause
%type  <obj> regexp
%type  <obj> correlation_name
%type  <obj> pattern_quantifier
%type  <obj> partition_clause
%type  <obj> opt_partition_clause
%type  <obj> pattern_measures_clause
%type  <obj> non_mt_measure_list
%type  <obj> measure_column
%type  <obj> pattern_skip_match_clause
%type  <obj> pattern_definition_clause
%type  <obj> correlation_name_definition
%type  <obj> non_mt_corrname_definition_list
%type  <obj> non_mt_cond_list
%type  <obj> subset_clause
%type  <obj> non_mt_subset_definition_list
%type  <obj> subset_definition
%type  <obj> subset_name
%type  <obj> non_mt_corr_list
%type  <obj> condition
%type  <obj> arith_expr
%type  <obj> const_arith_expr
%type  <obj> non_const_arith_expr
%type  <obj> boolean_value
%type  <obj> interval_value
%type  <obj> interval_format
%type  <obj> timestamp_format
%type  <obj> const_value
%type  <obj> const_string
%type  <obj> func_expr
%type  <obj> non_mt_arg_list
%type  <obj> non_mt_window_list
%type  <obj> binary
%type  <obj> binary_n
%type  <obj> nary
%type  <obj> nary_n
%type  <obj> datatype
%type  <obj> variable_length_datatype
%type  <obj> fixed_length_datatype
%type  <obj> setsystempars
%type  <obj> case_expr
%type  <obj> searched_case_list
%type  <obj> searched_case
%type  <obj> simple_case
%type  <obj> simple_case_list
%type  <obj> decode
%type  <obj> table_monitoring
%type  <obj> query_monitoring
%type  <obj> query_ordering_constraints
%type  <obj> non_mt_arg_list_set
%type  <obj> between_condition
%type  <obj> opt_order_by_clauses
%type  <obj> order_by_clause
%type  <obj> order_by_top_clause
%type  <obj> order_by_list
%type  <obj> orderterm
%type  <obj> const_int
%type  <obj> const_bigint
%type  <obj> null_spec
%type  <obj> asc_desc
%type  <obj> order_expr
%type  <obj> identifier
%type  <obj> extensible_qualified_identifier
%type  <obj> extensible_qualified_datatype
%type  <obj> extensible_identifier
%type  <obj> extensible_non_datatype_identifier
%type  <obj> compound_extensible_qualified_identifier
%type  <obj> object_expr
%type  <obj> array_expr
%type  <obj> nested_method_field_expr
%type  <obj> method_expr
%type  <obj> extensible_attr
%type <sval> unreserved_keyword
%type <sval> reserved_keyword
%type <sval> non_datatype_reserved_keyword
%type  <obj> builtin_func
%type  <obj> builtin_aggr
%type  <obj> builtin_aggr_incr
%type  <obj> extended_builtin_aggr
%type  <obj> xml_agg_expr
%type  <obj> xqryargs_list
%type  <obj> xtbl_cols_list
%type  <obj> xqryarg
%type  <obj> xtbl_col
%type  <obj> xmlnamespaces_list
%type  <obj> xml_namespace
%type  <obj> xmlnamespace_clause
%type  <obj> xml_parse_expr
%type  <obj> out_of_line_constraint
%type  <obj> inline_constraint
%type  <obj> xmlelement_expr
%type  <obj> xml_attribute_list
%type  <obj> xml_attr_list_aux
%type  <obj> xml_attr_list
%type  <obj> xml_attr
%type  <obj> arith_expr_list
%type  <obj> xmlforest_expr
%type  <obj> xmlcolattval_expr
%type  <obj> view_description
%type  <obj> archived_view_query_description
%type  <obj> archived_view_schema_description
%type  <obj> intToken
%type  <obj> bigIntToken
%type  <obj> numberToken
%type  <obj> stringToken
%type  <obj> qstringToken
%type  <obj> sqstringToken
%type  <obj> using_clause
%type  <obj> usinglist
%type  <obj> usingterm
%type  <obj> usingexpr
%type  <obj> setop_relation_list
%type  <obj> setop_relation_variable
%type  <obj> dimension_clause
%type  <sval> event_identifier_clause
%type  <sval> worker_identifier_clause
%type  <sval> txn_identifier_clause
%type  <obj> replay_spec
%type  <obj> ts_type
%type  <obj> opt_evaluate_clause
%%

start
   : command { parseTree = (CEPParseTreeNode)($1); } 
   ;     

command
   : query       
     { $$ = $1;} 

   | registerstream 
     {$$ = $1;}
    
   | registerrelation 
     {$$ = $1;}

   | registerarchivedview
     {$$ = $1;}

   | registerview
     {$$ = $1;}
     
   | view_ordering_constraint
     {$$ = $1;}

   | registerfunction
     {$$ = $1;}
     
   | registerwindow
     {$$ = $1;}

   | registeraggrfunction
     {$$ = $1;}

   | registerquery
     {$$ = $1;}

   | registersynonym
     {$$ = $1;}

   | startquery
     {$$ = $1;}
     
   | stopquery
     {$$ = $1;}

   | setquerystarttime
     {$$ = $1;}

   | addquerydest
     {$$ = $1;}

   | dropquery
     {$$ = $1;}

   | dropfunction
     {$$ = $1;}

   | dropwindow
     {$$ = $1;}

   | droprelorstream
     {$$ = $1;}

   | dropsynonym
     {$$ = $1;}

   | addtablesource
     {$$ = $1;}
     
   | setparallelismdegree
     {$$ = $1;}
     
   | alterhbtimeout
     {$$ = $1;}
     
   | alter_external_relation
     {$$ = $1;}

   | dropview
     {$$ = $1;}

   | setsystempars
     {$$ = $1;}
     
   | table_monitoring
     {$$ = $1;}
     
   | query_monitoring
     {$$ = $1;}
    
   | query_ordering_constraints
     {$$ = $1;}

   ;

replay_spec
   : RW_LAST intToken time_unit
     {$$ = new CEPReplaySpecNode(new CEPTimeSpecNode((TimeUnit)($3),(CEPIntTokenNode)($2)));}
   | RW_LAST intToken RW_ROWS
     {$$ = new CEPReplaySpecNode((CEPIntTokenNode)($2));}
   ;

ts_type
   : {$$ = new Boolean(false);}
   | RW_IS URW_SYSTEM RW_TS
     {$$ = new Boolean(true);}
   | RW_IS RW_APP RW_TS
     {$$ = new Boolean(false);}
   ;

registerstream
   : RW_REGISTER RW_STREAM identifier '(' non_mt_attrspec_list ')' ts_type
     {rdn = new CEPTableDefnNode((CEPStringTokenNode)($3), (List)$5); rdn.setSystemTimestamped((Boolean)($7)); rdn.setExternal(false); rdn.setPartitioned(false); $$ = rdn;}
   | RW_REGISTER RW_PARTITION RW_STREAM identifier '(' non_mt_attrspec_list ')' ts_type
     {rdn = new CEPTableDefnNode((CEPStringTokenNode)($4), (List)$6); rdn.setSystemTimestamped((Boolean)($8)); rdn.setExternal(false); rdn.setPartitioned(true); $$ = rdn;}
   | RW_REGISTER RW_STREAM identifier '(' non_mt_attrspec_list ')' RW_DERIVED RW_TS arith_expr
     {rdn = new CEPTableDefnNode((CEPStringTokenNode)($3), (List)$5); rdn.setSystemTimestamped(false); rdn.setExternal(false); rdn.setTimestampExpr((CEPExprNode)($9)); rdn.setPartitioned(false); $$ = rdn;}  
   | RW_REGISTER RW_PARTITION RW_STREAM identifier '(' non_mt_attrspec_list ')' RW_DERIVED RW_TS arith_expr
     {rdn = new CEPTableDefnNode((CEPStringTokenNode)($4), (List)$6); rdn.setSystemTimestamped(false); rdn.setExternal(false); rdn.setTimestampExpr((CEPExprNode)($10)); rdn.setPartitioned(true); $$ = rdn;}  
   | RW_REGISTER URW_ARCHIVED RW_STREAM identifier '(' non_mt_attrspec_list ')' URW_ARCHIVER extensible_qualified_identifier URW_ENTITY T_QSTRING RW_TIMESTAMP URW_COLUMN identifier URW_REPLAY replay_spec worker_identifier_clause txn_identifier_clause ts_type
     {rdn = new CEPTableDefnNode((CEPStringTokenNode)($4), (List)$6); rdn.setSystemTimestamped((Boolean)($19)); rdn.setExternal(false); rdn.setIsArchived(true); rdn.setArchiverName((List)($9)); rdn.setEntityName($11); rdn.setTimestampColumn((CEPStringTokenNode)($14)); rdn.setReplayClause((CEPReplaySpecNode)($16)); rdn.setWorkerIdColName((String)$17); rdn.setTxnIdColName((String)$18); $$ = rdn;} 
   ;

registerrelation
   : RW_REGISTER RW_RELATION identifier '(' non_mt_relation_attrspec_list ')' ts_type
     {rdn = new CEPTableDefnNode((CEPStringTokenNode)($3), (CEPRelationAttrSpecsNode)$5); rdn.setIsSilent(false); rdn.setSystemTimestamped((Boolean)($7)); rdn.setExternal(false); $$ = rdn;}
   | RW_REGISTER RW_EXTERNAL RW_RELATION identifier '(' non_mt_relation_attrspec_list ')'
     {rdn = new CEPTableDefnNode((CEPStringTokenNode)($4), (CEPRelationAttrSpecsNode)$6); rdn.setIsSilent(false); rdn.setSystemTimestamped(false); rdn.setExternal(true); $$ = rdn;}
   | RW_REGISTER RW_RELATION identifier '(' non_mt_relation_attrspec_list ')' RW_IS RW_SILENT
     {rdn = new CEPTableDefnNode((CEPStringTokenNode)($3), (CEPRelationAttrSpecsNode)$5); rdn.setIsSilent(true); rdn.setSystemTimestamped(false); rdn.setExternal(false); $$ = rdn;}
   | RW_REGISTER URW_ARCHIVED dimension_clause RW_RELATION identifier '(' non_mt_relation_attrspec_list ')' URW_ARCHIVER extensible_qualified_identifier URW_ENTITY T_QSTRING event_identifier_clause worker_identifier_clause txn_identifier_clause ts_type
     {rdn = new CEPTableDefnNode((CEPStringTokenNode)($5), (CEPRelationAttrSpecsNode)$7); 
      rdn.setIsSilent(false); 
      rdn.setSystemTimestamped((Boolean)($16)); 
      rdn.setExternal(false); 
      rdn.setIsArchived(true);
      rdn.setArchiverName((List)($10));
      rdn.setEntityName($12); 
      rdn.setEventIdColName((String)$13);
      rdn.setWorkerIdColName((String)$14);
      rdn.setTxnIdColName((String)$15);
      rdn.setIsDimension((Boolean)$3);
      $$ = rdn;}   
   ;
   
dimension_clause
   : {/* empty */ $$ = new Boolean(false);}
   | URW_DIMENSION 
     {$$ = new Boolean(true);}
   ;

event_identifier_clause
   : {/*empty */ $$=null;}
   | RW_EVENT URW_IDENTIFIER identifier
     {$$=((CEPStringTokenNode)$3).getValue();}
   ; 

worker_identifier_clause
   : {/*empty */ $$=null;}
   | URW_WORKER URW_IDENTIFIER identifier
     {$$=((CEPStringTokenNode)$3).getValue();}
   ;

txn_identifier_clause
   : {/* empty*/ $$ = null;}
   | URW_TRANSACTION URW_IDENTIFIER identifier
     {$$=((CEPStringTokenNode)$3).getValue();}
   ;

registerview
   : view_description RW_AS {updateViewQryTxt();} query {updateViewQryTxt();}
     {((CEPViewDefnNode)($1)).setQueryTxt(viewQryTxt.toString());
      ((CEPViewDefnNode)($1)).setQueryNode((CEPQueryNode)($4)); 
      ((CEPViewDefnNode)($1)).setEndOffset(endOffset);
      $$ = $1;
      viewQryTxt = null;
     }
     
   | view_description RW_AS query_ref
     {((CEPQueryRefNode)($3)).setKind( CEPQueryRefKind.VIEW); 
      ((CEPViewDefnNode)($1)).setQueryNode((CEPQueryNode)($3));
      $$ = $1;
     }
   ;

view_description
   : RW_REGISTER RW_VIEW identifier '(' non_mt_attrspec_list ')'
     {vdn = new CEPViewDefnNode((CEPStringTokenNode)$3); vdn.setAttrSpecList((List)$5); $$ = vdn;}
   
   | RW_REGISTER RW_VIEW identifier '(' non_mt_attrname_list ')'
     {vdn = new CEPViewDefnNode((CEPStringTokenNode)$3); vdn.setAttrNameList((List)$5); $$ = vdn;}
     
   | RW_REGISTER RW_VIEW identifier
     {$$ = new CEPViewDefnNode((CEPStringTokenNode)$3);}
   
   ;

registerarchivedview 
   : archived_view_query_description event_identifier_clause
     { 
       ((CEPViewDefnNode)($1)).setEventIdColName((String)($2));
       $$ = $1;
     }
   ;

archived_view_query_description
   : archived_view_schema_description RW_AS {updateViewQryTxt();} query {updateViewQryTxt();}
     {
       ((CEPViewDefnNode)($1)).setQueryNode((CEPQueryNode)($4));
       ((CEPViewDefnNode)($1)).setQueryTxt(viewQryTxt.toString());
       String temp = viewQryTxt.toString();
       if(viewQryTxt.toString().endsWith("event "))
       {
         int eventPos = viewQryTxt.toString().lastIndexOf("event ");
	 temp = viewQryTxt.toString().substring(0, eventPos);
       }
       if(temp.startsWith("as "))
       {
         int asPos = temp.indexOf("as ");
         String temp1 = temp.substring(asPos+3);
	 temp = temp1;
       }
       ((CEPViewDefnNode)($1)).setQueryTxt(temp);
       viewQryTxt = null;
       $$ = $1;
     }
   ;

archived_view_schema_description
   : RW_REGISTER URW_ARCHIVED RW_VIEW identifier '(' non_mt_attrspec_list ')'  
     {
       vdn = new CEPViewDefnNode((CEPStringTokenNode)$4);
       vdn.setAttrSpecList((List)$6);
       vdn.setIsArchived(true);
       $$ = vdn;
     }
   ;
   
view_ordering_constraint
   : RW_ALTER RW_VIEW identifier RW_SET URW_ORDERING RW_CONSTRAINT URW_TOTAL RW_ORDER
     {$$ = new CEPViewOrderingConstraintNode((CEPStringTokenNode)($3), OrderingKind.TOTAL_ORDER);}
      
   | RW_ALTER RW_VIEW identifier RW_SET URW_ORDERING RW_CONSTRAINT RW_PARTITION RW_ORDER RW_ON arith_expr
    {$$ = new CEPViewOrderingConstraintNode((CEPStringTokenNode)($3), OrderingKind.PARTITION_ORDERED, (CEPExprNode)($10));}
      
   | RW_ALTER RW_VIEW identifier RW_REMOVE URW_ORDERING RW_CONSTRAINT
      {$$ = new CEPViewOrderingConstraintNode((CEPStringTokenNode)($3), OrderingKind.UNORDERED);}
   ;

registersynonym
   : RW_REGISTER RW_SYNONYM identifier RW_FOR RW_TYPE extensible_qualified_identifier 
     {$$ = new CEPSynonymDefnNode((CEPStringTokenNode)$3, (List)($6), SynonymType.TYPE);}
   ;
   
registerfunction
   : RW_REGISTER RW_FUNCTION builtin_func '(' paramspec ')' RW_RETURN datatype RW_AS RW_LANGUAGE RW_JAVA URW_NAME qstringToken
     {paramSpecList = new LinkedList(); paramSpecList.add($5); $$ = new CEPFunctionDefnNode((String)($3), paramSpecList, (Datatype)($8), (CEPStringTokenNode)($13));}

   | RW_REGISTER RW_FUNCTION identifier RW_RETURN datatype RW_AS RW_LANGUAGE RW_JAVA URW_NAME qstringToken
     {$$ = new CEPFunctionDefnNode((CEPStringTokenNode)($3), (Datatype)($5), (CEPStringTokenNode)($10), CEPFunctionDefnNode.NameType.CLASS_NAME);}
     
   | RW_REGISTER RW_FUNCTION identifier RW_RETURN datatype RW_AS RW_LANGUAGE RW_JAVA URW_INSTANCE qstringToken
     {$$ = new CEPFunctionDefnNode((CEPStringTokenNode)($3), (Datatype)($5), (CEPStringTokenNode)($10), CEPFunctionDefnNode.NameType.INSTANCE_NAME);}     

   | RW_REGISTER RW_FUNCTION identifier '(' paramspec ')' RW_RETURN datatype RW_AS RW_LANGUAGE RW_JAVA URW_NAME qstringToken
     {paramSpecList = new LinkedList(); paramSpecList.add($5); $$ = new CEPFunctionDefnNode((CEPStringTokenNode)($3), paramSpecList, (Datatype)($8), (CEPStringTokenNode)($13), CEPFunctionDefnNode.NameType.CLASS_NAME);}

   | RW_REGISTER RW_FUNCTION identifier '(' paramspec ')' RW_RETURN datatype RW_AS RW_LANGUAGE RW_JAVA URW_INSTANCE qstringToken
     {paramSpecList = new LinkedList(); paramSpecList.add($5); $$ = new CEPFunctionDefnNode((CEPStringTokenNode)($3), paramSpecList, (Datatype)($8), (CEPStringTokenNode)($13), CEPFunctionDefnNode.NameType.INSTANCE_NAME);}

   | RW_REGISTER RW_FUNCTION identifier '(' multi_paramspec_list ')' RW_RETURN datatype RW_AS RW_LANGUAGE RW_JAVA URW_NAME qstringToken
     {$$ = new CEPFunctionDefnNode((CEPStringTokenNode)($3), (List)($5), (Datatype)($8), (CEPStringTokenNode)($13), CEPFunctionDefnNode.NameType.CLASS_NAME);}
     
   | RW_REGISTER RW_FUNCTION identifier '(' multi_paramspec_list ')' RW_RETURN datatype RW_AS RW_LANGUAGE RW_JAVA URW_INSTANCE qstringToken
     {$$ = new CEPFunctionDefnNode((CEPStringTokenNode)($3), (List)($5), (Datatype)($8), (CEPStringTokenNode)($13), CEPFunctionDefnNode.NameType.INSTANCE_NAME);}     

   | RW_REGISTER RW_FUNCTION RW_PREV '(' paramspec ')' RW_RETURN datatype RW_AS RW_LANGUAGE RW_JAVA URW_NAME qstringToken
     {paramSpecList = new LinkedList(); paramSpecList.add($5); $$ = new CEPFunctionDefnNode("prev", paramSpecList, (Datatype)($8), (CEPStringTokenNode)($13));}

   | RW_REGISTER RW_FUNCTION RW_PREV '(' multi_paramspec_list ')' RW_RETURN datatype RW_AS RW_LANGUAGE RW_JAVA URW_NAME qstringToken
     {$$ = new CEPFunctionDefnNode("prev", (List)($5), (Datatype)($8), (CEPStringTokenNode)($13));}
   ;
   
registerwindow
   : RW_REGISTER RW_WINDOW identifier RW_IMPLEMENT RW_USING qstringToken
     {$$ = new CEPWindowDefnNode((CEPStringTokenNode)($3), (CEPStringTokenNode)($6));}
     
   | RW_REGISTER RW_WINDOW identifier '(' paramspec ')' RW_IMPLEMENT RW_USING qstringToken
     {paramSpecList = new LinkedList(); paramSpecList.add($5); $$ = new CEPWindowDefnNode((CEPStringTokenNode)($3), paramSpecList, (CEPStringTokenNode)($9));}
     
   | RW_REGISTER RW_WINDOW identifier '(' multi_paramspec_list ')' RW_IMPLEMENT RW_USING qstringToken
     {$$ = new CEPWindowDefnNode((CEPStringTokenNode)($3), (List)($5), (CEPStringTokenNode)($9));}
   ;
   
registeraggrfunction
   : RW_REGISTER RW_FUNCTION builtin_aggr '(' paramspec ')' RW_RETURN datatype RW_AGGREGATE RW_USING qstringToken
     {$$ = new CEPAggrFnDefnNode((String)$3, (CEPAttrSpecNode)($5), (Datatype)($8), (CEPStringTokenNode)($11), false);}
   
   | RW_REGISTER RW_FUNCTION builtin_aggr_incr '(' paramspec ')' RW_RETURN datatype RW_AGGREGATE RW_USING qstringToken incremental_clause
     {$$ = new CEPAggrFnDefnNode((String)$3, (CEPAttrSpecNode)($5), (Datatype)($8), (CEPStringTokenNode)($11), true);}

   | RW_REGISTER RW_FUNCTION extended_builtin_aggr '(' multi_paramspec_list ')' RW_RETURN datatype RW_AGGREGATE RW_USING qstringToken
     {$$ = new CEPAggrFnDefnNode((String)$3, (List<CEPAttrSpecNode>)($5), (Datatype)($8), (CEPStringTokenNode)($11), false);}
     
   | RW_REGISTER RW_FUNCTION identifier '(' paramspec ')' RW_RETURN datatype RW_AGGREGATE RW_USING qstringToken
     {$$ = new CEPAggrFnDefnNode((CEPStringTokenNode)($3), (CEPAttrSpecNode)($5), (Datatype)($8), (CEPStringTokenNode)($11), false, CEPFunctionDefnNode.NameType.CLASS_NAME);}
     
   | RW_REGISTER RW_FUNCTION identifier '(' paramspec ')' RW_RETURN datatype RW_AGGREGATE RW_USING URW_INSTANCE qstringToken
     {$$ = new CEPAggrFnDefnNode((CEPStringTokenNode)($3), (CEPAttrSpecNode)($5), (Datatype)($8), (CEPStringTokenNode)($12), false, CEPFunctionDefnNode.NameType.INSTANCE_NAME);}     

   | RW_REGISTER RW_FUNCTION identifier '(' paramspec ')' RW_RETURN datatype RW_AGGREGATE RW_USING qstringToken incremental_clause
     {$$ = new CEPAggrFnDefnNode((CEPStringTokenNode)($3), (CEPAttrSpecNode)($5), (Datatype)($8), (CEPStringTokenNode)($11), true, CEPFunctionDefnNode.NameType.CLASS_NAME);}
     
   | RW_REGISTER RW_FUNCTION identifier '(' paramspec ')' RW_RETURN datatype RW_AGGREGATE RW_USING URW_INSTANCE qstringToken incremental_clause
     {$$ = new CEPAggrFnDefnNode((CEPStringTokenNode)($3), (CEPAttrSpecNode)($5), (Datatype)($8), (CEPStringTokenNode)($12), true, CEPFunctionDefnNode.NameType.INSTANCE_NAME);}
     
   | RW_REGISTER RW_FUNCTION identifier '(' multi_paramspec_list ')' RW_RETURN datatype RW_AGGREGATE RW_USING qstringToken
     {$$ = new CEPAggrFnDefnNode((CEPStringTokenNode)($3), (List<CEPAttrSpecNode>)($5), (Datatype)($8), (CEPStringTokenNode)($11), false, CEPFunctionDefnNode.NameType.CLASS_NAME);}
     
   | RW_REGISTER RW_FUNCTION identifier '(' multi_paramspec_list ')' RW_RETURN datatype RW_AGGREGATE RW_USING URW_INSTANCE qstringToken
     {$$ = new CEPAggrFnDefnNode((CEPStringTokenNode)($3), (List<CEPAttrSpecNode>)($5), (Datatype)($8), (CEPStringTokenNode)($12), false, CEPFunctionDefnNode.NameType.INSTANCE_NAME);}     
     
   | RW_REGISTER RW_FUNCTION identifier '(' multi_paramspec_list ')' RW_RETURN datatype RW_AGGREGATE RW_USING qstringToken incremental_clause
     {$$ = new CEPAggrFnDefnNode((CEPStringTokenNode)($3), (List<CEPAttrSpecNode>)($5), (Datatype)($8), (CEPStringTokenNode)($11), true, CEPFunctionDefnNode.NameType.CLASS_NAME);}
     
   | RW_REGISTER RW_FUNCTION identifier '(' multi_paramspec_list ')' RW_RETURN datatype RW_AGGREGATE RW_USING URW_INSTANCE qstringToken incremental_clause
     {$$ = new CEPAggrFnDefnNode((CEPStringTokenNode)($3), (List<CEPAttrSpecNode>)($5), (Datatype)($8), (CEPStringTokenNode)($12), true, CEPFunctionDefnNode.NameType.INSTANCE_NAME);}     
   ;

dropfunction
   : RW_DROP RW_FUNCTION identifier '(' datatype_list ')'
    {dropParamList = new LinkedList(); dropParamList.add($5); $$ = new CEPFunctionRefNode((CEPStringTokenNode)($3), dropParamList);}

   | RW_DROP RW_FUNCTION identifier '(' multi_datatype_list ')'
     {$$ = new CEPFunctionRefNode((CEPStringTokenNode)($3), (List)($5));}
 
   | RW_DROP RW_FUNCTION identifier
     {$$ = new CEPFunctionRefNode((CEPStringTokenNode)($3));}

   ;

dropwindow
   : RW_DROP RW_WINDOW identifier
     {$$ = new CEPWindowRefNode((CEPStringTokenNode)($3));}
   ;

dropsynonym
   : RW_DROP RW_SYNONYM identifier
     {$$ = new CEPSynonymRefNode((CEPStringTokenNode)$3);}
   ;

registerquery
   : RW_REGISTER RW_QUERY identifier RW_AS named_query
     {$$ = new CEPQueryDefnNode((CEPStringTokenNode)($3), (CEPQueryNode)$5);}

   | RW_REGISTER RW_QUERY identifier RW_AS named_query out_of_line_constraint
     {$$ = new CEPQueryDefnNode( (CEPStringTokenNode)($3), (CEPQueryNode)$5, (CEPRelationConstraintNode)$6);}

   ;

opt_evaluate_clause
   : {  /* empty production */$$ = null; }

   | URW_EVALUATE URW_EVERY time_spec_with_timeunit
     {$$ = new CEPSlideExprNode((CEPTimeSpecNode)$3);}

   | URW_EVALUATE URW_EVERY const_bigint
     {$$ = new CEPSlideExprNode(((CEPBigintConstExprNode)$3).getValue().longValue()); }
   ;

named_query
   : query
     { $$ = $1;}
   ;

startquery
   : RW_ALTER query_ref RW_START
     {((CEPQueryRefNode)$2).setKind( CEPQueryRefKind.START); $$ = $2;}
   ;
   
stopquery
   : RW_ALTER query_ref RW_STOP
     {((CEPQueryRefNode)$2).setKind( CEPQueryRefKind.STOP); $$ = $2;}
   ;

/* We assume that the start time provided is in number of milliseconds from Jan 1, 1970 
   Equivalent to system.currentTimeMillis return value for that timestamp. 
   This is used only for archived relation test. */
setquerystarttime
   : RW_ALTER query_ref RW_SET URW_STARTTIME const_bigint
     {((CEPQueryRefNode)$2).setKind(CEPQueryRefKind.SETSTARTTIME);
      ((CEPQueryRefNode)$2).setStartTimeValue(((CEPBigintConstExprNode)$5).getValue().longValue());
      $$=$2;}
   ;

addquerydest
   : RW_ALTER query_ref RW_ADD RW_DEST qstringToken
     {((CEPQueryRefNode)$2).setKind( CEPQueryRefKind.ADDDEST);
      ((CEPQueryRefNode)$2).setValue((CEPStringTokenNode)($5)); $$ = $2;}
      
   | RW_ALTER query_ref RW_ADD RW_DEST qstringToken querydestproperties
     {((CEPQueryRefNode)$2).setKind( CEPQueryRefKind.ADDDEST);
      ((CEPQueryRefNode)$2).setValue((CEPStringTokenNode)($5));
      ((CEPQueryRefNode)$2).setDestProperties((Map)($6)); $$ = $2;}
 /* 
   | RW_ALTER query_ref RW_ADD RW_DEST qstringToken URW_USE RW_UPDATE RW_SEMANTICS
     {((CEPQueryRefNode)$2).setKind( CEPQueryRefKind.ADDDEST);
      ((CEPQueryRefNode)$2).setValue((CEPStringTokenNode)($5));
      ((CEPQueryRefNode)$2).setIsUpdateSemantics(true); $$ = $2;}
  
   | RW_ALTER query_ref RW_ADD RW_DEST qstringToken RW_BATCH RW_OUTPUT
     {((CEPQueryRefNode)$2).setKind( CEPQueryRefKind.ADDDEST);
      ((CEPQueryRefNode)$2).setValue((CEPStringTokenNode)($5));
      ((CEPQueryRefNode)$2).setBatchOutputTuples(true); $$ = $2;}
   
   | RW_ALTER query_ref RW_ADD RW_DEST qstringToken URW_PROPAGATE RW_HEARTBEAT 
     {((CEPQueryRefNode)$2).setKind( CEPQueryRefKind.ADDDEST);
      ((CEPQueryRefNode)$2).setValue((CEPStringTokenNode)($5));
      ((CEPQueryRefNode)$2).setPropagateHeartbeat(true); $$ = $2;}*/
   ;

/*
 * As of now, we don't support primary key along with batching and hb
 * propagation. It might require some changes in the output operator. 
 * So in short we don't support all permutations of the properties. 
 * So just listed out the allowed ones here.
 */
querydestproperties
   : URW_USE RW_UPDATE RW_SEMANTICS
     {Map prop = new HashMap<String, Boolean>();
      prop.put(Constants.USE_UPDATE_SEMANTICS, new Boolean(true));
      $$=prop;}

   | RW_BATCH RW_OUTPUT
     {Map prop = new HashMap<String, Boolean>();
      prop.put(Constants.BATCH_OUTPUT, new Boolean(true));
      $$=prop;}

   | URW_PROPAGATE RW_HEARTBEAT
     {Map prop = new HashMap<String, Boolean>();
      prop.put(Constants.PROPAGATE_HB, new Boolean(true));
      $$=prop;}

   | RW_BATCH RW_OUTPUT ',' URW_PROPAGATE RW_HEARTBEAT
     {Map prop = new HashMap<String, Boolean>();
      prop.put(Constants.BATCH_OUTPUT, new Boolean(true));
      prop.put(Constants.PROPAGATE_HB, new Boolean(true));
      $$=prop;}

   | RW_BATCH RW_OUTPUT ',' URW_USE RW_UPDATE RW_SEMANTICS
     {Map prop = new HashMap<String, Boolean>();
      prop.put(Constants.BATCH_OUTPUT, new Boolean(true));
      prop.put(Constants.USE_UPDATE_SEMANTICS, new Boolean(true));
      $$=prop;}

   ;
   
dropquery
   : RW_DROP query_ref
     {((CEPQueryRefNode)$2).setKind( CEPQueryRefKind.DROP); $$ = $2;}
   ;

query_ref
   : RW_QUERY identifier
     {$$ = new CEPQueryRefNode((CEPStringTokenNode)($2));}
   ;

dropview
   : RW_DROP RW_VIEW identifier
     {$$ = new CEPViewDropNode((CEPStringTokenNode)($3));}
   ;

droprelorstream
   : RW_DROP RW_RELATION identifier
     {$$ = new CEPRelOrStreamRefNode((CEPStringTokenNode)($3), false);}

   | RW_DROP RW_STREAM identifier
     {$$ = new CEPRelOrStreamRefNode((CEPStringTokenNode)($3), true);}
   ;

addtablesource
   : RW_ALTER RW_RELATION identifier RW_ADD RW_SOURCE qstringToken
     {$$ = new CEPAddTableSourceNode( (CEPStringTokenNode)($3), (CEPStringTokenNode)($6), false);}

   | RW_ALTER RW_STREAM identifier RW_ADD RW_SOURCE qstringToken
     {$$ = new CEPAddTableSourceNode((CEPStringTokenNode)($3), (CEPStringTokenNode)($6), true);}

   | RW_ALTER RW_RELATION identifier RW_ADD RW_SOURCE RW_PUSH
     {$$ = new CEPAddTableSourceNode((CEPStringTokenNode)($3), new CEPStringTokenNode(null), false);}

   | RW_ALTER RW_STREAM identifier RW_ADD RW_SOURCE RW_PUSH
     {$$ = new CEPAddTableSourceNode((CEPStringTokenNode)($3), new CEPStringTokenNode(null), true);}

   | RW_ALTER RW_RELATION identifier RW_ADD RW_PUSH RW_SOURCE qstringToken
     {$$ = new CEPAddPushSourceNode((CEPStringTokenNode)($3), (CEPStringTokenNode)($7), false, true);}

   | RW_ALTER RW_STREAM identifier RW_ADD RW_PUSH RW_SOURCE qstringToken
     {$$ = new CEPAddPushSourceNode((CEPStringTokenNode)($3), (CEPStringTokenNode)($7), true, true);}
   
   ;
   
setparallelismdegree
   : RW_ALTER RW_STREAM identifier RW_SET URW_DEGREE RW_OF URW_PARALLELISM T_EQ T_INT
     {$$ = new CEPSetParallelismDegreeNode((CEPStringTokenNode)($3), new Integer($9), true);}
     
   | RW_ALTER RW_RELATION identifier RW_SET URW_DEGREE RW_OF URW_PARALLELISM T_EQ T_INT
     {$$ = new CEPSetParallelismDegreeNode((CEPStringTokenNode)($3), new Integer($9), false);}
   ;
   
alterhbtimeout
   : RW_ALTER RW_STREAM identifier RW_SET RW_HEARTBEAT RW_TIMEOUT time_spec
     {$$ = new CEPSetHeartbeatTimeoutNode((CEPStringTokenNode)($3), (CEPTimeSpecNode)($7), true);}
    
   | RW_ALTER RW_RELATION identifier RW_SET RW_HEARTBEAT RW_TIMEOUT time_spec
     {$$ = new CEPSetHeartbeatTimeoutNode((CEPStringTokenNode)($3), (CEPTimeSpecNode)($7), false);}
     
   | RW_ALTER RW_STREAM identifier RW_REMOVE RW_HEARTBEAT RW_TIMEOUT
     {$$ = new CEPRemoveHeartbeatTimeoutNode((CEPStringTokenNode)($3), true);}
     
   | RW_ALTER RW_RELATION identifier RW_REMOVE RW_HEARTBEAT RW_TIMEOUT
     {$$ = new CEPRemoveHeartbeatTimeoutNode((CEPStringTokenNode)($3), false);}   
   ;  
   
alter_external_relation
   : RW_ALTER RW_EXTERNAL RW_RELATION identifier RW_SET RW_EXTERNAL RW_ROWS URW_THRESHOLD bigIntToken
     {$$ = new CEPExternalRelationNode((CEPStringTokenNode)($4), 
                                       (CEPBigIntTokenNode)($9));}
   
query_monitoring
   : RW_ALTER query_ref RW_ENABLE RW_MONITORING
     {((CEPQueryRefNode)$2).setKind( CEPQueryRefKind.ENABLE_MONITOR); 
     ((CEPQueryRefNode)$2).setBaseTimelineMillisecond(false);$$ = $2;}
         
   | RW_ALTER query_ref RW_ENABLE RW_MONITORING URW_USE RW_JAVA RW_MILLISECOND
     {((CEPQueryRefNode)$2).setKind( CEPQueryRefKind.ENABLE_MONITOR);
     ((CEPQueryRefNode)$2).setBaseTimelineMillisecond(true);$$ = $2;}
     
   | RW_ALTER query_ref RW_ENABLE RW_MONITORING URW_USE RW_JAVA RW_NANOSECOND
       {((CEPQueryRefNode)$2).setKind( CEPQueryRefKind.ENABLE_MONITOR);
      ((CEPQueryRefNode)$2).setBaseTimelineMillisecond(false);$$ = $2;}
   
   | RW_ALTER query_ref RW_DISABLE RW_MONITORING
     {((CEPQueryRefNode)$2).setKind( CEPQueryRefKind.DISABLE_MONITOR); $$ = $2;}
   ;
   
query_ordering_constraints
   : RW_ALTER query_ref RW_SET URW_ORDERING RW_CONSTRAINT URW_TOTAL RW_ORDER 
     {((CEPQueryRefNode)$2).setKind(CEPQueryRefKind.ORDERING_CONSTRAINT);
      ((CEPQueryRefNode)$2).setOrderingConstraint(OrderingKind.TOTAL_ORDER);
      $$ = $2;}
      
   | RW_ALTER query_ref RW_SET URW_ORDERING RW_CONSTRAINT RW_PARTITION RW_ORDER RW_ON arith_expr
    {((CEPQueryRefNode)$2).setKind(CEPQueryRefKind.ORDERING_CONSTRAINT);
     ((CEPQueryRefNode)$2).setOrderingConstraint(OrderingKind.PARTITION_ORDERED);
     ((CEPQueryRefNode)$2).setParallelPartitioningExpr((CEPExprNode)($9));
     $$ = $2;}
      
   | RW_ALTER query_ref RW_REMOVE URW_ORDERING RW_CONSTRAINT
     {((CEPQueryRefNode)$2).setKind(CEPQueryRefKind.ORDERING_CONSTRAINT);
      ((CEPQueryRefNode)$2).setOrderingConstraint(OrderingKind.UNORDERED);
      $$ = $2;}
   ;
   
table_monitoring
   : RW_ALTER RW_STREAM identifier RW_ENABLE RW_MONITORING
     {$$ = new CEPTableMonitorNode((CEPStringTokenNode)($3), true,false);}
    
   | RW_ALTER RW_STREAM identifier RW_ENABLE RW_MONITORING URW_USE RW_JAVA RW_MILLISECOND
     {$$ = new CEPTableMonitorNode((CEPStringTokenNode)($3), true,true);}
     
   | RW_ALTER RW_STREAM identifier RW_ENABLE RW_MONITORING URW_USE RW_JAVA RW_NANOSECOND
     {$$ = new CEPTableMonitorNode((CEPStringTokenNode)($3), true,false);}
      
   | RW_ALTER RW_STREAM identifier RW_DISABLE RW_MONITORING
     {$$ = new CEPTableMonitorNode((CEPStringTokenNode)($3), false);}    
   ;
   
 
setsystempars
   : RW_ALTER URW_SYSTEM RW_RUNTIME T_EQ const_int
     {$$ = new CEPSystemNode(CEPSystemKind.RUNTIME, (CEPConstExprNode)($5));}
     
   | RW_ALTER URW_SYSTEM RW_THREADED T_EQ const_int
     {$$ = new CEPSystemNode(CEPSystemKind.THREADED, (CEPConstExprNode)($5));}
     
   | RW_ALTER URW_SYSTEM RW_SCHEDNAME T_EQ const_string
     {$$ = new CEPSystemNode(CEPSystemKind.SCHEDNAME, (CEPConstExprNode)($5));}

   | RW_ALTER URW_SYSTEM RW_TIMESLICE T_EQ const_int
     {$$ = new CEPSystemNode(CEPSystemKind.TIMESLICE, (CEPConstExprNode)($5));}
     
   | RW_ALTER URW_SYSTEM RW_RUN
     {$$ = new CEPSystemRunNode();}

   | RW_ALTER URW_SYSTEM RW_RUN RW_DURATION T_EQ const_int
     {$$ = new CEPSystemRunNode((CEPConstExprNode)($6));}
     
   | RW_ALTER URW_SYSTEM RW_START RW_TRUSTED RW_CALLOUT const_string
     {$$ = new CEPSystemNode(CEPSystemKind.START_CALLOUT, (CEPConstExprNode)($6));}
   ;

incremental_clause
   : URW_SUPPORTS URW_INCREMENTAL URW_COMPUTATION
     {$$ = new Boolean(true);}
   ;

multi_paramspec_list
   : paramspec ',' multi_paramspec_list
     {((LinkedList)($3)).addFirst($1); $$ = $3;}

   | paramspec ',' paramspec
     {paramSpecList = new LinkedList(); paramSpecList.add($1); paramSpecList.add($3); $$ = paramSpecList;}
   ;

paramspec
   : identifier datatype
     {$$ = new CEPAttrSpecNode((CEPStringTokenNode)($1), (Datatype)($2),0);}

   ;

multi_datatype_list
   : datatype_list ',' multi_datatype_list
     {((LinkedList)($3)).addFirst($1); $$ = $3;}

   | datatype_list ',' datatype_list
     {dropParamList = new LinkedList(); dropParamList.add($1); dropParamList.add($3); $$ = dropParamList;}
   ;

datatype_list
   : datatype 
     {$$ = new CEPAttrSpecNode(null, (Datatype)($1),0); ((CEPAttrSpecNode)($$)).setStartOffset(startOffset); ((CEPAttrSpecNode)($$)).setEndOffset(endOffset);}
   ;

non_mt_attrspec_list
   : attrspec ',' non_mt_attrspec_list
     {((LinkedList)($3)).addFirst($1); $$ = $3;}

   | attrspec
     {attrSpecList = new LinkedList(); attrSpecList.add($1); $$ = attrSpecList;}
   ;

non_mt_relation_attrspec_list
   : non_mt_attrspec_list attrspec ',' out_of_line_constraint
     {((LinkedList)($1)).add($2); 
      $$ = new CEPRelationAttrSpecsNode((List)$1, (CEPRelationConstraintNode)$4);}

   | out_of_line_constraint ',' non_mt_attrspec_list
     {$$ = new CEPRelationAttrSpecsNode((CEPRelationConstraintNode)$1, (List)$3);}

   | non_mt_attrspec_list attrspec ',' out_of_line_constraint ',' non_mt_attrspec_list
     {((LinkedList)($1)).add($2); ((LinkedList)($1)).addAll((LinkedList)$6); 
      $$ = new CEPRelationAttrSpecsNode((List)$1, (CEPRelationConstraintNode)$4);}

   | attrspec inline_constraint ',' non_mt_attrspec_list
     {((LinkedList)($4)).addFirst($1); ((CEPRelationConstraintNode)($2)).addColumn((CEPAttrSpecNode)($1)); 
      $$ = new CEPRelationAttrSpecsNode((CEPRelationConstraintNode)$2, (List)$4);}

   | non_mt_attrspec_list attrspec ',' attrspec inline_constraint
     {((LinkedList)($1)).add($2); ((CEPRelationConstraintNode)($5)).addColumn((CEPAttrSpecNode)($4)); 
      ((LinkedList)($1)).add($4); 
      $$ = new CEPRelationAttrSpecsNode((List)$1, (CEPRelationConstraintNode)$5);}
   
   | non_mt_attrspec_list attrspec ',' attrspec inline_constraint ',' non_mt_attrspec_list
     {((LinkedList)($1)).add($2); ((CEPRelationConstraintNode)($5)).addColumn((CEPAttrSpecNode)($4)); 
      ((LinkedList)($1)).add($4); ((LinkedList)($1)).addAll((LinkedList)$7); 
      $$ = new CEPRelationAttrSpecsNode((List)$1, (CEPRelationConstraintNode)$7);}

   | attrspec ',' attrspec inline_constraint ',' non_mt_attrspec_list
     {((LinkedList)($6)).addFirst($1); ((CEPRelationConstraintNode)($4)).addColumn((CEPAttrSpecNode)($3)); 
      ((LinkedList)($6)).addFirst($3); 
      $$ = new CEPRelationAttrSpecsNode((CEPRelationConstraintNode)$4, (List)$6);}

   | attrspec inline_constraint
     {attrSpecList = new LinkedList(); attrSpecList.addFirst($1); 
      ((CEPRelationConstraintNode)($2)).addColumn((CEPAttrSpecNode)($1)); 
      $$ = new CEPRelationAttrSpecsNode((List)attrSpecList, (CEPRelationConstraintNode)$2);}

   | attrspec ',' out_of_line_constraint
     {attrSpecList = new LinkedList(); attrSpecList.addFirst($1); 
      $$ = new CEPRelationAttrSpecsNode((List)attrSpecList, (CEPRelationConstraintNode)$3);}
   
   | non_mt_attrspec_list
     {$$ = new CEPRelationAttrSpecsNode((List)$1);}
   ;

non_mt_attrname_list
   : identifier ',' non_mt_attrname_list
     {((LinkedList)($3)).addFirst((CEPStringTokenNode)($1)); $$ = $3;}

   | identifier
     {attrNameList = new LinkedList(); attrNameList.add((CEPStringTokenNode)($1)); $$ = attrNameList;}
   ;

attrspec
   : identifier fixed_length_datatype
     {$$ = new CEPAttrSpecNode((CEPStringTokenNode)($1), (Datatype)($2));}

   | identifier variable_length_datatype '(' intToken ')'
     {$$ = new CEPAttrSpecNode((CEPStringTokenNode)($1), (Datatype)($2), (CEPIntTokenNode)$4);}
     
   | identifier extensible_qualified_identifier '[' ']'
     {Datatype extensibleType = CartridgeHelper.getArrayType(execContext, (List) $2);
      $$ = new CEPAttrSpecNode((CEPStringTokenNode)($1), extensibleType);}
     
   | identifier numberToken '(' intToken ',' intToken ')'
     {$$ = new CEPAttrSpecNode((CEPStringTokenNode)($1), (Datatype)($2), (CEPIntTokenNode)$4 , (CEPIntTokenNode)$6);}

   ;

inline_constraint
   : RW_PRIMARY RW_KEY
     {$$ = new CEPRelationConstraintNode(); ((CEPRelationConstraintNode)($$)).setStartOffset(startOffset); ((CEPRelationConstraintNode)($$)).setEndOffset(endOffset);}
   ;

out_of_line_constraint
   : RW_PRIMARY RW_KEY '(' non_mt_attrname_list ')'
     {$$ = new CEPRelationConstraintNode((List)($4));}
   ;
   
query
   : sfw_block_n
    { $$ = $1;}

   | idstream_clause '(' sfw_block_n ')' using_clause
     {
       $$ = new CEPQueryStreamNode((CEPRelationNode)($3), (RelToStrOp)($1), (List)($5));
     }

   | RW_RSTREAM '(' sfw_block_n ')' 
     { 
       $$ = new CEPQueryStreamNode((CEPRelationNode)($3), RelToStrOp.RSTREAM); 
     }

   | binary_n
     { $$ = $1;}

   | nary_n
     { $$ = $1; }
    
   | idstream_clause '(' binary_n ')' using_clause
     {
       $$ = new CEPQueryStreamNode((CEPRelationNode)($3), (RelToStrOp)($1), (List)($5));
     }

   | idstream_clause '(' nary_n ')' using_clause
     {
       $$ = new CEPQueryStreamNode((CEPRelationNode)($3), (RelToStrOp)($1), (List)($5));
     }

   | RW_RSTREAM '(' binary_n ')' 
     { 
       $$ = new CEPQueryStreamNode((CEPRelationNode)($3), RelToStrOp.RSTREAM); 
     }

   | RW_RSTREAM '(' nary_n')' 
     { 
       $$ = new CEPQueryStreamNode((CEPRelationNode)($3), RelToStrOp.RSTREAM); 
     }

   ;

idstream_clause
   : RW_ISTREAM
     {$$ = RelToStrOp.ISTREAM;}

   | RW_DSTREAM
     {$$ = RelToStrOp.DSTREAM;}
   ;

sfw_block
   : select_clause from_clause opt_where_clause opt_group_by_clause opt_having_clause opt_order_by_clauses
     {$$ = new CEPSFWQueryNode((CEPSelectListNode)($1), (List)($2), (CEPBooleanExprNode)($3), (List)($4), (CEPBooleanExprNode)($5), (CEPOrderByNode)($6));}
   ;

sfw_block_n
   : sfw_block opt_evaluate_clause
     {
       CEPQueryRelationNode queryRelNode = (CEPQueryRelationNode)($1);
       queryRelNode.setEvaluateClause((CEPSlideExprNode)($2));
       $$ = queryRelNode;
     }
   ;

select_clause
   : RW_SELECT RW_DISTINCT non_mt_projterm_list
     {$$ = new CEPSelectListNode(true, (List)($3));}

   | RW_SELECT non_mt_projterm_list
     {$$ = new CEPSelectListNode(false, (List)($2));}

   | RW_SELECT RW_DISTINCT '*'
     {$$ = new CEPSelectListNode(true); ((CEPSelectListNode)$$).setStartOffset(0); ((CEPSelectListNode)$$).setEndOffset(endOffset);}

   | RW_SELECT '*'
     {$$ = new CEPSelectListNode(false); ((CEPSelectListNode)$$).setStartOffset(0); ((CEPSelectListNode)$$).setEndOffset(endOffset);}
   ;

from_clause
   : RW_FROM non_mt_relation_list
     {$$ = $2;}
   ;
   
opt_order_by_clauses
   : {/*empty*/ $$=null;}
   | order_by_clause
     {$$ = $1;}
   | order_by_top_clause
     {$$ = $1;}

order_by_clause
   : RW_ORDER RW_BY order_by_list
     {$$ = new CEPOrderByNode((List)($3), null);}
   ;

order_by_top_clause
   : RW_ORDER RW_BY order_by_list RW_ROWS intToken
     {$$ = new CEPOrderByNode((List)($3), new CEPOrderByTopExprNode((CEPIntTokenNode)($5), startOffset, endOffset) );}

   | partition_clause RW_ORDER RW_BY order_by_list RW_ROWS intToken 
     {$$ = new CEPOrderByNode((List)($4), new CEPOrderByTopExprNode((CEPIntTokenNode)($6), (List)($1), startOffset, endOffset) );}

   | RW_ORDER RW_BY order_by_list RW_ROWS intToken partition_clause
     {$$ = new CEPOrderByNode((List)($3), new CEPOrderByTopExprNode((CEPIntTokenNode)($5), (List)($6), startOffset, endOffset) );}
   ;

opt_where_clause
   : {/*empty  */ $$=null;}
   | RW_WHERE non_mt_cond_list
     {$$ = $2;}
   ;

opt_group_by_clause
   : {/*empty  */ $$=null;}
   | RW_GROUP RW_BY arith_expr_list
     {$$ = $3;}
   ;

opt_having_clause
   : {/*empty  */ $$=null;}
   | RW_HAVING non_mt_cond_list
     {$$ = $2;}
   ;

using_clause: { /* empty */ $$ = null; };
   | RW_DIFFERENCE RW_USING '(' usinglist ')'
     { $$ = $4;}
   ;
   
non_mt_projterm_list
   : projterm ',' non_mt_projterm_list
     {((LinkedList)($3)).addFirst($1); $$ = $3;}

   | projterm
     {projList = new LinkedList(); projList.add($1); $$ = projList;}
   ;

projterm  
   : identifier T_DOTSTAR
     {$$ = new CEPRelationStarNode((CEPStringTokenNode)($1));} 
     
   | arith_expr
     {$$ = $1;}
     
   | arith_expr RW_AS identifier
     {((CEPExprNode)$1).setAlias((CEPStringTokenNode)($3)); $$ = $1;}
   ;


usinglist
   : usingterm ',' usinglist
     {((LinkedList)($3)).addFirst($1); $$ = $3;}
     
   | usingterm
     {usingList = new LinkedList(); usingList.add($1); $$ = usingList;}
   ;
   
usingterm
   : usingexpr
     {$$ = $1;}
   ;
 
usingexpr
   : attr
     {$$ = $1;}
     
   | const_int
     {$$ = $1;}
   ;

order_by_list
   : orderterm ',' order_by_list
     {((LinkedList)($3)).addFirst($1); $$ = $3;}
     
   | orderterm
     {orderList = new LinkedList(); orderList.add($1); $$ = orderList;}
   ;
   
orderterm
   : order_expr
     {$$ = new CEPOrderByExprNode((CEPExprNode)($1),  new Boolean(true), new Boolean(false));}
     
   | order_expr null_spec
     {$$ = new CEPOrderByExprNode((CEPExprNode)($1), new Boolean(true), (Boolean)($2)); }
     
   | order_expr asc_desc
     {$$ = new CEPOrderByExprNode((CEPExprNode)($1), (Boolean)($2), new Boolean(false));}
     
   | order_expr asc_desc null_spec
     {$$ = new CEPOrderByExprNode((CEPExprNode)($1), (Boolean)($2), (Boolean)($3));}
   ;
 
order_expr
   : attr
     {$$ = $1;}
     
   | const_int
     {$$ = $1;}
   ;  
     
     
null_spec
   : RW_NULLS RW_FIRST
     {$$ = new Boolean(true);}
     
   | RW_NULLS RW_LAST
     {$$ = new Boolean(false);}
   ;  
     
asc_desc
   : RW_ASC
     {$$ = new Boolean(true);}
     
   | RW_DESC
     {$$ = new Boolean(false);}
   ;
    
non_mt_attr_list
   : attr ',' non_mt_attr_list
     {((LinkedList)($3)).addFirst($1); $$ = $3;}

   | attr
     {attrList = new LinkedList(); attrList.add($1); $$ = attrList;}
   ;

non_mt_relation_list
   : generic_relation_variable ',' non_mt_relation_list
     {((LinkedList)($3)).addFirst($1); $$ = $3;}

   | generic_relation_variable
     {relList = new LinkedList(); relList.add($1); $$ = relList;}
   ;

generic_relation_variable
   : relation_variable outer_relation_list
     {$$ = new CEPOuterJoinRelationNode((CEPRelationNode)$1, (List)$2);}

   | relation_variable
     {$$ = (CEPRelationNode)($1);}

   ;

outer_relation_list
   : outer_join_relation_variable outer_relation_list
     {((LinkedList)($2)).addFirst($1); $$ = $2;}
   
   | outer_join_relation_variable
     {outerRightList = new LinkedList(); outerRightList.add($1); $$ = outerRightList;} 
   ;


outer_join_relation_variable
   : outer_join_type relation_variable RW_ON non_mt_cond_list
     {$$ = new CEPRightOuterJoinNode((CEPRelationNode)$2, (CEPBooleanExprNode)($4), (OuterJoinType)($1));} 

   ;

outer_join_type
   : RW_LEFT RW_OUTER RW_JOIN
     {$$ = OuterJoinType.LEFT_OUTER;}

   | RW_LEFT RW_JOIN
     {$$ = OuterJoinType.LEFT_OUTER;}

   | RW_RIGHT RW_OUTER RW_JOIN
     {$$ = OuterJoinType.RIGHT_OUTER;}

   | RW_RIGHT RW_JOIN
     {$$ = OuterJoinType.RIGHT_OUTER;}
     
   | RW_FULL RW_OUTER RW_JOIN
     {$$ = OuterJoinType.FULL_OUTER;}

   | RW_FULL RW_JOIN
     {$$ = OuterJoinType.FULL_OUTER;}
   ;

relation_variable
   : identifier '[' window_type ']'
     {
       $$ = new CEPWindowRelationNode(new 
                CEPBaseStreamNode((CEPStringTokenNode)($1)), 
                (CEPWindowExprNode)($3));
     }
 
   | '(' query ')' '[' window_type ']'
     {
       $$ = new 
              CEPWindowRelationNode(new CEPStreamSubqueryNode((CEPQueryNode)($2)),
                                    (CEPWindowExprNode)($5));
     }  

   | identifier '[' window_type ']' RW_AS identifier
     {
       $$ = new 
         CEPWindowRelationNode(new
         CEPBaseStreamNode((CEPStringTokenNode)($1), (CEPStringTokenNode)($6)), 
                           (CEPWindowExprNode)($3), 
                           (CEPStringTokenNode)($6));
     }

   | '(' query ')' '[' window_type ']' RW_AS identifier
     {
       $$ = new 
           CEPWindowRelationNode(new CEPStreamSubqueryNode((CEPQueryNode)($2),
                                                    (CEPStringTokenNode)($8)),
                                (CEPWindowExprNode)($5));
     }

   | identifier
     {$$ = new CEPBaseRelationNode((CEPStringTokenNode)($1));}

   | '(' query ')'
     {
       $$ = new CEPRelationSubqueryNode((CEPQueryNode)($2));
     }

   | identifier RW_AS identifier
     {$$ = new CEPBaseRelationNode((CEPStringTokenNode)($1), (CEPStringTokenNode)($3));}

   | '(' query ')' RW_AS identifier
     {
       $$ = new CEPRelationSubqueryNode((CEPQueryNode)($2), 
                                        (CEPStringTokenNode)($5));
     }

   | identifier pattern_recognition_clause1 RW_AS identifier
     {
       $$ = new 
           CEPWindowRelationNode(new CEPPatternStreamNode(new
                          CEPBaseStreamNode((CEPStringTokenNode)($1)),
                          (CEPRecognizeNode)($2), 
                          (CEPStringTokenNode)($4)), 
                          new CEPTimeWindowExprNode(new 
                          CEPTimeSpecNode(TimeUnit.SECOND, Constants.INFINITE)),
                          (CEPStringTokenNode)($4)); 
     }

   | '(' query ')' RW_AS identifier pattern_recognition_clause1 RW_AS identifier
     {
       $$ = new 
           CEPWindowRelationNode(new 
             CEPPatternStreamNode(new CEPStreamSubqueryNode((CEPQueryNode)($2),
                                                            (CEPStringTokenNode)($5)),
                   (CEPRecognizeNode)($6), (CEPStringTokenNode)($8)), 
                   new CEPTimeWindowExprNode(new 
                         CEPTimeSpecNode(TimeUnit.SECOND, Constants.INFINITE)),
                        (CEPStringTokenNode)($8)); 
     }


   | '(' non_mt_double_src_identifier_list ')' pattern_recognition_clause1 RW_AS identifier
     {
       $$ = new 
           CEPWindowRelationNode(new 
             CEPMultiStreamNode((List)($2),
                   (CEPRecognizeNode)($4), (CEPStringTokenNode)($6)), 
                   new CEPTimeWindowExprNode(new 
                         CEPTimeSpecNode(TimeUnit.SECOND, Constants.INFINITE)),
                        (CEPStringTokenNode)($6)); 
     }

   | identifier xmltable_clause RW_AS identifier
     {$$ = new CEPWindowRelationNode(new CEPXmlTableStreamNode(new CEPBaseStreamNode((CEPStringTokenNode)($1)), (CEPXmlTableNode)($2), (CEPStringTokenNode)($4)), new CEPTimeWindowExprNode(new CEPTimeSpecNode(TimeUnit.SECOND, Constants.INFINITE)), (CEPStringTokenNode)($4)); }

   | identifier '[' user_window_type ']'
     {$$ = new CEPWindowRelationNode(new CEPBaseStreamNode((CEPStringTokenNode)($1)), (CEPWindowExprNode)($3));}

   | identifier '[' user_window_type ']' RW_AS identifier
     {$$ = new CEPWindowRelationNode(new CEPBaseStreamNode((CEPStringTokenNode)($1),(CEPStringTokenNode)($6)), (CEPWindowExprNode)($3),(CEPStringTokenNode)($6));}

   | URW_TABLE '(' object_expr RW_AS identifier ')' RW_AS identifier
     {$$ = new CEPTableFunctionRelationNode((CEPExprNode)($3), (CEPStringTokenNode)($5), (CEPStringTokenNode)($8));}
    
   | URW_TABLE '(' object_expr RW_AS identifier RW_OF datatype ')' RW_AS identifier
     {$$ = new CEPTableFunctionRelationNode((CEPExprNode)($3), (CEPStringTokenNode)($5), (Datatype)($7), (CEPStringTokenNode)($10));}
 
   ;

non_mt_double_src_identifier_list
   : src_identifier_variable ',' src_identifier_variable ',' non_mt_src_identifier_list
     {((LinkedList)($5)).addFirst($1); ((LinkedList)($5)).addFirst($3); $$ = $5;}
   | src_identifier_variable ',' src_identifier_variable
     {srcList = new LinkedList(); srcList.add($1); srcList.add($3); $$ = srcList;}
;
   
non_mt_src_identifier_list
   : src_identifier_variable ',' non_mt_src_identifier_list
     {((LinkedList)($3)).addFirst($1); $$ = $3;}
   | src_identifier_variable
     {srcList = new LinkedList(); srcList.add($1); $$ = srcList;}
;
   
src_identifier_variable
   : identifier
     {$$ = new CEPBaseStreamNode((CEPStringTokenNode)($1));}
	 
   | identifier RW_AS identifier
     {$$ = new CEPBaseStreamNode((CEPStringTokenNode)($1), (CEPStringTokenNode)($3));}
	 
   | '(' query ')' RW_AS identifier
     {
       $$ = new CEPStreamSubqueryNode((CEPQueryNode)($2), 
                                        (CEPStringTokenNode)($5));
     }
;

user_window_type
   : identifier
     {$$ = new CEPExtensibleWindowExprNode((CEPStringTokenNode)($1));}
     
   | identifier '(' ')'
     {$$ = new CEPExtensibleWindowExprNode((CEPStringTokenNode)($1));}
     
   | identifier '(' non_mt_window_list ')'
     {$$ = new CEPExtensibleWindowExprNode((CEPStringTokenNode)($1), (List)($3));} 
   ;
   
non_mt_window_list
   : const_value ',' non_mt_window_list
     {((LinkedList)($3)).addFirst($1); $$ = $3;}

   | const_value
     {argList = new LinkedList(); argList.add($1); $$ = argList;}
   ;
   
window_type 
   : RW_RANGE time_spec
     {$$ = new CEPTimeWindowExprNode((CEPTimeSpecNode)($2));}

   | RW_RANGE time_spec RW_SLIDE time_spec
     {$$ = new CEPTimeWindowExprNode((CEPTimeSpecNode)($2), (CEPTimeSpecNode)($4));}

   | RW_NOW
     {$$ = new CEPTimeWindowExprNode(new CEPTimeSpecNode(TimeUnit.SECOND, 0)); ((CEPTimeWindowExprNode)$$).setStartOffset(startOffset); ((CEPTimeWindowExprNode)$$).setEndOffset(endOffset);}
      
   | RW_ROWS intToken 
     {$$ = new CEPRowsWindowExprNode((CEPIntTokenNode)($2));}

   | RW_ROWS intToken RW_SLIDE intToken
     {$$ = new CEPRowsWindowExprNode((CEPIntTokenNode)($2),(CEPIntTokenNode)($4));}

   | RW_RANGE RW_UNBOUNDED
     {$$ = new CEPTimeWindowExprNode(new CEPTimeSpecNode(TimeUnit.SECOND, Constants.INFINITE));}

   | RW_PARTITION RW_BY non_mt_attr_list RW_ROWS intToken
     {$$ = new CEPPartnWindowExprNode((List)$3, (CEPIntTokenNode)($5));}

   | RW_PARTITION RW_BY non_mt_attr_list RW_ROWS intToken RW_RANGE time_spec
     {$$ = new CEPPartnWindowExprNode((List)$3, (CEPIntTokenNode)($5), (CEPTimeSpecNode)$7);}

   | RW_PARTITION RW_BY non_mt_attr_list RW_ROWS intToken RW_RANGE time_spec RW_SLIDE time_spec
     {$$ = new CEPPartnWindowExprNode((List)$3, (CEPIntTokenNode)($5), (CEPTimeSpecNode)$7, (CEPTimeSpecNode)$9);}
   
   | RW_RANGE const_value RW_ON RW_ELEMENT_TIME
     {$$ = new CEPValueWindowExprNode((CEPConstExprNode)($2), (CEPExprNode)(new CEPAttrNode(StreamPseudoColumn.ELEMENT_TIME.getColumnName())));}

   | RW_RANGE const_value RW_ON identifier
     {$$ = new CEPValueWindowExprNode((CEPConstExprNode)($2), (CEPExprNode)(new CEPAttrNode((CEPStringTokenNode)($4))));}

   | RW_RANGE time_spec_with_timeunit RW_ON identifier
     {$$ = new CEPValueWindowExprNode((CEPTimeSpecNode)($2), (CEPExprNode)(new CEPAttrNode((CEPStringTokenNode)($4))));}
   
   | RW_RANGE time_spec_with_timeunit RW_ON RW_ELEMENT_TIME
     {$$ = new CEPValueWindowExprNode((CEPTimeSpecNode)($2), (CEPExprNode)(new CEPAttrNode(StreamPseudoColumn.ELEMENT_TIME.getColumnName())));}
   
   | RW_RANGE const_value RW_ON identifier RW_SLIDE bigIntToken
     {$$ = new CEPValueWindowExprNode((CEPConstExprNode)($2), 
                                      (CEPExprNode)(new CEPAttrNode((CEPStringTokenNode)($4))), 
                                      (CEPBigIntTokenNode)($6));}
 
   | RW_RANGE const_value RW_ON RW_ELEMENT_TIME RW_SLIDE bigIntToken
     {$$ = new CEPValueWindowExprNode((CEPConstExprNode)($2), 
                                      (CEPExprNode)(new CEPAttrNode(StreamPseudoColumn.ELEMENT_TIME.getColumnName())), 
                                      (CEPBigIntTokenNode)($6));}

   | RW_RANGE time_spec_with_timeunit RW_ON identifier RW_SLIDE time_spec_with_timeunit
     {$$ = new CEPValueWindowExprNode((CEPTimeSpecNode)($2), 
                                      (CEPExprNode)(new CEPAttrNode((CEPStringTokenNode)($4))),
                                      (CEPTimeSpecNode)($6));}

   | RW_RANGE time_spec_with_timeunit RW_ON RW_ELEMENT_TIME RW_SLIDE time_spec_with_timeunit
     {$$ = new CEPValueWindowExprNode((CEPTimeSpecNode)($2), 
                                      (CEPExprNode)(new CEPAttrNode(StreamPseudoColumn.ELEMENT_TIME.getColumnName())),
                                      (CEPTimeSpecNode)($6));}

   | URW_CURRENTHOUR RW_ON identifier
     {$$ = new CEPValueWindowExprNode(ValueWindowType.CURRENT_HOUR, 
                                      (CEPExprNode)(new CEPAttrNode((CEPStringTokenNode)($3))), 
                                      null, 
                                      null,
                                      null);}

   | URW_CURRENTHOUR RW_ON RW_ELEMENT_TIME
     {$$ = new CEPValueWindowExprNode(ValueWindowType.CURRENT_HOUR, 
                                      (CEPExprNode)(new CEPAttrNode(StreamPseudoColumn.ELEMENT_TIME.getColumnName())), 
                                      null, 
                                      null,
                                      null);}

   | URW_CURRENTPERIOD '(' qstringToken ',' qstringToken ')' RW_ON identifier
     {$$ = new CEPValueWindowExprNode(ValueWindowType.CURRENT_PERIOD, 
                                      (CEPExprNode)(new CEPAttrNode((CEPStringTokenNode)($8))), 
                                      (CEPStringTokenNode)($3), 
                                      (CEPStringTokenNode)($5),
                                      null);}

   | URW_CURRENTPERIOD '(' qstringToken ',' qstringToken ')' RW_ON RW_ELEMENT_TIME
     {$$ = new CEPValueWindowExprNode(ValueWindowType.CURRENT_PERIOD, 
                                      (CEPExprNode)(new CEPAttrNode(StreamPseudoColumn.ELEMENT_TIME.getColumnName())), 
                                      (CEPStringTokenNode)($3), 
                                      (CEPStringTokenNode)($5),
                                      null);}

   | URW_CURRENTHOUR RW_ON identifier RW_SLIDE time_spec_with_timeunit
     {$$ = new CEPValueWindowExprNode(ValueWindowType.CURRENT_HOUR, 
                                      (CEPExprNode)(new CEPAttrNode((CEPStringTokenNode)($3))), 
                                      null, 
                                      null,
                                      (CEPTimeSpecNode)($5));}

   | URW_CURRENTHOUR RW_ON RW_ELEMENT_TIME RW_SLIDE time_spec_with_timeunit
     {$$ = new CEPValueWindowExprNode(ValueWindowType.CURRENT_HOUR, 
                                      (CEPExprNode)(new CEPAttrNode(StreamPseudoColumn.ELEMENT_TIME.getColumnName())), 
                                      null, 
                                      null,
                                      (CEPTimeSpecNode)($5));}

   | URW_CURRENTPERIOD '(' qstringToken ',' qstringToken ')' RW_ON identifier RW_SLIDE time_spec_with_timeunit
     {$$ = new CEPValueWindowExprNode(ValueWindowType.CURRENT_PERIOD, 
                                      (CEPExprNode)(new CEPAttrNode((CEPStringTokenNode)($8))), 
                                      (CEPStringTokenNode)($3), 
                                      (CEPStringTokenNode)($5),
                                      (CEPTimeSpecNode)($10));}

   | URW_CURRENTPERIOD '(' qstringToken ',' qstringToken ')' RW_ON RW_ELEMENT_TIME RW_SLIDE time_spec_with_timeunit
     {$$ = new CEPValueWindowExprNode(ValueWindowType.CURRENT_PERIOD, 
                                      (CEPExprNode)(new CEPAttrNode(StreamPseudoColumn.ELEMENT_TIME.getColumnName())), 
                                      (CEPStringTokenNode)($3), 
                                      (CEPStringTokenNode)($5),
                                      (CEPTimeSpecNode)($10));}
   ;

time_spec
   : time_spec_without_timeunit
     {$$ = $1;}

   | time_spec_with_timeunit
     {$$ = $1;}
   ;
   
time_spec_without_timeunit   
   : intToken
     {$$ = new CEPTimeSpecNode(TimeUnit.NOTIMEUNIT, (CEPIntTokenNode)($1));}

   | bigIntToken
     {$$ = new CEPTimeSpecNode(TimeUnit.NOTIMEUNIT, (CEPBigIntTokenNode)($1));}

   | non_const_arith_expr
     {$$ = new CEPTimeSpecNode(TimeUnit.NOTIMEUNIT, (CEPExprNode)($1));}
   ;

time_spec_with_timeunit
   : intToken time_unit
     {$$ = new CEPTimeSpecNode((TimeUnit)($2),(CEPIntTokenNode)($1));}

   | bigIntToken time_unit
     {$$ = new CEPTimeSpecNode((TimeUnit)($2), (CEPBigIntTokenNode)($1));}
   
   | non_const_arith_expr time_unit
     {$$ = new CEPTimeSpecNode((TimeUnit)($2),(CEPExprNode)($1));}
   ;

time_unit
   : RW_NANOSECOND
     {$$ = TimeUnit.NANOSECOND;}

   | RW_MICROSECOND
     {$$ = TimeUnit.MICROSECOND;}
     
   | RW_MILLISECOND
     {$$ = TimeUnit.MILLISECOND;}  

   | RW_SECOND
     {$$ = TimeUnit.SECOND;}

   | RW_MINUTE
     {$$ = TimeUnit.MINUTE;}

   | RW_HOUR
     {$$ = TimeUnit.HOUR;}

   | RW_DAY
     {$$ = TimeUnit.DAY;}

   | RW_MONTH
     {$$ = TimeUnit.MONTH;}

   | RW_YEAR
     {$$ = TimeUnit.YEAR;}

   | URW_UNIT
     {$$ = TimeUnit.NANOSECOND;}
   ;

xmltable_clause
   : RW_XMLTABLE '(' xmlnamespace_clause ',' sqstringToken RW_PASSING RW_BY RW_VALUE xqryargs_list RW_COLUMNS xtbl_cols_list ')'
	 {$$ = new CEPXmlTableNode((CEPStringTokenNode)($5), (List)($9), (List)($11), (List)($3));} 
   
   | RW_XMLTABLE '(' sqstringToken RW_PASSING RW_BY RW_VALUE xqryargs_list RW_COLUMNS xtbl_cols_list ')'
     {$$ = new CEPXmlTableNode((CEPStringTokenNode)($3), (List)$7, (List)$9);} 
   ;
   
xmlnamespace_clause
   : URW_XMLNAMESPACES '(' xmlnamespaces_list ')'
     {$$ = (List)($3);}
   ;
   
xmlnamespaces_list
   : xml_namespace ',' xmlnamespaces_list
   	 {((LinkedList)($3)).addFirst($1); $$ = $3;}
   	 
   | xml_namespace
     {xmlNamespaceList = new LinkedList(); xmlNamespaceList.add($1); $$ = xmlNamespaceList;}
   ;

xml_namespace
   : sqstringToken RW_AS sqstringToken
     {$$ = new CEPXmlNamespaceNode((CEPStringTokenNode)($1), (CEPStringTokenNode)($3));}
   
   | sqstringToken RW_AS qstringToken
     {$$ = new CEPXmlNamespaceNode((CEPStringTokenNode)($1), (CEPStringTokenNode)($3));}
   
   | qstringToken RW_AS sqstringToken
     {$$ = new CEPXmlNamespaceNode((CEPStringTokenNode)($1), (CEPStringTokenNode)($3));}
   
   | qstringToken RW_AS qstringToken
     {$$ = new CEPXmlNamespaceNode((CEPStringTokenNode)($1), (CEPStringTokenNode)($3));}
   
   | RW_DEFAULT qstringToken
   	 {$$ = new CEPXmlNamespaceNode((CEPStringTokenNode)($2));}
   	 
   | RW_DEFAULT sqstringToken
   	 {$$ = new CEPXmlNamespaceNode((CEPStringTokenNode)($2));}
   ;

xtbl_cols_list 
   : xtbl_col ',' xtbl_cols_list
     {((LinkedList)($3)).addFirst($1); $$ = $3;}
   
   | xtbl_col
     {xtblColList = new LinkedList(); xtblColList.add($1); $$ = xtblColList;}
	
   ;

xtbl_col
   : identifier fixed_length_datatype RW_PATH sqstringToken
     {$$ = new CEPXmlTableColumnNode((CEPStringTokenNode)($1), (Datatype)($2), (CEPStringTokenNode)($4));}

   | identifier variable_length_datatype '(' intToken ')' RW_PATH sqstringToken
     {$$ = new CEPXmlTableColumnNode((CEPStringTokenNode)($1), (Datatype)($2), (CEPIntTokenNode)($4), (CEPStringTokenNode)($7));}
     
  ;

pattern_recognition_clause1
   : pattern_recognition_clause 
     {defaultSubsetRequired = false; $$ = $1;}

pattern_recognition_clause
   : RW_MATCH_RECOGNIZE '(' opt_partition_clause pattern_measures_clause pattern_skip_match_clause RW_INCLUDE RW_TIMER RW_EVENTS pattern_clause duration_clause subset_clause pattern_definition_clause ')'
     {$$ = new CEPRecognizeNode((List)($3), (CEPPatternMeasuresNode)($4), (PatternSkip)($5), (CEPPatternNode)($9), (CEPPatternDefinitionNode)($12), (List)($11), (CEPPatternDurationNode)($10), null, defaultSubsetRequired);}

   | RW_MATCH_RECOGNIZE '(' opt_partition_clause pattern_measures_clause pattern_skip_match_clause pattern_clause within_clause subset_clause pattern_definition_clause ')'
     {$$ = new CEPRecognizeNode((List)($3), (CEPPatternMeasuresNode)($4), (PatternSkip)($5), (CEPPatternNode)($6), (CEPPatternDefinitionNode)($9), (List)($8), null, (CEPPatternWithinNode)($7), defaultSubsetRequired);}
   ;

duration_clause
   : RW_DURATION time_spec
     {$$ = new CEPPatternDurationNode((CEPTimeSpecNode)($2));}
   | RW_DURATION RW_MULTIPLES RW_OF time_spec
     {$$ = new CEPPatternDurationNode((CEPTimeSpecNode)($4), true);}
   ;

within_clause
   : { /* empty */ $$ = null; }
   | URW_WITHIN time_spec
     {$$ = new CEPPatternWithinNode((CEPTimeSpecNode)($2));}
   | URW_WITHIN URW_INCLUSIVE time_spec
     {$$ = new CEPPatternWithinNode((CEPTimeSpecNode)($3), true);}
   ;

subset_clause
   : { /* empty */ $$ = null;}
   | RW_SUBSET non_mt_subset_definition_list
     {$$ = $2;}
   ;
   
non_mt_subset_definition_list
   : subset_definition non_mt_subset_definition_list
     {((LinkedList)($2)).addFirst($1); $$ = $2;}
   
   | subset_definition
     {subsetList = new LinkedList(); subsetList.add($1); $$ = subsetList;}
   ;

subset_definition
   : subset_name T_EQ '(' non_mt_corr_list ')'
     {$$ = new CEPSubsetDefNode((CEPStringTokenNode)($1), (List)($4));}
   ;

subset_name
   : stringToken 
     {$$ = $1;}
   ;

non_mt_corr_list
   : correlation_name ',' non_mt_corr_list
     {((LinkedList)($3)).addFirst($1); $$ = $3;}
     
   | correlation_name
     {corrList = new LinkedList(); corrList.add($1); $$ = corrList;}
   ;

pattern_clause
   : RW_PATTERN '(' regexp ')'
     {$$ = new CEPPatternNode((CEPRegexpNode)($3));}
   ;

regexp
   : correlation_name pattern_quantifier
     {$$ = new CEPComplexRegexpNode((RegexpOp)($2), new CEPSimpleRegexpNode((CEPStringTokenNode)($1))); }

   | correlation_name
     {$$ = new CEPSimpleRegexpNode((CEPStringTokenNode)$1);}

   | '(' regexp ')'
     {
     	$$ = $2;
     	((CEPRegexpNode)$$).setMyString("(" + ($2).toString() + ")");
     }

   | '(' regexp ')' pattern_quantifier
     {
     	$$ = new CEPComplexRegexpNode((RegexpOp)($4), (CEPRegexpNode) ($2));
  		((CEPComplexRegexpNode)$$).setMyString("(" + ($2).toString() + ")" + ($4).toString());   	
     }

   | regexp '|' regexp
     {$$ = new CEPComplexRegexpNode(RegexpOp.ALTERNATION, (CEPRegexpNode)($1), (CEPRegexpNode)($3)); }

   | regexp regexp %prec '*'
     {$$ = new CEPComplexRegexpNode(RegexpOp.CONCAT, (CEPRegexpNode)($1), (CEPRegexpNode)($2)); }
   ;

correlation_name
   : stringToken 
     {$$ = $1;}
   ;

pattern_quantifier
   : '*' '?'
     {$$ = RegexpOp.LAZY_STAR;}

   | '+' '?'
     {$$ = RegexpOp.LAZY_PLUS;}

   | '?' '?'
     {$$ = RegexpOp.LAZY_QUESTION;}

   | '*'
     {$$ = RegexpOp.GREEDY_STAR;}

   | '+'
     {$$ = RegexpOp.GREEDY_PLUS;}

   | '?'
     {$$ = RegexpOp.GREEDY_QUESTION;}
   ;

opt_partition_clause
    : { /* empty */ $$ = null; }

    | partition_clause
      {$$ = $1;}
    ;
     
partition_clause
    : RW_PARTITION RW_BY non_mt_attr_list
     {$$ = $3;}
    ;

pattern_measures_clause
   : RW_MEASURES {insideDefineOrMeasures = true;} non_mt_measure_list
     {insideDefineOrMeasures = false; $$ = new CEPPatternMeasuresNode((List)($3));}
   ;

non_mt_measure_list
   : measure_column ',' non_mt_measure_list
     {((LinkedList)($3)).addFirst($1); $$ = $3;}
   
   | measure_column
     {measureColList = new LinkedList(); measureColList.add($1); $$ = measureColList;}
   ;

measure_column
   : arith_expr RW_AS identifier
     {((CEPExprNode)$1).setAlias((CEPStringTokenNode)($3)); $$ = $1;}
   ;

pattern_skip_match_clause
   : { /*empty - skip past last row */ $$ = PatternSkip.SKIP_PAST_LAST_ROW;}
   | RW_ALL RW_MATCHES
     {$$ = PatternSkip.ALL_MATCHES;}
   ;

pattern_definition_clause
   : RW_DEFINE {insideDefineOrMeasures = true;} non_mt_corrname_definition_list
     {insideDefineOrMeasures = false; $$ = new CEPPatternDefinitionNode((List)($3));}
   ;

non_mt_corrname_definition_list
   : correlation_name_definition ',' non_mt_corrname_definition_list
     {((LinkedList)($3)).addFirst($1); $$ = $3;}

   | correlation_name_definition
     {corrNameDefList = new LinkedList(); corrNameDefList.add($1); $$ = corrNameDefList;}
   ;

correlation_name_definition
   : correlation_name RW_AS non_mt_cond_list
     {$$ = new CEPCorrNameDefNode((CEPStringTokenNode)($1), (CEPBooleanExprNode)($3));}
   ;

non_mt_cond_list
   : non_mt_cond_list RW_AND non_mt_cond_list
     {
     	$$ = new CEPComplexBooleanExprNode(LogicalOp.AND, (CEPExprNode)($1), (CEPExprNode)($3));
     }
   
   | non_mt_cond_list RW_OR non_mt_cond_list
     {
     	$$ = new CEPComplexBooleanExprNode(LogicalOp.OR, (CEPExprNode)($1), (CEPExprNode)($3));
     }

   | non_mt_cond_list RW_XOR non_mt_cond_list
     {
     	$$ = new CEPComplexBooleanExprNode(LogicalOp.XOR, (CEPExprNode)($1), (CEPExprNode)($3));
     }

   | RW_NOT non_mt_cond_list
     {
     	$$ = new CEPComplexBooleanExprNode(LogicalOp.NOT, (CEPExprNode)($2));
     }

   | '(' non_mt_cond_list ')'
     {
     	$$ = $2;
     	((CEPBooleanExprNode)$$).setMyString("("+($2).toString()+")");
     }

   | condition
     {$$ = $1;}
     
   | between_condition
     {$$ = $1;}
   ;
   
between_condition
   : arith_expr RW_BETWEEN arith_expr RW_AND arith_expr
     {
     	$$ = new CEPComplexBooleanExprNode(LogicalOp.AND, 
               new CEPBaseBooleanExprNode(CompOp.GE, (CEPExprNode)($1), (CEPExprNode)($3)),
               new CEPBaseBooleanExprNode(CompOp.LE, (CEPExprNode)($1), (CEPExprNode)($5)));
     }
   ;

condition
   : arith_expr T_LT arith_expr
     {
     	$$ = new CEPBaseBooleanExprNode(CompOp.LT, (CEPExprNode)($1), (CEPExprNode)($3));
     }

   | arith_expr T_JPLUS T_LT arith_expr
     {
     	$$ = new CEPBaseBooleanExprNode(CompOp.LT, (CEPExprNode)($1), (CEPExprNode)($4),OuterJoinType.RIGHT_OUTER);
     }

   | arith_expr T_LT arith_expr T_JPLUS
     {
     	$$ = new CEPBaseBooleanExprNode(CompOp.LT, (CEPExprNode)($1), (CEPExprNode)($3), OuterJoinType.LEFT_OUTER);
     }

   | arith_expr T_LE arith_expr
     {
     	$$ = new CEPBaseBooleanExprNode(CompOp.LE, (CEPExprNode)($1), (CEPExprNode)($3));
     }

   | arith_expr T_JPLUS T_LE arith_expr
     {
     	$$ = new CEPBaseBooleanExprNode(CompOp.LE, (CEPExprNode)($1), (CEPExprNode)($4), OuterJoinType.RIGHT_OUTER);
     }

   | arith_expr T_LE arith_expr T_JPLUS
     {
     	$$ = new CEPBaseBooleanExprNode(CompOp.LE, (CEPExprNode)($1), (CEPExprNode)($3), OuterJoinType.LEFT_OUTER);
     }

   | arith_expr T_GT arith_expr
     {
     	$$ = new CEPBaseBooleanExprNode(CompOp.GT, (CEPExprNode)($1), (CEPExprNode)($3));
     }

   | arith_expr T_JPLUS T_GT arith_expr
     {
     	$$ = new CEPBaseBooleanExprNode(CompOp.GT, (CEPExprNode)($1), (CEPExprNode)($4), OuterJoinType.RIGHT_OUTER);
     }

   | arith_expr T_GT arith_expr T_JPLUS
     {
     	$$ = new CEPBaseBooleanExprNode(CompOp.GT, (CEPExprNode)($1), (CEPExprNode)($3), OuterJoinType.LEFT_OUTER);
     }

   | arith_expr T_GE arith_expr
     {
     	$$ = new CEPBaseBooleanExprNode(CompOp.GE, (CEPExprNode)($1), (CEPExprNode)($3));
     }

   | arith_expr T_JPLUS T_GE arith_expr
     {
     	$$ = new CEPBaseBooleanExprNode(CompOp.GE, (CEPExprNode)($1), (CEPExprNode)($4), OuterJoinType.RIGHT_OUTER);
     }

   | arith_expr T_GE arith_expr T_JPLUS
     {
     	$$ = new CEPBaseBooleanExprNode(CompOp.GE, (CEPExprNode)($1), (CEPExprNode)($3), OuterJoinType.LEFT_OUTER);
     }

   | arith_expr T_EQ arith_expr
     {
     	$$ = new CEPBaseBooleanExprNode(CompOp.EQ, (CEPExprNode)($1), (CEPExprNode)($3));
     }

   | arith_expr T_JPLUS T_EQ arith_expr
     {
     	$$ = new CEPBaseBooleanExprNode(CompOp.EQ, (CEPExprNode)($1), (CEPExprNode)($4), OuterJoinType.RIGHT_OUTER);
     }

   | arith_expr T_EQ arith_expr T_JPLUS
     {
     	$$ = new CEPBaseBooleanExprNode(CompOp.EQ, (CEPExprNode)($1), (CEPExprNode)($3), OuterJoinType.LEFT_OUTER);
     }

   | arith_expr T_NE arith_expr
     {
     	$$ = new CEPBaseBooleanExprNode(CompOp.NE, (CEPExprNode)($1), (CEPExprNode)($3));
     }
     
   | arith_expr T_JPLUS T_NE arith_expr
     {
     	$$ = new CEPBaseBooleanExprNode(CompOp.NE, (CEPExprNode)($1), (CEPExprNode)($4), OuterJoinType.RIGHT_OUTER);
     }

   | arith_expr T_NE arith_expr T_JPLUS
     {
     	$$ = new CEPBaseBooleanExprNode(CompOp.NE, (CEPExprNode)($1), (CEPExprNode)($3), OuterJoinType.LEFT_OUTER);
     }

   | arith_expr RW_LIKE arith_expr
     {
     	$$ = new CEPBaseBooleanExprNode(CompOp.LIKE, (CEPExprNode)($1), (CEPExprNode)($3));
     }
     
   | arith_expr RW_IS RW_NULL
     {
     	$$ = new CEPBaseBooleanExprNode(UnaryOp.IS_NULL, (CEPExprNode)($1));
     }
     
   | arith_expr RW_IS RW_NOT RW_NULL
     {
     	$$ = new CEPBaseBooleanExprNode(UnaryOp.IS_NOT_NULL, (CEPExprNode)($1));
     }

   | arith_expr RW_IN '(' non_mt_arg_list ')'
     {
     	$$ = new CEPComplexBooleanExprNode((CEPExprNode)($1), (List)($4), false);
     }
     
   | arith_expr RW_NOT RW_IN '(' non_mt_arg_list ')'
     {
     	$$ = new CEPComplexBooleanExprNode((CEPExprNode)($1), (List)($5), true);
     }

   | '(' arith_expr ',' non_mt_arg_list ')' RW_IN '(' non_mt_arg_list_set ')'
     {
     	((LinkedList)($4)).addFirst($2); $$ = new CEPComplexBooleanExprNode((List)($4), (List)($8), false);
     }
     
   | '(' arith_expr ',' non_mt_arg_list ')' RW_NOT RW_IN '(' non_mt_arg_list_set ')'
     {
     	((LinkedList)($4)).addFirst($2); $$ = new CEPComplexBooleanExprNode((List)($4), (List)($9), true);
     }
   ;

non_mt_arg_list_set
   : '(' non_mt_arg_list ')' ',' non_mt_arg_list_set
      {((LinkedList)($5)).addFirst($2); $$ = $5;}
     
   | '(' non_mt_arg_list ')'
     {arithExprListSet = new LinkedList(); arithExprListSet.add($2); $$ = arithExprListSet;}
  ;

arith_expr
   : const_arith_expr
     {$$ = $1;}
    
   | non_const_arith_expr
     {$$ = $1;}
  ;   

const_arith_expr
   : const_value
     {$$ = $1;}
     
non_const_arith_expr
:  func_expr
     {$$ = $1;}
     
   | object_expr
     { $$ = $1; }
      
   | aggr_expr
     {$$ = $1;}
     
   | aggr_distinct_expr
     {$$ = $1;}
     
   | case_expr
     {$$ = $1;}
     
   | decode
     {$$ = $1;}
     
   | arith_expr '+' arith_expr
     {
     	$$ = new CEPArithExprNode(ArithOp.ADD, (CEPExprNode)($1), (CEPExprNode)($3));
     }

   | arith_expr '-' arith_expr
     {
     	$$ = new CEPArithExprNode(ArithOp.SUB, (CEPExprNode)($1), (CEPExprNode)($3));
     }

   | arith_expr '*' arith_expr
     {
     	$$ = new CEPArithExprNode(ArithOp.MUL, (CEPExprNode)($1), (CEPExprNode)($3));
     }

   | arith_expr '/' arith_expr
     {
     	$$ = new CEPArithExprNode(ArithOp.DIV, (CEPExprNode)($1), (CEPExprNode)($3));
     }

   | arith_expr '|' '|' arith_expr
     {
     	$$ = new CEPArithExprNode(ArithOp.CONCAT, (CEPExprNode)($1), (CEPExprNode)($4));
     }
     
   | '+' arith_expr %prec UNARYPREC
     {
     	$$ = $2;
     }
     
   | '-' arith_expr %prec UNARYPREC
     {
     	$$ = new CEPArithExprNode(ArithOp.MUL, new CEPIntConstExprNode(-1), (CEPExprNode)($2)); ((CEPArithExprNode)$$).setStartOffset(((CEPExprNode)$2).getStartOffset());
     }
     
   | '(' arith_expr ')'
     {
     	$$ = $2;
     	((CEPExprNode)$$).setMyString("(" + ($$).toString() + ")");
     }
     
   ;
   
attr
   : identifier '.' identifier
     {$$ = new CEPAttrNode((CEPStringTokenNode)($1), (CEPStringTokenNode)($3));}

   | identifier '.' pseudo_column
     {$$ = new CEPAttrNode((CEPStringTokenNode)($1), ((StreamPseudoColumn)($3)).getColumnName());}
     
   | identifier
     {if(insideDefineOrMeasures) 
      {
        defaultSubsetRequired = true;
        $$ = new CEPAttrNode(Constants.DEFAULT_SUBSET_NAME, (CEPStringTokenNode)($1));
      }
      else
        $$ = new CEPAttrNode((CEPStringTokenNode)($1));} 

   | pseudo_column
     {$$ = new CEPAttrNode(((StreamPseudoColumn)($1)).getColumnName()); ((CEPAttrNode)$$).setStartOffset(startOffset); ((CEPAttrNode)$$).setEndOffset(endOffset);}
   ;
   
extensible_attr
   : extensible_qualified_identifier
     {
      if (insideDefineOrMeasures || insideXmlAttr) 
      {
        List ids = (List) $1;
        if (ids.size() == 1)
        {
          if (insideDefineOrMeasures)
          {
            defaultSubsetRequired = true;
            $$ = new CEPAttrNode(Constants.DEFAULT_SUBSET_NAME, (CEPStringTokenNode) ids.get(0));
          }
          else /* insideXmlAttr */
            $$ = new CEPAttrNode((CEPStringTokenNode) ids.get(0));
        }
        else if (ids.size() == 2)
        {
          $$ = new CEPAttrNode((CEPStringTokenNode) ids.get(0), (CEPStringTokenNode) ids.get(1));
        }
        else /* greater than 2 ids */
        {
          if (insideDefineOrMeasures)
          {
            $$ = new CEPObjExprNode((List)($1));
          }
          else
           throw new SyntaxException(SyntaxError.ATTR_ID_ERROR, 
            ((CEPStringTokenNode) ids.get(2)).getStartOffset(), 
            ((CEPStringTokenNode) ids.get(ids.size()-1)).getEndOffset(), new Object[0]);
        }
      }
      else 
        $$ = new CEPObjExprNode((List)($1));
     }
        
   | extensible_identifier '.' pseudo_column
     {$$ = new CEPAttrNode((CEPStringTokenNode)($1), ((StreamPseudoColumn)($3)).getColumnName());}
     
   | pseudo_column
     {$$ = new CEPAttrNode(((StreamPseudoColumn)($1)).getColumnName()); ((CEPAttrNode)$$).setStartOffset(startOffset); ((CEPAttrNode)$$).setEndOffset(endOffset);}
   
   ;
    
object_expr
   /* handles both attributes as well as object types */
   : extensible_attr
     {$$ = $1;}

   | nested_method_field_expr
     {$$ = $1;}
     
   ;
   
nested_method_field_expr
   : nested_method_field_expr '.' extensible_identifier
     {$$ = new CEPObjExprNode((CEPObjExprNode)($1), (CEPStringTokenNode)($3));}

   | nested_method_field_expr '.' extensible_identifier '(' ')' 
     {$$ = new CEPObjExprNode((CEPObjExprNode)($1), (CEPStringTokenNode)($3), Collections.emptyList());}

   | nested_method_field_expr '.' extensible_identifier '(' non_mt_arg_list ')' 
     {$$ = new CEPObjExprNode((CEPObjExprNode)($1), (CEPStringTokenNode)($3),  (List)($5));}

   | method_expr
     {$$ = $1;}
     
   | array_expr
     {$$ = $1;}

   ;
   
method_expr
   : extensible_qualified_identifier '(' ')' 
     {$$ = new CEPObjExprNode((List)($1), Collections.emptyList());}
     
   | extensible_qualified_identifier '(' non_mt_arg_list ')'
     {$$ = new CEPObjExprNode((List)($1), (List)($3));}

   /** Handles only function references */
   | extensible_qualified_identifier '(' RW_DISTINCT  arith_expr ')'
     {argList = new LinkedList(); argList.add($4); $$ = new CEPFunctionExprNode((List)($1), argList, true);}
   ;
   
array_expr
   : object_expr '[' intToken ']'
     {$$ = new CEPObjExprNode((CEPObjExprNode)($1), (CEPIntTokenNode)($3));}
   ;    
   
pseudo_column
   : RW_ELEMENT_TIME
     {$$ = StreamPseudoColumn.ELEMENT_TIME;}

   | RW_QUERY_ID
     {$$ = StreamPseudoColumn.QUERY_ID;}
   ;

const_value
   : boolean_value
     {$$ = $1;}

   | interval_value
     {$$ = $1;}

   | const_string
     {$$ = $1;}
   
   | RW_NULL 
     {$$ = new CEPNullConstExprNode(); ((CEPNullConstExprNode)$$).setStartOffset(startOffset); ((CEPNullConstExprNode)$$).setEndOffset(endOffset);}

   | const_int
     {$$ = $1;}

   | const_bigint
     {$$ = $1;}

   | T_FLOAT
     {$$ = new CEPFloatConstExprNode($1);((CEPFloatConstExprNode)$$).setStartOffset(startOffset); ((CEPFloatConstExprNode)$$).setEndOffset(endOffset);}
     
   | T_DOUBLE
     {$$ = new CEPDoubleConstExprNode($1);((CEPDoubleConstExprNode)$$).setStartOffset(startOffset); ((CEPDoubleConstExprNode)$$).setEndOffset(endOffset);}  
   
   | T_NUMBER
     {$$ = new CEPBigDecimalConstExprNode((BigDecimal)$1);((CEPBigDecimalConstExprNode)$$).setStartOffset(startOffset); ((CEPBigDecimalConstExprNode)$$).setEndOffset(endOffset);}  
   ;
   
const_int
   : T_INT
     {$$ = new CEPIntConstExprNode($1); ((CEPIntConstExprNode)$$).setStartOffset(startOffset); ((CEPIntConstExprNode)$$).setEndOffset(endOffset);}
   ;

const_bigint
   : bigIntToken
     {
       Long bigIntVal = ((CEPBigIntTokenNode)($1)).getValue();
       $$ = new CEPBigintConstExprNode(bigIntVal);
       ((CEPBigintConstExprNode)$$).setStartOffset(startOffset); ((CEPBigintConstExprNode)$$).setEndOffset(endOffset);
     }
   ;

boolean_value
   : RW_TRUE
     {$$ = new CEPBooleanConstExprNode((String)"TRUE"); ((CEPBooleanConstExprNode)$$).setStartOffset(startOffset); ((CEPBooleanConstExprNode)$$).setEndOffset(endOffset); }

   | RW_FALSE
     {$$ = new CEPBooleanConstExprNode((String)"FALSE"); ((CEPBooleanConstExprNode)$$).setStartOffset(startOffset); ((CEPBooleanConstExprNode)$$).setEndOffset(endOffset);}
   ;
   
const_string
   : T_QSTRING
     {
     	$$ = new CEPStringConstExprNode($1);
     	((CEPStringConstExprNode)$$).setStartOffset(startOffset); 
     	((CEPStringConstExprNode)$$).setEndOffset(endOffset);
     	((CEPStringConstExprNode)$$).setSingleQuote(false);
     }
     
   | T_SQSTRING
     {
     	$$ = new CEPStringConstExprNode($1);
     	((CEPStringConstExprNode)$$).setStartOffset(startOffset); 
     	((CEPStringConstExprNode)$$).setEndOffset(endOffset);
     	((CEPStringConstExprNode)$$).setSingleQuote(true);
     }
   ;
     
interval_value
   : RW_INTERVAL T_QSTRING interval_format
     {$$ = new CEPIntervalConstExprNode($2, (IntervalFormat)$3);
      ((CEPIntervalConstExprNode)$$).setStartOffset(startOffset); ((CEPIntervalConstExprNode)$$).setEndOffset(endOffset);}
   ;

interval_format
   : time_unit 
     {$$ = new IntervalFormat((TimeUnit)$1);}

   | time_unit '(' const_int ')' 
     {$$ = new IntervalFormat((TimeUnit)$1, ((CEPIntConstExprNode)$3).getValue());}

   | time_unit RW_TO time_unit
     {$$ = new IntervalFormat((TimeUnit)($1), (TimeUnit)($3));}

   | time_unit '(' const_int ')' RW_TO time_unit
     {$$ = new IntervalFormat((TimeUnit)($1), (TimeUnit)($6), ((CEPIntConstExprNode)$3).getValue(), true);}

   | time_unit '(' const_int ',' const_int ')'
     {$$ = new IntervalFormat((TimeUnit)$1, ((CEPIntConstExprNode)$3).getValue(), ((CEPIntConstExprNode)$5).getValue());}

   | time_unit '(' const_int ')' RW_TO time_unit '(' const_int ')'
     {$$ = new IntervalFormat((TimeUnit)$1, (TimeUnit)$6, ((CEPIntConstExprNode)$3).getValue(), ((CEPIntConstExprNode)$8).getValue());}

   | time_unit RW_TO time_unit '(' const_int ')'
     {$$ = new IntervalFormat((TimeUnit)$1, (TimeUnit)$3, ((CEPIntConstExprNode)$5).getValue(), false);}
     
   ;

timestamp_format
   : '(' const_int ')'
     {
       TimestampFormat fmt = new TimestampFormat(((CEPIntConstExprNode)$2).getValue());
       $$ = fmt;
     }

   | '(' const_int ')' URW_WITH RW_TIMESTAMP URW_ZONE
     {
       TimestampFormat fmt = new TimestampFormat(((CEPIntConstExprNode)$2).getValue());
       fmt.setHasTimeZone(true);
       $$ = fmt;
     }

   | URW_WITH RW_TIMESTAMP URW_ZONE
     {
       TimestampFormat fmt = new TimestampFormat();
       fmt.setHasTimeZone(true);
       $$ = fmt;
     }
   
   | '(' const_int ')' URW_WITH URW_LOCAL RW_TIMESTAMP URW_ZONE
     {
       TimestampFormat fmt = new TimestampFormat(((CEPIntConstExprNode)$2).getValue());
       fmt.setLocalTimeZone(true);
       $$ = fmt;
     }

   | URW_WITH URW_LOCAL RW_TIMESTAMP URW_ZONE
     {
       TimestampFormat fmt = new TimestampFormat();
       fmt.setLocalTimeZone(true);
       $$ = fmt;
     }
   ;
 
func_expr
   : RW_PREV '(' identifier '.' identifier ')'
     {argList = new LinkedList(); argList.add(new CEPAttrNode((CEPStringTokenNode)($3), (CEPStringTokenNode)($5)));
      $$ = new CEPPREVExprNode(argList);}

   | RW_PREV '(' identifier '.' identifier ',' const_int ')'
     {argList = new LinkedList(); argList.add(new CEPAttrNode((CEPStringTokenNode)($3), (CEPStringTokenNode)($5)));
      argList.add($7); $$ = new CEPPREVExprNode(argList);}

   | RW_PREV '(' identifier '.' identifier ',' const_int ',' const_bigint ')'
     {argList = new LinkedList(); argList.add(new CEPAttrNode((CEPStringTokenNode)$3, (CEPStringTokenNode)$5));
      argList.add($7); argList.add($9);
      argList.add(new CEPAttrNode((CEPStringTokenNode)($3), StreamPseudoColumn.ELEMENT_TIME.getColumnName()));
      $$ = new CEPPREVExprNode(argList);}

   | URW_COALESCE '(' non_mt_arg_list ')'
     { $$ = new CEPCoalesceExprNode((List)($3)); }

   | RW_XMLQUERY '(' sqstringToken RW_PASSING RW_BY RW_VALUE xqryargs_list RW_RETURNING RW_CONTENT ')' RW_XMLDATA
     {$$ = new CEPXQryFunctionExprNode((CEPStringTokenNode)($3), (List)$7);}

   | RW_XMLEXISTS '(' sqstringToken RW_PASSING RW_BY RW_VALUE xqryargs_list RW_RETURNING RW_CONTENT ')' RW_XMLDATA
     {$$ = new CEPXExistsFunctionExprNode((CEPStringTokenNode)($3), (List)$7);}
   
   | RW_XMLCONCAT '(' non_mt_arg_list ')'
     {$$ = new CEPXMLConcatExprNode((List)($3));}
   	 
   | xml_parse_expr
     {$$ = $1;}
   
   | RW_FIRST '(' identifier '.' identifier ',' const_int ')'
     {argList = new LinkedList(); argList.add(new CEPAttrNode((CEPStringTokenNode)($3),(CEPStringTokenNode)($5)));
      argList.add($7); $$ = new CEPFirstLastMultiExprNode(AggrFunction.FIRST.getFuncName(), argList);}

   | RW_LAST '(' identifier '.' identifier ',' const_int ')'
     {argList = new LinkedList(); argList.add(new CEPAttrNode((CEPStringTokenNode)($3),(CEPStringTokenNode)($5)));
      argList.add($7); $$ = new CEPFirstLastMultiExprNode(AggrFunction.LAST.getFuncName(), argList);}
  
   | xmlelement_expr
     {$$ = $1;}
     
   | xmlforest_expr
     {$$ = $1;}
     
   | xmlcolattval_expr
     {$$ = $1;}
      
   ; 
   
xml_parse_expr
   : RW_XMLPARSE '(' RW_CONTENT arith_expr RW_WELLFORMED ')'
   	 { $$ = new CEPXMLParseExprNode((CEPExprNode)($4),true,true);}
   	 
   | RW_XMLPARSE '(' RW_CONTENT arith_expr ')'
   	 { $$ = new CEPXMLParseExprNode((CEPExprNode)($4),false,true);}
   	 
   | RW_XMLPARSE '(' RW_DOCUMENT arith_expr RW_WELLFORMED ')'
   	 { $$ = new CEPXMLParseExprNode((CEPExprNode)($4),true,false);}
   	 
   | RW_XMLPARSE '(' RW_DOCUMENT arith_expr ')'
     { $$ = new CEPXMLParseExprNode((CEPExprNode)($4),false,false);}
   ;
   
xml_agg_expr
   : RW_XMLAGG '(' arith_expr order_by_clause ')'
     {$$ = new CEPXMLAggNode((CEPExprNode)($3), (CEPOrderByNode)($4));} 
   
   | RW_XMLAGG '(' arith_expr ')'
     {$$ = new CEPXMLAggNode((CEPExprNode)($3),null);}
   ;
   
xmlelement_expr
   : RW_XMLELEMENT '(' URW_NAME qstringToken ',' xml_attribute_list ',' arith_expr_list ')' 
     {$$ = new CEPElementExprNode((CEPStringTokenNode)($4), (List)($6), (List)($8));}

   | RW_XMLELEMENT '(' RW_EVALNAME arith_expr ',' xml_attribute_list ',' arith_expr_list ')' 
     {$$ = new CEPElementExprNode((CEPExprNode)($4), (List)($6), (List)($8));}

   | RW_XMLELEMENT '(' qstringToken ',' xml_attribute_list ',' arith_expr_list ')' 
     {$$ = new CEPElementExprNode((CEPStringTokenNode)($3), (List)($5), (List)($7));}

   | RW_XMLELEMENT '(' URW_NAME qstringToken ',' arith_expr_list ')'
     {$$ = new CEPElementExprNode((CEPStringTokenNode)($4), null, (List)($6)) ;}
   
   | RW_XMLELEMENT '(' RW_EVALNAME arith_expr ',' arith_expr_list ')'
     {$$ = new CEPElementExprNode((CEPExprNode)$4, null, (List)($6)) ;}

   | RW_XMLELEMENT '(' qstringToken ',' arith_expr_list ')'
     {$$ = new CEPElementExprNode((CEPStringTokenNode)($3), null, (List)($5)) ;}

   | RW_XMLELEMENT '(' URW_NAME qstringToken ')'
     {$$ = new CEPElementExprNode((CEPStringTokenNode)($4), null, null);}
     
   | RW_XMLELEMENT '(' RW_EVALNAME arith_expr ')'
     {$$ = new CEPElementExprNode((CEPExprNode)$4, null, null);}

   | RW_XMLELEMENT '(' qstringToken ')'
     {$$ = new CEPElementExprNode((CEPStringTokenNode)($3), null, null);}

   | RW_XMLELEMENT '(' URW_NAME qstringToken ',' xml_attribute_list ')'
     {$$ = new CEPElementExprNode((CEPStringTokenNode)($4), (List)($6), null);}
   
   | RW_XMLELEMENT '(' RW_EVALNAME arith_expr ',' xml_attribute_list ')'
     {$$ = new CEPElementExprNode((CEPExprNode)$4, (List)($6), null);}

   | RW_XMLELEMENT '(' qstringToken ',' xml_attribute_list ')'
     {$$ = new CEPElementExprNode((CEPStringTokenNode)($3), (List)($5), null);}
   ;

xml_attribute_list
   : RW_XMLATTRIBUTES {insideXmlAttr = true;} xml_attr_list_aux
     {insideXmlAttr = false; $$ = $3;}

   ;
   
xml_attr_list_aux
   : '(' xml_attr_list ')'
     {$$ = $2;}
   
   | '(' ')'
     {$$ = new LinkedList();}
   
   ;  

xml_attr_list
   : xml_attr ',' xml_attr_list
     {((LinkedList)($3)).addFirst($1); $$ = $3;}

   | xml_attr
     {attrList = new LinkedList(); attrList.add($1); $$ = attrList;}
   ;

xml_attr
   : arith_expr RW_AS qstringToken
     {$$ = new CEPXmlAttrExprNode((CEPExprNode)$1, (CEPStringTokenNode)($3));}
     
   | arith_expr RW_AS RW_EVALNAME arith_expr
     {$$ = new CEPXmlAttrExprNode((CEPExprNode)$1, (CEPExprNode)$4);}

   | extensible_attr 
     {$$ = new CEPXmlAttrExprNode((CEPExprNode)$1);}
   ;
   
xmlforest_expr
   : RW_XMLFOREST {insideXmlAttr = true;} '(' xml_attr_list ')'
     {insideXmlAttr = false; $$ = new CEPXmlForestExprNode((List)$4);}
   ;
   
xmlcolattval_expr
   : RW_XMLCOLATTVAL {insideXmlAttr = true;} '(' xml_attr_list ')'
     {insideXmlAttr = false; $$ = new CEPXmlColAttValExprNode((List)$4);}
   ;

arith_expr_list
   : arith_expr ',' arith_expr_list
     {((LinkedList)($3)).addFirst($1); $$ = $3;}

   | arith_expr
     {arithList = new LinkedList(); arithList.add($1); $$ = arithList;}
   ;

xqryargs_list
   : xqryarg ',' xqryargs_list 
     {((LinkedList)($3)).addFirst($1); $$ = $3;}

   | xqryarg
     {argList = new LinkedList(); argList.add($1); $$ = argList;}
   ;

xqryarg
   : arith_expr RW_AS qstringToken
     {$$ = new CEPXQryArgExprNode((CEPExprNode)$1, (CEPStringTokenNode)($3));}
   ;

non_mt_arg_list
   : arith_expr ',' non_mt_arg_list
     {((LinkedList)($3)).addFirst($1); $$ = $3;}

   | arith_expr
     {argList = new LinkedList(); argList.add($1); $$ = argList;}
   ;
   
case_expr
   : RW_CASE searched_case_list RW_END
     {$$ = new CEPSearchedCaseExprNode((List)($2), new CEPNullConstExprNode()); ((CEPSearchedCaseExprNode)$$).setEndOffset(endOffset);}
   
   | RW_CASE searched_case_list RW_ELSE arith_expr RW_END
     {$$ = new CEPSearchedCaseExprNode((List)($2), (CEPExprNode)($4));}

   | RW_CASE arith_expr simple_case_list RW_END
     {$$ = new CEPSimpleCaseExprNode((CEPExprNode)($2), (List)($3), new CEPNullConstExprNode()); ((CEPSimpleCaseExprNode)$$).setEndOffset(endOffset);}
     
   | RW_CASE arith_expr simple_case_list RW_ELSE arith_expr RW_END
     {$$ = new CEPSimpleCaseExprNode((CEPExprNode)($2), (List)($3), (CEPExprNode)($5));}
   ;
   
 simple_case_list
   : simple_case simple_case_list
     {((LinkedList)($2)).addFirst($1); $$ = $2;}
     
   | simple_case
     {argList = new LinkedList(); argList.add($1); $$ = argList;}
   ;
   
 simple_case
   : RW_WHEN arith_expr RW_THEN arith_expr
     {$$ = new CEPCaseComparisonExprNode((CEPExprNode)($2),(CEPExprNode)($4));}
   ;
   
 searched_case_list
   : searched_case searched_case_list
     {((LinkedList)($2)).addFirst($1); $$ = $2;}
     
   | searched_case
     {argList = new LinkedList(); argList.add($1); $$ = argList;}
   ;
   
 searched_case
   : RW_WHEN non_mt_cond_list RW_THEN arith_expr
     {$$ = new CEPCaseConditionExprNode((CEPBooleanExprNode)($2),(CEPExprNode)($4));}
   ;
   
decode
   : RW_DECODE '(' non_mt_arg_list ')'
     {$$ = new CEPDecodeExprNode((List)($3));}
   ;
   
aggr_expr
   : RW_COUNT '(' arith_expr ')'
     {$$ = new CEPOtherAggrExprNode(AggrFunction.COUNT, (CEPExprNode)($3));}

   | RW_COUNT '('  '*' ')'
     {
       if(!insideDefineOrMeasures) {
         $$ = new CEPCountStarNode();
         ((CEPCountStarNode)$$).setStartOffset(startOffset);
         ((CEPCountStarNode)$$).setEndOffset(endOffset);
       }
       else{
         defaultSubsetRequired = true;
         $$ = new CEPCountCorrStarNode((CEPExprNode)(new CEPAttrNode(new CEPStringTokenNode(Constants.DEFAULT_SUBSET_NAME),
                                       new CEPStringTokenNode(null))));
         ((CEPCountCorrStarNode)$$).setStartOffset(startOffset);
         ((CEPCountCorrStarNode)$$).setEndOffset(endOffset);
       }
     }

   | RW_COUNT '(' identifier T_DOTSTAR ')'
     {$$ = new CEPCountCorrStarNode((CEPExprNode)(new CEPAttrNode((CEPStringTokenNode)($3), new CEPStringTokenNode(null)))); ((CEPCountCorrStarNode)$$).setEndOffset(endOffset);}

   | RW_SUM   '(' arith_expr ')'
     {$$ = new CEPOtherAggrExprNode(AggrFunction.SUM, (CEPExprNode)($3));}

   | RW_AVG   '(' arith_expr ')'
     {$$ = new CEPOtherAggrExprNode(AggrFunction.AVG, (CEPExprNode)($3));}

   | RW_MAX   '(' arith_expr ')'
     {$$ = new CEPOtherAggrExprNode(AggrFunction.MAX, (CEPExprNode)($3));}

   | RW_MIN   '(' arith_expr ')'
     {$$ = new CEPOtherAggrExprNode(AggrFunction.MIN, (CEPExprNode)($3));}

   | RW_FIRST '(' identifier '.' identifier ')'
     {$$ = new CEPFirstLastExprNode(AggrFunction.FIRST, 
                               (CEPExprNode)(new CEPAttrNode((CEPStringTokenNode)($3), (CEPStringTokenNode)($5))));}

   | RW_LAST  '(' identifier '.' identifier ')'
     {$$ = new CEPFirstLastExprNode(AggrFunction.LAST, 
                               (CEPExprNode)(new CEPAttrNode((CEPStringTokenNode)($3), (CEPStringTokenNode)($5))));}
                               
   | xml_agg_expr
     {$$ = $1;}
  ;
  
aggr_distinct_expr
   : RW_COUNT '(' RW_DISTINCT arith_expr ')'
     {$$ = new CEPOtherAggrExprNode(AggrFunction.COUNT, (CEPExprNode)($4), true);}

   | RW_SUM   '(' RW_DISTINCT arith_expr ')'
     {$$ = new CEPOtherAggrExprNode(AggrFunction.SUM, (CEPExprNode)($4), true);}

   | RW_AVG   '(' RW_DISTINCT arith_expr ')'
     {$$ = new CEPOtherAggrExprNode(AggrFunction.AVG, (CEPExprNode)($4), true);}

   | RW_MAX   '(' RW_DISTINCT arith_expr ')'
     {$$ = new CEPOtherAggrExprNode(AggrFunction.MAX, (CEPExprNode)($4), true);}

   | RW_MIN   '(' RW_DISTINCT arith_expr ')'
     {$$ = new CEPOtherAggrExprNode(AggrFunction.MIN, (CEPExprNode)($4), true);}
  ;

binary 
   : identifier RW_NOT RW_IN identifier
     {$$ = new CEPSetopQueryNode( RelSetOp.NOT_IN, (CEPStringTokenNode)($1), (CEPStringTokenNode)($4));}
          
   | identifier RW_IN identifier
     {$$ = new CEPSetopQueryNode( RelSetOp.IN, (CEPStringTokenNode)($1), (CEPStringTokenNode)($3));}
     
   ;


binary_n
   : binary opt_evaluate_clause
     {
       CEPQueryRelationNode queryRelNode = (CEPQueryRelationNode)($1); 
       queryRelNode.setEvaluateClause((CEPSlideExprNode)($2));
       $$ = queryRelNode;
     }
   ;

query_n
   : query
     { $$ = $1; }
  
   | '(' query ')'
     { $$ = $2; }
  

nary
   : query_n RW_UNION query_n
     { $$ = new CEPSetopSubqueryNode(RelSetOp.UNION, (CEPQueryNode)($1), (CEPQueryNode)($3), false); }

   | query_n RW_UNION RW_ALL query_n
     { $$ = new CEPSetopSubqueryNode(RelSetOp.UNION, (CEPQueryNode)($1), (CEPQueryNode)($4), true); }

   | query_n RW_EXCEPT query_n
     { $$ = new CEPSetopSubqueryNode(RelSetOp.EXCEPT, (CEPQueryNode)($1), (CEPQueryNode)($3), false); }

   | query_n RW_INTERSECT query_n
     { $$ = new CEPSetopSubqueryNode(RelSetOp.INTERSECT, (CEPQueryNode)($1), (CEPQueryNode)($3), false); }

   | query_n RW_MINUS query_n
     { $$ = new CEPSetopSubqueryNode(RelSetOp.MINUS, (CEPQueryNode)($1), (CEPQueryNode)($3), false); }

   | '(' nary ')'
     { $$ = $2; }    
   ;

nary
   : identifier setop_relation_list
     { $$ = new CEPGenericSetOpNode((CEPStringTokenNode)($1), (List)($2)); }
   ;

nary_n
  : nary opt_evaluate_clause 
     {
       CEPQueryRelationNode queryRelNode = (CEPQueryRelationNode)($1);
       queryRelNode.setEvaluateClause((CEPSlideExprNode)($2));
       $$ = queryRelNode;
     }
  ;    
setop_relation_list
  : setop_relation_variable setop_relation_list
    { ((LinkedList)($2)).addFirst($1); $$ = $2; }    
 
  | setop_relation_variable
    { setopRelList = new LinkedList();
      setopRelList.add($1); $$ = setopRelList; 
    } 

setop_relation_variable
  : RW_UNION identifier
    {
      $$ = new CEPSetOpNode((CEPStringTokenNode)($2), RelSetOp.UNION, false); 
    }

  | RW_UNION RW_ALL identifier
    { 
      $$ = new CEPSetOpNode((CEPStringTokenNode)($3), RelSetOp.UNION, true);
    }

  | RW_EXCEPT identifier
    { 
      $$ = new CEPSetOpNode((CEPStringTokenNode)($2), RelSetOp.EXCEPT, false);
    }

  | RW_MINUS identifier
    {
      $$ = new CEPSetOpNode((CEPStringTokenNode)($2), RelSetOp.MINUS, false);
    }

  | RW_INTERSECT identifier
    {
      $$ = new CEPSetOpNode((CEPStringTokenNode)($2), RelSetOp.INTERSECT, false);
    }

   ;

datatype
   : variable_length_datatype
     {$$ = $1;}

   | fixed_length_datatype
     {$$ = $1;}
   ;

numberToken
   : RW_NUMBER
     {$$ = Datatype.BIGDECIMAL;}
   ;
   
variable_length_datatype
   : RW_CHAR
     {$$ = Datatype.CHAR;}

   | RW_BYTE
     {$$ = Datatype.BYTE;}
   ;     

fixed_length_datatype
   : RW_INTEGER
     {$$ = Datatype.INT;}
   
   | RW_BIGINT
     {$$ = Datatype.BIGINT;}

   | RW_FLOAT
     {$$ = Datatype.FLOAT;}
     
   | RW_DOUBLE
     {$$ = Datatype.DOUBLE;}
          
   | RW_TIMESTAMP
     {
       /** support for backward compatibility */
       Datatype timestampType = Datatype.TIMESTAMP;
       timestampType.setTimestampFormat(new TimestampFormat());
       $$ = timestampType;
     }
     
   | RW_TIMESTAMP timestamp_format
     {
       Datatype timestampTypeWithFmt = Datatype.TIMESTAMP;
       TimestampFormat format = (TimestampFormat)($2);
       timestampTypeWithFmt.setTimestampFormat(format);
       $$ = timestampTypeWithFmt;
     }

   | RW_BOOLEAN
     {$$ = Datatype.BOOLEAN;}

   | RW_XMLTYPE
     {$$ = Datatype.XMLTYPE;}
   
   | RW_OBJECT
     {$$ = Datatype.OBJECT;}

   | RW_NUMBER
     {$$ = Datatype.BIGDECIMAL;}

   | RW_INTERVAL
     {
       /** Support for Backward Compatibility */
       Datatype intervalType = Datatype.INTERVAL;
       intervalType.setIntervalFormat(
         new IntervalFormat(
           TimeUnit.DAY, 
           TimeUnit.SECOND, 
           Constants.DEFAULT_INTERVAL_LEADING_PRECISION,
           Constants.DEFAULT_INTERVAL_FRACTIONAL_SECONDS_PRECISION));

       $$ = intervalType;
     }

   | RW_INTERVAL interval_format
     {
       IntervalFormat format = (IntervalFormat)($2); 
       Datatype       dt     = null;

       if(format.isYearToMonthInterval())
         dt = Datatype.INTERVALYM;
       else
         dt = Datatype.INTERVAL;
       dt.setIntervalFormat(format);
       $$ = dt;
     }

   | extensible_qualified_datatype
     {
       $$ = CartridgeHelper.getType(execContext, (List) $1);       
     } 
   ;

identifier
   : T_STRING
     {$$ = new CEPStringTokenNode($1); ((CEPStringTokenNode)$$).setStartOffset(startOffset); ((CEPStringTokenNode)$$).setEndOffset(endOffset);}

   | T_UPPER_LETTER
     {$$ = new CEPStringTokenNode($1); ((CEPStringTokenNode)$$).setStartOffset(startOffset); ((CEPStringTokenNode)$$).setEndOffset(endOffset);}
   
   | unreserved_keyword
     {$$ = new CEPStringTokenNode($1); ((CEPStringTokenNode)$$).setStartOffset(startOffset); ((CEPStringTokenNode)$$).setEndOffset(endOffset);}
   ;
   
extensible_identifier
   : identifier
     {$$ = $1;}
   
   | reserved_keyword
     {$$ = new CEPStringTokenNode($1); ((CEPStringTokenNode)$$).setStartOffset(startOffset); ((CEPStringTokenNode)$$).setEndOffset(endOffset);} 
   ;  
   
extensible_non_datatype_identifier
   : identifier
     {$$ = $1;}
   
   | non_datatype_reserved_keyword
     {$$ = new CEPStringTokenNode($1); ((CEPStringTokenNode)$$).setStartOffset(startOffset); ((CEPStringTokenNode)$$).setEndOffset(endOffset);} 
   ;  
   
extensible_qualified_identifier
   : compound_extensible_qualified_identifier  
    {$$ = $1;}
    
   | T_CHARAT extensible_identifier
     {idList = new LinkedList();
      CEPStringTokenNode stringNode = new CEPStringTokenNode("char"); stringNode.setStartOffset(startOffset); stringNode.setEndOffset(endOffset); 
      idList.add(stringNode); ((CEPStringTokenNode)$2).setIsLink(true); idList.add($2); $$ = idList;
     }    
        
   | extensible_identifier
     {idList = new LinkedList(); idList.add($1); $$ = idList;}
   ;   
   
extensible_qualified_datatype
   : compound_extensible_qualified_identifier  
    {$$ = $1;}
        
   | T_CHARAT extensible_identifier
     {idList = new LinkedList();
      CEPStringTokenNode stringNode = new CEPStringTokenNode("char"); stringNode.setStartOffset(startOffset); stringNode.setEndOffset(endOffset); 
      idList.add(stringNode); ((CEPStringTokenNode)$2).setIsLink(true); idList.add($2); $$ = idList;
     }
     
   | extensible_non_datatype_identifier
     {idList = new LinkedList(); idList.add($1); $$ = idList;}
   ;      
   
/** A compound qualified identifier has at least two identifiers
    This is distinction allows us to avoid conflicts between CQL integer and Java integer.
*/
compound_extensible_qualified_identifier
   : extensible_identifier '.' compound_extensible_qualified_identifier
     {((LinkedList)($3)).addFirst($1); $$ = $3;}

   | extensible_identifier '.' extensible_identifier
     {idList = new LinkedList(); idList.add($1); idList.add($3); $$ = idList;}
     
   | extensible_identifier '@' extensible_identifier
     {idList = new LinkedList(); idList.add($1); ((CEPStringTokenNode)$3).setIsLink(true); idList.add($3); $$ = idList;}
   ;
   
intToken
   : T_INT
     {$$ = new CEPIntTokenNode($1); ((CEPIntTokenNode)$$).setStartOffset(startOffset); ((CEPIntTokenNode)$$).setEndOffset(endOffset);}
   ;
   
bigIntToken
   : T_BIGINT
     {$$ = new CEPBigIntTokenNode((Long)$1); ((CEPBigIntTokenNode)$$).setStartOffset(startOffset); ((CEPBigIntTokenNode)$$).setEndOffset(endOffset);}
   ;

stringToken
   : T_STRING
     {$$ = new CEPStringTokenNode($1); ((CEPStringTokenNode)$$).setStartOffset(startOffset); ((CEPStringTokenNode)$$).setEndOffset(endOffset);}
   ;

qstringToken
   : T_QSTRING
     {
     	$$ = new CEPStringTokenNode($1); 
     	((CEPStringTokenNode)$$).setStartOffset(startOffset); 
     	((CEPStringTokenNode)$$).setEndOffset(endOffset);
     	((CEPStringTokenNode)$$).setSingleQuote(false);
     }
   ;

sqstringToken
   : T_SQSTRING
     {
     	$$ = new CEPStringTokenNode($1); 
     	((CEPStringTokenNode)$$).setStartOffset(startOffset); 
     	((CEPStringTokenNode)$$).setEndOffset(endOffset);
     	((CEPStringTokenNode)$$).setSingleQuote(true);
     }
   ;

unreserved_keyword
   : URW_NAME
   | URW_SUPPORTS
   | URW_INCREMENTAL
   | URW_COMPUTATION
   | URW_USE
   | URW_INSTANCE
   | URW_SYSTEM
   | URW_TOTAL
   | URW_ORDERING
   | URW_THRESHOLD
   | URW_DEGREE
   | URW_PARALLELISM
   | URW_ARCHIVER
   | URW_ARCHIVED
   | URW_STARTTIME
   | URW_ENTITY
   | URW_IDENTIFIER
   | URW_XMLNAMESPACES
   | URW_UNIT
   | URW_PROPAGATE
   | URW_COLUMN
   | URW_REPLAY
   | URW_WORKER
   | URW_TRANSACTION
   | URW_DIMENSION
   | URW_TABLE
   | URW_WITHIN
   | URW_INCLUSIVE
   | URW_CURRENTHOUR
   | URW_CURRENTPERIOD
   | URW_WITH
   | URW_LOCAL
   | URW_ZONE
   | URW_EVALUATE
   | URW_EVERY
   | URW_COALESCE
   ; 

/**
 Add all reserved words that could be used as type/constructor/method/field name here. Set them as <sval>. 
*/
reserved_keyword
   : RW_INTEGER 
   | RW_FLOAT
   | RW_DOUBLE
   | RW_TIMESTAMP
   | RW_BYTE
   | RW_BOOLEAN
   | RW_OBJECT
   | RW_NUMBER
   | non_datatype_reserved_keyword
   ;     
   
/**
  This list includes those reserved keywords that can be used in an extensible identifier that
  only contains a single identifier. It must not be any tokens that collide with the native datatypes.
*/ 
non_datatype_reserved_keyword
   : RW_JAVA
   | RW_MATCHES
   | RW_EVENT
   | RW_PATTERN
   | RW_START
   | RW_END
   ;  

builtin_func
   : RW_XMLQUERY
     {$$ = (String)"XMLQUERY";}

   | RW_XMLEXISTS
     {$$ = (String)"XMLEXISTS";}

   ; 

extended_builtin_aggr
   : RW_FIRST
     {$$ = AggrFunction.FIRST.getFuncName();}

   | RW_LAST
     {$$ = AggrFunction.LAST.getFuncName();}
   ;
   
builtin_aggr
   : RW_MAX
     {$$ = AggrFunction.MAX.getFuncName();}
     
   | RW_MIN
     {$$ = AggrFunction.MIN.getFuncName();}
     
   | RW_XMLAGG
     {$$ = AggrFunction.XML_AGG.getFuncName();}
   ; 
       
builtin_aggr_incr
   : RW_SUM
     {$$ = AggrFunction.SUM.getFuncName();}
       
   | RW_AVG
     {$$ = AggrFunction.AVG.getFuncName();}
     
   | RW_COUNT
     {$$ = AggrFunction.COUNT.getFuncName();}
   ;
       
%%

  public CEPParseTreeNode parseTree;      
	
  private CEPViewDefnNode vdn;
  
  private CEPTableDefnNode rdn;

  private LinkedList relList;
  private LinkedList outerRightList;
  private LinkedList attrList;
  private LinkedList projList;
  private LinkedList attrSpecList;
  private LinkedList attrNameList;
  private LinkedList numList;
  private LinkedList paramSpecList;
  private LinkedList dropParamList;
  private LinkedList argList;
  private LinkedList xtblColList;
  private LinkedList xmlNamespaceList;
  private LinkedList parValList;
  private LinkedList corrNameDefList;
  private LinkedList arithExprListSet;   
  private LinkedList measureColList;
  private LinkedList orderList;
  private LinkedList subsetList;
  private LinkedList corrList;
  private LinkedList arithList;
  private LinkedList usingList;
  private LinkedList setopRelList;
  private LinkedList idList;
  private LinkedList srcList;
  private boolean    insideDefineOrMeasures = false;
  private boolean    defaultSubsetRequired = false;
  private boolean    insideXmlAttr = false;

  private Yylex         lexer;
  private String        lastInputRead;
  private StringBuffer parsedSoFar;   
  private boolean       parseError;
  private StringBuffer viewQryTxt;

  private String        inputCommand; 
  private int           startOffset;
  private int           endOffset;
  private int           lastStartOffset;
  private int           lastEndOffset;
  private String        errorSupplementMsg;
  
  private ExecContext   execContext;
 
  /**
   * updateViewQryTxt() takes care of 2 tasks:
   * 1) initialize viewQryTxt on its first call from a rule
   * 2) update viewQryTxt on its further calls
   */ 
  private void updateViewQryTxt() {
    if(viewQryTxt == null)
    {
      viewQryTxt = new StringBuffer();
    }
    else if(lastInputRead != null)
    {
      viewQryTxt.append(lastInputRead + " ");
    }
  }

  private int yylex () {
    int yyl_return = -1;

    if (lastInputRead != null)
    {
       parsedSoFar.append(lastInputRead + " ");
       lastStartOffset = startOffset;
       lastEndOffset = endOffset;
    }     
    /**
      viewQryTxt represents referenced query of view
      e.g. create view v(...) as select * from S; then
           viewQryTxt will be "select * from S"
    */ 
    if(viewQryTxt != null)
      updateViewQryTxt();

    try {
      yylval = new ParserVal(0);
      yyl_return = lexer.yylex();
      if (lexer.yytext() != null)
      {
        lastInputRead = lexer.yytext();
        endOffset = startOffset + lexer.yylength() -1;
      }
    }
    catch (IOException e) {
       LogUtil.info(LoggerType.TRACE, "IO error :"+e);
    }
    return yyl_return;
  }

  public Parser(Reader r) {
        lexer = new Yylex(r, this);
      //  yydebug = true;
  }

  public void yyerror (String error) {
    if (parseError)
      return;

    parseError = true;
    
    //build a detailed error message
    String errorToken = lexer.yytext();
    String[] expTokens = getExpectedTokens();  
    String parsedStr = parsedSoFar.toString();     
    StringBuilder b = new StringBuilder();
    b.append(" ");
    if (expTokens.length > 0)
    {
    //  b.append("Possible valid next tokens : ");
      for (int i=0; i<expTokens.length; i++) {
        if (i > 0) {
          b.append(", ");
        }
        b.append(expTokens[i]);
      }
    }
    errorSupplementMsg = b.toString();
    lastEndOffset += errorToken.length();
  }

  public String[] getExpectedTokens() {
    LinkedList<String> expTokenList = new LinkedList<String>();

    int s;

    for (int c=0; c<YYMAXTOKEN; c++) {
      s = yysindex[yystate];
      if (s != 0) {
        s += c;
        if (s >= 0 && s < YYTABLESIZE) {
          if (yycheck[s] == c)
          {
            String tokenName = LexerHelper.getTokenName(c);
            if (tokenName == null)
              tokenName = yyname[c];
            if(tokenName != null)
              expTokenList.add(tokenName);
          }
        }
      }
    }

    for (int c=0; c<YYMAXTOKEN; c++) {
      s = yyrindex[yystate];
      if (s != 0) {
        s += c;
        if (s >= 0 && s < YYTABLESIZE) {
          if (yycheck[s] == c)
          {
            String tokenName = LexerHelper.getTokenName(c);
            if (tokenName == null)
              tokenName = yyname[c];
            if(tokenName != null)
              expTokenList.add(tokenName);
          }

        }
      }
    }

    return expTokenList.toArray(new String[0]);    
  }


  public CEPParseTreeNode parseCommand(ExecContext ec, String command)
         throws CEPException {
    Reader r;
    this.execContext = ec;
    try {
        // add space since parser resets the startoffset to 0 for last token
        command = command + " ";
        //yydebug = true;
        parseError = false;
        inputCommand = command;
        lastStartOffset = 0;
        lastEndOffset = 0;
        startOffset = 0;
        endOffset = 0;
        parsedSoFar = new StringBuffer();
        r = new StringReader(command);
        lastInputRead = null;
        errorSupplementMsg = null; 
        lexer = new Yylex(r, this);
        yyparse();
    }
    catch(Exception e) {
      //bug 16896556 and 17076966 - reset boolean vars in case of error
      insideDefineOrMeasures = false;
      defaultSubsetRequired = false;
      insideXmlAttr = false;
      LogUtil.severe(LoggerType.TRACE, "Parser error "+e);
      LogUtil.logStackTrace(e);
      if(!(e instanceof CEPException))
        throw new CEPException(ParserError.PARSER_ERROR, e);
      else
        throw (CEPException)e;
    }
    
    if (parseError)
    {
      //bug 16896556 and 17076966 - reset boolean vars in case of error
      insideDefineOrMeasures = false;
      defaultSubsetRequired = false;
      insideXmlAttr = false;
      // TODO: Get specific identifier and print instead of errorSupplementMsg
      if(LexerHelper.verifyReservedWord(lastInputRead))
        throw new SyntaxException(SyntaxError.RESERVED_WORD_ERROR,
                                  lastStartOffset, lastEndOffset, 
                                  new Object[]{lastInputRead,errorSupplementMsg});
      throw new SyntaxException(SyntaxError.SYNTAX_ERROR, lastStartOffset,
                                lastEndOffset, errorSupplementMsg);
    }

    return parseTree;  
  }

  public void setStartOffset(int off)
  {
    startOffset = off;
  }

  public void setSpaceEndOffset(int off)
  {
    endOffset = off;
    lastEndOffset = endOffset;
  }

  static boolean interactive;

  public static void main(String args[]) throws IOException, CEPException {
    System.out.println("BYACC/Java with JFlex Stanford CQL");

    Parser yyparser;
    if ( args.length > 0 ) {
      // parse a file
      yyparser = new Parser(new FileReader(args[0]));
    }
    else {
      // interactive mode
      System.out.println("[Quit with CTRL-D]");
      System.out.print("Expression: ");
      interactive = true;
      yyparser = new Parser(new InputStreamReader(System.in));
    }

    try {
      yyparser.yyparse();
    }
    catch(Exception e) {
      if(!(e instanceof CEPException))
        throw new CEPException(ParserError.PARSER_ERROR, e);
      else
        throw (CEPException)e;
    }
    
    if (interactive) {
      System.out.println();
      System.out.println("Have a nice day");
    }
  }


