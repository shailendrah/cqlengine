package oracle.cep.test.embedded;

import oracle.cep.dataStructures.external.TupleValue;
import oracle.cep.dataStructures.internal.QueueElement;
import oracle.cep.exceptions.CEPException;
import oracle.cep.interfaces.output.QueryOutputBase;
import oracle.cep.service.ExecContext;

public class OutputDestination extends QueryOutputBase
{
   public OutputDestination(ExecContext ec)
   {
     super(ec);
   }
   
   public void putNext(TupleValue tv, QueueElement.Kind k) throws CEPException
   {
     int c1 = tv.iValueGet(0);
     int c2 = tv.iValueGet(1);
     System.out.println("c1="+c1 + "," + "c2="+c2);
     
   }
   public void start(){}
   public void end(){}
}
