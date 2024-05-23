/* $Header: cep/wlevs_cql/modules/cqlengine/cartridges/java/src/main/java/com/oracle/cep/cartridge/java/impl/JavaFunctors.java /main/3 2012/06/28 05:37:49 alealves Exp $ */

/* Copyright (c) 2009, 2012, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    alealves    Feb 8, 2011 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/cartridges/java/src/main/java/com/oracle/cep/cartridge/java/impl/JavaFunctors.java /main/3 2012/06/28 05:37:49 alealves Exp $
 *  @author  alealves
 *  @since   release specific (what release of product did this appear in)
 */
package com.oracle.cep.cartridge.java.impl;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import oracle.cep.common.Datatype;
import oracle.cep.extensibility.cartridge.AmbiguousMetadataException;
import oracle.cep.extensibility.cartridge.ICartridgeContext;
import oracle.cep.extensibility.cartridge.MetadataNotFoundException;
import oracle.cep.extensibility.functions.IUserFunctionMetadata;
import oracle.cep.extensibility.functions.IUserFunctionMetadataLocator;
import oracle.cep.extensibility.type.IType.Kind;

import com.oracle.cep.cartridge.java.JavaCartridgeLogger;
import com.oracle.cep.cartridge.java.JavaTypeSystem;

public class JavaFunctors implements IUserFunctionMetadataLocator
{
  private static final Log logger = 
    LogFactory.getLog(JavaCartridge.JAVA_CARTRIDGE_LOGGER);
  
  private JavaTypeSystem javaTypeSystem;

  public JavaFunctors(JavaTypeSystem typeSystem)
  {
    javaTypeSystem = typeSystem;
  }

  @Override
  public List<IUserFunctionMetadata> getAllFunctions(ICartridgeContext context)
      throws MetadataNotFoundException
  {
    return null; // REVIEW what should say here? Does it even matter?
  }

  @Override
  public IUserFunctionMetadata getFunction(String name, Datatype[] paramTypes,
      ICartridgeContext context) throws MetadataNotFoundException,
      AmbiguousMetadataException
  {
    if (logger.isDebugEnabled())
      logger.debug("getFunction(" + name + ", " + paramTypes + ")");
    
    if ("to_cql".equals(name)) 
    {
      // Support for casting only
      if ((paramTypes.length != 1) || !(paramTypes[0] instanceof JavaDatatype))
        return null;
      
      JavaDatatype fromType = (JavaDatatype) paramTypes[0];
      
      Datatype toType = (Datatype) 
        ((JavaTypeSystemImpl) javaTypeSystem).findAssignableNativeType(fromType.getUnderlyingClass());
      
      // Do not convert from extensible object to opaque object
      if (toType != null && toType.getKind() != Kind.OBJECT)
        return new JavaCastOperator(name, fromType, toType);
    } 
    else if ("cast".equals(name))
    {
      // FIXME We should re-factor extensible function framework to allow for better error handling in this
      //  case. In the meantime, just log problem.
      
      if (paramTypes.length != 2) 
      {
        if (logger.isErrorEnabled())
          logger.error(JavaCartridgeLogger.invalidNumberOfArgumentsForFunctionLoggable(
              "cast@java(object, type)", 2, paramTypes.length).getMessageText());  
        return null;
      }
      
      if (!(paramTypes[1] instanceof JavaDatatype)) 
      {
        if (logger.isErrorEnabled())
          logger.error(JavaCartridgeLogger.illegalArgumentTypeForFunctionLoggable(
              "cast@java(object, type)", "Java type", 2, paramTypes[1].name()).getMessageText());
        return null;
      }
      
      return new JavaCastOperator(name, paramTypes, paramTypes[1]);
    }
    
    return null;
  }

}
