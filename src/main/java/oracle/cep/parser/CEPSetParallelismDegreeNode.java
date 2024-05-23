/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/parser/CEPSetParallelismDegreeNode.java /main/2 2011/05/19 15:28:46 hopark Exp $ */

/* Copyright (c) 2011, Oracle and/or its affiliates. All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    hopark      04/21/11 - make public to be reused in cqservice
    sborah      03/17/11 - add parallelism degree
    sborah      03/17/11 - Creation
 */

/**
 *  @version $Header: CEPSetParallelismDegreeNode.java 17-mar-2011.07:03:08 sborah   Exp $
 *  @author  sborah  
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.parser;

public class CEPSetParallelismDegreeNode implements CEPParseTreeNode 
{

  /** The name of the stream or relation */
  private String   name;
  
  private int      parallelismDegree;

  private boolean  isStream;
  
  private int      startOffset;
  
  private int      endOffset;
  
  public CEPSetParallelismDegreeNode(CEPStringTokenNode name, Integer parallelismDegree, 
                              boolean isStream)
  {
    this.name              = name.getValue();
    this.parallelismDegree = parallelismDegree;
    this.isStream          = isStream;
    
    setStartOffset(name.getStartOffset());
    setEndOffset(name.getEndOffset());
  }

  public int getStartOffset()
  {
    return startOffset;
  }

  public void setStartOffset(int startOffset)
  {
    this.startOffset = startOffset;
  }

  public int getEndOffset()
  {
    return endOffset;
  }

  public void setEndOffset(int endOffset)
  {
    this.endOffset = endOffset;
  }

  public String getName()
  {
    return name;
  }

  public boolean isStream()
  {
    return isStream;
  }

  public int getParallelismDegree()
  {
    return parallelismDegree;
  }
  
}