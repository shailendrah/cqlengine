/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/dataStructures/internal/memory/FloatAttrVal.java hopark_cqlsnapshot/1 2016/02/26 10:21:33 hopark Exp $ */

/* Copyright (c) 2006, 2016, Oracle and/or its affiliates. 
All rights reserved.*/

/*
 DESCRIPTION
 Declares FloatAttrVal in package oracle.cep.execution.internals.
 
 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>
 
 NOTES
 <other useful comments, qualifications, etc.>
 
 MODIFIED    (MM/DD/YY)
 hopark    09/04/07 - optimize
 najain    03/12/07 - bug fix
 skaluska  02/18/06 - add attrType 
 skaluska  02/13/06 - Creation
 skaluska  02/13/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/dataStructures/internal/memory/FloatAttrVal.java hopark_cqlsnapshot/1 2016/02/26 10:21:33 hopark Exp $
 *  @author  skaluska
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.dataStructures.internal.memory;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import oracle.cep.common.Datatype;
import oracle.cep.execution.snapshot.IPersistenceContext;

/**
 * Internal representation of float attribute value
 *
 * @author skaluska
 */
public class FloatAttrVal extends AttrVal
{
  /** Attribute value */
  float value;

  /**
   * Zero Argument Constructor for FloatAttrVal.
   * Invoked while deserialization of instance of FloatAttrVal type
   */
  public FloatAttrVal()
  {
    super(Datatype.FLOAT);
  }
  /**
   * Constructor for FloatAttrVal
   * @param val Attribute value
   */
  public FloatAttrVal(float val)
  {
    super(Datatype.FLOAT);
    value = val;
  }

  /**
   * Getter for value in FloatAttrVal
   * @return Returns the value
   */
  public float getValue()
  {
    return value;
  }

  /**
   * Setter for value in FloatAttrVal
   * @param value The value to set.
   */
  public void setValue(float value)
  {
    this.value = value;
  }

  @Override
  public void writeExternal(ObjectOutput out)
      throws IOException
  {
    super.writeExternal(out);
    out.writeFloat(this.value);
  }

  @Override
  public void readExternal(ObjectInput in)
      throws IOException, ClassNotFoundException
  {
    super.readExternal(in);
    this.value = in.readFloat();
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
