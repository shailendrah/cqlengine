/* $Header: pcbpel/cep/server/src/oracle/cep/util/DAGNode.java /main/2 2009/02/23 06:47:36 sborah Exp $ */

/* Copyright (c) 2006, 2009, Oracle and/or its affiliates.
All rights reserved. */

/*
   DESCRIPTION
    Utiltity interface related to DAGs, represents a vertex of a DAG

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    sborah      12/17/08 - handle constants
    anasrini    03/20/06 - Creation
    anasrini    03/20/06 - Creation
    anasrini    03/20/06 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/util/DAGNode.java /main/2 2009/02/23 06:47:36 sborah Exp $
 *  @author  anasrini
 *  @since   1.0
 */

package oracle.cep.util;

import java.util.ArrayList;

/**
 * Utiltity interface related to DAGs, represents a vertex of a DAG
 *
 * @since 1.0
 */

public interface DAGNode {

  /**
   * Get the set of vertices who are the out neighbours of this vertex.
   * For this vertex v, these are the set of vertices u such that (v,u)
   * is a directed edge
   * @return null if out neighbour set is empty (a sink vertex),
   *         else the set of out neighbours as an array
   */
  public ArrayList<DAGNode> getOutNeighbours();

  /**
   * Get the set of vertices who are the in neighbours of this vertex.
   * For this vertex v, these are the set of vertices u such that (u,v)
   * is a directed edge
   * @return null if in neighbour set is empty (a source vertex),
   *         else the set of in neighbours as an array
   */
  public DAGNode[] getInNeighbours();

  /**
   * Get the number of out neighbours
   * @return  the number of out neighbours
   */
  public int getOutDegree();

  /**
   * Get the number of in neighbours
   * @return  the number of in neighbours
   */
  public int getInDegree();
}
