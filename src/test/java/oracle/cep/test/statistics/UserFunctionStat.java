package oracle.cep.test.statistics;

import oracle.cep.statistics.IStats;

public class UserFunctionStat implements IStats
{
   public UserFunctionStat(String name, int fnId, boolean is, String txt,
                                 String fn, int num, long tim)
   {
     System.out.println("functionName : "+ name);
     System.out.println("functionId : "+ fnId);
     System.out.println("isAggregate : "+ is);
     System.out.println("text : "+ txt);
     System.out.println("mappingFunction : "+fn);
     System.out.println("numInvokations : "+ num);
     System.out.println("time : "+tim);
   }
   
}
