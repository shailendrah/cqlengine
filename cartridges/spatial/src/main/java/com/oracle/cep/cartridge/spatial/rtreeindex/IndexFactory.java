package com.oracle.cep.cartridge.spatial.rtreeindex;

import oracle.cep.extensibility.indexes.IIndex;
import oracle.cep.extensibility.indexes.IIndexTypeFactory;

public class IndexFactory implements IIndexTypeFactory
{
  static IndexFactory s_factory = null;
  
  public static synchronized IndexFactory getInstance()
  {
    if (s_factory == null)
    {
      s_factory = new IndexFactory();
    }
    return s_factory;
  }
  
  public IIndex create(Object[] args)
  {
    return new RTreeIndex();
  }

  public void drop(IIndex index)
  {
  }
}