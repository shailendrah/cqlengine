/* $Header: cep/wlevs_cql/modules/cqlengine/common/src/oracle/cep/extensibility/type/IType.java /main/4 2011/09/05 22:47:26 sbishnoi Exp $
/* Copyright (c) 2006, 2011, Oracle and/or its affiliates. 
All rights reserved. */

/*
 DESCRIPTION
 Enumeration of CEP datatypes supported

 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>

 NOTES
 <other useful comments, qualifications, etc.>
 MODIFIED    (MM/DD/YY)
    sbishnoi  07/16/11 - adding INTERVALYM
    sborah    09/09/09 - support for bigdecimal
  */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/common/src/oracle/cep/extensibility/type/IType.java /main/3 2009/11/09 10:10:58 sborah Exp $
 *  @author  
 *  @since
 */
package oracle.cep.extensibility.type;

import oracle.cep.common.Datatype;

/**
 * Base interface for all CQL types.
 * CQL types fall into two categories:
 * - native
 * - extensible types
 * 
 * The native types are those defined by {@link Datatype} and have special handling within the engine.
 * Extensible types are those defined externally by cartridges.
 * 
 * @author Alex Alves
 *
 */
public interface IType
{
  /**
   * Meta-type for CQL types.
   * In particular, the meta-type OBJECT can be used for extensible types.
   *  
   */
  public enum Kind
  {
    INT, 
    BIGINT, 
    FLOAT, 
    DOUBLE,  
    BYTE, 
    CHAR, 
    BOOLEAN, 
    TIMESTAMP, 
    OBJECT, 
    INTERVAL,    
    VOID, 
    XMLTYPE, 
    UNKNOWN, 
    BIGDECIMAL,
    INTERVALYM
  }

  /**
   * Returns type name
   * 
   * @return
   */
  String name();

  /**
   * Returns meta-type for this type. 
   * All complex types use the 'object' meta-type.
   * 
   * @return
   */
  Kind getKind();

  /**
   * Returns length, or size, of type.
   * This is not to be confused with the length of an array type.
   * 
   * @return
   */
  int getLength();

  /**
   * Returns precision for number-based types.
   * 
   * @return
   */
  int getPrecision();

  /**
   * Returns if it is case sensitive.
   * 
   * @return
   */
  boolean isCaseSensitive();

  /**
   * Returns true if from parameter can be assigned to a variable of this type.
   * 
   * @param fromDatatype
   * @return
   */
  boolean isAssignableFrom(IType fromDatatype);
  
}
