/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/dataStructures/internal/memory/XmltypeAttrVal.java hopark_cqlsnapshot/1 2016/02/26 10:21:33 hopark Exp $ */

/* Copyright (c) 2007, 2016, Oracle and/or its affiliates. 
All rights reserved.*/

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    hopark      05/15/08 - xValueGet from object type
    parujain    05/12/08 - getitem object
    hopark      03/05/08 - xml spill
    hopark      02/09/08 - add parseNode
    hopark      02/05/08 - parameterized error
    najain      02/04/08 - add object representation
    mthatte     12/26/07 - 
    najain      11/28/07 - 
    anasrini    11/28/07 - 
    najain      10/19/07 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/dataStructures/internal/memory/XmltypeAttrVal.java hopark_cqlsnapshot/1 2016/02/26 10:21:33 hopark Exp $
 *  @author  najain  
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.dataStructures.internal.memory;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Reader;
import java.io.StringReader;

import org.xml.sax.SAXException;

import oracle.cep.common.Datatype;
import oracle.cep.exceptions.ExecutionError;
import oracle.cep.execution.ExecException;
import oracle.cep.execution.snapshot.IPersistenceContext;
import oracle.cep.execution.xml.IXmlContext;
import oracle.cep.execution.xml.XMLItem;
import oracle.cep.common.Constants;
import oracle.xml.parser.v2.DOMParser;

/**
 * Internal representation of xmltype attribute value
 *
 * @author najain
 */
public class XmltypeAttrVal extends AttrVal
{
  boolean isObject;

  Object objVal;

  /** Attribute value */
  char value[];

  /** Actual Attribute length */
  /** Maximum attribute length */
  int  length;
  private int  maxLen;
  
  /**
   * Constructor for XmltypeAttrVal
   */
  public XmltypeAttrVal(int maxLen)
  {
    super(Datatype.XMLTYPE);
    value = null;
    length = 0;
    this.maxLen = maxLen;
    isObject = false;
  }

  /**
   * Constructor for XmltypeAttrVal
   */
  public XmltypeAttrVal()
  {
    this(Constants.MAX_XMLTYPE_LENGTH);
  }

  /**
   * Getter for length in XmltypeAttrVal
   * @return Returns the length
   */
  public int getLength()
  {
    assert isObject == false;
    return length;
  }

  /**
   * Getter for value in XmltypeAttrVal
   * @return Returns the value
   */
  public char[] getValue()
  {
    if (isObject)
    {
      assert (objVal != null);
      return objVal.toString().toCharArray();
    }
    return value;
  }

  /**
   * Setter for value in XmltypeAttrVal
   * @param v The value to set.
   * @param l The length for value
   * @throws ExecException 
   */
  public void setValue(char[] v, int l) throws ExecException
  {
    isObject = false;
    objVal = null;

    if (l > maxLen)
      throw new ExecException(ExecutionError.INVALID_ATTR, l, maxLen);
    // TODO: we can have better heuristics here, like doubling the value size
    // or something like that - good enough for now
    if (v == null)
    {
      assert (l == 0);
      setBNull(true);
    } 
    else
    {
      setBNull(false);
      if ((value == null) || (value.length < l))
        value = new char[l];
      for (int i = 0; i < l; i++)
        value[i] = v[i];
    }
    length = l;
  }

  public void setValue(Object o) throws ExecException
  {
    isObject = true;
    objVal = o;
  }

  public static Object parseNode(Object ctx, char[] val, int len)
    throws IOException, SAXException
  {
    XMLItem item = ((IXmlContext)ctx).createItem();

    DOMParser dom = new DOMParser();
    Reader reader = 
      new StringReader(new String(val, 0, len));
    dom.parse(reader);
    item.setNode(dom.getDocument());
    return item;
  }
  
  public Object getItem(Object ctx) throws Exception
  {
    if (isObject)
      return objVal;
    return parseNode(ctx, value, length);
  }

  /**
   * Setter for value in XmltypeAttrVal
   * @param v The value to set.
   * @throws ExecException 
   */
  public void copy(XmltypeAttrVal v) throws ExecException
  {
    if (v.isObject)
      setValue(v.objVal);
    else
      setValue(v.value, v.length);
  }

  @Override
  public void writeExternal(ObjectOutput out)
      throws IOException
  {
    super.writeExternal(out);
    out.writeBoolean(this.isObject);
    out.writeObject(this.objVal);
    out.writeObject(this.value);
    out.writeInt(this.length);
    out.writeInt(this.maxLen);
  }

  @Override
  public void readExternal(ObjectInput in)
      throws IOException, ClassNotFoundException
  {
    super.readExternal(in);
    this.isObject = in.readBoolean();
    this.objVal = in.readObject();
    this.value = (char[]) in.readObject();
    this.length = in.readInt();
    this.maxLen = in.readInt();
  }
  
  @Override
  public void writeExternal(ObjectOutput out, IPersistenceContext ctx)
      throws IOException
  {
    writeExternal(out);
  }

  @Override
  public void readExternal(ObjectInput in, IPersistenceContext ctx)
      throws IOException, ClassNotFoundException
  {
    readExternal(in);
  }
}
