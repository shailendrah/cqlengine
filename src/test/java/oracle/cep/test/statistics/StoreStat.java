package oracle.cep.test.statistics;

import oracle.cep.statistics.IStats;

public class StoreStat implements IStats
{
  public StoreStat(int id, int execId, int numElems)
  {
    System.out.println("storeId : "+ id);
    System.out.println("execStoreId : "+ execId);
    System.out.println("numElems : "+ numElems);
  }

}

