/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/dataStructures/internal/memory/ObjectAttrVal.java hopark_cqlsnapshot/1 2016/02/26 10:21:33 hopark Exp $ */

/* Copyright (c) 2006, 2016, Oracle and/or its affiliates. 
All rights reserved.*/

/*
   DESCRIPTION
    Class encapsulating an attribute of type OBJECT in a tuple

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    hopark      09/04/07 - optimize
    najain      03/12/07 - bug fix
    anasrini    07/17/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/dataStructures/internal/memory/ObjectAttrVal.java hopark_cqlsnapshot/1 2016/02/26 10:21:33 hopark Exp $
 *  @author  anasrini
 *  @since   1.0
 */

package oracle.cep.dataStructures.internal.memory;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;

import oracle.cep.common.Datatype;
import oracle.cep.execution.snapshot.IPersistenceContext;

/**
 * Internal representation of float attribute value
 *
 * @author skaluska
 */
public class ObjectAttrVal extends AttrVal
{
  private static final long serialVersionUID = -9013564033908693573L;

  /** Attribute value */
  Object value;

  /**
   * Zero Argument Constructor for ObjectAttrVal.
   * Invoked while deserialization of instances of ObjectAttrVal type
   */
  public ObjectAttrVal()
  {
    super(Datatype.OBJECT);
  }
  
  /**
   * Constructor for ObjectAttrVal
   * @param v Attribute value
   */
  public ObjectAttrVal(Object v)
  {
    super(Datatype.OBJECT);
    value = v;
  }

  /**
   * Getter for value in ObjectAttrVal
   * @return Returns the value
   */
  public Object getValue()
  {
    return value;
  }

  /**
   * Setter for value in ObjectAttrVal
   * @param value The value to set.
   */
  public void setValue(Object value)
  {
    this.value = value;
  }

  @Override
  public void writeExternal(ObjectOutput out)
      throws IOException
  {
    super.writeExternal(out);
    if(this.value instanceof Externalizable || this.value instanceof Serializable)
    {
      out.writeBoolean(true);
      out.writeObject(this.value);
    }
    else
      out.writeBoolean(false);
  }

  @Override
  public void readExternal(ObjectInput in)
      throws IOException, ClassNotFoundException
  {
    super.readExternal(in);
    boolean isObjectReadRequired = in.readBoolean();
    if(isObjectReadRequired)
      this.value = in.readObject();
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
