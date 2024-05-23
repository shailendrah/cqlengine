/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/planmgr/codegen/ConstTupleSpec.java /main/25 2012/01/20 11:47:14 sbishnoi Exp $ */

/* Copyright (c) 2006, 2012, Oracle and/or its affiliates. 
All rights reserved. */

/*
 DESCRIPTION
 This class is used to organize the constants that will be referenced
 during run-time

 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>

 NOTES
 <other useful comments, qualifications, etc.>

 MODIFIED    (MM/DD/YY)
 sbishnoi    01/13/12 - improved timestamp support to include timezone
 sbishnoi    08/27/11 - adding support for interval year to month
 sborah      10/14/09 - support for bigdecimal
 sborah      06/22/09 - support for BigDecimal
 sborah      05/05/09 - set isNull flag in boolean case
 hopark      02/17/09 - support boolean as external datatype
 hopark      02/05/09 - objtype support
 sborah      12/16/08 - handle constants.
 hopark      10/10/08 - remove statics
 skmishra    06/06/08 - cleanup
 skmishra    05/15/08 - adding addXmltype()
 hopark      03/11/08 - xml spill
 hopark      02/05/08 - parameterized error
 udeshmuk    01/31/08 - support for double data type.
 udeshmuk    01/13/08 - add extra argument isNull in attr adding methods.
 najain      11/02/07 - xquery support
 hopark      10/22/07 - remove TimeStamp
 hopark      06/20/07 - cleanup
 najain      05/09/07 - variable length datatype support
 najain      03/14/07 - cleanup
 hopark      03/06/07 - use ITuplePtr
 hopark      11/16/06 - add bigint datatype
 parujain    10/09/06 - Interval datatype
 parujain    08/11/06 - cleanup planmgr
 parujain    08/10/06 - Timestamp datatype
 parujain    08/07/06 - timestamp datatype
 ayalaman    04/21/06 - get a sample tuple for a TupleSpecification 
 anasrini    03/14/06 - Creation
 anasrini    03/14/06 - Creation
 anasrini    03/14/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/planmgr/codegen/ConstTupleSpec.java /main/23 2009/11/09 10:10:58 sborah Exp $
 *  @author  anasrini
 *  @since   1.0
 */

package oracle.cep.planmgr.codegen;

import java.math.BigDecimal;
import java.util.ArrayList;

import oracle.cep.common.AttributeMetadata;
import oracle.cep.common.Constants;
import oracle.cep.common.Datatype;
import oracle.cep.common.IntervalFormat;
import oracle.cep.dataStructures.internal.ExpandableArray;
import oracle.cep.dataStructures.internal.ITuple;
import oracle.cep.dataStructures.internal.ITuplePtr;
import oracle.cep.dataStructures.internal.memory.AttrVal;
import oracle.cep.dataStructures.internal.memory.BigDecimalAttrVal;
import oracle.cep.dataStructures.internal.memory.BigintAttrVal;
import oracle.cep.dataStructures.internal.memory.BooleanAttrVal;
import oracle.cep.dataStructures.internal.memory.ByteAttrVal;
import oracle.cep.dataStructures.internal.memory.CharAttrVal;
import oracle.cep.dataStructures.internal.memory.DoubleAttrVal;
import oracle.cep.dataStructures.internal.memory.FloatAttrVal;
import oracle.cep.dataStructures.internal.memory.IntAttrVal;
import oracle.cep.dataStructures.internal.memory.IntervalAttrVal;
import oracle.cep.dataStructures.internal.memory.IntervalYMAttrVal;
import oracle.cep.dataStructures.internal.memory.ObjectAttrVal;
import oracle.cep.dataStructures.internal.memory.TimestampAttrVal;
import oracle.cep.dataStructures.internal.memory.XmltypeAttrVal;
import oracle.cep.exceptions.CEPException;
import oracle.cep.exceptions.CodeGenError;
import oracle.cep.execution.internals.TupleSpec;
import oracle.cep.execution.xml.IXmlContext;
import oracle.cep.execution.xml.XMLItem;
import oracle.cep.execution.xml.XmlManager;
import oracle.cep.memmgr.FactoryManager;
import oracle.cep.memmgr.IManagedObj;
import oracle.cep.memmgr.IPinnable;
import oracle.cep.service.ExecContext;

/**
 * This class is used to organize the constants that will be referenced
 * during run-time
 *
 * @since 1.0
 */

class ConstTupleSpec
{

  private ArrayList<AttrVal> val;
  private int                numAttrs;
  private TupleSpec          ts;

  ConstTupleSpec(FactoryManager factoryMgr)
  {
    val      = new ExpandableArray<AttrVal>(Constants.INITIAL_ATTRS_NUMBER);
    numAttrs = 0;
    ts       = new TupleSpec(factoryMgr.getNextId());
  }

  int addInt(int value, boolean isNull) throws CEPException
  {
    int pos = numAttrs;

    val.add(new IntAttrVal(value));
    val.get(numAttrs).setBNull(isNull);
    numAttrs++;

    ts.addAttr(Datatype.INT);

    return pos;
  }

  int addInt(int value) throws CEPException
  {
    return addInt(value, false);
  }

  int addBigint(long value, boolean isNull) throws CEPException
  {
    int pos = numAttrs;

    val.add(new BigintAttrVal(value));
    val.get(numAttrs).setBNull(isNull);
    numAttrs++;

    ts.addAttr(Datatype.BIGINT);

    return pos;
  }

  int addBigint(long value) throws CEPException
  {
    return addBigint(value, false);
  }

  int addFloat(float value, boolean isNull) throws CEPException
  {
    int pos = numAttrs;

    val.add(new FloatAttrVal(value));
    val.get(numAttrs).setBNull(isNull);
    numAttrs++;

    ts.addAttr(Datatype.FLOAT);

    return pos;
  }

  int addFloat(float value) throws CEPException
  {
    return addFloat(value, false);
  }

  int addDouble(double value, boolean isNull) throws CEPException
  {
    int pos = numAttrs;

    val.add(new DoubleAttrVal(value));
    val.get(numAttrs).setBNull(isNull);
    numAttrs++;

    ts.addAttr(Datatype.DOUBLE);

    return pos;
  }

  int addDouble(double value) throws CEPException
  {
    return addDouble(value, false);
  }
  
  int addBigDecimal(BigDecimal value, boolean isNull) throws CEPException
  {
    int pos = numAttrs;

    val.add(new BigDecimalAttrVal(value));
    val.get(numAttrs).setBNull(isNull);
    numAttrs++;

    ts.addAttr(Datatype.BIGDECIMAL);

    return pos;
  }

  int addBigDecimal(BigDecimal value) throws CEPException
  {
    return addBigDecimal(value, false);
  }

  int addTimeStamp(long value, boolean isNull) throws CEPException
  {
    int pos = numAttrs;

    val.add(new TimestampAttrVal(value));
    val.get(numAttrs).setBNull(isNull);
    numAttrs++;

    ts.addAttr(Datatype.TIMESTAMP);

    return pos;

  }

  int addTimeStamp(long value) throws CEPException
  {
    return addTimeStamp(value, false);
  }

  int addChar(char[] value, boolean isNull) throws CEPException
  {
    int pos = numAttrs;

    val.add(new CharAttrVal(value.length));
    val.get(numAttrs).cValueSet(value, value.length);
    val.get(numAttrs).setBNull(isNull);
    numAttrs++;

    ts.addAttr(new AttributeMetadata(Datatype.CHAR, value.length, 0, 0));

    return pos;
  }

  int addChar(char[] value) throws CEPException
  {
    return addChar(value, false);
  }

  int addByte(byte[] value, boolean isNull) throws CEPException
  {
    int pos = numAttrs;

    val.add(new ByteAttrVal(value.length));
    val.get(numAttrs).bValueSet(value, value.length);
    val.get(numAttrs).setBNull(isNull);
    numAttrs++;

    ts.addAttr(new AttributeMetadata(Datatype.BYTE, value.length, 0, 0));

    return pos;
  }

  int addByte(byte[] value) throws CEPException
  {
    return addByte(value, false);
  }

  int addInterval(long value, boolean isNull, boolean isYearToMonth,
                  IntervalFormat format) 
    throws CEPException
  {
    int pos = numAttrs;
    if(isYearToMonth)
    {
      val.add(new IntervalYMAttrVal(value, format));
      ts.addAttr(Datatype.INTERVALYM);
    }
    else
    {
      val.add(new IntervalAttrVal(value, format));
      ts.addAttr(Datatype.INTERVAL);
    }
    val.get(numAttrs).setBNull(isNull);
    numAttrs++;
    return pos;
  }

  int addInterval(long value, boolean isYearToMonth, IntervalFormat format) throws CEPException
  {
    return addInterval(value, false, isYearToMonth, format);
  }

  int addObject(Object value, boolean isNull) throws CEPException
  {
    int pos = numAttrs;

    val.add(new ObjectAttrVal(value));
    val.get(numAttrs).setBNull(isNull);
    numAttrs++;

    if (value instanceof IManagedObj)
      ts.addManagedObj(Datatype.OBJECT);
    else
      ts.addAttr(Datatype.OBJECT);

    return pos;
  }

  int addXmltype(XMLItem node)
      throws CEPException
  {
    int pos = numAttrs;
   
    val.add( new XmltypeAttrVal());
    ((XmltypeAttrVal)val.get(numAttrs)).setValue(node);
    numAttrs++;

    ts.addAttr(Datatype.XMLTYPE);
    return pos;
  }

  int addBoolean(boolean value, boolean isNull) throws CEPException
  {
    int pos = numAttrs;
    
    val.add(new BooleanAttrVal(value));
    val.get(numAttrs).setBNull(isNull);
    numAttrs++;

    ts.addAttr(Datatype.BOOLEAN);
    return pos;
  }

  int addObject(Object value) throws CEPException
  {
    return addObject(value, false);
  }

  TupleSpec getTupleSpec() throws CEPException
  {
    return ts;
  }

  /**
   * Get the number of registered attributes
   * @return the number of registered attributes
   */
  public int getNumAttrs()
  {
    return numAttrs;
  }

  void populateTuple(ExecContext ec, ITuplePtr tPtr) throws CEPException
  {
    AttrVal av;
    Datatype dt;

    ITuple t = tPtr.pinTuple(IPinnable.WRITE);
    for (int i = 0; i < numAttrs; i++)
    {
      av = val.get(i);
      dt = av.getAttrType();
      switch (dt.getKind())
      {
      case INT:
        t.iValueSet(i, av.iValueGet());
        break;
      case BIGINT:
        t.lValueSet(i, av.lValueGet());
        break;
      case FLOAT:
        t.fValueSet(i, av.fValueGet());
        break;
      case DOUBLE:
        t.dValueSet(i, av.dValueGet());
        break;
      case BIGDECIMAL:
        t.nValueSet(i, av.nValueGet(), av.nPrecisionGet(), av.nScaleGet());
        break;
      case CHAR:
        t.cValueSet(i, av.cValueGet(), av.cLengthGet());
        break;
      case BYTE:
        t.bValueSet(i, av.bValueGet(), av.bLengthGet());
        break;
      case TIMESTAMP:
        t.tValueSet(i, av.tValueGet());
        t.tFormatSet(i, av.tFormatGet());
        break;
      case INTERVAL:
        t.vValueSet(i, av.vValueGet(), av.vFormatGet());
        break;
      case INTERVALYM:
        t.vymValueSet(i, av.vymValueGet(), av.vFormatGet());
        break;
      case OBJECT:
        t.oValueSet(i, av.oValueGet());
        break;
      case BOOLEAN:
        t.boolValueSet(i, av.boolValueGet());
        break;
      case XMLTYPE:
        try
        {
          XmlManager xmlMgr = ec.getXmlMgr();
          IXmlContext ctx = xmlMgr.createContext();
          t.xValueSet(i, av.getItem(ctx));
        }
        catch(Exception e)
        {
          throw new CEPException(CodeGenError.XML_ARG_ERROR);
        }
        break;
      default:
        assert false : dt;
      }
      if (av.isBNull())
        t.setAttrNull(i);
    }
    tPtr.unpinTuple();
  }

}
