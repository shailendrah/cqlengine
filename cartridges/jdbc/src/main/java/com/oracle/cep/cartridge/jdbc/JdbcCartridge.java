/* $Header: cep/wlevs_cql/modules/cqlengine/cartridges/jdbc/src/main/java/com/oracle/cep/cartridge/jdbc/JdbcCartridge.java /main/2 2010/08/06 02:09:19 udeshmuk Exp $ */

/* Copyright (c) 2010, Oracle and/or its affiliates. All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    udeshmuk    08/05/10 - XbranchMerge udeshmuk_bug-9946995_ps3 from
                           st_pcbpel_11.1.1.4.0
    udeshmuk    08/04/10 - implement bundleListener
    udeshmuk    01/04/10 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/cartridges/jdbc/src/main/java/com/oracle/cep/cartridge/jdbc/JdbcCartridge.java /main/2 2010/08/06 02:09:19 udeshmuk Exp $
 *  @author  udeshmuk
 *  @since   release specific (what release of product did this appear in)
 */
package com.oracle.cep.cartridge.jdbc;

import com.oracle.cep.cartridge.jdbc.JdbcCartridgeFunctionMetadataLocator;
import com.oracle.cep.cartridge.jdbc.JdbcCartridgeTypeLocator;

import oracle.cep.extensibility.cartridge.CartridgeException;
import oracle.cep.extensibility.cartridge.ICartridge;
import oracle.cep.extensibility.functions.IUserFunctionMetadataLocator;
import oracle.cep.extensibility.indexes.IIndexInfoLocator;
import oracle.cep.extensibility.type.ITypeLocator;
import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;
import oracle.cep.service.ICartridgeRegistry;

import org.osgi.framework.BundleContext;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.DisposableBean;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;
import org.springframework.osgi.context.BundleContextAware;

import java.util.List;
import java.util.LinkedList;
import java.util.Map;
import java.util.HashMap;
import java.sql.SQLException;

public class JdbcCartridge implements ICartridge, DisposableBean, InitializingBean,
  BundleContextAware, BundleListener
{
  public static final String CARTRIDGE_NAME = 
    "com.oracle.cep.cartridge.jdbc";
  
  private static JdbcCartridge j_instance = null;
  
  private static ICartridgeRegistry m_registry = null;
  
  private static JdbcCartridgeFunctionMetadataLocator funcLocator =
    new JdbcCartridgeFunctionMetadataLocator();
  
  private static JdbcCartridgeTypeLocator typeLocator =
    new JdbcCartridgeTypeLocator();

  private static BundleContext bundleContext = null;

  private static Map<Long, List<JdbcCartridgeContext>> appContextsMap = new HashMap<Long, List<JdbcCartridgeContext>>();
  
  @Override
  public IUserFunctionMetadataLocator getFunctionMetadataLocator()
  {
    return funcLocator;
  }

  @Override
  public IIndexInfoLocator getIndexInfoLocator()
  {
    // No indexes are managed by this cartridge
    return null;
  }

  @Override
  public ITypeLocator getTypeLocator()
  {
    return typeLocator;
  }  
  
  public static JdbcCartridge createInstance(ICartridgeRegistry registry)
    throws CartridgeException
  {
    if(j_instance != null)
      return j_instance;
    else
    {
      j_instance = new JdbcCartridge(registry);
      registry.registerCartridge(CARTRIDGE_NAME, j_instance);
      return j_instance;
      //No server context to register for this cartridge
    }
  }
  
  private JdbcCartridge(ICartridgeRegistry registry)
  {
    m_registry = registry;
  }
  
  public static ICartridgeRegistry getCartridgeRegistry()
  {
    return m_registry;
  }

  /**
   * This is a method of BundleContextAware interface. 
   * This is also called by Spring on its own to set the bundleContext reference
   * @param bundleContext The bundle context object
   */
  public void setBundleContext(BundleContext bundleContext)
  {
    this.bundleContext = bundleContext;
  }
  
  @Override
  public void afterPropertiesSet() throws Exception
  {
    if(bundleContext != null)
    {
      bundleContext.addBundleListener(this);
    }
  }
  
  @Override
  public void destroy() throws Exception {
    if(bundleContext != null){
      bundleContext.removeBundleListener(this);
    }
    if(m_registry != null)
      m_registry.unregisterCartridge(CARTRIDGE_NAME);
  }


  @Override
  public void bundleChanged(BundleEvent event) 
  {
    
    if((event.getType() & (BundleEvent.UPDATED | BundleEvent.UNRESOLVED)) != 0)
    {
      long bundleId = event.getBundle().getBundleId();
      List<JdbcCartridgeContext> ctxList = appContextsMap.get(bundleId);
      if(ctxList != null) 
      {
	for(JdbcCartridgeContext currCtx : ctxList)
	{
          try
          {
            currCtx.unregisterApplicationContext(event.getBundle().getSymbolicName());
          }
          catch(SQLException se)
          {
            LogUtil.severe(LoggerType.CUSTOMER,
                           "SQLException while closing connection for : "+
			   currCtx.getContextName()+ " " + se.getMessage());
          }
  	  catch(CartridgeException ce)
          {
	    LogUtil.severe(LoggerType.CUSTOMER,
	                   "CartridgeException while unregistering cartridge "+
			   "application context "+currCtx.getContextName()+" "+ 
	                   ce.getMessage());
          }
	}
	ctxList = null;
      }
      appContextsMap.remove(bundleId);
    }
  }

  public static void addAppCartridgeContext(long bundleId, JdbcCartridgeContext ctx)
  {
    List<JdbcCartridgeContext> ctxList = appContextsMap.get(bundleId);
    if(ctxList != null) //existing application/bundle
      ctxList.add(ctx);
    else
    { //new application/bundle
      ctxList = new LinkedList<JdbcCartridgeContext>();
      ctxList.add(ctx);
      appContextsMap.put(bundleId, ctxList);
    }
  }
} 
