/* $Header: cep/wlevs_cql/modules/cqlengine/cartridges/jdbc/src/main/java/com/oracle/cep/cartridge/jdbc/JdbcCartridgeFuncParamMetadata.java /main/2 2010/06/14 03:43:14 udeshmuk Exp $ */

/* Copyright (c) 2010, Oracle and/or its affiliates. All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    udeshmuk    06/11/10 - support cql types as parameter
    udeshmuk    01/08/10 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/cartridges/jdbc/src/main/java/com/oracle/cep/cartridge/jdbc/JdbcCartridgeFuncParamMetadata.java /main/2 2010/06/14 03:43:14 udeshmuk Exp $
 *  @author  udeshmuk
 *  @since   release specific (what release of product did this appear in)
 */

package com.oracle.cep.cartridge.jdbc;

import oracle.cep.common.Datatype;
import oracle.cep.extensibility.functions.IAttribute;

public class JdbcCartridgeFuncParamMetadata implements IAttribute
{
  
  private String paramName;
  
  private Datatype paramType;
  
  public JdbcCartridgeFuncParamMetadata(String paramName, Datatype type)
  {
    this.paramName = paramName;
    this.paramType = type;
  }

  @Override
  public int getMaxLength()
  {
    return paramType.getLength();
  }

  @Override
  public String getName()
  {
    return paramName;
  }

  @Override
  public int getPosition()
  {
    return 0; //TODO: what to do here?
  }

  @Override
  public int getPrecision()
  {
    return paramType.getPrecision();
  }

  @Override
  public int getScale()
  {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public Datatype getType()
  {
    return paramType;
  }

  @Override
  public String toXml()
  {
    return paramType.toString();
  }
  
}