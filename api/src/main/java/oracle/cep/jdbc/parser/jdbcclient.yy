%{
import java.io.*;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.LinkedList;
import java.util.List;

import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;

import oracle.cep.parser.CEPBaseBooleanExprNode;
import oracle.cep.parser.CEPBigintConstExprNode;
import oracle.cep.parser.CEPBigDecimalConstExprNode;
import oracle.cep.parser.CEPBooleanExprNode;
import oracle.cep.parser.CEPConstExprNode;
import oracle.cep.parser.CEPExprNode;
import oracle.cep.parser.CEPFloatConstExprNode;
import oracle.cep.parser.CEPDoubleConstExprNode;
import oracle.cep.parser.CEPIntConstExprNode;
import oracle.cep.parser.CEPIntervalConstExprNode;
import oracle.cep.parser.CEPRelationNode;
import oracle.cep.parser.CEPSelectListNode;
import oracle.cep.parser.CEPStringConstExprNode;
import oracle.cep.parser.CEPNullConstExprNode;

import oracle.cep.jdbc.parser.*;

import oracle.cep.common.CompOp;

@SuppressWarnings({"unchecked"})
%}

%token RW_INSERT
%token RW_INTO
%token RW_VALUES
%token RW_INTERVAL
%token RW_HEARTBEAT
%token RW_AT
%token RW_DAY
%token RW_TO
%token RW_SECOND
%token RW_NULL
%token RW_SELECT
%token RW_FROM
%token RW_WHERE
%token RW_EXPLAIN
%token RW_PLAN
%token T_EQ
%token T_DQ

%token NOTOKEN

%token <ival> T_INT
%token <obj>  T_BIGINT
%token <dval> T_FLOAT
%token <dval> T_DOUBLE
%token <obj>  T_BIGDECIMAL
%token <sval> T_QUES
%token <sval> T_STRING
%token <sval> T_QSTRING

%type  <obj>  start
%type  <obj>  insert
%type  <obj>  non_mt_bind_var_list
%type  <obj>  non_mt_column_list
%type  <obj>  non_mt_const_list
%type  <obj>  ques
%type  <obj>  column
%type  <obj>  const_val
%type  <obj>  interval_value
%type  <sval> identifier
%type  <sval> quoted_identifier
%type  <obj>  timestampValue

%type  <obj>  sfw_block
%type  <obj>  select_clause
%type  <obj>  from_clause
%type  <obj>  non_mt_projterm_list
%type  <obj>  projterm
%%

start
   : insert
     {
     	parseTree = (CEPInsertNode)($1);
     }
    |sfw_block
     {
     	parseTree = (CEPSFWQueryNode)($1);
     }
    |RW_EXPLAIN RW_PLAN
     {
        parseTree = new CEPExplainPlanNode();
     }
   ;

insert
   : RW_INSERT RW_INTO identifier RW_VALUES '(' non_mt_bind_var_list ')'
     {
     	$$ = new CEPInsertNode($3, (Integer)($6));
     }

   
   | RW_INSERT RW_INTO identifier RW_VALUES '('non_mt_const_list ')'
     {
       	$$ = new CEPInsertNode($3, (LinkedList)($6));
     }
     
   | RW_INSERT RW_INTO identifier '(' non_mt_column_list ')' RW_VALUES '(' non_mt_bind_var_list ')'
     {
     	$$ = new CEPInsertNode($3, (Integer)($9), (LinkedList)($5));
     }
   
   | RW_INSERT RW_INTO quoted_identifier '(' non_mt_column_list ')' RW_VALUES '(' non_mt_bind_var_list ')'
     {
     	$$ = new CEPInsertNode($3, (Integer)($9), (LinkedList)($5));
     }
   
   | RW_INSERT RW_INTO identifier '(' non_mt_column_list ')' RW_VALUES '(' non_mt_const_list ')'
     {
       	$$ = new CEPInsertNode($3, (LinkedList)($9), (LinkedList)($5));
     }
     
   | RW_INSERT RW_INTO identifier RW_HEARTBEAT RW_AT timestampValue
   	 {
   		$$ = new CEPInsertNode($3,(CEPConstExprNode)($6));   		
   	 }
   	 
   | RW_INSERT RW_INTO identifier RW_HEARTBEAT RW_AT ques
   	 {
   		$$ = new CEPInsertNode($3);
   	 }
   ;

non_mt_bind_var_list
   : ques ',' non_mt_bind_var_list
     {
     	$$ = new Integer(((Integer)($3)).intValue()+1);
     }

   | ques
     {
     	$$ = new Integer(1);
     }
   ;

non_mt_column_list
   : column ',' non_mt_column_list
     {
       ((LinkedList)($3)).addFirst($1);
       $$ = $3;
     }
   
   | column
     {
       argList = new LinkedList();
       argList.add($1);
       $$ = argList ;
     }
   ;

non_mt_const_list
   : const_val ',' non_mt_const_list
     {
       ((LinkedList)($3)).addFirst($1);
       $$ = $3;
     }
     
   | const_val
     {
       constValList = new LinkedList<CEPConstExprNode>();
       constValList.add((CEPConstExprNode)$1);
       $$ = constValList;
     }
ques
   : T_QUES
     {
     	$$ = new Object();
     }
   ;

column
   : identifier 
     {
     	$$ = $1;
     }
     
   | quoted_identifier
     {
     	$$ = $1;	
     }
   ;

const_val
   : interval_value
     {
     	$$ = $1;
     }
     
   | T_INT
     {
     	$$ = new CEPIntConstExprNode($1);
     }
     
   | T_BIGINT
     {
     	$$ = new CEPBigintConstExprNode($1);
     }
   
   | T_FLOAT
     {
     	$$ = new CEPFloatConstExprNode($1);
     }
     
   | T_DOUBLE
     {
     	$$ = new CEPDoubleConstExprNode($1);
     } 
     
   | T_BIGDECIMAL
     {
     	$$ = new CEPBigDecimalConstExprNode((BigDecimal)$1);
     } 
    
   | RW_NULL
     {
     	$$ = new CEPNullConstExprNode(); 
     }
       
   | T_QSTRING
     {	
     	$$ = new CEPStringConstExprNode($1);
     }
     
   ; 

interval_value
   : RW_INTERVAL T_QSTRING RW_DAY RW_TO RW_SECOND
     {
     	$$ = new CEPIntervalConstExprNode($2);
     }
   ;

timestampValue
   : T_QSTRING
     {
     	$$ = new CEPStringConstExprNode($1);
     }  
   
   | T_INT
     {
     	$$ = new CEPIntConstExprNode($1);
     }
     
   | T_BIGINT
     {
     	$$ = new CEPBigintConstExprNode($1);
     } 
   ;
   
identifier
   : T_STRING
     {
     	$$ = $1;
     }
   ;
quoted_identifier
   : T_QSTRING
   	{
   		$$ = $1;
   	}
   ;
sfw_block
   :
   select_clause from_clause RW_WHERE T_INT T_EQ T_INT
    {
     	$$ = new CEPSFWQueryNode((List<String>)$1, (String)$2);
    }
   ;
   
select_clause
  :RW_SELECT non_mt_projterm_list
     {
     	$$ = (LinkedList<String>)($2);
     }
   ;
     
from_clause
  :  RW_FROM quoted_identifier
     {
     	$$ = $2;
     }
    ;
non_mt_projterm_list
   : projterm ',' non_mt_projterm_list
     {
     	((LinkedList)($3)).addFirst($1); 
     	$$ = $3;
     }

   | projterm
     {
     	projList = new LinkedList<CEPExprNode>(); 
     	projList.add($1); 
     	$$ = projList;
     }
   ;

projterm  
   : quoted_identifier
   	  {
   	  	$$ = $1;
   	  }
   ;

  	 

%%

  public CEPParseTreeNode parseTree;

  private Yylex                        lexer;
  private String                       lastInputRead;
  private StringBuilder                parsedSoFar;
  private boolean                      parseError;
  private LinkedList<CEPConstExprNode> constValList;
  private LinkedList                   argList;
  private LinkedList 				   projList;
  private LinkedList				   relList;
    
  private int yylex () {
    int yyl_return = -1;

    if (lastInputRead != null)
        parsedSoFar.append(lastInputRead + " ");

    try {
      yylval = new ParserVal(0);
      yyl_return = lexer.yylex();
      if (lexer.yytext() != null)
        lastInputRead = lexer.yytext();
    }
    catch (IOException e) {
       LogUtil.info(LoggerType.TRACE, "IO error :"+e);
    }
    return yyl_return;
  }

  public void yyerror (String error) {
    if (parseError)
      return;

    parseError = true;
    /**** Uncomment for error messages on console
    System.err.println ("Error: ");
    System.err.println (" Error occured at: " + parsedSoFar);
    System.err.println (" Error occured at Token: " + yyname[yychar]);
    System.err.println (" Error occured at Input Text: " + lexer.yytext());
    String[] expTokens = getExpectedTokens();
    System.err.println (" Possible Valid Next Tokens: ");
    for (int i=0; i<expTokens.length; i++) {
      System.err.println("   " + expTokens[i]);
    }*/
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
            expTokenList.add(yyname[c]);
        }
      }
    }

    for (int c=0; c<YYMAXTOKEN; c++) {
      s = yyrindex[yystate];
      if (s != 0) {
        s += c;
        if (s >= 0 && s < YYTABLESIZE) {
          if (yycheck[s] == c)
            expTokenList.add(yyname[c]);
        }
      }
    }

    return expTokenList.toArray(new String[0]);
  }

  public CEPParseTreeNode parseCommand(String command)
         throws Exception {
    Reader r;
    try {
//        yydebug = true;
        parseError = false;
        parsedSoFar = new StringBuilder();
        r = new StringReader(command);
        lexer = new Yylex(r, this);
        yyparse();
    }
    catch(Exception e) {
      throw e;
    }

    if (parseError)
      throw new Exception("JDBC Parser Error");

    return parseTree;
  }
  
  public CEPParseTreeNode getParseTree() {
    return parseTree;
  }
