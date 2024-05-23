/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/parser/CEPAttrSpecNode.java /main/5 2011/05/19 15:28:46 hopark Exp $ */

/* Copyright (c) 2006, 2011, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    The parse tree node corresponding to attribute specification in the DDL
    for a table

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    hopark      04/21/11 - make public to be reused in cqservice
    sborah      07/09/09 - support for bigdecimal
    parujain    08/11/08 - error offset
    mthatte     09/26/07 - Adding length for fixed length types
    anasrini    02/28/06 - Creation
    anasrini    02/28/06 - Creation
    anasrini    02/28/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/parser/CEPAttrSpecNode.java /main/4 2009/11/09 10:10:58 sborah Exp $
 *  @author  anasrini
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.parser;

import oracle.cep.common.AttributeMetadata;
import oracle.cep.common.Datatype;

/**
 * The parse tree node corresponding to attribute specification in the DDL
 * for a table
 *
 * @since 1.0
 */

public class CEPAttrSpecNode implements CEPParseTreeNode {

  /** Name of the attribute */
  protected String name;
  
  protected AttributeMetadata attrMetadata;
    
  protected int startOffset = 0;
  
  protected int endOffset = 0;

  /**
   * Constructor
   * @param name name of the attribute
   * @param dt datatype of the attribute
   */
  public CEPAttrSpecNode(CEPStringTokenNode nameToken, Datatype dt) {
    if(nameToken != null)
      this.name = nameToken.getValue();
    else
      this.name = null;
    
    this.attrMetadata = new AttributeMetadata(dt, dt.getLength(), 
                                              dt.getPrecision(), 0);
    
    if(nameToken != null)
    {
      setStartOffset(nameToken.getStartOffset());
      setEndOffset(nameToken.getEndOffset());
    }
  }

  /**
   * Constructor for datatype with a length attribute
   * @param name name of the attribute
   * @param dt datatype of the attribute
   * @param length length associated with the datatype
   */
  public CEPAttrSpecNode(CEPStringTokenNode nameToken, Datatype dt, CEPIntTokenNode len) {
    if(nameToken != null)
      this.name    = nameToken.getValue();
    else
      this.name    = null;
    
    this.attrMetadata = new AttributeMetadata(dt, len.getValue(), 
                                              dt.getPrecision(), 0);

    if(nameToken != null)
    {
      setStartOffset(nameToken.getStartOffset());
    }
    if(len != null)
    {
      setEndOffset(len.getEndOffset());
    }
  }

  /**
   * Constructor for datatype with a length attribute
   * @param name name of the attribute
   * @param dt datatype of the attribute
   * @param length length associated with the datatype
   */
  public CEPAttrSpecNode(CEPStringTokenNode nameToken, Datatype dt, int length) {
    if(nameToken != null)
      this.name    = nameToken.getValue();
    else
      this.name    = null;
    this.attrMetadata = new AttributeMetadata(dt, length, 
                                              dt.getPrecision(), 0);
    if(nameToken != null)
    {
      setStartOffset(nameToken.getStartOffset());
      setEndOffset(nameToken.getEndOffset());
    }
  }
  
  /**
   * Constructor for datatype with a length attribute
   * @param name name of the attribute
   * @param dt datatype of the attribute
   * @param length length associated with the datatype
   */
  public CEPAttrSpecNode(CEPStringTokenNode nameToken, Datatype dt, 
                  CEPIntTokenNode precision, CEPIntTokenNode scale)
  {
    if(nameToken != null)
      this.name   = nameToken.getValue();
    else
      this.name    = null;
    this.attrMetadata = new AttributeMetadata(dt, 0, 
                          precision.getValue(), scale.getValue());
    if(nameToken != null)
    {
      setStartOffset(nameToken.getStartOffset());
    }
    if(scale != null)
     setEndOffset(scale.getEndOffset());
    else 
      setEndOffset(precision.getEndOffset());
    
  }

  /**
   * Constructor for datatype with a length attribute
   * @param name name of the attribute
   * @param dt datatype of the attribute
   * @param length length associated with the datatype
   */
  public CEPAttrSpecNode(CEPStringTokenNode nameToken, Datatype dt, int precision, 
                  int scale) 
  {
    if(nameToken  != null)
      this.name    = nameToken.getValue();
    else
      this.name    = null;
     this.attrMetadata = new AttributeMetadata(dt, 0, 
        precision, scale);
    
    if(nameToken != null)
    {
      setStartOffset(nameToken.getStartOffset());
      setEndOffset(nameToken.getEndOffset());
    }
  }
  // getter methods

  /**
   * Get the name of the attribute
   * @return the name of the attribute
   */
  public String getName() {
    return name;
  }

  /**
   * Get the datatype of the attribute
   * @return the datatype of the attribute
   */
  public Datatype getDatatype() {
    return this.attrMetadata.getDatatype();
  }
  

  /**
   * Get the Attribute Metadata of the attribute
   * @return the Attribute Metadata of the attribute
   */
  public AttributeMetadata getAttributeMetadata()
  {
    return this.attrMetadata;
  }
  
  /**
   * Get the length associated with the datatype, if any. This applies only 
   * to datatypes that support an associated length (like CHAR)
   * @return the length associated with the datatype, if any
   */
  public int getLength() {
    return this.attrMetadata.getLength();
  }
  
  /**
   * Getter for precision value of Attribute
   * @return the precision value
   */
  /*public int getPrecision()
  {
    return this.precision;
  }*/
  
  /**
   * Getter for scale value of Attribute
   * @return the scale value
   */
  /*public int getScale()
  {
    return this.scale;
  }
  */
  /**
   * Sets startoffset corresponding to ddl
   */
  public void setStartOffset(int start)
  {
    this.startOffset = start;
  }
  
  /**
   * Gets the start offset
   */
  public int getStartOffset()
  {
    return this.startOffset;
  }
  
  /**
   * Sets the EndOffset corresponding to DDL
   */
  public void setEndOffset(int end)
  {
    this.endOffset = end;
  }
  
  /**
   * Gets the endoffset
   */
  public int getEndOffset()
  {
    return this.endOffset;
  }


  // toString
  
  public String toString() {
    
    String s = "<AttrDefn name=\"" + name + "\" datatype=\"" + this.attrMetadata.getDatatype() + "\" ";
    StringBuilder sb = new StringBuilder(s);
    if (this.attrMetadata.getLength() > 0)
      sb.append("length=\"" + this.attrMetadata.getLength() + "\" ");

    sb.append("/>");
    return sb.toString();
  }
  
}
