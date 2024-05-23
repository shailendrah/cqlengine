package oracle.cep.test.statistics;

import oracle.cep.statistics.IStats;

public class SystemStat implements IStats
{
  public SystemStat(long free, long max, long tim, long total, long used, int num)
  {
    System.out.println("freeMemory : "+ free);
    System.out.println("maxMemory : "+ max);
    System.out.println("time : "+ tim);
    System.out.println("totalMemory : "+ total);
    System.out.println("usedMemory : "+ used);
    System.out.println("numOfThreads : "+ num);
  }
  
}
