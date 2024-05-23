/* $Header: pcbpel/cep/server/src/oracle/cep/util/DAGHelper.java /main/2 2009/02/23 06:47:36 sborah Exp $ */

/* Copyright (c) 2006, 2009, Oracle and/or its affiliates.
All rights reserved. */

/*
   DESCRIPTION
    Utility class related to Directed Acyclic Graphs (DAG)

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    sborah      12/17/08 - handle constants
    anasrini    03/24/06 - bug fix 
    anasrini    03/23/06 - fix bug 
    anasrini    03/20/06 - Creation
    anasrini    03/20/06 - Creation
    anasrini    03/20/06 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/util/DAGHelper.java /main/2 2009/02/23 06:47:36 sborah Exp $
 *  @author  anasrini
 *  @since   1.0
 */

package oracle.cep.util;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Utility class related to Directed Acyclic Graphs (DAG)
 *
 * @since 1.0
 */

public class DAGHelper {
  
  private static enum DFSVisitState {
    UNDISCOVERED, DISCOVERED, FINISHED;
  }

  private static enum Direction {
    FORWARD, REVERSE;
  }

  private static class DFSState {

    /** the DFS visit status of the vertex */
    DFSVisitState c;

    /** the discovery time */
    int d;
    
    /** the finish time */
    int f;
  }

  private static class DFSContext {
    /** Map of node->DFSState */
    HashMap<DAGNode, DFSState> stateMap;

    /** time */
    int time;

    /** Direction of traversal */
    Direction dir;

    /** Linked List of DAGNodes in the order that they finish */
    LinkedList<DAGNode> finishOrder;

    /** Linked List of DAGNodes in the order that they are discovered */
    LinkedList<DAGNode> discoverOrder;

    DFSContext() {
      stateMap      = new HashMap<DAGNode, DFSState>();
      time          = 0;
      dir           = Direction.FORWARD;
      finishOrder   = new LinkedList<DAGNode>();
      discoverOrder = new LinkedList<DAGNode>();
    }
  }


  /**
   * Return the set of nodes of the inpur DAG in ascending topological
   * sorted order. That is, if there is a directed (u,v) path in the DAG,
   * then u appears before v in the sorted order.
   * <p>
   * In this case, the input DAG is represented by a sink node v with the
   * property that for every node u of the DAG, there is a directed (u,v) 
   * path in the DAG
   * <p>
   * An implication of this is that there is no other sink other than v in 
   * the DAG
   * @param sink a sink node with the property that for every node u of 
   *             the DAG, there is a directed (u,v) path in the DAG
   * @return array of vertices of the DAG in ascending topologically sorted
   *         order
   */
  public static ArrayList<DAGNode> getTopologicalSort(DAGNode sink) {

    DFSContext ctx = new DFSContext();
    ctx.dir        = Direction.REVERSE;

    DFSVisit(sink, ctx);

    return new ArrayList<DAGNode>(ctx.finishOrder);
  }

  private static void DFSVisit(DAGNode u, DFSContext ctx) {

    List<DAGNode> nbrs;
    DFSState  s = new DFSState();
    DFSState  s1;

    ctx.stateMap.put(u, s);

    s.c = DFSVisitState.DISCOVERED;
    s.d = ++(ctx.time);
    ctx.discoverOrder.add(u);
    
    nbrs = getNeighbours(u, ctx.dir);
    if (nbrs != null) {
      for(DAGNode v : nbrs) {
        s1 = ctx.stateMap.get(v);
        if (s1 == null || s1.c == DFSVisitState.UNDISCOVERED)
          DFSVisit(v, ctx);
      }
    }

    s.c = DFSVisitState.FINISHED;
    s.f = ++(ctx.time);
    ctx.finishOrder.add(u);
  }

  private static List<DAGNode> getNeighbours(DAGNode u, Direction dir) {
    if (dir == Direction.FORWARD)
      return u.getOutNeighbours();
    else if (dir == Direction.REVERSE)
    {
      DAGNode[] nodes = u.getInNeighbours();
      if(nodes != null)
        return Arrays.asList(u.getInNeighbours());
      else
        return null;
    }
      
   

    // Should not come here
    assert false;
    return null;
  }
  
}
