/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/metadata/Attribute.java /main/7 2009/11/09 10:10:58 sborah Exp $ */

/* Copyright (c) 2006, 2009, Oracle and/or its affiliates. 
All rights reserved. */

/*
 DESCRIPTION
 Declares Attribute in package oracle.cep.metadata.
 
 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>
 
 NOTES
 <other useful comments, qualifications, etc.>
 
 MODIFIED    (MM/DD/YY)
 sborah    07/08/09 - support for bigdecimal
 parujain  01/14/09 - metadata in-mem
 hopark    02/06/08 - fix autofields
 hopark    01/18/08 - support dump
 parujain  01/09/07 - BDB integration
 anasrini  03/27/06 - add setPosition 
 skaluska  03/10/06 - Creation
 skaluska  03/10/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/metadata/Attribute.java /main/7 2009/11/09 10:10:58 sborah Exp $
 *  @author  skaluska
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.metadata;

import oracle.cep.common.AttributeMetadata;
import oracle.cep.common.Datatype;
import oracle.cep.extensibility.functions.IAttribute;
import oracle.cep.logging.DumpDesc;

/**
 * Attribute definition
 *
 * @author skaluska
 */
@DumpDesc(autoFields=true)
public class Attribute implements IAttribute
{
  /**
   * Default suid
   */
  private static final long serialVersionUID = 1L;

/** Attribute position */
  private int      position;

  /** Attribute Name */
  private String   name;
    
  private AttributeMetadata attrMetadata;

  /**
   * Constructor for Attribute
   * @param attrName Name
   * @param attrType Datatype
   * @param maxLength Maximum length for array attributes
   */
  public Attribute(String attrName, Datatype attrType, int maxLength)
  {
    this.name      = attrName;
    this.attrMetadata = new AttributeMetadata(attrType, maxLength,
                                              attrType.getPrecision(), 0);
    
  }
  
  
  /**
   * Constructor for Attribute
   * @param attrName Name
   * @param attrTattrMetadataype Datatype
   */
  public Attribute(String attrName, AttributeMetadata attrMetadata)
  {
    this.name         = attrName;
    this.attrMetadata = attrMetadata;
  }
 
 
  public Attribute clone() throws CloneNotSupportedException {
    Attribute attr = (Attribute)super.clone();
    return attr;
  }


  /* (non-Javadoc)
   * @see oracle.cep.metadata.IAttribute#getName()
   */
  public String getName()
  {
    return name;
  }

  /**
   * Setter for name in Attribute
   * @param attrName The name to set.
   */
  public void setName(String attrName)
  {
    this.name = attrName;
  }

  /* (non-Javadoc)
   * @see oracle.cep.metadata.IAttribute#getType()
   */
  public Datatype getType()
  {
    return this.attrMetadata.getDatatype();
  }

  public AttributeMetadata getAttrMetadata()
  {
    return this.attrMetadata;
  }
  
  /* (non-Javadoc)
   * @see oracle.cep.metadata.IAttribute#getMaxLength()
   */
  public int getMaxLength()
  {
    return this.attrMetadata.getLength();
  }
  
  /**
   * Getter for precision value of Attribute
   * @return the precision value
   */
  public int getPrecision()
  {
    return this.attrMetadata.getPrecision();
  }
  
  /**
   * Getter for scale value of Attribute
   * @return the scale value
   */
  public int getScale()
  {
    return this.attrMetadata.getScale();
  }
  
  /* (non-Javadoc)
   * @see oracle.cep.metadata.IAttribute#getPosition()
   */
  public int getPosition()
  {
    return position;
  }

  /**
   * Setter for position in Attribute
   * @param attrPos the position of the attribute
   */
  void setPosition(int attrPos)
  {
   this.position = attrPos;
  }

  /* (non-Javadoc)
   * @see oracle.cep.metadata.IAttribute#toXml()
   */
  public String toXml()
  {
    // TODO
    return null;
  }
}
