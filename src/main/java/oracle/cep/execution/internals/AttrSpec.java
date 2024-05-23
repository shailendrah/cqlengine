/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/internals/AttrSpec.java /main/3 2009/11/09 10:10:58 sborah Exp $ */
/* Copyright (c) 2006, 2009, Oracle and/or its affiliates. 
All rights reserved. */
/*
 DESCRIPTION
 Declares AttrSpec in package oracle.cep.execution.internals.
 
 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>
 
 NOTES
 <other useful comments, qualifications, etc.>
 
 MODIFIED    (MM/DD/YY)
    sborah    07/15/09 - support for bigdecimal
    najain    03/12/07 - bug fix
    skaluska  02/25/06 - Creation
    skaluska  02/25/06 - Creation
 */
/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/internals/AttrSpec.java /main/3 2009/11/09 10:10:58 sborah Exp $
 *  @author  skaluska
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.execution.internals;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import oracle.cep.common.AttributeMetadata;
import oracle.cep.common.Datatype;

/**
 * Attribute specification
 * 
 * @author skaluska
 */
public class AttrSpec implements Externalizable
{
    private static final long serialVersionUID = -8910783720229368487L;

    /** Attribute Metadata */
   private AttributeMetadata attrMetadata;
  
   public AttrSpec()
   {
   }

   /**
   * Constructor for attrSpec
   * 
   * @param type
   *          Type for attribute
   * @param len
   *          Length of attribute
   */
  AttrSpec(AttributeMetadata attrMetadata)
  {
    this.attrMetadata = attrMetadata;
  }
  
  public AttributeMetadata getAttrMetadata()
  {
    return this.attrMetadata;
  }

  /**
   * Getter for len in AttrSpec
   * @return Returns the len
   */
  public int getLength()
  {
    if(this.attrMetadata != null)
      return this.attrMetadata.getLength();
    
    return -1;
  }
  
   /**
   * Getter for type in AttrSpec
   * @return Returns the type
   */
  public Datatype getType()
  {
    return this.attrMetadata.getDatatype();
    
  }

  @Override
  public void writeExternal(ObjectOutput out) throws IOException {
       out.writeObject(attrMetadata);
  }

  @Override
  public void readExternal(ObjectInput in) throws IOException,
        ClassNotFoundException {
      attrMetadata = (AttributeMetadata) in.readObject();
  }
 
}
