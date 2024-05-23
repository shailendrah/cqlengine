/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/extensibility/type/ITypeLocator.java /main/4 2010/02/18 08:25:50 alealves Exp $ */

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
      alealves  11/27/09 - Data cartridge context, default package support
    alealves    Sep 2, 2009 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/extensibility/type/ITypeLocator.java /main/4 2010/02/18 08:25:50 alealves Exp $
 *  @author  alealves
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.extensibility.type;

import oracle.cep.extensibility.cartridge.AmbiguousMetadataException;
import oracle.cep.extensibility.cartridge.ICartridgeContext;
import oracle.cep.extensibility.cartridge.MetadataNotFoundException;

/**
 * Locates extensible types that are not native to CQL.
 * One example of non-CQL type-systems is the Java type-system. 
 * 
 * Extensible types may either be a simple type (e.g. primitive Java short),
 *  or a complex type (e.g. java.lang.String), or an array type (e.g. java.lang.String[]).
 *
 */
public interface ITypeLocator 
{
  /**
   * Locates extensible CQL type from external type system.
   * 
   * 
   * @param extensibleTypeName full qualified type name
   * @param context cartridge context associated to this extensible object
   * @return extensible type
   * 
   * @throws MetadataNotFoundException if type is not found by locator
   * @throws AmbiguousMetadataException if multiple implementations are found for extensibleTypeName
   */
  IType getType(String extensibleTypeName, ICartridgeContext context) 
    throws MetadataNotFoundException, AmbiguousMetadataException;
  
  /**
   * Locates extensible CQL array type from external type system.
   * Parameter <code>length</code> may be -1 to indicate that it is unknown.
   * 
   * @param componentExtensibleTypeName
   * @param length
   * @param context cartridge context associated to this extensible object
   * @return
   * @throws MetadataNotFoundException if type is not found by locator
   * @throws AmbiguousMetadataException if multiple implementations are found for extensibleTypeName
   */
  IArrayType getArrayType(String componentExtensibleTypeName, ICartridgeContext context) 
    throws MetadataNotFoundException, AmbiguousMetadataException;

}
