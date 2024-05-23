/* $Header: pcbpel/cep/server/src/oracle/cep/dataStructures/internal/ExpandableArray.java /main/1 2009/02/23 06:47:36 sborah Exp $ */

/* Copyright (c) 2009, Oracle and/or its affiliates.All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    sborah      02/17/09 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/dataStructures/internal/ExpandableArray.java /main/1 2009/02/23 06:47:36 sborah Exp $
 *  @author  sborah  
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.dataStructures.internal;

import java.util.ArrayList;

/**
 * @author sborah
 * A custom Data structure which overrides the get() and set()
 * methods of the ArrayList. 
 * 
 * <p>The default ArrayList implementation does not fully mimic the behavior 
 * of an array while maintaining the added functionality of being a 
 * dynamically sized data structure.
 * 
 * <p>For instance, consider an ArrayList with an initial capacity 
 * of n (where n is non-zero positive integer). Values cannot be inserted
 * at arbitrary position x where 0 <= x < n if the current number of 
 * elements < x. In other words, elements can be added to the list only in a 
 * sequential manner and any insertion at position x can happen only if 
 * all positions from index [0, x-1] have already been filled.
 * 
 * <p>This custom data structure seeks to overcome this shortcoming by 
 * inserting nulls at all positions j where current_size <= j <= x
 * and then replaces the null value at position x with the specified
 * value. With this change it becomes easy to map an array based list to
 * its corresponding ExpandableArray based list.
 * 
 * <p>For accessing a value in the list :
 *   Array                            =>  ExpandableArray
 *   Object return_value = arr[index] =>  Object return_value = arr.get(index)
 * 
 * <p>For setting a value in the list :
 *   Array                            =>  ExpandableArray
 *   arr[index] = new_value;          =>  arr.set(index, new_value);
 *      
 * <p>In some cases, it was more useful for the get() method to simply 
 * return a null instead of an IndexOutOfBoundsException if the 
 * specified position is greater than the current size of the list.
 * A simple check for a null value can be used on the value returned
 * by the get() method instead of complex exception handling constructs. 
 * For handling these cases , the get() method has been appropriately
 * overridden.
 *      
 */
public class ExpandableArray<E> extends ArrayList<E>
{
  private static final long serialVersionUID = 1L;
  
 /**
  * Constructs an empty list with the specified initial capacity.
  *
  * @param   initialCapacity   the initial capacity of the list
  * @exception IllegalArgumentException if the specified initial capacity
  *            is negative
  */
  public ExpandableArray(int initialCapacity)
  {
    super(initialCapacity);
  }
  
  /**
   * Constructs an empty list with an initial capacity of ten.
   */
  public ExpandableArray()
  {
    super();
  }

  /**
   * Overrides the ArrayList : get() method. 
   * 
   * Instead of throwing a IndexOutOfBoundsException for 
   * indexes greater than the current size of the list, 
   * this method simply returns a null. For other cases , it
   * returns the element at the specified position in this list.
   *
   * @param  index index of the element to return
   * @return the element at the specified position in this list
   *         null if the specified position is greater than the 
   *         current size of the list.
   *  
   */
  public E get(int index)
  {
    if (index >= size())
    {
      return null;
    }
    
    return super.get(index);
  }//end of get()

  /**
   * Overrides the ArrayList : set() method.
   * 
   * If the specified position is greater than the current size, 
   * then it adds nulls to the list till the size of the list becomes
   * equal to the specified position. It then 
   * replaces the element at the specified position in this list with
   * the specified element.
   *
   * @param index index of the element to replace
   * @param obj element to be stored at the specified position
   * @return the element previously at the specified position
   */
  public E set(int index, E obj)
  {
    int sz = size();
      
    if (index >= sz)
    {
      for (int i = sz; i <= index; i++)
      {
        add(null);
      }
    }
    return super.set(index, obj);
  }//end of set()
 

}//end of ExpandableArray class
