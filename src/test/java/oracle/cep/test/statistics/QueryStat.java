package oracle.cep.test.statistics;

import oracle.cep.statistics.IStats;

public class QueryStat implements IStats
{
  public QueryStat(int id, String txt, String name, boolean isMetadata, boolean isInternal, long numOut, 
                   long start, long end, long latest, long executions, 
                   long time, float avg, float per, String orderingConstraint)
  {
    System.out.println("queryId : "+ id);
    System.out.println("queryTxt : "+ txt);
    System.out.println("objectName : "+ name);
    System.out.println("isView : "+ isMetadata);
    System.out.println("isInternal : " + isInternal);
    System.out.println("numOutMessages : "+ numOut);
    System.out.println("startTime : "+ start);
    System.out.println("endTime : "+ end);
    System.out.println("numOutMessagesLatest : "+ latest);
    System.out.println("numExecutions : "+executions);
    System.out.println("totalTime : "+ time);
    System.out.println("avgLatency : "+ avg);
    System.out.println("percent : "+ per);
    System.out.println("ordering constraint : "+ orderingConstraint);
  }
  
}
