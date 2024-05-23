/* $Header: ArrayUtil.java 15-jun-2007.10:22:22 hopark   Exp $ */

/* Copyright (c) 2007, Oracle. All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    hopark      06/15/07 - Creation
 */

/**
 *  @version $Header: ArrayUtil.java 15-jun-2007.10:22:22 hopark   Exp $
 *  @author  hopark  
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ArrayUtil
{
  //T[] fromCollection is desired, but java does not support creating generic arrays
  // e.g T[] res = new T[c.size()] is not supported
  public static <T> void fromCollection(Collection<T> c, T[] res)
  {
    assert (res.length == c.size());
    int pos = 0;
    for (T o : c) {
      res[pos++] = o;
    }
  }

  public static int[] fromCollection(Collection<Integer> c)
  {
    int[] res = new int[c.size()];
    int pos = 0;
    for (Integer o : c) {
      res[pos++] = o;
    }
    return res;
  }

  public static int[] fromArray(Integer[] a1)
  {
    int[] res = new int[a1.length];
    int pos = 0;
    for (Integer o : a1) {
      res[pos++] = o;
    }
    return res;
  }
 
  public static List<Integer> fromArray(int[] a1)
  {
    List<Integer> res = new ArrayList<Integer>();
    for (int o : a1) {
      res.add(o);
    }
    return res;
  }
    
  public static <T> void toCollection(T[] a, Collection<T> c)
  {
    for (T o : a) {
      c.add(o);
    }
  }
}

