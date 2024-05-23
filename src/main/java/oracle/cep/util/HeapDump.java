/* $Header: pcbpel/cep/server/src/oracle/cep/util/HeapDump.java /main/3 2009/02/19 16:44:31 hopark Exp $ */

/* Copyright (c) 2007, 2009, Oracle and/or its affiliates.
All rights reserved. */

/*
   DESCRIPTION
    copied from
    http://blogs.sun.com/sundararajan/entry/programmatically_dumping_heap_from_java

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    hopark      12/01/08 - fix dumpHeap api
    hopark      09/21/07 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/util/HeapDump.java /main/3 2009/02/19 16:44:31 hopark Exp $
 *  @author  hopark  
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.util;

import java.io.File;
import java.lang.management.ManagementFactory;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.MBeanServer;

import com.oracle.cep.common.util.SecureFile;
import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;

public class HeapDump {
    /**
     * Call this method from your application whenever you 
     * want to dump the heap snapshot into a file.
     *
     * @param fileName name of the heap dump file
     * @param live flag that tells whether to dump
     *             only the live objects
     */
    public static void dumpHeap(String fileName) 
    {
      try
      {
        File f = SecureFile.getFile(fileName);
        f.delete();
      } catch(Exception e) {}
      try
      {
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        ObjectName oname = new ObjectName("com.sun.management:type=HotSpotDiagnostic");
  
        Object[] params = new Object[2];
        params[0] = fileName;
        params[1] = new Boolean(true);
        String[] sigs= { "java.lang.String", "boolean" };
  
        mbs.invoke(oname,  "dumpHeap",  params, sigs);
      } catch(Exception e)
      {
        System.out.println(e);
      }
    }
}

