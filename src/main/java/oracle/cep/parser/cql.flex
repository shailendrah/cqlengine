package oracle.cep.parser;
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

/*letter       =  [A-Za-z]*/
digit        =  [0-9]
num          =  {digit}+
s_num        =  {num}

%%
[ \n\t]              {/* ignore spaces, tabs, and newlines */
                       yyparser.setStartOffset(zzStartRead); 
                       yyparser.setSpaceEndOffset(zzStartRead);}

{s_num}              {
                       yyparser.yylval = new ParserVal(Integer.parseInt(yytext())); 
                       yyparser.setStartOffset(zzStartRead); 
                       return Parser.T_INT;
                     }

{s_num}[Ll]          {
                       yyparser.yylval = new ParserVal(new Long(Long.parseLong(yytext().substring(0, yytext().length()-1)))); 
                       yyparser.setStartOffset(zzStartRead); 
                       return Parser.T_BIGINT;
                     }

{s_num}[Ff]          {
                       yyparser.yylval = new ParserVal(Float.parseFloat(yytext()));
                       yyparser.setStartOffset(zzStartRead);
                       return Parser.T_FLOAT;
                     }

{s_num}[Dd]          {
                       yyparser.yylval = new ParserVal(Double.parseDouble(yytext()));
                       yyparser.setStartOffset(zzStartRead); 
                       return Parser.T_DOUBLE;
                     }
{s_num}[Nn]          {
                       yyparser.yylval = new ParserVal(new BigDecimal(yytext().substring(0, yytext().length() - 1)));
                       yyparser.setStartOffset(zzStartRead); 
                       return Parser.T_NUMBER;
                     }
					   
{s_num}\.{num}([Ff])? {
                        yyparser.yylval = new ParserVal(Float.parseFloat(yytext()));
                       yyparser.setStartOffset(zzStartRead); 
                        return Parser.T_FLOAT;
                      }

{s_num}\.{num}[Ee]{s_num}([Ff])? {
                       yyparser.yylval = new ParserVal(Float.parseFloat(yytext()));
                       yyparser.setStartOffset(zzStartRead); 
                       return Parser.T_FLOAT;
                     }

{s_num}\.{num}[Dd]  {
                       yyparser.yylval = new ParserVal(Double.parseDouble(yytext()));
                       yyparser.setStartOffset(zzStartRead); 
                       return Parser.T_DOUBLE;
                     }
                     
{s_num}\.{num}[Ee]{s_num}[Dd] {
                       yyparser.yylval = new ParserVal(Double.parseDouble(yytext()));
                       yyparser.setStartOffset(zzStartRead);
                       return Parser.T_DOUBLE;
                     }
                     
{s_num}\.{num}[Nn]  {
                       yyparser.yylval = new ParserVal(new BigDecimal(yytext().substring(0, yytext().length() - 1)));
                       yyparser.setStartOffset(zzStartRead); 
                       return Parser.T_NUMBER;
                     }
                     
{s_num}\.{num}[Ee]{s_num}[Nn] {
                       yyparser.yylval = new ParserVal(new BigDecimal(yytext().substring(0, yytext().length() - 1)));
                       yyparser.setStartOffset(zzStartRead);
                       return Parser.T_NUMBER;
                     }
                 

\'([^\'\n]|(\'\'))*\' {
                     yyparser.yylval = new ParserVal(yytext().substring(1,yylength()-1));
                       yyparser.setStartOffset(zzStartRead); 
                     return Parser.T_SQSTRING;
                    }
\"([^\"\n]|(\"\"))*\" {
                     yyparser.yylval = new ParserVal(yytext().substring(1,yylength()-1));
                       yyparser.setStartOffset(zzStartRead); 
                     return Parser.T_QSTRING;
                    }

\"([^\"\n]|(\"\"))*\n {
                       yyparser.setStartOffset(zzStartRead); 
                     System.out.println("newline in string constant");
                    }

[:jletter:]([:jletter:]|[:jletterdigit:]|_)*   {
                       yyparser.setStartOffset(zzStartRead);  
                     return LexerHelper.getId(yyparser, yytext());
                     }

"<"                  {yyparser.setStartOffset(zzStartRead);return Parser.T_LT;}
"<="                 {yyparser.setStartOffset(zzStartRead);return Parser.T_LE;}
">"                  {yyparser.setStartOffset(zzStartRead);return Parser.T_GT;}
">="                 {yyparser.setStartOffset(zzStartRead);return Parser.T_GE;}
"="                  {yyparser.setStartOffset(zzStartRead);return Parser.T_EQ;}
"!="                 {yyparser.setStartOffset(zzStartRead);return Parser.T_NE;}
"<>"                 {yyparser.setStartOffset(zzStartRead);return Parser.T_NE;}
"(+)"                {yyparser.setStartOffset(zzStartRead);return Parser.T_JPLUS;}
".*"                 {yyparser.setStartOffset(zzStartRead);return Parser.T_DOTSTAR;}
"char@"              {yyparser.setStartOffset(zzStartRead);return Parser.T_CHARAT;}

[*/+\-=<>':;,.|&()?@] {yyparser.setStartOffset(zzStartRead);return yycharat(0);}
"["                   {yyparser.setStartOffset(zzStartRead);return yycharat(0);}
"]"                   {yyparser.setStartOffset(zzStartRead);return yycharat(0);}
"$"                   {yyparser.setStartOffset(zzStartRead);return yycharat(0);}
.                     {
                       /* ignore '\0' */ 
                       yyparser.setStartOffset(zzStartRead); 
                       System.out.println("illegal character [" + yycharat(0) + "]");
                      }

