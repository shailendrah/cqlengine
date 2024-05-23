/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/interfaces/output/FileDestination.java /main/35 2013/03/04 00:54:59 sbishnoi Exp $ */

/* Copyright (c) 2006, 2013, Oracle and/or its affiliates. 
All rights reserved. */

/*
 DESCRIPTION
 Declares FileDestination in package oracle.cep.interfaces.output.
 
 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>
 
 NOTES
 <other useful comments, qualifications, etc.>
 
 MODIFIED    (MM/DD/YY)
 sbishnoi  02/27/13 - bigdecimal output should print without E notation
 sbishnoi  11/07/11 - formatting timestamp value
 sbishnoi  08/29/11 - support for interval year to month based operations
 anasrini  06/30/11 - XbranchMerge anasrini_bug-12675151_ps5 from
                      st_pcbpel_11.1.1.4.0
 anasrini  06/18/11 - support for partition parallel regression tests
 sbishnoi  08/31/09 - modifying file output to include the output batch number
 sborah    07/03/09 - support for bigdecimal
 hopark    03/05/09 - add opaque type
 hopark    05/07/09 - support utf-8
 sbishnoi  04/17/09 - handle timestamp of long.MIN_VAL
 hopark    02/05/09 - objtype support
 hopark    12/04/08 - add toString
 hopark    11/28/08 - use CEPDateFormat
 hopark    11/15/08 - log end
 hopark    10/10/08 - remove statics
 sbishnoi  07/29/08 - support of nanosecond timestamp
 parujain  05/13/08 - xml publishing fns
 skmishra  05/19/08 - 
 udeshmuk  03/13/08 - parameterize errors.
 udeshmuk  01/30/08 - support for double data type.
 udeshmuk  01/17/08 - change in the data type of time field in TupleValue.
 najain    10/24/07 - xmltype
 mthatte   12/05/07 - changing output date format
 sbishnoi  11/27/07 - support for update semantics
 hopark    10/30/07 - remove IQueueElement
 mthatte   09/10/07 - Opening file in over-write mode instead of append
 dlenkov   08/03/07 - fixed the use of finally
 hopark    05/16/07 - remove printStackTrace
 dlenkov   03/15/07 - 
 najain    03/12/07 - bug fix
 rkomurav  12/15/06 - field separator for nulls
 anasrini  10/25/06 - implement putNext
 anasrini  08/18/06 - refactoring
 parujain  08/04/06 - Datatype Timestamp
 najain    07/28/06 - hanlde nulls 
 skaluska  04/04/06 - add kind to putNext 
 skaluska  03/28/06 - implementation
 skaluska  03/27/06 - Creation
 skaluska  03/27/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/interfaces/output/FileDestination.java /main/35 2013/03/04 00:54:59 sbishnoi Exp $
 *  @author  skaluska
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.interfaces.output;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.logging.Level;

import oracle.cep.common.CEPDateFormat;
import oracle.cep.common.Datatype;
import oracle.cep.dataStructures.external.AttributeValue;
import oracle.cep.dataStructures.external.TupleValue;
import oracle.cep.dataStructures.internal.QueueElement;
import oracle.cep.exceptions.CEPException;
import oracle.cep.exceptions.InterfaceError;
import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;
import oracle.cep.service.ExecContext;

/**
 * FileDestination
 *
 * @author skaluska
 */
public class FileDestination extends QueryOutputBase
{
  private static final char   FIELD_SEPARATOR = ',';

  private static final char   TS_SEPARATOR    = ':';

  private static final char   KIND_SEPARATOR  = ' ';

  private static final char   PLUS            = '+';

  private static final char   MINUS           = '-';

  private static final char   HEARTBEAT       = '!';
  
  private static final char   UPDATE          = 'U';
  
  private static final char   BATCH_SEPARATOR = ';';

  /** filename */
  private String              filename;

  /** buffered file writer */
  private Writer              writer;

  private long                nTuples;
  
  /**
   * Constructor for FileDestination
   * @param ec TODO
   * @param filename
   */
  public FileDestination(ExecContext ec, String filename)
  {
    super(ec);
    this.filename = filename;
  }

  /*
   * (non-Javadoc)
   * 
   * @see oracle.cep.interfaces.output.QueryOutput#start()
   */
  public void start() throws CEPException
  {
    //System.out.println(filename + " : start");
    assert numAttrs != 0;

    nTuples = 0;
    
    // set batch number to 1
    currentBatchNumber = 1;
    
    // Make it idempotent for now
    if (writer != null)
      return;

    try
    {
      // Setup the file for writing
      writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filename), "UTF8"));
    }
    catch (Exception e)
    {
      e.printStackTrace();
      LogUtil.logStackTrace(LoggerType.TRACE, Level.FINE, e);
      throw new CEPException(InterfaceError.FILE_OPERATION_FAILURE, e,
                             new Object[]{filename});
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see oracle.cep.interfaces.output.QueryOutput#end()
   */
  public void end() throws CEPException
  {
    LogUtil.info(LoggerType.TRACE, filename + " : end , " + nTuples + " tuples written." );
    try
    {
      writer.flush();
      writer.close();
      writer = null;
    }
    catch (Exception e)
    {
      LogUtil.logStackTrace(LoggerType.TRACE, Level.FINE, e);
      throw new CEPException(InterfaceError.FILE_OPERATION_FAILURE, e,
                             new Object[]{filename});
    }
  }
  
  public void putNext(TupleValue tuple, QueueElement.Kind k) throws CEPException 
  {
    //System.out.println(filename + " : " + tuple.toSimpleString());
    String str = tupleToString(tuple, k, this.convertTs);
    nTuples++;
    try{      
      // Write the Batch number (if output batching is enabled)
      if(isBatchOutput)
      {
        writer.write(currentBatchNumber + "" + BATCH_SEPARATOR+ " ");
      }      
      
      writer.write(str);
      writer.write("\n");
      
     // Flush
     writer.flush();
    }
    catch (IOException ioe) {
      throw new CEPException(InterfaceError.FILE_OPERATION_FAILURE, ioe,
                             new Object[]{filename});
    }
  }

  public static String tupleToString(TupleValue tuple, QueueElement.Kind k, boolean convertTs) throws CEPException 
  {
      StringBuilder b = new StringBuilder();
      String val = null;
      AttributeValue att = null;
      
      // Write the timestamp
      // Assumption: We are assuming that tuple's time-stamp in output file will
      // be of nanosecond unit of time-stamp;
      // This is the reason to divide tuple.getTime() by 1000,000
      if(convertTs && tuple.getTime() != Long.MIN_VALUE)
        b.append(String.valueOf(tuple.getTime()/1000000));
      else
        b.append(String.valueOf(tuple.getTime()));
      b.append(TS_SEPARATOR);
      
      // Write the element type
      switch(k)
      {
       case E_PLUS:
         b.append(PLUS);
         break;
       case E_MINUS:
         b.append(MINUS);
         break;
       case E_UPDATE:
         b.append(UPDATE);
         break;
       case E_HEARTBEAT:
         b.append(HEARTBEAT);
         return b.toString();
       default:
         assert false;
      }
      
      b.append(KIND_SEPARATOR);
      
      for (int i=0; i<tuple.getNoAttributes(); i++) 
      {
        val = null;

        //Add a field separator
        if (i != 0)
          b.append(FIELD_SEPARATOR);

        att = tuple.getAttribute(i);
        if(att.isBNull())
          continue;

        switch (att.getAttributeType().getKind()) 
        {
          case INT:
            val = String.valueOf(tuple.iValueGet(i));
            break;
          case BIGINT:
            val = String.valueOf(tuple.lValueGet(i));
            break;
          case FLOAT:
            val = String.valueOf(tuple.fValueGet(i));
            break;
          case DOUBLE:
            val = String.valueOf(tuple.dValueGet(i));
            break;
          case BIGDECIMAL:
            val = tuple.nValueGet(i).toPlainString();
            break;
          case CHAR:
            b.append(new String(tuple.cValueGet(i), 0, tuple.cLengthGet(i)));
            break;
          case XMLTYPE:
            b.append(new String(tuple.xValueGet(i), 0, tuple.xLengthGet(i)));
            break;
          case BOOLEAN:
            val = String.valueOf(tuple.boolValueGet(i));
            break;
          case BYTE:
            char[] inp = Datatype.byteToHex( tuple.bValueGet(i),
                tuple.bLengthGet(i));
            b.append( '\"');
            b.append( inp);
            b.append( '\"');
            break;
            
          case TIMESTAMP:
            CEPDateFormat sdf = CEPDateFormat.getInstance();
            val = sdf.format(tuple.tValueGet(i), tuple.tFormatGet(i));
            break;
          case INTERVAL:
            val = String.valueOf(tuple.vValueGet(i));
            break;
          case INTERVALYM:
            val = String.valueOf(tuple.vymValueGet(i));
            break;
          case OBJECT:
            val = (tuple.oValueGet(i) == null) ? "null":tuple.oValueGet(i).toString();
            break;
          default:
            assert false : att.getAttributeType();
        }

        if (val != null)
        {
          b.append(val);
        }
      }
     return b.toString();
  }

  public String toString()
  {
    return toString("FileDestination(" + filename + ")");
  }
}
