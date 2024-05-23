/* $Header: cep/wlevs_cql/modules/cqlengine/test/src/oracle/cep/test/TestServer.java /main/2 2011/04/27 18:37:35 apiper Exp $ */

/* Copyright (c) 2007, 2011, Oracle and/or its affiliates. 
All rights reserved. */

/*
 DESCRIPTION
 <short description of component this file declares/defines>

 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>

 NOTES
 <other useful comments, qualifications, etc.>

 MODIFIED    (MM/DD/YY)
 hopark      11/19/08 - move main to TestServer
 hopark      11/17/08 - add setSchema
 hopark      10/15/08 - TupleValue refactoring
 skmishra    11/04/08 - adding synchronized to DDLs
 hopark      11/03/08 - fix schema
 hopark      10/09/08 - remove statics
 hopark      10/07/08 - use execContext to remove statics
 hopark      10/13/08 - fix jdbc
 hopark      09/26/08 - implement CEPServerEnvConfigurable
 parujain    09/23/08 - multiple schema
 sbishnoi    09/23/08 - incorporating changes of Constant
 sbishnoi    09/09/08 - support for schema
 skmishra    08/21/08 - import CEPServerXface
 hopark      08/01/08 - parser error handling
 hopark      05/09/08 - add main
 hopark      03/18/08 - reorg config
 rkomurav    04/18/08 - add explainplan
 mthatte     03/19/08 - jdbc reorg
 mthatte     02/25/08 - single task mode
 mthatte     11/26/07 - changing logging level to trace
 hopark      11/19/07 - localize customer log msg
 mthatte     11/26/07 - minor bug
 skmishra    11/14/07 - cleanup
 mthatte     11/06/07 - cleaning up BDB access
 mthatte     10/01/07 - cleaning stuff 
 mthatte     08/20/07 - bug fix; adding support for IStorage.getNextKey();
 mthatte     09/04/07 - 
 hopark      05/18/07 - use logging
 parujain    05/09/07 - 
 najain      04/24/07 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/test/src/oracle/cep/test/TestServer.java /main/1 2008/11/19 23:09:07 hopark Exp $
 *  @author  najain  
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.test;

import oracle.cep.common.Constants;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

public class TestServer 
{
 public static void main(String args[])
  {
    try
    {
      ConfigurableApplicationContext appContext = null;

      if (args.length > 0)
      {
        appContext = new FileSystemXmlApplicationContext(args[0]);
      }
      else
      {
        appContext = new ClassPathXmlApplicationContext(Constants.DEFAULT_CONFIG_FILE);
      }
      System.out.println("TestServer STARTED");

      if (!(args.length > 1 && args[1].equals("-nohup"))) {
        appContext.close();
        // The tests create non-daemon threads which prevent the app exiting
        System.exit(1);
      }
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }
}
