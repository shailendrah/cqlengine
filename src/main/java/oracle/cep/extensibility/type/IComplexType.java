/* Copyright (c) 2009, 2010, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    alealves    Jun 24, 2010 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/extensibility/type/IComplexType.java /main/2 2010/06/29 09:16:03 udeshmuk Exp $
 *  @author  alealves
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.extensibility.type;

import oracle.cep.extensibility.cartridge.MetadataNotFoundException;

/**
 * Complex types are types composed of other simple 'member' types.
 * The member types can be accessed as methods, fields, or constructors.
 *
 */
public interface IComplexType extends IType
{
  /**
   * Returns meta-data for type's member method.
   * 
   * @param methodName
   * @param parameters
   * @return
   * @throws MetadataNotFoundException if method is not found.
   * 
   */
  IMethodMetadata getMethod(String methodName, IType... parameters)
    throws MetadataNotFoundException;
  
  /**
   * Returns meta-data for type's member fields.
   * 
   * @param fieldName
   * @return
   * @throws MetadataNotFoundException if field is not found.
   * 
   */
  IFieldMetadata getField(String fieldName)
    throws MetadataNotFoundException;

  /** 
   * Returns meta-data for all member  fields.
   * 
   * @return array of IFieldMetadata
   * 
   */
  IFieldMetadata [] getFields()
    throws MetadataNotFoundException;
  
  /**
   * Returns meta-data for type's constructors.
   * 
   * @param parameters
   * @return
   * @throws MetadataNotFoundException if constructor is not found.
   * 
   */
  IConstructorMetadata getConstructor(IType... parameters)
    throws MetadataNotFoundException;

}
