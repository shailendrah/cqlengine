package com.oracle.cep.cartridge.spatial.rtreeindex;

import java.util.Iterator;

import oracle.spatial.util.RTree;

public class RuntimeContext
{
    RTree       rtree;
    
    @SuppressWarnings("rawtypes")
    Iterator    iterator;
    
    OpBase      op;
    int		    id;
    
    RuntimeContext(RTree rtree) 
    { 
      this.rtree = rtree;
    }
}
