package oracle.cep.test.statistics;

import oracle.cep.statistics.IStats;

public class MemoryStat implements IStats
{
  public MemoryStat(String type, float hr)
  {
    System.out.println("objectType : " +type);
    System.out.println("hitRatio : " +hr);
  }

}
