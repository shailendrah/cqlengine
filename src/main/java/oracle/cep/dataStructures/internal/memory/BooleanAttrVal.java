/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/dataStructures/internal/memory/BooleanAttrVal.java hopark_cqlsnapshot/1 2016/02/26 10:21:33 hopark Exp $ */

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
    mthatte     12/26/07 - 
    najain      11/28/07 - 
    anasrini    11/28/07 - 
    najain      11/21/07 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/dataStructures/internal/memory/BooleanAttrVal.java hopark_cqlsnapshot/1 2016/02/26 10:21:33 hopark Exp $
 *  @author  najain  
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.dataStructures.internal.memory;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import oracle.cep.common.Datatype;
import oracle.cep.execution.snapshot.IPersistenceContext;

/**
 * Internal representation of boolean attribute value
 *
 * @author najain
 */
public class BooleanAttrVal extends AttrVal
{
  /** Attribute value */
  boolean value;

  
  /**
   * Empty Constructor for BooleanAttrVal
   * Invoked while deserialization on instances
   */
  public BooleanAttrVal()
  {
    super(Datatype.BOOLEAN);
  }
  
  /**
   * Constructor for BooleanAttrVal
   * @param v Attribute value
   */
  public BooleanAttrVal(boolean v)
  {
    super(Datatype.BOOLEAN);
    value = v;
  }

  /**
   * Getter for value in BooleanAttrVal
   * @return Returns the value
   */
  public boolean getValue()
  {
    return value;
  }

  /**
   * Setter for value in BooleanAttrVal
   * @param value The value to set.
   */
  public void setValue(boolean value)
  {
    this.value = value;
  }

  @Override
  public void writeExternal(ObjectOutput out)
      throws IOException
  {
    super.writeExternal(out);
    out.writeBoolean(this.value);
  }

  @Override
  public void readExternal(ObjectInput in)
      throws IOException, ClassNotFoundException
  {
    super.readExternal(in);
    this.value = in.readBoolean();
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

