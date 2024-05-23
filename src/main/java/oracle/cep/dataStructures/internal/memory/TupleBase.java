/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/dataStructures/internal/memory/TupleBase.java hopark_cqlsnapshot/1 2016/02/26 10:21:33 hopark Exp $ */

/* Copyright (c) 2007, 2016, Oracle and/or its affiliates. 
All rights reserved.*/

/*
 DESCRIPTION
 This is the base class of tuple

 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>
 NOTES
 <other useful comments, qualifications, etc.>

 MODIFIED    (MM/DD/YY)
 udeshmuk  09/11/12 - add snapshotid
 sbishnoi  01/13/12 - improved timestamp support to include timezone
 sbishnoi  09/02/11 - support for interval format
 sbishnoi  08/29/11 - adding support for interval year to month
 udeshmuk  08/28/11 - add setter for id
 hopark    10/30/09 - support large string
 hopark    10/26/09 - support number type
 sborah    06/29/09 - support for bigdecimal
 hopark    05/18/09 - fix toString
 hopark    02/02/09 - objtype support
 hopark    05/16/08 - fix xmltype copy
 hopark    02/04/08 - fix object dump in tuple
 hopark    02/18/08 - add string functions
 sbishnoi  02/07/08 - Add linkedhashSet
 udeshmuk  01/30/08 - support for double data type.
 hopark    01/24/08 - support stack overflow from toString
 hopark    12/31/07 - support xmllog
 hopark    12/07/07 - cleanup spill
 hopark    11/27/07 - add BOOLEAN
 hopark    11/16/07 - xquery support
 hopark    09/06/07 - add evals
 hopark    07/19/07 - creation
*/

package oracle.cep.dataStructures.internal.memory;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;

import oracle.cep.common.Datatype;
import oracle.cep.dataStructures.external.AttributeValue;
import oracle.cep.dataStructures.external.TupleValue;
import oracle.cep.dataStructures.internal.ITuple;
import oracle.cep.dataStructures.internal.ITuplePtr;
import oracle.cep.exceptions.CEPException;
import oracle.cep.execution.ExecException;
import oracle.cep.execution.internals.Column;
import oracle.cep.execution.internals.TupleSpec;
import oracle.cep.logging.DumpDesc;
import oracle.cep.logging.IDumpContext;
import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;
import oracle.cep.snapshot.SnapshotContext;

/**
 * @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/dataStructures/internal/memory/TupleBase.java hopark_cqlsnapshot/1 2016/02/26 10:21:33 hopark Exp $
 * @author najain
 * @since release specific (what release of product did this appear in)
 */
@DumpDesc(attribTags={"Id", "Hashcode"}, 
    attribVals={"getId", "hashCode"})
public abstract class TupleBase implements ITuple
{
  private static final long serialVersionUID = 254510718065325615L;
      
  private static AtomicLong nextId = new AtomicLong();
  protected long          id;
  protected long snapshotId;

  /** true if tuple is Null */
  protected boolean     bNull;

  public static class Scratch
  {
    private static final int MAX_SCRATCH_LENGTH = 4096;
    public char[] charBuf;
    public byte[] byteBuf;
    public StringBuilder charSeq;
    public LinkedHashSet<Character> charLinkedHashSet;
    public Scratch()
    {
      charBuf = new char[MAX_SCRATCH_LENGTH];
      byteBuf = new byte[MAX_SCRATCH_LENGTH];
      charSeq = new StringBuilder(MAX_SCRATCH_LENGTH);
      charLinkedHashSet = new LinkedHashSet<Character>();
    }
    
    public char[] getCharBuf(int len)
    {
      if (charBuf.length < len)
        charBuf = new char[len];
      return charBuf;
    }

  }
  protected static ThreadLocal<Scratch> scratchBuf = new ThreadLocal<Scratch>() {
    protected synchronized Scratch initialValue() {
      return new Scratch();
    }
  };
  
  /**
   * Constructor for Tuple
   */
  public TupleBase()
  {
    super();
    bNull = false;
    id = nextId.incrementAndGet();
  }

  /**
   * Constructor for Tuple
   * 
   * @param max
   *          Maximum number of attributes
   */
  public TupleBase(int max)
  {
    super();
    bNull = false;
    id = nextId.incrementAndGet();
    snapshotId = Long.MAX_VALUE;
  } 
  
  public long getId()
  {
    return id;
  }
  
  public void setId(long newId)
  {
    this.id = newId;
  }
  
  public long getSnapshotId()
  {
    return snapshotId;
  }

  public void setSnapshotId(long newSnapshotId)
  {
    this.snapshotId = newSnapshotId;
  }
  /**
   * Getter for bNull in Tuple
   * 
   * @return Returns the bNull
   */
  public boolean isBNull()
  {
    return bNull;
  }

  /**
   * Setter for bNull in Tuple
   * 
   * @param null1
   *          The bNull to set.
   */
  public void setBNull(boolean null1)
  {
    bNull = null1;
  }

  protected ITuplePtr getRef()
  {
    // TODO
    return null;
  }

  public boolean equals(ITuple e)
  {
    return (getId() == e.getId());
  }

  public void copy(ITuple src) throws ExecException
  {
    assert src instanceof TupleBase;
    copy(src, getNumAttrs());
  }

  public void copy(ITuple src, int numAttrs) throws ExecException
  {
    for (int a = 0; a < numAttrs; a++)
    {
      if (src.isAttrNull(a))
        setAttrNull(a);
      else
      {
        switch (getAttrType(a).getKind())
        {
          case INT:
            iValueSet(a, src.iValueGet(a));
            break;
          case BIGINT:
            lValueSet(a, src.lValueGet(a));
            break;
          case FLOAT:
            fValueSet(a, src.fValueGet(a));
            break;
          case DOUBLE:
            dValueSet(a, src.dValueGet(a));
            break;
          case BIGDECIMAL:
            nValueSet(a, src.nValueGet(a), src.nPrecisionGet(a), src.nScaleGet(a));
            break;
          case BYTE:
            bValueSet(a, src.bValueGet(a), src.bLengthGet(a));
            break;
          case CHAR:
            cValueSet(a, src.cValueGet(a), src.cLengthGet(a));
            break;
          case TIMESTAMP:
            tValueSet(a, src.tValueGet(a));
            tFormatSet(a, src.tFormatGet(a));
            break;
          case OBJECT:
            oValueSet(a, src.oValueGet(a));
            break;
          case INTERVAL:            
            vValueSet(a, src.vValueGet(a), src.vFormatGet(a));
            break;
          case INTERVALYM:
            vymValueSet(a, src.vymValueGet(a), src.vFormatGet(a));
            break;
          case XMLTYPE:
            xValueSet(a, src.xValueGet(a), src.xLengthGet(a));
            break;
          case BOOLEAN:
            boolValueSet(a, src.boolValueGet(a));
            break;
          default:
            // Should not come
            assert false;
        }
      }
    }
  }

  public void copy(ITuple srcT, int[] srcPoss, int[] destPoss) throws ExecException
  {
    int numAttrs = srcPoss.length;
    assert (numAttrs == destPoss.length);
    
    // Get the attributes
    for (int a = 0; a < numAttrs; a++)
    {
      int src = srcPoss[a];
      int dest = destPoss[a];
      
      assert getAttrType(dest).getKind() == srcT.getAttrType(src).getKind();

      if (srcT.isAttrNull(src))
        setAttrNull(dest);
      else
      {
        switch (getAttrType(dest).getKind())
        {
        case INT:
          iValueSet(dest, srcT.iValueGet(src));
          break;
        case BIGINT:
          lValueSet(dest, srcT.lValueGet(src));
          break;
        case FLOAT:
          fValueSet(dest, srcT.fValueGet(src));
          break;
        case DOUBLE:
          dValueSet(dest, srcT.dValueGet(src));
          break;
        case BIGDECIMAL:
          nValueSet(dest, srcT.nValueGet(src), srcT.nPrecisionGet(src), srcT.nScaleGet(src));
          break;
        case BYTE:
          bValueSet(dest, srcT.bValueGet(src), srcT.bLengthGet(src));
          break;
        case CHAR:
          cValueSet(dest, srcT.cValueGet(src), srcT.cLengthGet(src));
          break;
        case TIMESTAMP:
          tValueSet(dest, srcT.tValueGet(src));
          tFormatSet(dest, srcT.tFormatGet(src));
          break;
        case OBJECT:
          oValueSet(dest, srcT.oValueGet(src));
          break;
        case INTERVAL:
          vValueSet(dest, srcT.vValueGet(src), srcT.vFormatGet(src));
          break;
        case INTERVALYM:
          vymValueSet(dest, srcT.vymValueGet(src), srcT.vFormatGet(src));
          break;
        case XMLTYPE:
          xValueSet(dest, srcT.xValueGet(src), srcT.xLengthGet(src));
          break;
        case BOOLEAN:
          boolValueSet(dest, srcT.boolValueGet(src));
          break;
        default:
          // Should not come
          assert false;
        }
      }
    }
  }
  
  public void copyTo(TupleValue s, int numAttrs, TupleSpec attrSpecs, Column inCols[]) 
    throws CEPException
  {
    // Set attributes
    for (int i = 0; i < numAttrs; i++)
    {
      boolean isNull = false;
      AttributeValue outAttr = s.getAttribute(i);
      isNull = isAttrNull(i);
      outAttr.setBNull(isNull);
      if (!isNull)
      {
        int pos = inCols[i].getColnum();
        switch (attrSpecs.getAttrType(i).getKind())
        {
          case INT:
            s.iValueSet(i, iValueGet(pos));
            break;
          case BIGINT:
            s.lValueSet(i, lValueGet(pos));
            break;
          case FLOAT:
            s.fValueSet(i, fValueGet(pos));
            break;
          case DOUBLE:
            s.dValueSet(i, dValueGet(pos));
            break;
          case BIGDECIMAL:
            s.nValueSet(i, nValueGet(pos), nPrecisionGet(pos), nScaleGet(pos));
            break; 
          case CHAR:
            s.cValueSet(i, cValueGet(pos));
            break;
          case BYTE:
            s.bValueSet(i, bValueGet(pos));
            break;
          case TIMESTAMP:
            s.tValueSet(i, tValueGet(pos));
            s.tFormatSet(i, tFormatGet(pos));
            break;
          case INTERVAL:
            s.vValueSet(i, vValueGet(pos), vFormatGet(pos));
            break;
          case INTERVALYM:
            s.vymValueSet(i, vymValueGet(pos), vFormatGet(pos));
            break;
          case XMLTYPE:
            s.xValueSet(i, xValueGet(pos));
            break;
          case BOOLEAN:
            s.boolValueSet(i, boolValueGet(pos));
            break;
          case OBJECT:
            s.oValueSet(i, oValueGet(pos));
            break;
          default:
            assert false;
        }
      }
    }
  }

  public void copyFrom(TupleValue s, int numAttrs, TupleSpec attrSpecs) 
    throws CEPException
  {
    // Set attributes
    for (int i = 0; i < numAttrs; i++)
    {
      boolean isNull = false;
      AttributeValue inpAttr = s.getAttribute(i);
      isNull = inpAttr.isBNull();
      if (isNull)
        setAttrNull(i);
      else
      {
        switch (attrSpecs.getAttrType(i).getKind())
        {
          case INT:
            iValueSet(i, s.iValueGet(i));
            break;
          case BIGINT:
            lValueSet(i, s.lValueGet(i));
            break;
          case FLOAT:
            fValueSet(i, s.fValueGet(i));
            break;
          case DOUBLE:
            dValueSet(i, s.dValueGet(i));
            break;
          case BIGDECIMAL:
            nValueSet(i, s.nValueGet(i), s.nPrecisionGet(i), s.nScaleGet(i));
            break; 
          case CHAR:
            cValueSet(i, s.cValueGet(i), s.cLengthGet(i));
            break;
          case BYTE:
            bValueSet(i, s.bValueGet(i), s.bLengthGet(i));
            break;
          case TIMESTAMP:
            tValueSet(i, s.tValueGet(i));
            tFormatSet(i, s.tFormatGet(i));
            break;
          case INTERVAL:
            vValueSet(i, s.intervalValGet(i), s.vFormatGet(i));
            break;
          case INTERVALYM:
            vymValueSet(i, s.intervalYMValGet(i), s.vFormatGet(i));
            break;
          case XMLTYPE:
            xValueSet(i, s.xValueGet(i), s.xLengthGet(i));
            break;
          case BOOLEAN:
            boolValueSet(i, s.boolValueGet(i));
            break;
          case OBJECT:
            oValueSet(i, s.oValueGet(i));
            break;
          default:
            assert false;
        }
      }
    }
  }

  public boolean compare(ITuple src) throws ExecException
  {
    int numAttrs = getNumAttrs();
    boolean same = true;

    // Get the attributes
    for (int a = 0; a < numAttrs; a++)
    {
      if (isAttrNull(a))
          same = src.isAttrNull(a);
      else
      {
          int ival;
          long lval;
          float fval;
          double dval;
          BigDecimal nVal;
          byte[] bval, bval2;
          char[] cval, cval2;
          Object oval, oval2;
          boolean boolval;
          switch (getAttrType(a).getKind())
          {
            case INT:
              ival = iValueGet(a);
              same = (ival == src.iValueGet(a));
              break;
            case BIGINT:
              lval = lValueGet(a);
              same = (lval == src.lValueGet(a));
              break;
            case FLOAT:
              fval = fValueGet(a);
              same = (fval == src.fValueGet(a));
              break;
            case DOUBLE:
              dval = dValueGet(a);
              same = (dval == src.dValueGet(a));
              break;
            case BIGDECIMAL:
              nVal = nValueGet(a);
              same = (nVal.compareTo(src.nValueGet(a)) == 0);
              break;
            case BYTE:
              ival = bLengthGet(a);
              same = (ival == src.bLengthGet(a));
              if (!same) break;
              bval = bValueGet(a);
              bval2 = src.bValueGet(a);
              for (int i = 0; i < ival; i++)
              {
                same = (bval[i] == bval2[i]);
                if (!same) break;
              }
              break;
            case CHAR:
              ival = cLengthGet(a);
              same = (ival == src.cLengthGet(a));
              if (!same) break;
              cval = cValueGet(a);
              cval2 = src.cValueGet(a);
              for (int i = 0; i < ival; i++)
              {
                same = (cval[i] == cval2[i]);
                if (!same) break;
              }
              break;
            case TIMESTAMP:
              //TODO: Ensure that timezone doesn't impact the comparison
              lval = tValueGet(a);
              same = (lval == src.tValueGet(a));
              break;
            case OBJECT:
              oval = oValueGet(a);
              oval2 = src.oValueGet(a);
              same = (oval == oval2);
              break;
            case INTERVAL:
              lval = vValueGet(a);
              same = (lval == src.vValueGet(a));
              break;
            case INTERVALYM:
              lval = vymValueGet(a);
              same = (lval == src.vymValueGet(a));
              break;
            case BOOLEAN:
              boolval = boolValueGet(a);
              same = (boolval == src.boolValueGet(a));
              break;
            default:
              // Should not come
              assert false;
        }
      }
      if (!same) break;
    }
    return same;
  }
  
  @Override
  public String toString()
  {
    return toString(this);
  }
  
  public static String toString(ITuple tuple)
  {
    StringBuffer buf = new StringBuffer();
    buf.append("Tuple(" + tuple.getId() + "," + tuple.hashCode() + ") '");
    try
    {
      String str;
      // Get the attributes
      for (int a = 0; a < tuple.getNumAttrs(); a++)
      {
        if (tuple.isAttrNull(a))
          buf.append("null");
        else
        {
          switch (tuple.getAttrType(a).getKind())
          {
            case INT:
              buf.append(tuple.iValueGet(a));
              break;
            case BIGINT:
              buf.append(tuple.lValueGet(a));
              break;
            case FLOAT:
              buf.append(tuple.fValueGet(a));
              break;
            case DOUBLE:
              buf.append(tuple.dValueGet(a));
              break;
            case BIGDECIMAL:
              buf.append(tuple.nValueGet(a) + "(" +tuple.nPrecisionGet(a) +","
                       + tuple.nScaleGet(a) + ")");
              break;
            case BYTE:
              buf.append("byte[" + tuple.bLengthGet(a) + "]");
              break;
            case CHAR:
            {
              char[] chars = tuple.cValueGet(a);
              if (chars == null)
                str = "null";
              else if(chars.length == 0)
                str = "empty";
              else
                str = new String(chars, 0, tuple.cLengthGet(a));
              buf.append("char[" + tuple.cLengthGet(a) + "]=" + str);
            }
              break;
            case TIMESTAMP:
              buf.append(tuple.tValueGet(a));
              break;
            case OBJECT:
              Object oval = tuple.oValueGet(a);
              buf.append("obj=");
              if (oval != null)
                buf.append(oval.getClass().getSimpleName() + oval.hashCode());
              else buf.append("null");
              break;
            case INTERVAL:
              buf.append(tuple.vValueGet(a));
              break;
            case INTERVALYM:
              buf.append(tuple.vymValueGet(a));
              break;
            case XMLTYPE:
            {
              char[] chars = tuple.xValueGet(a);
              if (chars.length == 0)
                str = "empty";
              else
                str = new String(chars, 0, tuple.xLengthGet(a));
              buf.append("xml[" + tuple.xLengthGet(a) + "]=" + str);
            }
              break;
            case BOOLEAN:
              buf.append(tuple.boolValueGet(a));
              break;
            default:
              // Should not come
              assert false;
          }
        }
        buf.append(",");
      }
      buf.append("'");
    }
    catch (ExecException e)
    {
      LogUtil.logStackTrace(LoggerType.TRACE, Level.SEVERE, e);
    }
    return buf.toString();
  }

  
  public static void dump(IDumpContext dumper, ITuple tuple) 
  {
    String tag = LogUtil.beginDumpObj(dumper, tuple);
    try
    {
      String str;
      // Get the attributes
      for (int a = 0; a < tuple.getNumAttrs(); a++)
      {
        Datatype attrType = tuple.getAttrType(a);
        if (tuple.isAttrNull(a))
          dumper.writeln(attrType.toString(), "null");
        else
        {
          switch (attrType.getKind())
          {
            case INT:
              dumper.writeln(attrType.toString(), Integer.toString(tuple.iValueGet(a)));
              break;
            case BIGINT:
              dumper.writeln(attrType.toString(), Long.toString(tuple.lValueGet(a)));
              break;
            case FLOAT:
              dumper.writeln(attrType.toString(), Float.toString(tuple.fValueGet(a)));
              break;
            case DOUBLE:
              dumper.writeln(attrType.toString(), Double.toString(tuple.dValueGet(a)));
              break;
            case BIGDECIMAL:
              dumper.writeln(attrType.toString(), tuple.nValueGet(a).toString());
              break;
            case BYTE:
              dumper.writeln(attrType.toString(), "byte[" + tuple.bLengthGet(a) + "]");
              break;
            case CHAR:
            {
              char[] chars = tuple.cValueGet(a);
              if (chars.length == 0)
                str = "empty";
              else
                str = new String(chars, 0, tuple.cLengthGet(a));
              dumper.writeln(attrType.toString(), str);
            }
              break;
            case XMLTYPE:
            {
              char[] chars = tuple.xValueGet(a);
              if(tuple.xIsObj(a))
              {
                // Object oval = tuple.oValueGet(a);
                dumper.writeln(attrType.toString(), 
                       chars.hashCode());
              }
              else
              {
                if (chars.length == 0)
                  str = "empty";
                else
                  str = new String(chars, 0, tuple.xLengthGet(a));
                dumper.writeln(attrType.toString(), str);
              }
            }
              break;
            case TIMESTAMP:
              dumper.writeln(attrType.toString(), Long.toString(tuple.tValueGet(a)));
              break;
            case OBJECT:
              Object oval = tuple.oValueGet(a);
              if (oval != null)
                dumper.writeln(attrType.toString(), 
                       oval.getClass().getSimpleName() + oval.hashCode());
              else 
                dumper.writeln(attrType.toString(), "null");
              break;
            case INTERVAL:
              dumper.writeln(attrType.toString(), Long.toString(tuple.vValueGet(a)));
              break;
            case INTERVALYM:
              dumper.writeln(attrType.toString(), Long.toString(tuple.vymValueGet(a)));
              break;
            case BOOLEAN:
              dumper.writeln(attrType.toString(), Boolean.toString(tuple.boolValueGet(a)));
              break;
            default:
              // Should not come
              assert false;
          }
        }
      }
    }
    catch (ExecException e)
    {
      LogUtil.logStackTrace(LoggerType.TRACE, Level.SEVERE, e);
    }
    LogUtil.endDumpObj(dumper, tag);
  }

  public synchronized void dump(IDumpContext dumper) 
  {
    dump(dumper, this);
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

  /**
   * Integer hash
   * 
   * @param key
   *          Integer to hash
   * @return Hash value
   */
  public static final int inthash(int key)
  {
    key += ~(key << 15);
    key ^= (key >> 10);
    key += (key << 3);
    key ^= (key >> 6);
    key += ~(key << 11);
    key ^= (key >> 16);
    return key;
  }

  /**
   * Long hash
   * 
   * @param key
   *          Long to hash
   * @return Hash value
   */
  public static final int longhash(long key)
  {
    key = (~key) + (key << 30); // key = (key << 30) - key - 1;
    key = key ^ (key >>> 27);
    key = key * 69; // key = (key + (key << 2)) + (key << 6);
    key = key ^ (key >>> 13);
    key = key * 41; // key = (key + (key << 3)) + (key << 5);
    key = key ^ (key >>> 26);
    return (int) key;
  }  

  /**
   * Return next tuple identifier
   * @return
   */
  public static long getNextTupleId()
  {
    return nextId.get();
  }
  
  /**
   * Setting next tuple identifier
   * @param id
   */
  public static void setNextTupleId(long id)
  {
    nextId.set(id);
  }
  
  public void writeExternal(ObjectOutput out) throws IOException
  {
    if(SnapshotContext.getVersion() < SnapshotContext.SOURCEOP_TUPID_VERSION)
      out.writeObject(nextId);
    out.writeLong(id);
    out.writeLong(snapshotId);
    out.writeBoolean(bNull);
  }
  
  public void readExternal(ObjectInput in) throws IOException,
      ClassNotFoundException
  {
    if(SnapshotContext.getVersion() < SnapshotContext.SOURCEOP_TUPID_VERSION)
      nextId = (AtomicLong) in.readObject();
    id = in.readLong();
    snapshotId = in.readLong();
    bNull = in.readBoolean();
  }  
}
