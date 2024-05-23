package oracle.cep.test.statistics;

import oracle.cep.statistics.IStats;

public class StreamStat implements IStats
{
  public StreamStat(int id, int opId, String name, boolean is, String txt, 
                    long numIn, long start, long end, long numInLatest, 
                    float avg, float rate, float per, boolean ispush,
                    long mem, long disk, float hit)
  {
    System.out.println("streamId : "+ id);
    System.out.println("operatorId : "+ opId);
    System.out.println("streamName : "+ name);
    System.out.println("isStream : "+ is);
    System.out.println("text : "+ txt);
    System.out.println("numInMessages : "+ numIn);
    System.out.println("startTime : "+ start);
    System.out.println("endTime : "+ end);
    System.out.println("numInMessagesLatest : "+ numInLatest);
    System.out.println("avgLatency : "+ avg);
    System.out.println( "inputRate : "+ rate);
    System.out.println("percent : "+ per);
    System.out.println("isPushSrc : "+ispush);
    System.out.println("totalTuplesInMemory : "+mem);
    System.out.println("totalTuplesOnDisk : "+disk);
    System.out.println("hitRatio : "+ hit);
  }

}
