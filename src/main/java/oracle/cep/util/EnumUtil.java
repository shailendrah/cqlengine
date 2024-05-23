/* $Header: EnumUtil.java 20-dec-2007.14:23:29 hopark Exp $ */

/* Copyright (c) 2007, Oracle. All rights reserved.  */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    hopark      12/20/07 - make fromString work for general enum
    hopark      07/02/07 - add getOrdinals
    hopark      06/25/07 - add getDescs
    hopark      06/07/07 - Creation
 */

/**
 *  @version $Header: EnumUtil.java 20-dec-2007.14:23:29 hopark Exp $
 *  @author  hopark  
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class EnumUtil
{
  public static <T extends Enum<T>> T fromString(Class<T> enumType, String name) 
  {
    assert (name != null);
    try {
      Method mname = enumType.getMethod("getName");          
      if (mname == null)
      {
        mname = enumType.getMethod("toString");
      }
      assert (mname != null);
      T[] vals = enumType.getEnumConstants();
      for (T k : vals) 
      {
        String kname = (String) mname.invoke(k);
        if (name.equals(kname)) return k;
      }
      assert false : "unknown " + enumType.getName() + " for " + name;
      return null;
    }
    catch (NoSuchMethodException ex) 
    {
      assert false : "getName should be implemented in " + enumType.getName();
      return null; 
    }
    catch (InvocationTargetException ex) 
    { 
      assert false : ex.toString() + " in accessing getName in " + enumType.getName();
      return null; 
    }
    catch (IllegalAccessException ex) 
    { 
      assert false : ex.toString() + " in accessing getName in " + enumType.getName();
      return null; 
    }
  }

  public static <T extends Enum<T>> T fromValue(Class<T> enumType, int val) 
  {
    try {
      Method getV = enumType.getMethod("getValue");
      assert (getV != null);
      T[] vals = enumType.getEnumConstants();
      for (T k : vals) 
      {
        Object o = getV.invoke(k);
        Integer v = (Integer) o;
        if (val == v.intValue()) return k;
      }
      assert false : "getValue " + enumType.getName() + " for " + val;
      return null;
    }
    catch (NoSuchMethodException ex) 
    {
      assert false : "getValue should be implemented in " + enumType.getName();
      return null; 
    }
    catch (InvocationTargetException ex) 
    { 
      assert false : ex.toString() + " in accessing getValue in " + enumType.getName();
      return null; 
    }
    catch (IllegalAccessException ex) 
    { 
      assert false : ex.toString() + " in accessing getValue in " + enumType.getName();
      return null; 
    }
  }

  public static <T extends Enum<T>> String[] getDescs(Class<T> enumType)
  {
    T[] types = enumType.getEnumConstants();
    String[] res = new String[types.length];
    int pos = 0;
    for (T k : types) 
    {
      res[pos++] = k.ordinal() + "," + k.toString();
    }
    return res;
  }
  
  public static <T extends Enum<T>> T fromOrdinal(Class<T> enumType, int ord) 
  {
    assert (ord >= 0);
    
    T[] vals = enumType.getEnumConstants();
    if (ord < vals.length)
      return vals[ord];

    assert false : "unknown " + enumType.getName() + " for " + ord;
    return null;
  }

  public static <T extends Enum<T>> int[] getOrdinals(Class<T> enumType)
  {
    T[] types = enumType.getEnumConstants();
    int[] res = new int[types.length];
    int pos = 0;
    for (T k : types) 
    {
      res[pos++] = k.ordinal();
    }
    return res;
  }
}
