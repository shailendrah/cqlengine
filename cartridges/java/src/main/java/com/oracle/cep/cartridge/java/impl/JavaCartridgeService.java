/* $Header: cep/wlevs_cql/modules/cqlengine/cartridges/java/src/main/java/com/oracle/cep/cartridge/java/impl/JavaCartridgeService.java /main/1 2013/06/21 07:24:51 pkali Exp $ */

/* Copyright (c) 2013, Oracle and/or its affiliates. All rights reserved. */

/*
 DESCRIPTION
 <short description of component this file declares/defines>

 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>

 NOTES
 <other useful comments, qualifications, etc.>

 MODIFIED    (MM/DD/YY)
 pkali       03/28/13 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/cartridges/java/src/main/java/com/oracle/cep/cartridge/java/impl/JavaCartridgeService.java /main/1 2013/06/21 07:24:51 pkali Exp $
 *  @author  pkali   
 *  @since   release specific (what release of product did this appear in)
 */
package com.oracle.cep.cartridge.java.impl;

/*
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTracker;

import com.oracle.cep.cartridge.java.JavaTypeSystem;
import oracle.cep.extensibility.type.ITypeLocator;
import com.oracle.cep.cartridge.java.JavaCartridgeClassLoader;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class JavaCartridgeService implements BundleActivator
{

  private ServiceRegistration javaTypeSysRegistration;
  private ServiceRegistration typeLocatorRegistration;
  private ServiceTracker      javaCartridgeLoaderService;

  private static final Log logger = LogFactory.getLog("JavaCartridge");

  @Override
  public void start(BundleContext context) throws Exception
  {
    final BundleContext bundleContext = context;
    new Thread() {
      public void run() {
        // Getting the JavaCartridgeClassLoader service
        javaCartridgeLoaderService = new ServiceTracker(bundleContext,
            JavaCartridgeClassLoader.class.getName(), null);
        javaCartridgeLoaderService.open();
        JavaCartridgeClassLoader javaCartLoader = null;
        try
        {
          javaCartLoader = (JavaCartridgeClassLoader) javaCartridgeLoaderService
              .waitForService(0);
          if (logger.isDebugEnabled())
            logger.debug("Successfully retrieved the JavaCartridgeClassLoader service :" + javaCartLoader);
        }
        catch (InterruptedException e)
        {
          if (logger.isErrorEnabled())
            logger.error("Failed to retrieve the JavaCartridgeClassLoader service : " + e.getMessage());
        }

        JavaTypeSystemImpl javaTypeSys = new JavaTypeSystemImpl();
        javaTypeSys.setJavaCartridgeClassLoader(javaCartLoader);

        // Registering JavaTypeSystem Service
        javaTypeSysRegistration = bundleContext.registerService(
            JavaTypeSystem.class.getName(), javaTypeSys, null);
        if (logger.isDebugEnabled())
          logger.debug("Registered the JavaTypeSystem service :" + javaTypeSysRegistration);

        // Registering ITypeLocator Service
        typeLocatorRegistration = bundleContext.registerService(
            ITypeLocator.class.getName(), javaTypeSys, null);
        if (logger.isDebugEnabled())
          logger.debug("Registered the ITypeLocator service :" + typeLocatorRegistration);
      }
    }.start();

  }

  @Override
  public void stop(BundleContext bundleContext) throws Exception
  {
    javaCartridgeLoaderService.close();
    javaTypeSysRegistration.unregister();
    typeLocatorRegistration.unregister();
  }

}
*/
