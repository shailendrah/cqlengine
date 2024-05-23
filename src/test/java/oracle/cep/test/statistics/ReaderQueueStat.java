package oracle.cep.test.statistics;

import oracle.cep.statistics.IStats;

public class ReaderQueueStat implements IStats 
{
  public ReaderQueueStat(int id, int opId, int rId, int dequeued, 
                         int posDequeued, int negDequeued, int hbDequeued,
                         int present, int posPresent, int negPresent, 
                         int hbPresent, long tsLast, long tsLastPos,
                         long tsLastNeg, long tsLasthb)
  {
    System.out.println("queueId : "+ id);
    System.out.println("execOperatorId : "+ opId);
    System.out.println("readerId : "+  rId);
    System.out.println("numDequeued : "+  dequeued);
    System.out.println("numPosDequeued : "+  posDequeued);
    System.out.println("numNegDequeued : "+ negDequeued);
    System.out.println("numHeartbeatsDequeued : "+  hbDequeued);
    System.out.println("numPresent : "+  present);
    System.out.println("numPosPresent : "+  posPresent);
    System.out.println("numNegPresent : "+  negPresent);
    System.out.println("numHeartbeatsPresent : "+  hbPresent);
    System.out.println("tsLastDequeued : "+ tsLast);
    System.out.println("tsLastPosDequeued : "+ tsLastPos);
    System.out.println("tsLastNegDequeued : "+ tsLastNeg);
    System.out.println("tsLastHeartbeatDequeued : "+ tsLasthb);
  }

}
