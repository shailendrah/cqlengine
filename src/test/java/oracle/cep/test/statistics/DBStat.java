package oracle.cep.test.statistics;

import oracle.cep.statistics.IStats;

public class DBStat implements IStats
{
  public DBStat(String loc, long cache, long log, long misses, int requests)
  {
    System.out.println("location : " +loc);
    System.out.println("cacheSize : " +cache);
    System.out.println("logSize : " +log);
    System.out.println("cacheMisses : " +misses);
    System.out.println("requests : " +requests);
  }
  
}
