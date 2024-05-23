package oracle.cep.demo.pattern;

import java.util.List;

import oracle.cep.dataStructures.external.AttributeValue;
import oracle.cep.dataStructures.external.TupleValue;
import oracle.cep.dataStructures.internal.QueueElement;
import com.oracle.osa.exceptions.CEPException;
import oracle.cep.interfaces.output.QueryOutputBase;

public class OutputBean extends QueryOutputBase
{
   public void putNext(TupleValue tv, QueueElement.Kind k) 
     throws CEPException
   {
     assert eprArgs.length == 1 : eprArgs.length;
     
     System.out.println(tv.toSimpleString());
     if (eprArgs[0].equalsIgnoreCase("ATime"))
     {
        DemoServlet.getInstance().addTime(tv);
     } else if (eprArgs[0].equalsIgnoreCase("Input"))
     {
       DemoServlet.getInstance().addInput(tv);
     }
   }

  public void end() throws CEPException
  {
  }

  public void start() throws CEPException
  {
  }
}
