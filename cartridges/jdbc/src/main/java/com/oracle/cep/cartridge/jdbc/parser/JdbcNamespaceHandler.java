/* $Header: cep/wlevs_cql/modules/cqlengine/cartridges/jdbc/src/main/java/com/oracle/cep/cartridge/jdbc/parser/JdbcNamespaceHandler.java /main/2 2010/05/05 05:35:07 udeshmuk Exp $ */

/* Copyright (c) 2010, Oracle and/or its affiliates. All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    udeshmuk    01/06/10 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/cartridges/jdbc/src/main/java/com/oracle/cep/cartridge/jdbc/parser/JdbcNamespaceHandler.java /main/2 2010/05/05 05:35:07 udeshmuk Exp $
 *  @author  udeshmuk
 *  @since   release specific (what release of product did this appear in)
 */

package com.oracle.cep.cartridge.jdbc.parser;

import org.osgi.framework.BundleContext;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.xml.NamespaceHandler;
import org.springframework.beans.factory.xml.NamespaceHandlerSupport;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class JdbcNamespaceHandler implements NamespaceHandler
{  
  private NamespaceHandlerSupport support = new NamespaceHandlerSupport() {
    public void init() {
      //register jdbc defn parser to parse spec starting with jdbc-context tag
      registerBeanDefinitionParser("jdbc-context",
                                   new  JdbcDefinitionParser());
    }
  };

  @Override
  public BeanDefinitionHolder decorate(Node arg0, BeanDefinitionHolder arg1,
      ParserContext arg2)
  {
    return support.decorate(arg0, arg1, arg2);
  }

  @Override
  public void init()
  {
    support.init();
  }

  @Override
  public BeanDefinition parse(Element arg0, ParserContext arg1)
  { //parser method that gets called by the spring infrastructure
    return support.parse(arg0, arg1);
  }
  
}
