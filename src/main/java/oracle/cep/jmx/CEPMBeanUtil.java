/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/jmx/CEPMBeanUtil.java /main/6 2013/10/08 10:15:00 udeshmuk Exp $ */

/* Copyright (c) 2007, 2013, Oracle and/or its affiliates. 
All rights reserved.*/

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    sbishnoi    07/09/13 - enable jmx framework
    parujain    09/02/08 - fix bug
    parujain    09/24/07 - 
    hopark      07/13/07 - dump stack trace on exception
    parujain    06/19/07 - fix lint
    parujain    05/30/07 - MBean Utility
    parujain    05/30/07 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/jmx/CEPMBeanUtil.java /main/6 2013/10/08 10:15:00 udeshmuk Exp $
 *  @author  parujain
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.jmx;

import java.lang.management.ManagementFactory;
//import java.util.Set;
import java.util.logging.Level;

import javax.management.MBeanServer;
import javax.management.ObjectName;

//import oracle.as.jmx.framework.PortableMBeanFactory;
import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;

public class CEPMBeanUtil{
 //static final PortableMBeanFactory pmbf;

 /*static{
   PortableMBeanFactory factory = null;
    try {
      factory = new PortableMBeanFactory();
    } catch (Exception e) {
      LogUtil.logStackTrace(LoggerType.TRACE, Level.FINE, e);
      // Do nothing
    }
    finally{
      pmbf = factory;
    }
  }*/

  public static<T> void registerMBean(Object mbeanObject, String mbeanName)
  {
    try {
      // Get the MBeanServer
      MBeanServer mbeanServer = CEPMBeanUtil.getMBeanServer();    
      
      // Register the actual MBean with the MBeanServer
      ObjectName objectName = new ObjectName(mbeanName);
           
      mbeanServer.registerMBean(mbeanObject, objectName);
  }
  catch (Exception e)
  {
    LogUtil.logStackTrace(LoggerType.TRACE, Level.FINE, e);            
  }
  }
  /**
  public static <T> void registerMBean(Class<T> mbeanClass, T mbeanImpl, String mbeanName)
    {
        try {
            // Get the MBeanServer
            MBeanServer mbeanServer = CEPMBeanUtil.getMBeanServer();    
            
            // Register the actual MBean with the MBeanServer
            ObjectName objectName = new ObjectName(mbeanName);
            
            // check if this composite MBean was already created previously - if so abort.
            Set queryResult = mbeanServer.queryMBeans( objectName, null );
            if(queryResult.size() != 0 ){
                // either return or unregister and continue with registering
              mbeanServer.unregisterMBean(objectName);
            }

            Object mbean = CEPMBeanUtil.createMBean(mbeanImpl, mbeanClass);

            mbeanServer.registerMBean(mbean, objectName);
        }
        catch (Exception e)
        {
          LogUtil.logStackTrace(LoggerType.TRACE, Level.FINE, e);            
        }

    }
    

 public static <T> Object createMBean(T o, Class<T> cls) throws Exception {
        return pmbf.createMBean(o, cls);
    }*/

    public static MBeanServer getMBeanServer() throws Exception{
        // Get the MBeanServer
       // return pmbf.getMBeanServer();
      return ManagementFactory.getPlatformMBeanServer();
      
    }

}
