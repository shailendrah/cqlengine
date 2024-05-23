/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/interfaces/input/FileSource.java /main/45 2013/05/13 06:00:34 sbishnoi Exp $ */

/* Copyright (c) 2006, 2013, Oracle and/or its affiliates. 
All rights reserved. */

/*
 DESCRIPTION
 Declares FileSource in package oracle.cep.interfaces.input.
 
 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>
 
 NOTES
 <other useful comments, qualifications, etc.>
 
 MODIFIED    (MM/DD/YY)
 sbishnoi  10/03/11 - changing format to intervalformat
 sbishnoi  08/28/11 - adding support for interval year to month
 hopark    02/22/10 - add startDelay
 hopark    10/30/09 - support large string
 sborah    06/24/09 - support for bigdecimal
 sbishnoi  07/30/09 - support for total ordering flag
 hopark    06/22/09 - add obj support
 hopark    03/05/09 - add opaque type
 sborah    05/20/09 - enable totalOrderingFlag in Application TS
 hopark    05/08/09 - support utf8
 sbishnoi  04/08/09 - setting totalordering flag if source is systemtimestamped
 hopark    02/17/09 - support boolean as external datatype
 hopark    02/05/09 - objtype support
 hopark    12/04/08 - add toString
 hopark    11/28/08 - remove tValueSet(str)
 hopark    10/15/08 - TupleValue refactoring
 hopark    10/09/08 - remove statics
 sbishnoi  07/29/08 - support for nanosecond; handling timestamp unit changes
 udeshmuk  05/04/08 - fix the problem of losing precision
 mthatte   04/22/08 - removed isBnull from TupleValue cnstructor
 mthatte   04/02/08 - adding isderivedTS
 sbishnoi  03/26/08 - remove commented println statements
 udeshmuk  03/17/08 - restructure hasNext and getOldestTs.
 udeshmuk  03/13/08 - parameterize errors.
 sbishnoi  02/11/08 - cep error message parametrization
 udeshmuk  01/30/08 - support for double data type.
 udeshmuk  01/17/08 - change in data type of time in TupleValue.
 udeshmuk  12/20/07 - add delay for systemtimestamped.
 najain    10/19/07 - add xmltype
 udeshmuk  11/22/07 - call insertTimestamp in getNext.
 sbishnoi  10/30/07 - support for update tuple
 udeshmuk  09/18/07 - Correcting indentation issues.
 udeshmuk  09/14/07 - Handling null line in the input file.
 mthatte   07/31/07 - null pointer exception. modified start().
 mthatte   07/20/07 - catch schema_mismatch errors
 najain    07/17/07 - bug
 parujain  05/24/07 - softexecExceptions
 hopark    05/16/07 - remove printStackTrace
 rkomurav  03/27/07 - delay scale
 najain    03/12/07 - bug fix
 rkomurav  01/19/07 - fix a bug
 najain    01/22/07 - bug fix
 rkomurav  12/18/06 - fix the bug for null values not recognised at EOL and EOF
 hopark    11/16/06 - add bigint datatype
 najain    11/07/06 - add getOldestTs
 dlenkov   10/25/06 - byte data type fixes
 parujain  10/06/06 - Interval datatype
 parujain  09/21/06 - to_timestamp built-in function
 anasrini  09/13/06 - extend TableSourceBase
 najain    08/10/06 - minor bugs
 skmishra  08/04/06 - Timestamp datatype
 parujain  08/03/06 - Timestamp datastructure
 najain    07/28/06 - handle silent relations 
 najain    07/28/06 - handle nulls 
 najain    05/22/06 - heartbeat
 najain    05/19/06 - add sign 
 najain    04/24/06 - add BufferedReader 
 skaluska  04/06/06 - remove use of deprecated method 
 najain    03/31/06 - Read timestamp 
 skaluska  03/25/06 - implementation
 skaluska  03/22/06 - implementation
 skaluska  03/21/06 - Creation
 skaluska  03/21/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/interfaces/input/FileSource.java /main/45 2013/05/13 06:00:34 sbishnoi Exp $
 *  @author  skaluska
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.interfaces.input;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StreamTokenizer;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;

import oracle.cep.common.CEPDate;
import oracle.cep.common.CEPDateFormat;
import oracle.cep.common.Constants;
import oracle.cep.common.Datatype;
import oracle.cep.common.IntervalFormat;
import oracle.cep.dataStructures.external.AttributeValue;
import oracle.cep.dataStructures.external.BigDecimalAttributeValue;
import oracle.cep.dataStructures.external.BigintAttributeValue;
import oracle.cep.dataStructures.external.BooleanAttributeValue;
import oracle.cep.dataStructures.external.ByteAttributeValue;
import oracle.cep.dataStructures.external.CharAttributeValue;
import oracle.cep.dataStructures.external.DoubleAttributeValue;
import oracle.cep.dataStructures.external.FloatAttributeValue;
import oracle.cep.dataStructures.external.IntAttributeValue;
import oracle.cep.dataStructures.external.IntervalAttributeValue;
import oracle.cep.dataStructures.external.IntervalYMAttributeValue;
import oracle.cep.dataStructures.external.ObjAttributeValue;
import oracle.cep.dataStructures.external.TimestampAttributeValue;
import oracle.cep.dataStructures.external.TupleKind;
import oracle.cep.dataStructures.external.TupleValue;
import oracle.cep.dataStructures.external.XmltypeAttributeValue;
import oracle.cep.exceptions.CEPException;
import oracle.cep.exceptions.InterfaceError;
import oracle.cep.extensibility.cartridge.ICartridgeContext;
import oracle.cep.extensibility.type.IComplexType;
import oracle.cep.extensibility.type.IConstructorMetadata;
import oracle.cep.extensibility.type.IType;
import oracle.cep.extensibility.type.ITypeLocator;
import oracle.cep.interfaces.InterfaceException;
import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;
import oracle.cep.parser.CartridgeHelper;
import oracle.cep.service.ExecContext;

/**
 * FileSource reads tuples from a file and returns them.
 * 
 * @author skaluska
 */
public class FileSource extends TableSourceBase
{
  /** filename */
  private String              filename;

  /** file reader */
  private FileInputStream     reader;

  /** buffered reader */
  private BufferedReader      bufReader;

  /** tokenizer for parse the input file */
  private StreamTokenizer     tokenizer;

  /** Types of attributes */
  private ArrayList<Datatype> attrTypes;

  /** Lengths of attributes */
  private ArrayList<Integer>  attrLen;

  /** Number of attributes */
  private int                 numAttrs;

  /** Tuple value to be returned */
  private TupleValue          tuple;
 
  /** Timestamp for the tuple */
  private long                timeStamp;
  /** whether we have reached EOF */
  private boolean             bEof;

  /** Is sign present with every element */
  private boolean             isSign;

  /** Is timeStamp present with every element */
  private boolean             noTimeStamp;

  /** heartBeat already read */
  private boolean             isHeartBeat;
  
  /** stores the precision and scale of bigdecimal values and are mapped by its
   * numAttrs position **/
  private HashMap<Integer,int[]> precisionScale;
  
  /** flag to check whether there should be any delay between two consecutively
   *  read tuples
   */
  private boolean             isDelay;
  
  /** delay scale is the amount of delay between two consecutive input tuples*/
  private int                 delayScale;
    
  /** system timestamp as base */
  private long                tsysbase;
  private long                tbase;
 
  private static final long   ONE_MINUTE = 60000;
  
  /** flag to check wehther total ordering flag is specified in schema*/
  private boolean             isTotalOrderingFlag;
  
  /** total ordering flag value */
  private boolean             totalOrderingFlagValue;
  
  /** initial delay */
  private int             startDelay = 0;

  /**
   * Constructor for FileSource
   * 
   * @param filename
   *          File name to read from
   */
  public FileSource(ExecContext ec, String filename)
  {
    super(ec);
    this.filename = filename;
    attrTypes = new ArrayList<Datatype>();
    attrLen = new ArrayList<Integer>();
    precisionScale = new HashMap<Integer,int[]>();
    timeStamp = 0;
    bEof = false;
    // The sign is not present unless the user explicity says so
    isSign = false;
    isHeartBeat = false;
    isDelay = false;
    noTimeStamp = false;
    totalOrderingFlagValue = false;
    isTotalOrderingFlag = false;
  }
  
  public FileSource(ExecContext ec, String filename, int delayScale)
  {
    this(ec, filename);
    isDelay = true;
    this.delayScale = delayScale;
    tsysbase = System.currentTimeMillis();

    tbase = tsysbase / 1000;
    long t = tbase / 300;
    if ((tbase % 300) == 0)
      tbase = (t * 300);
    else
      tbase = ((t + 1) * 300);
    tbase *= 1000;
  }

  public void setStartDelay(int n)
  {
     startDelay = n;  
  }
    
  /**
   * Reads the next number from the input stream
   * 
   * @return Number read
   * @throws IOException
   * @throws CEPException
   */
  private long readNumber() throws IOException, CEPException
  {
    int t;

    // Get the next token
    t = tokenizer.nextToken();

    // Check if it is a number
    if (t != StreamTokenizer.TT_NUMBER)
    {
      // Remember if we reached EOF
      if (t == StreamTokenizer.TT_EOF)
        bEof = true;
      else
        throw new CEPException(InterfaceError.NUMBER_NOT_FOUND);
    }
    
    boolean inRange = true;
    inRange = (tokenizer.nval >= Long.MIN_VALUE) &&
              (tokenizer.nval <= Long.MAX_VALUE);
    if (!inRange)
      throw new InterfaceException(InterfaceError.INVALID_NUMBER,
        new Object[]{filename, tokenizer.lineno(),tokenizer.nval});
    // Return value
    return (long)tokenizer.nval;
  }

  /**
   * Parses the schema line in the file This is expected to be of the form (<attr
   * type>[<attr length>]),... attr type is one of i, f, b , d, n or l
   * Also validates whether the schema is as specified by StreamSource in TableSourceBase.attrTypes and TableSourceBase.attrLen
   * 
   * @throws CEPException
   * @throws IOException
   */
  private void parseSchema() throws IOException, CEPException
  {
    int t;
    boolean found = false;

    numAttrs = 0;
    while ((t = tokenizer.nextToken()) != StreamTokenizer.TT_EOF)
    {

      switch (t)
      {
        case StreamTokenizer.TT_WORD:
          if (tokenizer.sval.equalsIgnoreCase("i"))
          {
            if(super.attrMetadata[numAttrs].getDatatype()!=Datatype.INT)
              throw new InterfaceException(InterfaceError.SCHEMA_MISMATCH,
                                           new Object[]{filename});
            attrTypes.add(numAttrs, Datatype.INT);
            attrLen.add(numAttrs, null);
          }
          else if (tokenizer.sval.equalsIgnoreCase("l"))
          {
            if(super.attrMetadata[numAttrs].getDatatype()!=Datatype.BIGINT)
              throw new InterfaceException(InterfaceError.SCHEMA_MISMATCH,
                                           new Object[]{filename});
            attrTypes.add(numAttrs, Datatype.BIGINT);
            attrLen.add(numAttrs, null);
          }
          else if (tokenizer.sval.equalsIgnoreCase("f"))
          {
            if(super.attrMetadata[numAttrs].getDatatype()!=Datatype.FLOAT)
              throw new InterfaceException(InterfaceError.SCHEMA_MISMATCH,
                                           new Object[]{filename});
            attrTypes.add(numAttrs, Datatype.FLOAT);
            attrLen.add(numAttrs, null);
          }
          else if (tokenizer.sval.equalsIgnoreCase("d"))
          {
            if(super.attrMetadata[numAttrs].getDatatype()!=Datatype.DOUBLE)
              throw new InterfaceException(InterfaceError.SCHEMA_MISMATCH,
                                           new Object[]{filename});
            attrTypes.add(numAttrs, Datatype.DOUBLE);
            attrLen.add(numAttrs, null);
          }
          else if (tokenizer.sval.equalsIgnoreCase("n"))
          {
            if(super.attrMetadata[numAttrs].getDatatype()!=Datatype.BIGDECIMAL)
              throw new InterfaceException(InterfaceError.SCHEMA_MISMATCH,
                                           new Object[]{filename});
            attrTypes.add(numAttrs, Datatype.BIGDECIMAL);
            attrLen.add(numAttrs, null);
            
            int precision = (int)readNumber();
            int scale     = (int)readNumber();
            precisionScale.put(numAttrs, new int[]{precision, scale});
          }
          else if (tokenizer.sval.equalsIgnoreCase("b"))
          {
            if(super.attrMetadata[numAttrs].getDatatype()!=Datatype.BYTE)
              throw new InterfaceException(InterfaceError.SCHEMA_MISMATCH,
                                           new Object[]{filename});
            int byteLen = (int)readNumber();
            if(byteLen > super.attrMetadata[numAttrs].getLength())
              throw new InterfaceException(InterfaceError.SCHEMA_MISMATCH,
                                           new Object[]{filename});
            attrTypes.add(numAttrs, Datatype.BYTE);
            attrLen.add(numAttrs, new Integer(byteLen));
          }
          else if (tokenizer.sval.equalsIgnoreCase("c"))
          {
            if(super.attrMetadata[numAttrs].getDatatype()!=Datatype.CHAR )
              throw new InterfaceException(InterfaceError.SCHEMA_MISMATCH,
                                           new Object[]{filename});
            int charLen = (int)readNumber();
            if(charLen > super.attrMetadata[numAttrs].getLength())
            {
              throw new InterfaceException(InterfaceError.SCHEMA_MISMATCH,
                                           new Object[]{filename});
            }
            attrTypes.add(numAttrs, Datatype.CHAR);
            attrLen.add(numAttrs, new Integer(charLen));
          }
          else if (tokenizer.sval.equalsIgnoreCase("x"))
          {
            if(super.attrMetadata[numAttrs].getDatatype()!=Datatype.XMLTYPE)
              throw new InterfaceException(InterfaceError.SCHEMA_MISMATCH,
                                           new Object[]{filename});
            attrTypes.add(numAttrs, Datatype.XMLTYPE);
            attrLen.add(numAttrs, null);
          }
          else if (tokenizer.sval.equalsIgnoreCase("t"))
          {
            if(super.attrMetadata[numAttrs].getDatatype()!=Datatype.TIMESTAMP)
              throw new InterfaceException(InterfaceError.SCHEMA_MISMATCH,
                                           new Object[]{filename});
            attrTypes.add(numAttrs, Datatype.TIMESTAMP);
            attrLen.add(numAttrs, null);
          }
          else if (tokenizer.sval.equalsIgnoreCase("v"))
          {
            if(super.attrMetadata[numAttrs].getDatatype()!=Datatype.INTERVAL)
              throw new InterfaceException(InterfaceError.SCHEMA_MISMATCH,
                                           new Object[]{filename});
            attrTypes.add(numAttrs, Datatype.INTERVAL);
            attrLen.add(numAttrs, null);
          }
          else if (tokenizer.sval.equalsIgnoreCase("vym"))
          {
            if(super.attrMetadata[numAttrs].getDatatype()!=Datatype.INTERVALYM)
              throw new InterfaceException(InterfaceError.SCHEMA_MISMATCH,
                                           new Object[]{filename});
            attrTypes.add(numAttrs, Datatype.INTERVALYM);
            attrLen.add(numAttrs, null);
          }
          else if (tokenizer.sval.equalsIgnoreCase("o"))
          {
            if(super.attrMetadata[numAttrs].getDatatype()!=Datatype.BOOLEAN)
              throw new InterfaceException(InterfaceError.SCHEMA_MISMATCH,
                                           new Object[]{filename});
            attrTypes.add(numAttrs, Datatype.BOOLEAN);
            attrLen.add(numAttrs, null);
          }
          else if (tokenizer.sval.equalsIgnoreCase("z"))
          {
            if(super.attrMetadata[numAttrs].getDatatype().getKind() != Datatype.Kind.OBJECT)
              throw new InterfaceException(InterfaceError.SCHEMA_MISMATCH,
                                           new Object[]{filename});
            attrTypes.add(numAttrs, super.attrMetadata[numAttrs].getDatatype());
            attrLen.add(numAttrs, null);
          }
          else if (tokenizer.sval.equalsIgnoreCase("s"))
          {
            isSign = true;
          }
          else if(tokenizer.sval.equalsIgnoreCase("TOF"))
          {
            isTotalOrderingFlag = true;
          }
          else
            throw new CEPException(InterfaceError.INVALID_ATTR_TYPE,
                                   tokenizer.sval);
          
          found = true;
          break;
        case ',':
          if (!found)
            throw new CEPException(InterfaceError.EMPTY_FIELD);
          found = false;
          continue;
        case StreamTokenizer.TT_EOL:
          // Finished parsing the schema line
          break;
        default:
          throw new CEPException(InterfaceError.INVALID_CHARACTER, t);
      }

      // We are done if we reached EOL
      if (t == StreamTokenizer.TT_EOL)
      { 
        if (numAttrs == 0) 
          throw new CEPException(InterfaceError.SCHEMA_NOT_FOUND);
        else  
          break;
      }
      // Else go for the next attr.
       numAttrs++; 
    }

    // Can't reach EOF right now
    if (t == StreamTokenizer.TT_EOF)
    {
      bEof = true;
      throw new CEPException(InterfaceError.UNEXPECTED_EOF,
                             new Object[]{filename, tokenizer.lineno()});
    }

    if (isSign)
      numAttrs--;
    if(isTotalOrderingFlag)
      numAttrs--;
  }

  /**
   * Allocate output tuple
   * 
   * @throws CEPException
   */
  private void allocateTuple() throws CEPException
  {
    int i;
    AttributeValue[] attrval;

    // Allocate attributes
    attrval = new AttributeValue[numAttrs];
    for (i = 0; i < numAttrs; i++)
    {
      String attrName = null;
      Integer l = attrLen.get(i);
      int length = (l == null) ? 0 : l.intValue();
      switch (attrTypes.get(i).getKind())
      {
        case INT:
          attrval[i] = new IntAttributeValue(attrName, 0);
          break;
        case BIGINT:
          attrval[i] = new BigintAttributeValue(attrName, 0);
          break;
        case FLOAT:
          attrval[i] = new FloatAttributeValue(attrName, 0);
          break;
        case DOUBLE:
          attrval[i] = new DoubleAttributeValue(attrName, 0);
          break;
        case BIGDECIMAL:
          attrval[i] = new BigDecimalAttributeValue(attrName,
                             attrMetadata[i].getPrecision(),
                             attrMetadata[1].getScale(), 0);
          break;
        case CHAR:
          attrval[i] = new CharAttributeValue(attrName, null);
          break;
        case XMLTYPE:
          attrval[i] = new XmltypeAttributeValue(attrName);
          break;
        case BYTE:
          attrval[i] = new ByteAttributeValue(attrName, null);
          break;
        case TIMESTAMP:
          attrval[i] = new TimestampAttributeValue(attrName, 0);
          break;
        case INTERVAL:
          attrval[i] = new IntervalAttributeValue(attrName, new String());
          IntervalFormat format = this.attrMetadata[i].getIntervalFormat();
          ((IntervalAttributeValue)attrval[i]).setFormat(format);
          break;
        case INTERVALYM:
          IntervalFormat fmt = this.attrMetadata[i].getIntervalFormat();
          attrval[i] 
            = new IntervalYMAttributeValue(attrName, new String(), fmt);
          break;
        case OBJECT:
          attrval[i] = new ObjAttributeValue(attrName, attrTypes.get(i));
          break;
        case BOOLEAN:
          attrval[i] = new BooleanAttributeValue(attrName, false);
          break;
        default:
          throw new CEPException(InterfaceError.INVALID_ATTR_TYPE, 
                                 new Object[]{attrTypes.get(i)});
      }
      if (noTimeStamp)
        tuple = new TupleValue(null, Constants.NULL_TIMESTAMP, attrval, false);
      else
        tuple = new TupleValue(null, 0, attrval, false);
      
      
      // Set the total order guarantee true if stream is system timeStamped
      tuple.setTotalOrderGuarantee(isSystemTimeStamped);

      // The tuples are positive by default
      if (!isSign)
        tuple.setKind(TupleKind.PLUS);
      
      
    }
  }

  /**
   * Parses the input file for the next tuple value
   * 
   * @return true if next tuple value found else false
   * @throws CEPException
   * @throws IOException
   * @throws ParseException
   */
  private boolean parseTuple() throws InterfaceException
  {
    int i = 0;
    int t;
    AttributeValue a;
    boolean found = false;
    boolean heartBeat = false;
    totalOrderingFlagValue = false;
    
    synchronized (this)
    {
      try 
      {
        while ((t = tokenizer.nextToken()) == StreamTokenizer.TT_EOL) ; // skip the EOLs
        if (t != StreamTokenizer.TT_EOF)
        {
          tokenizer.pushBack();
        } 
        else 
        {
          bEof = true;
          return false;
        }
      } 
      catch (IOException e)
      {
        throw new InterfaceException(InterfaceError.FILE_OPERATION_FAILURE, e,
                                     new Object[]{filename}); 
      }
      if (!isHeartBeat)
      {
        if (!noTimeStamp)
        {
          try
          {
            // The first field MUST be the timestamp represented as a long
            timeStamp = readNumber();
          }
          catch (CEPException e)
          {
            // Does it represent a heartbeat:
            if (e.getErrorCode() == InterfaceError.NUMBER_NOT_FOUND)
            {
              tokenizer.pushBack();
              heartBeat = true;
            }
            else
              throw new InterfaceException(InterfaceError.INVALID_TIMESTAMP,
                new Object[]{filename, tokenizer.lineno()});
          }
          catch (IOException e)
          {
            throw new InterfaceException(InterfaceError.INVALID_TIMESTAMP,
              new Object[]{filename, tokenizer.lineno()});
          }
        }

        if (heartBeat)
        {
          // Get the next token
          try
          {
            t = tokenizer.nextToken();
          }
          catch (IOException e)
          {
            throw new InterfaceException(InterfaceError.NUMBER_NOT_FOUND);
          }
          
          // It better be a heartbeat row
          if ((t != StreamTokenizer.TT_WORD)
              || (!tokenizer.sval.equalsIgnoreCase("h")))
            throw new InterfaceException(InterfaceError.NUMBER_NOT_FOUND);
          isHeartBeat = true;
        }
      }

      if (isHeartBeat)
      {
        tuple.setBHeartBeat(true);
        try
        {
          timeStamp = readNumber();
        }
        catch (Exception e)
        {
          throw new InterfaceException(InterfaceError.INVALID_TIMESTAMP,
            new Object[]{filename, tokenizer.lineno()});
        }
        heartBeat = true;
      }
      else
        tuple.setBHeartBeat(false);

      isHeartBeat = false;

      // Assumption: Input Tuple time-stamp in file source is of millisecond
      // time unit and our system's granularity is nanosecond unit of time
      if(convertTs)
        timeStamp = timeStamp * 1000000;      
      if (!noTimeStamp)
        tuple.setTime(timeStamp);

      // Read the total ordering flag (if mentioned in the schema)
      if(isTotalOrderingFlag && !bEof)
      {
        // Get the next token (it should be value of total ordering flag)
        try
        {
          t = tokenizer.nextToken();
        }
        catch(IOException e)
        {
          throw new InterfaceException(InterfaceError.VALUE_NOT_FOUND);
        }
        if(t == StreamTokenizer.TT_EOF)
        {
          bEof = true;
          throw new InterfaceException(InterfaceError.UNEXPECTED_EOF,
              new Object[]{filename, tokenizer.lineno()});
        }
        if(t == StreamTokenizer.TT_WORD && 
            tokenizer.sval.equalsIgnoreCase("t"))
          totalOrderingFlagValue= true;
        else
          totalOrderingFlagValue = false;
      }
      
      // Read the sign if needed
      if (isSign && !bEof && !heartBeat)
      {
        // Get the next token
        try
        {
          t = tokenizer.nextToken();
        }
        catch (IOException e)
        {
          throw new InterfaceException(InterfaceError.VALUE_NOT_FOUND);
        }
        if (t == StreamTokenizer.TT_EOF)
        {
          bEof = true;
          throw new InterfaceException(InterfaceError.UNEXPECTED_EOF,
            new Object[]{filename, tokenizer.lineno()});
        }

        if (t == '+')
          tuple.setKind(TupleKind.PLUS);
        else if (t == '-')
          tuple.setKind(TupleKind.MINUS);
        else if(t == StreamTokenizer.TT_WORD && 
            (tokenizer.sval.equalsIgnoreCase("u")))
          tuple.setKind(TupleKind.UPDATE);
        else if(t == StreamTokenizer.TT_WORD && 
            (tokenizer.sval.equalsIgnoreCase("m")))
          tuple.setKind(TupleKind.UPSERT);
        else
          throw new InterfaceException(InterfaceError.INVALID_CHARACTER, t);
      }

      try
      {
        while ((t = tokenizer.nextToken()) != StreamTokenizer.TT_EOF)
        {
          switch (t)
          {
            case ',':
              if (!found)
              {
                a = tuple.getAttribute(i);
                a.setBNull(true);
                break;
              }
              else
              {
                a = tuple.getAttribute(i - 1);
                found = false;
                a.setBNull(false);
                continue;
              }
            case '"':
              // if field separator "," missing
              if (found)
                throw new InterfaceException(InterfaceError.INVALID_CHARACTER, t);
              if (attrTypes.get(i) == Datatype.TIMESTAMP)
              {
                CEPDateFormat df = CEPDateFormat.getInstance();
                try
                {
                  CEPDate d = df.parse(tokenizer.sval);
                  tuple.tValueSet(i, d.getValue());
                  tuple.tFormatSet(i, d.getFormat());
                }
                catch(ParseException e)
                {
                  throw new InterfaceException(InterfaceError.INVALID_TIMESTAMP,
                      new Object[]{filename, tokenizer.lineno()});
                }
              }
              else if (attrTypes.get(i) == Datatype.CHAR)
              {
                tuple.cValueSet(i, (tokenizer.sval).toCharArray());
              }
              else if (attrTypes.get(i) == Datatype.XMLTYPE)
              {
                tuple.xValueSet(i, (tokenizer.sval).toCharArray());
              }
              else if (attrTypes.get(i) == Datatype.BYTE)
              {
                char[] hex = tokenizer.sval.toCharArray();
                byte[] bytes = Datatype.hexToByte(hex);
                tuple.bValueSet(i, bytes);
              }
              else if (attrTypes.get(i) == Datatype.INTERVAL)
              {
                tuple.vValueSet(i, tokenizer.sval, attrMetadata[i].getIntervalFormat());
              }
              else if (attrTypes.get(i) == Datatype.INTERVALYM)
              {
                tuple.vymValueSet(i, tokenizer.sval, attrMetadata[i].getIntervalFormat());
              }
              else if (attrTypes.get(i) == Datatype.BOOLEAN)
              {
                try
                {
                  boolean b = Datatype.strToBoolean(tokenizer.sval); 
                  tuple.boolValueSet(i, b);
                }catch(NumberFormatException e)
                {
                  throw new InterfaceException(InterfaceError.INVALID_BOOLEAN,
                      new Object[]{filename, tokenizer.lineno(), tokenizer.sval});
                }
              }
              else if (attrTypes.get(i).getKind() == Datatype.Kind.OBJECT)
              {
                setObject(tuple, i, tokenizer.sval, false);
              } else
                
                throw new InterfaceException(InterfaceError.INVALID_TIMESTAMP,
                  new Object[]{filename, tokenizer.lineno()});
              found = true;
              break;
            case StreamTokenizer.TT_WORD:
              if (found)
                throw new InterfaceException(InterfaceError.INVALID_CHARACTER, t);
              if (attrTypes.get(i) == Datatype.BYTE)
              {
                char[] hex = tokenizer.sval.toCharArray();
                byte[] bytes = Datatype.hexToByte(hex);
                tuple.bValueSet(i, bytes);
              }
              else if (attrTypes.get(i) == Datatype.CHAR)
              {
                char[] v = tokenizer.sval.toCharArray();
                tuple.cValueSet(i, v);
              }
              else if (attrTypes.get(i) == Datatype.BOOLEAN)
              {
                try
                {
                  boolean b = Datatype.strToBoolean(tokenizer.sval); 
                  tuple.boolValueSet(i, b);
                }catch(NumberFormatException e)
                {
                  throw new InterfaceException(InterfaceError.INVALID_BOOLEAN,
                      new Object[]{filename, tokenizer.lineno(), tokenizer.sval});
                }
              }
              else if (attrTypes.get(i).getKind() == Datatype.Kind.OBJECT)
              {
                setObject(tuple, i, tokenizer.sval, false);
              } else
                throw new InterfaceException(InterfaceError.INVALID_CHARACTER, t);
              found = true;
              break;
            case StreamTokenizer.TT_NUMBER:
              if (found)
                throw new InterfaceException(InterfaceError.INVALID_CHARACTER, t);
              boolean inRange = true;
              if (attrTypes.get(i) == Datatype.INT)
              {
                inRange = (tokenizer.nval >= Integer.MIN_VALUE) && 
                          (tokenizer.nval <= Integer.MAX_VALUE);
                if (inRange)
                  tuple.iValueSet(i, (int) tokenizer.nval);
                else 
                {
                  inRange = true;
                  throw new InterfaceException(InterfaceError.INVALID_NUMBER,
                    new Object[]{filename, tokenizer.lineno(),tokenizer.nval});
                }
              }
              else if (attrTypes.get(i) == Datatype.BIGINT)
              {
                inRange = (tokenizer.nval >= Long.MIN_VALUE) && 
                          (tokenizer.nval <= Long.MAX_VALUE);
                
                if (inRange)
                  tuple.lValueSet(i, (long) tokenizer.nval);
                else 
                {
                  inRange = true;
                  throw new InterfaceException(InterfaceError.INVALID_NUMBER,
                    new Object[]{filename, tokenizer.lineno(),tokenizer.nval});
                }
              }
              else if (attrTypes.get(i) == Datatype.FLOAT)
              {
                tuple.fValueSet(i, (float) tokenizer.nval);
              }
              else if (attrTypes.get(i) == Datatype.DOUBLE)
              {
                tuple.dValueSet(i, (double) tokenizer.nval);
              }
              else if (attrTypes.get(i) == Datatype.BIGDECIMAL)
              {
                tuple.nValueSet(i, new BigDecimal(
                               String.valueOf((double)tokenizer.nval)),
                               attrMetadata[i].getPrecision(),
                               attrMetadata[i].getScale());
              }
              else if (attrTypes.get(i) == Datatype.BYTE)
              {
                // Should never come here. Byte array values are
                // represented as quoted hex strings
                // String s = String.valueOf(tokenizer.nval);
                // tuple.bValueSet(i, s.getBytes());
                // tuple.bLengthSet(i, s.length());
              }
              else if (attrTypes.get(i) == Datatype.CHAR)
              {
                String s = String.valueOf(tokenizer.nval);
                char[] v = s.toCharArray();
                tuple.cValueSet(i, v);
              }
              else if (attrTypes.get(i) == Datatype.BOOLEAN)
              {
                tuple.boolValueSet(i, (tokenizer.nval != 0));
              }
              else if (attrTypes.get(i).getKind() == Datatype.Kind.OBJECT)
              {
                setObject(tuple, i, tokenizer.nval, true);
              }
              else
                throw new InterfaceException(InterfaceError.INVALID_CHARACTER, t);
              found = true;
              break;
            case StreamTokenizer.TT_EOL:
              if (!found && (i == numAttrs - 1) && (!heartBeat))
              {
                a = tuple.getAttribute(i);
                a.setBNull(true);
                i++; // make i reflect the no.of attrs set.
                break;
              }
              if ((i < numAttrs) && (!heartBeat))
                throw new InterfaceException(InterfaceError.UNEXPECTED_EOL,
                  new Object[]{filename, tokenizer.lineno()});
              break;
            default:
              throw new InterfaceException(InterfaceError.INVALID_CHARACTER, t);
          }

          // We are done
          if (t == StreamTokenizer.TT_EOL)
          {
            // set the bNull flag for the last attribute
            if (found)
            {
              a = tuple.getAttribute(i - 1);
              found = false;
              a.setBNull(false);
            }
            break;
          }

          // Else get ready for the next attr
          i++;
        }
      }
      catch (InterfaceException e)
      {
        LogUtil.logStackTrace(LoggerType.TRACE, Level.FINE, e);
        throw e;
      }
      catch (Exception e)
      {
        LogUtil.logStackTrace(LoggerType.TRACE, Level.FINE, e);
        throw new InterfaceException(InterfaceError.FILE_OPERATION_FAILURE, e,
                                     new Object[]{filename});
      }

      // Did we reach EOF?
      if (t == StreamTokenizer.TT_EOF)
      {
        
        bEof = true;
        if ((i != 0) && (i < numAttrs))
          throw new InterfaceException(InterfaceError.UNEXPECTED_EOF,
            new Object[]{filename, tokenizer.lineno()});
      }

      return ((i == 0) && !heartBeat) ? false : true;
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see oracle.cep.interfaces.input.TableSource#start()
   */
  public void start() throws CEPException
  {
   
    try
    {
      // Clear EOF
      bEof = false;

      // Setup the file for reading
      reader = new FileInputStream(filename);
      bufReader = new BufferedReader(new InputStreamReader(reader, "UTF8"));
      tokenizer = new StreamTokenizer(bufReader);
      tokenizer.parseNumbers();
      tokenizer.eolIsSignificant(true);
      tokenizer.slashStarComments(true);
      
      //Do the input file tuples have a timestamp?
      if (isSystemTimeStamped || isDerivedTimeStamped)
        noTimeStamp = true;
      
      // Parse schema line
      parseSchema();

      // Allocate tuple based on the schema
      allocateTuple();
    }
    catch (Exception e)
    {
      LogUtil.logStackTrace(LoggerType.TRACE, Level.FINE, e);
      throw new CEPException(InterfaceError.FILE_OPERATION_FAILURE, e,
                             new Object[]{filename});
    }
  }

  
  /*
   * (non-Javadoc)
   * 
   * @see oracle.cep.interfaces.input.TableSource#getNext()
   */
  
  public TupleValue getNext() throws CEPException
  {
    long tsys;
    long tcurr;
    long ttarget;
   
    // Have we already finished all the tuples?
    if (bEof)
      return null;

    if (startDelay > 0)
    {
       try
       {
          Thread.sleep(startDelay * 1000);
       }
       catch(InterruptedException e){}
       startDelay = 0;
    }

    try
    {
      if (parseTuple())
      {
        if (isDelay)
        {
          if (isSystemTimeStamped)
          {
            try
            {
              Thread.sleep(delayScale);
            }
            catch(InterruptedException e){}
            insertTimestamp(tuple);
            return tuple;
          }
          tsys = System.currentTimeMillis() - tsysbase;
          tcurr = tuple.getTime();
          ttarget = tcurr / delayScale;

          while (tsys < ttarget)
          {
            try
            {
              if (ttarget - tsys > ONE_MINUTE / delayScale)
                Thread.sleep(ONE_MINUTE / delayScale);
              else
                Thread.sleep(ttarget - tsys);
            }
            catch (InterruptedException ie)
            {
            }
            tsys = System.currentTimeMillis() - tsysbase;

            if (tsys < ttarget)
            {
              // insert a hearbeat..how?
              // hbeat.setTime(new Timestamp(tsys*scale+tbase));
              // jd.putNext(hbeat, ElementKind.E_HEARTBEAT);
            }

          }

          //Assumption: Input Tuple time-stamp in file source is of millisecond
          //time unit and our system's granularity is nanosecond unit of time
          if(convertTs)
            tuple.setTime((tcurr + tbase)*1000000);
          else
            tuple.setTime(tcurr+tbase);

        }
        if (isSystemTimeStamped)
          insertTimestamp(tuple);
        else if(!isTotalOrderingFlag)
        {
          // set the total ordering flag based on the timestamp value
          // of the next tuple
          Long nextTs = getNextTupleTimestamp();
          
          if(nextTs != Constants.NULL_TIMESTAMP && tuple.getTime() < nextTs)
            tuple.setTotalOrderGuarantee(true);
          else
            tuple.setTotalOrderGuarantee(false);
        }
        else if(isTotalOrderingFlag)
        {
          tuple.setTotalOrderGuarantee(totalOrderingFlagValue);
        }
        
        return tuple;
      }
    }
    catch (InterfaceException e)
    {
      throw e;
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
    if(hasNext())
      return getOldestTs();
    }
    catch(CEPException e)
    {
      
    }
    return Constants.NULL_TIMESTAMP;
  }

  /*
   * (non-Javadoc)
   * 
   * @see oracle.cep.interfaces.input.TableSource#end()
   */
  public void end() throws CEPException
  {
    try
    {
      tokenizer = null;
      bufReader.close();
      bufReader = null;
      reader.close();
      reader = null;
    }
    catch (Exception e)
    {
      LogUtil.logStackTrace(LoggerType.TRACE, Level.FINE, e);
      throw new CEPException(InterfaceError.FILE_OPERATION_FAILURE, e,
                             new Object[]{filename});
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see oracle.cep.interfaces.input.TableSource#getNumAttrs()
   */
  public int getNumAttrs()
  {
    return numAttrs;
  }

  /*
   * (non-Javadoc)
   * 
   * @see oracle.cep.interfaces.input.TableSource#getAttrType(int)
   */
  /*public Datatype getAttrType(int pos)
  {
    return attrTypes.get(pos);
  }*/

  /*
   * (non-Javadoc)
   * 
   * @see oracle.cep.interfaces.input.TableSource#getAttrLength(int)
   */
  /*public int getAttrLength(int pos)
  {
    Integer i;

    i = attrLen.get(pos);
    return (i == null) ? 0 : i.intValue();
  }*/

  public long getOldestTs() throws CEPException
  {
    return oldTs;
  }

  public boolean hasNext() throws CEPException
  {
       
    boolean hrtBeat = false;

    synchronized (this)
    {     
      if (!noTimeStamp)
      { //do this only for application ts and non-silent sources
        if (bEof)
        {
          oldTs = Constants.NULL_TIMESTAMP;
          return false;
        }
        
        if (!isHeartBeat)
        {
          try
          {
            oldTs = readNumber();
            tokenizer.pushBack();
          }
          catch (CEPException e)
          {
            // Does it represent a heartbeat:
            if (e.getErrorCode() == InterfaceError.NUMBER_NOT_FOUND)
            {
              tokenizer.pushBack();
              hrtBeat = true;
            }
            else
              throw new InterfaceException(InterfaceError.INVALID_TIMESTAMP,
                new Object[]{filename, tokenizer.lineno()});
          }
          catch (Exception ex)
          {
            LogUtil.logStackTrace(LoggerType.TRACE, Level.FINE, ex);
            throw new InterfaceException(InterfaceError.INVALID_TIMESTAMP, ex,
              new Object[]{filename, tokenizer.lineno()});
          }
  
          if (hrtBeat)
          {
            // Get the next token
            int t;
            try
            {
              t = tokenizer.nextToken();
            }
            catch (IOException e)
            {
              throw new InterfaceException(InterfaceError.INVALID_TIMESTAMP, e,
                new Object[]{filename, tokenizer.lineno()});
            }
  
            // It better be a heartbeat row
            if ((t != StreamTokenizer.TT_WORD)
                || (!tokenizer.sval.equalsIgnoreCase("h")))
              throw new InterfaceException(InterfaceError.INVALID_TIMESTAMP,
                new Object[]{filename, tokenizer.lineno()});
            else
              isHeartBeat = true; 
          }
        }
  
        if (isHeartBeat)
        {
          try
          {
            oldTs = readNumber();            
            tokenizer.pushBack();
          }
          catch (Exception e)
          {
            throw new InterfaceException(InterfaceError.INVALID_TIMESTAMP, e,
              new Object[]{filename, tokenizer.lineno()});
          }
        }
        //Assumption: Input Tuple time-stamp in file source is of millisecond
        //time unit and our system's granularity is nanosecond unit of time
        if(convertTs)
          oldTs = oldTs * 1000000;
      } 
      else //systs or silent source(set oldTs = Long.MAX_VALUE)
        oldTs = Constants.NULL_TIMESTAMP;
      
      //check below is used by sys ts n app ts both.
      //this is because call to readNumber may change the value of bEOf in non-silent app ts case
      if (bEof)
        return false;
      else
        return true;
    } 
    
  }

  
  private void setObject(TupleValue tuple, int pos, Object o, boolean isNumber)
    throws Exception
  {
    AttributeValue v = tuple.getAttribute(pos);
    Datatype type = v.getAttributeType();
    Object inpAttrValue = null;
    Object finalAttrValue = null;
    if (type instanceof IComplexType) 
    {
      IComplexType complexType = (IComplexType) type;
      
      ICartridgeContext context = 
        CartridgeHelper.createCartridgeContext(this.execContext);
        
      if(isNumber)
        inpAttrValue = getCorrectNumericTypeObj(o, complexType, context);
      else
        inpAttrValue = o;
      
      IType argType = 
        this.execContext.getServiceManager().
          getConfigMgr().getCartridgeLocator().getJavaTypeSystem().getType(
            inpAttrValue.getClass().getName(), context);
      
      IConstructorMetadata constructor =
        complexType.getConstructor(argType);
      
      finalAttrValue = 
        constructor.getConstructorImplementation().instantiate(
          new Object[]{inpAttrValue}, context);
    }
    
    v.oValueSet(finalAttrValue);
  }

  private Object getCorrectNumericTypeObj(Object o, IComplexType type, 
                                          ICartridgeContext context)
    throws Exception
  {
    // Tokenizer always parse the number value into a double
    Double parsedValue = (Double)o;
    
    ITypeLocator javaTypeSystem = this.execContext.getServiceManager().
            getConfigMgr().getCartridgeLocator().getJavaTypeSystem();
    
    if(type.isAssignableFrom(javaTypeSystem.getType(Double.class.getName(), context)))
    {
      return parsedValue;
    }
    else if(type.isAssignableFrom(javaTypeSystem.getType(Float.class.getName(), context)))
    {
      return parsedValue.floatValue();
    }
    else if(type.isAssignableFrom(javaTypeSystem.getType(Long.class.getName(), context)))
    {
      return parsedValue.longValue();
    }
    else if(type.isAssignableFrom(javaTypeSystem.getType(Integer.class.getName(), context)))
    {
      return parsedValue.intValue();
    }
    
    // Return Double for remaining numeric types 
    return parsedValue;
  }
  
  public String toString()
  {
    return toString("FileSource(" + filename + ")");
  }
}
