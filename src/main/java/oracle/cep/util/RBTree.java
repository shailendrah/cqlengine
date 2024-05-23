/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/util/RBTree.java /main/2 2011/10/10 10:31:44 udeshmuk Exp $ */

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
    sbishnoi    02/07/11 - cleanup
    sbishnoi    01/21/11 - removing ExtendedComparable
    sborah      11/29/10 - Red Black tree
    sborah      11/29/10 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/util/RBTree.java st_pcbpel_sborah_median/9 2010/12/20 01:11:02 sborah Exp $
 *  @author  sborah  
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.util;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;

public class RBTree implements Cloneable, Externalizable
{
  private static final long serialVersionUID = -6235386386448227844L;
  
/** Root Node of this tree */
  RBTreeNode root;
    
  public RBTreeNode getRoot()
  {
    return root;
  }

  /**
   * Constructor for the tree
   */
  public RBTree()
  {   
    root = RBTreeNode.nil;
  }
  
  /**
   * Insert given parameter value in this tree object
   * @param data
   */
  @SuppressWarnings("unchecked")
  public void insert(Comparable data)
  {
    RBTreeNode currNode = RBTreeNode.nil;
    RBTreeNode newNode = new RBTreeNode(data);
    RBTreeNode tempNode = root;
    while(tempNode != RBTreeNode.nil && !RBTreeNode.nil.equals(tempNode))
    {
      currNode = tempNode;
      if(newNode.data.compareTo(tempNode.data) < 0)
        tempNode = tempNode.left;
      else
        tempNode = tempNode.right;
      
      // increment the size of the node
      currNode.setSize(currNode.getSize() + 1);
    }   
    newNode.parent = currNode;
    
    if(currNode == RBTreeNode.nil || RBTreeNode.nil.equals(currNode))
      root = newNode;
    else if(newNode.data.compareTo(currNode.data) < 0)
      currNode.left = newNode;
    else
      currNode.right = newNode;
      
    newNode.left = RBTreeNode.nil;
    newNode.right = RBTreeNode.nil;
    newNode.setTreeNodeRED();
    newNode.setSize(1);
    
    insertFixUp(newNode);
  }
  
  private void insertFixUp(RBTreeNode currNode)
  {
    RBTreeNode parentNode = currNode.parent;
        
    while(parentNode.isRed())
    {
      if(parentNode == parentNode.parent.left)
      {
        RBTreeNode parentRightSibling = parentNode.parent.right;
        if(parentRightSibling.isRed())
        {
          parentNode.setTreeNodeBLACK();
          parentRightSibling.setTreeNodeBLACK();
          parentNode.parent.setTreeNodeRED();
          currNode = parentNode.parent;
          parentNode = currNode.parent;
        }
        else
        {
          if(currNode == parentNode.right)
          {
            currNode = parentNode;
            leftRotate(currNode);
            parentNode = currNode.parent;
          }
          
          if(parentNode != RBTreeNode.nil && !RBTreeNode.nil.equals(parentNode))
          {
            parentNode.setTreeNodeBLACK();
            parentNode.parent.setTreeNodeRED();
            rightRotate(parentNode.parent);
          }
        }
      }
      else
      {
        RBTreeNode parentLeftSibling = parentNode.parent.left;
        if(parentLeftSibling.isRed())
        {
          parentNode.setTreeNodeBLACK();
          parentLeftSibling.setTreeNodeBLACK();
          parentNode.parent.setTreeNodeRED();
          currNode = parentNode.parent;
          parentNode = currNode.parent;         
        }
        else 
        {
          if(currNode == parentNode.left)
          {
            currNode = parentNode;
            rightRotate(currNode);
            parentNode = currNode.parent;
          }
         
          if(parentNode != RBTreeNode.nil && !RBTreeNode.nil.equals(parentNode))
          {
            parentNode.setTreeNodeBLACK();
            parentNode.parent.setTreeNodeRED();
            leftRotate(parentNode.parent);
          }
        }
      }
    }
    // Root should be black after all rotations
    root.setTreeNodeBLACK();
  }
  
  private void leftRotate(RBTreeNode currNode)
  {
    if(currNode == RBTreeNode.nil || RBTreeNode.nil.equals(currNode))
      return;
    
    RBTreeNode rightChild = currNode.right;
    RBTreeNode parentNode = currNode.parent;
    if(rightChild == RBTreeNode.nil || RBTreeNode.nil.equals(rightChild))
      return;
    
    currNode.right = rightChild.left;
    if(rightChild.left != RBTreeNode.nil && !RBTreeNode.nil.equals(rightChild.left))
      rightChild.left.parent = currNode;
    rightChild.parent = currNode.parent;
    
    if(currNode.parent == RBTreeNode.nil || RBTreeNode.nil.equals(currNode.parent))
      root = rightChild;
    else if (currNode == parentNode.left && (parentNode != RBTreeNode.nil && !RBTreeNode.nil.equals(parentNode)))
      parentNode.left = rightChild;
    else if(parentNode != RBTreeNode.nil && !RBTreeNode.nil.equals(parentNode))
      parentNode.right = rightChild;    
    
    rightChild.left = currNode;
    currNode.parent = rightChild;
    
    rightChild.setSize(currNode.getSize());
    currNode.setSize(currNode.left.getSize() + currNode.right.getSize() + 1);
  }
  
  private void rightRotate(RBTreeNode currNode)
  {
    if(currNode == RBTreeNode.nil || RBTreeNode.nil.equals(currNode))
      return;
    
    RBTreeNode leftChild = currNode.left;
    RBTreeNode parentNode = currNode.parent;
    
    if(leftChild == RBTreeNode.nil || RBTreeNode.nil.equals(leftChild))
      return;
    
    currNode.left = leftChild.right;
    if(leftChild.right != RBTreeNode.nil && !RBTreeNode.nil.equals(leftChild.right))
      leftChild.right.parent = currNode;
    
    leftChild.parent = currNode.parent;
    
    if(currNode.parent == RBTreeNode.nil || RBTreeNode.nil.equals(currNode.parent))
      root = leftChild;
    else if(currNode == parentNode.left && (parentNode != RBTreeNode.nil && !RBTreeNode.nil.equals(parentNode)))
      parentNode.left = leftChild;
    else if(parentNode != RBTreeNode.nil && !RBTreeNode.nil.equals(parentNode))
      parentNode.right = leftChild;
    
    leftChild.right = currNode;
    currNode.parent = leftChild;
    
    leftChild.setSize(currNode.getSize());
    currNode.setSize(currNode.left.getSize() + currNode.right.getSize() + 1);
  }
  
  @SuppressWarnings("unchecked")
  public int delete(Comparable data)
  {
    RBTreeNode currNode = getNode(data);
    return delete(currNode);
  }
  
  public int delete(RBTreeNode currNode)
  {   
    if(currNode == RBTreeNode.nil || RBTreeNode.nil.equals(currNode))
    {
      System.out.println("Node Not Found");
      return 0;
    }
    
    RBTreeNode tempNode; // y
    RBTreeNode newNode;  // x
    
    if((currNode.left == RBTreeNode.nil || RBTreeNode.nil.equals(currNode.left))|| 
       (currNode.right == RBTreeNode.nil|| RBTreeNode.nil.equals(currNode.right)))
      tempNode = currNode;
    else
      tempNode = getTreeSuccessor(currNode);
    
    if(tempNode.left != RBTreeNode.nil && !RBTreeNode.nil.equals(tempNode.left))
      newNode = tempNode.left;
    else
      newNode = tempNode.right;
    
    newNode.parent = tempNode.parent;
    
    if(tempNode.parent == RBTreeNode.nil || RBTreeNode.nil.equals(tempNode.parent))
      root = newNode;
    else if(tempNode == tempNode.parent.left)
      tempNode.parent.left = newNode;
    else
      tempNode.parent.right = newNode;
    
    if(tempNode != currNode)
      currNode.data = tempNode.data;
    
    updateSizeOnDelete(tempNode);
    
    if(tempNode.isBlack() && (root != RBTreeNode.nil && !RBTreeNode.nil.equals(root)))
      deleteFixup(newNode);
    
    return 1;
  }
  
  private void updateSizeOnDelete(RBTreeNode node)
  {
    node = node.parent;
    while(node != RBTreeNode.nil && !RBTreeNode.nil.equals(node))
    {
      node.setSize(node.getSize() - 1);
      node = node.parent;
    }
  }
  
  private void deleteFixup(RBTreeNode currNode)
  {
    while(currNode != root && currNode.isBlack())
    {
      RBTreeNode parentNode = currNode.parent;
      if(currNode == parentNode.left)
      {
        RBTreeNode rightSiblingNode = parentNode.right;
        if(rightSiblingNode.isRed())
        {
          rightSiblingNode.setTreeNodeBLACK();
          parentNode.setTreeNodeRED();
          leftRotate(parentNode);
          
          parentNode = currNode.parent;
          rightSiblingNode = parentNode.right;
        }
        
        if(rightSiblingNode.left.isBlack() && rightSiblingNode.right.isBlack())
        {
          rightSiblingNode.setTreeNodeRED();
          currNode = parentNode;
        }
        else if(rightSiblingNode.right.isBlack())
        {
          rightSiblingNode.left.setTreeNodeBLACK();
          rightSiblingNode.setTreeNodeRED();
          rightRotate(rightSiblingNode);
          
          parentNode = currNode.parent;
          rightSiblingNode = parentNode.right;
        }
        else
        {
          if(parentNode.isBlack()) 
              rightSiblingNode.setTreeNodeBLACK();
          else 
            rightSiblingNode.setTreeNodeRED();
          
          parentNode.setTreeNodeBLACK();
          rightSiblingNode.right.setTreeNodeBLACK();
          leftRotate(parentNode);
          currNode = root;          
        }
        
      }
      else
      {
        RBTreeNode leftSiblingNode = parentNode.left;
        if(leftSiblingNode.isRed())
        {
          leftSiblingNode.setTreeNodeBLACK();
          parentNode.setTreeNodeRED();
          rightRotate(parentNode);
          
          parentNode = currNode.parent;
          leftSiblingNode = parentNode.left;
        }
        
        if(leftSiblingNode.right.isBlack() && leftSiblingNode.left.isBlack())
        {
          leftSiblingNode.setTreeNodeRED();
          currNode = parentNode;
        }
        else if(leftSiblingNode.left.isBlack())
        {
          leftSiblingNode.right.setTreeNodeBLACK();
          leftSiblingNode.setTreeNodeRED();
          leftRotate(leftSiblingNode);
          
          parentNode = currNode.parent;
          leftSiblingNode = parentNode.left;
        }
        else
        {
          if(parentNode.isBlack()) 
              leftSiblingNode.setTreeNodeBLACK();
          else 
            leftSiblingNode.setTreeNodeRED();
          
          parentNode.setTreeNodeBLACK();
          leftSiblingNode.left.setTreeNodeBLACK();
          rightRotate(parentNode);
          currNode = root;          
        }       
      }
    }
    currNode.setTreeNodeBLACK();
  }
  
  @SuppressWarnings("unchecked")
  private RBTreeNode getNode(Comparable data)
  {
    RBTreeNode currNode = root;
    while(currNode != RBTreeNode.nil && !RBTreeNode.nil.equals(currNode))
    {
      if(currNode.data.compareTo(data) == 0)
        break;
      else if(data.compareTo(currNode.data) < 0)
        currNode = currNode.left;
      else
        currNode = currNode.right;
    }
    return currNode;
  }
  
  public long getCount()
  {
    return root.getSize();
  }
  
  public Comparable getRankElement(RBTreeNode currNode, long rank) 
  {
    RBTreeNode targetNode = getRankElementNode(currNode, rank);
    if(targetNode != null)
      return targetNode.getData();
    else if(targetNode == RBTreeNode.nil || RBTreeNode.nil.equals(targetNode))
      return null;
    else 
      return null;
  }
  
  private RBTreeNode getRankElementNode(RBTreeNode currNode, long rank)
  {
    if(currNode == RBTreeNode.nil || RBTreeNode.nil.equals(currNode))
    {
      return null;
    }
    
    long currRank = currNode.left.getSize() + 1;
    if(rank == currRank)
    {
      return currNode;
    }
    else if(rank < currRank)
    {
      return getRankElementNode(currNode.left, rank);
    }
    else
    {
      return getRankElementNode(currNode.right, rank - currRank);
    }
    
  }
  
  public Comparable getPercentageRankElement(RBTreeNode currNode, int pRank)
  {
    RBTreeNode targetNode = getPercentageRankElementNode(currNode, pRank);
    if(targetNode != null)
      return targetNode.getData();
    else if(targetNode == RBTreeNode.nil || RBTreeNode.nil.equals(targetNode))
      return null;
    else 
      return null;    
  }
  
  private RBTreeNode getPercentageRankElementNode(RBTreeNode currNode, int pRank)
  {
    if(pRank < 1 || pRank > 100)
    {
      LogUtil.warning(LoggerType.TRACE, "Invalid value : "+ pRank + 
          ".Enter values between 1 to 100 for percentage.");
      return null;
    }
    
    long size = currNode.getSize();
    long rank = (long)Math.ceil(pRank*0.01*size);
    return getRankElementNode(currNode, rank);
  }
  
  private RBTreeNode getTreeSuccessor(RBTreeNode currNode)
  {
    if(currNode.right != RBTreeNode.nil && !RBTreeNode.nil.equals(currNode.right))
      return getTreeMinimum(currNode.right);
    
    RBTreeNode parentNode = currNode.parent;
    while((parentNode != RBTreeNode.nil && !RBTreeNode.nil.equals(parentNode)) && currNode == parentNode.right)
    {
      currNode = parentNode;
      parentNode = currNode.parent;
    }
    return parentNode;
  }
  
  private RBTreeNode getTreeMinimum(RBTreeNode currNode)
  {
    while(currNode.left != RBTreeNode.nil && !RBTreeNode.nil.equals(currNode.left))
      currNode = currNode.left;
    return currNode;
  }
  
  public Object clone()
  {
    RBTree myClone = new RBTree();
    myClone.root = (RBTreeNode)this.root.copy(RBTreeNode.nil);
    return myClone;
  }

  @Override
  public void writeExternal(ObjectOutput out) throws IOException
  {
    out.writeObject(root);
  }

  @Override
  public void readExternal(ObjectInput in) throws IOException,
      ClassNotFoundException
  {
    root = (RBTreeNode) in.readObject();    
  }
}
