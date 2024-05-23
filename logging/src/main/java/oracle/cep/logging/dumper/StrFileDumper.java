/* $Header: cep/wlevs_cql/modules/cqlengine/logging/src/oracle/cep/logging/dumper/StrFileDumper.java /main/4 2012/08/30 22:19:43 pkali Exp $ */

/* Copyright (c) 2008, 2012, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    pkali       07/25/12 - fixed the file stream resource release
    hopark      10/10/08 - remove statics
    hopark      02/05/08 - add setVoid
    hopark      02/04/08 - pass id
    hopark      01/01/08 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/logging/src/oracle/cep/logging/dumper/StrFileDumper.java /main/4 2012/08/30 22:19:43 pkali Exp $
 *  @author  hopark  
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.logging.dumper;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import oracle.cep.logging.ILogLevelManager;

public class StrFileDumper extends StrDumper
{
  protected static AtomicInteger s_fileid = new AtomicInteger();
  
  protected String m_traceFolder;
  protected String m_fileName;
  protected Exception m_exception;

  public StrFileDumper(ILogLevelManager lm, String key, String traceFolder, String tracePostfix)
  {
    super(lm);
    m_exception = null;
    HashMap<String,String> valMap = new HashMap<String,String>();
    int id = s_fileid.incrementAndGet();
    valMap.put("TRC_ID", Integer.toString(id));
    Format formatter = new SimpleDateFormat("yyMMdd_HHmmss");
    Date date = new Date();
    String s = formatter.format(date);
    valMap.put("TRC_DATETIME", s);
    StringBuilder b = new StringBuilder();
    b.append(key);
    b.append(expand(tracePostfix, valMap));
    b.append(".trc");
    m_fileName = b.toString();
    m_traceFolder = traceFolder;
    File f = new File(traceFolder);
    f.mkdirs();
  }

  protected String getFilePath()
  {
    return m_traceFolder + File.separator + m_fileName;
  }

  public String toString()
  {
    String filePath = getFilePath();
    if (m_exception == null)
    {
      return filePath;
    }
    else
    {
      return m_exception.toString() + " : " + filePath;
    }
  }
  
  public void close()
  {
    BufferedWriter out = null;
    try 
    {
      String filePath = getFilePath();
      out = new BufferedWriter(new FileWriter(filePath));
      out.write(m_buf.toString());
    } 
    catch (IOException e) 
    {
      m_exception = e; 
    } 
    finally 
    {
      try 
      {
        if(out != null) 
        {
          out.close();
        }
      } 
      catch (IOException e) 
      {
        m_exception = e;
      }
    }
  }

  /*
   * This function extends keywords given by '@keyword@' with
   * the given map of key/value pairs.
   */
  public static String expand(String s, Map<String,String> valmap)
  {
    boolean done = false;
    while (!done)
    {
      int b = s.indexOf('@');
      if (b < 0) break;
      int e = s.indexOf('@', b+1);
      if (e < 0) break;
      String key = s.substring(b+1, e);
      if (key.length() <= 0)
        break;
      String val = valmap.get(key);
      StringBuffer res = new StringBuffer();
      res.append(s.substring(0, b));
      res.append(val);
      res.append(s.substring(e+1));
      s = res.toString();
    }
    return s;
  }
}
