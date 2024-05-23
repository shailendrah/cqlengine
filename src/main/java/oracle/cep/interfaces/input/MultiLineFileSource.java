/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/interfaces/input/MultiLineFileSource.java /main/15 2012/01/20 11:47:14 sbishnoi Exp $ */

/* Copyright (c) 2008, 2012, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    sbishnoi    01/12/12 - support for timezone
    sbishnoi    10/03/11 - changing format to intervalformat
    sbishnoi    08/29/11 - support for interval year to month based operations
    hopark      10/28/09 - support long char
    sborah      06/29/09 - support for bigdecimal
    hopark      03/06/09 - add opaque type
    sborah      05/20/09 - enable totalOrderingFlag in Application TS
    hopark      02/17/09 - support boolean as external datatype
    hopark      12/04/08 - add toString
    hopark      11/28/08 - remove tValueSet(str)
    hopark      10/09/08 - remove statics
    sbishnoi    08/01/08 - support for nanosecond;
    udeshmuk    05/07/08 - restructure error-handling
    mthatte     04/02/08 - setting isderivedTS
    udeshmuk    02/28/08 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/interfaces/input/MultiLineFileSource.java /main/12 2009/11/21 07:38:14 hopark Exp $
 *  @author  udeshmuk
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.interfaces.input;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.Iterator;
import java.util.logging.Level;

import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVFormat;

import oracle.cep.common.CEPDate;
import oracle.cep.common.CEPDateFormat;
import oracle.cep.common.Constants;
import oracle.cep.common.Datatype;
import oracle.cep.common.TimestampFormat;
import oracle.cep.dataStructures.external.AttributeValue;
import oracle.cep.dataStructures.external.TupleKind;
import oracle.cep.dataStructures.external.TupleValue;
import oracle.cep.exceptions.CEPException;
import oracle.cep.exceptions.InterfaceError;
import oracle.cep.execution.ExecException;
import oracle.cep.interfaces.InterfaceException;
import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;
import oracle.cep.service.ExecContext;
import org.apache.commons.csv.CSVRecord;

public class MultiLineFileSource extends TableSourceBase {

  CSVParser parser;
  Iterator<CSVRecord> parsed;
  CSVFormat csvFormat;

  CSVRecord currLine;

  CSVRecord nextLine;

  private boolean bEof;

  private FileReader reader;

  private BufferedReader bufReader;
  
  private String filename;

  private boolean noTimeStamp;

  private boolean isSign;

  private int sub = 0;
  
  private char fs;
  
  private char ec;
  
  private static final String plus   = "+";
  
  private static final String minus  = "-";
  
  private static final String hbmark = "h";
  
  private static final String update = "u";
  
  private static final String upsert = "m";
  
  public MultiLineFileSource(ExecContext exc, String fileName, char fieldSeparator, char encapsulator)
  {
    super(exc);
    parser = null;
    csvFormat = null;
    currLine = null;
    nextLine = null;
    bEof = false;
    reader = null;
    bufReader = null;
    filename = fileName;
    noTimeStamp = false;
    isSign = false;
    sub = 0;
    fs  = fieldSeparator;
    ec  = encapsulator;
    
  }
  
  private void parseSchema() throws InterfaceException
  {
    //just skip the first line
    parsed.next();
  }
  
  public void start() throws CEPException
  {
    // This will handle allocateTuple also
    super.start();
    try
    {
      // Clear EOF
      bEof = false;

      // Setup the file for reading
      reader = new FileReader(filename);
      bufReader = new BufferedReader(reader);
      /* ',' is the default field delimiter
       * '"' is the default encapsulator
       * '!' is comment char (may need to change this)
       * ignoreLeadingWhiteSpaces is true
       * interpretUnicodeEscapes is false
       * ignoreEmptyLines is true
       */

      csvFormat = CSVFormat.newFormat(fs).withQuote(ec).withCommentMarker('!').withIgnoreSurroundingSpaces().withIgnoreEmptyLines();
      parser = new CSVParser(bufReader, csvFormat);
      parsed = parser.iterator();

      parseSchema();
      if (!parsed.hasNext())
      {
        long lineno = parser.getCurrentLineNumber()-1;
        throw new InterfaceException(InterfaceError.UNEXPECTED_EOF,
          new Object[]{filename, lineno});
      }
      currLine = parsed.next();
      nextLine = parsed.hasNext() ? parsed.next() : null;
      if (isSystemTimeStamped || isDerivedTimeStamped)
        noTimeStamp = true;
      if (!isStream) isSign = true;

      /*
       * 'sub' is initialized to zero and it represents the number to be subtracted 
       * from the index into the string array given by the parser to get the index into
       * the attribute array. It is used in parseTuple method.
       * Following table explains the logic:
       * ----------------------------------------------------------
       * i/p format  | noTimeStamp | isSign | strIndex | attrIndex
       * ----------------------------------------------------------
       *  +, "data"  |   true      | true   |   1      |   0  
       *  "data"     |   true      | false  |   0      |   0
       *  ts,+,"data |   false     | true   |   2      |   0
       *  ts,"data"  |   false     | false  |   1      |   0
       * ----------------------------------------------------------
       * strIndex is the position where value of the first attribute of the tuple 
       * can be found in the string of array returned by parser.
       * The two code statements below capture the logic in this table. 
       */
      if (!noTimeStamp) sub++;
      if (isSign) sub++;
    }
    catch (CEPException e)
    {
      LogUtil.logStackTrace(LoggerType.TRACE, Level.FINE, e);
      if (e instanceof InterfaceException)
        throw e;
      else
        throw new CEPException(InterfaceError.FILE_OPERATION_FAILURE, e,
                             new Object[]{filename});
    }
    catch (IOException e)
    {
      throw new CEPException(InterfaceError.FILE_OPERATION_FAILURE, e,
          new Object[]{filename});
    }
  }
  
  public void end() throws CEPException
  {
    try
    {
      parser = null;
      bufReader.close();
      bufReader = null;
      reader.close();
      reader = null;
      sub = 0;
    }
    catch (Exception e)
    {
      LogUtil.logStackTrace(LoggerType.TRACE, Level.FINE, e);
      throw new CEPException(InterfaceError.FILE_OPERATION_FAILURE, e,
                             new Object[]{filename});
    }
  }
  
  private void populateAttrValueInTuple(int pos, String value)
    throws InterfaceException, CEPException
  {
    AttributeValue a;
    long lineno = parser.getCurrentLineNumber()-1;
    
    a = tuple.getAttribute(pos);
    if (value.equalsIgnoreCase(""))
    {
      a.setBNull(true);
      return;
    }
    else
      a.setBNull(false);
    
    try
    {
      switch(attrMetadata[pos].getDatatype().getKind())
      {
        case TIMESTAMP:
        {
          CEPDateFormat dateFormatParser = CEPDateFormat.getInstance();
          TimestampFormat tsFormat = attrMetadata[pos].getTimestampFormat();
          
          CEPDate d = dateFormatParser.parse(value, tsFormat);
          tuple.tValueSet(pos, d.getValue());
          tuple.tFormatSet(pos, d.getFormat());
        }
        break;
        case CHAR:
          tuple.cValueSet(pos, value.toCharArray());
          break;
        case XMLTYPE:
          tuple.xValueSet(pos, value.toCharArray());
          break;
        case BYTE:
          char[] hex = value.toCharArray();
          byte[] bytes = Datatype.hexToByte(hex);
          tuple.bValueSet(pos, bytes);
          break;
        case INTERVAL:
          tuple.vValueSet(pos, value, attrMetadata[pos].getIntervalFormat());
          break;
        case INTERVALYM:
          tuple.vymValueSet(pos, value, attrMetadata[pos].getIntervalFormat());
          break;
        case INT:
          tuple.iValueSet(pos, Integer.parseInt(value));
          break;
        case BIGINT:
          tuple.lValueSet(pos, Long.parseLong(value));
          break;
        case FLOAT:
          tuple.fValueSet(pos, Float.parseFloat(value));
          break;
        case DOUBLE:
          tuple.dValueSet(pos, Double.parseDouble(value));
          break;
        case BIGDECIMAL:
          tuple.nValueSet(pos, new BigDecimal(value),
              attrMetadata[pos].getPrecision(),
              attrMetadata[pos].getScale());
          break;
        case BOOLEAN:
          {
            try
            {
              boolean b = Datatype.strToBoolean(value); 
              tuple.boolValueSet(pos, b);
            }catch(NumberFormatException e)
            {
              throw new InterfaceException(InterfaceError.INVALID_BOOLEAN,
                  new Object[]{filename, lineno, value});
            }
          }
          break;
      }
    }
    catch(NumberFormatException e)
    {
      throw new InterfaceException(InterfaceError.INVALID_NUMBER,
        new Object[]{filename, lineno,value});
    }
    catch(ParseException e)
    {
      throw new InterfaceException(InterfaceError.INVALID_TIMESTAMP,
        new Object[]{filename, lineno});
    }
  }
  
  private boolean parseTuple() throws InterfaceException,CEPException
  {
    long lineno = 0;
    long timeStamp = 0;
    if (currLine!=null)
    {
      try
      {
        if (currLine.get(0).equalsIgnoreCase(hbmark))
        { // heart beat tuple
          lineno = parser.getCurrentLineNumber()-1;
          if (currLine.size() < 2)
            throw new InterfaceException(InterfaceError.UNEXPECTED_EOL,
              new Object[]{filename, lineno});
          if (currLine.size() > 2)
            throw new InterfaceException(InterfaceError.SCHEMA_MISMATCH,
                                         new Object[]{filename});
          tuple.setBHeartBeat(true);
          try
          {
            timeStamp = Long.parseLong(currLine.get(1));
            // As we are assuming that time stamp given in file data is in
            //millisecond; Convert time stamp to nanosecond unit;
            if(convertTs)
              tuple.setTime(timeStamp * 1000000);
            else
              tuple.setTime(timeStamp);
          }
          catch(NumberFormatException e)
          {
            throw new InterfaceException(InterfaceError.INVALID_TIMESTAMP,
              new Object[]{filename, lineno});
          }
        }
        else //normal tuple 
        {
          lineno = parser.getCurrentLineNumber()-1;
          //error checking
          int noOfAttrs=0;
          if (isStream)
            noOfAttrs = numAttrs - 1;
          else
            noOfAttrs = numAttrs;
          if (currLine.size() < noOfAttrs+sub)
            throw new InterfaceException(InterfaceError.UNEXPECTED_EOL,
              new Object[]{filename, lineno});
          if (currLine.size() > noOfAttrs+sub)
            throw new InterfaceException(InterfaceError.SCHEMA_MISMATCH,
                                         new Object[]{filename});
          //normal processing
          for (int i=0; i < currLine.size(); i++)
          {
            switch(i)
            {
              case 0: 
                if (!noTimeStamp)
                { //ts,[+],data  ..square brackets indicate sign is optional
                  try
                  {
                    timeStamp = Long.parseLong(currLine.get(i));
                    // As we are assuming that time stamp given in file data is
                    // in millisecond; Convert time stamp to nanosecond unit;
                    if(convertTs)
                      tuple.setTime(timeStamp * 1000000);
                    else
                      tuple.setTime(timeStamp);
                  }
                  catch(NumberFormatException e)
                  {
                    throw new InterfaceException(InterfaceError.INVALID_TIMESTAMP,
                      new Object[]{filename, lineno});
                  }
                }
                else if(isSign)
                { // +,data 
                  if (currLine.get(i).equalsIgnoreCase(plus))
                    tuple.setKind(TupleKind.PLUS);
                  else if (currLine.get(i).equalsIgnoreCase(minus))
                    tuple.setKind(TupleKind.MINUS);
                  else if (currLine.get(i).equalsIgnoreCase(update))
                    tuple.setKind(TupleKind.UPDATE);
                  else if (currLine.get(i).equalsIgnoreCase(upsert))
                    tuple.setKind(TupleKind.UPSERT);
                  else 
                    throw new InterfaceException(InterfaceError.INVALID_ELEMENT_KIND,
                      currLine.get(i));
                }
                else //line starts with data
                  populateAttrValueInTuple(i-sub, currLine.get(i));
                break;
              case 1:
                if (isSign && !noTimeStamp)
                { // ts, +, data 
                  if (currLine.get(i).equalsIgnoreCase(plus))
                    tuple.setKind(TupleKind.PLUS);
                  else if (currLine.get(i).equalsIgnoreCase(minus))
                    tuple.setKind(TupleKind.MINUS);
                  else if (currLine.get(i).equalsIgnoreCase(update))
                    tuple.setKind(TupleKind.UPDATE);
                  else if (currLine.get(i).equalsIgnoreCase(upsert))
                    tuple.setKind(TupleKind.UPSERT);
                  else 
                    throw new InterfaceException(InterfaceError.INVALID_ELEMENT_KIND,
                      currLine.get(i));
                }
                else
                  populateAttrValueInTuple(i-sub, currLine.get(i));
                break;
              default:
                populateAttrValueInTuple(i-sub, currLine.get(i));
            }
          }
        }
        //advance to next record
        currLine = nextLine;
        if (nextLine!=null)
        {
          nextLine = parsed.hasNext() ? parsed.next():null;
        }
      }
      catch(InterfaceException e){ 
        // This is a soft exec exception so when next time the Streamsrc/Relsrc calls getNext()
        // it should be able to proceed with next line(record).
        // For any other exception the processing should halt and hence we could not include the
        // common part in finally block.
        LogUtil.logStackTrace(LoggerType.TRACE, Level.FINE, e);
        currLine = nextLine;
        if (nextLine!=null)
        {
          nextLine = parsed.hasNext() ? parsed.next():null;
        }
        throw e;
      }
    }
    else 
    {
      bEof = true;
      return false;
    }
    return true;
  }

   
  public TupleValue getNext() throws CEPException
  {
    // Have we already finished all the tuples?
    if (bEof)
      return null;
   
    if (parseTuple())
    {
      if (isSystemTimeStamped) 
        insertTimestamp(tuple);
      else
      { // set the total ordering flag based on the timestamp value
        // of the next tuple
        Long nextTs = getNextTupleTimestamp();
        
        /*if(nextTs != Constants.NULL_TIMESTAMP && tuple.getTime() < nextTs)
        {
          System.out.println("currts= " +tuple.getTime() +" nextts "+nextTs + " TRUE");
          tuple.setTotalOrderGuarantee(true);
        }
        else
        {
          System.out.println("currts= "+tuple.getTime() +" nextts "+nextTs + " FALSE");
          tuple.setTotalOrderGuarantee(false);
        }*/
        tuple.setTotalOrderGuarantee(false);
      }
      
      return tuple;
    }
    return null;
  }
  
  /**
   * 
   * @return The timestamp value of the next tuple if it exists, 
   *         otherwise , return a null timestamp value.
   * @throws CEPException
   */
  private Long getNextTupleTimestamp() 
  {
    try
    {
      return getOldestTs();
    }
    catch(CEPException e)
    {
      
    }
    return Constants.NULL_TIMESTAMP;
  }

  public long getOldestTs() throws InterfaceException
  {
    if (nextLine == null || bEof || noTimeStamp)
      return Constants.NULL_TIMESTAMP;
    else 
    {
      long lineno = parser.getCurrentLineNumber()-1;
      
      try
      {
        if (nextLine.get(0).equalsIgnoreCase(hbmark))
        { // heartbeat tuple
          if (nextLine.size() < 2)
            throw new InterfaceException(InterfaceError.UNEXPECTED_EOL,
              new Object[]{filename, lineno});
          if (nextLine.size() > 2)
            throw new InterfaceException(InterfaceError.SCHEMA_MISMATCH,
                                         new Object[]{filename});
          oldTs =  Long.parseLong(nextLine.get(1));
        }
        else
        { //normal tuple.. !noTimeStamp will be true here.
          oldTs = Long.parseLong(nextLine.get(0));
        }
        //Assumption: Input Tuple time-stamp in file source is of millisecond
        //time unit and our system's granularity is nanosecond unit of time      
        if(convertTs)
          oldTs = oldTs * 1000000;
       
        return oldTs;
      }
      catch(NumberFormatException e)
      {
        throw new InterfaceException(InterfaceError.INVALID_TIMESTAMP,
          new Object[]{filename, lineno});
      }
    }
  }

  public boolean hasNext() throws ExecException
  {
    if (bEof)
      return false;

    return true;
  }
 
  public int getNumAttrs()
  {
    return numAttrs;
  }
  
  /*public Datatype getAttrType(int pos)
  {
    return attrMetadata[pos].getDatatype();
  }
  
  public int getAttrLength(int pos)
  {
    return attrMetadata[pos].getLength();
  }*/
  
  /* for debugging purpose
  private void printLine(String[] line)
  {
    if (line==null) { 
      System.out.println("Null line");
      return;
    }
    for (int i=0; i < line.length; i++)
      if (line[i]!=null) System.out.print(line[i]+" ");
    System.out.println();
  }*/

  public String toString()
  {
    return toString("MultiLineFileSource(" + filename + ")");
  }
}

