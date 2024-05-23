package oracle.cep.test.statistics;

import oracle.cep.statistics.IStats;

public class WriterQueueStat implements IStats
{

  public WriterQueueStat(int id, int opId, int msgsPres, int positivePres,
                         int negativePres, int hbPres, int msgs,
                         int positive, int negative, int hb, long lastenq,
                         long lastpos, long lastneg, long lasthb)
  {
    System.out.println("queueId : "+id);
    System.out.println("operatorId : "+opId);
    System.out.println("messagesPresent : "+ msgsPres);
    System.out.println("positivePresent : "+  positivePres);
    System.out.println("negativePresent : "+  negativePres);
    System.out.println("heartbeatsPresent : "+ hbPres);
    System.out.println("messagesEnqueued : "+  msgs);
    System.out.println("positiveEnqueued : "+  positive);
    System.out.println("negativeEnqueued : "+  negative);
    System.out.println("heartbeatsEnqueued : "+  hb);
    System.out.println("lastEnqueued : "+  lastenq);
    System.out.println("lastPositiveEnqueued : "+ lastpos);
    System.out.println("lastNegativeEnqueued : "+ lastneg);
    System.out.println("lastHeatbeatEnqueued : "+ lasthb);

  }
  
}
