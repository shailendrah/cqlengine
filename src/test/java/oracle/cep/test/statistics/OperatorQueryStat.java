package oracle.cep.test.statistics;

import oracle.cep.statistics.IStats;

public class OperatorQueryStat implements IStats 
{
  public OperatorQueryStat(int opId, int qid)
  {
    System.out.println("opId : "+ opId);
    System.out.println("qid : "+ qid);
  }
}
