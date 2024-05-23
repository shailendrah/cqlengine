package oracle.cep.jdbc.parser;
import java.math.BigDecimal;
%%

%byaccj
%unicode

%{
  private Parser yyparser;

  public Yylex(java.io.Reader r, Parser yyparser) {
    this(r);
    this.yyparser = yyparser;
  }
%}

/*letter   = [A-Za-z]*/
digit    = [0-9]
s_digit  = -[0-9]
num      = {digit}*
s_num    = {s_digit}*{num}     

%%
[ \n\t]                              {/* ignore spaces, tabs, and newlines */}

[:jletter:]([:jletter:]|[:jletterdigit:]|_)*        {
                                       return JDBCLexerHelper.getId(yyparser, yytext());
                                     }

"?"                                  {
                                       yyparser.yylval = new ParserVal(yytext());
                                       return Parser.T_QUES;
                                     }

[,;()]                               {return yycharat(0);}

{s_num}                              { 
                                       yyparser.yylval = new ParserVal(Integer.parseInt(yytext()));
                                       return Parser.T_INT;
                                     }


{s_num}[Ll]                          {
                                       yyparser.yylval = new ParserVal(new Long(yytext().substring(0, yytext().length()-1)));                                       
                                       return Parser.T_BIGINT;
                                     }
                                     
{s_num}[Ff]                          {
                                       yyparser.yylval = new ParserVal(Float.parseFloat(yytext()));
                                       return Parser.T_FLOAT;
                                     }

{s_num}[Dd]                          {
                                       yyparser.yylval = new ParserVal(Double.parseDouble(yytext()));
                                       return Parser.T_DOUBLE;
                                     }    
                                     
{s_num}[Nn]                          {
                                       yyparser.yylval = new ParserVal(new BigDecimal(yytext().substring(0, yytext().length() - 1)));
                                       return Parser.T_BIGDECIMAL;
                                     }                                                                                                                                       

{s_num}\.{num}([Ff])?                {
                                       yyparser.yylval = new ParserVal(Float.parseFloat(yytext()));
                                       return Parser.T_FLOAT;
                                     }

{s_num}\.{num}[Ee]{s_num}([Ff])?     {
                                       yyparser.yylval = new ParserVal(Float.parseFloat(yytext()));
                                       return Parser.T_FLOAT;
                                     }

{s_num}\.{num}[Dd]                   {
                                       yyparser.yylval = new ParserVal(Double.parseDouble(yytext()));
                                       return Parser.T_DOUBLE;
                                     }

{s_num}\.{num}[Ee]{s_num}[Dd]        {
                                       yyparser.yylval = new ParserVal(Double.parseDouble(yytext()));
                                       return Parser.T_DOUBLE;
                                     }

-\.{num}[dD]                         {
                                       yyparser.yylval = new ParserVal(Double.parseDouble(yytext()));
                                       return Parser.T_DOUBLE;
                                     }

{s_num}\.{num}[Nn]                   {
                                       yyparser.yylval = new ParserVal(new BigDecimal(yytext().substring(0, yytext().length() - 1)));
                                       return Parser.T_BIGDECIMAL;
                                     }
                     
{s_num}\.{num}[Ee]{s_num}[Nn]        {
                                       yyparser.yylval = new ParserVal(new BigDecimal(yytext().substring(0, yytext().length() - 1)));
                                       return Parser.T_BIGDECIMAL;
                                     }                                     
                                     

   
\"([^\"\n]|(\"\"))*\"                {
                                       yyparser.yylval = new ParserVal(yytext().substring(1,yylength()-1));
                                       return Parser.T_QSTRING;
                                     }  
                                     
=									 { 
										return Parser.T_EQ;
		                             }

.                                    {
                                       /* ignore '\0' */
                                       /* System.out.println("illegal character [" + yycharat(0) + "]"); */
                                     }

