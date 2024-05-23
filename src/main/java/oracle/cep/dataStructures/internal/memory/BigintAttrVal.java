/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/dataStructures/internal/memory/BigintAttrVal.java hopark_cqlsnapshot/1 2016/02/26 10:21:33 hopark Exp $ */

/* Copyright (c) 2006, 2016, Oracle and/or its affiliates. 
All rights reserved.*/

/*
 DESCRIPTION
 Declares BigintAttrVal in package oracle.cep.execution.internals.
 
 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>
 
 NOTES
 <other useful comments, qualifications, etc.>
 
 MODIFIED    (MM/DD/YY)
 hopark       09/04/07 - optimize
 najain       03/12/07 - bug fix
 hopark       10/17/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/dataStructures/internal/memory/BigintAttrVal.java hopark_cqlsnapshot/1 2016/02/26 10:21:33 hopark Exp $
 *  @author  hopark
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.dataStructures.internal.memory;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import oracle.cep.common.Datatype;
import oracle.cep.execution.snapshot.IPersistenceContext;

/**
 * Internal representation of bigint attribute value
 *
 * @author hopark
 */
public class BigintAttrVal extends AttrVal
{
  /** Attribute value */
  long value;

  /**
   * Empty constructor for BigintAttrVal.
   * Invoked while deserialization of instances.
   */
  public BigintAttrVal()
  {
    super(Datatype.BIGINT);
  }
  
  /**
   * Constructor for BigintAttrVal
   * @param v Attribute value
   */
  public BigintAttrVal(long v)
  {
    super(Datatype.BIGINT);
    value = v;
  }

  /**
   * Getter for value in BigintAttrVal
   * @return Returns the value
   */
  public long getValue()
  {
    return value;
  }

  /**
   * Setter for value in BigintAttrVal
   * @param value The value to set.
   */
  public void setValue(long value)
  {
    this.value = value;
  }

 
  @Override
  public void readExternal(ObjectInput in)
      throws IOException, ClassNotFoundException
  {
    super.readExternal(in);
    this.value = in.readLong();
  }
  
  @Override
  public void writeExternal(ObjectOutput out)
      throws IOException
  {
    super.writeExternal(out);
    out.writeLong(this.value);
  }
  

  @Override
  public void readExternal(ObjectInput in, IPersistenceContext ctx)
      throws IOException, ClassNotFoundException
  {
    readExternal(in);
  }
  
  @Override
  public void writeExternal(ObjectOutput out, IPersistenceContext ctx)
      throws IOException
  {
    writeExternal(out);    
  }

}
