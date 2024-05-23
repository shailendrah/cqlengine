/* $Header: cep/wlevs_cql/modules/cqlengine/test/src/oracle/cep/test/cartridge/treeindex/TreeIndex.java /main/2 2013/03/04 00:54:59 sbishnoi Exp $ */

/* Copyright (c) 2009, 2013, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    udeshmuk    09/14/09 - make less than results non-exact
    udeshmuk    09/13/09 - insert should add bucket to tree.
    anasrini    09/10/09 - Creatio
    anasrini    09/10/09 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/test/src/oracle/cep/test/cartridge/treeindex/TreeIndex.java /main/2 2013/03/04 00:54:59 sbishnoi Exp $
 *  @author  anasrini
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.test.cartridge.treeindex;

import java.util.Iterator;
import java.util.TreeMap;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;

import oracle.cep.dataStructures.internal.ITuplePtr;
import oracle.cep.extensibility.indexes.IIndex;

class TreeIndex implements IIndex
{
  private TreeMap<Integer, Set<Object>>  tree;
  private Iterator<Set<Object>>          iter;
  private Iterator<Object>               valIter;

  TreeIndex()
  {
    tree    = new TreeMap<Integer, Set<Object>>();
    iter    = null;
    valIter = null;
  }

  public void insert(Object newkey, Object val)
  {
    Integer key = ((Number)newkey).intValue();
    
    Set<Object> bucket = tree.get(key);
    if (bucket == null)
    {
      bucket = new HashSet<Object>();
      tree.put(key, bucket);
    }

    bucket.add(val);
  }

  public void delete(Object oldkey, Object oldval)
  {
    Integer key = ((Number)oldkey).intValue();

    Set<Object> bucket = tree.get(key);
    
    bucket.remove(oldval);
    
    if(bucket.isEmpty())
      tree.remove(key);
  }

  public void update(Object oldkey, Object newkey, 
                     Object oldval, Object newval)
  {
    delete(oldkey, oldval);
    insert(newkey, newval);
  }

  public void startScan(Object indexCallbackContext, Object[] args)
  {
    TreeIndexCallbackContext ctx =
      (TreeIndexCallbackContext) indexCallbackContext;

    TreeIndexOperation        op  = ctx.getOp();
    Object                    keyObj = args[ctx.getKeyPosition()];
    Integer                   key = ((Number)keyObj).intValue();

    Map<Integer, Set<Object>> sets;

    switch(op)
    {
      case LESS:
        /*
         * The headMap operation returns results non-inclusive of the key.
         * So the results are EXACT.
         */
        sets = tree.headMap(key);
        iter = (sets != null && sets.values() != null ?
                sets.values().iterator() : null);
        break;
      case GREATER:
        /*
         * The tailMap method returns results inclusive of key. 
         * So the results are INEXACT (greater than or equal to).
         */
        sets = tree.tailMap(key);
        iter = (sets != null && sets.values() != null ?
                sets.values().iterator() : null);
        break;
      default:
        iter = null;
        break;
    }
  }

  public Object getNext()
  {
    if (valIter != null && valIter.hasNext())
    {
      return valIter.next();
    }
    else if (iter != null && iter.hasNext())
    {
      Set s = iter.next();
      valIter = s.iterator();

      // Each bucket (set) is non-empty
      return valIter.next();
    }
    else
    {
      valIter = null;
      iter = null;
      return null;
    }
  }

  public void releaseScan()
  {
    valIter = null;
    iter    = null;
  }
  
  /**
   * Dumps index contents. used for debugging.
   */
  private void dump()
  {
    System.out.println("*** INDEX Contents ***");
    Set<Integer> keyset = tree.keySet();
    Iterator<Integer> myIter = keyset.iterator();
    while(myIter.hasNext())
    {
      Integer myKey = myIter.next();
      System.out.println("Key : "+myKey);
      Set<Object> val = tree.get(myKey);
      Iterator<Object> bucketIter = val.iterator();
      if(!bucketIter.hasNext())
        System.out.println("No values present!");
      else
      {
        while(bucketIter.hasNext())
        {
          System.out.println((ITuplePtr)bucketIter.next());   
        }
      }
      System.out.println("---------------------------------------");
    }
  }
}
