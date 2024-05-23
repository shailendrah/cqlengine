/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/jmx/CEPStatsController.java /main/6 2013/10/08 10:15:00 udeshmuk Exp $ */

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
    sbishnoi    07/31/13 - add getter for CEPStats
    sbishnoi    07/09/13 - enable jmx framework
    hopark      10/10/08 - remove statics
    hopark      05/08/08 - server reorg
    parujain    09/19/07 - standalone
    anasrini    09/11/07 - fix mbeanObjName
    parujain    05/30/07 - CEPStatistic Controller
    parujain    05/30/07 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/jmx/CEPStatsController.java /main/6 2013/10/08 10:15:00 udeshmuk Exp $
 *  @author  parujain
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.jmx;

import javax.management.MBeanServer;

import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;
import oracle.cep.service.CEPManager;
import oracle.cep.service.ExecContext;

public class CEPStatsController{
 
    // instance of CEPStatss mbean
    CEPStats statsMBean;
  
    public CEPStatsController(ExecContext ec)
    {
      try
      {
        /*
         MBeanServer  mbeanServ = CEPMBeanUtil.getMBeanServer();
         //     String domainName     = mbeanServ.getDefaultDomain();
         System.out.println("Initialized MBeanServer handle.");
         String domainName = "oracle.cep.jmx";
         //String type = ":type=CEPStatsController,";
         String type = ":type=CEPStats";
         //String name = "name=CEPStats";
         String mbeanObjName = domainName + type;// + name;
         */
         // Create the CEP Stats MBean
         statsMBean = new CEPStats(ec);
         /*
         // Register the CEP Stats MBean
         CEPMBeanUtil.registerMBean(statsMBean, mbeanObjName);
         System.out.println("Initialized and registered CEPStats MBean");
         //CEPMBeanUtil.registerMBean(CEPStatsMXBean.class, statsMBean, mbeanObjName);
         */
      }
      catch(Exception e)
      {
        LogUtil.severe(LoggerType.TRACE, "failed to create CEPStatsController" + e.toString());
      }
    }

    public CEPStats getCEPStatsMBean()
    {
      LogUtil.info(LoggerType.TRACE, "CEPStats MBean object returned");
      return statsMBean;
    }
}
