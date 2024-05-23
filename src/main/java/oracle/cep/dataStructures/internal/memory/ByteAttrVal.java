/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/dataStructures/internal/memory/ByteAttrVal.java hopark_cqlsnapshot/1 2016/02/26 10:21:33 hopark Exp $ */

/* Copyright (c) 2006, 2016, Oracle and/or its affiliates. 
All rights reserved.*/

/*
 DESCRIPTION
 Declares ByteAttrVal in package oracle.cep.execution.internals.
 
 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>
 
 NOTES
 <other useful comments, qualifications, etc.>
 
 MODIFIED    (MM/DD/YY)
 sborah    02/21/10 - correct setValue implementation
 hopark    02/05/08 - parameterized error
 hopark    09/04/07 - optimize
 najain    05/10/07 - variable length support
 najain    03/12/07 - bug fix
 najain    06/15/06 - add isNull 
 skaluska  03/28/06 - implementation
 skaluska  02/18/06 - add attrType 
 skaluska  02/14/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/dataStructures/internal/memory/ByteAttrVal.java hopark_cqlsnapshot/1 2016/02/26 10:21:33 hopark Exp $
 *  @author  najain
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.dataStructures.internal.memory;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import oracle.cep.common.Datatype;
import oracle.cep.exceptions.ExecutionError;
import oracle.cep.execution.ExecException;
import oracle.cep.execution.snapshot.IPersistenceContext;

/**
 * Internal representation of byte attribute value
 * 
 * @author najain
 */
public class ByteAttrVal extends AttrVal
{
  /** Attribute value */
  byte value[];

  /** Actual/Maximum Attribute length */
  int  length;
  private int  maxLen;

  /**
   * Empty Constructor for ByteAttrVal
   * Invoked while deserialization of instances of this type.
   */
  public ByteAttrVal()
  {
    super(Datatype.BYTE);
  }
  
  /**
   * Constructor for ByteAttrVal
   * 
   * @param v
   *          Attribute value
   */
  public ByteAttrVal(int maxLen)
  {
    super(Datatype.BYTE);
    value = null;
    length = 0;
    this.maxLen = maxLen;
  }

  /**
   * Getter for length in ByteAttrVal
   * 
   * @return Returns the length
   */
  public int getLength()
  {
    return length;
  }

  /**
   * Getter for value in ByteAttrVal
   * 
   * @return Returns the value
   */
  public byte[] getValue()
  {
    return value;
  }

  /**
   * Setter for value in ByteAttrVal
   * 
   * @param v
   *          The value to set.
   * @param l
   *          The length for value
   * @throws ExecException
   */
  public void setValue(byte[] v, int l) throws ExecException
  {
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
        value = new byte[l];
      for (int i = 0; i < l; i++)
        value[i] = v[i];
    }
    length = l;
  }

  /**
   * Setter for value in ByteAttrVal
   * 
   * @param v
   *          The value to set.
   * @throws ExecException
   */
  public void copy(ByteAttrVal v) throws ExecException
  {
    setValue(v.value, v.length);
  }

  public boolean isNull()
  {
    for (int i = 0; i < length; i++)
      if (value[i] != 0)
	return false;
    
    return true;
  }

  @Override
  public void writeExternal(ObjectOutput out)
      throws IOException
  {
    super.writeExternal(out);
    out.writeObject(this.value);
    out.writeInt(this.length);
    out.writeInt(this.maxLen);
  }

  @Override
  public void readExternal(ObjectInput in)
      throws IOException, ClassNotFoundException
  {
    super.readExternal(in);
    this.value = (byte[]) in.readObject();
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
