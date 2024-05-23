/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/dataStructures/internal/memory/CharAttrVal.java hopark_cqlsnapshot/1 2016/02/26 10:21:33 hopark Exp $ */

/* Copyright (c) 2006, 2016, Oracle and/or its affiliates. 
All rights reserved.*/

/*
 DESCRIPTION
 Declares CharAttrVal in package oracle.cep.execution.internals.
 
 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>
 
 NOTES
 <other useful comments, qualifications, etc.>
 
 MODIFIED    (MM/DD/YY)
 sborah    02/16/10 - correct setValue implementation
 hopark    02/05/08 - parameterized error
 hopark    09/04/07 - optimize
 hopark    05/15/07 - fix null string support
 najain    05/09/07 - variable length datatype
 najain    03/12/07 - bug fix
 skaluska  03/28/06 - implementation
 anasrini  03/14/06 - make constructor public 
 skaluska  02/18/06 - add attrType 
 skaluska  02/14/06 - Creation
 skaluska  02/14/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/dataStructures/internal/memory/CharAttrVal.java hopark_cqlsnapshot/1 2016/02/26 10:21:33 hopark Exp $
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
 * Internal representation of char attribute value
 *
 * @author najain
 */
public class CharAttrVal extends AttrVal
{
  /** Attribute value */
  char value[];

  /** Actual Attribute length */
  /** Maximum attribute length */
  int  length;
  private int  maxLen;

  /**
   * Empty Constructor for CharAttrVal.
   * Invoked while deserializing instances of CharAttrVal type.
   */
  public CharAttrVal()
  {
    super(Datatype.CHAR);
  }
  
  /**
   * Constructor for CharAttrVal
   * @param v Attribute value
   */
  public CharAttrVal(int maxLen)
  {
    super(Datatype.CHAR);
    value = null;
    length = 0;
    this.maxLen = maxLen;
  }

  /**
   * Getter for length in CharAttrVal
   * @return Returns the length
   */
  public int getLength()
  {
    return length;
  }

  /**
   * Getter for value in CharAttrVal
   * @return Returns the value
   */
  public char[] getValue()
  {
    return value;
  }

  /**
   * Setter for value in CharAttrVal
   * @param v The value to set.
   * @param l The length for value
   * @throws ExecException 
   */
  public void setValue(char[] v, int l) throws ExecException
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
        value = new char[l];
      for (int i = 0; i < l; i++)
        value[i] = v[i];
    }
    length = l;
  }

  /**
   * Setter for value in CharAttrVal
   * @param v The value to set.
   * @throws ExecException 
   */
  public void copy(CharAttrVal v) throws ExecException
  {
    setValue(v.value, v.length);
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
