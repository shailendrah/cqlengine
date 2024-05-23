package oracle.cep.test.statistics;

import oracle.cep.statistics.IStats;

public class OperatorStat implements IStats 
{
  public OperatorStat(int id, int phyId, int qid, long out, long in, 
                      long executions,long time,long start, long end, 
                      long inLatest,long outLatest, String typ, String name, 
                      float per, long cmiss, long chit, String cname, boolean cached,long pstmtTTime, long pstmtTExec)
  {
    System.out.println("operatorId : "+ id);
    System.out.println("phyOperatorId : "+ phyId);
    System.out.println("queueId : "+ qid);
    System.out.println("numOutMessages : "+ out);
    System.out.println("numInMessages : "+ in);
    System.out.println("numExecutions : "+ executions);
    System.out.println("totalTime : "+ time);
    System.out.println("startTime : "+ start);
    System.out.println("endTime : "+ end);
    System.out.println("numInMessagesLatest : "+ inLatest);
    System.out.println("numOutMessagesLatest : "+ outLatest);
    System.out.println("opttype : "+ typ);
    System.out.println("optName : "+ name);
    System.out.println("percent : "+ per);
    System.out.println("cmiss : "+ cmiss);
    System.out.println("chit : "+ chit);
    System.out.println("cname : "+ cname);
    System.out.println("cached : "+ cached);
    System.out.println("pstmtTTime : "+ pstmtTTime);
    System.out.println("pstmtTExec : "+ pstmtTExec);
  }
  
}
