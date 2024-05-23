//### This file created by BYACC 1.8(/Java extension  1.15)
//### Java capabilities added 7 Jan 97, Bob Jamison
//### Updated : 27 Nov 97  -- Bob Jamison, Joe Nieten
//###           01 Jan 98  -- Bob Jamison -- fixed generic semantic constructor
//###           01 Jun 99  -- Bob Jamison -- added Runnable support
//###           06 Aug 00  -- Bob Jamison -- made state variables class-global
//###           03 Jan 01  -- Bob Jamison -- improved flags, tracing
//###           16 May 01  -- Bob Jamison -- added custom stack sizing
//###           04 Mar 02  -- Yuval Oren  -- improved java performance, added options
//###           14 Mar 02  -- Tomas Hurka -- -d support, static initializer workaround
//### Please send bug reports to tom@hukatronic.cz
//### static char yysccsid[] = "@(#)yaccpar	1.8 (Berkeley) 01/20/90";



package oracle.cep.parser;



//#line 2 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
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

//#line 56 "Parser.java"




public class Parser
{

boolean yydebug;        //do I want debug output?
int yynerrs;            //number of errors so far
int yyerrflag;          //was there an error?
int yychar;             //the current working character

//########## MESSAGES ##########
//###############################################################
// method: debug
//###############################################################
void debug(String msg)
{
  if (yydebug)
    System.out.println(msg);
}

//########## STATE STACK ##########
final static int YYSTACKSIZE = 1024;  //maximum stack size
int statestk[] = new int[YYSTACKSIZE]; //state stack
int stateptr;
int stateptrmax;                     //highest index of stackptr
int statemax;                        //state when highest index reached
//###############################################################
// methods: state stack push,pop,drop,peek
//###############################################################
final void state_push(int state)
{
  try {
		stateptr++;
		statestk[stateptr]=state;
	 }
	 catch (ArrayIndexOutOfBoundsException e) {
     int oldsize = statestk.length;
     int newsize = oldsize * 2;
     int[] newstack = new int[newsize];
     System.arraycopy(statestk,0,newstack,0,oldsize);
     statestk = newstack;
     statestk[stateptr]=state;
  }
}
final int state_pop()
{
  return statestk[stateptr--];
}
final void state_drop(int cnt)
{
  stateptr -= cnt; 
}
final int state_peek(int relative)
{
  return statestk[stateptr-relative];
}
//###############################################################
// method: init_stacks : allocate and prepare stacks
//###############################################################
final boolean init_stacks()
{
  stateptr = -1;
  val_init();
  return true;
}
//###############################################################
// method: dump_stacks : show n levels of the stacks
//###############################################################
void dump_stacks(int count)
{
int i;
  System.out.println("=index==state====value=     s:"+stateptr+"  v:"+valptr);
  for (i=0;i<count;i++)
    System.out.println(" "+i+"    "+statestk[i]+"      "+valstk[i]);
  System.out.println("======================");
}


//########## SEMANTIC VALUES ##########
//public class ParserVal is defined in ParserVal.java


String   yytext;//user variable to return contextual strings
ParserVal yyval; //used to return semantic vals from action routines
ParserVal yylval;//the 'lval' (result) I got from yylex()
ParserVal valstk[];
int valptr;
//###############################################################
// methods: value stack push,pop,drop,peek.
//###############################################################
void val_init()
{
  valstk=new ParserVal[YYSTACKSIZE];
  yyval=new ParserVal();
  yylval=new ParserVal();
  valptr=-1;
}
void val_push(ParserVal val)
{
  if (valptr>=YYSTACKSIZE)
    return;
  valstk[++valptr]=val;
}
ParserVal val_pop()
{
  if (valptr<0)
    return new ParserVal();
  return valstk[valptr--];
}
void val_drop(int cnt)
{
int ptr;
  ptr=valptr-cnt;
  if (ptr<0)
    return;
  valptr = ptr;
}
ParserVal val_peek(int relative)
{
int ptr;
  ptr=valptr-relative;
  if (ptr<0)
    return new ParserVal();
  return valstk[ptr];
}
final ParserVal dup_yyval(ParserVal val)
{
  ParserVal dup = new ParserVal();
  dup.ival = val.ival;
  dup.dval = val.dval;
  dup.sval = val.sval;
  dup.obj = val.obj;
  return dup;
}
//#### end semantic value section ####
public final static short RW_REGISTER=257;
public final static short RW_STREAM=258;
public final static short RW_RELATION=259;
public final static short RW_SYNONYM=260;
public final static short RW_EXTERNAL=261;
public final static short RW_VIEW=262;
public final static short RW_FUNCTION=263;
public final static short RW_QUERY=264;
public final static short RW_ALTER=265;
public final static short RW_DROP=266;
public final static short RW_WINDOW=267;
public final static short RW_ISTREAM=268;
public final static short RW_DSTREAM=269;
public final static short RW_RSTREAM=270;
public final static short RW_SELECT=271;
public final static short RW_DISTINCT=272;
public final static short RW_FROM=273;
public final static short RW_WHERE=274;
public final static short RW_GROUP=275;
public final static short RW_BY=276;
public final static short RW_HAVING=277;
public final static short RW_AND=278;
public final static short RW_OR=279;
public final static short RW_XOR=280;
public final static short RW_NOT=281;
public final static short RW_AS=282;
public final static short RW_UNION=283;
public final static short RW_ALL=284;
public final static short RW_EXCEPT=285;
public final static short RW_MINUS=286;
public final static short RW_INTERSECT=287;
public final static short RW_START=288;
public final static short RW_STOP=289;
public final static short RW_ADD=290;
public final static short RW_DEST=291;
public final static short RW_SOURCE=292;
public final static short RW_PUSH=293;
public final static short RW_LIKE=294;
public final static short RW_SET=295;
public final static short RW_SILENT=296;
public final static short RW_TS=297;
public final static short RW_APP=298;
public final static short URW_SYSTEM=299;
public final static short RW_HEARTBEAT=300;
public final static short RW_TIMEOUT=301;
public final static short RW_REMOVE=302;
public final static short RW_RUN=303;
public final static short RW_RUNTIME=304;
public final static short RW_THREADED=305;
public final static short RW_SCHEDNAME=306;
public final static short RW_TIMESLICE=307;
public final static short RW_BETWEEN=308;
public final static short RW_NULLS=309;
public final static short RW_ORDER=310;
public final static short RW_ASC=311;
public final static short RW_DESC=312;
public final static short RW_DERIVED=313;
public final static short RW_FOR=314;
public final static short RW_LOGGING=315;
public final static short RW_DUMP=316;
public final static short RW_IDENTIFIED=317;
public final static short RW_LEVEL=318;
public final static short RW_TYPE=319;
public final static short RW_EVENT=320;
public final static short RW_CLEAR=321;
public final static short RW_LEFT=322;
public final static short RW_RIGHT=323;
public final static short RW_FULL=324;
public final static short RW_OUTER=325;
public final static short RW_JOIN=326;
public final static short RW_SYSTEMSTATE=327;
public final static short RW_OPERATOR=328;
public final static short RW_QUEUE=329;
public final static short RW_STORE=330;
public final static short RW_SYNOPSIS=331;
public final static short RW_INDEX=332;
public final static short RW_METADATA_QUERY=333;
public final static short RW_METADATA_TABLE=334;
public final static short RW_METADATA_WINDOW=335;
public final static short RW_METADATA_USERFUNC=336;
public final static short RW_METADATA_VIEW=337;
public final static short RW_METADATA_SYSTEM=338;
public final static short RW_METADATA_SYNONYM=339;
public final static short RW_STORAGE=340;
public final static short RW_SPILL=341;
public final static short RW_BINJOIN=342;
public final static short RW_BINSTREAMJOIN=343;
public final static short RW_GROUPAGGR=344;
public final static short RW_OUTPUT=345;
public final static short RW_PARTITIONWINDOW=346;
public final static short RW_PATTERNSTRM=347;
public final static short RW_PATTERNSTRMB=348;
public final static short RW_PROJECT=349;
public final static short RW_RANGEWINDOW=350;
public final static short RW_RELSOURCE=351;
public final static short RW_ROWWINDOW=352;
public final static short RW_SINK=353;
public final static short RW_STREAMSOURCE=354;
public final static short RW_VIEWRELNSRC=355;
public final static short RW_VIEWSTRMSRC=356;
public final static short RW_ORDERBY=357;
public final static short RW_ORDERBYTOP=358;
public final static short RW_DIFFERENCE=359;
public final static short RW_LINEAGE=360;
public final static short RW_PARTNWINDOW=361;
public final static short RW_REL=362;
public final static short RW_WIN=363;
public final static short RW_BIND=364;
public final static short RW_DURATION=365;
public final static short RW_ENABLE=366;
public final static short RW_DISABLE=367;
public final static short RW_MONITORING=368;
public final static short RW_IN=369;
public final static short RW_AVG=370;
public final static short RW_MIN=371;
public final static short RW_MAX=372;
public final static short RW_COUNT=373;
public final static short RW_SUM=374;
public final static short RW_FIRST=375;
public final static short RW_LAST=376;
public final static short RW_IS=377;
public final static short RW_NULL=378;
public final static short RW_ROWS=379;
public final static short RW_RANGE=380;
public final static short RW_NOW=381;
public final static short RW_PARTITION=382;
public final static short RW_UNBOUNDED=383;
public final static short RW_SLIDE=384;
public final static short RW_ON=385;
public final static short URW_UNIT=386;
public final static short RW_NANOSECOND=387;
public final static short RW_MICROSECOND=388;
public final static short RW_MILLISECOND=389;
public final static short RW_SECOND=390;
public final static short RW_MINUTE=391;
public final static short RW_HOUR=392;
public final static short RW_DAY=393;
public final static short RW_YEAR=394;
public final static short RW_MONTH=395;
public final static short RW_TO=396;
public final static short RW_RETURN=397;
public final static short RW_LANGUAGE=398;
public final static short RW_JAVA=399;
public final static short RW_IMPLEMENT=400;
public final static short RW_AGGREGATE=401;
public final static short RW_USING=402;
public final static short RW_MATCH_RECOGNIZE=403;
public final static short RW_PATTERN=404;
public final static short RW_SUBSET=405;
public final static short RW_DEFINE=406;
public final static short RW_MEASURES=407;
public final static short RW_MATCHES=408;
public final static short URW_WITHIN=409;
public final static short URW_INCLUSIVE=410;
public final static short RW_INCLUDE=411;
public final static short RW_TIMER=412;
public final static short RW_EVENTS=413;
public final static short RW_MULTIPLES=414;
public final static short RW_OF=415;
public final static short RW_PREV=416;
public final static short RW_XMLPARSE=417;
public final static short RW_XMLCONCAT=418;
public final static short RW_XMLCOMMENT=419;
public final static short RW_XMLCDATA=420;
public final static short RW_XMLQUERY=421;
public final static short RW_XMLEXISTS=422;
public final static short RW_XMLTABLE=423;
public final static short URW_XMLNAMESPACES=424;
public final static short RW_DEFAULT=425;
public final static short RW_XMLELEMENT=426;
public final static short RW_XMLATTRIBUTES=427;
public final static short RW_XMLFOREST=428;
public final static short RW_XMLCOLATTVAL=429;
public final static short RW_PASSING=430;
public final static short RW_VALUE=431;
public final static short RW_COLUMNS=432;
public final static short RW_XMLDATA=433;
public final static short RW_RETURNING=434;
public final static short RW_CONTENT=435;
public final static short RW_PATH=436;
public final static short RW_XMLAGG=437;
public final static short RW_WELLFORMED=438;
public final static short RW_DOCUMENT=439;
public final static short RW_EVALNAME=440;
public final static short URW_ORDERING=441;
public final static short URW_TOTAL=442;
public final static short URW_DEGREE=443;
public final static short URW_PARALLELISM=444;
public final static short RW_CASE=445;
public final static short RW_WHEN=446;
public final static short RW_THEN=447;
public final static short RW_ELSE=448;
public final static short RW_END=449;
public final static short RW_DECODE=450;
public final static short URW_THRESHOLD=451;
public final static short T_EQ=452;
public final static short T_LT=453;
public final static short T_LE=454;
public final static short T_GT=455;
public final static short T_GE=456;
public final static short T_NE=457;
public final static short T_JPLUS=458;
public final static short T_DOTSTAR=459;
public final static short T_CHARAT=460;
public final static short RW_INTEGER=461;
public final static short RW_BIGINT=462;
public final static short RW_FLOAT=463;
public final static short RW_DOUBLE=464;
public final static short RW_NUMBER=465;
public final static short RW_CHAR=466;
public final static short RW_BYTE=467;
public final static short RW_TIMESTAMP=468;
public final static short RW_INTERVAL=469;
public final static short RW_BOOLEAN=470;
public final static short RW_XMLTYPE=471;
public final static short RW_OBJECT=472;
public final static short RW_ELEMENT_TIME=473;
public final static short RW_QUERY_ID=474;
public final static short RW_TRUSTED=475;
public final static short RW_CALLOUT=476;
public final static short RW_CONSTRAINT=477;
public final static short RW_PRIMARY=478;
public final static short RW_KEY=479;
public final static short RW_UPDATE=480;
public final static short RW_SEMANTICS=481;
public final static short URW_ARCHIVED=482;
public final static short URW_ARCHIVER=483;
public final static short URW_ENTITY=484;
public final static short URW_STARTTIME=485;
public final static short URW_IDENTIFIER=486;
public final static short URW_WORKER=487;
public final static short URW_TRANSACTION=488;
public final static short URW_DIMENSION=489;
public final static short URW_COLUMN=490;
public final static short URW_REPLAY=491;
public final static short URW_PROPAGATE=492;
public final static short RW_TRUE=493;
public final static short RW_FALSE=494;
public final static short RW_BATCH=495;
public final static short URW_NAME=496;
public final static short URW_SUPPORTS=497;
public final static short URW_INCREMENTAL=498;
public final static short URW_COMPUTATION=499;
public final static short URW_USE=500;
public final static short URW_INSTANCE=501;
public final static short URW_TABLE=502;
public final static short URW_CURRENTHOUR=503;
public final static short URW_CURRENTPERIOD=504;
public final static short URW_WITH=505;
public final static short URW_LOCAL=506;
public final static short URW_ZONE=507;
public final static short URW_EVALUATE=508;
public final static short URW_EVERY=509;
public final static short URW_COALESCE=510;
public final static short NOTOKEN=511;
public final static short T_INT=512;
public final static short T_BIGINT=513;
public final static short T_DOUBLE=514;
public final static short T_FLOAT=515;
public final static short T_NUMBER=516;
public final static short T_STRING=517;
public final static short T_SQSTRING=518;
public final static short T_QSTRING=519;
public final static short T_UPPER_LETTER=520;
public final static short UNARYPREC=521;
public final static short YYERRCODE=256;
final static short yylhs[] = {                           -1,
    0,    1,    1,    1,    1,    1,    1,    1,    1,    1,
    1,    1,    1,    1,    1,    1,    1,    1,    1,    1,
    1,    1,    1,    1,    1,    1,    1,    1,    1,    1,
  192,  192,  193,  193,  193,    6,    6,    6,    6,    6,
    7,    7,    7,    7,  188,  188,  189,  189,  190,  190,
  191,  191,  195,  196,    8,    8,  173,  173,  173,    9,
  197,  198,  174,  175,   10,   10,   10,   12,   11,   11,
   11,   11,   11,   11,   11,   11,   11,   13,   13,   13,
   14,   14,   14,   14,   14,   14,   14,   14,   14,   14,
   14,   22,   22,   22,   23,   25,   15,   15,  194,  194,
  194,    4,   16,   17,   18,   19,   19,   20,   20,   20,
   20,   20,   21,    5,   30,   24,   24,   26,   26,   26,
   26,   26,   26,   27,   27,   28,   28,   28,   28,   29,
  122,  122,  122,  122,  123,  123,  123,  121,  121,  121,
  121,  114,  114,  114,  114,  114,  114,  114,   31,   32,
   32,   33,   35,   35,   34,   36,   36,   37,   37,   37,
   37,   37,   37,   37,   37,   37,   37,   39,   39,   38,
   38,   38,   38,  164,  163,    2,    2,    2,    2,    2,
    2,    2,    2,    2,   40,   40,   41,   42,   43,   43,
   43,   43,   44,  126,  126,  126,  127,  128,  128,  128,
   45,   45,   46,   46,   47,   47,  182,  182,   48,   48,
   49,   49,   49,  183,  183,  184,  185,  185,  129,  129,
  130,  130,  130,  130,  135,  135,  133,  133,  134,  134,
   54,   54,   55,   55,   57,   57,   59,   59,   58,   60,
   60,   60,   60,   60,   60,   56,   56,   56,   56,   56,
   56,   56,   56,   56,   56,   56,   56,   56,   56,   56,
   56,   69,   69,   68,   68,   67,   67,   67,   62,   62,
   62,  106,  106,   61,   61,   61,   61,   61,   61,   61,
   61,   61,   61,   61,   61,   61,   61,   61,   61,   61,
   61,   61,   61,   61,   61,   61,   61,   61,   63,   63,
   64,   64,   64,   65,   65,   65,   66,   66,   66,   66,
   66,   66,   66,   66,   66,   66,   75,   75,  161,  159,
  159,  160,  160,  160,  160,  160,  160,  156,  156,  158,
  158,   71,   70,   70,   73,   73,   74,   74,   74,   89,
   89,   90,   90,   91,   92,   93,   93,   72,   76,   76,
   76,   76,   76,   76,   77,   78,   78,   78,   78,   78,
   78,   80,   80,   79,  199,   81,   82,   82,   83,   84,
   84,  200,   85,   87,   87,   86,   88,   88,   88,   88,
   88,   88,   88,  125,   94,   94,   94,   94,   94,   94,
   94,   94,   94,   94,   94,   94,   94,   94,   94,   94,
   94,   94,   94,   94,   94,   94,   94,   94,   94,  124,
  124,   95,   95,   96,   97,   97,   97,   97,   97,   97,
   97,   97,   97,   97,   97,   97,   97,   97,   52,   52,
   52,   52,  146,  146,  146,  142,  142,  144,  144,  144,
  144,  144,  145,  145,  145,  143,   53,   53,  102,  102,
  102,  102,  102,  102,  102,  102,  102,  131,  132,   98,
   98,  103,  103,   99,  100,  100,  100,  100,  100,  100,
  100,  101,  101,  101,  101,  101,  104,  104,  104,  104,
  104,  104,  104,  104,  104,  104,  104,  104,  104,  162,
  162,  162,  162,  154,  154,  165,  165,  165,  165,  165,
  165,  165,  165,  165,  165,  165,  165,  201,  166,  167,
  167,  168,  168,  169,  169,  169,  202,  171,  203,  172,
  170,  170,  155,  155,  157,  105,  105,  115,  115,  115,
  115,  119,  119,  118,  116,  116,  117,  120,   50,   50,
   50,   50,   50,   50,   50,   50,   50,   50,   51,   51,
   51,   51,   51,  107,  107,  108,    3,    3,  109,  109,
  109,  109,  109,  109,  109,  110,  186,  186,  187,  187,
  187,  187,  187,  111,  111,  178,  112,  112,  113,  113,
  113,  113,  113,  113,  113,  113,  113,  113,  113,  113,
  113,  136,  136,  136,  139,  139,  140,  140,  137,  137,
  137,  138,  138,  138,  141,  141,  141,  176,  177,  179,
  180,  181,  147,  147,  147,  147,  147,  147,  147,  147,
  147,  147,  147,  147,  147,  147,  147,  147,  147,  147,
  147,  147,  147,  147,  147,  147,  147,  147,  147,  147,
  147,  147,  147,  147,  147,  147,  147,  147,  148,  148,
  148,  148,  148,  148,  148,  148,  148,  149,  149,  149,
  149,  149,  149,  150,  150,  153,  153,  151,  151,  151,
  152,  152,  152,
};
final static short yylen[] = {                            2,
    1,    1,    1,    1,    1,    1,    1,    1,    1,    1,
    1,    1,    1,    1,    1,    1,    1,    1,    1,    1,
    1,    1,    1,    1,    1,    1,    1,    1,    1,    1,
    3,    3,    0,    3,    3,    7,    8,    9,   10,   19,
    7,    7,    8,   16,    0,    1,    0,    3,    0,    3,
    0,    3,    0,    0,    5,    3,    6,    6,    3,    2,
    0,    0,    5,    7,    8,   10,    6,    6,   13,   10,
   10,   13,   13,   13,   13,   13,   13,    6,    9,    9,
   11,   12,   11,   11,   12,   12,   13,   11,   12,   12,
   13,    6,    6,    3,    3,    3,    5,    6,    0,    3,
    3,    1,    3,    3,    5,    5,    6,    3,    2,    2,
    5,    6,    2,    2,    3,    3,    3,    6,    6,    6,
    6,    7,    7,    9,    9,    7,    7,    6,    6,    9,
    4,    7,    7,    4,    7,    9,    5,    5,    8,    8,
    5,    5,    5,    5,    5,    3,    6,    6,    3,    3,
    3,    2,    3,    3,    1,    3,    1,    4,    3,    6,
    4,    5,    7,    6,    2,    3,    1,    3,    1,    2,
    5,    4,    7,    2,    5,    1,    5,    4,    1,    1,
    5,    5,    4,    4,    1,    1,    6,    2,    3,    2,
    3,    2,    2,    0,    1,    1,    3,    5,    6,    6,
    0,    2,    0,    3,    0,    2,    0,    5,    3,    1,
    2,    1,    3,    3,    1,    1,    1,    1,    3,    1,
    1,    2,    2,    3,    1,    1,    2,    2,    1,    1,
    3,    1,    3,    1,    2,    1,    2,    1,    4,    3,
    2,    3,    2,    3,    2,    4,    6,    6,    8,    1,
    3,    3,    5,    4,    8,    6,    4,    4,    6,    8,
   10,    5,    3,    3,    1,    1,    3,    5,    1,    3,
    4,    3,    1,    2,    4,    1,    2,    4,    2,    5,
    7,    9,    4,    4,    4,    4,    6,    6,    6,    6,
    3,    3,    8,    8,    5,    5,   10,   10,    1,    1,
    1,    1,    1,    2,    2,    2,    1,    1,    1,    1,
    1,    1,    1,    1,    1,    1,   12,   10,    4,    3,
    1,    3,    3,    3,    3,    2,    2,    3,    1,    4,
    7,    1,   13,   10,    2,    4,    0,    2,    3,    0,
    2,    2,    1,    5,    1,    3,    1,    4,    2,    1,
    3,    4,    3,    2,    1,    2,    2,    2,    1,    1,
    1,    0,    1,    3,    0,    3,    3,    1,    3,    0,
    2,    0,    3,    3,    1,    3,    3,    3,    3,    2,
    3,    1,    1,    5,    3,    4,    4,    3,    4,    4,
    3,    4,    4,    3,    4,    4,    3,    4,    4,    3,
    4,    4,    3,    3,    4,    5,    6,    9,   10,    5,
    3,    1,    1,    1,    1,    1,    1,    1,    1,    1,
    3,    3,    3,    3,    4,    2,    2,    3,    3,    3,
    1,    1,    1,    3,    1,    1,    1,    3,    5,    6,
    1,    1,    3,    4,    5,    4,    1,    1,    1,    1,
    1,    1,    1,    1,    1,    1,    1,    1,    1,    1,
    1,    1,    1,    3,    1,    4,    3,    6,    6,    9,
    6,    3,    6,    3,    7,    4,    6,    8,   10,    4,
   11,   11,    4,    1,    8,    8,    1,    1,    1,    6,
    5,    6,    5,    5,    4,    9,    9,    8,    7,    7,
    6,    5,    5,    4,    7,    7,    6,    0,    3,    3,
    2,    3,    1,    3,    4,    1,    0,    5,    0,    5,
    3,    1,    3,    1,    3,    3,    1,    3,    5,    4,
    6,    2,    1,    4,    2,    1,    4,    4,    4,    4,
    5,    4,    4,    4,    4,    6,    6,    1,    5,    5,
    5,    5,    5,    4,    3,    2,    1,    3,    3,    4,
    3,    3,    3,    3,    2,    2,    2,    1,    2,    3,
    2,    2,    2,    1,    1,    1,    1,    1,    1,    1,
    1,    1,    1,    2,    1,    1,    1,    1,    1,    2,
    1,    1,    1,    1,    1,    1,    1,    1,    1,    2,
    1,    1,    2,    1,    3,    3,    3,    1,    1,    1,
    1,    1,    1,    1,    1,    1,    1,    1,    1,    1,
    1,    1,    1,    1,    1,    1,    1,    1,    1,    1,
    1,    1,    1,    1,    1,    1,    1,    1,    1,    1,
    1,    1,    1,    1,    1,    1,    1,    1,    1,    1,
    1,    1,    1,    1,    1,    1,    1,    1,    1,    1,
    1,    1,    1,    1,    1,    1,    1,    1,    1,    1,
    1,    1,    1,
};
final static short yydefred[] = {                         0,
    0,    0,    0,  185,  186,    0,    0,  619,  631,  639,
  640,  630,  621,  620,  623,  624,  622,  626,  625,  628,
  627,  629,  635,  636,  637,  633,  634,  632,  613,  614,
  615,  616,  617,  618,  638,  641,  642,  643,  644,  645,
  646,  647,  648,  592,  593,    0,    0,    1,    0,    0,
    3,    4,    6,    5,    7,    8,   12,    9,   10,   11,
   13,   14,   15,   16,   17,   18,   19,   20,   21,   22,
   23,   24,   25,   26,    0,    0,  176,    0,    0,  179,
    0,  180,   27,   28,   29,   30,    0,  594,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,  113,    0,    0,  662,  660,    0,
    0,    0,    0,    0,    0,    0,  452,  658,  661,  659,
    0,    0,    0,    0,    0,    0,  517,  519,    0,    0,
  663,    0,    0,  649,  650,  651,  656,  653,  652,    0,
  654,  655,  447,  448,  460,  461,    0,  458,  609,  456,
  455,  457,  463,  462,    0,    0,    0,  192,  190,    0,
  417,  418,  435,    0,  412,  413,  449,  450,  414,  451,
  415,  419,  420,  453,  454,    0,    0,    0,  599,    0,
  442,    0,  441,  436,  596,  657,  548,  484,  487,  488,
  489,  459,    0,    0,    0,    0,    0,    0,    0,    0,
  188,    0,    0,  556,  566,    0,    0,    0,    0,    0,
    0,  565,    0,    0,    0,   60,   61,    0,    0,    0,
    0,    0,  672,  669,  668,  673,  671,  666,  667,    0,
  664,  665,  670,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,   46,    0,    0,    0,    0,    0,  114,
    0,    0,    0,    0,    0,    0,  103,  104,    0,    0,
    0,    0,    0,  117,  116,   96,  115,    0,   95,  557,
    0,    0,    0,  191,  189,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,  595,    0,  600,    0,    0,
    0,  426,  427,    0,    0,    0,    0,    0,    0,    0,
  211,    0,    0,    0,    0,    0,  558,  564,    0,  559,
  561,  563,  562,    0,    0,    0,    0,    0,    0,  193,
    0,    0,    0,    0,    0,    0,    0,  569,  571,  572,
  573,  555,  567,   56,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,  134,    0,
  178,  183,  184,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,  612,    0,    0,    0,    0,  611,    0,    0,
    0,    0,    0,    0,    0,  382,    0,  383,    0,    0,
    0,    0,  528,  535,    0,  316,  307,  308,  309,  310,
  311,  312,  313,  315,  314,    0,  464,    0,  428,  209,
  213,    0,    0,    0,  423,  424,    0,  443,    0,  434,
    0,  605,  607,  608,    0,    0,    0,    0,    0,    0,
    0,  100,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,  235,    0,    0,
    0,    0,    0,    0,  332,    0,    0,    0,    0,    0,
  554,  570,    0,   48,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,  580,    0,    0,    0,  577,    0,    0,
    0,    0,  586,    0,    0,  574,  575,    0,  591,    0,
  604,  602,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,  141,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,  142,  143,  144,  145,
    0,    0,  105,  137,    0,    0,    0,  155,    0,  543,
    0,  545,    0,  544,    0,  540,  539,    0,    0,  542,
    0,    0,    0,    0,    0,    0,  483,    0,    0,    0,
    0,  504,    0,    0,    0,    0,    0,    0,    0,  495,
    0,  380,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,  532,    0,  530,    0,  538,    0,    0,  480,    0,
    0,  444,    0,  446,    0,    0,  177,  181,  182,  306,
  304,  305,    0,    0,    0,    0,    0,    0,    0,  241,
    0,  243,    0,  245,  237,    0,  233,  252,    0,    0,
    0,    0,  276,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,   55,   63,    0,    0,    0,    0,
    0,  170,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,   68,    0,   57,   58,    0,    0,    0,
    0,  152,  603,    0,    0,  584,  590,    0,    0,    0,
    0,    0,    0,    0,   98,   78,    0,    0,    0,    0,
    0,    0,  121,  119,    0,    0,    0,  128,    0,  120,
  118,    0,    0,    0,  129,    0,    0,   67,  148,  147,
    0,    0,    0,  107,    0,    0,    0,   92,    0,   93,
  551,  553,  552,  549,  541,  550,    0,    0,    0,    0,
  491,    0,  493,  526,    0,    0,  503,    0,  502,    0,
  508,    0,    0,    0,    0,  518,    0,  520,    0,  494,
  381,    0,  377,    0,    0,    0,    0,    0,    0,    0,
    0,  404,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,  529,    0,    0,  445,
  439,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,  267,  240,  242,  244,    0,    0,  363,    0,    0,
    0,    0,    0,  279,    0,  299,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,  254,  257,  204,
    0,    0,    0,  187,  195,  196,    0,    0,   36,  156,
    0,    0,    0,    0,    0,    0,    0,   41,  174,    0,
  166,    0,  159,   42,  168,    0,    0,    0,  150,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,   64,    0,  123,  126,  300,    0,
    0,  122,  127,    0,    0,    0,    0,  110,    0,    0,
    0,  135,  133,  132,    0,  153,  546,    0,  547,    0,
  477,    0,  490,  492,    0,    0,    0,    0,    0,    0,
    0,    0,  507,    0,  501,    0,  514,  512,  225,  432,
  197,    0,  226,    0,    0,    0,    0,    0,    0,  405,
  399,  387,  390,  393,  396,  402,    0,    0,    0,    0,
    0,    0,    0,  531,    0,    0,    0,  440,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
  365,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,  270,    0,    0,    0,
    0,    0,    0,    0,    0,  172,    0,    0,    0,    0,
   43,    0,  161,    0,    0,  474,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,   37,    0,
    0,    0,  140,  139,    0,    0,    0,   65,    0,  108,
    0,    0,    0,    0,    0,    0,    0,    0,  506,    0,
  500,  505,    0,  499,    0,  509,  521,    0,    0,    0,
    0,  229,  230,  222,    0,    0,    0,    0,    0,  406,
    0,    0,    0,  217,  218,    0,    0,  216,    0,    0,
  268,    0,    0,    0,    0,  262,  256,  364,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,  278,  275,
    0,    0,    0,    0,    0,    0,    0,    0,    0,  248,
  259,    0,  271,    0,    0,    0,   35,   34,  171,    0,
  175,    0,    0,    0,    0,    0,  476,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,   80,   79,
    0,    0,    0,  124,  125,  130,    0,    0,    0,    0,
  485,  486,  478,    0,    0,    0,    0,    0,    0,    0,
  511,    0,  498,  219,  227,  228,  224,  430,  429,    0,
    0,  407,  471,    0,  469,  208,    0,    0,    0,    0,
    0,    0,  366,    0,    0,  371,    0,    0,    0,  326,
  327,  319,    0,    0,    0,    0,    0,    0,    0,    0,
    0,  231,    0,  296,  295,    0,  272,    0,    0,    0,
    0,  160,  164,    0,    0,    0,    0,   70,   71,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,  111,    0,    0,  525,    0,  523,    0,  497,  496,
  510,    0,    0,    0,  214,    0,  260,  255,  249,  264,
    0,    0,    0,    0,    0,    0,  320,  325,  324,  323,
  322,    0,    0,  290,  289,  288,  287,    0,    0,    0,
    0,  173,  163,    0,    0,  473,    0,    0,    0,    0,
    0,    0,    0,    0,   81,    0,   83,    0,    0,  112,
  479,    0,    0,    0,    0,    0,    0,    0,  367,  369,
  610,    0,    0,    0,  355,    0,    0,  338,    0,    0,
    0,    0,    0,    0,  200,  199,    0,    0,  475,    0,
    0,    0,    0,   90,    0,    0,    0,   86,    0,   82,
    0,    0,  481,  482,    0,    0,  408,  470,  261,    0,
    0,  348,  354,    0,    0,    0,  349,    0,  339,  341,
    0,    0,  345,  372,    0,    0,    0,    0,    0,    0,
    0,    0,   77,   76,   74,   75,   91,    0,   72,   73,
   87,   69,    0,    0,  409,    0,    0,    0,  357,  356,
  358,    0,    0,  342,    0,    0,  334,    0,    0,    0,
  318,    0,  282,    0,    0,  149,    0,    0,    0,    0,
  352,    0,  335,    0,    0,    0,    0,  373,    0,    0,
    0,  328,  298,  297,    0,    0,    0,    0,  410,    0,
    0,    0,    0,    0,    0,  317,    0,  330,    0,    0,
   50,    0,   44,  336,  333,    0,  344,    0,  374,    0,
    0,    0,   52,  346,    0,   32,   31,    0,  331,   40,
};
final static short yydgoto[] = {                         47,
   48,  280,   50,  562,  108,   51,   52,   53,   54,   55,
   56,   57,   58,   59,   60,   61,   62,   63,   64,  774,
   65,   66,   67,   68,   69,   70,   71,   72,   73,   74,
 1354,  529,  530,  596,  597,  520,  521,  517,  527,   75,
   76,   77,   78,  213,  345,  510,  704,  169,  170,  171,
  172,  969,  173, 1021,  340,  341,  342,  497,  498,  499,
  697,  698,  865,  866,  929,  456, 1115, 1116,  492,  505,
  506, 1219, 1413, 1286,  507, 1373, 1334, 1377,  858,  859,
 1012, 1213, 1214, 1121, 1385, 1437, 1438,  435, 1340, 1380,
 1381, 1382, 1453,  436,  483,  175,  176,  177,  178,  457,
  736,  179,  180,  181,  422, 1029,   79,   80,   81,   82,
  598,  546,  547,   83,  182,  304,  305,  440,  441,  183,
   84,   85,   86, 1326,  438,  884,  631,  886,  971,  972,
  184,  185, 1094, 1095,  974,  306,  187,  549,  188,  551,
  189,  190,  191,  192,  193,  194,   88,  195,  196,  245,
  246,  247,  248,  197, 1076, 1388, 1077, 1389, 1123, 1124,
  861,  198,  523,  722,  199,  803, 1086,  626,  627,  804,
  200,  201,   89,   90,   91,  870,  202,  716, 1335, 1125,
 1126,  667, 1106, 1107, 1108,  222,  223,  255,  226, 1429,
 1448, 1460,  889,  215,  355,  705,  357,  706, 1119, 1416,
  961,  299,  300,
};
final static short yysindex[] = {                      9568,
 1387, 1401, 1537,    0,    0,  100, 5192,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0, 9947,    0,    0,    0,  720,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,  148, -331,    0,   15, -331,    0,
 -331,    0,    0,    0,    0,    0, 1139,    0,   21,   63,
   36,13948,13948,13948,  177,13948,13082,13948,13948,  133,
  -27,13948,13948,  220,13948,13948,  583, 2009,13948,13948,
13948,13948,13948,13948,    0, 9947, 5923,    0,    0,  370,
  419,  453,  458,  474,  483,  513,    0,    0,    0,    0,
  559,  571,  581,  593,  632,  648,    0,    0,  660, 6162,
    0,  671,12931,    0,    0,    0,    0,    0,    0,  226,
    0,    0,    0,    0,    0,    0,  718,    0,    0,    0,
    0,    0,    0,    0, 8823, 8823, 8823,    0,    0,  708,
    0,    0,    0,   38,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,  307,  722,  265,    0,  680,
    0,  729,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,  738,  -20, 9821, 9947, 9947, 9947, 9947,  290,
    0,10309,  529,    0,    0,  442,12144,13948,13948,13948,
13948,    0,  834,  549,  336,    0,    0,  792,  804,  524,
13948,  806,    0,    0,    0,    0,    0,    0,    0,  824,
    0,    0,    0,   14,  832,  837,  845,  855,  572,   39,
13948,13948,13948,    0,  642,  899,  333,13948,   26,    0,
  436,  550,  470,  472,  476,  481,    0,    0,  644, -166,
  501,  576,  605,    0,    0,    0,    0,  947,    0,    0,
  960,  989,  997,    0,    0, 6411, 6650, 6899, 5442, 7138,
13948,13948,13948,  116, 8823,  522,  522,   -2, 1015, 1017,
 8823, 7380,  411,  193,  615,    0, 8823,    0, 1510, 8823,
 1391,    0,    0, 8823,13948,  951, 8823, 8823, 8823, 8823,
    0, 5681,12679,12931,  565,12931,    0,    0, 9947,    0,
    0,    0,    0, 1039, 1045, 1066, 9062, 1069,10074,    0,
  640, 1067,   32, 7380,  849,13948,13948,    0,    0,    0,
    0,    0,    0,    0, 9947,13948, 9947,13948,13559,  808,
 1092,13948,13948,12301,13948,13948,13948,13948,13948, 9947,
  732,13948, 1103, 1105, 1116,13948,  364, -198,  891,  803,
  839,  657, -176,  909,  919,  775,  778,  758,  788,  731,
  731,  437,  731,  733,  764,  735,  776,  756,    0,12301,
    0,    0,    0, 8823, 1718, 8823, 1816, 8823, 1911, 8823,
 1216, 1949,  800, 8823, 1997, 1214, 1221, 1222, 8823, 8823,
  739, 1229,    0,  833,  841, 8823,  733,    0,  158, 8823,
 8823,  857, 7380, 7380, -122,    0, 3678,    0, 8823,  836,
  564, 8823,    0,    0, 1237,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    8,    0, 1248,    0,    0,
    0, 8823,  156,  156,    0,    0, 8823,    0, 1249,    0,
  388,    0,    0,    0, 1198, 1257,  720,  940,  940,  940,
    0,    0, 1061, 1510,    0, 1510, 1510,12553, 9947, 1260,
 1261, 1263, 1196,  769,  789,  823,  640,    0,10309,10309,
13948, 1269, 1270,13214,    0, 1030, 1035,  744, 1046, 1047,
    0,    0,    0,    0,    0, 1285, 1283,12427,  858,13948,
 1293,   -7, 1295,12805,13559, 1301, 1303,12074, 1312,  268,
12301,12931,    0,    0,    0,    0,    0,    0,    0,  -16,
 1510,    0,    0,    0, 1074,    0,    0,    0,    0,  388,
    0,    0,    0, 1313,  294, 1319, 1322, 1329, 1331, 1317,
    0,  898,  733, 1337,  627,13948,13948,13948, 1341, -211,
 1101, 1096,  983, 1098,  900,    0, -192, 1110, 1102,  996,
 1108, 1169,  954,  962,  437,  731,    0,    0,    0,    0,
  151, -237,    0,    0, 1038,  669, 1399,    0, 2017,    0,
 2056,    0, 2072,    0, 2120,    0,    0, 1402, 2128,    0,
13948,13948,13948,  542,  746, 8823,    0, 1168, 1170, 1647,
  675,    0, 7619,  946,    0, 1404, 1406, 1411, 1177,    0,
 1419,    0,   18, 3063, 7380, 7380, 7380, 8823, 1099, 8823,
 8823, 1421, -107, 8823, 8823, 8823, 8823, 8823, 8823, 1320,
  257,    0, 8823,    0,  215,    0, 1510,  731,    0,  712,
 2181,    0,12931,    0, 7861, 1065,    0,    0,    0,    0,
    0,    0,   -1, 1434,    5,10379, 1077,13948, 1158,    0,
 1159,    0, 1161,    0,    0, 1120,    0,    0, 1127, -292,
  565, 8100,    0, 1235, 1129, 1458, 1423, 1425, 1472,13948,
13948, 8823, 7380, -171,    0,    0,  -70,13948,12931,    0,
 1481,    0, 1433,  388,    0, 1485, 1486, 1484, 1152, 1053,
13559, 1489,13948,    0, 1493,    0,    0,13948, 1138, 1141,
13948,    0,    0,  118,  731,    0,    0, 1144, 1146, 1147,
 1149, 1154, 1155, 1157,    0,    0, 1160, 1166, 1514, 1527,
 1532,13559,    0,    0,  733, 9062, 1132,    0, 1180,    0,
    0,  733, 9062, 1134,    0, 1202,   91,    0,    0,    0,
 1289, 1239, 1117,    0, 1302, 1304,  187,    0,12301,    0,
    0,    0,    0,    0,    0,    0,  698,  726,  782, 1562,
    0, 1564,    0,    0, 1185, 1190,    0, 7619,    0, 7619,
    0, 1324,  796, 1572, -243,    0, 8823,    0,13323,    0,
    0, 8823,    0,  406, 1345, 1061, 1585, 1061,  994, 8823,
 1251,    0,  105,  122,  140,  149,  237,  289, 8823, 8823,
 8823, 8823, 8823, 8823, 8823,  227,    0, 1597,  809,    0,
    0, 1600, 1602,13948, 1361,13948,  702, 9947, 1609, 1374,
 1376,    0,    0,    0,    0, 7380, 1385,    0, 1265, 1627,
 1624, 1240, 1291,    0, 1309,    0, 1292, 1510, 1294, 1510,
 1510,13450,13629,  733, 1403, 1414,  761,    0,    0,    0,
  744, 1393, 1388,    0,    0,    0, 1400,  854,    0,    0,
    0,  565, 1591,  565,13948,13559,  429,    0,    0,   13,
    0,13948,    0,    0,    0, 1655,12301,12301,    0, 1317,
 1206, 1252, 1664, 1330,12301,12301,12301,12301,12301,12301,
 1336, 1339,  -35, 1250,    0, 1709,    0,    0,    0, 1306,
  344,    0,    0, 1314, 1316, 1444, 1446,    0, 1713, 1287,
 1379,    0,    0,    0, 1742,    0,    0,  731,    0,  731,
    0,  731,    0,    0, 8823, 8823,  838, 1746,  866, 1748,
 1750, 8823,    0, 8823,    0, 8823,    0,    0,    0,    0,
    0, 1749,    0,  518, 1745, 1761, 8823, 8823, 1762,    0,
    0,    0,    0,    0,    0,    0, 1061, 1061, 1061, 1061,
 1061, 1061, 1061,    0,  731, 1409,  731,    0,13323,   -3,
13948, 1077, 1129, 1458, 1714, 1768,10379,13948,  744,13450,
    0, 1528, -271,  522, 1538,  565, 9062,13699,13808, 1782,
 1448, 1447, 1452, 1784,13948,13948,    0, 1788, 1792,13323,
 1561, 8823, 1544, 1546, 1803,    0, 1801, 1805, 1369, 1806,
    0, 1807,    0, 1567, 1570,    0, 1348, 1349,  159, -140,
 -116, 1574, 1459, 1463, 1465,  733,  733, 1571,    0,12805,
 1396, 1362,    0,    0, 1380,  735, 1503,    0, -118,    0,
 8823, 1853, 1865,  888, 1005, 1474, 1866, 1475,    0, 8823,
    0,    0, 8823,    0, 8342,    0,    0, 1881, 1061,13323,
  798,    0,    0,    0, 1614,13450, -154, 1883, 1061,    0,
 1900, 1510, 1901,    0,    0, 1902, 1903,    0,12301, 1662,
    0, 1663, 1666, 1361, 1906,    0,    0,    0, 8823, 1543,
   33,  659, 1914, 1913, 1677, 1683, 1539, 1540,    0,    0,
 1586, 1589, 1605, 1613,13450,  565, 9062, 9062,  733,    0,
    0,  871,    0, 1634,13323, 1061,    0,    0,    0,  565,
    0, 1962,13948,13948, 1595, 1612,    0,  132,  733,  733,
 1616, 1615, 1620, 1617, 1628, 1625, 1626, 1629,    0,    0,
 8823, 1545,12805,    0,    0,    0, 8823, 1722, 1563, 1061,
    0,    0,    0,  735,  733, 1606, 8823, 1611, 2006, 2007,
    0, 2010,    0,    0,    0,    0,    0,    0,    0, 1681,
 2012,    0,    0, 2014,    0,    0,13323, 2015,13948,13948,
13948,10379,    0, 2011, 1020,    0, 2021, 1651, 1656,    0,
    0,    0, -271,  659,  659, 1790, 8823, 9062, 9062,  735,
  735,    0, 1688,    0,    0, 2028,    0,  565, 1692, 2031,
13948,    0,    0, 1675, 1676, 1569, 1632,    0,    0, 1680,
 -333, 1682,  107, 1705,  733,  733,  733, 1061, 1590, 1596,
 1061,    0, 1601, 2064,    0, 2067,    0, 2071,    0,    0,
    0, 2076, 2080,  731,    0, 1840,    0,    0,    0,    0,
 8823,13948,  -33, 1710, 9304, 1719,    0,    0,    0,    0,
    0, 1695, 1696,    0,    0,    0,    0, 9062, 1751, 1127,
  565,    0,    0, 1636, 1642,    0, 1623,  174,  733, 1630,
  211,  733, 1630, 1644,    0, 1630,    0, 1678, 1641,    0,
    0, 1731, 1733, 2080, 8823, 2110, 2131,13948,    0,    0,
    0,  -33,   20,  465,    0, 1770, 9062,    0, 1659, 1771,
 8823,13948, 1794,13878,    0,    0,  733,  733,    0,  733,
  733, 1630, 1684,    0,  733,  733, 1630,    0,  733,    0,
 1691,   63,    0,    0, 2153, 2154,    0,    0,    0,   22,
  -33,    0,    0, 2149, 2150, 2151,    0, 1850,    0,    0,
 1659, 1764,    0,    0, 2176, 1797,12301, 2179, 2186, 9062,
 1852, 1864,    0,    0,    0,    0,    0, 1752,    0,    0,
    0,    0,13948, 1763,    0, 2209,  465,  -33,    0,    0,
    0, 8581, 1719,    0, 2216, 1659,    0,13948, 2217, 1822,
    0,13948,    0, 9062, 9062,    0, 1774, 1781, 1783, 2080,
    0, 1854,    0, 1771, 1659, 1990, 2229,    0, 2233,  565,
  522,    0,    0,    0, 1899,13948, 1791, 1904,    0, 9062,
 2237, 2235, 2245, 7380, 1659,    0, 2246,    0,  565, 1763,
    0,13948,    0,    0,    0, 1659,    0,  744,    0, 1856,
 2040, 1783,    0,    0,  522,    0,    0, 1904,    0,    0,
};
final static short yyrindex[] = {                         0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,  582,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0, 1203,    0,    0, 1203,    0,
 1203,    0,    0,    0,    0,    0,    0,    0,    0, 2288,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
 2030,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0, 2750,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0, 2023,
    0,    0,    0,   40,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0, 3128, 3377, 2959,    0, 3795,
    0, 3587,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,  883, 1172,    0,    0,    0,    0,    0,    0,
    0,    0, 2811,    0,    0,    0,    0,    0,    0,    0,
    0,    0,   87,10200,    0,    0,    0,    0,    0,    0,
    0, 2018,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0, 2290,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0, 2301,    0,    0,
 1305, 1315, 1347,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,  734,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0, 1305, 1315, 1347,    0,  913,    0,    0,
  815, 1300, 2779,    0,  556,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0, 2302,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0, 1921,    0,    0,    0,    0,    0,    0,    0,
 2262,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,  783,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0, 1701,    0,    0,    0,    0,
    0,    0, 4005, 4213,    0,    0,    0,    0,    0,    0,
 1429,    0,    0,    0,    0, 3169,   88,  195,  195,  195,
 2440,    0,    0, 1182, 1332,    0, 1848,    0,    0,  883,
    0,    0, 2263,    0,    0,    0, 1131,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0, 3227,    0, 1262,
    0,    0,  699,    0,  376,    0, 9440,    0,    0, 2265,
    0, 9440,    0,    0,    0,    0,    0, 2267,    0,    0,
    0,    0,10505,    0,10670,10809,  814,    0,  677,11478,
11590,10935,    0,11074,    0,    0,    0,11213,    0,    0,
    0,    0,11339,    0,    0,    0,    0,    0,    0,    0,
   58, 2310,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0, 2313,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
 2314,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0, 1738,    0, 2274,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0, 4423,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,  883, 3046,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0, 1909,    0,
    0,    0,    0,    0,  123, 2224,    0,    0, 2226,    0,
    0,    0,    0,  773,    0,    0, 2320,    0,    0, 9694,
    0,    0,    0,   34,11839,    0,    0,    0, 2320,    0,
    0, 2280,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0, 2008,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0, 4867, 4889,  328,    0,  189,    0,    0,
    0,    0, 1072, 1217, 1284, 3220, 3638, 4056,    0,    0,
    0,    0,    0,    0,    0,    0,    0, 1960,    0,    0,
    0,    0,    0,    0, 1449,    0,    0,    0, 2281,  928,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0, 2230,    0, 2231,    0,  -38, 1593, 1288,   43,
 1631,    0,    0,    0, 4943, 4998,    0,    0,    0,    0,
 1747,    0,    0,    0,    0,    0,    0,    0,    0,    0,
11948,    0,    0,    0,    0,    0,    0,    0,    0, 9440,
    0,    0,    0,    0,    0, 2267,    0,    0,    0, 2284,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0, 2320,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0, 2327,    0,
    0,    0,    0,    0, 2289,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,   67,    0,  137,  131,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0, 4474, 4572, 4639, 4693,
 4714, 4778,  491,    0,    0, 2219,    0,    0,    0,    0,
    0, 5030,    0,    0,    0,  883,    0,    0, 5104,    0,
    0,  223,    0,    0,    0,    0,    0,    0,    0, 1028,
    0, 2238, 2239,    0,    0,    0,    0, 2292,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0, 2293,
    0,    0,    0,    0,    0,    0,    0,11720,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,  303,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0, 1010,    0,
    0,    0,    0,    0,  200,    0,    0,    0, 4804,    0,
    0,    0,    0,    0,    0,    0, 2294,    0,    0,    0,
    0,    0, 5072,    0, 2295,    0,    0,    0,    0,    0,
    0,    0,    0, 2297,    0,    0,    0,    0,    0,    0,
 2247, 2248, 2264, 2266,    0,    0,    0,    0,    0,    0,
    0,    0,    0, 1179,    0, 2339,    0,    0,    0,    0,
    0, 2317,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0, 2367,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0, 2478,    0,    0,    0,    0,    0,    0,
    0,    0,    0, -162,    0,    0,    0,    0,  816,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0, 2275,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0, 2371,    0,    0,
 2372,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0, 1967,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0, 2077,
    0,    0,    0,    0,    0,    0,    0,    0,    0, 2374,
    0,    0, 2377,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,   25,    0,    0, 2185,    0,    0,    0,
    0,    0, 2285,    0,    0,    0,    0,    0,    0,    0,
    0, 2379,    0,    0,    0,    0, 2380,    0,    0,    0,
    0,   16,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,   29,   31,   35,    0,    0,    0,    0,
 1975,    0,    0,    0,    0,    0,    0,    0, 2341,    0,
 2296, 2298,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,   53,    0, 2342,   37,  139,    0,    0,
    0,    0, 1967,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,   28,    0,
    0,    0,    0,    0,    0,    0, 2344,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0, 2320,    0,    0,
    0, 2345,    0,    0,    0,    0,    0,    0,    0,   53,
    0,    0,    0,    0,    0,    0,    0, 1027,    0,    0,
    0,   28,    0,    0,    0,    0,    0, 2320,    0,    0,
};
final static short yygindex[] = {                         0,
    0,   45, 1113,    0,  138,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
 -907, -239,  649, 1608, 1621, -329, -457, -349, -639,    0,
    0,   19,    0,    0,    0,    0,    0,   -8,    0,    0,
    0, -345, -284, -867, 1888, 1893,    0,    0, 1896,    0,
 1547,    0, -707,    0, -337, -473, -298, 1183,    0,    0,
 -603, 1063,    0,    0,    0,-1082, -541,  998, -678,    0,
    0, 1140,    0,    0,  976,    0,  965, -313, 1009, 1042,
    0,    0,  958,    0,  330,    0, -232,    0,    0, 1884,
    0, -665, -319,    0, -290, 1297,    0,   74,  -28,   77,
 -226, -506, -503,    0,    0, 2132,    0,    0, 1996,    0,
    0,    0,    0,-1190,    0,    0, 1737,    0, -911,    0,
   -5, -332, 1358,    0,    0,    1, -488,    0,  -99,    0,
 -314, 1966,    0,    0,    0, -379,    0,    0, -358,    0,
    0,    0,    0,    0, -910, -878,    0,    0, 1232,    0,
    0,    0, -470, -784,    0,    7,    0, -427,    0,   -4,
    0,    0,    0,    0,    0, -302, -186,    0,-1048,  371,
 -294,  767, 1264,    0,    0, 2249,    0,    0, 1111, 1014,
 1003,    0, -700,  970,    0,    0,    0,    0,    0,    0,
    0,    0,    0,
};
final static int YYTABLESIZE=14468;
static short yytable1[],yytable2[];
static { yytable1();yytable2();}
static short yytable(int index) {
  if (index <=1 * 5000)
  {
    return yytable1[index];
  }
  return yytable2[index - 1 -5000 * (2 - 1)];
}
static void yytable1() {
yytable1 = new short[] {
                        482,
   87,  424,  425,  628,  485,  553, 1332,  186,  472,  522,
  670,  711,  671,  672,  712,   47,  445,  204,  898,  458,
  328,  711,  475,  735,  712,  883,  869,   51,  516,  713,
  508,  469,  526,  601,  486,  724,  721, 1110,  470,  713,
  491,  553,  301,  308,   49, 1078,   87,  658,  928,  552,
  625,  625,   49,  365,  300,  933,  708,  102,  811, 1332,
 1372, 1332, 1407,  593,  350,  350,  220,  725,  360,  360,
  359,  359,  589,  851,  361,  361,  351,  351,  372,  319,
  317,  753,  318,  212,  320,  552,  568,  560,  905,  325,
  203,  745,  228,  229,  230,  847,  232,  244,  249,  250,
  760,  572,  256,  257,  484,  259,  260,  220,  285,  274,
  275,  276,  277,  278,  279, 1042,   87,  186, 1144,  632,
  633,  368,  504,  579,  601,  554, 1200,  568,  560,  559,
  431,  860,  564, 1365,  281,  301,  221,  545,  882,  116,
  115, 1161, 1118, 1371,  775, 1371,  319,  317,  350,  318,
  487,  320,  360, 1122,  359,  635,  636,  637,  361,  553,
  351,  316,  641,  319,  317, 1163,  318, 1309,  320,  553,
  718,  431,  553,  821,  431,  522,  210,  221, 1194,  353,
  221,  319,  317,  838,  318,  428,  320,  209,  403,  282,
  319,  317,  283,  318,  207,  320,  966,  319,  622,  223,
 1333,  623,  320,  715,  776,   87,   87,   87,   87,   87,
  857, 1028,  343,  715, 1201,  641,  552,  348,  349,  350,
  351,  352, 1059,  471,  473,  423,  476,  334,  316,  403,
  252,  361,  403, 1239,  253,  207,  749,  750,  751, 1449,
  223,  368,  887,  223,  573,  316,  423,  428,  368, 1370,
  901,  373,  374,  375, 1152, 1038,  319,  317,  385,  318,
 1162,  320,  353,  316,  550,  769,  580, 1232,  319,  317,
  822,  318,  316,  320,  395,  428, 1267, 1058,  319,  317,
  844,  318,  335,  320, 1164,  336,  846,  212, 1408,  413,
 1383,  416,  417,  418,  926,  635,  636,  637,  319,  317,
  550,  318,  224,  320,  732,  460,  888,  428,  730, 1130,
  323,  731,  212,  501,  186,  461, 1293,  227,  396,  315,
  386,  813,  814,  815,  638,  794,  428,  387,  324,   87,
  319,  317, 1383,  318,  740,  320,  174,  731,  316,  493,
  557,  888,  557,  557,  557,  300,  511,  512,  472,  220,
  316,  220,  220,  220,  867,   87,  514,   87,  518,  518,
  316,  354,  528,  531,  548,  531,  531,  531,  531,  531,
   87,  900,  531, 1178,  842,   62,  569,  849,  890,  968,
  316, 1179,  225,  490,  587,  588,  220,  590,  863,  881,
  251,  890,   47,  903,  670,  862,  671,  672, 1112,  513,
  548,  515,  522,  657,   51, 1358,  568,  560, 1360,  286,
  364, 1109,  316,  431,  561,  431,  431,  431,  714,  221,
  553,  221,  221,  221,  714, 1040,  301,  625,  714,   49,
 1386,  550,  733,  663,  502,  231, 1217,  426,  371,  431,
  431,  431,  431, 1218, 1397,  220,  174,  301,  301, 1401,
  431,  324,  319,  317,  503,  318,  221,  320,  287,  868,
  204,  254,  403,  403,  552,  403,  403,  403,  403,  303,
  720,  403,  936,  403,  403,  403, 1028,  207,  258,  207,
  207,  207,  223, 1331,  223,  223,  223,  210,  734,   87,
  720,  909,  288,  427,  311,  312,  313,  289,  403,  343,
  343,  688,   47,   47,  699,  871, 1375, 1374,  403,  431,
  403,  403,  403,  290,  207,  221,  428,  601,  548,  223,
  518,  976,  291,  868,  970,  518, 1020, 1376,  548,  979,
  868,  548,  937,  674,  316,  102, 1331,  431, 1331, 1439,
   49,  350, 1009, 1442,  220,  360, 1039,  359,  553,  553,
  419,  361,  292,  351,  420,  203,  553,  553,  553,  553,
  553,  553,  981,  471,  568,  560,  518,  518,  518,  871,
  403, 1172, 1043,  943,  220,  944,  871, 1338,  223,  982,
  770,    2,  791,  319,  317,  911,  318,  970,  320, 1035,
 1343, 1037,  552,  552,  568,  560,  203,  983,  293, 1246,
  552,  552,  552,  552,  552,  552,  984, 1312,  431,  891,
  294,  787,  788,  789,  221,  405,  407,  409,  412,  415,
  295, 1345,  382,  912,  421,  428,  370,  383, 1204, 1379,
  432,  437,  296,  370,  384,  403,  421, 1247,  431,  421,
  442,  443,  771,  174,  221,  772,  463,  464,  465,  466,
  773,  421,  839, 1104, 1159,  570,  571, 1192,  557, 1160,
  557,  557,  557,  837, 1020,  316,  403,  748,  429, 1350,
  731,  297,  207,  437, 1351,  994,  850,  223,  852,  550,
 1044, 1045, 1423,  635, 1260,  637, 1098,  298, 1050, 1051,
 1052, 1053, 1054, 1055,  985,   62,  403,  880,   54,  301,
  878,  879,  207,  835, 1433,  625, 1355,  223,  518,  778,
  307, 1356,  779, 1129,  970,  799,  578,  578,  800, 1127,
  578,  518,  653,  518, 1041,  970, 1033, 1034,  906,  913,
 1063,  531, 1064,  599,  524,  601,  524,  603,  947,  605,
  653,  948, 1464,  609,  309,  970,  986, 1463,  614,  615,
  553,  314,  518,  319,  317,  620,  318,  310,  320,  624,
  624,  322,  437,  634,  591,  321,  949,  653,  651,  950,
  325,  655,  194,  537,  326,  537,  537, 1480,  327,  548,
  319,  317,  616,  318,  868,  320,  793,  319,  317, 1020,
  318,  660,  320,  958,  552,  960,  661,  621,  337, 1234,
 1235, 1027,  344,  973,  957,  970,  959,  550,  550,  975,
  346, 1198,  106,  194,  236,  550,  550,  550,  550,  550,
  550,  356,  951, 1242, 1243,  952, 1091, 1221, 1092, 1093,
  871,  358,  203, 1233,  486,  486,  963,  360,  203,  964,
  203,  203,  203,  359, 1000,  362, 1002, 1240,   87,  996,
  970, 1264,  997,  370,  588,  236,  439,  588,  236,  656,
  970, 1104,  316,  363,  557,  203,  557,  557,  557,  316,
  261,  366,  975, 1023, 1436,  203,  367,  656, 1079, 1176,
 1419, 1080, 1208, 1420,  368,  262,  263,  264,  265,  266,
 1294, 1295, 1006, 1452,  369,  906,  518,  630,  319,  317,
  376,  318,  518,  320,  484,  484, 1082,  548,  548, 1083,
  388, 1303,  638, 1436,  389,  548,  548,  548,  548,  548,
  548,  390,  970,  391, 1452,  486,  486,  392, 1183, 1289,
 1291, 1184,  393,  746,  394, 1300,  534,  203,  534,  534,
  754,  397, 1072,  398, 1073,  421, 1074,  761,  577,  578,
  487,  487,  802,  638,  163,  164,  638, 1087,  578, 1088,
  714,  494,  495,  496,  437,  437,  437,  816,  266,  818,
  819,  266,  399,  823,  824,  825,  826,  827,  828,  790,
  316,  557,  836,  557,  557,  557,  400,  319,  317, 1101,
  318, 1103,  320, 1105,  421,  484,  484, 1477, 1346,  975,
  401, 1111,  205,  638,  206,  207,  208,  850, 1117,  550,
  975,  653,  654,  555,  556,  557,  558,  560, 1132, 1134,
  565,  635,  636,  637,  973, 1140, 1141,  232,  553,  402,
  975,  802,  437,  203, 1366,  319,  317,  403,  318,  423,
  320,  487,  487, 1296, 1297,  211,  319,  317,  214,  318,
  515,  320,  868,  515,  430,  194,  431,  194,  194,  194,
  302,  319,  317,  203,  318,  868,  320,  376,  232,  316,
  376,  397,  552,  714,  462, 1189,  474,  578, 1190,  478,
  691,  692,  693,  694,  973,  479, 1443, 1444,  236,  236,
  975,  236,  194,  679,  680,  588, 1199,  236,  871,  236,
  236,  236,  319,  317,  868,  318,  480,  320,  488,  548,
  500,  871,  397,  681,  682,  397,  217,  316,  218,  219,
  220,  486,  486,  509,  236,  927,  524,  802,  316,  802,
  238,  525,  932,  563,  236,  975,  624, 1457,  127,  973,
 1468,  421,  566,  316,  567,  975, 1458,  683,  684,  421,
  871, 1033, 1034,  518,  518,  568, 1471,  868,  987,  988,
  989,  990,  991,  992,  993,  557,  629,  557,  557,  557,
  575,  238, 1195, 1196,  238,  967,  423,  428,  197,  868,
 1479,  536,  536,  792,  316,  437,  638,  638,  377,  638,
  574,  484,  484,  378,  638,  638,  236,  638,  638,  638,
  379, 1105,   99,  871, 1003, 1004,  576,  975,  581, 1277,
 1278, 1279,  850,  582,  588,  583,  385,  868,  584,  197,
  337,  337,  638,  413,  413,  871,  413,  805,  413,  150,
  533,  533,  638,  585,  638,  638,  638,  487,  487,  586,
  592,  518,  158,   99, 1024,  668,  669,  159,  127,  588,
  194,  428,  594,  155,  156,  595,  606,  385,  608,  611,
  385,  205,  618,  871,  380,  381,  612,  613, 1327,  617,
  619,  978,  158,  159,  160,  161,  162,  656,  163,  164,
  194,  439, 1330,  388, 1075, 1075, 1185,  550,  659,  662,
  664,  802,  236,  802,  638, 1089,  665,  638,  666,  234,
  675, 1282,  205,  677,  676,  413,  421, 1099,  689,  690,
  232,  700,  232,  232,  232,  638,  701,  330,  331,  332,
  333,  702,  236,  703,  388,  707,  708,  388, 1369,  414,
  414,  101,  414,  719,  414,  638,  717,  232,  723,  150,
  234,  726, 1387,  727, 1392,  397,  397,  232,  397,  397,
  397,  397,  729,  739,  397,  738,  397,  397,  397,  741,
  731, 1146,  742,  155,  156,  319,  317,  962,  318,  743,
  320,  744,  101,  454,  454,  519,  454,  747,  454,  910,
  752,  397,  158,  159,  160,  161,  162,  548,  163,  164,
  638,  397,  755,  397,  397,  397,  756,  757,  758,  759,
 1180,  762,  763, 1427,  238,  238,  232,  238,  765,  802,
  764,  414,  802,  238,  624,  238,  238,  238, 1387,  216,
  638,  217, 1387,  218,  219,  220, 1169, 1170,  606,  766,
  767,  459,  319,  317,  232,  318,  777,  320,  768,  780,
  238,  477,  785,  795,  806,  796, 1461,  316, 1215,  807,
  238,  808,  809,  397,   99,  454,   99,   99,   99,  810,
  820,  197, 1473,  197,  197,  197,  843,  817,  606,  606,
  606,  606,  606,  606,  845,  606,  216,  678,  217,  502,
  218,  219,  220,  853,  854,   99,  855,   99,   99,   99,
  385,  385, 1220,  385,  385,  385,  385,  874,  197,  385,
 1258,  385,  385,  385,  856,  232, 1261,  221,  857, 1236,
  872,  877,  238,  873,  316,  875, 1075,  876,  397,  606,
  892,  606,   99,  893,  894,  895,  385,  896,  897, 1248,
 1249,  899,  902,  904,  907,  232,  385,  908,  385,  385,
  385,  914,  915,  916,  205,  917,  205,  205,  205,  397,
  918,  919,  606,  920,  923, 1265, 1075,  388,  388,  921,
  388,  388,  388,  388,  221,  922,  388,  924,  388,  388,
  388,  205,  925,  234,  234,  930,  234,  934,  931,  397,
  935,  205,  234,  939,  234,  234,  234,  176,  938,  176,
  176,  176,  303,  388, 1288, 1290,  940,  179,  385,  179,
  179,  179,  953,  388,  954,  388,  388,  388,  238,  234,
 1215,  941,  965,  942,  101,  955,  101,  101,  101,  234,
  956, 1310,  635, 1313,  977, 1315, 1316, 1317,  980,  180,
  302,  180,  180,  180,  413,  413,  995,  413,  238,  413,
  998,  999, 1001,  205,   92,   93,   94,   95,   96,   97,
   98,  101, 1007,   99,  421,  678,  197, 1008,  102,  103,
 1010,  104,  105,  385,  106,  388, 1013, 1014, 1030, 1015,
 1075, 1011,  459,  459, 1016,  459, 1018,  459, 1019, 1352,
   99,  234, 1357, 1036, 1025,  303,  197,  797,  319,  317,
  798,  318, 1017,  320,  385, 1026, 1032, 1031,  728,  107,
  465,  606,  606,  606, 1048,  606,  606,  606,  606,  606,
  606,  606, 1046,  606,  606,  606,  413, 1393, 1394, 1047,
 1395, 1396,  606,  302,  385, 1399, 1400,  606, 1049, 1402,
  388,  558, 1060,  558,  558,  558,  606, 1056,  606,  205,
 1057,  465,  465,  465,  465,  465,  206,  465,  606, 1061,
  606,  606,  606, 1067,  459, 1068, 1069, 1062,  600,  319,
  317,  388,  318, 1071,  320, 1065, 1066, 1070,  100,  205,
  316,  829,  830,  831,  832,  833,  834,  234,  516,  436,
  436,  516,  436,  437,  436,  779, 1081,  206, 1084, 1085,
 1096,  388, 1090,  465,  109,  110,  111,  606,  112,  113,
  106, 1097, 1100,  114, 1102,  606, 1113,  234, 1114,  101,
  606, 1120,  606, 1128,  606,  606,  606,  606,  606,  606,
  606,  606,  606,  606,  465, 1135, 1136, 1139,  436,  606,
 1137, 1142, 1143,  606,  606, 1138, 1145,  606,  606,  101,
 1147,  316, 1148, 1149, 1150, 1151,  720,  459, 1155, 1153,
 1154, 1156,  606, 1158, 1157, 1165,  602,  319,  317, 1166,
  318,  436,  320, 1167,  606, 1168,  606, 1171,  101,  606,
  606,  606,  606, 1174,  606,  606,  606,  606, 1173,  606,
  606,  606,  606,  606,  606,  606,  606, 1177,  459,  459,
  459, 1175,  459, 1181,  459,  446,  447,  448,  449,  450,
  451,  452,  453,  454,  455, 1182,  606, 1186, 1188, 1187,
  606,  606,  606,  606,  606,  606,  606,  606,  606,  606,
  606, 1193, 1091, 1202,  606,  606,  606,  606,  606,  606,
  606,  606,  606,  606,  606,  606,  606,  606,  606,  316,
 1203, 1205, 1206, 1209, 1210,  606, 1207, 1211,  606, 1212,
 1216,  604,  319,  317, 1222,  318, 1223,  320, 1224,  467,
  595,  595,  595,  595, 1225,  595,  595,  595, 1226, 1228,
 1227,  459, 1229,  465,  465,  465,  303,  465,  465,  465,
  465,  465,  465,  465,  595,  465,  465,  465, 1230,  607,
  319,  317, 1244,  318,  465,  320, 1231,  303,  303,  465,
  467,  467,  467,  467,  467, 1241,  467,  522,  465, 1245,
  465,  595, 1238, 1250,  302,  459, 1251, 1252, 1253,  436,
  465, 1262,  465,  465,  465, 1254, 1255, 1256, 1259,  206,
 1257,  206,  206,  206,  316,  302,  302,  610,  319,  317,
 1266,  318, 1263,  320,  595, 1268, 1269, 1270,  522, 1272,
 1271, 1273,  467, 1274, 1281, 1276,  206,  781,  319,  317,
 1283,  318, 1284,  320, 1285, 1292,  206, 1298, 1299,  465,
 1301, 1302,  316, 1304, 1305, 1306,  198,  465, 1308, 1319,
 1311, 1320,  465,  467,  465,  465,  465,  465,  465,  465,
  465,  465,  465,  465,  465,  465,  782,  319,  317, 1307,
  318,  465,  320, 1314, 1321,  465,  465, 1322, 1318,  465,
  465, 1323,  783,  319,  317, 1324,  318,  198,  320, 1325,
  316, 1328, 1336, 1339,  465, 1341, 1353, 1342,  206, 1349,
  459, 1347,  459,  459,  459, 1344,  465, 1348,  465, 1359,
  316,  465,  465,  465,  465, 1361,  465,  465,  465,  465,
 1367,  465,  465,  465,  465,  465,  465,  465,  465, 1362,
  784,  319,  317, 1363,  318, 1364,  320,  459,  786,  319,
  317, 1368,  318, 1217,  320, 1331, 1384, 1390,  465,  316,
 1403, 1398,  465,  465,  465,  465,  465,  465,  465,  465,
  465,  465,  465, 1405, 1406,  316,  465,  465,  465,  465,
  465,  465,  465,  465,  465,  465,  465,  465,  465,  465,
  465, 1409, 1410, 1411, 1412, 1415, 1417,  465,  466, 1421,
  465,  840,  319,  317,  206,  318,  640,  320, 1418, 1422,
  640,  640,  467,  467,  467, 1424,  467,  467,  467,  467,
  467,  467,  467,  316,  467,  467,  467, 1425,  640, 1428,
 1426,  316, 1430,  467,  206, 1435, 1440, 1441,  467,  466,
  466,  466,  466,  466, 1445,  466, 1446,  467, 1450,  467,
 1447, 1454, 1455, 1456, 1459,  640, 1462, 1465, 1466,  467,
  888,  467,  467,  467,  522, 1467, 1470,   47,   45,  146,
  522, 1475,  522,  522,  522,  210,  267,  268,  269,   59,
   94,  131,  527,  270,  316,  167,  266,  169,  640,   97,
  271,  466,  138,  106,  513,  362,  642,  522,  269,   33,
  165,  263,  277,  274,  151,  459,  109,  522,  467,  154,
  292,  291,  273,  158,  215,  265,  467,  321,   38,  286,
  285,  467,  466,  467,  467,  467,  467,  467,  467,  467,
  467,  467,  467,  467,  467,  459,  283,  162,  284,  198,
  467,  198,  198,  198,  467,  467,  136,  280,  467,  467,
   39,   66,  340,   88,  272,  273,   84,  281,   89,   85,
  343,  329,  411,  467,  375,  347,  945,  687,  294,  522,
  293,  686,  685, 1005, 1280,  467,  198,  467, 1378,  946,
  467,  467,  467,  467, 1431,  467,  467,  467,  467, 1451,
  467,  467,  467,  467,  467,  467,  467,  467, 1476, 1469,
 1329, 1434, 1414, 1474,  737,  446,  447,  448,  449,  450,
  451,  452,  453,  454,  455,  652,  444,  467, 1237,  608,
  885,  467,  467,  467,  467,  467,  467,  467,  467,  467,
  467,  467, 1197,  673, 1287,  467,  467,  467,  467,  467,
  467,  467,  467,  467,  467,  467,  467,  467,  467,  467,
 1275,  353, 1404, 1472, 1478,    0,  467,  468,    0,  467,
    0,  458,  458,    0,  458,  522,  458,    0,    0,    0,
    0,  466,  466,  466,    0,  466,  466,  466,  466,  466,
  466,  466,    0,  466,  466,  466,    0,    0,    0,    0,
    0,    0,  466,    0,    0,  522,    0,  466,  468,  468,
  468,  468,  468,    0,  468,    0,  466,    0,  466,    0,
    0,    0,  608,    0,    0,    0,    0,    0,  466,    0,
  466,  466,  466,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,  198,    0,    0,    0,    0,    0,
    0,    0,    0,  458,    0,    0,    0,    0,    0,    0,
  468,  640,  640,  640,  640,  640,  640,  640,  640,  640,
    0,    0,    0,    0,  198,    0,    0,  466,    0,  640,
  640,    0,    0,    0,    0,  466,    0,    0,    0,    0,
  466,  468,  466,  466,  466,  466,  466,  466,  466,  466,
  466,  466,  466,  466,    0,    0,    0,    0,    0,  466,
    0,    0,    0,  466,  466,    0,    0,  466,  466,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,  466,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,  466,    0,  466,    0,    0,  466,
  466,  466,  466,    0,  466,  466,  466,  466,    0,  466,
  466,  466,  466,  466,  466,  466,  466,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,  466,    0,    0,    0,
  466,  466,  466,  466,  466,  466,  466,  466,  466,  466,
  466,    0,    0,    0,  466,  466,  466,  466,  466,  466,
  466,  466,  466,  466,  466,  466,  466,  466,  466,    0,
    0,    0,    0,    0,    0,  466,    0,    0,  466,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,  648,
  468,  468,  468,    0,  468,  468,  468,  468,  468,  468,
  468,    0,  468,  468,  468,    0,    0,    0,    0,    0,
    0,  468,    0,    0,    0,    0,  468,    0,  250,    0,
    0,    0,    0,    0,    0,  468,    0,  468,    0,    0,
  648,  648,  648,  648,  648,  648,  648,  468,    0,  468,
  468,  468,    0,    0,    0,    0,    0,    0,    0,    0,
  201,    0,    0,  648,    0,    0,    0,    0,    0,  250,
    0,    0,  250,  608,  458,  608,  608,  608,  608,  608,
  608,  608,  608,  608,  608,    0,    0,    0,    0,    0,
  648,    0,  648,    0,  608,  608,  468,    0,    0,    0,
    0,  201,    0,    0,  468,    0,    0,    0,    0,  468,
    0,  468,  468,  468,  468,  468,  468,  468,  468,  468,
  468,  468,  468,  648,    0,    0,    0,    0,  468,    0,
    0,    0,  468,  468,    0,    0,  468,  468,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,  468,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,  468,    0,  468,    0,    0,  468,  468,
  468,  468,    0,  468,  468,  468,  468,    0,  468,  468,
  468,  468,  468,  468,  468,  468,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,  468,    0,    0,  601,  468,
  468,  468,  468,  468,  468,  468,  468,  468,  468,  468,
    0,    0,    0,  468,  468,  468,  468,  468,  468,  468,
  468,  468,  468,  468,  468,  468,  468,  468,    0,    0,
    0,    0,    0,    0,  468,    0,    0,  468,  601,  601,
  601,  601,  601,  601,    0,  601,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,  648,  648,  648,    0,  648,  648,  648,  648,
  648,  648,  648,    0,  648,  648,  648,    0,    0,    0,
    0,    0,    0,  648,    0,  251,    0,    0,    0,  601,
    0,  601,  250,  250,    0,  250,    0,  648,    0,  648,
    0,  250,    0,  250,  250,  250,    0,    0,    0,  648,
    0,  648,  648,  648,    0,    0,    0,    0,    0,    0,
    0,    0,  601,    0,    0,  201,  251,  201,  250,  251,
    0,    0,    0,  201,    0,  201,  201,  201,  250,    0,
  250,  250,  250,  459,  319,  317,  812,  318,    0,  320,
    0,    0,    0,    0,    0,    0,    0,    0,  648,    0,
  201,    0,    0,    0,    0,    0,  648,    0,    0,    0,
  201,  648,    0,  648,    0,  648,  648,  648,  648,  648,
  648,  648,  648,  648,  648,    0,    0,    0,    0,    0,
    0,    0,    0,    0,  648,  648,    0,    0,    0,    0,
  250,    0,    0,  250,    0,    0,    0,  595,  438,  595,
  595,  595,  595,  595,  595,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,  316,  648,    0,    0,
    0,  595,  201,    0,    0,  648,  648,  648,  648,    0,
    0,  648,  648,  648,  648,  648,  648,  648,  648,  438,
  438,  438,  438,  438,  438,  438,    0,    0,  595,  391,
    0,    0,    0,    0,    0,    0,  202,  648,    0,    0,
    0,  601,  601,  601,    0,  601,  601,  601,  601,  601,
  601,  601,    0,  601,  601,  601,    0,    0,    0,    0,
    0,  595,  601,    0,    0,    0,  250,  648,    0,  438,
  391,  438,    0,  391,    0,    0,  601,  202,  601,    0,
    0,    0,    0,    0,    0,    0,    0,    0,  601,    0,
  601,  601,  601,    0,    0,    0,  250,    0,  201,    0,
    0,    0,  438,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,  201,  251,
  251,    0,  251,    0,    0,    0,    0,  601,  251,    0,
  251,  251,  251,    0,    0,  601,    0,    0,    0,    0,
  601,    0,  601,  639,  601,  601,  601,  601,  601,  601,
  601,  601,  601,  601,    0,  251,  640,    0,    0,    0,
    0,    0,    0,  601,  601,  251,    0,  251,  251,  251,
  641,    0,    0,    0,    0,    0,  433,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,  601,    0,    0,    0,
  595,    0,    0,    0,  601,  601,  601,  601,    0,  595,
  601,  601,  601,  601,  601,  601,  601,  433,  433,  433,
  433,  433,    0,  433,    0,    0,    0,  251,    0,    0,
  251,  642,    0,    0,    0,    0,  601,    0,    0,  643,
    0,  438,  438,  438,    0,  438,  438,  438,  438,  438,
  438,  438,    0,  438,  438,  438,    0,    0,    0,    0,
    0,    0,  438,    0,    0,    0,  601,  433,    0,  433,
    0,    0,    0,    0,    0,    0,  438,    0,  438,    0,
    0,    0,    0,    0,    0,    0,    0,    0,  438,    0,
  438,  438,  438,  391,  391,    0,  391,  391,  391,  391,
  433,  202,  391,  202,  391,  391,  391,    0,    0,  202,
    0,  202,  202,  202,  644,  645,  646,  647,  648,  649,
  650,    0,    0,  251,    0,    0,    0,    0,    0,  391,
    0,    0,    0,    0,    0,    0,  202,  438,    0,  391,
    0,  391,  391,  391,    0,  438,  202,    0,    0,    0,
  438,    0,  438,  251,  438,  438,  438,  438,  438,  438,
  438,  438,  438,  438,    0,    0,    0,    0,    0,    0,
    0,    0,    0,  438,  438,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,  437,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,  391,    0,    0,    0,    0,  438,    0,  202,    0,
    0,    0,    0,    0,  438,  438,  438,  438,    0,    0,
  438,  438,  438,  438,  438,  438,  438,  437,  437,  437,
  437,  437,    0,  437,    0,    0,    0,  394,    0,    0,
    0,    0,    0,    0,    0,    0,  438,    0,    0,  433,
  433,  433,    0,  433,  433,  433,  433,  433,  433,  433,
    0,  433,  433,  433,    0,    0,  391,    0,    0,    0,
  433,    0,    0,    0,    0,    0,  438,  437,  394,  437,
    0,  394,    0,    0,  433,    0,  433,    0,    0,    0,
    0,    0,    0,    0,    0,    0,  433,  391,  433,  433,
  433,    0,    0,    0,  202,    0,    0,    0,    0,    0,
  437,    0,    0,    0,    0,    0,    0,    0,    0,  319,
  317,    0,  318,    0,  320,    0,    0,  391,    0,    0,
    0,    0,    0,    0,  202,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,  433,    0,    0,    0,    0,
    0,    0,    0,  433,    0,    0,    0,    0,  433,    0,
  433,    0,  433,  433,  433,  433,  433,  433,  433,  433,
  433,  433,    0,    0,    0,    0,    0,    0,    0,    0,
    0,  433,  433,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,  416,    0,    0,    0,    0,    0,
    0,  316,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,  433,    0,    0,    0,    0,    0,
    0,    0,  433,  433,  433,  433,    0,    0,  433,  433,
  433,  433,  433,  433,  433,  416,  416,  416,  416,  416,
    0,  416,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,  433,    0,    0,    0,    0,  437,
  437,  437,    0,  437,  437,  437,  437,  437,  437,  437,
    0,  437,  437,  437,    0,    0,    0,    0,    0,    0,
  437,    0,    0,    0,  433,    0,    0,  416,    0,    0,
    0,    0,    0,    0,  437,    0,  437,    0,    0,    0,
    0,    0,    0,    0,    0,    0,  437,    0,  437,  437,
  437,  394,  394,    0,  394,  394,  394,  394,  416,    0,
  394,    0,  394,  394,  394,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,  394,    0,    0,
    0,    0,    0,    0,    0,  437,    0,  394,  639,  394,
  394,  394,    0,  437,    0,    0,    0,    0,  437,    0,
  437,  640,  437,  437,  437,  437,  437,  437,  437,  437,
  437,  437,    0,    0,    0,  641,    0,    0,    0,    0,
    0,  437,  437,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,  421,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,  394,
    0,    0,    0,    0,  437,    0,    0,    0,    0,    0,
    0,    0,  437,  437,  437,  437,    0,    0,  437,  437,
  437,  437,  437,  437,  437,  421,  642,  421,  421,  421,
    0,    0,    0,    0,  643,  400,    0,    0,    0,    0,
    0,    0,    0,    0,  437,    0,    0,  416,  416,  416,
    0,  416,  416,  416,  416,  416,  416,  416,    0,  416,
  416,  416,    0,    0,  394,    0,    0,    0,  416,    0,
    0,    0,    0,    0,  437,    0,  400,  421,    0,  400,
    0,    0,  416,    0,  416,    0,    0,    0,    0,    0,
    0,    0,    0,    0,  416,  394,  416,  416,  416,    0,
    0,    0,    0,    0,    0,    0,    0,    0,  421,  644,
  645,  646,  647,  648,  649,  650,    0,    0,    0,    0,
    0,    0,    0,    0,    0,  394,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,  416,    0,    0,    0,    0,    0,    0,
    0,  416,    0,    0,    0,    0,  416,    0,  416,    0,
  416,  416,  416,  416,  416,  416,  416,  416,  416,  416,
    0,    0,    0,    0,    0,    0,    0,    0,    0,  416,
  416,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,  422,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,  416,    0,    0,    0,    0,    0,    0,    0,
  416,  416,  416,  416,    0,    0,  416,  416,  416,  416,
  416,  416,  416,  422,    0,  422,  422,  422,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,  416,    0,    0,    0,    0,  421,  421,  421,
    0,  421,  421,  421,  421,  421,  421,  421,    0,  421,
  421,  421,    0,    0,    0,    0,    0,    0,  421,    0,
    0,    0,  416,    0,    0,  422,    0,    0,    0,    0,
    0,    0,  421,    0,  421,    0,    0,    0,    0,    0,
    0,    0,    0,    0,  421,    0,  421,  421,  421,  400,
  400,    0,  400,  400,  400,  400,  422,    0,  400,    0,
  400,  400,  400,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,  400,    0,    0,    0,    0,
    0,    0,    0,  421,    0,  400,    0,  400,  400,  400,
    0,  421,    0,    0,    0,    0,  421,    0,  421,    0,
  421,  421,  421,  421,  421,  421,  421,  421,  421,  421,
    0,    0,    0,    0,    0,    0,    0,    0,    0,  421,
  421,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,  425,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,  400,    0,    0,
    0,    0,  421,    0,    0,    0,    0,    0,    0,    0,
  421,  421,  421,  421,    0,    0,  421,  421,  421,  421,
  421,  421,  421,  425,    0,    0,  425,    0,    0,    0,
    0,    0,    0,  398,    0,    0,    0,    0,    0,    0,
    0,    0,  421,    0,    0,  422,  422,  422,    0,  422,
  422,  422,  422,  422,  422,  422,    0,  422,  422,  422,
    0,    0,  400,    0,    0,    0,  422,    0,    0,    0,
    0,    0,  421,    0,  398,  425,    0,  398,    0,    0,
  422,    0,  422,    0,    0,    0,    0,    0,    0,    0,
    0,    0,  422,  400,  422,  422,  422,    0,    0,    0,
    0,    0,    0,    0,    0,    0,  425,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,  400,    0,    0,    0,    0,    0,    0,
    0,  386,    0,    0,    0,    0,    0,    0,    0,    0,
    0,  422,    0,    0,    0,    0,    0,    0,    0,  422,
    0,    0,    0,    0,  422,    0,  422,    0,  422,  422,
  422,  422,  422,  422,  422,  422,  422,  422,    0,    0,
    0,    0,  386,    0,    0,  386,    0,  422,  422,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,  389,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
  422,    0,    0,    0,    0,    0,    0,    0,  422,  422,
  422,  422,    0,    0,  422,  422,  422,  422,  422,  422,
  422,    0,    0,    0,    0,    0,    0,    0,    0,  389,
    0,    0,  389,    0,    0,    0,    0,    0,    0,    0,
  422,    0,  392,    0,    0,  425,  425,  425,    0,  425,
  425,  425,  425,  425,  425,  425,    0,  425,  425,  425,
    0,    0,    0,  395,    0,    0,  425,    0,    0,    0,
  422,    0,    0,    0,    0,    0,    0,    0,    0,    0,
  425,    0,  425,  392,    0,    0,  392,    0,    0,    0,
    0,    0,  425,    0,  425,  425,  425,  398,  398,    0,
  398,  398,  398,  398,  395,    0,  398,  395,  398,  398,
  398,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,  401,    0,    0,
    0,    0,    0,  398,    0,    0,    0,    0,    0,    0,
    0,  425,    0,  398,    0,  398,  398,  398,    0,  425,
    0,    0,    0,  384,  425,    0,  425,    0,  425,  425,
  425,  425,  425,  425,  425,  425,  425,  425,  401,    0,
    0,  401,    0,    0,    0,    0,    0,  425,  425,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,  384,  386,  386,  384,  386,  386,
  386,  386,    0,    0,  386,  398,  386,  386,  386,    0,
  425,    0,    0,    0,    0,    0,  378,    0,  425,  425,
  425,  425,    0,    0,  425,  425,  425,  425,  425,  425,
  425,  386,    0,    0,    0,    0,    0,    0,  379,    0,
    0,  386,    0,  386,  386,  386,    0,    0,    0,    0,
  425,    0,    0,    0,    0,    0,    0,  378,    0,    0,
  378,    0,  389,  389,    0,  389,  389,  389,  389,    0,
  398,  389,    0,  389,  389,  389,    0,    0,    0,  379,
  425,    0,  379,    0,    0,    0,    0,    0,    0,    0,
    0,    0,  246,    0,    0,    0,    0,    0,  389,    0,
    0,  398,    0,  386,    0,    0,    0,    0,  389,    0,
  389,  389,  389,    0,    0,    0,  392,  392,    0,  392,
  392,  392,  392,    0,    0,  392,    0,  392,  392,  392,
    0,  398,    0,  246,    0,    0,  246,  395,  395,    0,
  395,  395,  395,  395,    0,    0,  395,  258,  395,  395,
};
}
static void yytable2() {
yytable2 = new short[] {
  395,    0,  392,    0,    0,    0,    0,    0,    0,    0,
    0,    0,  392,    0,  392,  392,  392,    0,  386,    0,
  389,    0,    0,  395,    0,    0,    0,    0,    0,  253,
    0,    0,    0,  395,    0,  395,  395,  395,  258,    0,
    0,  258,    0,    0,    0,    0,    0,    0,    0,  386,
    0,  401,  401,    0,  401,  401,  401,  401,    0,    0,
  401,    0,  401,  401,  401,    0,    0,    0,    0,    0,
  253,  247,    0,  253,  392,    0,    0,  384,  384,  386,
  384,  384,  384,  384,    0,  389,  384,  401,  384,  384,
  384,    0,    0,    0,    0,  395,    0,  401,    0,  401,
  401,  401,    0,  239,    0,    0,    0,    0,    0,    0,
    0,    0,  247,  384,    0,  247,  389,    0,    0,    0,
    0,    0,    0,  384,    0,  384,  384,  384,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,  392,
  378,  378,    0,  378,  239,  378,  389,  239,    0,  378,
    0,  378,  378,  378,    0,    0,    0,    0,    0,  401,
  395,    0,  379,  379,    0,  379,    0,  379,  379,    0,
  392,  379,    0,  379,  379,  379,  378,    0,    0,    0,
    0,    0,    0,    0,    0,  384,  378,    0,  378,  378,
  378,  395,    0,    0,    0,    0,    0,    0,  379,    0,
  392,    0,    0,    0,    0,    0,    0,    0,  379,    0,
  379,  379,  379,    0,    0,    0,  246,  246,    0,  246,
    0,  395,    0,    0,  401,  246,    0,  246,  246,  246,
    0,  165,    0,  168,  166,    0,  167,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,  378,    0,
  384,    0,  246,    0,    0,  401,    0,    0,    0,    0,
    0,    0,  246,    0,  246,  246,  246,    0,    0,    0,
  379,  258,  258,    0,  258,    0,    0,    0,    0,    0,
  258,  384,  258,  258,  258,  401,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,  253,  253,    0,  253,  258,    0,    0,
    0,  384,  253,  378,  253,  253,  253,  258,    0,  258,
  258,  258,    0,    0,  246,    0,    0,  246,    0,    0,
    0,    0,    0,    0,    0,  379,    0,    0,    0,  253,
    0,    0,    0,    0,  378,  247,  247,    0,  247,  253,
    0,  253,  253,  253,  247,    0,  247,  247,  247,    0,
    0,    0,    0,    0,    0,    0,  379,    0,    0,    0,
    0,    0,    0,    0,  378,    0,    0,  239,  239,  258,
  239,  247,  258,    0,    0,    0,  239,    0,  239,  239,
  239,  247,    0,  247,  247,  247,  379,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,  253,    0,  239,  253,    0,    0,    0,    0,    0,
  246,    0,    0,  239,    0,  239,  239,  239,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
  246,    0,    0,  247,    0,    0,  247,    0,    0,    0,
    0,    0,    0,  117,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,  258,    0,    0,    0,  118,
    0,  165,    0,  411,  166,  239,  167,    0,    0,    0,
    8,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,  258,    0,  253,    0,    0,
    0,  119,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,  253,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,  247,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,  120,  121,  122,  123,  124,  125,  126,    0,  127,
    0,    0,    0,    0,    0,    0,    0,    9,    0,  247,
    0,  239,    0,    0,    0,    0,    0,    0,    0,    0,
  128,    0,    0,    0,    0,  129,    0,    0,    0,  130,
   10,   11,    0,    0,    0,    0,    0,  131,  132,  133,
    0,  239,  134,  135,    0,   12,    0,  136,    0,  137,
  138,    0,    0,    0,    0,    0,    0,    0,  139,    0,
    0,    0,   13,   14,   15,   16,  140,    0,    0,    0,
  141,  142,   17,    0,    0,    0,    0,    0,    0,    0,
    0,  143,  144,    0,  145,  146,  147,    0,  148,  149,
  150,  151,    0,  152,  153,  154,    0,    0,    0,    0,
    0,    0,    0,   18,   19,   20,   21,   22,   23,   24,
   25,   26,   27,   28,  155,  156,    0,   29,   30,   31,
   32,   33,   34,   35,   36,   37,   38,   39,   40,   41,
   42,  157,    0,  158,  159,  160,  161,  162,   44,  163,
  164,   45,    0,  410,    0,    0,    0,    0,    0,    0,
  165,  468,    0,  166,    0,  167,    0,    0,    0,  118,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    8,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,  119,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,  120,  121,  122,  123,  124,  125,  126,    0,  127,
    0,    0,    0,    0,    0,    0,    0,    9,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
  128,    0,    0,    0,    0,  129,    0,    0,    0,  130,
   10,   11,    0,    0,    0,    0,    0,  131,  132,  133,
    0,    0,  134,  135,    0,   12,    0,  136,    0,  137,
  138,    0,    0,    0,    0,    0,    0,    0,  139,    0,
    0,    0,   13,   14,   15,   16,  140,    0,    0,    0,
  141,  142,   17,    0,    0,    0,    0,    0,    0,    0,
    0,  143,  144,    0,  145,  146,  147,    0,  148,  149,
  150,  151,    0,  152,  153,  154,    0,    0,    0,    0,
    0,    0,    0,   18,   19,   20,   21,   22,   23,   24,
   25,   26,   27,   28,  155,  156,    0,   29,   30,   31,
   32,   33,   34,   35,   36,   37,   38,   39,   40,   41,
   42,  157,  467,  158,  159,  160,  161,  162,   44,  163,
  164,   45,  165,    0,  284,  166,    0,  167,  118,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    8,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
  119,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
  120,  121,  122,  123,  124,  125,  126,    0,  127,    0,
    0,    0,    0,    0,    0,    0,    9,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,  128,
    0,    0,    0,    0,  129,    0,    0,    0,  130,   10,
   11,    0,    0,    0,    0,    0,  131,  132,  133,    0,
    0,  134,  135,    0,   12,    0,  136,    0,  137,  138,
    0,    0,    0,    0,    0,    0,    0,  139,    0,    0,
    0,   13,   14,   15,   16,  140,    0,    0,    0,  141,
  142,   17,    0,    0,    0,    0,    0,    0,    0,    0,
  143,  144,    0,  145,  146,  147,    0,  148,  149,  150,
  151,    0,  152,  153,  154,    0,    0,    0,    0,    0,
    0,    0,   18,   19,   20,   21,   22,   23,   24,   25,
   26,   27,   28,  155,  156,    0,   29,   30,   31,   32,
   33,   34,   35,   36,   37,   38,   39,   40,   41,   42,
  157,    0,  158,  159,  160,  161,  162,   44,  163,  164,
   45,  165,    0,    0,  166,    0,  167,    0,    0,    0,
  118,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    8,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,  119,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,  120,  121,  122,  123,  124,  125,  126,    0,
  127,    0,    0,    0,    0,    0,    0,    0,    9,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,  128,    0,    0,    0,    0,  129,    0,    0,    0,
  130,   10,   11,    0,    0,    0,    0,    0,  131,  132,
  133,    0,    0,  134,  135,    0,   12,    0,  136,    0,
  137,  138,    0,    0,    0,    0,    0,    0,    0,  139,
    0,    0,    0,   13,   14,   15,   16,  140,    0,    0,
    0,  141,  142,   17,    0,    0,    0,    0,    0,    0,
    0,    0,  143,  144,    0,  145,  146,  147,    0,  148,
  149,  150,  151,    0,  152,  153,  154,    0,    0,    0,
    0,    0,    0,    0,   18,   19,   20,   21,   22,   23,
   24,   25,   26,   27,   28,  155,  156,    0,   29,   30,
   31,   32,   33,   34,   35,   36,   37,   38,   39,   40,
   41,   42,  157,    0,  158,  159,  160,  161,  162,   44,
  163,  164,   45,    0,    0,    0,    0,    0,    0,  118,
  165,    0,    0,  166,    0,  167,    0,    0,    0,    0,
    8,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,  119,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,  120,  121,  122,  123,  124,  125,  126,    0,  127,
    0,    0,    0,    0,    0,    0,    0,    9,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
  128,    0,    0,    0,    0,  129,    0,    0,    0,  130,
   10,   11,    0,    0,    0,    0,    0,  131,  132,  133,
    0,    0,  134,  135,    0,   12,    0,  136,    0,  137,
  138,    0,    0,    0,    0,    0,    0,    0,  139,    0,
    0,    0,   13,   14,   15,   16,  140,  302,    0,    0,
  141,  142,   17,    0,    0,    0,    0,    0,    0,    0,
    0,  143,  144,    0,  145,  146,  147,    0,  148,  149,
  150,  151,    0,  152,  153,  154,    0,    0,    0,    0,
    0,    0,    0,   18,   19,   20,   21,   22,   23,   24,
   25,   26,   27,   28,  155,  156,    0,   29,   30,   31,
   32,   33,   34,   35,   36,   37,   38,   39,   40,   41,
   42,  157,    0,  158,  159,  160,  161,  162,   44,  163,
  164,   45,  404,    0,    0,    0,    0,    0,    0,  165,
    0,    0,  166,    0,  167,    0,    0,    0,  118,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    8,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
  119,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
  120,  121,  122,  123,  124,  125,  126,    0,  127,    0,
    0,    0,    0,    0,    0,    0,    9,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,  128,
    0,    0,    0,    0,  129,    0,    0,    0,  130,   10,
   11,    0,    0,    0,    0,    0,  131,  132,  133,    0,
    0,  134,  135,    0,   12,    0,  136,    0,  137,  138,
    0,    0,    0,    0,    0,    0,    0,  139,    0,    0,
    0,   13,   14,   15,   16,  140,    0,    0,    0,  141,
  142,   17,    0,    0,    0,    0,    0,    0,    0,    0,
  143,  144,    0,  145,  146,  147,    0,  148,  149,  150,
  151,    0,  152,  153,  154,    0,    0,    0,    0,    0,
    0,    0,   18,   19,   20,   21,   22,   23,   24,   25,
   26,   27,   28,  155,  156,    0,   29,   30,   31,   32,
   33,   34,   35,   36,   37,   38,   39,   40,   41,   42,
  157,  406,  158,  159,  160,  161,  162,   44,  163,  164,
   45,    0,    0,    0,    0,    0,    0,  118,  165,    0,
    0,  166,    0,  167,    0,    0,    0,    0,    8,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,  119,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,  120,
  121,  122,  123,  124,  125,  126,    0,  127,    0,    0,
    0,    0,    0,    0,    0,    9,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,  128,    0,
    0,    0,    0,  129,    0,    0,    0,  130,   10,   11,
    0,    0,    0,    0,    0,  131,  132,  133,    0,    0,
  134,  135,    0,   12,    0,  136,    0,  137,  138,    0,
    0,    0,    0,    0,    0,    0,  139,    0,    0,    0,
   13,   14,   15,   16,  140,    0,    0,    0,  141,  142,
   17,    0,    0,    0,    0,    0,    0,    0,    0,  143,
  144,    0,  145,  146,  147,    0,  148,  149,  150,  151,
    0,  152,  153,  154,    0,    0,    0,    0,    0,    0,
    0,   18,   19,   20,   21,   22,   23,   24,   25,   26,
   27,   28,  155,  156,    0,   29,   30,   31,   32,   33,
   34,   35,   36,   37,   38,   39,   40,   41,   42,  157,
    0,  158,  159,  160,  161,  162,   44,  163,  164,   45,
  408,    0,    0,    0,    0,    0,    0,  165,    0,    0,
  166,    0,  167,    0,    0,    0,  118,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    8,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,  119,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,  120,  121,
  122,  123,  124,  125,  126,    0,  127,    0,    0,    0,
    0,    0,    0,    0,    9,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,  128,    0,    0,
    0,    0,  129,    0,    0,    0,  130,   10,   11,    0,
    0,    0,    0,    0,  131,  132,  133,    0,    0,  134,
  135,    0,   12,    0,  136,    0,  137,  138,    0,    0,
    0,    0,    0,    0,    0,  139,    0,    0,    0,   13,
   14,   15,   16,  140,    0,    0,    0,  141,  142,   17,
    0,    0,    0,    0,    0,    0,    0,    0,  143,  144,
    0,  145,  146,  147,    0,  148,  149,  150,  151,    0,
  152,  153,  154,    0,    0,    0,    0,    0,    0,    0,
   18,   19,   20,   21,   22,   23,   24,   25,   26,   27,
   28,  155,  156,    0,   29,   30,   31,   32,   33,   34,
   35,   36,   37,   38,   39,   40,   41,   42,  157,  414,
  158,  159,  160,  161,  162,   44,  163,  164,   45,  434,
    0,    0,  166,    0,  167,  118,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    8,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,  119,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,  120,  121,  122,
  123,  124,  125,  126,    0,  127,    0,    0,    0,    0,
    0,    0,    0,    9,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,  128,    0,    0,    0,
    0,  129,    0,    0,    0,  130,   10,   11,    0,    0,
    0,    0,    0,  131,  132,  133,    0,    0,  134,  135,
    0,   12,    0,  136,    0,  137,  138,    0,    0,    0,
    0,    0,    0,    0,  139,    0,    0,    0,   13,   14,
   15,   16,  140,    0,    0,    0,  141,  142,   17,    0,
    0,    0,    0,    0,    0,    0,    0,  143,  144,    0,
  145,  146,  147,    0,  148,  149,  150,  151,    0,  152,
  153,  154,    0,    0,    0,    0,    0,    0,    0,   18,
   19,   20,   21,   22,   23,   24,   25,   26,   27,   28,
  155,  156,    0,   29,   30,   31,   32,   33,   34,   35,
   36,   37,   38,   39,   40,   41,   42,  157,    0,  158,
  159,  160,  161,  162,   44,  163,  164,   45,  165,    0,
  433,  166,    0,  167,    0,    0,    0,  118,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    8,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,  119,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,  120,
  121,  122,  123,  124,  125,  126,    0,  127,    0,    0,
    0,    0,    0,    0,    0,    9,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,  128,    0,
    0,    0,    0,  129,    0,    0,    0,  130,   10,   11,
    0,    0,    0,    0,    0,  131,  132,  133,    0,    0,
  134,  135,    0,   12,    0,  136,    0,  137,  138,    0,
    0,    0,    0,    0,    0,    0,  139,    0,    0,    0,
   13,   14,   15,   16,  140,    0,    0,    0,  141,  142,
   17,    0,    0,    0,    0,    0,    0,    0,    0,  143,
  144,    0,  145,  146,  147,    0,  148,  149,  150,  151,
    0,  152,  153,  154,    0,    0,    0,    0,    0,    0,
    0,   18,   19,   20,   21,   22,   23,   24,   25,   26,
   27,   28,  155,  156,    0,   29,   30,   31,   32,   33,
   34,   35,   36,   37,   38,   39,   40,   41,   42,  157,
    0,  158,  159,  160,  161,  162,   44,  163,  164,   45,
  165,  841,    0,  166,    0,  167,  118,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    8,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,  119,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,  120,  121,
  122,  123,  124,  125,  126,    0,  127,    0,    0,    0,
    0,    0,    0,    0,    9,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,  128,    0,    0,
    0,    0,  129,    0,    0,    0,  130,   10,   11,    0,
    0,    0,    0,    0,  131,  132,  133,    0,    0,  134,
  135,    0,   12,    0,  136,  801,  137,  138,    0,    0,
    0,    0,    0,    0,    0,  139,    0,    0,    0,   13,
   14,   15,   16,  140,    0,    0,    0,  141,  142,   17,
    0,    0,    0,    0,    0,    0,    0,    0,  143,  144,
    0,  145,  146,  147,    0,  148,  149,  150,  151,    0,
  152,  153,  154,    0,    0,    0,    0,    0,    0,    0,
   18,   19,   20,   21,   22,   23,   24,   25,   26,   27,
   28,  155,  156,    0,   29,   30,   31,   32,   33,   34,
   35,   36,   37,   38,   39,   40,   41,   42,  157,    0,
  158,  159,  160,  161,  162,   44,  163,  164,   45,  165,
    0,    0,  166,    0,  167,    0,    0,    0,  118,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    8,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
  119,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
  120,  121,  122,  123,  124,  125,  126,    0,  127,    0,
    0,    0,    0,    0,    0,    0,    9,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,  128,
    0,    0,    0,    0,  129,    0,    0,    0,  130,   10,
   11,    0,    0,    0,    0,    0,  131,  132,  133,    0,
    0,  134,  135,    0,   12,    0,  136,    0,  137,  138,
    0,    0,    0,    0,    0,    0,    0,  139,    0,    0,
    0,   13,   14,   15,   16,  140,    0,    0,    0,  141,
  142,   17,    0,    0,    0,    0,    0,    0,    0,    0,
  143,  144,    0,  145,  146,  147,    0,  148,  149,  150,
  151,    0,  152,  153,  154,    0,    0,    0,    0,    0,
    0,    0,   18,   19,   20,   21,   22,   23,   24,   25,
   26,   27,   28,  155,  156,    0,   29,   30,   31,   32,
   33,   34,   35,   36,   37,   38,   39,   40,   41,   42,
  157,    0,  158,  159,  160,  161,  162,   44,  163,  164,
   45,  165, 1191,    0,  166,    0,  167,  118,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    8,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,  119,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,  120,
  121,  122,  123,  124,  125,  126,    0,  127,    0,    0,
    0,    0,  864,    0,    0,    9,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,  128,    0,
    0,    0,    0,  129,    0,    0,    0,  130,   10,   11,
    0,    0,    0,    0,    0,  131,  132,  133,    0,    0,
  134,  135,    0,   12,    0,  136,    0,  137,  138,    0,
    0,    0,    0,    0,    0,    0,  139,    0,    0,    0,
   13,   14,   15,   16,  140,    0,    0,    0,  141,  142,
   17,    0,    0,    0,    0,    0,    0,    0,    0,  143,
  144,    0,  145,  146,  147,    0,  148,  149,  150,  151,
    0,  152,  153,  154,    0,    0,    0,    0,    0,    0,
    0,   18,   19,   20,   21,   22,   23,   24,   25,   26,
   27,   28,  155,  156,    0,   29,   30,   31,   32,   33,
   34,   35,   36,   37,   38,   39,   40,   41,   42,  157,
    0,  481,  159,  160,  161,  162,   44,  163,  164,   45,
  165,    0,    0,  166,    0,  167,    0,    0,    0,  118,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    8,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,  119,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,  120,  121,  122,  123,  124,  125,  126,    0,  127,
    0,    0,    0,    0,    0,    0,    0,    9,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
  128,    0,    0,    0,    0,  129,    0,    0,    0,  130,
   10,   11,    0,    0,    0,    0,    0,  131,  132,  133,
    0,    0,  134,  135,    0,   12,    0,  136,    0,  137,
  138,    0,    0,    0,    0,    0,    0,    0,  139,    0,
    0,    0,   13,   14,   15,   16,  140,    0,    0,    0,
  141,  142,   17,    0,    0,    0,    0,    0,    0,    0,
    0,  143,  144,    0,  145,  146,  147,    0,  148,  149,
  150,  151,    0,  152,  153,  154,    0,    0,    0,    0,
    0,    0,    0,   18,   19,   20,   21,   22,   23,   24,
   25,   26,   27,   28,  155,  156,    0,   29,   30,   31,
   32,   33,   34,   35,   36,   37,   38,   39,   40,   41,
   42,  157,    0,  158,  159,  160,  161,  162,   44,  163,
  164,   45,  165,    0,    0,  166,    0,  167,  118,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    8,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
  119,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
  120,  121,  122,  123,  124,  125,  126,    0,  127,    0,
    0,    0,    0,    0,    0,    0,    9,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,  128,
    0,    0,    0,    0,  129,    0,    0,    0,  130,   10,
   11,    0,    0,    0, 1432,    0,  131,  132,  133,    0,
    0,  134,  135,    0,   12,    0,  136,    0,  137,  138,
    0,    0,    0,    0,    0,    0,    0,  139,    0,    0,
    0,   13,   14,   15,   16,  140,    0,    0,    0,  141,
  142,   17,    0,    0,    0,    0,    0,    0,    0,    0,
  143,  144,    0,  145,  146,  147,    0,  148,  149,  150,
  151,    0,  152,  153,  154,    0,    0,    0,    0,    0,
    0,    0,   18,   19,   20,   21,   22,   23,   24,   25,
   26,   27,   28,  155,  156,    0,   29,   30,   31,   32,
   33,   34,   35,   36,   37,   38,   39,   40,   41,   42,
  157,    0,  481,  159,  160,  161,  162,   44,  163,  164,
   45,  165,    0,    0,  166,    0,  167,    0,    0,    0,
  118,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    8,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,  119,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,  120,  121,  122,  123,  124,  125,  126,    0,
  127,    0,    0,    0,    0,    0,    0,    0,    9,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,  128,    0,    0,    0,    0,  129,    0,    0,    0,
  130,   10,   11,    0,    0,    0,    0,    0,  131,  132,
  133,    0,    0,  134,  135,    0,   12,    0,  136,    0,
  137,  138,    0,    0,    0,    0,    0,    0,    0,  139,
    0,    0,    0,   13,   14,   15,   16,  140,    0,    0,
    0,  141,  142,   17,    0,    0,    0,    0,    0,    0,
    0,    0,  143,  144,    0,  145,  146,  147,    0,  148,
  149,  150,  151,    0,  152,  153,  154,    0,    0,    0,
    0,    0,    0,    0,   18,   19,   20,   21,   22,   23,
   24,   25,   26,   27,   28,  155,  156,    0,   29,   30,
   31,   32,   33,   34,   35,   36,   37,   38,   39,   40,
   41,   42,  157,    0,  158,  159,  160,  161,  162,   44,
  163,  164,   45,  165,    0,    0,  166,    0,  167,  118,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    8,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,  119,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,  120,  121,  122,  123,  124,  125,  126,    0,  127,
    0,    0,    0,    0,    0,    0,    0,    9,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
  128,    0,    0,    0,    0,  129,    0,    0,    0,  130,
   10,   11,    0,    0,    0,    0,    0,  131,  132,  133,
  157,    0,  134,  135,    0,   12,    0,  136,    0,  137,
  138,    0,    0,    0,    0,    0,    0,    0,  139,    0,
    0,    0,   13,   14,   15,   16,  140,    0,    0,    0,
  141,  142,   17,    0,    0,    0,    0,    0,    0,    0,
    0,  143,  144,    0,  145,  146,  147,    0,  148,  149,
  150,  151,    0,  152,  153,  154,    0,    0,    0,    0,
    0,    0,    0,   18,   19,   20,   21,   22,   23,   24,
   25,   26,   27,   28,  155,  156,    0,   29,   30,   31,
   32,   33,   34,   35,   36,   37,   38,   39,   40,   41,
   42,  157,    0,  481,  159,  160,  161,  162,   44,  163,
  164,   45,    0,    0,    0,    0,    0,    0,    0,    0,
    0,  118,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    8,    0,    0,    0,    0,   46,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,  119,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,  120,  121,  122,  123,  124,  125,  126,
    0,  127,    0,    0,    0,    0,    0,    0,    0,    9,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,  128,    0,    0,    0,    0,  129,    0,    0,
    0,  130,   10, 1337,    0,    0,    0,    0,    0,  131,
  132,  133,    0,    0,  134,  135,    0,   12,    0,  136,
    0,  137,  138,  576,  588,    0,    0,  588,  157,  656,
  139,    0,    0,    0,   13,   14,   15,   16,  140,    0,
    0,    0,  141,  142,   17,    0,    0,  656,    0,    0,
    0,    0,    0,  143,  144,    0,  145,  146,  147,    0,
  148,  149,  150,  151,    0,  152,  153,  154,    0,    0,
    0,    0,    0,    0,  656,   18,   19,   20,   21,   22,
   23,   24,   25,   26,   27,   28,  155,  156,    0,   29,
   30,   31,   32,   33,   34,   35,   36,   37,   38,   39,
   40,   41,   42,  157,    0,  481,  159,  160,  161,  162,
   44,  163,  164,   45,    1,  157,    0,    0,    0,    0,
    0,    0,    2,    3,    0,    4,    5,    6,    7,    0,
    0,    0,    0,    0,    0,    0,    0,    0,  157,  157,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
   46,    0,    0,  157,    0,    0,    8,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
  157,  157,  157,  157,    0,    0,    0,    0,    0,    0,
  157,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,  157,  157,  157,  157,  157,  157,  157,  157,  157,
  157,  157,    0,    0,    0,  157,  157,  157,  157,  157,
  157,  157,  157,  157,  157,  157,  157,  157,  157,  157,
    0,    0,    0,    9,    0,    0,  157,    0,    0,  157,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,   10,   11,    0,    0,
    0,    0,    0,    0,    0,    0,   46,    0,    0,    0,
    0,   12,  588,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,   13,   14,
   15,   16,    0,    0,    0,    0,    0,    0,   17,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,   18,
   19,   20,   21,   22,   23,   24,   25,   26,   27,   28,
    0,    0,    0,   29,   30,   31,   32,   33,   34,   35,
   36,   37,   38,   39,   40,   41,   42,   43,    0,  588,
    0,    0,    0,    0,   44,    0,    0,   45,    4,    5,
    6,    7,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,  588,  588,  329,    0,    0,    0,    0,    0,
    0,    0,    0,  489,    0,    0,    0,  588,    0,    8,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,  588,  588,  588,  588,    0,    0,
    0,    0,    0,    0,  588,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,  588,    0,    0,    0,  588,  588,  588,  588,  588,
  588,  588,  588,  588,  588,  588,    0,    0,    0,  588,
  588,  588,  588,  588,  588,  588,  588,  588,  588,  588,
  588,  588,  588,  588,    0,    0,    9,    0,    0,    0,
  588,    0,    0,  588,    4,    5,    6,    7,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,   10,
   11,    0,    0,    0,    0,    0,    0,    0,    0,   53,
    0,    0,    0,    0,   12,    8,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,   13,   14,   15,   16,    0,    0,    0,    0,    0,
    0,   17,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,   18,   19,   20,   21,   22,   23,   24,   25,
   26,   27,   28,    0,    0,    0,   29,   30,   31,   32,
   33,   34,   35,   36,   37,   38,   39,   40,   41,   42,
   43,    0,    9,    0,    0,    0,    0,   44,    0,    0,
   45,    4,    5,    6,    7,    0,    0,    0,  339,    0,
    0,    0,    0,    0,    0,   10,   11,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
   12,    0,    8,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,   13,   14,   15,
   16,    0,    0,    0,    0,    0,    0,   17,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,  848,    0,
    0,    0,    0,    0,    0,    0,    0,    0,   18,   19,
   20,   21,   22,   23,   24,   25,   26,   27,   28,    0,
    0,    0,   29,   30,   31,   32,   33,   34,   35,   36,
   37,   38,   39,   40,   41,   42,   43,    0,    0,    9,
    0,    0,    0,   44,    0,    0,   45,   53,   53,   53,
   53,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,   10,   11,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,   12,   53,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,   13,   14,   15,   16,    0,    0,
    0,    0,    0,    0,   17,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,  579,    0,    0,  579,    0,
  649,    0,    0,    0,    0,   18,   19,   20,   21,   22,
   23,   24,   25,   26,   27,   28,    0,    0,  649,   29,
   30,   31,   32,   33,   34,   35,   36,   37,   38,   39,
   40,   41,   42,   43,    0,   53,    0,    0,    0,    0,
   44,    0,    0,   45,    0,  649,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    8,   53,   53,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,   53,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
   53,   53,   53,   53,    0,    0,    0,    0,    0,    0,
   53,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    8,    0,    0,
    0,   53,   53,   53,   53,   53,   53,   53,   53,   53,
   53,   53,    0,    0,    9,   53,   53,   53,   53,   53,
   53,   53,   53,   53,   53,   53,   53,   53,   53,   53,
  581,    0,    0,  581,    0,  650,   53,   10,   11,   53,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,   12,  650,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,   13,
   14,   15,   16,    0,    0,    0,    0,    0,    0,   17,
  650,    0,    0,    0,    9,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,  579,   10,   11,    0,
   18,   19,   20,   21,   22,   23,   24,   25,   26,   27,
   28,    0,   12,  579,   29,   30,   31,   32,   33,   34,
  338,   36,   37,   38,   39,   40,   41,   42,   43,   13,
   14,   15,   16,    0,    0,   44,    0,    0,   45,   17,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,  582,
    0,    0,  582,    0,  651,    0,    0,    0,    0,    0,
   18,   19,   20,   21,   22,   23,   24,   25,   26,   27,
   28,    0,  651,    0,   29,   30,   31,   32,   33,   34,
   35,   36,   37,   38,   39,   40,   41,   42,   43,    0,
  579,    0,    0,    0,    0,   44,    0,    0,   45,  651,
    0,    0,    0,    0,    0,  579,    0,    0,    0,    0,
    0,    0,    0,  579,  579,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,  579,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
  579,    0,    0,    0,    0,  579,  579,  579,  579,    0,
    0,  581,    0,    0,    0,  579,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,  581,    0,
    0,    0,    0,    0,    0,  585,    0,    0,  585,    0,
  654,    0,  579,    0,    0,    0,  579,  579,  579,  579,
  579,  579,  579,  579,  579,  579,  579,    0,  654,    0,
  579,  579,  579,  579,  579,  579,  579,  579,  579,  579,
  579,  579,  579,  579,  579,    0,    0,    0,    0,    0,
    0,  579,    0,    0,  579,  654,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,  581,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
  581,    0,    0,    0,    0,    0,    0,    0,  581,  581,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
  582,    0,    0,  581,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,  581,    0,  582,    0,    0,
  581,  581,  581,  581,  587,    0,    0,  587,    0,  655,
  581,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,  655,    0,    0,
    0,    0,    0,    0,    0,    0,    0,  581,    0,    0,
    0,  581,  581,  581,  581,  581,  581,  581,  581,  581,
  581,  581,    0,    0,  655,  581,  581,  581,  581,  581,
  581,  581,  581,  581,  581,  581,  581,  581,  581,  581,
    0,    0,    0,    0,    0,    0,  581,    0,    0,  581,
    0,    0,    0,    0,  582,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,  582,
    0,    0,    0,    0,    0,    0,  585,  582,  582,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,  582,  585,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,  582,    0,    0,    0,    0,  582,
  582,  582,  582,  597,    0,    0,  597,    0,  595,  582,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,  595,    0,    0,    0,
    0,    0,    0,    0,    0,    0,  582,    0,    0,    0,
  582,  582,  582,  582,  582,  582,  582,  582,  582,  582,
  582,    0,    0,  595,  582,  582,  582,  582,  582,  582,
  582,  582,  582,  582,  582,  582,  582,  582,  582,    0,
  585,    0,    0,    0,    0,  582,    0,    0,  582,    0,
    0,    0,    0,    0,    0,  585,    0,    0,    0,    0,
    0,    0,    0,  585,  585,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,  587,    0,    0,  585,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
  585,    0,  587,    0,    0,  585,  585,  585,  585,  598,
    0,    0,  598,    0,  657,  585,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,  657,    0,    0,    0,    0,    0,    0,    0,
    0,    0,  585,    0,    0,    0,  585,  585,  585,  585,
  585,  585,  585,  585,  585,  585,  585,    0,    0,  657,
  585,  585,  585,  585,  585,  585,  585,  585,  585,  585,
  585,  585,  585,  585,  585,    0,    0,    0,    0,    0,
    0,  585,    0,    0,  585,    0,    0,    0,    0,  587,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,  587,    0,    0,    0,    0,    0,
    0,    0,  587,  587,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,  597,    0,    0,  587,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,  587,
    0,  597,    0,    0,  587,  587,  587,  587,  583,    0,
    0,  583,    0,  652,  587,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,  652,    0,    0,    0,    0,    0,    0,    0,    0,
    0,  587,    0,    0,    0,  587,  587,  587,  587,  587,
  587,  587,  587,  587,  587,  587,    0,    0,  652,  587,
  587,  587,  587,  587,  587,  587,  587,  587,  587,  587,
  587,  587,  587,  587,    0,    0,    0,    0,    0,    0,
  587,    0,    0,  587,    0,    0,    0,    0,  597,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,  597,    0,    0,    0,    0,    0,    0,
  598,  597,  597,    0,    0,    0,    0,    0,    0,    0,
  589,    0,    0,  589,    0,    0,  597,  598,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,  597,    0,
    0,    0,    0,  597,  597,  597,  597,    0,    0,    0,
    0,    0,    0,  597,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
  597,    0,    0,    0,  597,  597,  597,  597,  597,  597,
  597,  597,  597,  597,  597,    0,    0,    0,  597,  597,
  597,  597,  597,  597,  597,  597,  597,  597,  597,  597,
  597,  597,  597,    0,  598,    0,    0,    0,    0,  597,
    0,    0,  597,    0,    0,    0,    0,    0,    0,  598,
    0,    0,    0,    0,    0,    0,    0,  598,  598,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,  583,
  472,    0,  598,  472,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,  598,    0,  583,    0,    0,  598,
  598,  598,  598,    0,    0,    0,    0,    0,    0,  598,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,  598,    0,    0,    0,
  598,  598,  598,  598,  598,  598,  598,  598,  598,  598,
  598,    0,    0,    0,  598,  598,  598,  598,  598,  598,
  598,  598,  598,  598,  598,  598,  598,  598,  598,    0,
    0,    0,    0,    0,    0,  598,    0,    0,  598,    0,
    0,    0,    0,  583,    0,    0,    0,    0,    0,    0,
    0,  589,    0,    0,    0,    0,    0,    0,  583,  602,
    0,    0,  602,    0,    0,    0,  583,  583,  589,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,  583,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,  583,    0,    0,    0,    0,  583,  583,
  583,  583,    0,    0,    0,    0,    0,    0,  583,  599,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,  583,    0,    0,    0,  583,
  583,  583,  583,  583,  583,  583,  583,  583,  583,  583,
    0,    0,    0,  583,  583,  583,  583,  583,  583,  583,
  583,  583,    0,  583,  583,  583,  583,  583,  603,    0,
  589,  603,    0,    0,  583,    0,    0,  583,  589,  589,
    0,  472,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,  589,    0,    0,    0,    0,  472,    0,
    0,    0,    0,    0,    0,  589,    0,    0,    0,    0,
  589,  589,  589,  589,    0,    0,    0,    0,  600,    0,
  589,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,  589,    0,    0,
    0,  589,  589,  589,  589,  589,  589,  589,  589,  589,
  589,  589,    0,    0,    0,  589,  589,  589,  589,  589,
  589,  589,  589,  589,  589,  589,  589,  589,  589,  589,
    0,    0,    0,    0,    0,  472,  589,    0,    0,  589,
    0,    0,    0,    0,    0,    0,    0,  728,    0,    0,
  472,    0,    0,    0,    0,    0,    0,    0,  472,  472,
    0,    0,    0,    0,    0,    0,    0,  602,    0,    0,
    0,    0,    0,  472,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,  472,    0,    0,    0,    0,
  472,  472,  472,  472,    0,    0,    0,    0,    0,    0,
  472,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,  472,    0,    0,
    0,  472,  472,  472,  472,  472,  472,  472,  472,  472,
  472,  472,    0,    0,    0,  472,  472,  472,  472,  472,
  472,  472,  472,  472,  602,  472,  472,  472,  472,  472,
    0,    0,    0,    0,    0,    0,  472,    0,    0,  472,
    0,    0,    0,    0,    0,    0,  603,  602,  602,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,  602,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,  602,
  602,  602,  602,    0,    0,    0,    0,    0,    0,  602,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,  602,    0,    0,    0,
  602,  602,  602,  602,  602,  602,  602,  602,  602,  602,
  602,    0,    0,  603,  602,  602,  602,  602,  602,  602,
  602,  602,  602,  602,  602,  602,  602,  602,  602,    0,
    0,    0,    0,    0,    0,  602,  603,  603,  602,    0,
    0,  118,    0,    0,    0,    0,    0,    0,    0,    0,
    0,  603,    8,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,  603,  603,
  603,  603,    0,  119,    0,    0,    0,    0,  603,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,  603,    0,  347,    0,  603,
  603,  603,  603,  603,  603,  603,  603,  603,  603,  603,
    0,    0,    8,  603,  603,  603,  603,  603,  603,  603,
  603,  603,  603,  603,  603,  603,  603,  603,    0,    9,
    0,    0,    0,    0,  603,    0,    0,  603,    0,    0,
    0,    0,  128,    0,    0,    0,    0,  129,    0,    0,
    0,  130,   10,   11,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,   12,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,   13,   14,   15,   16,    0,    0,
    0,    0,  141,    0,   17,    0,    0,    0,    0,    9,
    0,    0,    0,  709,  533,  534,  535,  536,  710,  538,
  539,  540,  541,  542,  543,  544,    0,    0,    0,    0,
    0,    0,   10,   11,    0,   18,   19,   20,   21,   22,
   23,   24,   25,   26,   27,   28,    0,   12,    0,   29,
   30,   31,   32,   33,   34,   35,   36,   37,   38,   39,
   40,   41,   42,   43,   13,   14,   15,   16,  118,    0,
   44,    0,    0,   45,   17,    0,    0,    0,    0,    8,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
  119,    0,    0,    0,    0,   18,   19,   20,   21,   22,
   23,   24,   25,   26,   27,   28,    0,    0,    0,   29,
   30,   31,   32,   33,   34,   35,   36,   37,   38,   39,
   40,   41,   42,   43,    0,    0,    0,    0,    0,    0,
   44,    0,    0,   45,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    9,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,  128,
    0,    0,    0,    0,  129,    0,    0,    0,  130,   10,
   11,    0,    0,    0,  118,    0,    0,    0,    0,    0,
    0,    0,    0,    0,   12,    8,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,   13,   14,   15,   16,    0,  119,    0,    0,  141,
    0,   17,    0,    0,    0,    0,    0,    0,    0,    0,
  532,  533,  534,  535,  536,  537,  538,  539,  540,  541,
  542,  543,  544,    0,    0,    0,    0,    0,    0,    0,
    0,    0,   18,   19,   20,   21,   22,   23,   24,   25,
   26,   27,   28,    0,    0,    0,   29,   30,   31,   32,
   33,   34,   35,   36,   37,   38,   39,   40,   41,   42,
   43,    0,    9,    0,    0,    0,    0,   44,    0,    0,
   45,    0,    0,    0,    0,  128,    0,    0,    0,    0,
  129,    0,    0,    0,  130,   10,   11,    0,    0,    0,
  118,    0,    0,    0,    0,    0,    0,    0,    0,    0,
   12,    8,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,   13,   14,   15,
   16,    0,  119,    0,    0,  141,    0,   17,    0,    0,
    0,    0,    0,    0,    0,    0,  709,  533,  534,  535,
  536,  710,  538,  539,  540,  541,  542,  543,  544,    0,
    0,    0,    0,    0,    0,    0,    0,    0,   18,   19,
   20,   21,   22,   23,   24,   25,   26,   27,   28,    0,
    0,    0,   29,   30,   31,   32,   33,   34,   35,   36,
   37,   38,   39,   40,   41,   42,   43,    0,    9,    0,
    0,    0,    0,   44,    0,    0,   45,    0,    0,    0,
    0,  128,    0,    0,    0,    0,  129,    0,    0,    0,
  130,   10,   11,    0,    0,    0,  118,    0,    0,    0,
    0,    0,    0,    0,    0,    0,   12,    8,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,   13,   14,   15,   16,    0,  119,    0,
    0,  141,    0,   17,    0,    0,    0,    0,    0,    0,
    0,    0,  143,  144,    0,  145,  146,  147,    0,  148,
  149,    0,  151,    0,  152,  153,  154,    0,    0,    0,
    0,    0,    0,    0,   18,   19,   20,   21,   22,   23,
   24,   25,   26,   27,   28,    0,    0,    0,   29,   30,
   31,   32,   33,   34,   35,   36,   37,   38,   39,   40,
   41,   42,   43,    0,    9,    0,    0,    0,    0,   44,
    0,    0,   45,    0,    0,    0,    0,  128,    0,    0,
    0,    0,  129,    0,    0,    0,  130,   10,   11,    0,
    0,    0,  118,    0,    0,    0,    0,    0,    0,    0,
    0,    0,   12,    8,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,   13,
   14,   15,   16,    0,  119,    0,    0,  141,    0,   17,
    0,    0,    0,    0,    0,    0,    0,    0,    0,  144,
    0,  145,  146,  147,    0,  148,  149,    0,  151,    0,
  152,  153,  154,    0,    0,    0,    0,    0,    0,    0,
   18,   19,   20,   21,   22,   23,   24,   25,   26,   27,
   28,    0,    0,    0,   29,   30,   31,   32,   33,   34,
   35,   36,   37,   38,   39,   40,   41,   42,   43,    0,
    9,    0,    0,    0,    0,   44,    0,    0,   45,    0,
    0,    0,    0,  128,    0,    0,    0,    0,  129,    0,
    0,    0,  130,   10,   11,    0,    0,    0,  118,    0,
    0,    0,    0,    0,    0,    0,    0,    0,   12,    8,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,   13,   14,   15,   16,    0,
  119,    0,    0,  141,    0,   17,    0,    0,    0,    0,
    0,    0,    0,    0,  143,  144,    0,  145,  146,  147,
    0,  148,  149,    0,  151,    0,  152,    0,    0,    0,
    0,    0,    0,    0,    0,    0,   18,   19,   20,   21,
   22,   23,   24,   25,   26,   27,   28,    0,    0,    0,
   29,   30,   31,   32,   33,   34,   35,   36,   37,   38,
   39,   40,   41,   42,   43,    0,    9,    0,    0,    0,
    0,   44,    0,    0,   45,    0,    0,    0,    0,  128,
    0,    0,    0,    0,  129,    0,    0,    0,  130,   10,
   11,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,   12,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,   13,   14,   15,   16,    0,    0,    0,    0,  141,
    8,   17,    0,    0,    0,    0,    0,    0,    0,    0,
    0,  144,    0,  145,  146,  147,    0,  148,  149,    0,
  151,    0,  152,    0,    0,    0,    0,    0,    0,    0,
    0,    0,   18,   19,   20,   21,   22,   23,   24,   25,
   26,   27,   28,    0,    0,    0,   29,   30,   31,   32,
   33,   34,   35,   36,   37,   38,   39,   40,   41,   42,
   43,    0,    0,    0,    0,    0,    0,   44,    0,    0,
   45,  233,  234,  235,  236,  237,  238,  239,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    9,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
   10,   11,    0,    0,    0,    0,    0,  240,    0,    0,
    0,    0,  241,  242,    0,   12,    0,    0,    0,    0,
    0,    0,    8,    0,    0,    0,    0,    0,  243,    0,
    0,    0,   13,   14,   15,   16,    0,    0,    0,    0,
    0,    0,   17,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,   18,   19,   20,   21,   22,   23,   24,
   25,   26,   27,   28,    0,    0,    0,   29,   30,   31,
   32,   33,   34,   35,   36,   37,   38,   39,   40,   41,
   42,   43,  691,  692,  693,  694,    0,    0,   44,    9,
    0,   45,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    8,   10,   11,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,   12,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,   13,   14,   15,   16,    0,    0,
    0,    0,    0,    0,   17,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,   18,   19,   20,   21,   22,
   23,   24,   25,   26,   27,   28,    0,    0,    9,   29,
   30,   31,   32,   33,   34,   35,  695,  696,   38,   39,
   40,   41,   42,   43,    0,    0,    0,    0,    0,    0,
   44,   10,   11,   45,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,   12,    0,    8,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,   13,   14,   15,   16,    0,    0,    0,
    0,    0,    0,   17,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,  153,  154,    0,    0,    0,
    0,    0,    0,    0,   18,   19,   20,   21,   22,   23,
   24,   25,   26,   27,   28,    0,    0,    0,   29,   30,
   31,   32,   33,   34,   35,   36,   37,   38,   39,   40,
   41,   42,   43,    0,  158,    9,    0,    0,    0,   44,
    0,    0,   45,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    8,   10,   11,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,   12,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
   13,   14,   15,   16,    0,    0,    0,    0,    0,    0,
   17,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,  153,  154,    0,    0,    0,    8,    0,    0,
    0,   18,   19,   20,   21,   22,   23,   24,   25,   26,
   27,   28,    0,    0,    9,   29,   30,   31,   32,   33,
   34,   35,   36,   37,   38,   39,   40,   41,   42,   43,
    0,    0,    0,    0,    0,    0,   44,   10,   11,   45,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,   12,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    8,    0,   13,
   14,   15,   16,    0,    0,    0,    0,    0,    0,   17,
    0,    0,    0,    0,    9,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,  519,   10,   11,    0,
   18,   19,   20,   21,   22,   23,   24,   25,   26,   27,
   28,    0,   12,    0,   29,   30,   31,   32,   33,   34,
   35,   36,   37,   38,   39,   40,   41,   42,   43,   13,
   14,   15,   16,    0,    0,   44,    0,    0,   45,   17,
    0,    0,    0,    0,    9,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0, 1022,    0,    0,    0,    0,    8,   10,   11,    0,
   18,   19,   20,   21,   22,   23,   24,   25,   26,   27,
   28,    0,   12,    0,   29,   30,   31,   32,   33,   34,
   35,   36,   37,   38,   39,   40,   41,   42,   43,   13,
   14,   15,   16,    0,    0,   44,    0,    0,   45,   17,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0, 1131,    0,    0,    0,    0,    8,    0,    0,    0,
   18,   19,   20,   21,   22,   23,   24,   25,   26,   27,
   28,    0,    0,    9,   29,   30,   31,   32,   33,   34,
   35,   36,   37,   38,   39,   40,   41,   42,   43,    0,
    0,    0,    0,    0,    0,   44,   10,   11,   45,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,   12,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    8,    0,   13,   14,
   15,   16,    0,    0,    0,    0,    0,    0,   17,    0,
    0,    0,    0,    9,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
 1133,    0,    0,    0,    0,    0,   10,   11,    0,   18,
   19,   20,   21,   22,   23,   24,   25,   26,   27,   28,
    0,   12,    0,   29,   30,   31,   32,   33,   34,   35,
   36,   37,   38,   39,   40,   41,   42,   43,   13,   14,
   15,   16,    0,    0,   44,    0,    0,   45,   17,    0,
    0,    0,    0,    9,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
 1391,    0,    0,    0,    0,    0,   10,   11,    0,   18,
   19,   20,   21,   22,   23,   24,   25,   26,   27,   28,
    0,   12,    0,   29,   30,   31,   32,   33,   34,   35,
   36,   37,   38,   39,   40,   41,   42,   43,   13,   14,
   15,   16,    0,    0,   44,    0,    0,   45,   17,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,   18,
   19,   20,   21,   22,   23,   24,   25,   26,   27,   28,
    0,    0,    0,   29,   30,   31,   32,   33,   34,   35,
   36,   37,   38,   39,   40,   41,   42,   43,    0,    0,
    0,    0,    0,    0,   44,    0,    0,   45,
};
}
static short yycheck1[],yycheck2[];
static { yycheck1();yycheck2();}
static short yycheck(int index) {
  if (index <=1 * 5000)
  {
    return yycheck1[index];
  }
  return yycheck2[index - 1 -5000 * (2 - 1)];
}
static void yycheck1() {
yycheck1 = new short[] {
                        337,
    0,  296,  297,  431,  337,  364,   40,    7,  323,  359,
  484,  518,  486,  487,  518,    0,  307,   46,  719,  310,
   41,  528,  325,   40,  528,  704,  692,    0,  358,  518,
  344,  322,  362,    0,  337,  524,   44,   41,  323,  528,
  339,  400,    0,  143,    0,  956,   46,   40,  756,  364,
  430,  431,    0,   40,   93,  763,   44,    0,   41,   40,
   41,   40,   41,  396,   40,   41,    0,  525,   40,   41,
   40,   41,  392,  677,   40,   41,   40,   41,   40,   42,
   43,  293,   45,   44,   47,  400,    0,    0,  728,   91,
   46,  562,   92,   93,   94,   91,   96,   97,   98,   99,
  293,  300,  102,  103,  337,  105,  106,   41,  117,  109,
  110,  111,  112,  113,  114,  900,  116,  117, 1030,  433,
  434,  284,   91,  300,   91,  365,  281,   41,   41,  369,
    0,  424,  372, 1324,  116,   93,    0,  364,  310,   40,
    3,  282, 1010,  124,  382,  124,   42,   43,  124,   45,
  337,   47,  124,  425,  124,  278,  279,  280,  124,  518,
  124,  124,   40,   42,   43,  282,   45,  501,   47,  528,
  520,   41,  531,  281,   44,  525,  508,   41, 1090,   41,
   44,   42,   43,  657,   45,  519,   47,   40,    0,  116,
   42,   43,  116,   45,    0,   47,  440,   42,   41,    0,
 1283,   44,   47,  518,  442,  205,  206,  207,  208,  209,
  382,  877,  212,  528,  369,   93,  531,  217,  218,  219,
  220,  221,  923,  323,  324,  518,  326,  209,  124,   41,
  258,  231,   44, 1145,  262,   41,  566,  567,  568, 1430,
   41,  404,  313,   44,  443,  124,  518,  519,  411, 1332,
  721,  251,  252,  253, 1039,  895,   42,   43,  258,   45,
  401,   47,  124,  124,  364,  585,  443, 1135,   42,   43,
  378,   45,  124,   47,  441,  519, 1187,  313,   42,   43,
  282,   45,  209,   47,  401,  209,  282,  273, 1371,  289,
 1339,  291,  292,  293,  752,  278,  279,  280,   42,   43,
  400,   45,  282,   47,  531,  314,  377,  519,   41, 1017,
   46,   44,  273,  282,  314,  315, 1227,  282,  485,  282,
  295,  635,  636,  637,  447,  616,  519,  302,   64,  329,
   42,   43, 1381,   45,   41,   47,    7,   44,  124,  339,
  283,  377,  285,  286,  287,  384,  346,  347,  663,  283,
  124,  285,  286,  287,  692,  355,  356,  357,  358,  359,
  124,  224,  362,  363,  364,  365,  366,  367,  368,  369,
  370,  721,  372,  492,  665,    0,  376,  676,  708,  807,
  124,  500,  320,  339,  390,  391,  320,  393,  691,  703,
  258,  721,  377,  723,  868,  690,  870,  871, 1002,  355,
  400,  357,  752,  396,  377, 1313,  320,  320, 1316,   40,
  397,  415,  124,  283,  370,  285,  286,  287,  518,  283,
  779,  285,  286,  287,  524,  896,  384,  807,  528,  377,
 1341,  531,  532,   46,  403,  259,  404,  440,  400,  309,
  310,  311,  312,  411, 1352,  379,  117,  405,  406, 1357,
  320,   64,   42,   43,  423,   45,  320,   47,   40,  692,
  489,  489,  274,  275,  779,  277,  278,  279,  280,  140,
  478,  283,  382,  285,  286,  287, 1142,  283,  259,  285,
  286,  287,  283,  517,  285,  286,  287,  508,  505,  489,
  478,  731,   40,  496,  165,  166,  167,   40,  310,  499,
  500,  501,  487,  488,  504,  692,   42,   43,  320,  379,
  322,  323,  324,   40,  320,  379,  519,  484,  518,  320,
  520,  812,   40,  756,  809,  525,  872,   63,  528,  820,
  763,  531,  442,  489,  124,  478,  517,  407,  517, 1418,
  488,  517,  856, 1422,  478,  517,  896,  517,  907,  908,
  435,  517,   40,  517,  439,    0,  915,  916,  917,  918,
  919,  920,  458,  663,  478,  478,  566,  567,  568,  756,
  382, 1060,  902,  387,  508,  389,  763, 1285,  379,  458,
  586,    0,   41,   42,   43,  468,   45,  872,   47,  892,
 1298,  894,  907,  908,  508,  508,   41,  458,   40,  468,
  915,  916,  917,  918,  919,  920,  458,  501,  478,  709,
   40,  611,  612,  613,  478,  286,  287,  288,  289,  290,
   40, 1300,  290,  506,  295,  519,  404,  295, 1102, 1337,
  301,  302,   40,  411,  302,  447,  307,  506,  508,  310,
  448,  449,  492,  314,  508,  495,  317,  318,  319,  320,
  500,  322,  658,  999,  496,  292,  293, 1085,  283,  501,
  285,  286,  287,  449, 1010,  124,  478,   41,  298,  496,
   44,   40,  478,  344,  501,  449,  676,  478,  678,  779,
  907,  908, 1390,  278, 1173,  280,  977,   40,  915,  916,
  917,  918,  919,  920,  458,  320,  508,  702,    0,   40,
  700,  701,  508,  447, 1412, 1085,  496,  508,  708,   41,
   40,  501,   44, 1016,  999,   41,   40,   41,   44, 1014,
   44,  721,   46,  723,  296, 1010,  298,  299,  728,  735,
  387,  731,  389,  404,  432,  406,  434,  408,   41,  410,
   64,   44, 1450,  414,  519, 1030,  458, 1448,  419,  420,
 1109,   44,  752,   42,   43,  426,   45,   40,   47,  430,
  431,   40,  433,  434,  394,  459,   41,   91,  439,   44,
   91,  442,    0,  446,   46,  448,  449, 1478,   41,  779,
   42,   43,   44,   45, 1017,   47,   41,   42,   43, 1135,
   45,  462,   47,  798, 1109,  800,  467,  427,  509, 1137,
 1138,   41,  274,  809,  798, 1090,  800,  907,  908,  809,
  369, 1096,  264,   41,    0,  915,  916,  917,  918,  919,
  920,  486,   41, 1153, 1154,   44,  309, 1122,  311,  312,
 1017,   40,  277, 1136, 1137, 1138,   41,  314,  283,   44,
  285,  286,  287,   40,  844,   40,  846, 1150,  848,   41,
 1135, 1184,   44,  282,   41,   41,  446,   44,   44,   46,
 1145, 1207,  124,   40,  283,  310,  285,  286,  287,  124,
  288,   40,  872,  873, 1416,  320,   40,   64,   41, 1066,
 1387,   44, 1109, 1387,   40,  303,  304,  305,  306,  307,
 1228, 1229,  848, 1435,   40,  895,  896,   41,   42,   43,
  259,   45,  902,   47, 1137, 1138,   41,  907,  908,   44,
  475, 1241,    0, 1455,  365,  915,  916,  917,  918,  919,
  920,  452, 1207,  452, 1466, 1228, 1229,  452,   41, 1224,
 1225,   44,  452,  563,  291, 1238,  446,  382,  448,  449,
  570,  441,  948,  368,  950,  616,  952,  577,  292,  293,
 1137, 1138,  623,   41,  518,  519,   44,  962,  282,  964,
 1060,  322,  323,  324,  635,  636,  637,  638,   41,  640,
  641,   44,  368,  644,  645,  646,  647,  648,  649,  438,
  124,  283,  653,  285,  286,  287,   40,   42,   43,  995,
   45,  997,   47,  999,  665, 1228, 1229, 1471, 1301,  999,
   41, 1001,  283,   91,  285,  286,  287, 1007, 1008, 1109,
 1010,  448,  449,  365,  366,  367,  368,  369, 1018, 1019,
  372,  278,  279,  280, 1030, 1025, 1026,    0, 1387,   41,
 1030,  702,  703,  478, 1325,   42,   43,   41,   45,  518,
   47, 1228, 1229, 1230, 1231,   76,   42,   43,   79,   45,
   41,   47, 1285,   44,   40,  283,   40,  285,  286,  287,
  446,   42,   43,  508,   45, 1298,   47,   41,   41,  124,
   44,    0, 1387, 1173,  124, 1080,  512,  401, 1083,   41,
  379,  380,  381,  382, 1090,   41, 1424, 1425,  274,  275,
 1090,  277,  320,  325,  326,  282, 1096,  283, 1285,  285,
  286,  287,   42,   43, 1337,   45,   41,   47,   40, 1109,
   44, 1298,   41,  325,  326,   44,  283,  124,  285,  286,
  287, 1424, 1425,  275,  310,  755,  319,  798,  124,  800,
    0,   40,  762,  402,  320, 1135,  807, 1440,  378, 1145,
 1454,  812,   40,  124,   40, 1145, 1441,  325,  326,  820,
 1337,  298,  299, 1153, 1154,   40, 1459, 1390,  829,  830,
  831,  832,  833,  834,  835,  283,  310,  285,  286,  287,
  368,   41,  375,  376,   44,  805,  518,  519,    0, 1412,
 1475,  448,  449,  438,  124,  856,  274,  275,  290,  277,
  300, 1424, 1425,  295,  282,  283,  382,  285,  286,  287,
  302, 1207,    0, 1390,  503,  504,  368, 1207,  300, 1209,
 1210, 1211, 1212,  295,  401,  441,    0, 1450,  441,   41,
  405,  406,  310,   42,   43, 1412,   45,  282,   47,  469,
  448,  449,  320,  476,  322,  323,  324, 1424, 1425,  452,
  477, 1241,  512,   41,  874,  479,  480,  513,  378,  436,
  478,  519,  477,  493,  494,  500,   41,   41,  459,   46,
   44,    0,  430, 1450,  366,  367,   46,   46, 1274,   41,
  430,  278,  512,  513,  514,  515,  516,   41,  518,  519,
  508,  446, 1282,    0,  955,  956,  282, 1387,   41,   41,
   93,  962,  478,  964,  382,  966,   40,  385,  359,    0,
   41,  282,   41,   41,   44,  124,  977,  978,   40,   40,
  283,  282,  285,  286,  287,  403,  282,  205,  206,  207,
  208,  276,  508,  277,   41,   41,   44,   44, 1328,   42,
   43,    0,   45,   41,   47,  423,  479,  310,   44,  469,
   41,   41, 1342,   41, 1344,  274,  275,  320,  277,  278,
  279,  280,   41,   41,  283,  282,  285,  286,  287,   41,
   44, 1032,   41,  493,  494,   42,   43,   44,   45,   41,
   47,   41,   41,   42,   43,  478,   45,   41,   47,  731,
   40,  310,  512,  513,  514,  515,  516, 1387,  518,  519,
  478,  320,  292,  322,  323,  324,  301,  415,  301,  500,
 1071,  292,  301, 1403,  274,  275,  379,  277,  301, 1080,
  415,  124, 1083,  283, 1085,  285,  286,  287, 1418,  281,
  508,  283, 1422,  285,  286,  287, 1056, 1057,    0,  261,
  477,   41,   42,   43,  407,   45,  399,   47,  477,   41,
  310,  329,   41,  276,   41,  276, 1446,  124, 1119,   44,
  320,   41,  276,  382,  283,  124,  285,  286,  287,   41,
   40,  283, 1462,  285,  286,  287,  402,  369,   40,   41,
   42,   43,   44,   45,   41,   47,  281,  282,  283,  403,
  285,  286,  287,  326,  326,  283,  326,  285,  286,  287,
  274,  275, 1122,  277,  278,  279,  280,   40,  320,  283,
 1171,  285,  286,  287,  385,  478, 1177,  369,  382, 1139,
  276,   40,  382,  385,  124,   93, 1187,   93,  447,   91,
   40,   93,  320,   91,   40,   40,  310,   44,  377, 1159,
 1160,  479,   44,   41,  397,  508,  320,  397,  322,  323,
  324,  398,  397,  397,  283,  397,  285,  286,  287,  478,
  397,  397,  124,  397,   41, 1185, 1227,  274,  275,  400,
  277,  278,  279,  280,  369,  400,  283,   41,  285,  286,
  287,  310,   41,  274,  275,  444,  277,  444,  399,  508,
  379,  320,  283,  345,  285,  286,  287,  283,  300,  285,
  286,  287,    0,  310, 1224, 1225,  480,  283,  382,  285,
  286,  287,   41,  320,   41,  322,  323,  324,  478,  310,
 1281,  310,   41,  310,  283,  431,  285,  286,  287,  320,
  431, 1251,  278, 1253,   40, 1255, 1256, 1257,  378,  283,
    0,  285,  286,  287,   42,   43,   40,   45,  508,   47,
   41,   40,  282,  382,  258,  259,  260,  261,  262,  263,
  264,  320,   44,  267, 1325,  282,  478,  282,  258,  259,
  276,  261,  262,  447,  264,  382,   40,   44,  276,  430,
 1341,  407,   42,   43,  384,   45,  385,   47,  385, 1309,
  478,  382, 1312,   93,  282,   93,  508,   41,   42,   43,
   44,   45,  384,   47,  478,  282,  297,  310,   44,  299,
    0,  273,  274,  275,   41,  277,  278,  279,  280,  281,
  282,  283,  507,  285,  286,  287,  124, 1347, 1348,  468,
 1350, 1351,  294,   93,  508, 1355, 1356,  299,  399, 1359,
  447,  283,  483,  285,  286,  287,  308,  402,  310,  478,
  402,   41,   42,   43,   44,   45,    0,   47,  320,   41,
  322,  323,  324,  310,  124,  310,   44,  452,   41,   42,
   43,  478,   45,  385,   47,  452,  451,  481,  382,  508,
  124,  452,  453,  454,  455,  456,  457,  478,   41,   42,
   43,   44,   45, 1454,   47,   44,   41,   41,   41,   40,
   46,  508,   44,   93,  258,  259,  260,  369,  262,  263,
  264,   41,   41,  267,  396,  377,   93,  508,   41,  478,
  382,  284,  384,  276,  386,  387,  388,  389,  390,  391,
  392,  393,  394,  395,  124,   44,  379,   44,   91,  401,
  384,   44,   41,  405,  406,  384,  276,  409,  410,  508,
  297,  124,  297,   41,   44,   41,  478,    0,  282,   44,
   44,  282,  424,  505,  507,  282,   41,   42,   43,  401,
   45,  124,   47,  401,  436,  401,  438,  297,  482,  441,
  442,  443,  444,  512,  446,  447,  448,  449,  483,  451,
  452,  453,  454,  455,  456,  457,  458,  385,   41,   42,
   43,  512,   45,   41,   47,  386,  387,  388,  389,  390,
  391,  392,  393,  394,  395,   41,  478,  434,  434,   44,
  482,  483,  484,  485,  486,  487,  488,  489,  490,  491,
  492,   41,  309,   41,  496,  497,  498,  499,  500,  501,
  502,  503,  504,  505,  506,  507,  508,  509,  510,  124,
   41,   41,   41,  282,  282,  517,   44,  282,  520,   44,
  408,   41,   42,   43,   41,   45,   44,   47,  282,    0,
   40,   41,   42,   43,  282,   45,   46,   47,  430,  384,
  431,  124,  384,  273,  274,  275,  384,  277,  278,  279,
  280,  281,  282,  283,   64,  285,  286,  287,  384,   41,
   42,   43,  398,   45,  294,   47,  384,  405,  406,  299,
   41,   42,   43,   44,   45,   44,   47,    0,  308,  398,
  310,   91,  379,  398,  384,  385,  402,  398,  402,  282,
  320,  300,  322,  323,  324,  398,  402,  402,  484,  283,
  402,  285,  286,  287,  124,  405,  406,   41,   42,   43,
  435,   45,  480,   47,  124,  435,   41,   41,   41,  369,
   41,   40,   93,   40,   44,   41,  310,   41,   42,   43,
   40,   45,  412,   47,  409,  276,  320,  380,   41,  369,
  379,   41,  124,  399,  399,  507,    0,  377,  399,  484,
  399,  481,  382,  124,  384,  385,  386,  387,  388,  389,
  390,  391,  392,  393,  394,  395,   41,   42,   43,  468,
   45,  401,   47,  399,   41,  405,  406,   41,  519,  409,
  410,   41,   41,   42,   43,   40,   45,   41,   47,   40,
  124,  282,  413,  405,  424,  431,  497,  432,  382,  507,
  283,  496,  285,  286,  287,  385,  436,  496,  438,  496,
  124,  441,  442,  443,  444,  468,  446,  447,  448,  449,
   41,  451,  452,  453,  454,  455,  456,  457,  458,  519,
   41,   42,   43,  433,   45,  433,   47,  320,   41,   42,
   43,   41,   45,  404,   47,  517,  406,  384,  478,  124,
  490,  498,  482,  483,  484,  485,  486,  487,  488,  489,
  490,  491,  492,   41,   41,  124,  496,  497,  498,  499,
  500,  501,  502,  503,  504,  505,  506,  507,  508,  509,
  510,   63,   63,   63,  365,  452,   41,  517,    0,   41,
  520,   41,   42,   43,  478,   45,   42,   47,  432,   44,
   46,   47,  273,  274,  275,  384,  277,  278,  279,  280,
  281,  282,  283,  124,  285,  286,  287,  384,   64,  487,
  499,  124,   44,  294,  508,   40,   40,  436,  299,   41,
   42,   43,   44,   45,  491,   47,  486,  308,  415,  310,
  488,  282,   44,   41,  376,   91,  486,   41,   44,  320,
  377,  322,  323,  324,  277,   41,   41,    0,  259,    0,
  283,  436,  285,  286,  287,  273,  288,  289,  290,  282,
    0,    0,   41,  295,  124,   41,   44,   41,  124,    0,
  302,   93,    0,    0,   41,  407,   93,  310,   93,    0,
   41,   41,   93,   93,   41,  478,    0,  320,  369,   41,
   93,   93,   41,   41,   41,   41,  377,   41,    0,   93,
   93,  382,  124,  384,  385,  386,  387,  388,  389,  390,
  391,  392,  393,  394,  395,  508,   93,   41,   93,  283,
  401,  285,  286,  287,  405,  406,    0,   93,  409,  410,
    0,    0,  406,    0,  366,  367,    0,   93,    0,    0,
  406,   41,   41,  424,   41,   41,  779,  500,   93,  382,
   93,  499,  497,  847, 1212,  436,  320,  438, 1336,  779,
  441,  442,  443,  444, 1407,  446,  447,  448,  449, 1434,
  451,  452,  453,  454,  455,  456,  457,  458,  379, 1455,
 1281, 1413, 1381, 1466,  541,  386,  387,  388,  389,  390,
  391,  392,  393,  394,  395,  440,  305,  478, 1142,    0,
  704,  482,  483,  484,  485,  486,  487,  488,  489,  490,
  491,  492, 1095,  488, 1223,  496,  497,  498,  499,  500,
  501,  502,  503,  504,  505,  506,  507,  508,  509,  510,
 1207,  223, 1362, 1460, 1472,   -1,  517,    0,   -1,  520,
   -1,   42,   43,   -1,   45,  478,   47,   -1,   -1,   -1,
   -1,  273,  274,  275,   -1,  277,  278,  279,  280,  281,
  282,  283,   -1,  285,  286,  287,   -1,   -1,   -1,   -1,
   -1,   -1,  294,   -1,   -1,  508,   -1,  299,   41,   42,
   43,   44,   45,   -1,   47,   -1,  308,   -1,  310,   -1,
   -1,   -1,   93,   -1,   -1,   -1,   -1,   -1,  320,   -1,
  322,  323,  324,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,  478,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,  124,   -1,   -1,   -1,   -1,   -1,   -1,
   93,  387,  388,  389,  390,  391,  392,  393,  394,  395,
   -1,   -1,   -1,   -1,  508,   -1,   -1,  369,   -1,  405,
  406,   -1,   -1,   -1,   -1,  377,   -1,   -1,   -1,   -1,
  382,  124,  384,  385,  386,  387,  388,  389,  390,  391,
  392,  393,  394,  395,   -1,   -1,   -1,   -1,   -1,  401,
   -1,   -1,   -1,  405,  406,   -1,   -1,  409,  410,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,  424,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,  436,   -1,  438,   -1,   -1,  441,
  442,  443,  444,   -1,  446,  447,  448,  449,   -1,  451,
  452,  453,  454,  455,  456,  457,  458,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,  478,   -1,   -1,   -1,
  482,  483,  484,  485,  486,  487,  488,  489,  490,  491,
  492,   -1,   -1,   -1,  496,  497,  498,  499,  500,  501,
  502,  503,  504,  505,  506,  507,  508,  509,  510,   -1,
   -1,   -1,   -1,   -1,   -1,  517,   -1,   -1,  520,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,    0,
  273,  274,  275,   -1,  277,  278,  279,  280,  281,  282,
  283,   -1,  285,  286,  287,   -1,   -1,   -1,   -1,   -1,
   -1,  294,   -1,   -1,   -1,   -1,  299,   -1,    0,   -1,
   -1,   -1,   -1,   -1,   -1,  308,   -1,  310,   -1,   -1,
   41,   42,   43,   44,   45,   46,   47,  320,   -1,  322,
  323,  324,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
    0,   -1,   -1,   64,   -1,   -1,   -1,   -1,   -1,   41,
   -1,   -1,   44,  384,  385,  386,  387,  388,  389,  390,
  391,  392,  393,  394,  395,   -1,   -1,   -1,   -1,   -1,
   91,   -1,   93,   -1,  405,  406,  369,   -1,   -1,   -1,
   -1,   41,   -1,   -1,  377,   -1,   -1,   -1,   -1,  382,
   -1,  384,  385,  386,  387,  388,  389,  390,  391,  392,
  393,  394,  395,  124,   -1,   -1,   -1,   -1,  401,   -1,
   -1,   -1,  405,  406,   -1,   -1,  409,  410,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,  424,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,  436,   -1,  438,   -1,   -1,  441,  442,
  443,  444,   -1,  446,  447,  448,  449,   -1,  451,  452,
  453,  454,  455,  456,  457,  458,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,  478,   -1,   -1,    0,  482,
  483,  484,  485,  486,  487,  488,  489,  490,  491,  492,
   -1,   -1,   -1,  496,  497,  498,  499,  500,  501,  502,
  503,  504,  505,  506,  507,  508,  509,  510,   -1,   -1,
   -1,   -1,   -1,   -1,  517,   -1,   -1,  520,   40,   41,
   42,   43,   44,   45,   -1,   47,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,  273,  274,  275,   -1,  277,  278,  279,  280,
  281,  282,  283,   -1,  285,  286,  287,   -1,   -1,   -1,
   -1,   -1,   -1,  294,   -1,    0,   -1,   -1,   -1,   91,
   -1,   93,  274,  275,   -1,  277,   -1,  308,   -1,  310,
   -1,  283,   -1,  285,  286,  287,   -1,   -1,   -1,  320,
   -1,  322,  323,  324,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,  124,   -1,   -1,  275,   41,  277,  310,   44,
   -1,   -1,   -1,  283,   -1,  285,  286,  287,  320,   -1,
  322,  323,  324,   41,   42,   43,   44,   45,   -1,   47,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,  369,   -1,
  310,   -1,   -1,   -1,   -1,   -1,  377,   -1,   -1,   -1,
  320,  382,   -1,  384,   -1,  386,  387,  388,  389,  390,
  391,  392,  393,  394,  395,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,  405,  406,   -1,   -1,   -1,   -1,
  382,   -1,   -1,  385,   -1,   -1,   -1,   40,    0,   42,
   43,   44,   45,   46,   47,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,  124,  438,   -1,   -1,
   -1,   64,  382,   -1,   -1,  446,  447,  448,  449,   -1,
   -1,  452,  453,  454,  455,  456,  457,  458,  459,   41,
   42,   43,   44,   45,   46,   47,   -1,   -1,   91,    0,
   -1,   -1,   -1,   -1,   -1,   -1,    0,  478,   -1,   -1,
   -1,  273,  274,  275,   -1,  277,  278,  279,  280,  281,
  282,  283,   -1,  285,  286,  287,   -1,   -1,   -1,   -1,
   -1,  124,  294,   -1,   -1,   -1,  478,  508,   -1,   91,
   41,   93,   -1,   44,   -1,   -1,  308,   41,  310,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,  320,   -1,
  322,  323,  324,   -1,   -1,   -1,  508,   -1,  478,   -1,
   -1,   -1,  124,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,  508,  274,
  275,   -1,  277,   -1,   -1,   -1,   -1,  369,  283,   -1,
  285,  286,  287,   -1,   -1,  377,   -1,   -1,   -1,   -1,
  382,   -1,  384,  281,  386,  387,  388,  389,  390,  391,
  392,  393,  394,  395,   -1,  310,  294,   -1,   -1,   -1,
   -1,   -1,   -1,  405,  406,  320,   -1,  322,  323,  324,
  308,   -1,   -1,   -1,   -1,   -1,    0,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,  438,   -1,   -1,   -1,
  273,   -1,   -1,   -1,  446,  447,  448,  449,   -1,  282,
  452,  453,  454,  455,  456,  457,  458,   41,   42,   43,
   44,   45,   -1,   47,   -1,   -1,   -1,  382,   -1,   -1,
  385,  369,   -1,   -1,   -1,   -1,  478,   -1,   -1,  377,
   -1,  273,  274,  275,   -1,  277,  278,  279,  280,  281,
  282,  283,   -1,  285,  286,  287,   -1,   -1,   -1,   -1,
   -1,   -1,  294,   -1,   -1,   -1,  508,   91,   -1,   93,
   -1,   -1,   -1,   -1,   -1,   -1,  308,   -1,  310,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,  320,   -1,
  322,  323,  324,  274,  275,   -1,  277,  278,  279,  280,
  124,  275,  283,  277,  285,  286,  287,   -1,   -1,  283,
   -1,  285,  286,  287,  452,  453,  454,  455,  456,  457,
  458,   -1,   -1,  478,   -1,   -1,   -1,   -1,   -1,  310,
   -1,   -1,   -1,   -1,   -1,   -1,  310,  369,   -1,  320,
   -1,  322,  323,  324,   -1,  377,  320,   -1,   -1,   -1,
  382,   -1,  384,  508,  386,  387,  388,  389,  390,  391,
  392,  393,  394,  395,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,  405,  406,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,    0,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,  382,   -1,   -1,   -1,   -1,  438,   -1,  382,   -1,
   -1,   -1,   -1,   -1,  446,  447,  448,  449,   -1,   -1,
  452,  453,  454,  455,  456,  457,  458,   41,   42,   43,
   44,   45,   -1,   47,   -1,   -1,   -1,    0,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,  478,   -1,   -1,  273,
  274,  275,   -1,  277,  278,  279,  280,  281,  282,  283,
   -1,  285,  286,  287,   -1,   -1,  447,   -1,   -1,   -1,
  294,   -1,   -1,   -1,   -1,   -1,  508,   91,   41,   93,
   -1,   44,   -1,   -1,  308,   -1,  310,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,  320,  478,  322,  323,
  324,   -1,   -1,   -1,  478,   -1,   -1,   -1,   -1,   -1,
  124,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   42,
   43,   -1,   45,   -1,   47,   -1,   -1,  508,   -1,   -1,
   -1,   -1,   -1,   -1,  508,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,  369,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,  377,   -1,   -1,   -1,   -1,  382,   -1,
  384,   -1,  386,  387,  388,  389,  390,  391,  392,  393,
  394,  395,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,  405,  406,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,    0,   -1,   -1,   -1,   -1,   -1,
   -1,  124,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,  438,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,  446,  447,  448,  449,   -1,   -1,  452,  453,
  454,  455,  456,  457,  458,   41,   42,   43,   44,   45,
   -1,   47,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,  478,   -1,   -1,   -1,   -1,  273,
  274,  275,   -1,  277,  278,  279,  280,  281,  282,  283,
   -1,  285,  286,  287,   -1,   -1,   -1,   -1,   -1,   -1,
  294,   -1,   -1,   -1,  508,   -1,   -1,   93,   -1,   -1,
   -1,   -1,   -1,   -1,  308,   -1,  310,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,  320,   -1,  322,  323,
  324,  274,  275,   -1,  277,  278,  279,  280,  124,   -1,
  283,   -1,  285,  286,  287,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,  310,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,  369,   -1,  320,  281,  322,
  323,  324,   -1,  377,   -1,   -1,   -1,   -1,  382,   -1,
  384,  294,  386,  387,  388,  389,  390,  391,  392,  393,
  394,  395,   -1,   -1,   -1,  308,   -1,   -1,   -1,   -1,
   -1,  405,  406,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,    0,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,  382,
   -1,   -1,   -1,   -1,  438,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,  446,  447,  448,  449,   -1,   -1,  452,  453,
  454,  455,  456,  457,  458,   41,  369,   43,   44,   45,
   -1,   -1,   -1,   -1,  377,    0,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,  478,   -1,   -1,  273,  274,  275,
   -1,  277,  278,  279,  280,  281,  282,  283,   -1,  285,
  286,  287,   -1,   -1,  447,   -1,   -1,   -1,  294,   -1,
   -1,   -1,   -1,   -1,  508,   -1,   41,   93,   -1,   44,
   -1,   -1,  308,   -1,  310,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,  320,  478,  322,  323,  324,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,  124,  452,
  453,  454,  455,  456,  457,  458,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,  508,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,  369,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,  377,   -1,   -1,   -1,   -1,  382,   -1,  384,   -1,
  386,  387,  388,  389,  390,  391,  392,  393,  394,  395,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,  405,
  406,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,    0,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,  438,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
  446,  447,  448,  449,   -1,   -1,  452,  453,  454,  455,
  456,  457,  458,   41,   -1,   43,   44,   45,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,  478,   -1,   -1,   -1,   -1,  273,  274,  275,
   -1,  277,  278,  279,  280,  281,  282,  283,   -1,  285,
  286,  287,   -1,   -1,   -1,   -1,   -1,   -1,  294,   -1,
   -1,   -1,  508,   -1,   -1,   93,   -1,   -1,   -1,   -1,
   -1,   -1,  308,   -1,  310,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,  320,   -1,  322,  323,  324,  274,
  275,   -1,  277,  278,  279,  280,  124,   -1,  283,   -1,
  285,  286,  287,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,  310,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,  369,   -1,  320,   -1,  322,  323,  324,
   -1,  377,   -1,   -1,   -1,   -1,  382,   -1,  384,   -1,
  386,  387,  388,  389,  390,  391,  392,  393,  394,  395,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,  405,
  406,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,    0,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,  382,   -1,   -1,
   -1,   -1,  438,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
  446,  447,  448,  449,   -1,   -1,  452,  453,  454,  455,
  456,  457,  458,   41,   -1,   -1,   44,   -1,   -1,   -1,
   -1,   -1,   -1,    0,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,  478,   -1,   -1,  273,  274,  275,   -1,  277,
  278,  279,  280,  281,  282,  283,   -1,  285,  286,  287,
   -1,   -1,  447,   -1,   -1,   -1,  294,   -1,   -1,   -1,
   -1,   -1,  508,   -1,   41,   93,   -1,   44,   -1,   -1,
  308,   -1,  310,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,  320,  478,  322,  323,  324,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,  124,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,  508,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,    0,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,  369,   -1,   -1,   -1,   -1,   -1,   -1,   -1,  377,
   -1,   -1,   -1,   -1,  382,   -1,  384,   -1,  386,  387,
  388,  389,  390,  391,  392,  393,  394,  395,   -1,   -1,
   -1,   -1,   41,   -1,   -1,   44,   -1,  405,  406,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,    0,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
  438,   -1,   -1,   -1,   -1,   -1,   -1,   -1,  446,  447,
  448,  449,   -1,   -1,  452,  453,  454,  455,  456,  457,
  458,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   41,
   -1,   -1,   44,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
  478,   -1,    0,   -1,   -1,  273,  274,  275,   -1,  277,
  278,  279,  280,  281,  282,  283,   -1,  285,  286,  287,
   -1,   -1,   -1,    0,   -1,   -1,  294,   -1,   -1,   -1,
  508,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
  308,   -1,  310,   41,   -1,   -1,   44,   -1,   -1,   -1,
   -1,   -1,  320,   -1,  322,  323,  324,  274,  275,   -1,
  277,  278,  279,  280,   41,   -1,  283,   44,  285,  286,
  287,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,    0,   -1,   -1,
   -1,   -1,   -1,  310,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,  369,   -1,  320,   -1,  322,  323,  324,   -1,  377,
   -1,   -1,   -1,    0,  382,   -1,  384,   -1,  386,  387,
  388,  389,  390,  391,  392,  393,  394,  395,   41,   -1,
   -1,   44,   -1,   -1,   -1,   -1,   -1,  405,  406,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   41,  274,  275,   44,  277,  278,
  279,  280,   -1,   -1,  283,  382,  285,  286,  287,   -1,
  438,   -1,   -1,   -1,   -1,   -1,    0,   -1,  446,  447,
  448,  449,   -1,   -1,  452,  453,  454,  455,  456,  457,
  458,  310,   -1,   -1,   -1,   -1,   -1,   -1,    0,   -1,
   -1,  320,   -1,  322,  323,  324,   -1,   -1,   -1,   -1,
  478,   -1,   -1,   -1,   -1,   -1,   -1,   41,   -1,   -1,
   44,   -1,  274,  275,   -1,  277,  278,  279,  280,   -1,
  447,  283,   -1,  285,  286,  287,   -1,   -1,   -1,   41,
  508,   -1,   44,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,    0,   -1,   -1,   -1,   -1,   -1,  310,   -1,
   -1,  478,   -1,  382,   -1,   -1,   -1,   -1,  320,   -1,
  322,  323,  324,   -1,   -1,   -1,  274,  275,   -1,  277,
  278,  279,  280,   -1,   -1,  283,   -1,  285,  286,  287,
   -1,  508,   -1,   41,   -1,   -1,   44,  274,  275,   -1,
  277,  278,  279,  280,   -1,   -1,  283,    0,  285,  286,
};
}
static void yycheck2() {
yycheck2 = new short[] {
  287,   -1,  310,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,  320,   -1,  322,  323,  324,   -1,  447,   -1,
  382,   -1,   -1,  310,   -1,   -1,   -1,   -1,   -1,    0,
   -1,   -1,   -1,  320,   -1,  322,  323,  324,   41,   -1,
   -1,   44,   -1,   -1,   -1,   -1,   -1,   -1,   -1,  478,
   -1,  274,  275,   -1,  277,  278,  279,  280,   -1,   -1,
  283,   -1,  285,  286,  287,   -1,   -1,   -1,   -1,   -1,
   41,    0,   -1,   44,  382,   -1,   -1,  274,  275,  508,
  277,  278,  279,  280,   -1,  447,  283,  310,  285,  286,
  287,   -1,   -1,   -1,   -1,  382,   -1,  320,   -1,  322,
  323,  324,   -1,    0,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   41,  310,   -1,   44,  478,   -1,   -1,   -1,
   -1,   -1,   -1,  320,   -1,  322,  323,  324,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,  447,
  274,  275,   -1,  277,   41,  279,  508,   44,   -1,  283,
   -1,  285,  286,  287,   -1,   -1,   -1,   -1,   -1,  382,
  447,   -1,  274,  275,   -1,  277,   -1,  279,  280,   -1,
  478,  283,   -1,  285,  286,  287,  310,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,  382,  320,   -1,  322,  323,
  324,  478,   -1,   -1,   -1,   -1,   -1,   -1,  310,   -1,
  508,   -1,   -1,   -1,   -1,   -1,   -1,   -1,  320,   -1,
  322,  323,  324,   -1,   -1,   -1,  274,  275,   -1,  277,
   -1,  508,   -1,   -1,  447,  283,   -1,  285,  286,  287,
   -1,   40,   -1,   42,   43,   -1,   45,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,  382,   -1,
  447,   -1,  310,   -1,   -1,  478,   -1,   -1,   -1,   -1,
   -1,   -1,  320,   -1,  322,  323,  324,   -1,   -1,   -1,
  382,  274,  275,   -1,  277,   -1,   -1,   -1,   -1,   -1,
  283,  478,  285,  286,  287,  508,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,  274,  275,   -1,  277,  310,   -1,   -1,
   -1,  508,  283,  447,  285,  286,  287,  320,   -1,  322,
  323,  324,   -1,   -1,  382,   -1,   -1,  385,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,  447,   -1,   -1,   -1,  310,
   -1,   -1,   -1,   -1,  478,  274,  275,   -1,  277,  320,
   -1,  322,  323,  324,  283,   -1,  285,  286,  287,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,  478,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,  508,   -1,   -1,  274,  275,  382,
  277,  310,  385,   -1,   -1,   -1,  283,   -1,  285,  286,
  287,  320,   -1,  322,  323,  324,  508,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,  382,   -1,  310,  385,   -1,   -1,   -1,   -1,   -1,
  478,   -1,   -1,  320,   -1,  322,  323,  324,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
  508,   -1,   -1,  382,   -1,   -1,  385,   -1,   -1,   -1,
   -1,   -1,   -1,  272,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,  478,   -1,   -1,   -1,  288,
   -1,   40,   -1,   42,   43,  382,   45,   -1,   -1,   -1,
  299,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,  508,   -1,  478,   -1,   -1,
   -1,  320,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,  508,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,  478,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,  370,  371,  372,  373,  374,  375,  376,   -1,  378,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,  386,   -1,  508,
   -1,  478,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
  399,   -1,   -1,   -1,   -1,  404,   -1,   -1,   -1,  408,
  409,  410,   -1,   -1,   -1,   -1,   -1,  416,  417,  418,
   -1,  508,  421,  422,   -1,  424,   -1,  426,   -1,  428,
  429,   -1,   -1,   -1,   -1,   -1,   -1,   -1,  437,   -1,
   -1,   -1,  441,  442,  443,  444,  445,   -1,   -1,   -1,
  449,  450,  451,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,  460,  461,   -1,  463,  464,  465,   -1,  467,  468,
  469,  470,   -1,  472,  473,  474,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,  482,  483,  484,  485,  486,  487,  488,
  489,  490,  491,  492,  493,  494,   -1,  496,  497,  498,
  499,  500,  501,  502,  503,  504,  505,  506,  507,  508,
  509,  510,   -1,  512,  513,  514,  515,  516,  517,  518,
  519,  520,   -1,  272,   -1,   -1,   -1,   -1,   -1,   -1,
   40,   41,   -1,   43,   -1,   45,   -1,   -1,   -1,  288,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
  299,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,  320,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,  370,  371,  372,  373,  374,  375,  376,   -1,  378,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,  386,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
  399,   -1,   -1,   -1,   -1,  404,   -1,   -1,   -1,  408,
  409,  410,   -1,   -1,   -1,   -1,   -1,  416,  417,  418,
   -1,   -1,  421,  422,   -1,  424,   -1,  426,   -1,  428,
  429,   -1,   -1,   -1,   -1,   -1,   -1,   -1,  437,   -1,
   -1,   -1,  441,  442,  443,  444,  445,   -1,   -1,   -1,
  449,  450,  451,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,  460,  461,   -1,  463,  464,  465,   -1,  467,  468,
  469,  470,   -1,  472,  473,  474,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,  482,  483,  484,  485,  486,  487,  488,
  489,  490,  491,  492,  493,  494,   -1,  496,  497,  498,
  499,  500,  501,  502,  503,  504,  505,  506,  507,  508,
  509,  510,  272,  512,  513,  514,  515,  516,  517,  518,
  519,  520,   40,   -1,   42,   43,   -1,   45,  288,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,  299,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
  320,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
  370,  371,  372,  373,  374,  375,  376,   -1,  378,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,  386,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,  399,
   -1,   -1,   -1,   -1,  404,   -1,   -1,   -1,  408,  409,
  410,   -1,   -1,   -1,   -1,   -1,  416,  417,  418,   -1,
   -1,  421,  422,   -1,  424,   -1,  426,   -1,  428,  429,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,  437,   -1,   -1,
   -1,  441,  442,  443,  444,  445,   -1,   -1,   -1,  449,
  450,  451,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
  460,  461,   -1,  463,  464,  465,   -1,  467,  468,  469,
  470,   -1,  472,  473,  474,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,  482,  483,  484,  485,  486,  487,  488,  489,
  490,  491,  492,  493,  494,   -1,  496,  497,  498,  499,
  500,  501,  502,  503,  504,  505,  506,  507,  508,  509,
  510,   -1,  512,  513,  514,  515,  516,  517,  518,  519,
  520,   40,   -1,   -1,   43,   -1,   45,   -1,   -1,   -1,
  288,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,  299,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,  320,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,  370,  371,  372,  373,  374,  375,  376,   -1,
  378,   -1,   -1,   -1,   -1,   -1,   -1,   -1,  386,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,  399,   -1,   -1,   -1,   -1,  404,   -1,   -1,   -1,
  408,  409,  410,   -1,   -1,   -1,   -1,   -1,  416,  417,
  418,   -1,   -1,  421,  422,   -1,  424,   -1,  426,   -1,
  428,  429,   -1,   -1,   -1,   -1,   -1,   -1,   -1,  437,
   -1,   -1,   -1,  441,  442,  443,  444,  445,   -1,   -1,
   -1,  449,  450,  451,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,  460,  461,   -1,  463,  464,  465,   -1,  467,
  468,  469,  470,   -1,  472,  473,  474,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,  482,  483,  484,  485,  486,  487,
  488,  489,  490,  491,  492,  493,  494,   -1,  496,  497,
  498,  499,  500,  501,  502,  503,  504,  505,  506,  507,
  508,  509,  510,   -1,  512,  513,  514,  515,  516,  517,
  518,  519,  520,   -1,   -1,   -1,   -1,   -1,   -1,  288,
   40,   -1,   -1,   43,   -1,   45,   -1,   -1,   -1,   -1,
  299,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,  320,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,  370,  371,  372,  373,  374,  375,  376,   -1,  378,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,  386,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
  399,   -1,   -1,   -1,   -1,  404,   -1,   -1,   -1,  408,
  409,  410,   -1,   -1,   -1,   -1,   -1,  416,  417,  418,
   -1,   -1,  421,  422,   -1,  424,   -1,  426,   -1,  428,
  429,   -1,   -1,   -1,   -1,   -1,   -1,   -1,  437,   -1,
   -1,   -1,  441,  442,  443,  444,  445,  446,   -1,   -1,
  449,  450,  451,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,  460,  461,   -1,  463,  464,  465,   -1,  467,  468,
  469,  470,   -1,  472,  473,  474,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,  482,  483,  484,  485,  486,  487,  488,
  489,  490,  491,  492,  493,  494,   -1,  496,  497,  498,
  499,  500,  501,  502,  503,  504,  505,  506,  507,  508,
  509,  510,   -1,  512,  513,  514,  515,  516,  517,  518,
  519,  520,  272,   -1,   -1,   -1,   -1,   -1,   -1,   40,
   -1,   -1,   43,   -1,   45,   -1,   -1,   -1,  288,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,  299,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
  320,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
  370,  371,  372,  373,  374,  375,  376,   -1,  378,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,  386,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,  399,
   -1,   -1,   -1,   -1,  404,   -1,   -1,   -1,  408,  409,
  410,   -1,   -1,   -1,   -1,   -1,  416,  417,  418,   -1,
   -1,  421,  422,   -1,  424,   -1,  426,   -1,  428,  429,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,  437,   -1,   -1,
   -1,  441,  442,  443,  444,  445,   -1,   -1,   -1,  449,
  450,  451,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
  460,  461,   -1,  463,  464,  465,   -1,  467,  468,  469,
  470,   -1,  472,  473,  474,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,  482,  483,  484,  485,  486,  487,  488,  489,
  490,  491,  492,  493,  494,   -1,  496,  497,  498,  499,
  500,  501,  502,  503,  504,  505,  506,  507,  508,  509,
  510,  272,  512,  513,  514,  515,  516,  517,  518,  519,
  520,   -1,   -1,   -1,   -1,   -1,   -1,  288,   40,   -1,
   -1,   43,   -1,   45,   -1,   -1,   -1,   -1,  299,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,  320,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,  370,
  371,  372,  373,  374,  375,  376,   -1,  378,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,  386,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,  399,   -1,
   -1,   -1,   -1,  404,   -1,   -1,   -1,  408,  409,  410,
   -1,   -1,   -1,   -1,   -1,  416,  417,  418,   -1,   -1,
  421,  422,   -1,  424,   -1,  426,   -1,  428,  429,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,  437,   -1,   -1,   -1,
  441,  442,  443,  444,  445,   -1,   -1,   -1,  449,  450,
  451,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,  460,
  461,   -1,  463,  464,  465,   -1,  467,  468,  469,  470,
   -1,  472,  473,  474,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,  482,  483,  484,  485,  486,  487,  488,  489,  490,
  491,  492,  493,  494,   -1,  496,  497,  498,  499,  500,
  501,  502,  503,  504,  505,  506,  507,  508,  509,  510,
   -1,  512,  513,  514,  515,  516,  517,  518,  519,  520,
  272,   -1,   -1,   -1,   -1,   -1,   -1,   40,   -1,   -1,
   43,   -1,   45,   -1,   -1,   -1,  288,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,  299,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,  320,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,  370,  371,
  372,  373,  374,  375,  376,   -1,  378,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,  386,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,  399,   -1,   -1,
   -1,   -1,  404,   -1,   -1,   -1,  408,  409,  410,   -1,
   -1,   -1,   -1,   -1,  416,  417,  418,   -1,   -1,  421,
  422,   -1,  424,   -1,  426,   -1,  428,  429,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,  437,   -1,   -1,   -1,  441,
  442,  443,  444,  445,   -1,   -1,   -1,  449,  450,  451,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,  460,  461,
   -1,  463,  464,  465,   -1,  467,  468,  469,  470,   -1,
  472,  473,  474,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
  482,  483,  484,  485,  486,  487,  488,  489,  490,  491,
  492,  493,  494,   -1,  496,  497,  498,  499,  500,  501,
  502,  503,  504,  505,  506,  507,  508,  509,  510,  272,
  512,  513,  514,  515,  516,  517,  518,  519,  520,   40,
   -1,   -1,   43,   -1,   45,  288,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,  299,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,  320,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,  370,  371,  372,
  373,  374,  375,  376,   -1,  378,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,  386,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,  399,   -1,   -1,   -1,
   -1,  404,   -1,   -1,   -1,  408,  409,  410,   -1,   -1,
   -1,   -1,   -1,  416,  417,  418,   -1,   -1,  421,  422,
   -1,  424,   -1,  426,   -1,  428,  429,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,  437,   -1,   -1,   -1,  441,  442,
  443,  444,  445,   -1,   -1,   -1,  449,  450,  451,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,  460,  461,   -1,
  463,  464,  465,   -1,  467,  468,  469,  470,   -1,  472,
  473,  474,   -1,   -1,   -1,   -1,   -1,   -1,   -1,  482,
  483,  484,  485,  486,  487,  488,  489,  490,  491,  492,
  493,  494,   -1,  496,  497,  498,  499,  500,  501,  502,
  503,  504,  505,  506,  507,  508,  509,  510,   -1,  512,
  513,  514,  515,  516,  517,  518,  519,  520,   40,   -1,
  281,   43,   -1,   45,   -1,   -1,   -1,  288,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,  299,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,  320,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,  370,
  371,  372,  373,  374,  375,  376,   -1,  378,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,  386,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,  399,   -1,
   -1,   -1,   -1,  404,   -1,   -1,   -1,  408,  409,  410,
   -1,   -1,   -1,   -1,   -1,  416,  417,  418,   -1,   -1,
  421,  422,   -1,  424,   -1,  426,   -1,  428,  429,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,  437,   -1,   -1,   -1,
  441,  442,  443,  444,  445,   -1,   -1,   -1,  449,  450,
  451,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,  460,
  461,   -1,  463,  464,  465,   -1,  467,  468,  469,  470,
   -1,  472,  473,  474,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,  482,  483,  484,  485,  486,  487,  488,  489,  490,
  491,  492,  493,  494,   -1,  496,  497,  498,  499,  500,
  501,  502,  503,  504,  505,  506,  507,  508,  509,  510,
   -1,  512,  513,  514,  515,  516,  517,  518,  519,  520,
   40,   41,   -1,   43,   -1,   45,  288,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,  299,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,  320,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,  370,  371,
  372,  373,  374,  375,  376,   -1,  378,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,  386,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,  399,   -1,   -1,
   -1,   -1,  404,   -1,   -1,   -1,  408,  409,  410,   -1,
   -1,   -1,   -1,   -1,  416,  417,  418,   -1,   -1,  421,
  422,   -1,  424,   -1,  426,  427,  428,  429,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,  437,   -1,   -1,   -1,  441,
  442,  443,  444,  445,   -1,   -1,   -1,  449,  450,  451,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,  460,  461,
   -1,  463,  464,  465,   -1,  467,  468,  469,  470,   -1,
  472,  473,  474,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
  482,  483,  484,  485,  486,  487,  488,  489,  490,  491,
  492,  493,  494,   -1,  496,  497,  498,  499,  500,  501,
  502,  503,  504,  505,  506,  507,  508,  509,  510,   -1,
  512,  513,  514,  515,  516,  517,  518,  519,  520,   40,
   -1,   -1,   43,   -1,   45,   -1,   -1,   -1,  288,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,  299,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
  320,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
  370,  371,  372,  373,  374,  375,  376,   -1,  378,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,  386,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,  399,
   -1,   -1,   -1,   -1,  404,   -1,   -1,   -1,  408,  409,
  410,   -1,   -1,   -1,   -1,   -1,  416,  417,  418,   -1,
   -1,  421,  422,   -1,  424,   -1,  426,   -1,  428,  429,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,  437,   -1,   -1,
   -1,  441,  442,  443,  444,  445,   -1,   -1,   -1,  449,
  450,  451,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
  460,  461,   -1,  463,  464,  465,   -1,  467,  468,  469,
  470,   -1,  472,  473,  474,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,  482,  483,  484,  485,  486,  487,  488,  489,
  490,  491,  492,  493,  494,   -1,  496,  497,  498,  499,
  500,  501,  502,  503,  504,  505,  506,  507,  508,  509,
  510,   -1,  512,  513,  514,  515,  516,  517,  518,  519,
  520,   40,   41,   -1,   43,   -1,   45,  288,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,  299,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,  320,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,  370,
  371,  372,  373,  374,  375,  376,   -1,  378,   -1,   -1,
   -1,   -1,  383,   -1,   -1,  386,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,  399,   -1,
   -1,   -1,   -1,  404,   -1,   -1,   -1,  408,  409,  410,
   -1,   -1,   -1,   -1,   -1,  416,  417,  418,   -1,   -1,
  421,  422,   -1,  424,   -1,  426,   -1,  428,  429,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,  437,   -1,   -1,   -1,
  441,  442,  443,  444,  445,   -1,   -1,   -1,  449,  450,
  451,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,  460,
  461,   -1,  463,  464,  465,   -1,  467,  468,  469,  470,
   -1,  472,  473,  474,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,  482,  483,  484,  485,  486,  487,  488,  489,  490,
  491,  492,  493,  494,   -1,  496,  497,  498,  499,  500,
  501,  502,  503,  504,  505,  506,  507,  508,  509,  510,
   -1,  512,  513,  514,  515,  516,  517,  518,  519,  520,
   40,   -1,   -1,   43,   -1,   45,   -1,   -1,   -1,  288,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
  299,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,  320,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,  370,  371,  372,  373,  374,  375,  376,   -1,  378,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,  386,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
  399,   -1,   -1,   -1,   -1,  404,   -1,   -1,   -1,  408,
  409,  410,   -1,   -1,   -1,   -1,   -1,  416,  417,  418,
   -1,   -1,  421,  422,   -1,  424,   -1,  426,   -1,  428,
  429,   -1,   -1,   -1,   -1,   -1,   -1,   -1,  437,   -1,
   -1,   -1,  441,  442,  443,  444,  445,   -1,   -1,   -1,
  449,  450,  451,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,  460,  461,   -1,  463,  464,  465,   -1,  467,  468,
  469,  470,   -1,  472,  473,  474,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,  482,  483,  484,  485,  486,  487,  488,
  489,  490,  491,  492,  493,  494,   -1,  496,  497,  498,
  499,  500,  501,  502,  503,  504,  505,  506,  507,  508,
  509,  510,   -1,  512,  513,  514,  515,  516,  517,  518,
  519,  520,   40,   -1,   -1,   43,   -1,   45,  288,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,  299,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
  320,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
  370,  371,  372,  373,  374,  375,  376,   -1,  378,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,  386,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,  399,
   -1,   -1,   -1,   -1,  404,   -1,   -1,   -1,  408,  409,
  410,   -1,   -1,   -1,  414,   -1,  416,  417,  418,   -1,
   -1,  421,  422,   -1,  424,   -1,  426,   -1,  428,  429,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,  437,   -1,   -1,
   -1,  441,  442,  443,  444,  445,   -1,   -1,   -1,  449,
  450,  451,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
  460,  461,   -1,  463,  464,  465,   -1,  467,  468,  469,
  470,   -1,  472,  473,  474,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,  482,  483,  484,  485,  486,  487,  488,  489,
  490,  491,  492,  493,  494,   -1,  496,  497,  498,  499,
  500,  501,  502,  503,  504,  505,  506,  507,  508,  509,
  510,   -1,  512,  513,  514,  515,  516,  517,  518,  519,
  520,   40,   -1,   -1,   43,   -1,   45,   -1,   -1,   -1,
  288,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,  299,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,  320,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,  370,  371,  372,  373,  374,  375,  376,   -1,
  378,   -1,   -1,   -1,   -1,   -1,   -1,   -1,  386,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,  399,   -1,   -1,   -1,   -1,  404,   -1,   -1,   -1,
  408,  409,  410,   -1,   -1,   -1,   -1,   -1,  416,  417,
  418,   -1,   -1,  421,  422,   -1,  424,   -1,  426,   -1,
  428,  429,   -1,   -1,   -1,   -1,   -1,   -1,   -1,  437,
   -1,   -1,   -1,  441,  442,  443,  444,  445,   -1,   -1,
   -1,  449,  450,  451,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,  460,  461,   -1,  463,  464,  465,   -1,  467,
  468,  469,  470,   -1,  472,  473,  474,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,  482,  483,  484,  485,  486,  487,
  488,  489,  490,  491,  492,  493,  494,   -1,  496,  497,
  498,  499,  500,  501,  502,  503,  504,  505,  506,  507,
  508,  509,  510,   -1,  512,  513,  514,  515,  516,  517,
  518,  519,  520,   40,   -1,   -1,   43,   -1,   45,  288,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
  299,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,  320,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,  370,  371,  372,  373,  374,  375,  376,   -1,  378,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,  386,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
  399,   -1,   -1,   -1,   -1,  404,   -1,   -1,   -1,  408,
  409,  410,   -1,   -1,   -1,   -1,   -1,  416,  417,  418,
   41,   -1,  421,  422,   -1,  424,   -1,  426,   -1,  428,
  429,   -1,   -1,   -1,   -1,   -1,   -1,   -1,  437,   -1,
   -1,   -1,  441,  442,  443,  444,  445,   -1,   -1,   -1,
  449,  450,  451,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,  460,  461,   -1,  463,  464,  465,   -1,  467,  468,
  469,  470,   -1,  472,  473,  474,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,  482,  483,  484,  485,  486,  487,  488,
  489,  490,  491,  492,  493,  494,   -1,  496,  497,  498,
  499,  500,  501,  502,  503,  504,  505,  506,  507,  508,
  509,  510,   -1,  512,  513,  514,  515,  516,  517,  518,
  519,  520,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,  288,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,  299,   -1,   -1,   -1,   -1,   40,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,  320,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,  370,  371,  372,  373,  374,  375,  376,
   -1,  378,   -1,   -1,   -1,   -1,   -1,   -1,   -1,  386,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,  399,   -1,   -1,   -1,   -1,  404,   -1,   -1,
   -1,  408,  409,  410,   -1,   -1,   -1,   -1,   -1,  416,
  417,  418,   -1,   -1,  421,  422,   -1,  424,   -1,  426,
   -1,  428,  429,   40,   41,   -1,   -1,   44,  299,   46,
  437,   -1,   -1,   -1,  441,  442,  443,  444,  445,   -1,
   -1,   -1,  449,  450,  451,   -1,   -1,   64,   -1,   -1,
   -1,   -1,   -1,  460,  461,   -1,  463,  464,  465,   -1,
  467,  468,  469,  470,   -1,  472,  473,  474,   -1,   -1,
   -1,   -1,   -1,   -1,   91,  482,  483,  484,  485,  486,
  487,  488,  489,  490,  491,  492,  493,  494,   -1,  496,
  497,  498,  499,  500,  501,  502,  503,  504,  505,  506,
  507,  508,  509,  510,   -1,  512,  513,  514,  515,  516,
  517,  518,  519,  520,  257,  386,   -1,   -1,   -1,   -1,
   -1,   -1,  265,  266,   -1,  268,  269,  270,  271,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,  409,  410,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   40,   -1,   -1,  424,   -1,   -1,  299,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
  441,  442,  443,  444,   -1,   -1,   -1,   -1,   -1,   -1,
  451,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,  482,  483,  484,  485,  486,  487,  488,  489,  490,
  491,  492,   -1,   -1,   -1,  496,  497,  498,  499,  500,
  501,  502,  503,  504,  505,  506,  507,  508,  509,  510,
   -1,   -1,   -1,  386,   -1,   -1,  517,   -1,   -1,  520,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,  409,  410,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   40,   -1,   -1,   -1,
   -1,  424,  299,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,  441,  442,
  443,  444,   -1,   -1,   -1,   -1,   -1,   -1,  451,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,  482,
  483,  484,  485,  486,  487,  488,  489,  490,  491,  492,
   -1,   -1,   -1,  496,  497,  498,  499,  500,  501,  502,
  503,  504,  505,  506,  507,  508,  509,  510,   -1,  386,
   -1,   -1,   -1,   -1,  517,   -1,   -1,  520,  268,  269,
  270,  271,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,  409,  410,  284,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   40,   -1,   -1,   -1,  424,   -1,  299,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,  441,  442,  443,  444,   -1,   -1,
   -1,   -1,   -1,   -1,  451,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,  478,   -1,   -1,   -1,  482,  483,  484,  485,  486,
  487,  488,  489,  490,  491,  492,   -1,   -1,   -1,  496,
  497,  498,  499,  500,  501,  502,  503,  504,  505,  506,
  507,  508,  509,  510,   -1,   -1,  386,   -1,   -1,   -1,
  517,   -1,   -1,  520,  268,  269,  270,  271,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,  409,
  410,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   40,
   -1,   -1,   -1,   -1,  424,  299,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,  441,  442,  443,  444,   -1,   -1,   -1,   -1,   -1,
   -1,  451,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,  482,  483,  484,  485,  486,  487,  488,  489,
  490,  491,  492,   -1,   -1,   -1,  496,  497,  498,  499,
  500,  501,  502,  503,  504,  505,  506,  507,  508,  509,
  510,   -1,  386,   -1,   -1,   -1,   -1,  517,   -1,   -1,
  520,  268,  269,  270,  271,   -1,   -1,   -1,   40,   -1,
   -1,   -1,   -1,   -1,   -1,  409,  410,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
  424,   -1,  299,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,  441,  442,  443,
  444,   -1,   -1,   -1,   -1,   -1,   -1,  451,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   40,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,  482,  483,
  484,  485,  486,  487,  488,  489,  490,  491,  492,   -1,
   -1,   -1,  496,  497,  498,  499,  500,  501,  502,  503,
  504,  505,  506,  507,  508,  509,  510,   -1,   -1,  386,
   -1,   -1,   -1,  517,   -1,   -1,  520,  268,  269,  270,
  271,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,  409,  410,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,  424,  299,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,  441,  442,  443,  444,   -1,   -1,
   -1,   -1,   -1,   -1,  451,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   41,   -1,   -1,   44,   -1,
   46,   -1,   -1,   -1,   -1,  482,  483,  484,  485,  486,
  487,  488,  489,  490,  491,  492,   -1,   -1,   64,  496,
  497,  498,  499,  500,  501,  502,  503,  504,  505,  506,
  507,  508,  509,  510,   -1,  386,   -1,   -1,   -1,   -1,
  517,   -1,   -1,  520,   -1,   91,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,  299,  409,  410,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,  424,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
  441,  442,  443,  444,   -1,   -1,   -1,   -1,   -1,   -1,
  451,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,  299,   -1,   -1,
   -1,  482,  483,  484,  485,  486,  487,  488,  489,  490,
  491,  492,   -1,   -1,  386,  496,  497,  498,  499,  500,
  501,  502,  503,  504,  505,  506,  507,  508,  509,  510,
   41,   -1,   -1,   44,   -1,   46,  517,  409,  410,  520,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,  424,   64,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,  441,
  442,  443,  444,   -1,   -1,   -1,   -1,   -1,   -1,  451,
   91,   -1,   -1,   -1,  386,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,  282,  409,  410,   -1,
  482,  483,  484,  485,  486,  487,  488,  489,  490,  491,
  492,   -1,  424,  299,  496,  497,  498,  499,  500,  501,
  502,  503,  504,  505,  506,  507,  508,  509,  510,  441,
  442,  443,  444,   -1,   -1,  517,   -1,   -1,  520,  451,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   41,
   -1,   -1,   44,   -1,   46,   -1,   -1,   -1,   -1,   -1,
  482,  483,  484,  485,  486,  487,  488,  489,  490,  491,
  492,   -1,   64,   -1,  496,  497,  498,  499,  500,  501,
  502,  503,  504,  505,  506,  507,  508,  509,  510,   -1,
  386,   -1,   -1,   -1,   -1,  517,   -1,   -1,  520,   91,
   -1,   -1,   -1,   -1,   -1,  401,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,  409,  410,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,  424,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
  436,   -1,   -1,   -1,   -1,  441,  442,  443,  444,   -1,
   -1,  282,   -1,   -1,   -1,  451,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,  299,   -1,
   -1,   -1,   -1,   -1,   -1,   41,   -1,   -1,   44,   -1,
   46,   -1,  478,   -1,   -1,   -1,  482,  483,  484,  485,
  486,  487,  488,  489,  490,  491,  492,   -1,   64,   -1,
  496,  497,  498,  499,  500,  501,  502,  503,  504,  505,
  506,  507,  508,  509,  510,   -1,   -1,   -1,   -1,   -1,
   -1,  517,   -1,   -1,  520,   91,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,  386,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
  401,   -1,   -1,   -1,   -1,   -1,   -1,   -1,  409,  410,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
  282,   -1,   -1,  424,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,  436,   -1,  299,   -1,   -1,
  441,  442,  443,  444,   41,   -1,   -1,   44,   -1,   46,
  451,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   64,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,  478,   -1,   -1,
   -1,  482,  483,  484,  485,  486,  487,  488,  489,  490,
  491,  492,   -1,   -1,   91,  496,  497,  498,  499,  500,
  501,  502,  503,  504,  505,  506,  507,  508,  509,  510,
   -1,   -1,   -1,   -1,   -1,   -1,  517,   -1,   -1,  520,
   -1,   -1,   -1,   -1,  386,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,  401,
   -1,   -1,   -1,   -1,   -1,   -1,  282,  409,  410,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,  424,  299,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,  436,   -1,   -1,   -1,   -1,  441,
  442,  443,  444,   41,   -1,   -1,   44,   -1,   46,  451,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   64,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,  478,   -1,   -1,   -1,
  482,  483,  484,  485,  486,  487,  488,  489,  490,  491,
  492,   -1,   -1,   91,  496,  497,  498,  499,  500,  501,
  502,  503,  504,  505,  506,  507,  508,  509,  510,   -1,
  386,   -1,   -1,   -1,   -1,  517,   -1,   -1,  520,   -1,
   -1,   -1,   -1,   -1,   -1,  401,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,  409,  410,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,  282,   -1,   -1,  424,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
  436,   -1,  299,   -1,   -1,  441,  442,  443,  444,   41,
   -1,   -1,   44,   -1,   46,  451,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   64,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,  478,   -1,   -1,   -1,  482,  483,  484,  485,
  486,  487,  488,  489,  490,  491,  492,   -1,   -1,   91,
  496,  497,  498,  499,  500,  501,  502,  503,  504,  505,
  506,  507,  508,  509,  510,   -1,   -1,   -1,   -1,   -1,
   -1,  517,   -1,   -1,  520,   -1,   -1,   -1,   -1,  386,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,  401,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,  409,  410,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,  282,   -1,   -1,  424,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,  436,
   -1,  299,   -1,   -1,  441,  442,  443,  444,   41,   -1,
   -1,   44,   -1,   46,  451,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   64,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,  478,   -1,   -1,   -1,  482,  483,  484,  485,  486,
  487,  488,  489,  490,  491,  492,   -1,   -1,   91,  496,
  497,  498,  499,  500,  501,  502,  503,  504,  505,  506,
  507,  508,  509,  510,   -1,   -1,   -1,   -1,   -1,   -1,
  517,   -1,   -1,  520,   -1,   -1,   -1,   -1,  386,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,  401,   -1,   -1,   -1,   -1,   -1,   -1,
  282,  409,  410,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   41,   -1,   -1,   44,   -1,   -1,  424,  299,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,  436,   -1,
   -1,   -1,   -1,  441,  442,  443,  444,   -1,   -1,   -1,
   -1,   -1,   -1,  451,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
  478,   -1,   -1,   -1,  482,  483,  484,  485,  486,  487,
  488,  489,  490,  491,  492,   -1,   -1,   -1,  496,  497,
  498,  499,  500,  501,  502,  503,  504,  505,  506,  507,
  508,  509,  510,   -1,  386,   -1,   -1,   -1,   -1,  517,
   -1,   -1,  520,   -1,   -1,   -1,   -1,   -1,   -1,  401,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,  409,  410,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,  282,
   41,   -1,  424,   44,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,  436,   -1,  299,   -1,   -1,  441,
  442,  443,  444,   -1,   -1,   -1,   -1,   -1,   -1,  451,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,  478,   -1,   -1,   -1,
  482,  483,  484,  485,  486,  487,  488,  489,  490,  491,
  492,   -1,   -1,   -1,  496,  497,  498,  499,  500,  501,
  502,  503,  504,  505,  506,  507,  508,  509,  510,   -1,
   -1,   -1,   -1,   -1,   -1,  517,   -1,   -1,  520,   -1,
   -1,   -1,   -1,  386,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,  282,   -1,   -1,   -1,   -1,   -1,   -1,  401,   41,
   -1,   -1,   44,   -1,   -1,   -1,  409,  410,  299,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,  424,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,  436,   -1,   -1,   -1,   -1,  441,  442,
  443,  444,   -1,   -1,   -1,   -1,   -1,   -1,  451,   91,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,  478,   -1,   -1,   -1,  482,
  483,  484,  485,  486,  487,  488,  489,  490,  491,  492,
   -1,   -1,   -1,  496,  497,  498,  499,  500,  501,  502,
  503,  504,   -1,  506,  507,  508,  509,  510,   41,   -1,
  401,   44,   -1,   -1,  517,   -1,   -1,  520,  409,  410,
   -1,  282,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,  424,   -1,   -1,   -1,   -1,  299,   -1,
   -1,   -1,   -1,   -1,   -1,  436,   -1,   -1,   -1,   -1,
  441,  442,  443,  444,   -1,   -1,   -1,   -1,   91,   -1,
  451,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,  478,   -1,   -1,
   -1,  482,  483,  484,  485,  486,  487,  488,  489,  490,
  491,  492,   -1,   -1,   -1,  496,  497,  498,  499,  500,
  501,  502,  503,  504,  505,  506,  507,  508,  509,  510,
   -1,   -1,   -1,   -1,   -1,  386,  517,   -1,   -1,  520,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   44,   -1,   -1,
  401,   -1,   -1,   -1,   -1,   -1,   -1,   -1,  409,  410,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,  299,   -1,   -1,
   -1,   -1,   -1,  424,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,  436,   -1,   -1,   -1,   -1,
  441,  442,  443,  444,   -1,   -1,   -1,   -1,   -1,   -1,
  451,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,  478,   -1,   -1,
   -1,  482,  483,  484,  485,  486,  487,  488,  489,  490,
  491,  492,   -1,   -1,   -1,  496,  497,  498,  499,  500,
  501,  502,  503,  504,  386,  506,  507,  508,  509,  510,
   -1,   -1,   -1,   -1,   -1,   -1,  517,   -1,   -1,  520,
   -1,   -1,   -1,   -1,   -1,   -1,  299,  409,  410,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,  424,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,  441,
  442,  443,  444,   -1,   -1,   -1,   -1,   -1,   -1,  451,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,  478,   -1,   -1,   -1,
  482,  483,  484,  485,  486,  487,  488,  489,  490,  491,
  492,   -1,   -1,  386,  496,  497,  498,  499,  500,  501,
  502,  503,  504,  505,  506,  507,  508,  509,  510,   -1,
   -1,   -1,   -1,   -1,   -1,  517,  409,  410,  520,   -1,
   -1,  288,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,  424,  299,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,  441,  442,
  443,  444,   -1,  320,   -1,   -1,   -1,   -1,  451,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,  478,   -1,  284,   -1,  482,
  483,  484,  485,  486,  487,  488,  489,  490,  491,  492,
   -1,   -1,  299,  496,  497,  498,  499,  500,  501,  502,
  503,  504,  505,  506,  507,  508,  509,  510,   -1,  386,
   -1,   -1,   -1,   -1,  517,   -1,   -1,  520,   -1,   -1,
   -1,   -1,  399,   -1,   -1,   -1,   -1,  404,   -1,   -1,
   -1,  408,  409,  410,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,  424,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,  441,  442,  443,  444,   -1,   -1,
   -1,   -1,  449,   -1,  451,   -1,   -1,   -1,   -1,  386,
   -1,   -1,   -1,  460,  461,  462,  463,  464,  465,  466,
  467,  468,  469,  470,  471,  472,   -1,   -1,   -1,   -1,
   -1,   -1,  409,  410,   -1,  482,  483,  484,  485,  486,
  487,  488,  489,  490,  491,  492,   -1,  424,   -1,  496,
  497,  498,  499,  500,  501,  502,  503,  504,  505,  506,
  507,  508,  509,  510,  441,  442,  443,  444,  288,   -1,
  517,   -1,   -1,  520,  451,   -1,   -1,   -1,   -1,  299,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
  320,   -1,   -1,   -1,   -1,  482,  483,  484,  485,  486,
  487,  488,  489,  490,  491,  492,   -1,   -1,   -1,  496,
  497,  498,  499,  500,  501,  502,  503,  504,  505,  506,
  507,  508,  509,  510,   -1,   -1,   -1,   -1,   -1,   -1,
  517,   -1,   -1,  520,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,  386,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,  399,
   -1,   -1,   -1,   -1,  404,   -1,   -1,   -1,  408,  409,
  410,   -1,   -1,   -1,  288,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,  424,  299,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,  441,  442,  443,  444,   -1,  320,   -1,   -1,  449,
   -1,  451,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
  460,  461,  462,  463,  464,  465,  466,  467,  468,  469,
  470,  471,  472,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,  482,  483,  484,  485,  486,  487,  488,  489,
  490,  491,  492,   -1,   -1,   -1,  496,  497,  498,  499,
  500,  501,  502,  503,  504,  505,  506,  507,  508,  509,
  510,   -1,  386,   -1,   -1,   -1,   -1,  517,   -1,   -1,
  520,   -1,   -1,   -1,   -1,  399,   -1,   -1,   -1,   -1,
  404,   -1,   -1,   -1,  408,  409,  410,   -1,   -1,   -1,
  288,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
  424,  299,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,  441,  442,  443,
  444,   -1,  320,   -1,   -1,  449,   -1,  451,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,  460,  461,  462,  463,
  464,  465,  466,  467,  468,  469,  470,  471,  472,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,  482,  483,
  484,  485,  486,  487,  488,  489,  490,  491,  492,   -1,
   -1,   -1,  496,  497,  498,  499,  500,  501,  502,  503,
  504,  505,  506,  507,  508,  509,  510,   -1,  386,   -1,
   -1,   -1,   -1,  517,   -1,   -1,  520,   -1,   -1,   -1,
   -1,  399,   -1,   -1,   -1,   -1,  404,   -1,   -1,   -1,
  408,  409,  410,   -1,   -1,   -1,  288,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,  424,  299,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,  441,  442,  443,  444,   -1,  320,   -1,
   -1,  449,   -1,  451,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,  460,  461,   -1,  463,  464,  465,   -1,  467,
  468,   -1,  470,   -1,  472,  473,  474,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,  482,  483,  484,  485,  486,  487,
  488,  489,  490,  491,  492,   -1,   -1,   -1,  496,  497,
  498,  499,  500,  501,  502,  503,  504,  505,  506,  507,
  508,  509,  510,   -1,  386,   -1,   -1,   -1,   -1,  517,
   -1,   -1,  520,   -1,   -1,   -1,   -1,  399,   -1,   -1,
   -1,   -1,  404,   -1,   -1,   -1,  408,  409,  410,   -1,
   -1,   -1,  288,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,  424,  299,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,  441,
  442,  443,  444,   -1,  320,   -1,   -1,  449,   -1,  451,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,  461,
   -1,  463,  464,  465,   -1,  467,  468,   -1,  470,   -1,
  472,  473,  474,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
  482,  483,  484,  485,  486,  487,  488,  489,  490,  491,
  492,   -1,   -1,   -1,  496,  497,  498,  499,  500,  501,
  502,  503,  504,  505,  506,  507,  508,  509,  510,   -1,
  386,   -1,   -1,   -1,   -1,  517,   -1,   -1,  520,   -1,
   -1,   -1,   -1,  399,   -1,   -1,   -1,   -1,  404,   -1,
   -1,   -1,  408,  409,  410,   -1,   -1,   -1,  288,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,  424,  299,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,  441,  442,  443,  444,   -1,
  320,   -1,   -1,  449,   -1,  451,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,  460,  461,   -1,  463,  464,  465,
   -1,  467,  468,   -1,  470,   -1,  472,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,  482,  483,  484,  485,
  486,  487,  488,  489,  490,  491,  492,   -1,   -1,   -1,
  496,  497,  498,  499,  500,  501,  502,  503,  504,  505,
  506,  507,  508,  509,  510,   -1,  386,   -1,   -1,   -1,
   -1,  517,   -1,   -1,  520,   -1,   -1,   -1,   -1,  399,
   -1,   -1,   -1,   -1,  404,   -1,   -1,   -1,  408,  409,
  410,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,  424,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,  441,  442,  443,  444,   -1,   -1,   -1,   -1,  449,
  299,  451,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,  461,   -1,  463,  464,  465,   -1,  467,  468,   -1,
  470,   -1,  472,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,  482,  483,  484,  485,  486,  487,  488,  489,
  490,  491,  492,   -1,   -1,   -1,  496,  497,  498,  499,
  500,  501,  502,  503,  504,  505,  506,  507,  508,  509,
  510,   -1,   -1,   -1,   -1,   -1,   -1,  517,   -1,   -1,
  520,  370,  371,  372,  373,  374,  375,  376,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,  386,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
  409,  410,   -1,   -1,   -1,   -1,   -1,  416,   -1,   -1,
   -1,   -1,  421,  422,   -1,  424,   -1,   -1,   -1,   -1,
   -1,   -1,  299,   -1,   -1,   -1,   -1,   -1,  437,   -1,
   -1,   -1,  441,  442,  443,  444,   -1,   -1,   -1,   -1,
   -1,   -1,  451,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,  482,  483,  484,  485,  486,  487,  488,
  489,  490,  491,  492,   -1,   -1,   -1,  496,  497,  498,
  499,  500,  501,  502,  503,  504,  505,  506,  507,  508,
  509,  510,  379,  380,  381,  382,   -1,   -1,  517,  386,
   -1,  520,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,  299,  409,  410,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,  424,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,  441,  442,  443,  444,   -1,   -1,
   -1,   -1,   -1,   -1,  451,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,  482,  483,  484,  485,  486,
  487,  488,  489,  490,  491,  492,   -1,   -1,  386,  496,
  497,  498,  499,  500,  501,  502,  503,  504,  505,  506,
  507,  508,  509,  510,   -1,   -1,   -1,   -1,   -1,   -1,
  517,  409,  410,  520,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,  424,   -1,  299,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,  441,  442,  443,  444,   -1,   -1,   -1,
   -1,   -1,   -1,  451,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,  473,  474,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,  482,  483,  484,  485,  486,  487,
  488,  489,  490,  491,  492,   -1,   -1,   -1,  496,  497,
  498,  499,  500,  501,  502,  503,  504,  505,  506,  507,
  508,  509,  510,   -1,  512,  386,   -1,   -1,   -1,  517,
   -1,   -1,  520,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,  299,  409,  410,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,  424,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
  441,  442,  443,  444,   -1,   -1,   -1,   -1,   -1,   -1,
  451,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,  473,  474,   -1,   -1,   -1,  299,   -1,   -1,
   -1,  482,  483,  484,  485,  486,  487,  488,  489,  490,
  491,  492,   -1,   -1,  386,  496,  497,  498,  499,  500,
  501,  502,  503,  504,  505,  506,  507,  508,  509,  510,
   -1,   -1,   -1,   -1,   -1,   -1,  517,  409,  410,  520,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,  424,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,  299,   -1,  441,
  442,  443,  444,   -1,   -1,   -1,   -1,   -1,   -1,  451,
   -1,   -1,   -1,   -1,  386,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,  478,  409,  410,   -1,
  482,  483,  484,  485,  486,  487,  488,  489,  490,  491,
  492,   -1,  424,   -1,  496,  497,  498,  499,  500,  501,
  502,  503,  504,  505,  506,  507,  508,  509,  510,  441,
  442,  443,  444,   -1,   -1,  517,   -1,   -1,  520,  451,
   -1,   -1,   -1,   -1,  386,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,  473,   -1,   -1,   -1,   -1,  299,  409,  410,   -1,
  482,  483,  484,  485,  486,  487,  488,  489,  490,  491,
  492,   -1,  424,   -1,  496,  497,  498,  499,  500,  501,
  502,  503,  504,  505,  506,  507,  508,  509,  510,  441,
  442,  443,  444,   -1,   -1,  517,   -1,   -1,  520,  451,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,  473,   -1,   -1,   -1,   -1,  299,   -1,   -1,   -1,
  482,  483,  484,  485,  486,  487,  488,  489,  490,  491,
  492,   -1,   -1,  386,  496,  497,  498,  499,  500,  501,
  502,  503,  504,  505,  506,  507,  508,  509,  510,   -1,
   -1,   -1,   -1,   -1,   -1,  517,  409,  410,  520,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,  424,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,  299,   -1,  441,  442,
  443,  444,   -1,   -1,   -1,   -1,   -1,   -1,  451,   -1,
   -1,   -1,   -1,  386,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
  473,   -1,   -1,   -1,   -1,   -1,  409,  410,   -1,  482,
  483,  484,  485,  486,  487,  488,  489,  490,  491,  492,
   -1,  424,   -1,  496,  497,  498,  499,  500,  501,  502,
  503,  504,  505,  506,  507,  508,  509,  510,  441,  442,
  443,  444,   -1,   -1,  517,   -1,   -1,  520,  451,   -1,
   -1,   -1,   -1,  386,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
  473,   -1,   -1,   -1,   -1,   -1,  409,  410,   -1,  482,
  483,  484,  485,  486,  487,  488,  489,  490,  491,  492,
   -1,  424,   -1,  496,  497,  498,  499,  500,  501,  502,
  503,  504,  505,  506,  507,  508,  509,  510,  441,  442,
  443,  444,   -1,   -1,  517,   -1,   -1,  520,  451,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,  482,
  483,  484,  485,  486,  487,  488,  489,  490,  491,  492,
   -1,   -1,   -1,  496,  497,  498,  499,  500,  501,  502,
  503,  504,  505,  506,  507,  508,  509,  510,   -1,   -1,
   -1,   -1,   -1,   -1,  517,   -1,   -1,  520,
};
}
final static short YYFINAL=47;
final static short YYMAXTOKEN=521;
final static String yyname[] = {
"end-of-file",null,null,null,null,null,null,null,null,null,null,null,null,null,
null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,
null,null,null,null,null,null,null,null,null,null,"'('","')'","'*'","'+'","','",
"'-'","'.'","'/'",null,null,null,null,null,null,null,null,null,null,null,null,
null,null,null,"'?'","'@'",null,null,null,null,null,null,null,null,null,null,
null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,
"'['",null,"']'",null,null,null,null,null,null,null,null,null,null,null,null,
null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,
null,null,"'|'",null,null,null,null,null,null,null,null,null,null,null,null,
null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,
null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,
null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,
null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,
null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,
null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,
null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,
null,null,null,null,null,null,null,null,"RW_REGISTER","RW_STREAM","RW_RELATION",
"RW_SYNONYM","RW_EXTERNAL","RW_VIEW","RW_FUNCTION","RW_QUERY","RW_ALTER",
"RW_DROP","RW_WINDOW","RW_ISTREAM","RW_DSTREAM","RW_RSTREAM","RW_SELECT",
"RW_DISTINCT","RW_FROM","RW_WHERE","RW_GROUP","RW_BY","RW_HAVING","RW_AND",
"RW_OR","RW_XOR","RW_NOT","RW_AS","RW_UNION","RW_ALL","RW_EXCEPT","RW_MINUS",
"RW_INTERSECT","RW_START","RW_STOP","RW_ADD","RW_DEST","RW_SOURCE","RW_PUSH",
"RW_LIKE","RW_SET","RW_SILENT","RW_TS","RW_APP","URW_SYSTEM","RW_HEARTBEAT",
"RW_TIMEOUT","RW_REMOVE","RW_RUN","RW_RUNTIME","RW_THREADED","RW_SCHEDNAME",
"RW_TIMESLICE","RW_BETWEEN","RW_NULLS","RW_ORDER","RW_ASC","RW_DESC",
"RW_DERIVED","RW_FOR","RW_LOGGING","RW_DUMP","RW_IDENTIFIED","RW_LEVEL",
"RW_TYPE","RW_EVENT","RW_CLEAR","RW_LEFT","RW_RIGHT","RW_FULL","RW_OUTER",
"RW_JOIN","RW_SYSTEMSTATE","RW_OPERATOR","RW_QUEUE","RW_STORE","RW_SYNOPSIS",
"RW_INDEX","RW_METADATA_QUERY","RW_METADATA_TABLE","RW_METADATA_WINDOW",
"RW_METADATA_USERFUNC","RW_METADATA_VIEW","RW_METADATA_SYSTEM",
"RW_METADATA_SYNONYM","RW_STORAGE","RW_SPILL","RW_BINJOIN","RW_BINSTREAMJOIN",
"RW_GROUPAGGR","RW_OUTPUT","RW_PARTITIONWINDOW","RW_PATTERNSTRM",
"RW_PATTERNSTRMB","RW_PROJECT","RW_RANGEWINDOW","RW_RELSOURCE","RW_ROWWINDOW",
"RW_SINK","RW_STREAMSOURCE","RW_VIEWRELNSRC","RW_VIEWSTRMSRC","RW_ORDERBY",
"RW_ORDERBYTOP","RW_DIFFERENCE","RW_LINEAGE","RW_PARTNWINDOW","RW_REL","RW_WIN",
"RW_BIND","RW_DURATION","RW_ENABLE","RW_DISABLE","RW_MONITORING","RW_IN",
"RW_AVG","RW_MIN","RW_MAX","RW_COUNT","RW_SUM","RW_FIRST","RW_LAST","RW_IS",
"RW_NULL","RW_ROWS","RW_RANGE","RW_NOW","RW_PARTITION","RW_UNBOUNDED",
"RW_SLIDE","RW_ON","URW_UNIT","RW_NANOSECOND","RW_MICROSECOND","RW_MILLISECOND",
"RW_SECOND","RW_MINUTE","RW_HOUR","RW_DAY","RW_YEAR","RW_MONTH","RW_TO",
"RW_RETURN","RW_LANGUAGE","RW_JAVA","RW_IMPLEMENT","RW_AGGREGATE","RW_USING",
"RW_MATCH_RECOGNIZE","RW_PATTERN","RW_SUBSET","RW_DEFINE","RW_MEASURES",
"RW_MATCHES","URW_WITHIN","URW_INCLUSIVE","RW_INCLUDE","RW_TIMER","RW_EVENTS",
"RW_MULTIPLES","RW_OF","RW_PREV","RW_XMLPARSE","RW_XMLCONCAT","RW_XMLCOMMENT",
"RW_XMLCDATA","RW_XMLQUERY","RW_XMLEXISTS","RW_XMLTABLE","URW_XMLNAMESPACES",
"RW_DEFAULT","RW_XMLELEMENT","RW_XMLATTRIBUTES","RW_XMLFOREST",
"RW_XMLCOLATTVAL","RW_PASSING","RW_VALUE","RW_COLUMNS","RW_XMLDATA",
"RW_RETURNING","RW_CONTENT","RW_PATH","RW_XMLAGG","RW_WELLFORMED","RW_DOCUMENT",
"RW_EVALNAME","URW_ORDERING","URW_TOTAL","URW_DEGREE","URW_PARALLELISM",
"RW_CASE","RW_WHEN","RW_THEN","RW_ELSE","RW_END","RW_DECODE","URW_THRESHOLD",
"T_EQ","T_LT","T_LE","T_GT","T_GE","T_NE","T_JPLUS","T_DOTSTAR","T_CHARAT",
"RW_INTEGER","RW_BIGINT","RW_FLOAT","RW_DOUBLE","RW_NUMBER","RW_CHAR","RW_BYTE",
"RW_TIMESTAMP","RW_INTERVAL","RW_BOOLEAN","RW_XMLTYPE","RW_OBJECT",
"RW_ELEMENT_TIME","RW_QUERY_ID","RW_TRUSTED","RW_CALLOUT","RW_CONSTRAINT",
"RW_PRIMARY","RW_KEY","RW_UPDATE","RW_SEMANTICS","URW_ARCHIVED","URW_ARCHIVER",
"URW_ENTITY","URW_STARTTIME","URW_IDENTIFIER","URW_WORKER","URW_TRANSACTION",
"URW_DIMENSION","URW_COLUMN","URW_REPLAY","URW_PROPAGATE","RW_TRUE","RW_FALSE",
"RW_BATCH","URW_NAME","URW_SUPPORTS","URW_INCREMENTAL","URW_COMPUTATION",
"URW_USE","URW_INSTANCE","URW_TABLE","URW_CURRENTHOUR","URW_CURRENTPERIOD",
"URW_WITH","URW_LOCAL","URW_ZONE","URW_EVALUATE","URW_EVERY","URW_COALESCE",
"NOTOKEN","T_INT","T_BIGINT","T_DOUBLE","T_FLOAT","T_NUMBER","T_STRING",
"T_SQSTRING","T_QSTRING","T_UPPER_LETTER","UNARYPREC",
};
final static String yyrule[] = {
"$accept : start",
"start : command",
"command : query",
"command : registerstream",
"command : registerrelation",
"command : registerarchivedview",
"command : registerview",
"command : view_ordering_constraint",
"command : registerfunction",
"command : registerwindow",
"command : registeraggrfunction",
"command : registerquery",
"command : registersynonym",
"command : startquery",
"command : stopquery",
"command : setquerystarttime",
"command : addquerydest",
"command : dropquery",
"command : dropfunction",
"command : dropwindow",
"command : droprelorstream",
"command : dropsynonym",
"command : addtablesource",
"command : setparallelismdegree",
"command : alterhbtimeout",
"command : alter_external_relation",
"command : dropview",
"command : setsystempars",
"command : table_monitoring",
"command : query_monitoring",
"command : query_ordering_constraints",
"replay_spec : RW_LAST intToken time_unit",
"replay_spec : RW_LAST intToken RW_ROWS",
"ts_type :",
"ts_type : RW_IS URW_SYSTEM RW_TS",
"ts_type : RW_IS RW_APP RW_TS",
"registerstream : RW_REGISTER RW_STREAM identifier '(' non_mt_attrspec_list ')' ts_type",
"registerstream : RW_REGISTER RW_PARTITION RW_STREAM identifier '(' non_mt_attrspec_list ')' ts_type",
"registerstream : RW_REGISTER RW_STREAM identifier '(' non_mt_attrspec_list ')' RW_DERIVED RW_TS arith_expr",
"registerstream : RW_REGISTER RW_PARTITION RW_STREAM identifier '(' non_mt_attrspec_list ')' RW_DERIVED RW_TS arith_expr",
"registerstream : RW_REGISTER URW_ARCHIVED RW_STREAM identifier '(' non_mt_attrspec_list ')' URW_ARCHIVER extensible_qualified_identifier URW_ENTITY T_QSTRING RW_TIMESTAMP URW_COLUMN identifier URW_REPLAY replay_spec worker_identifier_clause txn_identifier_clause ts_type",
"registerrelation : RW_REGISTER RW_RELATION identifier '(' non_mt_relation_attrspec_list ')' ts_type",
"registerrelation : RW_REGISTER RW_EXTERNAL RW_RELATION identifier '(' non_mt_relation_attrspec_list ')'",
"registerrelation : RW_REGISTER RW_RELATION identifier '(' non_mt_relation_attrspec_list ')' RW_IS RW_SILENT",
"registerrelation : RW_REGISTER URW_ARCHIVED dimension_clause RW_RELATION identifier '(' non_mt_relation_attrspec_list ')' URW_ARCHIVER extensible_qualified_identifier URW_ENTITY T_QSTRING event_identifier_clause worker_identifier_clause txn_identifier_clause ts_type",
"dimension_clause :",
"dimension_clause : URW_DIMENSION",
"event_identifier_clause :",
"event_identifier_clause : RW_EVENT URW_IDENTIFIER identifier",
"worker_identifier_clause :",
"worker_identifier_clause : URW_WORKER URW_IDENTIFIER identifier",
"txn_identifier_clause :",
"txn_identifier_clause : URW_TRANSACTION URW_IDENTIFIER identifier",
"$$1 :",
"$$2 :",
"registerview : view_description RW_AS $$1 query $$2",
"registerview : view_description RW_AS query_ref",
"view_description : RW_REGISTER RW_VIEW identifier '(' non_mt_attrspec_list ')'",
"view_description : RW_REGISTER RW_VIEW identifier '(' non_mt_attrname_list ')'",
"view_description : RW_REGISTER RW_VIEW identifier",
"registerarchivedview : archived_view_query_description event_identifier_clause",
"$$3 :",
"$$4 :",
"archived_view_query_description : archived_view_schema_description RW_AS $$3 query $$4",
"archived_view_schema_description : RW_REGISTER URW_ARCHIVED RW_VIEW identifier '(' non_mt_attrspec_list ')'",
"view_ordering_constraint : RW_ALTER RW_VIEW identifier RW_SET URW_ORDERING RW_CONSTRAINT URW_TOTAL RW_ORDER",
"view_ordering_constraint : RW_ALTER RW_VIEW identifier RW_SET URW_ORDERING RW_CONSTRAINT RW_PARTITION RW_ORDER RW_ON arith_expr",
"view_ordering_constraint : RW_ALTER RW_VIEW identifier RW_REMOVE URW_ORDERING RW_CONSTRAINT",
"registersynonym : RW_REGISTER RW_SYNONYM identifier RW_FOR RW_TYPE extensible_qualified_identifier",
"registerfunction : RW_REGISTER RW_FUNCTION builtin_func '(' paramspec ')' RW_RETURN datatype RW_AS RW_LANGUAGE RW_JAVA URW_NAME qstringToken",
"registerfunction : RW_REGISTER RW_FUNCTION identifier RW_RETURN datatype RW_AS RW_LANGUAGE RW_JAVA URW_NAME qstringToken",
"registerfunction : RW_REGISTER RW_FUNCTION identifier RW_RETURN datatype RW_AS RW_LANGUAGE RW_JAVA URW_INSTANCE qstringToken",
"registerfunction : RW_REGISTER RW_FUNCTION identifier '(' paramspec ')' RW_RETURN datatype RW_AS RW_LANGUAGE RW_JAVA URW_NAME qstringToken",
"registerfunction : RW_REGISTER RW_FUNCTION identifier '(' paramspec ')' RW_RETURN datatype RW_AS RW_LANGUAGE RW_JAVA URW_INSTANCE qstringToken",
"registerfunction : RW_REGISTER RW_FUNCTION identifier '(' multi_paramspec_list ')' RW_RETURN datatype RW_AS RW_LANGUAGE RW_JAVA URW_NAME qstringToken",
"registerfunction : RW_REGISTER RW_FUNCTION identifier '(' multi_paramspec_list ')' RW_RETURN datatype RW_AS RW_LANGUAGE RW_JAVA URW_INSTANCE qstringToken",
"registerfunction : RW_REGISTER RW_FUNCTION RW_PREV '(' paramspec ')' RW_RETURN datatype RW_AS RW_LANGUAGE RW_JAVA URW_NAME qstringToken",
"registerfunction : RW_REGISTER RW_FUNCTION RW_PREV '(' multi_paramspec_list ')' RW_RETURN datatype RW_AS RW_LANGUAGE RW_JAVA URW_NAME qstringToken",
"registerwindow : RW_REGISTER RW_WINDOW identifier RW_IMPLEMENT RW_USING qstringToken",
"registerwindow : RW_REGISTER RW_WINDOW identifier '(' paramspec ')' RW_IMPLEMENT RW_USING qstringToken",
"registerwindow : RW_REGISTER RW_WINDOW identifier '(' multi_paramspec_list ')' RW_IMPLEMENT RW_USING qstringToken",
"registeraggrfunction : RW_REGISTER RW_FUNCTION builtin_aggr '(' paramspec ')' RW_RETURN datatype RW_AGGREGATE RW_USING qstringToken",
"registeraggrfunction : RW_REGISTER RW_FUNCTION builtin_aggr_incr '(' paramspec ')' RW_RETURN datatype RW_AGGREGATE RW_USING qstringToken incremental_clause",
"registeraggrfunction : RW_REGISTER RW_FUNCTION extended_builtin_aggr '(' multi_paramspec_list ')' RW_RETURN datatype RW_AGGREGATE RW_USING qstringToken",
"registeraggrfunction : RW_REGISTER RW_FUNCTION identifier '(' paramspec ')' RW_RETURN datatype RW_AGGREGATE RW_USING qstringToken",
"registeraggrfunction : RW_REGISTER RW_FUNCTION identifier '(' paramspec ')' RW_RETURN datatype RW_AGGREGATE RW_USING URW_INSTANCE qstringToken",
"registeraggrfunction : RW_REGISTER RW_FUNCTION identifier '(' paramspec ')' RW_RETURN datatype RW_AGGREGATE RW_USING qstringToken incremental_clause",
"registeraggrfunction : RW_REGISTER RW_FUNCTION identifier '(' paramspec ')' RW_RETURN datatype RW_AGGREGATE RW_USING URW_INSTANCE qstringToken incremental_clause",
"registeraggrfunction : RW_REGISTER RW_FUNCTION identifier '(' multi_paramspec_list ')' RW_RETURN datatype RW_AGGREGATE RW_USING qstringToken",
"registeraggrfunction : RW_REGISTER RW_FUNCTION identifier '(' multi_paramspec_list ')' RW_RETURN datatype RW_AGGREGATE RW_USING URW_INSTANCE qstringToken",
"registeraggrfunction : RW_REGISTER RW_FUNCTION identifier '(' multi_paramspec_list ')' RW_RETURN datatype RW_AGGREGATE RW_USING qstringToken incremental_clause",
"registeraggrfunction : RW_REGISTER RW_FUNCTION identifier '(' multi_paramspec_list ')' RW_RETURN datatype RW_AGGREGATE RW_USING URW_INSTANCE qstringToken incremental_clause",
"dropfunction : RW_DROP RW_FUNCTION identifier '(' datatype_list ')'",
"dropfunction : RW_DROP RW_FUNCTION identifier '(' multi_datatype_list ')'",
"dropfunction : RW_DROP RW_FUNCTION identifier",
"dropwindow : RW_DROP RW_WINDOW identifier",
"dropsynonym : RW_DROP RW_SYNONYM identifier",
"registerquery : RW_REGISTER RW_QUERY identifier RW_AS named_query",
"registerquery : RW_REGISTER RW_QUERY identifier RW_AS named_query out_of_line_constraint",
"opt_evaluate_clause :",
"opt_evaluate_clause : URW_EVALUATE URW_EVERY time_spec_with_timeunit",
"opt_evaluate_clause : URW_EVALUATE URW_EVERY const_bigint",
"named_query : query",
"startquery : RW_ALTER query_ref RW_START",
"stopquery : RW_ALTER query_ref RW_STOP",
"setquerystarttime : RW_ALTER query_ref RW_SET URW_STARTTIME const_bigint",
"addquerydest : RW_ALTER query_ref RW_ADD RW_DEST qstringToken",
"addquerydest : RW_ALTER query_ref RW_ADD RW_DEST qstringToken querydestproperties",
"querydestproperties : URW_USE RW_UPDATE RW_SEMANTICS",
"querydestproperties : RW_BATCH RW_OUTPUT",
"querydestproperties : URW_PROPAGATE RW_HEARTBEAT",
"querydestproperties : RW_BATCH RW_OUTPUT ',' URW_PROPAGATE RW_HEARTBEAT",
"querydestproperties : RW_BATCH RW_OUTPUT ',' URW_USE RW_UPDATE RW_SEMANTICS",
"dropquery : RW_DROP query_ref",
"query_ref : RW_QUERY identifier",
"dropview : RW_DROP RW_VIEW identifier",
"droprelorstream : RW_DROP RW_RELATION identifier",
"droprelorstream : RW_DROP RW_STREAM identifier",
"addtablesource : RW_ALTER RW_RELATION identifier RW_ADD RW_SOURCE qstringToken",
"addtablesource : RW_ALTER RW_STREAM identifier RW_ADD RW_SOURCE qstringToken",
"addtablesource : RW_ALTER RW_RELATION identifier RW_ADD RW_SOURCE RW_PUSH",
"addtablesource : RW_ALTER RW_STREAM identifier RW_ADD RW_SOURCE RW_PUSH",
"addtablesource : RW_ALTER RW_RELATION identifier RW_ADD RW_PUSH RW_SOURCE qstringToken",
"addtablesource : RW_ALTER RW_STREAM identifier RW_ADD RW_PUSH RW_SOURCE qstringToken",
"setparallelismdegree : RW_ALTER RW_STREAM identifier RW_SET URW_DEGREE RW_OF URW_PARALLELISM T_EQ T_INT",
"setparallelismdegree : RW_ALTER RW_RELATION identifier RW_SET URW_DEGREE RW_OF URW_PARALLELISM T_EQ T_INT",
"alterhbtimeout : RW_ALTER RW_STREAM identifier RW_SET RW_HEARTBEAT RW_TIMEOUT time_spec",
"alterhbtimeout : RW_ALTER RW_RELATION identifier RW_SET RW_HEARTBEAT RW_TIMEOUT time_spec",
"alterhbtimeout : RW_ALTER RW_STREAM identifier RW_REMOVE RW_HEARTBEAT RW_TIMEOUT",
"alterhbtimeout : RW_ALTER RW_RELATION identifier RW_REMOVE RW_HEARTBEAT RW_TIMEOUT",
"alter_external_relation : RW_ALTER RW_EXTERNAL RW_RELATION identifier RW_SET RW_EXTERNAL RW_ROWS URW_THRESHOLD bigIntToken",
"query_monitoring : RW_ALTER query_ref RW_ENABLE RW_MONITORING",
"query_monitoring : RW_ALTER query_ref RW_ENABLE RW_MONITORING URW_USE RW_JAVA RW_MILLISECOND",
"query_monitoring : RW_ALTER query_ref RW_ENABLE RW_MONITORING URW_USE RW_JAVA RW_NANOSECOND",
"query_monitoring : RW_ALTER query_ref RW_DISABLE RW_MONITORING",
"query_ordering_constraints : RW_ALTER query_ref RW_SET URW_ORDERING RW_CONSTRAINT URW_TOTAL RW_ORDER",
"query_ordering_constraints : RW_ALTER query_ref RW_SET URW_ORDERING RW_CONSTRAINT RW_PARTITION RW_ORDER RW_ON arith_expr",
"query_ordering_constraints : RW_ALTER query_ref RW_REMOVE URW_ORDERING RW_CONSTRAINT",
"table_monitoring : RW_ALTER RW_STREAM identifier RW_ENABLE RW_MONITORING",
"table_monitoring : RW_ALTER RW_STREAM identifier RW_ENABLE RW_MONITORING URW_USE RW_JAVA RW_MILLISECOND",
"table_monitoring : RW_ALTER RW_STREAM identifier RW_ENABLE RW_MONITORING URW_USE RW_JAVA RW_NANOSECOND",
"table_monitoring : RW_ALTER RW_STREAM identifier RW_DISABLE RW_MONITORING",
"setsystempars : RW_ALTER URW_SYSTEM RW_RUNTIME T_EQ const_int",
"setsystempars : RW_ALTER URW_SYSTEM RW_THREADED T_EQ const_int",
"setsystempars : RW_ALTER URW_SYSTEM RW_SCHEDNAME T_EQ const_string",
"setsystempars : RW_ALTER URW_SYSTEM RW_TIMESLICE T_EQ const_int",
"setsystempars : RW_ALTER URW_SYSTEM RW_RUN",
"setsystempars : RW_ALTER URW_SYSTEM RW_RUN RW_DURATION T_EQ const_int",
"setsystempars : RW_ALTER URW_SYSTEM RW_START RW_TRUSTED RW_CALLOUT const_string",
"incremental_clause : URW_SUPPORTS URW_INCREMENTAL URW_COMPUTATION",
"multi_paramspec_list : paramspec ',' multi_paramspec_list",
"multi_paramspec_list : paramspec ',' paramspec",
"paramspec : identifier datatype",
"multi_datatype_list : datatype_list ',' multi_datatype_list",
"multi_datatype_list : datatype_list ',' datatype_list",
"datatype_list : datatype",
"non_mt_attrspec_list : attrspec ',' non_mt_attrspec_list",
"non_mt_attrspec_list : attrspec",
"non_mt_relation_attrspec_list : non_mt_attrspec_list attrspec ',' out_of_line_constraint",
"non_mt_relation_attrspec_list : out_of_line_constraint ',' non_mt_attrspec_list",
"non_mt_relation_attrspec_list : non_mt_attrspec_list attrspec ',' out_of_line_constraint ',' non_mt_attrspec_list",
"non_mt_relation_attrspec_list : attrspec inline_constraint ',' non_mt_attrspec_list",
"non_mt_relation_attrspec_list : non_mt_attrspec_list attrspec ',' attrspec inline_constraint",
"non_mt_relation_attrspec_list : non_mt_attrspec_list attrspec ',' attrspec inline_constraint ',' non_mt_attrspec_list",
"non_mt_relation_attrspec_list : attrspec ',' attrspec inline_constraint ',' non_mt_attrspec_list",
"non_mt_relation_attrspec_list : attrspec inline_constraint",
"non_mt_relation_attrspec_list : attrspec ',' out_of_line_constraint",
"non_mt_relation_attrspec_list : non_mt_attrspec_list",
"non_mt_attrname_list : identifier ',' non_mt_attrname_list",
"non_mt_attrname_list : identifier",
"attrspec : identifier fixed_length_datatype",
"attrspec : identifier variable_length_datatype '(' intToken ')'",
"attrspec : identifier extensible_qualified_identifier '[' ']'",
"attrspec : identifier numberToken '(' intToken ',' intToken ')'",
"inline_constraint : RW_PRIMARY RW_KEY",
"out_of_line_constraint : RW_PRIMARY RW_KEY '(' non_mt_attrname_list ')'",
"query : sfw_block_n",
"query : idstream_clause '(' sfw_block_n ')' using_clause",
"query : RW_RSTREAM '(' sfw_block_n ')'",
"query : binary_n",
"query : nary_n",
"query : idstream_clause '(' binary_n ')' using_clause",
"query : idstream_clause '(' nary_n ')' using_clause",
"query : RW_RSTREAM '(' binary_n ')'",
"query : RW_RSTREAM '(' nary_n ')'",
"idstream_clause : RW_ISTREAM",
"idstream_clause : RW_DSTREAM",
"sfw_block : select_clause from_clause opt_where_clause opt_group_by_clause opt_having_clause opt_order_by_clauses",
"sfw_block_n : sfw_block opt_evaluate_clause",
"select_clause : RW_SELECT RW_DISTINCT non_mt_projterm_list",
"select_clause : RW_SELECT non_mt_projterm_list",
"select_clause : RW_SELECT RW_DISTINCT '*'",
"select_clause : RW_SELECT '*'",
"from_clause : RW_FROM non_mt_relation_list",
"opt_order_by_clauses :",
"opt_order_by_clauses : order_by_clause",
"opt_order_by_clauses : order_by_top_clause",
"order_by_clause : RW_ORDER RW_BY order_by_list",
"order_by_top_clause : RW_ORDER RW_BY order_by_list RW_ROWS intToken",
"order_by_top_clause : partition_clause RW_ORDER RW_BY order_by_list RW_ROWS intToken",
"order_by_top_clause : RW_ORDER RW_BY order_by_list RW_ROWS intToken partition_clause",
"opt_where_clause :",
"opt_where_clause : RW_WHERE non_mt_cond_list",
"opt_group_by_clause :",
"opt_group_by_clause : RW_GROUP RW_BY arith_expr_list",
"opt_having_clause :",
"opt_having_clause : RW_HAVING non_mt_cond_list",
"using_clause :",
"using_clause : RW_DIFFERENCE RW_USING '(' usinglist ')'",
"non_mt_projterm_list : projterm ',' non_mt_projterm_list",
"non_mt_projterm_list : projterm",
"projterm : identifier T_DOTSTAR",
"projterm : arith_expr",
"projterm : arith_expr RW_AS identifier",
"usinglist : usingterm ',' usinglist",
"usinglist : usingterm",
"usingterm : usingexpr",
"usingexpr : attr",
"usingexpr : const_int",
"order_by_list : orderterm ',' order_by_list",
"order_by_list : orderterm",
"orderterm : order_expr",
"orderterm : order_expr null_spec",
"orderterm : order_expr asc_desc",
"orderterm : order_expr asc_desc null_spec",
"order_expr : attr",
"order_expr : const_int",
"null_spec : RW_NULLS RW_FIRST",
"null_spec : RW_NULLS RW_LAST",
"asc_desc : RW_ASC",
"asc_desc : RW_DESC",
"non_mt_attr_list : attr ',' non_mt_attr_list",
"non_mt_attr_list : attr",
"non_mt_relation_list : generic_relation_variable ',' non_mt_relation_list",
"non_mt_relation_list : generic_relation_variable",
"generic_relation_variable : relation_variable outer_relation_list",
"generic_relation_variable : relation_variable",
"outer_relation_list : outer_join_relation_variable outer_relation_list",
"outer_relation_list : outer_join_relation_variable",
"outer_join_relation_variable : outer_join_type relation_variable RW_ON non_mt_cond_list",
"outer_join_type : RW_LEFT RW_OUTER RW_JOIN",
"outer_join_type : RW_LEFT RW_JOIN",
"outer_join_type : RW_RIGHT RW_OUTER RW_JOIN",
"outer_join_type : RW_RIGHT RW_JOIN",
"outer_join_type : RW_FULL RW_OUTER RW_JOIN",
"outer_join_type : RW_FULL RW_JOIN",
"relation_variable : identifier '[' window_type ']'",
"relation_variable : '(' query ')' '[' window_type ']'",
"relation_variable : identifier '[' window_type ']' RW_AS identifier",
"relation_variable : '(' query ')' '[' window_type ']' RW_AS identifier",
"relation_variable : identifier",
"relation_variable : '(' query ')'",
"relation_variable : identifier RW_AS identifier",
"relation_variable : '(' query ')' RW_AS identifier",
"relation_variable : identifier pattern_recognition_clause1 RW_AS identifier",
"relation_variable : '(' query ')' RW_AS identifier pattern_recognition_clause1 RW_AS identifier",
"relation_variable : '(' non_mt_double_src_identifier_list ')' pattern_recognition_clause1 RW_AS identifier",
"relation_variable : identifier xmltable_clause RW_AS identifier",
"relation_variable : identifier '[' user_window_type ']'",
"relation_variable : identifier '[' user_window_type ']' RW_AS identifier",
"relation_variable : URW_TABLE '(' object_expr RW_AS identifier ')' RW_AS identifier",
"relation_variable : URW_TABLE '(' object_expr RW_AS identifier RW_OF datatype ')' RW_AS identifier",
"non_mt_double_src_identifier_list : src_identifier_variable ',' src_identifier_variable ',' non_mt_src_identifier_list",
"non_mt_double_src_identifier_list : src_identifier_variable ',' src_identifier_variable",
"non_mt_src_identifier_list : src_identifier_variable ',' non_mt_src_identifier_list",
"non_mt_src_identifier_list : src_identifier_variable",
"src_identifier_variable : identifier",
"src_identifier_variable : identifier RW_AS identifier",
"src_identifier_variable : '(' query ')' RW_AS identifier",
"user_window_type : identifier",
"user_window_type : identifier '(' ')'",
"user_window_type : identifier '(' non_mt_window_list ')'",
"non_mt_window_list : const_value ',' non_mt_window_list",
"non_mt_window_list : const_value",
"window_type : RW_RANGE time_spec",
"window_type : RW_RANGE time_spec RW_SLIDE time_spec",
"window_type : RW_NOW",
"window_type : RW_ROWS intToken",
"window_type : RW_ROWS intToken RW_SLIDE intToken",
"window_type : RW_RANGE RW_UNBOUNDED",
"window_type : RW_PARTITION RW_BY non_mt_attr_list RW_ROWS intToken",
"window_type : RW_PARTITION RW_BY non_mt_attr_list RW_ROWS intToken RW_RANGE time_spec",
"window_type : RW_PARTITION RW_BY non_mt_attr_list RW_ROWS intToken RW_RANGE time_spec RW_SLIDE time_spec",
"window_type : RW_RANGE const_value RW_ON RW_ELEMENT_TIME",
"window_type : RW_RANGE const_value RW_ON identifier",
"window_type : RW_RANGE time_spec_with_timeunit RW_ON identifier",
"window_type : RW_RANGE time_spec_with_timeunit RW_ON RW_ELEMENT_TIME",
"window_type : RW_RANGE const_value RW_ON identifier RW_SLIDE bigIntToken",
"window_type : RW_RANGE const_value RW_ON RW_ELEMENT_TIME RW_SLIDE bigIntToken",
"window_type : RW_RANGE time_spec_with_timeunit RW_ON identifier RW_SLIDE time_spec_with_timeunit",
"window_type : RW_RANGE time_spec_with_timeunit RW_ON RW_ELEMENT_TIME RW_SLIDE time_spec_with_timeunit",
"window_type : URW_CURRENTHOUR RW_ON identifier",
"window_type : URW_CURRENTHOUR RW_ON RW_ELEMENT_TIME",
"window_type : URW_CURRENTPERIOD '(' qstringToken ',' qstringToken ')' RW_ON identifier",
"window_type : URW_CURRENTPERIOD '(' qstringToken ',' qstringToken ')' RW_ON RW_ELEMENT_TIME",
"window_type : URW_CURRENTHOUR RW_ON identifier RW_SLIDE time_spec_with_timeunit",
"window_type : URW_CURRENTHOUR RW_ON RW_ELEMENT_TIME RW_SLIDE time_spec_with_timeunit",
"window_type : URW_CURRENTPERIOD '(' qstringToken ',' qstringToken ')' RW_ON identifier RW_SLIDE time_spec_with_timeunit",
"window_type : URW_CURRENTPERIOD '(' qstringToken ',' qstringToken ')' RW_ON RW_ELEMENT_TIME RW_SLIDE time_spec_with_timeunit",
"time_spec : time_spec_without_timeunit",
"time_spec : time_spec_with_timeunit",
"time_spec_without_timeunit : intToken",
"time_spec_without_timeunit : bigIntToken",
"time_spec_without_timeunit : non_const_arith_expr",
"time_spec_with_timeunit : intToken time_unit",
"time_spec_with_timeunit : bigIntToken time_unit",
"time_spec_with_timeunit : non_const_arith_expr time_unit",
"time_unit : RW_NANOSECOND",
"time_unit : RW_MICROSECOND",
"time_unit : RW_MILLISECOND",
"time_unit : RW_SECOND",
"time_unit : RW_MINUTE",
"time_unit : RW_HOUR",
"time_unit : RW_DAY",
"time_unit : RW_MONTH",
"time_unit : RW_YEAR",
"time_unit : URW_UNIT",
"xmltable_clause : RW_XMLTABLE '(' xmlnamespace_clause ',' sqstringToken RW_PASSING RW_BY RW_VALUE xqryargs_list RW_COLUMNS xtbl_cols_list ')'",
"xmltable_clause : RW_XMLTABLE '(' sqstringToken RW_PASSING RW_BY RW_VALUE xqryargs_list RW_COLUMNS xtbl_cols_list ')'",
"xmlnamespace_clause : URW_XMLNAMESPACES '(' xmlnamespaces_list ')'",
"xmlnamespaces_list : xml_namespace ',' xmlnamespaces_list",
"xmlnamespaces_list : xml_namespace",
"xml_namespace : sqstringToken RW_AS sqstringToken",
"xml_namespace : sqstringToken RW_AS qstringToken",
"xml_namespace : qstringToken RW_AS sqstringToken",
"xml_namespace : qstringToken RW_AS qstringToken",
"xml_namespace : RW_DEFAULT qstringToken",
"xml_namespace : RW_DEFAULT sqstringToken",
"xtbl_cols_list : xtbl_col ',' xtbl_cols_list",
"xtbl_cols_list : xtbl_col",
"xtbl_col : identifier fixed_length_datatype RW_PATH sqstringToken",
"xtbl_col : identifier variable_length_datatype '(' intToken ')' RW_PATH sqstringToken",
"pattern_recognition_clause1 : pattern_recognition_clause",
"pattern_recognition_clause : RW_MATCH_RECOGNIZE '(' opt_partition_clause pattern_measures_clause pattern_skip_match_clause RW_INCLUDE RW_TIMER RW_EVENTS pattern_clause duration_clause subset_clause pattern_definition_clause ')'",
"pattern_recognition_clause : RW_MATCH_RECOGNIZE '(' opt_partition_clause pattern_measures_clause pattern_skip_match_clause pattern_clause within_clause subset_clause pattern_definition_clause ')'",
"duration_clause : RW_DURATION time_spec",
"duration_clause : RW_DURATION RW_MULTIPLES RW_OF time_spec",
"within_clause :",
"within_clause : URW_WITHIN time_spec",
"within_clause : URW_WITHIN URW_INCLUSIVE time_spec",
"subset_clause :",
"subset_clause : RW_SUBSET non_mt_subset_definition_list",
"non_mt_subset_definition_list : subset_definition non_mt_subset_definition_list",
"non_mt_subset_definition_list : subset_definition",
"subset_definition : subset_name T_EQ '(' non_mt_corr_list ')'",
"subset_name : stringToken",
"non_mt_corr_list : correlation_name ',' non_mt_corr_list",
"non_mt_corr_list : correlation_name",
"pattern_clause : RW_PATTERN '(' regexp ')'",
"regexp : correlation_name pattern_quantifier",
"regexp : correlation_name",
"regexp : '(' regexp ')'",
"regexp : '(' regexp ')' pattern_quantifier",
"regexp : regexp '|' regexp",
"regexp : regexp regexp",
"correlation_name : stringToken",
"pattern_quantifier : '*' '?'",
"pattern_quantifier : '+' '?'",
"pattern_quantifier : '?' '?'",
"pattern_quantifier : '*'",
"pattern_quantifier : '+'",
"pattern_quantifier : '?'",
"opt_partition_clause :",
"opt_partition_clause : partition_clause",
"partition_clause : RW_PARTITION RW_BY non_mt_attr_list",
"$$5 :",
"pattern_measures_clause : RW_MEASURES $$5 non_mt_measure_list",
"non_mt_measure_list : measure_column ',' non_mt_measure_list",
"non_mt_measure_list : measure_column",
"measure_column : arith_expr RW_AS identifier",
"pattern_skip_match_clause :",
"pattern_skip_match_clause : RW_ALL RW_MATCHES",
"$$6 :",
"pattern_definition_clause : RW_DEFINE $$6 non_mt_corrname_definition_list",
"non_mt_corrname_definition_list : correlation_name_definition ',' non_mt_corrname_definition_list",
"non_mt_corrname_definition_list : correlation_name_definition",
"correlation_name_definition : correlation_name RW_AS non_mt_cond_list",
"non_mt_cond_list : non_mt_cond_list RW_AND non_mt_cond_list",
"non_mt_cond_list : non_mt_cond_list RW_OR non_mt_cond_list",
"non_mt_cond_list : non_mt_cond_list RW_XOR non_mt_cond_list",
"non_mt_cond_list : RW_NOT non_mt_cond_list",
"non_mt_cond_list : '(' non_mt_cond_list ')'",
"non_mt_cond_list : condition",
"non_mt_cond_list : between_condition",
"between_condition : arith_expr RW_BETWEEN arith_expr RW_AND arith_expr",
"condition : arith_expr T_LT arith_expr",
"condition : arith_expr T_JPLUS T_LT arith_expr",
"condition : arith_expr T_LT arith_expr T_JPLUS",
"condition : arith_expr T_LE arith_expr",
"condition : arith_expr T_JPLUS T_LE arith_expr",
"condition : arith_expr T_LE arith_expr T_JPLUS",
"condition : arith_expr T_GT arith_expr",
"condition : arith_expr T_JPLUS T_GT arith_expr",
"condition : arith_expr T_GT arith_expr T_JPLUS",
"condition : arith_expr T_GE arith_expr",
"condition : arith_expr T_JPLUS T_GE arith_expr",
"condition : arith_expr T_GE arith_expr T_JPLUS",
"condition : arith_expr T_EQ arith_expr",
"condition : arith_expr T_JPLUS T_EQ arith_expr",
"condition : arith_expr T_EQ arith_expr T_JPLUS",
"condition : arith_expr T_NE arith_expr",
"condition : arith_expr T_JPLUS T_NE arith_expr",
"condition : arith_expr T_NE arith_expr T_JPLUS",
"condition : arith_expr RW_LIKE arith_expr",
"condition : arith_expr RW_IS RW_NULL",
"condition : arith_expr RW_IS RW_NOT RW_NULL",
"condition : arith_expr RW_IN '(' non_mt_arg_list ')'",
"condition : arith_expr RW_NOT RW_IN '(' non_mt_arg_list ')'",
"condition : '(' arith_expr ',' non_mt_arg_list ')' RW_IN '(' non_mt_arg_list_set ')'",
"condition : '(' arith_expr ',' non_mt_arg_list ')' RW_NOT RW_IN '(' non_mt_arg_list_set ')'",
"non_mt_arg_list_set : '(' non_mt_arg_list ')' ',' non_mt_arg_list_set",
"non_mt_arg_list_set : '(' non_mt_arg_list ')'",
"arith_expr : const_arith_expr",
"arith_expr : non_const_arith_expr",
"const_arith_expr : const_value",
"non_const_arith_expr : func_expr",
"non_const_arith_expr : object_expr",
"non_const_arith_expr : aggr_expr",
"non_const_arith_expr : aggr_distinct_expr",
"non_const_arith_expr : case_expr",
"non_const_arith_expr : decode",
"non_const_arith_expr : arith_expr '+' arith_expr",
"non_const_arith_expr : arith_expr '-' arith_expr",
"non_const_arith_expr : arith_expr '*' arith_expr",
"non_const_arith_expr : arith_expr '/' arith_expr",
"non_const_arith_expr : arith_expr '|' '|' arith_expr",
"non_const_arith_expr : '+' arith_expr",
"non_const_arith_expr : '-' arith_expr",
"non_const_arith_expr : '(' arith_expr ')'",
"attr : identifier '.' identifier",
"attr : identifier '.' pseudo_column",
"attr : identifier",
"attr : pseudo_column",
"extensible_attr : extensible_qualified_identifier",
"extensible_attr : extensible_identifier '.' pseudo_column",
"extensible_attr : pseudo_column",
"object_expr : extensible_attr",
"object_expr : nested_method_field_expr",
"nested_method_field_expr : nested_method_field_expr '.' extensible_identifier",
"nested_method_field_expr : nested_method_field_expr '.' extensible_identifier '(' ')'",
"nested_method_field_expr : nested_method_field_expr '.' extensible_identifier '(' non_mt_arg_list ')'",
"nested_method_field_expr : method_expr",
"nested_method_field_expr : array_expr",
"method_expr : extensible_qualified_identifier '(' ')'",
"method_expr : extensible_qualified_identifier '(' non_mt_arg_list ')'",
"method_expr : extensible_qualified_identifier '(' RW_DISTINCT arith_expr ')'",
"array_expr : object_expr '[' intToken ']'",
"pseudo_column : RW_ELEMENT_TIME",
"pseudo_column : RW_QUERY_ID",
"const_value : boolean_value",
"const_value : interval_value",
"const_value : const_string",
"const_value : RW_NULL",
"const_value : const_int",
"const_value : const_bigint",
"const_value : T_FLOAT",
"const_value : T_DOUBLE",
"const_value : T_NUMBER",
"const_int : T_INT",
"const_bigint : bigIntToken",
"boolean_value : RW_TRUE",
"boolean_value : RW_FALSE",
"const_string : T_QSTRING",
"const_string : T_SQSTRING",
"interval_value : RW_INTERVAL T_QSTRING interval_format",
"interval_format : time_unit",
"interval_format : time_unit '(' const_int ')'",
"interval_format : time_unit RW_TO time_unit",
"interval_format : time_unit '(' const_int ')' RW_TO time_unit",
"interval_format : time_unit '(' const_int ',' const_int ')'",
"interval_format : time_unit '(' const_int ')' RW_TO time_unit '(' const_int ')'",
"interval_format : time_unit RW_TO time_unit '(' const_int ')'",
"timestamp_format : '(' const_int ')'",
"timestamp_format : '(' const_int ')' URW_WITH RW_TIMESTAMP URW_ZONE",
"timestamp_format : URW_WITH RW_TIMESTAMP URW_ZONE",
"timestamp_format : '(' const_int ')' URW_WITH URW_LOCAL RW_TIMESTAMP URW_ZONE",
"timestamp_format : URW_WITH URW_LOCAL RW_TIMESTAMP URW_ZONE",
"func_expr : RW_PREV '(' identifier '.' identifier ')'",
"func_expr : RW_PREV '(' identifier '.' identifier ',' const_int ')'",
"func_expr : RW_PREV '(' identifier '.' identifier ',' const_int ',' const_bigint ')'",
"func_expr : URW_COALESCE '(' non_mt_arg_list ')'",
"func_expr : RW_XMLQUERY '(' sqstringToken RW_PASSING RW_BY RW_VALUE xqryargs_list RW_RETURNING RW_CONTENT ')' RW_XMLDATA",
"func_expr : RW_XMLEXISTS '(' sqstringToken RW_PASSING RW_BY RW_VALUE xqryargs_list RW_RETURNING RW_CONTENT ')' RW_XMLDATA",
"func_expr : RW_XMLCONCAT '(' non_mt_arg_list ')'",
"func_expr : xml_parse_expr",
"func_expr : RW_FIRST '(' identifier '.' identifier ',' const_int ')'",
"func_expr : RW_LAST '(' identifier '.' identifier ',' const_int ')'",
"func_expr : xmlelement_expr",
"func_expr : xmlforest_expr",
"func_expr : xmlcolattval_expr",
"xml_parse_expr : RW_XMLPARSE '(' RW_CONTENT arith_expr RW_WELLFORMED ')'",
"xml_parse_expr : RW_XMLPARSE '(' RW_CONTENT arith_expr ')'",
"xml_parse_expr : RW_XMLPARSE '(' RW_DOCUMENT arith_expr RW_WELLFORMED ')'",
"xml_parse_expr : RW_XMLPARSE '(' RW_DOCUMENT arith_expr ')'",
"xml_agg_expr : RW_XMLAGG '(' arith_expr order_by_clause ')'",
"xml_agg_expr : RW_XMLAGG '(' arith_expr ')'",
"xmlelement_expr : RW_XMLELEMENT '(' URW_NAME qstringToken ',' xml_attribute_list ',' arith_expr_list ')'",
"xmlelement_expr : RW_XMLELEMENT '(' RW_EVALNAME arith_expr ',' xml_attribute_list ',' arith_expr_list ')'",
"xmlelement_expr : RW_XMLELEMENT '(' qstringToken ',' xml_attribute_list ',' arith_expr_list ')'",
"xmlelement_expr : RW_XMLELEMENT '(' URW_NAME qstringToken ',' arith_expr_list ')'",
"xmlelement_expr : RW_XMLELEMENT '(' RW_EVALNAME arith_expr ',' arith_expr_list ')'",
"xmlelement_expr : RW_XMLELEMENT '(' qstringToken ',' arith_expr_list ')'",
"xmlelement_expr : RW_XMLELEMENT '(' URW_NAME qstringToken ')'",
"xmlelement_expr : RW_XMLELEMENT '(' RW_EVALNAME arith_expr ')'",
"xmlelement_expr : RW_XMLELEMENT '(' qstringToken ')'",
"xmlelement_expr : RW_XMLELEMENT '(' URW_NAME qstringToken ',' xml_attribute_list ')'",
"xmlelement_expr : RW_XMLELEMENT '(' RW_EVALNAME arith_expr ',' xml_attribute_list ')'",
"xmlelement_expr : RW_XMLELEMENT '(' qstringToken ',' xml_attribute_list ')'",
"$$7 :",
"xml_attribute_list : RW_XMLATTRIBUTES $$7 xml_attr_list_aux",
"xml_attr_list_aux : '(' xml_attr_list ')'",
"xml_attr_list_aux : '(' ')'",
"xml_attr_list : xml_attr ',' xml_attr_list",
"xml_attr_list : xml_attr",
"xml_attr : arith_expr RW_AS qstringToken",
"xml_attr : arith_expr RW_AS RW_EVALNAME arith_expr",
"xml_attr : extensible_attr",
"$$8 :",
"xmlforest_expr : RW_XMLFOREST $$8 '(' xml_attr_list ')'",
"$$9 :",
"xmlcolattval_expr : RW_XMLCOLATTVAL $$9 '(' xml_attr_list ')'",
"arith_expr_list : arith_expr ',' arith_expr_list",
"arith_expr_list : arith_expr",
"xqryargs_list : xqryarg ',' xqryargs_list",
"xqryargs_list : xqryarg",
"xqryarg : arith_expr RW_AS qstringToken",
"non_mt_arg_list : arith_expr ',' non_mt_arg_list",
"non_mt_arg_list : arith_expr",
"case_expr : RW_CASE searched_case_list RW_END",
"case_expr : RW_CASE searched_case_list RW_ELSE arith_expr RW_END",
"case_expr : RW_CASE arith_expr simple_case_list RW_END",
"case_expr : RW_CASE arith_expr simple_case_list RW_ELSE arith_expr RW_END",
"simple_case_list : simple_case simple_case_list",
"simple_case_list : simple_case",
"simple_case : RW_WHEN arith_expr RW_THEN arith_expr",
"searched_case_list : searched_case searched_case_list",
"searched_case_list : searched_case",
"searched_case : RW_WHEN non_mt_cond_list RW_THEN arith_expr",
"decode : RW_DECODE '(' non_mt_arg_list ')'",
"aggr_expr : RW_COUNT '(' arith_expr ')'",
"aggr_expr : RW_COUNT '(' '*' ')'",
"aggr_expr : RW_COUNT '(' identifier T_DOTSTAR ')'",
"aggr_expr : RW_SUM '(' arith_expr ')'",
"aggr_expr : RW_AVG '(' arith_expr ')'",
"aggr_expr : RW_MAX '(' arith_expr ')'",
"aggr_expr : RW_MIN '(' arith_expr ')'",
"aggr_expr : RW_FIRST '(' identifier '.' identifier ')'",
"aggr_expr : RW_LAST '(' identifier '.' identifier ')'",
"aggr_expr : xml_agg_expr",
"aggr_distinct_expr : RW_COUNT '(' RW_DISTINCT arith_expr ')'",
"aggr_distinct_expr : RW_SUM '(' RW_DISTINCT arith_expr ')'",
"aggr_distinct_expr : RW_AVG '(' RW_DISTINCT arith_expr ')'",
"aggr_distinct_expr : RW_MAX '(' RW_DISTINCT arith_expr ')'",
"aggr_distinct_expr : RW_MIN '(' RW_DISTINCT arith_expr ')'",
"binary : identifier RW_NOT RW_IN identifier",
"binary : identifier RW_IN identifier",
"binary_n : binary opt_evaluate_clause",
"query_n : query",
"query_n : '(' query ')'",
"nary : query_n RW_UNION query_n",
"nary : query_n RW_UNION RW_ALL query_n",
"nary : query_n RW_EXCEPT query_n",
"nary : query_n RW_INTERSECT query_n",
"nary : query_n RW_MINUS query_n",
"nary : '(' nary ')'",
"nary : identifier setop_relation_list",
"nary_n : nary opt_evaluate_clause",
"setop_relation_list : setop_relation_variable setop_relation_list",
"setop_relation_list : setop_relation_variable",
"setop_relation_variable : RW_UNION identifier",
"setop_relation_variable : RW_UNION RW_ALL identifier",
"setop_relation_variable : RW_EXCEPT identifier",
"setop_relation_variable : RW_MINUS identifier",
"setop_relation_variable : RW_INTERSECT identifier",
"datatype : variable_length_datatype",
"datatype : fixed_length_datatype",
"numberToken : RW_NUMBER",
"variable_length_datatype : RW_CHAR",
"variable_length_datatype : RW_BYTE",
"fixed_length_datatype : RW_INTEGER",
"fixed_length_datatype : RW_BIGINT",
"fixed_length_datatype : RW_FLOAT",
"fixed_length_datatype : RW_DOUBLE",
"fixed_length_datatype : RW_TIMESTAMP",
"fixed_length_datatype : RW_TIMESTAMP timestamp_format",
"fixed_length_datatype : RW_BOOLEAN",
"fixed_length_datatype : RW_XMLTYPE",
"fixed_length_datatype : RW_OBJECT",
"fixed_length_datatype : RW_NUMBER",
"fixed_length_datatype : RW_INTERVAL",
"fixed_length_datatype : RW_INTERVAL interval_format",
"fixed_length_datatype : extensible_qualified_datatype",
"identifier : T_STRING",
"identifier : T_UPPER_LETTER",
"identifier : unreserved_keyword",
"extensible_identifier : identifier",
"extensible_identifier : reserved_keyword",
"extensible_non_datatype_identifier : identifier",
"extensible_non_datatype_identifier : non_datatype_reserved_keyword",
"extensible_qualified_identifier : compound_extensible_qualified_identifier",
"extensible_qualified_identifier : T_CHARAT extensible_identifier",
"extensible_qualified_identifier : extensible_identifier",
"extensible_qualified_datatype : compound_extensible_qualified_identifier",
"extensible_qualified_datatype : T_CHARAT extensible_identifier",
"extensible_qualified_datatype : extensible_non_datatype_identifier",
"compound_extensible_qualified_identifier : extensible_identifier '.' compound_extensible_qualified_identifier",
"compound_extensible_qualified_identifier : extensible_identifier '.' extensible_identifier",
"compound_extensible_qualified_identifier : extensible_identifier '@' extensible_identifier",
"intToken : T_INT",
"bigIntToken : T_BIGINT",
"stringToken : T_STRING",
"qstringToken : T_QSTRING",
"sqstringToken : T_SQSTRING",
"unreserved_keyword : URW_NAME",
"unreserved_keyword : URW_SUPPORTS",
"unreserved_keyword : URW_INCREMENTAL",
"unreserved_keyword : URW_COMPUTATION",
"unreserved_keyword : URW_USE",
"unreserved_keyword : URW_INSTANCE",
"unreserved_keyword : URW_SYSTEM",
"unreserved_keyword : URW_TOTAL",
"unreserved_keyword : URW_ORDERING",
"unreserved_keyword : URW_THRESHOLD",
"unreserved_keyword : URW_DEGREE",
"unreserved_keyword : URW_PARALLELISM",
"unreserved_keyword : URW_ARCHIVER",
"unreserved_keyword : URW_ARCHIVED",
"unreserved_keyword : URW_STARTTIME",
"unreserved_keyword : URW_ENTITY",
"unreserved_keyword : URW_IDENTIFIER",
"unreserved_keyword : URW_XMLNAMESPACES",
"unreserved_keyword : URW_UNIT",
"unreserved_keyword : URW_PROPAGATE",
"unreserved_keyword : URW_COLUMN",
"unreserved_keyword : URW_REPLAY",
"unreserved_keyword : URW_WORKER",
"unreserved_keyword : URW_TRANSACTION",
"unreserved_keyword : URW_DIMENSION",
"unreserved_keyword : URW_TABLE",
"unreserved_keyword : URW_WITHIN",
"unreserved_keyword : URW_INCLUSIVE",
"unreserved_keyword : URW_CURRENTHOUR",
"unreserved_keyword : URW_CURRENTPERIOD",
"unreserved_keyword : URW_WITH",
"unreserved_keyword : URW_LOCAL",
"unreserved_keyword : URW_ZONE",
"unreserved_keyword : URW_EVALUATE",
"unreserved_keyword : URW_EVERY",
"unreserved_keyword : URW_COALESCE",
"reserved_keyword : RW_INTEGER",
"reserved_keyword : RW_FLOAT",
"reserved_keyword : RW_DOUBLE",
"reserved_keyword : RW_TIMESTAMP",
"reserved_keyword : RW_BYTE",
"reserved_keyword : RW_BOOLEAN",
"reserved_keyword : RW_OBJECT",
"reserved_keyword : RW_NUMBER",
"reserved_keyword : non_datatype_reserved_keyword",
"non_datatype_reserved_keyword : RW_JAVA",
"non_datatype_reserved_keyword : RW_MATCHES",
"non_datatype_reserved_keyword : RW_EVENT",
"non_datatype_reserved_keyword : RW_PATTERN",
"non_datatype_reserved_keyword : RW_START",
"non_datatype_reserved_keyword : RW_END",
"builtin_func : RW_XMLQUERY",
"builtin_func : RW_XMLEXISTS",
"extended_builtin_aggr : RW_FIRST",
"extended_builtin_aggr : RW_LAST",
"builtin_aggr : RW_MAX",
"builtin_aggr : RW_MIN",
"builtin_aggr : RW_XMLAGG",
"builtin_aggr_incr : RW_SUM",
"builtin_aggr_incr : RW_AVG",
"builtin_aggr_incr : RW_COUNT",
};

//#line 3297 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"

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
          if (yycheck(s) == c)
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
          if (yycheck(s) == c)
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


//#line 4949 "Parser.java"
//###############################################################
// method: yylexdebug : check lexer state
//###############################################################
void yylexdebug(int state,int ch)
{
String s=null;
  if (ch < 0) ch=0;
  if (ch <= YYMAXTOKEN) //check index bounds
     s = yyname[ch];    //now get it
  if (s==null)
    s = "illegal-symbol";
  debug("state "+state+", reading "+ch+" ("+s+")");
}





//The following are now global, to aid in error reporting
int yyn;       //next next thing to do
int yym;       //
int yystate;   //current parsing state from state table
String yys;    //current token string


//###############################################################
// method: yyparse : parse input and execute indicated items
//###############################################################
int yyparse()
throws java.lang.Exception
{
boolean doaction;
  init_stacks();
  yynerrs = 0;
  yyerrflag = 0;
  yychar = -1;          //impossible char forces a read
  yystate=0;            //initial state
  state_push(yystate);  //save it
  val_push(yylval);     //save empty value
  while (true) //until parsing is done, either correctly, or w/error
    {
    doaction=true;
    if (yydebug) debug("loop"); 
    //#### NEXT ACTION (from reduction table)
    for (yyn=yydefred[yystate];yyn==0;yyn=yydefred[yystate])
      {
      if (yydebug) debug("yyn:"+yyn+"  state:"+yystate+"  yychar:"+yychar);
      if (yychar < 0)      //we want a char?
        {
        yychar = yylex();  //get next token
        if (yydebug) debug(" next yychar:"+yychar);
        //#### ERROR CHECK ####
        if (yychar < 0)    //it it didn't work/error
          {
          yychar = 0;      //change it to default string (no -1!)
          if (yydebug)
            yylexdebug(yystate,yychar);
          }
        }//yychar<0
      yyn = yysindex[yystate];  //get amount to shift by (shift index)
      if ((yyn != 0) && (yyn += yychar) >= 0 &&
          yyn <= YYTABLESIZE && yycheck(yyn) == yychar)
        {
        if (yydebug)
          debug("state "+yystate+", shifting to state "+yytable(yyn));
        //#### NEXT STATE ####
        yystate = yytable(yyn);//we are in a new state
        state_push(yystate);   //save it
        val_push(yylval);      //push our lval as the input for next rule
        yychar = -1;           //since we have 'eaten' a token, say we need another
        if (yyerrflag > 0)     //have we recovered an error?
           --yyerrflag;        //give ourselves credit
        doaction=false;        //but don't process yet
        break;   //quit the yyn=0 loop
        }

    yyn = yyrindex[yystate];  //reduce
    if ((yyn !=0 ) && (yyn += yychar) >= 0 &&
            yyn <= YYTABLESIZE && yycheck(yyn) == yychar)
      {   //we reduced!
      if (yydebug) debug("reduce");
      yyn = yytable(yyn);
      doaction=true; //get ready to execute
      break;         //drop down to actions
      }
    else //ERROR RECOVERY
      {
      if (yyerrflag==0)
        {
        yyerror("syntax error");
        yynerrs++;
        }
      if (yyerrflag < 3) //low error count?
        {
        yyerrflag = 3;
        while (true)   //do until break
          {
          if (stateptr<0)   //check for under & overflow here
            {
            yyerror("stack underflow. aborting...");  //note lower case 's'
            return 1;
            }
          yyn = yysindex[state_peek(0)];
          if ((yyn != 0) && (yyn += YYERRCODE) >= 0 &&
                    yyn <= YYTABLESIZE && yycheck(yyn) == YYERRCODE)
            {
            if (yydebug)
              debug("state "+state_peek(0)+", error recovery shifting to state "+yytable(yyn)+" ");
            yystate = yytable(yyn);
            state_push(yystate);
            val_push(yylval);
            doaction=false;
            break;
            }
          else
            {
            if (yydebug)
              debug("error recovery discarding state "+state_peek(0)+" ");
            if (stateptr<0)   //check for under & overflow here
              {
              yyerror("Stack underflow. aborting...");  //capital 'S'
              return 1;
              }
            state_pop();
            val_pop();
            }
          }
        }
      else            //discard this token
        {
        if (yychar == 0)
          return 1; //yyabort
        if (yydebug)
          {
          yys = null;
          if (yychar <= YYMAXTOKEN) yys = yyname[yychar];
          if (yys == null) yys = "illegal-symbol";
          debug("state "+yystate+", error recovery discards token "+yychar+" ("+yys+")");
          }
        yychar = -1;  //read another
        }
      }//end error recovery
    }//yyn=0 loop
    if (!doaction)   //any reason not to proceed?
      continue;      //skip action
    yym = yylen[yyn];          //get count of terminals on rhs
    if (yydebug)
      debug("state "+yystate+", reducing "+yym+" by rule "+yyn+" ("+yyrule[yyn]+")");
    if (yym>0)                 //if count of rhs not 'nil'
      yyval = val_peek(yym-1); //get current semantic value
    yyval = dup_yyval(yyval); //duplicate yyval if ParserVal is used as semantic value
    switch(yyn)
      {
//########## USER-SUPPLIED ACTIONS ##########
case 1:
//#line 549 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{ parseTree = (CEPParseTreeNode)(val_peek(0).obj); }
break;
case 2:
//#line 554 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{ yyval.obj = val_peek(0).obj;}
break;
case 3:
//#line 557 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = val_peek(0).obj;}
break;
case 4:
//#line 560 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = val_peek(0).obj;}
break;
case 5:
//#line 563 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = val_peek(0).obj;}
break;
case 6:
//#line 566 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = val_peek(0).obj;}
break;
case 7:
//#line 569 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = val_peek(0).obj;}
break;
case 8:
//#line 572 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = val_peek(0).obj;}
break;
case 9:
//#line 575 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = val_peek(0).obj;}
break;
case 10:
//#line 578 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = val_peek(0).obj;}
break;
case 11:
//#line 581 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = val_peek(0).obj;}
break;
case 12:
//#line 584 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = val_peek(0).obj;}
break;
case 13:
//#line 587 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = val_peek(0).obj;}
break;
case 14:
//#line 590 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = val_peek(0).obj;}
break;
case 15:
//#line 593 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = val_peek(0).obj;}
break;
case 16:
//#line 596 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = val_peek(0).obj;}
break;
case 17:
//#line 599 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = val_peek(0).obj;}
break;
case 18:
//#line 602 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = val_peek(0).obj;}
break;
case 19:
//#line 605 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = val_peek(0).obj;}
break;
case 20:
//#line 608 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = val_peek(0).obj;}
break;
case 21:
//#line 611 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = val_peek(0).obj;}
break;
case 22:
//#line 614 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = val_peek(0).obj;}
break;
case 23:
//#line 617 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = val_peek(0).obj;}
break;
case 24:
//#line 620 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = val_peek(0).obj;}
break;
case 25:
//#line 623 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = val_peek(0).obj;}
break;
case 26:
//#line 626 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = val_peek(0).obj;}
break;
case 27:
//#line 629 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = val_peek(0).obj;}
break;
case 28:
//#line 632 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = val_peek(0).obj;}
break;
case 29:
//#line 635 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = val_peek(0).obj;}
break;
case 30:
//#line 638 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = val_peek(0).obj;}
break;
case 31:
//#line 644 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = new CEPReplaySpecNode(new CEPTimeSpecNode((TimeUnit)(val_peek(0).obj),(CEPIntTokenNode)(val_peek(1).obj)));}
break;
case 32:
//#line 646 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = new CEPReplaySpecNode((CEPIntTokenNode)(val_peek(1).obj));}
break;
case 33:
//#line 650 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = new Boolean(false);}
break;
case 34:
//#line 652 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = new Boolean(true);}
break;
case 35:
//#line 654 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = new Boolean(false);}
break;
case 36:
//#line 659 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{rdn = new CEPTableDefnNode((CEPStringTokenNode)(val_peek(4).obj), (List)val_peek(2).obj); rdn.setSystemTimestamped((Boolean)(val_peek(0).obj)); rdn.setExternal(false); rdn.setPartitioned(false); yyval.obj = rdn;}
break;
case 37:
//#line 661 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{rdn = new CEPTableDefnNode((CEPStringTokenNode)(val_peek(4).obj), (List)val_peek(2).obj); rdn.setSystemTimestamped((Boolean)(val_peek(0).obj)); rdn.setExternal(false); rdn.setPartitioned(true); yyval.obj = rdn;}
break;
case 38:
//#line 663 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{rdn = new CEPTableDefnNode((CEPStringTokenNode)(val_peek(6).obj), (List)val_peek(4).obj); rdn.setSystemTimestamped(false); rdn.setExternal(false); rdn.setTimestampExpr((CEPExprNode)(val_peek(0).obj)); rdn.setPartitioned(false); yyval.obj = rdn;}
break;
case 39:
//#line 665 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{rdn = new CEPTableDefnNode((CEPStringTokenNode)(val_peek(6).obj), (List)val_peek(4).obj); rdn.setSystemTimestamped(false); rdn.setExternal(false); rdn.setTimestampExpr((CEPExprNode)(val_peek(0).obj)); rdn.setPartitioned(true); yyval.obj = rdn;}
break;
case 40:
//#line 667 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{rdn = new CEPTableDefnNode((CEPStringTokenNode)(val_peek(15).obj), (List)val_peek(13).obj); rdn.setSystemTimestamped((Boolean)(val_peek(0).obj)); rdn.setExternal(false); rdn.setIsArchived(true); rdn.setArchiverName((List)(val_peek(10).obj)); rdn.setEntityName(val_peek(8).sval); rdn.setTimestampColumn((CEPStringTokenNode)(val_peek(5).obj)); rdn.setReplayClause((CEPReplaySpecNode)(val_peek(3).obj)); rdn.setWorkerIdColName((String)val_peek(2).sval); rdn.setTxnIdColName((String)val_peek(1).sval); yyval.obj = rdn;}
break;
case 41:
//#line 672 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{rdn = new CEPTableDefnNode((CEPStringTokenNode)(val_peek(4).obj), (CEPRelationAttrSpecsNode)val_peek(2).obj); rdn.setIsSilent(false); rdn.setSystemTimestamped((Boolean)(val_peek(0).obj)); rdn.setExternal(false); yyval.obj = rdn;}
break;
case 42:
//#line 674 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{rdn = new CEPTableDefnNode((CEPStringTokenNode)(val_peek(3).obj), (CEPRelationAttrSpecsNode)val_peek(1).obj); rdn.setIsSilent(false); rdn.setSystemTimestamped(false); rdn.setExternal(true); yyval.obj = rdn;}
break;
case 43:
//#line 676 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{rdn = new CEPTableDefnNode((CEPStringTokenNode)(val_peek(5).obj), (CEPRelationAttrSpecsNode)val_peek(3).obj); rdn.setIsSilent(true); rdn.setSystemTimestamped(false); rdn.setExternal(false); yyval.obj = rdn;}
break;
case 44:
//#line 678 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{rdn = new CEPTableDefnNode((CEPStringTokenNode)(val_peek(11).obj), (CEPRelationAttrSpecsNode)val_peek(9).obj); 
      rdn.setIsSilent(false); 
      rdn.setSystemTimestamped((Boolean)(val_peek(0).obj)); 
      rdn.setExternal(false); 
      rdn.setIsArchived(true);
      rdn.setArchiverName((List)(val_peek(6).obj));
      rdn.setEntityName(val_peek(4).sval); 
      rdn.setEventIdColName((String)val_peek(3).sval);
      rdn.setWorkerIdColName((String)val_peek(2).sval);
      rdn.setTxnIdColName((String)val_peek(1).sval);
      rdn.setIsDimension((Boolean)val_peek(13).obj);
      yyval.obj = rdn;}
break;
case 45:
//#line 693 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{/* empty */ yyval.obj = new Boolean(false);}
break;
case 46:
//#line 695 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = new Boolean(true);}
break;
case 47:
//#line 699 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{/*empty */ yyval.sval=null;}
break;
case 48:
//#line 701 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.sval=((CEPStringTokenNode)val_peek(0).obj).getValue();}
break;
case 49:
//#line 705 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{/*empty */ yyval.sval=null;}
break;
case 50:
//#line 707 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.sval=((CEPStringTokenNode)val_peek(0).obj).getValue();}
break;
case 51:
//#line 711 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{/* empty*/ yyval.sval = null;}
break;
case 52:
//#line 713 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.sval=((CEPStringTokenNode)val_peek(0).obj).getValue();}
break;
case 53:
//#line 717 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{updateViewQryTxt();}
break;
case 54:
//#line 717 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{updateViewQryTxt();}
break;
case 55:
//#line 718 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{((CEPViewDefnNode)(val_peek(4).obj)).setQueryTxt(viewQryTxt.toString());
      ((CEPViewDefnNode)(val_peek(4).obj)).setQueryNode((CEPQueryNode)(val_peek(1).obj)); 
      ((CEPViewDefnNode)(val_peek(4).obj)).setEndOffset(endOffset);
      yyval.obj = val_peek(4).obj;
      viewQryTxt = null;
     }
break;
case 56:
//#line 726 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{((CEPQueryRefNode)(val_peek(0).obj)).setKind( CEPQueryRefKind.VIEW); 
      ((CEPViewDefnNode)(val_peek(2).obj)).setQueryNode((CEPQueryNode)(val_peek(0).obj));
      yyval.obj = val_peek(2).obj;
     }
break;
case 57:
//#line 734 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{vdn = new CEPViewDefnNode((CEPStringTokenNode)val_peek(3).obj); vdn.setAttrSpecList((List)val_peek(1).obj); yyval.obj = vdn;}
break;
case 58:
//#line 737 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{vdn = new CEPViewDefnNode((CEPStringTokenNode)val_peek(3).obj); vdn.setAttrNameList((List)val_peek(1).obj); yyval.obj = vdn;}
break;
case 59:
//#line 740 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = new CEPViewDefnNode((CEPStringTokenNode)val_peek(0).obj);}
break;
case 60:
//#line 746 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{ 
       ((CEPViewDefnNode)(val_peek(1).obj)).setEventIdColName((String)(val_peek(0).sval));
       yyval.obj = val_peek(1).obj;
     }
break;
case 61:
//#line 753 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{updateViewQryTxt();}
break;
case 62:
//#line 753 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{updateViewQryTxt();}
break;
case 63:
//#line 754 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{
       ((CEPViewDefnNode)(val_peek(4).obj)).setQueryNode((CEPQueryNode)(val_peek(1).obj));
       ((CEPViewDefnNode)(val_peek(4).obj)).setQueryTxt(viewQryTxt.toString());
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
       ((CEPViewDefnNode)(val_peek(4).obj)).setQueryTxt(temp);
       viewQryTxt = null;
       yyval.obj = val_peek(4).obj;
     }
break;
case 64:
//#line 777 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{
       vdn = new CEPViewDefnNode((CEPStringTokenNode)val_peek(3).obj);
       vdn.setAttrSpecList((List)val_peek(1).obj);
       vdn.setIsArchived(true);
       yyval.obj = vdn;
     }
break;
case 65:
//#line 787 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = new CEPViewOrderingConstraintNode((CEPStringTokenNode)(val_peek(5).obj), OrderingKind.TOTAL_ORDER);}
break;
case 66:
//#line 790 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = new CEPViewOrderingConstraintNode((CEPStringTokenNode)(val_peek(7).obj), OrderingKind.PARTITION_ORDERED, (CEPExprNode)(val_peek(0).obj));}
break;
case 67:
//#line 793 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = new CEPViewOrderingConstraintNode((CEPStringTokenNode)(val_peek(3).obj), OrderingKind.UNORDERED);}
break;
case 68:
//#line 798 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = new CEPSynonymDefnNode((CEPStringTokenNode)val_peek(3).obj, (List)(val_peek(0).obj), SynonymType.TYPE);}
break;
case 69:
//#line 803 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{paramSpecList = new LinkedList(); paramSpecList.add(val_peek(8).obj); yyval.obj = new CEPFunctionDefnNode((String)(val_peek(10).obj), paramSpecList, (Datatype)(val_peek(5).obj), (CEPStringTokenNode)(val_peek(0).obj));}
break;
case 70:
//#line 806 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = new CEPFunctionDefnNode((CEPStringTokenNode)(val_peek(7).obj), (Datatype)(val_peek(5).obj), (CEPStringTokenNode)(val_peek(0).obj), CEPFunctionDefnNode.NameType.CLASS_NAME);}
break;
case 71:
//#line 809 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = new CEPFunctionDefnNode((CEPStringTokenNode)(val_peek(7).obj), (Datatype)(val_peek(5).obj), (CEPStringTokenNode)(val_peek(0).obj), CEPFunctionDefnNode.NameType.INSTANCE_NAME);}
break;
case 72:
//#line 812 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{paramSpecList = new LinkedList(); paramSpecList.add(val_peek(8).obj); yyval.obj = new CEPFunctionDefnNode((CEPStringTokenNode)(val_peek(10).obj), paramSpecList, (Datatype)(val_peek(5).obj), (CEPStringTokenNode)(val_peek(0).obj), CEPFunctionDefnNode.NameType.CLASS_NAME);}
break;
case 73:
//#line 815 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{paramSpecList = new LinkedList(); paramSpecList.add(val_peek(8).obj); yyval.obj = new CEPFunctionDefnNode((CEPStringTokenNode)(val_peek(10).obj), paramSpecList, (Datatype)(val_peek(5).obj), (CEPStringTokenNode)(val_peek(0).obj), CEPFunctionDefnNode.NameType.INSTANCE_NAME);}
break;
case 74:
//#line 818 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = new CEPFunctionDefnNode((CEPStringTokenNode)(val_peek(10).obj), (List)(val_peek(8).obj), (Datatype)(val_peek(5).obj), (CEPStringTokenNode)(val_peek(0).obj), CEPFunctionDefnNode.NameType.CLASS_NAME);}
break;
case 75:
//#line 821 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = new CEPFunctionDefnNode((CEPStringTokenNode)(val_peek(10).obj), (List)(val_peek(8).obj), (Datatype)(val_peek(5).obj), (CEPStringTokenNode)(val_peek(0).obj), CEPFunctionDefnNode.NameType.INSTANCE_NAME);}
break;
case 76:
//#line 824 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{paramSpecList = new LinkedList(); paramSpecList.add(val_peek(8).obj); yyval.obj = new CEPFunctionDefnNode("prev", paramSpecList, (Datatype)(val_peek(5).obj), (CEPStringTokenNode)(val_peek(0).obj));}
break;
case 77:
//#line 827 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = new CEPFunctionDefnNode("prev", (List)(val_peek(8).obj), (Datatype)(val_peek(5).obj), (CEPStringTokenNode)(val_peek(0).obj));}
break;
case 78:
//#line 832 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = new CEPWindowDefnNode((CEPStringTokenNode)(val_peek(3).obj), (CEPStringTokenNode)(val_peek(0).obj));}
break;
case 79:
//#line 835 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{paramSpecList = new LinkedList(); paramSpecList.add(val_peek(4).obj); yyval.obj = new CEPWindowDefnNode((CEPStringTokenNode)(val_peek(6).obj), paramSpecList, (CEPStringTokenNode)(val_peek(0).obj));}
break;
case 80:
//#line 838 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = new CEPWindowDefnNode((CEPStringTokenNode)(val_peek(6).obj), (List)(val_peek(4).obj), (CEPStringTokenNode)(val_peek(0).obj));}
break;
case 81:
//#line 843 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = new CEPAggrFnDefnNode((String)val_peek(8).obj, (CEPAttrSpecNode)(val_peek(6).obj), (Datatype)(val_peek(3).obj), (CEPStringTokenNode)(val_peek(0).obj), false);}
break;
case 82:
//#line 846 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = new CEPAggrFnDefnNode((String)val_peek(9).obj, (CEPAttrSpecNode)(val_peek(7).obj), (Datatype)(val_peek(4).obj), (CEPStringTokenNode)(val_peek(1).obj), true);}
break;
case 83:
//#line 849 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = new CEPAggrFnDefnNode((String)val_peek(8).obj, (List<CEPAttrSpecNode>)(val_peek(6).obj), (Datatype)(val_peek(3).obj), (CEPStringTokenNode)(val_peek(0).obj), false);}
break;
case 84:
//#line 852 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = new CEPAggrFnDefnNode((CEPStringTokenNode)(val_peek(8).obj), (CEPAttrSpecNode)(val_peek(6).obj), (Datatype)(val_peek(3).obj), (CEPStringTokenNode)(val_peek(0).obj), false, CEPFunctionDefnNode.NameType.CLASS_NAME);}
break;
case 85:
//#line 855 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = new CEPAggrFnDefnNode((CEPStringTokenNode)(val_peek(9).obj), (CEPAttrSpecNode)(val_peek(7).obj), (Datatype)(val_peek(4).obj), (CEPStringTokenNode)(val_peek(0).obj), false, CEPFunctionDefnNode.NameType.INSTANCE_NAME);}
break;
case 86:
//#line 858 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = new CEPAggrFnDefnNode((CEPStringTokenNode)(val_peek(9).obj), (CEPAttrSpecNode)(val_peek(7).obj), (Datatype)(val_peek(4).obj), (CEPStringTokenNode)(val_peek(1).obj), true, CEPFunctionDefnNode.NameType.CLASS_NAME);}
break;
case 87:
//#line 861 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = new CEPAggrFnDefnNode((CEPStringTokenNode)(val_peek(10).obj), (CEPAttrSpecNode)(val_peek(8).obj), (Datatype)(val_peek(5).obj), (CEPStringTokenNode)(val_peek(1).obj), true, CEPFunctionDefnNode.NameType.INSTANCE_NAME);}
break;
case 88:
//#line 864 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = new CEPAggrFnDefnNode((CEPStringTokenNode)(val_peek(8).obj), (List<CEPAttrSpecNode>)(val_peek(6).obj), (Datatype)(val_peek(3).obj), (CEPStringTokenNode)(val_peek(0).obj), false, CEPFunctionDefnNode.NameType.CLASS_NAME);}
break;
case 89:
//#line 867 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = new CEPAggrFnDefnNode((CEPStringTokenNode)(val_peek(9).obj), (List<CEPAttrSpecNode>)(val_peek(7).obj), (Datatype)(val_peek(4).obj), (CEPStringTokenNode)(val_peek(0).obj), false, CEPFunctionDefnNode.NameType.INSTANCE_NAME);}
break;
case 90:
//#line 870 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = new CEPAggrFnDefnNode((CEPStringTokenNode)(val_peek(9).obj), (List<CEPAttrSpecNode>)(val_peek(7).obj), (Datatype)(val_peek(4).obj), (CEPStringTokenNode)(val_peek(1).obj), true, CEPFunctionDefnNode.NameType.CLASS_NAME);}
break;
case 91:
//#line 873 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = new CEPAggrFnDefnNode((CEPStringTokenNode)(val_peek(10).obj), (List<CEPAttrSpecNode>)(val_peek(8).obj), (Datatype)(val_peek(5).obj), (CEPStringTokenNode)(val_peek(1).obj), true, CEPFunctionDefnNode.NameType.INSTANCE_NAME);}
break;
case 92:
//#line 878 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{dropParamList = new LinkedList(); dropParamList.add(val_peek(1).obj); yyval.obj = new CEPFunctionRefNode((CEPStringTokenNode)(val_peek(3).obj), dropParamList);}
break;
case 93:
//#line 881 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = new CEPFunctionRefNode((CEPStringTokenNode)(val_peek(3).obj), (List)(val_peek(1).obj));}
break;
case 94:
//#line 884 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = new CEPFunctionRefNode((CEPStringTokenNode)(val_peek(0).obj));}
break;
case 95:
//#line 890 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = new CEPWindowRefNode((CEPStringTokenNode)(val_peek(0).obj));}
break;
case 96:
//#line 895 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = new CEPSynonymRefNode((CEPStringTokenNode)val_peek(0).obj);}
break;
case 97:
//#line 900 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = new CEPQueryDefnNode((CEPStringTokenNode)(val_peek(2).obj), (CEPQueryNode)val_peek(0).obj);}
break;
case 98:
//#line 903 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = new CEPQueryDefnNode( (CEPStringTokenNode)(val_peek(3).obj), (CEPQueryNode)val_peek(1).obj, (CEPRelationConstraintNode)val_peek(0).obj);}
break;
case 99:
//#line 908 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{  /* empty production */yyval.obj = null; }
break;
case 100:
//#line 911 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = new CEPSlideExprNode((CEPTimeSpecNode)val_peek(0).obj);}
break;
case 101:
//#line 914 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = new CEPSlideExprNode(((CEPBigintConstExprNode)val_peek(0).obj).getValue().longValue()); }
break;
case 102:
//#line 919 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{ yyval.obj = val_peek(0).obj;}
break;
case 103:
//#line 924 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{((CEPQueryRefNode)val_peek(1).obj).setKind( CEPQueryRefKind.START); yyval.obj = val_peek(1).obj;}
break;
case 104:
//#line 929 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{((CEPQueryRefNode)val_peek(1).obj).setKind( CEPQueryRefKind.STOP); yyval.obj = val_peek(1).obj;}
break;
case 105:
//#line 937 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{((CEPQueryRefNode)val_peek(3).obj).setKind(CEPQueryRefKind.SETSTARTTIME);
      ((CEPQueryRefNode)val_peek(3).obj).setStartTimeValue(((CEPBigintConstExprNode)val_peek(0).obj).getValue().longValue());
      yyval.obj=val_peek(3).obj;}
break;
case 106:
//#line 944 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{((CEPQueryRefNode)val_peek(3).obj).setKind( CEPQueryRefKind.ADDDEST);
      ((CEPQueryRefNode)val_peek(3).obj).setValue((CEPStringTokenNode)(val_peek(0).obj)); yyval.obj = val_peek(3).obj;}
break;
case 107:
//#line 948 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{((CEPQueryRefNode)val_peek(4).obj).setKind( CEPQueryRefKind.ADDDEST);
      ((CEPQueryRefNode)val_peek(4).obj).setValue((CEPStringTokenNode)(val_peek(1).obj));
      ((CEPQueryRefNode)val_peek(4).obj).setDestProperties((Map)(val_peek(0).obj)); yyval.obj = val_peek(4).obj;}
break;
case 108:
//#line 976 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{Map prop = new HashMap<String, Boolean>();
      prop.put(Constants.USE_UPDATE_SEMANTICS, new Boolean(true));
      yyval.obj=prop;}
break;
case 109:
//#line 981 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{Map prop = new HashMap<String, Boolean>();
      prop.put(Constants.BATCH_OUTPUT, new Boolean(true));
      yyval.obj=prop;}
break;
case 110:
//#line 986 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{Map prop = new HashMap<String, Boolean>();
      prop.put(Constants.PROPAGATE_HB, new Boolean(true));
      yyval.obj=prop;}
break;
case 111:
//#line 991 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{Map prop = new HashMap<String, Boolean>();
      prop.put(Constants.BATCH_OUTPUT, new Boolean(true));
      prop.put(Constants.PROPAGATE_HB, new Boolean(true));
      yyval.obj=prop;}
break;
case 112:
//#line 997 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{Map prop = new HashMap<String, Boolean>();
      prop.put(Constants.BATCH_OUTPUT, new Boolean(true));
      prop.put(Constants.USE_UPDATE_SEMANTICS, new Boolean(true));
      yyval.obj=prop;}
break;
case 113:
//#line 1006 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{((CEPQueryRefNode)val_peek(0).obj).setKind( CEPQueryRefKind.DROP); yyval.obj = val_peek(0).obj;}
break;
case 114:
//#line 1011 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = new CEPQueryRefNode((CEPStringTokenNode)(val_peek(0).obj));}
break;
case 115:
//#line 1016 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = new CEPViewDropNode((CEPStringTokenNode)(val_peek(0).obj));}
break;
case 116:
//#line 1021 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = new CEPRelOrStreamRefNode((CEPStringTokenNode)(val_peek(0).obj), false);}
break;
case 117:
//#line 1024 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = new CEPRelOrStreamRefNode((CEPStringTokenNode)(val_peek(0).obj), true);}
break;
case 118:
//#line 1029 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = new CEPAddTableSourceNode( (CEPStringTokenNode)(val_peek(3).obj), (CEPStringTokenNode)(val_peek(0).obj), false);}
break;
case 119:
//#line 1032 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = new CEPAddTableSourceNode((CEPStringTokenNode)(val_peek(3).obj), (CEPStringTokenNode)(val_peek(0).obj), true);}
break;
case 120:
//#line 1035 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = new CEPAddTableSourceNode((CEPStringTokenNode)(val_peek(3).obj), new CEPStringTokenNode(null), false);}
break;
case 121:
//#line 1038 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = new CEPAddTableSourceNode((CEPStringTokenNode)(val_peek(3).obj), new CEPStringTokenNode(null), true);}
break;
case 122:
//#line 1041 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = new CEPAddPushSourceNode((CEPStringTokenNode)(val_peek(4).obj), (CEPStringTokenNode)(val_peek(0).obj), false, true);}
break;
case 123:
//#line 1044 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = new CEPAddPushSourceNode((CEPStringTokenNode)(val_peek(4).obj), (CEPStringTokenNode)(val_peek(0).obj), true, true);}
break;
case 124:
//#line 1050 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = new CEPSetParallelismDegreeNode((CEPStringTokenNode)(val_peek(6).obj), new Integer(val_peek(0).ival), true);}
break;
case 125:
//#line 1053 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = new CEPSetParallelismDegreeNode((CEPStringTokenNode)(val_peek(6).obj), new Integer(val_peek(0).ival), false);}
break;
case 126:
//#line 1058 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = new CEPSetHeartbeatTimeoutNode((CEPStringTokenNode)(val_peek(4).obj), (CEPTimeSpecNode)(val_peek(0).obj), true);}
break;
case 127:
//#line 1061 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = new CEPSetHeartbeatTimeoutNode((CEPStringTokenNode)(val_peek(4).obj), (CEPTimeSpecNode)(val_peek(0).obj), false);}
break;
case 128:
//#line 1064 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = new CEPRemoveHeartbeatTimeoutNode((CEPStringTokenNode)(val_peek(3).obj), true);}
break;
case 129:
//#line 1067 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = new CEPRemoveHeartbeatTimeoutNode((CEPStringTokenNode)(val_peek(3).obj), false);}
break;
case 130:
//#line 1072 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = new CEPExternalRelationNode((CEPStringTokenNode)(val_peek(5).obj), 
                                       (CEPBigIntTokenNode)(val_peek(0).obj));}
break;
case 131:
//#line 1077 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{((CEPQueryRefNode)val_peek(2).obj).setKind( CEPQueryRefKind.ENABLE_MONITOR); 
     ((CEPQueryRefNode)val_peek(2).obj).setBaseTimelineMillisecond(false);yyval.obj = val_peek(2).obj;}
break;
case 132:
//#line 1081 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{((CEPQueryRefNode)val_peek(5).obj).setKind( CEPQueryRefKind.ENABLE_MONITOR);
     ((CEPQueryRefNode)val_peek(5).obj).setBaseTimelineMillisecond(true);yyval.obj = val_peek(5).obj;}
break;
case 133:
//#line 1085 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{((CEPQueryRefNode)val_peek(5).obj).setKind( CEPQueryRefKind.ENABLE_MONITOR);
      ((CEPQueryRefNode)val_peek(5).obj).setBaseTimelineMillisecond(false);yyval.obj = val_peek(5).obj;}
break;
case 134:
//#line 1089 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{((CEPQueryRefNode)val_peek(2).obj).setKind( CEPQueryRefKind.DISABLE_MONITOR); yyval.obj = val_peek(2).obj;}
break;
case 135:
//#line 1094 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{((CEPQueryRefNode)val_peek(5).obj).setKind(CEPQueryRefKind.ORDERING_CONSTRAINT);
      ((CEPQueryRefNode)val_peek(5).obj).setOrderingConstraint(OrderingKind.TOTAL_ORDER);
      yyval.obj = val_peek(5).obj;}
break;
case 136:
//#line 1099 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{((CEPQueryRefNode)val_peek(7).obj).setKind(CEPQueryRefKind.ORDERING_CONSTRAINT);
     ((CEPQueryRefNode)val_peek(7).obj).setOrderingConstraint(OrderingKind.PARTITION_ORDERED);
     ((CEPQueryRefNode)val_peek(7).obj).setParallelPartitioningExpr((CEPExprNode)(val_peek(0).obj));
     yyval.obj = val_peek(7).obj;}
break;
case 137:
//#line 1105 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{((CEPQueryRefNode)val_peek(3).obj).setKind(CEPQueryRefKind.ORDERING_CONSTRAINT);
      ((CEPQueryRefNode)val_peek(3).obj).setOrderingConstraint(OrderingKind.UNORDERED);
      yyval.obj = val_peek(3).obj;}
break;
case 138:
//#line 1112 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = new CEPTableMonitorNode((CEPStringTokenNode)(val_peek(2).obj), true,false);}
break;
case 139:
//#line 1115 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = new CEPTableMonitorNode((CEPStringTokenNode)(val_peek(5).obj), true,true);}
break;
case 140:
//#line 1118 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = new CEPTableMonitorNode((CEPStringTokenNode)(val_peek(5).obj), true,false);}
break;
case 141:
//#line 1121 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = new CEPTableMonitorNode((CEPStringTokenNode)(val_peek(2).obj), false);}
break;
case 142:
//#line 1127 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = new CEPSystemNode(CEPSystemKind.RUNTIME, (CEPConstExprNode)(val_peek(0).obj));}
break;
case 143:
//#line 1130 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = new CEPSystemNode(CEPSystemKind.THREADED, (CEPConstExprNode)(val_peek(0).obj));}
break;
case 144:
//#line 1133 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = new CEPSystemNode(CEPSystemKind.SCHEDNAME, (CEPConstExprNode)(val_peek(0).obj));}
break;
case 145:
//#line 1136 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = new CEPSystemNode(CEPSystemKind.TIMESLICE, (CEPConstExprNode)(val_peek(0).obj));}
break;
case 146:
//#line 1139 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = new CEPSystemRunNode();}
break;
case 147:
//#line 1142 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = new CEPSystemRunNode((CEPConstExprNode)(val_peek(0).obj));}
break;
case 148:
//#line 1145 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = new CEPSystemNode(CEPSystemKind.START_CALLOUT, (CEPConstExprNode)(val_peek(0).obj));}
break;
case 149:
//#line 1150 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = new Boolean(true);}
break;
case 150:
//#line 1155 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{((LinkedList)(val_peek(0).obj)).addFirst(val_peek(2).obj); yyval.obj = val_peek(0).obj;}
break;
case 151:
//#line 1158 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{paramSpecList = new LinkedList(); paramSpecList.add(val_peek(2).obj); paramSpecList.add(val_peek(0).obj); yyval.obj = paramSpecList;}
break;
case 152:
//#line 1163 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = new CEPAttrSpecNode((CEPStringTokenNode)(val_peek(1).obj), (Datatype)(val_peek(0).obj),0);}
break;
case 153:
//#line 1169 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{((LinkedList)(val_peek(0).obj)).addFirst(val_peek(2).obj); yyval.obj = val_peek(0).obj;}
break;
case 154:
//#line 1172 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{dropParamList = new LinkedList(); dropParamList.add(val_peek(2).obj); dropParamList.add(val_peek(0).obj); yyval.obj = dropParamList;}
break;
case 155:
//#line 1177 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = new CEPAttrSpecNode(null, (Datatype)(val_peek(0).obj),0); ((CEPAttrSpecNode)(yyval.obj)).setStartOffset(startOffset); ((CEPAttrSpecNode)(yyval.obj)).setEndOffset(endOffset);}
break;
case 156:
//#line 1182 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{((LinkedList)(val_peek(0).obj)).addFirst(val_peek(2).obj); yyval.obj = val_peek(0).obj;}
break;
case 157:
//#line 1185 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{attrSpecList = new LinkedList(); attrSpecList.add(val_peek(0).obj); yyval.obj = attrSpecList;}
break;
case 158:
//#line 1190 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{((LinkedList)(val_peek(3).obj)).add(val_peek(2).obj); 
      yyval.obj = new CEPRelationAttrSpecsNode((List)val_peek(3).obj, (CEPRelationConstraintNode)val_peek(0).obj);}
break;
case 159:
//#line 1194 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = new CEPRelationAttrSpecsNode((CEPRelationConstraintNode)val_peek(2).obj, (List)val_peek(0).obj);}
break;
case 160:
//#line 1197 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{((LinkedList)(val_peek(5).obj)).add(val_peek(4).obj); ((LinkedList)(val_peek(5).obj)).addAll((LinkedList)val_peek(0).obj); 
      yyval.obj = new CEPRelationAttrSpecsNode((List)val_peek(5).obj, (CEPRelationConstraintNode)val_peek(2).obj);}
break;
case 161:
//#line 1201 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{((LinkedList)(val_peek(0).obj)).addFirst(val_peek(3).obj); ((CEPRelationConstraintNode)(val_peek(2).obj)).addColumn((CEPAttrSpecNode)(val_peek(3).obj)); 
      yyval.obj = new CEPRelationAttrSpecsNode((CEPRelationConstraintNode)val_peek(2).obj, (List)val_peek(0).obj);}
break;
case 162:
//#line 1205 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{((LinkedList)(val_peek(4).obj)).add(val_peek(3).obj); ((CEPRelationConstraintNode)(val_peek(0).obj)).addColumn((CEPAttrSpecNode)(val_peek(1).obj)); 
      ((LinkedList)(val_peek(4).obj)).add(val_peek(1).obj); 
      yyval.obj = new CEPRelationAttrSpecsNode((List)val_peek(4).obj, (CEPRelationConstraintNode)val_peek(0).obj);}
break;
case 163:
//#line 1210 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{((LinkedList)(val_peek(6).obj)).add(val_peek(5).obj); ((CEPRelationConstraintNode)(val_peek(2).obj)).addColumn((CEPAttrSpecNode)(val_peek(3).obj)); 
      ((LinkedList)(val_peek(6).obj)).add(val_peek(3).obj); ((LinkedList)(val_peek(6).obj)).addAll((LinkedList)val_peek(0).obj); 
      yyval.obj = new CEPRelationAttrSpecsNode((List)val_peek(6).obj, (CEPRelationConstraintNode)val_peek(0).obj);}
break;
case 164:
//#line 1215 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{((LinkedList)(val_peek(0).obj)).addFirst(val_peek(5).obj); ((CEPRelationConstraintNode)(val_peek(2).obj)).addColumn((CEPAttrSpecNode)(val_peek(3).obj)); 
      ((LinkedList)(val_peek(0).obj)).addFirst(val_peek(3).obj); 
      yyval.obj = new CEPRelationAttrSpecsNode((CEPRelationConstraintNode)val_peek(2).obj, (List)val_peek(0).obj);}
break;
case 165:
//#line 1220 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{attrSpecList = new LinkedList(); attrSpecList.addFirst(val_peek(1).obj); 
      ((CEPRelationConstraintNode)(val_peek(0).obj)).addColumn((CEPAttrSpecNode)(val_peek(1).obj)); 
      yyval.obj = new CEPRelationAttrSpecsNode((List)attrSpecList, (CEPRelationConstraintNode)val_peek(0).obj);}
break;
case 166:
//#line 1225 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{attrSpecList = new LinkedList(); attrSpecList.addFirst(val_peek(2).obj); 
      yyval.obj = new CEPRelationAttrSpecsNode((List)attrSpecList, (CEPRelationConstraintNode)val_peek(0).obj);}
break;
case 167:
//#line 1229 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = new CEPRelationAttrSpecsNode((List)val_peek(0).obj);}
break;
case 168:
//#line 1234 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{((LinkedList)(val_peek(0).obj)).addFirst((CEPStringTokenNode)(val_peek(2).obj)); yyval.obj = val_peek(0).obj;}
break;
case 169:
//#line 1237 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{attrNameList = new LinkedList(); attrNameList.add((CEPStringTokenNode)(val_peek(0).obj)); yyval.obj = attrNameList;}
break;
case 170:
//#line 1242 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = new CEPAttrSpecNode((CEPStringTokenNode)(val_peek(1).obj), (Datatype)(val_peek(0).obj));}
break;
case 171:
//#line 1245 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = new CEPAttrSpecNode((CEPStringTokenNode)(val_peek(4).obj), (Datatype)(val_peek(3).obj), (CEPIntTokenNode)val_peek(1).obj);}
break;
case 172:
//#line 1248 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{Datatype extensibleType = CartridgeHelper.getArrayType(execContext, (List) val_peek(2).obj);
      yyval.obj = new CEPAttrSpecNode((CEPStringTokenNode)(val_peek(3).obj), extensibleType);}
break;
case 173:
//#line 1252 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = new CEPAttrSpecNode((CEPStringTokenNode)(val_peek(6).obj), (Datatype)(val_peek(5).obj), (CEPIntTokenNode)val_peek(3).obj , (CEPIntTokenNode)val_peek(1).obj);}
break;
case 174:
//#line 1258 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = new CEPRelationConstraintNode(); ((CEPRelationConstraintNode)(yyval.obj)).setStartOffset(startOffset); ((CEPRelationConstraintNode)(yyval.obj)).setEndOffset(endOffset);}
break;
case 175:
//#line 1263 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = new CEPRelationConstraintNode((List)(val_peek(1).obj));}
break;
case 176:
//#line 1268 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{ yyval.obj = val_peek(0).obj;}
break;
case 177:
//#line 1271 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{
       yyval.obj = new CEPQueryStreamNode((CEPRelationNode)(val_peek(2).obj), (RelToStrOp)(val_peek(4).obj), (List)(val_peek(0).obj));
     }
break;
case 178:
//#line 1276 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{ 
       yyval.obj = new CEPQueryStreamNode((CEPRelationNode)(val_peek(1).obj), RelToStrOp.RSTREAM); 
     }
break;
case 179:
//#line 1281 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{ yyval.obj = val_peek(0).obj;}
break;
case 180:
//#line 1284 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{ yyval.obj = val_peek(0).obj; }
break;
case 181:
//#line 1287 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{
       yyval.obj = new CEPQueryStreamNode((CEPRelationNode)(val_peek(2).obj), (RelToStrOp)(val_peek(4).obj), (List)(val_peek(0).obj));
     }
break;
case 182:
//#line 1292 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{
       yyval.obj = new CEPQueryStreamNode((CEPRelationNode)(val_peek(2).obj), (RelToStrOp)(val_peek(4).obj), (List)(val_peek(0).obj));
     }
break;
case 183:
//#line 1297 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{ 
       yyval.obj = new CEPQueryStreamNode((CEPRelationNode)(val_peek(1).obj), RelToStrOp.RSTREAM); 
     }
break;
case 184:
//#line 1302 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{ 
       yyval.obj = new CEPQueryStreamNode((CEPRelationNode)(val_peek(1).obj), RelToStrOp.RSTREAM); 
     }
break;
case 185:
//#line 1310 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = RelToStrOp.ISTREAM;}
break;
case 186:
//#line 1313 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = RelToStrOp.DSTREAM;}
break;
case 187:
//#line 1318 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = new CEPSFWQueryNode((CEPSelectListNode)(val_peek(5).obj), (List)(val_peek(4).obj), (CEPBooleanExprNode)(val_peek(3).obj), (List)(val_peek(2).obj), (CEPBooleanExprNode)(val_peek(1).obj), (CEPOrderByNode)(val_peek(0).obj));}
break;
case 188:
//#line 1323 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{
       CEPQueryRelationNode queryRelNode = (CEPQueryRelationNode)(val_peek(1).obj);
       queryRelNode.setEvaluateClause((CEPSlideExprNode)(val_peek(0).obj));
       yyval.obj = queryRelNode;
     }
break;
case 189:
//#line 1332 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = new CEPSelectListNode(true, (List)(val_peek(0).obj));}
break;
case 190:
//#line 1335 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = new CEPSelectListNode(false, (List)(val_peek(0).obj));}
break;
case 191:
//#line 1338 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = new CEPSelectListNode(true); ((CEPSelectListNode)yyval.obj).setStartOffset(0); ((CEPSelectListNode)yyval.obj).setEndOffset(endOffset);}
break;
case 192:
//#line 1341 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = new CEPSelectListNode(false); ((CEPSelectListNode)yyval.obj).setStartOffset(0); ((CEPSelectListNode)yyval.obj).setEndOffset(endOffset);}
break;
case 193:
//#line 1346 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = val_peek(0).obj;}
break;
case 194:
//#line 1350 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{/*empty*/ yyval.obj=null;}
break;
case 195:
//#line 1352 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = val_peek(0).obj;}
break;
case 196:
//#line 1354 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = val_peek(0).obj;}
break;
case 197:
//#line 1358 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = new CEPOrderByNode((List)(val_peek(0).obj), null);}
break;
case 198:
//#line 1363 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = new CEPOrderByNode((List)(val_peek(2).obj), new CEPOrderByTopExprNode((CEPIntTokenNode)(val_peek(0).obj), startOffset, endOffset) );}
break;
case 199:
//#line 1366 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = new CEPOrderByNode((List)(val_peek(2).obj), new CEPOrderByTopExprNode((CEPIntTokenNode)(val_peek(0).obj), (List)(val_peek(5).obj), startOffset, endOffset) );}
break;
case 200:
//#line 1369 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = new CEPOrderByNode((List)(val_peek(3).obj), new CEPOrderByTopExprNode((CEPIntTokenNode)(val_peek(1).obj), (List)(val_peek(0).obj), startOffset, endOffset) );}
break;
case 201:
//#line 1373 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{/*empty  */ yyval.obj=null;}
break;
case 202:
//#line 1375 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = val_peek(0).obj;}
break;
case 203:
//#line 1379 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{/*empty  */ yyval.obj=null;}
break;
case 204:
//#line 1381 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = val_peek(0).obj;}
break;
case 205:
//#line 1385 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{/*empty  */ yyval.obj=null;}
break;
case 206:
//#line 1387 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = val_peek(0).obj;}
break;
case 207:
//#line 1390 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{ /* empty */ yyval.obj = null; }
break;
case 208:
//#line 1392 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{ yyval.obj = val_peek(1).obj;}
break;
case 209:
//#line 1397 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{((LinkedList)(val_peek(0).obj)).addFirst(val_peek(2).obj); yyval.obj = val_peek(0).obj;}
break;
case 210:
//#line 1400 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{projList = new LinkedList(); projList.add(val_peek(0).obj); yyval.obj = projList;}
break;
case 211:
//#line 1405 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = new CEPRelationStarNode((CEPStringTokenNode)(val_peek(1).obj));}
break;
case 212:
//#line 1408 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = val_peek(0).obj;}
break;
case 213:
//#line 1411 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{((CEPExprNode)val_peek(2).obj).setAlias((CEPStringTokenNode)(val_peek(0).obj)); yyval.obj = val_peek(2).obj;}
break;
case 214:
//#line 1417 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{((LinkedList)(val_peek(0).obj)).addFirst(val_peek(2).obj); yyval.obj = val_peek(0).obj;}
break;
case 215:
//#line 1420 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{usingList = new LinkedList(); usingList.add(val_peek(0).obj); yyval.obj = usingList;}
break;
case 216:
//#line 1425 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = val_peek(0).obj;}
break;
case 217:
//#line 1430 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = val_peek(0).obj;}
break;
case 218:
//#line 1433 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = val_peek(0).obj;}
break;
case 219:
//#line 1438 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{((LinkedList)(val_peek(0).obj)).addFirst(val_peek(2).obj); yyval.obj = val_peek(0).obj;}
break;
case 220:
//#line 1441 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{orderList = new LinkedList(); orderList.add(val_peek(0).obj); yyval.obj = orderList;}
break;
case 221:
//#line 1446 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = new CEPOrderByExprNode((CEPExprNode)(val_peek(0).obj),  new Boolean(true), new Boolean(false));}
break;
case 222:
//#line 1449 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = new CEPOrderByExprNode((CEPExprNode)(val_peek(1).obj), new Boolean(true), (Boolean)(val_peek(0).obj)); }
break;
case 223:
//#line 1452 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = new CEPOrderByExprNode((CEPExprNode)(val_peek(1).obj), (Boolean)(val_peek(0).obj), new Boolean(false));}
break;
case 224:
//#line 1455 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = new CEPOrderByExprNode((CEPExprNode)(val_peek(2).obj), (Boolean)(val_peek(1).obj), (Boolean)(val_peek(0).obj));}
break;
case 225:
//#line 1460 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = val_peek(0).obj;}
break;
case 226:
//#line 1463 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = val_peek(0).obj;}
break;
case 227:
//#line 1469 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = new Boolean(true);}
break;
case 228:
//#line 1472 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = new Boolean(false);}
break;
case 229:
//#line 1477 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = new Boolean(true);}
break;
case 230:
//#line 1480 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = new Boolean(false);}
break;
case 231:
//#line 1485 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{((LinkedList)(val_peek(0).obj)).addFirst(val_peek(2).obj); yyval.obj = val_peek(0).obj;}
break;
case 232:
//#line 1488 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{attrList = new LinkedList(); attrList.add(val_peek(0).obj); yyval.obj = attrList;}
break;
case 233:
//#line 1493 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{((LinkedList)(val_peek(0).obj)).addFirst(val_peek(2).obj); yyval.obj = val_peek(0).obj;}
break;
case 234:
//#line 1496 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{relList = new LinkedList(); relList.add(val_peek(0).obj); yyval.obj = relList;}
break;
case 235:
//#line 1501 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = new CEPOuterJoinRelationNode((CEPRelationNode)val_peek(1).obj, (List)val_peek(0).obj);}
break;
case 236:
//#line 1504 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = (CEPRelationNode)(val_peek(0).obj);}
break;
case 237:
//#line 1510 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{((LinkedList)(val_peek(0).obj)).addFirst(val_peek(1).obj); yyval.obj = val_peek(0).obj;}
break;
case 238:
//#line 1513 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{outerRightList = new LinkedList(); outerRightList.add(val_peek(0).obj); yyval.obj = outerRightList;}
break;
case 239:
//#line 1519 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = new CEPRightOuterJoinNode((CEPRelationNode)val_peek(2).obj, (CEPBooleanExprNode)(val_peek(0).obj), (OuterJoinType)(val_peek(3).obj));}
break;
case 240:
//#line 1525 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = OuterJoinType.LEFT_OUTER;}
break;
case 241:
//#line 1528 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = OuterJoinType.LEFT_OUTER;}
break;
case 242:
//#line 1531 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = OuterJoinType.RIGHT_OUTER;}
break;
case 243:
//#line 1534 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = OuterJoinType.RIGHT_OUTER;}
break;
case 244:
//#line 1537 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = OuterJoinType.FULL_OUTER;}
break;
case 245:
//#line 1540 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = OuterJoinType.FULL_OUTER;}
break;
case 246:
//#line 1545 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{
       yyval.obj = new CEPWindowRelationNode(new 
                CEPBaseStreamNode((CEPStringTokenNode)(val_peek(3).obj)), 
                (CEPWindowExprNode)(val_peek(1).obj));
     }
break;
case 247:
//#line 1552 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{
       yyval.obj = new 
              CEPWindowRelationNode(new CEPStreamSubqueryNode((CEPQueryNode)(val_peek(4).obj)),
                                    (CEPWindowExprNode)(val_peek(1).obj));
     }
break;
case 248:
//#line 1559 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{
       yyval.obj = new 
         CEPWindowRelationNode(new
         CEPBaseStreamNode((CEPStringTokenNode)(val_peek(5).obj), (CEPStringTokenNode)(val_peek(0).obj)), 
                           (CEPWindowExprNode)(val_peek(3).obj), 
                           (CEPStringTokenNode)(val_peek(0).obj));
     }
break;
case 249:
//#line 1568 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{
       yyval.obj = new 
           CEPWindowRelationNode(new CEPStreamSubqueryNode((CEPQueryNode)(val_peek(6).obj),
                                                    (CEPStringTokenNode)(val_peek(0).obj)),
                                (CEPWindowExprNode)(val_peek(3).obj));
     }
break;
case 250:
//#line 1576 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = new CEPBaseRelationNode((CEPStringTokenNode)(val_peek(0).obj));}
break;
case 251:
//#line 1579 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{
       yyval.obj = new CEPRelationSubqueryNode((CEPQueryNode)(val_peek(1).obj));
     }
break;
case 252:
//#line 1584 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = new CEPBaseRelationNode((CEPStringTokenNode)(val_peek(2).obj), (CEPStringTokenNode)(val_peek(0).obj));}
break;
case 253:
//#line 1587 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{
       yyval.obj = new CEPRelationSubqueryNode((CEPQueryNode)(val_peek(3).obj), 
                                        (CEPStringTokenNode)(val_peek(0).obj));
     }
break;
case 254:
//#line 1593 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{
       yyval.obj = new 
           CEPWindowRelationNode(new CEPPatternStreamNode(new
                          CEPBaseStreamNode((CEPStringTokenNode)(val_peek(3).obj)),
                          (CEPRecognizeNode)(val_peek(2).obj), 
                          (CEPStringTokenNode)(val_peek(0).obj)), 
                          new CEPTimeWindowExprNode(new 
                          CEPTimeSpecNode(TimeUnit.SECOND, Constants.INFINITE)),
                          (CEPStringTokenNode)(val_peek(0).obj)); 
     }
break;
case 255:
//#line 1605 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{
       yyval.obj = new 
           CEPWindowRelationNode(new 
             CEPPatternStreamNode(new CEPStreamSubqueryNode((CEPQueryNode)(val_peek(6).obj),
                                                            (CEPStringTokenNode)(val_peek(3).obj)),
                   (CEPRecognizeNode)(val_peek(2).obj), (CEPStringTokenNode)(val_peek(0).obj)), 
                   new CEPTimeWindowExprNode(new 
                         CEPTimeSpecNode(TimeUnit.SECOND, Constants.INFINITE)),
                        (CEPStringTokenNode)(val_peek(0).obj)); 
     }
break;
case 256:
//#line 1618 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{
       yyval.obj = new 
           CEPWindowRelationNode(new 
             CEPMultiStreamNode((List)(val_peek(4).obj),
                   (CEPRecognizeNode)(val_peek(2).obj), (CEPStringTokenNode)(val_peek(0).obj)), 
                   new CEPTimeWindowExprNode(new 
                         CEPTimeSpecNode(TimeUnit.SECOND, Constants.INFINITE)),
                        (CEPStringTokenNode)(val_peek(0).obj)); 
     }
break;
case 257:
//#line 1629 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = new CEPWindowRelationNode(new CEPXmlTableStreamNode(new CEPBaseStreamNode((CEPStringTokenNode)(val_peek(3).obj)), (CEPXmlTableNode)(val_peek(2).obj), (CEPStringTokenNode)(val_peek(0).obj)), new CEPTimeWindowExprNode(new CEPTimeSpecNode(TimeUnit.SECOND, Constants.INFINITE)), (CEPStringTokenNode)(val_peek(0).obj)); }
break;
case 258:
//#line 1632 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = new CEPWindowRelationNode(new CEPBaseStreamNode((CEPStringTokenNode)(val_peek(3).obj)), (CEPWindowExprNode)(val_peek(1).obj));}
break;
case 259:
//#line 1635 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = new CEPWindowRelationNode(new CEPBaseStreamNode((CEPStringTokenNode)(val_peek(5).obj),(CEPStringTokenNode)(val_peek(0).obj)), (CEPWindowExprNode)(val_peek(3).obj),(CEPStringTokenNode)(val_peek(0).obj));}
break;
case 260:
//#line 1638 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = new CEPTableFunctionRelationNode((CEPExprNode)(val_peek(5).obj), (CEPStringTokenNode)(val_peek(3).obj), (CEPStringTokenNode)(val_peek(0).obj));}
break;
case 261:
//#line 1641 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = new CEPTableFunctionRelationNode((CEPExprNode)(val_peek(7).obj), (CEPStringTokenNode)(val_peek(5).obj), (Datatype)(val_peek(3).obj), (CEPStringTokenNode)(val_peek(0).obj));}
break;
case 262:
//#line 1647 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{((LinkedList)(val_peek(0).obj)).addFirst(val_peek(4).obj); ((LinkedList)(val_peek(0).obj)).addFirst(val_peek(2).obj); yyval.obj = val_peek(0).obj;}
break;
case 263:
//#line 1649 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{srcList = new LinkedList(); srcList.add(val_peek(2).obj); srcList.add(val_peek(0).obj); yyval.obj = srcList;}
break;
case 264:
//#line 1654 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{((LinkedList)(val_peek(0).obj)).addFirst(val_peek(2).obj); yyval.obj = val_peek(0).obj;}
break;
case 265:
//#line 1656 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{srcList = new LinkedList(); srcList.add(val_peek(0).obj); yyval.obj = srcList;}
break;
case 266:
//#line 1661 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = new CEPBaseStreamNode((CEPStringTokenNode)(val_peek(0).obj));}
break;
case 267:
//#line 1664 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = new CEPBaseStreamNode((CEPStringTokenNode)(val_peek(2).obj), (CEPStringTokenNode)(val_peek(0).obj));}
break;
case 268:
//#line 1667 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{
       yyval.obj = new CEPStreamSubqueryNode((CEPQueryNode)(val_peek(3).obj), 
                                        (CEPStringTokenNode)(val_peek(0).obj));
     }
break;
case 269:
//#line 1675 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = new CEPExtensibleWindowExprNode((CEPStringTokenNode)(val_peek(0).obj));}
break;
case 270:
//#line 1678 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = new CEPExtensibleWindowExprNode((CEPStringTokenNode)(val_peek(2).obj));}
break;
case 271:
//#line 1681 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = new CEPExtensibleWindowExprNode((CEPStringTokenNode)(val_peek(3).obj), (List)(val_peek(1).obj));}
break;
case 272:
//#line 1686 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{((LinkedList)(val_peek(0).obj)).addFirst(val_peek(2).obj); yyval.obj = val_peek(0).obj;}
break;
case 273:
//#line 1689 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{argList = new LinkedList(); argList.add(val_peek(0).obj); yyval.obj = argList;}
break;
case 274:
//#line 1694 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = new CEPTimeWindowExprNode((CEPTimeSpecNode)(val_peek(0).obj));}
break;
case 275:
//#line 1697 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = new CEPTimeWindowExprNode((CEPTimeSpecNode)(val_peek(2).obj), (CEPTimeSpecNode)(val_peek(0).obj));}
break;
case 276:
//#line 1700 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = new CEPTimeWindowExprNode(new CEPTimeSpecNode(TimeUnit.SECOND, 0)); ((CEPTimeWindowExprNode)yyval.obj).setStartOffset(startOffset); ((CEPTimeWindowExprNode)yyval.obj).setEndOffset(endOffset);}
break;
case 277:
//#line 1703 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = new CEPRowsWindowExprNode((CEPIntTokenNode)(val_peek(0).obj));}
break;
case 278:
//#line 1706 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = new CEPRowsWindowExprNode((CEPIntTokenNode)(val_peek(2).obj),(CEPIntTokenNode)(val_peek(0).obj));}
break;
case 279:
//#line 1709 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = new CEPTimeWindowExprNode(new CEPTimeSpecNode(TimeUnit.SECOND, Constants.INFINITE));}
break;
case 280:
//#line 1712 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = new CEPPartnWindowExprNode((List)val_peek(2).obj, (CEPIntTokenNode)(val_peek(0).obj));}
break;
case 281:
//#line 1715 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = new CEPPartnWindowExprNode((List)val_peek(4).obj, (CEPIntTokenNode)(val_peek(2).obj), (CEPTimeSpecNode)val_peek(0).obj);}
break;
case 282:
//#line 1718 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = new CEPPartnWindowExprNode((List)val_peek(6).obj, (CEPIntTokenNode)(val_peek(4).obj), (CEPTimeSpecNode)val_peek(2).obj, (CEPTimeSpecNode)val_peek(0).obj);}
break;
case 283:
//#line 1721 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = new CEPValueWindowExprNode((CEPConstExprNode)(val_peek(2).obj), (CEPExprNode)(new CEPAttrNode(StreamPseudoColumn.ELEMENT_TIME.getColumnName())));}
break;
case 284:
//#line 1724 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = new CEPValueWindowExprNode((CEPConstExprNode)(val_peek(2).obj), (CEPExprNode)(new CEPAttrNode((CEPStringTokenNode)(val_peek(0).obj))));}
break;
case 285:
//#line 1727 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = new CEPValueWindowExprNode((CEPTimeSpecNode)(val_peek(2).obj), (CEPExprNode)(new CEPAttrNode((CEPStringTokenNode)(val_peek(0).obj))));}
break;
case 286:
//#line 1730 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = new CEPValueWindowExprNode((CEPTimeSpecNode)(val_peek(2).obj), (CEPExprNode)(new CEPAttrNode(StreamPseudoColumn.ELEMENT_TIME.getColumnName())));}
break;
case 287:
//#line 1733 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = new CEPValueWindowExprNode((CEPConstExprNode)(val_peek(4).obj), 
                                      (CEPExprNode)(new CEPAttrNode((CEPStringTokenNode)(val_peek(2).obj))), 
                                      (CEPBigIntTokenNode)(val_peek(0).obj));}
break;
case 288:
//#line 1738 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = new CEPValueWindowExprNode((CEPConstExprNode)(val_peek(4).obj), 
                                      (CEPExprNode)(new CEPAttrNode(StreamPseudoColumn.ELEMENT_TIME.getColumnName())), 
                                      (CEPBigIntTokenNode)(val_peek(0).obj));}
break;
case 289:
//#line 1743 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = new CEPValueWindowExprNode((CEPTimeSpecNode)(val_peek(4).obj), 
                                      (CEPExprNode)(new CEPAttrNode((CEPStringTokenNode)(val_peek(2).obj))),
                                      (CEPTimeSpecNode)(val_peek(0).obj));}
break;
case 290:
//#line 1748 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = new CEPValueWindowExprNode((CEPTimeSpecNode)(val_peek(4).obj), 
                                      (CEPExprNode)(new CEPAttrNode(StreamPseudoColumn.ELEMENT_TIME.getColumnName())),
                                      (CEPTimeSpecNode)(val_peek(0).obj));}
break;
case 291:
//#line 1753 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = new CEPValueWindowExprNode(ValueWindowType.CURRENT_HOUR, 
                                      (CEPExprNode)(new CEPAttrNode((CEPStringTokenNode)(val_peek(0).obj))), 
                                      null, 
                                      null,
                                      null);}
break;
case 292:
//#line 1760 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = new CEPValueWindowExprNode(ValueWindowType.CURRENT_HOUR, 
                                      (CEPExprNode)(new CEPAttrNode(StreamPseudoColumn.ELEMENT_TIME.getColumnName())), 
                                      null, 
                                      null,
                                      null);}
break;
case 293:
//#line 1767 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = new CEPValueWindowExprNode(ValueWindowType.CURRENT_PERIOD, 
                                      (CEPExprNode)(new CEPAttrNode((CEPStringTokenNode)(val_peek(0).obj))), 
                                      (CEPStringTokenNode)(val_peek(5).obj), 
                                      (CEPStringTokenNode)(val_peek(3).obj),
                                      null);}
break;
case 294:
//#line 1774 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = new CEPValueWindowExprNode(ValueWindowType.CURRENT_PERIOD, 
                                      (CEPExprNode)(new CEPAttrNode(StreamPseudoColumn.ELEMENT_TIME.getColumnName())), 
                                      (CEPStringTokenNode)(val_peek(5).obj), 
                                      (CEPStringTokenNode)(val_peek(3).obj),
                                      null);}
break;
case 295:
//#line 1781 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = new CEPValueWindowExprNode(ValueWindowType.CURRENT_HOUR, 
                                      (CEPExprNode)(new CEPAttrNode((CEPStringTokenNode)(val_peek(2).obj))), 
                                      null, 
                                      null,
                                      (CEPTimeSpecNode)(val_peek(0).obj));}
break;
case 296:
//#line 1788 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = new CEPValueWindowExprNode(ValueWindowType.CURRENT_HOUR, 
                                      (CEPExprNode)(new CEPAttrNode(StreamPseudoColumn.ELEMENT_TIME.getColumnName())), 
                                      null, 
                                      null,
                                      (CEPTimeSpecNode)(val_peek(0).obj));}
break;
case 297:
//#line 1795 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = new CEPValueWindowExprNode(ValueWindowType.CURRENT_PERIOD, 
                                      (CEPExprNode)(new CEPAttrNode((CEPStringTokenNode)(val_peek(2).obj))), 
                                      (CEPStringTokenNode)(val_peek(7).obj), 
                                      (CEPStringTokenNode)(val_peek(5).obj),
                                      (CEPTimeSpecNode)(val_peek(0).obj));}
break;
case 298:
//#line 1802 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = new CEPValueWindowExprNode(ValueWindowType.CURRENT_PERIOD, 
                                      (CEPExprNode)(new CEPAttrNode(StreamPseudoColumn.ELEMENT_TIME.getColumnName())), 
                                      (CEPStringTokenNode)(val_peek(7).obj), 
                                      (CEPStringTokenNode)(val_peek(5).obj),
                                      (CEPTimeSpecNode)(val_peek(0).obj));}
break;
case 299:
//#line 1811 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = val_peek(0).obj;}
break;
case 300:
//#line 1814 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = val_peek(0).obj;}
break;
case 301:
//#line 1819 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = new CEPTimeSpecNode(TimeUnit.NOTIMEUNIT, (CEPIntTokenNode)(val_peek(0).obj));}
break;
case 302:
//#line 1822 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = new CEPTimeSpecNode(TimeUnit.NOTIMEUNIT, (CEPBigIntTokenNode)(val_peek(0).obj));}
break;
case 303:
//#line 1825 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = new CEPTimeSpecNode(TimeUnit.NOTIMEUNIT, (CEPExprNode)(val_peek(0).obj));}
break;
case 304:
//#line 1830 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = new CEPTimeSpecNode((TimeUnit)(val_peek(0).obj),(CEPIntTokenNode)(val_peek(1).obj));}
break;
case 305:
//#line 1833 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = new CEPTimeSpecNode((TimeUnit)(val_peek(0).obj), (CEPBigIntTokenNode)(val_peek(1).obj));}
break;
case 306:
//#line 1836 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = new CEPTimeSpecNode((TimeUnit)(val_peek(0).obj),(CEPExprNode)(val_peek(1).obj));}
break;
case 307:
//#line 1841 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = TimeUnit.NANOSECOND;}
break;
case 308:
//#line 1844 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = TimeUnit.MICROSECOND;}
break;
case 309:
//#line 1847 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = TimeUnit.MILLISECOND;}
break;
case 310:
//#line 1850 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = TimeUnit.SECOND;}
break;
case 311:
//#line 1853 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = TimeUnit.MINUTE;}
break;
case 312:
//#line 1856 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = TimeUnit.HOUR;}
break;
case 313:
//#line 1859 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = TimeUnit.DAY;}
break;
case 314:
//#line 1862 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = TimeUnit.MONTH;}
break;
case 315:
//#line 1865 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = TimeUnit.YEAR;}
break;
case 316:
//#line 1868 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = TimeUnit.NANOSECOND;}
break;
case 317:
//#line 1873 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = new CEPXmlTableNode((CEPStringTokenNode)(val_peek(7).obj), (List)(val_peek(3).obj), (List)(val_peek(1).obj), (List)(val_peek(9).obj));}
break;
case 318:
//#line 1876 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = new CEPXmlTableNode((CEPStringTokenNode)(val_peek(7).obj), (List)val_peek(3).obj, (List)val_peek(1).obj);}
break;
case 319:
//#line 1881 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = (List)(val_peek(1).obj);}
break;
case 320:
//#line 1886 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{((LinkedList)(val_peek(0).obj)).addFirst(val_peek(2).obj); yyval.obj = val_peek(0).obj;}
break;
case 321:
//#line 1889 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{xmlNamespaceList = new LinkedList(); xmlNamespaceList.add(val_peek(0).obj); yyval.obj = xmlNamespaceList;}
break;
case 322:
//#line 1894 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = new CEPXmlNamespaceNode((CEPStringTokenNode)(val_peek(2).obj), (CEPStringTokenNode)(val_peek(0).obj));}
break;
case 323:
//#line 1897 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = new CEPXmlNamespaceNode((CEPStringTokenNode)(val_peek(2).obj), (CEPStringTokenNode)(val_peek(0).obj));}
break;
case 324:
//#line 1900 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = new CEPXmlNamespaceNode((CEPStringTokenNode)(val_peek(2).obj), (CEPStringTokenNode)(val_peek(0).obj));}
break;
case 325:
//#line 1903 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = new CEPXmlNamespaceNode((CEPStringTokenNode)(val_peek(2).obj), (CEPStringTokenNode)(val_peek(0).obj));}
break;
case 326:
//#line 1906 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = new CEPXmlNamespaceNode((CEPStringTokenNode)(val_peek(0).obj));}
break;
case 327:
//#line 1909 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = new CEPXmlNamespaceNode((CEPStringTokenNode)(val_peek(0).obj));}
break;
case 328:
//#line 1914 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{((LinkedList)(val_peek(0).obj)).addFirst(val_peek(2).obj); yyval.obj = val_peek(0).obj;}
break;
case 329:
//#line 1917 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{xtblColList = new LinkedList(); xtblColList.add(val_peek(0).obj); yyval.obj = xtblColList;}
break;
case 330:
//#line 1923 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = new CEPXmlTableColumnNode((CEPStringTokenNode)(val_peek(3).obj), (Datatype)(val_peek(2).obj), (CEPStringTokenNode)(val_peek(0).obj));}
break;
case 331:
//#line 1926 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = new CEPXmlTableColumnNode((CEPStringTokenNode)(val_peek(6).obj), (Datatype)(val_peek(5).obj), (CEPIntTokenNode)(val_peek(3).obj), (CEPStringTokenNode)(val_peek(0).obj));}
break;
case 332:
//#line 1932 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{defaultSubsetRequired = false; yyval.obj = val_peek(0).obj;}
break;
case 333:
//#line 1936 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = new CEPRecognizeNode((List)(val_peek(10).obj), (CEPPatternMeasuresNode)(val_peek(9).obj), (PatternSkip)(val_peek(8).obj), (CEPPatternNode)(val_peek(4).obj), (CEPPatternDefinitionNode)(val_peek(1).obj), (List)(val_peek(2).obj), (CEPPatternDurationNode)(val_peek(3).obj), null, defaultSubsetRequired);}
break;
case 334:
//#line 1939 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = new CEPRecognizeNode((List)(val_peek(7).obj), (CEPPatternMeasuresNode)(val_peek(6).obj), (PatternSkip)(val_peek(5).obj), (CEPPatternNode)(val_peek(4).obj), (CEPPatternDefinitionNode)(val_peek(1).obj), (List)(val_peek(2).obj), null, (CEPPatternWithinNode)(val_peek(3).obj), defaultSubsetRequired);}
break;
case 335:
//#line 1944 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = new CEPPatternDurationNode((CEPTimeSpecNode)(val_peek(0).obj));}
break;
case 336:
//#line 1946 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = new CEPPatternDurationNode((CEPTimeSpecNode)(val_peek(0).obj), true);}
break;
case 337:
//#line 1950 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{ /* empty */ yyval.obj = null; }
break;
case 338:
//#line 1952 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = new CEPPatternWithinNode((CEPTimeSpecNode)(val_peek(0).obj));}
break;
case 339:
//#line 1954 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = new CEPPatternWithinNode((CEPTimeSpecNode)(val_peek(0).obj), true);}
break;
case 340:
//#line 1958 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{ /* empty */ yyval.obj = null;}
break;
case 341:
//#line 1960 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = val_peek(0).obj;}
break;
case 342:
//#line 1965 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{((LinkedList)(val_peek(0).obj)).addFirst(val_peek(1).obj); yyval.obj = val_peek(0).obj;}
break;
case 343:
//#line 1968 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{subsetList = new LinkedList(); subsetList.add(val_peek(0).obj); yyval.obj = subsetList;}
break;
case 344:
//#line 1973 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = new CEPSubsetDefNode((CEPStringTokenNode)(val_peek(4).obj), (List)(val_peek(1).obj));}
break;
case 345:
//#line 1978 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = val_peek(0).obj;}
break;
case 346:
//#line 1983 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{((LinkedList)(val_peek(0).obj)).addFirst(val_peek(2).obj); yyval.obj = val_peek(0).obj;}
break;
case 347:
//#line 1986 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{corrList = new LinkedList(); corrList.add(val_peek(0).obj); yyval.obj = corrList;}
break;
case 348:
//#line 1991 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = new CEPPatternNode((CEPRegexpNode)(val_peek(1).obj));}
break;
case 349:
//#line 1996 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = new CEPComplexRegexpNode((RegexpOp)(val_peek(0).obj), new CEPSimpleRegexpNode((CEPStringTokenNode)(val_peek(1).obj))); }
break;
case 350:
//#line 1999 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = new CEPSimpleRegexpNode((CEPStringTokenNode)val_peek(0).obj);}
break;
case 351:
//#line 2002 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{
     	yyval.obj = val_peek(1).obj;
     	((CEPRegexpNode)yyval.obj).setMyString("(" + (val_peek(1).obj).toString() + ")");
     }
break;
case 352:
//#line 2008 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{
     	yyval.obj = new CEPComplexRegexpNode((RegexpOp)(val_peek(0).obj), (CEPRegexpNode) (val_peek(2).obj));
  		((CEPComplexRegexpNode)yyval.obj).setMyString("(" + (val_peek(2).obj).toString() + ")" + (val_peek(0).obj).toString());   	
     }
break;
case 353:
//#line 2014 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = new CEPComplexRegexpNode(RegexpOp.ALTERNATION, (CEPRegexpNode)(val_peek(2).obj), (CEPRegexpNode)(val_peek(0).obj)); }
break;
case 354:
//#line 2017 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = new CEPComplexRegexpNode(RegexpOp.CONCAT, (CEPRegexpNode)(val_peek(1).obj), (CEPRegexpNode)(val_peek(0).obj)); }
break;
case 355:
//#line 2022 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = val_peek(0).obj;}
break;
case 356:
//#line 2027 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = RegexpOp.LAZY_STAR;}
break;
case 357:
//#line 2030 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = RegexpOp.LAZY_PLUS;}
break;
case 358:
//#line 2033 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = RegexpOp.LAZY_QUESTION;}
break;
case 359:
//#line 2036 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = RegexpOp.GREEDY_STAR;}
break;
case 360:
//#line 2039 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = RegexpOp.GREEDY_PLUS;}
break;
case 361:
//#line 2042 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = RegexpOp.GREEDY_QUESTION;}
break;
case 362:
//#line 2046 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{ /* empty */ yyval.obj = null; }
break;
case 363:
//#line 2049 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = val_peek(0).obj;}
break;
case 364:
//#line 2054 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = val_peek(0).obj;}
break;
case 365:
//#line 2058 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{insideDefineOrMeasures = true;}
break;
case 366:
//#line 2059 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{insideDefineOrMeasures = false; yyval.obj = new CEPPatternMeasuresNode((List)(val_peek(0).obj));}
break;
case 367:
//#line 2064 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{((LinkedList)(val_peek(0).obj)).addFirst(val_peek(2).obj); yyval.obj = val_peek(0).obj;}
break;
case 368:
//#line 2067 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{measureColList = new LinkedList(); measureColList.add(val_peek(0).obj); yyval.obj = measureColList;}
break;
case 369:
//#line 2072 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{((CEPExprNode)val_peek(2).obj).setAlias((CEPStringTokenNode)(val_peek(0).obj)); yyval.obj = val_peek(2).obj;}
break;
case 370:
//#line 2076 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{ /*empty - skip past last row */ yyval.obj = PatternSkip.SKIP_PAST_LAST_ROW;}
break;
case 371:
//#line 2078 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = PatternSkip.ALL_MATCHES;}
break;
case 372:
//#line 2082 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{insideDefineOrMeasures = true;}
break;
case 373:
//#line 2083 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{insideDefineOrMeasures = false; yyval.obj = new CEPPatternDefinitionNode((List)(val_peek(0).obj));}
break;
case 374:
//#line 2088 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{((LinkedList)(val_peek(0).obj)).addFirst(val_peek(2).obj); yyval.obj = val_peek(0).obj;}
break;
case 375:
//#line 2091 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{corrNameDefList = new LinkedList(); corrNameDefList.add(val_peek(0).obj); yyval.obj = corrNameDefList;}
break;
case 376:
//#line 2096 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = new CEPCorrNameDefNode((CEPStringTokenNode)(val_peek(2).obj), (CEPBooleanExprNode)(val_peek(0).obj));}
break;
case 377:
//#line 2101 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{
     	yyval.obj = new CEPComplexBooleanExprNode(LogicalOp.AND, (CEPExprNode)(val_peek(2).obj), (CEPExprNode)(val_peek(0).obj));
     }
break;
case 378:
//#line 2106 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{
     	yyval.obj = new CEPComplexBooleanExprNode(LogicalOp.OR, (CEPExprNode)(val_peek(2).obj), (CEPExprNode)(val_peek(0).obj));
     }
break;
case 379:
//#line 2111 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{
     	yyval.obj = new CEPComplexBooleanExprNode(LogicalOp.XOR, (CEPExprNode)(val_peek(2).obj), (CEPExprNode)(val_peek(0).obj));
     }
break;
case 380:
//#line 2116 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{
     	yyval.obj = new CEPComplexBooleanExprNode(LogicalOp.NOT, (CEPExprNode)(val_peek(0).obj));
     }
break;
case 381:
//#line 2121 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{
     	yyval.obj = val_peek(1).obj;
     	((CEPBooleanExprNode)yyval.obj).setMyString("("+(val_peek(1).obj).toString()+")");
     }
break;
case 382:
//#line 2127 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = val_peek(0).obj;}
break;
case 383:
//#line 2130 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = val_peek(0).obj;}
break;
case 384:
//#line 2135 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{
     	yyval.obj = new CEPComplexBooleanExprNode(LogicalOp.AND, 
               new CEPBaseBooleanExprNode(CompOp.GE, (CEPExprNode)(val_peek(4).obj), (CEPExprNode)(val_peek(2).obj)),
               new CEPBaseBooleanExprNode(CompOp.LE, (CEPExprNode)(val_peek(4).obj), (CEPExprNode)(val_peek(0).obj)));
     }
break;
case 385:
//#line 2144 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{
     	yyval.obj = new CEPBaseBooleanExprNode(CompOp.LT, (CEPExprNode)(val_peek(2).obj), (CEPExprNode)(val_peek(0).obj));
     }
break;
case 386:
//#line 2149 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{
     	yyval.obj = new CEPBaseBooleanExprNode(CompOp.LT, (CEPExprNode)(val_peek(3).obj), (CEPExprNode)(val_peek(0).obj),OuterJoinType.RIGHT_OUTER);
     }
break;
case 387:
//#line 2154 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{
     	yyval.obj = new CEPBaseBooleanExprNode(CompOp.LT, (CEPExprNode)(val_peek(3).obj), (CEPExprNode)(val_peek(1).obj), OuterJoinType.LEFT_OUTER);
     }
break;
case 388:
//#line 2159 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{
     	yyval.obj = new CEPBaseBooleanExprNode(CompOp.LE, (CEPExprNode)(val_peek(2).obj), (CEPExprNode)(val_peek(0).obj));
     }
break;
case 389:
//#line 2164 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{
     	yyval.obj = new CEPBaseBooleanExprNode(CompOp.LE, (CEPExprNode)(val_peek(3).obj), (CEPExprNode)(val_peek(0).obj), OuterJoinType.RIGHT_OUTER);
     }
break;
case 390:
//#line 2169 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{
     	yyval.obj = new CEPBaseBooleanExprNode(CompOp.LE, (CEPExprNode)(val_peek(3).obj), (CEPExprNode)(val_peek(1).obj), OuterJoinType.LEFT_OUTER);
     }
break;
case 391:
//#line 2174 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{
     	yyval.obj = new CEPBaseBooleanExprNode(CompOp.GT, (CEPExprNode)(val_peek(2).obj), (CEPExprNode)(val_peek(0).obj));
     }
break;
case 392:
//#line 2179 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{
     	yyval.obj = new CEPBaseBooleanExprNode(CompOp.GT, (CEPExprNode)(val_peek(3).obj), (CEPExprNode)(val_peek(0).obj), OuterJoinType.RIGHT_OUTER);
     }
break;
case 393:
//#line 2184 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{
     	yyval.obj = new CEPBaseBooleanExprNode(CompOp.GT, (CEPExprNode)(val_peek(3).obj), (CEPExprNode)(val_peek(1).obj), OuterJoinType.LEFT_OUTER);
     }
break;
case 394:
//#line 2189 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{
     	yyval.obj = new CEPBaseBooleanExprNode(CompOp.GE, (CEPExprNode)(val_peek(2).obj), (CEPExprNode)(val_peek(0).obj));
     }
break;
case 395:
//#line 2194 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{
     	yyval.obj = new CEPBaseBooleanExprNode(CompOp.GE, (CEPExprNode)(val_peek(3).obj), (CEPExprNode)(val_peek(0).obj), OuterJoinType.RIGHT_OUTER);
     }
break;
case 396:
//#line 2199 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{
     	yyval.obj = new CEPBaseBooleanExprNode(CompOp.GE, (CEPExprNode)(val_peek(3).obj), (CEPExprNode)(val_peek(1).obj), OuterJoinType.LEFT_OUTER);
     }
break;
case 397:
//#line 2204 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{
     	yyval.obj = new CEPBaseBooleanExprNode(CompOp.EQ, (CEPExprNode)(val_peek(2).obj), (CEPExprNode)(val_peek(0).obj));
     }
break;
case 398:
//#line 2209 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{
     	yyval.obj = new CEPBaseBooleanExprNode(CompOp.EQ, (CEPExprNode)(val_peek(3).obj), (CEPExprNode)(val_peek(0).obj), OuterJoinType.RIGHT_OUTER);
     }
break;
case 399:
//#line 2214 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{
     	yyval.obj = new CEPBaseBooleanExprNode(CompOp.EQ, (CEPExprNode)(val_peek(3).obj), (CEPExprNode)(val_peek(1).obj), OuterJoinType.LEFT_OUTER);
     }
break;
case 400:
//#line 2219 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{
     	yyval.obj = new CEPBaseBooleanExprNode(CompOp.NE, (CEPExprNode)(val_peek(2).obj), (CEPExprNode)(val_peek(0).obj));
     }
break;
case 401:
//#line 2224 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{
     	yyval.obj = new CEPBaseBooleanExprNode(CompOp.NE, (CEPExprNode)(val_peek(3).obj), (CEPExprNode)(val_peek(0).obj), OuterJoinType.RIGHT_OUTER);
     }
break;
case 402:
//#line 2229 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{
     	yyval.obj = new CEPBaseBooleanExprNode(CompOp.NE, (CEPExprNode)(val_peek(3).obj), (CEPExprNode)(val_peek(1).obj), OuterJoinType.LEFT_OUTER);
     }
break;
case 403:
//#line 2234 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{
     	yyval.obj = new CEPBaseBooleanExprNode(CompOp.LIKE, (CEPExprNode)(val_peek(2).obj), (CEPExprNode)(val_peek(0).obj));
     }
break;
case 404:
//#line 2239 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{
     	yyval.obj = new CEPBaseBooleanExprNode(UnaryOp.IS_NULL, (CEPExprNode)(val_peek(2).obj));
     }
break;
case 405:
//#line 2244 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{
     	yyval.obj = new CEPBaseBooleanExprNode(UnaryOp.IS_NOT_NULL, (CEPExprNode)(val_peek(3).obj));
     }
break;
case 406:
//#line 2249 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{
     	yyval.obj = new CEPComplexBooleanExprNode((CEPExprNode)(val_peek(4).obj), (List)(val_peek(1).obj), false);
     }
break;
case 407:
//#line 2254 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{
     	yyval.obj = new CEPComplexBooleanExprNode((CEPExprNode)(val_peek(5).obj), (List)(val_peek(1).obj), true);
     }
break;
case 408:
//#line 2259 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{
     	((LinkedList)(val_peek(5).obj)).addFirst(val_peek(7).obj); yyval.obj = new CEPComplexBooleanExprNode((List)(val_peek(5).obj), (List)(val_peek(1).obj), false);
     }
break;
case 409:
//#line 2264 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{
     	((LinkedList)(val_peek(6).obj)).addFirst(val_peek(8).obj); yyval.obj = new CEPComplexBooleanExprNode((List)(val_peek(6).obj), (List)(val_peek(1).obj), true);
     }
break;
case 410:
//#line 2271 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{((LinkedList)(val_peek(0).obj)).addFirst(val_peek(3).obj); yyval.obj = val_peek(0).obj;}
break;
case 411:
//#line 2274 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{arithExprListSet = new LinkedList(); arithExprListSet.add(val_peek(1).obj); yyval.obj = arithExprListSet;}
break;
case 412:
//#line 2279 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = val_peek(0).obj;}
break;
case 413:
//#line 2282 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = val_peek(0).obj;}
break;
case 414:
//#line 2287 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = val_peek(0).obj;}
break;
case 415:
//#line 2291 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = val_peek(0).obj;}
break;
case 416:
//#line 2294 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{ yyval.obj = val_peek(0).obj; }
break;
case 417:
//#line 2297 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = val_peek(0).obj;}
break;
case 418:
//#line 2300 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = val_peek(0).obj;}
break;
case 419:
//#line 2303 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = val_peek(0).obj;}
break;
case 420:
//#line 2306 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = val_peek(0).obj;}
break;
case 421:
//#line 2309 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{
     	yyval.obj = new CEPArithExprNode(ArithOp.ADD, (CEPExprNode)(val_peek(2).obj), (CEPExprNode)(val_peek(0).obj));
     }
break;
case 422:
//#line 2314 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{
     	yyval.obj = new CEPArithExprNode(ArithOp.SUB, (CEPExprNode)(val_peek(2).obj), (CEPExprNode)(val_peek(0).obj));
     }
break;
case 423:
//#line 2319 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{
     	yyval.obj = new CEPArithExprNode(ArithOp.MUL, (CEPExprNode)(val_peek(2).obj), (CEPExprNode)(val_peek(0).obj));
     }
break;
case 424:
//#line 2324 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{
     	yyval.obj = new CEPArithExprNode(ArithOp.DIV, (CEPExprNode)(val_peek(2).obj), (CEPExprNode)(val_peek(0).obj));
     }
break;
case 425:
//#line 2329 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{
     	yyval.obj = new CEPArithExprNode(ArithOp.CONCAT, (CEPExprNode)(val_peek(3).obj), (CEPExprNode)(val_peek(0).obj));
     }
break;
case 426:
//#line 2334 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{
     	yyval.obj = val_peek(0).obj;
     }
break;
case 427:
//#line 2339 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{
     	yyval.obj = new CEPArithExprNode(ArithOp.MUL, new CEPIntConstExprNode(-1), (CEPExprNode)(val_peek(0).obj)); ((CEPArithExprNode)yyval.obj).setStartOffset(((CEPExprNode)val_peek(0).obj).getStartOffset());
     }
break;
case 428:
//#line 2344 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{
     	yyval.obj = val_peek(1).obj;
     	((CEPExprNode)yyval.obj).setMyString("(" + (yyval.obj).toString() + ")");
     }
break;
case 429:
//#line 2353 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = new CEPAttrNode((CEPStringTokenNode)(val_peek(2).obj), (CEPStringTokenNode)(val_peek(0).obj));}
break;
case 430:
//#line 2356 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = new CEPAttrNode((CEPStringTokenNode)(val_peek(2).obj), ((StreamPseudoColumn)(val_peek(0).obj)).getColumnName());}
break;
case 431:
//#line 2359 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{if(insideDefineOrMeasures) 
      {
        defaultSubsetRequired = true;
        yyval.obj = new CEPAttrNode(Constants.DEFAULT_SUBSET_NAME, (CEPStringTokenNode)(val_peek(0).obj));
      }
      else
        yyval.obj = new CEPAttrNode((CEPStringTokenNode)(val_peek(0).obj));}
break;
case 432:
//#line 2368 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = new CEPAttrNode(((StreamPseudoColumn)(val_peek(0).obj)).getColumnName()); ((CEPAttrNode)yyval.obj).setStartOffset(startOffset); ((CEPAttrNode)yyval.obj).setEndOffset(endOffset);}
break;
case 433:
//#line 2373 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{
      if (insideDefineOrMeasures || insideXmlAttr) 
      {
        List ids = (List) val_peek(0).obj;
        if (ids.size() == 1)
        {
          if (insideDefineOrMeasures)
          {
            defaultSubsetRequired = true;
            yyval.obj = new CEPAttrNode(Constants.DEFAULT_SUBSET_NAME, (CEPStringTokenNode) ids.get(0));
          }
          else /* insideXmlAttr */
            yyval.obj = new CEPAttrNode((CEPStringTokenNode) ids.get(0));
        }
        else if (ids.size() == 2)
        {
          yyval.obj = new CEPAttrNode((CEPStringTokenNode) ids.get(0), (CEPStringTokenNode) ids.get(1));
        }
        else /* greater than 2 ids */
        {
          if (insideDefineOrMeasures)
          {
            yyval.obj = new CEPObjExprNode((List)(val_peek(0).obj));
          }
          else
           throw new SyntaxException(SyntaxError.ATTR_ID_ERROR, 
            ((CEPStringTokenNode) ids.get(2)).getStartOffset(), 
            ((CEPStringTokenNode) ids.get(ids.size()-1)).getEndOffset(), new Object[0]);
        }
      }
      else 
        yyval.obj = new CEPObjExprNode((List)(val_peek(0).obj));
     }
break;
case 434:
//#line 2408 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = new CEPAttrNode((CEPStringTokenNode)(val_peek(2).obj), ((StreamPseudoColumn)(val_peek(0).obj)).getColumnName());}
break;
case 435:
//#line 2411 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = new CEPAttrNode(((StreamPseudoColumn)(val_peek(0).obj)).getColumnName()); ((CEPAttrNode)yyval.obj).setStartOffset(startOffset); ((CEPAttrNode)yyval.obj).setEndOffset(endOffset);}
break;
case 436:
//#line 2418 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = val_peek(0).obj;}
break;
case 437:
//#line 2421 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = val_peek(0).obj;}
break;
case 438:
//#line 2427 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = new CEPObjExprNode((CEPObjExprNode)(val_peek(2).obj), (CEPStringTokenNode)(val_peek(0).obj));}
break;
case 439:
//#line 2430 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = new CEPObjExprNode((CEPObjExprNode)(val_peek(4).obj), (CEPStringTokenNode)(val_peek(2).obj), Collections.emptyList());}
break;
case 440:
//#line 2433 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = new CEPObjExprNode((CEPObjExprNode)(val_peek(5).obj), (CEPStringTokenNode)(val_peek(3).obj),  (List)(val_peek(1).obj));}
break;
case 441:
//#line 2436 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = val_peek(0).obj;}
break;
case 442:
//#line 2439 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = val_peek(0).obj;}
break;
case 443:
//#line 2445 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = new CEPObjExprNode((List)(val_peek(2).obj), Collections.emptyList());}
break;
case 444:
//#line 2448 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = new CEPObjExprNode((List)(val_peek(3).obj), (List)(val_peek(1).obj));}
break;
case 445:
//#line 2452 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{argList = new LinkedList(); argList.add(val_peek(1).obj); yyval.obj = new CEPFunctionExprNode((List)(val_peek(4).obj), argList, true);}
break;
case 446:
//#line 2457 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = new CEPObjExprNode((CEPObjExprNode)(val_peek(3).obj), (CEPIntTokenNode)(val_peek(1).obj));}
break;
case 447:
//#line 2462 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = StreamPseudoColumn.ELEMENT_TIME;}
break;
case 448:
//#line 2465 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = StreamPseudoColumn.QUERY_ID;}
break;
case 449:
//#line 2470 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = val_peek(0).obj;}
break;
case 450:
//#line 2473 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = val_peek(0).obj;}
break;
case 451:
//#line 2476 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = val_peek(0).obj;}
break;
case 452:
//#line 2479 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = new CEPNullConstExprNode(); ((CEPNullConstExprNode)yyval.obj).setStartOffset(startOffset); ((CEPNullConstExprNode)yyval.obj).setEndOffset(endOffset);}
break;
case 453:
//#line 2482 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = val_peek(0).obj;}
break;
case 454:
//#line 2485 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = val_peek(0).obj;}
break;
case 455:
//#line 2488 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = new CEPFloatConstExprNode(val_peek(0).dval);((CEPFloatConstExprNode)yyval.obj).setStartOffset(startOffset); ((CEPFloatConstExprNode)yyval.obj).setEndOffset(endOffset);}
break;
case 456:
//#line 2491 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = new CEPDoubleConstExprNode(val_peek(0).dval);((CEPDoubleConstExprNode)yyval.obj).setStartOffset(startOffset); ((CEPDoubleConstExprNode)yyval.obj).setEndOffset(endOffset);}
break;
case 457:
//#line 2494 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = new CEPBigDecimalConstExprNode((BigDecimal)val_peek(0).obj);((CEPBigDecimalConstExprNode)yyval.obj).setStartOffset(startOffset); ((CEPBigDecimalConstExprNode)yyval.obj).setEndOffset(endOffset);}
break;
case 458:
//#line 2499 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = new CEPIntConstExprNode(val_peek(0).ival); ((CEPIntConstExprNode)yyval.obj).setStartOffset(startOffset); ((CEPIntConstExprNode)yyval.obj).setEndOffset(endOffset);}
break;
case 459:
//#line 2504 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{
       Long bigIntVal = ((CEPBigIntTokenNode)(val_peek(0).obj)).getValue();
       yyval.obj = new CEPBigintConstExprNode(bigIntVal);
       ((CEPBigintConstExprNode)yyval.obj).setStartOffset(startOffset); ((CEPBigintConstExprNode)yyval.obj).setEndOffset(endOffset);
     }
break;
case 460:
//#line 2513 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = new CEPBooleanConstExprNode((String)"TRUE"); ((CEPBooleanConstExprNode)yyval.obj).setStartOffset(startOffset); ((CEPBooleanConstExprNode)yyval.obj).setEndOffset(endOffset); }
break;
case 461:
//#line 2516 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = new CEPBooleanConstExprNode((String)"FALSE"); ((CEPBooleanConstExprNode)yyval.obj).setStartOffset(startOffset); ((CEPBooleanConstExprNode)yyval.obj).setEndOffset(endOffset);}
break;
case 462:
//#line 2521 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{
     	yyval.obj = new CEPStringConstExprNode(val_peek(0).sval);
     	((CEPStringConstExprNode)yyval.obj).setStartOffset(startOffset); 
     	((CEPStringConstExprNode)yyval.obj).setEndOffset(endOffset);
     	((CEPStringConstExprNode)yyval.obj).setSingleQuote(false);
     }
break;
case 463:
//#line 2529 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{
     	yyval.obj = new CEPStringConstExprNode(val_peek(0).sval);
     	((CEPStringConstExprNode)yyval.obj).setStartOffset(startOffset); 
     	((CEPStringConstExprNode)yyval.obj).setEndOffset(endOffset);
     	((CEPStringConstExprNode)yyval.obj).setSingleQuote(true);
     }
break;
case 464:
//#line 2539 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = new CEPIntervalConstExprNode(val_peek(1).sval, (IntervalFormat)val_peek(0).obj);
      ((CEPIntervalConstExprNode)yyval.obj).setStartOffset(startOffset); ((CEPIntervalConstExprNode)yyval.obj).setEndOffset(endOffset);}
break;
case 465:
//#line 2545 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = new IntervalFormat((TimeUnit)val_peek(0).obj);}
break;
case 466:
//#line 2548 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = new IntervalFormat((TimeUnit)val_peek(3).obj, ((CEPIntConstExprNode)val_peek(1).obj).getValue());}
break;
case 467:
//#line 2551 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = new IntervalFormat((TimeUnit)(val_peek(2).obj), (TimeUnit)(val_peek(0).obj));}
break;
case 468:
//#line 2554 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = new IntervalFormat((TimeUnit)(val_peek(5).obj), (TimeUnit)(val_peek(0).obj), ((CEPIntConstExprNode)val_peek(3).obj).getValue(), true);}
break;
case 469:
//#line 2557 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = new IntervalFormat((TimeUnit)val_peek(5).obj, ((CEPIntConstExprNode)val_peek(3).obj).getValue(), ((CEPIntConstExprNode)val_peek(1).obj).getValue());}
break;
case 470:
//#line 2560 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = new IntervalFormat((TimeUnit)val_peek(8).obj, (TimeUnit)val_peek(3).obj, ((CEPIntConstExprNode)val_peek(6).obj).getValue(), ((CEPIntConstExprNode)val_peek(1).obj).getValue());}
break;
case 471:
//#line 2563 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = new IntervalFormat((TimeUnit)val_peek(5).obj, (TimeUnit)val_peek(3).obj, ((CEPIntConstExprNode)val_peek(1).obj).getValue(), false);}
break;
case 472:
//#line 2569 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{
       TimestampFormat fmt = new TimestampFormat(((CEPIntConstExprNode)val_peek(1).obj).getValue());
       yyval.obj = fmt;
     }
break;
case 473:
//#line 2575 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{
       TimestampFormat fmt = new TimestampFormat(((CEPIntConstExprNode)val_peek(4).obj).getValue());
       fmt.setHasTimeZone(true);
       yyval.obj = fmt;
     }
break;
case 474:
//#line 2582 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{
       TimestampFormat fmt = new TimestampFormat();
       fmt.setHasTimeZone(true);
       yyval.obj = fmt;
     }
break;
case 475:
//#line 2589 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{
       TimestampFormat fmt = new TimestampFormat(((CEPIntConstExprNode)val_peek(5).obj).getValue());
       fmt.setLocalTimeZone(true);
       yyval.obj = fmt;
     }
break;
case 476:
//#line 2596 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{
       TimestampFormat fmt = new TimestampFormat();
       fmt.setLocalTimeZone(true);
       yyval.obj = fmt;
     }
break;
case 477:
//#line 2605 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{argList = new LinkedList(); argList.add(new CEPAttrNode((CEPStringTokenNode)(val_peek(3).obj), (CEPStringTokenNode)(val_peek(1).obj)));
      yyval.obj = new CEPPREVExprNode(argList);}
break;
case 478:
//#line 2609 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{argList = new LinkedList(); argList.add(new CEPAttrNode((CEPStringTokenNode)(val_peek(5).obj), (CEPStringTokenNode)(val_peek(3).obj)));
      argList.add(val_peek(1).obj); yyval.obj = new CEPPREVExprNode(argList);}
break;
case 479:
//#line 2613 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{argList = new LinkedList(); argList.add(new CEPAttrNode((CEPStringTokenNode)val_peek(7).obj, (CEPStringTokenNode)val_peek(5).obj));
      argList.add(val_peek(3).obj); argList.add(val_peek(1).obj);
      argList.add(new CEPAttrNode((CEPStringTokenNode)(val_peek(7).obj), StreamPseudoColumn.ELEMENT_TIME.getColumnName()));
      yyval.obj = new CEPPREVExprNode(argList);}
break;
case 480:
//#line 2619 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{ yyval.obj = new CEPCoalesceExprNode((List)(val_peek(1).obj)); }
break;
case 481:
//#line 2622 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = new CEPXQryFunctionExprNode((CEPStringTokenNode)(val_peek(8).obj), (List)val_peek(4).obj);}
break;
case 482:
//#line 2625 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = new CEPXExistsFunctionExprNode((CEPStringTokenNode)(val_peek(8).obj), (List)val_peek(4).obj);}
break;
case 483:
//#line 2628 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = new CEPXMLConcatExprNode((List)(val_peek(1).obj));}
break;
case 484:
//#line 2631 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = val_peek(0).obj;}
break;
case 485:
//#line 2634 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{argList = new LinkedList(); argList.add(new CEPAttrNode((CEPStringTokenNode)(val_peek(5).obj),(CEPStringTokenNode)(val_peek(3).obj)));
      argList.add(val_peek(1).obj); yyval.obj = new CEPFirstLastMultiExprNode(AggrFunction.FIRST.getFuncName(), argList);}
break;
case 486:
//#line 2638 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{argList = new LinkedList(); argList.add(new CEPAttrNode((CEPStringTokenNode)(val_peek(5).obj),(CEPStringTokenNode)(val_peek(3).obj)));
      argList.add(val_peek(1).obj); yyval.obj = new CEPFirstLastMultiExprNode(AggrFunction.LAST.getFuncName(), argList);}
break;
case 487:
//#line 2642 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = val_peek(0).obj;}
break;
case 488:
//#line 2645 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = val_peek(0).obj;}
break;
case 489:
//#line 2648 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = val_peek(0).obj;}
break;
case 490:
//#line 2654 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{ yyval.obj = new CEPXMLParseExprNode((CEPExprNode)(val_peek(2).obj),true,true);}
break;
case 491:
//#line 2657 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{ yyval.obj = new CEPXMLParseExprNode((CEPExprNode)(val_peek(1).obj),false,true);}
break;
case 492:
//#line 2660 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{ yyval.obj = new CEPXMLParseExprNode((CEPExprNode)(val_peek(2).obj),true,false);}
break;
case 493:
//#line 2663 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{ yyval.obj = new CEPXMLParseExprNode((CEPExprNode)(val_peek(1).obj),false,false);}
break;
case 494:
//#line 2668 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = new CEPXMLAggNode((CEPExprNode)(val_peek(2).obj), (CEPOrderByNode)(val_peek(1).obj));}
break;
case 495:
//#line 2671 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = new CEPXMLAggNode((CEPExprNode)(val_peek(1).obj),null);}
break;
case 496:
//#line 2676 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = new CEPElementExprNode((CEPStringTokenNode)(val_peek(5).obj), (List)(val_peek(3).obj), (List)(val_peek(1).obj));}
break;
case 497:
//#line 2679 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = new CEPElementExprNode((CEPExprNode)(val_peek(5).obj), (List)(val_peek(3).obj), (List)(val_peek(1).obj));}
break;
case 498:
//#line 2682 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = new CEPElementExprNode((CEPStringTokenNode)(val_peek(5).obj), (List)(val_peek(3).obj), (List)(val_peek(1).obj));}
break;
case 499:
//#line 2685 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = new CEPElementExprNode((CEPStringTokenNode)(val_peek(3).obj), null, (List)(val_peek(1).obj)) ;}
break;
case 500:
//#line 2688 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = new CEPElementExprNode((CEPExprNode)val_peek(3).obj, null, (List)(val_peek(1).obj)) ;}
break;
case 501:
//#line 2691 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = new CEPElementExprNode((CEPStringTokenNode)(val_peek(3).obj), null, (List)(val_peek(1).obj)) ;}
break;
case 502:
//#line 2694 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = new CEPElementExprNode((CEPStringTokenNode)(val_peek(1).obj), null, null);}
break;
case 503:
//#line 2697 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = new CEPElementExprNode((CEPExprNode)val_peek(1).obj, null, null);}
break;
case 504:
//#line 2700 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = new CEPElementExprNode((CEPStringTokenNode)(val_peek(1).obj), null, null);}
break;
case 505:
//#line 2703 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = new CEPElementExprNode((CEPStringTokenNode)(val_peek(3).obj), (List)(val_peek(1).obj), null);}
break;
case 506:
//#line 2706 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = new CEPElementExprNode((CEPExprNode)val_peek(3).obj, (List)(val_peek(1).obj), null);}
break;
case 507:
//#line 2709 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = new CEPElementExprNode((CEPStringTokenNode)(val_peek(3).obj), (List)(val_peek(1).obj), null);}
break;
case 508:
//#line 2713 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{insideXmlAttr = true;}
break;
case 509:
//#line 2714 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{insideXmlAttr = false; yyval.obj = val_peek(0).obj;}
break;
case 510:
//#line 2720 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = val_peek(1).obj;}
break;
case 511:
//#line 2723 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = new LinkedList();}
break;
case 512:
//#line 2729 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{((LinkedList)(val_peek(0).obj)).addFirst(val_peek(2).obj); yyval.obj = val_peek(0).obj;}
break;
case 513:
//#line 2732 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{attrList = new LinkedList(); attrList.add(val_peek(0).obj); yyval.obj = attrList;}
break;
case 514:
//#line 2737 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = new CEPXmlAttrExprNode((CEPExprNode)val_peek(2).obj, (CEPStringTokenNode)(val_peek(0).obj));}
break;
case 515:
//#line 2740 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = new CEPXmlAttrExprNode((CEPExprNode)val_peek(3).obj, (CEPExprNode)val_peek(0).obj);}
break;
case 516:
//#line 2743 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = new CEPXmlAttrExprNode((CEPExprNode)val_peek(0).obj);}
break;
case 517:
//#line 2747 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{insideXmlAttr = true;}
break;
case 518:
//#line 2748 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{insideXmlAttr = false; yyval.obj = new CEPXmlForestExprNode((List)val_peek(1).obj);}
break;
case 519:
//#line 2752 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{insideXmlAttr = true;}
break;
case 520:
//#line 2753 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{insideXmlAttr = false; yyval.obj = new CEPXmlColAttValExprNode((List)val_peek(1).obj);}
break;
case 521:
//#line 2758 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{((LinkedList)(val_peek(0).obj)).addFirst(val_peek(2).obj); yyval.obj = val_peek(0).obj;}
break;
case 522:
//#line 2761 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{arithList = new LinkedList(); arithList.add(val_peek(0).obj); yyval.obj = arithList;}
break;
case 523:
//#line 2766 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{((LinkedList)(val_peek(0).obj)).addFirst(val_peek(2).obj); yyval.obj = val_peek(0).obj;}
break;
case 524:
//#line 2769 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{argList = new LinkedList(); argList.add(val_peek(0).obj); yyval.obj = argList;}
break;
case 525:
//#line 2774 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = new CEPXQryArgExprNode((CEPExprNode)val_peek(2).obj, (CEPStringTokenNode)(val_peek(0).obj));}
break;
case 526:
//#line 2779 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{((LinkedList)(val_peek(0).obj)).addFirst(val_peek(2).obj); yyval.obj = val_peek(0).obj;}
break;
case 527:
//#line 2782 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{argList = new LinkedList(); argList.add(val_peek(0).obj); yyval.obj = argList;}
break;
case 528:
//#line 2787 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = new CEPSearchedCaseExprNode((List)(val_peek(1).obj), new CEPNullConstExprNode()); ((CEPSearchedCaseExprNode)yyval.obj).setEndOffset(endOffset);}
break;
case 529:
//#line 2790 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = new CEPSearchedCaseExprNode((List)(val_peek(3).obj), (CEPExprNode)(val_peek(1).obj));}
break;
case 530:
//#line 2793 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = new CEPSimpleCaseExprNode((CEPExprNode)(val_peek(2).obj), (List)(val_peek(1).obj), new CEPNullConstExprNode()); ((CEPSimpleCaseExprNode)yyval.obj).setEndOffset(endOffset);}
break;
case 531:
//#line 2796 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = new CEPSimpleCaseExprNode((CEPExprNode)(val_peek(4).obj), (List)(val_peek(3).obj), (CEPExprNode)(val_peek(1).obj));}
break;
case 532:
//#line 2801 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{((LinkedList)(val_peek(0).obj)).addFirst(val_peek(1).obj); yyval.obj = val_peek(0).obj;}
break;
case 533:
//#line 2804 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{argList = new LinkedList(); argList.add(val_peek(0).obj); yyval.obj = argList;}
break;
case 534:
//#line 2809 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = new CEPCaseComparisonExprNode((CEPExprNode)(val_peek(2).obj),(CEPExprNode)(val_peek(0).obj));}
break;
case 535:
//#line 2814 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{((LinkedList)(val_peek(0).obj)).addFirst(val_peek(1).obj); yyval.obj = val_peek(0).obj;}
break;
case 536:
//#line 2817 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{argList = new LinkedList(); argList.add(val_peek(0).obj); yyval.obj = argList;}
break;
case 537:
//#line 2822 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = new CEPCaseConditionExprNode((CEPBooleanExprNode)(val_peek(2).obj),(CEPExprNode)(val_peek(0).obj));}
break;
case 538:
//#line 2827 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = new CEPDecodeExprNode((List)(val_peek(1).obj));}
break;
case 539:
//#line 2832 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = new CEPOtherAggrExprNode(AggrFunction.COUNT, (CEPExprNode)(val_peek(1).obj));}
break;
case 540:
//#line 2835 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{
       if(!insideDefineOrMeasures) {
         yyval.obj = new CEPCountStarNode();
         ((CEPCountStarNode)yyval.obj).setStartOffset(startOffset);
         ((CEPCountStarNode)yyval.obj).setEndOffset(endOffset);
       }
       else{
         defaultSubsetRequired = true;
         yyval.obj = new CEPCountCorrStarNode((CEPExprNode)(new CEPAttrNode(new CEPStringTokenNode(Constants.DEFAULT_SUBSET_NAME),
                                       new CEPStringTokenNode(null))));
         ((CEPCountCorrStarNode)yyval.obj).setStartOffset(startOffset);
         ((CEPCountCorrStarNode)yyval.obj).setEndOffset(endOffset);
       }
     }
break;
case 541:
//#line 2851 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = new CEPCountCorrStarNode((CEPExprNode)(new CEPAttrNode((CEPStringTokenNode)(val_peek(2).obj), new CEPStringTokenNode(null)))); ((CEPCountCorrStarNode)yyval.obj).setEndOffset(endOffset);}
break;
case 542:
//#line 2854 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = new CEPOtherAggrExprNode(AggrFunction.SUM, (CEPExprNode)(val_peek(1).obj));}
break;
case 543:
//#line 2857 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = new CEPOtherAggrExprNode(AggrFunction.AVG, (CEPExprNode)(val_peek(1).obj));}
break;
case 544:
//#line 2860 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = new CEPOtherAggrExprNode(AggrFunction.MAX, (CEPExprNode)(val_peek(1).obj));}
break;
case 545:
//#line 2863 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = new CEPOtherAggrExprNode(AggrFunction.MIN, (CEPExprNode)(val_peek(1).obj));}
break;
case 546:
//#line 2866 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = new CEPFirstLastExprNode(AggrFunction.FIRST, 
                               (CEPExprNode)(new CEPAttrNode((CEPStringTokenNode)(val_peek(3).obj), (CEPStringTokenNode)(val_peek(1).obj))));}
break;
case 547:
//#line 2870 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = new CEPFirstLastExprNode(AggrFunction.LAST, 
                               (CEPExprNode)(new CEPAttrNode((CEPStringTokenNode)(val_peek(3).obj), (CEPStringTokenNode)(val_peek(1).obj))));}
break;
case 548:
//#line 2874 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = val_peek(0).obj;}
break;
case 549:
//#line 2879 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = new CEPOtherAggrExprNode(AggrFunction.COUNT, (CEPExprNode)(val_peek(1).obj), true);}
break;
case 550:
//#line 2882 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = new CEPOtherAggrExprNode(AggrFunction.SUM, (CEPExprNode)(val_peek(1).obj), true);}
break;
case 551:
//#line 2885 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = new CEPOtherAggrExprNode(AggrFunction.AVG, (CEPExprNode)(val_peek(1).obj), true);}
break;
case 552:
//#line 2888 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = new CEPOtherAggrExprNode(AggrFunction.MAX, (CEPExprNode)(val_peek(1).obj), true);}
break;
case 553:
//#line 2891 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = new CEPOtherAggrExprNode(AggrFunction.MIN, (CEPExprNode)(val_peek(1).obj), true);}
break;
case 554:
//#line 2896 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = new CEPSetopQueryNode( RelSetOp.NOT_IN, (CEPStringTokenNode)(val_peek(3).obj), (CEPStringTokenNode)(val_peek(0).obj));}
break;
case 555:
//#line 2899 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = new CEPSetopQueryNode( RelSetOp.IN, (CEPStringTokenNode)(val_peek(2).obj), (CEPStringTokenNode)(val_peek(0).obj));}
break;
case 556:
//#line 2906 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{
       CEPQueryRelationNode queryRelNode = (CEPQueryRelationNode)(val_peek(1).obj); 
       queryRelNode.setEvaluateClause((CEPSlideExprNode)(val_peek(0).obj));
       yyval.obj = queryRelNode;
     }
break;
case 557:
//#line 2915 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{ yyval.obj = val_peek(0).obj; }
break;
case 558:
//#line 2918 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{ yyval.obj = val_peek(1).obj; }
break;
case 559:
//#line 2923 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{ yyval.obj = new CEPSetopSubqueryNode(RelSetOp.UNION, (CEPQueryNode)(val_peek(2).obj), (CEPQueryNode)(val_peek(0).obj), false); }
break;
case 560:
//#line 2926 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{ yyval.obj = new CEPSetopSubqueryNode(RelSetOp.UNION, (CEPQueryNode)(val_peek(3).obj), (CEPQueryNode)(val_peek(0).obj), true); }
break;
case 561:
//#line 2929 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{ yyval.obj = new CEPSetopSubqueryNode(RelSetOp.EXCEPT, (CEPQueryNode)(val_peek(2).obj), (CEPQueryNode)(val_peek(0).obj), false); }
break;
case 562:
//#line 2932 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{ yyval.obj = new CEPSetopSubqueryNode(RelSetOp.INTERSECT, (CEPQueryNode)(val_peek(2).obj), (CEPQueryNode)(val_peek(0).obj), false); }
break;
case 563:
//#line 2935 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{ yyval.obj = new CEPSetopSubqueryNode(RelSetOp.MINUS, (CEPQueryNode)(val_peek(2).obj), (CEPQueryNode)(val_peek(0).obj), false); }
break;
case 564:
//#line 2938 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{ yyval.obj = val_peek(1).obj; }
break;
case 565:
//#line 2943 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{ yyval.obj = new CEPGenericSetOpNode((CEPStringTokenNode)(val_peek(1).obj), (List)(val_peek(0).obj)); }
break;
case 566:
//#line 2948 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{
       CEPQueryRelationNode queryRelNode = (CEPQueryRelationNode)(val_peek(1).obj);
       queryRelNode.setEvaluateClause((CEPSlideExprNode)(val_peek(0).obj));
       yyval.obj = queryRelNode;
     }
break;
case 567:
//#line 2956 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{ ((LinkedList)(val_peek(0).obj)).addFirst(val_peek(1).obj); yyval.obj = val_peek(0).obj; }
break;
case 568:
//#line 2959 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{ setopRelList = new LinkedList();
      setopRelList.add(val_peek(0).obj); yyval.obj = setopRelList; 
    }
break;
case 569:
//#line 2965 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{
      yyval.obj = new CEPSetOpNode((CEPStringTokenNode)(val_peek(0).obj), RelSetOp.UNION, false); 
    }
break;
case 570:
//#line 2970 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{ 
      yyval.obj = new CEPSetOpNode((CEPStringTokenNode)(val_peek(0).obj), RelSetOp.UNION, true);
    }
break;
case 571:
//#line 2975 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{ 
      yyval.obj = new CEPSetOpNode((CEPStringTokenNode)(val_peek(0).obj), RelSetOp.EXCEPT, false);
    }
break;
case 572:
//#line 2980 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{
      yyval.obj = new CEPSetOpNode((CEPStringTokenNode)(val_peek(0).obj), RelSetOp.MINUS, false);
    }
break;
case 573:
//#line 2985 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{
      yyval.obj = new CEPSetOpNode((CEPStringTokenNode)(val_peek(0).obj), RelSetOp.INTERSECT, false);
    }
break;
case 574:
//#line 2993 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = val_peek(0).obj;}
break;
case 575:
//#line 2996 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = val_peek(0).obj;}
break;
case 576:
//#line 3001 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = Datatype.BIGDECIMAL;}
break;
case 577:
//#line 3006 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = Datatype.CHAR;}
break;
case 578:
//#line 3009 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = Datatype.BYTE;}
break;
case 579:
//#line 3014 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = Datatype.INT;}
break;
case 580:
//#line 3017 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = Datatype.BIGINT;}
break;
case 581:
//#line 3020 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = Datatype.FLOAT;}
break;
case 582:
//#line 3023 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = Datatype.DOUBLE;}
break;
case 583:
//#line 3026 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{
       /** support for backward compatibility */
       Datatype timestampType = Datatype.TIMESTAMP;
       timestampType.setTimestampFormat(new TimestampFormat());
       yyval.obj = timestampType;
     }
break;
case 584:
//#line 3034 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{
       Datatype timestampTypeWithFmt = Datatype.TIMESTAMP;
       TimestampFormat format = (TimestampFormat)(val_peek(0).obj);
       timestampTypeWithFmt.setTimestampFormat(format);
       yyval.obj = timestampTypeWithFmt;
     }
break;
case 585:
//#line 3042 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = Datatype.BOOLEAN;}
break;
case 586:
//#line 3045 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = Datatype.XMLTYPE;}
break;
case 587:
//#line 3048 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = Datatype.OBJECT;}
break;
case 588:
//#line 3051 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = Datatype.BIGDECIMAL;}
break;
case 589:
//#line 3054 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{
       /** Support for Backward Compatibility */
       Datatype intervalType = Datatype.INTERVAL;
       intervalType.setIntervalFormat(
         new IntervalFormat(
           TimeUnit.DAY, 
           TimeUnit.SECOND, 
           Constants.DEFAULT_INTERVAL_LEADING_PRECISION,
           Constants.DEFAULT_INTERVAL_FRACTIONAL_SECONDS_PRECISION));

       yyval.obj = intervalType;
     }
break;
case 590:
//#line 3068 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{
       IntervalFormat format = (IntervalFormat)(val_peek(0).obj); 
       Datatype       dt     = null;

       if(format.isYearToMonthInterval())
         dt = Datatype.INTERVALYM;
       else
         dt = Datatype.INTERVAL;
       dt.setIntervalFormat(format);
       yyval.obj = dt;
     }
break;
case 591:
//#line 3081 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{
       yyval.obj = CartridgeHelper.getType(execContext, (List) val_peek(0).obj);       
     }
break;
case 592:
//#line 3088 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = new CEPStringTokenNode(val_peek(0).sval); ((CEPStringTokenNode)yyval.obj).setStartOffset(startOffset); ((CEPStringTokenNode)yyval.obj).setEndOffset(endOffset);}
break;
case 593:
//#line 3091 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = new CEPStringTokenNode(val_peek(0).sval); ((CEPStringTokenNode)yyval.obj).setStartOffset(startOffset); ((CEPStringTokenNode)yyval.obj).setEndOffset(endOffset);}
break;
case 594:
//#line 3094 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = new CEPStringTokenNode(val_peek(0).sval); ((CEPStringTokenNode)yyval.obj).setStartOffset(startOffset); ((CEPStringTokenNode)yyval.obj).setEndOffset(endOffset);}
break;
case 595:
//#line 3099 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = val_peek(0).obj;}
break;
case 596:
//#line 3102 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = new CEPStringTokenNode(val_peek(0).sval); ((CEPStringTokenNode)yyval.obj).setStartOffset(startOffset); ((CEPStringTokenNode)yyval.obj).setEndOffset(endOffset);}
break;
case 597:
//#line 3107 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = val_peek(0).obj;}
break;
case 598:
//#line 3110 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = new CEPStringTokenNode(val_peek(0).sval); ((CEPStringTokenNode)yyval.obj).setStartOffset(startOffset); ((CEPStringTokenNode)yyval.obj).setEndOffset(endOffset);}
break;
case 599:
//#line 3115 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = val_peek(0).obj;}
break;
case 600:
//#line 3118 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{idList = new LinkedList();
      CEPStringTokenNode stringNode = new CEPStringTokenNode("char"); stringNode.setStartOffset(startOffset); stringNode.setEndOffset(endOffset); 
      idList.add(stringNode); ((CEPStringTokenNode)val_peek(0).obj).setIsLink(true); idList.add(val_peek(0).obj); yyval.obj = idList;
     }
break;
case 601:
//#line 3124 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{idList = new LinkedList(); idList.add(val_peek(0).obj); yyval.obj = idList;}
break;
case 602:
//#line 3129 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = val_peek(0).obj;}
break;
case 603:
//#line 3132 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{idList = new LinkedList();
      CEPStringTokenNode stringNode = new CEPStringTokenNode("char"); stringNode.setStartOffset(startOffset); stringNode.setEndOffset(endOffset); 
      idList.add(stringNode); ((CEPStringTokenNode)val_peek(0).obj).setIsLink(true); idList.add(val_peek(0).obj); yyval.obj = idList;
     }
break;
case 604:
//#line 3138 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{idList = new LinkedList(); idList.add(val_peek(0).obj); yyval.obj = idList;}
break;
case 605:
//#line 3146 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{((LinkedList)(val_peek(0).obj)).addFirst(val_peek(2).obj); yyval.obj = val_peek(0).obj;}
break;
case 606:
//#line 3149 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{idList = new LinkedList(); idList.add(val_peek(2).obj); idList.add(val_peek(0).obj); yyval.obj = idList;}
break;
case 607:
//#line 3152 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{idList = new LinkedList(); idList.add(val_peek(2).obj); ((CEPStringTokenNode)val_peek(0).obj).setIsLink(true); idList.add(val_peek(0).obj); yyval.obj = idList;}
break;
case 608:
//#line 3157 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = new CEPIntTokenNode(val_peek(0).ival); ((CEPIntTokenNode)yyval.obj).setStartOffset(startOffset); ((CEPIntTokenNode)yyval.obj).setEndOffset(endOffset);}
break;
case 609:
//#line 3162 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = new CEPBigIntTokenNode((Long)val_peek(0).obj); ((CEPBigIntTokenNode)yyval.obj).setStartOffset(startOffset); ((CEPBigIntTokenNode)yyval.obj).setEndOffset(endOffset);}
break;
case 610:
//#line 3167 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = new CEPStringTokenNode(val_peek(0).sval); ((CEPStringTokenNode)yyval.obj).setStartOffset(startOffset); ((CEPStringTokenNode)yyval.obj).setEndOffset(endOffset);}
break;
case 611:
//#line 3172 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{
     	yyval.obj = new CEPStringTokenNode(val_peek(0).sval); 
     	((CEPStringTokenNode)yyval.obj).setStartOffset(startOffset); 
     	((CEPStringTokenNode)yyval.obj).setEndOffset(endOffset);
     	((CEPStringTokenNode)yyval.obj).setSingleQuote(false);
     }
break;
case 612:
//#line 3182 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{
     	yyval.obj = new CEPStringTokenNode(val_peek(0).sval); 
     	((CEPStringTokenNode)yyval.obj).setStartOffset(startOffset); 
     	((CEPStringTokenNode)yyval.obj).setEndOffset(endOffset);
     	((CEPStringTokenNode)yyval.obj).setSingleQuote(true);
     }
break;
case 664:
//#line 3259 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = (String)"XMLQUERY";}
break;
case 665:
//#line 3262 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = (String)"XMLEXISTS";}
break;
case 666:
//#line 3268 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = AggrFunction.FIRST.getFuncName();}
break;
case 667:
//#line 3271 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = AggrFunction.LAST.getFuncName();}
break;
case 668:
//#line 3276 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = AggrFunction.MAX.getFuncName();}
break;
case 669:
//#line 3279 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = AggrFunction.MIN.getFuncName();}
break;
case 670:
//#line 3282 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = AggrFunction.XML_AGG.getFuncName();}
break;
case 671:
//#line 3287 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = AggrFunction.SUM.getFuncName();}
break;
case 672:
//#line 3290 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = AggrFunction.AVG.getFuncName();}
break;
case 673:
//#line 3293 "/scratch/santkumk/gitlocal/soa-osa/source/modules/spark-cql/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy"
{yyval.obj = AggrFunction.COUNT.getFuncName();}
break;
//#line 8032 "Parser.java"
//########## END OF USER-SUPPLIED ACTIONS ##########
    }//switch
    //#### Now let's reduce... ####
    if (yydebug) debug("reduce");
    state_drop(yym);             //we just reduced yylen states
    yystate = state_peek(0);     //get new state
    val_drop(yym);               //corresponding value drop
    yym = yylhs[yyn];            //select next TERMINAL(on lhs)
    if (yystate == 0 && yym == 0)//done? 'rest' state and at first TERMINAL
      {
      if (yydebug) debug("After reduction, shifting from state 0 to state "+YYFINAL+"");
      yystate = YYFINAL;         //explicitly say we're done
      state_push(YYFINAL);       //and save it
      val_push(yyval);           //also save the semantic value of parsing
      if (yychar < 0)            //we want another character?
        {
        yychar = yylex();        //get next character
        if (yychar<0) yychar=0;  //clean, if necessary
        if (yydebug)
          yylexdebug(yystate,yychar);
        }
      if (yychar == 0)          //Good exit (if lex returns 0 ;-)
         break;                 //quit the loop--all DONE
      }//if yystate
    else                        //else not done yet
      {                         //get next state and push, for next yydefred[]
      yyn = yygindex[yym];      //find out where to go
      if ((yyn != 0) && (yyn += yystate) >= 0 &&
            yyn <= YYTABLESIZE && yycheck(yyn) == yystate)
        yystate = yytable(yyn); //get new state
      else
        yystate = yydgoto[yym]; //else go to new defred
      if (yydebug) debug("after reduction, shifting from state "+state_peek(0)+" to state "+yystate+"");
      state_push(yystate);     //going again, so push state & val...
      val_push(yyval);         //for next action
      }
    }//main loop
  return 0;//yyaccept!!
}
//## end of method parse() ######################################



//## run() --- for Thread #######################################
//## The -Jnorun option was used ##
//## end of method run() ########################################



//## Constructors ###############################################
/**
 * Default constructor.  Turn off with -Jnoconstruct .

 */
public Parser()
{
  //nothing to do
}


/**
 * Create a parser, setting the debug to true or false.
 * @param debugMe true for debugging, false for no debug.
 */
public Parser(boolean debugMe)
{
  yydebug=debugMe;
}
//###############################################################



}
//################### END OF CLASS ##############################
