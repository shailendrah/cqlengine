/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/util/RBTreeNode.java /main/2 2011/10/10 10:31:44 udeshmuk Exp $ */

/* Copyright (c) 2010, 2011, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    udeshmuk    10/10/11 - XbranchMerge udeshmuk_bug-11933156_ps5 from
                           st_pcbpel_11.1.1.4.0
    udeshmuk    09/30/11 - implement clone
    sbishnoi    01/21/11 - removing ExtendedComparable
    sborah      11/25/10 - Red Black tree node
    sborah      11/25/10 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/util/RBTreeNode.java st_pcbpel_sborah_median/5 2010/12/19 15:19:33 sborah Exp $
 *  @author  sborah  
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.util;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

public class RBTreeNode implements Externalizable
{
    
  private static final long serialVersionUID = -1318495996587321554L;

  static RBTreeNode nil =  new RBTreeNode(null, null, null, false);
      
  /* node key value */
  @SuppressWarnings("unchecked")
  Comparable data;
  
  /* node size value */
  long size;
  
  /* color of the node*/
  boolean isRed;
  
  /* pointer to left subtree */
  RBTreeNode left;
  
  /* pointer to right subtree */
  RBTreeNode right;
  
  /* pointer to parent node */
  RBTreeNode parent;
  
  /** Constructor will be used to deserialize objects */
  public RBTreeNode()
  {}
  
  @SuppressWarnings("unchecked")
  RBTreeNode(Comparable data) 
  {
    this(data, null, null); 
    setTreeNodeRED();
  }

  @SuppressWarnings("unchecked")
  RBTreeNode(Comparable data2, RBTreeNode lst, RBTreeNode rst) 
  {     
    this.data = data2;
    this.left = lst;
    this.right = rst;
    this.size = 0;
  }
  
  @SuppressWarnings("unchecked")
  RBTreeNode(Comparable data, RBTreeNode lst, RBTreeNode rst, 
             RBTreeNode parent)
  {
    this(data, lst, rst);
    this.parent = parent;
  }
  
  @SuppressWarnings("unchecked")
  RBTreeNode(Comparable data, RBTreeNode lst, RBTreeNode rst, 
             boolean isRed) 
  {      
    this(data, lst, rst);
    if(isRed)
      setTreeNodeRED(); 
    else
      setTreeNodeBLACK();
  }
  
  public void setTreeNodeRED()
  {     
    isRed = true;
  }
  
  public void setTreeNodeBLACK()
  {     
    isRed = false;
  }
  
  public void setLeftChild(RBTreeNode lst)
  {
    left = lst;
  }
  
  public void setRightChild(RBTreeNode rst)
  {
    right = rst;
  }
  
  public void setParent(RBTreeNode parent)
  {
    this.parent = parent; 
  }
  
  public boolean isRed()
  {
    return isRed;
  }
  
  public boolean isBlack()
  {
    return !isRed;
  }
  
  public String toString()
  {
    return "  Value: " + data + " Size: " + size + 
    " Color: " + (isRed() ? "RED" : "BLACK");
  }

  public long getSize() {
    return size;
  }

  public void setSize(long size) {
    this.size = size;
  }
  
  public Comparable getData()
  {
    if(this == RBTreeNode.nil)
      return null;
    else
      return data;
  }
  
  public RBTreeNode copy(RBTreeNode parent)
  {
    if(this == RBTreeNode.nil || this.equals(RBTreeNode.nil))
      return RBTreeNode.nil;
    else
    {
      RBTreeNode myCopy = new RBTreeNode(null);
     
      myCopy.data = this.data;
      myCopy.isRed = this.isRed;
      myCopy.size = this.size;
      myCopy.parent = parent;
      myCopy.left = this.left.copy(myCopy);
      myCopy.right = this.right.copy(myCopy);
      return myCopy;
    }
  }

  @Override
  public void writeExternal(ObjectOutput out) throws IOException
  {
    out.writeObject(data);
    out.writeBoolean(isRed);
    out.writeLong(size);
    out.writeObject(parent);
    out.writeObject(left);
    out.writeObject(right);
  }

  @Override
  public boolean equals(Object obj)
  {
    if (this == obj)
      return true;
    
    if (obj == null)
      return false;
    
    if (getClass() != obj.getClass())
      return false;
    
    RBTreeNode other = (RBTreeNode) obj;
    if (data == null)
    {
      if (other.data != null)
        return false;
    } 
    else if (!data.equals(other.data))
      return false;
    
    if (isRed != other.isRed)
      return false;
    
    if (left == null)
    {
      if (other.left != null)
        return false;
    } 
    else if (!left.equals(other.left))
      return false;
    
    if (parent == null)
    {
      if (other.parent != null)
        return false;
    } 
    else if (!parent.equals(other.parent))
      return false;
    
    if (right == null)
    {
      if (other.right != null)
        return false;
    } else if (!right.equals(other.right))
      return false;
    
    if (size != other.size)
      return false;
    return true;
  }

  @Override
  public void readExternal(ObjectInput in) throws IOException,
      ClassNotFoundException
  {
    data = (Comparable) in.readObject();
    isRed = in.readBoolean();
    size = in.readLong();
    parent = (RBTreeNode) in.readObject();
    left = (RBTreeNode) in.readObject();
    right = (RBTreeNode) in.readObject();
  }
}

