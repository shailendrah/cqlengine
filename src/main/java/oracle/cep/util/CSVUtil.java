/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/util/CSVUtil.java /main/5 2009/12/30 21:49:27 hopark Exp $ */

/* Copyright (c) 2007, 2009, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
   Helper class of Comma Separted format handling

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    hopark      12/18/09 - support single quote
    hopark      07/03/07 - handle null objects
    hopark      06/20/07 - cleanup
    najain      03/02/07 - 
    hopark      03/02/07 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/util/CSVUtil.java /main/5 2009/12/30 21:49:27 hopark Exp $
 *  @author  hopark  
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.util;

import java.io.IOException;
import java.io.StringReader;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import oracle.cep.exceptions.CEPException;
import oracle.cep.exceptions.ParserError;

public class CSVUtil
{
    public static final boolean CNV_NUMBER = true;

    private static final String DOUBLE_QUOTE = "\"";
    private static final String SINGLE_QUOTE = "\'";
    private static final char DOUBLE_QUOTE_CHAR ='"';
    private static final char SINGLE_QUOTE_CHAR ='\'';
    private static final char stateINIT = 'S';
    private static final char stateQUOTED_DATA = 'q';
    private static final char stateQUOTE_IN_QUOTED_DATA = 'Q';
    private static final char stateDATA = 'D';
    private static final char stateNEW_TOKEN = 'N';
    private static final char stateWHITESPACE = 'W';
    
    private boolean m_convertNumber;
    private char    m_delim;
    private char    m_quote;
    private boolean m_trim;
    
    public static String fromList(List<? extends Object> ar)
    {
        StringBuffer buf = new StringBuffer();
        for (Object o : ar) {
            if (o == null) continue;
            if (buf.length() > 0)
                buf.append(",");
            String val = o.toString();
            boolean needQuote = (val.lastIndexOf(',') >= 0);
            if (needQuote) buf.append("\"");
            buf.append(val);
            if (needQuote) buf.append("\"");
        }
        return buf.toString();
    }
    
    public static String fromArray(Object[] ar)
    {
        StringBuffer buf = new StringBuffer();
        for(int i = 0; i < ar.length; i++) {
            if (i > 0) buf.append(",");
            String val;
            if (ar[i] == null) val = "";
            else val = ar[i].toString();
            boolean needQuote = (val.lastIndexOf(',') >= 0);
            if (needQuote) buf.append("\"");
            buf.append(val);
            if (needQuote) buf.append("\"");
        }
        return buf.toString();
    }

    public static List<String> parseStr(String line)
        throws CEPException
    {
        try {
            return parseStr(line, ',');
        } catch (ParseException e) {
            throw new CEPException(ParserError.PARSER_ERROR);
        }
    }

    public static List<Long> parseLong(String line, boolean convertNumber)
        throws CEPException
    {
        try {
            List<String> r = parseStr(line, ',');
            List<Long> result = new ArrayList<Long>();
            for (int i = 0; i < r.size(); i++) {
                String s = r.get(i);
                result.add(Long.parseLong(s));
            }
            return result;
        } catch (NumberFormatException e) {
            throw new CEPException(ParserError.PARSER_ERROR);
        } catch (ParseException e) {
            throw new CEPException(ParserError.PARSER_ERROR);
        }
    }
    
    /**
     * Split the given line using the 'delim' delimiter
     * This method will not split if delimiter is mentioned in a quoted substring.
     * The quote can be single quote or double quote.
     * @param line
     * @param delim
     * @return
     * @throws ParseException
     */
    public static List<String> parseStr(String line, char delim) throws ParseException
    {
      char[] arr = line.toCharArray();
      List<String> result = new ArrayList<String>();
      Stack<Character> quoteStack = new Stack<Character>();
      StringBuilder nextToken = new StringBuilder();
      
      for (int i = 0; i < arr.length; i++) 
      {
        char ch = arr[i];
        if(quoteStack.isEmpty())
        {
          if(ch ==delim)
          {
            result.add(nextToken.toString());
            nextToken = new StringBuilder();
          }
          // This is to allow double quotes inside literal ,
          // Double quote is enclosed/escaped by single quotes
          // no need to validate the quotes
           else if (ch == SINGLE_QUOTE_CHAR && (i+2 < arr.length) && arr[i + 1] == DOUBLE_QUOTE_CHAR
                     && arr[i + 2] == SINGLE_QUOTE_CHAR) {
            nextToken.append(ch);
            nextToken.append(arr[i + 1]);
            nextToken.append(arr[i + 2]);
            i = i + 2;
          }
          else if(ch == SINGLE_QUOTE_CHAR || ch == DOUBLE_QUOTE_CHAR)
          {
            quoteStack.push(ch); 
            nextToken.append(ch);
          }
          else
            nextToken.append(ch);
        }
        else
        {
            // This is to allow double quotes inside literal ,
            // Double quote is enclosed/escaped by single quotes
            // no need to validate the quotes
            if (ch == SINGLE_QUOTE_CHAR && (i+2 < arr.length) && arr[i + 1] == DOUBLE_QUOTE_CHAR
                 && arr[i + 2] == SINGLE_QUOTE_CHAR) {
                nextToken.append(ch);
                nextToken.append(arr[i + 1]);
                nextToken.append(arr[i + 2]);
                i = i + 2;
            }
            // This is to allow single quotes inside the literal, append as  it is
            else if (ch == SINGLE_QUOTE_CHAR
                     && quoteStack.peek() == DOUBLE_QUOTE_CHAR) {
                nextToken.append(ch);
            } else if (ch == SINGLE_QUOTE_CHAR || ch == DOUBLE_QUOTE_CHAR) {
                if (quoteStack.peek() == ch)
                    quoteStack.pop();
                else
                    quoteStack.push(ch);
                nextToken.append(ch);
            } else
                nextToken.append(ch);       
        }
      }
      // Add the pending parsed token to result as final token
      result.add(nextToken.toString());
      if(!quoteStack.isEmpty())
        throw new ParseException("Given input string " + line + " has unclosed quotes", line.length());      
      return result;
    }

    public static String[] split(String line, char delim) throws ParseException
    {
        List<String> r = parseStr(line, delim);
        return r.toArray(new String[0]);
    }
    
}


