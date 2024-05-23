/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/dataStructures/internal/memory/DoubleAttrVal.java hopark_cqlsnapshot/1 2016/02/26 10:21:33 hopark Exp $ */

/* Copyright (c) 2008, 2016, Oracle and/or its affiliates. 
All rights reserved.*/

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    udeshmuk    01/30/08 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/dataStructures/internal/memory/DoubleAttrVal.java hopark_cqlsnapshot/1 2016/02/26 10:21:33 hopark Exp $
 *  @author  udeshmuk
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.dataStructures.internal.memory;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import oracle.cep.common.Datatype;
import oracle.cep.execution.snapshot.IPersistenceContext;

/**
 * Internal representation of double attribute value
 *
 * @author udeshmuk
 */
public class DoubleAttrVal extends AttrVal
{
  /** Attribute value */
  double value;

  /**
   * Empty Constructor for DoubleAttrVal.
   * Invoked while deserialization on instances of DoubleAttrVal type
   */
  public DoubleAttrVal()
  {
    super(Datatype.DOUBLE);
  }
  /**
   * Constructor for DoubleAttrVal
   * @param val Attribute value
   */
  public DoubleAttrVal(double val)
  {
    super(Datatype.DOUBLE);
    value = val;
  }

  /**
   * Getter for value in DoubleAttrVal
   * @return Returns the value
   */
  public double getValue()
  {
    return value;
  }

  /**
   * Setter for value in DoubleAttrVal
   * @param value The value to set.
   */
  public void setValue(double value)
  {
    this.value = value;
  }

  @Override
  public void writeExternal(ObjectOutput out)
      throws IOException
  {
    super.writeExternal(out);
    out.writeDouble(this.value);
  }

  @Override
  public void readExternal(ObjectInput in)
      throws IOException, ClassNotFoundException
  {
    super.readExternal(in);
    this.value = in.readDouble();
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
