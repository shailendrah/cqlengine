/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/interfaces/TupleValueHelper.java /main/32 2012/01/20 11:47:14 sbishnoi Exp $ */

/* Copyright (c) 2006, 2012, Oracle and/or its affiliates. 
All rights reserved. */

/*
 DESCRIPTION
 Helper class to convert between TupleValue and other external formats
 such as XML

 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>

 NOTES
 <other useful comments, qualifications, etc.>

 MODIFIED    (MM/DD/YY)
 sbishnoi    11/07/11 - modified CEPDAteFormat API
 sbishnoi    10/03/11 - changing format to intervalformat
 sbishnoi    08/27/11 - adding support for interval year to month
 sborah      06/29/09 - support for bigdecimal
 hopark      03/06/09 - add opaque type
 hopark      02/17/09 - support boolean as external datatype
 hopark      11/28/08 - use CEPDateFormat
 skmishra    10/30/08 - bug-7250555
 sbishnoi    10/21/08 - removing prefix namespace
 sbishnoi    09/18/08 - testing with EDN
 skmishra    08/07/08 - bug 7250555
 mthatte     06/25/08 - bug 6880179
 sbishnoi    08/01/08 - support for nanosecond
 sbishnoi    02/11/08 - error parameterization
 udeshmuk    01/30/08 - support for double data type.
 udeshmuk    01/25/08 - create a static Timestamp object instead of recreating it
 every time.
 udeshmuk    01/17/08 - change in the data type of timestamp of TupleValue.
 najain      10/25/07 - xmltype support
 udeshmuk    11/23/07 - allow coverting of xml to tuple with or without ts.
 hopark      10/30/07 - remove IQueueElement
 dlenkov     10/10/07 - 
 mthatte     09/07/07 - Using universal timestamp format
 mthatte     09/05/07 - Using TupleKind
 dlenkov     07/30/07 - fixed the outer tag handling
 hopark      07/13/07 - dump stack trace on exception
 parujain    06/08/07 - byte support and bug fix
 parujain    05/23/07 - throw InterfaceException
 hopark      05/16/07 - remove printStackTrace
 najain      03/12/07 - bug fix
 dlenkov     12/08/06 - byte array length fix
 hopark      11/16/06 - add bigint datatype
 najain      11/07/06 - add convertXmlToTimestamp
 parujain    10/30/06 - Push source for interval/timestamp
 dlenkov     10/27/06 - byte data type support
 anasrini    10/24/06 - convert TupleValue to XML
 anasrini    09/14/06 - change date format
 anasrini    09/11/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/interfaces/TupleValueHelper.java /main/30 2009/11/09 10:10:58 sborah Exp $
 *  @author  anasrini
 *  @since   1.0
 */

package oracle.cep.interfaces;

import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.BitSet;
import java.util.logging.Level;

import oracle.cep.common.AttributeMetadata;
import oracle.cep.common.CEPDate;
import oracle.cep.common.CEPDateFormat;
import oracle.cep.common.Datatype;
import oracle.cep.common.IntervalFormat;
import oracle.cep.common.TimestampFormat;
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
import oracle.cep.dataStructures.external.TimestampAttributeValue;
import oracle.cep.dataStructures.external.TupleKind;
import oracle.cep.dataStructures.external.TupleValue;
import oracle.cep.dataStructures.external.XmltypeAttributeValue;
import oracle.cep.dataStructures.internal.QueueElement;
import oracle.cep.exceptions.CEPException;
import oracle.cep.exceptions.InterfaceError;
import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;
import oracle.xml.parser.v2.DOMParser;
import oracle.xml.parser.v2.XMLDocument;
import oracle.xml.parser.v2.XMLNode;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Helper class to convert between TupleValue and other external formats
 * such as XML
 *
 * @since 1.0
 */

public class TupleValueHelper
{
  /** Format to be used for the timestamps */
  private static NumberFormat     nf;
  
  /** Strings for element kind */
  private static final String     ce                     = "ComplexEvent";
  private static final String     ts                     = "Timestamp";
  private static final String     kind                   = "ElementKind";
  private static final String     plus                   = TupleKind.PLUS
                                                             .toString();
  private static final String     minus                  = TupleKind.MINUS
                                                             .toString();
  private static final String     heartBeat              = TupleKind.HEARTBEAT
                                                             .toString();

  private static final String     OPEN_ROOT_ELEMENT      = "<" + ce + ">";
  private static final String     OPEN_TS_ELEMENT        = "<" + ts + ">";
  private static final String     OPEN_KIND_ELEMENT      = "<" + kind + ">";
  private static final String     CLOSE_ROOT_ELEMENT     = "</" + ce + ">";
  private static final String     CLOSE_TS_ELEMENT       = "</" + ts + ">";
  private static final String     CLOSE_KIND_ELEMENT     = "</" + kind + ">";

  /** Element Tags having namespaces */
  private static final String     OPEN_TS_ELEMENT_NSP    = "<cep:" + ts + ">";
  private static final String     OPEN_KIND_ELEMENT_NSP  = "<cep:" + kind + ">";
  private static final String     CLOSE_ROOT_ELEMENT_NSP = "</cep:" + ce + ">";
  private static final String     CLOSE_TS_ELEMENT_NSP   = "</cep:" + ts + ">";
  private static final String     CLOSE_KIND_ELEMENT_NSP = "</cep:" + kind
                                                             + ">";

  static
  {
    nf = NumberFormat.getNumberInstance();
  }

  public static void convertXmlToTupleValue(String data, TupleValue tuple,
      int numAttrs, AttributeMetadata[] attrMetadata, String[] names, BitSet attrsList,
      boolean isSystemTimestamped) throws InterfaceException
  {

    try
    {
      // Create the document from the source
      DOMParser parser = new DOMParser();

      Reader reader = new StringReader(data);
      parser.parse(reader);

      XMLDocument xmlDoc = parser.getDocument();

      tuple.setObjectName(null); // string name is not needed/used by anyone

      NodeList list = xmlDoc.getChildNodes();
      if ((list == null) || (list.getLength() != 1))
        throw new InterfaceException(InterfaceError.COMPLEX_EVENT_NOT_FOUND);

      Element root = (Element) list.item(0);

      list = root.getChildNodes();
      if (list == null)
        throw new InterfaceException(InterfaceError.COMPLEX_EVENT_NOT_FOUND);

      attrsList.clear();
      boolean tsFound = false;
      boolean elemKindFound = false;

      for (int i = 0; i < list.getLength(); i++)
      {
        Node n = list.item(i);
        if (n.getNodeType() != Node.ELEMENT_NODE)
          continue;
        Element elem = (Element) n;
        String name = elem.getNodeName();
        Node txt = elem.getFirstChild();
        String value = txt.getNodeValue();
        if (value != null)
          value = value.trim();

        if (name.equalsIgnoreCase(ts))
        {
          if (tsFound)
            throw new InterfaceException(InterfaceError.DUPLICATE_TIMESTAMP);

          /** Date specific to the timestamp */
          CEPDateFormat sdf = CEPDateFormat.getInstance();
          CEPDate date = sdf.parse(value);

          // Set tuple time in nanosecond time unit
          tuple.setTime(date.getValue());
          
          tsFound = true;
        } else if (name.equalsIgnoreCase(kind))
        {
          if (elemKindFound)
            throw new InterfaceException(InterfaceError.DUPLICATE_ELEMENT);

          tuple.setBHeartBeat(false);
          if (value.equalsIgnoreCase(plus))
            tuple.setKind(TupleKind.PLUS);
          else if (value.equalsIgnoreCase(minus))
            tuple.setKind(TupleKind.MINUS);
          else if (value.equalsIgnoreCase(heartBeat))
          {
            tuple.setKind(TupleKind.HEARTBEAT);
            tuple.setBHeartBeat(true);
            return;
          } else
            throw new InterfaceException(InterfaceError.INVALID_ELEMENT_KIND,
                value);
          elemKindFound = true;
        } else
        // attribute
        {
          int attrId = getAttrId(names, numAttrs, name);

          // xmltype treated specially
          if (value == null && attrMetadata[attrId].getDatatype() != Datatype.XMLTYPE)
            tuple.getAttribute(attrId).setBNull(true);
          
          else
          {
            if (attrMetadata[attrId].getDatatype() == Datatype.XMLTYPE)
            {
              System.out.println("Entering XMLTYPE case");
              // Iterate over the children, ie the xmltype attr.
              Node child = elem.getFirstChild();
              Node nextChild;
              XMLNode docFrag = (XMLNode) xmlDoc.createDocumentFragment();

              while (child != null)
              {
                nextChild = child.getNextSibling();
                docFrag.appendChild(child);
                child = nextChild;
              }

              StringWriter str = new StringWriter();
              PrintWriter pw = new PrintWriter(str);
              docFrag.print(pw);
              value = str.getBuffer().toString();
              System.out.println("XML Value recd. : " + value);
              if (value != null)
              {
                str = null;
                pw = null;

                AttributeValue xAttr = tuple.getAttribute(attrId);
                assert xAttr instanceof XmltypeAttributeValue;
                xAttr.xValueSet(value.toCharArray());
                xAttr.xLengthSet(value.length());
                tuple.getAttribute(attrId).setBNull(false);
              } else
              {
                tuple.getAttribute(attrId).setBNull(true);
              }
            }

            else
            {
              IntervalFormat fmt = null;
              switch (attrMetadata[attrId].getDatatype().getKind())
              {
              case INT:
                int iValue = -1;
                synchronized (nf)
                {
                  if (value != null)
                    iValue = nf.parse(value).intValue();
                }
                assert tuple.getAttribute(attrId) instanceof IntAttributeValue;
                tuple.getAttribute(attrId).iValueSet(iValue);
                break;
              case BIGINT:
                long lValue = -1;
                synchronized (nf)
                {
                  if (value != null)
                    lValue = nf.parse(value).longValue();
                }
                assert tuple.getAttribute(attrId) instanceof BigintAttributeValue;
                tuple.getAttribute(attrId).lValueSet(lValue);
                break;
              case FLOAT:
                float fValue;
                synchronized (nf)
                {
                  fValue = nf.parse(value).floatValue();
                }
                assert tuple.getAttribute(attrId) instanceof FloatAttributeValue;
                tuple.getAttribute(attrId).fValueSet(fValue);
                break;
              case DOUBLE:
                double dValue;
                synchronized (nf)
                {
                  dValue = nf.parse(value).doubleValue();
                }
                assert tuple.getAttribute(attrId) instanceof DoubleAttributeValue;
                tuple.getAttribute(attrId).dValueSet(dValue);
                break;
              case BIGDECIMAL:
                assert tuple.getAttribute(attrId) instanceof BigDecimalAttributeValue;
                tuple.getAttribute(attrId).nValueSet(new BigDecimal(value),
                    attrMetadata[attrId].getPrecision(),
                    attrMetadata[attrId].getScale());
                break;
                
              case CHAR:
                AttributeValue cAttr = tuple.getAttribute(attrId);
                assert cAttr instanceof CharAttributeValue;
                cAttr.cValueSet(value.toCharArray());
                cAttr.cLengthSet(value.length());
                break;

              case BYTE:
                AttributeValue bAttr = tuple.getAttribute(attrId);
                assert bAttr instanceof ByteAttributeValue;
                byte[] bytes = Datatype.hexToByte(value.toCharArray());
                bAttr.bValueSet(bytes);
                bAttr.bLengthSet(bytes.length);
                break;
              case TIMESTAMP:                
                assert tuple.getAttribute(attrId) instanceof TimestampAttributeValue;
                TimestampFormat format = attrMetadata[attrId].getTimestampFormat();
                
                // Get the date/timestamp format parser
                CEPDateFormat dateFormatParser = CEPDateFormat.getInstance();
                CEPDate date = dateFormatParser.parse(value, format);

                tuple.getAttribute(attrId).tValueSet(date.getValue());
                tuple.getAttribute(attrId).tFormatSet(date.getFormat());
                break;
              case INTERVAL:
                assert tuple.getAttribute(attrId) instanceof IntervalAttributeValue;
                fmt = attrMetadata[attrId].getIntervalFormat();
                tuple.getAttribute(attrId).vValueSet(value, fmt);
                break;
              case INTERVALYM:
                assert tuple.getAttribute(attrId) instanceof IntervalYMAttributeValue;
                fmt = attrMetadata[attrId].getIntervalFormat();
                tuple.getAttribute(attrId).vymValueSet(value, fmt);
                break;
              case BOOLEAN:
                try
                {
                  boolean bValue = Datatype.strToBoolean(value);
                  assert tuple.getAttribute(attrId) instanceof BooleanAttributeValue;
                  tuple.getAttribute(attrId).boolValueSet(bValue);
                }
                catch(NumberFormatException e)
                {
                  throw new InterfaceException(InterfaceError.INVALID_BOOLEAN_FORMAT,
                      new Object[]{value});
                }
                break;
            default:
                assert false;
              }
            }
            tuple.getAttribute(attrId).setBNull(false);
            attrsList.set(attrId);
          }
        }
      }

      // The remaining attributes are null
      int attrId = 0;
      attrId = attrsList.nextClearBit(attrId);
      while (attrId < numAttrs)
      {
        tuple.getAttribute(attrId).setBNull(true);
        attrId++;
        attrId = attrsList.nextClearBit(attrId);
      }

      if (!tsFound && !isSystemTimestamped)
        throw new InterfaceException(InterfaceError.TIMESTAMP_NOT_FOUND);
      else if (!elemKindFound)
        throw new InterfaceException(InterfaceError.ELEMENT_KIND_NOT_FOUND);
    } catch (InterfaceException e)
    {
      LogUtil.logStackTrace(LoggerType.TRACE, Level.FINE, e);
      throw e;
    } catch (Exception e)
    {
      LogUtil.logStackTrace(LoggerType.TRACE, Level.FINE, e);
      throw new InterfaceException(InterfaceError.XML_FORMAT_ERROR, e);
    }
  }

  public static String convertTupleValueToXML(TupleValue tuple,
      QueueElement.Kind k, int numAttrs, Datatype[] dty, String[] names)
  {

    StringBuilder sb = new StringBuilder();
    String ek = "";
    String val = "";
    long ts = tuple.getTime();

    try
    {
      // Root Element
      sb.append(OPEN_ROOT_ELEMENT);

      // Write the timestamp
      CEPDateFormat sdf = CEPDateFormat.getInstance();;
      sb.append(OPEN_TS_ELEMENT + sdf.format(ts) + CLOSE_TS_ELEMENT);

      // IQueueElement.Kind
      switch (k)
      {
      case E_PLUS:
        ek = plus;
        break;
      case E_MINUS:
        ek = minus;
        break;
      case E_HEARTBEAT:
        ek = heartBeat;
        break;
      default:
        break;
      }
      sb.append(OPEN_KIND_ELEMENT + ek + CLOSE_KIND_ELEMENT);

      if (k != QueueElement.Kind.E_HEARTBEAT)
      {

        // Attributes
        AttributeValue att = null;
        for (int i = 0; i < numAttrs; i++)
        {
          val = null;

          att = tuple.getAttribute(i);
          if (att.isBNull())
          {
            sb.append("<" + names[i] + " isNull=\"true\" />");
            continue;
          }

          sb.append("<" + names[i] + ">");
          switch (dty[i].getKind())
          {
          case INT:
            sb.append(tuple.iValueGet(i));
            break;
          case BIGINT:
            val = String.valueOf(tuple.lValueGet(i));
            break;
          case FLOAT:
            sb.append(tuple.fValueGet(i));
            break;
          case DOUBLE:
            sb.append(tuple.dValueGet(i));
            break;
          case BIGDECIMAL:
            sb.append(tuple.nValueGet(i));
            break;
          case CHAR:
            sb.append(tuple.cValueGet(i), 0, tuple.cLengthGet(i));
            break;
          case XMLTYPE:
            sb.append(tuple.xValueGet(i), 0, tuple.xLengthGet(i));
            break;
          case BYTE:
            char[] inp = Datatype.byteToHex(tuple.bValueGet(i), tuple
                .bLengthGet(i));
            sb.append(inp);
            break;
          case TIMESTAMP:
            val = sdf.format(tuple.tValueGet(i), tuple.tFormatGet(i));
            break;
          case INTERVAL:
            val = String.valueOf(tuple.vValueGet(i));
            break;
          case INTERVALYM:
            val = String.valueOf(tuple.vymValueGet(i));
            break;
          case BOOLEAN:
            sb.append(tuple.boolValueGet(i));
            break;
          default:
            assert false : dty[i];
          }

          if (val != null)
            sb.append(val + "</" + names[i] + ">");
          else
            sb.append("</" + names[i] + ">");
        }
      }

      // Close
      sb.append(CLOSE_ROOT_ELEMENT);
    } catch (CEPException e)
    {
      LogUtil.logStackTrace(LoggerType.TRACE, Level.FINE, e);
      // should never come here, else system is inconsistent
      assert false;
    }

    return sb.toString();
  }

  public static String convertTValueToNspXML(TupleValue tuple,
      QueueElement.Kind k, String nsp, int numAttrs, 
      AttributeMetadata[] attributeMetadata,
      String[] names, boolean isNewEPRFormat)
  {

    StringBuilder sb = new StringBuilder();
    String ek = "";
    String val = "";
    long ts = tuple.getTime();

    try
    {
      // Root Element
      sb.append("<cep:" + ce + " xmlns:cep=\"" + nsp + "\">");

      // Write the timestamp
      CEPDateFormat sdf = CEPDateFormat.getInstance();
      if (isNewEPRFormat)
        sb.append(OPEN_TS_ELEMENT_NSP + sdf.format(ts)
            + CLOSE_TS_ELEMENT_NSP);
      else
        sb.append(OPEN_TS_ELEMENT + sdf.format(ts) + CLOSE_TS_ELEMENT);

      // IQueueElement.Kind
      switch (k)
      {
      case E_PLUS:
        ek = plus;
        break;
      case E_MINUS:
        ek = minus;
        break;
      case E_HEARTBEAT:
        ek = heartBeat;
        break;
      default:
        break;
      }
      if (isNewEPRFormat)
        sb.append(OPEN_KIND_ELEMENT_NSP + ek + CLOSE_KIND_ELEMENT_NSP);
      else
        sb.append(OPEN_KIND_ELEMENT + ek + CLOSE_KIND_ELEMENT);

      if (k != QueueElement.Kind.E_HEARTBEAT)
      {
        // Attributes
        AttributeValue att = null;
        for (int i = 0; i < numAttrs; i++)
        {
          val = null;
          att = tuple.getAttribute(i);
          if (att.isBNull())
          {
            if (isNewEPRFormat)
              sb.append("<cep:" + names[i] + " isNull=\"true\" />");
            else
              sb.append("<" + names[i] + " isNull=\"true\" />");
            continue;
          }

          if (isNewEPRFormat)
            sb.append("<cep:" + names[i] + ">");
          else
            sb.append("<" + names[i] + ">");

          switch (attributeMetadata[i].getDatatype().getKind())
          {
          case INT:
            sb.append(tuple.iValueGet(i));
            break;
          case BIGINT:
            val = String.valueOf(tuple.lValueGet(i));
            break;
          case FLOAT:
            sb.append(tuple.fValueGet(i));
            break;
          case DOUBLE:
            sb.append(tuple.dValueGet(i));
            break;
          case BIGDECIMAL:
            sb.append(tuple.nValueGet(i));
            break;
          case CHAR:
            sb.append(tuple.cValueGet(i), 0, tuple.cLengthGet(i));
            break;
          case XMLTYPE:
            sb.append(tuple.xValueGet(i), 0, tuple.xLengthGet(i));
            break;
          case BYTE:
            char[] inp = Datatype.byteToHex(tuple.bValueGet(i), tuple
                .bLengthGet(i));
            sb.append(inp);
            break;
          case TIMESTAMP:
            val = sdf.format(tuple.tValueGet(i), tuple.tFormatGet(i));
            break;
          case INTERVAL:
            val = String.valueOf(tuple.vValueGet(i));
            break;
          case INTERVALYM:
            val = String.valueOf(tuple.vymValueGet(i));
            break;
          case BOOLEAN:
            sb.append(tuple.boolValueGet(i));
            break;
          default:
            assert false : attributeMetadata[i].getDatatype();
          }

          if (val != null)
            sb.append(val);

          if (isNewEPRFormat)
            sb.append("</cep:" + names[i] + ">");
          else
            sb.append("</" + names[i] + ">");
        }
      }

      // Close
      sb.append(CLOSE_ROOT_ELEMENT_NSP);
    } catch (CEPException e)
    {
      LogUtil.logStackTrace(LoggerType.TRACE, Level.FINE, e);
      // should never come here, else system is inconsistent
      assert false;
    }

    return sb.toString();
  }

  private static int getAttrId(String[] names, int numAttrs, String name)
      throws InterfaceException
  {
    for (int i = 0; i < numAttrs; i++)
      if (name.equalsIgnoreCase(names[i]))
        return i;
    throw new InterfaceException(InterfaceError.INVALID_NAME, name);
  }
}
