/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/dataStructures/internal/memory/DynTupleBase.java /main/29 2012/01/20 11:47:14 sbishnoi Exp $ */

/* Copyright (c) 2007, 2012, Oracle and/or its affiliates. 
All rights reserved. */

/*
 DESCRIPTION
 This is the base class of auto generated Tuple from TupleSpec.

 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>
 NOTES
 <other useful comments, qualifications, etc.>

 MODIFIED    (MM/DD/YY)
 sbishnoi  01/13/12 - improved timestamp support to include timezone
 sbishnoi  11/07/11 - formatting timestamp value
 sbishnoi  10/03/11 - changing format to intervalformat
 udeshmuk  11/16/10 - support for to_bigint(timestamp)
 sborah    04/28/10 - char to number conversions
 hopark    12/03/09 - check maxlen of byte/char types
 sborah    07/02/09 - support for bigdecimal
 sborah    06/01/09 - support for xmltype in to_char
 hopark    03/16/09 - add obj heval
 hopark    02/17/09 - support boolean as external datatype
 hopark    02/17/09 - add OBJ_CPY - objtype support
 sborah    02/13/09 - support for is_not_null
 hopark    11/28/08 - use CEPDateFormat
 skmishra  08/22/08 - exception frm hexToByte()
 sbishnoi  07/14/08 - fix systimestamp
 parujain  07/08/08 - value based windows
 sbishnoi  06/24/08 - modifying length() to return if attr arg is empty
 sbishnoi  06/20/08 - support of to_char for other datatypes
 sbishnoi  06/19/08 - support for to_char(integer)
 parujain  06/03/08 - support xmltype for heval
 sbishnoi  04/27/08 - support of modulus and arithmetic divide by zero error
                      fix
 hopark    02/18/08 - add lower
 hopark    02/05/08 - parameterized error
 hopark    02/01/08 - fix boolean eq
 udeshmuk  01/31/08 - support for double data type
 hopark    11/27/07 - add BOOLEAN
 hopark    11/16/07 - xquery support
 hopark    11/04/07 - fix initial char/byte value
 udeshmuk  10/16/07 - support for max/min on char and byte.
 hopark    09/06/07 - add evals
 hopark    07/19/07 - creation
*/

package oracle.cep.dataStructures.internal.memory;

import java.text.ParseException;
import java.time.DateTimeException;
import java.time.ZoneId;
import java.time.zone.ZoneRulesException;
import java.util.Calendar;
import java.util.LinkedHashSet;
import java.util.TimeZone;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import oracle.cep.common.CEPDateFormat;
import oracle.cep.common.Constants;
import oracle.cep.common.Datatype;
import oracle.cep.common.IntervalFormat;
import oracle.cep.common.TimeUnit;
import oracle.cep.common.TimestampFormat;
import oracle.cep.dataStructures.internal.ITuple;
import oracle.cep.exceptions.CEPException;
import oracle.cep.exceptions.ExecutionError;
import oracle.cep.execution.ExecException;
import oracle.cep.execution.SoftExecException;
import oracle.cep.execution.internals.TupleSpec;
import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;


/**
 * @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/dataStructures/internal/memory/DynTupleBase.java /main/27 2010/11/22 07:07:06 udeshmuk Exp $
 * @author najain
 * @since release specific (what release of product did this appear in)
 */
public abstract class DynTupleBase extends TupleBase
{
  protected TupleSpec   spec;
  
  /**
   * Constructor for TupleBase
   */
  public DynTupleBase()
  {
    super();
  }

  public void init(TupleSpec spec, boolean nullValue) throws ExecException
  {
    this.spec = spec;
    int max = spec.getNumAttrs();
    for (int i = 0; i < max; i++)
    {
      switch (spec.getAttrType(i).getKind())
      {
        case INT:
          iValueSet(i, 0);
          break;
        case BIGINT:
          lValueSet(i, 0);
          break;
        case FLOAT:
          fValueSet(i, 0);
          break;
        case DOUBLE:
          dValueSet(i, 0);
          break;
        case BIGDECIMAL:
          nValueSet(i, BigDecimal.ZERO, 1, 0);
          break;
        case CHAR:
          cValueSet(i, null, 0);
          break;
        case BYTE:
          bValueSet(i, null, 0);
          break;
        case OBJECT:
          oValueSet(i, null);
          break;
        case TIMESTAMP:
          tValueSet(i, 0);
          tFormatSet(i, spec.getAttrMetadata(i).getTimestampFormat());
          break;
        case INTERVAL:
          vValueSet(i, 0, spec.getAttrMetadata(i).getIntervalFormat());
          break;
        case INTERVALYM:
          vymValueSet(i, 0, spec.getAttrMetadata(i).getIntervalFormat());
          break;
        case XMLTYPE:
          xValueSet(i, null, 0);
          break;
        case BOOLEAN:
          boolValueSet(i, false);
          break;
        default:
          assert false;
      }
      if (nullValue)
        setAttrNull(i);
    }
  }

  public int getNumAttrs()
  {
    return spec.getNumAttrs();
  }

  public Datatype getAttrType(int pos)
  {
    return spec.getAttrType(pos);    
  }
  
  protected void throwInvalidAttr() throws ExecException
  {
    throw new ExecException(ExecutionError.INVALID_ATTR);
  }
  
  protected void checkMaxLen(int pos, int l)throws ExecException
  { 
   int maxLen = spec.getAttrLen(pos);
   if (l > maxLen)
	   throw new ExecException(ExecutionError.INVALID_ATTR, l, maxLen);
  }
    
  /**
   * Gets the float value of an attribute (either int or float)
   * 
   * @param pos
   *          Position of interest
   * @return Float Attibute value
   * @throws ExecException
   */
  public float floatValueGet(int pos) throws ExecException
  {
    assert isAttrNull(pos) == false;
    switch (spec.getAttrType(pos).getKind())
    {
    case INT:
      return (float) iValueGet(pos);
    case BIGINT:
      return (float) lValueGet(pos);
    case FLOAT:
      break;
     default:
       throw new ExecException(ExecutionError.TYPE_MISMATCH,
           spec.getAttrType(pos).toString(), 
           Datatype.FLOAT.toString() +"," + Datatype.INT.toString() + "," + Datatype.BIGINT.toString());
    }
    return fValueGet(pos);
  }

  /**
   * Gets the double value of an attribute (either int, bigint or double)
   * 
   * @param pos
   *          Position of interest
   * @return Double Attribute value
   * @throws ExecException
   */
  public double doubleValueGet(int pos) throws ExecException
  {
    assert isAttrNull(pos) == false;
    switch (spec.getAttrType(pos).getKind())
    {
      case INT:
        return (double) iValueGet(pos);
      case BIGINT:
        return (double) lValueGet(pos);
      case FLOAT:
        return (double) fValueGet(pos);
      case DOUBLE:
        break;
      default:
        throw new ExecException(ExecutionError.TYPE_MISMATCH);
    }
    return dValueGet(pos);
  }
  
  /**
   * Gets the BigDecimal value of the attribute (int, bigint, timestamp, 
   * interval, float and double)
   * @return BigDecimal value
   * @throws ExecException
   */
  public BigDecimal bigDecimalValueGet(int position) throws ExecException
  {
    assert isAttrNull(position) == false;
    Datatype type = spec.getAttrType(position);
    if (!((type == Datatype.INT) || (type == Datatype.BIGINT)
       || (type == Datatype.TIMESTAMP) || (type == Datatype.INTERVAL)
       || (type == Datatype.INTERVALYM)
       || (type == Datatype.FLOAT) || (type == Datatype.DOUBLE)
       ))
      throw new ExecException(ExecutionError.TYPE_MISMATCH);
    switch(type.getKind())
    {
      case INT:
        return new BigDecimal(String.valueOf(iValueGet(position)));
      case BIGINT:
        return new BigDecimal(String.valueOf(lValueGet(position)));
      case TIMESTAMP:
        return new BigDecimal(String.valueOf(tValueGet(position)));
      case INTERVAL: 
        return new BigDecimal(String.valueOf(vValueGet(position)));
      case INTERVALYM: 
        return new BigDecimal(String.valueOf(vymValueGet(position)));
      case FLOAT:
        return new BigDecimal(String.valueOf(fValueGet(position)));
      default :
        assert type == Datatype.DOUBLE;
        return new BigDecimal(String.valueOf(dValueGet(position)));
    }
  }
  
  /**
   * Gets the double value of an attribute (either float or double)
   * 
   * @param pos
   *          Position of interest
   * @return Double value
   * @throws ExecException
   */
  public double dblValueGet(int pos) throws ExecException
  {
    assert isAttrNull(pos) == false;
    switch (spec.getAttrType(pos).getKind())
    {
      case FLOAT:
        return (double) fValueGet(pos);
      case DOUBLE:
        break;
      default:
        throw new ExecException(ExecutionError.TYPE_MISMATCH);
    }
    return dValueGet(pos);
  }
  
  /**
   * Gets the long value of an attribute (int,bigint, timestamp or interval)
   * 
   * @param pos
   *          Position of interest
   * @return Long value
   * @throws ExecException
   */
  public long longValueGet(int pos) throws ExecException
  {
    assert isAttrNull(pos) == false;
    switch (spec.getAttrType(pos).getKind())
    {
      case INT:
        return (long) iValueGet(pos);
      case BIGINT:
        break;
      case TIMESTAMP:
        return tValueGet(pos);
      case INTERVAL:
        return vValueGet(pos);
      case INTERVALYM:
        return vymValueGet(pos);
      default:
        throw new ExecException(ExecutionError.TYPE_MISMATCH);
    }
    return lValueGet(pos);
  }

  protected char[] copyVChar(char[] cv, char[] v, int l)
  {
    if (cv == null || cv.length < l)
      cv = new char[l];
    for (int i = 0; i < l; i++)
      cv[i] = v[i];
    return cv;
  }
  
  protected byte[] copyVByte(byte[] bv, byte[] v, int l)
  {
    if (bv == null || bv.length < l)
      bv = new byte[l];
    for (int i = 0; i < l; i++)
      bv[i] = v[i];
    return bv;
  }

  public void tValueSet(int pos, Timestamp ts) throws ExecException
  {
    tValueSet(pos, ts.getTime() * 1000000l);
  }
  
  public void tFormatSet(int pos, TimestampFormat format) throws ExecException
  {
    tFormatSet(pos, format);
  }


  /**
   * Compare 2 char arrays lexicographically (dictionary order)
   * 
   * @param c1
   *          First array to be compared
   * @param l1
   *          Length of first argument
   * @param c2
   *          Second array to be compared
   * @param l2
   *          Length of second argument
   * @return -ve number if first array is less +ve number if first array is more
   *         else 0
   */
  public static int cCompare(char[] c1, int l1, char[] c2, int l2)
  {
    int i;
    int limit = (l1 < l2) ? l1 : l2;
    int ret = (l1 - l2);

    // scan until elements are the same
    for (i = 0; (i < limit) && (c1[i] == c2[i]); i++)
      ;
    if (i == limit)
      return ret;
    else
      return (c1[i] - c2[i]);
  }

  /**
   * Compare 2 byte arrays lexicographically (dictionary order)
   * 
   * @param b1
   *          First array to be compared
   * @param l1
   *          Length of first argument
   * @param b2
   *          Second array to be compared
   * @param l2
   *          Length of second argument
   * @return -ve number if first array is less +ve number if first array is more
   *         else 0
   */
  public static int bCompare(byte[] b1, int l1, byte[] b2, int l2)
  {
    int i;
    int limit = (l1 < l2) ? l1 : l2;
    char c1, c2;

    // scan until elements are the same
    for (i = 0; (i < limit) && (b1[i] == b2[i]); i++)
      ;
    if (i == limit)
      return (l1 - l2);
    else {
      c1 = Datatype.hexchars[(b1[i]&0xf0)>>>4];
      c2 = Datatype.hexchars[(b2[i]&0xf0)>>>4];
      if (c1 != c2)
       return (c1 - c2);
      else {
      c1 = Datatype.hexchars[b1[i]&0x0f];
      c2 = Datatype.hexchars[b2[i]&0x0f];
      return (c1 - c2);
      }
    }
  }

  public boolean beval(Datatype type, Op op, int col, int bit, 
                      ITuple other, int col2, int bit2, 
                      Pattern pattern, boolean n2ntrue) 
    throws ExecException
  {
    if (op == Op.IS_NULL)
      return isAttrNull(col);
    
    if(op == Op.IS_NOT_NULL)
      return !isAttrNull(col);

    switch (op.nullType)
    {
     case ANY_N2N:
      if (n2ntrue && isAttrNull(col) && other.isAttrNull(col2)) 
        return true;
      else if(isAttrNull(col) || other.isAttrNull(col2))
        return false;        
      break;
     case ANY:
      if (isAttrNull(col) || other.isAttrNull(col2))
        return false;
      break;
    }

    assert (!isAttrNull(col));
    if (op.args == 2) 
    {
      assert (other != null);
      assert (!other.isAttrNull(col2));
    }
    switch(type.getKind())
    {
      case INT:
      {
        int val1, val2;
        val1 = iValueGet(col);
        val2 = other.iValueGet(col2);
        switch(op)
        {
          case LT: return (val1 < val2);
          case LE: return (val1 <= val2);
          case GT: return (val1 > val2);
          case GE: return (val1 >= val2);
          case EQ: return (val1 == val2);
          case NE: return (val1 != val2);
          default: assert false;
        }
      }
      break;
      
      case BIGINT:
      {
        long val1, val2;
        val1 = lValueGet(col);
        val2 = other.lValueGet(col2);
        switch(op)
        {
          case LT: return (val1 < val2);
          case LE: return (val1 <= val2);
          case GT: return (val1 > val2);
          case GE: return (val1 >= val2);
          case EQ: return (val1 == val2);
          case NE: return (val1 != val2);
          default: assert false;
        }
      }
      break;
      
      case FLOAT:
      {
        float val1, val2;
        val1 = fValueGet(col);
        val2 = other.fValueGet(col2);
        switch(op)
        {
          case LT: return (val1 < val2);
          case LE: return (val1 <= val2);
          case GT: return (val1 > val2);
          case GE: return (val1 >= val2);
          case EQ: return (val1 == val2);
          case NE: return (val1 != val2);
          default: assert false;
        }
      }
      break;
      
      case DOUBLE:
      {
        double val1, val2;
        val1 = dValueGet(col);
        val2 = other.dValueGet(col2);
        switch(op)
        {
          case LT: return (val1 < val2);
          case LE: return (val1 <= val2);
          case GT: return (val1 > val2);
          case GE: return (val1 >= val2);
          case EQ: return (val1 == val2);
          case NE: return (val1 != val2);
          default: assert false;
        }
      }
      break;
      
      case BIGDECIMAL:
      {
        BigDecimal val1, val2;
        val1 = nValueGet(col);
        val2 = other.nValueGet(col);
        switch(op)
        {
          case LT:
            return (val1.compareTo(val2) < 0);
          case LE:
            return (val1.compareTo(val2) <= 0);
          case GT:
            return (val1.compareTo(val2) > 0);
          case GE:
            return (val1.compareTo(val2) >= 0);
          case EQ:
            return (val1.compareTo(val2) == 0);
          case NE:
            return (val1.compareTo(val2) != 0);
          default:
            assert false;
        }
      }
        break;
      case TIMESTAMP:
      {
        long val1, val2;
        val1 = tValueGet(col);
        val2 = other.tValueGet(col2);
        //TODO: Ensure that timezone doesn't impact the comparison
        switch(op)
        {
          case LT: return (val1 < val2);
          case LE: return (val1 <= val2);
          case GT: return (val1 > val2);
          case GE: return (val1 >= val2);
          case EQ: return (val1 == val2);
          case NE: return (val1 != val2);
          default: assert false;
        }
      }
      break;
      
      case BYTE:
      {
        int val = bCompare(bValueGet(col), 
                           bLengthGet(col), 
                           other.bValueGet(col2), 
                           other.bLengthGet(col2));
        switch(op)
        {
          case LT: return (val < 0);
          case LE: return (val <= 0);
          case GT: return (val > 0);
          case GE: return (val >= 0);
          case EQ: return (val == 0);
          case NE: return (val != 0);
          default: assert false;
        }
      }
      break;
      
      case CHAR:
      {
        int val = 0;
        if (op != Op.LIKE)
        {
            val = cCompare(cValueGet(col), 
                           cLengthGet(col), 
                           other.cValueGet(col2), 
                           other.cLengthGet(col2));
        }
        switch(op)
        {
          case LT: return (val < 0);
          case LE: return (val <= 0);
          case GT: return (val > 0);
          case GE: return (val >= 0);
          case EQ: return (val == 0);
          case NE: return (val != 0);
          case LIKE:  //CHR_LIKE
          {
            int len = cLengthGet(col);
            if(len == 0)
              return false;
         
            assert pattern != null;
            Scratch buf = scratchBuf.get();
            StringBuilder charSeq = buf.charSeq;
            charSeq.setLength(len);
         
            char[] c1 = cValueGet(col);
            for(int i=0; i<len; i++)
            {
              charSeq.setCharAt(i, c1[i]);
            }
        
            Matcher matcher = pattern.matcher(charSeq);
            return (matcher.find(0));
          }  
          default: assert false;
        }
      }
      break;
      case INTERVAL:
      {
        long val1, val2;
        val1 = vValueGet(col);
        val2 = other.vValueGet(col2);
        switch(op)
        {
          case LT: return (val1 < val2);
          case LE: return (val1 <= val2);
          case GT: return (val1 > val2);
          case GE: return (val1 >= val2);
          case EQ: return (val1 == val2);
          case NE: return (val1 != val2);
          default: assert false;
        }
      }
      break;
      case INTERVALYM:
      {
        long val1, val2;
        val1 = vymValueGet(col);
        val2 = other.vymValueGet(col2);
        switch(op)
        {
          case LT: return (val1 < val2);
          case LE: return (val1 <= val2);
          case GT: return (val1 > val2);
          case GE: return (val1 >= val2);
          case EQ: return (val1 == val2);
          case NE: return (val1 != val2);
          default: assert false;
        }
      }
      break;
      case BOOLEAN:
      {
        if (op == Op.EQ)
        {
          boolean val1 = boolValueGet(col);
          boolean val2 = other.boolValueGet(col2);
          return (val1 == val2);
        } else if (op == Op.NE)
        {
          boolean val1 = boolValueGet(col);
          boolean val2 = other.boolValueGet(col2);
          return (val1 != val2);
        }
        byte[] arr1 = bValueGet(col);;
        int bpos = bit/(Constants.BITS_PER_BYTE);
        assert arr1.length >= bpos;
        boolean val1 = ((arr1[bpos] & (1 << (bit % Constants.BITS_PER_BYTE))) != 0);
        boolean val2 = false;
        if (op != Op.NOT)
        {
          byte[] arr2 = other.bValueGet(col2); 
          bpos = bit2/(Constants.BITS_PER_BYTE);
          assert arr2.length >= bpos;
          val2 = ((arr2[bpos] & (1 << (bit2 % Constants.BITS_PER_BYTE))) != 0);
        }
        switch (op)
        {
          case AND: return (val1 && val2);
          case OR: return (val1 || val2);
          case NOT: return (!val1);
          case XOR: return (val1 || val2) && (!(val1 && val2));
          default: assert false;
        }
      }
      break;
      
      case XMLTYPE:
      {
        char[] xval1 =  xValueGet(col);
        char[] xval2 =  other.xValueGet(col2);
       
        boolean same;
        
        switch(op)
        {
          case EQ:
          {
          
            // Due to the way in which a XmltypeAttributeValue and a 
            // XmltypeAttrVal is implemented , 
            // the character array sizes may differ along with the total 
            // contents. We only need to compare the character arrays
            // till their specified length.
            int length1 = xIsObj(col) ? xval1.length : xLengthGet(col);
            int length2 = other.xIsObj(col2) ? xval2.length : other.xLengthGet(col2);
            
            int val = TupleBase.cCompare(xval1, length1, 
                               xval2, length2);
            
            same = val == 0;
            
            return same;
          }
          default:
            assert false;
        }
        
        break;
      }
    }
    return true;
  }

  public int heval(Datatype dtype, int col, int hash) throws ExecException
  {
    if (!isAttrNull(col))
    {
      switch (dtype.getKind())
      {
        case INT:
          hash = ((hash << 5) + hash) + inthash(iValueGet(col));
          break;

        case BIGINT:
          hash = ((hash << 5) + hash) + longhash(lValueGet(col));
          break;

        case BYTE:
          hash = ((hash << 5) + hash) + (bValueGet(col))[0];
          break;

        case CHAR:
          char cptr[] = cValueGet(col);
          int clen = cLengthGet(col);
          for (int j = 0; j < clen; j++)
            hash = ((hash << 5) + hash) + cptr[j];
          break;

        case FLOAT:
          hash = ((hash << 5) + hash) + inthash((int)fValueGet(col));
          break;
          
        case DOUBLE:
          hash = ((hash << 5) + hash) + inthash((int)dValueGet(col));
          break;
          
        case BIGDECIMAL:
          hash = ((hash << 5) + hash)
               + longhash(nValueGet(col).longValue());
          break;

        case TIMESTAMP:
          hash = ((hash << 5) + hash) + inthash((int)tValueGet(col));
          break;
          
        case INTERVAL:
          hash = ((hash << 5) + hash) + longhash(vValueGet(col));
          break;
          
        case INTERVALYM:
          hash = ((hash << 5) + hash) + longhash(vymValueGet(col));
          break;
          
        case BOOLEAN:
          hash = ((hash << 5) + hash) + inthash(boolValueGet(col) ? 1 : 0);
          break;
          
        case XMLTYPE:
          char xptr[] = xValueGet(col);
          boolean isobj = xIsObj(col);
          int xlen =0;
          if(isobj)
            xlen = xptr.length;
          else
            xlen = xLengthGet(col);
          for(int k=0; k<xlen; k++)
            hash = ((hash << 5) + hash) + xptr[k];
          break;
         
        case OBJECT:
          hash = ((hash << 5) + hash);
          Object oval = oValueGet(col);
          if (oval != null)
          {
            hash += oval.hashCode();
          }
          break;
 
        default:
          assert false;
      }
    }
    return hash;
  }
  
  public void aeval(Datatype type, Op op, 
        int dcol,
        ITuple s1, int col1, 
        ITuple s2, int col2) 
    throws ExecException
    {
      TupleBase src1 = (TupleBase) s1;
      TupleBase src2 = null;
      if (op == Op.NULL_CPY)
      {
        setAttrNull(dcol);
        return;
      }
      // SYSTIME OP has no arguments
      if(op == Op.SYSTIME)
      {
        // Presently As we are dealing with millisecond time value;
        // Result value is set as current system time in millisecond
        long currentTimeMillis = System.currentTimeMillis();        
        tValueSet(dcol, currentTimeMillis * 1000000l);
        tFormatSet(dcol, TimestampFormat.getDefault());
        return;
      }

      if (op.args == 1)
      {
        if (op.nullType != NullType.NOOP && src1.isAttrNull(col1))
        {
          setAttrNull(dcol);
          return;
        }
        // SYSTIMEWITHTZ represents systimestamp with time zone
        // It accepts timezone as an argument
        if(op == Op.SYSTIMEWITHTZ)
        {          
          // Negative Case Handling: SELECT SYSTIMESTAMP(null) FROM INPUT;
       	  if(src1.isAttrNull(col1))
       	  {
            LogUtil.warning(LoggerType.TRACE,"specified timezone value is null");
            throw new SoftExecException(ExecutionError.INVALID_TIME_ZONE_ID, "null");
          }
       	  
       	  char[] val1 =  src1.cValueGet(col1);
       	  ZoneId zid = null;
       	  
       	  // Parse timezone string and check if a valid time zone is provided
       	  try
       	  {
       	    zid = ZoneId.of(String.valueOf(val1));
       	  }
       	  catch (DateTimeException e) 
       	  {
            LogUtil.warning(LoggerType.TRACE, e.toString());
            throw new SoftExecException(ExecutionError.INVALID_TIME_ZONE_ID, String.valueOf(val1));
          }
       	  
          long currentTimeMillis = System.currentTimeMillis();        
          tValueSet(dcol, currentTimeMillis * 1000000l);
          tFormatSet(dcol, TimestampFormat.getTimestampWithTz(TimeZone.getTimeZone(zid)));
          return;
        }
      }
      
      
      if (op.args == 2)
      {
        // prepare the second attr for binary op
        src2 = (TupleBase) s2;
        switch (op.nullType)
        {
          case ANY:
            if (src1.isAttrNull(col1) || src2.isAttrNull(col2))
            {
              setAttrNull(dcol);
              return;
            }
            break;
  
          case BOTH:
            if (src1.isAttrNull(col1) && src2.isAttrNull(col2))
            {
              setAttrNull(dcol);
              return;
            }
            break;
        }  
      }

      // evaluate
      switch(type.getKind())
      {
        case INT:
        {
          int val1 = 0, val2 = 0, dval = 0;
          if (op != Op.CLEN && op != Op.BLEN)
          {
            if (!src1.isAttrNull(col1)) val1 = src1.iValueGet(col1);
            if (src2 != null && !src2.isAttrNull(col2))
              val2 = src2.iValueGet(col2);
          }
          switch(op)
          {
            case ADD: //INT_ADD
              dval = val1 + val2; 
              break;
            case SUB: //INT_SUB
              dval = val1 - val2; 
              break;
            case MUL: //INT_MUL
              dval = val1 * val2; 
              break;
            case DIV: //INT_DIV
              if(val2 == 0)
                throw new ExecException(ExecutionError.DIVIDE_BY_ZERO);
              dval = val1 / val2; 
              break;
            case SUM_ADD: //INT_SUM_ADD
              /*INT_SUM_ADD(x,y) = x + y if x is non null and y is non null
              x, if x is non null and y is null
              y, if x is null and y is non null
              null, if x and y are null */
              if (!src1.isAttrNull(col1) && !src2.isAttrNull(col2))
                 dval = val1 + val2;
              else if(!src1.isAttrNull(col1))
                dval = val1;
              else if(!src2.isAttrNull(col2))
                dval = val2;
              break;
              
            case SUM_SUB: //INT_SUM_SUB
              /*INT_SUM_SUB(x,y) = x - y if x is non null and y is non null
              x, if x is non null and y is null
              y, if x is null and y is non null
              null, if x and y are null */
              if (!src1.isAttrNull(col1) && !src2.isAttrNull(col2))
                dval = val1 - val2;
              else if(!src1.isAttrNull(col1))
                dval = val1;
              else
                assert(false);
              break;
              
            case NVL: //INT_NVL
              dval = src1.isAttrNull(col1) ? val2 : val1;
              break;
              
            case CPY: //INT_CPY
              dval = val1;
              break;
              
            case TO_BIGINT: //INT_TO_BIGINT
              lValueSet(dcol, (long) val1);
              return;

            case TO_BOOLEAN: // INT_TO_BOOLEAN
              boolValueSet(dcol, (val1 != 0));
              return;

            case TO_FLT: //INT_TO_FLT
              fValueSet(dcol, (float) val1);
              return;
            
            case TO_CHR1: //INT_TO_CHAR
              Scratch buf = scratchBuf.get();
              // Get & Cleaning temp Buffer
              StringBuilder tmpData = buf.charSeq;              
              tmpData.delete(0, tmpData.length());
              
              char[] tmpArray = buf.charBuf;
              
              tmpData.append(val1);
              // Read content from tmpData into tmpArray
              tmpData.getChars(0, tmpData.length(), tmpArray, 0);
              cValueSet(dcol, tmpArray, tmpData.length());            
              return;              
              
            case TO_DBL: //INT_TO_DBL
              dValueSet(dcol, (double) val1);
              return;
              
            case TO_BIGDECIMAL: //INT_TO_BIGDECIMAL
              BigDecimal val = new BigDecimal(val1);
              nValueSet(dcol, val, val.precision(), val.scale());
              return;
              
            case UMX:   //INT_UMX
              if (src1.isAttrNull(col1))
              {
                dval = val2;
              } else if (src2.isAttrNull(col2))
              {
                dval = val1;
              } else
              {
                dval = (val1 < val2) ? val2 : val1;
              }
              break;

            case UMN:   //INT_UMN
              if (src1.isAttrNull(col1))
              {
                dval = val2;
              } else if (src2.isAttrNull(col2))
              {
                dval = val1;
              } else
              {
                dval = (val1 > val2) ? val2 : val1;
              }
              break;

            case AVG:   //INT_AVG
              float fdval;
              if (src1.isAttrNull(col1))
              {
                fdval = (float) val2;
              } else if (src2.isAttrNull(col2))
              {
                fdval = (float) val1;
              } else
              {
                fdval = 
                  (float) (1.0 * val1) / (float) (1.0 * val2);
              }
              fValueSet(dcol, fdval);
              return;
              
            case CLEN:  //CHR_LEN
              // if argument to length(..) is null; then it returns null
              // else if argument to length(..) is empty string; then it returns null
              // else it returns length of argument
              if (src1.isAttrNull(col1) || src1.cLengthGet(col1) == 0){
                setAttrNull(dcol);
                return;
              }
              else
                dval = src1.cLengthGet(col1);
              break;


            case BLEN:  //BYTE_LEN
              // if argument to length(..) is null; then it returns null
              // else if argument to length(..) is empty string; then it returns null
              // else it returns length of argument
              if (src1.isAttrNull(col1) || src1.cLengthGet(col1) == 0) {
                setAttrNull(dcol);
                return;
              }
              else
                dval = src1.bLengthGet(col1);
              break;
          
            case MOD: // INT_MOD
              if(val2 == 0)
                dval = val1;
              else
                dval = val1 % val2;
              break;            
              
           default: 
              assert false;
          }
          iValueSet(dcol, dval);
        }
        break;

        case BIGINT:
        {
          long val1 = 0, val2 = 0, dval = 0;
          if (!src1.isAttrNull(col1))
            val1 = src1.lValueGet(col1);
          if (src2 != null && op != Op.AVG && !src2.isAttrNull(col2))
            val2 = src2.lValueGet(col2);
          switch(op)
          {
            case ADD: //BIGINT_ADD
              dval = val1 + val2; 
              break;
            case SUB: //BIGINT_SUB
              dval = val1 - val2; 
              break;
            case MUL: //BIGINT_MUL
              dval = val1 * val2; 
              break;
            case DIV: //BIGINT_DIV
              if(val2 == 0l)
                throw new ExecException(ExecutionError.DIVIDE_BY_ZERO);
              dval = val1 / val2; 
              break;
            case SUM_ADD: //BIGINT_SUM_ADD
              /*INT_SUM_ADD(x,y) = x + y if x is non null and y is non null
              x, if x is non null and y is null
              y, if x is null and y is non null
              null, if x and y are null */
              if (!src1.isAttrNull(col1) && !src2.isAttrNull(col2))
                 dval = val1 + val2;
              else if(!src1.isAttrNull(col1))
                dval = val1;
              else if(!src2.isAttrNull(col2))
                dval = val2;
              break;
              
            case SUM_SUB: //BIGINT_SUM_SUB
              /*INT_SUM_SUB(x,y) = x - y if x is non null and y is non null
              x, if x is non null and y is null
              y, if x is null and y is non null
              null, if x and y are null */
              if (!src1.isAttrNull(col1) && !src2.isAttrNull(col2))
                dval = val1 - val2;
              else if(!src1.isAttrNull(col1))
                dval = val1;
              else
                assert(false);
              break;
              
            case NVL: //BIGINT_NVL
              dval = src1.isAttrNull(col1) ? val2 : val1;
              break;
              
            case TO_BOOLEAN: // BIGINT_TO_BOOLEAN
              boolValueSet(dcol, (val1 != 0));
              return;

            case TO_FLT: //BIGINT_TO_FLT
              fValueSet(dcol, (float) val1);
              return;
              
            case TO_DBL: //BIGINT_TO_DBL
              dValueSet(dcol, (double) val1);
              return;
              
            case TO_BIGDECIMAL: //BIGINT_TO_BIGDECIMAL
              BigDecimal val = new BigDecimal(val1);
              nValueSet(dcol, val, val.precision(), val.scale());
              return;
              
            case TO_CHR1: //BIGINT_TO_CHAR
              Scratch buf = scratchBuf.get();
              
              // Allocate and Cleaning temp Buffer
              StringBuilder tmpData = buf.charSeq;
              tmpData.delete(0, tmpData.length());
              
              char[] tmpArray = buf.charBuf;            
              
              tmpData.append(val1);
              //Read tmpData into tmpArray
              tmpData.getChars(0, tmpData.length(), tmpArray, 0);
              cValueSet(dcol, tmpArray, tmpData.length());            
              return;
              
              
            case CPY: //BIGINT_CPY
              dval = val1;
              break;

            case UMX:   //BIGIN_UMX
              if (src1.isAttrNull(col1))
              {
                dval = val2;
              } else if (src2.isAttrNull(col2))
              {
                dval = val1;
              } else
              {
                dval = (val1 < val2) ? val2 : val1;
              }
              break;

            case UMN:   //BIGINT_UMN
              if (src1.isAttrNull(col1))
              {
                dval = val2;
              } else if (src2.isAttrNull(col2))
              {
                dval = val1;
              } else
              {
                dval = (val1 > val2) ? val2 : val1;
              }
              break;
            
            case MOD:  //BIGINT_MOD
              if(val2 == 0L)
                dval = val1;
              else
                dval = val1 % val2;
              break; 

            case AVG:   //BIGINT_AVG
              int ival2 = src2.iValueGet(col2);
              double ddval;
              if (src1.isAttrNull(col1))
              {
                ddval = (double) ival2;
              } else if (src2.isAttrNull(col2))
              {
                ddval = (double) val1;
              } else
              {
                ddval = 
                  (double) (1.0 * val1) / (double) (1.0 * ival2);
              }
              dValueSet(dcol, ddval);
              return;
              
            default: 
              assert false;
          }
          lValueSet(dcol, dval);
        }
        break;

        case FLOAT:
        {
          float val1 = 0, val2 = 0, dval = 0;
          if (!src1.isAttrNull(col1))
            val1 = src1.floatValueGet(col1);
          if (src2 != null && op != Op.AVG && !src2.isAttrNull(col2))
            val2 = src2.floatValueGet(col2);
          switch(op)
          {
            case ADD: //FLT_ADD
              dval = val1 + val2; 
              break;
            case SUB: //FLT_SUB
              dval = val1 - val2; 
              break;
            case MUL: //FLT_MUL
              dval = val1 * val2; 
              break;
            case DIV: //FLT_DIV
              if(val2 == 0.0f)
                throw new ExecException(ExecutionError.DIVIDE_BY_ZERO);
              dval = val1 / val2; 
              break;
            case SUM_ADD: //FLT_SUM_ADD
              /*INT_SUM_ADD(x,y) = x + y if x is non null and y is non null
              x, if x is non null and y is null
              y, if x is null and y is non null
              null, if x and y are null */
              if (!src1.isAttrNull(col1) && !src2.isAttrNull(col2))
                 dval = val1 + val2;
              else if(!src1.isAttrNull(col1))
                dval = val1;
              else if(!src2.isAttrNull(col2))
                dval = val2;
              break;
              
            case SUM_SUB: //FLT_SUM_SUB
              /*INT_SUM_SUB(x,y) = x - y if x is non null and y is non null
              x, if x is non null and y is null
              y, if x is null and y is non null
              null, if x and y are null */
              if (!src1.isAttrNull(col1) && !src2.isAttrNull(col2))
                dval = val1 - val2;
              else if(!src1.isAttrNull(col1))
                dval = val1;
              else
                assert(false);
              break;
              
            case TO_DBL: //FLT_TO_DBL
              dValueSet(dcol, (double) val1);
              return;
              
            case TO_BIGDECIMAL: //FLT_TO_BIGDECIMAL
              BigDecimal val = new BigDecimal(String.valueOf(val1));
              nValueSet(dcol, val, val.precision(), val.scale());
              return; 
              
            case TO_CHR1: //FLT_TO_CHR
              Scratch buf = scratchBuf.get();
              
              // Allocate and Cleaning temp Buffer
              StringBuilder tmpData = buf.charSeq;
              tmpData.delete(0, tmpData.length());
              
              char[] tmpArray = buf.charBuf;            
              
              tmpData.append(val1);
              //Read tmpData into tmpArray
              tmpData.getChars(0, tmpData.length(), tmpArray, 0);
              cValueSet(dcol, tmpArray, tmpData.length());            
              return;
              
            case NVL: //FLT_NVL
              dval = src1.isAttrNull(col1) ? val2 : val1;
              break;
              
            case CPY: //FLT_CPY
              dval = val1;
              break;
              
            case UMX:   //FLT_UMX
              if (src1.isAttrNull(col1))
              {
                dval = val2;
              } else if (src2.isAttrNull(col2))
              {
                dval = val1;
              } else
              {
                dval = (val1 < val2) ? val2 : val1;
              }
              break;

            case UMN:   //FLT_UMN
              if (src1.isAttrNull(col1))
              {
                dval = val2;
              } else if (src2.isAttrNull(col2))
              {
                dval = val1;
              } else
              {
                dval = (val1 > val2) ? val2 : val1;
              }
              break;
              
            case AVG:   //FLT_AVG
              int ival2 = src2.iValueGet(col2);
              if (src1.isAttrNull(col1))
              {
                dval = (float) ival2;
              } else if (src2.isAttrNull(col2))
              {
                dval = val1;
              } else
              {
                dval = val1 / (float) (1.0 * ival2);
              }
              break;
            
            case MOD:  //FLOAT_MOD
              if(val2 == 0.0f)
                dval = val1;
              else
                dval = val1 % val2;
              break; 
              
            default: 
              assert false;
          }
          fValueSet(dcol, dval);
        }
        break;
        
        case DOUBLE:
        {
          double val1 = 0, val2 = 0, dval = 0;
          if (!src1.isAttrNull(col1))
            val1 = src1.doubleValueGet(col1);
          if (src2 != null && op != Op.AVG && !src2.isAttrNull(col2))
            val2 = src2.doubleValueGet(col2);
          switch(op)
          {
            case ADD: //DBL_ADD
              dval = val1 + val2; 
              break;
            case SUB: //DBL_SUB
              dval = val1 - val2; 
              break;
            case MUL: //DBL_MUL
              dval = val1 * val2; 
              break;
            case DIV: //DBL_DIV
              if(val2 == 0d)
                throw new ExecException(ExecutionError.DIVIDE_BY_ZERO);
              dval = val1 / val2; 
              break;
            case SUM_ADD: //DBL_SUM_ADD
              /*DBL_SUM_ADD(x,y) = x + y if x is non null and y is non null
              x, if x is non null and y is null
              y, if x is null and y is non null
              null, if x and y are null */
              if (!src1.isAttrNull(col1) && !src2.isAttrNull(col2))
                 dval = val1 + val2;
              else if(!src1.isAttrNull(col1))
                dval = val1;
              else if(!src2.isAttrNull(col2))
                dval = val2;
              break;
              
            case SUM_SUB: //DBL_SUM_SUB
              /*DBL_SUM_SUB(x,y) = x - y if x is non null and y is non null
              x, if x is non null and y is null
              y, if x is null and y is non null
              null, if x and y are null */
              if (!src1.isAttrNull(col1) && !src2.isAttrNull(col2))
                dval = val1 - val2;
              else if(!src1.isAttrNull(col1))
                dval = val1;
              else
                assert(false);
              break;
              
            case TO_BIGDECIMAL:
              BigDecimal val = new BigDecimal(String.valueOf(val1));
              nValueSet(dcol, val, val.precision(), val.scale());
              return; 
              
            case TO_CHR1: //DBL_TO_CHR
              Scratch buf = scratchBuf.get();
              
              // Allocate and Cleaning temp Buffer
              StringBuilder tmpData = buf.charSeq;
              tmpData.delete(0, tmpData.length());
              
              char[] tmpArray = buf.charBuf;            
              
              tmpData.append(val1);
              //Read tmpData into tmpArray
              tmpData.getChars(0, tmpData.length(), tmpArray, 0);
              cValueSet(dcol, tmpArray, tmpData.length());            
              return;
              
              
            case NVL: //DBL_NVL
              dval = src1.isAttrNull(col1) ? val2 : val1;
              break;
              
            case CPY: //DBL_CPY
              dval = val1;
              break;
              
            case UMX:   //DBL_UMX
              if (src1.isAttrNull(col1))
              {
                dval = val2;
              } else if (src2.isAttrNull(col2))
              {
                dval = val1;
              } else
              {
                dval = (val1 < val2) ? val2 : val1;
              }
              break;

            case UMN:   //DBL_UMN
              if (src1.isAttrNull(col1))
              {
                dval = val2;
              } else if (src2.isAttrNull(col2))
              {
                dval = val1;
              } else
              {
                dval = (val1 > val2) ? val2 : val1;
              }
              break;
              
            case AVG:   //DBL_AVG
              int ival2 = src2.iValueGet(col2);
              if (src1.isAttrNull(col1))
              {
                dval = (double) ival2;
              } else if (src2.isAttrNull(col2))
              {
                dval = val1;
              } else
              {
                dval = val1 / (double) (1.0 * ival2);
              }
              break;
          
            case MOD: //DOUBLE_MOD
              if(val2 == 0d)
                dval = val1;
              else
                dval = val1 % val2;
              break; 
              
            default: 
              assert false;
          }
          dValueSet(dcol, dval);
        }
        break;
        
        case BIGDECIMAL:
        {
          BigDecimal val1 = BigDecimal.ZERO, val2 = BigDecimal.ZERO, 
                            nval = BigDecimal.ZERO;
          if (!src1.isAttrNull(col1))
            val1 = src1.bigDecimalValueGet(col1);
          if (src2 != null && op != Op.AVG && !src2.isAttrNull(col2))
            val2 = src2.bigDecimalValueGet(col2);
          switch(op)
          {
            case ADD: //BIGDECIMAL_ADD
              nval = val1.add(val2); 
              break;
            case SUB: //BIGDECIMAL_SUB
              nval = val1.subtract(val2); 
              break;
            case MUL: //BIGDECIMAL_MUL
              nval = val1.multiply(val2); 
              break;
            case DIV: //BIGDECIMAL_DIV
              if(val2.compareTo(BigDecimal.ZERO) == 0)
                throw new ExecException(ExecutionError.DIVIDE_BY_ZERO);
              nval = val1.divide(val2, RoundingMode.HALF_UP); 
              break;
            case SUM_ADD: //BIGDECIMAL_SUM_ADD
              /*BIGDECIMAL_SUM_ADD(x,y) = x + y if x is non null and y is non null
              x, if x is non null and y is null
              y, if x is null and y is non null
              null, if x and y are null */
              if (!src1.isAttrNull(col1) && !src2.isAttrNull(col2))
                 nval = val1.add(val2);
              else if(!src1.isAttrNull(col1))
                nval = val1;
              else if(!src2.isAttrNull(col2))
                nval = val2;
              break;
              
            case SUM_SUB: //DBL_SUM_SUB
              /*BIGDECIMAL_SUM_SUB(x,y) = x - y if x is non null and y is non null
              x, if x is non null and y is null
              y, if x is null and y is non null
              null, if x and y are null */
              if (!src1.isAttrNull(col1) && !src2.isAttrNull(col2))
                nval = val1.subtract(val2);
              else if(!src1.isAttrNull(col1))
                nval = val1;
              else
                assert(false);
              break;
              
            case TO_CHR1: //BIGDECIMAL_TO_CHR
              Scratch buf = scratchBuf.get();
              
              // Allocate and Cleaning temp Buffer
              StringBuilder tmpData = buf.charSeq;
              tmpData.delete(0, tmpData.length());
              
              char[] tmpArray = buf.charBuf;            
              
              tmpData.append(val1);
              //Read tmpData into tmpArray
              tmpData.getChars(0, tmpData.length(), tmpArray, 0);
              cValueSet(dcol, tmpArray, tmpData.length());            
              return;
              
            case NVL: //BIGDECIMAL_NVL
              nval = src1.isAttrNull(col1) ? val2 : val1;
              break;
              
            case CPY: //BIGDECIMAL_CPY
              nval = val1;
              break;
              
            case UMX:   //BIGDECIMAL_UMX
              if (src1.isAttrNull(col1))
              {
                nval = val2;
              } else if (src2.isAttrNull(col2))
              {
                nval = val1;
              } else
              {
                nval = (val1.compareTo(val2) < 0) ? val2 : val1;
              }
              break;

            case UMN:   //BIGDECIMAL_UMN
              if (src1.isAttrNull(col1))
              {
                nval = val2;
              } else if (src2.isAttrNull(col2))
              {
                nval = val1;
              } else
              {
                nval = (val1.compareTo(val2) > 0) ? val2 : val1;
              }
              break;
              
            case AVG:   //BIGDECIMAL_AVG
              int ival2 = src2.iValueGet(col2);
              if (src1.isAttrNull(col1))
              {
                nval = new BigDecimal(ival2);
              } else if (src2.isAttrNull(col2))
              {
                nval = val1;
              } else
              {
                nval = val1.divide(new BigDecimal(ival2));
              }
              break;
          
            case MOD: /*//BIGDECIMAL_MOD
              if(val2 == 0d)
                dval = val1;
              else
                dval = val1 % val2;*/
              break; 
              
            default: 
              assert false;
          }
          nValueSet(dcol, nval, nval.precision(), nval.scale());
        }
        break;
        case CHAR:
        {
          char[] val1 = null;
          int len1 = 0;
          char[] val2 = null;
          int len2 = 0;
          boolean isDestChar = true;
          
          if (op != Op.BYT_TO_HEX && !src1.isAttrNull(col1))
          {
            val1 = src1.cValueGet(col1);
            len1 = src1.cLengthGet(col1);
          }
          if (src2 != null && !src2.isAttrNull(col2))
          {
            val2 = src2.cValueGet(col2);
            len2 = src2.cLengthGet(col2);
          }
          char[] dval = null;
          Scratch buf = scratchBuf.get();
          char[] tempArray = buf.charBuf;
          int dlen = 0;
          int i = 0;
          int j = 0;
          switch(op)
          {
            case CPY: //CHAR_CPY
              dval = val1;
              dlen = len1;
              break;
              
            case LOWER: // CHR_LOWER
              for(i=0; i<len1; i++)
                tempArray[i] = Character.toLowerCase(val1[i]);
              dval = tempArray;
              dlen = len1;
              break;
            case UPPER: // CHR_UPPER
              for(i=0; i<len1; i++)
                tempArray[i] = Character.toUpperCase(val1[i]);
              dval = tempArray;
              dlen = len1;
              break;
            case INITCAP:
              boolean isConvertible = true;
              for(i=0; i < len1; i++)
              {
                if(isConvertible && Character.isLetterOrDigit(val1[i]))
                {
                  tempArray[i] = Character.toUpperCase(val1[i]);
                  isConvertible = false;
                }
                else
                {
                  tempArray[i] = Character.toLowerCase(val1[i]);
                  if(!Character.isLetterOrDigit(val1[i]))
                    isConvertible = true;
                }
              }
              dval = tempArray;
              dlen = len1;
              break;
            case LTRIM1:
            case RTRIM1:
              if(op == Op.LTRIM1)
              {
                for(i=0; i < len1 && Character.getType(val1[i]) == 
                  Character.SPACE_SEPARATOR ; i++);
                System.arraycopy(val1, i, tempArray, 0, len1-i);
                dlen = len1-i;
              }
              else if(op == Op.RTRIM1)
              {
                for(i=len1-1; i >= 0 && Character.getType(val1[i]) == 
                  Character.SPACE_SEPARATOR; i--);
                System.arraycopy(val1, 0, tempArray, 0, i+1);
                dlen = i+1;
              }
              dval = tempArray;
              break;
            case LTRIM2:
            case RTRIM2:
              LinkedHashSet<Character> set = buf.charLinkedHashSet;
              for(i = 0; i < len2; i++)
                set.add(val2[i]);
              if(op == Op.LTRIM2)
              {
                for(j = 0; j < len1 && set.contains(val1[j]); j++);
                System.arraycopy(val1, j,tempArray, 0, len1-j);
                dlen = len1-j;
              }
              else if(op == Op.RTRIM2)
              {
                for(j = len1-1 ; j >= 0 && set.contains(val1[j]); j--);
                System.arraycopy(val1, 0, tempArray, 0, j+1);
                dlen = j+1;
              }
              dval = tempArray;
              
              break;
            case SUBSTR:
              i = src2.iValueGet(col2);
              if(i <= len1 && i > 0)
              {
                int attr2val = i;
                System.arraycopy(val1, attr2val-1 , tempArray, 0, len1 - attr2val + 1 );
                dval = tempArray;
                dlen = len1 - attr2val + 1;
              }
              break;
            case LPAD:
            case RPAD:
              i = src2.iValueGet(col2);
              if(i > 0)
              {
                int attr2val     = i;
                final char BLANK = ' '; 
                
                if(attr2val <= len1)
                  System.arraycopy(val1, 0, tempArray, 0, attr2val);
                else
                {
                  if(op == Op.LPAD)
                  {
                    for(i=0; i < attr2val -len1; i++)
                      tempArray[i] = BLANK;
                    System.arraycopy(val1, 0, tempArray, i, len1);
                  }
                  else if(op == Op.RPAD)
                  {
                    System.arraycopy(val1, 0, tempArray, 0, len1);
                    for(i= len1; i < attr2val; i++)
                      tempArray[i] = BLANK;
                  } 
                }
                dval = tempArray;
                dlen = attr2val;
              }
              break;
              
            case CONCAT:        //CHR_CONCAT
              if (src1.isAttrNull(col1))
              {
                dval = val2;
                dlen = len2;
              }
              else if (src2.isAttrNull(col2))
              {
                dval = val1;
                dlen = len1;
              }
              else
              {
                for (j = 0; j < len1; j++)
                  tempArray[j] = val1[j];
                for (j = 0; j < len2; j++)
                  tempArray[j + len1] = val2[j];
                dval = tempArray;
                dlen = len1 + len2;
              }
              break;
              
            case NVL:   //CHR_NVL
              if(src1.isAttrNull(col1))
              {
                dval = val2;
                dlen = len2;
              }
              else
              {
                dval = val1;
                dlen = len1;
              }
              break;
              
            case BYT_TO_HEX:    //BYT_TO_HEX
            {
              byte[] bval1 = src1.bValueGet(col1);
              int blen1 = src1.bLengthGet(col1);
              try
              {
                dval = Datatype.byteToHex( bval1, blen1);
                dlen = dval.length;
              }
              catch(CEPException ce)
              {
                throw new SoftExecException(ExecutionError.INVALID_ATTR, ce.getMessage());
              }
            }
            break;
            
            case UMX: // CHR_UMX
              if (src1.isAttrNull(col1))
              {
                dval = val2;
                dlen = len2;
              } else if (src2.isAttrNull(col2))
              {
                dval = val1;
                dlen = len1;
              } else
              {
                String value1 = new String(val1);
                String value2 = new String(val2);
                if ((value1.compareTo(value2)) < 0)
                {
                  dval = val2;
                  dlen = len2;
                }
                else
                {
                  dval = val1;
                  dlen = len1;
                }
              }             
              break;
              
            case UMN: //CHR_UMN
              if (src1.isAttrNull(col1))
              {
                dval = val2;
                dlen = len2;
              } else if (src2.isAttrNull(col2))
              {
                dval = val1;
                dlen = len1;
              } else
              {
                String value1 = new String(val1);
                String value2 = new String(val2);
                if ((value1.compareTo(value2)) > 0)
                {
                  dval = val2;
                  dlen = len2;
                }
                else
                {
                  dval = val1;
                  dlen = len1;
                }
              }             
              break;
              
              /**
               * Character to Number conversions 
               */
            case TO_INT:
              if (val1 != null)
              {
                String value1 = new String(val1);
                Integer dvalue = null;
                try
                {
                  dvalue = Integer.parseInt(value1);
                }
                catch(NumberFormatException e)
                {
                  LogUtil.fine(LoggerType.TRACE, e.toString());
                  throw new SoftExecException(ExecutionError.INVALID_NUMBER, value1,
                      "integer");
                }
                iValueSet(dcol, dvalue);
                
                isDestChar = false;
              }
              break;
              
            case TO_BIGINT:
              if (val1 != null)
              {
                String value1 = new String(val1);
                Long dvalue = null;
                try
                {
                  dvalue = Long.parseLong(value1);
                }
                catch(NumberFormatException e)
                {
                  LogUtil.fine(LoggerType.TRACE, e.toString());
                  throw new SoftExecException(ExecutionError.INVALID_NUMBER, "bigint");
                }
                lValueSet(dcol, dvalue);
                
                isDestChar = false;
              }
              break;
              
            case TO_FLT:
              if (val1 != null)
              {
                String value1 = new String(val1);
                Float dvalue = null;
                try
                {
                  dvalue = Float.parseFloat(value1);
                }
                catch(NumberFormatException e)
                {
                  LogUtil.fine(LoggerType.TRACE, e.toString());
                  throw new SoftExecException(ExecutionError.INVALID_NUMBER, value1,
                      "float");
                }
                fValueSet(dcol, dvalue);
                
                isDestChar = false;
              }
              break;
              
            case TO_DBL:
              if (val1 != null)
              {
                String value1 = new String(val1);
                Double dvalue = null;
                try
                {
                  dvalue = Double.parseDouble(value1);
                }
                catch(NumberFormatException e)
                {
                  LogUtil.fine(LoggerType.TRACE, e.toString());
                  throw new SoftExecException(ExecutionError.INVALID_NUMBER, value1,
                      "double");
                }
                dValueSet(dcol, dvalue);
                
                isDestChar = false;
              }
              break;
              
            case TO_BIGDECIMAL:
              if (val1 != null)
              {
                BigDecimal dvalue = null;
                try
                {
                  dvalue = new BigDecimal(val1);
                }
                catch(NumberFormatException e)
                {
                  LogUtil.fine(LoggerType.TRACE, e.toString());
                  throw new SoftExecException(ExecutionError.INVALID_NUMBER, 
                      new String(val1), "bigdecimal");
                }
                nValueSet(dcol, dvalue, dvalue.precision(), dvalue.scale());
                
                isDestChar = false;
              }
              break;
              
            default:
              assert false;
          }
          if(isDestChar)
            cValueSet(dcol, dval, dlen);
        }
        break;
        
        case BYTE:
        {
          byte[] val1 = null;
          int len1 = 0;
          byte[] val2 = null;
          int len2 = 0;
          if (op != Op.HEX_TO_BYT && !src1.isAttrNull(col1))
          {
            val1 = src1.bValueGet(col1);
            len1 = src1.bLengthGet(col1);
          }
          if (src2 != null && !src2.isAttrNull(col2))
          {
            val2 = src2.bValueGet(col2);
            len2 = src2.bLengthGet(col2);
          }
          byte[] dval = null;
          int dlen = 0;
          switch(op)
          {
            case CPY:   //BYT_CPY
              dval = val1;
              dlen = len1;
              break;
              
            case NVL:   //BYT_NVL
              if(src1.isAttrNull(col1))
              {
                dval = val2;
                dlen = len2;
              }
              else
              {
                dval = val1;
                dlen = len1;
              }
              break;

            case CONCAT:        //BYT_CONCAT
              if (src1.isAttrNull(col1))
              {
                dval = val2;
                dlen = len2;
              }
              else if (src2.isAttrNull(col2))
              {
                dval = val1;
                dlen = len1;
              }
              else
              {
                Scratch buf = scratchBuf.get();
                byte[] btmpArray = buf.byteBuf;
                for (int j = 0; j < len1; j++)
                  btmpArray[j] = val1[j];
                for (int j = 0; j < len2; j++)
                  btmpArray[j + len1] = val2[j];
                dval = btmpArray;
                dlen = len1 + len2;
              }
              break;

            case HEX_TO_BYT:    //HEX_TO_BYT
              char[] cval1 = src1.cValueGet(col1);
              int clen1 = src1.cLengthGet(col1);
              try
              {
                dval = Datatype.hexToByte(cval1, clen1);
                dlen = dval.length;
              } catch (CEPException c)
              {
                throw new ExecException(ExecutionError.INVALID_ATTR, c.getMessage());
              }
              break;
                  
            case  UMX: //BYT_UMX
              if (src1.isAttrNull(col1))
              {
                dval = val2;
                dlen = len2;
              } 
              else if (src2.isAttrNull(col2))
              {
                dval = val1;
                dlen = len1;
              }
              else
              {
                int len = len1 < len2 ? len1 : len2;
                int i   = 0;
                for (i = 0; i < len; i++) {
                  if (val1[i] < val2[i]) 
                  {
                    dval = val2;
                    dlen = len2;
                    break;
                  } 
                  else if (val1[i] > val2[i])
                  {
                    dval = val1;
                    dlen = len1;
                    break;
                  }
                }
                if (i == len) // when val1==val2 after loop, value having higher length is bigger. 
                {
                  if (len1 > len2)
                  {
                    dval = val1;
                    dlen = len1;
                  }
                  else
                  {
                    dval = val2;
                    dlen = len2;
                  }
                }
              }         
              break;
              
            case  UMN: //BYT_UMN
              if (src1.isAttrNull(col1))
              {
                dval = val2;
                dlen = len2;
              } 
              else if (src2.isAttrNull(col2))
              {
                dval = val1;
                dlen = len1;
              }
              else
              {
                int len = len1 < len2 ? len1 : len2;
                int i   = 0;
                for (i = 0; i < len; i++) {
                  if (val1[i] < val2[i]) 
                  {
                    dval = val1;
                    dlen = len1;
                    break;
                  } 
                  else if (val1[i] > val2[i])
                  {
                    dval = val2;
                    dlen = len2;
                    break;
                  }
                }
                if (i == len) // when val1==val2 after loop, value with shorter length is smaller. 
                {
                  if (len1 > len2)
                  {
                    dval = val2;
                    dlen = len2;
                  }
                  else
                  {
                    dval = val1;
                    dlen = len1;
                  }
                }
              }         
              break;  
            default:
              assert false;
          }
          bValueSet(dcol, dval, dlen);
        }
        break;
        
        case TIMESTAMP:
        {
          long val1 = 0, val2 = 0, dval = 0;
          String destFormatString = null;
          TimestampFormat resultFormat = null;
          TimestampFormat inpAttrFormat1 = null;
          TimestampFormat inpAttrFormat2 = null;                   
          
          if (op != Op.TIM_ADD && op != Op.TO_TIMESTAMP && !src1.isAttrNull(col1))
          {
            val1 = src1.tValueGet(col1);
            inpAttrFormat1 = src1.tFormatGet(col1);
          }
          if (src2 != null && op != Op.INTERVAL_ADD && op != Op.INTERVAL_SUB && !src2.isAttrNull(col2)
              && op != Op.INTERVALYM_ADD && op != Op.INTERVALYM_SUB && op != Op.TO_CHR2)
          {
            val2 = src2.tValueGet(col2);
            inpAttrFormat2 = src2.tFormatGet(col2);
          }
          if(src2 != null && op == Op.TO_CHR2)
          {
            destFormatString = new String((src2.cValueGet(col2)));
          }
         
          switch(op)
          {
            case INTERVAL_ADD:  //TIM_INTERVAL_ADD
              val2 = src2.vValueGet(col2);
              // interval value is in the unit of nanoseconds
              //val2 = val2 / 1000000l;
              dval = val1 + val2;
              resultFormat = inpAttrFormat1;
              break;
            case INTERVAL_SUB:  //TIM_INTERVAL_SUB
              val2 = src2.vValueGet(col2);
              // interval value is in the unit of nanoseconds
              //val2 = val2 / 1000000l;
              dval = val1 - val2;
              resultFormat = inpAttrFormat1;
              break;

            case TIM_ADD:  //INTERVAL_TIM_ADD & INTERVALYM_TIM_ADD
              if(src1.getAttrType(col1) == Datatype.INTERVAL)
              {
                val1 = src1.vValueGet(col1);
                //val1 = val1 / 1000000l;
                dval = val1 + val2;
                resultFormat = inpAttrFormat1;
              }
              else 
              {
                assert src1.getAttrType(col1) == Datatype.INTERVALYM;
                Long lval1 = src1.vymValueGet(col1);
                Calendar cal = Calendar.getInstance();
                
                // convert timestamp to millis ( from nanos)
                long val2Millis = val2 / 1000000l;
                long val2Offset = val2 - val2Millis * 1000000l;
                
                cal.setTimeInMillis(val2Millis);
                cal.add(Calendar.MONTH, lval1.intValue());
                
                long resultMillis = cal.getTimeInMillis();
                dval = resultMillis * 1000000l + val2Offset;
                resultFormat = inpAttrFormat2;
              }              
              break;
              
            case INTERVALYM_ADD:  //TIM_INTERVALYM_ADD
            {
              Long lval2 = src2.vymValueGet(col2);              
              Calendar cal = Calendar.getInstance();
              
              // convert timestamp to millis ( from nanos)
              long val1Millis = val1 / 1000000l;
              long val1Offset = val1 - val1Millis * 1000000l;
              
              cal.setTimeInMillis(val1Millis);              
              // interval value is in the unit of months
              cal.add(Calendar.MONTH, lval2.intValue());
              
              long resultMillis = cal.getTimeInMillis();
              dval = resultMillis * 1000000l + val1Offset;
              resultFormat = inpAttrFormat1;
              break;
            }
            case INTERVALYM_SUB:  //TIM_INTERVALYM_SUB
            {              
              Long lVal2 = src2.vymValueGet(col2);             
              Calendar cal2 = Calendar.getInstance();
              
              // convert timestamp value to millis ( from nanos)
              long val1Millis = val1 / 1000000l;
              long val1Offset = val1 - val1Millis * 1000000l;
              
              cal2.setTimeInMillis(val1Millis);              
              // interval value is in the unit of months
              cal2.add(Calendar.MONTH, 0-lVal2.intValue());
              
              long resultMillis = cal2.getTimeInMillis();
              dval = resultMillis * 1000000l + val1Offset;
              resultFormat = inpAttrFormat1;
              break;
            } 
            case NVL:   //TIM_NVL
              dval = src1.isAttrNull(col1) ? val2 : val1;
              resultFormat 
                = src1.isAttrNull(col1) ? inpAttrFormat2 : inpAttrFormat1;
              break;
              
            case CPY:   //TIM_CPY
              dval = val1;
              resultFormat = inpAttrFormat1;
              break;
              
            case UMX:   //TIM_UMX
              if (src1.isAttrNull(col1))
              {
                dval = val2;
                resultFormat = inpAttrFormat2;
              } 
              else if (src2.isAttrNull(col2))
              {
                dval = val1;
                resultFormat = inpAttrFormat1;
              } 
              else
              {
                dval = (val1 < val2) ? val2 : val1;
                resultFormat = 
                    (val1 < val2) ? inpAttrFormat2 : inpAttrFormat1;
              }
              break;

            case UMN:   //TIM_UMN
              if (src1.isAttrNull(col1))
              {
                dval = val2;
                resultFormat = inpAttrFormat2;
              } 
              else if (src2.isAttrNull(col2))
              {
                dval = val1;
                resultFormat = inpAttrFormat1;
              } 
              else
              {
                dval = (val1 > val2) ? val2 : val1;
                resultFormat = (val1 > val2) ? inpAttrFormat2 : inpAttrFormat1;
              }
              break;
              
            case TO_BIGINT:
            	lValueSet(dcol, val1);
              return;
              
            case TO_CHR1: //TIMESTAMP_TO_CHAR
            case TO_CHR2:
              Scratch buf = scratchBuf.get();
              
              CEPDateFormat sdf1 = CEPDateFormat.getInstance();
              //sdf1.setLenient(false); ??? what is this for? Lenient is supposed to be used with parsing.
              
              // Allocate and Cleaning temp Buffer
              StringBuilder tmpData = buf.charSeq;
              tmpData.delete(0, tmpData.length());
              
              char[] tmpArray = buf.charBuf;            
              
              // Reason for synchronized SimpleDateFormat object sdf1:
              // sdf1 object is oracle.cep.common.Constant.TIMESTAMP_FORMAT
              // sdf1 will be referred by all threads during execution time.       
              String formattedOutStr = null;
              try
              {
                if(destFormatString != null)
                {
                  formattedOutStr = sdf1.format(val1, destFormatString);
                }
                else
                {
                  formattedOutStr = sdf1.format(val1, inpAttrFormat1);
                }
              }
              catch(ParseException e)
              {
                throw new ExecException(ExecutionError.INVALID_TIMEFORMAT);
              }
              
              synchronized(sdf1)
              {
                tmpData.append(formattedOutStr);
              }
             
              //Read tmpData into tmpArray
              tmpData.getChars(0, tmpData.length(), tmpArray, 0);
              cValueSet(dcol, tmpArray, tmpData.length());            
              return;
              
            default: 
              assert false;
          }
          tValueSet(dcol, dval);
          tFormatSet(dcol, resultFormat);
        }
        break;
        
        case INTERVAL:
        {
          long val1 = 0, val2 = 0, dval = 0;
          if (op != Op.TIM_SUB)
          {
            if (!src1.isAttrNull(col1))
              val1 = src1.vValueGet(col1);
            if (src2 != null && !src2.isAttrNull(col2))
              val2 = src2.vValueGet(col2);
          }
          
          // If the second attribute is null, then destination format will be
          // first attribute's format
          // Else set destination format to format with highest precisions
          IntervalFormat destinationFormat = null;
          if(src2 != null || op == Op.TIM_SUB)
          {
            try
            {
              destinationFormat 
                = new IntervalFormat(TimeUnit.DAY, TimeUnit.SECOND, 9, 9);              
            } 
            catch (CEPException e)
            {
              // We shouldn't reach here as we are creating interval format
              // with correct parameters.
              assert false;
            }
          }
          else
            destinationFormat = src1.vFormatGet(col1);
                    
          
          switch(op)
          {
            case ADD: //INTERVAL_ADD
              dval = val1 + val2; 
              break;
            case SUB: //INTERVAL_SUB
              dval = val1 - val2; 
              break;
            case TIM_SUB: //TIM_SUB
              val1 = src1.tValueGet(col1);
              val2 = src2.tValueGet(col2);
    
              // Note: dval is in the unit of nanoseconds as val1 and vol2
              // represents timestamp values
              dval = val1 - val2;
              //TODO: Database doesn't allow subtraction between two timestamp
              // values if any of them is having format mentioned.
              this.spec.getAttrType(dcol).setIntervalFormat(destinationFormat);
              break;
            case SUM_ADD: //INTERVAL_SUM_ADD
              /*INT_SUM_ADD(x,y) = x + y if x is non null and y is non null
              x, if x is non null and y is null
              y, if x is null and y is non null
              null, if x and y are null */
              if (!src1.isAttrNull(col1) && !src2.isAttrNull(col2))
                 dval = val1 + val2;
              else if(!src1.isAttrNull(col1))
                dval = val1;
              else if(!src2.isAttrNull(col2))
                dval = val2;
              break;
              
            case SUM_SUB: //INTERVAL_SUM_SUB
              /*INT_SUM_SUB(x,y) = x - y if x is non null and y is non null
              x, if x is non null and y is null
              y, if x is null and y is non null
              null, if x and y are null */
              if (!src1.isAttrNull(col1) && !src2.isAttrNull(col2))
                dval = val1 - val2;
              else if(!src1.isAttrNull(col1))
                dval = val1;
              else
                assert(false);
              break;
              
            case NVL:   //INTERVAL_NVL
              dval = src1.isAttrNull(col1) ? val2 : val1;
              break;
              
            case CPY: //INTERVAL_CPY
              dval = val1;
              break;
              
            case UMX:   //INTERVAL_UMX
              if (src1.isAttrNull(col1))
              {
                dval = val2;
              } else if (src2.isAttrNull(col2))
              {
                dval = val1;
              } else
              {
                dval = (val1 < val2) ? val2 : val1;
              }
              break;

            case UMN:   //INTERVAL_UMN
              if (src1.isAttrNull(col1))
              {
                dval = val2;
              } else if (src2.isAttrNull(col2))
              {
                dval = val1;
              } else
              {
                dval = (val1 > val2) ? val2 : val1;
              }
              break;
              
            case TO_CHR1: //INTERVAL_TO_CHAR
              Scratch buf = scratchBuf.get();
              
              // Allocate and Cleaning temp Buffer
              StringBuilder tmpData = buf.charSeq;
              tmpData.delete(0, tmpData.length());
              
              char[] tmpArray = buf.charBuf;
              
              String intervalStringValue = 
                oracle.cep.common.IntervalConverter.getDSInterval(
                  val1, destinationFormat);
              
              tmpData.append(intervalStringValue);
              
              //Read tmpData into tmpArray
              tmpData.getChars(0, tmpData.length(), tmpArray, 0);
              cValueSet(dcol, tmpArray, tmpData.length());            
              return;
              
            default: 
              assert false;
          }
          vValueSet(dcol, dval, destinationFormat);
        }
        break;
        
        case INTERVALYM:
        {
          long val1 = 0, val2 = 0, dval = 0;
          if (op != Op.TIM_SUB)
          {
            if (!src1.isAttrNull(col1))
              val1 = src1.vymValueGet(col1);
            if (src2 != null && !src2.isAttrNull(col2))
              val2 = src2.vymValueGet(col2);
          }
          
          // If there are two input parameters, then set destination interval 
          // format to default format with maximum precision
          // Else set destination format to single input parameter's format
          IntervalFormat destinationFormat = null;
          if(src2 != null || op ==  Op.TIM_SUB)
          {
            try
            {
              destinationFormat 
                = new IntervalFormat(TimeUnit.YEAR, TimeUnit.MONTH, 9, true);              
            } 
            catch (CEPException e)
            {
              // We shouldn't reach here because we are creating interval
              // format with valid parameters
              assert false;
            }
          }
          else
          {
            destinationFormat =  src1.vFormatGet(col1);
          }
          
          switch(op)
          {
            case ADD: //INTERVALYM_ADD
              dval = val1 + val2; 
              break;
            case SUB: //INTERVALYM_SUB
              dval = val1 - val2; 
              break;
            case TIM_SUB: //TIM_SUB
              val1 = src1.tValueGet(col1);
              val2 = src2.tValueGet(col2);
              dval = val1 - val2; 
              // Note: dval is in the unit of nanosecond as val1 and vol2
              // represents timestamp values
              // Convert it to number of months
              dval = dval / (30l*24l*3600l*1000000000l);
              break;
            case SUM_ADD: //INTERVALYM_SUM_ADD
              /*INT_SUM_ADD(x,y) = x + y if x is non null and y is non null
              x, if x is non null and y is null
              y, if x is null and y is non null
              null, if x and y are null */
              if (!src1.isAttrNull(col1) && !src2.isAttrNull(col2))
                 dval = val1 + val2;
              else if(!src1.isAttrNull(col1))
                dval = val1;
              else if(!src2.isAttrNull(col2))
                dval = val2;
              break;
              
            case SUM_SUB: //INTERVALYM_SUM_SUB
              /*INT_SUM_SUB(x,y) = x - y if x is non null and y is non null
              x, if x is non null and y is null
              y, if x is null and y is non null
              null, if x and y are null */
              if (!src1.isAttrNull(col1) && !src2.isAttrNull(col2))
                dval = val1 - val2;
              else if(!src1.isAttrNull(col1))
                dval = val1;
              else
                assert(false);
              break;
              
            case NVL:   //INTERVALYM_NVL
              dval = src1.isAttrNull(col1) ? val2 : val1;
              break;
              
            case CPY: //INTERVALYM_CPY
              dval = val1;
              break;
              
            case UMX:   //INTERVALYM_UMX
              if (src1.isAttrNull(col1))
              {
                dval = val2;
              } else if (src2.isAttrNull(col2))
              {
                dval = val1;
              } else
              {
                dval = (val1 < val2) ? val2 : val1;
              }
              break;

            case UMN:   //INTERVALYM_UMN
              if (src1.isAttrNull(col1))
              {
                dval = val2;
              } else if (src2.isAttrNull(col2))
              {
                dval = val1;
              } else
              {
                dval = (val1 > val2) ? val2 : val1;
              }
              break;
              
            case TO_CHR1: //INTERVALYM_TO_CHAR
              Scratch buf = scratchBuf.get();
              
              // Allocate and Cleaning temp Buffer
              StringBuilder tmpData = buf.charSeq;
              tmpData.delete(0, tmpData.length());
              
              char[] tmpArray = buf.charBuf;
              
              tmpData.append(
                  oracle.cep.common.IntervalConverter.getDSInterval(val1,
                      destinationFormat));
              //Read tmpData into tmpArray
              tmpData.getChars(0, tmpData.length(), tmpArray, 0);
              cValueSet(dcol, tmpArray, tmpData.length());            
              return;
              
            default: 
              assert false;
          }
          vymValueSet(dcol, dval, destinationFormat);
        }
        break;
      case XMLTYPE:
      {
        char[] dval = src1.xValueGet(col1);
        int dlen = src1.xLengthGet(col1);
        if(src1.xIsObj(col1))
          dlen = dval.length;
        switch (op)
        {
          case CPY: // XMLTYPE_CPY
            xValueSet(dcol, dval, dlen);
            break;
          
          case TO_CHR1: // XMLTYPE_TO_CHR
            cValueSet(dcol, dval, dlen);
            break;
            
          default:
            assert false;
        }
      }
        break;

      case OBJECT:
      {
        switch (op)
        {
          case CPY: // OBJ_CPY
            Object dval = src1.oValueGet(col1);
            oValueSet(dcol, dval);
            break;
          default:
            assert false;
        }
      }
      break;
        
      case BOOLEAN:
      {
        boolean val1 = false, val2 = false, dval = false;
        val1 = src1.boolValueGet(col1);
        switch (op)
        {
          case NVL: // BOOLEAN_NVL
            val2 = src1.boolValueGet(col2);
            dval = src1.isAttrNull(col1) ? val2 : val1;
            boolValueSet(dcol, dval);
            break;

          case CPY: // BOOLEAN_CPY
            dval = val1;
            boolValueSet(dcol, dval);
            break;

          default:
            assert false;
        }
      }
      break;
             
      default:
        assert false;
      break;
    }
  }

}