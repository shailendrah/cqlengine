/* $Header: cep/wlevs_cql/modules/cqlengine/cartridges/jdbc/src/main/java/com/oracle/cep/cartridge/jdbc/DummyAdapterFactory.java /main/1 2010/05/05 05:35:07 udeshmuk Exp $ */

/* Copyright (c) 2010, Oracle and/or its affiliates. All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    udeshmuk    04/28/10 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/cartridges/jdbc/src/main/java/com/oracle/cep/cartridge/jdbc/DummyAdapterFactory.java /main/1 2010/05/05 05:35:07 udeshmuk Exp $
 *  @author  udeshmuk
 *  @since   release specific (what release of product did this appear in)
 */

package com.oracle.cep.cartridge.jdbc;

import com.bea.wlevs.ede.api.AdapterFactory;
import com.bea.wlevs.ede.api.Adapter;

/*
 * Added as per Andy's suggestion.
 * This has been added here so that we can use wlevs:factory tag in the EPN of
 * JDBC Cartridge wherein we can point to the ocep_jdbc_context_config.xsd as
 * the 'provider-schema'. The schema resolving code looks in adapter factories,
 * for getting hint on which XSD to use to parse tags in app-config file.
 * Without this, error comes during parsing context configuration as tags can
 * not be resolved.
 */

public class DummyAdapterFactory implements AdapterFactory
{
  @Override
  public Adapter create() throws IllegalArgumentException 
  {
    return null;
  }
}
  
