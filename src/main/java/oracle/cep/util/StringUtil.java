/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/util/StringUtil.java /main/9 2011/04/27 18:37:35 apiper Exp $ */

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
    alealves    05/05/09 - correcting expand() impl
    hopark      12/17/08 - use system properties in expanding string
    hopark      04/13/08 - add expandenv
    hopark      02/04/08 - add expand
    hopark      01/01/08 - handle inner class
    najain      03/02/07 - 
    hopark      03/02/07 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/util/StringUtil.java /main/8 2009/10/30 15:55:04 hopark Exp $
 *  @author hopark
 *  @since release specific (what release of product did this appear in)
 */

package oracle.cep.util;

import java.util.Map;

public class StringUtil {
  public static long parseLongPercent(String val) {
    long value;
    int pos = val.lastIndexOf("%");
    if (pos < 0) {
      value = Long.parseLong(val);
    } else {
      String vv = val.substring(0, pos);
      value = -Long.parseLong(vv);
    }
    return value;
  }

  public static String getBaseClassName(Object o) {
    String className = o.getClass().getName();
    String names[] = className.split("\\.");
    String b = names[names.length - 1];
    int pos = b.indexOf('$');
    if (pos > 0)
      return b.substring(pos + 1);
    return b;
  }

  public static String expand(String s) {
    return expand(s, null);
  }

  /*
  * This function extends keywords given by '@keyword@' with
  * the given map of key/value pairs.
  */
  public static String expand(String s, Map<String, String> valmap) {
    return expand(s, valmap, '@');
  }

  public static String expand(String s, Map<String, String> valmap, char delim) {
    StringBuffer res = new StringBuffer();
    int from = 0;
    while (true) {
      int b = s.indexOf(delim, from);
      if (b < 0) {
        res.append(s.substring(from));
        break;
      }
      int e = s.indexOf(delim, b + 1);
      if (e < 0) {
        res.append(s.substring(from));
        break;
      }
      String key = s.substring(b + 1, e);
      if (key.length() < 0) {
        res.append(s.substring(from, b + 1));
        from = b + 1;
        continue;
      }
      String val = null;
      if (valmap != null)
        val = valmap.get(key);
      if (val == null)
        val = System.getProperty(key);
      if (val == null)
        val = System.getenv(key);
      if (val == null) {
        res.append(s.substring(from, b + 1));
        from = b + 1;
        continue;
      }
      val = val.replace('\\', '/');
      res.append(s.substring(from, b));
      res.append(val);
      from = e + 1;
    }
    /* this also replace http://xmlns.oracle.cep to http:\\xmlns..
    if (File.separatorChar != '/')
    {
      s = s.replace('/', File.separatorChar);
    }
    */
    return res.toString();
  }
}
